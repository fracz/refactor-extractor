commit 56b3db6ba3463d244ea777b527b7a786caa5042c
Merge: 8d2a2f8 c10f116
Author: javanna <cavannaluca@gmail.com>
Date:   Thu Sep 10 15:52:28 2015 +0200

    Merge branch 'master' into feature/query-refactoring

    Conflicts:
            core/src/main/java/org/elasticsearch/index/query/AndQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/AndQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/FQueryFilterParser.java
            core/src/main/java/org/elasticsearch/index/query/FilteredQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/FilteredQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/HasChildQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/IndicesQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/LimitQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/LimitQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/MultiMatchQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/OrQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/OrQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/QueryFilterBuilder.java
            core/src/main/java/org/elasticsearch/index/query/QueryFilterParser.java
            core/src/main/java/org/elasticsearch/index/query/QueryParseContext.java
            core/src/main/java/org/elasticsearch/index/query/ScriptQueryParser.java
            core/src/main/java/org/elasticsearch/indices/query/IndicesQueriesRegistry.java
            core/src/main/java/org/elasticsearch/search/sort/GeoDistanceSortParser.java
            core/src/main/java/org/elasticsearch/search/sort/SortParseElement.java
            core/src/test/java/org/elasticsearch/benchmark/search/child/ChildSearchShortCircuitBenchmark.java
            core/src/test/java/org/elasticsearch/index/query/SimpleIndexQueryParserTests.java