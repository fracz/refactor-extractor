commit 417cd727151ce4b2a784b4a31fff7f7b929451a1
Author: Mike van Riel <me@mikevanriel.com>
Date:   Tue May 20 07:50:42 2014 +0200

    #1194: Add support for default argument values

    Magic methods did not support default values for their arguments.
    With this commit we improve the argument algorithm to detect, and
    set, these and in the process make the algorithm more foolproof.