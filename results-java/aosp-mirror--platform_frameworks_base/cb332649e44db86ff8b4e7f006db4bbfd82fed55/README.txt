commit cb332649e44db86ff8b4e7f006db4bbfd82fed55
Author: Fabrice Di Meglio <fdimeglio@google.com>
Date:   Fri Sep 23 19:08:04 2011 -0700

    Fix bug #5366547 TruncateAt.MARQUEE should be replaces with "two dot" ellipsis on hardware that dont support MARQUEE

    - introduce TruncateAt.END_SMALL
    - code refactoring for suppressing use of hardcoded constants

    Change-Id: I70e24857cd5d6bd012a743cbc0ba46fbd06d5457