<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Bundle\TwigBundle\Tests\DependencyInjection;

use Symfony\Bundle\TwigBundle\DependencyInjection\TwigExtension;
use Symfony\Bundle\TwigBundle\Tests\TestCase;
use Symfony\Component\Config\FileLocator;
use Symfony\Component\DependencyInjection\ContainerBuilder;
use Symfony\Component\DependencyInjection\Reference;
use Symfony\Component\DependencyInjection\Loader\PhpFileLoader;
use Symfony\Component\DependencyInjection\Loader\XmlFileLoader;
use Symfony\Component\DependencyInjection\Loader\YamlFileLoader;
use Symfony\Component\DependencyInjection\ParameterBag\ParameterBag;

class TwigExtensionTest extends TestCase
{
    /**
     * @dataProvider getFormats
     */
    public function testLoadEmptyConfiguration($format)
    {
        $container = $this->createContainer();
        $container->registerExtension(new TwigExtension());
        $container->loadFromExtension('twig', array());
        $this->compileContainer($container);

        $this->assertEquals('Twig_Environment', $container->getParameter('twig.class'), '->load() loads the twig.xml file');
        $this->assertFalse($container->getDefinition('templating.cache_warmer.templates_cache')->hasTag('kernel.cache_warmer'), '->load() does not enable cache warming by default');
        $this->assertContains('TwigBundle::form.html.twig', $container->getParameter('twig.form.resources'), '->load() includes default template for form resources');

        // Twig options
        $options = $container->getParameter('twig.options');
        $this->assertEquals(__DIR__.'/twig', $options['cache'], '->load() sets default value for cache option');
        $this->assertEquals('UTF-8', $options['charset'], '->load() sets default value for charset option');
        $this->assertEquals(false, $options['debug'], '->load() sets default value for debug option');
    }

    /**
     * @dataProvider getFormats
     */
    public function testLoadFullConfiguration($format)
    {
        $container = $this->createContainer();
        $container->registerExtension(new TwigExtension());
        $this->loadFromFile($container, 'full', $format);
        $this->compileContainer($container);

        $this->assertEquals('Twig_Environment', $container->getParameter('twig.class'), '->load() loads the twig.xml file');
        $this->assertTrue($container->getDefinition('templating.cache_warmer.templates_cache')->hasTag('kernel.cache_warmer'), '->load() enables cache warming');

        // Extensions
        foreach (array('twig.extension.debug', 'twig.extension.text') as $id) {
            $config = $container->getDefinition($id);
            $this->assertEquals(array('twig.extension'), array_keys($config->getTags()), '->load() adds tags to extension definitions');
        }

        // Form resources
        $resources = $container->getParameter('twig.form.resources');
        $this->assertContains('TwigBundle::form.html.twig', $resources, '->load() includes default template for form resources');
        $this->assertContains('MyBundle::form.html.twig', $resources, '->load() merges new templates into form resources');

        // Globals
        $calls = $container->getDefinition('twig')->getMethodCalls();
        $this->assertEquals('foo', $calls[0][1][0], '->load() registers services as Twig globals');
        $this->assertEquals(new Reference('bar'), $calls[0][1][1], '->load() registers services as Twig globals');
        $this->assertEquals('pi', $calls[1][1][0], '->load() registers variables as Twig globals');
        $this->assertEquals(3.14, $calls[1][1][1], '->load() registers variables as Twig globals');

        // Twig options
        $options = $container->getParameter('twig.options');
        $this->assertTrue($options['auto_reload'], '->load() sets the auto_reload option');
        $this->assertTrue($options['autoescape'], '->load() sets the autoescape option');
        $this->assertEquals('stdClass', $options['base_template_class'], '->load() sets the base_template_class option');
        $this->assertEquals('/tmp', $options['cache'], '->load() sets the cache option');
        $this->assertEquals('ISO-8859-1', $options['charset'], '->load() sets the charset option');
        $this->assertTrue($options['debug'], '->load() sets the debug option');
        $this->assertTrue($options['strict_variables'], '->load() sets the strict_variables option');
    }

    public function getFormats()
    {
        return array(
            array('php'),
            array('yml'),
            array('xml'),
        );
    }

    private function createContainer()
    {
        $container = new ContainerBuilder(new ParameterBag(array(
            'kernel.cache_dir' => __DIR__,
            'kernel.charset'   => 'UTF-8',
            'kernel.debug'     => false,
        )));

        return $container;
    }

    private function compileContainer(ContainerBuilder $container)
    {
        $container->getCompilerPassConfig()->setOptimizationPasses(array());
        $container->getCompilerPassConfig()->setRemovingPasses(array());
        $container->compile();
    }

    private function loadFromFile(ContainerBuilder $container, $file, $format)
    {
        $locator = new FileLocator(__DIR__.'/Fixtures/'.$format);

        switch ($format) {
            case 'php':
                $loader = new PhpFileLoader($container, $locator);
                break;
            case 'xml':
                $loader = new XmlFileLoader($container, $locator);
                break;
            case 'yml':
                $loader = new YamlFileLoader($container, $locator);
                break;
            default:
                throw new \InvalidArgumentException('Unsupported format: '.$format);
        }

        $loader->load($file.'.'.$format);
    }
}