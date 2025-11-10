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

import static io.ballerina.lib.activemq.util.ActiveMQConstants.DURABLE_TOPIC_PREFETCH;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.OPTIMIZE_DURABLE_TOPIC_PREFETCH_SIZE;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.QUEUE_PREFETCH;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.TOPIC_PREFETCH;

public record PrefetchPolicyConfig(
        int queuePrefetchSize,
        int topicPrefetchSize,
        int durableTopicPrefetchSize,
        int optimizeDurableTopicPrefetchSize
) {
    public PrefetchPolicyConfig(BMap<BString, Object> prefetchPolicyMap) {
        this(
                prefetchPolicyMap.getIntValue(QUEUE_PREFETCH).intValue(),
                prefetchPolicyMap.getIntValue(TOPIC_PREFETCH).intValue(),
                prefetchPolicyMap.getIntValue(DURABLE_TOPIC_PREFETCH).intValue(),
                prefetchPolicyMap.getIntValue(OPTIMIZE_DURABLE_TOPIC_PREFETCH_SIZE).intValue()
        );
    }
}
