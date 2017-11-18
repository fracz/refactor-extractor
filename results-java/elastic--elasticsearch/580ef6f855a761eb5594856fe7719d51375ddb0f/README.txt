commit 580ef6f855a761eb5594856fe7719d51375ddb0f
Author: javanna <cavannaluca@gmail.com>
Date:   Fri May 1 18:50:45 2015 +0200

    Filter refactoring: Introduce toFilter() and fromXContent() in FilterBuilders and FilterParsers

     The planned refactoring of search queries requires to split the "parse()" method in FilterParsers into two methods, first a "fromXContent(...)" method that allows parsing to an intermediate filter representation (currently called FooFilterBuilder) and second a "Filter toFilter(...)" method on these intermediate representations that create the actual lucene filters.
    This PR is a first step in that direction as it introduces the interface changes necessary for the further refactoring. It introduces the new interface methods while for now keeping the old Builder/Parsers still in place by delegating the new "toFilter()" implementations to the existing "parse()" methods, and by introducing a "catch-all" "fromXContent()" implementation in a BaseFilterParser that returns a temporary FilterBuilder wrapper implementation. This allows us to refactor the existing FilterBuilders step by step while already being able to start refactoring queries and filters with inner queries/filters.