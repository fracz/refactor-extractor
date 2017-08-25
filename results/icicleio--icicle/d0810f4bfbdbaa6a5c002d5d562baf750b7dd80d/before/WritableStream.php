<?php
namespace Icicle\Socket\Stream;

use Exception;
use Icicle\Socket\Socket;
use Icicle\Stream\Exception\ClosedException;

class WritableStream extends Socket implements WritableSocketInterface
{
    use WritableStreamTrait;

    /**
     * @param   resource $socket
     */
    public function __construct($socket)
    {
        parent::__construct($socket);
        $this->init($socket);
    }

    /**
     * @inheritdoc
     */
    public function close()
    {
        if ($this->isOpen()) {
            $this->free(new ClosedException('The connection was closed.'));
        }
    }

    /**
     * Frees resources associated with the stream and closes the stream.
     *
     * @param   \Exception $exception Reason for the stream closing.
     */
    protected function free(Exception $exception)
    {
        $this->detach($exception);

        parent::close();
    }
}