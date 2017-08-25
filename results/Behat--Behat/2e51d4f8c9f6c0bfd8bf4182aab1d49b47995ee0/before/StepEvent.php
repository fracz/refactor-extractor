<?php

namespace Behat\Behat\Event;

use Symfony\Component\EventDispatcher\Event;

use Behat\Behat\Environment\EnvironmentInterface,
    Behat\Behat\Definition\Definition;

use Behat\Gherkin\Node\StepNode;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Step event.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class StepEvent extends Event implements EventInterface
{
    const PASSED    = 0;
    const SKIPPED   = 1;
    const PENDING   = 2;
    const UNDEFINED = 3;
    const FAILED    = 4;

    private $step;
    private $environment;
    private $result;
    private $definition;
    private $exception;
    private $snippet;

    /**
     * Initializes step event.
     *
     * @param   Behat\Gherkin\Node\StepNode                     $step
     * @param   Behat\Behat\Environment\EnvironmentInterface    $environment
     * @param   integer                                         $result
     * @param   Behat\Behat\Definition\Definition               $definition
     * @param   Exception                                       $exception
     * @param   mixed                                           $snippet
     */
    public function __construct(StepNode $step, EnvironmentInterface $environment, $result = null,
                                Definition $definition = null, \Exception $exception = null, $snippet = null)
    {
        $this->step         = $step;
        $this->environment  = $environment;
        $this->result       = $result;
        $this->definition   = $definition;
        $this->exception    = $exception;
        $this->snippet      = $snippet;
    }

    /**
     * Returns step node.
     *
     * @return  Behat\Gherkin\Node\StepNode
     */
    public function getStep()
    {
        return $this->step;
    }

    /**
     * Returns environment object.
     *
     * @return  Behat\Behat\Environment\EnvironmentInterface
     */
    public function getEnvironment()
    {
        return $this->environment;
    }

    /**
     * Returns step tester result code.
     *
     * @return  integer
     */
    public function getResult()
    {
        return $this->result;
    }

    /**
     * Returns step definition object.
     *
     * @return  Behat\Behat\Definition\Definition
     */
    public function getDefinition()
    {
        return $this->definition;
    }

    /**
     * Checks whether event contains step definition.
     *
     * @return  Boolean
     */
    public function hasDefinition()
    {
        return null !== $this->getDefinition();
    }

    /**
     * Returns step tester exception.
     *
     * @return  Exception
     */
    public function getException()
    {
        return $this->exception;
    }

    /**
     * Checks whether event contains exception.
     *
     * @return  Boolean
     */
    public function hasException()
    {
        return null !== $this->getException();
    }

    /**
     * Returns step snippet.
     *
     * @return  mixed
     */
    public function getSnippet()
    {
        return $this->snippet;
    }

    /**
     * Checks whether event contains snippet.
     *
     * @return  Boolean
     */
    public function hasSnippet()
    {
        return null !== $this->getSnippet();
    }
}