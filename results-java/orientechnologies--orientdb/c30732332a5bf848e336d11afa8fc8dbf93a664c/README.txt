commit c30732332a5bf848e336d11afa8fc8dbf93a664c
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri Dec 9 19:46:51 2011 +0000

    Huge refactoring to remove database field in many classes: now the current database is always retrieved from thread local.