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

import ballerina/lang.runtime;
import ballerina/test;

listener Listener messageFieldsListener = check new Listener(BROKER_URL);

Message? receivedMessageWithFields = ();

@test:Config {
    groups: ["message-fields"]
}
function testAllMessageFields() returns error? {
    Service consumerSvc = @ServiceConfig {
        queueName: "message-fields-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                receivedMessageWithFields = message;
            }
        }
    };
    check messageFieldsListener.attach(consumerSvc, "message-fields-service");

    // Send message with all possible JMS headers set
    check sendMessageWithAllHeaders(BROKER_URL, "message-fields-test-queue", "Test message with all headers");
    runtime:sleep(2);
    Message? msg = ();
    lock {
        msg = receivedMessageWithFields;
    }
    test:assertTrue(msg is Message, "Message should be received");
    if msg is Message {
        test:assertTrue(msg.messageId.length() > 0, "messageId should be present");

        test:assertTrue(msg.timestamp is int, "timestamp should be present");
        int timestamp = <int>msg.timestamp;
        test:assertTrue(timestamp > 0, "timestamp should be greater than 0");

        test:assertTrue(msg.destination is string, "destination should be present");
        string destination = <string>msg.destination;
        test:assertTrue(destination.includes("message-fields-test-queue"),
            "destination should contain queue name");

        test:assertTrue(msg.persistent is boolean, "persistent should be present");
        boolean persistent = <boolean>msg.persistent;
        test:assertTrue(persistent, "persistent should be true");

        test:assertTrue(msg.redelivered is boolean, "redelivered should be present");
        boolean redelivered = <boolean>msg.redelivered;
        test:assertFalse(redelivered, "redelivered should be false for first delivery");

        test:assertTrue(msg.'type is string, "type should be present");
        string msgType = <string>msg.'type;
        test:assertEquals(msgType, "TestMessageType", "type should be 'TestMessageType'");

        test:assertTrue(msg.priority is int, "priority should be present");
        int priority = <int>msg.priority;
        test:assertEquals(priority, 5, "priority should be 5");

        test:assertTrue(msg.expiry is int, "expiry should be present");
        int expiry = <int>msg.expiry;
        // Expiry is absolute timestamp, not TTL - it should be > current time
        test:assertTrue(expiry > 0, "expiry should be > 0");
        test:assertTrue(expiry > timestamp, "expiry should be after message timestamp");

        test:assertTrue(msg.correlationId is string, "correlationId should be present");
        string correlationId = <string>msg.correlationId;
        test:assertEquals(correlationId, "test-correlation-id", "correlationId should be 'test-correlation-id'");

        test:assertTrue(msg.replyTo is string, "replyTo should be present");
        string replyTo = <string>msg.replyTo;
        test:assertEquals(replyTo, "queue://message-fields-test-queue", 
            "replyTo should be 'queue://message-fields-test-queue'");

        test:assertTrue(msg.payload.length() > 0, "payload should be present");
        string payloadStr = check string:fromBytes(msg.payload);
        test:assertEquals(payloadStr, "Test message with all headers", "payload content should match");
    }
}

Message? redeliveredMessage = ();
int redeliveryTestCount = 0;

@test:Config {
    groups: ["message-fields", "redelivery"]
}
function testRedeliveredField() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: SESSION_TRANSACTED,
        queueName: "redelivery-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message, Caller caller) returns error? {
            int count = 0;
            lock {
                redeliveryTestCount += 1;
                count = redeliveryTestCount;
            }
            if count == 1 {
                // First delivery - rollback to trigger redelivery
                lock {
                    redeliveredMessage = ();
                }
                check caller->'rollback();
            } else {
                // Second delivery - should have redelivered=true
                lock {
                    redeliveredMessage = message;
                }
                check caller->'commit();
            }
        }
    };
    check messageFieldsListener.attach(consumerSvc, "redelivery-test-service");
    check sendToQueue(BROKER_URL, "redelivery-test-queue", "Redelivery test message");
    runtime:sleep(3);
    Message? msg = ();
    lock {
        msg = redeliveredMessage;
    }
    if msg is Message {
        test:assertTrue(msg.redelivered is boolean, "redelivered field should be present");
        boolean redelivered = <boolean>msg.redelivered;
        test:assertTrue(redelivered, "redelivered should be true after rollback");
    }
}

Message? messageWithType = ();

@test:Config {
    groups: ["message-fields"]
}
function testTypeField() returns error? {
    Service consumerSvc = @ServiceConfig {
        queueName: "type-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                messageWithType = message;
            }
        }
    };
    check messageFieldsListener.attach(consumerSvc, "type-test-service");
    check sendMessageWithType(BROKER_URL, "type-test-queue",
        "Message with type", "OrderMessage");
    runtime:sleep(2);
    Message? msg = ();
    lock {
        msg = messageWithType;
    }
    test:assertTrue(msg is Message, "Message with type should be received");
    if msg is Message {
        test:assertTrue(msg.'type is string, "type field should be present");
        string? msgType = msg.'type;
        if msgType is string {
            test:assertEquals(msgType, "OrderMessage", "type should match sent value");
        }
    }
}

Message? persistentMessage = ();
Message? nonPersistentMessage = ();

@test:Config {
    groups: ["message-fields"]
}
function testPersistentField() returns error? {
    Service consumerSvc1 = @ServiceConfig {
        queueName: "persistent-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                persistentMessage = message;
            }
        }
    };
    check messageFieldsListener.attach(consumerSvc1, "persistent-test-service");

    Service consumerSvc2 = @ServiceConfig {
        queueName: "non-persistent-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                nonPersistentMessage = message;
            }
        }
    };
    check messageFieldsListener.attach(consumerSvc2, "non-persistent-test-service");

    // Send persistent message
    check sendPersistentMessage(BROKER_URL, "persistent-test-queue", "Persistent message");

    // Send non-persistent message
    check sendNonPersistentMessage(BROKER_URL, "non-persistent-test-queue", "Non-persistent message");

    runtime:sleep(2);

    Message? pMsg = ();
    Message? npMsg = ();
    lock {
        pMsg = persistentMessage;
        npMsg = nonPersistentMessage;
    }

    test:assertTrue(pMsg is Message, "Persistent message should be received");
    test:assertTrue(npMsg is Message, "Non-persistent message should be received");

    if pMsg is Message {
        boolean? persistent = pMsg.persistent;
        if persistent is boolean {
            test:assertTrue(persistent, "persistent field should be true for persistent message");
        }
    }

    if npMsg is Message {
        boolean? persistent = npMsg.persistent;
        if persistent is boolean {
            test:assertFalse(persistent, "persistent field should be false for non-persistent message");
        }
    }
}
