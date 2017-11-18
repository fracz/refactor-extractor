commit e5e3e9193a457ace9043bf70ff0253333c3435e5
Author: Florian Weikert <fwe@google.com>
Date:   Tue Mar 8 03:08:26 2016 +0000

    Skylark: improved documentation and error messages of getattr() and hasattr() when being called with the name of an existing method.

    While hasattr(obj, 'existing method') continues to return true, getattr(obj, 'existing method') always throws an exception (with a more detailed message than before), regardless of whether a default value was specified or not.

    --
    MOS_MIGRATED_REVID=116613716