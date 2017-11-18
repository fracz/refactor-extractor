commit 544be17c3d3fed6587766a09dfd4c331b5101d2b
Author: Pavel V. Talanov <Pavel.Talanov@jetbrains.com>
Date:   Tue Aug 28 20:26:26 2012 +0400

    Refactor JetTypeMapper#getAccessFlags.

    Rename to getVisibilityAccessFlag.
    Separate logic for special cases and default mapping.
    Use meaningful name instead of "magic" zero number.
    Add TODO for future refactoring.
    Various logic clarifications.