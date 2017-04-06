commit 8ab7ca5284581d59b3988024894a934504371092
Author: Adrien Grand <jpountz@gmail.com>
Date:   Mon Oct 10 09:32:26 2016 +0200

    Source filtering should treat dots in field names as sub objects. (#20736)

    Mappings treat dots in field names as sub objects, for instance

    ```
    {
      "a.b": "c"
    }
    ```

    generates the same dynamic mappings as

    ```
    {
      "a": {
        "b": "c"
      }
    }
    ```

    Source filtering should be consistent with this behaviour so that an include
    list containing `a` should include fields whose name is `a.b`.

    To make this change easier, source filtering was refactored to use automata.
    The ability to treat dots in field names as sub objects is provided by the
    `makeMatchDotsInFieldNames` method of `XContentMapValues`.

    Closes #20719