commit acb9e723550922feb76e100074a225ca9ac61517
Author: mark_story <mark@mark-story.com>
Date:   Mon Nov 24 05:36:06 2008 +0000

    Minor refactor to helper callback triggering.  Moving repeated loops to _triggerHelpers(). Allows View subclasses to add additional callbacks.

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@7886 3807eeeb-6ff5-0310-8944-8be069107fe0