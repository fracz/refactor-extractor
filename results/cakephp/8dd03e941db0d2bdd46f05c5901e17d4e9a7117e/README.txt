commit 8dd03e941db0d2bdd46f05c5901e17d4e9a7117e
Author: mark_story <mark@mark-story.com>
Date:   Wed Aug 7 23:49:57 2013 -0400

    Extract BufferedResultSet.

    During discussion on github, it became apparent that buffering results
    by default is not a good idea. Split the buffering out into a subclass
    that is optionally enabled using Query::bufferResults().

    Do some refactoring in ResultSet to make fewer method calls and less
    code overall.