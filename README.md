# GCloud message publishing helper library
This will help you to publish message to goolge pubsub topic with credentials.

Setup
-----
* Clone Repository.

        git clone https://github.com/tariqulgithub/gcloud-pubsub.git
        
* Compile and Install.

        mvn clean install
        
Dependency Setup
---------------

        <dependency>
           <groupId>org.simpleton</groupId>
           <artifactId>gcloud-pubsub</artifactId>
           <version>1.0.0</version>
        </dependency>

Usage Example Code
------------------

        String credentialString =  "";
        JSONObject messageBody = new JSONObject();
        messageBody.put("Hello", "Hello From Inline");
        GAccountCredentials gAccountCredentials = GAccountCredentials.builder().accountCredential(credentialString).projectId("projectId").topic("myTopic").build();
        GCloudMessagePublisher publisher = GCloudMessagePublisher.getInstance();
        publisher.publishMessage(MessageContainer.builder().content(messageBody.toString()).build(), gAccountCredentials); 
        publisher.shutdown();
