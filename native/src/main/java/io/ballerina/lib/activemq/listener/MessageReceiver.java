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

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {MessageReceiver} periodically polls messages from ActiveMQ and dispatches the messages to the ActiveMQ service
 * using the message dispatcher.
 *
 * @since 0.1.0
 */
public class MessageReceiver {
    private static final long stopTimeout = 30000;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Session session;
    private final MessageConsumer consumer;
    private final MessageDispatcher messageDispatcher;
    private final long receiveInterval;
    private final long receiveTimeout;

    private ScheduledFuture<?> pollingTaskFuture;

    public MessageReceiver(Session session, MessageConsumer consumer, MessageDispatcher messageDispatcher,
                           long pollingInterval, long receiveTimeout) {
        this.session = session;
        this.consumer = consumer;
        this.messageDispatcher = messageDispatcher;
        this.receiveInterval = pollingInterval;
        this.receiveTimeout = receiveTimeout;
    }

    private void poll() {
        try {
            Message message = null;
            if (!closed.get()) {
                message = this.consumer.receive(this.receiveTimeout);
            }
            if (Objects.isNull(message)) {
                return;
            }
            Semaphore semaphore = new Semaphore(0);
            OnMsgCallback callback = new OnMsgCallback(semaphore);
            this.messageDispatcher.onMessage(message, callback);
            // We suspend execution of poll cycle here before moving to the next cycle.
            // Once we receive signal from BVM via OnMsgCallback this suspension is removed
            // We will move to the next polling cycle.
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                this.messageDispatcher.onError(e);
                this.pollingTaskFuture.cancel(false);
            }
        } catch (JMSException e) {
            if (!closed.get()) {
                this.messageDispatcher.onError(e);
                this.pollingTaskFuture.cancel(false);
            }
        }
    }

    public void consume() {
        this.pollingTaskFuture = this.executorService.scheduleAtFixedRate(
                this::poll, 0, this.receiveInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() throws Exception {
        closed.set(true);
        if (Objects.nonNull(this.pollingTaskFuture) && !this.pollingTaskFuture.isCancelled()) {
            this.pollingTaskFuture.cancel(true);
        }
        this.executorService.shutdown();
        try {
            boolean terminated = this.executorService.awaitTermination(stopTimeout, TimeUnit.MILLISECONDS);
            if (!terminated) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            this.executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        this.consumer.close();
        this.session.close();
    }
}
