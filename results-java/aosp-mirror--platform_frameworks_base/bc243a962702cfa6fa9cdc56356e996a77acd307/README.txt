commit bc243a962702cfa6fa9cdc56356e996a77acd307
Author: Selim Cinek <cinek@google.com>
Date:   Tue Sep 27 16:35:13 2016 -0700

    Fixed a few bugs regarding big notification groups

    The overscrolling was listening to the paddingOverflow
    which didn't make sense. Also, we need to update the
    top padding if the height of the first element changes.

    This also fixes several cases where the notification size was
    wrong when the quick settings panel was expanded.
    It also fixes some flickering regarding the TopPaddingoverflow
    which was going rogue in a few cases.
    The transition from the locked shade is thereby also improved.

    Change-Id: I703ea27879b325c02a15fdacee3b58f5ef78fd20
    Fixes: 30801139