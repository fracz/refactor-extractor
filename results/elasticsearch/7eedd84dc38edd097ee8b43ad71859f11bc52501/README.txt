commit 7eedd84dc38edd097ee8b43ad71859f11bc52501
Author: javanna <cavannaluca@gmail.com>
Date:   Fri Sep 11 09:55:02 2015 +0200

    Query refactoring: refactor query_string query and score functions

    Refactor the function_score query so it can be parsed on the coordinating node, split parse into fromXContent and toQuery, make FunctionScoreQueryBuilder Writeable.

    Closes #13653