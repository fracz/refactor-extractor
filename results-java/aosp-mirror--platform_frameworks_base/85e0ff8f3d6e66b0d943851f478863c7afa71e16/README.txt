commit 85e0ff8f3d6e66b0d943851f478863c7afa71e16
Author: David Brown <dab@google.com>
Date:   Fri Oct 22 12:54:42 2010 -0700

    Fix bug 3121292: Contact photo not shown correctly for SIP calls

    The problem was that when we did a contact lookup based on a SIP address,
    the resulting CallerInfo object did not have the person_id field set
    correctly.  That meant we had no way to look up the photo for that person.

    This was because of a missing case in the logic to determine which column
    (in the resulting cursor) to use for the person_id lookup.  We were
    handling lookups fine in the PhoneLookup and Phone tables, but were
    missing a case for direct lookups in the Data table (which is how we look
    up SIP addresses.)

    The fix is to add a case for URIs like
    "content://com.android.contacts/data" when looking up the person_id.

    Also, since the person_id lookup is pretty hairy (and includes ~20 lines
    of comments to explain what it's doing!) refactor it out into a helper
    method.

    TESTED: Both SIP and PSTN calls; verified that contact name *and* photo
    are displayed correctly in all cases.

    Bug: 3121292
    Change-Id: I2b0083cc5394c1a49bbdc9a4e5651854aedb82f7