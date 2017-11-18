commit 7dba6eb3ac4fd6c8195cb0d0425866de50a9e114
Author: Narayan Kamath <narayan@google.com>
Date:   Wed Jul 16 08:53:30 2014 +0100

    Adjust nativeLibraryDir for package contexts of multiArch installs.

    When we're creating a package context for a multi-arch app,
    adjust the native library directory to match the bitness of
    the process creating the context.

    This change also removes apkRoot (which wasn't really being used)
    and replaces it with a fully constructed secondary library path.
    The secondary library path is a transitional measure until we
    can reorganize the system image so that we can use nativeLibraryRoot
    for system paths as well (nativeLibraryRootRequiresIsa will then
    be true for all packages except for legacy installs).

    bug: 16013931

    Change-Id: I5ae090334b377b9e087aecf40075fab81b20b132