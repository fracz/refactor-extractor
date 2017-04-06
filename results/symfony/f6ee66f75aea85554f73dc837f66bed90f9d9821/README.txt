commit f6ee66f75aea85554f73dc837f66bed90f9d9821
Merge: 84d9551 2c767d1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Feb 12 13:06:54 2012 +0100

    merged branch stof/session_cleanup (PR #3333)

    Commits
    -------

    2c767d1 [HttpFoundation] Fixed closeSession for the Memcached storage
    ec44e68 [HttpFoundation] Fixed the use of the prefix for the Memcached storage
    5808773 [HttpFoundation] Fixed a typo and updated the phpdoc for the session
    0550bef Removed methods duplicated from parent class

    Discussion
    ----------

    Session cleanup

    This cleans the phpdoc of the refactored session by using inheritdoc in all appropriate places. For the SessionInterface, I added the ``@api`` tag in all methods as the Session class had them.
    It also fixes a few typos in the variable names.

    I figured a few things:

    - the Session class implements some methods that are not part of the SessionInterface. Is it intended or simply a left-over ?
    - the MemcachedSessionStorage uses a ``prefix`` property which does not exist. This is clearly a copy-paste from the MemcacheSessionStorage which has it. It also call the ``Memcached#close`` method which does not exist according to PhpStorm. @drak could you check this class ? I don't know Memcached at all.
    - as I said on the refactoring PR, the Serializable implementation of the Session class seems totally wrong as the SessionStorage is not serializable.

    /cc @drak

    ---------------------------------------------------------------------------

    by drak at 2012-02-12T02:09:38Z

    @stof there is a ticket about this, the problem exists from Symfony 2.0 also - refs #3000 PDOSessionStorage

    ---------------------------------------------------------------------------

    by drak at 2012-02-12T02:10:12Z

    @stof I will look at the memcache issue and make a quick PR

    ---------------------------------------------------------------------------

    by stof at 2012-02-12T02:11:07Z

    @drak the issue could exist with any SessionStorage as your SessionStorageInterface does not enforce making it serializable

    ---------------------------------------------------------------------------

    by stof at 2012-02-12T02:14:15Z

    @drak note that this PR already fixes 2 typo in the memcached storage. It seems like some tests are missing for it as they should have been found (they would have raised a notice in the constructor)

    ---------------------------------------------------------------------------

    by drak at 2012-02-12T02:14:50Z

    Afaik, only PDO is not serializable@ but the `Serializable` is on the SessionInterface.  The problem is with #3000, the serialization is necessary but impossible.

    ---------------------------------------------------------------------------

    by stof at 2012-02-12T02:21:42Z

    @drak what about a Mongo storage or things like that ?

    ---------------------------------------------------------------------------

    by drak at 2012-02-12T02:23:58Z

    @stof Since you've started this PR would you mind removing `$prefix.` from the `*session()` methods in `MemcachedSessionStorage` - they are not required because Memcached handles prefixes itself.  I'll write some tests in another PR for them.  I guess I omitted it because at the time I didn't have them on my test env.

    ---------------------------------------------------------------------------

    by drak at 2012-02-12T02:24:35Z

    @stof - Let's take the serialization issue to #3000 because it's being discussed there.

    ---------------------------------------------------------------------------

    by stof at 2012-02-12T02:27:27Z

    @drak what about the ``close()`` method which does not exist in the Memcached class ?

    ---------------------------------------------------------------------------

    by drak at 2012-02-12T03:58:11Z

    The method just needs to `return true;`.