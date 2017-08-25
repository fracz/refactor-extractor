commit 65202d2a18aca3b253ce5f3c7c9af17ee6d12e52
Author: Lukas Reschke <lukas@owncloud.com>
Date:   Thu Mar 26 14:51:33 2015 +0100

    Add check for activated local memcache

    Also used the opportunity to refactor it into an AppFramework controller so that we can unit test it.

    Fixes https://github.com/owncloud/core/issues/14956