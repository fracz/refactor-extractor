commit e3e8813e3c586093c79cffe2b17418c0c1797d4a
Author: JP Sugarbroad <jpsugar@google.com>
Date:   Wed Jun 13 13:37:19 2012 -0700

    refactor($injector): move $injector into the providerCache

    Better than special-casing '$injector' in createInjector.