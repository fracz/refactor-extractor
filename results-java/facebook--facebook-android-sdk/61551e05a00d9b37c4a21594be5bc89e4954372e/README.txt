commit 61551e05a00d9b37c4a21594be5bc89e4954372e
Author: Kamil Kraszewski <kamil@mutalisk-3.local>
Date:   Wed Nov 30 15:51:25 2011 -0800

    Refactoring of Android's Hackbook example

    Summary:
    Cleaning of the Hackbook code. Main reason of this commit is mixing tabs and
    white spaces inside the code, which makes the code ugly (for example browsing
    the code inside github).

    In addition I also refactored few other things:
     - I tried to wrap the lines to 100 characters per line (80 per comments) - at
    least in those places where it made sense
     - Remove trailing whitespaces and unnecessary blank lines
     - Add missing @Override adnnotations
     - Fixed syntax in some places (like "for(i=0;..." -> "for (i = 0;...")
     - Added missing 'static' keywords

    Test Plan:
    Run the app and see if everything works :-)

    Reviewers: jimbru, raghuc1, vksgupta, dalves

    Reviewed By: dalves

    CC: platform-diffs@lists, nbushak, dalves

    Differential Revision: 370079