commit 9016d466aad0cea2c89074e70069a425341b6cb7
Author: Adam Murdoch <adam@gradle.com>
Date:   Thu Oct 5 06:04:30 2017 +1000

    Removed most of the hackery used to resolve the C++ header files for the compile dependencies of C++ components. This is no longer required as the dependency resolution engine now uses the contents of the Gradle module metadata file to figure out the result. This means, for example, that there is now 1 dependency graph resolve rather than 2 to resolve the header files, the result is no longer opaque to reporting and diagnostics, and the consuming build makes fewer assumptions about how a C++ library is structured. For example, the consuming build can deal with libraries for which there are no headers (eg a resource-only library) or libraries with variant specific headers.

    Some hackery still remains. Unpacking the headers can later be replaced with an artifact transform. Cleaning up the resolution of link and runtime files is waiting on further improvements to the dependency resolution engine.

    This is the first real usage of the Gradle metadata file.