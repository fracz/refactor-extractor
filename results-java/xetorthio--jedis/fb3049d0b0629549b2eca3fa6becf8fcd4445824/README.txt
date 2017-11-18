commit fb3049d0b0629549b2eca3fa6becf8fcd4445824
Author: stroller <fujian1115@gmail.com>
Date:   Thu Oct 13 09:15:07 2016 +0800

    improve slots cache initialize‘s retry logic (#1402)

    * improve slots cache initialize's retry logic

    [Motivation]
    When jedis.auth(password) throw JedisConnectionException, it won't retry
    next node. This should be improved.

    [Modifications]
    Move the code about auth/clientSetname into try-catch statements.

    [Result]
    When jedis fail to setup connection with one node to initial slots
    cache, it will retry next one.

    * remove similar case