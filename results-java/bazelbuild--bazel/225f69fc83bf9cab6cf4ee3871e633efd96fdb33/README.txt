commit 225f69fc83bf9cab6cf4ee3871e633efd96fdb33
Author: Irina Iancu <elenairina@google.com>
Date:   Thu Sep 8 12:18:35 2016 +0000

    Check in Dagger generated code to junitrunner.

    This is part of an initiative to remove Dagger from junitrunner. Because
    there are too many interdependent components that Dagger touches, its usage
    cannot be easily removed in small independent changes. Therefore the first step
    is to check in the code that Dagger generates (with slighty small changes, e.g.
    classes were renamed, some Dagger specific data types were changed to junitrunner internal types).
    The following steps include removing all the Dagger annotations and BUILD
    dependencies, and refactoring the generated code.

    --
    MOS_MIGRATED_REVID=132543906