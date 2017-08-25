commit 4753bcc0e2fd9417e885e128e8c9ab4bfc566c32
Author: Michael Hamann <michael@content-space.de>
Date:   Mon Nov 15 22:36:26 2010 +0100

    Indexer improvement: regex instead of arrays for lines

    When updating a single line that line was split into an array and in a
    loop over that array one entry was removed and afterwards a new one
    added. Tests have shown that using a regex for doing that is much faster
    which can be easily explained as that regex is very simple to match
    while a loop over an array isn't that fast. As that update function is
    called for every word in a page the impact of this change is
    significant.