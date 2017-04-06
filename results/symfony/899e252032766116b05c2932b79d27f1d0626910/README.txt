commit 899e252032766116b05c2932b79d27f1d0626910
Merge: d12f5b2 887c0e9
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Dec 31 08:12:02 2011 +0100

    merged branch symfony/streaming (PR #2935)

    Commits
    -------

    887c0e9 moved EngineInterface::stream() to a new StreamingEngineInterface to keep BC with 2.0
    473741b added the possibility to change a StreamedResponse callback after its creation
    8717d44 moved a test in the constructor
    e44b8ba made some cosmetic changes
    0038d1b [HttpFoundation] added support for streamed responses

    Discussion
    ----------

    [HttpFoundation] added support for streamed responses

    To stream a Response, use the StreamedResponse class instead of the
    standard Response class:

        $response = new StreamedResponse(function () {
            echo 'FOO';
        });

        $response = new StreamedResponse(function () {
            echo 'FOO';
        }, 200, array('Content-Type' => 'text/plain'));

    As you can see, a StreamedResponse instance takes a PHP callback instead of
    a string for the Response content. It's up to the developer to stream the
    response content from the callback with standard PHP functions like echo.
    You can also use flush() if needed.

    From a controller, do something like this:

        $twig = $this->get('templating');

        return new StreamedResponse(function () use ($templating) {
            $templating->stream('BlogBundle:Annot:streamed.html.twig');
        }, 200, array('Content-Type' => 'text/html'));

    If you are using the base controller, you can use the stream() method instead:

        return $this->stream('BlogBundle:Annot:streamed.html.twig');

    You can stream an existing file by using the PHP built-in readfile() function:

        new StreamedResponse(function () use ($file) {
            readfile($file);
        }, 200, array('Content-Type' => 'image/png');

    Read http://php.net/flush for more information about output buffering in PHP.

    Note that you should do your best to move all expensive operations to
    be "activated/evaluated/called" during template evaluation.

    Templates
    ---------

    If you are using Twig as a template engine, everything should work as
    usual, even if are using template inheritance!

    However, note that streaming is not supported for PHP templates. Support
    is impossible by design (as the layout is rendered after the main content).

    Exceptions
    ----------

    Exceptions thrown during rendering will be rendered as usual except that
    some content might have been rendered already.

    Limitations
    -----------

    As the getContent() method always returns false for streamed Responses, some
    event listeners won't work at all:

    * Web debug toolbar is not available for such Responses (but the profiler works fine);
    * ESI is not supported.

    Also note that streamed responses cannot benefit from HTTP caching for obvious
    reasons.

    ---------------------------------------------------------------------------

    by Seldaek at 2011/12/21 06:34:13 -0800

    Just an idea: what about exposing flush() to twig? Possibly in a way that it will not call it if the template is not streaming. That way you could always add a flush() after your </head> tag to make sure that goes out as fast as possible, but it wouldn't mess with non-streamed responses. Although it appears flush() doesn't affect output buffers, so I guess it doesn't need anything special.

    When you say "ESI is not supported.", that means only the AppCache right? I don't see why this would affect Varnish, but then again as far as I know Varnish will buffer if ESI is used so the benefit of streaming there is non-existent.

    ---------------------------------------------------------------------------

    by cordoval at 2011/12/21 08:04:21 -0800

    wonder what the use case is for streaming a response, very interesting.

    ---------------------------------------------------------------------------

    by johnkary at 2011/12/21 08:19:48 -0800

    @cordoval Common use cases are present fairly well by this RailsCast video: http://railscasts.com/episodes/266-http-streaming

    Essentially it allows faster fetching of web assets (JS, CSS, etc) located in the &lt;head>&lt;/head>, allowing those assets to be fetched as soon as possible before the remainder of the content body is computed and sent to the browser. The end goal is to improve page load speed.

    There are other uses cases too like making large body content available quickly to the service consuming it. Think if you were monitoring a live feed of JSON data of newest Twitter comments.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/21 08:54:35 -0800

    How does this relate the limitations mentioned in:
    http://yehudakatz.com/2010/09/07/automatic-flushing-the-rails-3-1-plan/

    Am I right to understand that due to how twig works we are not really streaming the content pieces when we call render(), but instead the entire template with its layout is rendered and only then will we flush? or does it mean that the render call will work its way to the top level layout template and form then on it can send the content until it hits another block, which it then first renders before it continues to send the data?

    ---------------------------------------------------------------------------

    by stof at 2011/12/21 09:02:53 -0800

    @lsmith77 this is why the ``stream`` method calls ``display`` in Twig instead of ``render``. ``display`` uses echo to print the output of the template line by line (and blocks are simply method calls in the middle). Look at your compiled templates to see it (the ``doDisplay`` method)
    Rendering a template with Twig simply use an output buffer around the rendering.

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/21 09:24:33 -0800

    @lsmith77: We don't have the Rails problem thanks to Twig as the order of execution is the right one by default (the layout is executed first); it means that we can have the flush feature without any change to how the core works. As @stof mentioned, we are using `display`, not `render`, so we are streaming your templates for byte one.

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/21 09:36:41 -0800

    @Seldaek: yes, I meant ESI with the PHP reverse proxy.

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/21 09:37:34 -0800

    @Seldaek: I have `flush()` support for Twig on my todo-list. As you mentioned, It should be trivial to implement.

    ---------------------------------------------------------------------------

    by fzaninotto at 2011/12/21 09:48:18 -0800

    How do streaming responses deal with assets that must be called in the head, but are declared in the body?

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/21 09:52:12 -0800

    @fzaninotto: What do you mean?

    With Twig, your layout is defined with blocks ("holes"). These blocks are overridden by child templates, but evaluated as they are encountered in the layout. So, everything works as expected.

    As noted in the commit message, this does not work with PHP templates for the problems mentioned in the Rails post (as the order of execution is not the right one -- the child template is first evaluated and then the layout).

    ---------------------------------------------------------------------------

    by fzaninotto at 2011/12/21 10:07:35 -0800

    I was referring to using Assetic. Not sure if this compiles to Twig the same way as javascript and stylesheet blocks placed in the head - and therefore executed in the right way.

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/21 10:34:59 -0800

    @Seldaek: I've just added a `flush` tag in Twig 1.5: https://github.com/fabpot/Twig/commit/1d6dfad4f55c721ec7b6e674ef225b8442bce896

    ---------------------------------------------------------------------------

    by catchamonkey at 2011/12/21 13:29:22 -0800

    I'm really happy you've got this into the core, it's a great feature to have! Good work.