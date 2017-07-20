commit 49366559af53f0d95f69d08025d68585dd54cfd8
Author: epriestley <git@epriestley.com>
Date:   Tue Mar 13 11:18:01 2012 -0700

    Various minor Audit UI improvements

    Summary:
      - Order audits by id desc so they tend to be in descending time order, like other content.
      - In audit tables without commit context (audit tool, home page) show commit descriptions.
      - Correctly hide pagers on "active" audit filter.
      - Make pagers work correctly on commit / audit views.

    Test Plan: Looked at homepage, audit, owners, differential. Paginated relevant interfaces.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran, epriestley

    Maniphest Tasks: T904

    Differential Revision: https://secure.phabricator.com/D1875