commit 50223c222e22b671812e4e4951154cb5b8f25e07
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Thu Dec 15 17:11:57 2016 +0700

    Fix IDEA-165466 Stream API migration: automatically simplify emptyList().stream() to empty()
    SimplifyStreamApiCallChainsInspection refactoring: simplifyCollectionStreamCalls extracted;