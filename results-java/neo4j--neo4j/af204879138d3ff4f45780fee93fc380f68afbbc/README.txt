commit af204879138d3ff4f45780fee93fc380f68afbbc
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Thu Dec 18 16:58:10 2014 +0100

    Fix forceClose() and refactor around closing in KernelStatement.

    Using the forceClose() method did nothing and resources could leak.
    Added a test for it.