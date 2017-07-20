commit a89cef8e396c31e7fd84be12257d93503bff51d7
Author: epriestley <git@epriestley.com>
Date:   Sun May 20 14:46:01 2012 -0700

    Remove PHID database, add Harbormaster database

    Summary:
      - We currently write every PHID we generate to a table. This was motivated by two concerns:
        - **Understanding Data**: At Facebook, the data was sometimes kind of a mess. You could look at a random user in the ID tool and see 9000 assocs with random binary data attached to them, pointing at a zillion other objects with no idea how any of it got there. I originally created this table to have a canonical source of truth about PHID basics, at least. In practice, our data model has been really tidy and consistent, and we don't use any of the auxiliary data in this table (or even write it). The handle abstraction is powerful and covers essentially all of the useful data in the app, and we have human-readable types in the keys. So I don't think we have a real need here, and this table isn't serving it if we do.
        - **Uniqueness**: With a unique key, we can be sure they're unique, even if we get astronomically unlucky and get a collision. But every table we use them in has a unique key anyway. So we actually get pretty much nothing here, except maybe some vague guarantee that we won't reallocate a key later if the original object is deleted. But it's hard to imagine any install will ever have a collision, given that the key space is 36^20 per object type.
      - We also currently use PHIDs and Users in tests sometimes. This is silly and can break (see D2461).
      - Drop the PHID database.
      - Introduce a "Harbormaster" database (the eventual CI tool, after Drydock).
      - Add a scratch table to the Harbormaster database for doing unit test meta-tests.
      - Now, PHID generation does no writes, and unit tests are isolated from the application.
      - @csilvers: This should slightly improve the performance of the large query-bound tail in D2457.

    Test Plan: Ran unit tests. Ran storage upgrade.

    Reviewers: btrahan, vrana, jungejason

    Reviewed By: btrahan

    CC: csilvers, aran, nh, edward

    Differential Revision: https://secure.phabricator.com/D2466