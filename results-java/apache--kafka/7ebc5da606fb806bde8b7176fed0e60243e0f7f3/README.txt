commit 7ebc5da606fb806bde8b7176fed0e60243e0f7f3
Author: Guozhang Wang <wangguoz@gmail.com>
Date:   Fri Feb 3 11:12:49 2017 -0800

    KAFKA-3452 Follow-up: Optimize ByteStore Scenarios

    This is a refactoring follow-up of https://github.com/apache/kafka/pull/2166. Main refactoring changes:

    1. Extract `InMemoryKeyValueStore` out of `InMemoryKeyValueStoreSupplier` and remove its duplicates in test package.

    2. Add two abstract classes `AbstractKeyValueIterator` and `AbstractKeyValueStore` to collapse common functional logics.

    3. Added specialized `BytesXXStore` to accommodate cases where key value types are Bytes / byte[] so that we can save calling the dummy serdes.

    4. Make the key type in `ThreadCache` from byte[] to Bytes, as SessionStore / WindowStore's result serialized bytes are in the form of Bytes anyways, so that we can save unnecessary `Bytes.get()` and `Bytes.wrap(bytes)`.

    Each of these should arguably be a separate PR and I apologize for the mess, this is because this branch was extracted from a rather large diff that has multiple refactoring mingled together and dguy and myself have already put lots of efforts to break it down to a few separate PRs, and this is the only left-over work. Such PR won't happen in the future.

    Ping dguy enothereska mjsax for reviews

    Author: Guozhang Wang <wangguoz@gmail.com>

    Reviewers: Damian Guy, Matthias J. Sax, Jun Rao

    Closes #2333 from guozhangwang/K3452-followup-state-store-refactor