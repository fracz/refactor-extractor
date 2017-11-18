commit a28b54033227d930672ec7f2714de52e5e0a67eb
Author: Ulf Adams <ulfjack@google.com>
Date:   Fri Feb 24 09:28:44 2017 +0000

    Fix Cpp action caching

    This combines both previous changes and extends them to work both with and
    without kchodorow@'s rollout of the exec root rearrangement. Unfortunately,
    each of these changes individually breaks something somewhere, so they must
    all go into a single commit.

    Change 1:
    CppCompileAction must return false from inputsKnown for .d pruning

    This is necessary (but not sufficient) for the action cache to work
    correctly. Consider the following sequence of events:
    1. action is executed
    2. .d pruning is performed
    3. action cache writes entry with post-pruning inputs list
    4. action gets regenerated (e.g., due to server restart)
    5. the action cache calls inputsKnown(), which returns true
    6. action cache checks entry from step 3 against pre-pruning inputs list,
       which results in a cache miss

    The action cache needs to know that the current list is not the final list,
    so inputsKnown() in step 5 must return false if .d pruning is enabled.

    Change 2:
    Fix artifact root discovery for external artifacts

    The SkyframeExecutor was assuming that all exec paths were coming from the
    main repository. Instead, rely on external exec paths to start with "../".

    Additional change 3:
    In addition, update the PackageRootResolverWithEnvironment and the
    HeaderDiscovery to use the single unified repository name guessing
    implementation. Previously, the PackageRootResolverWithEnvironment was
    poisoning the source artifact cache, which then caused subsequent lookups
    to return a bad artifact.

    Add a precondition to double-check that artifacts looked up by exec path
    have the correct source root.

    For compatibility with kchodorow@'s upcoming exec root refactor, if the exec
    path starts with "external", then assume it's coming from an external
    repository. This must be removed when that change is successfully rolled out,
    or it will break if anyone creates a package called 'external'.

    Additional change 4:
    On top of all of that, PackageRootResolverWithEnvironment and SkyframeExecutor
    must use the same source root computation as the Package class itself. I
    extracted the corresponding code to Root, and added comments both there and
    in Package to clearly indicate that these methods have to always be modified
    in sync.

    Fixes #2490.

    --
    PiperOrigin-RevId: 148439309
    MOS_MIGRATED_REVID=148439309