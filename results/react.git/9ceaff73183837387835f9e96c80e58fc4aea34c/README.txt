commit 9ceaff73183837387835f9e96c80e58fc4aea34c
Author: CommitSyncScript <jeffmo@fb.com>
Date:   Wed Jun 26 14:29:31 2013 -0700

    Refactor of ChangeEventPlugin

    some refactoring and also handle if `blur` doesn't fire on the form
    input in IE8 (by always cleaning up on focus). per discussions with
    @balpert