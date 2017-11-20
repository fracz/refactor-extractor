commit 481dba79f084f18a050342b6cc0ec597eea9d1d3
Author: Alex Eagle <alexeagle@google.com>
Date:   Mon Aug 12 01:11:10 2013 -0700

    More refactoring based on what Eddie started. Highlights:
    - Almost no checks implement their own Scanner
    - DescribingMatcher class deleted
    - Checks can implement more than one kind of matcher
    - Use a sentinel value rather than null for "no match"
    - Still construct efficient lists for executing checks against tree nodes based on the type of node
    All tests passing, and this feels nearly ready to merge to trunk. Big question is the proper resolution of the diverged SelfAssignment and SelfAssignmentChecker.