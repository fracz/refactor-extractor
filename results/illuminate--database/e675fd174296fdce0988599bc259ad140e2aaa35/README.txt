commit e675fd174296fdce0988599bc259ad140e2aaa35
Author: Adam Wathan <adam.wathan@gmail.com>
Date:   Mon Jan 26 20:18:29 2015 -0500

    Remove dead code

    This appears to be left-over from before a later refactoring. This
    function is never called with the `change` parameter, and the code path
    for changing columns is totally different.