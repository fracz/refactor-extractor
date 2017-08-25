<?php
/*
 * This file is part of the DebugBar package.
 *
 * (c) 2013 Maxime Bouroumeau-Fuseau
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace DebugBar\DataCollector;

/**
 * Abstract class for data collectors
 */
abstract class DataCollector implements DataCollectorInterface
{
    /**
     * Transforms a PHP variable to a string representation
     *
     * @param mixed $var
     * @return string
     */
    public function varToString($var)
    {
        return print_r($var, true);
    }
}