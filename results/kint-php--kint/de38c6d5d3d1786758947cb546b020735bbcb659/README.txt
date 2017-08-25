commit de38c6d5d3d1786758947cb546b020735bbcb659
Author: Jonathan Vollebregt <jnvsor@gmail.com>
Date:   Fri May 12 23:53:56 2017 +0200

    Kint_SourceParser: Big performance improvements

    It turns out realTokenIndex was a major performance killer.
    Especially when running backwards to find tokens that were
    passed over... By storing the last 3 real tokens we don't
    need to loop backwards and find them again, which makes parsing
    far faster.

    For reference, on a ~51k line test file the previous code went
    from 200ms at line 1 to 2750 ms at line 50k. After this it goes
    from 200ms to 350ms. The 200ms seems to be the bare minimum, as
    it's the runtime for the builtin function token_get_all.