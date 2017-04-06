commit 9e01dedef5518f470f1b2773196654f994f4df7f
Author: javanna <cavannaluca@gmail.com>
Date:   Fri May 1 14:24:44 2015 +0200

    Java api: remove redundant BytesQueryBuilder in favour of using WrapperQueryBuilder internally

    BytesQueryBuilder was introduced to be used internally by the phrase suggester and its collate feature. It ended up being exposed via Java api but the existing WrapperQueryBuilder could be used instead. Added WrapperQueryBuilder constructor that accepts a BytesReference as argument.

    One other reason why this filter builder should be removed is that it gets on the way of the query parsers refactoring, given that it's the only query builder that allows to build a query through java api without having a respective query parser.

    Closes #10919