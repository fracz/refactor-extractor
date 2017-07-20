<?php
namespace Aws\Test;

use Aws\AwsClientInterface;
use Aws\Result;
use Aws\Sdk;
use GuzzleHttp\Command\Event\PrepareEvent;
use GuzzleHttp\Command\Exception\CommandException;
use GuzzleHttp\Message\Response;
use GuzzleHttp\Subscriber\Mock;

trait UsesServiceClientTrait
{
    /**
     * Creates an instance of the AWS SDK for a test
     *
     * @param array $args
     *
     * @return Sdk
     */
    private function getTestSdk(array $args = [])
    {
        return new Sdk($args + [
            'region'      => 'us-east-1',
            'credentials' => false,
            'retries'     => false
        ]);
    }

    /**
     * Creates an instance of a service client for a test
     *
     * @param string $service
     * @param array  $args
     *
     * @return AwsClientInterface
     */
    private function getTestClient($service, array $args = [])
    {
        return $this->getTestSdk()->getClient($service, $args);
    }

    /**
     * Queues up mock Result objects for a client
     *
     * @param AwsClientInterface $client
     * @param Result[]           $results
     *
     * @return AwsClientInterface
     */
    private function addMockResults(AwsClientInterface $client, array $results)
    {
        $client->getEmitter()->on('prepare',
            function (PrepareEvent $event) use (&$results) {
                $result = array_shift($results);
                if ($result instanceof Result) {
                    $event->setResult($result);
                } elseif ($result instanceof CommandException) {
                    throw $result;
                } else {
                    throw new \Exception('There are no more mock results left. '
                        . 'This client executed more commands than expected.');
                }
            },
            'first'
        );

        return $client;
    }

    /**
     * Queues up mock HTTP Response objects for a client
     *
     * @param AwsClientInterface $client
     * @param Response[]         $responses
     *
     * @return AwsClientInterface
     * @throws \InvalidArgumentException
     */
    private function addMockResponses($client, array $responses)
    {
        $client->getHttpClient()->getEmitter()->attach(new Mock($responses));

        return $client;
    }

    /**
     * Creates a mock CommandException with a given error code
     *
     * @param string $code
     *
     * @return CommandException
     */
    private function createMockCommandException($code)
    {
        return new CommandException(
            'Test error',
            $this->getMock('Aws\AwsClientInterface'),
            $this->getMock('Aws\AwsCommandInterface'),
            null,
            null,
            null,
            ['aws_error' => ['message' => 'Test error', 'code' => $code]]
        );
    }
}