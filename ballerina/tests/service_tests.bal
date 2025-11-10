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

listener Listener activemqListener = check new Listener(BROKER_URL);

isolated int queueServiceReceivedMessageCount = 0;
isolated int topicServiceReceivedMessageCount = 0;
isolated string? queueServiceReceivedMessage = ();

@test:Config {
    groups: ["service"]
}
isolated function testQueueService() returns error? {
    Service consumerSvc = @ServiceConfig {
        queueName: "service-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            string msg = check string:fromBytes(message.payload);
            lock {
                queueServiceReceivedMessageCount += 1;
            }
            lock {
                queueServiceReceivedMessage = msg;
            }
        }
    };
    check activemqListener.attach(consumerSvc, "test-queue-service");

    // Send message using test producer utility
    check sendToQueue(BROKER_URL, "service-test-queue", "Hello World from queue");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = queueServiceReceivedMessageCount;
    }
    test:assertEquals(count, 1, "'service-test-queue' did not receive the expected number of messages");

    string? msg = ();
    lock {
        msg = queueServiceReceivedMessage;
    }
    test:assertEquals(msg, "Hello World from queue", "Received message content does not match");
}

@test:Config {
    groups: ["service"]
}
isolated function testTopicService() returns error? {
    Service consumerSvc = @ServiceConfig {
        topicName: "service-test-topic",
        subscriberName: "sub-1",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                topicServiceReceivedMessageCount += 1;
            }
        }
    };
    check activemqListener.attach(consumerSvc, "test-topic-service");

    // Give the subscriber time to fully attach before sending
    // Topics require subscribers to be connected before messages are sent
    runtime:sleep(2);

    // Send message using test producer utility
    check sendToTopic(BROKER_URL, "service-test-topic", "Hello World from topic");

    runtime:sleep(2);
    lock {
        test:assertEquals(topicServiceReceivedMessageCount, 1,
            "'service-test-topic' did not receive the expected number of messages");
    }
}

isolated int serviceWithCallerReceivedMsgCount = 0;

@test:Config {
    groups: ["service"]
}
isolated function testServiceWithCaller() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "service-caller-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message, Caller caller) returns error? {
            lock {
                serviceWithCallerReceivedMsgCount += 1;
            }
            check caller->acknowledge(message);
        }
    };
    check activemqListener.attach(consumerSvc, "test-caller-svc");

    // Send message using test producer utility
    check sendToQueue(BROKER_URL, "service-caller-test-queue", "Hello World with caller");

    runtime:sleep(2);
    lock {
        test:assertEquals(serviceWithCallerReceivedMsgCount, 1,
            "'service-caller-test-queue' did not receive the expected number of messages");
    }
}

isolated int serviceWithTransactionsMsgCount = 0;

@test:Config {
    groups: ["service", "transactions"]
}
isolated function testServiceWithTransactions() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: SESSION_TRANSACTED,
        queueName: "trx-service-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        isolated remote function onMessage(Message message, Caller caller) returns error? {
            lock {
                serviceWithTransactionsMsgCount += 1;
            }
            check caller->'commit();
        }
    };
    check activemqListener.attach(consumerSvc, "test-transacted-svc");

    // Send message using test producer utility
    check sendToQueue(BROKER_URL, "trx-service-test-queue", "Transaction test message");

    runtime:sleep(2);
    lock {
        test:assertEquals(serviceWithTransactionsMsgCount, 1,
            "'trx-service-test-queue' did not receive the expected number of messages");
    }
}

isolated int ServiceWithTransactionsRollbackMsgCount = 0;

@test:Config {
    groups: ["service", "transactions"]
}
isolated function testServiceWithTransactionsRollback() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: SESSION_TRANSACTED,
        queueName: "trx-rollback-service-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        isolated remote function onMessage(Message message, Caller caller) returns error? {
            int count = 0;
            lock {
                ServiceWithTransactionsRollbackMsgCount += 1;
                count = ServiceWithTransactionsRollbackMsgCount;
            }
            // Rollback to test redelivery
            if count == 1 {
                check caller->'rollback();
            } else {
                check caller->'commit();
            }
        }
    };
    check activemqListener.attach(consumerSvc, "test-rollback-svc");

    // Send message using test producer utility
    check sendToQueue(BROKER_URL, "trx-rollback-service-test-queue", "Rollback test message");

    runtime:sleep(3);
    lock {
        // Should receive message twice (once before rollback, once after redelivery)
        test:assertEquals(ServiceWithTransactionsRollbackMsgCount, 2,
            "'trx-rollback-service-test-queue' did not receive the expected number of messages");
    }
}

isolated int durableTopicServiceReceivedMsgCount = 0;

@test:Config {
    groups: ["service", "topics"]
}
isolated function testDurableTopicService() returns error? {
    Service consumerSvc = @ServiceConfig {
        topicName: "service-durable-topic",
        consumerType: DURABLE,
        subscriberName: "durable-sub-1",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                durableTopicServiceReceivedMsgCount += 1;
            }
        }
    };
    check activemqListener.attach(consumerSvc, "test-durable-topic-service");

    // Give the subscriber time to fully attach before sending
    // Topics require subscribers to be connected before messages are sent
    runtime:sleep(5);

    // Send message using test producer utility
    check sendToTopic(BROKER_URL, "service-durable-topic", "Hello durable subscriber");

    runtime:sleep(5);
    lock {
        test:assertEquals(durableTopicServiceReceivedMsgCount, 1,
            "'service-durable-topic' did not receive the expected number of messages");
    }
}

isolated int exclusiveQueueServiceReceivedMsgCount = 0;

@test:Config {
    groups: ["service"]
}
isolated function testExclusiveQueueService() returns error? {
    Service consumerSvc = @ServiceConfig {
        queueName: "service-exclusive-queue",
        exclusive: true,
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                exclusiveQueueServiceReceivedMsgCount += 1;
            }
        }
    };
    check activemqListener.attach(consumerSvc, "test-exclusive-service");

    // Send message using test producer utility
    check sendToQueue(BROKER_URL, "service-exclusive-queue", "Exclusive consumer message");

    runtime:sleep(2);
    lock {
        test:assertEquals(exclusiveQueueServiceReceivedMsgCount, 1,
            "'service-exclusive-queue' did not receive the expected number of messages");
    }
}

isolated int messageSelectorServiceReceivedMsgCount = 0;

@test:Config {
    groups: ["service"]
}
isolated function testMessageSelectorService() returns error? {
    Service consumerSvc = @ServiceConfig {
        queueName: "service-selector-queue",
        messageSelector: "priority = 'high'",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                messageSelectorServiceReceivedMsgCount += 1;
            }
        }
    };
    check activemqListener.attach(consumerSvc, "test-selector-service");

    // Send messages with different priorities
    // Only messages with priority='high' should be received
    check sendToQueueWithProperties(BROKER_URL, "service-selector-queue", "Low priority message",
        {priority: "low"});
    check sendToQueueWithProperties(BROKER_URL, "service-selector-queue", "High priority message",
        {priority: "high"});
    check sendToQueueWithProperties(BROKER_URL, "service-selector-queue", "Medium priority message",
        {priority: "medium"});

    runtime:sleep(2);
    lock {
        // Only the message with priority='high' should be received
        test:assertEquals(messageSelectorServiceReceivedMsgCount, 1,
            "'service-selector-queue' should receive only messages matching selector");
    }
}
