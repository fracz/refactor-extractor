commit 83de4c2b0209adf807383c4f440d0b36e7aaa04c
Merge: d45a76b 0159358
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Nov 13 14:20:12 2012 +0100

    merged branch fabpot/public-resources (PR #6000)

    This PR was merged into the master branch.

    Commits
    -------

    0159358 refactored CSS, images, templates included in the built-in bundles
    812b9b1 replace _ in stylesheets (ids and classes) by - (should be consistent across the whole framework now)
    983b2b5 uniformized styles
    e0aab40 renamed sf-exceptionreset to sf-reset

    Discussion
    ----------

    Public resources refactoring

    The first 3 commits are just cosmetic ones.

    The last one refactors CSS, images, and templates included in the built-in bundles. Right now, everything is tied to the exception pages, but the code can be used standalone.

    So, the goal is to make things more decoupled and more reusable across different bundles. That way, a bundle can provide pages that look like the other ones in Symfony without the need to duplicate code.

    See the associated PR for the distribution bundle to see an example.

    If you want to have a look at the last commit (not sure if it is worth it), you probably want to append ?w=1 to the URL to avoid too much whitespace noise.

    ---------------------------------------------------------------------------

    by pborreli at 2012-11-13T09:38:00Z

    congrats ! #6000

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-13T09:38:39Z

    A simple usage example:

    ```jinja
    {% extends "TwigBundle::layout.html.twig" %}

    {% block body %}
        <div class="block">
            FOOBAR
        </div>
    {% endblock %}
    ```