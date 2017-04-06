commit 14ae4ecfd7b08f6c1db2ebd43092238f5e9323c1
Merge: d474ebd f092c7f
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Apr 23 14:40:40 2013 +0200

    merged branch bschussek/improve-name-parser (PR #7812)

    This PR was merged into the master branch.

    Discussion
    ----------

    [FrameworkBundle] Improved TemplateNameParser performance

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Performance test in symfony-standard:
    ```php
    <?php

    use Symfony\Bundle\FrameworkBundle\Templating\TemplateNameParser;

    require __DIR__.'/vendor/autoload.php';
    require __DIR__.'/app/AppKernel.php';

    $kernel = new AppKernel('dev', true);
    $kernel->boot();
    $parser = new TemplateNameParser($kernel);

    $time = microtime(true);

    for ($i = 0; $i < 50; ++$i) {
        $parser->parse("AcmeDemoBundle:Foo:bar$i.html.twig");
    }

    echo "Time:   " . (microtime(true) - $time)*1000 . "ms\n";
    echo "Memory: " . memory_get_peak_usage(true)/(1024*1024) . "MB\n";
    ```

    Before:
    ```
    Time:   3.80706787109ms
    Memory: 1.5MB
    ```

    After:
    ```
    Time:   3.13401222229ms
    Memory: 1.5MB
    ```

    Commits
    -------

    f092c7f [FrameworkBundle] Improved TemplateNameParser performance