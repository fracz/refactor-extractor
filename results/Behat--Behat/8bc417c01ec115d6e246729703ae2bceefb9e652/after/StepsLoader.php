<?php

namespace Everzet\Behat\Loader;

use Symfony\Component\DependencyInjection\Container;
use Symfony\Component\EventDispatcher\EventDispatcher;
use Symfony\Component\EventDispatcher\Event;
use Symfony\Component\Finder\Finder;

use Everzet\Gherkin\Element\StepElement;
use Everzet\Gherkin\Element\Inline\PyStringElement;
use Everzet\Gherkin\Element\Inline\TableElement;

use Everzet\Behat\Definition\StepDefinition;
use Everzet\Behat\Exception\Redundant;
use Everzet\Behat\Exception\Ambiguous;
use Everzet\Behat\Exception\Undefined;

/*
 * This file is part of the behat package.
 * (c) 2010 Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Steps loader.
 * Loads & initializates step definitions.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class StepsLoader implements LoaderInterface
{
    protected $container;
    protected $dispatcher;
    protected $steps = array();

    /**
     * Creates steps loader
     *
     * @param   Container       $container  dependency container
     * @param   EventDispatcher $dispatcher event dispatcher
     */
    public function __construct(Container $container, EventDispatcher $dispatcher)
    {
        $this->container    = $container;
        $this->dispatcher   = $dispatcher;
    }

    /**
     * Loads step definitions from specified path(s)
     *
     * @param   string|array    $paths      step definitions path(s)
     *
     * @return  Everzet\Behat\Loader\StepsLoader
     */
    public function load($paths)
    {
        $this->dispatcher->notify(new Event($this, 'steps.load.before'));

        $steps = $this;
        foreach ((array) $paths as $path) {
            if (is_dir($path)) {
                $finder = new Finder();
                foreach ($finder->files()->name('*.php')->in($path) as $definitionFile) {
                    require $definitionFile;
                }
            }
        }

        $this->dispatcher->notify(new Event($this, 'steps.load.after'));
    }

    /**
     * Define a step with ->Given('/regex/', callback)
     *
     * @param   string  $type       step type (Given/When/Then/And or localized one)
     * @param   string  $arguments  step regex & callback
     *
     * @throws  Everzet\Behat\Exceptions\Redundant  if step definition is already exists
     */
    public function __call($type, $arguments)
    {
        $debug = debug_backtrace();
        $debug = $debug[1];

        $class = $this->container->getParameter('step_definition.class');
        $definition = new $class(
            $type, $arguments[0], $arguments[1], $debug['file'], $debug['line']
        );

        if (isset($this->steps[$definition->getRegex()])) {
            throw new Redundant(
                $definition, $this->steps[$definition->getRegex()]
            );
        }

        $this->steps[$definition->getRegex()] = $definition;

        return $this;
    }

    /**
     * Proposes step definition for step elelemt
     *
     * @param   StepElement $step   step element
     * @param   string      $text   step test
     *
     * @return  array               associative array of (md5_key => definition)
     */
    public function proposeDefinition(StepElement $step)
    {
        $text = $step->getText();

        $regexp = preg_replace(
            array('/\'([^\']*)\'/', '/\"([^\"]*)\"/', '/(\d+)/'),
            array("\'([^\']*)\'", "\"([^\"]*)\"", "(\\d+)"),
            $text, -1, $count
        );

        $args = array("\$world");
        for ($i = 0; $i < $count; $i++) {
            $args[] = "\$arg".($i + 1);
        }

        foreach ($step->getArguments() as $argument) {
            if ($argument instanceof PyStringElement) {
                $args[] = "\$string";
            } elseif ($argument instanceof TableElement) {
                $args[] = "\$table";
            }
        }

        $description = sprintf(<<<PHP
\$steps->%s('/^%s$/', function(%s) {
    throw new \Everzet\Behat\Exception\Pending();
});
PHP
          , '%s', $regexp, implode(', ', $args)
        );

        return array(md5($description) => sprintf($description, $step->getType()));
    }

    /**
     * Finds & returns step definition, that matches specific step description
     *
     * @param   StepElement     $step       specific step to match
     *
     * @return  StepDefinition
     *
     * @throws  Everzet\Behat\Exceptions\Ambiguous  if step description is ambiguous
     * @throws  Everzet\Behat\Exceptions\Undefined  if step definition not found
     */
    public function findDefinition(StepElement $step)
    {
        $text       = $step->getText();
        $args       = $step->getArguments();
        $matches    = array();

        foreach ($this->steps as $regex => $definition) {
            if (preg_match($regex, $text, $values)) {
                $definition->setMatchedText($text);
                $definition->setValues(array_merge(array_slice($values, 1), $args));
                $matches[] = $definition;
            }
        }

        if (count($matches) > 1) {
            throw new Ambiguous($text, $matches);
        }

        if (0 === count($matches)) {
            throw new Undefined($text);
        }

        return $matches[0];
    }
}