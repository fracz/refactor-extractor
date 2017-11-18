commit 497457c397d9c45b127285c5cc3f2ba36aa5b890
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Wed Jul 26 13:29:38 2017 -0700

    Rename ApplicationContextTester -> Runner

    Rename `ApplicationContextTester` and related classes to
    `ApplicationContextRunner` and refactor existing tests to use correctly
    named variables.

    See gh-9875