<?php

namespace Symfony\Component\HttpKernel\Profiler;

use Symfony\Component\EventDispatcher\EventDispatcher;
use Symfony\Component\EventDispatcher\Event;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\HttpKernelInterface;

/*
 * This file is part of the Symfony framework.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * This source file is subject to the MIT license that is bundled
 * with this source code in the file LICENSE.
 */

/**
 * ProfilerListener collects data for the current request by listening to the core.response event.
 *
 * @author     Fabien Potencier <fabien.potencier@symfony-project.com>
 */
class ProfilerListener
{
    protected $profiler;
    protected $exception;

    /**
     * Constructor.
     *
     * @param Profiler $profiler A Profiler instance
     */
    public function __construct(Profiler $profiler)
    {
        $this->profiler = $profiler;
    }

    /**
     * Registers a core.response and core.exception listeners.
     *
     * @param EventDispatcher $dispatcher An EventDispatcher instance
     * @param integer         $priority   The priority
     */
    public function register(EventDispatcher $dispatcher, $priority = 0)
    {
        $dispatcher->connect('core.exception', array($this, 'handleException'), $priority);
        $dispatcher->connect('core.response', array($this, 'handleResponse'), $priority);
    }

    /**
     * Handles the core.exception event.
     *
     * @param Event $event An Event instance
     */
    public function handleException(Event $event)
    {
        if (HttpKernelInterface::MASTER_REQUEST !== $event->getParameter('request_type')) {
            return false;
        }

        $this->exception = $event->getParameter('exception');

        return false;
    }

    /**
     * Handles the core.response event.
     *
     * @param Event $event An Event instance
     *
     * @return Response $response A Response instance
     */
    public function handleResponse(Event $event, Response $response)
    {
        if (HttpKernelInterface::MASTER_REQUEST !== $event->getParameter('request_type')) {
            return $response;
        }

        $this->profiler->collect($event->getParameter('request'), $response, $this->exception);
        $this->exception = null;

        return $response;
    }
}