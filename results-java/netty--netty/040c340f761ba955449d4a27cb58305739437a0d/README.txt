commit 040c340f761ba955449d4a27cb58305739437a0d
Author: Trustin Lee <t@motd.kr>
Date:   Sat Nov 22 07:36:09 2014 +0900

    Add back IntObjectMap.values(Class<V>)

    Motivation:

    Although the new IntObjectMap.values() that returns Collection is
    useful, the removed values(Class<V>) that returns an array is also
    useful. It's also good for backward compatibility.

    Modifications:

    - Add IntObjectMap.values(Class<V>) back
    - Miscellaneous improvements
      - Cache the collection returned by IntObjectHashMap.values()
      - Inspector warnings
    - Update the IntObjectHashMapTest to test both values()

    Result:

    - Backward compatibility
    - Potential performance improvement of values()