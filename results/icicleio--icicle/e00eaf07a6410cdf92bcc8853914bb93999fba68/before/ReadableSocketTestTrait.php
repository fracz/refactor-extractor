<?php
namespace Icicle\Tests\Socket;

use Icicle\Loop\Loop;
use Icicle\Promise\Promise;

trait ReadableSocketTestTrait
{
    /**
     * @depends testRead
     */
    public function testReadAfterEof()
    {
        list($readable, $writable) = $this->createStreams();

        $writable->write(self::WRITE_STRING);

        fclose($writable->getResource()); // Close other end of pipe.

        $promise = $readable->read();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo(self::WRITE_STRING));

        $promise->done($callback, $this->createCallback(0));

        Loop::run(); // Drain readable buffer.

        $promise = $readable->read();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->isInstanceOf('Icicle\Socket\Exception\EofException'));

        $promise->done($this->createCallback(0), $callback);

        Loop::run();
    }

    /**
     * @depends testRead
     */
    public function testReadToAfterEof()
    {
        list($readable, $writable) = $this->createStreams();

        $writable->write(self::WRITE_STRING);

        fclose($writable->getResource()); // Close other end of pipe.

        $promise = $readable->readTo("\0");

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo(self::WRITE_STRING));

        $promise->done($callback, $this->createCallback(0));

        Loop::run(); // Drain readable buffer.

        $promise = $readable->readTo("\0");

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->isInstanceOf('Icicle\Socket\Exception\EofException'));

        $promise->done($this->createCallback(0), $callback);

        Loop::run();
    }

    /**
     * @depends testPipe
     */
    public function testPipeTimeout()
    {
        list($readable, $writable) = $this->createStreams();

        $writable->write(self::WRITE_STRING);

        $mock = $this->getMockBuilder('Icicle\Stream\WritableStreamInterface')->getMock();

        $mock->method('isWritable')
             ->will($this->returnValue(true));

        $mock->expects($this->once())
             ->method('write')
             ->will($this->returnCallback(function ($data) {
                 $this->assertSame(self::WRITE_STRING, $data);
                 return Promise::resolve(strlen($data));
             }));

        $mock->expects($this->never())
             ->method('end');

        $promise = $readable->pipe($mock, false, null, self::TIMEOUT);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->isInstanceOf('Icicle\Socket\Exception\TimeoutException'));

        $promise->done($this->createCallback(0), $callback);

        Loop::run();

        $this->assertTrue($readable->isOpen());
    }

    /**
     * @depends testPipeTimeout
     */
    public function testPipeWithLengthTimeout()
    {
        list($readable, $writable) = $this->createStreams();

        $writable->write(self::WRITE_STRING);

        $length = 8;

        $mock = $this->getMockBuilder('Icicle\Stream\WritableStreamInterface')->getMock();

        $mock->method('isWritable')
             ->will($this->returnValue(true));

        $mock->expects($this->once())
             ->method('write')
             ->will($this->returnCallback(function ($data) use ($length) {
                 return Promise::resolve(strlen($data));
             }));

        $mock->expects($this->never())
             ->method('end');

        $promise = $readable->pipe($mock, false, strlen(self::WRITE_STRING) + 1, self::TIMEOUT);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->isInstanceOf('Icicle\Socket\Exception\TimeoutException'));

        $promise->done($this->createCallback(0), $callback);

        Loop::run();

        $this->assertTrue($readable->isOpen());
    }

    /**
     * @depends testPipeTo
     */
    public function testPipeToTimeout()
    {
        list($readable, $writable) = $this->createStreams();

        $writable->write(self::WRITE_STRING);

        $mock = $this->getMockBuilder('Icicle\Stream\WritableStreamInterface')->getMock();

        $mock->method('isWritable')
             ->will($this->returnValue(true));

        $mock->expects($this->once())
             ->method('write')
             ->will($this->returnCallback(function ($data) {
                 $this->assertSame(self::WRITE_STRING, $data);
                 return Promise::resolve(strlen($data));
             }));

        $mock->expects($this->never())
             ->method('end');

        $promise = $readable->pipeTo($mock, '!', false, null, self::TIMEOUT);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->isInstanceOf('Icicle\Socket\Exception\TimeoutException'));

        $promise->done($this->createCallback(0), $callback);

        Loop::run();

        $this->assertTrue($readable->isOpen());
    }

    /**
     * @depends testPipeToTimeout
     */
    public function testPipeToWithLengthTimeout()
    {
        list($readable, $writable) = $this->createStreams();

        $writable->write(self::WRITE_STRING);

        $length = 8;

        $mock = $this->getMockBuilder('Icicle\Stream\WritableStreamInterface')->getMock();

        $mock->method('isWritable')
             ->will($this->returnValue(true));

        $mock->expects($this->once())
             ->method('write')
             ->will($this->returnCallback(function ($data) use ($length) {
                 return Promise::resolve(strlen($data));
             }));

        $mock->expects($this->never())
             ->method('end');

        $promise = $readable->pipeTo($mock, '!', false, strlen(self::WRITE_STRING) + 1, self::TIMEOUT);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->isInstanceOf('Icicle\Socket\Exception\TimeoutException'));

        $promise->done($this->createCallback(0), $callback);

        Loop::run();

        $this->assertTrue($readable->isOpen());
    }
}