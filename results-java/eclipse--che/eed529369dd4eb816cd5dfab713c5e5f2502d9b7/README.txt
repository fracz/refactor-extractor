commit eed529369dd4eb816cd5dfab713c5e5f2502d9b7
Author: Vladyslav Zhukovskii <vzhukovskii@codenvy.com>
Date:   Tue Mar 1 17:47:08 2016 +0200

    Fetch module config directly from item reference

    Get children operation (on the server side, project service) is returning item reference with project configuration for the module if such exists. Client based on this information may faster construct module node instead of making additional request for the configuration.

    Note: temporary solution will be alive before new changes will be merged (related to new project type with server side vfs refactorings)

    Related issue: CHE-660

    Squashed commit of the following:

    commit e86d058795c7bd1cb30221b6e79b376d662ad191
    Merge: 32caa18 7b6bb02
    Author: Vladyslav Zhukovskii <vzhukovskii@codenvy.com>
    Date:   Tue Mar 1 17:42:55 2016 +0200

        Merge branch 'master' into CHE-660

    commit 32caa185e3cd18cbe71fa644a733abaaacc9562a
    Author: Vladyslav Zhukovskii <vzhukovskii@codenvy.com>
    Date:   Tue Mar 1 17:37:36 2016 +0200

        Fetch module config directly from item reference

    commit 04513ae3c58624028405e3b62edfacf7e13807ef
    Author: Dmitry Shnurenko <dshnurenko@codenvy.com>
    Date:   Tue Mar 1 17:06:12 2016 +0200

        CHE-660: Add java doc

    commit 20e99f884e434b726cd2066b4f3663b86c77e455
    Author: Dmitry Shnurenko <dshnurenko@codenvy.com>
    Date:   Tue Mar 1 16:57:56 2016 +0200

        CHE-660: Improve server side code to less time of getting project

    Signed-off-by: Vladyslav Zhukovskii <vzhukovskii@codenvy.com>