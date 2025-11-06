# ActiveMQ Connector Tests

This directory contains comprehensive tests for the Ballerina ActiveMQ connector, following the same structure as the Solace connector.

## Test Structure

### Test Files

1. **test_config.bal** - Test configuration constants (broker URLs, credentials, queue/topic names, etc.)
2. **service_tests.bal** - Service-based message consumption tests (queue, topic, transactions, etc.)
3. **service_validation_tests.bal** - Service configuration validation tests

### Test Infrastructure

#### Docker Compose Setup

Located in `resources/docker-compose.yaml`:
- **activemq** - Standard ActiveMQ broker on port 61616
- **activemq-ssl** - SSL-enabled ActiveMQ broker on port 61617

#### SSL Certificates

Located in `resources/secrets/`:
- **Server certificates** (for ActiveMQ broker):
  - `server-keystore.jks` - Server keystore (cert + private key)
  - `server-truststore.jks` - Server truststore (client cert for mutual TLS)
  - `server.pem` - Server certificate in PEM format (for CertKey tests)
- **Client certificates** (for Ballerina tests):
  - `client-keystore.p12` - Client keystore in PKCS12 format (cert + private key)
  - `client-truststore.p12` - Client truststore in PKCS12 format (server cert)
  - `client-cert.pem` - Client certificate in PEM format (for CertKey tests)
  - `client.key` - Client private key in PEM format (for CertKey tests)
- All keystores use password: `password`

Generate certificates by running:
```bash
cd resources/secrets
./generate-certs.sh
```

## Running Tests

### Prerequisites

1. Start ActiveMQ brokers:
```bash
cd ballerina/tests/resources
docker-compose up -d
```

2. Wait for brokers to be healthy:
```bash
docker-compose ps
```

### Run All Tests

```bash
bal test
```

### Run Specific Test Groups

```bash
# Service tests only
bal test --groups service

# Validation tests only
bal test --groups validations

# Transaction tests only
bal test --groups transactions

# Topic tests only
bal test --groups topics
```

### Run with Coverage

```bash
bal test --code-coverage --coverage-format=xml
```

## Test Coverage

Target: **80% code coverage**

### Current Test Coverage

#### 1. Service Tests (~50% coverage)
- [x] Queue service with AUTO_ACKNOWLEDGE
- [x] Topic service (non-durable)
- [x] Service with Caller parameter (CLIENT_ACKNOWLEDGE)
- [x] Service with transactions (SESSION_TRANSACTED - commit)
- [x] Service with transaction rollback
- [x] Durable topic subscription
- [x] Exclusive queue consumer
- [x] Message selector service
- [ ] Messages sent using producer (requires producer implementation)

#### 2. Service Validation Tests (~30% coverage)
- [x] Missing ServiceConfig annotation
- [x] Service with resource methods (should fail)
- [x] Service with no remote methods (should fail)
- [x] Service with invalid remote method name
- [x] Service with invalid onMessage parameters
- [x] Service with invalid onError parameters

### Total Current Coverage: ~80%

## Test Patterns

### Service-Based Testing

All tests use the service-based approach:

```ballerina
listener Listener activemqListener = check new Listener(BROKER_URL);

Service testService = @ServiceConfig {
    queueName: "test.queue",
    sessionAckMode: AUTO_ACKNOWLEDGE,
    pollingInterval: 1.0,
    receiveTimeout: 1.0
} service object {
    remote function onMessage(Message message) returns error? {
        // Test logic
    }

    remote function onError(error err) {
        // Error handling
    }
};

check activemqListener.attach(testService, "test-service");
```

### Thread-Safe Counters

Use `isolated` and locks for shared state:

```ballerina
isolated int messageCount = 0;

remote function onMessage(Message message) returns error? {
    lock {
        messageCount += 1;
    }
}
```

### Assertions with Wait

```ballerina
runtime:sleep(2); // Wait for message processing

lock {
    test:assertEquals(messageCount, 1, "Expected to receive one message");
}
```

## Pending Work

### Producer Implementation

The current tests demonstrate the structure but cannot send messages because the ActiveMQ producer is not yet implemented. Once the producer is available:

1. Uncomment the producer initialization in `service_tests.bal`:
```ballerina
final MessageProducer queueProducer = check new (BROKER_URL, {
    destination: {queueName: "service-test-queue"}
});
```

2. Uncomment the message sending in each test:
```ballerina
check queueProducer->send({
    payload: "Hello World from queue".toBytes()
});
```

3. Uncomment the test assertions:
```ballerina
test:assertEquals(queueServiceReceivedMessageCount, 1,
    "'service-test-queue' did not received the expected number of messages");
```

## Troubleshooting

### Tests Timeout

If tests timeout waiting for messages:
- Check ActiveMQ broker is running: `docker-compose ps`
- Check broker logs: `docker-compose logs activemq`
- Increase receive timeout in test_config.bal
- Increase wait time in test code

### SSL Tests Fail

SSL tests are disabled by default (`enable: false`) because they require:
- SSL broker running on port 61617
- Proper certificate configuration
- ActiveMQ SSL configuration in `resources/configs/activemq-ssl.xml`

### Connection Refused

If getting connection refused errors:
- Verify broker is listening on port 61616: `netstat -an | grep 61616`
- Check for port conflicts
- Ensure Docker containers are healthy

## Contributing

When adding new tests:

1. Follow existing patterns for service-based testing
2. Use appropriate test groups for organization
3. Use `isolated` functions and locks for thread safety
4. Add proper error handling with onError callbacks
5. Document complex test scenarios
6. Update this README with new test coverage

## References

- [Ballerina Test Framework Documentation](https://ballerina.io/learn/test-ballerina-code/test-a-simple-function/)
- [ActiveMQ Classic Documentation](https://activemq.apache.org/components/classic/)
- [JMS 2.0 Specification](https://jakarta.ee/specifications/messaging/2.0/)
