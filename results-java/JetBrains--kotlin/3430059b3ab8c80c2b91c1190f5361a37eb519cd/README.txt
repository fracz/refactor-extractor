commit 3430059b3ab8c80c2b91c1190f5361a37eb519cd
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Apr 10 15:51:53 2017 +0300

    Minor, refactor TargetPlatformDetector.getPlatform

    Inline dangerous getPlatform(PsiFile) method to prevent it from being
    called accidentally instead of getPlatform(KtFile)