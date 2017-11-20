commit e1338d8c8a42a4cee31b7819eead17d8629253c3
Author: dta <dta@compart.com>
Date:   Tue Sep 6 16:12:02 2016 +0200

    * Added interface (strategy pattern) for saving/loading fields from a Document
    * ObjectField handling strategies take into account:
    ** The class schema
    ** The existing data
    ** null values
    * Added handler for accessing a field handling strategy.
    * Aded OGlobalConfiguration entry to select the strategy to be used
    * OObjectProxyMethodHandler refactor to use ObjectFieldHandlingStrategy
    * Added strategy interface and implementations for custom Otypes
    * Added strategy to store binary data in a single ORecordBytes document
    * Added strategy to store binary data split in several ORecordBytes documents
    * Fixed bug identifying otype binary