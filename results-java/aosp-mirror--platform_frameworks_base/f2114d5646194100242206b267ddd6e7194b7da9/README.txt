commit f2114d5646194100242206b267ddd6e7194b7da9
Author: Raph Levien <raph@google.com>
Date:   Sun Jun 1 15:54:47 2014 -0700

    Better Minikin integration

    This patch improves the Minikin integration in a number of ways,
    including:

    Software rendering does text decorations and handles alignment
    correctly. This fixes bug 15139067 "Centered text isn't working".

    Paint implements getTextPath. This fixes bug 15143354 "Text rendering in
    Maps Navigation wrong typeface?"

    Also a bit of refactoring, since there was duplicated code for iterating
    font runs that's now a static method in MinikinUtils.

    Change-Id: I4cfdb2c0559982376348325a757d95235fab1768