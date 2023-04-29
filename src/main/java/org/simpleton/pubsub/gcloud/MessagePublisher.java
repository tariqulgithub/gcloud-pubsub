/**
 * 
 */
package org.simpleton.pubsub.gcloud;

/**
 * 
 * @author <a href="https://www.linkedin.com/in/tariqulislam">Tariqul Islam</a>
 * <br>
 * Email: <a href="mailto:sumon.screenfusion@gmail.com">sumon.screenfusion@gmail.com</a>
 * <br>
 * CreatedAt : Apr 30, 2023 12:39:22 AM
 *
 */
public interface MessagePublisher {
	
	public boolean publishMessage(MessageContainer messageContainer, GAccountCredentials gaccountCredentials);

}
