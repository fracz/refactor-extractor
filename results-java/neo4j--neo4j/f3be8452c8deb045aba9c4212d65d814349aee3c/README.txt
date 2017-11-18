commit f3be8452c8deb045aba9c4212d65d814349aee3c
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Mon Jul 7 11:45:27 2014 +0200

    Reimplement IdGenerator rebuilding

    The procedure for rebuilding the IdGenerators has been reimplemented to use the new PageCache API for accessing
    the store files, instead of accessing the StoreChannels directly. We had to make this change, because the page
    cache may contain changes that are not reflected in the store channel.
    This commit also introduces some refactoring, where the procedure has been generalised for all stores, and
    pulled up into the CommonAbstractStore class. This removes 3 other implementations in various stores.
    Tests have been added to make sure that the behaviour did not otherwise change. One thing that has changed, is
    how we count the number of ids that have been defragmented and salvaged. This is because we now operate in
    whole-page increments, and so we now also count the last empty records of the last page in the file.