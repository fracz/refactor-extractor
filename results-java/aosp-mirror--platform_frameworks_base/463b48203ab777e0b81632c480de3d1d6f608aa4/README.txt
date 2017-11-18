commit 463b48203ab777e0b81632c480de3d1d6f608aa4
Author: Roozbeh Pournader <roozbeh@google.com>
Date:   Thu Aug 6 16:04:45 2015 -0700

    Support all RTL scripts in getLayoutDirectionFromLocale().

    Previously, only the languages written in Arabic and Hebrew were
    considered right-to-left. Now, ICU is used to return the direction
    of the language, which not only support other right-to-left scripts
    (such as Thaana), but also has better logic to determine the
    direction of the locale and uses caching to improve the speed.

    Bug: 22559274
    Change-Id: I760be7984a9b35ea77d59ca84a220798e205af36