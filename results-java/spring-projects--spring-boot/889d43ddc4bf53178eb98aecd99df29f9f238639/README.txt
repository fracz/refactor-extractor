commit 889d43ddc4bf53178eb98aecd99df29f9f238639
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Mon May 15 18:21:34 2017 -0700

    Refine SpringApplication source types

    Update `SpringApplication` so that the `run` methods and constructors
    now require `Class<?>` arguments, rather than `Objects`. String based
    sources can still be loaded, but must now be set on the `getSources()`
    collections. `Package` and `Resource` types are no longer directly
    supported.

    This change should help IDEs offer better content assist, and will
    help integrations with alternative languages such as Ceylon.

    Users currently passing in Class references or using the
    `spring.main.sources` property should not be affected by this change. If
    an XML resource is being used, some refactoring may be required (see the
    changes to `SampleSpringXmlApplication` in this commit).

    Fixes gh-9170