commit 0c5b83ab95363b8af57416f75f250c3ef6ffee8d
Merge: d602924 98621f4
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Apr 28 12:16:40 2016 +0200

    feature #18359 [Form] [DoctrineBridge] optimized LazyChoiceList and DoctrineChoiceLoader (HeahDude)

    This PR was merged into the 3.1-dev branch.

    Discussion
    ----------

    [Form] [DoctrineBridge] optimized LazyChoiceList and DoctrineChoiceLoader

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | yes
    | Tests pass?   | yes
    | Fixed tickets | ~
    | License       | MIT
    | Doc PR        | ~

    Problem
    ======
    Actually we got a circular dependency:

    | Object | Return
    |----------|-------------|
    |`DefaultChoiceListFactory::createListFromLoader()` (decorated) | `LazyChoiceList` with loader and resolved `$value`
    | `LazyChoiceList::get*` | `DoctrineChoiceLoader` with resolved `$value`
    `DoctrineChoiceLoader::loadChoiceList()` | (decorated) `DefaultChoiceListFactory` with loaded choices and resolved `$value`
    `DefaultChoiceListFactory::createListFromChoices()` | `ArrayChoiceList` with `resolved `$value`

    With this refactoring, the `DoctrineChoiceLoader` is no longer dependant to the factory, the `ChoiceLoaderInterface::loadChoiceList()` must return a `ChoiceListInterface` but should not need a decorated factory while `$value` is already resolved. It should remain lazy IMHO.

    Solution
    ======

    | Object | Return |
    |----------|-----------|
    | `DefaultChoiceListFactory::createListFromLoader()` | `LazyChoiceList` with loader and resolved `$value`
    | `LazyChoiceList::get*()` | `DoctrineChoiceLoader` with resolved `$value`
    | `DoctrineChoiceLoader::loadChoiceList()` | `ArrayChoiceList` with resolved `$value`.

    Since `choiceListFactory` is a private property, this change should be safe regarding BC.

    To justify this change, I've made some blackfire profiling.
    You can see my [branch of SE here](https://github.com/HeahDude/symfony-standard/tree/test/optimize-doctrine_choice_loader) and the [all in file test implementation](https://github.com/HeahDude/symfony-standard/blob/test/optimize-doctrine_choice_loader/src/AppBundle/Controller/DefaultController.php).

    Basically it loads a form with 3 `EntityType` fields with different classes holding 50 instances each.

    (INIT events are profiled with an empty cache)

    When | What | Diff (SE => PR)
    --------|-------|------
    INIT (1) | build form (load types) | [see](https://blackfire.io/profiles/compare/061d5d28-15c6-4e01-b8c0-3edc9cb8daf0/graph)
    INIT (2) | build view (load choices) | [see](https://blackfire.io/profiles/compare/04f142a8-d886-405a-be4d-636ba82d8acd/graph)
    CACHED | build form (load types) | [see](https://blackfire.io/profiles/compare/293b27b6-aa58-42ae-bafb-655513201505/graph)
    CACHED | build view (load choices) | [see](https://blackfire.io/profiles/compare/e5b37dfe-cc9e-498f-b98a-7448830ad190/graph)
    SUBMIT | build form (load types) | [see](https://blackfire.io/profiles/compare/7f3baea9-0d27-46b6-8c24-c577742382dc/graph)
    SUBMIT | handle request (load choices) | [see](https://blackfire.io/profiles/compare/8644ebfb-4397-495b-8f3d-1a6e1d7f8476/graph)
    SUBMIT | build view (load values) | [see](https://blackfire.io/profiles/compare/89c3cc7c-ea07-4300-91b3-99004cb58ea1/graph)

    (1):
    ![build_form-no_cache](https://cloud.githubusercontent.com/assets/10107633/14136166/b5a85eb8-f661-11e5-8556-3e0dcbfaf404.jpg)
    (2):
    ![build_view-no_cache](https://cloud.githubusercontent.com/assets/10107633/14136240/1162f3ee-f662-11e5-834a-1ed1e519dc83.jpg)

    It can seem like 1 and 2 balance each other but it comes clear when comparing values:

    | -  | Build form | Build view |
    |-----|---------|--------------|
    | wall time | -88 ms | +9.71 ms
    | blocking I/O | -40 ms | +3.67 ms
    | cpu | -48 ms | +13.4 ms
    | memory | -4.03 MB | +236 kB
    | network | -203 B | +2.21 kB

    Commits
    -------

    98621f4 [Form] optimized LazyChoiceList
    86b2ff1 [DoctrineBridge] optimized DoctrineChoiceLoader