commit 0435f122d4e16aafbd142d3525abb1c7aee79c1b
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Mon Feb 27 19:06:36 2017 +0100

    Rework JSR-303 validation exposure with Spring MVC

    This commit improves the initial solution by actually overriding the
    `mvcValidator` `@Bean`. This gives us more control as whether a custom
    validator has been specified or not. We now wrap it regardless of it
    being custom or provided by auto-configuration.

    Closes gh-8223