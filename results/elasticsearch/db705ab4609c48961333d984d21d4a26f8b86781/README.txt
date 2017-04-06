commit db705ab4609c48961333d984d21d4a26f8b86781
Merge: 678e1be 73d84e4
Author: javanna <cavannaluca@gmail.com>
Date:   Wed Sep 9 17:08:22 2015 +0200

    Merge branch 'master' into feature/query-refactoring

    Conflicts:
            core/src/main/java/org/elasticsearch/index/mapper/internal/ParentFieldMapper.java
            core/src/main/java/org/elasticsearch/index/query/HasChildQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/HasChildQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/HasParentQueryParser.java
            core/src/main/java/org/elasticsearch/index/query/MoreLikeThisQueryBuilder.java
            core/src/main/java/org/elasticsearch/index/query/MoreLikeThisQueryParser.java
            core/src/main/java/org/elasticsearch/index/search/child/ChildrenQuery.java
            core/src/test/java/org/elasticsearch/index/query/SimpleIndexQueryParserTests.java
            core/src/test/java/org/elasticsearch/index/search/child/AbstractChildTestCase.java
            core/src/test/java/org/elasticsearch/index/search/child/ChildrenConstantScoreQueryTests.java
            core/src/test/java/org/elasticsearch/index/search/child/ChildrenQueryTests.java
            core/src/test/java/org/elasticsearch/search/child/ChildQuerySearchBwcIT.java
            core/src/test/java/org/elasticsearch/search/child/ChildQuerySearchIT.java
            core/src/test/resources/org/elasticsearch/index/query/simple-query-string.json