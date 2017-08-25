<?php

namespace Everzet\Behat\EventDispatcher;

use Symfony\Component\EventDispatcher\EventDispatcher as BaseEventDispatcher;
use Symfony\Component\EventDispatcher\Event;
use Symfony\Component\DependencyInjection\ContainerInterface;

/*
 * This file is part of the behat package.
 * (c) 2010 Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Event dispatcher.
 * Dispatches custom Behat events to hook with.
 *
 * @package     Behat
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class EventDispatcher extends BaseEventDispatcher
{
    /**
     * Creates dispatcher & run registerListeners on all services with tag 'event_listener'
     *
     * @param   ContainerInterface  $container  dependency container
     */
    public function __construct(ContainerInterface $container)
    {
        foreach ($container->findTaggedServiceIds('events_listener') as $id) {
            $container->get($id)->registerListeners($this);
        }
    }
}