commit 9878fee2992a845dff1e3e083f487013ebde8064
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Fri Feb 14 11:44:41 2014 +0000

    test(docs-app-e2e): refactor test inline with new docs app

    The links to code elements have now changed: api/ng.directive:ngClick ->
    api/ng/directive/ngClick.

    Examples now run inside iframes, so we need to instruct Protractor to
    switch to the example iframe.