commit bf92e90c2d4df9cd942f78f0b1e146ae4f392176
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Thu Apr 4 20:33:22 2013 +0000

    Fix missing messages and re-add target parameter

    Due to the refactoring in the previous commit was an error introduced
    that caused all messages in the core plugin not to be shown. This
    was caused because Zend\I18n reindexes the arrays for messages when
    you have multiple sources. Since the original codes were purel numeric
    they were re-ordered as different integers.

    In addition to the above the target option was re-added to the parse command
    as a simpler version.