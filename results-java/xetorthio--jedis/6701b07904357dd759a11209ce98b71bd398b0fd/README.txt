commit 6701b07904357dd759a11209ce98b71bd398b0fd
Author: Marcos Lilljedahl <marcosnils@gmail.com>
Date:   Mon Mar 9 02:33:50 2015 -0400

    This commit addresses the first part discussed in #909.
    To improve usability we're changing the visibility of `returnResoure`
    and `returnBrokenResource` in favor of `jedis.close()` which
    automatically returns the resource to the pool accordingly.
    As `Pool` and `Jedis` are currently in different packages i've created
    a JedisPoolAbstract class to provide a bridge between the two
    implementations