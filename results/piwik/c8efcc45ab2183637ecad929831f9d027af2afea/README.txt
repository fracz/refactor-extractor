commit c8efcc45ab2183637ecad929831f9d027af2afea
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Jan 11 04:56:34 2011 +0000

    Page titles report improvements fixes #1898
     * new fields in 2 log tables (major schema upgrade in next release!)
     * code refactored and optimized,
     (only the UI to display top entry/exit page titles is missing)

    Performance improvements
     * Actions Archiving is much more efficient, removed many JOINs and updating algorithm so that we select and parse action names only once per action.
     * Fixes #1600: datatables now indexed by int, no data migration but code works with both old and new data structure
     * Fixes #1780: new index

    Improvements to integration tests
     * never loads the Provider plugin in proxy-piwik.php since reverse ip lookup slows up tests a lot
     * fixing a test result that were previously incorrect (_withCookieSupport) because a static cache wasn't cleaned after each test

    git-svn-id: http://dev.piwik.org/svn/trunk@3696 59fd770c-687e-43c8-a1e3-f5a4ff64c105