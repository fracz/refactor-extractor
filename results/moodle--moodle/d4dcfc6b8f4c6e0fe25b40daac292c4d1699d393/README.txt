commit d4dcfc6b8f4c6e0fe25b40daac292c4d1699d393
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Mon May 31 15:30:45 2010 +0000

    MDL-20204 Fixed regression in the recent url_select improvements

    Now, url_select checks for valid URLs in optgrouped lists and paramater
    'selected' is converted to the local URL form, too. The patch also
    fixes a legacy handling of thr Gradebook plugins selector.