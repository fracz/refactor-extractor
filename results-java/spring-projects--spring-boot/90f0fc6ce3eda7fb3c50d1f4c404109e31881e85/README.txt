commit 90f0fc6ce3eda7fb3c50d1f4c404109e31881e85
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Tue Feb 16 16:44:50 2016 +0100

    Harmonize JTA properties

    Previously, both Atomikos and Bitronix were bound on the `spring.jta`
    namespace which makes very hard to figure out which property belong to
    which implementation. Besides, `AtomikosProperties` only exposed public
    setter which does not generate any useful meta-data.

    This commit moves the external configuration for Atomikos and Bitronix to
    `spring.jta.atomikos.properties` and `spring.jta.bitronix.properties`
    respectively. It also improves the meta-data support for those two
    namespaces.

    Closes gh-5165