commit b7adc19d29e6e5bf10b3e0f37a081ece3d96475c
Author: Borek Bernard <borekb@gmail.com>
Date:   Sun Feb 15 01:18:27 2015 +0100

    [#302] GitRepository cleanup. Some details:

     - `add()` and `update()` methods merged into a single `stageAll()` because that's all we really use
     - Removed `assumeUnchanged()` - was used from Initializer only and in some left-over code (we now have .gitignore instead of this temporary method)
     - Removed `pull()` and `push()` methods that are not going to be used in VersionPress 1.0
     - `commit()` method's $authorName and $authorEmail no longer have defaults (empty values didn't make sense and were never actually used)
     - Inlined constants containing git commands so that they are closer to the code that uses them
     - `runShellCommand()` made private (again)
     - Many small rename refactorings and PhpDoc updates