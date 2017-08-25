<?php

namespace Behat\Behat\Definition\Loader;

use Symfony\Component\EventDispatcher\EventDispatcher,
    Symfony\Component\EventDispatcher\Event;

use Behat\Gherkin\Node\StepNode;

use Behat\Behat\Definition\Definition,
    Behat\Behat\Definition\Transformation;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Plain PHP Files Steps Loader.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class PhpFileLoader implements LoaderInterface
{
    protected $dispatcher;
    protected $objects = array();

    /**
     * Initialize loader.
     *
     * @param   EventDispatcher $dispatcher event dispatcher
     */
    public function __construct(EventDispatcher $dispatcher)
    {
        $this->dispatcher = $dispatcher;
    }

    /**
     * Load definitions from file.
     *
     * @param   string          $path       plain php file path
     * @return  array                       array of Definitions & Transformations
     */
    public function load($path)
    {
        $this->objects  = array();
        $steps          = $this;

        require_once($path);

        return $this->objects;
    }

    /**
     * Define argument transformation to container.
     *
     * @param   string      $regex      regex to argument for transformation
     * @param   callback    $callback   transformation callback (returns transformed argument)
     */
    public function Transform($regex, $callback)
    {
        $this->objects[] = new Transformation($regex, $callback);
    }

    /**
     * Define a step with ->Given('/regex/', callback) or
     * call a step with ->Given('I enter "12" in the field', $world) or
     * even with arguments ->Given('I fill up fields', $world, $table).
     *
     * @param   string  $type       step type (Given/When/Then/And or localized one)
     * @param   string  $arguments  step regex & callback
     *
     * @throws  Behat\Behat\Exception\Redundant  if step definition is already exists
     */
    public function __call($type, $arguments)
    {
        if (2 == count($arguments) && is_callable($arguments[1])) {
            $debug = debug_backtrace();
            $debug = $debug[1];

            $this->objects[] = new Definition(
                $type, $arguments[0], $arguments[1], $debug['file'], $debug['line']
            );
        } else {
            $text   = array_shift($arguments);
            $world  = array_shift($arguments);

            $step   = new StepNode($type, $text);
            $step->setArguments($arguments);

            $this->dispatcher->notify(new Event($step, 'step.run', array('world' => $world)));
        }

        return $this;
    }
}