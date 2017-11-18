commit 4f40c5bd587af8262c66daeea4d30843eb823324
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Mon Feb 27 19:44:31 2012 +0400

    Git roots refactoring. Step 1. GitRootDetector

    GitRootDetector detects actual .git repositories above and inside the project directory in the file system and returns the analysis result as GitRootDetectInfo.
     GitRootDetectorTest - unit test for different roots configuration.