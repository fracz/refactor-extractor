commit a30bfb31cc5feacb8f717c6d4ef0c81802eaa4a7
Author: teichsta <devnull@localhost>
Date:   Wed Jul 11 16:21:55 2012 +0200

    fix - apply configured filters when downloading files from dropbox
    enhancement - query for deltas if 'hasMore==true' is returned, upload file from local to Dropbox if requested (false by default), improved logging