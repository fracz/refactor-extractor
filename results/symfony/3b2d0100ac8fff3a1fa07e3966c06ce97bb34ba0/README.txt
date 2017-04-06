commit 3b2d0100ac8fff3a1fa07e3966c06ce97bb34ba0
Merge: ebd55fc 284dc75
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Oct 30 15:37:44 2015 -0700

    bug #16294 [PropertyAccess] Major performance improvement (dunglas)

    This PR was squashed before being merged into the 2.3 branch (closes #16294).

    Discussion
    ----------

    [PropertyAccess] Major performance improvement

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #16179
    | License       | MIT
    | Doc PR        | n/a

    This PR improves performance of the PropertyAccess component of ~70%.

    The two main changes are:

    * caching the `PropertyPath` initialization
    * caching the guessed access strategy

    This is especially important for the `ObjectNormalizer` (Symfony Serializer) and the JSON-LD normalizer ([API Platform](https://api-platform.com)) because they use the `PropertyAccessor` class in large loops (ex: normalization of a list of entities).

    Here is the Blackfire comparison: https://blackfire.io/profiles/compare/c42fd275-2b0c-4ce5-8bf3-84762054d31e/graph

    The code of the benchmark I've used (with Symfony 2.3 as dependency):

    ```php
    <?php

    require 'vendor/autoload.php';

    class Foo
    {
        private $baz;
        public $bar;

        public function getBaz()
        {
            return $this->baz;
        }

        public function setBaz($baz)
        {
            $this->baz = $baz;
        }
    }

    use Symfony\Component\PropertyAccess\PropertyAccess;

    $accessor = PropertyAccess::createPropertyAccessor();

    $start = microtime(true);

    for ($i = 0; $i < 10000; ++$i) {
        $foo = new Foo();
        $accessor->setValue($foo, 'bar', 'Lorem');
        $accessor->setValue($foo, 'baz', 'Ipsum');
        $accessor->getValue($foo, 'bar');
        $accessor->getValue($foo, 'baz');
    }

    echo 'Time: '.(microtime(true) - $start).PHP_EOL;
    ```

    This PR also adds an optional support for Doctrine cache to keep access information across requests and improve the overall application performance (even outside of loops).

    Commits
    -------

    284dc75 [PropertyAccess] Major performance improvement