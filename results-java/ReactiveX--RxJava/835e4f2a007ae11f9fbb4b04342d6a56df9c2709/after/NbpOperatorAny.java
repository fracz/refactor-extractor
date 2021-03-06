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

import io.reactivex.Observer;
import io.reactivex.Observable.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.subscriptions.SubscriptionHelper;

public final class NbpOperatorAny<T> implements NbpOperator<Boolean, T> {
    final Predicate<? super T> predicate;
    public NbpOperatorAny(Predicate<? super T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Observer<? super T> apply(Observer<? super Boolean> t) {
        return new AnySubscriber<T>(t, predicate);
    }

    static final class AnySubscriber<T> implements Observer<T> {

        final Observer<? super Boolean> actual;
        final Predicate<? super T> predicate;

        Disposable s;

        boolean done;

        public AnySubscriber(Observer<? super Boolean> actual, Predicate<? super T> predicate) {
            this.actual = actual;
            this.predicate = predicate;
        }
        @Override
        public void onSubscribe(Disposable s) {
            if (SubscriptionHelper.validateDisposable(this.s, s)) {
                return;
            }
            this.s = s;
            actual.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            boolean b;
            try {
                b = predicate.test(t);
            } catch (Throwable e) {
                done = true;
                s.dispose();
                actual.onError(e);
                return;
            }
            if (b) {
                done = true;
                s.dispose();
                actual.onNext(true);
                actual.onComplete();
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!done) {
                done = true;
                actual.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                actual.onNext(false);
                actual.onComplete();
            }
        }
    }
}