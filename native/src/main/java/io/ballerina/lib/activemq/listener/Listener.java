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

package io.ballerina.lib.activemq.listener;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import static io.ballerina.lib.activemq.util.ActiveMQConstants.ACTIVEMQ_ERROR;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.CERT;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.KEY;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PROPERTIES;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.QUERY_PARAM_EXCLUSIVE_CONSUMER;
import static io.ballerina.lib.activemq.util.CommonUtils.createError;
import static io.ballerina.lib.activemq.util.CommonUtils.getAcknowledgementMode;
import static io.ballerina.lib.activemq.util.SslUtils.getKeyManagers;
import static io.ballerina.lib.activemq.util.SslUtils.getTrustmanagers;

/**
 * Native class for the Ballerina ActiveMQ Listener.
 *
 * @since 0.1.0
 */
public final class Listener {
    static final String NATIVE_CONNECTION = "native.connection";
    static final String NATIVE_SERVICE_LIST = "native.service.list";
    static final String NATIVE_SERVICE = "native.service";
    static final String NATIVE_RECEIVER = "native.receiver";
    static final String LISTENER_STARTED = "listener.started";
    static final String DURABLE = "DURABLE";

    private Listener() {
    }

    @SuppressWarnings("unchecked")
    public static Object init(BObject bListener, BString url, BMap<BString, Object> configurations) {
        try {
            String brokerURL = url.getValue();
            ConnectionConfig config = new ConnectionConfig(configurations);
            ActiveMQConnectionFactory factory;

            // Configure SSL/TLS if secureSocket is provided
            if (Objects.nonNull(config.secureSocket())) {
                ActiveMQSslConnectionFactory sslFactory = new ActiveMQSslConnectionFactory(brokerURL);
                BMap<BString, Object> secureSocket = config.secureSocket();
                Object bCert = secureSocket.get(CERT);
                BMap<BString, BString> keyRecord = (BMap<BString, BString>) secureSocket.getMapValue(KEY);
                // Create KeyManagers and TrustManagers
                KeyManager[] keyManagers = getKeyManagers(keyRecord);
                TrustManager[] trustManagers = getTrustmanagers(bCert);
                sslFactory.setKeyAndTrustManagers(keyManagers, trustManagers, new SecureRandom());
                factory = sslFactory;
            } else {
                factory = new ActiveMQConnectionFactory(brokerURL);
            }

            if (Objects.nonNull(config.username()) && Objects.nonNull(config.password())) {
                factory.setUserName(config.username());
                factory.setPassword(config.password());
            }
            factory.setClientID(config.clientId());

            factory.setOptimizeAcknowledge(config.optimizeAcknowledgements());
            factory.setAlwaysSessionAsync(config.setAlwaysSessionAsync());

            if (config.prefetchPolicyConfig() != null) {
                factory.setPrefetchPolicy(generatePrefetchPolicy(config.prefetchPolicyConfig()));
            }
            if (config.redeliveryPolicyConfig() != null) {
                factory.setRedeliveryPolicy(generateRedeliveryPolicy(config.redeliveryPolicyConfig()));
            }
            factory.setProperties(generateConnectionProperties(configurations));

            Connection connection = factory.createConnection();
            bListener.addNativeData(NATIVE_CONNECTION, connection);
            bListener.addNativeData(NATIVE_SERVICE_LIST, new ArrayList<BObject>());
        } catch (Exception e) {
            return createError(ACTIVEMQ_ERROR, "Failed to initialize listener", e); // TODO: specific error is not shown
        }
        return null;
    }

    public static Object attach(Environment env, BObject bListener, BObject bService, Object name) {
        // TODO: break this method into smaller methods
        Connection connection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        Object started = bListener.getNativeData(LISTENER_STARTED);
        try {
            Service.validateService(bService);
            Service nativeService = new Service(bService);
            ServiceConfig svcConfig = nativeService.getServiceConfig();

            int sessionAckMode = getAcknowledgementMode(svcConfig.ackMode());
            Session session = connection.createSession(sessionAckMode);
            MessageConsumer consumer = getConsumer(session, svcConfig);
            MessageDispatcher messageDispatcher = new MessageDispatcher(env.getRuntime(), nativeService, session);
            MessageReceiver receiver = new MessageReceiver(
                    session, consumer, messageDispatcher, svcConfig.pollingInterval(), svcConfig.receiveTimeout());
            bService.addNativeData(NATIVE_SERVICE, nativeService);
            bService.addNativeData(NATIVE_RECEIVER, receiver);
            List<BObject> serviceList = getBServices(bListener);
            serviceList.add(bService);
            if (Objects.nonNull(started) && ((Boolean) started)) {
                receiver.consume();
            }
        } catch (BError | JMSException e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(ACTIVEMQ_ERROR, String.format("Failed to attach service to listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object detach(BObject bService) {
        Object receiver = bService.getNativeData(NATIVE_RECEIVER);
        try {
            if (Objects.isNull(receiver)) {
                return createError(ACTIVEMQ_ERROR, "Could not find the native ActiveMQ message receiver");
            }
            ((MessageReceiver) receiver).stop();
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(ACTIVEMQ_ERROR,
                    String.format("Failed to detach a service from the listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object start(BObject bListener) {
        Connection connection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        List<BObject> bServices = getBServices(bListener);
        try {
            connection.start();
            for (BObject bService: bServices) {
                MessageReceiver receiver = (MessageReceiver) bService.getNativeData(NATIVE_RECEIVER);
                receiver.consume();
            }
            bListener.addNativeData(LISTENER_STARTED, Boolean.TRUE);
        } catch (JMSException e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(ACTIVEMQ_ERROR,
                    String.format("Error occurred while starting the Ballerina ActiveMQ listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object gracefulStop(BObject bListener) {
        Connection nativeConnection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        List<BObject> bServices = getBServices(bListener);
        try {
            for (BObject bService: bServices) {
                MessageReceiver receiver = (MessageReceiver) bService.getNativeData(NATIVE_RECEIVER);
                receiver.stop();
            }
            nativeConnection.stop();
            nativeConnection.close();
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(ACTIVEMQ_ERROR,
                    String.format(
                            "Error occurred while gracefully stopping the Ballerina ActiveMQ listener: %s", errorMsg),
                    e);
        }
        return null;
    }

    public static Object immediateStop(BObject bListener) {
        Connection nativeConnection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        List<BObject> bServices = getBServices(bListener);
        try {
            for (BObject bService: bServices) {
                MessageReceiver receiver = (MessageReceiver) bService.getNativeData(NATIVE_RECEIVER);
                receiver.stop();
            }
            nativeConnection.stop();
            nativeConnection.close();
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(ACTIVEMQ_ERROR,
                    String.format("Error occurred while immediately stopping the Ballerina ActiveMQ listener: %s",
                            errorMsg), e);
        }
        return null;
    }

    private static MessageConsumer getConsumer(Session session, ServiceConfig svcConfig)
            throws JMSException {
        MessageConsumer baseConsumer;
        if (svcConfig instanceof QueueConfig queueConfig) {
            String queueName = queueConfig.queueName();
            if (queueConfig.exclusive()) {
                queueName = queueName + QUERY_PARAM_EXCLUSIVE_CONSUMER;
            }
            Queue queue = session.createQueue(queueName);
            baseConsumer = session.createConsumer(queue, queueConfig.messageSelector());
        } else {
            TopicConfig topicConfig = (TopicConfig) svcConfig;
            String topicName = topicConfig.topicName();
            if (topicConfig.exclusive()) {
                topicName = topicName + QUERY_PARAM_EXCLUSIVE_CONSUMER;
            }
            Topic topic = session.createTopic(topicName);
            if (topicConfig.consumerType().equals(DURABLE)) {
                baseConsumer = session.createDurableSubscriber(
                        topic, topicConfig.subscriberName(), topicConfig.messageSelector(), topicConfig.noLocal());
            } else {
                baseConsumer = session.createConsumer(topic, topicConfig.messageSelector(), topicConfig.noLocal());
            }
        }
        ActiveMQMessageConsumer consumer = (ActiveMQMessageConsumer) baseConsumer;
        if (svcConfig.redeliveryPolicyConfig() != null) {
            consumer.setRedeliveryPolicy(generateRedeliveryPolicy(svcConfig.redeliveryPolicyConfig()));
        }
        return consumer;
    }

    private static ActiveMQPrefetchPolicy generatePrefetchPolicy(
            PrefetchPolicyConfig prefetchPolicyConfig) {
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setQueuePrefetch(prefetchPolicyConfig.queuePrefetchSize());
        prefetchPolicy.setTopicPrefetch(prefetchPolicyConfig.topicPrefetchSize());
        prefetchPolicy.setDurableTopicPrefetch(prefetchPolicyConfig.durableTopicPrefetchSize());
        prefetchPolicy.setOptimizeDurableTopicPrefetch(prefetchPolicyConfig.optimizeDurableTopicPrefetchSize());
        return prefetchPolicy;
    }

    private static RedeliveryPolicy generateRedeliveryPolicy(
            RedeliveryPolicyConfig redeliveryPolicyConfig) {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setCollisionAvoidancePercent(redeliveryPolicyConfig.collisionAvoidancePercent());
        redeliveryPolicy.setMaximumRedeliveries(redeliveryPolicyConfig.maximumRedeliveries());
        redeliveryPolicy.setMaximumRedeliveryDelay(redeliveryPolicyConfig.maximumRedeliveryDelay());
        redeliveryPolicy.setInitialRedeliveryDelay(redeliveryPolicyConfig.initialRedeliveryDelay());
        redeliveryPolicy.setUseCollisionAvoidance(redeliveryPolicyConfig.useCollisionAvoidance());
        redeliveryPolicy.setUseExponentialBackOff(redeliveryPolicyConfig.useExponentialBackOff());
        redeliveryPolicy.setBackOffMultiplier(redeliveryPolicyConfig.backOffMultiplier());
        redeliveryPolicy.setRedeliveryDelay(redeliveryPolicyConfig.redeliveryDelay());
        redeliveryPolicy.setPreDispatchCheck(redeliveryPolicyConfig.preDispatchCheck());
        return redeliveryPolicy;
    }

    @SuppressWarnings("unchecked")
    private static Properties generateConnectionProperties(BMap<BString, Object> connectionConfigs) {
        BMap<BString, BString> additionalProperties = (BMap<BString, BString>) connectionConfigs
                .getMapValue(PROPERTIES);
        Properties properties = new Properties();
        for (BString key : additionalProperties.getKeys()) {
            properties.put(key.getValue(), additionalProperties.getStringValue(key).getValue());
        }
        return properties;
    }

    @SuppressWarnings("unchecked")
    private static List<BObject> getBServices(BObject bListener) {
        Object nativeData = bListener.getNativeData(NATIVE_SERVICE_LIST);
        if (nativeData instanceof List<?>) {
            return (List<BObject>) nativeData;
        } else {
            // This should never happen
            throw new IllegalStateException("Expected List<BObject> but got: " +
                    (nativeData != null ? nativeData.getClass() : "null"));
        }
    }
}
