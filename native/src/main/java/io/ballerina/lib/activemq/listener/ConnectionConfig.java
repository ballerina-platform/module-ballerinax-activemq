/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import io.ballerina.runtime.api.values.BString;

import java.util.UUID;

import static io.ballerina.lib.activemq.util.ActiveMQConstants.CLIENT_ID;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.OPTIMIZE_ACKNOWLEDGEMENTS;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PASSWORD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PREFETCH_POLICY;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PROPERTIES;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.REDELIVERY_POLICY;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.SECURE_SOCKET;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.SET_ALWAYS_SESSION_ASYNC;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.USERNAME;
import static io.ballerina.lib.activemq.util.CommonUtils.getOptionalStringProperty;

public record ConnectionConfig(
        String username,
        String password,
        String clientId,
        BMap<BString, Object> secureSocket,
        boolean optimizeAcknowledgements,
        boolean setAlwaysSessionAsync,
        PrefetchPolicyConfig prefetchPolicyConfig,
        RedeliveryPolicyConfig redeliveryPolicyConfig,
        BMap<BString, BString> properties
) {
    @SuppressWarnings("unchecked")
    public ConnectionConfig(BMap<BString, Object> configurations) {
        this(
                getOptionalStringProperty(configurations, USERNAME).orElse(null),
                getOptionalStringProperty(configurations, PASSWORD).orElse(null),
                getOptionalStringProperty(configurations, CLIENT_ID).orElse(UUID.randomUUID().toString()),
                (BMap<BString, Object>) configurations.getMapValue(SECURE_SOCKET),
                configurations.getBooleanValue(OPTIMIZE_ACKNOWLEDGEMENTS),
                configurations.getBooleanValue(SET_ALWAYS_SESSION_ASYNC),
                configurations.getMapValue(PREFETCH_POLICY) != null ? new PrefetchPolicyConfig(
                        (BMap<BString, Object>) configurations.getMapValue(PREFETCH_POLICY)) : null,
                configurations.getMapValue(REDELIVERY_POLICY) != null ?
                        new RedeliveryPolicyConfig(
                                (BMap<BString, Object>) configurations.getMapValue(REDELIVERY_POLICY)) : null,
                (BMap<BString, BString>) configurations.getMapValue(PROPERTIES)
        );
    }
}
