commit 838edab168c2999af7ba16f000c1180814e98570
Author: Simon Willnauer <simonw@apache.org>
Date:   Fri Jan 29 15:07:49 2016 +0100

    Use index name rather than the index object to check against existence in the set

    This is an issue due to some refactoring we did along the lines of Index.java etc.
    We pass the index object to Set#contains which should actually be only it's name.

    Closes #16299