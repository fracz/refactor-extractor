commit 7fbbed34cd584028bc22ff73756b4bad647687df
Author: Adam Murdoch <adam@gradle.com>
Date:   Tue Oct 4 12:08:59 2016 +1100

    Fixed incorrect classpath calculation when a tooling API client uses multiple different `BuildAction` implementations loaded from the same client side `ClassLoader`.

    "Fixed" in this change really means "worked around". This will be improved in subsequent changes.