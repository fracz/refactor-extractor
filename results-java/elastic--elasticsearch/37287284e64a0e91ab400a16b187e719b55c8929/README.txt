commit 37287284e64a0e91ab400a16b187e719b55c8929
Author: Ryan Ernst <ryan@iernst.net>
Date:   Wed Dec 10 16:28:19 2014 -0800

    Settings: Remove `mapping.date.round_ceil` setting for date math parsing

    The setting `mapping.date.round_ceil` (and the undocumented setting
    `index.mapping.date.parse_upper_inclusive`) affect how date ranges using
    `lte` are parsed.  In #8556 the semantics of date rounding were
    solidified, eliminating the need to have different parsing functions
    whether the date is inclusive or exclusive.

    This change removes these legacy settings and improves the tests
    for the date math parser (now at 100% coverage!). It also removes the
    unnecessary function `DateMathParser.parseTimeZone` for which
    the existing `DateTimeZone.forID` handles all use cases.

    Any user previously using these settings can refer to the changed
    semantics and change their query accordingly. This is a breaking change
    because even dates without datemath previously used the different
    parsing functions depending on context.

    closes #8598
    closes #8889