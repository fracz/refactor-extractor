commit abe51d5b3531a2768214a82d192b9de6b85ec90b
Author: Andrig Miller <andy.miller@jboss.com>
Date:   Wed May 25 17:00:11 2011 -0600

    HHH-6258:  Cached service registry classes in local variables to remove load from AbstractServiceRegistryImpl.locateServiceBinding(java.lang.Class).  This resulted in a decent improvement in throughput.