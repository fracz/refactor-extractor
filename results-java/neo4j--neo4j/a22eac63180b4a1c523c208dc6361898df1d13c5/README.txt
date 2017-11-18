commit a22eac63180b4a1c523c208dc6361898df1d13c5
Author: Anton Persson <anton.persson@neotechnology.com>
Date:   Tue May 5 10:13:43 2015 +0200

    ShortestPath now interrupts traversal if nodes belongs to disconnected graphs

    ShortestPath is bidirectional.
    If one sides sees that other side is exhausted, that is, does not have any more nodes to traverse, it continues to search for hits in the current layer of nodes. Then it stops.
    This improves efficiency alot when searching for shortest path between disconnected graphs where difference in graph size is large.

    Moved TestShortestPath to matching package and changed visibility of MutableInteger private -> protected for usage in test.

    Correct formatting, imports and other PR comments