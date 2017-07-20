commit 0587683d8e9d9b1ee791be257faf240b80cd1d48
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Sat Mar 26 06:45:47 2011 +0000

    Refs #572
     * Adding Date Range calendar UI, with "Apply Date Range" button. Possibility to edit the INPUT fields directly rather than clicking in the calendar. Testing that dates are valid (from < to, valid string).
     * Updating calendar to show Loading.. on click (hopefully it makes it less confusing)
     * Fixing few bugs when period=range
     * All reports should load correctly when period=range, including sparklines, graphs & standard tables
     * Refs #2145 renaming parameter to $filter_limit which seems to work OK - it's not even a hack isn't it? :)
    TODO:
     * refactor period_select.tpl JS in helper,
     * Metadata compatibility with period=range (at least the World map doesn't work for now)
     * previous TODO still valid

    Testing is welcome, please report any bug you find!

    git-svn-id: http://dev.piwik.org/svn/trunk@4188 59fd770c-687e-43c8-a1e3-f5a4ff64c105