commit 0936680fa237484bb94d559a17776fc37b8a53bc
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Wed Dec 15 21:44:42 2010 +0000

    Merged the fast-writes branch. Basically it makes committing changes to lucene indexes
    faster and more throughput-friendly for multiple threads. The performance improvement
    for committing lucene transactions shows best for small transactions, but will improve
    all modifying operations to lucene indexes.


    git-svn-id: https://svn.neo4j.org/components/lucene-index/trunk/src@7777 0b971d98-bb2f-0410-8247-b05b2b5feb2a