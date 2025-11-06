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

// SSL/TLS Secure Socket Tests

isolated int sslQueueReceivedMessageCount = 0;
isolated int sslTopicReceivedMessageCount = 0;
isolated int sslWithCertKeyReceivedMessageCount = 0;

@test:Config {
    groups: ["ssl", "secure"]
}
isolated function testSSLQueueWithKeyStore() returns error? {
    // Reset counter for this test
    lock {
        sslQueueReceivedMessageCount = 0;
    }

    // Create listener with SSL configuration using KeyStore
    Listener sslListener = check new (BROKER_SSL_URL, {
        secureSocket: {
            cert: SERVER_CERT_PATH,  // Use PEM file for server trust
            key: {
                path: CLIENT_KEYSTORE_PATH,
                password: KEYSTORE_PASSWORD
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        queueName: "ssl-test-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                sslQueueReceivedMessageCount += 1;
            }
        }
    };

    check sslListener.attach(consumerSvc, "ssl-queue-service");
    check sslListener.start();

    // Give listener time to initialize SSL connection and start consuming
    runtime:sleep(1);

    // Send message using test producer with SSL URL
    check sendToQueue(BROKER_SSL_URL, "ssl-test-queue", "Hello from SSL queue");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslQueueReceivedMessageCount;
    }
    test:assertEquals(count, 1, "SSL queue did not receive the expected number of messages");
}

@test:Config {
    groups: ["ssl", "secure", "topics"]
}
isolated function testSSLTopicWithKeyStore() returns error? {
    // Create listener with SSL configuration using KeyStore
    Listener sslListener = check new (BROKER_SSL_URL, {
        secureSocket: {
            cert: SERVER_CERT_PATH,  // Use PEM file for server trust
            key: {
                path: CLIENT_KEYSTORE_PATH,
                password: KEYSTORE_PASSWORD
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        topicName: "ssl-test-topic",
        subscriberName: "ssl-sub-1",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                sslTopicReceivedMessageCount += 1;
            }
        }
    };

    check sslListener.attach(consumerSvc, "ssl-topic-service");
    check sslListener.start();

    // Give subscriber time to connect
    runtime:sleep(2);

    // Send message using test producer with SSL URL
    check sendToTopic(BROKER_SSL_URL, "ssl-test-topic", "Hello from SSL topic");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslTopicReceivedMessageCount;
    }
    test:assertEquals(count, 1, "SSL topic did not receive the expected number of messages");
}

@test:Config {
    groups: ["ssl", "secure"]
}
isolated function testSSLWithCertKeyConfiguration() returns error? {
    // Reset counter for this test
    lock {
        sslWithCertKeyReceivedMessageCount = 0;
    }

    // Create listener with SSL configuration using CertKey (PEM files)
    Listener sslListener = check new (BROKER_SSL_URL, {
        secureSocket: {
            cert: SERVER_CERT_PATH,
            key: {
                certFile: CLIENT_CERT_PATH,
                keyFile: CLIENT_KEY_PATH
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        queueName: "ssl-certkey-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                sslWithCertKeyReceivedMessageCount += 1;
            }
        }
    };

    check sslListener.attach(consumerSvc, "ssl-certkey-service");
    check sslListener.start();

    // Give listener time to initialize SSL connection and start consuming
    runtime:sleep(1);

    // Send message
    check sendToQueue(BROKER_SSL_URL, "ssl-certkey-queue", "Hello from SSL with CertKey");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslWithCertKeyReceivedMessageCount;
    }
    test:assertEquals(count, 1, "SSL CertKey queue did not receive the expected number of messages");
}

isolated int sslTransactionsMsgCount = 0;

@test:Config {
    groups: ["ssl", "secure", "transactions"]
}
isolated function testSSLWithTransactions() returns error? {
    // Reset counter for this test
    lock {
        sslTransactionsMsgCount = 0;
    }

    // Create listener with SSL configuration
    Listener sslListener = check new (BROKER_SSL_URL, {
        secureSocket: {
            cert: SERVER_CERT_PATH,  // Use PEM file for server trust
            key: {
                path: CLIENT_KEYSTORE_PATH,
                password: KEYSTORE_PASSWORD
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        sessionAckMode: SESSION_TRANSACTED,
        queueName: "ssl-trx-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        isolated remote function onMessage(Message message, Caller caller) returns error? {
            lock {
                sslTransactionsMsgCount += 1;
            }
            check caller->'commit();
        }
    };

    check sslListener.attach(consumerSvc, "ssl-trx-service");
    check sslListener.start();

    // Give listener time to initialize SSL connection and start consuming
    runtime:sleep(1);

    // Send message
    check sendToQueue(BROKER_SSL_URL, "ssl-trx-queue", "SSL transaction message");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslTransactionsMsgCount;
    }
    test:assertEquals(count, 1, "SSL transaction queue did not receive the expected number of messages");
}

isolated int sslClientAckMsgCount = 0;

@test:Config {
    groups: ["ssl", "secure"]
}
isolated function testSSLWithClientAcknowledge() returns error? {
    // Reset counter for this test
    lock {
        sslClientAckMsgCount = 0;
    }

    // Create listener with SSL configuration
    Listener sslListener = check new (BROKER_SSL_URL, {
        secureSocket: {
            cert: SERVER_CERT_PATH,  // Use PEM file for server trust
            key: {
                path: CLIENT_KEYSTORE_PATH,
                password: KEYSTORE_PASSWORD
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "ssl-client-ack-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message, Caller caller) returns error? {
            lock {
                sslClientAckMsgCount += 1;
            }
            check caller->acknowledge(message);
        }
    };

    check sslListener.attach(consumerSvc, "ssl-client-ack-service");
    check sslListener.start();

    // Give listener time to initialize SSL connection and start consuming
    runtime:sleep(1);

    // Send message
    check sendToQueue(BROKER_SSL_URL, "ssl-client-ack-queue", "SSL client ack message");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslClientAckMsgCount;
    }
    test:assertEquals(count, 1, "SSL client ack queue did not receive the expected number of messages");
}

isolated int sslSelectorMsgCount = 0;

@test:Config {
    groups: ["ssl", "secure"]
}
isolated function testSSLWithMessageSelector() returns error? {
    // Reset counter for this test
    lock {
        sslSelectorMsgCount = 0;
    }

    // Create listener with SSL configuration
    Listener sslListener = check new (BROKER_SSL_URL, {
        secureSocket: {
            cert: SERVER_CERT_PATH,  // Use PEM file for server trust
            key: {
                path: CLIENT_KEYSTORE_PATH,
                password: KEYSTORE_PASSWORD
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        queueName: "ssl-selector-queue",
        messageSelector: "priority = 'high'",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                sslSelectorMsgCount += 1;
            }
        }
    };

    check sslListener.attach(consumerSvc, "ssl-selector-service");
    check sslListener.start();

    // Give listener time to initialize SSL connection and start consuming
    runtime:sleep(1);

    // Send messages with different priorities
    check sendToQueueWithProperties(BROKER_SSL_URL, "ssl-selector-queue", "Low priority",
        {priority: "low"});
    check sendToQueueWithProperties(BROKER_SSL_URL, "ssl-selector-queue", "High priority",
        {priority: "high"});
    check sendToQueueWithProperties(BROKER_SSL_URL, "ssl-selector-queue", "Medium priority",
        {priority: "medium"});

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslSelectorMsgCount;
    }
    // Only the message with priority='high' should be received
    test:assertEquals(count, 1, "SSL selector queue should receive only messages matching selector");
}

isolated int sslDurableTopicMsgCount = 0;

@test:Config {
    groups: ["ssl", "secure"]
}
isolated function testSSLWithDurableTopic() returns error? {

    // Create listener with SSL configuration
    Listener sslListener = check new (BROKER_SSL_URL, {
        secureSocket: {
            cert: SERVER_CERT_PATH,  // Use PEM file for server trust
            key: {
                path: CLIENT_KEYSTORE_PATH,
                password: KEYSTORE_PASSWORD
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        topicName: "ssl-durable-topic",
        consumerType: DURABLE,
        subscriberName: "ssl-durable-sub",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                sslDurableTopicMsgCount += 1;
            }
        }
    };

    check sslListener.attach(consumerSvc, "ssl-durable-topic-service");
    check sslListener.start();

    // Give subscriber time to connect
    runtime:sleep(2);

    // Send message
    check sendToTopic(BROKER_SSL_URL, "ssl-durable-topic", "Hello SSL durable subscriber");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslDurableTopicMsgCount;
    }
    test:assertEquals(count, 1, "SSL durable topic did not receive the expected number of messages");
}

isolated int sslExclusiveMsgCount = 0;

@test:Config {
    groups: ["ssl", "secure"]
}
isolated function testSSLWithExclusiveConsumer() returns error? {
    // Reset counter for this test
    lock {
        sslExclusiveMsgCount = 0;
    }

    // Create listener with SSL configuration
    Listener sslListener = check new (BROKER_SSL_URL, {
        secureSocket: {
            cert: SERVER_CERT_PATH,  // Use PEM file for server trust
            key: {
                path: CLIENT_KEYSTORE_PATH,
                password: KEYSTORE_PASSWORD
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        queueName: "ssl-exclusive-queue",
        exclusive: true,
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                sslExclusiveMsgCount += 1;
            }
        }
    };

    check sslListener.attach(consumerSvc, "ssl-exclusive-service");
    check sslListener.start();

    // Give listener time to initialize SSL connection and start consuming
    runtime:sleep(1);

    // Send message
    check sendToQueue(BROKER_SSL_URL, "ssl-exclusive-queue", "Exclusive SSL consumer message");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslExclusiveMsgCount;
    }
    test:assertEquals(count, 1, "SSL exclusive queue did not receive the expected number of messages");
}

isolated int sslAuthMsgCount = 0;

@test:Config {
    groups: ["ssl", "secure", "auth"]
}
isolated function testSSLWithAuthentication() returns error? {
    // Reset counter for this test
    lock {
        sslAuthMsgCount = 0;
    }

    // Create listener with SSL configuration and authentication
    Listener sslListener = check new (BROKER_SSL_URL, {
        username: BROKER_USERNAME,
        password: BROKER_PASSWORD,
        secureSocket: {
            cert: SERVER_CERT_PATH,  // Use PEM file for server trust
            key: {
                path: CLIENT_KEYSTORE_PATH,
                password: KEYSTORE_PASSWORD
            }
        }
    });

    Service consumerSvc = @ServiceConfig {
        queueName: "ssl-auth-queue",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                sslAuthMsgCount += 1;
            }
        }
    };

    check sslListener.attach(consumerSvc, "ssl-auth-service");
    check sslListener.start();

    // Give listener time to initialize SSL connection and start consuming
    runtime:sleep(1);

    // Send message
    check sendToQueue(BROKER_SSL_URL, "ssl-auth-queue", "SSL with authentication");

    runtime:sleep(2);

    int count = 0;
    lock {
        count = sslAuthMsgCount;
    }
    test:assertEquals(count, 1, "SSL auth queue did not receive the expected number of messages");
}
