commit ae633b2d36172862df3cd5ab240882bdff5a2a6c
Author: Andy McFadden <fadden@android.com>
Date:   Tue Jun 14 12:43:49 2011 -0700

    Port EventRecurrence.parse() from native

    This adds a Java-language implementation of EventRecurrence.parse(),
    to make it easier to relocate it for the benefit of unbundled
    Calendar.

    Differences from the native version:

     - enforces that FREQ appears first
     - allows (but ignores) X-* parts
     - improved validation on various values
     - error messages are more specific
     - enforces that only one of UNTIL and COUNT may be present [disabled]
     - allows lower-case property and enumeration values [disabled]

    As part of the transition process, both versions of the parser are
    called on every request, and the results are compared.  If the results
    are different a warning message is logged.

    An unnecessary constructor was removed.

    This also this moves some EventRecurrence tests out of CalendarProvider,
    into coretests, and adds a simple parse test with the examples from
    the RFC.

    Bug 4575374

    Change-Id: If737ed1272fda65c93363d87b2da12b85e644f5b