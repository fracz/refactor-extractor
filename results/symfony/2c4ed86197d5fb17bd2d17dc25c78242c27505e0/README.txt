commit 2c4ed86197d5fb17bd2d17dc25c78242c27505e0
Merge: 926dd1d 310c2f9
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Sep 28 21:16:51 2012 +0200

    merged branch dlsniper/wpb-improvements (PR #5518)

    This PR was squashed before being merged into the master branch (closes #5518).

    Commits
    -------

    3303ca2 WPB and WDT improvements
    be194cb Changed icons to be a bit more consistent
    08241b8 Added minimize option to Web Profiler panels

    Discussion
    ----------

    [2.2][WebProfilerBundle] Added minimize option to Web Profiler panels

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: ~
    Todo: ~
    License of the code: MIT
    Documentation PR: ~

    I've added a minimize option to the profiler bundle so that you can have more space to work with on the panels.
    You can view it in action here:http://sf2demo.rodb.ro/app_dev.php/

    Feedback is welcomed!

    Thanks!

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-15T13:36:51Z

    I could add a remember option via a cookie if you think this would help, I know I'd want one, but I'm not sure about the general opinion about this. Let me know if I should do it.

    ---------------------------------------------------------------------------

    by stof at 2012-09-15T17:05:58Z

    The profiler is totally broken when minimizing the menu in your demo.

    ---------------------------------------------------------------------------

    by lennerd at 2012-09-15T17:10:54Z

    I would not make it disappear completely. So I think a combination of Cookie and a small visual for open it again would be great to have.

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-15T17:15:06Z

    @stof I've only enabled a few panels to work in the demo, rest defaults to DB panel. If this is what you mean then it's not broken, it's designed to do so. I've tested the thing on Opera, FF and Chrome (on Linux) before uploading the demo/PR so I'm not sure what's broken. Can you please provide a screenshot?

    @lennerd I could be doing something like only display it in the upper left corner and appear on mouse over as an overlay. Would that be better?

    ---------------------------------------------------------------------------

    by stof at 2012-09-15T17:21:22Z

    @dlsniper what I mean is that the text of the menu does not disappear. It simply goes over the panel itself as the menu becomes smaller. And this appears for all panels I tried.

    ---------------------------------------------------------------------------

    by stof at 2012-09-15T17:22:32Z

    hmm, sorry. It is a browser cache issue. It seems like your server was sending cache headers for the assets, and as I already looked at the demo previously (for your DoctrineBundle PR), it kept the old CSS

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-15T17:25:09Z

    @stof no problem, the server is configured a bit more on caching side in order to speed it up and save bandwidth ;)

    ---------------------------------------------------------------------------

    by lennerd at 2012-09-15T17:38:41Z

    @dlsniper I would use the close button changing to maybe an arrow in the bottom right. So it's more intuitive and you can simply show and hide it if you only want to take a quick look at a small detail behind it.

    ---------------------------------------------------------------------------

    by henrikbjorn at 2012-09-15T18:08:02Z

    What about making this the default, the icons are self explanatory already. The title would then be the "link" text instead.

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-15T20:30:51Z

    @henrikbjorn I wouldn't make this by default as new people might find it a bit confusing. Hence the suggestion to use the cookie to remember the preference.

    ---

    Also I'm trying not to break the current format of the menu too much as hiding all that stuff by hand is pain but if I'm allowed to break the current way of displaying the left menu then this is going to be easy.

    What I didn't understood so far is why is the toolbar displayed on the top as well since we have it on the left side already so I've remove it from my current changes (will be up soonish).

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-15T21:10:03Z

    @lennerd what exactly do you mean by 'I would use the close button' there's no close button on the profiler page, only on the toolbar.

    ---------------------------------------------------------------------------

    by lennerd at 2012-09-15T21:21:20Z

    That was the button I was talking about. So that there is a little close button at the bottom right for toggling the toolbar.

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-15T23:14:06Z

    I've changed the way the menu minimizes now, it hides in the top left corner and it maintains its state on refresh. I'll do something similar for the toolbar tomorrow.

    You can view it on the same URL.

    Please do leave feedback. Thanks!

    ---------------------------------------------------------------------------

    by lennerd at 2012-09-16T01:02:27Z

    Sorry. I misunderstand your PR.

    ---------------------------------------------------------------------------

    by stof at 2012-09-16T03:01:06Z

    @dlsniper The toolbar is displayed at the top because it gives a quick overview without having to go in each panel. So removing it is a bad idea IMO.

    And hiding everything is a bad idea IMO. It means navigating is impossible, making it usable when minimizing it (and btw, this would make the cookie a non-sense as it would hide the menu for subsequent pages)

    ---------------------------------------------------------------------------

    by fabpot at 2012-09-16T06:38:08Z

    -1 for removing the toolbar at the top

    I prefer the first version where you only hide the menu text but leave the icons. Keeping the state in a cookie is also a must (that cookie might be used to store some other states in the profiler too).

    ---------------------------------------------------------------------------

    by Partugal at 2012-09-16T08:14:11Z

    i'm not see first version but show icons without text is more useful.
    imho minimize trigger should be always placed on top as it showns in minized state

    ---------------------------------------------------------------------------

    by Partugal at 2012-09-16T08:24:49Z

    http://s14.postimage.org/qkdcr8d4h/image.png

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-16T09:06:50Z

    @fabpot I've just had a look on how the timeline stores the selected value and it's using the local storage capabilities. Should I drop the cookie and use the local storage as well to have some sort of uniformity?
    Also is there any reason why no generic JS library is used? I'm thinking now about jQuery mostly but any other should do just fine I think. I'm not saying that we should use a library when displaying the WDT as it might bump into issues with the frontend but for the rest of the profiler I guess it wouldn't be a problem to use a library, no?

    ---------------------------------------------------------------------------

    by fabpot at 2012-09-16T09:15:37Z

    Let's use the local storage for better consistency. I don't want to embed a JS library as we only need basic JS scripts.

    ---------------------------------------------------------------------------

    by lennerd at 2012-09-16T10:29:20Z

    @dlsniper Do we need the up and down arrows any longer?

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-16T14:24:27Z

    I've added a minimize mode to the toolbar but the 'design' isn't the best all around, I'll try to improve it in the future.
    It also remembers the state of the toolbar so that you don't need to hide it every time.

    @lennerd we don't need the up/down arrows for now, I've removed on the last commit. Thank you for the new icons ;)

    L.E.
    I've made some sort of rounded corner/gradient background in for the minimized toolbar

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-18T22:02:35Z

    @pborreli thanks for the idea regarding to auto-minimize on window resize, I'll implement it soon, I don't really have time right now to add the event handling part to Sfjs.

    L. E.
    I'm not going to implement the auto-resize as it proved not to be that useful given the fixed width of the panel. If it proves to be a requested thing then I'll improve it if no one else does it before me :)

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-19T20:52:43Z

    If there's nothing else left to be changed/improved/added, I'll lift the WIP tag of this PR so that it can be merged if you consider it.

    @fabpot, if this gets its way to the repository, should I rebase this before merging so that it catches the next Symfony 2.1 release as it doesn't break anything? I don't mean the very next release which I've read that it would be done when Doctrine will release their new version but the version after that.

    ---------------------------------------------------------------------------

    by fabpot at 2012-09-19T20:56:45Z

    This is a new feature, so it can only be included in the master branch.

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-23T12:27:41Z

    As soon as this feature goes in master I'll start working on adding AJAX requests to the toolbar to make it even more useful.

    Let me know if this change is good to merge or needs more work.

    Thanks @stof for all the input and @lennerd for the icons.

    ---------------------------------------------------------------------------

    by stof at 2012-09-23T13:40:38Z

    Adding which ajax requests to the toolbar ?

    ---------------------------------------------------------------------------

    by dlsniper at 2012-09-23T13:49:43Z

    'Userland' AJAX requests, so that one could access the information from an AJAX request more straight forward