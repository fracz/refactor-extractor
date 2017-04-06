commit ea750de39f2f1ae312515d0770cb6487787a72db
Author: javanna <cavannaluca@gmail.com>
Date:   Fri Oct 23 18:53:02 2015 +0200

    Explain api: move query parsing to the coordinating node

    Similarly to what we did with the search api, we can now also move query parsing on the coordinating node for the explain api. Given that the explain api is a single shard operation (compared to search which is instead a broadcast operation), this doesn't change a lot in how the api works internally. The main benefit is that we can simplify the java api by requiring a structured query object to be provided rather than a bytes array that will get parsed on the data node. Previously if you specified a QueryBuilder it would be serialized in json format and would get reparsed on the data node, while now it doesn't go through parsing anymore (as expected), given that after the query-refactoring we are able to properly stream queries natively.

    Closes #14270