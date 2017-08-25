commit 4adc9c4f60f495447e4b6eae76cc2392896d2929
Author: skodak <skodak>
Date:   Sat Dec 2 19:05:24 2006 +0000

    MDL-7768 forgot_password.php copnverted to use new forms:
    * new forms
    * minor refactoring and code cleanup
    * secret is now cleared to prevent repeated use of change confirmation link, added new error message see MDL-7755 (patch by Andrew Walbran)