<?php

require_once 'Swift/Tests/SwiftUnitTestCase.php';
require_once 'Swift/Transport/EsmtpHandler.php';
require_once 'Swift/Transport/CommandSentException.php';
require_once 'Swift/Mime/Message.php';
require_once 'Swift/Transport/IoBuffer.php';

abstract class Swift_Transport_AbstractEsmtpTest extends Swift_Tests_SwiftUnitTestCase
{

  /** Abstract test method */
  abstract protected function _getTransport($buf);

  public function testHostCanBeSetAndFetched()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $smtp->setHost('foo');
    $this->assertEqual('foo', $smtp->getHost(), '%s: Host should be returned');
    $context->assertIsSatisfied();
  }

  public function testPortCanBeSetAndFetched()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $smtp->setPort(25);
    $this->assertEqual(25, $smtp->getPort(), '%s: Port should be returned');
    $context->assertIsSatisfied();
  }

  public function testTimeoutCanBeSetAndFetched()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $smtp->setTimeout(10);
    $this->assertEqual(10, $smtp->getTimeout(), '%s: Timeout should be returned');
    $context->assertIsSatisfied();
  }

  public function testEncryptionCanBeSetAndFetched()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $smtp->setEncryption('tls');
    $this->assertEqual('tls', $smtp->getEncryption(), '%s: Crypto should be returned');
    $context->assertIsSatisfied();
  }

  public function testStartAccepts220ServiceGreeting()
  {
    /* -- RFC 2821, 4.2.

     Greeting = "220 " Domain [ SP text ] CRLF

     -- RFC 2822, 4.3.2.

     CONNECTION ESTABLISHMENT
         S: 220
         E: 554
    */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $s = $context->sequence('SMTP-convo');
    $context->checking(Expectations::create()
      -> one($buf)->initialize() -> inSequence($s)
      -> one($buf)->readLine(0) -> inSequence($s) -> returns("220 some.server.tld bleh\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $this->assertFalse($smtp->isStarted(), '%s: SMTP should begin non-started');
      $smtp->start();
      $this->assertTrue($smtp->isStarted(), '%s: start() should have started connection');
    }
    catch (Exception $e)
    {
      $this->fail('220 is a valid SMTP greeting and should be accepted');
    }
    $context->assertIsSatisfied();
  }

  public function testBadGreetingCausesException()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $s = $context->sequence('SMTP-convo');
    $context->checking(Expectations::create()
      -> one($buf)->initialize() -> inSequence($s)
      -> one($buf)->readLine(0) -> inSequence($s) -> returns("554 I'm busy\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $this->assertFalse($smtp->isStarted(), '%s: SMTP should begin non-started');
      $smtp->start();
      $this->fail('554 greeting indicates an error and should cause an exception');
    }
    catch (Exception $e)
    {
      $this->assertFalse($smtp->isStarted(), '%s: start() should have failed');
    }
    $context->assertIsSatisfied();
  }

  public function testStartSendsEhloToInitiate()
  {
    /* -- RFC 2821, 3.2.

      3.2 Client Initiation

         Once the server has sent the welcoming message and the client has
         received it, the client normally sends the EHLO command to the
         server, indicating the client's identity.  In addition to opening the
         session, use of EHLO indicates that the client is able to process
         service extensions and requests that the server provide a list of the
         extensions it supports.  Older SMTP systems which are unable to
         support service extensions and contemporary clients which do not
         require service extensions in the mail session being initiated, MAY
         use HELO instead of EHLO.  Servers MUST NOT return the extended
         EHLO-style response to a HELO command.  For a particular connection
         attempt, if the server returns a "command not recognized" response to
         EHLO, the client SHOULD be able to fall back and send HELO.

         In the EHLO command the host sending the command identifies itself;
         the command may be interpreted as saying "Hello, I am <domain>" (and,
         in the case of EHLO, "and I support service extension requests").

       -- RFC 2281, 4.1.1.1.

       ehlo            = "EHLO" SP Domain CRLF
       helo            = "HELO" SP Domain CRLF

       -- RFC 2821, 4.3.2.

       EHLO or HELO
           S: 250
           E: 504, 550

     */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $s = $context->sequence('SMTP-convo');
    $context->checking(Expectations::create()
      -> one($buf)->initialize() -> inSequence($s)
      -> one($buf)->readLine(0) -> inSequence($s) -> returns("220 some.server.tld bleh\r\n")
      -> one($buf)->write(pattern('~^EHLO .*?\r\n$~D')) -> inSequence($s) -> returns(1)
      -> one($buf)->readLine(1) -> inSequence($s) -> returns('250 ServerName' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $smtp->start();
    }
    catch (Exception $e)
    {
      $this->fail('Starting Esmtp should send EHLO and accept 250 response');
    }
    $context->assertIsSatisfied();
  }

  public function testHeloIsUsedAsFallback()
  {
    /* -- RFC 2821, 4.1.4.

       If the EHLO command is not acceptable to the SMTP server, 501, 500,
       or 502 failure replies MUST be returned as appropriate.  The SMTP
       server MUST stay in the same state after transmitting these replies
       that it was in before the EHLO was received.
    */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $s = $context->sequence('SMTP-convo');
    $context->checking(Expectations::create()
      -> one($buf)->initialize() -> inSequence($s)
      -> one($buf)->readLine(0) -> inSequence($s) -> returns("220 some.server.tld bleh\r\n")
      -> one($buf)->write(pattern('~^EHLO .*?\r\n$~D')) -> inSequence($s) -> returns(1)
      -> one($buf)->readLine(1) -> inSequence($s) -> returns('501 WTF' . "\r\n")
      -> one($buf)->write(pattern('~^HELO .*?\r\n$~D')) -> inSequence($s) -> returns(2)
      -> one($buf)->readLine(2) -> inSequence($s) -> returns('250 HELO' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $smtp->start();
    }
    catch (Exception $e)
    {
      $this->fail(
        'Starting Esmtp should fallback to HELO if needed and accept 250 response'
        );
    }
    $context->assertIsSatisfied();
  }

  public function testInvalidHeloResponseCausesException()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $s = $context->sequence('SMTP-convo');
    $context->checking(Expectations::create()
      -> one($buf)->initialize() -> inSequence($s)
      -> one($buf)->readLine(0) -> inSequence($s) -> returns("220 some.server.tld bleh\r\n")
      -> one($buf)->write(pattern('~^EHLO .*?\r\n$~D')) -> inSequence($s) -> returns(1)
      -> one($buf)->readLine(1) -> inSequence($s) -> returns('501 WTF' . "\r\n")
      -> one($buf)->write(pattern('~^HELO .*?\r\n$~D')) -> inSequence($s) -> returns(2)
      -> one($buf)->readLine(2) -> inSequence($s) -> returns('504 WTF' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $this->assertFalse($smtp->isStarted(), '%s: SMTP should begin non-started');
      $smtp->start();
      $this->fail('Non 250 HELO response should raise Exception');
    }
    catch (Exception $e)
    {
      $this->assertFalse($smtp->isStarted(), '%s: SMTP start() should have failed');
    }
    $context->assertIsSatisfied();
  }

  public function testDomainNameIsPlaceInEhlo()
  {
    /* -- RFC 2821, 4.1.4.

       The SMTP client MUST, if possible, ensure that the domain parameter
       to the EHLO command is a valid principal host name (not a CNAME or MX
       name) for its host.  If this is not possible (e.g., when the client's
       address is dynamically assigned and the client does not have an
       obvious name), an address literal SHOULD be substituted for the
       domain name and supplemental information provided that will assist in
       identifying the client.
    */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $s = $context->sequence('SMTP-convo');
    $context->checking(Expectations::create()
      -> one($buf)->initialize() -> inSequence($s)
      -> one($buf)->readLine(0) -> inSequence($s) -> returns("220 some.server.tld bleh\r\n")
      -> one($buf)->write("EHLO mydomain.com\r\n") -> inSequence($s) -> returns(1)
      -> one($buf)->readLine(1) -> inSequence($s) -> returns('250 ServerName' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->setLocalDomain('mydomain.com');
    $smtp->start();
    $context->assertIsSatisfied();
  }

  public function testSuccessfulMailCommand()
  {
    /* -- RFC 2821, 3.3.

    There are three steps to SMTP mail transactions.  The transaction
    starts with a MAIL command which gives the sender identification.

    .....

    The first step in the procedure is the MAIL command.

      MAIL FROM:<reverse-path> [SP <mail-parameters> ] <CRLF>

    -- RFC 2821, 4.1.1.2.

    Syntax:

      "MAIL FROM:" ("<>" / Reverse-Path)
                       [SP Mail-parameters] CRLF
    -- RFC 2821, 4.1.2.

    Reverse-path = Path
      Forward-path = Path
      Path = "<" [ A-d-l ":" ] Mailbox ">"
      A-d-l = At-domain *( "," A-d-l )
            ; Note that this form, the so-called "source route",
            ; MUST BE accepted, SHOULD NOT be generated, and SHOULD be
            ; ignored.
      At-domain = "@" domain

    -- RFC 2821, 4.3.2.

    MAIL
      S: 250
      E: 552, 451, 452, 550, 553, 503
    */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar'=>null))
      -> allowing($message)

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('250 OK' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $smtp->start();
      $smtp->send($message);
    }
    catch (Exception $e)
    {
     $this->fail('MAIL FROM should accept a 250 response');
    }
    $context->assertIsSatisfied();
  }

  public function testInvalidResponseCodeFromMailCausesException()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar'=>null))
      -> allowing($message)

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('553 Bad' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $smtp->start();
      $smtp->send($message);
      $this->fail('MAIL FROM should accept a 250 response');
    }
    catch (Exception $e)
    {
    }
    $context->assertIsSatisfied();
  }

  public function testSenderIsPreferredOverFrom()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getSender() -> returns(array('another@domain.com'=>'Someone'))
      -> allowing($message)->getTo() -> returns(array('foo@bar'=>null))
      -> allowing($message)

      -> one($buf)->write("MAIL FROM: <another@domain.com>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('250 OK' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $smtp->send($message);
    $context->assertIsSatisfied();
  }

  public function testReturnPathIsPreferredOverSender()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getSender() -> returns(array('another@domain.com'=>'Someone'))
      -> allowing($message)->getReturnPath() -> returns('more@domain.com')
      -> allowing($message)->getTo() -> returns(array('foo@bar'=>null))
      -> allowing($message)

      -> one($buf)->write("MAIL FROM: <more@domain.com>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('250 OK' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $smtp->send($message);
    $context->assertIsSatisfied();
  }

  public function testSuccessfulRcptCommandWith250Response()
  {
    /* -- RFC 2821, 3.3.

     The second step in the procedure is the RCPT command.

      RCPT TO:<forward-path> [ SP <rcpt-parameters> ] <CRLF>

     The first or only argument to this command includes a forward-path
     (normally a mailbox and domain, always surrounded by "<" and ">"
     brackets) identifying one recipient.  If accepted, the SMTP server
     returns a 250 OK reply and stores the forward-path.  If the recipient
     is known not to be a deliverable address, the SMTP server returns a
     550 reply, typically with a string such as "no such user - " and the
     mailbox name (other circumstances and reply codes are possible).
     This step of the procedure can be repeated any number of times.

    -- RFC 2821, 4.1.1.3.

    This command is used to identify an individual recipient of the mail
    data; multiple recipients are specified by multiple use of this
    command.  The argument field contains a forward-path and may contain
    optional parameters.

    The forward-path normally consists of the required destination
    mailbox.  Sending systems SHOULD not generate the optional list of
    hosts known as a source route.

    .......

    "RCPT TO:" ("<Postmaster@" domain ">" / "<Postmaster>" / Forward-Path)
                    [SP Rcpt-parameters] CRLF

    -- RFC 2821, 4.2.2.

      250 Requested mail action okay, completed
      251 User not local; will forward to <forward-path>
         (See section 3.4)
      252 Cannot VRFY user, but will accept message and attempt
          delivery

    -- RFC 2821, 4.3.2.

    RCPT
      S: 250, 251 (but see section 3.4 for discussion of 251 and 551)
      E: 550, 551, 552, 553, 450, 451, 452, 503, 550
    */

    //We'll treat 252 as accepted since it isn't really a failure

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $s = $context->sequence('SMTP-envelope');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar'=>null))
      -> allowing($message)

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> inSequence($s) -> returns(1)
      -> one($buf)->readLine(1) -> returns('250 OK' . "\r\n")
      -> one($buf)->write("RCPT TO: <foo@bar>\r\n") -> inSequence($s) -> returns(2)
      -> one($buf)->readLine(2) -> returns('250 OK' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $smtp->start();
      $smtp->send($message);
    }
    catch (Exception $e)
    {
      $this->fail('RCPT TO should accept a 250 response');
    }
    $context->assertIsSatisfied();
  }

  public function testMultipleRecipientsSendsMultipleRcpt()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array(
        'foo@bar' => null,
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> allowing($message)

      -> one($buf)->write("RCPT TO: <foo@bar>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('250 OK' . "\r\n")
      -> one($buf)->write("RCPT TO: <zip@button>\r\n") -> returns(2)
      -> one($buf)->readLine(2) -> returns('250 OK' . "\r\n")
      -> one($buf)->write("RCPT TO: <test@domain>\r\n") -> returns(3)
      -> one($buf)->readLine(3) -> returns('250 OK' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $smtp->send($message);
    $context->assertIsSatisfied();
  }

  public function testCcRecipientsSendsMultipleRcpt()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)->getCc() -> returns(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> allowing($message)

      -> one($buf)->write("RCPT TO: <foo@bar>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('250 OK' . "\r\n")
      -> one($buf)->write("RCPT TO: <zip@button>\r\n") -> returns(2)
      -> one($buf)->readLine(2) -> returns('250 OK' . "\r\n")
      -> one($buf)->write("RCPT TO: <test@domain>\r\n") -> returns(3)
      -> one($buf)->readLine(3) -> returns('250 OK' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $smtp->send($message);
    $context->assertIsSatisfied();
  }

  public function testSendReturnsNumberOfSuccessfulRecipients()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)->getCc() -> returns(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> allowing($message)

      -> one($buf)->write("RCPT TO: <foo@bar>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('250 OK' . "\r\n")
      -> one($buf)->write("RCPT TO: <zip@button>\r\n") -> returns(2)
      -> one($buf)->readLine(2) -> returns('501 Nobody here' . "\r\n")
      -> one($buf)->write("RCPT TO: <test@domain>\r\n") -> returns(3)
      -> one($buf)->readLine(3) -> returns('250 OK' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $this->assertEqual(2, $smtp->send($message),
      '%s: 1 of 3 recipients failed so 2 should be returned'
      );
    $context->assertIsSatisfied();
  }

  public function testRsetIsSentIfNoSuccessfulRecipients()
  {
    /* --RFC 2821, 4.1.1.5.

    This command specifies that the current mail transaction will be
    aborted.  Any stored sender, recipients, and mail data MUST be
    discarded, and all buffers and state tables cleared.  The receiver
    MUST send a "250 OK" reply to a RSET command with no arguments.  A
    reset command may be issued by the client at any time.

    -- RFC 2821, 4.3.2.

    RSET
      S: 250
    */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)

      -> one($buf)->write("RCPT TO: <foo@bar>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('503 Bad' . "\r\n")
      -> one($buf)->write("RSET\r\n") -> returns(2)
      -> one($buf)->readLine(2) -> returns('250 OK' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $this->assertEqual(0, $smtp->send($message),
      '%s: 1 of 1 recipients failed so 0 should be returned'
      );
    $context->assertIsSatisfied();
  }

  public function testSuccessfulDataCommand()
  {
    /* -- RFC 2821, 3.3.

    The third step in the procedure is the DATA command (or some
    alternative specified in a service extension).

          DATA <CRLF>

    If accepted, the SMTP server returns a 354 Intermediate reply and
    considers all succeeding lines up to but not including the end of
    mail data indicator to be the message text.

    -- RFC 2821, 4.1.1.4.

    The receiver normally sends a 354 response to DATA, and then treats
    the lines (strings ending in <CRLF> sequences, as described in
    section 2.3.7) following the command as mail data from the sender.
    This command causes the mail data to be appended to the mail data
    buffer.  The mail data may contain any of the 128 ASCII character
    codes, although experience has indicated that use of control
    characters other than SP, HT, CR, and LF may cause problems and
    SHOULD be avoided when possible.

    -- RFC 2821, 4.3.2.

    DATA
      I: 354 -> data -> S: 250
                        E: 552, 554, 451, 452
      E: 451, 554, 503
    */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)

      -> one($buf)->write("DATA\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('354 Go ahead' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $smtp->start();
      $smtp->send($message);
    }
    catch (Exception $e)
    {
      $this->fail('354 is the expected response to DATA');
    }
    $context->assertIsSatisfied();
  }

  public function testBadDataResponseCausesException()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)

      -> one($buf)->write("DATA\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns('451 Bad' . "\r\n")
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $smtp->start();
      $smtp->send($message);
      $this->fail('354 is the expected response to DATA (not observed)');
    }
    catch (Exception $e)
    {
    }
    $context->assertIsSatisfied();
  }

  public function testMessageIsStreamedToBufferForData()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $s = $context->sequence('DATA Streaming');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))

      -> one($buf)->write("DATA\r\n") -> inSequence($s) -> returns(1)
      -> one($buf)->readLine(1) -> returns('354 OK' . "\r\n")
      -> one($message)->toByteStream($buf) -> inSequence($s)
      -> one($buf)->write("\r\n.\r\n") -> inSequence($s) -> returns(2)
      -> one($buf)->readLine(2) -> returns('250 OK' . "\r\n")

      -> allowing($message)
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $smtp->send($message);
    $context->assertIsSatisfied();
  }

  public function testBadResponseAfterDataTransmissionCausesException()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $s = $context->sequence('DATA Streaming');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))

      -> one($buf)->write("DATA\r\n") -> inSequence($s) -> returns(1)
      -> one($buf)->readLine(1) -> returns('354 OK' . "\r\n")
      -> one($message)->toByteStream($buf) -> inSequence($s)
      -> one($buf)->write("\r\n.\r\n") -> inSequence($s) -> returns(2)
      -> one($buf)->readLine(2) -> returns('554 Error' . "\r\n")

      -> allowing($message)
      );
    $this->_finishBuffer($context, $buf);
    try
    {
      $smtp->start();
      $smtp->send($message);
      $this->fail('250 is the expected response after a DATA transmission (not observed)');
    }
    catch (Exception $e)
    {
    }
    $context->assertIsSatisfied();
  }

  public function testBccRecipientsAreRemovedFromHeaders()
  {
    /* -- RFC 2821, 7.2.

     Addresses that do not appear in the message headers may appear in the
     RCPT commands to an SMTP server for a number of reasons.  The two
     most common involve the use of a mailing address as a "list exploder"
     (a single address that resolves into multiple addresses) and the
     appearance of "blind copies".  Especially when more than one RCPT
     command is present, and in order to avoid defeating some of the
     purpose of these mechanisms, SMTP clients and servers SHOULD NOT copy
     the full set of RCPT command arguments into the headers, either as
     part of trace headers or as informational or private-extension
     headers.  Since this rule is often violated in practice, and cannot
     be enforced, sending SMTP systems that are aware of "bcc" use MAY
     find it helpful to send each blind copy as a separate message
     transaction containing only a single RCPT command.
     */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)->getBcc() -> returns(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> atLeast(1)->of($message)->setBcc(array())
      -> allowing($message)
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $smtp->send($message);
    $context->assertIsSatisfied();
  }

  public function testEachBccRecipientIsSentASeparateMessage()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)->getBcc() -> returns(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> atLeast(1)->of($message)->setBcc(array())
      -> one($message)->setBcc(array('zip@button' => 'Zip Button'))
      -> one($message)->setBcc(array('test@domain' => 'Test user'))
      -> atLeast(1)->of($message)->setBcc(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> allowing($message)

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns("250 OK\r\n")
      -> one($buf)->write("RCPT TO: <foo@bar>\r\n") -> returns(2)
      -> one($buf)->readLine(2) -> returns("250 OK\r\n")
      -> one($buf)->write("DATA\r\n") -> returns(3)
      -> one($buf)->readLine(3) -> returns("354 OK\r\n")
      -> one($buf)->write("\r\n.\r\n") -> returns(4)
      -> one($buf)->readLine(4) -> returns("250 OK\r\n")

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(5)
      -> one($buf)->readLine(5) -> returns("250 OK\r\n")
      -> one($buf)->write("RCPT TO: <zip@button>\r\n") -> returns(6)
      -> one($buf)->readLine(6) -> returns("250 OK\r\n")
      -> one($buf)->write("DATA\r\n") -> returns(7)
      -> one($buf)->readLine(7) -> returns("354 OK\r\n")
      -> one($buf)->write("\r\n.\r\n") -> returns(8)
      -> one($buf)->readLine(8) -> returns("250 OK\r\n")

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(9)
      -> one($buf)->readLine(9) -> returns("250 OK\r\n")
      -> one($buf)->write("RCPT TO: <test@domain>\r\n") -> returns(10)
      -> one($buf)->readLine(10) -> returns("250 OK\r\n")
      -> one($buf)->write("DATA\r\n") -> returns(11)
      -> one($buf)->readLine(11) -> returns("354 OK\r\n")
      -> one($buf)->write("\r\n.\r\n") -> returns(12)
      -> one($buf)->readLine(12) -> returns("250 OK\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $this->assertEqual(3, $smtp->send($message));
    $context->assertIsSatisfied();
  }

  public function testMessageStateIsRestoredOnFailure()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)->getBcc() -> returns(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> one($message)->setBcc(array())
      -> one($message)->setBcc(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> allowing($message)
      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns("250 OK\r\n")
      -> one($buf)->write("RCPT TO: <foo@bar>\r\n") -> returns(2)
      -> one($buf)->readLine(2) -> returns("250 OK\r\n")
      -> one($buf)->write("DATA\r\n") -> returns(3)
      -> one($buf)->readLine(3) -> returns("451 No\r\n")
      );
    $this->_finishBuffer($context, $buf);

    $smtp->start();
    try
    {
      $smtp->send($message);
      $this->fail('A bad response was given so exception is expected');
    }
    catch (Exception $e)
    {
    }

    $context->assertIsSatisfied();
  }

  public function testStopSendsQuitCommand()
  {
    /* -- RFC 2821, 4.1.1.10.

    This command specifies that the receiver MUST send an OK reply, and
    then close the transmission channel.

    The receiver MUST NOT intentionally close the transmission channel
    until it receives and replies to a QUIT command (even if there was an
    error).  The sender MUST NOT intentionally close the transmission
    channel until it sends a QUIT command and SHOULD wait until it
    receives the reply (even if there was an error response to a previous
    command).  If the connection is closed prematurely due to violations
    of the above or system or network failure, the server MUST cancel any
    pending transaction, but not undo any previously completed
    transaction, and generally MUST act as if the command or transaction
    in progress had received a temporary error (i.e., a 4yz response).

    The QUIT command may be issued at any time.

    Syntax:
      "QUIT" CRLF
    */

    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> one($buf)->initialize()
      -> one($buf)->write("QUIT\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns("221 Bye\r\n")
      -> one($buf)->terminate()
      );
    $this->_finishBuffer($context, $buf);

    $this->assertFalse($smtp->isStarted());
    $smtp->start();
    $this->assertTrue($smtp->isStarted());
    $smtp->stop();
    $this->assertFalse($smtp->isStarted());

    $context->assertIsSatisfied();
  }

  public function testBufferCanBeFetched()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);

    $this->assertReference($buf, $smtp->getBuffer());

    $context->assertIsSatisfied();
  }

  public function testBufferCanBeWrittenToUsingExecuteCommand()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> one($buf)->write("FOO\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns("250 OK\r\n")
      -> ignoring($buf)
      );

    $res = $smtp->executeCommand("FOO\r\n");
    $this->assertEqual("250 OK\r\n", $res);

    $context->assertIsSatisfied();
  }

  public function testResponseCodesAreValidated()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> one($buf)->write("FOO\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns("551 Not ok\r\n")
      -> ignoring($buf)
      );

    try
    {
      $smtp->executeCommand("FOO\r\n", array(250, 251));
      $this->fail('A 250 or 251 response was needed but 551 was returned.');
    }
    catch (Exception $e)
    {
    }

    $context->assertIsSatisfied();
  }

  public function testFailedRecipientsCanBeCollectedByReference()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);
    $message = $context->mock('Swift_Mime_Message');
    $context->checking(Expectations::create()
      -> allowing($message)->getFrom() -> returns(array('me@domain.com'=>'Me'))
      -> allowing($message)->getTo() -> returns(array('foo@bar' => null))
      -> allowing($message)->getBcc() -> returns(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> atLeast(1)->of($message)->setBcc(array())
      -> one($message)->setBcc(array('zip@button' => 'Zip Button'))
      -> one($message)->setBcc(array('test@domain' => 'Test user'))
      -> atLeast(1)->of($message)->setBcc(array(
        'zip@button' => 'Zip Button',
        'test@domain' => 'Test user'
        ))
      -> allowing($message)

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(1)
      -> one($buf)->readLine(1) -> returns("250 OK\r\n")
      -> one($buf)->write("RCPT TO: <foo@bar>\r\n") -> returns(2)
      -> one($buf)->readLine(2) -> returns("250 OK\r\n")
      -> one($buf)->write("DATA\r\n") -> returns(3)
      -> one($buf)->readLine(3) -> returns("354 OK\r\n")
      -> one($buf)->write("\r\n.\r\n") -> returns(4)
      -> one($buf)->readLine(4) -> returns("250 OK\r\n")

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(5)
      -> one($buf)->readLine(5) -> returns("250 OK\r\n")
      -> one($buf)->write("RCPT TO: <zip@button>\r\n") -> returns(6)
      -> one($buf)->readLine(6) -> returns("500 Bad\r\n")
      -> one($buf)->write("RSET\r\n") -> returns(7)
      -> one($buf)->readLine(7) -> returns("250 OK\r\n")

      -> one($buf)->write("MAIL FROM: <me@domain.com>\r\n") -> returns(8)
      -> one($buf)->readLine(8) -> returns("250 OK\r\n")
      -> one($buf)->write("RCPT TO: <test@domain>\r\n") -> returns(9)
      -> one($buf)->readLine(9) -> returns("500 Bad\r\n")
      -> one($buf)->write("RSET\r\n") -> returns(10)
      -> one($buf)->readLine(10) -> returns("250 OK\r\n")
      );
    $this->_finishBuffer($context, $buf);
    $smtp->start();
    $this->assertEqual(1, $smtp->send($message, $failures));
    $this->assertEqual(array('zip@button', 'test@domain'), $failures,
      '%s: Failures should be caught in an array'
      );
    $context->assertIsSatisfied();
  }

  public function testFluidInterface()
  {
    $context = new Mockery();
    $buf = $this->_getBuffer($context);
    $smtp = $this->_getTransport($buf);

    $ref = $smtp
      ->setHost('foo')
      ->setPort(25)
      ->setEncryption('tls')
      ->setTimeout(30)
      ;
    $this->assertReference($ref, $smtp);

    $context->assertIsSatisfied();
  }

  // -- Protected methods

  protected function _getBuffer($context)
  {
    return $context->mock('Swift_Transport_IoBuffer');
  }

  protected function _finishBuffer($context, $buf)
  {
    $context->checking(Expectations::create()
      -> ignoring($buf)->readLine(0) -> returns('220 server.com foo' . "\r\n")
      -> ignoring($buf)->write(pattern('~^EHLO .*?\r\n$~D')) -> returns($x = uniqid())
      -> ignoring($buf)->readLine($x) -> returns('250 ServerName' . "\r\n")
      -> ignoring($buf)->write(pattern('~^MAIL FROM: <.*?>\r\n$~D')) -> returns($x = uniqid())
      -> ignoring($buf)->readLine($x) -> returns('250 OK' . "\r\n")
      -> ignoring($buf)->write(pattern('~^RCPT TO: <.*?>\r\n$~D')) -> returns($x = uniqid())
      -> ignoring($buf)->readLine($x) -> returns('250 OK' . "\r\n")
      -> ignoring($buf)->write("DATA\r\n") -> returns($x = uniqid())
      -> ignoring($buf)->readLine($x) -> returns('354 OK' . "\r\n")
      -> ignoring($buf)->write("\r\n.\r\n") -> returns($x = uniqid())
      -> ignoring($buf)->readLine($x) -> returns('250 OK' . "\r\n")
      -> ignoring($buf) -> returns(false)
      );
  }

}