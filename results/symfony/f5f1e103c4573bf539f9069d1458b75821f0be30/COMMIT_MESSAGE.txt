commit f5f1e103c4573bf539f9069d1458b75821f0be30
Merge: 7381d8e 6f6139c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Aug 23 10:17:40 2016 -0700

    feature #19687 [FrameworkBundle] Use relative paths in templates paths cache (tgalopin)

    This PR was merged into the 3.2-dev branch.

    Discussion
    ----------

    [FrameworkBundle] Use relative paths in templates paths cache

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | yes
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #3079
    | License       | MIT
    | Doc PR        | -

    This implements the usage of relative paths instead of absolute ones in `var/cache/*/templates.php`, important for ability to build the cache in a different context than where it will be used.

    This PR transforms the following `templates.php`:

    ``` php
    <?php return array (
      ':default:index.html.twig' => '/home/tgalopin/www/symfony-standard/app/Resources/views/default/index.html.twig',
      '::base.html.twig' => '/home/tgalopin/www/symfony-standard/app/Resources/views/base.html.twig',
    );
    ```

    Into:

    ``` php
    <?php return array (
      ':default:index.html.twig' => __DIR__.'/../../../app/Resources/views/default/index.html.twig',
      '::base.html.twig' => __DIR__.'/../../../app/Resources/views/base.html.twig',
    );
    ```

    I also added tests for the TemplateCachePathsWarmer and improved tests for the TemplateLocator.

    Commits
    -------

    6f6139c [FrameworkBundle] Use relative paths in templates paths cache