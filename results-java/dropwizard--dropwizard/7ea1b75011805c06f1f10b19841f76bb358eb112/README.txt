commit 7ea1b75011805c06f1f10b19841f76bb358eb112
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Mon Dec 14 14:56:15 2015 +0300

    Refactor ResourceTestRule

    Currently the `ResourceTestResourceConfig` class is kind of a mess.
    It uses several inner classes, a static map of test rules,
    and doesn't have any comments. It's hard to see what's going on there.

    This change:
    * moves the inner class `ResourceTestResourceConfig` to a separate class
    named `DropwizardTestResourceConfig`. This name is more meaningful and
    suitable in my opinion;
    * introduces the class `ResourceTestJerseyConfiguration` which contains
    a configuration passed by a user via `ResourceTest.Builder`. It allows
    to decouple `DropwizardTestResourceConfig` from `ResourceTestRule`.
    * this class is also used for passing the current `ResourceConfig`
    configuration via a servlet context instead of the `ResourceTestRule`
    class in a Grizzly web test container. This removes additinal responsibility
    from the `ResourceTestRule` class.
    * adds a minimal documentation for every class, describing their goals.

    As a result, this change should make easier to make future improvements
    to the `ResourceTestRule` class, because of separation of concerns, better
    class names and added documentation.