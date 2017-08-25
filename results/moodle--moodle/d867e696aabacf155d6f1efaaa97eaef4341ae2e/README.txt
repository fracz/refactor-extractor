commit d867e696aabacf155d6f1efaaa97eaef4341ae2e
Author: tjhunt <tjhunt>
Date:   Mon Mar 23 03:54:50 2009 +0000

    accesslib: MDL-18620 do not used static to cache things, it makes unit testing impossible.

    Instead we have a new $ACCESSLIB_PRIVATE for all caching. Regrettably, the only way to make this work in PHP (other than rewriting everything to be methods of a class rather than functions) is to make this a global variable. However $ACCESSLIB_PRIVATE should not be thought of a global, it is a private implementation detail of accesslib.php. (And there is a comment saying that.)

    There is a new function accesslib_clear_all_caches_for_unit_testing(). In a unit test, you need to call this at the start of your test method, before you set up any test data, and again at the end, after you have discarded all your test data.

    This new $ACCESSLIB_PRIVATE subsumes the old $ACCESS, $RDEFS and $DITRYCONTEXTS globals.

    Also, I took the opportunity to refactor the (inconsistently) duplicated code for adding a context to the caches code into a cache_context function.