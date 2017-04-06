commit 62dbe85798c19246fb2fb121bb4278248e564486
Author: Karl Seamon <bannanafiend@gmail.com>
Date:   Wed Dec 4 16:02:28 2013 -0500

    perf: use call and === instead of apply and == in type check functions

    Updates isDate et al to use call instead of apply and === instead of ==.
    The change to call brings minor performance improvement and === is just
    better practice than ==.
    http://jsperf.com/call-vs-apply-tostring

    Closes #5295