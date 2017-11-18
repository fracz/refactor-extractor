commit 77e7a79f8d8a29fb4ca9283edb43d4a5f7c7f114
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Apr 22 21:21:10 2015 +0300

    PY-12360 Several improvements in pep8 external annotator

    * Various E3xx errors about blank lines are reported on corresponding
    PSI whitespaces, not at the following elements
    * Do not report missing blank line at the end of file if
    "Settings | Editor | Other | Ensure line feed on Save" is enabled
    * It that option was not enabled report W292 error on the last element
    in file
    * Suppress pep8 errors about redundant blanks if they conflict with
    preferences in Python's code style settings