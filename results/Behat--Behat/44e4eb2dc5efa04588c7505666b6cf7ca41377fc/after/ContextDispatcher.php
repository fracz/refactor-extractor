<?php

namespace Behat\Behat\Context;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Context dispatcher.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class ContextDispatcher
{
    /**
     * Context class name.
     *
     * @var     string
     */
    private $contextClass;
    /**
     * Context initialization parameters.
     *
     * @var     array
     */
    private $parameters = array();

    /**
     * Initialize dispatcher.
     *
     * @param   string  $className  context class name
     * @param   array   $parameters context parameters
     */
    public function __construct($className, array $parameters = array())
    {
        $this->setContextClass($className);
        $this->parameters = $parameters;
    }

    /**
     * Sets context class name.
     *
     * @param   string  $className      context class name
     */
    public function setContextClass($className)
    {
        if (!class_exists($className)) {
            throw new \InvalidArgumentException(sprintf('Context class "%s" not found', $className));
        }

        $contextClassRefl = new \ReflectionClass($className);
        if (!$contextClassRefl->implementsInterface('Behat\Behat\Context\ContextInterface')) {
            throw new \InvalidArgumentException(sprintf(
                'Context class "%s" should implement ContextInterface', $className
            ));
        }

        $this->contextClass = $className;
    }

    /**
     * Returns context class name.
     *
     * @return  string
     */
    public function getContextClass()
    {
        return $this->contextClass;
    }

    /**
     * Create new context instance.
     *
     * @return  Behat\Behat\Context\ContextInterface
     */
    public function createContext()
    {
        return new $this->contextClass($this->parameters);
    }
}