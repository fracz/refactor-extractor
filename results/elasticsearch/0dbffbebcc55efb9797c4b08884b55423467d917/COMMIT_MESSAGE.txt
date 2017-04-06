commit 0dbffbebcc55efb9797c4b08884b55423467d917
Author: javanna <cavannaluca@gmail.com>
Date:   Thu May 7 14:58:38 2015 +0200

    Revert "Filter refactoring: Introduce toFilter() and fromXContent() in FilterBuilders and FilterParsers"

    This reverts commit 580ef6f855a761eb5594856fe7719d51375ddb0f given that FilterBuilder and FilterParser are going away with #10985

    Conflicts:
            src/main/java/org/elasticsearch/index/query/BaseFilterBuilder.java
            src/main/java/org/elasticsearch/index/query/BaseFilterParser.java
            src/main/java/org/elasticsearch/index/query/BaseFilterParserTemp.java
            src/main/java/org/elasticsearch/index/query/FilterWrappingFilterBuilder.java