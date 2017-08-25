<?php

namespace Everzet\Behat\Exceptions;

use \Everzet\Behat\Exceptions\BehaviorException as BaseException;

/*
 * This file is part of the behat package.
 * (c) 2010 Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Ambiguous Exception
 *
 * @package     behat
 * @subpackage  Behat
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class Ambiguous extends BaseException
{
    protected $text;
    protected $matches = array();

    /**
     * Creates exception
     *
     * @param   string  $text       step description
     * @param   array   $matches    ambigious matches (array of StepDefinition's)
     */
    public function __construct($text, array $matches)
    {
        parent::__construct();

        $this->definition = $definition;
        $this->matches = $matches;

        $this->message = sprintf("Ambiguous match of \"%s\":", $this->text);
        foreach ($this->matches as $definition){
            $this->message .= sprintf("\n%s:%d:in `%s`",
                $definition->getFile(), $definition->getLine(), $definition->getRegex()
            );
        }
    }
}