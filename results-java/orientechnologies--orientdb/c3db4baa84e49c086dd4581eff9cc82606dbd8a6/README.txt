commit c3db4baa84e49c086dd4581eff9cc82606dbd8a6
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Wed Nov 7 22:32:35 2012 +0000

    SQL:
    - new last() function to retrieve the last element of the resultset or a multi value (collection, map, array)
    - improved first() to support the aggregate result to return the first item of the result set
    - improved speed on retrieving of the first element in case of List avoiding the iterator