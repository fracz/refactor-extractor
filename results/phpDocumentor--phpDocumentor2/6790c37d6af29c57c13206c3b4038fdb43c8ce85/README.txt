commit 6790c37d6af29c57c13206c3b4038fdb43c8ce85
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sat Jan 18 11:24:04 2014 +0100

    Extract Methods, Arguments, DocBlocks and Tags

    With this commit we extract the method, argument and DocBlock handling
    fromt he XML writer into their own class. This refactoring makes it
    easier to re-use specific parts (recent bug fixes needed significant
    work without these refactorings) and by adding unit tests immediately
    we also increase code coverage.