commit bd87e94100fdd68f8c6d5ddb9efdc3db5b557a52
Author: Roozbeh Pournader <roozbeh@google.com>
Date:   Fri Aug 7 16:27:28 2015 -0700

    Fix and deprecate TextUtils.join(Iterable<CharSequence>).

    There seems to be only one caller of this signature, and even that is
    in a deprecated method, so it should be safe to deprecate this.

    The new implementation uses ICU for the format string and so removes
    the need for a localized string, although it's still a hack and is
    probably slower. It's added here since we may actually want to clean
    it up later and use it for an improved simple list formatter.

    Bug: 6823087
    Change-Id: I2aa1af9b170e41da840d9d7bc663df06fb96d136