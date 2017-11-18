commit 6132946ceda57a801d60cb1a5bee281113443b86
Author: Pavel V. Talanov <Pavel.Talanov@jetbrains.com>
Date:   Mon Aug 27 14:21:53 2012 +0400

    Introduce ClassKind#CLASS_OBJECT and ClassKind#isObject and usages.

    Rename LazyClassDescriptor#onlyEnumEntries -> enumClassObjectInfo.
    Determine ClassKind in JetClassInfo and JetObjectInfo constructor.
    Remove complex constructor for enum entry in enum test.
    Logic simplified in LazyClassMemberScope.
    Minor refactorings.