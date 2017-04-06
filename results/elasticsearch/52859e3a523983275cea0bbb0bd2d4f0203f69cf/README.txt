commit 52859e3a523983275cea0bbb0bd2d4f0203f69cf
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Mon Jul 6 17:32:39 2015 +0200

    Internal: refactored MetaData to split the concrete index name resolution to IndexNameExpressionResolver.

    Changes in a nutshell:
    * All expression logic is now encapsulated by ExpressionResolver interface.
    * MetaData#convertFromWildcards() gets replaced by WildcardExpressionResolver.
    * All of the indices expansion methods are being moved from MetaData class to the new IndexNameExpressionResolver class.
    * All single index expansion optimisations are removed.

    The logic for resolving a concrete index name from an expression has been moved from MetaData to IndexExpressionResolver. The logic has been cleaned up and simplified were was possible without breaking bwc.

    Also the notion of aliasOrIndex has been changed to index expression.

    The IndexNameExpressionResolver translates index name expressions into concrete indices. The list of index name expressions are first delegated to the known ExpressionResolverS. An ExpressionResolver is responsible for translating if possible an expression into another expression (possibly but not required this can be concrete indices or aliases) otherwise the expressions are left untouched. Concretely this means converting wildcard expressions into concrete indices or aliases, but in the future other implementations could convert expressions based on different rules.

    To prevent many overloading of methods, DocumentRequest extends now from IndicesRequest. All implementation of DocumentRequest already did implement IndicesRequest indirectly.