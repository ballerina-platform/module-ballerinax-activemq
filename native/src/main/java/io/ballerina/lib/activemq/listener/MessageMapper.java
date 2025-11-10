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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import static io.ballerina.lib.activemq.util.ActiveMQConstants.BMESSAGE_NAME;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.CORRELATION_ID;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.DELIVERY_TIME_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.DESTINATION_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.EXPIRY_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.FORMAT_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_ID;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_PAYLOAD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_PROPERTIES;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_USERID;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PERSISTENT_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PRIORITY_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.REDELIVERED_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.REPLY_TO;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.TIMESTAMP_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.TYPE_FIELD;
import static io.ballerina.lib.activemq.util.ModuleUtils.getModule;

/**
 * MessageMapper maps JMS messages to Ballerina message records.
 *
 * @since 0.1.0
 */
public class MessageMapper {
    static final BString TEXT = StringUtils.fromString("text");
    static final BString BINARY = StringUtils.fromString("binary");
    static final BString UNKNOWN = StringUtils.fromString("unknown");
    static final String NATIVE_MESSAGE = "native.message";

    public static BMap<BString, Object> toBallerinaMessage(Message message) throws JMSException {
        BMap<BString, Object> result = ValueCreator.createRecordValue(getModule(), BMESSAGE_NAME);

        // Standard JMS message headers
        result.put(MESSAGE_ID, StringUtils.fromString(message.getJMSMessageID()));

        long timestamp = message.getJMSTimestamp();
        if (timestamp > 0) {
            result.put(TIMESTAMP_FIELD, timestamp);
        }

        String correlationId = message.getJMSCorrelationID();
        if (correlationId != null) {
            result.put(CORRELATION_ID, StringUtils.fromString(correlationId));
        }

        if (message.getJMSReplyTo() != null) {
            result.put(REPLY_TO, StringUtils.fromString(message.getJMSReplyTo().toString()));
        }

        if (message.getJMSDestination() != null) {
            result.put(DESTINATION_FIELD, StringUtils.fromString(message.getJMSDestination().toString()));
        }

        // Convert JMSDeliveryMode (1=non-persistent, 2=persistent) to boolean
        result.put(PERSISTENT_FIELD, message.getJMSDeliveryMode() == 2);

        result.put(REDELIVERED_FIELD, message.getJMSRedelivered());

        String type = message.getJMSType();
        if (type != null) {
            result.put(TYPE_FIELD, StringUtils.fromString(type));
        }

        long expiry = message.getJMSExpiration();
        if (expiry > 0) {
            result.put(EXPIRY_FIELD, expiry);
        }

        long deliveryTime = message.getJMSDeliveryTime();
        if (deliveryTime > 0) {
            result.put(DELIVERY_TIME_FIELD, deliveryTime);
        }

        int priority = message.getJMSPriority();
        if (priority > 0) {
            result.put(PRIORITY_FIELD, priority);
        }

        String userID = message.getStringProperty("JMSXUserID");
        if (userID != null) {
            result.put(MESSAGE_USERID, StringUtils.fromString(userID));
        }

        // Custom Properties
        BMap<BString, Object> props = ValueCreator.createMapValue();
        Enumeration<?> propNames = message.getPropertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            Object value = message.getObjectProperty(name);
            props.put(StringUtils.fromString(name), value);
        }
        result.put(MESSAGE_PROPERTIES, props);

        // Payload - convert byte arrays to Ballerina arrays
        if (message instanceof TextMessage) {
            byte[] payload = ((TextMessage) message).getText().getBytes(StandardCharsets.UTF_8);
            result.put(MESSAGE_PAYLOAD, ValueCreator.createArrayValue(payload));
            result.put(FORMAT_FIELD, TEXT);

        } else if (message instanceof BytesMessage bytesMessage) {
            byte[] payload = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(payload);
            result.put(MESSAGE_PAYLOAD, ValueCreator.createArrayValue(payload));
            result.put(FORMAT_FIELD, BINARY);

        } else {
            // fallback: try getBody
            byte[] fallback;
            fallback = message.getBody(String.class).getBytes(StandardCharsets.UTF_8);
            result.put(MESSAGE_PAYLOAD, ValueCreator.createArrayValue(fallback));
            result.put(FORMAT_FIELD, UNKNOWN);
        }
        result.addNativeData(NATIVE_MESSAGE, message);
        return result;
    }
}
