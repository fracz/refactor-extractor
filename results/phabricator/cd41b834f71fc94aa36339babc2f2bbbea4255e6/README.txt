commit cd41b834f71fc94aa36339babc2f2bbbea4255e6
Author: epriestley <git@epriestley.com>
Date:   Mon Feb 18 09:44:43 2013 -0800

    Improve Diviner linking

    Summary:
    Do this somewhat reasonably:

      - For links to the same documentation book (the common case), go look up that the thing you're linking to actualy exists. If it doesn't, render a <span> which we can make have a red background and warn about later.
      - For links to some other book, just generate a link and hope it hits something. We can improve and augment this later.
      - For non-documentation links (links in comments, e.g.) just generate a query link into the Diviner app. We'll do a query and figure out where to send the user after they click the link. We could pre-resolve these later.

    Test Plan: Generated documentation, saw it build mostly-correct links when objects were referenced correctly. Used preview to generate various `@{x:y|z}` things and made sure they ended up reasonable-looking.

    Reviewers: chad

    Reviewed By: chad

    CC: aran

    Maniphest Tasks: T988

    Differential Revision: https://secure.phabricator.com/D5001