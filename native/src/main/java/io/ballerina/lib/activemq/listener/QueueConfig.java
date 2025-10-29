/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.activemq.listener;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.lib.activemq.util.ActiveMQConstants.EXCLUSIVE;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_SELECTOR;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MILLISECOND_MULTIPLIER;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.POLLING_INTERVAL;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.QUEUE_NAME;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.RECEIVE_TIMEOUT;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.REDELIVERY_POLICY;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.SESSION_ACK_MODE;

/**
 * Represents configuration details for consuming messages from an ActiveMQ queue.
 *
 * @param ackMode The acknowledgement mode for message consumption. This determines how
 *                messages received by the session are acknowledged.
 *                Common values include "AUTO_ACKNOWLEDGE", "CLIENT_ACKNOWLEDGE", and "DUPS_OK_ACKNOWLEDGE".
 * @param queueName       The name of the JMS queue to consume messages from.
 * @param messageSelector An optional JMS message selector expression. Only messages with properties
 *                        matching this selector will be delivered to the consumer.
 *                        If this value is {@code null}, no selector is applied.
 * @param pollingInterval   The polling interval in milliseconds
 * @param receiveTimeout    The timeout to wait till a `receive` action finishes when there are no messages
 * @param exclusive       Whether the queue is exclusive to the connection.
 * @param redeliveryPolicyConfig The redelivery policy configuration for handling message redelivery
 *
 * @since 0.1.0
 */
public record QueueConfig(String ackMode, String queueName, String messageSelector, long pollingInterval,
                          long receiveTimeout, boolean exclusive, RedeliveryPolicyConfig redeliveryPolicyConfig)
        implements ServiceConfig {

    @SuppressWarnings("unchecked")
    QueueConfig(BMap<BString, Object> configurations) {
        this(
                configurations.getStringValue(SESSION_ACK_MODE).getValue(),
                configurations.getStringValue(QUEUE_NAME).getValue(),
                configurations.containsKey(MESSAGE_SELECTOR) ?
                        configurations.getStringValue(MESSAGE_SELECTOR).getValue() : null,
                ((BDecimal) configurations.get(POLLING_INTERVAL)).decimalValue().multiply(MILLISECOND_MULTIPLIER)
                        .longValue(),
                ((BDecimal) configurations.get(RECEIVE_TIMEOUT)).decimalValue().multiply(MILLISECOND_MULTIPLIER)
                        .longValue(),
                configurations.getBooleanValue(EXCLUSIVE),
                configurations.getMapValue(REDELIVERY_POLICY) != null ?
                        new RedeliveryPolicyConfig(
                                (BMap<BString, Object>) configurations.getMapValue(REDELIVERY_POLICY)) : null

        );
    }
}
