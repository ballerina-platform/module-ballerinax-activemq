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

import java.util.concurrent.Semaphore;

/**
 * {@code OnMsgCallback} provides ability to control poll cycle flow by notifications received from Ballerina
 * ActiveMQ service.
 *
 * @since 0.1.0
 */
public class OnMsgCallback {
    private final Semaphore semaphore;

    OnMsgCallback(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void notifySuccess(Object obj) {
        semaphore.release();
        if (obj instanceof BError bError) {
            bError.printStackTrace();
        }
    }

    public void notifyFailure(BError bError) {
        semaphore.release();
        bError.printStackTrace();
    }
}
