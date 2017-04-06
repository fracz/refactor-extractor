commit 57581193d3c9238d710fb74670aa190cee85dfba
Merge: a7ad32b 819fe34
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jul 6 12:00:31 2012 +0200

    merged branch Inori/refactor-naming (PR #4767)

    Commits
    -------

    819fe34 [Form] refactored variable name to be more consistent with rest of the naming

    Discussion
    ----------

    [Form] Refactored config variable naming to be more consistent

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes

    Since `clientTransformer` is renamed to `viewTransformer` everywhere, I think for consistency reasons config variable name should be changed too..

    ---------------------------------------------------------------------------

    by stloyd at 2012-07-06T07:31:39Z

    Maybe also rename of `normTransformers` to `modelTransformers` ? As this were changed too.

    ---------------------------------------------------------------------------

    by Inori at 2012-07-06T07:40:53Z

    @stloyd makes sense, done

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-06T08:24:43Z

    Thanks for the cleanup. Can you squash the commits and prefix the message with "[Form]" please?

    ---------------------------------------------------------------------------

    by Inori at 2012-07-06T08:43:49Z

    @bschussek done

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-06T09:31:04Z

    Thanks! @fabpot :+1: