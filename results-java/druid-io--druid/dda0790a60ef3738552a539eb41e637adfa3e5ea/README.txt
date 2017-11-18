commit dda0790a60ef3738552a539eb41e637adfa3e5ea
Author: Slim Bouguerra <bslim@yahoo-inc.com>
Date:   Fri Jul 24 09:54:12 2015 -0500

    Fix extractionFilter by implementing make matcher
    Fix getBitmapIndex to consider the case were dim is null
    Unit Test for exractionFn with empty result and null_column
    UT for TopN queries with Extraction filter
    refactor in Extractiuon fileter makematcher for realtime segment and clean code in b/processing/src/test/java/io/druid/query/groupby/GroupByQueryRunnerTest.java
    fix to make sure that empty string are converted to null