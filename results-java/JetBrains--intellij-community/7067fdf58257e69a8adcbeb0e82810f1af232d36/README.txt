commit 7067fdf58257e69a8adcbeb0e82810f1af232d36
Author: Oleg Sukhodolsky <oleg.sukhodolsky@jetbrains.com>
Date:   Fri Feb 15 15:32:40 2013 +0400

    RUBY-13086: performance improvement
    We have to add all text we've got at once to avoid overhead Document provides for every insertion (it fires document events and update UI)