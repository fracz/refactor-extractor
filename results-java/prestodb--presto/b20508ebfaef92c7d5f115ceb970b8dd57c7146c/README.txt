commit b20508ebfaef92c7d5f115ceb970b8dd57c7146c
Author: Haozhun Jin <haozhun.jin@gmail.com>
Date:   Tue Mar 29 13:34:16 2016 -0700

    Remove supported type check in HiveType factories

    * Add supported type check where necessary after surveying callers
    * Improve supported type check in HiveMetadata to catch unsupported type
      (Originally, only supported type that can't be written is caught.
      Unsupported types are caught downstream by HiveRecordWriter.)
    * Improve error message to provide user more context on unsupported type
    * Pre-requisite for upcoming refactoring of metastore classes