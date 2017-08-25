<?php

require_once 'Swift/Tests/SwiftUnitTestCase.php';
require_once 'Swift/Events/TransportExceptionEvent.php';
require_once 'Swift/Transport.php';
require_once 'Swift/Transport/TransportException.php';

class Swift_Events_TransportExceptionEventTest extends Swift_Tests_SwiftUnitTestCase
{

  public function testExceptionIsInjectable()
  {
    $e = new Swift_Transport_TransportException('foo');
    $evt = new Swift_Events_TransportExceptionEvent();
    $evt->exception = $e;
    $ref = $evt->getException();
    $this->assertReference($e, $ref,
      '%s: Exception should be injectable'
      );
  }

  public function testCleanCloneIsGenerated()
  {
    $transport = $this->_mock('Swift_Transport');

    $evt = new Swift_Events_TransportExceptionEvent();
    $evt->exception = new Swift_Transport_TransportException('foo');

    $clone = $evt->cloneFor($transport);

    $this->assertNull($clone->getException());
    $source = $clone->getSource();
    $this->assertReference($transport, $source,
      '%s: Transport should be available via getSource()'
      );
  }

}