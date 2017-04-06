commit 2d6adf64286152470cfdfe669171c1a5fe0f6f37
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Wed Jan 6 16:08:10 2016 +0100

    Percolator refactoring:
    * Added percolator field mapper that extracts the query terms and indexes these terms with the percolator query.
    * At percolate time these extracted terms are used to query percolator queries that are like to be evaluated. This can significantly cut down the time it takes to percolate. Whereas before all percolator queries were evaluated if they matches with the document being percolated.
    * Changes made to percolator queries are no longer immediately visible, a refresh needs to happen before the changes are visible.
    * By default the percolate api only returns upto 10 matches instead of returning all matching percolator queries.
    * Made percolate more modular, so that it is easier to add unit tests.
    * Added unit tests for the percolator.

    Closes #12664
    Closes #13646