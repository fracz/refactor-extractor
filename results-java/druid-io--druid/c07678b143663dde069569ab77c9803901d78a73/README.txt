commit c07678b143663dde069569ab77c9803901d78a73
Author: Atul Mohan <atulmohan.mec@gmail.com>
Date:   Thu Oct 12 21:22:24 2017 -0500

    Synchronization of lookups during startup of druid processes (#4758)

    * Changes for lookup synchronization

    * Refactor of Lookup classes

    * Minor refactors and doc update

    * Change coordinator instance to be retrieved by DruidLeaderClient

    * Wait before thread shutdown

    * Make disablelookups flag true by default

    * Update docs

    * Rename flag

    * Move executorservice shutdown to finally block

    * Update LookupConfig

    * Refactoring and doc changes

    * Remove lookup config constructor

    * Revert Lookupconfig constructor changes

    * Add tests to LookupConfig

    * Make executorservice local

    * Update LRM

    * Move ListeningScheduledExecutorService to ExecutorCompletionService

    * Move exception to outer block

    * Remove check to see future is done

    * Remove unnecessary assignment

    * Add logging