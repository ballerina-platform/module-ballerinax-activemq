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

import io.ballerina.runtime.api.values.BError;

/**
 * {@code OnErrorCallback} provides ability to handle the error results which is captured when dispatching messages to
 * the ActiveMQ service.
 *
 * @since 0.1.0
 */
public class OnErrorCallback {

    public void notifySuccess(Object result) {
        if (result instanceof BError bError) {
            bError.printStackTrace();
        }
    }

    public void notifyFailure(BError bError) {
        bError.printStackTrace();
        System.exit(1);
    }
}
