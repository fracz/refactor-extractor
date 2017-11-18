commit 44e440cc7e834de7811f005998acb32716835b00
Author: Neil Fuller <nfuller@google.com>
Date:   Mon Apr 20 14:39:00 2015 +0100

    Add checks for types in Parcel / avoid class initialization

    Make Parcel more stringent to avoid initializing classes
    that are not related to Parcelable.

    Two new checks:
    1) That the class found on the stream implements Parcelable.
    2) That the type of the CREATOR field declared on the class
    found on the stream actually implements Parcelable.Creator.

    For (1) the new check that a class in the stream is actually
    Parcelable. This will affect handling of invalid streams or
    code that didn't obey the requirements.

    For (2) this change could break some apps that had a CREATOR
    field in a Parcelable class that was not declared to be
    (at least) a Parcelable.Creator: it is no longer sufficient
    for the type to implement Parcelable.Creator, the field
    must be declared as such.

    This change includes doc updates for Parcelable to make
    the requirement around the declared type of the CREATOR
    field more concrete.

    This change also makes the generics slightly tidier/explicit,
    annotates code as unchecked where needed and removes some
    assumptions that can not be guaranteed with Java's type
    system and the current definitions.

    For example, there's no guarantee right now that
    Parcelable.Creator returns objects that are actually
    Parcelable, or that the CREATOR object associated
    with a Parcelable will return objects of the surrounding
    class. The first we can't do something about without
    breaking the public API (due to implementations like
    TextUtils.CHAR_SEQUENCE_CREATOR). The second is
    currently typically implicitly enforced with an implicit
    cast in the (app's) calling code (e.g. callers to
    readParcelable() that causes a language-introduced cast
    to the type expected). A larger refactoring of Parcel
    would be required to ensure that the class that is
    produced by Creator is of a type compatible with the
    class that declared CREATOR, and is not a goal for this
    change.

    A fix is included for a class that doesn't implement
    Parcelable like it should and would probably fail
    check (1).

    Bug: 1171613
    Change-Id: I31d07516efee29a320e80f4bc4f96aaac628f81c