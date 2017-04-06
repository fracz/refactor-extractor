commit 785a5fd7c182f39f4ae80d603c0098bc63ce41a4
Author: Karl Seamon <bannanafiend@gmail.com>
Date:   Wed Dec 4 16:02:28 2013 -0500

    chore(Angular.js): Use call and === instead of apply and == in type check functions

    Updates isDate et al to use call instead of apply and === instead of ==.
    The change to call brings minor performance improvement and === is just
    better practice than ==.
    http://jsperf.com/call-vs-apply-tostring

    Closes #5295