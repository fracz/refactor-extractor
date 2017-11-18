commit 25714283bcb2ae0a03416c07dd3ace9a7f02d004
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Sun May 16 18:04:54 2010 +0000

    Made some refactorings (which should, if anything, make things a little faster):

    o ExpansionSourceImpl also implements Position so that no Position objects
      need to be instantiated. (Also removed PositionImpl class).
    o Made TraverserImpl stateless and instantiates filters and source selectors,
      a.s.o. in the TraverserIterator instead. This is how it should've been
      to begin with.
    o Moved out code which was only executed for the first expansion source into
      StartNodeExpansionSource so that some if-statements in both constructor and
      next() method could be removed in favor of polymorphism.


    git-svn-id: https://svn.neo4j.org/components/kernel/trunk@4416 0b971d98-bb2f-0410-8247-b05b2b5feb2a