commit b56bbf62dd88225d2c396fc023e2bb44cc8698fc
Author: javanna <cavannaluca@gmail.com>
Date:   Wed Oct 28 19:10:19 2015 +0100

    Validate query api: move query parsing on the coordinating node

     Similarly to what we did with the search api, we can now also move query parsing on the coordinating node for the validate query api. Given that the explain api is a single shard operation (compared to search which is instead a broadcast operation), this doesn't change a lot in how the api works internally. The main benefit is that we can simplify the java api by requiring a structured query object to be provided rather than a bytes array that will get parsed on the data node. Previously if you specified a QueryBuilder it would be serialized in json format and would get reparsed on the data node, while now it doesn't go through parsing anymore (as expected), given that after the query-refactoring we are able to properly stream queries natively. Note that the WrapperQueryBuilder can be used from the java api to provide a query as a string, in that case the actual parsing of the inner query will happen on the data node.

    Relates to #10217
    Closes #14384