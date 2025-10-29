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

# Represents an ActiveMQ listener that can be used to receive messages from ActiveMQ queues or
# topics.
public isolated class Listener {

    # Initializes the ActiveMQ listener with the specified broker URL and connection configurations.
    #
    # ```ballerina
    # activemq:Listener listener = check new ("tcp://localhost:61616",
    #     username = "admin",
    #     password = "admin"
    # );
    # ```
    #
    # + url - The URL of the ActiveMQ broker. Supported formats:
    #         - TCP: `"tcp://localhost:61616"`
    #         - SSL: `"ssl://localhost:61617"`
    #         - Failover: `"failover:(tcp://host1:61616,tcp://host2:61616)"`
    # + configurations - The connection configurations including authentication, SSL, and policies
    # + return - `activemq:Error` if the initialization fails, `()` otherwise
    public isolated function init(string url, *ConnectionConfiguration configurations) returns Error? {
        return self.initListener(url, configurations);
    }

    # Attaches an ActiveMQ service to the listener to start consuming messages.
    #
    # + s - The ActiveMQ service to attach (must be annotated with `@activemq:ServiceConfig`)
    # + name - Optional parameter, reserved for future use (currently ignored)
    # + return - `activemq:Error` if the attachment fails, `()` otherwise
    public isolated function attach(Service s, string[]|string? name = ()) returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    # Detaches an ActiveMQ service from the listener, stopping message consumption for that service.
    #
    # + s - The ActiveMQ service to detach
    # + return - `activemq:Error` if the detachment fails, `()` otherwise
    public isolated function detach(Service s) returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    # Starts the ActiveMQ listener and begins consuming messages for all attached services.
    #
    # + return - `activemq:Error` if starting fails, `()` otherwise
    public isolated function 'start() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    # Gracefully stops the ActiveMQ listener. Waits for currently processing messages to complete
    # before closing the connection.
    #
    # + return - `activemq:Error` if stopping fails, `()` otherwise
    public isolated function gracefulStop() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    # Immediately stops the ActiveMQ listener without waiting for in-progress message processing
    # to complete. May result in message loss or partial processing.
    #
    # + return - `activemq:Error` if stopping fails, `()` otherwise
    public isolated function immediateStop() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    isolated function initListener(string url, ConnectionConfiguration configurations) returns Error? = @java:Method {
        name: "init",
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;
}
