commit 2b40223a921ae8ea705c604b92f6de5bc6579800
Author: mark_story <mark@mark-story.com>
Date:   Tue Apr 22 22:53:05 2014 -0400

    Switch tasks from using execute() to using main().

    This change unifies Tasks and Shells. It removes an un-necessary
    convention that has little benefit but causes additional human mental
    energy to remember. Removing it makes refactoring a shell into a task
    easier.