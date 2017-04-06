commit 9eaed3589261de9a317741121eeb7ea53a3b213a
Merge: 96c4486 d9bb4ff
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 30 22:05:53 2013 +0100

    feature #9857 Form Debugger JavaScript improvements (WouterJ)

    This PR was submitted for the 2.4 branch but it was merged into the 2.5-dev branch instead (closes #9857).

    Discussion
    ----------

    Form Debugger JavaScript improvements

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | -
    | Deprecations? | -
    | Tests pass?   | -
    | Fixed tickets | #9123
    | License       | MIT
    | Doc PR        | -

    Commits
    -------

    406756d Reverted Sfjs.toggle change
    226ac7c Reverted new image
    53f164f Fixed asset function
    c763d65 Merge pull request #1 from bschussek/issue9857
    5afbaeb [WebProfilerBundle] Enlarged the clickable area of the toggle button in the form tree
    58559d3 [WebProfilerBundle] Moved toggle icon behind the headlines in the form debugger
    a0030b8 [WebProfilerBundle] Changed toggle color back to blue and made headlines in the form debugger clickable
    505c5be [WebProfilerBundle] Added "use strict" statements
    ebf13ed [WebProfilerBundle] Inverted toggler images and improved button coloring
    07994d5 [WebProfilerBundle] Improved JavaScript of the form debugger
    11bfda6 [WebProfilerBundle] Vertically centered the icons in the form tree
    52b1620 Fixed CS
    f21dab2 Added error badge
    111a404 Made sections collapsable
    60b0764 Improved form tree
    0e03189 Expand tree