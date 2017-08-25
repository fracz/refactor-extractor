<?php

require_once 'Swift/Mime/MimeEntity.php';
require_once 'Swift/Mime/SimpleMessage.php';
require_once 'Swift/Tests/SwiftUnitTestCase.php';
require_once 'Swift/Mime/ContentEncoder.php';
require_once 'Swift/Mime/Header.php';
require_once 'Swift/Mime/FieldChangeObserver.php';
require_once 'Swift/KeyCache.php';
require_once 'Swift/KeyCache/KeyCacheInputStream.php';

Mock::generate('Swift_Mime_ContentEncoder', 'Swift_Mime_MockContentEncoder');
Mock::generate('Swift_Mime_Header', 'Swift_Mime_MockHeader');
Mock::generate('Swift_Mime_FieldChangeObserver',
  'Swift_Mime_MockFieldChangeObserver'
  );
Mock::generate('Swift_Mime_MimeEntity', 'Swift_Mime_MockMimeEntity');
Mock::generate('Swift_KeyCache', 'Swift_MockKeyCache');
Mock::generate('Swift_KeyCache_KeyCacheInputStream',
  'Swift_KeyCache_MockKeyCacheInputStream'
  );

class Swift_Mime_SimpleMessageTest extends Swift_Tests_SwiftUnitTestCase
{
  private $_encoder;
  private $_cache;

  public function setUp()
  {
    $this->_cache = new Swift_MockKeyCache();
    $this->_encoder = new Swift_Mime_MockContentEncoder();
    $this->_encoder->setReturnValue('getName', 'quoted-printable');
  }

  public function testHeadersAreReturned()
  {
    $h = new Swift_Mime_MockHeader();
    $h->setReturnValue('getFieldName', 'Content-Type');
    $headers = array($h);
    $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);
    $this->assertEqual($headers, $message->getHeaders());
  }

  public function testHeadersAppearInString()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $h1->setReturnValue('getFieldBody', 'text/html');
    $h1->setReturnValue('toString', 'Content-Type: text/html' . "\r\n");
    $h2 = new Swift_Mime_MockHeader();
    $h2->setReturnValue('getFieldName', 'X-Header');
    $h2->setReturnValue('getFieldBody', 'foo');
    $h2->setReturnValue('toString', 'X-Header: foo' . "\r\n");
    $headers = array($h1, $h2);

    $this->_cache->setReturnValue('getString',
      'Content-Type: text/html' . "\r\n" .
      'X-Header: foo' . "\r\n",
      array('*', 'headers')
      );

    $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);
    $this->assertEqual(
      'Content-Type: text/html' . "\r\n" .
      'X-Header: foo' . "\r\n",
      $message->toString()
      );
  }

  public function testBodyIsAppended()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $h1->setReturnValue('getFieldBody', 'text/html');
    $h1->setReturnValue('toString', 'Content-Type: text/html' . "\r\n");
    $h2 = new Swift_Mime_MockHeader();
    $h2->setReturnValue('getFieldName', 'X-Header');
    $h2->setReturnValue('getFieldBody', 'foo');
    $h2->setReturnValue('toString', 'X-Header: foo' . "\r\n");
    $headers = array($h1, $h2);
    $this->_encoder->setReturnValue('encodeString', 'my body');

    $this->_cache->setReturnValue('getString',
      'Content-Type: text/html' . "\r\n" .
      'X-Header: foo' . "\r\n",
      array('*', 'headers')
      );
    $this->_cache->setReturnValue('getString', "\r\n" . 'my body',
      array('*', 'body')
      );

    $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);
    $message->setBodyAsString('my body');
    $this->assertEqual(
      'Content-Type: text/html' . "\r\n" .
      'X-Header: foo' . "\r\n" .
      "\r\n" .
      'my body',
      $message->toString()
      );
  }

  public function testContentTypeCanBeSetAndFetched()
  {
    /* --
    This comes in very useful so Headers can observe the entity for things
    such as content-type or content-transfer-encoding changes.
    */

    $h = new Swift_Mime_MockHeader();
    $h->setReturnValue('getFieldName', 'Content-Type');
    $headers = array($h);

    $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);
    $message->setContentType('text/html');

    $this->assertEqual('text/html', $message->getContentType());
  }

  public function testMimeFieldObserversAreNotifiedOnChange()
  {
    /* --
    This comes in very useful so Headers can observe the entity for things
    such as content-type or content-transfer-encoding changes.
    */

    $h = new Swift_Mime_MockHeader();
    $h->setReturnValue('getFieldName', 'Content-Type');
    $headers = array($h);

    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('contenttype', 'text/html'));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('contenttype', 'text/html'));

    $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);
    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setContentType('text/html');
  }

  public function testAddingChildrenGeneratesBoundary()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $headers1 = array($h1);

    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    //ack!
    $observer1->expectAt(1, 'fieldChanged', array('boundary', '*'));

    $entity1 = $this->_createMessage($headers1, $this->_encoder, $this->_cache);
    $entity1->registerFieldChangeObserver($observer1);

    $h2 = new Swift_Mime_MockHeader();
    $h2->setReturnValue('getFieldName', 'Content-Type');
    $headers2 = array($h2);

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getHeaders', $headers2);
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );

    $entity1->setChildren(array($entity2));
  }

  public function testChildrenOfLevelAttachmentOrLessGeneratesMultipartMixed()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $headers1 = array($h1);

    for ($level = Swift_Mime_MimeEntity::LEVEL_ATTACHMENT;
      $level > Swift_Mime_MimeEntity::LEVEL_TOP; $level--)
    {
      $message = $this->_createMessage($headers1, $this->_encoder, $this->_cache);

      $observer = new Swift_Mime_MockFieldChangeObserver();
      $observer->expectAt(0, 'fieldChanged', array('contenttype', 'multipart/mixed'));
      $observer->expectAt(1, 'fieldChanged', array('boundary', '*'));
      $observer->expectMinimumCallCount('fieldChanged', 2);

      $message->registerFieldChangeObserver($observer);

      $h2 = new Swift_Mime_MockHeader();
      $h2->setReturnValue('getFieldName', 'Content-Type');
      $headers2 = array($h2);

      $entity2 = new Swift_Mime_MockMimeEntity();
      $entity2->setReturnValue('getHeaders', $headers2);
      $entity2->setReturnValue('getNestingLevel', $level);

      $message->setChildren(array($entity2));
    }
  }

  public function testChildrenOfLevelEmbeddedOrLessGeneratesMultipartRelated()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $headers1 = array($h1);

    for ($level = Swift_Mime_MimeEntity::LEVEL_EMBEDDED;
      $level > Swift_Mime_MimeEntity::LEVEL_ATTACHMENT; $level--)
    {
      $message = $this->_createMessage($headers1, $this->_encoder, $this->_cache);

      $observer = new Swift_Mime_MockFieldChangeObserver();
      $observer->expectAt(0, 'fieldChanged', array('contenttype', 'multipart/related'));
      $observer->expectAt(1, 'fieldChanged', array('boundary', '*'));
      $observer->expectMinimumCallCount('fieldChanged', 2);

      $message->registerFieldChangeObserver($observer);

      $h2 = new Swift_Mime_MockHeader();
      $h2->setReturnValue('getFieldName', 'Content-Type');
      $headers2 = array($h2);

      $entity2 = new Swift_Mime_MockMimeEntity();
      $entity2->setReturnValue('getHeaders', $headers2);
      $entity2->setReturnValue('getNestingLevel', $level);

      $message->setChildren(array($entity2));
    }
  }

  public function testChildrenOfLevelSubpartOrLessGeneratesMultipartAlternative()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $headers1 = array($h1);

    for ($level = Swift_Mime_MimeEntity::LEVEL_SUBPART;
      $level > Swift_Mime_MimeEntity::LEVEL_EMBEDDED; $level--)
    {
      $message = $this->_createMessage($headers1, $this->_encoder, $this->_cache);

      $observer = new Swift_Mime_MockFieldChangeObserver();
      $observer->expectAt(0, 'fieldChanged', array('contenttype', 'multipart/alternative'));
      $observer->expectAt(1, 'fieldChanged', array('boundary', '*'));
      $observer->expectMinimumCallCount('fieldChanged', 2);

      $message->registerFieldChangeObserver($observer);

      $h2 = new Swift_Mime_MockHeader();
      $h2->setReturnValue('getFieldName', 'Content-Type');
      $headers2 = array($h2);

      $entity2 = new Swift_Mime_MockMimeEntity();
      $entity2->setReturnValue('getHeaders', $headers2);
      $entity2->setReturnValue('getNestingLevel', $level);

      $message->setChildren(array($entity2));
    }
  }

  public function testHighestLevelChildDeterminesContentType()
  {
    $combinations  = array(
      array('levels' => array(Swift_Mime_MimeEntity::LEVEL_ATTACHMENT,
        Swift_Mime_MimeEntity::LEVEL_EMBEDDED,
        Swift_Mime_MimeEntity::LEVEL_SUBPART
        ),
        'type' => 'multipart/mixed'
        ),
      array('levels' => array(Swift_Mime_MimeEntity::LEVEL_ATTACHMENT,
        Swift_Mime_MimeEntity::LEVEL_EMBEDDED
        ),
        'type' => 'multipart/mixed'
        ),
      array('levels' => array(Swift_Mime_MimeEntity::LEVEL_ATTACHMENT,
        Swift_Mime_MimeEntity::LEVEL_SUBPART
        ),
        'type' => 'multipart/mixed'
        ),
      array('levels' => array(Swift_Mime_MimeEntity::LEVEL_EMBEDDED,
        Swift_Mime_MimeEntity::LEVEL_SUBPART
        ),
        'type' => 'multipart/related'
        )
      );

    foreach ($combinations as $combination)
    {
      $children = array();
      foreach ($combination['levels'] as $level)
      {
        $subentity = new Swift_Mime_MockMimeEntity();
        $subentity->setReturnValue('getNestingLevel', $level);
        $children[] = $subentity;
      }

      $headers = array();
      $h1 = new Swift_Mime_MockHeader();
      $h1->setReturnValue('getFieldName', 'Content-Type');

      $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);

      $observer = new Swift_Mime_MockFieldChangeObserver();
      $observer->expectAt(0, 'fieldChanged',
        array('contenttype', $combination['type'])
        );
      $observer->expectAt(1, 'fieldChanged', array('boundary', '*'));

      $message->registerFieldChangeObserver($observer);

      $message->setChildren($children);
    }
  }

  public function testBoundaryCanBeRetrieved()
  {
    /* -- RFC 2046, 5.1.1.
     boundary := 0*69<bchars> bcharsnospace

     bchars := bcharsnospace / " "

     bcharsnospace := DIGIT / ALPHA / "'" / "(" / ")" /
                      "+" / "_" / "," / "-" / "." /
                      "/" / ":" / "=" / "?"
    */

    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $headers1 = array($h1);

    $entity1 = $this->_createMessage($headers1, $this->_encoder, $this->_cache);

    $h2 = new Swift_Mime_MockHeader();
    $h2->setReturnValue('getFieldName', 'Content-Type');
    $headers2 = array($h2);

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getHeaders', $headers2);
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );

    $entity1->setChildren(array($entity2));

    $this->assertPattern(
      '/^[a-zA-Z0-9\'\(\)\+_\-,\.\/:=\?\ ]{0,69}[a-zA-Z0-9\'\(\)\+_\-,\.\/:=\?]$/D',
      $entity1->getBoundary()
      );
  }

  public function testBoundaryNeverChanges()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $headers1 = array($h1);

    $entity1 = $this->_createMessage($headers1, $this->_encoder, $this->_cache);

    $h2 = new Swift_Mime_MockHeader();
    $h2->setReturnValue('getFieldName', 'Content-Type');
    $headers2 = array($h2);

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getHeaders', $headers2);
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );

    $entity1->setChildren(array($entity2));

    $boundary = $entity1->getBoundary();
    for ($i = 0; $i < 10; $i++)
    {
      $this->assertEqual($boundary, $entity1->getBoundary());
    }
  }

  public function testBoundaryCanBeManuallySet()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $headers1 = array($h1);

    $entity1 = $this->_createMessage($headers1, $this->_encoder, $this->_cache);

    $h2 = new Swift_Mime_MockHeader();
    $h2->setReturnValue('getFieldName', 'Content-Type');
    $headers2 = array($h2);

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getHeaders', $headers2);
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );

    $entity1->setBoundary('my_boundary');

    $entity1->setChildren(array($entity2));

    $this->assertEqual('my_boundary', $entity1->getBoundary());
  }

  public function testChildrenAppearInString()
  {
    /* -- RFC 2046, 5.1.1.
     (excerpt too verbose to paste here)
     */

    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $h1->setReturnValue('getFieldBody', 'multipart/alternative;' . "\r\n" .
      ' boundary="_=_foo_=_"'
      );
    $h1->setReturnValue('toString',
      'Content-Type: multipart/alternative;' . "\r\n" .
      ' boundary="_=_foo_=_"' . "\r\n"
      );
    $headers1 = array($h1);

    $this->_cache->setReturnValue('getString',
      'Content-Type: multipart/alternative;' . "\r\n" .
      ' boundary="_=_foo_=_"' . "\r\n",
      array('*', 'headers')
      );

    $entity1 = $this->_createMessage($headers1, $this->_encoder, $this->_cache);
    $entity1->setBoundary('_=_foo_=_');

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_SUBPART
      );
    $entity2->setReturnValue('toString',
      'Content-Type: text/plain' . "\r\n" .
      "\r\n" .
      'foobar test'
      );

    $entity3 = new Swift_Mime_MockMimeEntity();
    $entity3->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_SUBPART
      );
    $entity3->setReturnValue('toString',
      'Content-Type: text/html' . "\r\n" .
      "\r\n" .
      'foobar <strong>test</strong>'
      );

    $entity1->setChildren(array($entity2, $entity3));

    $this->assertEqual(
      'Content-Type: multipart/alternative;' . "\r\n" .
      ' boundary="_=_foo_=_"' . "\r\n" .
      "\r\n" .
      '--_=_foo_=_' . "\r\n" .
      'Content-Type: text/plain' . "\r\n" .
      "\r\n" .
      'foobar test' . "\r\n" .
      '--_=_foo_=_' . "\r\n" .
      'Content-Type: text/html' . "\r\n" .
      "\r\n" .
      'foobar <strong>test</strong>' . "\r\n" .
      '--_=_foo_=_--' . "\r\n"
      ,
      $entity1->toString()
      );
  }

  public function testMixingLevelsIsHierarchical()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $h1->setReturnValue('getFieldBody', 'multipart/mixed;' . "\r\n" .
      ' boundary="_=_foo_=_"'
      );
    $h1->setReturnValue('toString',
      'Content-Type: multipart/mixed;' . "\r\n" .
      ' boundary="_=_foo_=_"' . "\r\n"
      );
    $headers = array($h1);

    $this->_cache->setReturnValueAt(0, 'getString',
      'Content-Type: multipart/mixed;' . "\r\n" .
      ' boundary="_=_foo_=_"' . "\r\n",
      array('*', 'headers')
      );
    $this->_cache->setReturnValueAt(1, 'getString',
      'Content-Type: multipart/alternative;' . "\r\n" .
      ' boundary="_=_bar_=_"' . "\r\n",
      array('*', 'headers')
      );

    $entity1 = $this->_createMessage($headers, $this->_encoder, $this->_cache);
    $entity1->setBoundary('_=_foo_=_');

    //Create some entities which nest differently
    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );
    $entity2->setReturnValue('toString',
      'Content-Type: application/octet-stream' . "\r\n" .
      "\r\n" .
      'foo'
      );

    $entity3 = new Swift_Mime_MockMimeEntity();
    $entity3->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_SUBPART
      );
    $entity3->setReturnValue('toString',
      'Content-Type: text/plain' . "\r\n" .
      "\r\n" .
      'xyz'
      );

    $entity1->setChildren(array($entity2, $entity3));

    $stringEntity = $entity1->toString();

    $this->assertPattern(
      '~^' .
      'Content-Type: multipart/mixed;' . "\r\n" .
      ' boundary="_=_foo_=_"' . "\r\n" .
      "\r\n" .
      '--_=_foo_=_' . "\r\n" .
      'Content-Type: multipart/alternative;' . "\r\n" .
      ' boundary="_=_bar_=_"' . "\r\n" .
      "\r\n" .
      '--(.*?)' . "\r\n" .
      'Content-Type: text/plain' . "\r\n" .
      "\r\n" .
      'xyz' . "\r\n" .
      '--\\1--' . "\r\n" .
      "\r\n" .
      '--_=_foo_=_' . "\r\n" .
      'Content-Type: application/octet-stream' . "\r\n" .
      "\r\n" .
      'foo' .
      "\r\n" .
      '--_=_foo_=_--' . "\r\n" .
      '$~D',
      $stringEntity
      );
  }

  public function testSettingEncoderNotifiesFieldChange()
  {
    $this->_encoder->setReturnValue('getName', 'quoted-printable');

    $h = new Swift_Mime_MockHeader();
    $h->setReturnValue('getFieldName', 'Content-Type');
    $headers = array($h);

    $encoder = new Swift_Mime_MockContentEncoder();
    $encoder->setReturnValue('getName', 'base64');

    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged',
      array('encoder', $encoder)
      );
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged',
      array('encoder', $encoder)
      );

    $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setEncoder($encoder);
  }

  public function testIdCanBeSet()
  {
    /* -- RFC 2045, 7.
    In constructing a high-level user agent, it may be desirable to allow
    one body to make reference to another.  Accordingly, bodies may be
    labelled using the "Content-ID" header field, which is syntactically
    identical to the "Message-ID" header field
    */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setId('foo@bar');
    $this->assertEqual('foo@bar', $message->getId());
  }

  public function testIdIsAutoGenerated()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $this->assertPattern('/^.*?@.*?$/D', $message->getId());
  }

  public function testIdDoesntChangeForSameEntity()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $id = $message->getId();
    for ($i = 0; $i < 10; ++$i)
    {
      $this->assertEqual($id, $message->getId());
    }
  }

  public function testIdsAreUniquePerEntity()
  {
    $lastid = null;
    for ($i = 0; $i < 10; ++$i)
    {
      $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
      $this->assertNotEqual($lastid, $message->getId());
      $lastid = $message->getId();
    }
  }

  public function testFieldChangeObserverIsNotifiedOfIdChange()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged',
      array('id', 'foo@bar')
      );
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged',
      array('id', 'foo@bar')
      );

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setId('foo@bar');
  }

  public function testDescriptionCanBeSet()
  {
    /* -- RFC 2045, 8.
    The ability to associate some descriptive information with a given
    body is often desirable.  For example, it may be useful to mark an
    "image" body as "a picture of the Space Shuttle Endeavor."  Such text
    may be placed in the Content-Description header field.  This header
    field is always optional.
    */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setDescription('my mime entity');
    $this->assertEqual('my mime entity', $message->getDescription());
  }

  public function testDescriptionNotifiesFieldChangeObserver()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged',
      array('description', 'my desc')
      );
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged',
      array('description', 'my desc')
      );

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setDescription('my desc');
  }

  public function testEncoderIsUsedForStringGeneration()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $h1->setReturnValue('getFieldBody', 'text/html');
    $h1->setReturnValue('toString', 'Content-Type: text/html' . "\r\n");
    $h2 = new Swift_Mime_MockHeader();
    $h2->setReturnValue('getFieldName', 'X-Header');
    $h2->setReturnValue('getFieldBody', 'foo');
    $h2->setReturnValue('toString', 'X-Header: foo' . "\r\n");
    $headers = array($h1, $h2);
    $this->_encoder->expectOnce('encodeString', array('my body', '*', '*'));
    $this->_encoder->setReturnValue('encodeString', 'my body');
    $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);
    $message->setBodyAsString('my body');
    $message->toString();
  }

  public function testMaxLineLengthIsProvidedForEncoding()
  {
    $h1 = new Swift_Mime_MockHeader();
    $h1->setReturnValue('getFieldName', 'Content-Type');
    $h1->setReturnValue('toString', 'Content-Type: text/html' . "\r\n");
    $h2 = new Swift_Mime_MockHeader();
    $h2->setReturnValue('getFieldName', 'X-Header');
    $h2->setReturnValue('toString', 'X-Header: foo' . "\r\n");
    $headers = array($h1, $h2);

    $this->_encoder->expectOnce('encodeString', array('my body', 0, 78));
    $this->_encoder->setReturnValue('encodeString', 'my body');

    $message = $this->_createMessage($headers, $this->_encoder, $this->_cache);
    $message->setMaxLineLength(78);
    $message->setBodyAsString('my body');

    $message->toString();
  }

  public function testNestingLevelIsTop()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $this->assertEqual(
      Swift_Mime_MimeEntity::LEVEL_TOP, $message->getNestingLevel()
      );
  }

  public function testCharsetCanBeSetAndFetched()
  {
    /* -- RFC 2046, 4.1.2.
    A critical parameter that may be specified in the Content-Type field
    for "text/plain" data is the character set.  This is specified with a
    "charset" parameter, as in:

     Content-type: text/plain; charset=iso-8859-1

    Unlike some other parameter values, the values of the charset
    parameter are NOT case sensitive.  The default character set, which
    must be assumed in the absence of a charset parameter, is US-ASCII.
    */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setCharset('ucs2');
    $this->assertEqual('ucs2', $message->getCharset());
  }

  public function testSettingCharsetNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('charset', 'utf-8'));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('charset', 'utf-8'));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setCharset('utf-8');
  }

  public function testFormatCanBeSetAndFetched()
  {
    /* -- RFC 3676.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setFormat('flowed'); //'fixed' is valid too
    $this->assertEqual('flowed', $message->getFormat());
  }

  public function testSettingFormatNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('format', 'fixed'));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('format', 'fixed'));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setFormat('fixed');
  }

  public function testDelSpCanBeSetAndFetched()
  {
    /* -- RFC 3676.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setDelSp(true); //false is valid too
    $this->assertTrue($message->getDelSp());
  }

  public function testSettingDelSpNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('delsp', true));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('delsp', true));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setDelSp(true);
  }

  public function testSubjectCanBeSetAndFetched()
  {
    /* -- RFC 2822, 3.6.5.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setSubject('some subject');
    $this->assertEqual('some subject', $message->getSubject());
  }

  public function testSettingSubjectNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('subject', 'test'));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('subject', 'test'));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setSubject('test');
  }

  public function testDateCanBeSetAndFetched()
  {
    /* -- RFC 2822, 3.6.1.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setDate(123456);
    $this->assertEqual(123456, $message->getDate());
  }

  public function testSettingDateNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('date', 123456));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('date', 123456));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setDate(123456);
  }

  public function testReturnPathCanBeSetAndFetched()
  {
    /* -- RFC 2822, 3.6.7.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setReturnPath('chris.corbyn@swiftmailer.org');
    $this->assertEqual('chris.corbyn@swiftmailer.org', $message->getReturnPath());
  }

  public function testSettingReturnPathNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('returnpath', 'chris@site'));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('returnpath', 'chris@site'));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setReturnPath('chris@site');
  }

  public function testSenderCanBeSetAndFetched()
  {
    /* -- RFC 2822, 3.6.2.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setSender('chris.corbyn@swiftmailer.org');
    $this->assertEqual(array('chris.corbyn@swiftmailer.org'=>null), $message->getSender());
  }

  public function testSettingSenderNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('sender', array('chris@site'=>null)));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('sender', array('chris@site'=>null)));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setSender('chris@site');
  }

  public function testFromCanBeSetAndFetched()
  {
    /* -- RFC 2822 3.6.2.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setFrom('chris.corbyn@swiftmailer.org');
    $this->assertEqual(array('chris.corbyn@swiftmailer.org'=>null),
      $message->getFrom()
      );
  }

  public function testSettingFromNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('from', array('chris@site'=>null)));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('from', array('chris@site'=>null)));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setFrom('chris@site');
  }

  public function testFromCanBeSetAsNameAddressList()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setFrom(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
    $this->assertEqual(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'),
      $message->getFrom()
      );
  }

  public function testSettingFromAsNameAddrNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('from', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('from', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setFrom(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
  }

  public function testReplyToCanBeSetAndFetched()
  {
    /* -- RFC 2822 3.6.2.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setReplyTo('chris.corbyn@swiftmailer.org');
    $this->assertEqual(array('chris.corbyn@swiftmailer.org'=>null),
      $message->getReplyTo()
      );
  }

  public function testSettingReplyToNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('replyto', array('chris@site'=>null)));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('replyto', array('chris@site'=>null)));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setReplyTo('chris@site');
  }

  public function testReplyToCanBeSetAsNameAddressList()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setReplyTo(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
    $this->assertEqual(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'),
      $message->getReplyTo()
      );
  }

  public function testSettingReplyToAsNameAddrNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('replyto', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('replyto', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setReplyTo(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
  }

  public function testToCanBeSetAndFetched()
  {
    /* -- RFC 2822 3.6.3.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setTo('chris.corbyn@swiftmailer.org');
    $this->assertEqual(array('chris.corbyn@swiftmailer.org'=>null),
      $message->getTo()
      );
  }

  public function testSettingToNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('to', array('chris@site'=>null)));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('to', array('chris@site'=>null)));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setTo('chris@site');
  }

  public function testToCanBeSetAsNameAddressList()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setTo(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
    $this->assertEqual(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'),
      $message->getTo()
      );
  }

  public function testSettingToAsNameAddrNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('to', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('to', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setTo(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
  }

  public function testCcCanBeSetAndFetched()
  {
    /* -- RFC 2822 3.6.3.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setCc('chris.corbyn@swiftmailer.org');
    $this->assertEqual(array('chris.corbyn@swiftmailer.org'=>null),
      $message->getCc()
      );
  }

  public function testSettingCcNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('cc', array('chris@site'=>null)));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('cc', array('chris@site'=>null)));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setCc('chris@site');
  }

  public function testCcCanBeSetAsNameAddressList()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setCc(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
    $this->assertEqual(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'),
      $message->getCc()
      );
  }

  public function testSettingCcAsNameAddrNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('cc', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('cc', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setCc(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
  }

  public function testBccCanBeSetAndFetched()
  {
    /* -- RFC 2822 3.6.3.
     */

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setBcc('chris.corbyn@swiftmailer.org');
    $this->assertEqual(array('chris.corbyn@swiftmailer.org'=>null),
      $message->getBcc()
      );
  }

  public function testSettingBccNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('bcc', array('chris@site'=>null)));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('bcc', array('chris@site'=>null)));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setBcc('chris@site');
  }

  public function testBccCanBeSetAsNameAddressList()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $message->setBcc(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
    $this->assertEqual(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'),
      $message->getBcc()
      );
  }

  public function testSettingBccAsNameAddrNotifiesFieldChangeObservers()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('bcc', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('bcc', array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      )));

    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $message->registerFieldChangeObserver($observer1);
    $message->registerFieldChangeObserver($observer2);

    $message->setBcc(array(
      'chris.corbyn@swiftmailer.org'=>'Chris',
      'john@site.tld' => 'John'
      ));
  }

  public function testChildrenCanBeAttached()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $entity1 = new Swift_Mime_MockMimeEntity();
    $entity1->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );

    $message->attach($entity1);

    $this->assertEqual(array($entity1), $message->getChildren(),
      '%s: Child should be appended'
      );

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_SUBPART
      );

    $message->attach($entity2);

    $this->assertEqual(array($entity1, $entity2), $message->getChildren(),
      '%s: Children should be incrementally appended'
      );
  }

  public function testChildrenCanBeDetached()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $entity1 = new Swift_Mime_MockMimeEntity();
    $entity1->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );
    $entity1->setReturnValue('getId', 'foo@bar');

    $message->attach($entity1);

    $this->assertEqual(array($entity1), $message->getChildren(),
      '%s: Child should be appended'
      );

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_SUBPART
      );
    $entity2->setReturnValue('getId', 'zip@button');

    $message->attach($entity2);

    $this->assertEqual(array($entity1, $entity2), $message->getChildren(),
      '%s: Children should be incrementally appended'
      );

    $message->detach($entity1);

    $this->assertEqual(array($entity2), $message->getChildren(),
      '%s: Child should be detached'
      );
  }

  public function testEmbedAttachesChild()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $entity1 = new Swift_Mime_MockMimeEntity();
    $entity1->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );
    $entity1->setReturnValue('getId', 'foo@bar');

    $message->embed($entity1);

    $this->assertEqual(array($entity1), $message->getChildren(),
      '%s: embed() should attach child'
      );

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_SUBPART
      );
    $entity2->setReturnValue('getId', 'zip@button');

    $message->embed($entity2);

    $this->assertEqual(array($entity1, $entity2), $message->getChildren(),
      '%s: Children should be incrementally appended'
      );
  }

  public function testEmbedReturnsValidCid()
  {
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);

    $entity1 = new Swift_Mime_MockMimeEntity();
    $entity1->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT
      );
    $entity1->setReturnValue('getId', 'foo@bar');

    $this->assertEqual('cid:foo@bar', $message->embed($entity1));

    $entity2 = new Swift_Mime_MockMimeEntity();
    $entity2->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_SUBPART
      );
    $entity2->setReturnValue('getId', 'zip@button');

    $this->assertEqual('cid:zip@button', $message->embed($entity2));
  }

  public function testFluidInterface()
  {
    $child = new Swift_Mime_MockMimeEntity();
    $child->setReturnValue('getNestingLevel',
      Swift_Mime_MimeEntity::LEVEL_SUBPART
      );
    $message = $this->_createMessage(array(), $this->_encoder, $this->_cache);
    $ref = $message
      ->setContentType('text/plain')
      ->setEncoder($this->_encoder)
      ->setId('foo@bar')
      ->setDescription('my description')
      ->setMaxLineLength(998)
      ->setBodyAsString('xx')
      ->setNestingLevel(10)
      ->setBoundary('xyz')
      ->setChildren(array())
      ->setHeaders(array())
      ->setCharset('iso-8859-1')
      ->setFormat('flowed')
      ->setDelSp(false)
      ->setSubject('subj')
      ->setDate(123)
      ->setReturnPath('foo@bar')
      ->setSender('foo@bar')
      ->setFrom(array('x@y' => 'XY'))
      ->setReplyTo(array('ab@cd' => 'ABCD'))
      ->setTo(array('chris@site.tld', 'mark@site.tld'))
      ->setCc('john@somewhere.tld')
      ->setBcc(array('one@site', 'two@site' => 'Two'))
      ->attach($child)
      ->detach($child)
      ;

    $this->assertReference($message, $ref);
  }

  // -- Private helpers

  private function _createMessage($headers, $encoder, $cache)
  {
    return new Swift_Mime_SimpleMessage($headers, $encoder, $cache);
  }

}