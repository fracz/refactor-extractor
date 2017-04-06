commit 194f8c0739f4fa8db9940679668834b8d7fbbe32
Merge: 3dc880f 47ebf08
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Nov 22 09:43:50 2011 +0100

    merged branch beberlei/DoctrineValidation (PR #2535)

    Commits
    -------

    47ebf08 Fix some bugs
    fad825e Add DoctrineValidationPass to DoctrineBundle#buildContainer
    a064acd Implement feature to add validations based on the Manager-Type (ORM, MongoDB, CouchDB)

    Discussion
    ----------

    [WIP] Validation on a Doctrine Manager Basis

    Hello,

    we have had problems before with validation that is "persistence" related. Unique-validators or any other validation that is based on services that depend on persistence.

    The problem is two-fold:

    1. In annotations you cannot define validators for all persistence layers you support, because then users need them all installed.
    2. In XML/YAML the same is true, since there is only one validation.xml or validation.yml file looked for.

    Now one solution is to have three model classes that extend from a base class to get around this (like FOSUserBundle does) but that is cumbersome. This PR provides a new solution that is Doctrine specific (and takes the responsibility out of the Core).

    Each Doctrine Bundle (ORM, CouchDB, MongoDB, PHPCR) can add this compiler pass with a manager type name:

        $container->addCompilerPass(new DoctrineValidationPass('orm'));

    This leads to the compiler pass searching for additional validation files "Resources/config/validation.orm.yml" and "Resources/configvalidation.orm.xml".

    My first idea was to put this into the Resources/config/doctrine folder as well, but then it is detected as mapping file of course.

    Regarding tests, this is not easily testable without a full fledged bundle setup, i tested this inside Acme Demo Bundle, however for a good unit-test we probably need a filesystem abstraction testing layer. Has anyone a good idea how to test this without having to setup another test-bundle? I can't use the one from DoctrineBundle since this code is in the Bridge.

    ---------------------------------------------------------------------------

    by fabpot at 2011/11/13 23:12:06 -0800

    @beberlei: Is it still WIP?

    ---------------------------------------------------------------------------

    by beberlei at 2011/11/15 10:47:49 -0800

    @fabpot it is complete, but it has no tests, that was the WIP part. :-)

    ---------------------------------------------------------------------------

    by mvrhov at 2011/11/15 23:56:11 -0800

    I wanted to refactor how validation is managed today, so it could do one validation file per class, same as with Doctrine but @stof pointed me to this PR. I still find this a great idea as the validation is easier to find.

    ```php
            foreach ($container->getParameter('kernel.bundles') as $bundle) {
                $reflection = new \ReflectionClass($bundle);
                $bundleDir = dirname($reflection->getFilename());

                //check for per class validation files
                if (is_dir($dir = $bundleDir . '/Resources/config/validation')) {
                    $finder = new Finder();
                    $finder
                        ->name('*'.$extension)
                        ->in($dir);

                    foreach ($finder as $file) {
                        $files[] = realpath($file);
                        $container->addResource(new FileResource($file));
                    }
                }

                //global validation file?
                if (is_file($file = $bundleDir . '/Resources/config/validation'.$extension)) {
                    $files[] = realpath($file);
                    $container->addResource(new FileResource($file));
                }
            }
    ```