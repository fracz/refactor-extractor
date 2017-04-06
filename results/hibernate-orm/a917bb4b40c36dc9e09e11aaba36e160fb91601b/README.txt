commit a917bb4b40c36dc9e09e11aaba36e160fb91601b
Author: Andrig Miller <andy.miller@jboss.com>
Date:   Wed May 25 17:35:07 2011 -0600

    HHH-6258:  Cached JdbcServices and Dialect in local variables to lessen load on AbtractServiceRegistryImpl.localServiceBinding(java.lang.Class).  This improved throughput.