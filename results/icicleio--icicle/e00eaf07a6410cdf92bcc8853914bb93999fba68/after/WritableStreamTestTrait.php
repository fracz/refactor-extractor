<?php
namespace Icicle\Tests\Stream;

use Icicle\Loop\Loop;

trait WritableStreamTestTrait
{
    /**
     * @return  [ReadableStreamInterface, WritableStreamInterface]
     */
    abstract public function createStreams();

    public function testWrite()
    {
        list($readable, $writable) = $this->createStreams();

        $string = "{'New String\0To Write'}\r\n";

        $promise = $writable->write($string);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo(strlen($string)));

        $promise->done($callback, $this->createCallback(0));

        Loop::run();

        $promise = $readable->read();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo($string));

        $promise->done($callback, $this->createCallback(0));

        Loop::run();
    }

    /**
     * @depends testWrite
     */
    public function testWriteAfterClose()
    {
        list($readable, $writable) = $this->createStreams();

        $writable->close();

        $this->assertFalse($writable->isWritable());

        $promise = $writable->write(self::WRITE_STRING);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->isInstanceOf('Icicle\Stream\Exception\UnwritableException'));

        $promise->done($this->createCallback(0), $callback);

        Loop::run();
    }

    /**
     * @depends testWrite
     */
    public function testWriteEmptyString()
    {
        list($readable, $writable) = $this->createStreams();

        $promise = $writable->write('');

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo(0));

        $promise->done($callback, $this->createCallback(0));

        Loop::run();

        $promise = $writable->write('0');

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo(1));

        $promise->done($callback, $this->createCallback(0));

        $promise = $readable->read(1);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo('0'));

        $promise->done($callback, $this->createCallback(0));

        Loop::run();
    }

    /**
     * @depends testWrite
     */
    public function testEnd()
    {
        list($readable, $writable) = $this->createStreams();

        $promise = $writable->end(self::WRITE_STRING);

        $this->assertFalse($writable->isWritable());

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo(strlen(self::WRITE_STRING)));

        $promise->done($callback, $this->createCallback(0));

        $promise = $readable->read();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo(self::WRITE_STRING));

        $promise->done($callback, $this->createCallback(0));

        $this->assertTrue($writable->isOpen());

        Loop::run();

        $this->assertFalse($writable->isOpen());
    }

    public function testAwait()
    {
        list($readable, $writable) = $this->createStreams();

        $promise = $writable->await();

        $promise->done($this->createCallback(1), $this->createCallback(0));

        Loop::run();
    }

    /**
     * @depends testAwait
     */
    public function testAwaitAfterClose()
    {
        list($readable, $writable) = $this->createStreams();

        $writable->close();

        $promise = $writable->await();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->isInstanceOf('Icicle\Stream\Exception\UnwritableException'));

        $promise->done($this->createCallback(0), $callback);

        Loop::run();
    }
}