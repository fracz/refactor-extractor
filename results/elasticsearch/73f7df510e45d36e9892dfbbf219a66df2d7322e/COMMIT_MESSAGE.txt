commit 73f7df510e45d36e9892dfbbf219a66df2d7322e
Merge: d49a744 2c618a1
Author: javanna <cavannaluca@gmail.com>
Date:   Fri Sep 11 14:15:12 2015 +0200

    Merge branch 'master' into feature/query-refactoring

    Conflicts:
            core/src/main/java/org/elasticsearch/index/query/HasChildQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/HasChildQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/HasParentQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/HasParentQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/functionscore/FunctionScoreQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/functionscore/FunctionScoreQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/functionscore/factor/FactorParser.java
            core/src/test/java/org/elasticsearch/ExceptionSerializationTests.java
            core/src/test/java/org/elasticsearch/benchmark/search/child/ChildSearchBenchmark.java
            core/src/test/java/org/elasticsearch/benchmark/search/child/ChildSearchShortCircuitBenchmark.java
            core/src/test/java/org/elasticsearch/index/query/SimpleIndexQueryParserTests.java
            core/src/test/java/org/elasticsearch/percolator/PercolatorIT.java
            core/src/test/java/org/elasticsearch/search/child/ChildQuerySearchIT.java
            docs/reference/query-dsl/has-parent-query.asciidoc