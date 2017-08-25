<?php

namespace Behat\Behat\Exception;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Pending exception (throw this to mark pending definition).
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class Pending extends BehaviorException
{
    /**
     * Initialize Exception.
     *
     * @param   string  $text   step description
     */
    public function __construct($text = 'write pending definition')
    {
        parent::__construct();
        $this->message = sprintf('TODO: %s', $text);
    }
}