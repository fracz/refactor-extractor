<?php

namespace Wallabag\ImportBundle\Tests\Import;

use Symfony\Component\DependencyInjection\ContainerBuilder;
use Wallabag\ImportBundle\Import\ImportCompilerPass;

class ImportCompilerPassTest extends \PHPUnit_Framework_TestCase
{
    public function testProcessNoDefinition()
    {
        $container = new ContainerBuilder();
        $res = $this->process($container);

        $this->assertNull($res);
    }

    public function testProcess()
    {
        $container = new ContainerBuilder();
        $container
            ->register('wallabag_import.chain')
            ->setPublic(false)
        ;

        $container
            ->register('foo')
            ->addTag('wallabag_import.import', array('alias' => 'pocket'))
        ;

        $this->process($container);

        $this->assertTrue($container->hasDefinition('wallabag_import.chain'));

        $definition = $container->getDefinition('wallabag_import.chain');
        $this->assertTrue($definition->hasMethodCall('addImport'));

        $calls = $definition->getMethodCalls();
        $this->assertEquals('pocket', $calls[0][1][1]);
    }

    protected function process(ContainerBuilder $container)
    {
        $repeatedPass = new ImportCompilerPass();
        $repeatedPass->process($container);
    }
}