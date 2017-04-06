commit 3f04ee076e605dbf28c75f79bf08988ddc18079b
Author: javanna <cavannaluca@gmail.com>
Date:   Tue Aug 18 13:04:30 2015 +0200

    Internal: IndicesQueriesRegitry back to being created only once

    With #12921 we refactored IndicesModule but we forgot to make sure we create IndicesQueriesRegistry once. IndicesQueriesModule used to do `bind(IndicesQueriesRegistry.class).asEagerSingleton();` otherwise we get multiple instances of the registry. This needs to be ported do the IndicesModule.