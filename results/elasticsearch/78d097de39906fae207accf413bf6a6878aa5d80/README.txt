commit 78d097de39906fae207accf413bf6a6878aa5d80
Merge: 084a610 db5e225
Author: javanna <cavannaluca@gmail.com>
Date:   Fri Aug 28 10:49:28 2015 +0200

    Merge branch 'master' into feature/query-refactoring

    Conflicts:
            core/src/main/java/org/apache/lucene/queryparser/classic/MapperQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/AndQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/AndQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/BoolQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/DisMaxQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/DisMaxQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/GeoPolygonQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/HasChildQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/HasParentQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/MoreLikeThisQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/MultiMatchQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/NestedQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/OrQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/OrQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/PrefixQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/RegexpQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/SpanNearQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/SpanOrQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/TermQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/TermsQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/support/InnerHitsQueryParserHelper.java
            core/src/test/java/org/elasticsearch/index/query/SimpleIndexQueryParserTests.java