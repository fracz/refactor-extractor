commit 610ce078fb3c84c47d6d32aff7d77ba850e28f9d
Author: Robert Muir <rmuir@apache.org>
Date:   Wed Nov 5 15:48:51 2014 -0500

    Upgrade master to lucene 5.0 snapshot

    This has a lot of improvements in lucene, particularly around memory usage, merging, safety, compressed bitsets, etc.

    On the elasticsearch side, summary of the larger changes:

        API changes: postings API became a "pull" rather than "push", collector API became per-segment, etc.
        packaging changes: add lucene-backwards-codecs.jar as a dependency.
        improvements to boolean filtering: especially ensuring it will not be slow for SparseBitSet.
        use generic BitSet api in plumbing so that concrete bitset type is an implementation detail.
        use generic BitDocIdSetFilter api for dedicated bitset cache, so there is type safety.
        changes to support atomic commits
        implement Accountable.getChildResources (detailed memory usage API) for fielddata, etc
        change handling of IndexFormatTooOld/New, since they no longer extends CorruptIndexException

    Closes #8347.

    Squashed commit of the following:

    commit d90d53f5f21b876efc1e09cbd6d63c538a16cd89
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Nov 5 21:35:28 2014 +0100

        Make default codec/postings/docvalues format constants

    commit cb66c22c71cd304a36e7371b199a8c279908ae37
    Merge: d4e2f6d ad4ff43
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Nov 5 11:41:13 2014 -0500

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit d4e2f6dfe767a5128c9b9ae9e75036378de08f47
    Merge: 4e5445c 4111d93
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Nov 5 06:26:32 2014 -0500

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit 4e5445c775f580730eb01360244e9330c0dc3958
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 16:19:19 2014 -0500

        FixedBitSet -> BitSet

    commit 9887ea73e8b857eeda7f851ef3722ef580c92acf
    Merge: 1bf8894 fc84666
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 15:26:25 2014 -0500

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit 1bf8894430de3e566d0dc5623b0cc28b0d674ebb
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 15:22:51 2014 -0500

        remove nocommit

    commit a9c2a2259ff79c69bae7806b64e92d5f472c18c8
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 13:48:43 2014 -0500

        turn jenkins red again

    commit 067baaaa4d52fce772c81654dcdb5051ea79139f
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 13:18:21 2014 -0500

        unzip from stream

    commit 82b6fba33d362aca2313cc0ca495f28f5ebb9260
    Merge: b2214bb 6523cd9
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 13:10:59 2014 -0500

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit b2214bb093ec2f759003c488c3c403c8931db914
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 13:09:53 2014 -0500

        go back to my URL until we can figure out what is up with jenkins

    commit e7d614172240175a51f580aeaefb6460d21cede9
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 10:52:54 2014 -0500

        try this jenkins

    commit 337a3c7704efa7c9809bf373152d711ee55f876c
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Tue Nov 4 16:17:49 2014 +0100

        Rename temp-files under lock to prevent metadata reads while renaming

    commit 77d5ba80d0a76efa549dd753b9f114b2f2d2d29c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 10:07:11 2014 -0500

        continue to treat too-old/too-new as corruption for now

    commit 98d0fd2f4851bc50e505a94ca592a694d502c51c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Nov 4 09:24:21 2014 -0500

        fix last nocommit

    commit 643fceed66c8caf22b97fc489d67b4a2a90a1a1c
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Tue Nov 4 14:46:17 2014 +0100

        remove NoSuchDirectoryException

    commit 2e43c4feba05cfaf451df70f946c0930cbcc4557
    Merge: 93826e4 8163107
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Tue Nov 4 14:38:00 2014 +0100

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit 93826e4d56a6a97c2074669014af77ff519bde63
    Merge: 7f10129 44e24d3
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Tue Nov 4 12:54:27 2014 +0100

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

        Conflicts:
            src/main/java/org/elasticsearch/index/store/DistributorDirectory.java
            src/main/java/org/elasticsearch/index/store/Store.java
            src/main/java/org/elasticsearch/indices/recovery/RecoveryStatus.java
            src/test/java/org/elasticsearch/index/store/DistributorDirectoryTest.java
            src/test/java/org/elasticsearch/index/store/StoreTest.java
            src/test/java/org/elasticsearch/indices/recovery/RecoveryStatusTests.java

    commit 7f10129364623620575c109df725cf54488b3abb
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Nov 4 11:32:24 2014 +0100

        Fix TopHitsAggregator to not ignore the top-level/leaf collector split.

    commit 042fadc8603b997bdfdc45ca44fec70dc86774a6
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Nov 4 11:31:20 2014 +0100

        Remove MatchDocIdSet in favor of DocValuesDocIdSet.

    commit 7d877581ff5db585a674c95ac391ac78a0282826
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Tue Nov 4 11:10:08 2014 +0100

        Make the and filter use the cost API.

        Lucene 5 ensured that cost() can safely be used, and this will have the benefit
        that the order in which filters are specified is not important anymore (only
        for slow random-access filters in practice).

    commit 78f1718aa2cd82184db7c3a8393e6215f43eb4a8
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 23:55:17 2014 -0500

        fix previous eclipse import braindamage

    commit 186c40e9258ce32f22a9a714ab442a310b6376e0
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 22:32:34 2014 -0500

        allow child queries to exhaust iterators again

    commit b0b1271305e1b6d0c4c4da51a3c54df1aa5c0605
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Mon Nov 3 14:50:44 2014 -0800

        Fix nocommit for mapping output.  index_options will not be printed if
        the field is not indexed.

    commit ba223eb85e399c9620a347a983e29bf703953e7a
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Mon Nov 3 14:07:26 2014 -0800

        Remove no commit for chinese analyzer provider.  We should have a
        separate issue to address not using this provider on new indexes.

    commit ca554b03c4471797682b2fb724f25205cf040c4a
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Mon Nov 3 13:41:59 2014 -0800

        Fix stop tests

    commit de67c4653ec47dee9c671390536110749d2bb05f
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Mon Nov 3 12:51:17 2014 -0800

        Remove analysis nocommits, switching over to Lucene43*Filters for
        backcompat

    commit 50cae9bec72c25c33a1ab8a8931bccb3355171e2
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 15:32:25 2014 -0500

        add ram accounting and TODO lazy-loading (its no worse than master, can be a followup improvement) for suggesters

    commit 7a7f0122f138684b312d0f0b03dc2a9c16c15f9c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 15:11:26 2014 -0500

        bump lucene version

    commit cd0cae5c35e7a9e049f49ae45431f658fb86676b
    Merge: 446bc09 3c72073
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 14:49:05 2014 -0500

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit 446bc09b4e8bf4602d3c252b53ddaa0da65cce2f
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 14:46:30 2014 -0500

        remove hack

    commit a19d85a968d82e6d00292b49630ef6ff2dbf2f32
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 12:53:11 2014 -0500

        dont create exceptions with circular references on corruption (will open a PR for this)

    commit 0beefb9e821d97c37e90ec556d81ac7b00369b8a
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 11:47:14 2014 -0500

        temporarily add craptastic detector for this horrible bug

    commit e9f2d298bff75f3d1591f8622441e459c3ce7ac3
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 10:56:01 2014 -0500

        add nocommit

    commit e97f1d50a91a7129650b8effc7a9ecf74ca0569a
    Merge: c57a3c8 f1f50ac
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 10:12:12 2014 -0500

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit c57a3c8341ed61dca62eaf77fad6b8b48aeb6940
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 10:11:46 2014 -0500

        fix nocommit

    commit dd0e77e4ec07c7011ab5f6b60b2ead33dc2333d2
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Nov 3 09:54:09 2014 -0500

        nocommit -> TODO, this is in much more places in the codebase, bigger issue

    commit 3cc3bf56d72d642059f8fe220d6f2fed608363e9
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Sat Nov 1 23:59:17 2014 -0700

        Remove nocommit and awaitsfix for edge ngram filter test.

    commit 89f115245155511c0fbc0d5ee62e63141c3700c1
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Sat Nov 1 23:57:44 2014 -0700

        Fix EdgeNGramTokenFilter logic for version <= 4.3, and fixed instanceof
        checks in corresponding tests to correctly check for reverse filter when
        applicable.

    commit 112df869cd199e36aab0e1a7a288bb1fdb2ebf1c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sun Nov 2 00:08:30 2014 -0400

        execute geo disjoint query/filter as intersects

    commit e5061273cc685f1252e9a3a9ae4877ec9bce7752
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Nov 1 22:58:59 2014 -0400

        remove chinese analyzer from docs

    commit ea1af11b8978fcc551f198e24fe21d52806993ef
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Nov 1 22:29:00 2014 -0400

        fix ram accounting bug

    commit 53c0a42c6aa81aa6bf81d3aa77b95efd513e0f81
    Merge: e3bcd3c 6011a18
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Nov 1 22:16:29 2014 -0400

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit e3bcd3cc07a4957e12c7b3affc462c31290a9186
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Nov 1 22:15:01 2014 -0400

        fix url-email back compat (thanks ryan)

    commit 91d6b096a96c357755abee167098607223be1aad
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Nov 1 22:11:26 2014 -0400

        bump lucene version

    commit d2bb9568df72b37ec7050d25940160b8517394bc
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Nov 1 20:33:07 2014 -0400

        remove nocommit

    commit 1d049c471e19e5c457262c7399c5bad9e023b2e3
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Nov 1 20:28:58 2014 -0400

        fix eclipse to group org/com imports together: without this, its madness

    commit 09d8c1585ee99b6e63be032732c04ef6fed84ed2
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Nov 1 14:27:41 2014 -0400

        remove nocommit, if you dont liek it, print assembly and tell me how it can be better

    commit 8a6a294313fdf33b50c7126ec20c07867ecd637c
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 31 20:01:55 2014 +0100

        Remove deprecated usage of DocIdSets.newDocIDSet.

    commit 601bee60543610558403298124a84b1b3bbd1045
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 31 14:13:18 2014 -0400

        maybe one of these zillions of annotations will stop thread leaks

    commit 9d3f69abc7267c5e455aefa26db95cb554b02d62
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 31 14:05:39 2014 -0400

        fix some analysis nocommits

    commit 312e3a29c77214b8142d21c33a6b2c2b151acf9a
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 31 18:28:45 2014 +0100

        Remove XConstantScoreQuery/XFilteredQuery/ApplyAcceptedDocsFilter.

    commit 5a0cb9f8e167215df7f1b1fad11eec6e6c74940f
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 31 17:06:45 2014 +0100

        Fix misleading documentation of DocIdSets.toCacheable.

    commit 8b4ef2b5b476fff4c79c0c2a0e4769ead26cf82b
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 31 17:05:59 2014 +0100

        Fix CustomRandomAccessFilterStrategy to override the right method.

    commit d7a9a407a615987cfffc651f724fbd8795c9c671
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 31 16:21:35 2014 +0100

        Better handle the special case when there is a single SHOULD clause.

    commit 648ad389f07e92dfc451f345549c9841ba5e4c9a
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 31 15:53:38 2014 +0100

        Cut over XBooleanFilter to BitDocIdSet.Builder.

        The idea is similar to what happened to Lucene's BooleanFilter.

        Yet XBooleanFilter is a bit more sophisticated and I had to slightly
        change the way it is implemented in order to make it work. The main difference
        with before is that slow filters are now applied lazily, so eg. if you have 3
        MUST clauses, two with a fast iterator and the third with a slow iterator, the
        previous implementation used to apply the fast iterators first and then only
        check the slow filter for bits which were set in the bit set. Now we are
        computing a bit set based on the fast must clauses and then basically returning
        a BitsFilteredDocIdSet.wrap(bitset, slowClause).

        Other than that, BooleanFilter still uses the bitset optimizations when or-ing
        and and-ind filters.

        Another improvement is that BooleanFilter is now aware of the cost API.

    commit b2dad312b4bc9f931dc3a25415dd81c0d9deee08
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 31 10:18:53 2014 -0400

        clear nocommit

    commit 4851d2091e744294336dfade33906c75fbe695cd
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 15:15:16 2014 +0100

        cut over to RoaringDocIdSet

    commit ca6aec24a901073e65ce4dd6b70964fd3612409e
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 14:57:30 2014 +0100

        make nocommit more explicit

    commit d0742ee2cb7a6c48b0bbb31580b7fbcebdb6ec40
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 31 09:55:24 2014 -0400

        fix standardtokenizer nocommit

    commit 7d6faccafff22a86af62af0384838391d46695ca
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 14:54:08 2014 +0100

        fix compilation

    commit a038a405c1ff6458ad294e6b5bc469e622f699d0
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 14:53:43 2014 +0100

        fix compilation

    commit 30c9e307b1f5d80e2deca3392c0298682241207f
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 14:52:35 2014 +0100

        fix compilation

    commit e5139bc5a0a9abd2bdc6ba0dfbcb7e3c2e7b8481
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 31 09:52:16 2014 -0400

        clear nocommit here

    commit 85dd2cedf7a7994bed871ac421cfda06aaf5c0a5
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 14:46:17 2014 +0100

        fix CompletionPostingsFormatTest

    commit c0f3781f616c9b0ee3b5c4d0998810f595868649
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 31 09:38:00 2014 -0400

        add tests for these analyzers

    commit 51f9999b4ad079c283ae762c862fd0e22d00445f
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 14:10:26 2014 +0100

        remove nocommit - this is not an issue

    commit fd1388fa03e622b0738601c8aeb2dbf7949a6dd2
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Fri Oct 31 14:07:01 2014 +0100

        Remove redundant null check

    commit 3d6dd51b0927337ba941a235446b22e8cd500dc3
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Fri Oct 31 14:01:37 2014 +0100

        Removed the work around to prevent p/c error when invoking #iterator() twice, because the custom query filter wrapper now doesn't transform the result to a cache doc id set any more.

        I think the transforming to a cachable doc id set in CustomQueryWrappingFilter isn't needed at all, because we use the DocIdSet only once and because of that is just slowed things down.

    commit 821832a537e00cd1216064b379df3e01d2911d3a
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 13:54:33 2014 +0100

        one more nocommit

    commit 77eb9ea4c4ea50afb2680c29682ddcb3851a9d4f
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Fri Oct 31 13:52:29 2014 +0100

        Remove cast

    commit a400573c034ed602221f801b20a58a9186a06eae
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 13:49:24 2014 +0100

        fix stop filter

    commit 51746087cf8ec34c4d20aa05ba8dbff7b3b43eec
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 13:21:36 2014 +0100

        fix changed semantics of FBS.nextSetBit to check for NO_MORE_DOCS

    commit 8d0a4e2511310f1293860823fe3ba80ac771bbe3
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 31 08:13:44 2014 -0400

        do the bogus cast differently

    commit 46a5cc5732dea096c0c80ae5ce42911c9c51e44e
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Fri Oct 31 13:00:16 2014 +0100

        I hate it but P/C now passes

    commit 580c0c2f82bbeacf217e594f22312b11d1bdb839
    Merge: a9d3c00 1645434
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 31 06:54:31 2014 -0400

        fix nocommit/classcast

    commit a9d3c004d62fe04989f49a897e6ff84973c06eb9
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Fri Oct 31 08:49:31 2014 +0100

        Update TODO.

    commit aa75af0b407792aeef32017f03a6f442ed970baa
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu Oct 30 19:18:25 2014 -0400

        clear obselete nocommits from lucene bump

    commit d438534cf41fcbe2d88070e2f27c994625e082c2
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu Oct 30 18:53:20 2014 -0400

        throw classcastexception when ES abuses regular filtercache for nested docs

    commit 2c751f3a8feda43ec127c34769b069de21f3d16f
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu Oct 30 18:31:34 2014 -0400

        bump lucene revision, fix tests

    commit d6ef7f6304ae262bf6228a7d661b2a452df332be
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 22:37:58 2014 +0100

        fix merge problems

    commit de9d361f88a9ce6bb3fba85285de41f223c95767
    Merge: 41f6aab f6b37a3
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 22:28:59 2014 +0100

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

        Conflicts:
            pom.xml
            src/main/java/org/elasticsearch/Version.java
            src/main/java/org/elasticsearch/gateway/local/state/meta/MetaDataStateFormat.java

    commit 41f6aab388aa80c40b08a2facab2617576203a0d
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 17:48:46 2014 +0100

        fix potiential NPE

    commit c4428b12e1ae838b91e847df8b4a8be7f49e10f4
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 17:38:46 2014 +0100

        don't advance iterator in a match(doc) method

    commit 28ab948e99e3ea4497c9b1e468384806ba7e1790
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 17:34:58 2014 +0100

        don't advance iterator in a match(doc) method

    commit eb0f33f6634fadfcf4b2bf7327400e568f0427bb
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 16:55:54 2014 +0100

        fix GeoUtilsTest

    commit 7f711fe3eaf73b6c2268cf42d5a41132a61ad831
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 16:43:16 2014 +0100

        Use a dedicated default index option if field type is not indexed by default

    commit 78e3f37ab779e3e1b25b45a742cc86ab5f975149
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu Oct 30 10:56:14 2014 -0400

        disable this test with AwaitsFix to reduce noise

    commit 9a590f563c8e03a99ecf0505c92d12d7ab20d11d
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 09:38:49 2014 +0100

        fix lucene version

    commit abe3ca1d8bb6b5101b545198f59aec44bacfa741
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Thu Oct 30 09:35:05 2014 +0100

        fix AnalyzingCompletionLookupProvider to wrok with new codec API

    commit 464293b245852d60bde050c6d3feb5907dcfbf5f
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu Oct 30 00:26:00 2014 -0400

        don't try to write stuff to tests class directory

    commit 031cc6c19f4fe4423a034b515f77e5a0e282a124
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu Oct 30 00:12:36 2014 -0400

        AwaitsFix these known issues to reduce noise

    commit 4600d51891e35847f2d344247d6f915a0605c0d1
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu Oct 30 00:06:53 2014 -0400

        openbitset lives on

    commit 8492bae056249e2555d24acd55f1046b66a667c4
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 29 23:42:54 2014 -0400

        fixes for filter tests

    commit 31f24ce4efeda31f97eafdb122346c7047a53bf2
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 29 23:12:38 2014 -0400

        don't use fieldcache

    commit 8480789942fdff14a6d2b2cd8134502fe62f20c8
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 29 23:04:29 2014 -0400

        ancient index no longer supported

    commit 02e78dc7ebdd827533009f542582e8db44309c57
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 23:37:02 2014 +0100

        fix more tests

    commit ff746c6df23c50b3f3ec24922413b962c8983080
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 23:08:19 2014 +0100

        fix all mapper

    commit e4fb84b517107b25cb064c66f83c9aa814a311b2
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 22:55:54 2014 +0100

        fix distributor tests and cut over to FileStore API

    commit 20c850e2cfe3210cd1fb9e232afed8d4ac045857
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 22:42:18 2014 +0100

        use DOCS_ONLY if index=true and current options == null

    commit 44169c108418413cfe51f5ce23ab82047463e4c2
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 22:33:36 2014 +0100

        Fix index=yes|no settings in mappers

    commit a3c5f77987461a18121156ed345d42ded301c566
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 21:51:41 2014 +0100

        fix several field mappers conversion from setIndexed to indexOptions

    commit df84d736908e88a031d710f98e222be68ae96af1
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 21:33:35 2014 +0100

        fix SourceFieldMapper to be not indexed

    commit b2bf01d12a8271a31fb2df601162d0e89924c8f5
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 21:23:08 2014 +0100

        Cut over to .liv files in store and corruption tests

    commit 619004df436f9ef05d24bef1b6a7f084c6b0ad75
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 17:05:52 2014 +0100

        fix more tests

    commit b7ed653a8b464de446e00456bce0a89e47627c38
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 16:19:08 2014 +0100

        [STORE] Add dedicated method to write temporary files

        Recovery writes temporary files which might not end up in the
        right distributor directories today. This commit adds a dedicated
        API that allows specifying the target file name in order to create the
        tempoary file in the correct directory.

    commit 7d574659f6ae04adc2b857146ad0d8d56ca66f12
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 29 10:28:49 2014 -0400

        add some leniency to temporary bogus method

    commit f97022ea7c2259f7a5cf97d924c59ed75ab65b32
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 29 10:24:17 2014 -0400

        fix MultiCollector bug

    commit b760533128c2b4eb10ad76e9689ef714293dd819
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 14:56:08 2014 +0100

        CheckIndex is now closeable we need to close it

    commit 9dae9fb6d63546a6c2427be2a2d5c8358f5b1934
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 14:45:11 2014 +0100

        s/Lucene51/Lucene50

    commit 7aea9b86856a8c1b06a08e7c312ede1168af1287
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 14:42:30 2014 +0100

        fix BloomFilterPostingsFormat

    commit 16fea6fe842e88665d59cc091e8224e8dc6ce08c
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 14:41:16 2014 +0100

        fix some codec format issues

    commit 3d77aa97dd2c4012b63befef3f2ba2525965e8a6
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 14:30:43 2014 +0100

        fix CodecTests

    commit 6ef823b1fde25657438ace1aabd9d552d6ae215e
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 14:26:47 2014 +0100

        make it compile

    commit 9991eee1fe99435118d4dd42b297ffc83fce5ec5
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 29 09:12:43 2014 -0400

        add an ugly hack for TopHitsAggregator for now

    commit 03e768a01fcae6b1f4cb50bcceec7d42977ac3e6
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Wed Oct 29 14:01:02 2014 +0100

        cut over ES090PostingsFormat

    commit 463d281faadb794fdde3b469326bdaada25af048
    Merge: 0f8740a 8eac79c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 29 08:30:36 2014 -0400

        Merge branch 'master' into enhancement/lucene_5_0_upgrade

    commit 0f8740a782455a63524a5a82169f6bbbfc613518
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 29 01:00:15 2014 -0400

        fix/hack remaining filter and analysis issues

    commit df534488569da13b31d66e581456dfd4b55156b9
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Oct 28 23:11:47 2014 -0400

        fix ngrams / openbitset usage

    commit 11f5dc3b9887f4da80a0fa1818e1350b30599329
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Oct 28 22:42:44 2014 -0400

        hack over sort comparators

    commit 4ebdc754350f512596f6a02770d223e9f5f7975a
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Oct 28 21:27:07 2014 -0400

        compiler errors < 100

    commit 2d60c9e29de48ccb0347dd87f7201f47b67b83a0
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Oct 28 03:13:08 2014 -0400

        clear some nocommits around ram usage

    commit aaf47fe6c0aabcfb2581dd456fc50edf871da758
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Oct 27 12:27:34 2014 -0400

        migrate fieldinfo handling

    commit ef6ed6d15d8def71cd880d97249678136cd29fe3
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Oct 27 12:07:13 2014 -0400

        more simple fixes

    commit f475e1048ae697dd9da5bd9da445102b0b7bc5b3
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Oct 27 11:58:21 2014 -0400

        more fielddata ram accounting fixes

    commit 16b4239eaa9b4262df258257df4f31d39f28a3a2
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Mon Oct 27 16:47:32 2014 +0100

        add missing file

    commit 5b542fa2a6da81e36a0c35b8e891a1d8bc58f663
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Mon Oct 27 16:43:29 2014 +0100

        cut over completion posting formats - still some nocommits

    commit ecdea49404c4ec4e1b78fb54575825f21b4e096e
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Oct 27 11:21:09 2014 -0400

        fielddata accountable fixes

    commit d43da265718917e20c8264abd43342069198fe9c
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Mon Oct 27 16:19:53 2014 +0100

        cut over BloomFilterPostings to new API

    commit 29b192ba621c14820175775d01242162b88bd364
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Oct 27 10:22:51 2014 -0400

        fix more analyzers

    commit 74b4a0c5283e323a7d02490df469497c722780d2
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Oct 27 09:54:25 2014 -0400

        fix tests

    commit 554084ccb4779dd6b1c65fa7212ad1f64f3a6968
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Mon Oct 27 14:51:48 2014 +0100

        maintain supressed exceptions on CorruptIndexException

    commit cf882d9112c5e8ef1e9f2b0f800f7aa59001a4f2
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Mon Oct 27 14:47:17 2014 +0100

        commitOnClose=false

    commit ebb2a9189ab2f459b7c6c9985be610fd90dfe410
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Mon Oct 27 14:46:06 2014 +0100

        cut over indexwriter closeing in InternalEngine

    commit cd21b3d4706f0b562bd37792d077d60832aff65f
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Mon Oct 27 14:38:10 2014 +0100

        fix constant

    commit f93f900c4a1c90af3a21a4af5735a7536423fe28
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Oct 27 09:50:49 2014 -0400

        fix test

    commit a9a752940b1ab4699a6a08ba8b34afca82b843fe
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Mon Oct 27 09:26:18 2014 +0100

        Be explicit about the index options

    commit d9ee815babd030fa2ceaec9f467c105ee755bf6b
    Author: Simon Willnauer <simonw@apache.org>
    Date:   Sun Oct 26 20:03:44 2014 +0100

        cut over store and directory

    commit b3f5c8e39039dd8f5caac0c4dd1fc3b1116e64ca
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sun Oct 26 13:08:39 2014 -0400

        more test fixes

    commit 8842f2684e3606aae0860c27f7a4c53e273d47fb
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sun Oct 26 12:14:52 2014 -0400

        tests manual labor

    commit c43de5aec337919a3fdc3638406dff17fc80bc98
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sun Oct 26 11:04:13 2014 -0400

        BytesRef -> BytesRefBuilder

    commit 020c0d087a2f37566a1db390b0e044ebab030138
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Sun Oct 26 15:53:37 2014 +0100

        Moved over to BitSetFilter

    commit 48dd1b909e6c52cef733961c9ecebfe4f67109fe
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Sun Oct 26 15:53:11 2014 +0100

        Left over Collector api change in ScanContext

    commit 6ec248ef63f262bcda400181b838fd9244752625
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Sun Oct 26 15:47:40 2014 +0100

        Moved indexed() over to indexOptions != null or indexOptions == null

    commit 9937aebfd8546ae4bb652cd976b3b43ac5ab7a63
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Sun Oct 26 13:26:31 2014 +0100

        Fixed many compile errors. Mainly around the breaking Collector api change in 5.0.

    commit fec32c4abc0e3309cf34260c8816305a6f820c9e
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Oct 25 11:22:17 2014 -0400

        more easy fixes

    commit dab22531d801800d17a65dc7c9464148ce8ebffd
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Oct 25 09:33:41 2014 -0400

        more progress

    commit 414767e9a955010076b0497cc4f6d0c1850b48d3
    Author: Robert Muir <rmuir@apache.org>
    Date:   Sat Oct 25 06:33:17 2014 -0400

        more progress

    commit ad9d969fddf139a8830254d3eb36a908ba87cc12
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 24 14:28:01 2014 -0400

        current state of fun

    commit 464475eecb0be15d7d084135ed16051f76a7e521
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Oct 24 11:42:41 2014 -0400

        bump to 5.0 snapshot