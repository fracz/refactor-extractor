/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.core;

import java.util.concurrent.TimeUnit;

/**
 * Distributed implementation of {@link java.util.concurrent.locks.Lock}
 * Implements reentrant lock.
 * Use {@link RSemaphore#getHoldCount()} to get a holds count.
 *
 * @author Nikita Koksharov
 *
 */

public interface RSemaphore extends RExpirable {

    void acquire() throws InterruptedException;

    void acquire(int permits) throws InterruptedException;

    boolean tryAcquire();

    boolean tryAcquire(int permits);

    boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException;

    boolean tryAcquire(int permits, long waitTime, TimeUnit unit) throws InterruptedException;

    void release();

    void release(int permits);

    int availablePermits();

}