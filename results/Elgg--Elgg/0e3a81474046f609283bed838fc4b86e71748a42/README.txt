commit 0e3a81474046f609283bed838fc4b86e71748a42
Author: Steve Clay <steve@mrclay.org>
Date:   Tue May 19 23:51:27 2015 -0400

    chore(core): Use PHP5.5 ::class syntax to remove string classnames

    Strings are bad for static analysis and make refactoring more dangerous.