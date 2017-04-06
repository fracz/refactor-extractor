commit 9a70dbb51ab9a5140691c6822ee7002ddd366337
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Thu Dec 17 12:05:43 2015 -0500

    Add ability to profile query and collectors

    Provides a new flag which can be enabled on a per-request basis.
    When `"profile": true` is set, the search request will execute in a
    mode that collects detailed timing information for query components.

    ```
    GET /test/test/_search
    {
       "profile": true,
       "query": {
          "match": {
             "foo": "bar"
          }
       }
    }
    ```

    Closes #14889

    Squashed commit of the following:

    commit a92db5723d2c61b8449bd163d2f006d12f9889ad
    Merge: 117dd99 3f87b08
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Dec 17 09:44:10 2015 -0500

        Merge remote-tracking branch 'upstream/master' into query_profiler

    commit 117dd9992e8014b70203c6110925643769d80e62
    Merge: 9b29d68 82a64fd
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Dec 15 13:27:18 2015 -0500

        Merge remote-tracking branch 'upstream/master' into query_profiler

        Conflicts:
            core/src/main/java/org/elasticsearch/search/SearchService.java

    commit 9b29d6823a71140ecd872df25ff9f7478e7fe766
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Dec 14 16:13:23 2015 -0500

        [TEST] Profile flag needs to be set, ensure searches go against primary only for consistency

    commit 4d602d8ad1f8cbc7b475450921fa3bc7d395b34f
    Merge: 8b48e87 7742c1e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Dec 14 10:56:25 2015 -0500

        Merge remote-tracking branch 'upstream/master' into query_profiler

    commit 8b48e876348b163ab730eeca7fa35142165b05f9
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Dec 14 10:56:01 2015 -0500

        Delegate straight to in.matchCost, no need for profiling

    commit fde3b0587911f0b5f15e779c671d0510cbd568a9
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Dec 14 10:28:23 2015 -0500

        Documentation tweaks, renaming build_weight -> create_weight

    commit 46f5e011ee23fe9bb8a1f11ceb4fa9d19fe48e2e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Dec 14 10:27:52 2015 -0500

        Profile TwoPhaseIterator should override matchCost()

    commit b59f894ddb11b2a7beebba06c4ec5583ff91a7b2
    Merge: 9aa1a3a b4e0c87
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Dec 9 14:23:26 2015 -0500

        Merge remote-tracking branch 'upstream/master' into query_profiler

    commit 9aa1a3a25c34c9cd9fffaa6114c25a0ec791307d
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Dec 9 13:41:48 2015 -0500

        Revert "Move some of the collector wrapping logic into ProfileCollectorBuilder"

        This reverts commit 02cc31767fb76a7ecd44a302435e93a05fb4220e.

    commit 57f7c04cea66b3f98ba2bec4879b98e4fba0b3c0
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Dec 9 13:41:31 2015 -0500

        Revert "Rearrange if/else to make intent clearer"

        This reverts commit 59b63c533fcaddcdfe4656e86a6f6c4cb1bc4a00.

    commit 2874791b9c9cd807113e75e38be465f3785c154e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Dec 9 13:38:13 2015 -0500

        Revert "Move state into ProfileCollectorBuilder"

        This reverts commit 0bb3ee0dd96170b06f07ec9e2435423d686a5ae6.

    commit 0bb3ee0dd96170b06f07ec9e2435423d686a5ae6
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Dec 3 11:21:55 2015 -0500

        Move state into ProfileCollectorBuilder

    commit 59b63c533fcaddcdfe4656e86a6f6c4cb1bc4a00
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Dec 2 17:21:12 2015 -0500

        Rearrange if/else to make intent clearer

    commit 511db0af2f3a86328028b88a6b25fa3dfbab963b
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Dec 2 17:12:06 2015 -0500

        Rename WEIGHT -> BUILD_WEIGHT

    commit 02cc31767fb76a7ecd44a302435e93a05fb4220e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Dec 2 17:11:22 2015 -0500

        Move some of the collector wrapping logic into ProfileCollectorBuilder

    commit e69356d3cb4c60fa281dad36d84faa64f5c32bc4
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 30 15:12:35 2015 -0500

        Cleanup imports

    commit c1b4f284f16712be60cd881f7e4a3e8175667d62
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 30 15:11:25 2015 -0500

        Review cleanup: Make InternalProfileShardResults writeable

    commit 9e61c72f7e1787540f511777050a572b7d297636
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 30 15:01:22 2015 -0500

        Review cleanup: Merge ProfileShardResult, InternalProfileShardResult.  Convert to writeable

    commit 709184e1554f567c645690250131afe8568a5799
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 30 14:38:08 2015 -0500

        Review cleanup: Merge ProfileResult, InternalProfileResult.  Convert to writeable

    commit 7d72690c44f626c34e9c608754bc7843dd7fd8fe
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 30 14:01:34 2015 -0500

        Review cleanup: use primitive (and default) for profile flag

    commit 97d557388541bbd3388cdcce7d9718914d88de6d
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 30 13:09:12 2015 -0500

        Review cleanup: Use Collections.emptyMap() instead of building an empty one explicitly

    commit 219585b8729a8b0982e653d99eb959efd0bef84e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 30 13:08:12 2015 -0500

        Add todo to revisit profiler architecture in the future

    commit b712edb2160e032ee4b2f2630fadf131a0936886
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 30 13:05:32 2015 -0500

        Split collector serialization from timing, use writeable instead of streamable

        Previously, the collector timing was done in the same class that was serialized, which required
        leaving the collector null when serializing.  Besides being a bit gross, this made it difficult to
        change the class to Writeable.

        This splits up the timing (InternalProfileCollector + ProfileCollector) and the serialization of
        the times (CollectorResult).  CollectorResult is writeable, and also acts as the public interface.

    commit 6ddd77d066262d4400e3d338b11cebe7dd27ca78
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Nov 25 13:15:12 2015 -0500

        Remove dead code

    commit 06033f8a056e2121d157654a65895c82bbe93a51
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Nov 25 12:49:51 2015 -0500

        Review cleanup:  Delegate to in.getProfilers()

        Note:  Need to investigate how this is used exactly so we can add a test, it isn't touched by a
        normal inner_hits query...

    commit a77e13da21b4bad1176ca2b5d5b76034fb12802f
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Nov 25 11:59:58 2015 -0500

        Review cleanup:  collapse to single `if` statement

    commit e97bb6215a5ebb508b0293ac3acd60d5ae479be1
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Nov 25 11:39:43 2015 -0500

        Review cleanup: Return empty map instead of null for profile results

        Note: we still need to check for nullness in SearchPhaseController, since an empty/no-hits result
        won't have profiling instantiated (or any other component like aggs or suggest).  Therefore
        QuerySearchResult.profileResults() is still @Nullable

    commit db8e691de2a727389378b459fa76c942572e6015
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Nov 25 10:14:47 2015 -0500

        Review cleanup: renaming, formatting fixes, assertions

    commit 9011775fe80ba22c2fd948ca64df634b4e32772d
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Nov 19 20:09:52 2015 -0500

        [DOCS] Add missing annotation

    commit 4b58560b06f08d4b99b149af20916ee839baabd7
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Nov 19 20:07:17 2015 -0500

        [DOCS] Update documentation for new format

    commit f0458c58e5538ed8ec94849d4baf3250aa9ec841
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Nov 17 10:14:09 2015 +0100

        Reduce visibility of internal classes.

    commit d0a7d319098e60b028fa772bf8a99b2df9cf6146
    Merge: e158070 1bdf29e
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Nov 17 10:09:18 2015 +0100

        Merge branch 'master' into query_profiler

    commit e158070a48cb096551f3bb3ecdcf2b53bbc5e3c5
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Nov 17 10:08:48 2015 +0100

        Fix compile error due to bad html in javadocs.

    commit a566b5d08d659daccb087a9afbe908ec3d96cd6e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 16 17:48:37 2015 -0500

        Remove unused collector

    commit 4060cd72d150cc68573dbde62ca7321c47f75703
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 16 17:48:10 2015 -0500

        Comment cleanup

    commit 43137952bf74728f5f5d5a8d1bfc073e0f9fe4f9
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Nov 16 17:32:06 2015 -0500

        Fix negative formatted time

    commit 5ef3a980266326aff12d4fe380f73455ff28209f
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Nov 13 17:10:17 2015 +0100

        Fix javadocs.

    commit 276114d29e4b17a0cc0982cfff51434f712dc59e
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Nov 13 16:25:23 2015 +0100

        Fix: include rewrite time as well...

    commit 21d9e17d05487bf4903ae3d2ab6f429bece2ffef
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Nov 13 15:10:15 2015 +0100

        Remove TODO about profiling explain.

    commit 105a31e8e570efb879447159c3852871f5cf7db4
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Nov 13 14:59:30 2015 +0100

        Fix nocommit now that the global collector is a root collector.

    commit 2e8fc5cf84adb1bfaba296808c329e5f982c9635
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Nov 13 14:53:38 2015 +0100

        Make collector wrapping more explicit/robust (and a bit less magical).

    commit 5e30b570b0835e1ce79a57933a31b6a2d0d58e2d
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Nov 13 12:44:03 2015 +0100

        Simplify recording API a bit.

    commit 9b453afced6adc0a59ca1d67d90c28796b105185
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Nov 13 10:54:25 2015 +0100

        Fix serialization-related nocommits.

    commit ad97b200bb123d4e9255e7c8e02f7e43804057a5
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Nov 13 10:46:30 2015 +0100

        Fix DFS.

    commit a6de06986cd348a831bd45e4f524d2e14d9e03c3
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Nov 12 19:29:16 2015 +0100

        Remove forbidden @Test annotation.

    commit 4991a28e19501109af98026e14756cb25a56f4f4
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Nov 12 19:25:59 2015 +0100

        Limit the impact of query profiling on the SearchContext API.

        Rule is: we can put as much as we want in the search.profile package but should
        aim at touching as little as possible other areas of the code base.

    commit 353d8d75a5ce04d9c3908a0a63d4ca6e884c519a
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Nov 12 18:05:09 2015 +0100

        Remove dead code.

    commit a3ffafb5ddbb5a2acf43403c946e5ed128f47528
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Nov 12 15:30:35 2015 +0100

        Remove call to forbidden String.toLowerCase() API.

    commit 1fa8c7a00324fa4e32bd24135ebba5ecf07606f1
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Nov 12 15:30:27 2015 +0100

        Fix compilation.

    commit 2067f1797e53bef0e1a8c9268956bc5fb8f8ad97
    Merge: 22e631f fac472f
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Nov 12 15:21:12 2015 +0100

        Merge branch 'master' into query_profiler

    commit 22e631fe6471fed19236578e97c628d5cda401a9
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Nov 3 18:52:05 2015 -0500

        Fix and simplify serialization of shard profile results

    commit 461da250809451cd2b47daf647343afbb4b327f2
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Nov 3 18:32:22 2015 -0500

        Remove helper methods, simpler without them

    commit 5687aa1c93d45416d895c2eecc0e6a6b302139f2
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Nov 3 18:29:32 2015 -0500

        [TESTS] Fix tests for new rewrite format

    commit ba9e82857fc6d4c7b72ef4d962d2102459365299
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 30 15:28:14 2015 -0400

        Rewrites begone! Record all rewrites as a single time metric

    commit 5f28d7cdff9ee736651d564f71f713bf45fb1d91
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Oct 29 15:36:06 2015 -0400

        Merge duplicate rewrites into one entry

        By using the Query as the key in a map, we can easily merge rewrites together.  This means
        the preProcess(), assertion and main query rewrites all get merged together.  Downside is that
        rewrites of the same Query (hashcode) but in different places get lumped together.  I think the
        simplicity of the solution is better than the slight loss in output fidelity.

    commit 9a601ea46bb21052746157a45dcc6de6bc350e9c
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Oct 29 15:28:27 2015 -0400

        Allow multiple "searches" per profile (e.g. query + global agg)

    commit ee30217328381cd83f9e653d3a4d870c1d2bdfce
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Oct 29 11:29:18 2015 -0400

        Update comment, add nullable annotation

    commit 405c6463a64e118f170959827931e8c6a1661f13
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Oct 29 11:04:30 2015 -0400

        remove out-dated comment

    commit 2819ae8f4cf1bfd5670dbd1c0e06195ae457b58f
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Oct 27 19:50:47 2015 +0100

        Don't render children in the profiles when there are no children.

    commit 7677c2ddefef321bbe74660471603d202a4ab66f
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Oct 27 19:50:35 2015 +0100

        Set the profiler on the ContextIndexSearcher.

    commit 74a4338c35dfed779adc025ec17cfd4d1c9f66f5
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Oct 27 19:50:01 2015 +0100

        Fix json rendering.

    commit 6674d5bebe187b0b0d8b424797606fdf2617dd27
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Oct 27 19:20:19 2015 +0100

        Revert "nocommit - profiling never enabled because setProfile() on ContextIndexSearcher never called"

        This reverts commit d3dc10949024342055f0d4fb7e16c7a43423bfab.

    commit d3dc10949024342055f0d4fb7e16c7a43423bfab
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 23 17:20:57 2015 -0400

        nocommit - profiling never enabled because setProfile() on ContextIndexSearcher never called

        Previously, it was enabled by using DefaultSearchContext as a third-party "proxy", but since
        the refactor to make it unit testable, setProfile() needs to be called explicitly.  Unfortunately,
        this is not possible because SearchService only has access to an IndexSearcher.  And it is not
        cast'able to a DefaultIndexSearcher.

    commit b9ba9c5d1f93b9c45e97b0a4e35da6f472c9ea53
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 23 16:27:00 2015 -0400

        [TESTS] Fix unit tests

    commit cf5d1e016b2b4a583175e07c16c7152f167695ce
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 23 16:22:34 2015 -0400

        Increment token after recording a rewrite

    commit b7d08f64034e498533c4a81bff8727dd8ac2843e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 23 16:14:09 2015 -0400

        Fix NPE if a top-level root doesn't have children

    commit e4d3b514bafe2a3a9db08438c89f0ed68628f2d6
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 23 16:05:47 2015 -0400

        Fix NPE when profiling is disabled

    commit 445384fe48ed62fdd01f7fc9bf3e8361796d9593
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 23 16:05:37 2015 -0400

        [TESTS] Fix integration tests

    commit b478296bb04fece827a169e7522df0a5ea7840a3
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 23 15:43:24 2015 -0400

        Move rewrites to their own section, remove reconciliation

        Big commit because the structural change affected a lot of the wrapping code.  Major changes:

        - Rewrites are now in their own section in the response
        - Reconciliation is gone...we don't attempt to roll the rewrites into the query tree structure
        - InternalProfileShardResults (plural) simply holds a Map<String, InternalProfileShardResult> and
        helps to serialize / ToXContent
        - InternalProfileShardResult (singular) is now the holder for all shard-level profiling details. Currently
        this includes query, collectors and rewrite.  In the future it would hold suggest, aggs, etc
        - When the user requests the profiled results, they get back a Map<String, ProfileShardResult>
        instead of doing silly helper methods to convert to maps, etc
        - Shard details are baked into a string instead of serializing the object

    commit 24819ad094b208d0e94f17ce9c3f7c92f7414124
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Oct 23 10:25:38 2015 -0400

        Make Profile results immutable by removing relative_time

    commit bfaf095f45fed74194ef78160a8e5dcae1850f9e
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 23 10:54:59 2015 +0200

        Add nocommits.

    commit e9a128d0d26d5b383b52135ca886f2c987850179
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 23 10:39:37 2015 +0200

        Move all profile-related classes to the same package.

    commit f20b7c7fdf85384ecc37701bb65310fb8c20844f
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 23 10:33:14 2015 +0200

        Reorganize code a bit to ease unit testing of ProfileCollector.

    commit 3261306edad6a0c70f59eaee8fe58560f61a75fd
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 18:07:28 2015 +0200

        Remove irrelevant nocommit.

    commit a6ac868dad12a2e17929878681f66dbd0948d322
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 18:06:45 2015 +0200

        Make CollectorResult's reason a free-text field to ease bw compat.

    commit 5d0bf170781a950d08b81871cd1e403e49f3cc12
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 16:50:52 2015 +0200

        Add unit tests for ProfileWeight/ProfileScorer.

    commit 2cd88c412c6e62252504ef69a59216adbb574ce4
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 15:55:17 2015 +0200

        Rename InternalQueryProfiler to Profiler.

    commit 84f5718fa6779f710da129d9e0e6ff914fd85e36
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 15:53:58 2015 +0200

        Merge InternalProfileBreakdown into ProfileBreakdown.

    commit 135168eaeb8999c8117ea25288104b0961ce9b35
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 13:56:57 2015 +0200

        Make it possible to instantiate a ContextIndexSearcher without SearchContext.

    commit 5493fb52376b48460c4ce2dedbe00cc5f6620499
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 11:53:29 2015 +0200

        Move ProfileWeight/Scorer to their own files.

    commit bf2d917b9dae3b32dfc29c35a7cac4ccb7556cce
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 11:38:24 2015 +0200

        Fix bug that caused phrase queries to fail.

    commit b2bb0c92c343334ec1703a221af24a1b55e36d53
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 11:36:17 2015 +0200

        Parsing happens on the coordinating node now.

    commit 416cabb8621acb5cd8dfa77374fd23e428f52fe9
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 11:22:17 2015 +0200

        Fix compilation (in particular remove guava deps).

    commit f996508645f842629d403fc2e71c1890c0e2cac9
    Merge: 4616a25 bc3b91e
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Thu Oct 22 10:44:38 2015 +0200

        Merge branch 'master' into query_profiler

    commit 4616a25afffe9c24c6531028f7fccca4303d2893
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Oct 20 12:11:32 2015 -0400

        Make Java Count API compatible with profiling

    commit cbfba74e16083d719722500ac226efdb5cb2ff55
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Oct 20 12:11:19 2015 -0400

        Fix serialization of profile query param, NPE

    commit e33ffac383b03247046913da78c8a27e457fae78
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Oct 20 11:17:48 2015 -0400

        TestSearchContext should return null Profiler instead of exception

    commit 73a02d69b466dc1a5b8a5f022464d6c99e6c2ac3
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Oct 19 12:07:29 2015 -0400

        [DOCS] Update docs to reflect new ID format

    commit 36248e388c354f954349ecd498db7b66f84ce813
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Oct 19 12:03:03 2015 -0400

        Use the full [node][index][shard] string as profile result ID

    commit 5cfcc4a6a6b0bcd6ebaa7c8a2d0acc32529a80e1
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Oct 15 17:51:40 2015 -0400

        Add failing test for phrase matching

        Stack trace generated:

        [2015-10-15 17:50:54,438][ERROR][org.elasticsearch.search.profile] shard [[JNj7RX_oSJikcnX72aGBoA][test][2]], reason [RemoteTransportException[[node_s0][local[1]][indices:data/read/search[phase/query]]]; nested: QueryPhaseExecutionException[Query Failed [Failed to execute main query]]; nested: AssertionError[nextPosition() called more than freq() times!]; ], cause [java.lang.AssertionError: nextPosition() called more than freq() times!
            at org.apache.lucene.index.AssertingLeafReader$AssertingPostingsEnum.nextPosition(AssertingLeafReader.java:353)
            at org.apache.lucene.search.ExactPhraseScorer.phraseFreq(ExactPhraseScorer.java:132)
            at org.apache.lucene.search.ExactPhraseScorer.access$000(ExactPhraseScorer.java:27)
            at org.apache.lucene.search.ExactPhraseScorer$1.matches(ExactPhraseScorer.java:69)
            at org.elasticsearch.common.lucene.search.ProfileQuery$ProfileScorer$2.matches(ProfileQuery.java:226)
            at org.apache.lucene.search.ConjunctionDISI$TwoPhaseConjunctionDISI.matches(ConjunctionDISI.java:175)
            at org.apache.lucene.search.ConjunctionDISI$TwoPhase.matches(ConjunctionDISI.java:213)
            at org.apache.lucene.search.ConjunctionDISI.doNext(ConjunctionDISI.java:128)
            at org.apache.lucene.search.ConjunctionDISI.nextDoc(ConjunctionDISI.java:151)
            at org.apache.lucene.search.ConjunctionScorer.nextDoc(ConjunctionScorer.java:62)
            at org.elasticsearch.common.lucene.search.ProfileQuery$ProfileScorer$1.nextDoc(ProfileQuery.java:205)
            at org.apache.lucene.search.Weight$DefaultBulkScorer.scoreAll(Weight.java:224)
            at org.apache.lucene.search.Weight$DefaultBulkScorer.score(Weight.java:169)
            at org.apache.lucene.search.BulkScorer.score(BulkScorer.java:39)
            at org.apache.lucene.search.IndexSearcher.search(IndexSearcher.java:795)
            at org.apache.lucene.search.IndexSearcher.search(IndexSearcher.java:509)
            at org.elasticsearch.search.query.QueryPhase.execute(QueryPhase.java:347)
            at org.elasticsearch.search.query.QueryPhase.execute(QueryPhase.java:111)
            at org.elasticsearch.search.SearchService.loadOrExecuteQueryPhase(SearchService.java:366)
            at org.elasticsearch.search.SearchService.executeQueryPhase(SearchService.java:378)
            at org.elasticsearch.search.action.SearchServiceTransportAction$SearchQueryTransportHandler.messageReceived(SearchServiceTransportAction.java:368)
            at org.elasticsearch.search.action.SearchServiceTransportAction$SearchQueryTransportHandler.messageReceived(SearchServiceTransportAction.java:365)
            at org.elasticsearch.transport.local.LocalTransport$2.doRun(LocalTransport.java:280)
            at org.elasticsearch.common.util.concurrent.AbstractRunnable.run(AbstractRunnable.java:37)
            at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
            at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
            at java.lang.Thread.run(Thread.java:745)

    commit 889fe6383370fe919aaa9f0af398e3040209e40b
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Oct 15 17:30:38 2015 -0400

        [DOCS] More docs

    commit 89177965d031d84937753538b88ea5ebae2956b0
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Oct 15 09:59:09 2015 -0400

        Fix multi-stage rewrites to recursively find most appropriate descendant rewrite

        Previously, we chose the first rewrite that matched.  But in situations where a query may
        rewrite several times, this won't build the tree correctly.  Instead we need to recurse
        down all the rewrites until we find the most appropriate "leaf" rewrite

        The implementation of this is kinda gross: we recursively call getRewrittenParentToken(),
        which does a linear scan over the rewriteMap and tries to find rewrites with a larger token
        value (since we know child tokens are always larger).  Can almost certainly find a better
        way to do this...

    commit 0b4d782b5348e5d03fd26f7d91bc4a3fbcb7f6a5
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Oct 14 19:30:06 2015 -0400

        [Docs] Documentation checkpoint

    commit 383636453f6610fcfef9070c21ae7ca11346793e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Sep 16 16:02:22 2015 -0400

        Comments

    commit a81e8f31e681be16e89ceab9ba3c3e0a018f18ef
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Sep 16 15:48:49 2015 -0400

        [TESTS] Ensure all tests use QUERY_THEN_FETCH, DFS does not profile

    commit 1255c2d790d85fcb9cbb78bf2a53195138c6bc24
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Sep 15 16:43:46 2015 -0400

        Refactor rewrite handling to handle identical rewrites

    commit 85b7ec82eb0b26a6fe87266b38f5f86f9ac0c44f
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Sep 15 08:51:14 2015 -0400

        Don't update parent when a token is added as root -- Fixes NPE

    commit 109d02bdbc49741a3b61e8624521669b0968b839
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Sep 15 08:50:40 2015 -0400

        Don't set the rewritten query if not profiling -- Fixes NPE

    commit 233cf5e85f6f2c39ed0a2a33d7edd3bbd40856e8
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Sep 14 18:04:51 2015 -0400

        Update tests to new response format

    commit a930b1fc19de3a329abc8ffddc6711c1246a4b15
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Sep 14 18:03:58 2015 -0400

        Fix serialization

    commit 69afdd303660510c597df9bada5531b19d134f3d
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Sep 14 15:11:31 2015 -0400

        Comments and cleanup

    commit 64e7ca7f78187875378382ec5d5aa2462ff71df5
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Sep 14 14:40:21 2015 -0400

        Move timing into dedicated class, add proper rewrite integration

    commit b44ff85ddbba0a080e65f2e7cc8c50d30e95df8e
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Sep 14 12:00:38 2015 -0400

        Checkpoint - Refactoring to use a token-based dependency tree

    commit 52cedd5266d6a87445c6a4cff3be8ff2087cd1b7
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Fri Sep 4 19:18:19 2015 -0400

        Need to set context profiling flag before calling queryPhase.preProcess

    commit c524670cb1ce29b4b3a531fa2bff0c403b756f46
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Sep 4 18:00:37 2015 +0200

        Reduce profiling overhead a bit.

        This removes hash-table lookups everytime we start/stop a profiling clock.

    commit 111444ff8418737082236492b37321fc96041e09
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Sep 4 16:18:59 2015 +0200

        Add profiling of two-phase iterators.

        This is useful for eg. phrase queries or script filters, since they are
        typically consumed through their two-phase iterator instead of the scorer.

    commit f275e690459e73211bc8494c6de595c0320f4c0b
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Sep 4 16:03:21 2015 +0200

        Some more improvements.

        I changed profiling to disable bulk scoring, since it makes it impossible to
        know where time is spent. Also I removed profiling of operations that are
        always fast (eg. normalization) and added nextDoc/advance.

    commit 3c8dcd872744de8fd76ce13b6f18f36f8de44068
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Sep 4 14:39:50 2015 +0200

        Remove println.

    commit d68304862fb38a3823aebed35a263bd9e2176c2f
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Sep 4 14:36:03 2015 +0200

        Fix some test failures introduced by the rebase...

    commit 04d53ca89fb34b7a21515d770c32aaffcc513b90
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Sep 4 13:57:35 2015 +0200

        Reconcile conflicting changes after rebase

    commit fed03ec8e2989a0678685cd6c50a566cec42ea4f
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Thu Aug 20 22:40:39 2015 -0400

        Add Collectors to profile results

        Profile response element has now been re-arranged so that everything is listed per-shard to
        facilitate grouping elements together.  The new `collector` element looks like this:

        ```
        "profile": {
          "shards": [
             {
                "shard_id": "keP4YFywSXWALCl4m4k24Q",
                "query": [...],
                "collector": [
                   {
                      "name": "MultiCollector",
                      "purpose": "search_multi",
                      "time": "16.44504400ms",
                      "relative_time": "100.0000000%",
                      "children": [
                         {
                            "name": "FilteredCollector",
                            "purpose": "search_post_filter",
                            "time": "4.556013000ms",
                            "relative_time": "27.70447437%",
                            "children": [
                               {
                                  "name": "SimpleTopScoreDocCollector",
                                  "purpose": "search_sorted",
                                  "time": "1.352166000ms",
                                  "relative_time": "8.222331299%",
                                  "children": []
                               }
                            ]
                         },
                         {
                            "name": "BucketCollector: [[non_global_term, another_agg]]",
                            "purpose": "aggregation",
                            "time": "10.40379400ms",
                            "relative_time": "63.26400829%",
                            "children": []
                         },
               ...
        ```

    commit 1368b495c934be642c00f6cbf9fc875d7e6c07ff
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Aug 19 12:43:03 2015 -0400

        Move InternalProfiler to profile package

    commit 53584de910db6d4a6bb374c9ebb954f204882996
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 18:34:58 2015 -0400

        Only reconcile rewrite timing when rewritten query is different from original

    commit 9804c3b29d2107cd97f1c7e34d77171b62cb33d0
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 16:40:15 2015 -0400

        Comments and cleanup

    commit 8e898cc7c59c0c1cc5ed576dfed8e3034ca0967f
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 14:19:07 2015 -0400

        [TESTS] Fix comparison test to ensure results sort identically

    commit f402a29001933eef29d5a62e81c8563f1c8d0969
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 14:17:59 2015 -0400

        Add note about DFS being unable to profile

    commit d446e08d3bc91cd85b24fc908e2d82fc5739d598
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 14:17:23 2015 -0400

        Implement some missing methods

    commit 13ca94fb86fb037a30d181b73d9296153a63d6e4
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 13:10:54 2015 -0400

        [TESTS] Comments & cleanup

    commit c76c8c771fdeee807761c25938a642612a6ed8e7
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 13:06:08 2015 -0400

        [TESTS] Fix profileMatchesRegular to handle NaN scores and nearlyEqual floats

    commit 7e7a10ecd26677b2239149468e24938ce5cc18e1
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 12:22:16 2015 -0400

        Move nearlyEquals() utility function to shared location

    commit 842222900095df4b27ff3593dbb55a42549f2697
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 18 12:04:35 2015 -0400

        Fixup rebase conflicts

    commit 674f162d7704dd2034b8361358decdefce1f76ce
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Aug 17 15:29:35 2015 -0400

        [TESTS] Update match and bool tests

    commit 520380a85456d7137734aed0b06a740e18c9cdec
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Aug 17 15:28:09 2015 -0400

        Make naming consistent re: plural

    commit b9221501d839bb24d6db575d08e9bee34043fc65
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Aug 17 15:27:39 2015 -0400

        Children need to be added to list after serialization

    commit 05fa51df940c332fbc140517ee56e849f2d40a72
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Aug 17 15:22:41 2015 -0400

        Re-enable bypass for non-profiled queries

    commit f132204d264af77a75bd26a02d4e251a19eb411d
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Aug 17 15:21:14 2015 -0400

        Fix serialization of QuerySearchResult, InternalProfileResult

    commit 27b98fd475fc2e9508c91436ef30624bdbee54ba
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Aug 10 17:39:17 2015 -0400

        Start to add back tests, refactor Java api

    commit bcfc9fefd49307045108408dc160774666510e85
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Aug 4 17:08:10 2015 -0400

        Checkpoint

    commit 26a530e0101ce252450eb23e746e48c2fd1bfcae
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Tue Jul 14 13:30:32 2015 -0400

        Add createWeight() checkpoint

    commit f0dd61de809c5c13682aa213c0be65972537a0df
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Mon Jul 13 12:36:27 2015 -0400

        checkpoint

    commit 377ee8ce5729b8d388c4719913b48fae77a16686
    Author: Zachary Tong <zacharyjtong@gmail.com>
    Date:   Wed Mar 18 10:45:01 2015 -0400

        checkpoint