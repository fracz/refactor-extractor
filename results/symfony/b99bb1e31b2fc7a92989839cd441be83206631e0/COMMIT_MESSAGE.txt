commit b99bb1e31b2fc7a92989839cd441be83206631e0
Merge: 5dcfeb2 ef322f6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Sep 9 11:17:04 2011 +0200

    merged branch michelsalib/translation-command (PR #2051)

    Commits
    -------

    ef322f6 -- add command that extracts translation messages from templates

    Discussion
    ----------

    [2.1] Extracting translation messages from templates

    As seen here #1283 and here #2045, I push the command that extract translation from templates.

    There are still a lot of new things here, but it seems more manageable.

    ---------------------------------------------------------------------------

    by stof at 2011/09/04 02:04:40 -0700

    @michelsalib Could you try to refactor the code to make it more flexible by moving the creating of the file to the dumpers to support other outputs (database...) ?

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/04 02:35:50 -0700

    You are right, I shall do it tonight.

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/04 11:53:35 -0700

    I just pushed a refactoring that should allow more flexibility. Dumpers are now responsive for writing the files. This way it is now possible to implement the DumperInterface that dump to a database and add it to the TranslationWriter.
    I updated the tests accordingly.

    ---------------------------------------------------------------------------

    by fabpot at 2011/09/05 23:27:27 -0700

    To be consistent with other dumpers in the framework, the dumpers extending `FileDumper` should use `FileDumper` as a suffix like in `YmlFileDumper`.

    ---------------------------------------------------------------------------

    by fabpot at 2011/09/05 23:41:12 -0700

    A general note on PHPDoc: The first line of a phpdoc ends with a dot and starts with a present verb like in  `Extracts translation messages from template files.`

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/06 01:23:31 -0700

    I fixed most of the remarks. I just need to go through the phpdoc (in a few minutes).

    ---------------------------------------------------------------------------

    by stloyd at 2011/09/06 01:28:55 -0700

    @michelsalib you should use `git rebase` (see [docs](http://symfony.com/doc/current/contributing/code/patches.html#id1)) instead of `git merge`.

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/06 01:31:06 -0700

    @stloyd sorry. I will rebase (squash) everything when I am finished.

    ---------------------------------------------------------------------------

    by kaiwa at 2011/09/06 01:31:18 -0700

    Hey, it might be a little bit late, but may i ask a not code-related question?

    Is it correct that the `--force` option only means to write the output to a file instead of writing it to stdout?

    If so,

    1. is it semantically correct? I mean... i'm not a native english speaker, but from unix programs i'm used to interpret a `--force` option as something like "overwrite", "ignore errors" or "suppress warnings". An option which is used in case of trouble most time. Feels confusing to me to be forced ;-) to use the `--force` option for simply writing to files.

    2. does it makes sense to have a default behaviour instead of requiring the user to give either `--force` or `--dump-messages`? In which cases does the user wants to dump the messages to the console? Is it only for debugging / to review the messages?

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/06 01:33:58 -0700

    @kaiwa Your concerns seems perfectly right. Initially I just wanted to mimic the `doctrine:schema:update` command. But it can be changed.
    @fabpot what do you think ?

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/06 02:01:22 -0700

    @stloyd I tried to do a `git rebase` and I am quite lost. I think I messed something when merge instead of rebase. How can I clean/squash my PR properly ?

    ---------------------------------------------------------------------------

    by stloyd at 2011/09/06 02:11:29 -0700

    @michelsalib for now just work as it is ;-) When I back from work I will try to help you to rebase it properly.

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/06 02:12:17 -0700

    @stloyd Thank you !

    ---------------------------------------------------------------------------

    by stloyd at 2011/09/06 12:39:18 -0700

    @michelsalib I was trying to rebase your code and revert it this _bad_ commit (merge), but without success.

    IMO best way would be making _brand new_ branch based on symfony/master, and the `git cherry-pick` ([hint](http://ariejan.net/2010/06/10/cherry-picking-specific-commits-from-another-branch)) commits you need (almost all in this PR but without this one with merge), then you will need to apply again _by hand_ changes from commit https://github.com/michelsalib/symfony/commit/fce24c7fa2346dd3a0e654225ae9dde42197aa5a (this clean up you have done there).

    I'm sorry I can't give you more help...

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/07 09:41:36 -0700

    @stloyd I finally succeed to fix this PR. Thanks to your help! I must admit the whole thing with `cherry-pick` command was quite epic.
    @fabpot Don't be sorry, I am about to fix the PHPDoc. I shall squash right after.

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/07 10:07:20 -0700

    I just squashed and did a last polish of the code. You might want to read it again before merge.

    ---------------------------------------------------------------------------

    by fabpot at 2011/09/07 11:40:48 -0700

     ok, code looks really good now. I think the only missing thing is some more unit tests (for the extractors for instance).

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/07 12:18:59 -0700

    Thanks, I'll look into it tomorrow.

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/08 15:13:10 -0700

    Hi,
    I just added unit tests for both extractors (php and yaml).
    Concerning the yaml extractor, the test is quite tricky because I need to mock the twig environment while returning a twig tree that contain a trans block and a trans filter. But it work fine with as little side effects as possible. Also I am not sure that the test should be in the TwigBundle.
    IMO, the PR seems ready.

    ---------------------------------------------------------------------------

    by stof at 2011/09/08 15:34:41 -0700

    As the extractor is in bridge, the tests should be in the folder containing the tests for the bridge in tests/

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/08 15:41:45 -0700

    Thanks @stof, it is now fixed.

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/09 00:48:47 -0700

    thanks @stloyd, it is fixed now.

    ---------------------------------------------------------------------------

    by michelsalib at 2011/09/09 01:25:24 -0700

    Fixed again ;)