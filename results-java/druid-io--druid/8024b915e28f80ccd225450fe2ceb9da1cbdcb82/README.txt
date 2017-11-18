commit 8024b915e28f80ccd225450fe2ceb9da1cbdcb82
Author: Charles Allen <charles@allen-net.com>
Date:   Tue May 24 10:56:40 2016 -0700

    [QTL] Implement LookupExtractorFactory of namespaced lookup (#2926)

    * support LookupReferencesManager registration of namespaced lookup and eliminate static configurations for lookup from namespecd lookup extensions

    - druid-namespace-lookup and druid-kafka-extraction-namespace are modified
    - However, druid-namespace-lookup still has configuration about ON/OFF
      HEAP cache manager selection, which is not namespace wide
      configuration but node wide configuration as multiple namespace shares
      the same cache manager

    * update KafkaExtractionNamespaceTest to reflect argument signature changes

    * Add more synchronization functionality to NamespaceLookupExtractorFactory

    * Remove old way of using extraction namespaces

    * resolve compile error by supporting LookupIntrospectHandler

    * Remove kafka lookups

    * Remove unused stuff

    * Fix start and stop behavior to be consistent with new javadocs

    * Remove unused strings

    * Add timeout option

    * Address comments on configurations and improve docs

    * Add more options and update hash key and replaces

    * Move monitoring to the overriding classes

    * Add better start/stop logging

    * Remove old docs about namespace names

    * Fix bad comma

    * Add `@JsonIgnore` to lookup factory

    * Address code review comments

    * Remove ExtractionNamespace from module json registration

    * Fix problems with naming and initialization. Add tests

    * Optimize imports / reformat

    * Fix future not being properly cancelled on failed initial scheduling

    * Fix delete returns

    * Add more docs about whole introspection

    * Add `/version` introspection point for lookups

    * Add more tests and address comments

    * Add StaticMap extraction namespace for testing. Also add a bunch of tests

    * Move cache system property to `druid.lookup.namespace.cache.type`

    * Make VERSION lower case

    * Change poll period to 0ms  for StaticMap

    * Move cache key to bytebuffer

    * Change hashCode and equals on static map extraction fn

    * Add more comments on StaticMap

    * Address comments

    * Make scheduleAndWait use a latch

    * Sanity renames and fix imports

    * Remove extra info in docs

    * Fix review comments

    * Strengthen failure on start from warn to error

    * Address comments

    * Rename namespace-lookup to lookups-cached-global

    * Fix injective mis-naming
    * Also add serde test