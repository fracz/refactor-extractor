<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Tests\Component\ClassLoader;

use Symfony\Component\ClassLoader\ApcUniversalClassLoader;

class ApcUniversalClassLoaderTest extends \PHPUnit_Framework_TestCase
{

    protected function setUp()
    {
        if (!extension_loaded('apc')) {
            $this->markTestSkipped('The apc extension is not available.');
        }

        if (!(ini_get('apc.enabled') && ini_get('apc.enable_cli'))) {
            $this->markTestSkipped('The apc extension is available, but not enabled.');
        }
    }

    public function testConstructor()
    {
        $loader = new ApcUniversalClassLoader('test.prefix.');
        $loader->registerNamespace('Namespaced', __DIR__.DIRECTORY_SEPARATOR.'Fixtures');

        $this->assertEquals($loader->findFile('\Namespaced\Bar'), apc_fetch('test.prefix.\Namespaced\Bar'), '__construct() takes a prefix as its first argument');
    }

    /**
     * @dataProvider getLoadClassTests
     */
    public function testLoadClass($className, $testClassName, $message)
    {
        $loader = new ApcUniversalClassLoader('test.prefix.');
        $loader->registerNamespace('Namespaced', __DIR__.DIRECTORY_SEPARATOR.'Fixtures');
        $loader->registerPrefix('Pearlike_', __DIR__.DIRECTORY_SEPARATOR.'Fixtures');
        $loader->loadClass($testClassName);
        $this->assertTrue(class_exists($className), $message);
    }

    public function getLoadClassTests()
    {
        return array(
            array('\\Namespaced\\Foo', 'Namespaced\\Foo',   '->loadClass() loads Namespaced\Foo class'),
            array('\\Pearlike_Foo',    'Pearlike_Foo',      '->loadClass() loads Pearlike_Foo class'),
            array('\\Namespaced\\Bar', '\\Namespaced\\Bar', '->loadClass() loads Namespaced\Bar class with a leading slash'),
            array('\\Pearlike_Bar',    '\\Pearlike_Bar',    '->loadClass() loads Pearlike_Bar class with a leading slash'),
        );
    }

    /**
     * @dataProvider getLoadClassFromFallbackTests
     */
    public function testLoadClassFromFallback($className, $testClassName, $message)
    {
        $loader = new ApcUniversalClassLoader('test.prefix.');
        $loader->registerNamespace('Namespaced', __DIR__.DIRECTORY_SEPARATOR.'Fixtures');
        $loader->registerPrefix('Pearlike_', __DIR__.DIRECTORY_SEPARATOR.'Fixtures');
        $loader->registerNamespaceFallback(__DIR__.DIRECTORY_SEPARATOR.'Fixtures/fallback');
        $loader->registerPrefixFallback(__DIR__.DIRECTORY_SEPARATOR.'Fixtures/fallback');
        $loader->loadClass($testClassName);
        $this->assertTrue(class_exists($className), $message);
    }

    public function getLoadClassFromFallbackTests()
    {
        return array(
            array('\\Namespaced\\Baz',    'Namespaced\\Baz',    '->loadClass() loads Namespaced\Baz class'),
            array('\\Pearlike_Baz',       'Pearlike_Baz',       '->loadClass() loads Pearlike_Baz class'),
            array('\\Namespaced\\FooBar', 'Namespaced\\FooBar', '->loadClass() loads Namespaced\Baz class from fallback dir'),
            array('\\Pearlike_FooBar',    'Pearlike_FooBar',    '->loadClass() loads Pearlike_Baz class from fallback dir'),
        );
    }

    /**
     * @dataProvider getLoadClassNamespaceCollisionTests
     */
    public function testLoadClassNamespaceCollision($namespaces, $className, $message)
    {
        $loader = new ApcUniversalClassLoader('test.prefix.');
        $loader->registerNamespaces($namespaces);

        $loader->loadClass($className);
        $this->assertTrue(class_exists($className), $message);
    }

    public function getLoadClassNamespaceCollisionTests()
    {
        return array(
            array(
                array(
                    'NamespaceCollision\\A' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/alpha',
                    'NamespaceCollision\\A\\B' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/beta',
                ),
                'NamespaceCollision\A\Foo',
                '->loadClass() loads NamespaceCollision\A\Foo from alpha.',
            ),
            array(
                array(
                    'NamespaceCollision\\A\\B' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/beta',
                    'NamespaceCollision\\A' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/alpha',
                ),
                'NamespaceCollision\A\Bar',
                '->loadClass() loads NamespaceCollision\A\Bar from alpha.',
            ),
            array(
                array(
                    'NamespaceCollision\\A' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/alpha',
                    'NamespaceCollision\\A\\B' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/beta',
                ),
                'NamespaceCollision\A\B\Foo',
                '->loadClass() loads NamespaceCollision\A\B\Foo from beta.',
            ),
            array(
                array(
                    'NamespaceCollision\\A\\B' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/beta',
                    'NamespaceCollision\\A' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/alpha',
                ),
                'NamespaceCollision\A\B\Bar',
                '->loadClass() loads NamespaceCollision\A\B\Bar from beta.',
            ),
        );
    }

    /**
     * @dataProvider getLoadClassPrefixCollisionTests
     */
    public function testLoadClassPrefixCollision($prefixes, $className, $message)
    {
        $loader = new ApcUniversalClassLoader('test.prefix.');
        $loader->registerPrefixes($prefixes);

        $loader->loadClass($className);
        $this->assertTrue(class_exists($className), $message);
    }

    public function getLoadClassPrefixCollisionTests()
    {
        return array(
            array(
                array(
                    'PrefixCollision_A_' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/alpha',
                    'PrefixCollision_A_B_' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/beta',
                ),
                'PrefixCollision_A_Foo',
                '->loadClass() loads PrefixCollision_A_Foo from alpha.',
            ),
            array(
                array(
                    'PrefixCollision_A_B_' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/beta',
                    'PrefixCollision_A_' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/alpha',
                ),
                'PrefixCollision_A_Bar',
                '->loadClass() loads PrefixCollision_A_Bar from alpha.',
            ),
            array(
                array(
                    'PrefixCollision_A_' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/alpha',
                    'PrefixCollision_A_B_' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/beta',
                ),
                'PrefixCollision_A_B_Foo',
                '->loadClass() loads PrefixCollision_A_B_Foo from beta.',
            ),
            array(
                array(
                    'PrefixCollision_A_B_' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/beta',
                    'PrefixCollision_A_' => __DIR__.DIRECTORY_SEPARATOR.'Fixtures/alpha',
                ),
                'PrefixCollision_A_B_Bar',
                '->loadClass() loads PrefixCollision_A_B_Bar from beta.',
            ),
        );
    }

}