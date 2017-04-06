commit d55877a7b46f061a16dcf9ca37f4a63447b4ac59
Merge: d9181ba 0992032
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 18 15:27:58 2013 +0100

    merged branch Tobion/trans-refactor (PR #7058)

    This PR was merged into the 2.2 branch.

    Commits
    -------

    0992032 [Translator] fix metadata
    3b71000 [Translator] fix typecast in transChoice
    88f98c9 [Translator] optimized adding of resources and saving a call to array_unique
    e88bf7b [Translator] fix phpdoc of MessageCatalogueInterface::add and ::replace
    c97ee8d [Translator] mention that the message id may also be an object that can be cast to string in TranslatorInterface and fix the IdentityTranslator that did not respect this
    5a36b2d [Translator] fix MessageCatalogueInterface::getFallbackCatalogue that can return null
    d1c34e8 [Translator] coding style

    Discussion
    ----------

    [Translator] several fixes and refactorings

    Reasoning see individual commits.

    BC break: no <del>yes because I added an array typehint to `MessageCatalogueInterface::add` and `::replace` since it's required. I could remove the typhint again so there would be no bc break, but IMO having it is much more explicit and consistent as there are already other array typhints as in the constructor.</del>

    ---------------------------------------------------------------------------

    by Tobion at 2013-02-14T09:36:35Z

    @fabpot removed typehint and code movement. Added 2 more commits.

    ---------------------------------------------------------------------------

    by Tobion at 2013-03-04T16:14:37Z

    @fabpot ping