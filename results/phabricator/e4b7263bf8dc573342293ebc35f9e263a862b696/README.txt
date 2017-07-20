commit e4b7263bf8dc573342293ebc35f9e263a862b696
Author: Bob Trahan <btrahan@phacility.com>
Date:   Thu Mar 26 12:24:29 2015 -0700

    Conpherence - Differentiate audience of Threads/Rooms with icon

    Summary:
    Fixes T7629 plus an un filed bug that's breaking creating new threads since we need to add participants EVEN EARLIER than we were doing it now that policy is actually enforced.

    Back to the main thrust of this, there is one UI corner case - in the main view if you go from 1:1 to 1:1:1 (i.e. add a 3rd recipient, or Nth in a row) the icon only updates on page reload. I figure this will get sorted out at a later refactor as we make the client better / share more code with durable column.

    One other small behavioral oddity is in the main view sometime we start loading with no conpherence. in that case, rather than show some incorrect icon, we show no icon (and "no title") and then things change at load. Seems okay-ish.

    Finally, @chad - the CSS is a very work-man-like "use the built in stuff you can specify from PHP" so I'm sure it needs some love.

    Test Plan: made all sorts of rooms and threads and liked the icons. noted smooth loading action as i switched around

    Reviewers: chad, epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, chad, epriestley

    Maniphest Tasks: T7629

    Differential Revision: https://secure.phabricator.com/D12163