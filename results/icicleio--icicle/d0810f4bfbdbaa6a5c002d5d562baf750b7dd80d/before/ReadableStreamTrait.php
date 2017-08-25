<?php
namespace Icicle\Socket\Stream;

use Exception;
use Icicle\Loop\Loop;
use Icicle\Promise\Deferred;
use Icicle\Promise\Promise;
use Icicle\Socket\Exception\EofException;
use Icicle\Socket\Exception\TimeoutException;
use Icicle\Stream\Exception\BusyException;
use Icicle\Socket\SocketInterface;
use Icicle\Stream\Exception\UnreadableException;
use Icicle\Stream\ParserTrait;

trait ReadableStreamTrait
{
    use ParserTrait;
    use PipeTrait;

    /**
     * @var \Icicle\Promise\Deferred|null
     */
    private $deferred;

    /**
     * @var \Icicle\Loop\Events\SocketEventInterface
     */
    private $poll;

    /**
     * @var int
     */
    private $length = 0;

    /**
     * @var string|null
     */
    private $byte;

    /**
     * @return  resource Stream socket resource.
     */
    abstract protected function getResource();

    /**
     * Closes the stream socket.
     */
    abstract public function close();

    /**
     * Frees resources associated with the stream and closes the stream.
     *
     * @param   \Exception $exception
     */
    abstract protected function free(Exception $exception);

    /**
     * @param  resource $socket Stream socket resource.
     */
    private function init($socket)
    {
        stream_set_read_buffer($socket, 0);
        stream_set_chunk_size($socket, SocketInterface::CHUNK_SIZE);

        $this->poll = $this->createPoll($socket);
    }

    /**
     * Frees all resources used by the writable stream.
     *
     * @param   \Exception $exception
     */
    private function detach(Exception $exception)
    {
        $this->poll->free();
        $this->poll = null;

        if (null !== $this->deferred) {
            $this->deferred->reject($exception);
            $this->deferred = null;
        }
    }

    /**
     * @inheritdoc
     */
    public function read($length = null, $byte = null, $timeout = null)
    {
        if (null !== $this->deferred) {
            return Promise::reject(new BusyException('Already waiting on stream.'));
        }

        if (!$this->isReadable()) {
            return Promise::reject(new UnreadableException('The stream is no longer readable.'));
        }

        $this->length = $this->parseLength($length);

        if (null === $this->length) {
            $this->length = SocketInterface::CHUNK_SIZE;
        }

        if (0 === $this->length) {
            return Promise::resolve('');
        }

        $this->byte = $this->parseByte($byte);

        $resource = $this->getResource();
        $data = $this->fetch($resource);

        if ('' !== $data) {
            return Promise::resolve($data);
        }

        if (feof($resource)) { // Close only if no data was read and at EOF.
            $this->close();
            return Promise::resolve($data); // Resolve with empty string on EOF.
        }

        $this->poll->listen($timeout);

        $this->deferred = new Deferred(function () {
            $this->poll->cancel();
            $this->deferred = null;
        });

        return $this->deferred->getPromise();
    }

    /**
     * Returns a promise that is fulfilled when there is data available to read, without actually consuming any data.
     *
     * @param   float|int|null $timeout Number of seconds until the returned promise is rejected with a TimeoutException
     *          if no data is received. Use null for no timeout.
     *
     * @return  \Icicle\Promise\PromiseInterface
     *
     * @resolve string Empty string.
     *
     * @reject  \Icicle\Stream\Exception\BusyException If a read was already pending on the stream.
     * @reject  \Icicle\Stream\Exception\UnreadableException If the stream is no longer readable.
     * @reject  \Icicle\Stream\Exception\ClosedException If the stream has been closed.
     * @reject  \Icicle\Stream\Exception\TimeoutException If the operation times out.
     */
    protected function poll($timeout = null)
    {
        if (null !== $this->deferred) {
            return Promise::reject(new BusyException('Already waiting on stream.'));
        }

        if (!$this->isReadable()) {
            return Promise::reject(new UnreadableException('The stream is no longer readable.'));
        }

        $this->length = 0;

        $this->poll->listen($timeout);

        $this->deferred = new Deferred(function () {
            $this->poll->cancel();
            $this->deferred = null;
        });

        return $this->deferred->getPromise();
    }

    /**
     * @inheritdoc
     */
    public function isReadable()
    {
        return $this->isOpen();
    }

    /**
     * @param   resource $socket Stream socket resource.
     *
     * @return  \Icicle\Loop\Events\SocketEventInterface
     */
    private function createPoll($socket)
    {
        return Loop::poll($socket, function ($resource, $expired) {
            if ($expired) {
                $this->deferred->reject(new TimeoutException('The connection timed out.'));
                $this->deferred = null;
                return;
            }

            if (0 === $this->length) {
                $this->deferred->resolve('');
                $this->deferred = null;
                return;
            }

            $data = $this->fetch($resource);

            $this->deferred->resolve($data);
            $this->deferred = null;

            if ('' === $data && feof($resource)) { // Close only if no data was read and at EOF.
                $this->close();
            }
        });
    }

    /**
     * Reads data from the stream socket resource based on set length and read-to byte.
     *
     * @param   resource $resource
     *
     * @return  string
     */
    private function fetch($resource)
    {
        if (null === $this->byte) {
            return (string) fread($resource, $this->length);
        }

        $data = '';

        for ($i = 0; $i < $this->length; ++$i) {
            if (false === ($byte = fgetc($resource))) {
                return $data;
            }

            $data .= $byte;

            if ($byte === $this->byte) {
                return $data;
            }
        }

        return $data;
    }
}