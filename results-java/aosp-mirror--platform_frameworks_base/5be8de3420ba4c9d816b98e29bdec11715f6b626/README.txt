commit 5be8de3420ba4c9d816b98e29bdec11715f6b626
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue May 24 18:11:57 2011 -0700

    More compatibility mode improvements.

    We now correctly adjust display metrics, fixing for example issues
    seen in Barcode Scanner.  In addition the decision about when to use
    compatibility mode has a bug fixed where certain apps would not go
    out of compatibility mode even though they should be able to.

    Change-Id: I5971206323df0f11ce653d1c790c700f457f0582