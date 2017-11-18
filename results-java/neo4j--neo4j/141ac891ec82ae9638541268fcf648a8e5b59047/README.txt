commit 141ac891ec82ae9638541268fcf648a8e5b59047
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Mon Mar 31 15:43:21 2014 +0200

    Another round of refactorings, to allow for versioned command reading

    LogDeserializers delegate the version read to the VersionAwareLogEntryReader
    LogEntries carry a version field
    LogDeserializers no longer require a CommandReader - they require a
     CommandReaderFactory that knows about versions
    CommandReaderFactory must now know about versions
    LuceneXaCommandFactory no longer depends on LuceneDataSource