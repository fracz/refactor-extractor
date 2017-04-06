commit 39c82f3fb7a8459304d5e07dc87bd0623ad1efd0
Author: Julie <ju.ralph@gmail.com>
Date:   Wed Feb 19 21:01:54 2014 -0800

    chore(travis): reorganize protractor configs to group by spec instead of by browser

    Use the multiConfiguration ability of Protractor to start tests on multiple browsers
    from the same travis cell. Group tests by type (jquery, jqlite, or docs tests) instead
    of by browser. Turn on tests for jQuery.