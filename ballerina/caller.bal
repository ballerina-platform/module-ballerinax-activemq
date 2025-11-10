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

import ballerina/jballerina.java;

# Represents an ActiveMQ caller, which provides operations for message acknowledgement and
# transaction management.
public isolated client class Caller {

    # Acknowledges an ActiveMQ message. This operation is only meaningful when using
    # `CLIENT_ACKNOWLEDGE` mode. In this mode, calling `acknowledge` on one message acknowledges
    # all messages that have been received in the session up to and including this message.
    #
    # ```ballerina
    # remote function onMessage(activemq:Message message, activemq:Caller caller) returns error? {
    #     // Process the message
    #     check caller->acknowledge(message);
    # }
    # ```
    #
    # + message - The ActiveMQ message to acknowledge
    # + return - `activemq:Error` if acknowledgement fails, `()` otherwise
    isolated remote function acknowledge(Message message) returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Caller"
    } external;

    # Commits all messages received in this transacted session and releases any locks currently
    # held. This operation is only valid when using `SESSION_TRANSACTED` acknowledgement mode.
    # After committing, a new transaction is automatically started.
    #
    # ```ballerina
    # check caller->'commit();
    # ```
    #
    # + return - `activemq:Error` if the commit fails, `()` otherwise
    isolated remote function 'commit() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Caller"
    } external;

    # Rolls back any messages received in this transacted session and releases any locks currently
    # held. This operation is only valid when using `SESSION_TRANSACTED` acknowledgement mode.
    # Rolled back messages will be redelivered according to the configured redelivery policy.
    #
    # ```ballerina
    # check caller->'rollback();
    # ```
    #
    # + return - `activemq:Error` if the rollback fails, `()` otherwise
    isolated remote function 'rollback() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Caller"
    } external;
}
