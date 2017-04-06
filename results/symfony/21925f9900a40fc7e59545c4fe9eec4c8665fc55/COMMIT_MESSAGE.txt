commit 21925f9900a40fc7e59545c4fe9eec4c8665fc55
Merge: 28abff8 a80ef6b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 14 18:09:27 2012 +0200

    merged branch cvschaefer/master (PR #4920)

    Commits
    -------

    a80ef6b Fixed: The type name specified for the service propel.form.type.model does not match the actual name

    Discussion
    ----------

    [Propel1Bridge][Form] ModelType name is invalid

    Since 6489a65960a05b6a5a4f5c5074b941a6765c6381 "[Form] Added an exception for invalid type services" Symfony/Component/Form/Extension/DependencyInjection/DependencyInjectionExtension requires form type names to match the service name.

    This fixes exception "The type name specified for the service propel.form.type.model does not match the actual name"

    ---------------------------------------------------------------------------

    by willdurand at 2012-07-14T14:03:20Z

    :+1:

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-14T14:17:36Z

    I think it would be better to named it `propel_model`.

    ---------------------------------------------------------------------------

    by willdurand at 2012-07-14T14:20:05Z

    There is `entity` for Doctrine entities, not `doctrine_entity`. This should
    be the same for Propel...

    2012/7/14 Fabien Potencier <
    reply@reply.github.com
    >

    > I think it would be better to named it `propel_model`.
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/4920#issuecomment-6983217
    >

    ---------------------------------------------------------------------------

    by stloyd at 2012-07-14T14:22:34Z

    @fabpot @willdurand Or we should rename Doctrine one too, to be more consistent...

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-14T14:30:41Z

    We should definitely rename Doctrine too.

    ---------------------------------------------------------------------------

    by cvschaefer at 2012-07-14T14:34:51Z

    @fabpot Wouldn't that break all existing forms with doctrine entity form types?

    ---------------------------------------------------------------------------

    by stof at 2012-07-14T14:54:03Z

    It would break BC indeed, and changing the propel type name in the DIC config (which is the one used in before the latest refactoring) would also be a BC break.

    And btw, we also use ``entity`` in the SecurityBundle config, not ``doctrine_entity``

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-14T15:04:31Z

    You're right, let's not break BC for the sake of it.

    ---------------------------------------------------------------------------

    by stof at 2012-07-14T15:13:23Z

    Ok, so this PR should be merged as is, as I guess more people are using the Propel type in a PropelBundle context than with a standalone Form component (which would have used the getName method)

    ---------------------------------------------------------------------------

    by cvschaefer at 2012-07-14T15:14:44Z

    +1