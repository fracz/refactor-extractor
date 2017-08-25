commit 54e8bb745e4b0c1f53cb7ed9d30a30b7f6899640
Author: Nate Abele <nate.abele@gmail.com>
Date:   Sat Feb 27 13:08:39 2010 -0500

    Implementing `'count'` query type in `\data\Model`, misc. refactorings in MongoDB adapter, added support for count and group queries. Refactored `\data\model\Query` to support dynamic settings. Refactored `Query` setters to also return values. Added support methods for grouping and aggregation.