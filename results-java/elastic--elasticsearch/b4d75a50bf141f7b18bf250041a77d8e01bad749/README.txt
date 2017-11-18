commit b4d75a50bf141f7b18bf250041a77d8e01bad749
Author: Shay Banon <kimchy@gmail.com>
Date:   Sat May 25 22:43:48 2013 +0200

    Dates accessed from scripts should use UTC timezone
    this was broken in the field data refactoring we did in 0.90, fixes #3091