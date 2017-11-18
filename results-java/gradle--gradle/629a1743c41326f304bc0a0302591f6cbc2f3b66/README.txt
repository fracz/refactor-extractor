commit 629a1743c41326f304bc0a0302591f6cbc2f3b66
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Dec 17 14:46:06 2011 +0100

    First stab at providing build environment information via tooling api...

    -Introduced new BuildEnvironment model. Unfortunately, the implementation must still inherit from ProjectVersion3 which is quite awkward because BuildEnvironment is not really a project. There might be away to implement it cleaner but it's difficult due to compatibility contract.
    -Added tiny bit of comments here and there and improved the javadocs.