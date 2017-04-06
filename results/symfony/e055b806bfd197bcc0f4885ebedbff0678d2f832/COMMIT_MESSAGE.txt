commit e055b806bfd197bcc0f4885ebedbff0678d2f832
Merge: d7fa841 39445c5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Mar 19 15:30:59 2013 +0100

    merged branch stof/toolbar_ie (PR #7423)

    This PR was merged into the 2.2 branch.

    Commits
    -------

    39445c5 [WebProfilerBundle] Fixed the toolbar styles to apply them in IE8

    Discussion
    ----------

    [WebProfilerBundle] Fixed the toolbar styles to apply them in IE8

    | Q             | A
    | ------------- | ---
    | Fixed tickets | #7422
    | License       | MIT

    Currently, the toolbar breaks the design of the whole page in IE8 and lower as it does not have styles applied. Even though it is a debugging tool and devs are often using modern browsers, it is painful to be forced to disable it when testing the site in IE (I won't bother about supporting the profiler JS in IE8 though as this is a different page which can be displayed in a modern browser even when testing in IE).
    The reason of the issue is that [IE8 removes style tags at the beginning when setting the innerHTML](http://social.msdn.microsoft.com/forums/en-US/iewebdevelopment/thread/33fd33f7-e857-4f6f-978e-fd486eba7174/). As the fix is as easy as moving the tag after the div, I don't see a reason to reject this change.

    I sent the bugfix to 2.2 because these templates have been refactored a lot between 2.1 and 2.2 so the fix would have been different. However, it is also possible to fix it in 2.1 if you want.