commit f06105ce0168630f5c2917090b23ef80745ee30e
Merge: 9e97e68 6548354
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Dec 13 14:05:30 2011 +0100

    merged branch Tobion/patch-1 (PR #2856)

    Commits
    -------

    6548354 fixed data-url

    Discussion
    ----------

    [WebProfilerBundle] fixed and adjusted HTML5 markup

    I corrected some markup errors that I found when validating the pages of the WebProfilerBundle.
    Along the way I also improved the semantic structure of HTML5 like table header and body, lang attribute.
    Removed type="text/css" that is the default with rel="stylesheet". Also no need for media="screen"!? Otherwise style does not apply when debugging with handheld device or when printing.

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/12 23:37:15 -0800

    @Tobion: Can you squash your commit before I merge your PR? Thanks.

    ---------------------------------------------------------------------------

    by Tobion at 2011/12/13 03:14:51 -0800

    @fabpot I would appreciate if you could do this.

    I see two problems with pull requests on @github that occur again and again. It's pretty annoying compared to the otherwise very user-friendly Github.

    1. Squashing commits of a pull request: If you've already pushed commits to GitHub, and then squash them locally, you will not be able to push that same branch to GitHub again. So you need to create a new branch and a new pull request.
    So there should be a button on Github that simply squashes all commits and allows you to enter a new commit message.

    2. Opening a pull request based on the master branch instead of the 2.0 branch where bug fixes should be made. So people must rebase their stuff and open a new pull request again. All this back and forth is taking time unnecessarily (both for admins and contributors) and cluttering Githubs news feed.
    There should be the possibility to allow switching the pull request base branch. Or at least give the users a configurable hint about the best practice of contributing to a specific repo when they open a pull request.

    ---------------------------------------------------------------------------

    by henrikbjorn at 2011/12/13 03:16:10 -0800

    @Tobion

    1. Solved by doing a git push -f remote_name branch_name
    2. Yes here you need to open a new PR

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/13 03:21:47 -0800

    @Tobion: I'm more than aware of these issues but unfortunately, there is nothing I can do if we want to continue using the Github PRs (and automatic closing).

    ---------------------------------------------------------------------------

    by Tobion at 2011/12/13 03:51:47 -0800

    That's why I hope that @github will provide a convenient solution to these issues.

    ---------------------------------------------------------------------------

    by stof at 2011/12/13 04:08:07 -0800

    @Tobion send a feature request to github. Commenting here will not make them implement it

    ---------------------------------------------------------------------------

    by Tobion at 2011/12/13 04:18:31 -0800

    @fabpot I squashed commits.
    @stof I will do it. But there is no public issue tracker for the Github software, is there? So need to use the contact form I suppose.