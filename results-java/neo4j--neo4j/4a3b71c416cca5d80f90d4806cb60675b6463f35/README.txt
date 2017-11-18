commit 4a3b71c416cca5d80f90d4806cb60675b6463f35
Author: Andres Taylor <andres@neotechnology.com>
Date:   Tue Mar 19 16:26:46 2013 +0100

    Adds SchemaState support to kernel

    o Adds TransactionAwareSchemaStateHolder and plugs it into the cake
    o Extracts UpdateableSchemaStateHolder, polishes tests, fixes transactional flush handling
    o Adds schema state handling tests to SchemaStateOperationsTest
    o Defers SchemaState flushing until an index comes ONLINE and makes the population job do that
    o Adds LRU eviction to SchemaState
    o Removes Class-usage and changes concurrency in SchemaState handling and some renaming
    o Removed legacy index stuff from kernel API.
    o SchemaState interface refactoring
    o ROContext support