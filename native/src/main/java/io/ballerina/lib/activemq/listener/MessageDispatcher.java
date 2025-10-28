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

import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.concurrent.StrandMetadata;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

import java.io.PrintStream;
import java.util.Optional;

import static io.ballerina.lib.activemq.listener.Caller.NATIVE_SESSION;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.BCALLER_NAME;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.ON_ERROR_METHOD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.ON_MESSAGE_METHOD;
import static io.ballerina.lib.activemq.util.CommonUtils.createError;
import static io.ballerina.lib.activemq.util.ModuleUtils.getModule;

/**
 * A {MessageDispatcher} dispatches JMS messages into the ActiveMQ service.
 *
 * @since 0.1.0
 */
public class MessageDispatcher {
    private static final PrintStream ERR_OUT = System.err;

    private final Runtime ballerinaRuntime;
    private final Service nativeService;
    private final Session session;
    private final OnErrorCallback onErrorCallback = new OnErrorCallback();

    MessageDispatcher(Runtime ballerinaRuntime, Service nativeService, Session session) {
        this.ballerinaRuntime = ballerinaRuntime;
        this.nativeService = nativeService;
        this.session = session;
    }

    public void onMessage(Message message, OnMsgCallback onMsgCallback) {
        Thread.startVirtualThread(() -> {
            try {
                boolean isConcurrentSafe = nativeService.isOnMessageMethodIsolated();
                StrandMetadata metadata = new StrandMetadata(isConcurrentSafe, null);
                Object[] params = getOnMessageParams(message);
                Object result = ballerinaRuntime.callMethod(
                        nativeService.getConsumerService(), ON_MESSAGE_METHOD, metadata, params);
                onMsgCallback.notifySuccess(result);
            } catch (BError e) {
                onMsgCallback.notifyFailure(e);
                onError(e);
            } catch (JMSException e) {
                onError(e);
            }
        });
    }

    private Object[] getOnMessageParams(Message message) throws JMSException {
        Parameter[] parameters = this.nativeService.getOnMessageMethod().getParameters();
        Object[] args = new Object[parameters.length];
        int idx = 0;
        for (Parameter param: parameters) {
            Type referredType = TypeUtils.getReferredType(param.type);
            switch (referredType.getTag()) {
                case TypeTags.OBJECT_TYPE_TAG:
                    args[idx++] = getCaller();
                    break;
                case TypeTags.RECORD_TYPE_TAG:
                    args[idx++] = MessageMapper.toBallerinaMessage(message);
                    break;
            }
        }
        return args;
    }

    private BObject getCaller() {
        BObject caller = ValueCreator.createObjectValue(getModule(), BCALLER_NAME);
        caller.addNativeData(NATIVE_SESSION, session);
        return caller;
    }

    public void onError(Throwable t) {
        Thread.startVirtualThread(() -> {
            try {
                ERR_OUT.println("Unexpected error occurred while message processing: " + t.getMessage());
                Optional<RemoteMethodType> onError = nativeService.getOnError();
                if (onError.isEmpty()) {
                    t.printStackTrace();
                    return;
                }
                BError error = createError("Error", "Failed to fetch the message", t);
                boolean isConcurrentSafe = nativeService.isOnErrorMethodIsolated();
                StrandMetadata metadata = new StrandMetadata(isConcurrentSafe, null);
                Object result = ballerinaRuntime.callMethod(
                        nativeService.getConsumerService(), ON_ERROR_METHOD, metadata, error);
                onErrorCallback.notifySuccess(result);
            } catch (BError err) {
                onErrorCallback.notifyFailure(err);
            }
        });
    }
}
