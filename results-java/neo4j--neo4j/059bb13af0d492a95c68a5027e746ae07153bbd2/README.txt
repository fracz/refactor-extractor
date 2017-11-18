commit 059bb13af0d492a95c68a5027e746ae07153bbd2
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Wed Oct 9 13:44:07 2013 -0700

    Performance optimizations for best-first selector

    Mostly revolving around finding single paths. This gives a good
    improvement when calling PathFinder#findSinglePath for those graph
    algorithms using the traversal framework and more specifically the
    BestFirstSelector. Currently this will benefit Dijkstra and A*, although
    the default implementation of A* is a custom non-traversal-framework
    version that only supports single paths anyways so already has got a
    similar performance behaviour. Look at TraversalAStar for a A* version
    that uses the traversal framework.