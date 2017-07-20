<?php

/*
 * This file is part of the Monolog package.
 *
 * (c) Jordi Boggiano <j.boggiano@seld.be>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Monolog;

use Monolog\Handler\HandlerInterface;
use Monolog\Handler\StreamHandler;

class Logger
{
    /**
     * Debug messages
     */
    const DEBUG   = 100;

    /**
     * Messages you usually don't want to see
     */
    const INFO    = 200;

    /**
     * Exceptional occurences that are not errors
     *
     * This is typically the logging level you want to use
     */
    const WARNING = 300;

    /**
     * Errors
     */
    const ERROR   = 400;

    protected static $levels = array(
        100 => 'DEBUG',
        200 => 'INFO',
        300 => 'WARNING',
        400 => 'ERROR',
    );

    protected $name;

    /**
     * The handler instance at the top of the handler stack
     *
     * @var Monolog\Handler\HandlerInterface
     */
    protected $handler;

    public function __construct($name)
    {
        $this->name = $name;
    }

    public function pushHandler(HandlerInterface $handler)
    {
        if ($this->handler) {
            $handler->setParent($this->handler);
        }
        $this->handler = $handler;
    }

    public function popHandler()
    {
        if (null === $this->handler) {
            throw new \LogicException('You tried to pop from an empty handler stack.');
        }
        $top = $this->handler;
        $this->handler = $top->getParent();
        return $top;
    }

    public function addMessage($level, $message)
    {
        if (null === $this->handler) {
            $this->pushHandler(new StreamHandler('php://stderr', self::DEBUG));
        }
        $message = array(
            'message' => $message,
            'level' => $level,
            'level_name' => $this->getLevelName($level),
            'channel' => $this->name,
            'datetime' => new \DateTime(),
            'extra' => array(),
        );
        $handled = false;
        $handler = $this->handler;
        while ($handler && true !== $handled) {
            $handled = (bool) $handler->handle($message);
            $handler = $handler->getParent();
        }
        return $handled;
    }

    public function addDebug($message, $channel = 'default')
    {
        $this->addMessage(self::DEBUG, $message, $channel);
    }

    public function addInfo($message, $channel = 'default')
    {
        $this->addMessage(self::INFO, $message, $channel);
    }

    public function addWarning($message, $channel = 'default')
    {
        $this->addMessage(self::WARNING, $message, $channel);
    }

    public function addError($message, $channel = 'default')
    {
        $this->addMessage(self::ERROR, $message, $channel);
    }

    public function getLevelName($level)
    {
        return self::$levels[$level];
    }

    // ZF Logger Compat

    public function debug($message, $channel = null)
    {
        $this->addMessage(self::DEBUG, $message, $channel);
    }

    public function info($message, $channel = 'default')
    {
        $this->addMessage(self::INFO, $message, $channel);
    }

    public function notice($message, $channel = 'default')
    {
        $this->addMessage(self::INFO, $message, $channel);
    }

    public function warn($message, $channel = 'default')
    {
        $this->addMessage(self::WARNING, $message, $channel);
    }

    public function err($message, $channel = 'default')
    {
        $this->addMessage(self::ERROR, $message, $channel);
    }

    public function crit($message, $channel = 'default')
    {
        $this->addMessage(self::ERROR, $message, $channel);
    }

    public function alert($message, $channel = 'default')
    {
        $this->addMessage(self::ERROR, $message, $channel);
    }

    public function emerg($message, $channel = 'default')
    {
        $this->addMessage(self::ERROR, $message, $channel);
    }
}