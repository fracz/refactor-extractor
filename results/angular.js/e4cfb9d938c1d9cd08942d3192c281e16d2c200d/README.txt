commit e4cfb9d938c1d9cd08942d3192c281e16d2c200d
Author: metaweta <metaweta@gmail.com>
Date:   Tue Jan 29 11:30:12 2013 -0800

    refactor(Angular.js): prevent Error variable name leak in tests

    Remove var Error = window.Error

    window.Error is a read-only property in Apps Script.

    Igor says, "we should just delete that line instead. I think it was
    misko's attempt to get better closure minification, but it turns out
    that it's actually hurting us after gzip (I verified it)."