commit 79a957be77d3afbb8b3ddbb83a330e05d85b876a
Merge: 5251177 4847d3a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Feb 4 08:01:13 2012 +0100

    merged branch kbond/config_dump_command (PR #3187)

    Commits
    -------

    4847d3a renamed command
    e97af0b code fixes
    df94282 [FrameworkBundle] removed unnecessary DebugCommand
    fa32885 [SecurityBundle] added configuration info
    2f8ad93 [MonologBundle] added configuration info
    9757958 [FrameworkBundle] added configuration info
    58939f1 [TwigBundle] added configuration docs
    8dc40e4 [FrameworkBundle] added config:dump console command

    Discussion
    ----------

    Config dump command

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: #1663
    Todo: add more config info/examples

    [![Build Status](https://secure.travis-ci.org/kbond/symfony.png?branch=config_dump_command)](http://travis-ci.org/kbond/symfony)

    This is a config dump command based on the additions of PR #1099.  This was initially part of that PR and there is some discussion there about it (https://github.com/symfony/symfony/pull/1099)

    ### Usage:

    1. dump by root node: ``app/console config:dump framework``
    2. dump by bundle name: ``app/console config:dump FrameworkBundle``

    A few issues/notes:

    * Only dumps to yaml
    * Only 1 configuration per bundle (this was brought by @stof here: https://github.com/symfony/symfony/pull/1099#issuecomment-1242993)
    * Works out of the box for most bundles but not ones that use a non-standard ``Configuration`` class (such as the assetic bundle).  In this case ``Extension::getConfiguration()`` must be configurated.

    I have used it to create some (most) of the config reference docs.  It works fine but I find it somewhat crude, any suggestions to improve it would be appreciated.

    ---------------------------------------------------------------------------

    by kbond at 2012-01-24T21:00:43Z

    Few more issues:

    1. Should I abstract the logic to a "normalizer" class that converts the Configuration class into a manageable array?  I struggle with this idea because isn't that what ``TreeBuilder`` basically is?
    2. @stof made a good point that ``config:dump`` doesn't really describe what this does.  Would dumping your config be useful?  Perhaps ``config:dump framework`` dumps the config for your project while ``config:dump --ref framework`` dumps the default reference?

    ---------------------------------------------------------------------------

    by stof at 2012-01-24T21:18:15Z

    @kbond you cannot really dump the config. Part of it does not go through these extensions at all. And it does not make much sense anyway IMO.
    the command as is does the right job IMO (i.e. dumping a reference for the extension). But its name should be improved

    ---------------------------------------------------------------------------

    by kbond at 2012-01-24T21:20:51Z

    ``config:reference`` perhaps?

    ---------------------------------------------------------------------------

    by fabpot at 2012-02-02T10:05:19Z

    This command is about displaying the default configuration for a given bundle. So, what about `config:dump-reference`? As I understand, the command name is the last element to figure out before merging, right?

    ---------------------------------------------------------------------------

    by stof at 2012-02-02T10:19:49Z

    @fabpot indeed.

    ---------------------------------------------------------------------------

    by stof at 2012-02-02T10:34:16Z

    and +1 for ``config:dump-reference``

    ---------------------------------------------------------------------------

    by Tobion at 2012-02-02T12:08:03Z

    why not use the words you chose yourself: `config:dump-default`
    I think it's more explicit.