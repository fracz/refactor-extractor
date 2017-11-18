/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.internal.operators.observable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.*;
import io.reactivex.internal.subscribers.observable.*;
import io.reactivex.observers.*;
import io.reactivex.plugins.RxJavaPlugins;

public final class ObservableTimeout<T, U, V> extends AbstractObservableWithUpstream<T, T> {
    final ObservableSource<U> firstTimeoutIndicator;
    final Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator;
    final ObservableSource<? extends T> other;

    public ObservableTimeout(
            ObservableSource<T> source,
            ObservableSource<U> firstTimeoutIndicator,
            Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator,
                    ObservableSource<? extends T> other) {
        super(source);
        this.firstTimeoutIndicator = firstTimeoutIndicator;
        this.itemTimeoutIndicator = itemTimeoutIndicator;
        this.other = other;
    }

    @Override
    public void subscribeActual(Observer<? super T> t) {
        if (other == null) {
            source.subscribe(new TimeoutSubscriber<T, U, V>(
                    new SerializedObserver<T>(t),
                    firstTimeoutIndicator, itemTimeoutIndicator));
        } else {
            source.subscribe(new TimeoutOtherSubscriber<T, U, V>(
                    t, firstTimeoutIndicator, itemTimeoutIndicator, other));
        }
    }

    static final class TimeoutSubscriber<T, U, V>
    extends AtomicReference<Disposable>
    implements Observer<T>, Disposable, OnTimeout {

        private static final long serialVersionUID = 2672739326310051084L;
        final Observer<? super T> actual;
        final ObservableSource<U> firstTimeoutIndicator;
        final Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator;

        Disposable s;

        volatile long index;

        TimeoutSubscriber(Observer<? super T> actual,
                ObservableSource<U> firstTimeoutIndicator,
                Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator) {
            this.actual = actual;
            this.firstTimeoutIndicator = firstTimeoutIndicator;
            this.itemTimeoutIndicator = itemTimeoutIndicator;
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                this.s = s;

                Observer<? super T> a = actual;

                ObservableSource<U> p = firstTimeoutIndicator;

                if (p != null) {
                    TimeoutInnerSubscriber<T, U, V> tis = new TimeoutInnerSubscriber<T, U, V>(this, 0);

                    if (compareAndSet(null, tis)) {
                        a.onSubscribe(this);
                        p.subscribe(tis);
                    }
                } else {
                    a.onSubscribe(this);
                }
            }
        }

        @Override
        public void onNext(T t) {
            long idx = index + 1;
            index = idx;

            actual.onNext(t);

            Disposable d = get();
            if (d != null) {
                d.dispose();
            }

            ObservableSource<V> p;

            try {
                p = itemTimeoutIndicator.apply(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                dispose();
                actual.onError(e);
                return;
            }

            if (p == null) {
                dispose();
                actual.onError(new NullPointerException("The ObservableSource returned is null"));
                return;
            }

            TimeoutInnerSubscriber<T, U, V> tis = new TimeoutInnerSubscriber<T, U, V>(this, idx);

            if (compareAndSet(d, tis)) {
                p.subscribe(tis);
            }
        }

        @Override
        public void onError(Throwable t) {
            dispose();
            actual.onError(t);
        }

        @Override
        public void onComplete() {
            dispose();
            actual.onComplete();
        }

        @Override
        public void dispose() {
            if (DisposableHelper.dispose(this)) {
                s.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return s.isDisposed();
        }

        @Override
        public void timeout(long idx) {
            if (idx == index) {
                dispose();
                actual.onError(new TimeoutException());
            }
        }
    }

    interface OnTimeout {
        void timeout(long index);

        void onError(Throwable e);
    }

    static final class TimeoutInnerSubscriber<T, U, V> extends DisposableObserver<Object> {
        final OnTimeout parent;
        final long index;

        boolean done;

        TimeoutInnerSubscriber(OnTimeout parent, final long index) {
            this.parent = parent;
            this.index = index;
        }

        @Override
        public void onNext(Object t) {
            if (done) {
                return;
            }
            done = true;
            dispose();
            parent.timeout(index);
        }

        @Override
        public void onError(Throwable t) {
            parent.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            parent.timeout(index);
        }
    }

    static final class TimeoutOtherSubscriber<T, U, V>
    extends AtomicReference<Disposable>
    implements Observer<T>, Disposable, OnTimeout {

        private static final long serialVersionUID = -1957813281749686898L;
        final Observer<? super T> actual;
        final ObservableSource<U> firstTimeoutIndicator;
        final Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator;
        final ObservableSource<? extends T> other;
        final ObserverFullArbiter<T> arbiter;

        Disposable s;

        boolean done;

        volatile long index;

        TimeoutOtherSubscriber(Observer<? super T> actual,
                                      ObservableSource<U> firstTimeoutIndicator,
                                      Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator, ObservableSource<? extends T> other) {
            this.actual = actual;
            this.firstTimeoutIndicator = firstTimeoutIndicator;
            this.itemTimeoutIndicator = itemTimeoutIndicator;
            this.other = other;
            this.arbiter = new ObserverFullArbiter<T>(actual, this, 8);
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                this.s = s;

                if (!arbiter.setSubscription(s)) {
                    return;
                }
                Observer<? super T> a = actual;

                ObservableSource<U> p = firstTimeoutIndicator;

                if (p != null) {
                    TimeoutInnerSubscriber<T, U, V> tis = new TimeoutInnerSubscriber<T, U, V>(this, 0);

                    if (compareAndSet(null, tis)) {
                        a.onSubscribe(arbiter);
                        p.subscribe(tis);
                    }
                } else {
                    a.onSubscribe(arbiter);
                }
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            long idx = index + 1;
            index = idx;

            if (!arbiter.onNext(t, s)) {
                return;
            }

            Disposable d = get();
            if (d != null) {
                d.dispose();
            }

            ObservableSource<V> p;

            try {
                p = itemTimeoutIndicator.apply(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                actual.onError(e);
                return;
            }

            if (p == null) {
                actual.onError(new NullPointerException("The ObservableSource returned is null"));
                return;
            }

            TimeoutInnerSubscriber<T, U, V> tis = new TimeoutInnerSubscriber<T, U, V>(this, idx);

            if (compareAndSet(d, tis)) {
                p.subscribe(tis);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            dispose();
            arbiter.onError(t, s);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            dispose();
            arbiter.onComplete(s);
        }

        @Override
        public void dispose() {
            if (DisposableHelper.dispose(this)) {
                s.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return s.isDisposed();
        }

        @Override
        public void timeout(long idx) {
            if (idx == index) {
                dispose();
                other.subscribe(new FullArbiterObserver<T>(arbiter));
            }
        }
    }
}