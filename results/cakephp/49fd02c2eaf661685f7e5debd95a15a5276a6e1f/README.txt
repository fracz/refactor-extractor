commit 49fd02c2eaf661685f7e5debd95a15a5276a6e1f
Author: Jose Lorenzo Rodriguez <jose.zap@gmail.com>
Date:   Mon Dec 31 19:54:42 2012 +0100

    Temporarily refactoring how bound values are stored to fix a bug
    Changed andWhere and orWhere so they don't need where() to be called
    before