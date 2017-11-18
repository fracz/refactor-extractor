commit efb2c7b3a5b0cb892afd4487c02a5c7839c35e33
Author: Adam Murdoch <adam@gradle.com>
Date:   Wed Sep 21 12:44:16 2016 +1000

    Some improvements and fixes to `SourceDirectorySet` to allow source directory sets to be wired together and queried in various ways without resolving the source directories as a set of `File` instances:

    - Added `SourceDirectorySet.getSourceDirectories()` to allow the source directories of the test to be referenced as a `FileCollection` without actually resolving them.
    - Fixed `SourceDirectorySet` so that the dependencies of `Buildable` source directories are visible as dependencies of the `SourceDirectorySet` and its various views, and to avoid resolving the source directories.