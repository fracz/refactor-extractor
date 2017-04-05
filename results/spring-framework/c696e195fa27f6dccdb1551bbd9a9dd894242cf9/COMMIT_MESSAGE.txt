commit c696e195fa27f6dccdb1551bbd9a9dd894242cf9
Author: Chris Beams <cbeams@vmware.com>
Date:   Mon May 23 10:03:23 2011 +0000

    Introduce AnnotationConfigCapableApplicationContext

    AnnotationConfigApplicationContext and
    AnnotationConfigWebApplicationContext both expose #register and #scan
    methods as of the completion of SPR-8320. This change introduces a new
    interface that declares each of these methods and refactors ACAC and
    ACWAC to implement it.

    Beyond information value, this is useful for implementors of the
    ApplicationContextInitializer interface, in that users may create an ACI
    that works consistently across ACAC and ACWAC for standalone (e.g.
    testing, batch) or web (e.g. production) use.

    Issue: SPR-8365,SPR-8320