commit 138fbf0d6c609c7d926b9dff10472d68efab8457
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Sun Oct 9 23:56:42 2016 +0200

    refactor(jqLite): wrap the jqueryVersion binding in a span

    Protractor's by.binding selector selects the whole element in which the binding
    is contained as otherwise it can't know which bit of text has been interpolated.

    It's safer to wrap the binding in a span so that we're sure what the e2e tests
    are exactly testing.