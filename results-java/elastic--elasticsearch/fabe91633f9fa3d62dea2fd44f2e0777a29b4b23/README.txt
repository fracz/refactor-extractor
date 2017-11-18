commit fabe91633f9fa3d62dea2fd44f2e0777a29b4b23
Author: javanna <cavannaluca@gmail.com>
Date:   Mon Jun 22 11:54:52 2015 +0200

    Query refactoring: make QueryBuilder an interface again and rename current abstract class to AbstractQueryBuilder

    It is handy to have a base interface, not just an abstract class, for all of our query builders. This gives us  more flexibility especialy with complex class hierarchies. For instance SpanTermQueryBuilder extends BaseTermQueryBuilder, but also needs to be marked as a SpanQueryBuilder. The latter is a marker interface that should extend QueryBuilder which is not possible unless QueryBuilder actually is an interface.

    Also remove queryId method as it created confusion, getName is good enough for the purpose, and override the return type of toQuery method for SpanQueryBuilders to SpanQuery.

    Closes #11796