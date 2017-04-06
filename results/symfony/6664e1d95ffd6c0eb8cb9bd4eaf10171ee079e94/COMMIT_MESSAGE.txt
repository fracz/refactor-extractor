commit 6664e1d95ffd6c0eb8cb9bd4eaf10171ee079e94
Merge: 3bbb6e5 1b57727
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Oct 15 03:26:46 2011 +0200

    merged branch schmittjoh/interBundleExtensibility (PR #2349)

    Commits
    -------

    1b57727 removed unused use statements
    fd67c78 updated implementation to re-use the existing build() method
    59e2e97 improves extensibility between bundles

    Discussion
    ----------

    [RFC] Improving extensibility between bundles

    This is a quick draft for improving extensibility between different bundles.

    The idea behind this is that an extension can provide configurable settings that other bundles can change.

    ---------------------------------------------------------------------------

    by Seldaek at 2011/10/07 13:28:13 -0700

    I am not yet sure what I would use it for, but I like the idea.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/10/07 13:45:19 -0700

    can you show a bit more how to use this?

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/10/07 13:47:38 -0700

    oh it appears this is an example?

    https://github.com/schmittjoh/JMSSerializerBundle/commit/f4e76640a07bc41d8e75a0433710143e19e302bc#diff-9

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/10/07 13:57:00 -0700

    yes

    On Fri, Oct 7, 2011 at 10:47 PM, Lukas Kahwe Smith <
    reply@reply.github.com>wrote:

    > oh it appears this is an example?
    >
    >
    > https://github.com/schmittjoh/JMSSerializerBundle/commit/f4e76640a07bc41d8e75a0433710143e19e302bc#diff-9
    >
    > --
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/2349#issuecomment-2328078
    >

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/10/11 01:11:39 -0700

    @fabpot, do you have an opinion on this, :+1: :-1:?