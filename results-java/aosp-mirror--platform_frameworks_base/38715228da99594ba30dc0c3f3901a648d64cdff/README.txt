commit 38715228da99594ba30dc0c3f3901a648d64cdff
Author: Tianjie Xu <xunchang@google.com>
Date:   Wed Mar 22 12:00:16 2017 -0700

    improve the format of locale argument when calling recovery

    Switch the locale argument from Locale.toString() to
    Locale.toLanguageTag(). The new format is more readable and less error
    prone.

    Bug: 35215015
    Test: recovery processes sr-Latn correctly

    Change-Id: I47e1cf54434cb841652d4b259e0e829104fb19a2