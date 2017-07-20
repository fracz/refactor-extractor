commit fb6b16c467cb4715aa2d9fce2f7b48ef8c4b0e5a
Author: mark_story <mark@mark-story.com>
Date:   Wed Jul 8 03:25:30 2009 +0000

    Fixing empty time value handling in Model::deconstruct().  Both null and 00:00:00 are valid values now.  Test cases added and refactored.  Fixes #6488, #6018, Refs #5659

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@8225 3807eeeb-6ff5-0310-8944-8be069107fe0