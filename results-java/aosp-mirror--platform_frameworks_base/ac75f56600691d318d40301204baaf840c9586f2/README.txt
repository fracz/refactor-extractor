commit ac75f56600691d318d40301204baaf840c9586f2
Author: Grace Kloba <klobag@google.com>
Date:   Wed Feb 3 10:24:06 2010 -0800

    Enable StreamLoader to be loaded in a separate thread.

    Move ContentLoader and FileLoader to this new way
    as they involves IO. Will work on CacheLoader later.

    Change StreamLoader to contain a Handler instead of
    derive from a Handler so that the Handler can be
    created in the thread where load() is called.

    Rename StreamLoader's old "LoadListener mHandler"
    to mLoadListener.

    Remove unused import and unreachable exception.

    Fix http://b/issue?id=2158613

    This improved page_cycler performance in moz/intl by
    10-30% as we are not blocked by IO any more.