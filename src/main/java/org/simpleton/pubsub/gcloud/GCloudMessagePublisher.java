/**
 * 
 */
package org.simpleton.pubsub.gcloud;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.json.JSONObject;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author <a href="https://www.linkedin.com/in/tariqulislam">Tariqul Islam</a>
 * <br>
 * Email: <a href="mailto:sumon.screenfusion@gmail.com">sumon.screenfusion@gmail.com</a>
 * <br>
 * CreatedAt : Apr 30, 2023 12:39:22 AM
 *
 */
@Slf4j
@ToString
@Getter
public class GCloudMessagePublisher implements MessagePublisher {
	
	private final ConcurrentHashMap<String, Publisher> publishers = new ConcurrentHashMap<>();
	
	private GCloudMessagePublisher(){}

    private static class SingletonHelper {
        private static final GCloudMessagePublisher INSTANCE = new GCloudMessagePublisher();
    }

    public static GCloudMessagePublisher getInstance() {
        return SingletonHelper.INSTANCE;
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		String credentialString =  "";//credential json payload
		 
		JSONObject messageBody = new JSONObject();
		
		messageBody.put("Hello", "Hello From Inline");
		
		GAccountCredentials gAccountCredentials = GAccountCredentials.builder().accountCredential(credentialString).projectId("projectId").topic("myTopic").build();
		
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
		for(int idx = 0 ; idx < 10 ; idx ++) {
			executor.schedule(() -> {
				GCloudMessagePublisher publisher = GCloudMessagePublisher.getInstance();
				publisher.publishMessage(MessageContainer.builder().content(messageBody.toString()).build(), gAccountCredentials); 
				System.out.println(publisher.fetchSize()); 
			}, 100, TimeUnit.MILLISECONDS);
		}
		
		try { 
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.MINUTES);
			System.out.println("executor shutdown complete"); 
			Thread.sleep(70000); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		GCloudMessagePublisher publisher = GCloudMessagePublisher.getInstance();
		 
		publisher.shutdown();

	}
	
	private Publisher buildPublisher(GAccountCredentials gaccountCredentials) {
		
		GooglePubSubStubProviders googlePubSubStubProviders = new GooglePubSubStubProviders();
		
		Optional<Publisher> publisher = googlePubSubStubProviders.getPublisher(gaccountCredentials);
		
		if(publisher.isEmpty()) {
			throw new IllegalArgumentException("Could not build publiser. ");
		}
		
		return publisher.get();
		
	}
	
	private Publisher fetchPublisher(GAccountCredentials gaccountCredentials) {
		StringBuilder keyBuilder = new StringBuilder();
	 	
	 	keyBuilder.append(gaccountCredentials.getProjectId());
	 	keyBuilder.append(":");
	 	keyBuilder.append(gaccountCredentials.getTopic());
	 	
	 	String key = keyBuilder.toString();
	 	
	 	Publisher publisher = publishers.getOrDefault(key, null);
	 	
	 	if(null == publisher) {
	 		synchronized (GCloudMessagePublisher.class) {
	 			publisher = buildPublisher(gaccountCredentials);
		 		publishers.put(key, publisher); 
	 		}
	 	}
	 	
	 	return publisher;
	 	
	}
	
	public int fetchSize() {
		return this.publishers.size();
	}
	
	@Override
	public boolean publishMessage(MessageContainer messageContainer, GAccountCredentials gaccountCredentials) {
		
		
		boolean success = false;
		
		if(null == messageContainer) {
			throw new IllegalArgumentException("MessageContainer can not be null. ");
		}
		
		if(null == gaccountCredentials) {
			throw new IllegalArgumentException("GAccountCredentials can not be null. ");
		}
		
		if(gaccountCredentials.getAccountCredential() == null) {
			throw new IllegalArgumentException("AccountCredential can not be null. ");
		}
		
		if(gaccountCredentials.getProjectId() == null) {
			throw new IllegalArgumentException("ProjectId can not be null. ");
		}
		
		if(gaccountCredentials.getTopic() == null) {
			throw new IllegalArgumentException("Topic can not be null. ");
		}
		
		try { 
			
				Publisher publisher = fetchPublisher(gaccountCredentials);
			
			 	ByteString data = ByteString.copyFromUtf8(messageContainer.getContent());
		        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
		        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage); 
		        String messageId = messageIdFuture.get();
		        if(null != messageId 
		        		&& !messageId.isEmpty()) {
		        	success = true;
		        }
		        ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {
		        	   public void onSuccess(String messageId) {
		        	     log.info("published with message id: " + messageId); 
		        	   }

		        	   public void onFailure(Throwable t) {
		        		 log.error("failed to publish: {}", t);    
		        	   }
		        	 }, MoreExecutors.directExecutor());
			
		} catch (Exception e) { 
			log.error("Error {}", e); 
		}
		
		return success;
	}
	
	@PreDestroy
	public void shutdown() {
		for (var entry : publishers.entrySet()) { 
			try {
				entry.getValue().shutdown();
				entry.getValue().awaitTermination(1, TimeUnit.MINUTES);
			} catch (Exception e) {
				log.error("Error {}", e); 
			}
		}
	}

}
