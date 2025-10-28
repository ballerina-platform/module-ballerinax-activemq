// Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/crypto;

# Represents an ActiveMQ service object that can be attached to an `activemq:Listener`.
public type Service distinct service object {};

# ActiveMQ broker connection configuration.
#
# + username - Username for authentication
# + password - Password for authentication
# + clientID - The JMS client ID to use. If not specified, a random UUID will be generated
# + secureSocket - Configurations related to SSL/TLS encryption
# + optimizeAcknowledgements - Whether to optimize acknowledgements for better performance
# + setAlwaysSessionAsync - Whether to always use asynchronous sessions
# + redeliveryPolicy - Configurations for message redelivery behavior
# + prefetchPolicy - Configurations for message prefetching behavior
# + properties - Additional connection properties as key-value pairs. If same property is set in both
#                connection properties and other configurations, the value in other configurations will take precedence
public type ConnectionConfiguration record {
    string username?;
    string password?;
    string clientID?;
    SecureSocket secureSocket?;
    boolean optimizeAcknowledgements = false;
    boolean setAlwaysSessionAsync = true;
    PrefetchPolicy prefetchPolicy?;
    RedeliveryPolicy redeliveryPolicy?;
    map<string> properties = {};
};

# Configurations for message prefetching behavior.
#
# + queuePrefetchSize - The prefetch size for queue subscriptions
# + topicPrefetchSize - The prefetch size for topic subscriptions
# + durableTopicPrefetchSize - The prefetch size for durable topic subscriptions
# + optimizeDurableTopicPrefetchSize - The optimized prefetch size for durable topic subscriptions
public type PrefetchPolicy record {|
    int queuePrefetchSize = 1000;
    int topicPrefetchSize = 32767;
    int durableTopicPrefetchSize = 100;
    int optimizeDurableTopicPrefetchSize = 1000;
|};

# Configurations for secure communication with the ActiveMQ broker.
#
# + cert - Configurations associated with `crypto:TrustStore` or single certificate file that the client trusts
# + key - Configurations associated with `crypto:KeyStore` or combination of certificate and private key of the client
public type SecureSocket record {|
    crypto:TrustStore|string cert;
    crypto:KeyStore|CertKey key?;
|};

# Represents a combination of certificate, private key, and private key password if encrypted.
#
# + certFile - A file containing the certificate
# + keyFile - A file containing the private key in PKCS8 format
# + keyPassword - Password of the private key if it is encrypted
public type CertKey record {|
    string certFile;
    string keyFile;
    string keyPassword?;
|};

# Represents a message received from ActiveMQ.
#
# + messageId - Unique identifier for the message
# + correlationId - Correlation identifier for request-reply patterns
# + priority - Message priority (0-9, where 0-4 is normal, 5-9 is expedited)
# + expiry - Message expiration time
# + persistence - Message delivery mode (1 = non-persistent, 2 = persistent)
# + replyToQueueName - Name of the queue to which a reply should be sent
# + userId - User identifier of the message sender
# + format - Format name of the message data (e.g., "text", "binary")
# + properties - Custom message properties
# + payload - The message payload as a byte array
public type Message record {|
    byte[] messageId;
    byte[] correlationId?;
    int priority?;
    int expiry?;
    int persistence?;
    string replyToQueueName?;
    string userId?;
    string format?;
    map<anydata> properties?;
    byte[] payload;
|};

# Defines the supported JMS message consumer types for ActiveMQ.
public enum ConsumerType {
    # Represents JMS durable subscriber
    DURABLE,
    # Represents JMS default consumer
    DEFAULT
}

# Defines the JMS session acknowledgement modes.
public enum AcknowledgementMode {
    # Indicates that the session will use a local transaction which may subsequently
    # be committed or rolled back by calling the session's `commit` or `rollback` methods.
    SESSION_TRANSACTED,
    # Indicates that the session automatically acknowledges a client's receipt of a message
    # either when the session has successfully returned from a call to `receive` or when
    # the message listener the session has called to process the message successfully returns.
    AUTO_ACKNOWLEDGE,
    # Indicates that the client acknowledges a consumed message by calling the
    # MessageConsumer's or Caller's `acknowledge` method. Acknowledging a consumed message
    # acknowledges all messages that the session has consumed.
    CLIENT_ACKNOWLEDGE,
    # Indicates that the session to lazily acknowledge the delivery of messages.
    # This is likely to result in the delivery of some duplicate messages if the JMS provider fails,
    # so it should only be used by consumers that can tolerate duplicate messages.
    # Use of this mode can reduce session overhead by minimizing the work the session does to prevent duplicates.
    DUPS_OK_ACKNOWLEDGE
}

# Configurations for message redelivery behavior. RedeliveryPolicy defines how messages are
# redelivered when they are rolled back. This can be set at the connection level or per consumer.
# The consumer level redelivery policy overrides the connection level redelivery policy.
#
# + collisionAvoidancePercent - Percentage of randomness to apply to redelivery delays to avoid collisions.
# The value should be between 0 and 100.
# + maximumRedeliveries - The maximum number of times to redeliver a message  
# + maximumRedeliveryDelay - The maximum delay between redelivery attempts.  
# This helps to cap the delay when using exponential backoff  
# + initialRedeliveryDelay - The initial delay before the first redelivery attempt  
# + useCollisionAvoidance - Whether to use collision avoidance for redelivery delays  
# + useExponentialBackOff - Whether to use exponential backoff for redelivery delays  
# + backOffMultiplier - Multiplier applied to the delay on each redelivery when exponential backoff is enabled  
# + redeliveryDelay - The delay between redelivery attempts  
# + preDispatchCheck - Whether to check the dispatch policy before redelivering a message
public type RedeliveryPolicy record {|
    int collisionAvoidancePercent = 15;
    int maximumRedeliveries = 6;
    int maximumRedeliveryDelay = -1;
    int initialRedeliveryDelay = 1000;
    boolean useCollisionAvoidance = false;
    boolean useExponentialBackOff = false;
    float backOffMultiplier = 5.0;
    int redeliveryDelay = 1000;
    boolean preDispatchCheck = true;
|};

# Common configurations related to the ActiveMQ queue or topic subscription.
#
# + sessionAckMode - Configuration indicating how messages received by the session will be acknowledged
# + messageSelector - Only messages with properties matching the message selector expression are delivered.
# If this value is not set that indicates that there is no message selector for the message consumer.
# For example, to only receive messages with a property `priority` set to `'high'`, use:
# `"priority = 'high'"`. If this value is not set, all messages in the queue will be delivered.
# + pollingInterval - The polling interval in seconds
# + receiveTimeout - The timeout to wait till a `receive` action finishes when there are no messages
# + exclusive - Whether only this consumer can consume messages from the destination
# + redeliveryPolicy - Configurations for message redelivery behavior
type CommonSubscriptionConfig record {|
    AcknowledgementMode sessionAckMode = AUTO_ACKNOWLEDGE;
    string messageSelector?;
    decimal pollingInterval = 10;
    decimal receiveTimeout = 5;
    boolean exclusive = false;
    RedeliveryPolicy redeliveryPolicy?;
|};

# Configuration for an ActiveMQ queue.
#
# + queueName - The name of the queue to consume messages from
public type QueueConfig record {|
    *CommonSubscriptionConfig;
    string queueName;
|};

# Configuration for an ActiveMQ topic subscription.
#
# + topicName - The name of the topic to subscribe to
# + noLocal - If true then any messages published to the topic using this session's connection, or any other connection
# with the same client identifier, will not be added to the subscription.
# + consumerType - The message consumer type
# + subscriberName - The name used to identify the subscription
public type TopicConfig record {|
    *CommonSubscriptionConfig;
    string topicName;
    boolean noLocal = false;
    ConsumerType consumerType = DEFAULT;
    string subscriberName?;
|};

# The service configuration type for the `activemq:Service`.
public type ServiceConfiguration QueueConfig|TopicConfig;

# Annotation to configure the `activemq:Service`.
public annotation ServiceConfiguration ServiceConfig on service;
