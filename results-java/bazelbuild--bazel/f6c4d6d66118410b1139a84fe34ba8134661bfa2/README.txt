commit f6c4d6d66118410b1139a84fe34ba8134661bfa2
Author: corysmith <corysmith@google.com>
Date:   Tue May 23 17:19:14 2017 +0200

    Add a new action for generating reconciled R classes for Robolectric.

    This includes some refactoring:
    * Move the symbol deserialization our of the merger and into the ParsedAndroidData (probably move again.)
    * Change the FailedFutureAggregator generics to work more callables

    RELNOTES: None
    PiperOrigin-RevId: 156863698