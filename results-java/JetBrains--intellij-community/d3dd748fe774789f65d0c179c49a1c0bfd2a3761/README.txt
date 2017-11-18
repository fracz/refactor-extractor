commit d3dd748fe774789f65d0c179c49a1c0bfd2a3761
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Tue Oct 16 20:15:32 2012 +0400

    GitBranch refactoring: encapsulate hash into Hash object; assert in equals

    Introduce the Hash class - encapsulation of the Git hash. To be used everywhere instead of Strings.

    The GitBranch constructor calls have been updated, however, getHash() has been left intact for now, and still returns String.
    There are several creations of the GitBranch object that don't know the hash of the branch at the time of creation. They use DUMMY_HASH object, but this is to be fixed in the future.

    GitBranch.equals still relies just on the name, but now it LOGs an error if two branches with equal names have different hashes, which means that one of the instances is out-of-date.

    Added a javadoc to GitBranch.