commit 6ea95c17f38368c21d3f47e65cabef89b33a3477
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Wed Jun 22 11:05:09 2016 +0200

    refactor: Moved remaining XML result generation to class

    The code was basically just moved, not yet refactored,
    so XML generation is finally testable.

    The generated XML is still *exactly* the same.