commit 5c71da8cdb6cb6b36bdbf5b12da532eb48746d04
Author: epriestley <git@epriestley.com>
Date:   Tue Jan 27 14:52:09 2015 -0800

    Quicksand, an ignoble successor to Quickling

    Summary:
    Ref T2086. Ref T7014. With the persistent column, there is significant value in retaining chrome state through navigation events, because the user may have a lot of state in the chat window (scroll position, text selection, room juggling, partially entered text, etc). We can do this by capturing navigation events and faking them with Javascript.

    (This can also improve performance, albeit slightly, and I believe there are better approaches to tackle performance any problems which exist with the chrome in many cases).

    At Facebook, this system was "Photostream" in photos and then "Quickling" in general, and the technical cost of the system was //staggering//. I am loathe to pursue it again. However:

      - Browsers are less junky now, and we target a smaller set of browsers. A large part of the technical cost of Quickling was the high complexity of emulating nagivation events in IE, where we needed to navigate a hidden iframe to make history entries. All desktop browsers which we might want to use this system on support the History API (although this prototype does not yet implement it).
      - Javelin and Phabricator's architecture are much cleaner than Facebook's was. A large part of the technical cost of Quickling was inconsistency, inlined `onclick` handlers, and general lack of coordination and abstraction. We will have //some// of this, but "correctly written" behaviors are mostly immune to it by design, and many of Javelin's architectural decisions were influenced by desire to avoid issues we encountered building this stuff for Facebook.
      - Some of the primitives which Quickling required (like loading resources over Ajax) have existed in a stable state in our codebase for a year or more, and adoption of these primitives was trivial and uneventful (vs a huge production at Facebook).
      - My hubris is bolstered by recent success with WebSockets and JX.Scrollbar, both of which I would have assessed as infeasibly complex to develop in this project a few years ago.

    To these points, the developer cost to prototype Photostream was several weeks; the developer cost to prototype this was a bit less than an hour. It is plausible to me that implementing and maintaining this system really will be hundreds of times less complex than it was at Facebook.

    Test Plan:
    My plan for this and D11497 is:

      - Get them in master.
      - Some secret key / relatively-hidden preference activates the column.
      - Quicksand activates //only// when the column is open.
      - We can use column + quicksand for a long period of time (i.e., over the course of Conpherence v2 development) and hammer out the long tail of issues.
      - When it derps up, you just hide the column and you're good to go.

    Reviewers: btrahan, chad

    Reviewed By: chad

    Subscribers: epriestley

    Maniphest Tasks: T2086, T7014

    Differential Revision: https://secure.phabricator.com/D11507