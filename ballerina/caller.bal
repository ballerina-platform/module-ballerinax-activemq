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

# Represents an ActiveMQ caller, which can be used to mark ActiveMQ message as received.
public isolated client class Caller {

    # Mark an ActiveMQ message as received.
    #
    # + message - ActiveMQ message record
    # + return - `activemq:Error` if there is an error in the execution or else '()'
    isolated remote function acknowledge(Message message) returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Caller"
    } external;

    # Commits all messages received in this transaction and releases any locks currently held.
    # ```ballerina
    # check caller->'commit();
    # ```
    #
    # + return - An `activemq:Error` if there is an error or else `()`
    isolated remote function 'commit() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Caller"
    } external;

    # Rolls back any messages received in this transaction and releases any locks currently held.
    # ```ballerina
    # check caller->'rollback();
    # ```
    #
    # + return - An `activemq:Error` if there is an error or else `()`
    isolated remote function 'rollback() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Caller"
    } external;
}
