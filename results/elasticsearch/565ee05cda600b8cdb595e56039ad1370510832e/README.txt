commit 565ee05cda600b8cdb595e56039ad1370510832e
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu Mar 5 21:41:01 2015 +0100

    [ENGINE] Inc store reference before reading segments info

    If a tragic even happens while we are reading the segments info
    from the store the store might have been closed concurrently. We had this behavior
    before and was lost in a refactoring.