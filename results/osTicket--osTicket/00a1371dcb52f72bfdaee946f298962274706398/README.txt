commit 00a1371dcb52f72bfdaee946f298962274706398
Author: Jared Hancock <jared@osticket.com>
Date:   Wed Apr 13 14:51:05 2016 -0400

    files: Attempt to standardize thread entry attaching

    This commit attempts to remove some of the confusing and redundant code to
    attach files to thread entries and replace it with a single code base. It
    also attempts to remove and error where a single attachment might be
    attached to a new thread entry multiple times.

    Lastly, it removes the insert followed by an update for emails with inline
    images. This should improve performance processing emails as only one trip
    to the database is now necessary for thread entries with inline images.