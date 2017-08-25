<?php

require_once 'Swift/Mime/UnstructuredHeader.php';
require_once 'Swift/Mime/HeaderAttribute.php';
require_once 'Swift/Mime/HeaderAttributeSet.php';
require_once 'Swift/Mime/HeaderEncoder.php';

Mock::generate('Swift_Mime_HeaderAttribute', 'Swift_Mime_MockHeaderAttribute');
Mock::generate('Swift_Mime_HeaderAttributeSet',
  'Swift_Mime_MockHeaderAttributeSet'
  );
Mock::generate('Swift_Mime_HeaderEncoder', 'Swift_Mime_MockHeaderEncoder');

class Swift_Mime_UnstructuredHeaderTest extends UnitTestCase
{

  private $_charset = 'utf-8';

  public function testGetNameReturnsNameVerbatim()
  {
    $header = $this->_getHeader('Subject', 'Test');
    $this->assertEqual('Subject', $header->getName());
  }

  public function testGetValueReturnsValueVerbatim()
  {
    $header = $this->_getHeader('Subject', 'Test');
    $this->assertEqual('Test', $header->getValue());
  }

  public function testValueCanBeSet()
  {
    $header = $this->_getHeader('Subject', '');
    $header->setValue('Something');
    $this->assertEqual('Something', $header->getValue());
  }

  public function testAttributesCanBeSet()
  {
    $header = $this->_getHeader('Content-Type', 'text/html');

    $attributes = array();
    $charsetAttribute = new Swift_Mime_MockHeaderAttribute();
    $charsetAttribute->setReturnValue('getName', 'charset');
    $charsetAttribute->setReturnValue('getValue', 'utf-8');
    $attributes[] = $charsetAttribute;

    $attributeSet = new Swift_Mime_MockHeaderAttributeSet();
    $attributeSet->setReturnValue('toArray', $attributes);

    $header->setAttributes($attributeSet);

    $testAttributes = $header->getAttributes()->toArray();

    $this->assertEqual($attributes, $testAttributes);
  }

  public function testBasicStructureIsKeyValuePair()
  {
    /* -- RFC 2822, 2.2
    Header fields are lines composed of a field name, followed by a colon
    (":"), followed by a field body, and terminated by CRLF.
    */
    $header = $this->_getHeader('Subject', 'Test');
    $this->assertEqual('Subject: Test' . "\r\n", $header->toString());
  }

  public function testLongHeadersAreFoldedAtWordBoundary()
  {
    /* -- RFC 2822, 2.2.3
    Each header field is logically a single line of characters comprising
    the field name, the colon, and the field body.  For convenience
    however, and to deal with the 998/78 character limitations per line,
    the field body portion of a header field can be split into a multiple
    line representation; this is called "folding".  The general rule is
    that wherever this standard allows for folding white space (not
    simply WSP characters), a CRLF may be inserted before any WSP.
    */

    $value = 'The quick brown fox jumped over the fence, he was a very very ' .
      'scary brown fox with a bushy tail';
    $header = $this->_getHeader('X-Custom-Header', $value);
    $header->setMaxLineLength(78); //A safe [RFC 2822, 2.2.3] default
    /*
    X-Custom-Header: The quick brown fox jumped over the fence, he was a very very
     scary brown fox with a bushy tail
    */
    $this->assertEqual(
      'X-Custom-Header: The quick brown fox jumped over the fence, he was a' .
      ' very very' . "\r\n" . //Folding
      ' scary brown fox with a bushy tail' . "\r\n",
      $header->toString(), '%s: The header should have been folded at 78th char'
      );
  }

  public function testAttributesAreAppended()
  {
    $attributes = array();
    $att1 = new Swift_Mime_MockHeaderAttribute();
    $att1->setReturnValue('toString', 'charset=utf-8');
    $attributes[] = $att1;
    $att2 = new Swift_Mime_MockHeaderAttribute();
    $att2->setReturnValue('toString', 'lang=en');
    $attributes[] = $att2;

    $attSet = new Swift_Mime_MockHeaderAttributeSet();
    $attSet->setReturnValue('toArray', $attributes);

    $header = $this->_getHeader('Content-Type', 'text/plain');
    $header->setAttributes($attSet);

    $this->assertEqual(
      'Content-Type: text/plain; charset=utf-8; lang=en' . "\r\n",
      $header->toString()
      );
  }

  public function testAttributesAreFoldedInLongHeaders()
  {
    $attributes = array();
    $att1 = new Swift_Mime_MockHeaderAttribute();
    $att1->setReturnValue('toString',
      'attrib*0="first line of attrib";' . "\r\n" .
      'attrib*1="second line of attrib"'
      );
    $attributes[] = $att1;
    $att2 = new Swift_Mime_MockHeaderAttribute();
    $att2->setReturnValue('toString', 'test=nothing');
    $attributes[] = $att2;

    $attSet = new Swift_Mime_MockHeaderAttributeSet();
    $attSet->setReturnValue('toArray', $attributes);

    $header = $this->_getHeader('X-Anything-Header',
      'something with a fairly long value'
      );
    $header->setAttributes($attSet);
    $header->setMaxLineLength(78);

    $this->assertEqual(
      'X-Anything-Header: something with a fairly long value;' . "\r\n" .
      ' attrib*0="first line of attrib";' . "\r\n" .
      ' attrib*1="second line of attrib"; test=nothing' . "\r\n",
      $header->toString()
      );
  }

  public function testPrintableAsciiOnlyAppearsInHeaders()
  {
    /* -- RFC 2822, 2.2.
    A field name MUST be composed of printable US-ASCII characters (i.e.,
    characters that have values between 33 and 126, inclusive), except
    colon.  A field body may be composed of any US-ASCII characters,
    except for CR and LF.
    */

    $nonAsciiChar = pack('C', 0x8F);
    $header = $this->_getHeader('X-Test', $nonAsciiChar);
    $this->assertPattern(
      '~^[^:\x00-\x20\x80-\xFF]+: [^\x80-\xFF\r\n]+\r\n$~s',
      $header->toString()
      );
  }

  public function testEncodedWordsFollowGeneralStructure()
  {
    /* -- RFC 2047, 1.
    Generally, an "encoded-word" is a sequence of printable ASCII
    characters that begins with "=?", ends with "?=", and has two "?"s in
    between.
    */

    $nonAsciiChar = pack('C', 0x8F);
    $header = $this->_getHeader('X-Test', $nonAsciiChar);
    $this->assertPattern(
      '~^X-Test: \=?.*?\?.*?\?.*?\?=\r\n$~s',
      $header->toString()
      );
  }

  public function testEncodedWordIncludesCharsetAndEncodingMethodAndText()
  {
    /* -- RFC 2047, 2.
    An 'encoded-word' is defined by the following ABNF grammar.  The
    notation of RFC 822 is used, with the exception that white space
    characters MUST NOT appear between components of an 'encoded-word'.

    encoded-word = "=?" charset "?" encoding "?" encoded-text "?="
    */

    $nonAsciiChar = pack('C', 0x8F);

    $encoder = new Swift_Mime_MockHeaderEncoder();
    $encoder->expectOnce('encodeString', array($nonAsciiChar, '*', '*'),
      'Encoding should be invoked');
    $encoder->setReturnValue('encodeString', '=8F');
    $encoder->setReturnValue('getName', 'Q');
    $header = $this->_getHeader('X-Test', $nonAsciiChar, $encoder);
    $this->assertEqual(
      'X-Test: =?' . $this->_charset . '?Q?=8F?=' . "\r\n",
      $header->toString()
      );
  }

  public function testEncodedWordsAreUsedToEncodedNonPrintableAscii()
  {
    //SPACE and TAB permitted
    $nonPrintableBytes = array_merge(
      range(0x00, 0x08), range(0x10, 0x19), array(0x7F)
      );

    foreach ($nonPrintableBytes as $byte)
    {
      $char = pack('C', $byte);
      $encodedChar = sprintf('=%02X', $byte);

      $encoder = new Swift_Mime_MockHeaderEncoder();
      $encoder->expectOnce('encodeString', array($char, '*', '*'),
        'Encoding should be invoked.'
        );
      $encoder->setReturnValue('encodeString', $encodedChar);
      $encoder->setReturnValue('getName', 'Q');

      $header = $this->_getHeader('X-A', $char, $encoder);

      $this->assertEqual(
        'X-A: =?' . $this->_charset . '?Q?' . $encodedChar . '?=' . "\r\n",
        $header->toString(), '%s: Non-printable ascii should be encoded'
        );
    }
  }

  public function testEncodedWordsAreUsedToEncode8BitOctets()
  {
    $_8BitBytes = range(0x80, 0xFF);

    foreach ($_8BitBytes as $byte)
    {
      $char = pack('C', $byte);
      $encodedChar = sprintf('=%02X', $byte);

      $encoder = new Swift_Mime_MockHeaderEncoder();
      $encoder->expectOnce('encodeString', array($char, '*', '*'),
        'Encoding should be invoked.'
        );
      $encoder->setReturnValue('encodeString', $encodedChar);
      $encoder->setReturnValue('getName', 'Q');

      $header = $this->_getHeader('X-A', $char, $encoder);

      $this->assertEqual(
        'X-A: =?' . $this->_charset . '?Q?' . $encodedChar . '?=' . "\r\n",
        $header->toString(), '%s: 8-bit octets should be encoded'
        );
    }
  }

  public function testEncodedWordsAreNoMoreThan75CharsPerLine()
  {
    /* -- RFC 2047, 2.
    An 'encoded-word' may not be more than 75 characters long, including
    'charset', 'encoding', 'encoded-text', and delimiters.

    ... SNIP ...

    While there is no limit to the length of a multiple-line header
    field, each line of a header field that contains one or more
    'encoded-word's is limited to 76 characters.
    */

    $nonAsciiChar = pack('C', 0x8F);

    $encoder = new Swift_Mime_MockHeaderEncoder();
    $encoder->expectOnce('encodeString', array($nonAsciiChar, 20, 75),
      '%s: Parameters for $firstLineOffset and $maxLineLength should be 20 ' .
      'and 75 respectively');
    //Note that multi-line headers begin with LWSP which makes 75 + 1 = 76
    $encoder->setReturnValue('encodeString', '=8F');
    $encoder->setReturnValue('getName', 'Q');

    //* X-Test: =?utf-8?Q??= is 20 chars
    $header = $this->_getHeader('X-Test', $nonAsciiChar, $encoder);

    $this->assertEqual(
      'X-Test: =?' . $this->_charset . '?Q?=8F?=' . "\r\n",
      $header->toString()
      );
  }

  public function testFWSPIsUsedWhenEncoderReturnsMultipleLines()
  {
    /* --RFC 2047, 2.
    If it is desirable to encode more text than will fit in an 'encoded-word' of
    75 characters, multiple 'encoded-word's (separated by CRLF SPACE) may
    be used.
    */

    //Note the Mock does NOT return 8F encoded, the 8F merely triggers encoding
    $nonAsciiChar = pack('C', 0x8F);

    $encoder = new Swift_Mime_MockHeaderEncoder();
    $encoder->expectOnce('encodeString', array($nonAsciiChar, 20, 75),
      'Encoding should be invoked');
    //Note that multi-line headers begin with LWSP which makes 75 + 1 = 76
    $encoder->setReturnValue('encodeString',
      'line_one_here' . "\r\n" . 'line_two_here'
      );
    $encoder->setReturnValue('getName', 'Q');

    //* X-Test: =?utf-8?Q??= is 20 chars
    $header = $this->_getHeader('X-Test', $nonAsciiChar, $encoder);

    $this->assertEqual(
      'X-Test: =?' . $this->_charset . '?Q?line_one_here?=' . "\r\n" .
      ' =?' . $this->_charset . '?Q?line_two_here?=' . "\r\n",
      $header->toString()
      );
  }

  // -- Private methods

  private function _getHeader($name, $value, $encoder = null)
  {
    if (!$encoder)
    {
      $encoder = new Swift_Mime_MockHeaderEncoder();
    }
    return new Swift_Mime_UnstructuredHeader(
      $name, $value, $this->_charset, $encoder
      );
  }

}