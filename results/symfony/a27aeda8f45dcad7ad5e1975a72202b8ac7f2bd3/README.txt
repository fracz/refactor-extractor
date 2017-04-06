commit a27aeda8f45dcad7ad5e1975a72202b8ac7f2bd3
Merge: 2dcc448 cd7835d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jul 13 21:26:31 2012 +0200

    merged branch bschussek/performance (PR #4882)

    Commits
    -------

    cd7835d [Form] Cached the form type hierarchy in order to improve performance
    2ca753b [Form] Fixed choice list hashing in DoctrineType
    2bf4d6c [Form] Fixed FormFactory not to set "data" option if not explicitely given
    7149d26 [Form] Removed invalid PHPDoc text

    Discussion
    ----------

    [Form] WIP Improved performance of form building

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: **Update the Silex extension**

    This PR is work in progress and up for discussion. It increases the performance of FormFactory::createForm() on a specific, heavy-weight form from **0.848** to **0.580** seconds.

    Before, the FormFactory had to traverse the hierarchy and calculate the default options of each FormType everytime a form was created of that type.

    Now, FormTypes are wrapped within instances of a new class `ResolvedFormType`, which caches the parent type, the type's extensions and its default options.

    The updated responsibilities: `FormFactory` is a registry and proxy for `ResolvedFormType` objects, `FormType` specifies how a form can be built on a specific layer of the type hierarchy (e.g. "form", or "date", etc.) and `ResolvedFormType` *does the actual building* across all layers of the hierarchy (by delegating to the parent type, which delegates to its parent type etc.).

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-12T18:25:40Z

    Maybe ResolvedFormType

    ---------------------------------------------------------------------------

    by jmather at 2012-07-13T02:56:38Z

    I really like ResolvedFormType. That's the naming method I took for my tag parser that handes the same conceptual issue.

    ---------------------------------------------------------------------------

    by axelarge at 2012-07-13T05:25:00Z

    ResolvedFormType sounds very clear.
    This change is great and I desperately hope to see more of this kind

    ---------------------------------------------------------------------------

    by Baachi at 2012-07-13T06:41:26Z

    Yes `ResolvedFormType` sounds good :) :+1:

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-13T07:11:33Z

    I like `ResolvedFormType` as well.

    ---------------------------------------------------------------------------

    by henrikbjorn at 2012-07-13T07:46:48Z

    :+1: `ResolvedFormType` :shipit:

    ---------------------------------------------------------------------------

    by stof at 2012-07-13T18:01:51Z

    This looks good to me