commit 8534b7c7c0aa352eb0f17dbe8b22ba34f995654d
Author: Di Peng <pengdi@google.com>
Date:   Fri Jul 29 13:50:22 2011 -0700

    refactor(date,curreny,number): inject and use $locale in filters

    - filter.number, filter.currency and filter.date are injected with
    $locale service so that we can just swap the service to localize these
    - date filter was beefed up in order to support literal strings found in
      localization rules