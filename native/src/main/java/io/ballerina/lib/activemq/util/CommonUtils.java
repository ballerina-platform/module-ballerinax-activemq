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

package io.ballerina.lib.activemq.util;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import java.util.Enumeration;
import java.util.Optional;

import static io.ballerina.lib.activemq.util.ActiveMQConstants.ERROR_COMPLETION_CODE;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.ERROR_DETAILS;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.ERROR_ERROR_CODE;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.ERROR_REASON_CODE;
import static io.ballerina.lib.activemq.util.ModuleUtils.getModule;

/**
 * Utility methods for ActiveMQ connector.
 */
public class CommonUtils {

    private CommonUtils() {
    }

    /**
     * Creates a Ballerina error with type and message.
     *
     * @param errorType Error type
     * @param message Error message
     * @return Ballerina error
     */
    public static BError createError(String errorType, String message) {
        return createError(errorType, message, null);
    }

    /**
     * Creates a Ballerina error with type, message and cause.
     *
     * @param errorType Error type
     * @param message Error message
     * @param throwable Cause of the error
     * @return Ballerina error
     */
    public static BError createError(String errorType, String message, Throwable throwable) {
        BError cause = null;
        if (throwable != null) {
            cause = ErrorCreator.createError(throwable);
        }
        BMap<BString, Object> errorDetails = ValueCreator.createRecordValue(getModule(), ERROR_DETAILS);
        return ErrorCreator.createError(
                getModule(), errorType, StringUtils.fromString(message), cause, errorDetails);
    }


    /**
     * Retrieves an optional string property from the configuration map.
     *
     * @param config Ballerina map containing configuration
     * @param fieldName Field name to retrieve
     * @return Optional string value
     */
    public static Optional<String> getOptionalStringProperty(BMap<BString, Object> config, BString fieldName) {
        if (config.containsKey(fieldName)) {
            return Optional.of(config.getStringValue(fieldName).getValue());
        }
        return Optional.empty();
    }

    /**
     * Converts a JMS message to a Ballerina message record.
     *
     * @param jmsMessage JMS message
     * @return Ballerina message record
     * @throws JMSException if an error occurs while processing the message
     */
    public static BMap<BString, Object> convertToMessageRecord(Message jmsMessage) throws JMSException {
        BMap<BString, Object> messageRecord = ValueCreator.createRecordValue(
                getModule(), "Message");

        // Set message ID
        String messageId = jmsMessage.getJMSMessageID();
        if (messageId != null) {
            messageRecord.put(ActiveMQConstants.MESSAGE_ID, StringUtils.fromString(messageId));
        } else {
            messageRecord.put(ActiveMQConstants.MESSAGE_ID, StringUtils.fromString(""));
        }

        // Set correlation ID
        String correlationId = jmsMessage.getJMSCorrelationID();
        if (correlationId != null) {
            messageRecord.put(ActiveMQConstants.CORRELATION_ID, StringUtils.fromString(correlationId));
        }

        // Set timestamp
        messageRecord.put(ActiveMQConstants.TIMESTAMP, jmsMessage.getJMSTimestamp());

        // Set redelivered flag
        messageRecord.put(ActiveMQConstants.REDELIVERED, jmsMessage.getJMSRedelivered());

        // Set type
        String type = jmsMessage.getJMSType();
        if (type != null) {
            messageRecord.put(ActiveMQConstants.TYPE, StringUtils.fromString(type));
        }

        // Set destination
        Destination destination = jmsMessage.getJMSDestination();
        if (destination != null) {
            messageRecord.put(ActiveMQConstants.DESTINATION_FIELD,
                    StringUtils.fromString(destination.toString()));
        } else {
            messageRecord.put(ActiveMQConstants.DESTINATION_FIELD, StringUtils.fromString(""));
        }

        // Set reply-to
        Destination replyTo = jmsMessage.getJMSReplyTo();
        if (replyTo != null) {
            messageRecord.put(ActiveMQConstants.REPLY_TO, StringUtils.fromString(replyTo.toString()));
        }

        // Set payload (assuming TextMessage for now)
        String payload = "";
        if (jmsMessage instanceof TextMessage) {
            payload = ((TextMessage) jmsMessage).getText();
        }
        messageRecord.put(ActiveMQConstants.PAYLOAD, StringUtils.fromString(payload != null ? payload : ""));

        // Set properties
        BMap<BString, Object> properties = ValueCreator.createMapValue();
        Enumeration<?> propertyNames = jmsMessage.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            Object propertyValue = jmsMessage.getObjectProperty(propertyName);
            if (propertyValue != null) {
                properties.put(StringUtils.fromString(propertyName), propertyValue);
            }
        }
        messageRecord.put(ActiveMQConstants.PROPERTIES, properties);

        return messageRecord;
    }

    /**
     * Gets the acknowledgement mode from the string value.
     *
     * @param ackMode Acknowledgement mode string
     * @return JMS acknowledgement mode constant
     */
    public static int getAcknowledgementMode(String ackMode) {
        return switch (ackMode) {
            case ActiveMQConstants.SESSION_TRANSACTED_MODE -> jakarta.jms.Session.SESSION_TRANSACTED;
            case ActiveMQConstants.AUTO_ACKNOWLEDGE_MODE -> jakarta.jms.Session.AUTO_ACKNOWLEDGE;
            case ActiveMQConstants.CLIENT_ACKNOWLEDGE_MODE -> jakarta.jms.Session.CLIENT_ACKNOWLEDGE;
            case null, default -> jakarta.jms.Session.DUPS_OK_ACKNOWLEDGE;
        };
    }
}
