commit c36638d159a66be3e8451a93b95c2373324d25f4
Author: Shay Banon <kimchy@gmail.com>
Date:   Tue Dec 4 02:00:36 2012 +0100

    not delete filter improvements
    - don't check no null for liveDocs, since we know they are not null with the check for hasDeletion
    - improve iteration over liveDocs vs. innerSet, prefer to iterate over the faster one