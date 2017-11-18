commit 76cb06a8d8161d29d985ef048b89e6a82b489058
Author: Roman Leventov <leventov@users.noreply.github.com>
Date:   Fri Dec 23 20:04:27 2016 -0600

    Lookup cache refactoring (the main part of #3667) (#3697)

    * Lookup cache refactoring (the main part of druid-io/druid#3667)

    * Use PowerMock's static methods in NamespaceLookupExtractorFactoryTest

    * Fix KafkaLookupExtractorFactoryTest

    * Use VisibleForTesting annotation instead of Javadoc comment

    * Create a NamespaceExtractionCacheManager separately for each test in NamespaceExtractionCacheManagersTest

    * Rename CacheScheduler.NoCache.ENTRY_DISPOSED to ENTRY_CLOSED

    * Reduce visibility of NamespaceExtractionCacheManager.cacheCount() and monitor() implementations, and don't run NamespaceExtractionCacheManagerExecutorsTest with off-heap cache (it didn't before)

    * In NamespaceLookupExtractorFactory, use safer idiom to check if CacheState is NoCache or VersionedCache

    * More logging in CacheHandler constructor and close(), VersionedCache.close()

    * PR comments addressed

    * Make CacheScheduler.EntryImpl AutoCloseable, avoid 'dispose' verb in comments, logging and naming in CacheScheduler in favor of 'close'

    * More Javadoc comments to CacheScheduler

    * Fix NPE

    * Remove logging in OnHeapNamespaceExtractionCacheManager.expungeCollectedCaches()

    * Make NamespaceExtractionCacheManagersTest.testRacyCreation() to have similar load to what it be before the refactoring

    * Unwrap NamespaceExtractionCacheManager.scheduledExecutorService from unneeded MoreExecutors.listeningDecorator() and specify that this is ScheduledThreadPoolExecutor, which ensures happens-before between periodic runs of the tasks

    * More comments on MapDbCacheDisposer.disposed

    * Replace concat with Long.toString()

    * Comment on why NamespaceExtractionCacheManager.scheduledExecutorService() returns ScheduledThreadPoolExecutor

    * Place logging statements in VersionedCache.close() and CacheHandler.close() after actual closing logic, because logging may fail

    * Make JDBCExtractionNamespaceCacheFactory and StaticMapExtractionNamespaceCacheFactory to try to close newly created VersionedCache if population has failed, as it is done already in URIExtractionNamespaceCacheFactory

    * Don't close the whole CacheScheduler.Entry, if the cache update task failed

    * Replace AtomicLong updateCounter and firstRunLatch with Phaser-based UpdateCounter in CacheScheduler.EntryImpl