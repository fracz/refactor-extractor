commit a3abfab865788a3828424f78801d19ebd5a78b81
Author: javanna <cavannaluca@gmail.com>
Date:   Tue Sep 29 23:54:53 2015 +0200

    Query refactoring: set has_parent & has_child types context properly

    While refactoring has_child and has_parent query we lost an important detail around types. The types that the inner query gets executed against shouldn't be the main types of the search request but the parent or child type set to the parent query. We used to use QueryParseContext#setTypesWithPrevious as part of XContentStructure class which has been deleted, without taking care though of setting the types and restoring them as part of the innerQuery#toQuery call.

    Meanwhile also we make sure that the original context types are restored in PercolatorQueriesRegistry

    Closes #13863