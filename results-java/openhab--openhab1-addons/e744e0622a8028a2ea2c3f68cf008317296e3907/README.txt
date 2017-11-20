commit e744e0622a8028a2ea2c3f68cf008317296e3907
Author: Chris Carman <namraccr@gmail.com>
Date:   Sun Sep 24 13:31:35 2017 -0400

    Fixed the Dropbox startup failure (#5247)

    * Fix the Dropbox startup failure.

    This required only setting non-null values for the appKey and appSecret
    that would be acceptable to the com.dropbox API.

    * Refactor.

    * Additional efficiency improvement.

    * Clean up the README.

    * Fix warnings, clean up, and format.