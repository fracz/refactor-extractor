/**
 * Copyright 2015 Netflix, Inc.
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
package io.reactivex;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Stream;

import org.reactivestreams.*;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.*;
import io.reactivex.internal.operators.*;
import io.reactivex.internal.subscribers.*;
import io.reactivex.internal.subscriptions.EmptySubscription;
import io.reactivex.observables.*;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.*;
import io.reactivex.subscribers.*;

public class Observable<T> implements Publisher<T> {
    final Publisher<T> onSubscribe;

    static final int BUFFER_SIZE;
    static {
        BUFFER_SIZE = Math.max(16, Integer.getInteger("rx2.buffer-size", 128));
    }

    protected Observable(Publisher<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(Publisher<T> onSubscribe) {
        Objects.requireNonNull(onSubscribe);
        onSubscribe = RxJavaPlugins.onCreate(onSubscribe);
        return new Observable<>(onSubscribe);
    }

    private void subscribeActual(Subscriber<? super T> s) {
        try {
            s = RxJavaPlugins.onSubscribe(s);

            onSubscribe.subscribe(s);
        } catch (NullPointerException e) {
            throw e;
        } catch (Throwable e) {
            // TODO throw if fatal?
            // can't call onError because no way to know if a Subscription has been set or not
            // can't call onSubscribe because the call might have set a Subscription already
            RxJavaPlugins.onError(e);
        }
    }

    // TODO decide if safe subscription or unsafe should be the default
    @Override
    public final void subscribe(Subscriber<? super T> s) {
        Objects.requireNonNull(s);
        subscribeActual(s);
    }

    // TODO decide if safe subscription or unsafe should be the default
    public final void unsafeSubscribe(Subscriber<? super T> s) {
        Objects.requireNonNull(s);
        subscribeActual(s);
    }

    // TODO decide if safe subscription or unsafe should be the default
    public final void safeSubscribe(Subscriber<? super T> s) {
        Objects.requireNonNull(s);
        if (s instanceof SafeSubscriber) {
            subscribeActual(s);
        } else {
            subscribeActual(new SafeSubscriber<>(s));
        }
    }

    /**
     * Interface to map/wrap a downstream subscriber to an upstream subscriber.
     *
     * @param <Downstream> the value type of the downstream
     * @param <Upstream> the value type of the upstream
     */
    @FunctionalInterface
    public interface Operator<Downstream, Upstream> extends Function<Subscriber<? super Downstream>, Subscriber<? super Upstream>> {

    }

    public final <R> Observable<R> lift(Operator<? extends R, ? super T> lifter) {
        Objects.requireNonNull(lifter);
        // using onSubscribe so the fusing has access to the underlying raw Publisher
        return create(new PublisherLift<>(onSubscribe, lifter));
    }

    // TODO generics
    @SuppressWarnings("unchecked")
    public final <R> Observable<R> compose(Function<? super Observable<T>, ? extends Observable<? extends R>> composer) {
        return (Observable<R>)to(composer);
    }

    // TODO generics
    public final <R> R to(Function<? super Observable<T>, R> converter) {
        return converter.apply(this);
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable<T> fromPublisher(Publisher<? extends T> publisher) {
        if (publisher instanceof Observable) {
            return (Observable<T>)publisher;
        }
        Objects.requireNonNull(publisher);

        return create(s -> publisher.subscribe(s)); // javac fails to compile publisher::subscribe, Eclipse is just fine
    }

    public static int bufferSize() {
        return BUFFER_SIZE;
    }

    public static <T> Observable<T> just(T value) {
        Objects.requireNonNull(value);
        return create(new PublisherScalarSource<>(value));
    }

    static final Observable<Object> EMPTY = create(PublisherEmptySource.INSTANCE);

    @SuppressWarnings("unchecked")
    public static <T> Observable<T> empty() {
        return (Observable<T>)EMPTY;
    }

    public static <T> Observable<T> error(Throwable e) {
        Objects.requireNonNull(e);
        return error(() -> e);
    }

    public static <T> Observable<T> error(Supplier<? extends Throwable> errorSupplier) {
        Objects.requireNonNull(errorSupplier);
        return create(new PublisherErrorSource<>(errorSupplier));
    }

    static final Observable<Object> NEVER = create(s -> s.onSubscribe(EmptySubscription.INSTANCE));

    @SuppressWarnings("unchecked")
    public static <T> Observable<T> never() {
        return (Observable<T>)NEVER;
    }

    // TODO match naming with RxJava 1.x
    public static <T> Observable<T> fromCallable(Callable<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        return create(new PublisherScalarAsyncSource<>(supplier));
    }

    public final Observable<T> asObservable() {
        return create(s -> this.subscribe(s));
    }

    @SafeVarargs
    public static <T> Observable<T> fromArray(T... values) {
        Objects.requireNonNull(values);
        if (values.length == 0) {
            return empty();
        } else
            if (values.length == 1) {
                return just(values[0]);
            }
        return create(new PublisherArraySource<>(values));
    }

    public static <T> Observable<T> fromIterable(Iterable<? extends T> source) {
        Objects.requireNonNull(source);
        return create(new PublisherIterableSource<>(source));
    }

    public static <T> Observable<T> fromStream(Stream<? extends T> stream) {
        Objects.requireNonNull(stream);
        return create(new PublisherStreamSource<>(stream));
    }

    public static <T> Observable<T> fromFuture(CompletableFuture<? extends T> future) {
        Objects.requireNonNull(future);
        return create(new PublisherCompletableFutureSource<>(future));
    }

    public static Observable<Integer> range(int start, int count) {
        if (count == 0) {
            return empty();
        } else
            if (count == 1) {
                return just(start);
            }
        if (start + (long)count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Integer overflow");
        }
        return create(new PublisherRangeSource(start, count));
    }

    public static <T> Observable<T> defer(Supplier<? extends Publisher<? extends T>> supplier) {
        return create(new PublisherDefer<>(supplier));
    }

    public final <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return lift(new OperatorMap<>(mapper));
    }

    public final <R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return flatMap(mapper, false, bufferSize(), bufferSize());
    }

    public final <R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, int maxConcurrency) {
        return flatMap(mapper, false, maxConcurrency, bufferSize());
    }

    public final <R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, boolean delayErrors) {
        return flatMap(mapper, delayErrors, bufferSize(), bufferSize());
    }

    public final <R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, boolean delayErrors, int maxConcurrency) {
        return flatMap(mapper, delayErrors, maxConcurrency, bufferSize());
    }

    public final <R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper,
            boolean delayErrors, int maxConcurrency, int bufferSize) {
        Objects.requireNonNull(mapper);
        if (maxConcurrency <= 0) {
            throw new IllegalArgumentException("maxConcurrency > 0 required but it was " + maxConcurrency);
        }
        validateBufferSize(bufferSize);
        if (onSubscribe instanceof PublisherScalarSource) {
            PublisherScalarSource<T> scalar = (PublisherScalarSource<T>) onSubscribe;
            return create(scalar.flatMap(mapper));
        }
        return lift(new OperatorFlatMap<>(mapper, delayErrors, maxConcurrency, bufferSize));
    }

    @SafeVarargs
    public static <T> Observable<T> merge(Publisher<? extends T>... sources) {
        return fromArray(sources).flatMap(v -> v, sources.length);
    }

    public static <T> Observable<T> merge(Iterable<? extends Publisher<? extends T>> sources) {
        return fromIterable(sources).flatMap(v -> v);
    }

    @SafeVarargs
    public static <T> Observable<T> merge(int maxConcurrency, Publisher<? extends T>... sources) {
        return fromArray(sources).flatMap(v -> v, maxConcurrency);
    }

    public static <T> Observable<T> merge(int maxConcurrency, Iterable<? extends Publisher<? extends T>> sources) {
        return fromIterable(sources).flatMap(v -> v, maxConcurrency);
    }

    @SafeVarargs
    public static <T> Observable<T> merge(int maxConcurrency, int bufferSize, Publisher<? extends T>... sources) {
        return fromArray(sources).flatMap(v -> v, false, maxConcurrency, bufferSize);
    }

    public static <T> Observable<T> merge(int maxConcurrency, int bufferSize, Iterable<? extends Publisher<? extends T>> sources) {
        return fromIterable(sources).flatMap(v -> v, false, maxConcurrency, bufferSize);
    }

    public static <T> Observable<T> merge(Publisher<? extends Publisher<? extends T>> sources) {
        return merge(sources, bufferSize());
    }

    public static <T> Observable<T> merge(Publisher<? extends Publisher<? extends T>> sources, int maxConcurrency) {
        return fromPublisher(sources).flatMap(v -> v, maxConcurrency);
    }

    @SafeVarargs
    public static <T> Observable<T> mergeDelayError(Publisher<? extends T>... sources) {
        return fromArray(sources).flatMap(v -> v, true, sources.length);
    }

    public static <T> Observable<T> mergeDelayError(boolean delayErrors, Iterable<? extends Publisher<? extends T>> sources) {
        return fromIterable(sources).flatMap(v -> v, true);
    }

    @SafeVarargs
    public static <T> Observable<T> mergeDelayError(int maxConcurrency, Publisher<? extends T>... sources) {
        return fromArray(sources).flatMap(v -> v, true, maxConcurrency);
    }

    public static <T> Observable<T> mergeDelayError(int maxConcurrency, Iterable<? extends Publisher<? extends T>> sources) {
        return fromIterable(sources).flatMap(v -> v, true, maxConcurrency);
    }

    @SafeVarargs
    public static <T> Observable<T> mergeDelayError(int maxConcurrency, int bufferSize, Publisher<? extends T>... sources) {
        return fromArray(sources).flatMap(v -> v, true, maxConcurrency, bufferSize);
    }

    public static <T> Observable<T> mergeDelayError(int maxConcurrency, int bufferSize, Iterable<? extends Publisher<? extends T>> sources) {
        return fromIterable(sources).flatMap(v -> v, true, maxConcurrency, bufferSize);
    }

    public static <T> Observable<T> mergeDelayError(Publisher<? extends Publisher<? extends T>> sources) {
        return mergeDelayError(sources, bufferSize());
    }

    public static <T> Observable<T> mergeDelayError(Publisher<? extends Publisher<? extends T>> sources, int maxConcurrency) {
        return fromPublisher(sources).flatMap(v -> v, true, maxConcurrency);
    }


    public final Observable<T> take(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("n >= required but it was " + n);
        } else
            if (n == 0) {
                return empty();
            }
        return lift(new OperatorTake<>(n));
    }

    public final <U> Observable<T> takeUntil(Publisher<U> other) {
        Objects.requireNonNull(other);
        return lift(new OperatorTakeUntil<>(other));
    }

    public final Observable<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return lift(new OperatorTakeUntilPredicate<>(predicate));
    }

    public final Observable<T> takeLast(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n >= required but it was " + n);
        } else
            if (n == 0) {
                return ignoreElements();
            } else
                if (n == 1) {
                    return lift(OperatorTakeLastOne.instance());
                }
        return lift(new OperatorTakeLast<>(n));
    }

    public final Observable<T> ignoreElements() {
        return lift(OperatorIgnoreElements.instance());
    }

    public final Observable<T> skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("n >= 0 required but it was " + n);
        } else
            if (n == 0) {
                return this;
            }
        return lift(new OperatorSkip<>(n));
    }

    public final Observable<T> skipLast(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n >= 0 required but it was " + n);
        } else
            if (n == 0) {
                return this;
            }
        return lift(new OperatorSkipLast<>(n));
    }

    public final Observable<T> skipWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return lift(new OperatorSkipWhile<>(predicate));
    }

    public final <U> Observable<T> skipUntil(Publisher<? extends U> other) {
        Objects.requireNonNull(other);
        return lift(new OperatorSkipUntil<>(other));
    }

    public final Observable<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return lift(new OperatorFilter<>(predicate));
    }

    public final Observable<List<T>> toList() {
        return lift(OperatorToList.defaultInstance());
    }

    public final Observable<List<T>> toList(int capacityHint) {
        if (capacityHint <= 0) {
            throw new IllegalArgumentException("capacityHint > 0 required but it was " + capacityHint);
        }
        return lift(new OperatorToList<>(() -> new ArrayList<>(capacityHint)));
    }

    public final <U extends Collection<? super T>> Observable<U> toList(Supplier<U> collectionSupplier) {
        Objects.requireNonNull(collectionSupplier);
        return lift(new OperatorToList<>(collectionSupplier));
    }

    @SuppressWarnings({ "unchecked", "rawtypes"})
    public final Observable<List<T>> toSortedList() {
        return toSortedList((Comparator)Comparator.naturalOrder());
    }

    @SuppressWarnings({ "unchecked", "rawtypes"})
    public final Observable<List<T>> toSortedList(int capacityHint) {
        return toSortedList((Comparator)Comparator.naturalOrder(), capacityHint);
    }

    public final Observable<List<T>> toSortedList(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return toList().map(v -> {
            Collections.sort(v, comparator);
            return v;
        });
    }

    public final Observable<List<T>> toSortedList(Comparator<? super T> comparator, int capacityHint) {
        Objects.requireNonNull(comparator);
        return toList(capacityHint).map(v -> {
            Collections.sort(v, comparator);
            return v;
        });
    }

    public final Observable<T> observeOn(Scheduler scheduler) {
        return observeOn(scheduler, false, bufferSize());
    }

    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError) {
        return observeOn(scheduler, delayError, bufferSize());
    }

    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        Objects.requireNonNull(scheduler);
        validateBufferSize(bufferSize);
        return lift(new OperatorObserveOn<>(scheduler, delayError, bufferSize));
    }

    public final Observable<T> subscribeOn(Scheduler scheduler) {
        return subscribeOn(scheduler, true);
    }

    public final Observable<T> subscribeOn(Scheduler scheduler, boolean requestOn) {
        Objects.requireNonNull(scheduler);
        return create(new PublisherSubscribeOn<>(this, scheduler, requestOn));
    }

    public final Observable<T> unsubscribeOn(Scheduler scheduler) {
        Objects.requireNonNull(scheduler);
        return lift(new OperatorUnsubscribeOn<>(scheduler));
    }

    public final Observable<T> cache() {
        return CachedObservable.from(this);
    }

    public final Observable<T> cache(int capacityHint) {
        return CachedObservable.from(this, capacityHint);
    }

    public final ConnectableObservable<T> publish() {
        return publish(bufferSize());
    }

    public final ConnectableObservable<T> publish(int bufferSize) {
        validateBufferSize(bufferSize);
        return OperatorPublish.create(this, bufferSize);
    }

    public final <R> Observable<R> publish(Function<? super Observable<T>, ? extends Observable<R>> selector) {
        return publish(selector, bufferSize());
    }

    public final <R> Observable<R> publish(Function<? super Observable<T>, ? extends Observable<R>> selector, int bufferSize) {
        validateBufferSize(bufferSize);
        Objects.requireNonNull(selector);
        return OperatorPublish.create(this, selector, bufferSize);
    }

    public final ConnectableObservable<T> replay() {
        return OperatorReplay.createFrom(this);
    }

    public final <R> Observable<R> replay(Function<? super Observable<T>, ? extends Observable<R>> selector) {
        Objects.requireNonNull(selector);
        return OperatorReplay.multicastSelector(this::replay, selector);
    }

    public final <R> Observable<R> replay(Function<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize) {
        Objects.requireNonNull(selector);
        return OperatorReplay.multicastSelector(() -> replay(bufferSize), selector);
    }

    public final <R> Observable<R> replay(Function<? super Observable<T>, ? extends Observable<R>> selector, int bufferSize, long time, TimeUnit unit) {
        return replay(selector, bufferSize, time, unit, Schedulers.computation());
    }

    public final <R> Observable<R> replay(Function<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize, final long time, final TimeUnit unit, final Scheduler scheduler) {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize < 0");
        }
        Objects.requireNonNull(selector);
        return OperatorReplay.multicastSelector(() -> replay(bufferSize, time, unit, scheduler), selector);
    }

    public final <R> Observable<R> replay(final Function<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize, final Scheduler scheduler) {
        return OperatorReplay.multicastSelector(() -> replay(bufferSize),
                t -> selector.apply(t).observeOn(scheduler));
    }

    public final <R> Observable<R> replay(Function<? super Observable<T>, ? extends Observable<R>> selector, long time, TimeUnit unit) {
        return replay(selector, time, unit, Schedulers.computation());
    }

    public final <R> Observable<R> replay(Function<? super Observable<T>, ? extends Observable<R>> selector, final long time, final TimeUnit unit, final Scheduler scheduler) {
        return OperatorReplay.multicastSelector(() -> replay(time, unit, scheduler), selector);
    }

    public final <R> Observable<R> replay(final Function<? super Observable<T>, ? extends Observable<R>> selector, final Scheduler scheduler) {
        return OperatorReplay.multicastSelector(() -> replay(),
                t -> selector.apply(t).observeOn(scheduler));
    }

    public final ConnectableObservable<T> replay(final int bufferSize) {
        return OperatorReplay.create(this, bufferSize);
    }

    public final ConnectableObservable<T> replay(int bufferSize, long time, TimeUnit unit) {
        return replay(bufferSize, time, unit, Schedulers.computation());
    }

    public final ConnectableObservable<T> replay(final int bufferSize, final long time, final TimeUnit unit, final Scheduler scheduler) {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize < 0");
        }
        return OperatorReplay.create(this, time, unit, scheduler, bufferSize);
    }

    public final ConnectableObservable<T> replay(final int bufferSize, final Scheduler scheduler) {
        return OperatorReplay.observeOn(replay(bufferSize), scheduler);
    }

    public final ConnectableObservable<T> replay(long time, TimeUnit unit) {
        return replay(time, unit, Schedulers.computation());
    }

    public final ConnectableObservable<T> replay(final long time, final TimeUnit unit, final Scheduler scheduler) {
        return OperatorReplay.create(this, time, unit, scheduler);
    }

    public final ConnectableObservable<T> replay(final Scheduler scheduler) {
        Objects.requireNonNull(scheduler);
        return OperatorReplay.observeOn(replay(), scheduler);
    }

    public final Observable<Boolean> any(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return lift(new OperatorAny<>(predicate));
    }

    public final Observable<Boolean> all(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return lift(new OperatorAny<>(predicate));
    }

    public final Observable<Long> count() {
        return lift(OperatorCount.instance());
    }

    public final Observable<T> elementAt(long index) {
        if (index < 0) {
            throw new IllegalArgumentException("index >= 0 required but it was " + index);
        }
        return lift(new OperatorElementAt<>(index, null));
    }

    public final Observable<T> elementAt(long index, T defaultValue) {
        if (index < 0) {
            throw new IllegalArgumentException("index >= 0 required but it was " + index);
        }
        Objects.requireNonNull(defaultValue);
        return lift(new OperatorElementAt<>(index, defaultValue));
    }

    public final Observable<Boolean> isEmpty() {
        return any(v -> true);
    }

    public final Observable<T> single() {
        return lift(OperatorSingle.instanceNoDefault());
    }

    public final Observable<T> single(T defaultValue) {
        Objects.requireNonNull(defaultValue);
        return lift(new OperatorSingle<>(defaultValue));
    }

    public final Observable<T> mergeWith(Publisher<? extends T> other) {
        return merge(this, other);
    }

    public final <R> Observable<R> concatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return concatMap(mapper, 2);
    }

    public final <R> Observable<R> concatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch) {
        Objects.requireNonNull(mapper);
        if (prefetch <= 0) {
            throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
        }
        return lift(new OperatorConcatMap<>(mapper, prefetch));
    }

    public final Observable<T> concatWith(Publisher<? extends T> other) {
        Objects.requireNonNull(other);
        return concat(this, other);
    }

    public final Observable<T> concat(Publisher<? extends Publisher<? extends T>> sources) {
        return concat(sources, bufferSize());
    }

    public final Observable<T> concat(Publisher<? extends Publisher<? extends T>> sources, int bufferSize) {
        return fromPublisher(sources).concatMap(v -> v);
    }

    @SafeVarargs
    public static <T> Observable<T> concat(Publisher<? extends T>... sources) {
        if (sources.length == 0) {
            return empty();
        } else
        if (sources.length == 1) {
            return fromPublisher(sources[0]);
        }
        return fromArray(sources).concatMap(v -> v);
    }

    public static <T> Observable<T> concat(Iterable<? extends Publisher<? extends T>> sources) {
        Objects.requireNonNull(sources);
        return fromIterable(sources).concatMap(v -> v);
    }

    @SafeVarargs
    public static <T> Observable<T> concat(int prefetch, Publisher<? extends T>... sources) {
        Objects.requireNonNull(sources);
        return fromArray(sources).concatMap(v -> v, prefetch);
    }

    public static <T> Observable<T> concat(int prefetch, Iterable<? extends Publisher<? extends T>> sources) {
        Objects.requireNonNull(sources);
        return fromIterable(sources).concatMap(v -> v, prefetch);
    }

    @SafeVarargs
    public final Observable<T> startWith(T... values) {
        Observable<T> fromArray = fromArray(values);
        if (fromArray == empty()) {
            return this;
        }
        return concat(fromArray, this);
    }

    public final Observable<T> startWith(Iterable<? extends T> values) {
        return concat(fromIterable(values), this);
    }

    @SafeVarargs
    public final Observable<T> endWith(T... values) {
        Observable<T> fromArray = fromArray(values);
        if (fromArray == empty()) {
            return this;
        }
        return concat(this, fromArray);
    }

    public final Observable<T> endWith(Iterable<? extends T> values) {
        return concat(this, fromIterable(values));
    }

    public final Disposable subscribe() {
        return subscribe(v -> { }, RxJavaPlugins::onError, () -> { }, s -> s.request(Long.MAX_VALUE));
    }

    public final Disposable subscribe(Consumer<? super T> onNext) {
        return subscribe(onNext, RxJavaPlugins::onError, () -> { }, s -> s.request(Long.MAX_VALUE));
    }

    public final Disposable forEach(Consumer<? super T> onNext) {
        return subscribe(onNext);
    }

    public final Disposable forEachWhile(Predicate<? super T> onNext) {
        return forEachWhile(onNext, RxJavaPlugins::onError, () -> { });
    }

    public final Disposable forEachWhile(Predicate<? super T> onNext, Consumer<? super Throwable> onError) {
        return forEachWhile(onNext, onError, () -> { });
    }

    public final Disposable forEachWhile(Predicate<? super T> onNext, Consumer<? super Throwable> onError,
            Runnable onComplete) {
        Objects.requireNonNull(onNext);
        Objects.requireNonNull(onError);
        Objects.requireNonNull(onComplete);

        AtomicReference<Subscription> subscription = new AtomicReference<>();
        return subscribe(v -> {
            if (!onNext.test(v)) {
                subscription.get().cancel();
                onComplete.run();
            }
        }, onError, onComplete, s -> {
            subscription.lazySet(s);
            s.request(Long.MAX_VALUE);
        });
    }

    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return subscribe(onNext, onError, () -> { }, s -> s.request(Long.MAX_VALUE));
    }

    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
            Runnable onComplete) {
        return subscribe(onNext, onError, onComplete, s -> s.request(Long.MAX_VALUE));
    }

    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
            Runnable onComplete, Consumer<? super Subscription> onSubscribe) {
        Objects.requireNonNull(onNext);
        Objects.requireNonNull(onError);
        Objects.requireNonNull(onComplete);
        Objects.requireNonNull(onSubscribe);

        LambdaSubscriber<T> ls = new LambdaSubscriber<>(onNext, onError, onComplete, onSubscribe);

        unsafeSubscribe(ls);

        return ls;
    }

    public final Observable<T> doOnSubscribe(Consumer<? super Subscription> onSubscribe) {
        return doOnLifecycle(onSubscribe, n -> { }, () -> { });
    }

    public final Observable<T> doOnRequest(LongConsumer onRequest) {
        return doOnLifecycle(s -> { }, onRequest, () -> { });
    }

    public final Observable<T> doOnCancel(Runnable onCancel) {
        return doOnLifecycle(s -> { }, n -> { }, onCancel);
    }

    /**
     *
     * @deprecated use {@link #doOnCancel(Runnable)} instead
     */
    @Deprecated
    public final Observable<T> doOnUnsubscribe(Runnable onUnsubscribe) {
        return doOnCancel(onUnsubscribe);
    }

    public final Observable<T> doOnLifecycle(Consumer<? super Subscription> onSubscribe, LongConsumer onRequest, Runnable onCancel) {
        return lift(s -> new SubscriptionLambdaSubscriber<>(s, onSubscribe, onRequest, onCancel));
    }

    public final Observable<T> repeat() {
        return repeat(Long.MAX_VALUE);
    }

    public final Observable<T> repeat(long times) {
        if (times < 0) {
            throw new IllegalArgumentException("times >= 0 required but it was " + times);
        }
        if (times == 0) {
            return empty();
        }
        return create(new PublisherRepeat<>(this, times));
    }

    public final Observable<T> repeatUntil(BooleanSupplier stop) {
        return create(new PublisherRepeatUntil<>(this, stop));
    }

    public final Observable<T> retry() {
        return retry(Long.MAX_VALUE, e -> true);
    }

    public final Observable<T> retry(long times) {
        return retry(times, e -> true);
    }

    public final Observable<T> retryUntil(BooleanSupplier stop) {
        return retry(Long.MAX_VALUE, e -> !stop.getAsBoolean());
    }

    public final Observable<T> retry(Predicate<? super Throwable> predicate) {
        return retry(Long.MAX_VALUE, predicate);
    }

    // Retries at most times or until the predicate returns false, whichever happens first
    public final Observable<T> retry(long times, Predicate<? super Throwable> predicate) {
        if (times < 0) {
            throw new IllegalArgumentException("times >= 0 required but it was " + times);
        }
        Objects.requireNonNull(predicate);

        return create(new PublisherRetryPredicate<>(this, times, predicate));
    }

    public static Observable<Long> timer(long delay, TimeUnit unit) {
        return timer(delay, unit, Schedulers.computation());
    }

    public static Observable<Long> timer(long delay, TimeUnit unit, Scheduler scheduler) {
        if (delay < 0) {
            delay = 0L;
        }
        Objects.requireNonNull(unit);
        Objects.requireNonNull(scheduler);

        return create(new PublisherIntervalOnceSource(delay, unit, scheduler));
    }
    public static Observable<Long> interval(long period, TimeUnit unit) {
        return interval(period, period, unit, Schedulers.computation());
    }

    public static Observable<Long> interval(long period, TimeUnit unit, Scheduler scheduler) {
        return interval(period, period, unit, scheduler);
    }

    public static Observable<Long> interval(long initialDelay, long period, TimeUnit unit) {
        return interval(initialDelay, period, unit, Schedulers.computation());
    }

    public static Observable<Long> interval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        if (initialDelay < 0) {
            initialDelay = 0L;
        }
        if (period < 0) {
            period = 0L;
        }
        Objects.requireNonNull(unit);
        Objects.requireNonNull(scheduler);

        return create(new PublisherIntervalSource(initialDelay, period, unit, scheduler));
    }

    public static Observable<Long> intervalRange(long start, long count, long initialDelay, long period, TimeUnit unit) {
        return intervalRange(start, count, initialDelay, period, unit, Schedulers.computation());
    }

    public static Observable<Long> intervalRange(long start, long count, long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {

        long end = start + (count - 1);
        if (end < 0) {
            throw new IllegalArgumentException("Overflow! start + count is bigger than Long.MAX_VALUE");
        }

        if (initialDelay < 0) {
            initialDelay = 0L;
        }
        if (period < 0) {
            period = 0L;
        }
        Objects.requireNonNull(unit);
        Objects.requireNonNull(scheduler);

        return create(new PublisherIntervalRangeSource(start, end, initialDelay, period, unit, scheduler));
    }

    public final Observable<T> onBackpressureDrop() {
        return lift(OperatorOnBackpressureDrop.instance());
    }

    public final Observable<T> onBackpressureDrop(Consumer<? super T> onDrop) {
        return lift(new OperatorOnBackpressureDrop<>(onDrop));
    }

    public final Observable<T> onBackpressureBuffer() {
        return onBackpressureBuffer(bufferSize(), false, true);
    }

    public final Observable<T> onBackpressureBuffer(int bufferSize) {
        return onBackpressureBuffer(bufferSize, false, false);
    }

    public final Observable<T> onBackpressureBuffer(boolean delayError) {
        return onBackpressureBuffer(bufferSize(), true, true);
    }

    public final Observable<T> onBackpressureBuffer(int bufferSize, boolean delayError) {
        return onBackpressureBuffer(bufferSize, true, false);
    }

    public final Observable<T> onBackpressureBuffer(int bufferSize, boolean delayError, boolean unbounded) {
        return lift(new OperatorOnBackpressureBuffer<>(bufferSize, unbounded, delayError));
    }

    public final Observable<T> onBackpressureLatest() {
        return lift(OperatorOnBackpressureLatest.instance());
    }

    public final <K> Observable<GroupedObservable<T, K>> groupBy(Function<? super T, ? extends K> keySelector) {
        return groupBy(keySelector, v -> v, false, bufferSize());
    }

    public final <K> Observable<GroupedObservable<T, K>> groupBy(Function<? super T, ? extends K> keySelector, boolean delayError) {
        return groupBy(keySelector, v -> v, delayError, bufferSize());
    }

    public final <K, V> Observable<GroupedObservable<V, K>> groupBy(Function<? super T, ? extends K> keySelector,
            Function<? super T, ? extends V> valueSelector) {
        return groupBy(keySelector, valueSelector, false, bufferSize());
    }

    public final <K, V> Observable<GroupedObservable<V, K>> groupBy(Function<? super T, ? extends K> keySelector,
            Function<? super T, ? extends V> valueSelector, boolean delayError) {
        return groupBy(keySelector, valueSelector, false, bufferSize());
    }

    public final <K, V> Observable<GroupedObservable<V, K>> groupBy(Function<? super T, ? extends K> keySelector,
            Function<? super T, ? extends V> valueSelector,
            boolean delayError, int bufferSize) {
        Objects.requireNonNull(keySelector);
        Objects.requireNonNull(valueSelector);
        validateBufferSize(bufferSize);

        return lift(new OperatorGroupBy<>(keySelector, valueSelector, bufferSize, delayError));
    }

    @SuppressWarnings("unchecked")
    private static <T1, T2, R> Function<Object[], R> toFunction(BiFunction<? super T1, ? super T2, ? extends R> biFunction) {
        Objects.requireNonNull(biFunction);
        return a -> {
            if (a.length != 2) {
                throw new IllegalArgumentException("Array of size 2 expected but got " + a.length);
            }
            return ((BiFunction<Object, Object, R>)biFunction).apply(a[0], a[1]);
        };
    }

    public static <T1, T2, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            BiFunction<? super T1, ? super T2, ? extends R> zipper) {
        return zipArray(toFunction(zipper), false, bufferSize(), p1, p2);
    }

    public static <T1, T2, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            BiFunction<? super T1, ? super T2, ? extends R> zipper, boolean delayError) {
        return zipArray(toFunction(zipper), delayError, bufferSize(), p1, p2);
    }

    public static <T1, T2, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            BiFunction<? super T1, ? super T2, ? extends R> zipper, boolean delayError, int bufferSize) {
        return zipArray(toFunction(zipper), delayError, bufferSize, p1, p2);
    }


    public static <T1, T2, T3, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2, Publisher<? extends T3> p3,
            Function3<? super T1, ? super T2, ? super T3, ? extends R> zipper) {
        return zipArray(zipper, false, bufferSize(), p1, p2, p3);
    }

    public static <T1, T2, T3, T4, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2, Publisher<? extends T3> p3,
            Publisher<? extends T4> p4,
            Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> zipper) {
        return zipArray(zipper, false, bufferSize(), p1, p2, p3, p4);
    }

    public static <T1, T2, T3, T4, T5, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2, Publisher<? extends T3> p3,
            Publisher<? extends T4> p4, Publisher<? extends T5> p5,
            Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> zipper) {
        return zipArray(zipper, false, bufferSize(), p1, p2, p3, p4, p5);
    }

    public static <T1, T2, T3, T4, T5, T6, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2, Publisher<? extends T3> p3,
            Publisher<? extends T4> p4, Publisher<? extends T5> p5, Publisher<? extends T6> p6,
            Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> zipper) {
        return zipArray(zipper, false, bufferSize(), p1, p2, p3, p4, p5, p6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2, Publisher<? extends T3> p3,
            Publisher<? extends T4> p4, Publisher<? extends T5> p5, Publisher<? extends T6> p6,
            Publisher<? extends T7> p7,
            Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> zipper) {
        return zipArray(zipper, false, bufferSize(), p1, p2, p3, p4, p5, p6, p7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2, Publisher<? extends T3> p3,
            Publisher<? extends T4> p4, Publisher<? extends T5> p5, Publisher<? extends T6> p6,
            Publisher<? extends T7> p7, Publisher<? extends T8> p8,
            Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> zipper) {
        return zipArray(zipper, false, bufferSize(), p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Observable<R> zip(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2, Publisher<? extends T3> p3,
            Publisher<? extends T4> p4, Publisher<? extends T5> p5, Publisher<? extends T6> p6,
            Publisher<? extends T7> p7, Publisher<? extends T8> p8, Publisher<? extends T9> p9,
            Function9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? extends R> zipper) {
        return zipArray(zipper, false, bufferSize(), p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    // TODO rest of the new zip overloads (got tired copy-pasting)

    @SafeVarargs
    public static <T, R> Observable<R> zipArray(Function<? super Object[], ? extends R> zipper,
            boolean delayError, int bufferSize, Publisher<? extends T>... sources) {
        if (sources.length == 0) {
            return empty();
        }
        Objects.requireNonNull(zipper);
        validateBufferSize(bufferSize);
        return create(new PublisherZip<>(sources, null, zipper, bufferSize, delayError));
    }

    public static <T, R> Observable<R> zipIterable(Function<? super Object[], ? extends R> zipper,
            boolean delayError, int bufferSize,
            Iterable<? extends Publisher<? extends T>> sources) {
        Objects.requireNonNull(zipper);
        Objects.requireNonNull(sources);
        validateBufferSize(bufferSize);
        return create(new PublisherZip<>(null, sources, zipper, bufferSize, delayError));
    }

    public final <U, R> Observable<R> withLatestFrom(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> combiner) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(combiner);

        return lift(new OperatorWithLatestFrom<>(combiner, other));
    }

    public final <U, R> Observable<R> zipWith(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip(this, other, zipper);
    }

    public final <U, R> Observable<R> zipWith(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper, boolean delayError) {
        return zip(this, other, zipper, delayError);
    }

    public final <U, R> Observable<R> zipWith(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper, boolean delayError, int bufferSize) {
        return zip(this, other, zipper, delayError, bufferSize);
    }

    public final <U, R> Observable<R> zipWith(Iterable<? extends U> other,  BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip(this, new PublisherIterableSource<>(other), zipper);
    }

    public final <U, R> Observable<R> zipWith(Iterable<? extends U> other,  BiFunction<? super T, ? super U, ? extends R> zipper, int bufferSize) {
        return zip(this, new PublisherIterableSource<>(other), zipper, false, bufferSize);
    }

    public static <T, R> Observable<R> zip(Publisher<? extends Publisher<? extends T>> sources, Function<Object[], R> zipper) {
        return fromPublisher(sources).toList().flatMap(list -> {
            return zipIterable(zipper, false, bufferSize(), list);
        });
    }

    public final <U> Observable<U> cast(Class<U> clazz) {
        return map(clazz::cast);
    }

    public final Observable<Boolean> contains(Object o) {
        return any(v -> Objects.equals(v, o));
    }

    /**
     * @deprecated use {@link #any(Predicate)}
     */
    @Deprecated
    public final Observable<Boolean> exists(Predicate<? super T> predicate) {
        return any(predicate);
    }

    public final <U> Observable<U> flatMapIterable(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        return flatMap(v -> new PublisherIterableSource<>(mapper.apply(v)));
    }

    public final <U> Observable<U> flatMapIterable(Function<? super T, ? extends Iterable<? extends U>> mapper, int bufferSize) {
        return flatMap(v -> new PublisherIterableSource<>(mapper.apply(v)), false, bufferSize);
    }

    public final <U> Observable<U> concatMapIterable(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        return concatMap(v -> new PublisherIterableSource<>(mapper.apply(v)));
    }

    public final <U> Observable<U> concatMapIterable(Function<? super T, ? extends Iterable<? extends U>> mapper, int prefetch) {
        return concatMap(v -> new PublisherIterableSource<>(mapper.apply(v)), prefetch);
    }

    public final <U, R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends U>> mapper, BiFunction<? super T, ? super U, ? extends R> combiner, boolean delayError) {
        return flatMap(mapper, combiner, delayError, bufferSize(), bufferSize());
    }

    public final <U, R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends U>> mapper, BiFunction<? super T, ? super U, ? extends R> combiner, int maxConcurrency) {
        return flatMap(mapper, combiner, false, maxConcurrency, bufferSize());
    }

    public final <U, R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends U>> mapper, BiFunction<? super T, ? super U, ? extends R> combiner, boolean delayError, int maxConcurrency) {
        return flatMap(mapper, combiner, delayError, maxConcurrency, bufferSize());
    }

    public <U, R> Observable<R> flatMap(Function<? super T, ? extends Publisher<? extends U>> mapper, BiFunction<? super T, ? super U, ? extends R> combiner, boolean delayError, int maxConcurrency, int bufferSize) {
        return flatMap(t -> {
            Observable<U> u = fromPublisher(mapper.apply(t));
            return u.map(w -> combiner.apply(t, w));
        }, delayError, maxConcurrency, bufferSize);
    }

    public final <U> Observable<U> ofType(Class<U> clazz) {
        return filter(clazz::isInstance).cast(clazz);
    }

    public final Observable<Timed<T>> timestamp() {
        return timestamp(TimeUnit.MILLISECONDS, Schedulers.trampoline());
    }

    public final Observable<Timed<T>> timestamp(Scheduler scheduler) {
        return timestamp(TimeUnit.MILLISECONDS, scheduler);
    }

    public final Observable<Timed<T>> timestamp(TimeUnit unit) {
        return timestamp(unit, Schedulers.trampoline());
    }

    public final Observable<Timed<T>> timestamp(TimeUnit unit, Scheduler scheduler) {
        return map(v -> new Timed<>(v, scheduler.now(unit), unit));
    }

    public final Observable<Try<Optional<T>>> materialize() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    //    @SuppressWarnings("unchecked")
    public final Observable<T> dematerialize() {
        //        Observable<Try<Optional<T>>> m = (Observable<Try<Optional<T>>>)this;
        // TODO implement
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #take(long)} instead
     */
    @Deprecated
    public final Observable<T> limit(long n) {
        return take(n);
    }

    public final Observable<T> distinct() {
        return distinct(HashSet::new);
    }

    public final Observable<T> distinct(Supplier<? extends Collection<? super T>> collectionSupplier) {
        return lift(OperatorDistinct.withCollection(collectionSupplier));
    }

    public final Observable<T> distinctUntilChanged() {
        return lift(OperatorDistinct.untilChanged());
    }

    @Deprecated
    public final Observable<Observable<T>> nest() {
        return just(this);
    }

    public final Observable<T> serialize() {
        return lift(s -> new SerializedSubscriber<>(s));
    }

    public final Observable<T> take(long time, TimeUnit unit, Scheduler scheduler) {
        // TODO consider inlining this behavior
        return takeUntil(timer(time, unit, scheduler));
    }

    public final Observable<T> skip(long time, TimeUnit unit, Scheduler scheduler) {
        // TODO consider inlining this behavior
        return skipUntil(timer(time, unit, scheduler));
    }

    public final Observable<Timed<T>> timeInterval() {
        return timeInterval(TimeUnit.MILLISECONDS, Schedulers.trampoline());
    }

    public final Observable<Timed<T>> timeInterval(Scheduler scheduler) {
        return timeInterval(TimeUnit.MILLISECONDS, scheduler);
    }

    public final Observable<Timed<T>> timeInterval(TimeUnit unit) {
        return timeInterval(unit, Schedulers.trampoline());
    }

    public final Observable<Timed<T>> timeInterval(TimeUnit unit, Scheduler scheduler) {
        return lift(new OperatorTimeInterval<>(unit, scheduler));
    }

    private Observable<T> doOnEach(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete, Runnable onAfterTerminate) {
        Objects.requireNonNull(onNext);
        Objects.requireNonNull(onError);
        Objects.requireNonNull(onComplete);
        Objects.requireNonNull(onAfterTerminate);
        return lift(new OperatorDoOnEach<>(onNext, onError, onComplete, onAfterTerminate));
    }

    public final Observable<T> doOnNext(Consumer<? super T> onNext) {
        return doOnEach(onNext, e -> { }, () -> { }, () -> { });
    }

    public final Observable<T> doOnError(Consumer<? super Throwable> onError) {
        return doOnEach(v -> { }, onError, () -> { }, () -> { });
    }

    public final Observable<T> doOnComplete(Runnable onComplete) {
        return doOnEach(v -> { }, e -> { }, onComplete, () -> { });
    }

    public final Observable<T> doOnTerminate(Runnable onTerminate) {
        return doOnEach(v -> { }, e -> onTerminate.run(), onTerminate, () -> { });
    }

    public final Observable<T> finallyDo(Runnable onFinally) {
        return doOnEach(v -> { }, e -> { }, () -> { }, onFinally);
    }

    public final Observable<T> doOnEach(Observer<? super T> observer) {
        return doOnEach(observer::onNext, observer::onError, observer::onComplete, () -> { });
    }

    public final Observable<T> doOnEach(Consumer<? super Try<Optional<T>>> consumer) {
        return doOnEach(
                v -> consumer.accept(Try.ofValue(Optional.of(v))),
                e -> consumer.accept(Try.ofError(e)),
                () -> consumer.accept(Try.ofValue(Optional.empty())),
                () -> { }
                );
    }

    /**
     *
     * @deprecated use composition
     */
    @Deprecated
    public static Observable<Integer> range(int start, int count, Scheduler scheduler) {
        return range(start, count).subscribeOn(scheduler);
    }

    /**
     *
     * @deprecated use composition
     */
    @Deprecated
    public final Observable<T> repeat(Scheduler scheduler) {
        return repeat().subscribeOn(scheduler);
    }

    /**
     *
     * @deprecated use composition
     */
    @Deprecated
    public final Observable<T> repeat(long times, Scheduler scheduler) {
        return repeat(times).subscribeOn(scheduler);
    }

    public final <U> Observable<U> collectInto(U initialValue, BiConsumer<? super U, ? super T> collector) {
        return collect(() -> initialValue, collector);
    }

    public final <U> Observable<U> collect(Supplier<? extends U> initialValueSupplier, BiConsumer<? super U, ? super T> collector) {
        Objects.requireNonNull(initialValueSupplier);
        Objects.requireNonNull(collector);
        return lift(new OperatorCollect<>(initialValueSupplier, collector));
    }

    /**
     *
     * @deprecated is this in use?
     */
    @Deprecated
    public final <K> Observable<Map<K, T>> toMap(Function<? super T, ? extends K> keySelector) {
        return collect(HashMap::new, (m, t) -> {
            K key = keySelector.apply(t);
            m.put(key, t);
        });
    }

    /**
     *
     * @deprecated is this in use?
     */
    @Deprecated
    public final <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector) {
        return collect(HashMap::new, (m, t) -> {
            K key = keySelector.apply(t);
            V value = valueSelector.apply(t);
            m.put(key, value);
        });
    }

    /**
     *
     * @deprecated is this in use?
     */
    @Deprecated
    public final <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keySelector,
            Function<? super T, ? extends V> valueSelector,
            Supplier<? extends Map<K, V>> mapSupplier) {
        return collect(mapSupplier, (m, t) -> {
            K key = keySelector.apply(t);
            V value = valueSelector.apply(t);
            m.put(key, value);
        });
    }

    /**
     *
     * @deprecated is this in use?
     */
    @Deprecated
    public final <K> Observable<Map<K, Collection<T>>> toMultimap(Function<? super T, ? extends K> keySelector) {
        return toMultimap(keySelector, v -> v, ArrayList::new);
    }

    /**
     *
     * @deprecated is this in use?
     */
    @Deprecated
    public final <K, V> Observable<Map<K, Collection<V>>> toMultimap(Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector) {
        return toMultimap(keySelector, valueSelector, ArrayList::new);
    }

    /**
     *
     * @deprecated is this in use?
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public final <K, V> Observable<Map<K, Collection<V>>> toMultimap(Function<? super T, ? extends K> keySelector,
            Function<? super T, ? extends V> valueSelector,
            Supplier<? extends Collection<? super V>> collectionSupplier) {
        return collect(HashMap::new, (m, t) -> {
            K key = keySelector.apply(t);

            Collection<V> coll = m.get(key);
            if (coll == null) {
                coll = (Collection<V>)collectionSupplier.get();
                m.put(key, coll);
            }

            V value = valueSelector.apply(t);

            coll.add(value);
        });
    }

    public final Observable<T> sample(long period, TimeUnit unit) {
        return sample(period, unit, Schedulers.computation());
    }

    public final Observable<T> sample(long period, TimeUnit unit, Scheduler scheduler) {
        Objects.requireNonNull(unit);
        Objects.requireNonNull(scheduler);
        return lift(new OperatorSampleTimed<>(period, unit, scheduler));
    }

    public final <U> Observable<T> sample(Publisher<U> sampler) {
        Objects.requireNonNull(sampler);
        return lift(new OperatorSamplePublisher<>(sampler));
    }

    public final Observable<T> switchIfEmpty(Publisher<? extends T> other) {
        Objects.requireNonNull(other);
        return lift(new OperatorSwitchIfEmpty<>(other));
    }

    public final Observable<T> defaultIfEmpty(T value) {
        Objects.requireNonNull(value);
        return switchIfEmpty(just(value));
    }

    /*
     * It doesn't add cancellation support by default like 1.x
     * if necessary, one can use composition to achieve it:
     * futureObservable.doOnCancel(() -> future.cancel(true));
     */
    public static <T> Observable<T> fromFuture(Future<? extends T> future) {
        if (future instanceof CompletableFuture) {
            return fromFuture((CompletableFuture<? extends T>)future);
        }
        Objects.requireNonNull(future);
        Observable<T> o = create(new PublisherFutureSource<>(future, 0L, null));

        return o;
    }

    public static <T> Observable<T> fromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {
        Objects.requireNonNull(future);
        Objects.requireNonNull(unit);
        Observable<T> o = create(new PublisherFutureSource<>(future, timeout, unit));
        return o;
    }

    public static <T> Observable<T> fromFuture(Future<? extends T> future, Scheduler scheduler) {
        Objects.requireNonNull(scheduler);
        Observable<T> o = fromFuture(future);
        return o.subscribeOn(Schedulers.io());
    }

    public static <T> Observable<T> fromFuture(Future<? extends T> future, long timeout, TimeUnit unit, Scheduler scheduler) {
        Objects.requireNonNull(scheduler);
        Observable<T> o = fromFuture(future, timeout, unit);
        return o.subscribeOn(scheduler);
    }

    public final Observable<T> skipLast(long time, TimeUnit unit) {
        return skipLast(time, unit, Schedulers.trampoline(), false, bufferSize());
    }

    public final Observable<T> skipLast(long time, TimeUnit unit, boolean delayError) {
        return skipLast(time, unit, Schedulers.trampoline(), delayError, bufferSize());
    }

    public final Observable<T> skipLast(long time, TimeUnit unit, Scheduler scheduler) {
        return skipLast(time, unit, scheduler, false, bufferSize());
    }

    public final Observable<T> skipLast(long time, TimeUnit unit, Scheduler scheduler, boolean delayError) {
        return skipLast(time, unit, scheduler, delayError, bufferSize());
    }

    public final Observable<T> skipLast(long time, TimeUnit unit, Scheduler scheduler, boolean delayError, int bufferSize) {
        Objects.requireNonNull(unit);
        Objects.requireNonNull(scheduler);
        validateBufferSize(bufferSize);
     // the internal buffer holds pairs of (timestamp, value) so double the default buffer size
        int s = bufferSize << 1;
        return lift(new OperatorSkipLastTimed<>(time, unit, scheduler, s, delayError));
    }

    public final Observable<T> takeLast(long time, TimeUnit unit) {
        return takeLast(time, unit, Schedulers.trampoline(), false, bufferSize());
    }

    public final Observable<T> takeLast(long count, long time, TimeUnit unit) {
        return takeLast(count, time, unit, Schedulers.trampoline(), false, bufferSize());
    }

    public final Observable<T> takeLast(long time, TimeUnit unit, boolean delayError) {
        return takeLast(time, unit, Schedulers.trampoline(), delayError, bufferSize());
    }

    public final Observable<T> takeLast(long time, TimeUnit unit, Scheduler scheduler) {
        return takeLast(time, unit, scheduler, false, bufferSize());
    }

    public final Observable<T> takeLast(long count, long time, TimeUnit unit, Scheduler scheduler) {
        return takeLast(count, time, unit, scheduler, false, bufferSize());
    }

    public final Observable<T> takeLast(long time, TimeUnit unit, Scheduler scheduler, boolean delayError) {
        return takeLast(time, unit, scheduler, delayError, bufferSize());
    }

    public final Observable<T> takeLast(long time, TimeUnit unit, Scheduler scheduler, boolean delayError, int bufferSize) {
        return takeLast(Long.MAX_VALUE, time, unit, scheduler, delayError, bufferSize);
    }

    public final Observable<T> takeLast(long count, long time, TimeUnit unit, Scheduler scheduler, boolean delayError, int bufferSize) {
        Objects.requireNonNull(unit);
        Objects.requireNonNull(scheduler);
        validateBufferSize(bufferSize);
        if (count < 0) {
            throw new IllegalArgumentException("count >= 0 required but it was " + count);
        }
        return lift(new OperatorTakeLastTimed<>(count, time, unit, scheduler, bufferSize, delayError));
    }

    public final Observable<List<T>> takeLastBuffer(int count) {
        return takeLast(count).toList();
    }

    public final Observable<List<T>> takeLastBuffer(int count, long time, TimeUnit unit) {
        return takeLast(count, time, unit).toList();
    }

    public final Observable<List<T>> takeLastBuffer(int count, long time, TimeUnit unit, Scheduler scheduler) {
        return takeLast(count, time, unit, scheduler).toList();
    }

    public final Observable<List<T>> takeLastBuffer(long time, TimeUnit unit) {
        return takeLast(time, unit).toList();
    }

    public final Observable<List<T>> takeLastBuffer(long time, TimeUnit unit, Scheduler scheduler) {
        return takeLast(time, unit, scheduler).toList();
    }

    public final Observable<T> first() {
        return take(1).single();
    }

    public final Observable<T> first(T defaultValue) {
        return take(1).single(defaultValue);
    }

    public final Observable<T> last() {
        return takeLast(1).single();
    }

    public final Observable<T> last(T defaultValue) {
        return takeLast(1).single(defaultValue);
    }

    public final <R> Observable<R> switchMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return switchMap(mapper, bufferSize());
    }

    public final <R> Observable<R> switchMap(Function<? super T, ? extends Publisher<? extends R>> mapper, int bufferSize) {
        Objects.requireNonNull(mapper);
        validateBufferSize(bufferSize);
        return lift(new OperatorSwitchMap<>(mapper, bufferSize));
    }

    public static <T> Observable<T> switchOnNext(Publisher<? extends Publisher<? extends T>> sources) {
        return fromPublisher(sources).switchMap(v -> v);
    }

    public static <T> Observable<T> switchOnNext(int bufferSize, Publisher<? extends Publisher<? extends T>> sources) {
        return fromPublisher(sources).switchMap(v -> v, bufferSize);
    }

    @SafeVarargs
    public static <T> Observable<T> amb(Publisher<? extends T>... sources) {
        Objects.requireNonNull(sources);
        int len = sources.length;
        if (len == 0) {
            return empty();
        } else
        if (len == 1) {
            return fromPublisher(sources[0]);
        }
        return create(new PublisherAmb<>(sources, null));
    }

    public static <T> Observable<T> amb(Iterable<? extends Publisher<? extends T>> sources) {
        Objects.requireNonNull(sources);
        return create(new PublisherAmb<>(null, sources));
    }

    public final Observable<T> ambWith(Publisher<? extends T> other) {
        return amb(this, other);
    }

    // TODO would result in ambiguity with onErrorReturn(Function)
    public final Observable<T> onErrorReturnValue(T value) {
        Objects.requireNonNull(value);
        return onErrorReturn(e -> value);
    }

    public final Observable<T> onErrorReturn(Function<? super Throwable, ? extends T> valueSupplier) {
        Objects.requireNonNull(valueSupplier);
        return lift(new OperatorOnErrorReturn<>(valueSupplier));
    }

    public final Observable<T> onErrorResumeNext(Publisher<? extends T> next) {
        Objects.requireNonNull(next);
        return onErrorResumeNext(e -> next);
    }

    public final Observable<T> onErrorResumeNext(Function<? super Throwable, ? extends Publisher<? extends T>> resumeFunction) {
        Objects.requireNonNull(resumeFunction);
        return lift(new OperatorOnErrorNext<>(resumeFunction, false));
    }

    public final Observable<T> onExceptionResumeNext(Publisher<? extends T> next) {
        Objects.requireNonNull(next);
        return lift(new OperatorOnErrorNext<>(e -> next, true));
    }

    public final Observable<T> retryWhen(Function<? super Observable<? extends Throwable>, ? extends Publisher<?>> handler) {
        Objects.requireNonNull(handler);

        Function<Observable<Try<Optional<Object>>>, Publisher<?>> f = no ->
            handler.apply(no.map(Try::error))
        ;

        return create(new PublisherRedo<>(this, f));
    }

    public final Observable<T> repeatWhen(Function<? super Observable<Void>, ? extends Publisher<?>> handler) {
        Objects.requireNonNull(handler);

        Function<Observable<Try<Optional<Object>>>, Publisher<?>> f = no ->
            handler.apply(no.map(v -> null))
        ;

        return create(new PublisherRedo<>(this, f));
    }

    public final Observable<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return lift(new OperatorTakeWhile<>(predicate));
    }

    public static <T> Observable<Boolean> sequenceEqual(Publisher<? extends T> p1, Publisher<? extends T> p2) {
        return sequenceEqual(p1, p2, Objects::equals, bufferSize());
    }

    public static <T> Observable<Boolean> sequenceEqual(Publisher<? extends T> p1, Publisher<? extends T> p2, int bufferSize) {
        return sequenceEqual(p1, p2, Objects::equals, bufferSize);
    }

    public static <T> Observable<Boolean> sequenceEqual(Publisher<? extends T> p1, Publisher<? extends T> p2, BiPredicate<? super T, ? super T> isEqual) {
        return sequenceEqual(p1, p2, isEqual, bufferSize());
    }

    public static <T> Observable<Boolean> sequenceEqual(Publisher<? extends T> p1, Publisher<? extends T> p2, BiPredicate<? super T, ? super T> isEqual, int bufferSize) {
        Objects.requireNonNull(p1);
        Objects.requireNonNull(p2);
        Objects.requireNonNull(isEqual);
        validateBufferSize(bufferSize);
        return create(new PublisherSequenceEqual<>(p1, p2, isEqual, bufferSize));
    }

    public static <T, R> Observable<R> combineLatest(Publisher<? extends T>[] sources, Function<? super Object[], ? extends R> combiner) {
        return combineLatest(sources, combiner, false, bufferSize());
    }

    public static <T, R> Observable<R> combineLatest(Publisher<? extends T>[] sources, Function<? super Object[], ? extends R> combiner, boolean delayError) {
        return combineLatest(sources, combiner, delayError, bufferSize());
    }

    @SafeVarargs
    public static <T, R> Observable<R> combineLatest(Function<? super Object[], ? extends R> combiner, boolean delayError, int bufferSize, Publisher<? extends T>... sources) {
        return combineLatest(sources, combiner, delayError, bufferSize);
    }

    public static <T, R> Observable<R> combineLatest(Publisher<? extends T>[] sources, Function<? super Object[], ? extends R> combiner, boolean delayError, int bufferSize) {
        validateBufferSize(bufferSize);
        Objects.requireNonNull(combiner);
        if (sources.length == 0) {
            return empty();
        }
        // the queue holds a pair of values so we need to double the capacity
        int s = bufferSize << 1;
        return create(new PublisherCombineLatest<>(sources, null, combiner, s, delayError));
    }

    public static <T, R> Observable<R> combineLatest(Iterable<? extends Publisher<? extends T>> sources, Function<? super Object[], ? extends R> combiner) {
        return combineLatest(sources, combiner, false, bufferSize());
    }

    public static <T, R> Observable<R> combineLatest(Iterable<? extends Publisher<? extends T>> sources, Function<? super Object[], ? extends R> combiner, boolean delayError) {
        return combineLatest(sources, combiner, delayError, bufferSize());
    }

    public static <T, R> Observable<R> combineLatest(Iterable<? extends Publisher<? extends T>> sources, Function<? super Object[], ? extends R> combiner, boolean delayError, int bufferSize) {
        Objects.requireNonNull(sources);
        Objects.requireNonNull(combiner);
        validateBufferSize(bufferSize);

        // the queue holds a pair of values so we need to double the capacity
        int s = bufferSize << 1;
        return create(new PublisherCombineLatest<>(null, sources, combiner, s, delayError));
    }

    private static void validateBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize > 0 required but it was " + bufferSize);
        }
    }

    public static <T1, T2, R> Observable<R> combineLatest(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            BiFunction<? super T1, ? super T2, ? extends R> combiner) {
        Function<Object[], R> f = toFunction(combiner);
        return combineLatest(f, false, bufferSize(), p1, p2);
    }

    public static <T1, T2, T3, R> Observable<R> combineLatest(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            Publisher<? extends T3> p3,
            Function3<? super T1, ? super T2, ? super T3, ? extends R> combiner) {
        return combineLatest(combiner, false, bufferSize(), p1, p2, p3);
    }

    public static <T1, T2, T3, T4, R> Observable<R> combineLatest(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            Publisher<? extends T3> p3, Publisher<? extends T4> p4,
            Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> combiner) {
        return combineLatest(combiner, false, bufferSize(), p1, p2, p3, p4);
    }

    public static <T1, T2, T3, T4, T5, R> Observable<R> combineLatest(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            Publisher<? extends T3> p3, Publisher<? extends T4> p4,
            Publisher<? extends T5> p5,
            Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> combiner) {
        return combineLatest(combiner, false, bufferSize(), p1, p2, p3, p4, p5);
    }

    public static <T1, T2, T3, T4, T5, T6, R> Observable<R> combineLatest(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            Publisher<? extends T3> p3, Publisher<? extends T4> p4,
            Publisher<? extends T5> p5, Publisher<? extends T6> p6,
            Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> combiner) {
        return combineLatest(combiner, false, bufferSize(), p1, p2, p3, p4, p5, p6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Observable<R> combineLatest(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            Publisher<? extends T3> p3, Publisher<? extends T4> p4,
            Publisher<? extends T5> p5, Publisher<? extends T6> p6,
            Publisher<? extends T7> p7,
            Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> combiner) {
        return combineLatest(combiner, false, bufferSize(), p1, p2, p3, p4, p5, p6, p7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Observable<R> combineLatest(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            Publisher<? extends T3> p3, Publisher<? extends T4> p4,
            Publisher<? extends T5> p5, Publisher<? extends T6> p6,
            Publisher<? extends T7> p7, Publisher<? extends T8> p8,
            Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> combiner) {
        return combineLatest(combiner, false, bufferSize(), p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Observable<R> combineLatest(
            Publisher<? extends T1> p1, Publisher<? extends T2> p2,
            Publisher<? extends T3> p3, Publisher<? extends T4> p4,
            Publisher<? extends T5> p5, Publisher<? extends T6> p6,
            Publisher<? extends T7> p7, Publisher<? extends T8> p8,
            Publisher<? extends T9> p9,
            Function9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? extends R> combiner) {
        return combineLatest(combiner, false, bufferSize(), p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    public static <T, D> Observable<T> using(Supplier<? extends D> resourceSupplier, Function<? super D, ? extends Publisher<? extends T>> sourceSupplier, Consumer<? super D> disposer) {
        return using(resourceSupplier, sourceSupplier, disposer, true);
    }

    public static <T, D> Observable<T> using(Supplier<? extends D> resourceSupplier, Function<? super D, ? extends Publisher<? extends T>> sourceSupplier, Consumer<? super D> disposer, boolean eager) {
        Objects.requireNonNull(resourceSupplier);
        Objects.requireNonNull(sourceSupplier);
        Objects.requireNonNull(disposer);
        return create(new PublisherUsing<>(resourceSupplier, sourceSupplier, disposer, eager));
    }
}