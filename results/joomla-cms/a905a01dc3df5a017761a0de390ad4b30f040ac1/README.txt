commit a905a01dc3df5a017761a0de390ad4b30f040ac1
Author: jo-sf <jo-sf@users.noreply.github.com>
Date:   Sat Sep 3 13:15:32 2016 +0100

    URL checking improved in com_wrapper. Fixes #4670

        When adding the scheme to an URL use correct scheme (http or https) and port.
        Checking for "http" or "https" via strstr() in the URL matched also these strings embedded in the URL (e.g. in /static/http/index.html), due to that checks changed to strpos().