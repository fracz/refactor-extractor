<?php

/*
 * This file is part of the Monolog package.
 *
 * (c) Jordi Boggiano <j.boggiano@seld.be>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Monolog\Handler;

use Monolog\Formatter\SimpleFormatter;
use Monolog\Logger;

class StreamHandler extends AbstractHandler
{
    protected $stream;
    protected $url;

    public function __construct($stream, $level = Logger::DEBUG, $bubble = true)
    {
        parent::__construct($level, $bubble);
        if (is_resource($stream)) {
            $this->stream = $stream;
        } else {
            $this->url = $stream;
        }
    }

    public function write($message)
    {
        if (null === $this->stream) {
            if (!$this->url) {
                throw new \LogicException('Missing stream url, the stream can not be opened. This may be caused by a premature call to close().');
            }
            $this->stream = fopen($this->url, 'a');
            if (!is_resource($this->stream)) {
                throw new \UnexpectedValueException('The stream could not be opened, "'.$this->url.'" may be an invalid url.');
            }
        }
        fwrite($this->stream, (string) $message['message']);
    }

    public function close()
    {
        if (null !== $this->stream) {
            fclose($this->stream);
            $this->stream = null;
        }
    }
}