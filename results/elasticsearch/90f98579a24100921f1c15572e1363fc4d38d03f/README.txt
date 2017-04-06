commit 90f98579a24100921f1c15572e1363fc4d38d03f
Author: Adrien Grand <jpountz@gmail.com>
Date:   Mon Jan 5 17:01:26 2015 +0100

    Upgrade to Lucene 5.0.0-snapshot-1649544.

    A couple of changes that triggerred a refactoring in Elasticsearch:

     - LUCENE-6148: Accountable.getChildResources returns a collection instead of
       a list.

     - LUCENE-6121: CachingTokenFilter now propagates reset(), as a result
       SimpleQueryParser.newPossiblyAnalyzedQuery has been fixed to not reset both
       the underlying stream and the wrapper (otherwise lucene would barf because of
       a doubl reset).

     - LUCENE-6119: The auto-throttle issue changed a couple of method
       names/parameters. It also made
       `UpdateSettingsTests.testUpdateMergeMaxThreadCount` dead slow so I muted this
       test until we clea up merge throttling to use LUCENE-6119.

    Close #9145