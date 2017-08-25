<?php

namespace Behat\Behat\DependencyInjection\Compiler;

use Symfony\Component\DependencyInjection\Reference,
    Symfony\Component\DependencyInjection\ContainerBuilder,
    Symfony\Component\DependencyInjection\Compiler\CompilerPassInterface;

/*
 * This file is part of the Behat.
 *
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * This source file is subject to the MIT license that is bundled
 * with this source code in the file LICENSE.
 */

/**
 * Formatters pass - registers all available formatters.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class FormattersPass implements CompilerPassInterface
{
    /**
     * Processes container.
     *
     * @param   Symfony\Component\DependencyInjection\ContainerBuilder  $container
     */
    public function process(ContainerBuilder $container)
    {
        if (!$container->hasDefinition('behat.format_manager')) {
            return;
        }
        $manager = $container->getDefinition('behat.format_manager');

        foreach ($container->findTaggedServiceIds('behat.formatter_dispatcher') as $id => $attributes) {
            $manager->addMethodCall('addDispatcher', array(new Reference($id)));
        }
    }
}