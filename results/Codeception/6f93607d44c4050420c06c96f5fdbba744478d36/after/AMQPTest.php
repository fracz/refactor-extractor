<?php

use Codeception\Util\Stub as Stub;

class AMQPTest extends \PHPUnit_Framework_TestCase
{
    protected $config = array(
        'host'     => 'localhost',
        'username' => 'guest',
        'password' => 'guest',
        'port' => '5672',
        'vhost'    => '/',
        'cleanup' => true,
        'queues' => array('queue1')
    );

    /**
     * @var \Codeception\Module\AMQP
     */
    protected $module = null;

    public function setUp()
    {
        $this->module = new \Codeception\Module\AMQP;
        $this->module->_setConfig($this->config);
        $res = stream_socket_client('tcp://localhost:5672');
        if ($res === false) $this->markTestSkipped('AMQP is not running');

        $this->module->_initialize();
        $this->module->_before(Stub::makeEmpty('\Codeception\TestCase\Cept'));
    }

    public function testQueueUsage()
    {
        $this->module->pushToQueue('queue1', 'hello');
        $this->module->seeMessageInQueueContainsText('queue1','hello');
        $msg = $this->module->grabMessageFromQueue('queue1');
        $this->assertEquals('hello', $msg->body);
    }


}