commit 019625d34e0f0f2e00e874ef84a35ab8f6d4c7ae
Merge: 6c256b0 24b764e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 21 19:51:42 2012 +0200

    merged branch bschussek/options_performance (PR #5004)

    Commits
    -------

    24b764e [Form] Fixed issues mentioned in the PR
    9216816 [Form] Turned Twig filters into tests
    310f985 [Form] Added a layer of 2.0 BC methods to FormView and updated UPGRADE and CHANGELOG
    5984b18 [Form] Precalculated the closure for deciding whether a choice is selected (PHP +30ms, Twig +30ms)
    5dc3c39 [Form] Moved the access to templating helpers out of the choice loop for performance reasons (PHP +100ms)
    0ef9acb [Form] Moved the method isChoiceSelected() to the ChoiceView class (PHP +150ms)
    8b72766 [Form] Tweaked the generation of option tags for performance (PHP +200ms, Twig +50ms)
    400c95b [Form] Replace methods in ChoiceView by public properties (PHP +100ms, Twig +400ms)
    d072f35 [Form] The properties of FormView are now accessed directly in order to increase performance (PHP +200ms, Twig +150ms)

    Discussion
    ----------

    [Form] Made FormView and ChoiceView properties public for performance reasons

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: **yes**
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    This PR changes the access to properties of `FormView` and `ChoiceView` objects from getters to direct property accesses. On [my example form](http://advancedform.gpserver.dk/app_dev.php/taxclasses/1) this improves rendering performance for **300ms** with PHP templates and **550ms** with Twig on my local machine.

    Unfortunately, this breaks BC both with 2.0 and with the current master in Form Types and PHP templates. Twig templates are not affected by this change.

    2.0:
    ```
    $formView->set('my_var', 'foobar');
    $formView->get('my_var');
    $formView->getChild('childName');
    $formView['childName'];
    ```

    master:
    ```
    $formView->setVar('my_var', 'foobar');
    $formView->getVar('my_var');
    $formView->get('childName');
    $formView['childName'];
    ```

    this PR:
    ```
    $formView->vars['my_var'] = 'foobar';
    $formView->vars['my_var'];
    $formView->children['childName'];
    $formView['childName'];
    ```

    Should we add methods to keep BC with 2.0?

    The second part of this PR contains improvements to the rendering of choice fields. These gain another **~500ms** for PHP templates and **80ms** for Twig. These improvements are BC, unless you overwrote the block "choice_widget_options" in your form themes which then needs to be adapted.

    **Update:**

    The PR now includes a BC layer for 2.0.

    ---------------------------------------------------------------------------

    by stof at 2012-07-21T11:37:41Z

    @bschussek couldn't we keep the getters and setters for BC even if the rendering accesses the public properties directly ?

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-21T11:52:33Z

    @stof A BC layer for 2.0 is now included. People who upgraded to master already unfortunately need to adapt their code.

    ---------------------------------------------------------------------------

    by sstok at 2012-07-21T12:40:57Z

    :+1: