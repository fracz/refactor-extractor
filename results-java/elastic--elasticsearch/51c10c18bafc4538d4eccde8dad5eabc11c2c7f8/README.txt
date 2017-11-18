commit 51c10c18bafc4538d4eccde8dad5eabc11c2c7f8
Author: David Pilato <david@pilato.fr>
Date:   Tue Aug 5 16:07:35 2014 +0200

    Tests: refactor tests

    We need to simplify a bit our tests.
    Also, we need to mark as `@Ignore` tests as they break now due to some cluster controls after each test in elasticsearch test infra.

    See [org.elasticsearch.test.ElasticsearchIntegrationTest#ensureClusterSizeConsistency()](https://github.com/elasticsearch/elasticsearch/blob/v1.3.1/src/test/java/org/elasticsearch/test/ElasticsearchIntegrationTest.java#L974-L979)

    Closes #33.