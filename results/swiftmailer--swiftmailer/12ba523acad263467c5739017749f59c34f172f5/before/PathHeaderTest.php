<?php

require_once 'Swift/Mime/Header/PathHeader.php';

class Swift_Mime_Header_PathHeaderTest extends UnitTestCase
{

  public function testSingleAddressCanBeSetAndFetched()
  {
    $header = $this->_getHeader('Return-Path');
    $header->setAddress('chris@swiftmailer.org');
    $this->assertEqual('chris@swiftmailer.org', $header->getAddress());
  }

  public function testAddressMustComplyWithRfc2822()
  {
    try
    {
      $header = $this->_getHeader('Return-Path');
      $header->setAddress('chr is@swiftmailer.org');
      $this->fail('Address must be valid according to RFC 2822 addr-spec grammar.');
    }
    catch (Exception $e)
    {
      $this->pass();
    }
  }

  public function testValueIsAngleAddrWithValidAddress()
  {
    /* -- RFC 2822, 3.6.7.
      return          =       "Return-Path:" path CRLF

      path            =       ([CFWS] "<" ([CFWS] / addr-spec) ">" [CFWS]) /
                              obs-path
     */

    $header = $this->_getHeader('Return-Path');
    $header->setAddress('chris@swiftmailer.org');
    $this->assertEqual('<chris@swiftmailer.org>', $header->getFieldBody());
  }

  public function testValueIsEmptyAngleBracketsIfEmptyAddressSet()
  {
    $header = $this->_getHeader('Return-Path');
    $header->setAddress('');
    $this->assertEqual('<>', $header->getFieldBody());
  }

  public function testToString()
  {
    $header = $this->_getHeader('Return-Path');
    $header->setAddress('chris@swiftmailer.org');
    $this->assertEqual('Return-Path: <chris@swiftmailer.org>' . "\r\n",
      $header->toString()
      );
  }

  public function testFieldChangeObserverCanSetReturnPath()
  {
    $header = $this->_getHeader('Return-Path');
    $header->fieldChanged('returnpath', 'chris@site');
    $this->assertEqual('chris@site', $header->getAddress());
  }

  public function testReturnPathFieldChangeIsIgnoredByOtherHeaders()
  {
    $header = $this->_getHeader('To');
    $header->setAddress('abc@def.tld');
    $header->fieldChanged('returnpath', 'testing@site.com');
    $this->assertEqual('abc@def.tld', $header->getAddress());
  }

  public function testOtherFieldChangesAreIgnoredForReturnPath()
  {
    $header = $this->_getHeader('Return-Path');
    $header->setAddress('testing@site.com');
    foreach (array('charset', 'comments', 'x-foo') as $field)
    {
      $header->fieldChanged($field, 'xxxxx');
      $this->assertEqual('testing@site.com', $header->getAddress());
    }
  }

  // -- Private methods

  private function _getHeader($name)
  {
    return new Swift_Mime_Header_PathHeader($name);
  }

}