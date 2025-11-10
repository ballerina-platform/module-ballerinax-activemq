// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org).
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

// Test configuration constants

// Broker URLs
const string BROKER_URL = "tcp://localhost:61616";
const string BROKER_SSL_URL = "ssl://localhost:61617";
const string FAILOVER_URL = "failover:(tcp://localhost:61616,tcp://localhost:61617)";

// Credentials
const string BROKER_USERNAME = "admin";
const string BROKER_PASSWORD = "admin";

// Test queues
const string TEST_QUEUE_1 = "test.queue.1";
const string TEST_QUEUE_2 = "test.queue.2";
const string TEST_QUEUE_3 = "test.queue.3";
const string TEST_QUEUE_4 = "test.queue.4";
const string TEST_QUEUE_5 = "test.queue.5";
const string TEST_QUEUE_CLIENT_ACK = "test.queue.client.ack";
const string TEST_QUEUE_TRANSACTED = "test.queue.transacted";
const string TEST_QUEUE_DUPS_OK = "test.queue.dups.ok";
const string TEST_QUEUE_PREFETCH = "test.queue.prefetch";
const string TEST_QUEUE_REDELIVERY = "test.queue.redelivery";
const string TEST_QUEUE_SELECTOR = "test.queue.selector";
const string TEST_QUEUE_EXCLUSIVE = "test.queue.exclusive";
const string TEST_QUEUE_SSL = "test.queue.ssl";

// Test topics
const string TEST_TOPIC_1 = "test.topic.1";
const string TEST_TOPIC_2 = "test.topic.2";
const string TEST_TOPIC_DURABLE = "test.topic.durable";
const string TEST_TOPIC_NO_LOCAL = "test.topic.nolocal";

// Test messages
const string TEXT_MESSAGE = "Hello from Ballerina ActiveMQ Connector";
const string TEXT_MESSAGE_2 = "Second test message";
const string TEXT_MESSAGE_3 = "Third test message";

// SSL certificate paths
const string SERVER_CERT_PATH = "./tests/resources/secrets/server.pem";
const string CLIENT_CERT_PATH = "./tests/resources/secrets/client-cert.pem";
const string CLIENT_KEY_PATH = "./tests/resources/secrets/client.key";
const string CLIENT_KEYSTORE_PATH = "./tests/resources/secrets/client-keystore.p12";
const string CLIENT_TRUSTSTORE_PATH = "./tests/resources/secrets/client-truststore.p12";
const string KEYSTORE_PASSWORD = "password";

// Timeouts
const decimal POLLING_INTERVAL = 1.0;
const decimal RECEIVE_TIMEOUT = 3.0;
const decimal TEST_WAIT_TIME = 2.0;
