commit ba8d67ebc362fe60df4c68e30f1fea7618ddc44b
Author: Saverio Veltri <save.veltri@gmail.com>
Date:   Wed Oct 14 10:47:29 2015 +0200

    - JournalFormatterTestBase and JsonJournalFormatterTestBase refactor: every entry is tested in a loop instead of in single method
    - added a method for checking if every entry is covered by tests
    - added test object and json representation for non tested entries
    - commented FREE EntryType (it is never used)