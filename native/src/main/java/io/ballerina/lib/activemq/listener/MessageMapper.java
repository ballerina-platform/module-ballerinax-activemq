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
import static io.ballerina.lib.activemq.util.ActiveMQConstants.EXPIRY_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.FORMAT_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_ID;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_PAYLOAD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_PROPERTIES;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MESSAGE_USERID;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PERSISTENCE_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PRIORITY_FIELD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.REPLY_TO;
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

        // Common properties - convert byte arrays to Ballerina arrays
        result.put(MESSAGE_ID, StringUtils.fromString(message.getJMSMessageID()));
        result.put(CORRELATION_ID, StringUtils.fromString(message.getJMSCorrelationID()));
        result.put(PRIORITY_FIELD, message.getJMSPriority());
        result.put(EXPIRY_FIELD, message.getJMSExpiration());
        result.put(PERSISTENCE_FIELD, message.getJMSDeliveryMode());
//
//
//        message.getJMSTimestamp();
//        message.getJMSDeliveryMode();
//        message.getJMSDestination();
//        message.getJMSRedelivered();

        result.put(REPLY_TO, (message.getJMSReplyTo() != null)
                ? StringUtils.fromString(message.getJMSReplyTo().toString()) : null);
        String userIdProperty = message.getStringProperty("JMSXUserID");
        result.put(MESSAGE_USERID, userIdProperty != null ? StringUtils.fromString(userIdProperty) : null);

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
