commit c011215b7ff4c5d7cda1e1745c1385797d4a3a7d
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Sun Mar 13 23:20:02 2016 +0100

    refactor(jqLite): don't pass useCapture to add/removeEventListener

    The useCapture parameter defaults to false even in oldest of our supported
    browsers; this is no longer needed. jQuery has removed it in 2.2 as well.