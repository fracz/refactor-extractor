commit d2d1e7ac2655eb1254027871f0420f08076c7110
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Wed Nov 30 11:14:11 2011 +0100

    use new FileSystem abstraction/implementation in production code

    refactored MyFileSystem into FileSystems/FileSystem/DefaultFileSystem
    removed OperatingSystem.getFileSystem() and the underlying file system implementations and replaced all usages with FileSystems.getDefault()
    fixed file system related tests which were green for the wrong reasons
    introduced another test precondition (NO_FILE_LOCK_ON_OPEN)