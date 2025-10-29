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
# + username - Username for broker authentication (must be provided with password)
# + password - Password for broker authentication (must be provided with username)
# + clientID - The JMS client ID to use for identifying this client connection. If not specified,
#              a random UUID will be generated
# + secureSocket - SSL/TLS encryption configurations for secure broker communication
# + optimizeAcknowledgements - When enabled, optimizes acknowledgement processing for better
#                              performance at the cost of potential duplicate messages
# + setAlwaysSessionAsync - When enabled, all sessions will use asynchronous message delivery
# + redeliveryPolicy - Configurations that control how failed messages are redelivered
# + prefetchPolicy - Configurations that control how many messages are prefetched from the broker
# + properties - Additional broker-specific connection properties as key-value pairs. Note: If the
#                same property is configured in both this map and dedicated fields, the dedicated
#                field value takes precedence
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

# Configurations for message prefetching behavior. Prefetching allows the broker to send messages
# to the consumer before they are explicitly requested, improving throughput by reducing network
# round trips.
#
# + queuePrefetchSize - Number of messages to prefetch for queue subscriptions (default: 1000)
# + topicPrefetchSize - Number of messages to prefetch for non-durable topic subscriptions
#                       (default: 32767)
# + durableTopicPrefetchSize - Number of messages to prefetch for durable topic subscriptions
#                              (default: 100)
# + optimizeDurableTopicPrefetchSize - Optimized prefetch size for durable topics when fast
#                                      consumption is expected (default: 1000)
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

# Represents a message received from an ActiveMQ broker.
#
# + messageId - Unique identifier assigned to the message by the JMS provider
# + correlationId - Optional identifier used to correlate this message with another (typically used
#                   in request-reply patterns)
# + priority - Message priority level (0-9, where 0-4 is normal priority and 5-9 is expedited
#              priority)
# + expiry - Timestamp (in milliseconds since epoch) when the message expires, or 0 if it doesn't
#            expire
# + persistence - Message delivery mode: 1 for non-persistent (faster but may be lost on broker
#                 failure), 2 for persistent (slower but survives broker restarts)
# + replyToQueueName - Name of the queue or topic where replies should be sent (used in
#                      request-reply patterns)
# + userId - Identifier of the user who sent the message (if available from the broker)
# + format - Format identifier of the message payload (e.g., "text", "binary", "json")
# + properties - Application-specific custom properties attached to the message
# + payload - The message content as a byte array
public type Message record {|
    string messageId;
    string correlationId?;
    int priority?;
    int expiry?;
    int persistence?;
    string replyToQueueName?;
    string userId?;
    string format?;
    map<anydata> properties?;
    byte[] payload;
|};

# Defines the supported JMS message consumer types for ActiveMQ topic subscriptions.
public enum ConsumerType {
    # Durable subscriber - Subscription persists even when the client disconnects. Messages
    # published while the subscriber is offline will be delivered when it reconnects.
    DURABLE,
    # Default (non-durable) consumer - Subscription exists only while the client is connected.
    # Messages published while the consumer is disconnected are not delivered.
    DEFAULT
}

# Defines the JMS session acknowledgement modes.
public enum AcknowledgementMode {
    # Indicates that the session will use a local transaction which may subsequently be
    # committed or rolled back by calling the session's `commit` or `rollback` methods.
    SESSION_TRANSACTED,
    # Indicates that the session automatically acknowledges a client's receipt of a message
    # either when the session has successfully returned from a call to `receive` or when the
    # message listener the session has called to process the message successfully returns.
    AUTO_ACKNOWLEDGE,
    # Indicates that the client acknowledges a consumed message by calling the MessageConsumer's
    # or Caller's `acknowledge` method. Acknowledging a consumed message acknowledges all
    # messages that the session has consumed.
    CLIENT_ACKNOWLEDGE,
    # Indicates that the session lazily acknowledges the delivery of messages. This is likely to
    # result in the delivery of some duplicate messages if the JMS provider fails, so it should
    # only be used by consumers that can tolerate duplicate messages. Use of this mode can reduce
    # session overhead by minimizing the work the session does to prevent duplicates.
    DUPS_OK_ACKNOWLEDGE
}

# Configurations for message redelivery behavior. RedeliveryPolicy defines how messages are
# redelivered when they are rolled back. This can be set at the connection level or per consumer.
# The consumer level redelivery policy overrides the connection level redelivery policy.
#
# + collisionAvoidancePercent - Percentage of randomness (0-100) to apply to redelivery delays to
#                               avoid collision when multiple consumers retry simultaneously
# + maximumRedeliveries - The maximum number of times to redeliver a message before it is
#                         considered a poison message and moved to a dead letter queue
# + maximumRedeliveryDelay - The maximum delay (in milliseconds) between redelivery attempts. This
#                            helps to cap the delay when using exponential backoff
# + initialRedeliveryDelay - The initial delay (in milliseconds) before the first redelivery
#                            attempt
# + useCollisionAvoidance - When enabled, adds randomness to redelivery delays to prevent multiple
#                           consumers from retrying simultaneously
# + useExponentialBackOff - When enabled, increases the delay between redelivery attempts
#                           exponentially using the backOffMultiplier
# + backOffMultiplier - Multiplier applied to the delay on each redelivery when exponential backoff
#                       is enabled (e.g., 5.0 means each retry delay is 5x the previous)
# + redeliveryDelay - The base delay (in milliseconds) between redelivery attempts
# + preDispatchCheck - When enabled, checks the dispatch policy before redelivering a message
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

# Common configurations related to ActiveMQ queue or topic subscriptions.
#
# + sessionAckMode - Specifies how messages received by the session will be acknowledged
# + messageSelector - JMS message selector expression to filter messages. Only messages with
#                     properties matching the selector are delivered. For example, to receive only
#                     messages with property `priority` set to `'high'`, use: `"priority = 'high'"`.
#                     If not set, all messages from the destination will be delivered.
# + pollingInterval - The interval (in seconds) between polling attempts for new messages
# + receiveTimeout - The timeout (in seconds) to wait for a message when polling. If no message
#                    arrives within this time, the receive operation returns without a message.
# + exclusive - When enabled, only this consumer can consume messages from the destination,
#               preventing other consumers from receiving messages from the same destination
# + redeliveryPolicy - Configurations for message redelivery behavior when messages fail processing
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
# + noLocal - When enabled, messages published to the topic using this session's connection, or any
#             other connection with the same client identifier, will not be delivered to this
#             subscriber (useful to avoid receiving your own messages)
# + consumerType - The message consumer type (DURABLE or DEFAULT). Use DURABLE for subscriptions
#                  that should persist when the client disconnects.
# + subscriberName - The name used to identify a durable subscription (required for DURABLE
#                    consumer type)
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
