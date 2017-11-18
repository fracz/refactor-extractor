commit 5dc5a913134354dbd3bd58a03dad90190f177212
Author: peter <peter@jetbrains.com>
Date:   Wed Mar 30 18:02:57 2011 +0200

    don't cancel lookups by clicking outside: this hurts inplace refactorings, and in usual circumstances caret and selection listeners will do the job