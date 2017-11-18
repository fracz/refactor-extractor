commit 8485215efdf5dceb84ceffb51b0d1bde2c55946e
Author: Andreas Kollegger <andreas.kollegger@neotechnology.com>
Date:   Wed Sep 15 09:21:18 2010 +0000

    refactored Hits into the org.neo4j.index.impl.lucene package namespace, to ensure it is included and avoid conflicts with lucene itself (which had dropped Hits)

    git-svn-id: https://svn.neo4j.org/laboratory/components/lucene-index/trunk/src@5659 0b971d98-bb2f-0410-8247-b05b2b5feb2a