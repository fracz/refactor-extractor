commit 1998619b0f2d1acfd905aac5df5707d2ae32e9e1
Author: Johan Svensson <johan@neotechnology.com>
Date:   Fri Apr 24 15:59:16 2009 +0000

    Changed implementation of adding/removing/getting relationships on a node in cache
    layer. Previous implementation worked with sets but that had a performance problem
    when doing small modifications to a node (create adding or removing a few
    relationships). This could be noticed when node had many relationships (copy on
    write on largers sets was just to slow).

    Replaced with an array implementation using array copy for add and set combined
    with array swap for remove. Also improved getRelationship performance by writing
    a smarter iterator.

    Tried a sorted set + binary search implementation but merge sort is to slow on
    larger arrays.



    git-svn-id: https://svn.neo4j.org/components/neo/trunk@2778 0b971d98-bb2f-0410-8247-b05b2b5feb2a