commit 976ca034dfaa85723329d87bc5386f5ee8e28a79
Author: SteveJobzniak <SteveJobzniak@users.noreply.github.com>
Date:   Thu May 25 20:58:31 2017 +0200

    Client: Massively improved cookie efficiency

    This is what I created the new StorageHandler callback system for!

    First, some backstory. One of the design goals of the "StorageHandler"
    was to be completely detached from having any "parents", so that it
    would be easily re-usable in other projects. Being decoupled has some
    drawbacks, such as classes not knowing about the state of each other.
    So the "StorageHandler" knew when IT was about to close down, but it had
    NO IDEA that our "Instagram Client" even EXISTS. It was fully decoupled.

    Therefore, we used the "Client" class to write the latest cookies to
    the storage. And since the Client could never know when the storage
    would be gone, we made it write the cookies to storage after EVERY
    request just to be sure the latest cookies were always stored.

    There was some caching in the storage handler, to prevent pointless
    writes if the new cookies hadn't changed since last time. But Instagram
    constantly varies their "expiration" time of cookies on almost every
    request, which meant that there were still tons of cookie writing to
    database. Ouch!

    Now, we use the brand new storage event callback system instead. So the
    "StorageHandler" is still cleanly detached from the rest of the system,
    but now we can act based on its callbacks!

    When the "current user storage is closing" (which happens when the
    script exits or when the active user is going to be switched to
    somebody else), we tell the client to save all cookies to storage.

    The result? We now only write the cookies to storage A SINGLE TIME per
    user session, at the END of the session. Extremely efficient!

    Enjoy this massive improvement! :-)