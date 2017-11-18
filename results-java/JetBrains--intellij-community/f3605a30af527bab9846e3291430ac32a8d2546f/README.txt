commit f3605a30af527bab9846e3291430ac32a8d2546f
Author: Irina.Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Mon Jun 5 23:22:15 2017 +0200

    es6 convert to export: improve when module.exports in comma expression

    - gather inline comments also around expression
    - better cut of the expression from comma expression together with
     inline comments around (old method does this poorly) and comma