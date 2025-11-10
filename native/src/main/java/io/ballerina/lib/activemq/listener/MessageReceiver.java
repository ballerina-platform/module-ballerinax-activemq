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
 * A message receiver that periodically polls messages from ActiveMQ and dispatches them to the
 * Ballerina service using the message dispatcher. This class manages the polling lifecycle and
 * ensures graceful shutdown with proper resource cleanup.
 *
 * @since 0.1.0
 */
public class MessageReceiver {
    private static final long STOP_TIMEOUT_MS = 30000;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Session session;
    private final MessageConsumer consumer;
    private final MessageDispatcher messageDispatcher;
    private final long receiveInterval;
    private final long receiveTimeout;

    private ScheduledFuture<?> pollingTaskFuture;

    /**
     * Creates a new message receiver.
     *
     * @param session            the JMS session
     * @param consumer           the JMS message consumer
     * @param messageDispatcher  the dispatcher for delivering messages to Ballerina service
     * @param pollingInterval    the interval (in milliseconds) between polling attempts
     * @param receiveTimeout     the timeout (in milliseconds) to wait for a message
     */
    public MessageReceiver(Session session, MessageConsumer consumer, MessageDispatcher messageDispatcher,
                           long pollingInterval, long receiveTimeout) {
        this.session = session;
        this.consumer = consumer;
        this.messageDispatcher = messageDispatcher;
        this.receiveInterval = pollingInterval;
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Polls for a single message from the JMS consumer and dispatches it to the Ballerina service.
     * This method blocks until a message is received or the timeout expires. Uses a semaphore to
     * ensure messages are processed sequentially before polling for the next message.
     */
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
            // Suspend polling cycle execution until the Ballerina service completes processing.
            // The semaphore will be released by OnMsgCallback when the Ballerina runtime signals
            // completion, allowing us to move to the next polling cycle.
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

    /**
     * Starts consuming messages by scheduling periodic polling at the configured interval.
     */
    public void consume() {
        this.pollingTaskFuture = this.executorService.scheduleAtFixedRate(
                this::poll, 0, this.receiveInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the message receiver gracefully. Cancels the polling task, shuts down the executor
     * service, and closes the JMS consumer and session. Waits up to 30 seconds for graceful
     * termination before forcing shutdown.
     *
     * @throws Exception if an error occurs during shutdown
     */
    public void stop() throws Exception {
        closed.set(true);
        if (Objects.nonNull(this.pollingTaskFuture) && !this.pollingTaskFuture.isCancelled()) {
            this.pollingTaskFuture.cancel(true);
        }
        this.executorService.shutdown();
        try {
            boolean terminated = this.executorService.awaitTermination(STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!terminated) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Re-cancel if current thread is also interrupted
            this.executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        this.consumer.close();
        this.session.close();
    }
}
