commit 9c7a5125e371096f596d4bb56b598a72c1cba28d
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Sun Nov 21 12:21:16 2010 +0000

    o Added IndexHits#currentScore which exposes the most recently fetched
      IndexHits item's score from the search, if any. The lucene-index supports this.
    o Moved the "index" shell app to the shell component where the other kernel apps are,
      it was misplaced in lucene-index.
    o Added QueryContext#topDocs(int) so that you can specify to retrieve only the top N
      hits from a search, which may improve performance - even if when retrieving all hits
      the iteration all the way down to lucene is done lazily.
    o Performance improvements regarding the iterator stack which makes up the return IndexHits
      instances from query/get methods. Previously there could be 5-7 nested iterators.
      Now there are only 3-4.
    o Added configuration parameter "similarity" which controls the Lucene
      IndexWriter#setSimilarity(Similarity) for an index. It points out class names.


    git-svn-id: https://svn.neo4j.org/components/kernel/trunk@7126 0b971d98-bb2f-0410-8247-b05b2b5feb2a