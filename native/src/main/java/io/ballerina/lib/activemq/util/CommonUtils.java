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

import java.util.Optional;

import static io.ballerina.lib.activemq.util.ActiveMQConstants.ERROR_DETAILS;
import static io.ballerina.lib.activemq.util.ModuleUtils.getModule;

/**
 * Utility methods for the ActiveMQ connector including error creation, configuration retrieval,
 * and acknowledgement mode conversion.
 */
public class CommonUtils {

    private CommonUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a Ballerina error with the specified type and message.
     *
     * @param errorType  the error type identifier
     * @param message    the error message
     * @return a Ballerina error object
     */
    public static BError createError(String errorType, String message) {
        return createError(errorType, message, null);
    }

    /**
     * Creates a Ballerina error with the specified type, message, and cause.
     *
     * @param errorType  the error type identifier
     * @param message    the error message
     * @param throwable  the underlying cause of the error (can be null)
     * @return a Ballerina error object with error details
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
     * Retrieves an optional string property from a Ballerina configuration map.
     *
     * @param config     the Ballerina configuration map
     * @param fieldName  the field name to retrieve
     * @return Optional containing the string value if present, empty otherwise
     */
    public static Optional<String> getOptionalStringProperty(BMap<BString, Object> config, BString fieldName) {
        if (config.containsKey(fieldName)) {
            return Optional.of(config.getStringValue(fieldName).getValue());
        }
        return Optional.empty();
    }

    /**
     * Converts a Ballerina acknowledgement mode string to the corresponding JMS session constant.
     *
     * @param ackMode  the Ballerina acknowledgement mode string
     * @return the JMS session acknowledgement mode constant
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
