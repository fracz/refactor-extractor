<?php

namespace Symfony\Component\DependencyInjection\Compiler;

use Symfony\Component\DependencyInjection\Definition;

use Symfony\Component\DependencyInjection\Reference;
use Symfony\Component\DependencyInjection\ContainerBuilder;

/*
 * This file is part of the Symfony framework.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * This source file is subject to the MIT license that is bundled
 * with this source code in the file LICENSE.
 */

/**
 * Inline service definitions where this is possible.
 *
 * @author Johannes M. Schmitt <schmittjoh@gmail.com>
 */
class InlineServiceDefinitionsPass implements RepeatablePassInterface
{
    protected $repeatedPass;
    protected $graph;

    public function setRepeatedPass(RepeatedPass $repeatedPass)
    {
        $this->repeatedPass = $repeatedPass;
    }

    public function process(ContainerBuilder $container)
    {
        $this->graph = $this->repeatedPass->getCompiler()->getServiceReferenceGraph();

        foreach ($container->getDefinitions() as $id => $definition) {
            $definition->setArguments(
                $this->inlineArguments($container, $definition->getArguments())
            );

            $definition->setMethodCalls(
                $this->inlineArguments($container, $definition->getMethodCalls())
            );
        }
    }

    protected function inlineArguments(ContainerBuilder $container, array $arguments)
    {
        foreach ($arguments as $k => $argument) {
            if (is_array($argument)) {
                $arguments[$k] = $this->inlineArguments($container, $argument);
            } else if ($argument instanceof Reference) {
                if (!$container->hasDefinition($id = (string) $argument)) {
                    continue;
                }

                if ($this->isInlinableDefinition($container, $id, $definition = $container->getDefinition($id))) {
                    $arguments[$k] = $definition;
                }
            }
        }

        return $arguments;
    }

    protected function isInlinableDefinition(ContainerBuilder $container, $id, Definition $definition)
    {
        if (!$definition->isShared()) {
            return true;
        }

        if ($definition->isPublic()) {
            return false;
        }

        if (!$this->graph->hasNode($id)) {
            return true;
        }

        $ids = array();
        foreach ($this->graph->getNode($id)->getInEdges() as $edge) {
            $ids[] = $edge->getSourceNode()->getId();
        }

        return count(array_unique($ids)) <= 1;
    }
}