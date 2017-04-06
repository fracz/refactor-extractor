commit 140ed985be656c8b13dd698fb061a83ccb556526
Merge: 2c2daf1 a0ef101
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Wed Dec 30 16:04:43 2015 +0100

    feature #16747 [Form] Improved performance of ChoiceType and its subtypes (webmozart)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Form] Improved performance of ChoiceType and its subtypes

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    I found out today that, although CachingFactoryDecorator is part of Symfony 2.7, it is not configured to be used in the DI configuration. This simple in-memory cache improved the page load by 50% for one considerably large form with many (~600) choice/entity fields that I was working on today.

    Also, caching of query builders with parameters was broken, since the parameters are represented by objects. PHP's object hashes were used to calculate the cache keys, hence the cache always missed. I converted parameters to arrays for calculating the cache keys to fix this problem.

    Commits
    -------

    a0ef101 [Form] Improved performance of ChoiceType and its subtypes