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

# Represents an ActiveMQ listener that can be used to receive messages from an ActiveMQ queue or topic.
public isolated class Listener {

    # Initializes the ActiveMQ listener.
    # + url - The URL of the ActiveMQ broker (e.g., "tcp://localhost:61616", "ssl://localhost:61617",
    #               "failover:(tcp://host1:61616,tcp://host2:61616)")
    # + configurations - The configurations to be used when initializing the ActiveMQ listener
    # + return - An error if the initialization failed, nil otherwise
    public isolated function init(string url, *ConnectionConfiguration configurations) returns Error? {
        return self.initListener(url, configurations);
    }

    # Attaches an ActiveMQ service to the ActiveMQ listener.
    # + s - The ActiveMQ Service to attach
    # + name - The name of the queue/topic to attach to
    # + return - An error if the attaching failed, nil otherwise
    public isolated function attach(Service s, string[]|string? name = ()) returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    # Detaches an ActiveMQ service from the ActiveMQ listener.
    # + s - The ActiveMQ Service to detach
    # + return - An error if the detaching failed, nil otherwise
    public isolated function detach(Service s) returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    # Starts the ActiveMQ listener.
    # + return - An error if the starting failed, nil otherwise
    public isolated function 'start() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    # Gracefully stops the ActiveMQ listener.
    # + return - An error if the stopping failed, nil otherwise
    public isolated function gracefulStop() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    # Immediately stops the ActiveMQ listener.
    # + return - An error if the stopping failed, nil otherwise
    public isolated function immediateStop() returns Error? = @java:Method {
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;

    isolated function initListener(string url, ConnectionConfiguration configurations) returns Error? = @java:Method {
        name: "init",
        'class: "io.ballerina.lib.activemq.listener.Listener"
    } external;
}
