commit 470ad1ae4a3fdcc85643f1df086e6fdc76189f68
Author: Ryan Ernst <ryan@iernst.net>
Date:   Tue Feb 7 09:34:41 2017 -0800

    Settings: Add secure settings validation on startup (#22894)

    Secure settings from the elasticsearch keystore were not yet validated.
    This changed improves support in Settings so that secure settings more
    seamlessly blend in with normal settings, allowing the existing settings
    validation to work. Note that the setting names are still not validated
    (yet) when using the elasticsearc-keystore tool.