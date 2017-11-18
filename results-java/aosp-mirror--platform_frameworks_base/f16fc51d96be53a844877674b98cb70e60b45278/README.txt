commit f16fc51d96be53a844877674b98cb70e60b45278
Author: Michael Jurka <mikejurka@google.com>
Date:   Mon Feb 27 15:02:23 2012 -0800

    Remove unnecessary code

    Found cleaner way to improve recents scrolling
    performance on crespo-- instead of rendering the
    background in the items, instead we just set
    a window flag. Removed need for a lot of code.