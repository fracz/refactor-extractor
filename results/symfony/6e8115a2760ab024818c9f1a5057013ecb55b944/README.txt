commit 6e8115a2760ab024818c9f1a5057013ecb55b944
Merge: 270e530 2379d86
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Nov 19 13:58:52 2012 +0100

    merged branch raziel057/COMPONENT_Form (PR #5888)

    This PR was squashed before being merged into the master branch (closes #5888).

    Commits
    -------

    2379d86 CS Fixes - Replaced "array of type" by "Type[]" in PHPDoc block

    Discussion
    ----------

    CS Fixes - Replaced "array of type" by "Type[]" in PHPDoc block

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: no (but tests doesn't pass on master too). See Travis.
    License of the code: MIT
    Documentation PR: Not Applicable
    Status: Finished

    To improve support of the eclipse PDT pluggin (for autocompletion), I propose to change the array notation in PHPDoc blocks to match the phpDocumentor notation for "array of type".

    Modifications are made for the following components:
    - BrowserKit
    - ClassLoader
    - Config
    - Console
    - CssSelector
    - DependencyInjection
    - DomCrawler
    - EventDispatcher (no changes)
    - Filesystem (no changes)
    - Finder
    - Form
    - HttpFoundation
    - HttpKernel
    - Locale
    - OptionResolver (no changes)
    - Process (no changes)
    - Routing (no changes)
    - Serializer (no changes)
    - Templating
    - Translation
    - Validator
    - Yaml (no changes)
    - Security
    - Stopwatch (no changes)

    See Proposal https://github.com/symfony/symfony/pull/5852

    ---------------------------------------------------------------------------

    by pborreli at 2012-11-01T15:19:27Z

    will you make a PR for each component ? why not only one PR with one commit for each component instead ?

    ---------------------------------------------------------------------------

    by raziel057 at 2012-11-01T15:32:39Z

    Ok, I'm going try to do it.

    ---------------------------------------------------------------------------

    by raziel057 at 2012-11-01T16:12:56Z

    I would like to rename my branch from COMPONENT_Form to changes-phpdoc (as all modifications would be commited in only one branch), so I tried to execute the following command but I have an error.

    git remote rename COMPONENT_Form changes-phpdoc
    error: Could not rename config section 'remote.COMPONENT_Form' to 'remote.changes-phpdoc'

    Do you know how to do it?

    ---------------------------------------------------------------------------

    by pborreli at 2012-11-01T16:14:26Z

    don't rename it, you will have to close and make another PR which is useless here, just edit the title.

    ---------------------------------------------------------------------------

    by stof at 2012-11-01T16:16:17Z

    and ``git remote rename`` is about renaming a remote repo, not a branch

    ---------------------------------------------------------------------------

    by raziel057 at 2012-11-03T11:36:02Z

    Is it normal that all my commit are duplicated? I would like just update my master and merge with my branch.

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-06T10:22:55Z

    @raziel057 Can you rebase on master? That should fix your problem.

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-09T13:28:53Z

    @raziel057 Can you finish this PR?

    ---------------------------------------------------------------------------

    by Tobion at 2012-11-09T13:34:45Z

    I'll do it for the routing component this evening because I know it by heart. ^^

    ---------------------------------------------------------------------------

    by raziel057 at 2012-11-09T15:06:26Z

    @Tobion ok Thanks!

    @fabpot Yes, I will try to finish it this week end.

    ---------------------------------------------------------------------------

    by raziel057 at 2012-11-11T13:04:07Z

    @Tobion Did you already change PHPDoc in the Routing component?

    ---------------------------------------------------------------------------

    by Tobion at 2012-11-11T15:21:18Z

    @raziel057 Yes I'm working on it.

    ---------------------------------------------------------------------------

    by Tobion at 2012-11-12T15:16:31Z

    @raziel057 Done. See #5994