commit eac7505d7a11322df7ba6fce4520f1a69beefe81
Author: Alan Jenkins <alan.christopher.jenkins@gmail.com>
Date:   Tue Aug 7 13:04:41 2012 +0100

    user settings controller: unpublish broken public methods used by index()

     * ban()
     * activate()
     * deactivate()

    ...all have no view file, so they won't work as public methods.  And
    the second two only accept arrays, which CodeIgniter doesn't provide.

     * delete()
     * restore()
     * purge()

    ...redirect to index(), which does have a view file.  However, they still
    do the redirect when called by index(), dropping any filter parameter
    which has been set by the user.

    I moved the precondition checks into index() (and made them consistent).
    There was so much repeated code there, and it didn't make enough sense
    to keep.  Maybe we'll need public versions of the other methods at some
    point, and it'll need refactored again.  But it should be easier to see
    what you're doing if they're not littered with dead code paths.