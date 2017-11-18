package de.greenrobot.daotest;

import java.util.concurrent.CountDownLatch;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemClock;
import de.greenrobot.dao.DaoLog;
import de.greenrobot.dao.test.AbstractDaoSessionTest;

public class DaoSessionConcurrentTest extends AbstractDaoSessionTest<Application, DaoMaster, DaoSession> {
    class TestThread extends Thread {
        final Runnable runnable;

        public TestThread(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            latchThreadsReady.countDown();
            try {
                latchInsideTx.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            runnable.run();
            latchThreadsDone.countDown();
        }

    }

    private final static int TIME_TO_WAIT_FOR_THREAD = 1000; // Use 1000 to be on the safe side, 100 once stable

    private TestEntityDao dao;

    private CountDownLatch latchThreadsReady;
    private CountDownLatch latchInsideTx;
    private CountDownLatch latchThreadsDone;

    public DaoSessionConcurrentTest() {
        super(DaoMaster.class);
    }

    @Override
    protected void setUp() {
        super.setUp();
        dao = daoSession.getTestEntityDao();
    }

    void initThreads(Runnable... runnables) throws InterruptedException {
        latchThreadsReady = new CountDownLatch(runnables.length);
        latchInsideTx = new CountDownLatch(1);
        latchThreadsDone = new CountDownLatch(runnables.length);
        for (Runnable runnable : runnables) {
            new TestThread(runnable).start();
        }
        latchThreadsReady.await();
    }

    public void testConcurrentInsertDuringTx() throws InterruptedException {
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                dao.insert(createEntity(null));
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                dao.insertInTx(createEntity(null));
            }
        };
        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                daoSession.runInTx(new Runnable() {
                    @Override
                    public void run() {
                        dao.insert(createEntity(null));
                    }
                });
            }
        };
        initThreads(runnable1, runnable2, runnable3);
        // Builds the statement so it is ready immediately in the thread
        dao.insert(createEntity(null));
        doTx(new Runnable() {
            @Override
            public void run() {
                dao.insert(createEntity(null));
            }
        });
        latchThreadsDone.await();
        assertEquals(5, dao.count());
    }

    public void testConcurrentUpdateDuringTx() throws InterruptedException {
        final TestEntity entity = createEntity(null);
        dao.insert(entity);
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                dao.update(entity);
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                dao.updateInTx(entity);
            }
        };
        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                daoSession.runInTx(new Runnable() {
                    @Override
                    public void run() {
                        dao.update(entity);
                    }
                });
            }
        };
        initThreads(runnable1, runnable2, runnable3);
        // Builds the statement so it is ready immediately in the thread
        dao.update(entity);
        doTx(new Runnable() {
            @Override
            public void run() {
                dao.update(entity);
            }
        });
        latchThreadsDone.await();
    }

    /**
     * We could put the statements inside ThreadLocals (fast enough), but it comes with initialization penalty for new
     * threads and costs more memory.
     */
    public void _testThreadLocalSpeed() {
        final SQLiteDatabase db = dao.getDatabase();
        ThreadLocal<SQLiteStatement> threadLocal = new ThreadLocal<SQLiteStatement>() {
            @Override
            protected SQLiteStatement initialValue() {
                return db.compileStatement("SELECT 42");
            }
        };
        threadLocal.get();
        long start = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i < 1000; i++) {
            SQLiteStatement sqLiteStatement = threadLocal.get();
            assertNotNull(sqLiteStatement);
        }
        Long time = SystemClock.currentThreadTimeMillis() - start;
        DaoLog.d("TIME: " + time + "ms");
        // Around 1ms on a S3
        assertTrue(time < 10);
    }

    private void doTx(final Runnable runnableInsideTx) {
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                latchInsideTx.countDown();
                // Give the concurrent thread time so it will try to acquire locks
                try {
                    Thread.sleep(TIME_TO_WAIT_FOR_THREAD);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                runnableInsideTx.run();
            }
        });
    }

    protected TestEntity createEntity(Long key) {
        TestEntity entity = new TestEntity(key);
        entity.setSimpleStringNotNull("green");
        return entity;
    }
}