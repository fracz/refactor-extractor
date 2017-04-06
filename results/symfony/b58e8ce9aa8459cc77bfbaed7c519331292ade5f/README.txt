commit b58e8ce9aa8459cc77bfbaed7c519331292ade5f
Merge: 2aba3aa 76fefe3
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jan 11 08:24:18 2013 +0100

    merged branch fabpot/kernel-refactor (PR #6459)

    This PR was merged into the master branch.

    Commits
    -------

    76fefe3 updated CHANGELOG and UPGRADE files
    f7da1f0 added some unit tests (and fixed some bugs)
    f17f586 moved the container aware HTTP kernel to the HttpKernel component
    2eea768 moved the deprecation logic calls outside the new HttpContentRenderer class
    bd102c5 made the content renderer work even when ESI is disabled or when no templating engine is available (the latter being mostly useful when testing)
    a8ea4e4 [FrameworkBundle] deprecated HttpKernel::forward() (it is only used once now and not part of any interface anyway)
    1240690 [HttpKernel] made the strategy a regular parameter in HttpContentRenderer::render()
    adc067e [FrameworkBundle] made some services private
    1f1392d [HttpKernel] simplified and enhanced code managing the hinclude strategy
    403bb06 [HttpKernel] added missing phpdoc and tweaked existing ones
    892f00f [HttpKernel] added a URL signer mechanism for hincludes
    a0c49c3 [TwigBridge] added a render_* function to ease usage of custom rendering strategies
    9aaceb1 moved the logic from HttpKernel in FrameworkBundle to the HttpKernel component

    Discussion
    ----------

    [WIP] Kernel refactor

    Currently, the handling of sub-requests (including ESI and hinclude) is mostly done in FrameworkBundle. It makes these important features harder to implement for people using only HttpKernel (like Drupal and Silex for instance).

    This PR moves the code to HttpKernel instead. The code has also been refactored to allow easier integration of other rendering strategies (refs #6108).

    The internal route has been re-introduced but it can only be used for trusted IPs (so for the internal rendering which is managed by Symfony itself, or by a trusted reverse proxy like Varnish for ESI handling). For the hinclude strategy, when using a controller, the URL is automatically signed (see #6463).

    The usage of a listener instead of a controller to handle internal sub-requests speeds up things quite a lot as it saves one sub-request handling. In Symfony 2.0 and 2.1, the handling of a sub-request actually creates two sub-requests.

    Rendering a sub-request from a controller can be done with the following code:

    ```jinja
    {# default strategy #}
    {{ render(path("partial")) }}
    {{ render(controller("SomeBundle:Controller:partial")) }}

    {# ESI strategy #}
    {{ render(path("partial"), { strategy: 'esi' }) }}
    {{ render(controller("SomeBundle:Controller:partial"), { strategy: 'esi' }) }}

    {# hinclude strategy #}
    {{ render(path("default1"), { strategy: 'hinclude' }) }}
    ```

    The second commit allows to simplify the calls a little bit thanks to some nice syntactic sugar:

    ```jinja
    {# default strategy #}
    {{ render(path("partial")) }}
    {{ render(controller("SomeBundle:Controller:partial")) }}

    {# ESI strategy #}
    {{ render_esi(path("partial")) }}
    {{ render_esi(controller("SomeBundle:Controller:partial")) }}

    {# hinclude strategy #}
    {{ render_hinclude(path("default1")) }}
    ```

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-03T17:58:49Z

    I've just pushed a new version of the code that actually works in my browser (but I've not yet written any unit tests). I've updated the PR description accordingly.

    All comments welcome!

    ---------------------------------------------------------------------------

    by Koc at 2013-01-03T20:11:43Z

    what about `render(controller="SomeBundle:Controller:partial", strategy="esi")`?

    ---------------------------------------------------------------------------

    by stof at 2013-01-04T09:01:01Z

    shouldn't we have interfaces for the UriSigner and the HttpContentRenderer ?

    ---------------------------------------------------------------------------

    by lsmith77 at 2013-01-04T19:28:09Z

    btw .. as mentioned in #6213 i think it would make sense to refactor the HttpCache to use a cache layer to allow more flexibility in where to cache the data (including clustering) and better invalidation. as such if you are refactoring HttpKernel .. it might also make sense to explore splitting off HttpCache.

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-04T19:30:07Z

    @lsmith77 This is a totally different topic. This PR is just about moving things from FrameworkBundle to HttpKernel to make them more reusable outside of the full-stack framework.

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-05T09:39:52Z

    I think this PR is almost ready now. I still need to update the docs and add some unit tests. Any other comments on the whole approach? The class names? The `controller` function thingy? The URI signer mechanism? The proxy protection for the internal controller? The proxy to handle internal routes?

    ---------------------------------------------------------------------------

    by sstok at 2013-01-05T10:08:25Z

    Looks good to me :+1:

    ---------------------------------------------------------------------------

    by sdboyer at 2013-01-07T18:17:08Z

    @Crell asked me to weigh in, since i'm one of the Drupal folks who's likely to work most with this.

    i think i've grokked about 60% of the big picture here, and i'm generally happy with what i see. the assumption that the HInclude strategy makes about working with templates probably isn't one that we'll be able to use (and so, would need to write our own), but that's not a big deal since the whole goal here is to make strategies pluggable.

    so, yeah. +1.

    ---------------------------------------------------------------------------

    by winzou at 2013-01-09T20:21:44Z

    Just for my information: will this PR be merged for 2.2 version? Thanks.

    ---------------------------------------------------------------------------

    by stof at 2013-01-09T20:41:04Z

    @winzou according to the blog post announcing the beta 1 release, yes. It is explicitly listed as being one of the reason to make it a beta instead of the first RC.

    ---------------------------------------------------------------------------

    by winzou at 2013-01-09T20:49:36Z

    OK thanks, I've totally skipped this blog post.

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-10T15:26:15Z

    I've just added a bunch of unit tests and fix some bugs I found while writing the tests.