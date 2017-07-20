commit baea31912299be11ea4fa76f62c2473103babf31
Author: mark_story <mark@mark-story.com>
Date:   Tue Jul 14 17:11:46 2009 +0000

    Applying patch from 'Dremora' fixes JavascriptHelper::codeBlock() and blockEnd() bugs where content vanished due to incorrectly structured buffers. Minor behavior change in that codeBlock(null) no longer returns an opening script tag.  The entire script tag is returned when blockEnd() is called.
    Test cases updated and refactored.
    Fixes #6504


    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@8231 3807eeeb-6ff5-0310-8944-8be069107fe0