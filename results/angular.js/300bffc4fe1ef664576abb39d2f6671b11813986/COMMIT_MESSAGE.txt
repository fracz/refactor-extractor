commit 300bffc4fe1ef664576abb39d2f6671b11813986
Author: Rouven Weßling <me@rouvenwessling.de>
Date:   Fri Aug 29 21:22:43 2014 +0200

    refactor(indexOf) use Array.prototype.indexOf exclusively

    Replace helper functions with the native ES5 method

    Closes #8847