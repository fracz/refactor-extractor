package org.redisson.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redisson.BaseTest;
import org.redisson.RedissonNode;
import org.redisson.api.RExecutorService;
import org.redisson.config.Config;
import org.redisson.config.RedissonNodeConfig;

public class RedissonExecutorServiceTest extends BaseTest {

    private static RedissonNode node;

    @BeforeClass
    public static void beforeClass() throws IOException, InterruptedException {
        BaseTest.beforeClass();

        Config config = createConfig();
        RedissonNodeConfig nodeConfig = new RedissonNodeConfig(config);
        nodeConfig.setExecutorServiceWorkers(Collections.singletonMap("test", 1));
        node = RedissonNode.create(nodeConfig);
        node.start();
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException {
        BaseTest.afterClass();

        node.shutdown();
    }

    @Test
    public void testMultipleTasks() throws InterruptedException, ExecutionException, TimeoutException {
        RExecutorService e = redisson.getExecutorService("test");
        e.execute(new RunnableTask());
        Future<?> f = e.submit(new RunnableTask2());
        f.get();
        Future<String> fs = e.submit(new CallableTask());
        assertThat(fs.get()).isEqualTo(CallableTask.RESULT);

        Future<Integer> f2 = e.submit(new RunnableTask(), 12);
        assertThat(f2.get()).isEqualTo(12);

        String invokeResult = e.invokeAny(Arrays.asList(new CallableTask(), new CallableTask(), new CallableTask()));
        assertThat(invokeResult).isEqualTo(CallableTask.RESULT);

        String a = e.invokeAny(Arrays.asList(new CallableTask(), new CallableTask(), new CallableTask()), 1, TimeUnit.SECONDS);
        assertThat(a).isEqualTo(CallableTask.RESULT);

        List<CallableTask> invokeAllParams = Arrays.asList(new CallableTask(), new CallableTask(), new CallableTask());
        List<Future<String>> allResult = e.invokeAll(invokeAllParams);
        assertThat(allResult).hasSize(invokeAllParams.size());
        for (Future<String> future : allResult) {
            assertThat(future.get()).isEqualTo(CallableTask.RESULT);
        }

        List<CallableTask> invokeAllParams1 = Arrays.asList(new CallableTask(), new CallableTask(), new CallableTask());
        List<Future<String>> allResult1 = e.invokeAll(invokeAllParams1, 1, TimeUnit.SECONDS);
        assertThat(allResult1).hasSize(invokeAllParams.size());
        for (Future<String> future : allResult1) {
            assertThat(future.get()).isEqualTo(CallableTask.RESULT);
        }

    }

    @Test(expected = RejectedExecutionException.class)
    public void testRejectExecute() throws InterruptedException, ExecutionException {
        RExecutorService e = redisson.getExecutorService("test");
        e.execute(new RunnableTask());
        Future<?> f1 = e.submit(new RunnableTask2());
        Future<String> f2 = e.submit(new CallableTask());

        e.shutdown();

        f1.get();
        assertThat(f2.get()).isEqualTo(CallableTask.RESULT);

        assertThat(e.isShutdown()).isTrue();
        e.execute(new RunnableTask());
    }

    @Test(expected = RejectedExecutionException.class)
    public void testRejectSubmitRunnable() throws InterruptedException, ExecutionException {
        RExecutorService e = redisson.getExecutorService("test");
        e.execute(new RunnableTask());
        Future<?> f1 = e.submit(new RunnableTask2());
        Future<String> f2 = e.submit(new CallableTask());

        e.shutdown();

        f1.get();
        assertThat(f2.get()).isEqualTo(CallableTask.RESULT);

        assertThat(e.isShutdown()).isTrue();
        e.submit(new RunnableTask2());
    }

    @Test(expected = RejectedExecutionException.class)
    public void testRejectSubmitCallable() throws InterruptedException, ExecutionException {
        RExecutorService e = redisson.getExecutorService("test");
        e.execute(new RunnableTask());
        Future<?> f1 = e.submit(new RunnableTask2());
        Future<String> f2 = e.submit(new CallableTask());

        e.shutdown();

        f1.get();
        assertThat(f2.get()).isEqualTo(CallableTask.RESULT);

        assertThat(e.isShutdown()).isTrue();
        e.submit(new CallableTask());
    }

    @Test(expected = RejectedExecutionException.class)
    public void testEmptyRejectSubmitRunnable() throws InterruptedException, ExecutionException {
        RExecutorService e = redisson.getExecutorService("test");
        e.shutdown();

        assertThat(e.isShutdown()).isTrue();
        e.submit(new RunnableTask2());
    }


    @Test
    public void testShutdown() throws InterruptedException {
        RExecutorService e = redisson.getExecutorService("test");
        assertThat(e.isShutdown()).isFalse();
        assertThat(e.isTerminated()).isFalse();
        e.execute(new RunnableTask());
        e.shutdown();
        assertThat(e.isShutdown()).isTrue();
        assertThat(e.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(e.isTerminated()).isTrue();
    }

    @Test
    public void testShutdownEmpty() throws InterruptedException {
        RExecutorService e = redisson.getExecutorService("test");
        assertThat(e.isShutdown()).isFalse();
        assertThat(e.isTerminated()).isFalse();
        e.shutdown();
        assertThat(e.isShutdown()).isTrue();
        assertThat(e.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(e.isTerminated()).isTrue();
    }

    @Test
    public void testResetShutdownState() throws InterruptedException, ExecutionException {
        for (int i = 0; i < 100; i++) {
            RExecutorService e = redisson.getExecutorService("test");
            e.execute(new RunnableTask());
            assertThat(e.isShutdown()).isFalse();
            e.shutdown();
            assertThat(e.isShutdown()).isTrue();
            assertThat(e.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            assertThat(e.isTerminated()).isTrue();
            assertThat(e.delete()).isTrue();
            assertThat(e.isShutdown()).isFalse();
            assertThat(e.isTerminated()).isFalse();
            Future<?> future = e.submit(new RunnableTask());
            future.get();
        }
    }

    @Test
    public void testRedissonInjected() throws InterruptedException, ExecutionException {
        Future<Long> s1 = redisson.getExecutorService("test").submit(new CallableRedissonTask(1L));
        Future<Long> s2 = redisson.getExecutorService("test").submit(new CallableRedissonTask(2L));
        Future<Long> s3 = redisson.getExecutorService("test").submit(new CallableRedissonTask(30L));
        Future<Void> s4 = (Future<Void>) redisson.getExecutorService("test").submit(new RunnableRedissonTask());

        List<Long> results = Arrays.asList(s1.get(), s2.get(), s3.get());
        assertThat(results).containsOnlyOnce(33L);

        s4.get();
        assertThat(redisson.getAtomicLong("runnableCounter").get()).isEqualTo(100L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnonymousRunnable() {
        redisson.getExecutorService("test").submit(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnonymousCallable() {
        redisson.getExecutorService("test").submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnonymousRunnableExecute() {
        redisson.getExecutorService("test").execute(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

}