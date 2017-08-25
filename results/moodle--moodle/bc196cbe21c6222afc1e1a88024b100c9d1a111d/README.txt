commit bc196cbe21c6222afc1e1a88024b100c9d1a111d
Author: Petr Skoda <commits@skodak.org>
Date:   Sun Nov 6 19:58:16 2011 +0100

    MDL-30151 finally remove mod/forum:initialsubscriptions and improve perf

    The 'mod/forum:initialsubscriptions' was introduced in 1.7 as a workaround for missing enrolment info - the mod_form forum help was not even updated to mention this capability. Now that we have reliable enrolment info we can significantly improve both enrol and unenrol performance. Hopefully majority of users will not miss this capability...