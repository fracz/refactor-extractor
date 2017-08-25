commit 89a5baaba1adfb988fe9a75045a8eb916f9e7e91
Author: defacer <defacer>
Date:   Fri Oct 29 16:56:59 2004 +0000

    Major cleanups (removed things no longer used by the new backup).
    Microsoft must surely be jealous (do more with less).

    Fixed a bug: if somehow the block weights in a page become discontinuous,
    moving things up and down will slowly improve the situation until it's back
    to normal again. It would just fail without notice before.

    Fixed a bug: you could add multiple instances from a block that didn't
    allow it if, after adding the first, you just hit Refresh in your browser.

    Fixed a typo: missing / inside filepath