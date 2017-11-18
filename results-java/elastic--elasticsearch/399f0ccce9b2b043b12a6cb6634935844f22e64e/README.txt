commit 399f0ccce9b2b043b12a6cb6634935844f22e64e
Author: Michael McCandless <mail@mikemccandless.com>
Date:   Thu Apr 16 04:54:00 2015 -0400

    Core: add only_ancient_segments to upgrade API, so only segments with an old Lucene version are upgraded

    This option defaults to false, because it is also important to upgrade
    the "merely old" segments since many Lucene improvements happen within
    minor releases.

    But you can pass true to do the minimal work necessary to upgrade to
    the next major Elasticsearch release.

    The HTTP GET upgrade request now also breaks out how many bytes of
    ancient segments need upgrading.

    Closes #10213

    Closes #10540

    Conflicts:
            dev-tools/create_bwc_index.py
            rest-api-spec/api/indices.upgrade.json
            src/main/java/org/elasticsearch/action/admin/indices/optimize/OptimizeRequest.java
            src/main/java/org/elasticsearch/action/admin/indices/optimize/ShardOptimizeRequest.java
            src/main/java/org/elasticsearch/action/admin/indices/optimize/TransportOptimizeAction.java
            src/main/java/org/elasticsearch/index/engine/InternalEngine.java
            src/test/java/org/elasticsearch/bwcompat/StaticIndexBackwardCompatibilityTest.java
            src/test/java/org/elasticsearch/index/engine/InternalEngineTests.java
            src/test/java/org/elasticsearch/rest/action/admin/indices/upgrade/UpgradeReallyOldIndexTest.java