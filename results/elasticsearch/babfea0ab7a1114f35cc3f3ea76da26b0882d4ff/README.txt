commit babfea0ab7a1114f35cc3f3ea76da26b0882d4ff
Author: javanna <cavannaluca@gmail.com>
Date:   Thu Aug 13 17:47:47 2015 +0200

    Query refactoring: fixed some leftover generics warnings

    Leftover after adding the query builder type to QueryParser. getBuilderPrototype is better typed now and doesn't require unchecked cast anymore. Fixed also some Tuple usage without types.