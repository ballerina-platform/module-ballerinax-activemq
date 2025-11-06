/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.activemq.util;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Test utility for producing messages to ActiveMQ queues and topics.
 * This is a simple producer implementation for testing purposes only.
 */
public final class TestProducer {

    private TestProducer() {
        // Prevent instantiation
    }

    /**
     * Creates a ConnectionFactory based on the broker URL.
     * For SSL URLs, creates an ActiveMQSslConnectionFactory with trust configuration.
     *
     * @param brokerUrl The broker URL
     * @return Configured ConnectionFactory
     * @throws Exception If SSL configuration fails
     */
    private static ActiveMQConnectionFactory createConnectionFactory(String brokerUrl) throws Exception {
        if (brokerUrl.startsWith("ssl://")) {
            ActiveMQSslConnectionFactory sslFactory = new ActiveMQSslConnectionFactory(brokerUrl);

            // For testing purposes, configure SSL to trust the self-signed server certificate
            // Load the server certificate from the truststore
            String trustStorePath = "./tests/resources/secrets/client-truststore.p12";
            String trustStorePassword = "password";

            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream is = new FileInputStream(trustStorePath)) {
                trustStore.load(is, trustStorePassword.toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            sslFactory.setTrustStore(trustStorePath);
            sslFactory.setTrustStorePassword(trustStorePassword);

            return sslFactory;
        } else {
            return new ActiveMQConnectionFactory(brokerUrl);
        }
    }

    /**
     * Send a text message to a queue.
     *
     * @param brokerUrl The broker URL (e.g., "tcp://localhost:61616")
     * @param queueName The queue name
     * @param message   The message text
     * @throws JMSException If message sending fails
     */
    public static void sendToQueue(BString brokerUrl, BString queueName, BString message) throws JMSException {
        sendToQueue(brokerUrl, queueName, message, null);
    }

    /**
     * Send a text message to a queue with properties.
     *
     * @param brokerUrl  The broker URL (e.g., "tcp://localhost:61616")
     * @param queueName  The queue name
     * @param message    The message text
     * @param properties Message properties (headers)
     * @throws JMSException If message sending fails
     */
    public static void sendToQueue(BString brokerUrl, BString queueName, BString message,
                                   BMap<BString, BString> properties) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            ActiveMQConnectionFactory connectionFactory = createConnectionFactory(brokerUrl.getValue());
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName.getValue());
            producer = session.createProducer(destination);

            TextMessage textMessage = session.createTextMessage(message.getValue());

            // Set properties if any
            if (properties != null) {
                for (BString key : properties.getKeys()) {
                    String value = properties.get(key).getValue();
                    textMessage.setStringProperty(key.getValue(), value);
                }
            }

            producer.send(textMessage);
        } catch (JMSException e) {
            throw e;
        } catch (Exception e) {
            throw new JMSException("Failed to create connection: " + e.getMessage());
        } finally {
            if (producer != null) {
                producer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Send a text message to a topic.
     *
     * @param brokerUrl The broker URL (e.g., "tcp://localhost:61616")
     * @param topicName The topic name
     * @param message   The message text
     * @throws JMSException If message sending fails
     */
    public static void sendToTopic(BString brokerUrl, BString topicName, BString message) throws JMSException {
        sendToTopic(brokerUrl, topicName, message, null);
    }

    /**
     * Send a text message to a topic with properties.
     *
     * @param brokerUrl  The broker URL (e.g., "tcp://localhost:61616")
     * @param topicName  The topic name
     * @param message    The message text
     * @param properties Message properties (headers)
     * @throws JMSException If message sending fails
     */
    public static void sendToTopic(BString brokerUrl, BString topicName, BString message,
                                   BMap<BString, Object> properties) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            ActiveMQConnectionFactory connectionFactory = createConnectionFactory(brokerUrl.getValue());
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName.getValue());
            producer = session.createProducer(destination);

            TextMessage textMessage = session.createTextMessage(message.getValue());

            // Set properties if any
            if (properties != null) {
                for (BString key : properties.getKeys()) {
                    Object value = properties.get(key);
                    textMessage.setObjectProperty(key.getValue(), value);
                }
            }

            producer.send(textMessage);
        } catch (JMSException e) {
            throw e;
        } catch (Exception e) {
            throw new JMSException("Failed to create connection: " + e.getMessage());
        } finally {
            if (producer != null) {
                producer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Send a byte message to a queue.
     *
     * @param brokerUrl The broker URL (e.g., "tcp://localhost:61616")
     * @param queueName The queue name
     * @param payload   The message payload as byte array
     * @throws JMSException If message sending fails
     */
    public static void sendBytesToQueue(BString brokerUrl, BString queueName, BArray payload) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            ActiveMQConnectionFactory connectionFactory = createConnectionFactory(brokerUrl.getValue());
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName.getValue());
            producer = session.createProducer(destination);

            jakarta.jms.BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(payload.getBytes());

            producer.send(bytesMessage);
        } catch (JMSException e) {
            throw e;
        } catch (Exception e) {
            throw new JMSException("Failed to create connection: " + e.getMessage());
        } finally {
            if (producer != null) {
                producer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
