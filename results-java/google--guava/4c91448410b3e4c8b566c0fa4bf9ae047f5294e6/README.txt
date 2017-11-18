commit 4c91448410b3e4c8b566c0fa4bf9ae047f5294e6
Author: Chris Povirk <cpovirk@google.com>
Date:   Fri Nov 22 11:48:25 2013 -0500

    Some documentation improvements for Ascii.truncate.

    Also remove the overload that used "..." as a default truncation indicator because:
    - Some people might assume that it used no truncation indicator ("")
    - It's easier to understand when reading if the indicator is passed explicitly
    - No need to warn people about "..." not being what they want to use for some locales
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=57067405