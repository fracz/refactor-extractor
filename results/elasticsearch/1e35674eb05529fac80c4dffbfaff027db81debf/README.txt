commit 1e35674eb05529fac80c4dffbfaff027db81debf
Author: David Pilato <david@pilato.fr>
Date:   Wed Jun 24 23:27:19 2015 +0200

    [doc] Reorganize and clean Java documentation

    This commit reorganizes the docs to make Java API docs looking more like the REST docs.
    Also, with 2.0.0, FilterBuilders don't exist anymore but only QueryBuilders.

    Also, all docs api move now to docs/java-api/docs dir as for REST doc.

    Remove removed queries/filters
    -----

    * Remove Constant Score Query with filter
    * Remove Fuzzy Like This (Field) Query (flt and flt_field)
    * Remove FilterBuilders

    Move filters to queries
    -----

    * Move And Filter to And Query
    * Move Bool Filter to Bool Query
    * Move Exists Filter to Exists Query
    * Move Geo Bounding Box Filter to Geo Bounding Box Query
    * Move Geo Distance Filter to Geo Distance Query
    * Move Geo Distance Range Filter to Geo Distance Range Query
    * Move Geo Polygon Filter to Geo Polygon Query
    * Move Geo Shape Filter to Geo Shape Query
    * Move Has Child Filter by Has Child Query
    * Move Has Parent Filter by Has Parent Query
    * Move Ids Filter by Ids Query
    * Move Limit Filter to Limit Query
    * Move MatchAll Filter to MatchAll Query
    * Move Missing Filter to Missing Query
    * Move Nested Filter to Nested Query
    * Move Not Filter to Not Query
    * Move Or Filter to Or Query
    * Move Range Filter to Range Query
    * Move Ids Filter to Ids Query
    * Move Term Filter to Term Query
    * Move Terms Filter to Terms Query
    * Move Type Filter to Type Query

    Add missing queries
    -----

    * Add Common Terms Query
    * Add Filtered Query
    * Add Function Score Query
    * Add Geohash Cell Query
    * Add Regexp Query
    * Add Script Query
    * Add Simple Query String Query
    * Add Span Containing Query
    * Add Span Multi Term Query
    * Add Span Within Query

    Reorganize the documentation
    -----

    * Organize by full text queries
    * Organize by term level queries
    * Organize by compound queries
    * Organize by joining queries
    * Organize by geo queries
    * Organize by specialized queries
    * Organize by span queries
    * Move Boosting Query
    * Move DisMax Query
    * Move Fuzzy Query
    * Move Indices Query
    * Move Match Query
    * Move Mlt Query
    * Move Multi Match Query
    * Move Prefix Query
    * Move Query String Query
    * Move Span First Query
    * Move Span Near Query
    * Move Span Not Query
    * Move Span Or Query
    * Move Span Term Query
    * Move Template Query
    * Move Wildcard Query

    Add some missing pages
    ----

    * Add multi get API
    * Add indexed-scripts link

    Also closes #7826
    Related to https://github.com/elastic/elasticsearch/pull/11477#issuecomment-114745934