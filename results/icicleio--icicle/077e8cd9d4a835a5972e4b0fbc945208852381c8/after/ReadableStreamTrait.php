<?php
namespace Icicle\Socket;

use Exception;
use Icicle\Loop\Loop;
use Icicle\Promise\Deferred;
use Icicle\Promise\Promise;
use Icicle\Socket\Exception\EofException;
use Icicle\Socket\Exception\FailureException;
use Icicle\Socket\Exception\TimeoutException;
use Icicle\Stream\Exception\BusyException;
use Icicle\Stream\Exception\ClosedException;
use Icicle\Stream\Exception\InvalidArgumentException;
use Icicle\Stream\Exception\UnreadableException;
use Icicle\Stream\Exception\UnwritableException;
use Icicle\Stream\WritableStreamInterface;

trait ReadableStreamTrait
{
    /**
     * @var Deferred|null
     */
    private $deferred;

    /**
     * @var PollInterface
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
     * @return  resource Socket resource.
     */
    abstract protected function getResource();

    /**
     * Determines if the stream is still open.
     *
     * @return  bool
     */
    abstract public function isOpen();

    /**
     * Closes the socket if it is still open.
     *
     * @param   Exception|null $exception
     */
    abstract public function close(Exception $exception = null);

    /**
     * @param   resource $socket Socket resource.
     */
    private function init($socket)
    {
        stream_set_read_buffer($socket, 0);
        stream_set_chunk_size($socket, self::CHUNK_SIZE);

        $this->poll = Loop::poll($socket, function ($resource, $expired) {
            if ($expired) {
                $this->deferred->reject(new TimeoutException('The connection timed out.'));
                $this->deferred = null;
                return;
            }

            $data = null;

            if (0 !== $this->length) {
                if (null !== $this->byte) {
                    $length = strlen($this->byte);
                    $offset = -$length;

                    for ($i = 0; $i < $this->length; ++$i) {
                        if (false === ($byte = fgetc($resource))) {
                            break;
                        }
                        $data .= $byte;
                        if ($byte === $this->byte) {
                            break;
                        }
                    }
                } elseif (!feof($resource)) {
                    $data = fread($resource, $this->length);
                }
            }

            if (null === $data && feof($resource)) { // Close only if no data was read and at EOF.
                $this->close(new EofException('Connection reset by peer or reached EOF.'));
                return;
            }

            $this->deferred->resolve($data);
            $this->deferred = null;
        });
    }

    /**
     * Frees all resources used by the writable stream.
     *
     * @param   Exception $exception
     */
    private function free(Exception $exception)
    {
        $this->poll->free();

        if (null !== $this->deferred) {
            $this->deferred->reject($exception);
            $this->deferred = null;
        }
    }

    /**
     * {@inheritdoc}
     */
    public function read($length = null, $timeout = null)
    {
        return $this->readTo(null, $length, $timeout);
    }

    /**
     * {@inheritdoc}
     */
    public function readTo($byte, $length = null, $timeout = null)
    {
        if (null !== $this->deferred) {
            return Promise::reject(new BusyException('Already waiting on stream.'));
        }

        if (!$this->isReadable()) {
            return Promise::reject(new UnreadableException('The stream is no longer readable.'));
        }

        if (null === $byte) {
            $this->byte = null;
        } else {
            $this->byte = is_int($byte) ? pack('C', $byte) : (string) $byte;
            $this->byte = strlen($this->byte) ? $this->byte[0] : null;
        }

        if (null === $length) {
            $this->length = self::CHUNK_SIZE;
        } else {
            $this->length = (int) $length;
            if (0 > $this->length) {
                $this->length = 0;
            }
        }

        $this->poll->listen($timeout);

        $this->deferred = new Deferred(function () {
            $this->poll->cancel();
            $this->deferred = null;
        });

        return $this->deferred->getPromise();
    }

    /**
     * {@inheritdoc}
     */
    public function poll($timeout = null)
    {
        return $this->readTo(null, 0, $timeout);
    }

    /**
     * {@inheritdoc}
     */
    public function isReadable()
    {
        return $this->isOpen();
    }

    /**
     * {@inheritdoc}
     */
    public function pipe(WritableStreamInterface $stream, $endOnClose = true, $length = null, $timeout = null)
    {
        return $this->pipeTo($stream, null, $endOnClose, $length, $timeout);
    }

    /**
     * {@inheritdoc}
     */
    public function pipeTo(WritableStreamInterface $stream, $byte, $endOnClose = true, $length = null, $timeout = null)
    {
        if (!$stream->isWritable()) {
            return Promise::reject(new UnwritableException('The stream is not writable.'));
        }

        if (null !== $length) {
            $length = (int) $length;
            if (0 > $length) {
                return Promise::resolve(0);
            }
        }

        if ($byte !== null) {
            $byte = is_int($byte) ? pack('C', $byte) : (string) $byte;
            $byte = strlen($byte) ? $byte[0] : null;
        }

        $result = new Promise(
            function ($resolve, $reject) use (&$promise, $stream, $byte, $length, $timeout) {
                $handler = function ($data) use (
                    &$handler,
                    &$promise,
                    &$length,
                    $stream,
                    $byte,
                    $timeout,
                    $resolve,
                    $reject
                ) {
                    static $bytes = 0;

                    $count = strlen($data);
                    $bytes += $count;

                    $promise = $stream->write($data, $timeout);

                    if (null !== $byte && $data[$count - 1] === $byte) {
                        $resolve($bytes);
                        return;
                    }

                    if (null !== $length && 0 >= $length -= $count) {
                        $resolve($bytes);
                        return;
                    }

                    $promise = $promise->then(
                        function () use ($byte, $length, $timeout) {
                            return $this->readTo($byte, $length, $timeout);
                        },
                        function (Exception $exception) use ($bytes, $resolve) {
                            $resolve($bytes);
                            throw $exception;
                        }
                    );

                    $promise->done($handler, $reject);
                };

                $promise = $this->readTo($byte, $length, $timeout);
                $promise->done($handler, $reject);
            },
            function (Exception $exception) use (&$promise) {
                $promise->cancel($exception);
            }
        );

        if ($endOnClose) {
            $result->done(null, function () use ($stream) {
                if (!$this->isOpen()) {
                    $stream->end();
                }
            });
        }

        return $result;
    }
}