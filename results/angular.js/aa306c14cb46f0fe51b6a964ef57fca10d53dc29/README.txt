commit aa306c14cb46f0fe51b6a964ef57fca10d53dc29
Author: gdi2290 <github@gdi2290.com>
Date:   Thu Mar 5 01:48:29 2015 -0700

    refactor(*): introduce isNumberNaN

    window.isNaN(‘lol’); //=> true
    Number.isNaN(‘lol’); //=> false

    isNaN converts it’s arguments into a Number before checking if it’s NaN.
    In various places in the code base, we are checking if a variable is a Number and
    NaN (or not), so this can be simplified with this new method (which is not exported on the
    global Angular object).

    Closes #11242