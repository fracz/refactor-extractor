<?php
namespace Aws\Test;

use Aws\Common\Api\Service;
use Aws\AwsClient;
use Aws\AwsException;
use GuzzleHttp\Client;
use GuzzleHttp\Command\Command;
use GuzzleHttp\Command\Exception\CommandException;

/**
 * @covers Aws\AwsException
 */
class AwsExceptionTest extends \PHPUnit_Framework_TestCase
{
    /**
     * @expectedException \InvalidArgumentException
     */
    public function testEnsuresWrappedExceptionIsValid()
    {
        $e = $this->getMockBuilder('GuzzleHttp\Command\Exception\CommandException')
            ->setMethods(['getClient'])
            ->disableOriginalConstructor()
            ->getMock();
        $e->expects($this->once())
            ->method('getClient')
            ->will($this->returnValue('foo'));
        AwsException::wrap($e);
    }

    public function testProvidesExceptionData()
    {
        $api = new Service([
            'metadata' => ['endpointPrefix' => 'ec2']
        ]);

        $client = new AwsClient([
            'api'         => $api,
            'credentials' => 'foo',
            'client'      => new Client(),
            'signature'   => 'bar',
            'region'      => 'boo'
        ]);

        $cmd = new Command('foo');

        $e1 = new CommandException('foo', $client, $cmd, null, null, null, [
            'aws_error' => [
                'message'    => 'a',
                'request_id' => 'b',
                'type'       => 'c',
                'code'       => 'd'
            ]
        ]);

        $e2 = AwsException::wrap($e1);
        $this->assertSame('ec2', $e2->getServiceName());
        $this->assertSame($e1, $e2->getPrevious());
        $this->assertSame($api, $e2->getApi());
        $this->assertSame($client, $e2->getClient());
        $this->assertEquals('d', $e2->getAwsErrorCode());
        $this->assertEquals('b', $e2->getAwsRequestId());
        $this->assertEquals('c', $e2->getAwsErrorType());
        $this->assertEquals('AWS (ec2) Error: a', $e2->getMessage());
    }

    public function testUsesPreviousExceptionMessage()
    {
        $api = new Service([
            'metadata' => ['endpointPrefix' => 'ec2']
        ]);

        $client = new AwsClient([
            'api'         => $api,
            'credentials' => 'foo',
            'client'      => new Client(),
            'signature'   => 'bar',
            'region'      => 'boo'
        ]);

        $command = $this->getMockBuilder('Aws\AwsCommandInterface')
            ->getMockForAbstractClass();
        $e = new CommandException('Previous', $client, $command);
        $ex = AwsException::wrap($e);
        $this->assertContains('Previous', $ex->getMessage());
    }
}