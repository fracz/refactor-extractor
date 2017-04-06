commit e2326a7b738e0fb8b08f06898bbc3608224f230f
Merge: 866b13e 75c7d3a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Apr 20 08:12:25 2012 +0200

    merged branch shiroyuki/master (PR #3850)

    Commits
    -------

    75c7d3a Fixed the link to the method with onclick event.
    ecbabec renamed 'Request handler' to 'Controller' and 'Route ID' to 'Route name'.
    3c5ede4 Add a new query to display all information.
    f6a866b Merged CSS for the toolbar in both embedded mode (on each page) and profiler.
    306533b Updated the responsive design in addition to the scenario with authenticated users and exception notification.
    4a3312b Updated the toolbar with the responsive design (normal-to-large scenario).
    1eec2a2 Updated the toolbar with the responsive design (normal-to-small scenario).
    03c8213 Refactored the CSS code for the toolbar out of the template.
    37843b3 Updated with PHP logo (only the text).
    d5e0ccc Made the toolbar to show the version, memory usage, the state of security (both a abbreviation and an associate description) and number of DB requests and request time.
    37ad8a6 Removed the check for verbose and adjusted the style when the toolbar is on the top of the page.
    67b0532 Redesigned the WDT.

    Discussion
    ----------

    Re-design the debugging toolbar

    The toolbar is very useful and containing lots of information. However, as there are too much information, it is very distracting and the toolbar area somehow ends up taking too much space and then becomes something like a panel.

    The main purpose of this pull request is to hide any information and show only whenever the user wants to see, except the status code and response time.

    This is based on [the pull request #3833](https://github.com/symfony/symfony/pull/3833) with the feedbacks and for 2.1 (master).

    The testing app is available at http://home.shiroyuki.com.

    ---------------------------------------------------------------------------

    by stof at 2012-04-10T06:24:36Z

    @shiroyuki your testing app denies the access because of the restriction in app_dev.php

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-10T06:27:27Z

    @stof: I'm sorry. It should be working now.

    ---------------------------------------------------------------------------

    by stof at 2012-04-10T06:45:39Z

    Moving the toolbar to the top of the page means it will hide some content of the page. You should keep it at the bottom

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-10T06:48:28Z

    Just a moment ago, I changed the position of the toolbar via `config_dev` so I could check when WDT is on the top.

    I just reverted the config file. :D

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-10T06:55:16Z

    Some comments:

     * I would have kept the number of database request as this number is probably the one everybody should have a look at on every page.
     * I would have used the original PHP logo (in black and white) instead of a non-standard one

    But overall, this is a very nice improvement.

    ---------------------------------------------------------------------------

    by stloyd at 2012-04-10T06:55:43Z

    There is an issue with "bubbling" at Firefox 11 (at least), when you hover `<a>` element, the hover event seems to be "launched" twice.

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-10T06:56:13Z

    As the verbose mode has been removed from the template, it should also be removed from the configuration (I can do that after merging if you don't know how to do that).

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-10T07:05:31Z

    @stloyd I noticed that too. As I couldn't find the same issue on Webkit-based browsers and all effects on this toolbar heavily relies on CSS, it could have been a glitch on Firefox.

    @fabpot I'll see what I can do with the number of DB request and the logo.

    ---------------------------------------------------------------------------

    by asm89 at 2012-04-10T07:26:28Z

    Will there be options to somehow keep the debug toolbar 'expanded' or something? I guess the folding of the sf and php information makes sense, but I personally look at the request/time/memory/security and query parts of the toolbar a lot. As my browser window is big enough to show all information at once, this would be a huge step backwards imo.

    ---------------------------------------------------------------------------

    by XWB at 2012-04-10T07:28:38Z

    Agreed with @asm89, I also want the option to show all the information on my screen.

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-10T08:28:00Z

    I tend to agree too with @asm89. What about reusing the `verbose` option for that. This was already its purpose anyway.

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-10T14:56:45Z

    How about using media query?

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-11T02:20:32Z

    Please note that the latest commit still doesn't have the new logo for PHP.

    As DoctrineBundle now has its own repository, the change to show the number of DB requests is already done via DoctrineBundle's [PR 57](https://github.com/doctrine/DoctrineBundle/pull/57).

    ---------------------------------------------------------------------------

    by guilhermeblanco at 2012-04-11T02:50:47Z

    @fabpot @shiroyuki as soon as this patch is merged I will do the same on DoctrineBundle.
    All you need to do is look at me over our desks' separator. =D

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-11T03:17:41Z

    The last commit has the updated PHP logo. Unfortunately as @stloyd and @guilhermeblanco pointed out, the flicking on the toolbar when the mouse is over might have been due to the CSS issue on Firefox.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-11T04:46:36Z

    Nice work shiroyuki. I always had the feeling the toolbar can be improved. Good that you got this one going.
    I would remove the verbose option (rarely nobody changes it) and use media queries to accomplish a responsive design that shows as much information as possible. And only shows the most important facts when there is not enough space.
    E.g. the symfony version could be removed if it doesn't fit on the screen because it's mostly static from request to request.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-11T04:48:45Z

    Another idea: Add a panel "PHP Info" to the profiler that shows the output of `phpinfo()`. This panel is linked from the PHP logo in the WDT which currently has no link on it.

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-11T15:47:51Z

    @Tobion: It would be an overkill if `phpinfo()` was visible in the toolbar. Additionally, the toolbar doesn't fit to show that amount of information. Plus, the information released by `phpinfo()` is also static and easily obtained by a simple PHP script. I don't think that WDT should be showing this information.

    Please note that the media query is not yet implement. The followings are still unknown to me:

    * should we support the toolbar for mobile device?
    * what is the minimum screen size?

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-11T15:52:43Z

    @shiroyuki you misunderstood me. phpinfo() should be a new panel in the PROFILER, not the WDT. It is reachable from the WDT by clicking on the PHP logo. But that can be implemented in a seperate PR. It's just an idea and before I would implement it, I'd like to receive feedback if it would be accepted at all.

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-11T16:38:44Z

    Displaying `phpinfo()` data is not in the scope of this PR.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-11T16:48:50Z

    @fabpot yeah. But would you accept such a PR or do you think it's not useful?

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-11T16:57:49Z

    @Tobion The web profiler is mainly about information for the current request; so I'm not sure it would be useful to have such a tab in the profiler.

    ---------------------------------------------------------------------------

    by vicb at 2012-04-11T17:06:15Z

    @fabpot @Tobion what about adding it in the config panel ? Not sure if it is very useful but I have seen to many `phpinfo.php` in the web root folder. (It could be an expandable panel loaded via ajax like what is used for the Doctrine explain panel).

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-12T03:11:40Z

    @tobian @vicb: what kind of information are you looking from `phpinfo()`?

    ---------------------------------------------------------------------------

    by Felds at 2012-04-12T03:30:02Z

    The equivalent for `phpinfo()` was extremely convenient and helped a lot in Symfony 1. It's out of scope but an optional panel could be nice.
    Ini flags are of great help when debugging on a hurry.

    :+1: for that!

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-12T03:37:52Z

    @shiroyuki I don't understand your question. Everything of it should be displayed. But don't worry about phpinfo(), I'll work on that in a seperate PR. You can focus on the responsive design. ;)

    ---------------------------------------------------------------------------

    by vicb at 2012-04-12T06:54:35Z

    @shiroyuki I am not looking for anything specific. Just saying I have seen many times customer code using a publicly accessible file to return the info and it would help to get ride of this file.

    ---------------------------------------------------------------------------

    by sstok at 2012-04-12T07:59:18Z

    ```
    should we support the toolbar for mobile device?
    ```

    Good question, I don't think so because the screen-size is to small to show anything useful.
    Maybe a small icon to display the information as overlay, including the token so you can refer to that on a bigger screen?.

    ---------------------------------------------------------------------------

    by johnnypeck at 2012-04-13T06:45:43Z

    If your interested in a useful but not so intrusive way of providing the toolbar on mobile devices perhaps take a look at what the guys at Twitter have done with the topbar navigation converting to a semi-accordion style menu on mobile in Bootstrap. I can see the usefulness. Checkout the responsive.less which makes it easy enough to include/exclude depending on screen size. I found it quite useful in a recent project.

    Regarding adding a tab for phpinfo, sure it would be useful BUT if the reasoning is that some people leave a publicly available phpinfo script therefore just include it then I would not include it. There are many more useful requirements of the toolbar rather than to insulate intro to web issues. That's like saying don't include the toolbar because someone may build an application that makes the toolbar available publicly (which will happen). I've seen too many projects in my years having no clue of versioning tools that must have been built on the server, live, with filenames like indexv1.php, indexv2.php, indexTryAgain.php, db credentials in the clear, and just hoping to find a point where it works enough. And yes, I've found those scripts were publicly available and still around years after they were created; security holes and all! I'm preaching to the choir here. You'll never stop stupid. All we can do is educate by any means we have and share our knowledge with one another. Aside from that devils advocate reasoning, I would include the phpinfo tab, it does make sense in those random "did I/they compile that in" circumstances. ;-) Sorry for the rant.

    +1 for mobile
    sorta+1 for phpinfo
    +10 for better educating on how to include anything you need so phpinfo could be a "my first foray into adding a tool to my toolbar for Symfony" tutorial in the cookbook.

    Again, sorry for the long winded rant. Cheers everyone. Goodnight.

    ---------------------------------------------------------------------------

    by shiroyuki at 2012-04-13T23:33:21Z

    @stof I think we can remove the CSS.