commit f92fc2d0564833de88d2f891878bcf7de236660c
Author: Iain Sproat <iainsproat@gmail.com>
Date:   Wed Jun 16 12:35:37 2010 +0000

    Internal refactor for IO - HistoryEntry is now a concrete class, so can be instantiated (reverting Operations classes back to r972 which were changed as a result of HistoryEntry being abstract).

    HistoryEntry now deals with backend (filesystem etc.) through classes which implement HistoryEntryManager.  This HistoryEntryManager is held by ProjectManager, which allows for FileProjectManager to create FileHistoryEntryManager as appropriate.

    git-svn-id: http://google-refine.googlecode.com/svn/trunk@982 7d457c2a-affb-35e4-300a-418c747d4874