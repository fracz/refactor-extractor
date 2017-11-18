commit 84ad7c25608aecaad925380466c4530c2afc23f8
Author: Chris Beams <chris@gradle.com>
Date:   Thu Oct 22 17:14:26 2015 +0200

    Introduce installation and executable specs

    This commit introduces the `NativeInstallationSpec` and
    `NativeExecutableFileSpec` types in order to make the concepts they
    represent first-class citizens of the native software model. Doing so
    has a number of benefits, including:

     1. providing users with better insight about these items, e.g. making
     them visibile in the model report

     2. making these items "addressable", so that they may be depended upon
     directly as inputs, e.g. via the @Path annotation, which in turn allows
     users to write rules against these items and to better ensure correct
     ordering of their configuration

     3. allowing users to configure these items directly within the build
     script

    And while this change is just a stepping stone toward the first two
    benefits being realized, the third benefit (configurability in the build
    script) is now possible where it wasn't before. Previously, the
    installation directory path for a given native executable binary was
    hard-coded within native component model plugin's @BinaryTasks rule.
    This approach meant that there was no way for users to customize the
    installation directory path. Now, the installation directory can be
    specified explicitly within the build script, e.g.:

        main(NativeExecutableSpec) {
            sources {
                // ...
            }
            binaries.withType(NativeExecutableBinarySpec) {
                installation.directory = file("foo/custom")
            }
        }

    The motivation for making this change began with attempts to refactor
    uses of `BinaryContainer` to `ModelMap`. During one such refactoring,
    task ordering issues were encountered in which tests were being run
    before certain sources they depend on had been generated. The generated
    source set in question was implicitly modeled as part of a sequence of
    other "unaddressable" items in the build pipeline, rendering it
    impossible to declare an explicit dependency on the task that generates
    the source set. Ultimately this meant that the task ordering issue could
    not be properly resolved, and that the refactoring could not continue.

    The installation directory and executable file items were two of the
    items in that overall build pipeline, and were selected as candidates
    for this change because of their relative simplicity. Further such items
    will need to be broken out and modeled in a similar way for the ordering
    issue described above to be resolved, and for the refactoring to
    continue.

    This commit can be seen as a successful first experiment toward making
    many other similar items explicit, visible, addressable and
    configurable. This work has begun in the native software model, but
    should be expected to continue into the Java software model and beyond.

    Note that breaking changes have been introduced here, and the release
    notes have been updated to reflect. Because many similar breakages may
    follow, the item added to the notes is just a placeholder until their
    scope and potential impact are clear.