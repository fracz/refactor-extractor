commit 548dc646393448b9235cd49980e61daaefa25182
Merge: 21f8f8c 142cffb
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Dec 13 18:14:33 2012 +0100

    merged branch fabpot/webprofiler-refactor (PR #6323)

    This PR was merged into the master branch.

    Commits
    -------

    142cffb fixed unit tests
    fc444f1 fixed support for Twig loaders when they do not extend Twig_ExistsLoaderInterface
    f005649 [WebProfilerBundle] decoupled the bundle from TwigBundle
    35d63df removed the dependency on the container for exception handling

    Discussion
    ----------

    Webprofiler refactor

    This PR removes two hard dependencies from WebProfilerBundle:

     * The dependency on the DIC;
     * The dependency on TwigBundle.

    It also removes the dependency on the DIC in the exception controller from TwigBundle for more consistency.