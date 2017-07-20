commit 1a63938b334c902e10cf9602a379f99959077454
Author: epriestley <git@epriestley.com>
Date:   Tue Oct 16 09:44:43 2012 -0700

    Further improve various Phame UI things

    Summary:
      - Better icons and action order.
      - "Move Post" action.
      - (Bugfix) Allow multiple blogs to be set to not having custom domains.
      - Make "Write Post" skip the "select a blog" step when coming from a blog view.
      - Sort blog list on "Write Post".
      - Show messages when a post is a draft or not on a blog.

    Test Plan: Created posts, blogs, moved posts, preview/live'd posts, etc.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T1373

    Differential Revision: https://secure.phabricator.com/D3708