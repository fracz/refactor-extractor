commit 28d045ed48a47f3b3dfcb3cf74ad98a17e4aa037
Merge: 48099a8 12e22c0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu May 10 11:47:04 2012 +0200

    merged branch vicb/session_nnhandlers (PR #4227)

    Commits
    -------

    12e22c0 [Session] Memcache/d cleanup, test improvements
    788adfb [Session] Pdo Handler cleanup
    0216e05 [HttpFoundation][Session] Assume that memcache(d) instances are already configured
    72d21c6 [HttpFoundation][Session] change possible replace() & set() for set only()

    Discussion
    ----------

    [Session] Non-native Session handlers

    A few item to discuss. Needs @drak inputs.

    * 72d21c66 is trivial,
    * 0216e056 is about memcache(d) handlers
        * I don't think the handlers should configure the memcache(d) instances. Those instances are injected into the storage so they should already be confidured (this will be done in the CacheBundle when available)
        * A SW prefix has been added to the memcached handlers so that the same instance of memcached can be shared - you can still set the `Memcached::OPT_PREFIX_KEY` before injecting the memcached instance.
        * It was not possible to use an expiration > 30days before, see [php.net](http://www.php.net/manual/en/memcached.expiration.php)
    * 788adfb6 is trivial (cleanup in the PDO handler)

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-08T09:49:03Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1274808) (merged 788adfb6 into e54f4e46).

    ---------------------------------------------------------------------------

    by drak at 2012-05-09T15:20:38Z

    Overall this PR looks good to me. Since Memcache/d objects are passed by DI anyway, there is no need to provide a way to configure the objects here.

    However, I am not sure it's consistent to provide internal handling of the prefix/expire if we are saying the objects should be configured and injected - if we hand over all configuration to the injected objects, that's exactly what we should do. In the case of the `Memcache` handler there is no handling for prefix by the Memcache object that is why it's handled internally.

    Unless there are some other technical consideration I've missed, I would also not expect the same Memcache/d object to be used in all use cases (e.g. session storage and database caching layer).   I realise we are trying to unify things in one cache component, but I am not entirely convinced session storage would necessarily have to be part of that nor that "one object fits all" is practical or wise.

    As far as I am aware, apart from default settings, memcache/memcached instances retain their own settings once configured so it's quite feasible to expect there might be a couple of differently configured instances in a complex system.

    In summary, I would remove the `$memcachedOptions` config entirely from the `MemcachedHander` along with the associated prefix and time and let it all be configured by the injected `Memcached` instance.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-10T07:32:53Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1293064) (merged 12e22c0d into e54f4e46).

    ---------------------------------------------------------------------------

    by vicb at 2012-05-10T07:34:31Z

    @drak thanks for your feeback.

    About the prefix: it might be necessary to avoid collisions when you re-use the same instance of `memcache/d`. This is why the prefix is handled internally and not by `memcached` (it would be global and not serve the purpose then).

    About the ttl:

    * `memcache/d` can not handle ttl > 30 days (they would consider the time as an absolute timestamp then) and this is why the PR always convert the ttl to an absolute ts (`time() + $ttl`)
    * Moreover I think that the ttl should be initialized by the `Session`: there is no reason why the ttl should be different from the `gc_maxlifetime`. I think this is out of the scope of this PR.

    About sharing `memcache/d ` instances: it will be possible but it does not mean that you have to, you still can use different instances if this suit your needs.

    The tests have been improved.

    If you are ok with the latest changes, this PR should be ready to be merged

    ---------------------------------------------------------------------------

    by drak at 2012-05-10T09:29:18Z

    @vicb - I think it's ok to merge now. You are right about the TTL since PHP will pass a maxlifetime not a timestamp, and since memcached varies how it treats $expire, it does need to be normalised in the handler. I'm not necessarily 100% convinced about the prefix, but I don't object.  Nice work.

    /cc @fabpot