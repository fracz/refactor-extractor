commit 5c748ddb42994f7f41475092db296f9a12cd23f0
Author: Carsten Brandt <mail@cebe.cc>
Date:   Tue Apr 29 12:04:10 2014 +0200

    added case insensitve LIKE to PostgresQueryBuilder

    fixes #3252

    also improved unit tests for querybuilder buildLikeCondition