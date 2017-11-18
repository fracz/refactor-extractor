commit d8974c1b16762228a734913be1b91865897ca2d1
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Wed Apr 20 16:15:33 2011 +0400

    Perforce refactoring.
    Definitely corrected bug with .idea files "not under Perforce"
    Targeted also to fix problems with multiple Perforce server connections
    and user's misprints when defining "client" parameter

    Accurately parse P4CONFIG files, try to find one for each separate module that's under Perforce.
    Report if connection/authorization/configuration problems
    Show loaded P4CONFIG files and their parameters (also in Test connection results)
    Try to reduce number of p4 info and p4 client queries