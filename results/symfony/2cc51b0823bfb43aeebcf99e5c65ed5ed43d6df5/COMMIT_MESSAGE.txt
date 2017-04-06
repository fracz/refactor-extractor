commit 2cc51b0823bfb43aeebcf99e5c65ed5ed43d6df5
Merge: 73c0397 163564b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 17 14:59:17 2012 +0100

    merged branch fabpot/twig-render (PR #6386)

    This PR was merged into the master branch.

    Commits
    -------

    163564b [WebProfilerBundle] replaced yaml_dump by json_encode to make the Web Profiler independent from the YAML component
    1c92307 [WebProfilerBundle] fixed exception panel when no exception is thrown
    00e08be [WebProfilerBundle] replaced usage of the render tag by the render function (to decouple the bundle from TwigBundle)
    0e2418c [TwigBundle] added the HttpKernel extension to the default Twig loaded extensions
    f0d9be0 [TwigBridge] added an extension for the HttpKernel component

    Discussion
    ----------

    Added an HttpKernelExtension in Twig bridge and used it in the WebProfiler

    The first commit introduces a new HttpKernelExtension in the Twig bridge that allows the rendering of a sub-request from a template (the code mostly comes from Silex, and will replace the code there at some point).

    The name `render` is probably not the best one as it does not really tell you what it does (the same goes for the `render` tag we have in Symfony2 by the way).

    Here is a list of possible names:

     * `render()`
     * `render_request()`
     * `request()`
     * `subrequest()`
     * `include_request()`

    I don't really like the last one, but it is (perhaps) consistent with the `include` tag/function in Twig.

    This new `render()` function is also a first step towards replacing the `render` tag (with support for ESI, SSI, ...). But it won't happen before we refactor the way it's managed now (a lot of the code is in the FrameworkBundle right now and that prevents Silex or Drupal to reuse it).

    The other commits make use of this new extension to make the Web Profiler truly independent from TwigBundle and FrameworkBundle.