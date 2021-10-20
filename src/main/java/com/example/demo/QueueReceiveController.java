package com.example.demo;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

@Component
public class QueueReceiveController {
    @Autowired
    private JmsTemplate jmsTemplate;
    private static final String QUEUE_NAME = "indy-in";
    private static final String QUEUE_NAME_SEND = "serviceout";
    
    private final Logger logger = LoggerFactory.getLogger(QueueReceiveController.class);

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(User user) {
        logger.info("Received message: {}", user.getName());
        sendDataToIbmMQ(user.getName());
    }
    
    public void sendMessageToAzure(String message)
    {
    	String connectionString = "Endpoint=sb://apicentrics.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=f0j9monM6TRW+QUS+709zVqWcjAB8zaFpbAR/Tk2uJc=Endpoint=sb://apicentrics.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=f0j9monM6TRW+QUS+709zVqWcjAB8zaFpbAR/Tk2uJc=";
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(QUEUE_NAME_SEND)
                .buildClient();
    
        senderClient.sendMessage(new ServiceBusMessage(message));
        System.out.println("Sent a single message to the queue: " + QUEUE_NAME_SEND);        
    }
   

    public void sendDataToIbmMQ(String data) {
		 Connection connection = null;
		 Session session = null;
		 Destination destination = null;
		 MessageProducer producer = null;
		 try {

			 JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
			 JmsConnectionFactory cf = ff.createConnectionFactory();

				/*
				 * System.setProperty("javax.net.ssl.keyStore", "key.jks" );
				 * System.setProperty("javax.net.ssl.keyStorePassword","Mule12345" );
				 * System.setProperty("javax.net.ssl.trustStore", "key.jks");
				 * System.setProperty("javax.net.ssl.trustStorePassword", "Mule12345");
				 * cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SPEC,
				 * "TLS_RSA_WITH_AES_128_CBC_SHA256");
				 * cf.setStringProperty(WMQConstants.WMQ_CHANNEL, "TEST.CONN.SVRCONN");
				 * cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE,
				 * WMQConstants.WMQ_CM_CLIENT); cf.setStringProperty(WMQConstants.WMQ_HOST_NAME,
				 * "ngridqm-9f57.qm.us-south.mq.appdomain.cloud");
				 * cf.setIntProperty(WMQConstants.WMQ_PORT, 32545);
				 * cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, "ngridqm");
				 * cf.setStringProperty(WMQConstants.USERID, "katta");
				 * cf.setStringProperty(WMQConstants.PASSWORD,
				 * "CbU1SZna0_HjAWxj6CV18QQmIXDpXm1mkd-_3UweWvzr");
				 * 
				 * connection = cf.createConnection(); session = connection.createSession(false,
				 * Session.AUTO_ACKNOWLEDGE); destination =
				 * session.createQueue("queue:///demoinbound");
				 */

			  cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, "ngridqm-9f57.qm.us-south.mq.appdomain.cloud"); 
			  cf.setIntProperty(WMQConstants.WMQ_PORT, 32545);
			  cf.setStringProperty(WMQConstants.WMQ_CHANNEL, "INDYCHANNEL");
			  cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
			  cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, "ngridqm");
			  cf.setStringProperty(WMQConstants.USERID, "katta");
			  cf.setStringProperty(WMQConstants.PASSWORD, "CbU1SZna0_HjAWxj6CV18QQmIXDpXm1mkd-_3UweWvzr");
   			  cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
   			  
			  connection = cf.createConnection();
			  session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			  destination = session.createQueue("queue:///servicein");

			  producer = session.createProducer(destination);
			  TextMessage message = session.createTextMessage(data);
			  connection.start();
			  producer.send(message);

			  }catch (JMSException jmsex) {
				  jmsex.printStackTrace();
			  }
		 }

}