commit 184be1c7858cbc6bb8bb93c0a0a487a08c914cb6
Author: Michael Staib <mstaib@google.com>
Date:   Sat Feb 27 00:00:46 2016 +0000

    Stop using preprocessed .aidl files for types in the same android_library.

    This makes very little sense, because we only do this when compiling
    them within a rule. The preprocessed files are not shipped to other rules,
    meaning we treat things differently depending on the dependency
    structure. And, worst of all, this can lead to a bug in the aidl compiler,
    which causes the preprocessed definition to take precedence over the input
    file, which causes certain modifiers to be stripped away.

    Also, Gradle doesn't do it, and that's proof enough that this is no longer
    the way to go, if ever it was.

    Unfortunately, this causes a problem: the preprocessing had an effect, in
    that all preprocessed types are available without the use of imports. This
    means that .aidl files which were previously legal before this change may
    now be broken, if they relied on this behavior. But, those .aidl files are
    actually not legal according to the .aidl specification. Unfortunate, but
    the right thing to do.

    This CL also updates the idl documentation. Which, let's face it, could
    probably have used some improvement.

    RELNOTES[INC]: .aidl files correctly require import statements for types
    defined in the same package and the same android_library.

    --
    MOS_MIGRATED_REVID=115718918