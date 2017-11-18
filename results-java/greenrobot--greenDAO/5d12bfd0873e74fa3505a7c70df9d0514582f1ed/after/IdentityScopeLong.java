/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.greenrobot.dao;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The context for entity identities. Provides the scope in which entities will be tracked and managed.
 *
 * @author Markus
 * @param <K>
 * @param <T>
 */
public class IdentityScopeLong<T> implements IdentityScope<Long, T> {
    private final LongHashMap<WeakReference<T>> identityScope;
    private final ReentrantLock lock;

    public IdentityScopeLong() {
        identityScope = new LongHashMap<WeakReference<T>>();
        lock = new ReentrantLock();
    }

    @Override
    public T get(Long key) {
        return get2(key);
    }

    public T get2(long key) {
        lock.lock();
        WeakReference<T> ref = identityScope.get(key);
        lock.unlock();
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    @Override
    public void put(Long key, T entity) {
        put2(key, entity);
    }

    public void put2(long key, T entity) {
        lock.lock();
        identityScope.put(key, new WeakReference<T>(entity));
        lock.unlock();
    }

    @Override
    public boolean detach(Long key, T entity) {
        lock.lock();
        if (get(key) == entity && entity != null) {
            remove(key);
            lock.unlock();
            return true;
        } else {
            lock.unlock();
            return false;
        }
    }

    @Override
    public void remove(Long key) {
        lock.lock();
        identityScope.remove(key);
        lock.unlock();
    }

    @Override
    public void clear() {
        lock.lock();
        identityScope.clear();
        lock.unlock();
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

}