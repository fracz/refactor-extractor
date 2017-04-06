commit c223eca2ad44d3b5e3af112bdc195d8cae57ad4a
Merge: c4a2a2b bcc5552
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jan 7 17:03:02 2013 +0100

    merged branch bschussek/issue6558_2 (PR #6580)

    This PR was merged into the master branch.

    Commits
    -------

    bcc5552 [Form] Protected methods in FormConfigBuilder and FormBuilder from being called when it is turned into a FormConfigInterface instance
    fee1bf5 [Form] Introduced base ExceptionInterface

    Discussion
    ----------

    [Form] Protected methods in FormConfigBuilder and FormBuilder

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: yes
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -
    License of the code: MIT
    Documentation PR: -

    When a form is created, a `FormConfigInterface` instance is passed to the constructor of `Form` and then accessible via `getConfig()`. For performance reasons, `FormBuilder` also implements `FormConfigInterface` and is passed here directly instead of copying its values to a new `FormConfig` instance, although protected from further modifications.

    In addition to the already existing guard clauses in the setters, this PR adds guard clauses to the getters of `FormBuilder` to prevent misuse of the "config" object giving unexpected results.

    Additionally, this PR introduces first improvements of the form's `Exception\` namespace as described in  #6549.

    ---------------------------------------------------------------------------

    by mvrhov at 2013-01-05T18:19:04Z

    @bschussek I don't have the time to review this one, but please do not over optimize. We do a lot of weird things. e.g. I have the following code in one of my models.

    ```php
    public function validateEmail(ExecutionContextInterface $context)
    {
        if ((null !== $this->product) && (in_array($this->product->getType(), array(ProductTypeEnum::Software, ProductTypeEnum::Subscription)))) {
            /** @var $form Form */
            $form = $context->getRoot();
            if (($form instanceof Form) && ('foo' == $form->getConfig()->getOption('app_name'))) {
                $context->validate($this, 'email', 'Email');
            }
        }
    }
    ```

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-05T18:46:17Z

    @mvrhov Absolutely no problem. Most people will use methods from `FormConfigInterface` on the result of `getConfig()`, because that's the type hinted return value, and that's the way it should be. I don't even know how many people are aware that `getConfig()` returns the builder object.

    This PR is for rare cases where people write forward-incompatible hacks by accessing `FormBuilder` methods like `get()`, `getParent()` or the like on `getConfig()` (I don't know why anyone would do this, but you never know...)

    ---------------------------------------------------------------------------

    by vicb at 2013-01-07T11:08:34Z

    > Backwards compatibility break: no

    Really ?

    ---------------------------------------------------------------------------

    by vicb at 2013-01-07T12:26:22Z

    Changing `FormException` from a class to an interface is a BC break (`new FormException()`)

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T12:41:20Z

    @vicb Fair enough ;) I adapted the CHANGELOG and UPGRADE-2.2.

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-07T15:50:03Z

    This PR break the tests.

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T15:50:34Z

    I just fixed them and they run on my computer. I'm not sure why Travis fails again.

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T15:56:41Z

    Travis merged the wrong commit. It merged 42c8987f350650a42d8c968d32cde50fc5d548c0 - see the merge commit here 766f7fe958ea69a302bed6298e55a3001a51e2b8 - when it should have merged cd3f4f9dfe43a526c23f30e16354ff11489b9db1.