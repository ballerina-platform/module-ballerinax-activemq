// Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

// Test producer utility functions for testing without full producer implementation
// These functions use Java JMS client directly to send messages for testing purposes

# Send a text message to a queue
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + queueName - The queue name
# + message - The message text
# + return - Error if sending fails
isolated function sendToQueue(string brokerUrl, string queueName, string message) returns error? = @java:Method {
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;

# Send a text message to a queue with properties
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + queueName - The queue name
# + message - The message text
# + properties - Message properties (headers)
# + return - Error if sending fails
isolated function sendToQueueWithProperties(string brokerUrl, string queueName, string message,
                                           map<string> properties) returns error? = @java:Method {
    name: "sendToQueue",
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;

# Send a text message to a topic
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + topicName - The topic name
# + message - The message text
# + return - Error if sending fails
isolated function sendToTopic(string brokerUrl, string topicName, string message) returns error? = @java:Method {
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;

# Send a text message to a topic with properties
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + topicName - The topic name
# + message - The message text
# + properties - Message properties (headers)
# + return - Error if sending fails
isolated function sendToTopicWithProperties(string brokerUrl, string topicName, string message,
                                           map<anydata> properties) returns error? = @java:Method {
    name: "sendToTopic",
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;

# Send a byte message to a queue
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + queueName - The queue name
# + payload - The message payload as byte array
# + return - Error if sending fails
isolated function sendBytesToQueue(string brokerUrl, string queueName, byte[] payload) returns error? = @java:Method {
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;

# Send a message with all JMS headers set (for testing message field mapping)
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + queueName - The queue name
# + message - The message text
# + return - Error if sending fails
isolated function sendMessageWithAllHeaders(string brokerUrl, string queueName, string message) returns error? = @java:Method {
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;

# Send a message with JMSType set
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + queueName - The queue name
# + message - The message text
# + messageType - The JMS message type
# + return - Error if sending fails
isolated function sendMessageWithType(string brokerUrl, string queueName, string message,
                                      string messageType) returns error? = @java:Method {
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;

# Send a persistent message
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + queueName - The queue name
# + message - The message text
# + return - Error if sending fails
isolated function sendPersistentMessage(string brokerUrl, string queueName, string message) returns error? = @java:Method {
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;

# Send a non-persistent message
#
# + brokerUrl - The broker URL (e.g., "tcp://localhost:61616")
# + queueName - The queue name
# + message - The message text
# + return - Error if sending fails
isolated function sendNonPersistentMessage(string brokerUrl, string queueName, string message) returns error? = @java:Method {
    'class: "io.ballerina.lib.activemq.util.TestProducer"
} external;
