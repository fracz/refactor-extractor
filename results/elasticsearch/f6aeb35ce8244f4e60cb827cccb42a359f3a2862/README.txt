commit f6aeb35ce8244f4e60cb827cccb42a359f3a2862
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Wed Aug 3 08:34:09 2016 +0200

    Tighten up concurrent store metadata listing and engine writes (#19684)

    In several places in our code we need to get a consistent list of files + metadata of the current index. We currently have a couple of ways to do in the `Store` class, which also does the right things and tries to verify the integrity of the smaller files. Sadly, those methods can run into trouble if anyone writes into the folder while they are busy. Most notably, the index shard's engine decides to commit half way and remove a `segment_N` file before the store got to checksum (but did already list it). This race condition typically doesn't happen as almost all of the places where we list files also happen to be places where the relevant shard doesn't yet have an engine. There  is however an exception (of course :)) which is the API to list shard stores, used by the master when it is looking for shard copies to assign to.

    I already took one shot at fixing this in #19416 , but it turns out not to be enough - see for example https://elasticsearch-ci.elastic.co/job/elastic+elasticsearch+master+multijob-os-compatibility/os=sles/822.

    The first inclination to fix this was to add more locking to the different Store methods and acquire the `IndexWriter` lock, thus preventing any engine for accessing if if the a shard is offline and use the current index commit snapshotting logic already existing in `IndexShard` for when the engine is started. That turned out to be a bad idea as we create more subtleties where, for example, a store listing can prevent a shard from starting up (the writer lock doesn't wait if it can't get access, but fails immediately, which is good). Another example is running on a shared directory where some other engine may actually hold the lock.

    Instead I decided to take another approach:
    1) Remove all the various methods on store and keep one, which accepts an index commit (which can be null) and also clearly communicates that the *caller* is responsible for concurrent access. This also tightens up the API which is a plus.
    2) Add a `snapshotStore` method to IndexShard that takes care of all the concurrency aspects with the engine, which is now possible because it's all in the same place. It's still a bit ugly but at least it's all in one place and we can evaluate how to improve on this later on. I also renamed the  `snapshotIndex` method to `acquireIndexCommit` to avoid confusion and I think it communicates better what it does.