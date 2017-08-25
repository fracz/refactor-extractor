<?php

namespace Everzet\Gherkin\Structures;

/*
 * This file is part of the behat package.
 * (c) 2010 Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Step.
 *
 * @package     behat
 * @subpackage  Gherkin
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class Step
{
    protected $type;
    protected $text;
    protected $arguments = array();
    protected $line;

    public function __construct($type, $text, $line = 0)
    {
        $this->type = $type;
        $this->text = $text;
        $this->line = $line;
    }

    public function getLine()
    {
        return $this->line;
    }

    public function getType()
    {
        return $this->type;
    }

    public function getText(array $tokens = array())
    {
        $text = $this->text;

        foreach ($tokens as $key => $value)
        {
          $text = str_replace('<'.$key.'>', $value, $text, $count);
        }

        return $text;
    }

    public function addArgument($argument)
    {
        $this->arguments[] = $argument;
    }

    public function hasArguments()
    {
        return count($this->arguments) > 0;
    }

    public function getArgumentsCount()
    {
        return count($this->arguments);
    }

    public function getArguments()
    {
        return $this->arguments;
    }
}