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

import static io.ballerina.lib.activemq.util.ActiveMQConstants.BACK_OFF_MULTIPLIER;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.COLLISION_AVOIDANCE_PERCENT;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.INITIAL_REDELIVERY_DELAY;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MAXIMUM_REDELIVERIES;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.MAXIMUM_REDELIVERY_DELAY;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PRE_DISPATCH_CHECK;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.REDELIVERY_DELAY;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.USE_COLLISION_AVOIDANCE;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.USE_EXPONENTIAL_BACK_OFF;

public record RedeliveryPolicyConfig(
        short collisionAvoidancePercent,
        int maximumRedeliveries,
        int maximumRedeliveryDelay,
        int initialRedeliveryDelay,
        boolean useCollisionAvoidance,
        boolean useExponentialBackOff,
        double backOffMultiplier,
        int redeliveryDelay,
        boolean preDispatchCheck
) {
    public RedeliveryPolicyConfig(BMap<BString, Object> redeliveryPolicyMap) {
        this(
                redeliveryPolicyMap.getIntValue(COLLISION_AVOIDANCE_PERCENT).shortValue(),
                redeliveryPolicyMap.getIntValue(MAXIMUM_REDELIVERIES).intValue(),
                redeliveryPolicyMap.getIntValue(MAXIMUM_REDELIVERY_DELAY).intValue(),
                redeliveryPolicyMap.getIntValue(INITIAL_REDELIVERY_DELAY).intValue(),
                redeliveryPolicyMap.getBooleanValue(USE_COLLISION_AVOIDANCE),
                redeliveryPolicyMap.getBooleanValue(USE_EXPONENTIAL_BACK_OFF),
                redeliveryPolicyMap.getFloatValue(BACK_OFF_MULTIPLIER),
                redeliveryPolicyMap.getIntValue(REDELIVERY_DELAY).intValue(),
                redeliveryPolicyMap.getBooleanValue(PRE_DISPATCH_CHECK)
        );
    }
}
