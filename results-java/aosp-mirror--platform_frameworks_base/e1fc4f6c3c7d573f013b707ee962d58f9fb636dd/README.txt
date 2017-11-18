commit e1fc4f6c3c7d573f013b707ee962d58f9fb636dd
Author: Gilles Debunne <debunne@google.com>
Date:   Mon Oct 3 17:01:19 2011 -0700

    Optimisations and bugs in SpellChecker

    A bug was introduced in a recent refactoring: correct words didn't have their
    SpellCheckSpan removed, leaving a lot of useless spans.

    SPAN_EXCLUSIVE_EXCLUSIVE should never have a 0-length. With Japanese characters
    wordStart could be equal to wordEnd when parsing the text: skip these.

    Using toString().substring(...) instead of subSequence(...).toString() which
    is more efficient.

    Change-Id: I670870a34565939b676400091f4852152a7f7124