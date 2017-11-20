commit f6dc375869f70df9fbc4926cc1b39ffea6e47b04
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri Apr 15 14:20:48 2011 +0000

    Big refactoring to improve performance at N levels:
    - Serialization now minimizes the creation of temporary small objects like Strings. All serialization method receives a StringBuilder and append data directly to it
    - OLazyRecordSet now keeps the items always sorted for faster retrieval of items