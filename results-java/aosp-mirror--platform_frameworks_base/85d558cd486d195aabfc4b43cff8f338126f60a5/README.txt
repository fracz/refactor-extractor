commit 85d558cd486d195aabfc4b43cff8f338126f60a5
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Nov 4 10:31:54 2014 -0800

    Add Activity API to get referrer information.

    This expands the use of EXTRA_REFERRER to be relevant anywhere,
    allowing apps to supply referrer information if they want.  However,
    if they don't explicitly supply it, then the platform now keeps
    track of package names that go with Intents when delivering them
    to apps, which it can be returned as the default value.

    The new method Activity.getReferrer() is used to retrieve this
    referrer information.  It knows about EXTRA_REFERRER, it can return
    the default package name tracked internally, and it also can return
    a new EXTRA_REFERRER_NAME if that exists.  The latter is needed
    because we can't use EXTRA_REFERRER in some cases since it is a Uri,
    and things like #Intent; URI extras can only generate primitive type
    extras.  We really need to support this syntax for referrers, so we
    need to have this additional extra field as an option.

    When a referrer is to a native app, we are adopting the android-app
    scheme.  Since we are doing this, Intent's URI creation and parsing
    now supports this scheme, and we improve its syntax to be able to build
    intents with custom actions and stuff, instead of being all hung up
    on custom schemes.

    While doing this, fixed a problem when parsing both intent: and new
    android-app: schemes with a selector portion, where we were not
    respecting any scheme that was specified.

    Change-Id: I06e55221e21a8156c1d6ac755a254fea386917a2