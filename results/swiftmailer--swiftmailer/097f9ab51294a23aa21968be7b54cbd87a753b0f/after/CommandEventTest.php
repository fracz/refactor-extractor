<?php

require_once 'Swift/Tests/SwiftUnitTestCase.php';
require_once 'Swift/Events/CommandEvent.php';
require_once 'Swift/Transport/EsmtpBufferWrapper.php';

class Swift_Events_CommandEventTest extends Swift_Tests_SwiftUnitTestCase
{

  public function testCommandCanBeInjected()
  {
    $evt = new Swift_Events_CommandEvent();
    $evt->command = "HELO foobar.net\r\n";
    $this->assertEqual("HELO foobar.net\r\n", $evt->getCommand());
  }

  public function testSuccessCodesCanBeInjected()
  {
    $evt = new Swift_Events_CommandEvent();
    $evt->successCodes = array(250, 251);
    $this->assertEqual(array(250, 251), $evt->getSuccessCodes());
  }

  public function testCleanCloneIsGenerated()
  {
    $buf = $this->_mock('Swift_Transport_EsmtpBufferWrapper');

    $evt = new Swift_Events_CommandEvent();
    $evt->command = "HELO foobar.net\r\n";
    $evt->successCodes = array(250);

    $clone = $evt->cloneFor($buf);

    $this->assertEqual('', $clone->getCommand());
    $this->assertEqual(array(), $clone->getSuccessCodes());
    $source = $clone->getSource();
    $this->assertReference($buf, $source);
  }

}