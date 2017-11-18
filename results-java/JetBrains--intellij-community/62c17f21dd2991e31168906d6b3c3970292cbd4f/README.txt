commit 62c17f21dd2991e31168906d6b3c3970292cbd4f
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Sun Mar 24 15:57:29 2013 +0400

    [git] Minor GitRootScanner refactoring

    Calling "new GitRootScanner(project);" looks a bit weird.
    Let's make the constructor private,
    and call static GitRootScanner.start().