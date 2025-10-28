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

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

import java.util.Objects;

import static io.ballerina.lib.activemq.util.CommonUtils.createError;

/**
 * Native class for the Ballerina ActiveMQ Caller.
 *
 * @since 0.1.0
 */
public class Caller {
    static final String NATIVE_MESSAGE = "native.message";
    static final String NATIVE_SESSION = "native.session";

    private Caller() {
    }

    public static Object commit(BObject caller) {
        Session nativeSession = (Session) caller.getNativeData(NATIVE_SESSION);
        try {
            nativeSession.commit();
        } catch (JMSException exception) {
            return createError("Error",
                    String.format("Error while committing the transaction: %s", exception.getMessage()), exception);
        }
        return null;
    }

    public static Object rollback(BObject caller) {
        Session nativeSession = (Session) caller.getNativeData(NATIVE_SESSION);
        try {
            nativeSession.rollback();
        } catch (JMSException exception) {
            return createError("Error",
                    String.format("Error while rolling back the transaction: %s", exception.getMessage()),
                    exception);
        }
        return null;
    }

    public static Object acknowledge(BMap<BString, Object> message) {
        try {
            Object nativeMessage = message.getNativeData(NATIVE_MESSAGE);
            if (Objects.nonNull(nativeMessage)) {
                ((Message) nativeMessage).acknowledge();
            }
        } catch (JMSException exception) {
            return createError("Error",
                    String.format("Error occurred while sending acknowledgement for the message: %s",
                            exception.getMessage()), exception);
        }
        return null;
    }
}
