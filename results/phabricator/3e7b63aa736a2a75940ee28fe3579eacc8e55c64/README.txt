commit 3e7b63aa736a2a75940ee28fe3579eacc8e55c64
Author: epriestley <git@epriestley.com>
Date:   Wed Mar 22 06:50:51 2017 -0700

    Add a <reviewer, revision> key to the reviewers table

    Summary:
    Ref T10967. I'm not 100% sure we need this, but the old edge table had it and I recall an issue long ago where not having this key left us with a bad query plan.

    Our data doesn't really provide a way to test this key (we have many revisions and few reviewers, so the query planner always uses revision keys), and building a convincing test case would take a while (lipsum needs some improvements to add reviewers). But in the worst case this key is mostly useless and wastes a few MB of disk space, which isn't a big deal.

    So I can't conclusively prove that this key does anything to the dashboard query, but the migration removed it and I'm more comfortable keeping it so I'm not worried about breaking stuff.

    At the very least, MySQL does select this key in the query plan when I do a "Reviewers:" query explicitly so it isn't //useless//.

    Test Plan: Ran `bin/storage upgrade`, ran dashboard query, the query plan didn't get any worse.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10967

    Differential Revision: https://secure.phabricator.com/D17532