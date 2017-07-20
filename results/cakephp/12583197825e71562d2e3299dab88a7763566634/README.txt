commit 12583197825e71562d2e3299dab88a7763566634
Author: mark_story <mark@mark-story.com>
Date:   Thu Sep 4 21:49:57 2014 -0400

    Remove dead code and reduce # of times tables are listed.

    There is no reason to get the table list for each and every fixture
    creation. The important part - the original tables - is unlikely.

    This should help improve performance on apps with many tables, as you
    shave N queries per test method which can all add up.