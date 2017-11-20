commit 3dfabbbd0c4d0a090d82961f0c98ab86928acf82
Author: Daniel Applebaum <danapple@danapple.com>
Date:   Sun Feb 1 00:08:14 2009 +0000

    Provide folder threading support.  When threading is on, messages in
    folders are sorted first by subject, then by date.  Subject sorting is
    case insensitive and disregards leading re:, fw: and fwd: prefixes.

    Threading can be toggled via the T hotkey or the option menu.

    The icons could use improvement.

    Threading state is maintained during a run of K-9, among all
    accounts.