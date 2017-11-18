commit 78c50da93de7d5b53232e0bc8b4be5477bc44585
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Thu Nov 3 19:13:32 2011 +0300

    New Git push - second take: better notifications, support for multiple branches.

    GitPusher refactored. It displays different notifications for errors, rejected pushes, successful pushes, takes multi-repository and multi-branch configuration into consideration.
    By default GitPushDialog is initialized by branches/commits ready for 'git push', i.e. all tracking or matching branches are to be pushed.