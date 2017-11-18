commit dfbaf90951c5eaa7fd40a956f24b042aa11a2e23
Author: Colin Goodheart-Smithe <colings86@users.noreply.github.com>
Date:   Wed Aug 9 15:53:30 2017 +0100

    Adds ToXContentFragment (#25771)

    * Adds ToXContentFragment

    This interface is meant for objects that implement `ToXContent` but are not complete objects. It is basically the opposite of `ToXContentObject`. It means that it will be easier to track the migration of classes over to the fragment/not fragment ToXContent model as it will be clear which classes are not migrated. When no classes directly implement `ToXContent` we can make `ToXContent` package private to be sure that all new classes must implement `ToXContentObject` or `ToXContentFragment`.

    * review comments

    * more review comments

    * javadocs

    * iter

    * Adds tests

    * iter

    * adds toString test for aggs

    * improves tests following review comments

    * iter

    * iter