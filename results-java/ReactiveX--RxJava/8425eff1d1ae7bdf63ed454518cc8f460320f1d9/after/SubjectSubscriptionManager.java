/**
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.subjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.operators.SafeObservableSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;

/* package */class SubjectSubscriptionManager<T> {

    private AtomicReference<State<T>> state = new AtomicReference<State<T>>(new State<T>());

    /**
     *
     * @param onSubscribe
     *            Always runs at the beginning of 'subscribe' regardless of terminal state.
     * @param onTerminated
     *            Only runs if Subject is in terminal state and the Observer ends up not being registered.
     * @return
     */
    public OnSubscribeFunc<T> getOnSubscribeFunc(final Action1<SubjectObserver<? super T>> onSubscribe, final Action1<SubjectObserver<? super T>> onTerminated) {
        return new OnSubscribeFunc<T>() {
            @Override
            public Subscription onSubscribe(Observer<? super T> actualObserver) {
                SubjectObserver<T> observer = new SubjectObserver<T>(actualObserver);
                // invoke onSubscribe logic
                if (onSubscribe != null) {
                    onSubscribe.call(observer);
                }

                State<T> current = null;
                State<T> newState = null;
                boolean addedObserver = false;
                Subscription s;
                do {
                    current = state.get();
                    if (current.terminated) {
                        // we are terminated so don't need to do anything
                        s = Subscriptions.empty();
                        addedObserver = false;
                        // break out and don't try to modify state
                        newState = current;
                        // wait for termination to complete if
                        try {
                            current.terminationLatch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Interrupted waiting for termination.", e);
                        }
                        break;
                    } else {
                        final SafeObservableSubscription subscription = new SafeObservableSubscription();
                        s = subscription;
                        addedObserver = true;
                        subscription.wrap(new Subscription() {
                            @Override
                            public void unsubscribe() {
                                State<T> current;
                                State<T> newState;
                                do {
                                    current = state.get();
                                    // on unsubscribe remove it from the map of outbound observers to notify
                                    newState = current.removeObserver(subscription);
                                } while (!state.compareAndSet(current, newState));
                            }
                        });

                        // on subscribe add it to the map of outbound observers to notify
                        newState = current.addObserver(subscription, observer);
                    }
                } while (!state.compareAndSet(current, newState));

                /**
                 * Whatever happened above, if we are terminated we run `onTerminated`
                 */
                if (newState.terminated && !addedObserver) {
                    onTerminated.call(observer);
                }

                return s;
            }

        };
    }

    protected void terminate(Action1<Collection<SubjectObserver<? super T>>> onTerminate) {
        State<T> current = null;
        State<T> newState = null;
        do {
            current = state.get();
            if (current.terminated) {
                // already terminated so do nothing
                return;
            } else {
                newState = current.terminate();
            }
        } while (!state.compareAndSet(current, newState));

        /*
         * if we get here then we won setting the state to terminated
         * and have a deterministic set of Observers to emit to (concurrent subscribes
         * will have failed and will try again and see we are term
         * inated)
         */
        try {
            // had to circumvent type check, we know what the array contains
            onTerminate.call((Collection)newState.observersList);
        } finally {
            // mark that termination is completed
            newState.terminationLatch.countDown();
        }
    }

    /**
     * Current snapshot of 'state.observers.keySet()' so that concurrent modifications aren't included.
     *
     * This makes it behave deterministically in a single-threaded execution when nesting subscribes.
     *
     * In multi-threaded execution it will cause new subscriptions to wait until the following onNext instead
     * of possibly being included in the current onNext iteration.
     *
     * @return List<Observer<T>>
     */
    private Collection<SubjectObserver<? super T>> snapshotOfObservers() {
        // had to circumvent type check, we know what the array contains
        return (Collection)state.get().observersList;
    }
    /**
     * Returns the array of observers directly.
     * <em>Don't modify the array!</em>
     * @return the array of current observers
     */
    public SubjectObserver<Object>[] rawSnapshot() {
        return state.get().observers;
    }

    protected static class State<T> {
        final boolean terminated;
        final CountDownLatch terminationLatch;
        final Subscription[] subscriptions;
        final SubjectObserver<Object>[] observers;
        // to avoid lots of empty arrays
        final Subscription[] EMPTY_S = new Subscription[0];
        @SuppressWarnings("rawtypes")
        // to avoid lots of empty arrays
        final SubjectObserver[] EMPTY_O = new SubjectObserver[0];
        @SuppressWarnings("rawtypes")
        final List<SubjectObserver<Object>> observersList;
        private State(boolean isTerminated, CountDownLatch terminationLatch,
                Subscription[] subscriptions, SubjectObserver[] observers) {
            this.terminationLatch = terminationLatch;
            this.terminated = isTerminated;
            this.subscriptions = subscriptions;
            this.observers = observers;
            this.observersList = Arrays.asList(this.observers);
        }

        State() {
            this.terminated = false;
            this.terminationLatch = null;
            this.subscriptions = EMPTY_S;
            this.observers = EMPTY_O;
            observersList = Collections.emptyList();
        }

        public State<T> terminate() {
            if (terminated) {
                throw new IllegalStateException("Already terminated.");
            }
            return new State<T>(true, new CountDownLatch(1), subscriptions, observers);
        }

        public State<T> addObserver(Subscription s, SubjectObserver<? super T> observer) {
            int n = this.observers.length;

            Subscription[] newsubscriptions = Arrays.copyOf(this.subscriptions, n + 1);
            SubjectObserver[] newobservers = Arrays.copyOf(this.observers, n + 1);

            newsubscriptions[n] = s;
            newobservers[n] = observer;

            return createNewWith(newsubscriptions, newobservers);
        }
        private State<T> createNewWith(Subscription[] newsubscriptions, SubjectObserver[] newobservers) {
            return new State<T>(terminated, terminationLatch, newsubscriptions, newobservers);
        }

        public State<T> removeObserver(Subscription s) {
            // we are empty, nothing to remove
            if (this.observers.length == 0) {
                return this;
            }
            int n = Math.max(this.observers.length - 1, 1);
            int copied = 0;
            Subscription[] newsubscriptions = Arrays.copyOf(this.subscriptions, n);
            SubjectObserver[] newobservers = Arrays.copyOf(this.observers, n);

            for (int i = 0; i < this.subscriptions.length; i++) {
                Subscription s0 = this.subscriptions[i];
                if (s0 != s) {
                    if (copied == n) {
                        // if s was not found till the end of the iteration
                        // we return ourselves since no modification should
                        // have happened
                        return this;
                    }
                    newsubscriptions[copied] = s0;
                    newobservers[copied] = this.observers[i];
                    copied++;
                }
            }

            if (copied == 0) {
                return createNewWith(EMPTY_S, EMPTY_O);
            }
            // if somehow copied less than expected, truncate the arrays
            // if s is unique, this should never happen
            if (copied < n) {
                return createNewWith(Arrays.copyOf(newsubscriptions, copied), Arrays.copyOf(newobservers, copied));
            }
            return createNewWith(newsubscriptions, newobservers);
        }
    }

    protected static class SubjectObserver<T> implements Observer<T> {

        private final Observer<? super T> actual;
        protected volatile boolean caughtUp = false;

        SubjectObserver(Observer<? super T> actual) {
            this.actual = actual;
        }

        @Override
        public void onCompleted() {
            this.actual.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            this.actual.onError(e);
        }

        @Override
        public void onNext(T v) {
            this.actual.onNext(v);
        }

    }

}