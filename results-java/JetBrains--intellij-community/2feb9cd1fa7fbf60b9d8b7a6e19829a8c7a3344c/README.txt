commit 2feb9cd1fa7fbf60b9d8b7a6e19829a8c7a3344c
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Tue Aug 2 12:44:59 2016 +0300

    refactor git & hg amend option layout & class hierarchy

    Make a provider of "Amend Commit" checkbox instead of existing
    additional component hierarchy which has
    unclear layouting hard to be customized in clients.

    Use GridBag instead of GridBagConstraints.