<?php

namespace Behat\Behat\Output\Formatter;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Timable Formatter Interface.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
interface TimableFormatterInterface
{
    /**
     * Allow colors in output.
     *
     * @param   boolean $tiemr  show timer in output
     */
    public function showTimer($timer = true);
}