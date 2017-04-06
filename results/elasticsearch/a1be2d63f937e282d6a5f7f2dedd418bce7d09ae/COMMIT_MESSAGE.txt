commit a1be2d63f937e282d6a5f7f2dedd418bce7d09ae
Author: javanna <cavannaluca@gmail.com>
Date:   Tue Aug 11 14:30:37 2015 +0200

    Query refactoring: better naming consistency for getters

    In the query refactoring branch we've been introducing getter methods for every bit that you can set to each query. The naming is not every consistent at the moment. The applied naming convention are the following:

    - `innerQuery()` for any inner query, when there's only one of them
    - when there's more than one inner query, use a prefix that identifies which query it is, and the `query` suffix (e.g. `positiveQuery` or `littleQuery`)
    - `fieldName()` for the name of the field to be queried
    - `value()` for the actual query

    These changes don't break bw comp given that these getters were all introduced with the query refactoring which hasn't been released yet. Also we are modifying getters that don't have a corresponding setter, as the fields are final, hence we are not breaking consistency between getter and setter.

    Closes #12800