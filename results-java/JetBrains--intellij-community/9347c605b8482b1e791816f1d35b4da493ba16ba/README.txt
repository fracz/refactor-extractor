commit 9347c605b8482b1e791816f1d35b4da493ba16ba
Author: Daniil Ovchinnikov <daniil.ovchinnikov@jetbrains.com>
Date:   Fri Apr 21 16:39:23 2017 +0300

    [groovy] refactor index access (GrIndexProperty)

    The element may appear in rhs as well as in lhs. There is special
    case when the same element is both a rValue and a lValue: a[0] += b.
    Because of that we need ability to separately resolve getAt() and
    putAt() methods. Thus the element cannot be a GrCall.

    Added:
    - getLValueReference() and getRValueReference();
    - getType() always returns type as if element is a rValue.
    Removed:
    - getGetterType() as it is became unneeded;
    - getSetterType() as it was unused already.