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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

import java.math.BigDecimal;

/**
 * Constants for ActiveMQ connector.
 */
public interface ActiveMQConstants {

    BString USERNAME = StringUtils.fromString("username");
    BString PASSWORD = StringUtils.fromString("password");
    BString CLIENT_ID = StringUtils.fromString("clientId");
    BString SECURE_SOCKET = StringUtils.fromString("secureSocket");
    BString OPTIMIZE_ACKNOWLEDGEMENTS = StringUtils.fromString("optimizeAcknowledgements");
    BString SET_ALWAYS_SESSION_ASYNC = StringUtils.fromString("setAlwaysSessionAsync");
    BString PREFETCH_POLICY = StringUtils.fromString("prefetchPolicy");
    BString REDELIVERY_POLICY = StringUtils.fromString("redeliveryPolicy");
    BString PROPERTIES = StringUtils.fromString("properties");

    // Prefetch policy field names
    BString QUEUE_PREFETCH = StringUtils.fromString("queuePrefetch");
    BString TOPIC_PREFETCH = StringUtils.fromString("topicPrefetch");
    BString DURABLE_TOPIC_PREFETCH = StringUtils.fromString("durableTopicPrefetch");
    BString OPTIMIZE_DURABLE_TOPIC_PREFETCH_SIZE = StringUtils.fromString("optimizeDurableTopicPrefetchSize");

    // Redelivery policy field names
    BString COLLISION_AVOIDANCE_PERCENT = StringUtils.fromString("collisionAvoidancePercent");
    BString MAXIMUM_REDELIVERIES = StringUtils.fromString("maximumRedeliveries");
    BString MAXIMUM_REDELIVERY_DELAY = StringUtils.fromString("maximumRedeliveryDelay");
    BString INITIAL_REDELIVERY_DELAY = StringUtils.fromString("initialRedeliveryDelay");
    BString USE_COLLISION_AVOIDANCE = StringUtils.fromString("useCollisionAvoidance");
    BString USE_EXPONENTIAL_BACK_OFF = StringUtils.fromString("useExponentialBackOff");
    BString BACK_OFF_MULTIPLIER = StringUtils.fromString("backOffMultiplier");
    BString REDELIVERY_DELAY = StringUtils.fromString("redeliveryDelay");
    BString PRE_DISPATCH_CHECK = StringUtils.fromString("preDispatchCheck");

    // SecureSocket field names
    BString CERT = StringUtils.fromString("cert");
    BString KEY = StringUtils.fromString("key");
    BString CERT_FILE = StringUtils.fromString("certFile");
    BString KEY_FILE = StringUtils.fromString("keyFile");
    BString KEY_PASSWORD = StringUtils.fromString("keyPassword");
    BString CRYPTO_TRUSTSTORE_PATH = StringUtils.fromString("path");
    BString CRYPTO_TRUSTSTORE_PASSWORD = StringUtils.fromString("password");
    BString KEY_STORE_PATH = StringUtils.fromString("path");
    BString KEY_STORE_PASSWORD = StringUtils.fromString("password");

    // Ballerina record names
    String BMESSAGE_NAME = "Message";
    String BCALLER_NAME = "Caller";

    // Service config field names
    BString MESSAGE_SELECTOR = StringUtils.fromString("messageSelector");
    BString QUEUE_NAME = StringUtils.fromString("queueName");
    BString TOPIC_NAME = StringUtils.fromString("topicName");
    BString SESSION_ACK_MODE = StringUtils.fromString("sessionAckMode");
    BString NO_LOCAL = StringUtils.fromString("noLocal");
    BString CONSUMER_TYPE = StringUtils.fromString("consumerType");
    BString SUBSCRIBER_NAME = StringUtils.fromString("subscriberName");
    BString POLLING_INTERVAL = StringUtils.fromString("pollingInterval");
    BString RECEIVE_TIMEOUT = StringUtils.fromString("receiveTimeout");
    BString EXCLUSIVE = StringUtils.fromString("exclusive");

    // Message record field names
    BString MESSAGE_ID = StringUtils.fromString("messageId");
    BString CORRELATION_ID = StringUtils.fromString("correlationId");
    BString REPLY_TO = StringUtils.fromString("replyTo");
    BString MESSAGE_PAYLOAD = StringUtils.fromString("payload");
    BString MESSAGE_PROPERTIES = StringUtils.fromString("properties");
    BString FORMAT_FIELD = StringUtils.fromString("format");
    BString PRIORITY_FIELD = StringUtils.fromString("priority");
    BString EXPIRY_FIELD = StringUtils.fromString("expiry");
    BString PERSISTENCE_FIELD = StringUtils.fromString("persistence");
    BString MESSAGE_USERID = StringUtils.fromString("userId");

    // consumer tags
    String QUERY_PARAM_EXCLUSIVE_CONSUMER = "?consumer.exclusive=true";

    // Acknowledgement modes
    String AUTO_ACKNOWLEDGE_MODE = "AUTO_ACKNOWLEDGE";
    String CLIENT_ACKNOWLEDGE_MODE = "CLIENT_ACKNOWLEDGE";
    String SESSION_TRANSACTED_MODE = "SESSION_TRANSACTED";

    // Remote method names
    String ON_MESSAGE_METHOD = "onMessage";
    String ON_ERROR_METHOD = "onError";

    // Error
    String ACTIVEMQ_ERROR = "Error";

    // Millisecond multiplier for time conversions
    BigDecimal MILLISECOND_MULTIPLIER = new BigDecimal(1000);
}
