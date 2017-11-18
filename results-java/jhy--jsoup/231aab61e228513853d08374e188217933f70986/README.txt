commit 231aab61e228513853d08374e188217933f70986
Author: Jonathan Hedley <jonathan@hedley.net>
Date:   Sun Jun 3 18:44:14 2012 -0700

    Replaced Strings with char array in CharacterReader, for well improved parse times.

    Faster to scan, and less garbage created.