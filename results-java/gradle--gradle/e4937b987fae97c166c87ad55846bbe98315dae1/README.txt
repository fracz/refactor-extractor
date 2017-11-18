commit e4937b987fae97c166c87ad55846bbe98315dae1
Author: Adam Murdoch <adam@gradle.com>
Date:   Sun Oct 15 10:40:21 2017 -0400

    Reorganised and simplified the hacks required to resolve C++ libraries from Maven repositories to take advantage of the ability to declare dependencies in the module metadata file.

    The published module metadata file for a C++ library now includes references to the link and runtime variants published in the `_debug` module. This means that the debug link/runtime variant is always selected regardless of the requested variant, which was also a limitation of the previous hacks so is not a regression. Referencing the release variants in the published module metadata file require some further improvements to the publishing and resolution infrastructure.

    Resolution for the link and runtime files in the C++ plugin uses a regular `Configuration` object without any workarounds.