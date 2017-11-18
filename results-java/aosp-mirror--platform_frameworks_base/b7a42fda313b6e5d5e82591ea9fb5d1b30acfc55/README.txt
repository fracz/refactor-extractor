commit b7a42fda313b6e5d5e82591ea9fb5d1b30acfc55
Author: Benjamin Franz <bfranz@google.com>
Date:   Thu Apr 23 15:33:32 2015 +0100

    Improve Accessibility behaviour in recents

    Currently accessibility services use the activity label that is also
    displayed in recents, which can be very confusing as it does not
    represent necessarily represent the app that the recent item is
    running. To improve this we use a combination of the application label
    and the activity label. The application label is also badged to
    distinguish between different users.

    Bug: 19688800
    Change-Id: Ibead6c87767649aa11cf2fe086924a4b69bf187c