commit 12d6b6e7e4bbe1b989663b11fd2c3ace95ac2e5a
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Thu Oct 22 23:26:37 2015 +0300

    Use "Any?" as out-type for type parameters instead of intersection of bounds

    Also refactor checkSubtypeForTheSameConstructor to compute everything in the
    best order