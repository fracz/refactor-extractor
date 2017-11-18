commit d4251125fc829d1e7640a52c8d9d821854353455
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Wed Apr 12 14:30:42 2017 +0200

    Make sure Admin MBean works also with webflux

    This commit improves `SpringApplicationAdminMXBeanRegistrar` so that it
    work with traditional Servlet-based app but also with Spring WebFlux.

    Closes gh-8533