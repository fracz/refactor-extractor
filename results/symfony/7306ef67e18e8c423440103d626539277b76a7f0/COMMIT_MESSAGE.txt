commit 7306ef67e18e8c423440103d626539277b76a7f0
Merge: 9622565 530bd22
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Sep 27 14:18:54 2012 +0200

    merged branch xavierlacot/ticket_5596 (PR #5597)

    Commits
    -------

    530bd22 fixed issue #5596 (Broken DOM with the profiler's toolbar set in position top)

    Discussion
    ----------

    fixed issue #5596 (Broken DOM with the profiler's toolbar set in position top)

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: #5596
    Todo: -
    License of the code: MIT
    Documentation PR: -

    Whatever the toolbar position, the html code associated to it may be placed at the end of the page (and this will be better from a webperf point of view).

    ---------------------------------------------------------------------------

    by stof at 2012-09-25T17:47:53Z

    The spacer div will not be in the right place when using ``position: top`` as it will be at the end of the body.

    ---------------------------------------------------------------------------

    by pborreli at 2012-09-25T17:53:03Z

    @stof what is the spacer div ? i guess all the profiler is in absolute so i guess it should work (and I'm sure @xavierlacot already tested it :))

    ---------------------------------------------------------------------------

    by pborreli at 2012-09-25T17:55:55Z

    @xavierlacot so maybe we should refactor

    ```
    $pos = $posrFunction($content, '</body>');

    $content = $substrFunction($content, 0, $pos).$toolbar.$substrFunction($content, $pos);
    ```
    with something like
    ```
    $content = str_replace('</body>', $toolbar.'</body>', $content);
    ```
    What do you think ?

    ---------------------------------------------------------------------------

    by stof at 2012-09-25T17:58:35Z

    @pborreli The toolbar is in position fixed. But to avoid hiding some of the content of your page, another div is added on with a margin, to force keeping some space after the content for the toolbar. With this change, the toolbar HTML is always at the end, so the 40px space is always added at the bottom of the page even if the toolbar is added at the top.

    ---------------------------------------------------------------------------

    by pborreli at 2012-09-25T18:03:08Z

    @stof maybe we should just fix the body/html margin-top in that case no ? or find a better solution, anyway I think the actual way to do it is bad, `<body>` and `</body>` are not even mandatory in html5, IMHO i would just put it at the end of file without any check, then fix it with some css and/or js

    ---------------------------------------------------------------------------

    by stof at 2012-09-25T18:06:58Z

    @pborreli Putting it at the end after the closing ``<html>`` tag would make the page invalid for people defining the markup fully. It is a bad idea.
    And anyway, detecting the body tag is still important, to avoid injecting the toolbar in partial page content (be it ESI requests or parts loaded through AJAX).

    Oh, and ``$content = str_replace('</body>', $toolbar.'</body>', $content);`` would not fix #5596 but make it worse: it would also inject the toolbar in the head even when being placed at the bottom (keeping it at the bottom too).

    ---------------------------------------------------------------------------

    by pborreli at 2012-09-25T18:18:46Z

    @stof for detecting ajax you already have `if ($request->isXmlHttpRequest()) {` called few lines before,
    the proposal for `$content = str_replace('</body>', $toolbar.'</body>', $content);` was only a refactoring for the above PR

    ---------------------------------------------------------------------------

    by stof at 2012-09-25T18:26:34Z

    @pborreli ESI requests are not AJAX requests. So simply appending at the end would still break them.

    And your code is *not* a refactoring. Your ``str_replace`` will replace **all** occurences of ``</body>``, not just the last one. See the related issue to understand why it makes a difference

    ---------------------------------------------------------------------------

    by pborreli at 2012-09-25T18:38:11Z

    ok I'm all wrong.

    ---------------------------------------------------------------------------

    by xavierlacot at 2012-09-25T18:51:42Z

    @stof, please review the last commit, which injects the wdt container at the top of the page in javascript, using the browser's DOM capacities. This fixes the spacer problem that you noticed.

    ---------------------------------------------------------------------------

    by stof at 2012-09-25T18:55:51Z

    Well, you are now breaking things when the spacer should be at the bottom as you are always putting the spacer at the top.

    ---------------------------------------------------------------------------

    by stloyd at 2012-09-25T19:06:14Z

    @xavierlacot Pass `position` variable to template and change:

    ```diff
    -        document.body.insertBefore(sfwdt, document.body.firstChild);
    +        {% if position == 'bottom' -%}
    +            document.body.appendChild(sfwdt);
    +        {%- else -%}
    +            document.body.insertBefore(sfwdt, document.body.firstChild);
    +        {%- endif %}

    ---------------------------------------------------------------------------

    by stof at 2012-09-25T20:18:31Z

    @xavierlacot could you squash your commits ?

    ---------------------------------------------------------------------------

    by xavierlacot at 2012-09-25T21:32:47Z

    @stof done. Thanks for the review :-)