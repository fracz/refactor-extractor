commit 06e2187d5f4f317110b27aa1516f2dd38b31d70b
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri Jul 16 18:14:20 2010 +0000

    Huge improvement on object binding:
    - OUser and ORole are not more hybrid classes that extend ODocument. Now they are mapped as for OGraphElement classes
    - New ODocumentWrapper class. Just extend it to get the control of POJO binding by yourself (instead of rely to ODatabaseDocument impl)
    - Implemented Map binding