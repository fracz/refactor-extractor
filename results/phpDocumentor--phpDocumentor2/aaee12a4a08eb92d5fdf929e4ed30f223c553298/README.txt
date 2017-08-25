commit aaee12a4a08eb92d5fdf929e4ed30f223c553298
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sat Dec 8 11:07:48 2012 +0100

    #697: Multiple templates in configuration only executes first one

    Due to the way the config is parsed (and the lack of a Query language) was only
    the first template picked. With these changes I have enabled a more robust way
    of extracting the templates. However; the command handling should be refactored
    to be more robust overall.

    Included in this commit is the reformatting of the config template and the config
    of phpDocumentor itself