<?php

require_once 'Swift/Mime/MimeEntity.php';
require_once 'Swift/Mime/Attachment.php';
require_once 'Swift/Tests/SwiftUnitTestCase.php';
require_once 'Swift/Mime/ContentEncoder.php';
require_once 'Swift/Mime/Header.php';
require_once 'Swift/Mime/FieldChangeObserver.php';
require_once 'Swift/FileStream.php';
require_once 'Swift/KeyCache.php';

Mock::generate('Swift_Mime_ContentEncoder', 'Swift_Mime_MockContentEncoder');
Mock::generate('Swift_Mime_Header', 'Swift_Mime_MockHeader');
Mock::generate('Swift_Mime_FieldChangeObserver',
  'Swift_Mime_MockFieldChangeObserver'
  );
Mock::generate('Swift_FileStream', 'Swift_MockFileStream');
Mock::generate('Swift_KeyCache', 'Swift_MockKeyCache');

class Swift_Mime_AttachmentTest extends Swift_Tests_SwiftUnitTestCase
{
  private $_encoder;
  private $_cache;

  public function setUp()
  {
    $this->_cache = new Swift_MockKeyCache();
    $this->_encoder = new Swift_Mime_MockContentEncoder();
    $this->_encoder->setReturnValue('getName', 'base64');
  }

  public function testNestingLevelIsAttachment()
  {
    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $this->assertEqual(
      Swift_Mime_MimeEntity::LEVEL_ATTACHMENT, $attachment->getNestingLevel()
      );
  }

  public function testDispositionCanBeSetAndFetched()
  {
    /* -- RFC 2183, 2.1, 2.2.
     */

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $attachment->setDisposition('inline');
    $this->assertEqual('inline', $attachment->getDisposition());
  }

  public function testSettingDispositionNotifiesFieldChangeObserver()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('disposition', 'attachment'));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('disposition', 'attachment'));

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);

    $attachment->registerFieldChangeObserver($observer1);
    $attachment->registerFieldChangeObserver($observer2);

    $attachment->setDisposition('attachment');
  }

  public function testDefaultDispositionIsAttachment()
  {
    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $this->assertEqual('attachment', $attachment->getDisposition());
  }

  public function testFilenameCanBeSetAndFetched()
  {
    /* -- RFC 2183, 2.3.
     */

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $attachment->setFilename('some-file.pdf');
    $this->assertEqual('some-file.pdf', $attachment->getFilename());
  }

  public function testSettingFilenameNotifiesFieldChangeObserver()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('filename', 'foo.bar'));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('filename', 'foo.bar'));

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);

    $attachment->registerFieldChangeObserver($observer1);
    $attachment->registerFieldChangeObserver($observer2);

    $attachment->setFilename('foo.bar');
  }

  public function testCreationDateCanBeSetAndFetched()
  {
    /* -- RFC 2183, 2.4.
     */

    $date = time();
    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $attachment->setCreationDate($date);
    $this->assertEqual($date, $attachment->getCreationDate());
  }

  public function testSettingCreationDateNotifiesFieldChangeObserver()
  {
    $date = time();

    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('creationdate', $date));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('creationdate', $date));

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);

    $attachment->registerFieldChangeObserver($observer1);
    $attachment->registerFieldChangeObserver($observer2);

    $attachment->setCreationDate($date);
  }

  public function testModificationDateCanBeSetAndFetched()
  {
    /* -- RFC 2183, 2.5.
     */

    $date = time();
    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $attachment->setModificationDate($date);
    $this->assertEqual($date, $attachment->getModificationDate());
  }

  public function testSettingModificationDateNotifiesFieldChangeObserver()
  {
    $date = time();

    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('modificationdate', $date));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('modificationdate', $date));

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);

    $attachment->registerFieldChangeObserver($observer1);
    $attachment->registerFieldChangeObserver($observer2);

    $attachment->setModificationDate($date);
  }

  public function testReadDateCanBeSetAndFetched()
  {
    /* -- RFC 2183, 2.6.
     */

    $date = time();
    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $attachment->setReadDate($date);
    $this->assertEqual($date, $attachment->getReadDate());
  }

  public function testSettingReadDateNotifiesFieldChangeObserver()
  {
    $date = time();

    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('readdate', $date));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('readdate', $date));

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);

    $attachment->registerFieldChangeObserver($observer1);
    $attachment->registerFieldChangeObserver($observer2);

    $attachment->setReadDate($date);
  }

  public function testSizeCanBeSetAndFetched()
  {
    /* -- RFC 2183, 2.7.
     */

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $attachment->setSize(123456);
    $this->assertEqual(123456, $attachment->getSize());
  }

  public function testSettingSizeNotifiesFieldChangeObserver()
  {
    $observer1 = new Swift_Mime_MockFieldChangeObserver();
    $observer1->expectOnce('fieldChanged', array('size', 123456));
    $observer2 = new Swift_Mime_MockFieldChangeObserver();
    $observer2->expectOnce('fieldChanged', array('size', 123456));

    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);

    $attachment->registerFieldChangeObserver($observer1);
    $attachment->registerFieldChangeObserver($observer2);

    $attachment->setSize(123456);
  }

  public function testFilnameCanBeReadFromFileStream()
  {
    $file = new Swift_MockFileStream();
    $file->setReturnValue('getPath', '/path/to/some-image.jpg');
    $file->setReturnValueAt(0, 'read', '<image data>');
    $file->setReturnValueAt(1, 'read', false);

    $entity = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $entity->setFile($file);
    $this->assertEqual('some-image.jpg', $entity->getFilename());
    $this->assertEqual('<image data>', $entity->getBodyAsString());
  }

  public function testFluidInterface()
  {
    $attachment = $this->_createAttachment(array(), $this->_encoder, $this->_cache);
    $ref = $attachment
      ->setContentType('application/pdf')
      ->setEncoder($this->_encoder)
      ->setId('foo@bar')
      ->setDescription('my pdf')
      ->setMaxLineLength(998)
      ->setBodyAsString('xx')
      ->setNestingLevel(10)
      ->setBoundary('xyz')
      ->setChildren(array())
      ->setHeaders(array())
      ->setDisposition('inline')
      ->setFilename('afile.txt')
      ->setCreationDate(time())
      ->setModificationDate(time() + 10)
      ->setReadDate(time() + 20)
      ->setSize(123)
      ->setFile(new Swift_MockFileStream())
      ;

    $this->assertReference($attachment, $ref);
  }

  // -- Private helpers

  private function _createAttachment($headers, $encoder, $cache)
  {
    return new Swift_Mime_Attachment($headers, $encoder, $cache);
  }

}