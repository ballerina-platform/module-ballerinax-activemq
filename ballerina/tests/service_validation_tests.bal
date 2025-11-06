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

import ballerina/test;

@test:Config {
    groups: ["service", "validations"]
}
isolated function testAnnotationNotFound() returns error? {
    Service svc = service object {
        remote function onMessage(Message message, Caller caller) returns error? {
        }
    };
    Error? result = activemqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: Service configuration annotation is required.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["service", "validations"]
}
isolated function testSvcWithResourceMethods() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        resource function get .() returns error? {
        }

        remote function onMessage(Message message, Caller caller) returns error? {
        }
    };
    Error? result = activemqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: ActiveMQ service cannot have resource methods.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["service", "validations"]
}
isolated function testSvcWithNoRemoteMethods() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {};
    Error? result = activemqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: ActiveMQ service must have exactly one or two remote methods.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["service", "validations"]
}
isolated function testSvcWithInvalidRemoteMethod() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        remote function onRequest(Message message, Caller caller) returns error? {
        }
    };
    Error? result = activemqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: Invalid remote method name: onRequest.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["service", "validations"]
}
isolated function testSvcWithInvalidOnMessageParams() returns error? {
    Service svc = @ServiceConfig {
        queueName: "test-svc-params"
    } service object {
        // Invalid - missing Message parameter
        remote function onMessage(Caller caller) returns error? {
        }
    };
    Error? result = activemqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertTrue(result.message().includes("Required parameter 'activemq:Message' cannot be found"),
                "Expected error about missing Message parameter");
    }
}

@test:Config {
    groups: ["service", "validations"]
}
isolated function testSvcWithInvalidOnErrorParams() returns error? {
    Service svc = @ServiceConfig {
        queueName: "test-svc-error"
    } service object {
        remote function onMessage(Message message) returns error? {
        }

        // Invalid - onError should have exactly one error parameter
        remote function onError(Message message) returns error? {
        }
    };
    Error? result = activemqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertTrue(result.message().includes("onError method parameter must be of type 'activemq:Error'"),
                "Expected error about invalid onError parameter");
    }
}
