commit bd5a779380fe0475df1140a16c2391634f477f50
Author: Peter Schaeffer <prestashop@shopmonauten.com>
Date:   Fri Nov 6 12:01:08 2015 +0100

    [*] CORE : improvement of override possibilities.

    There might be more SSL pages than conditions and revocation.
    Because of this the IDs should be retrieved in an own method to improve ability to override it. -> No need to override whole init method.