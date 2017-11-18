commit 39dbc9805f9d60acc9a98685826d2a5bb2dd0908
Author: Ola Rozenfeld <olaola@google.com>
Date:   Thu Nov 17 20:14:56 2016 +0000

    Adding an option to set the digest function that everything uses. Minor refactoring: enabling potential fast digest computation of more than one digest function type.
    Usage: bazel --host_jvm_args="-Dbazel.DigestFunction=SHA1" build ...

    Ugliness: using a system property (a static non-final variable), because the better way to do it (a flag) would result in a much, much larger refactoring.

    More ugliness: I have updated the minimal amount of tests. A lot of tests are still relying on the default value of MD5. Ideally, they need to be updated as well.

    --
    MOS_MIGRATED_REVID=139490836