commit f8a51f6fcbec4dbc61bab3c5a06cd62971a4df43
Author: Ben Kwa <kenobi@google.com>
Date:   Tue Dec 22 14:03:30 2015 -0800

    Update grid items to look more like the mocks.

    - Create a new layout for directory items, which have a different
      layout (no thumbnail, size or mod_date).

    - Add drop shadows.

    Also refactor a few things in the DocumentHolder and child classes to
    make things more efficient and cleaner.

    BUG=24326989,26229570

    Change-Id: I05df52b071667190d4c4c671f50d25498383cdaa