commit 288df9375e098544c8871e6def7a704eb8519f4d
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Thu Apr 14 18:19:35 2016 +0200

    json schema, refactor code that when searching definitions we go to definitions registry with absolute path only once (break the recursive call)