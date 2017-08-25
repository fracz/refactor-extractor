<?php

namespace Mpociot\BotMan\Interfaces;

use Mpociot\BotMan\BotMan;
use Mpociot\BotMan\Message;

interface MiddlewareInterface
{
    /**
     * Handle a captured message.
     *
     * @param Message $message
     * @param callable $next
     * @param BotMan $bot
     *
     * @return mixed
     */
    public function captured(Message $message, $next, BotMan $bot);

    /**
     * Handle an incoming message.
     *
     * @param Message $message
     * @param callable $next
     * @param BotMan $bot
     *
     * @return mixed
     */
    public function received(Message $message, $next, BotMan $bot);

    /**
     * @param Message $message
     * @param string $pattern
     * @param bool $regexMatched Indicator if the regular expression was matched too
     * @return bool
     */
    public function matching(Message $message, $pattern, $regexMatched);

    /**
     * Handle a message that was successfully heard, but not processed yet.
     *
     * @param Message $message
     * @param callable $next
     * @param BotMan $bot
     *
     * @return mixed
     */
    public function heard(Message $message, $next, BotMan $bot);

    /**
     * Handle an outgoing message payload before/after it
     * hits the message service.
     *
     * @param Message $message
     * @param callable $next
     * @param BotMan $bot
     *
     * @return mixed
     */
    public function sending(Message $message, $next, BotMan $bot);
}