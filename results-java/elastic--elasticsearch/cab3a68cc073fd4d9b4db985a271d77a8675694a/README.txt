commit cab3a68cc073fd4d9b4db985a271d77a8675694a
Author: javanna <cavannaluca@gmail.com>
Date:   Tue Jun 30 15:41:32 2015 +0200

    Query refactoring: unify boost and query name

    Following the discussion in #11744, move boost and query _name to base class AbstractQueryBuilder with their getters and setters. Unify their serialization code and equals/hashcode handling in the base class too. This guarantess that every query supports both _name and boost and nothing needs to be done around those in subclasses besides properly parsing the fields in the parsers and printing them out as part of the doXContent method in the builders. More specifically, these are the performed changes:

    - Introduced printBoostAndQueryName utility method in AbstractQueryBuilder that subclasses can use to print out _name and boost in their doXContent method.

    - readFrom and writeTo are now final methods that take care of _name and boost serialization. Subclasses have to implement doReadFrom and doWriteTo instead.

    - toQuery is a final method too that takes care of properly applying _name and boost to the lucene query. Subclasses have to implement doToQuery instead. The query returned will have boost and queryName applied automatically.

    - Removed BoostableQueryBuilder interface, given that every query is boostable after this change. This won't have any negative effect on filters, as the boost simply gets ignored in that case.

    - Extended equals and hashcode to handle queryName and boost automatically as well.

    - Update the query test infra so that queryName and boost are tested automatically, and whenever they are forgotten in parser or doXContent tests fail, so this makes things a lot less error-prone

    - Introduced DEFAULT_BOOST constant to make sure we don't repeat 1.0f all the time for default boost values.

    SpanQueryBuilder is again a marker interface only. The convenient toQuery that allowed us to override the return type to SpanQuery cannot be supported anymore due to a clash with the toQuery implementation from AbstractQueryBuilder. We have to go back to castin lucene Query to SpanQuery when dealing with span queries unfortunately.

    Note that this change touches not only the already refactored queries but also the untouched ones, by making sure that we parse _name and boost whenever we need to and that we print them out as part of QueryBuilder#doXContent. This will result in printing out the default boost all the time rather than skipping it in non refactored queries, something that we would have changed anyway as part of the query refactoring.

    The following are the queries that support boost now while previously they didn't (parser now parses it and builder prints it out): and, exists, fquery, geo_bounding_box, geo_distance, geo_distance_range, geo_hash_cell, geo_polygon, indices, limit, missing, not, or, script, type.

    The following are the queries that support _name now while previously they didn't (parser now parses it and builder prints it out): boosting, constant_score, function_score, limit, match_all,  type.

    Range query parser supports now _name at the same level as boost too (_name is still supported on the outer object though for bw comp).

    There are two exceptions that despite have getters and setters for queryName and boost don't really support boost and queryName: query filter and span multi term query. The reason for this is that they only support a single inner object which is another query that they wrap, no other elements.

    Relates to #11744
    Closes #10776
    Closes #11974