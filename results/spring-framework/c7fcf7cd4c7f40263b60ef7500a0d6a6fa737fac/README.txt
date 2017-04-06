commit c7fcf7cd4c7f40263b60ef7500a0d6a6fa737fac
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Mon Mar 9 16:26:41 2015 +0100

    `@Configuration` doc improvements

    Various documentation improvements related to `@Configuration` and
    `Bean`. Better describe how method parameter can be used to declare
    dependencies of a particular bean. Also add an explicit mentions related
    to "hard-wiring" of dependencies in configuration classes.

    Issue: SPR-12773