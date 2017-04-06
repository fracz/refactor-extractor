commit 1dcc86dfc17203294a13e19f0190ded43347df11
Merge: e707760 8a4e164
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jun 13 13:59:19 2016 +0200

    feature #18332 [Form] added `CallbackChoiceLoader` and refactored ChoiceType's children (HeahDude)

    This PR was merged into the 3.2-dev branch.

    Discussion
    ----------

    [Form] added `CallbackChoiceLoader` and refactored ChoiceType's children

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | ~
    | License       | MIT
    | Doc PR        | todo

    Todo
    ====
    - [ ] Address a doc PR
    - [x] Update CHANGELOG

    Changes
    =======
     - 39e937f added `CallbackChoiceLoader` to lazy load choices with a simple callable.

     - 995dc56 refactored `CountryType`, `CurrencyType`, `LanguageType`, `LocaleType` and `TimezoneType` for better performance by implementing `ChoiceLoaderInterface` for lazy loading.

    Usage
    =====
    ```php
    use Symfony\Component\Form\ChoiceList\Loader\CallbackChoiceLoader;
    use Symfony\Component\Form\Extension\Core\Type\ChoiceType;

    $builder->add('constants', ChoiceType::class, array(
        'choice_loader' => new CallbackChoiceLoader(function() {
                return StaticClass::getConstants();
        },
    ));
    ```

    Commits
    -------

    8a4e164 [Form] implemented ChoiceLoaderInterface in children of ChoiceType
    afd7bf8 [Form] added `CallbackChoiceLoader`