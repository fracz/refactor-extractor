commit c6abf5bff6bbfafa1f133644f02a5d50d5269b7f
Author: Raph Levien <raph@google.com>
Date:   Thu Apr 23 16:12:23 2015 -0700

    Expose drawTextRun publicly

    For correct low-level drawing of low level text, a method that
    includes context for shaping is necessary, and it's similarly useful
    to provide the direction explicitly rather than running the BiDi
    algorithm on the text. The drawTextRun method (in both char[] and
    CharSequence variants) has provided this functionality for several
    major releases but has been internal. This patch exposes the
    methods publicly, and also improves the doc strings for both
    the new method and some related ones.

    Bug: 20193553
    Change-Id: I9be33ca5ae3e7db2b69a56298400671d5ef8ad05