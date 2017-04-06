commit 3a4869dd14270017efab8687232e81fb1b7c9931
Merge: aa8b63b 6703fb5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jan 9 10:27:51 2013 +0100

    merged branch Tobion/relative-path (PR #3958)

    This PR was merged into the master branch.

    Commits
    -------

    6703fb5 added changelog entries
    1997e2e fix phpdoc of UrlGeneratorInterface that missed some exceptions and improve language of exception message
    f0415ed [Routing] made reference type fully BC and improved phpdoc considerably
    7db07d9 [Routing] added tests for generating relative paths and network paths
    75f59eb [Routing] add support for path-relative and scheme-relative URL generation

    Discussion
    ----------

    [2.2] [Routing] add support for path-relative URL generation

    Tests pass: yes
    Feature addition: yes
    BC break: <del>tiny (see below)</del> NO
    deprecations: NO

    At the moment the Routing component only supports absolute and domain-relative URLs, e.g.
    `http://example.org/user-slug/article-slug/comments` and
    `/user-slug/article-slug/comments`.

    But there are two link types missing: schema-relative URLs and path-relative URLs.
    schema-relative: e.g. `//example.org/user-slug/article-slug/comments`
    path-relative: e.g. `comments`.

    Both of them would now be possible with this PR. I think it closes a huge gap in the Routing component.
    Use cases are pretty common. Schema-relative URLs are for example used when you want to include assets (scripts, images etc) in a secured website with HTTPS. Path-relative URLs are the only option when you want to generate static files (e.g. documentation) that can be downloaded as an HTML archive. Such use-cases are currently not possible with symfony.

    The calculation of the relative path based on the request path and target path is hightly unit tested. So it is really equivalent. I found several implemenations on the internet but none of them worked in all cases. Mine is pretty short and works.

    I also added an optional parameter to the twig `path` function, so this feature can also be used in twig templates.

    Ref: This implements path-relative URLs as suggested in #3908.

    <del>[BC BREAK] The signature of UrlGeneratorInterface::generate changed to support scheme-relative and path-relative URLs. The core UrlGenerator is BC and does not break anything, but users who implemented their own UrlGenerator need to be aware of this change. See UrlGenerator::convertReferenceType.</del>

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-16T09:56:56Z

    @Tobion For completeness, you should add the option to the `url` and `asset` twig functions/template helpers.

    ---------------------------------------------------------------------------

    by stof at 2012-04-16T10:46:06Z

    @jalliot adding the option to ``url`` does not make any sense. The difference between ``path`` and ``url`` is that ``path`` generates a path and ``url`` generates an absolute url (thus including the scheme and the hostname)

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-16T12:27:49Z

    @stof I guess jalliot meant we could then generate scheme-relative URLs with `url`. Otherwise this would have no equivalent in twig.

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-16T12:34:08Z

    @stof Yep I meant what @Tobion said :)

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-18T11:57:04Z

    The $relative parameter I added besides the existing $absolute parameter of the `->generate` method was not clear enough. So I merged those into a different parameter `referenceType`. I adjusted all parts of symfony to use the new signature. And also made the default `UrlGenerator` implementation BC with the old style. So almost nobody will recognize a change. The only BC break would be for somebody who implemented his own `UrlGenerator` and did not call the parent default generator.
    Using `referenceType` instead of a simple Boolean is much more flexible. It will for example allow a custom generator to support a new reference type like http://en.wikipedia.org/wiki/CURIE

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-18T13:34:58Z

    ping @schmittjoh considering your https://github.com/schmittjoh/JMSI18nRoutingBundle/blob/master/Router/I18nRouter.php would need a tiny change

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-04-18T13:37:39Z

    Can you elaborate the necessary change?

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-18T13:51:10Z

    This PR changes the signature of `generate` to be able to generate path-relative and scheme-relative URLs. So it needs to be
    `public function generate($name, $parameters = array(), $referenceType = self::ABSOLUTE_PATH)` and your implementation would need to change `if ($absolute && $this->hostMap) {` to `if (self::ABSOLUTE_URL === $referenceType && $this->hostMap) {`
    I can do a PR if this gets merged.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-04-18T13:52:14Z

    If I understand correctly, the old parameter still works, no?

    edit: Ah, ok I see what you mean now.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-18T13:56:33Z

    Yeah the old parameter still works but $absolute would also evaluate to true (a string) in your case for non-absolute URLs, i.e. paths.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-19T21:09:46Z

    ping @fabpot

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-20T04:30:18Z

    Let's discuss that feature for 2.2.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-20T10:40:59Z

    What are your objections against it? It's already implemented, it works and it adds support for things that are part of a web standard. The BC break is tiny at the moment (almost nobody is affected) because the core UrlGenerator works as before. But if we waited for 2.2 it will be much harder to make the transition because 2.1 is LTS. So I think is makes sense to add it now. Furthermore it makes it much more future-proof as custom generators can more easiliy add support for other link types like CURIE. At the moment a Boolean for absolute URLs is simply too limited and also somehow inconsistent because $absolute = false stands for an absolute path. You see the awkwardness in this naming.

    Btw, I added a note in the changelog. And I will add documentation of this feature in symfony-docs once this is merged.

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-20T12:14:32Z

    nobody has ever said that 2.1 would be LTS. Actually, I think we are going to wait for 2.3 for LTS.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-20T12:27:18Z

    Well what I meant is, the longer we wait with this, the harder to apply it.
    In 04ac1fdba2498 you modified `generate` signature for better extensibility that is not even made use of. I think changing `$abolute` param goes in the same direction and has direct use.

    I'd like to know your reason to wait for 2.2. Not enough time to review it, or afraid of breaking something, or marketing for 2.2?

    ---------------------------------------------------------------------------

    by stof at 2012-04-20T16:28:27Z

    @Tobion the issue is that merging new features forces to postpone the release so that it is tested by enough devs first to be sure there is no blocking bug in it. Big changes cannot be merged when we are hunting the remaining bugs to be able to release.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-04-20T16:42:11Z

    Considering the changes that have been made to the Form component, and are still being made, I think this is in comparison to that a fairly minor change.

    Maybe a clearer guideline on the release process, or the direction would help, and avoid confusion, or wrong expectations on contributors' part.

    ---------------------------------------------------------------------------

    by Tobion at 2012-10-05T13:52:11Z

    @fabpot this is ready. So if you agree with it, I would create a documentation PR.

    ---------------------------------------------------------------------------

    by stof at 2012-10-13T16:09:47Z

    @fabpot what do you think about this PR ?

    ---------------------------------------------------------------------------

    by Crell at 2012-11-01T16:05:01Z

    This feels like it's overloading the generate() method to do double duty: One, make a URl based on a route.  Two, make a  URI based on a URI snippet.  Those are two separate operations.  Why not just add a second method that does the second operation and avoid the conditionals?  (We're likely to do that in Drupal for our own generator as well.)

    ---------------------------------------------------------------------------

    by Tobion at 2012-11-01T16:38:39Z

    @crell: No, you must have misunderstood something. The generate method still only generates a URI based on a route. The returned URI reference can now also be a relative path and a network path. Thats all.

    ---------------------------------------------------------------------------

    by Tobion at 2012-12-13T18:30:28Z

    @fabpot this is ready. It is fully BC! I also improved phpdoc considerably.

    ---------------------------------------------------------------------------

    by Tobion at 2012-12-14T20:51:38Z

    @fabpot Do you want me to write documentation for it? I would also be interested to write about the new features of the routing component in general. I wanted to do that anyway and it would probably be a good fit for your "new in symfony" articles.

    ---------------------------------------------------------------------------

    by fabpot at 2012-12-14T20:58:16Z

    Im' going to review this PR in the next coming days. And to answer your second question, more documentation or better documentation is always a good thing, so go for it.

    ---------------------------------------------------------------------------

    by Tobion at 2013-01-02T21:50:20Z

    @fabpot ping. I added changelog entries.