/**
 * 
 */
package org.simpleton.pubsub.gcloud;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ProjectTopicName;

import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@Slf4j
public class GooglePubSubStubProviders {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
	
	private Optional<CredentialsProvider> getCredentialsProvider(GAccountCredentials gaccountCredentials) {
		  	CredentialsProvider credentialsProvider = null;
	        try {
	            credentialsProvider = FixedCredentialsProvider
	                    .create(ServiceAccountCredentials
	                            .fromStream(new ByteArrayInputStream(StandardCharsets.UTF_8.encode(gaccountCredentials.getAccountCredential()).array())));
	        } catch (Exception e) {
	           log.error("Error {}",e); 
	        }
	        return Optional.ofNullable(credentialsProvider); 
	}
	
	public Optional<Publisher> getPublisher(GAccountCredentials gaccountCredentials) {
        Optional<CredentialsProvider> credentialsProvider = getCredentialsProvider(gaccountCredentials);
        if(credentialsProvider.isEmpty()) {
        	return Optional.empty();
        }
        ProjectTopicName topic = ProjectTopicName.of(gaccountCredentials.getProjectId(), gaccountCredentials.getTopic());
        try { 
            return Optional.ofNullable(Publisher.newBuilder(topic)
                    .setCredentialsProvider(credentialsProvider.get())
                    .build());
        } catch (Exception e) {
        	log.error("Error {}",e); 
        }
        
		return Optional.empty();
    }
	

}
