commit 858af7158f1400b706ad50a6d98d010f58f78305
Merge: 6327b41 018e0fc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 22 11:45:21 2017 -0700

    feature #21093 [Lock] Create a lock component (jderusse)

    This PR was squashed before being merged into the 3.3-dev branch (closes #21093).

    Discussion
    ----------

    [Lock] Create a lock component

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | they will
    | Fixed tickets | #20382
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#7364

    This PR aim to add a new component Lock going further than the FileSystem\LockHandler by allowing remote backend (like Redis, memcache, etc)
    Inspired by

    ## Usage

    The simplest way to use lock is to inject an instance of a Lock in your service
    ```php
    class MyService
    {
        private $lock;

        public function __construct(LockInterface $lock)
        {
            $this->lock = $lock;
        }

        public function run()
        {
            $this->lock->acquire(true);

            // If I'm here, no exception had been raised. Lock is acquired
            try {
                // do my job
            } finally {
                $this->lock->release();
            }
        }
    }
    ```
    Configured with something like
    ```yaml
    services:
        app.my_service:
            class: AppBundle\MyService
            arguments:
                - app.lock.my_service
        app.lock.my_service:
            class: Symfony\Component\Lock\Lock
            factory: ['@locker', createLock]
            arguments: ['my_service']
    ```

    If you need to lock serveral resource on runtime, wou'll nneed to inject the LockFactory.
    ```php
    class MyService
    {
        private $lockFactory;

        public function __construct(LockFactoryInterface $lockFactory)
        {
            $this->lockFactory = $lockFactory;
        }

        public function run()
        {
            foreach ($this->items as $item) {
                $lock = $this->lockFactory->createLock((string) $item);

                try {
                    $lock->acquire();
                } catch (LockConflictedException $e) {
                    continue;
                }

                // When I'm here, no exception had been, raised. Lock is acquired
                try {
                    // do my job
                } finally {
                    $lock->release();
                }
            }
        }
    }
    ```
    Configured with something like
    ```yaml
    services:
        app.my_service:
            class: AppBundle\MyService
            arguments:
                - '@locker'
    ```

    This component allow you to refresh an expirable lock.
    This is usefull, if you run a long operation split in several small parts.
    If you lock with a ttl for the overall operatoin time and your process crash, the lock will block everybody for the defined TTL.
    But thank to the refresh method, you're able to lock for a small TTL, and refresh it between each parts.
    ```php
    class MyService
    {
        private $lock;

        public function __construct(LockInterface $lock)
        {
            $this->lock = $lock;
        }

        public function run()
        {
            $this->lock->acquire(true);

            try {
                do {
                    $finished = $this->performLongTask();

                    // Increase the expire date by 300 more seconds
                    $this->lock->refresh();
                } while (!$finished)
                // do my job
            } finally {
                $this->lock->release();
            }
        }
    }
    ```

    ## Naming anc implementation choise

    ```
    $lock->acquire()
    vs
    $lock->lock()
    ```

    Choose to use acquire, because this component is full of `lock` Symfony\Component\Lock\Lock::Lock` raised a E_TOO_MANY_LOCK in my head.

    ```
    $lock->acquire(false);
    $lock->acquire(true);
    vs
    $lock->aquire()
    $lock->waitAndAquire()
    ```

    Not a big fan of flag feature and 2. But I choose to use the blocking flag to offer a simple (and common usecase) implementation

    ```
    $lock = $factory->createLock($key);
    $lock->acquire();
    vs
    $lock->aquire($key)
    ```

    I choose to a the pool of locks implementation. It allow the user to create 2 instances and use cross lock even in the same process.

    ```
    interface LockInterface
    final class Lock implements LockInterface
    vs
    final class Lock
    ```

    I choose to use a Interface even if there is only one implementaiton to offer an extension point here

    # TODO

    ## In this PR
    * [x] tests
    * [x] add logs
    * [x] offer several redis connectors
    * [x] try other store implementation to validate the architecture/interface

    ## In other PR
    * documentation
    * add configuration in framework bundle
    * add stop watch in the debug bar
    * improve the combined store (takes the drift into account and elapsed time between each store)
    * implement other stores (memcache, ...)
    * use this component in session manipulation (fixes #4976)

    Commits
    -------

    018e0fc330 [Lock] Create a lock component