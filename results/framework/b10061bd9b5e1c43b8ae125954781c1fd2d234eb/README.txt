commit b10061bd9b5e1c43b8ae125954781c1fd2d234eb
Author: Colin DeCarlo <colin@thedecarlos.ca>
Date:   Mon Feb 17 09:30:56 2014 -0500

    Squashed commits of general refactoring to the segments method.

    * Refactor segments method so that it is consitent with the expected values of the segment method
    * DRY out the segment method so that it uses the segments method
    * Add braces to control structures with a single line
    * PSR 2 requires that:

      > The body of each structure MUST be enclosed by braces

      Throw caution to the wind and remove braces from control structures containg a single line

    * Revert "Add braces to control structures with a single line"

      This reverts commit 8e56540a4301ede193d3743313487d9f77b457a0.