commit 3c76a34338c964e25530178b92cded7495fcb487
Author: Anton Persson <anton.persson@neotechnology.com>
Date:   Thu Jun 11 10:28:58 2015 +0200

    Performance enhancement with DijkstraBidirectional

    ... and fixed ordinary Dijkstra to behave correctly.

    Changes:

    * Dijkstra had problems with graph that used floating point and/or zero weights.
      - Solved by introducing a tolerance level, "epsilon".
      - Needed to use STATE for this, therefore removed option for user to use STATE with Dijkstra. (This option is still available through deprecated methods in GraphAlgoFactory and Dijkstra constructors, although new and improved execution will be lost when using this.)
      - Added seperate test for Dijkstra when using STATE.

    * Dijkstra didn't terminate as soon as it could.
      - Solved by introducting DijkstraEvaluator and DijkstraPathExpander. This is where STATE is occupied and therefor can not be used by user.

    * Added DijkstraBidirectional.
      - Exposed through GraphAlgoFactory
      - Moved Mutable* from ShortestPath to util as they are needed for DijkstraBidirectional

    * Added k shortest paths functionality
      - This should replace the previous option of finding ALL paths which was used to find an arbitrary number of paths in increading order (seen to weight).
      - For this Dijkstra (NOT DijkstraBidirectional) is used.
      - Functionality exposed through GraphAlgoFactory

    * Exchanged nested class PathInterest in BestFirstSelectorFactory
      - Now using an interface PathInterest that also provides a comparator and more options on how to determine interest of a path.

    * Added Precision class for comparing doubles with range tolerance.
      - Instead of depending on apache.commons.math3

    * Added ascii art for Dijkstra tests.
      - Very pedagogical

    * Fixed TraversalAStar
      - Changes in BestFirstSelectorFactory changed behaviour of TraversalAStar.

    * Combined Dijkstra tests into one class.

    What options are available on what paths to find?:
    * Dijkstra (behaviour is controlled with PathInterest)
      - All, use `PathInterestFactory.all( epsilon )` (This option should not be available as it is by far to inefficient. It is left for to not break current behavior.)
      - AllShortest, (default) use `PathInterestFactory.allShortest( epsilon )`
      - KShortest, use `PathInterestFactory.numberOfShortest( epsilon, numberOfPathsToFind )`
      - Single, just call `.findSinglePath(...)`

    * DijkstraBidirectional
      - AllShortest, call `.findAllPaths(...)`
      - Single, call `.findSinglePath(...)`

    Comments:
    Dijkstra now works correctly in all cases but should only be used to find a specified number of shortest paths, that is the top k shortest paths. When searching for AllShortest or Single DijkstraBidirectional should be used instead.

    Edit after review comments:
    * Removed constants from Mutable*. Added links to deprecated methods.
    * Made Dijkstra test parameterized. Test for fetching paths with increasing weight in seperate test class.
    * Added ZERO InitialBranchState as static constant.
    * Changed names from Precision to NoneStrictMath
    * Added test for TopFetchingWeightedPathIterator. Added possibility to create Path in SimpleGraphBuilder and assert on path in Neo4jAlgoTestCase
    * Added test for BestFirstSelectorFactory