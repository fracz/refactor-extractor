commit 0604bb7b7a6156e33679396e805e327662d9a178
Author: Smitha Milli <smitha.milli@gmail.com>
Date:   Sat Aug 23 12:57:31 2014 -0500

    fix(ngRepeat): improve errors for duplicate items

    -Log the value that had the duplicate key, as well as the key
    The error that is thrown when items have duplicate track by keys can be
    confusing because only the duplicate key is logged.  If the user didn't
    provide that key themselves, they may not know what it is or what item
    it corresponds to.