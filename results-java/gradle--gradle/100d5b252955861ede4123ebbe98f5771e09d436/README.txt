commit 100d5b252955861ede4123ebbe98f5771e09d436
Author: daz <daz@bigdaz.com>
Date:   Tue Dec 27 23:24:16 2011 -0700

    Switch MD5 strings for identifying files back to radix 32
    - This was an unintentional change in a previous refactor. Everything works with MD5 as Hex, but the file names are less compact.