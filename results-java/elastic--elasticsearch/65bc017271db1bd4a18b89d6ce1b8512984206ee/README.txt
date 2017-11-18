commit 65bc017271db1bd4a18b89d6ce1b8512984206ee
Author: Shay Banon <kimchy@gmail.com>
Date:   Sat Apr 12 12:10:56 2014 +0200

    Don't lookup version for auto generated id and create
    When a create document is executed, and its an auto generated id (based on UUID), we know that the document will not exists in the index, so there is no need to try and lookup the version from the index.
    For many cases, like logging, where ids are auto generated, this can improve the indexing performance, specifically for lightweight documents where analysis is not a big part of the execution.
    closes #5917