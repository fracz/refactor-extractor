<?php

require_once 'Swift/Tests/SwiftUnitTestCase.php';
require_once 'Swift/Mime/Header/UnstructuredHeader.php';
require_once 'Swift/Mime/HeaderEncoder.php';
require_once 'Swift/Mime/ContentEncoder.php';

Mock::generate('Swift_Mime_HeaderEncoder', 'Swift_Mime_MockHeaderEncoder');
Mock::generate('Swift_Mime_ContentEncoder', 'Swift_Mime_MockContentEncoder');

class Swift_Mime_Header_UnstructuredHeaderTest extends Swift_Tests_SwiftUnitTestCase
{

  private $_charset = 'utf-8';

  public function testGetNameReturnsNameVerbatim()
  {
    $header = $this->_getHeader('Subject', new Swift_Mime_MockHeaderEncoder());
    $this->assertEqual('Subject', $header->getFieldName());
  }

  public function testGetValueReturnsValueVerbatim()
  {
    $header = $this->_getHeader('Subject', new Swift_Mime_MockHeaderEncoder());
    $header->setValue('Test');
    $this->assertEqual('Test', $header->getValue());
  }

  public function testBasicStructureIsKeyValuePair()
  {
    /* -- RFC 2822, 2.2
    Header fields are lines composed of a field name, followed by a colon
    (":"), followed by a field body, and terminated by CRLF.
    */
    $header = $this->_getHeader('Subject', new Swift_Mime_MockHeaderEncoder());
    $header->setValue('Test');
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
    $header = $this->_getHeader('X-Custom-Header',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->setValue($value);
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

  public function testPrintableAsciiOnlyAppearsInHeaders()
  {
    /* -- RFC 2822, 2.2.
    A field name MUST be composed of printable US-ASCII characters (i.e.,
    characters that have values between 33 and 126, inclusive), except
    colon.  A field body may be composed of any US-ASCII characters,
    except for CR and LF.
    */

    $nonAsciiChar = pack('C', 0x8F);
    $header = $this->_getHeader('X-Test', new Swift_Mime_MockHeaderEncoder());
    $header->setValue($nonAsciiChar);
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
    $header = $this->_getHeader('X-Test', new Swift_Mime_MockHeaderEncoder());
    $header->setValue($nonAsciiChar);
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
    $encoder->expectOnce('encodeString', array(
      new Swift_Tests_IdenticalBinaryExpectation($nonAsciiChar), '*', '*'
      ));
    $encoder->setReturnValue('encodeString', '=8F');
    $encoder->setReturnValue('getName', 'Q');
    $header = $this->_getHeader('X-Test', $encoder);
    $header->setValue($nonAsciiChar);
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
      $encoder->expectOnce('encodeString', array(
        new Swift_Tests_IdenticalBinaryExpectation($char), '*', '*'
        ));
      $encoder->setReturnValue('encodeString', $encodedChar);
      $encoder->setReturnValue('getName', 'Q');

      $header = $this->_getHeader('X-A', $encoder);
      $header->setValue($char);

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
      $encoder->expectOnce('encodeString', array(
        new Swift_Tests_IdenticalBinaryExpectation($char), '*', '*'
        ));
      $encoder->setReturnValue('encodeString', $encodedChar);
      $encoder->setReturnValue('getName', 'Q');

      $header = $this->_getHeader('X-A', $encoder);
      $header->setValue($char);

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
    $encoder->expectOnce('encodeString', array(
      new Swift_Tests_IdenticalBinaryExpectation($nonAsciiChar), 8, 63),
      '%s: Parameters for $firstLineOffset and $maxLineLength should be 20 ' .
      'and 63 respectively');
    //Note that multi-line headers begin with LWSP which makes 75 + 1 = 76
    //Note also that =?utf-8?q??= is 12 chars which makes 75 - 12 = 63
    $encoder->setReturnValue('encodeString', '=8F');
    $encoder->setReturnValue('getName', 'Q');

    //* X-Test: is 8 chars
    $header = $this->_getHeader('X-Test', $encoder);
    $header->setValue($nonAsciiChar);

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

    //Note the Mock does NOT return 8F encoded, the 8F merely triggers
    // encoding for the sake of testing
    $nonAsciiChar = pack('C', 0x8F);

    $encoder = new Swift_Mime_MockHeaderEncoder();
    $encoder->expectOnce('encodeString', array(
      new Swift_Tests_IdenticalBinaryExpectation($nonAsciiChar), 8, 63)
      );
    //Note that multi-line headers begin with LWSP which makes 75 + 1 = 76
    //Note also that =?utf-8?q??= is 12 chars which makes 75 - 12 = 63
    $encoder->setReturnValue('encodeString',
      'line_one_here' . "\r\n" . 'line_two_here'
      );
    $encoder->setReturnValue('getName', 'Q');

    //* X-Test: is 8 chars
    $header = $this->_getHeader('X-Test', $encoder);
    $header->setValue($nonAsciiChar);

    $this->assertEqual(
      'X-Test: =?' . $this->_charset . '?Q?line_one_here?=' . "\r\n" .
      ' =?' . $this->_charset . '?Q?line_two_here?=' . "\r\n",
      $header->toString()
      );
  }

  public function testAdjacentWordsAreEncodedTogether()
  {
    /* -- RFC 2047, 5 (1)
     Ordinary ASCII text and 'encoded-word's may appear together in the
     same header field.  However, an 'encoded-word' that appears in a
     header field defined as '*text' MUST be separated from any adjacent
     'encoded-word' or 'text' by 'linear-white-space'.

     -- RFC 2047, 2.
     IMPORTANT: 'encoded-word's are designed to be recognized as 'atom's
     by an RFC 822 parser.  As a consequence, unencoded white space
     characters (such as SPACE and HTAB) are FORBIDDEN within an
     'encoded-word'.
     */

    //It would be valid to encode all words needed, however it's probably
    // easiest to encode the longest amount required at a time

    $word = 'w' . pack('C', 0x8F) . 'rd';
    $text = 'start ' . $word . ' ' . $word . ' then end ' . $word;
    // 'start', ' word word', ' and end', ' word'

    $encoder = new Swift_Mime_MockHeaderEncoder();
    $encoder->setReturnValue('getName', 'Q');
    $encoder->expectCallCount('encodeString', 2);
    $encoder->expectAt(0, 'encodeString', array(
      new Swift_Tests_IdenticalBinaryExpectation($word . ' ' . $word), '*', '*'),
      '%s: Adjacent words to be encoded should be encoded together with any WSP'
      );
    $encoder->setReturnValueAt(0, 'encodeString', 'w=8Frd_w=8Frd');
    $encoder->expectAt(1, 'encodeString', array(
      new Swift_Tests_IdenticalBinaryExpectation($word), '*', '*'),
      '%s: Full words should be encoded'
      );
    $encoder->setReturnValueAt(1, 'encodeString', 'w=8Frd');

    $header = $this->_getHeader('X-Test', $encoder);
    $header->setValue($text);

    $headerString = $header->toString();

    $this->assertEqual('X-Test: start =?' . $this->_charset . '?Q?' .
      'w=8Frd_w=8Frd?= then end =?' . $this->_charset . '?Q?'.
      'w=8Frd?=' . "\r\n", $headerString,
      '%s: Adjacent encoded words should appear grouped with WSP encoded'
      );
  }

  public function testLanguageInformationAppearsInEncodedWords()
  {
    /* -- RFC 2231, 5.
    5.  Language specification in Encoded Words

    RFC 2047 provides support for non-US-ASCII character sets in RFC 822
    message header comments, phrases, and any unstructured text field.
    This is done by defining an encoded word construct which can appear
    in any of these places.  Given that these are fields intended for
    display, it is sometimes necessary to associate language information
    with encoded words as well as just the character set.  This
    specification extends the definition of an encoded word to allow the
    inclusion of such information.  This is simply done by suffixing the
    character set specification with an asterisk followed by the language
    tag.  For example:

          From: =?US-ASCII*EN?Q?Keith_Moore?= <moore@cs.utk.edu>
    */

    $value = 'fo' . pack('C', 0x8F) .'bar';

    $encoder = new Swift_Mime_MockHeaderEncoder();
    $encoder->setReturnValue('getName', 'Q');
    $encoder->expectOnce('encodeString', array($value, '*', '*'));
    $encoder->setReturnValue('encodeString', 'fo=8Fbar');

    $header = $this->_getHeader('Subject', $encoder);
    $header->setLanguage('en');
    $header->setValue($value);
    $this->assertEqual("Subject: =?utf-8*en?Q?fo=8Fbar?=\r\n",
      $header->toString()
      );
  }

  // --- THESE TESTS ARE IMPLEMENTATION SPECIFIC FOR COMPATIBILITY WITH --
  // --- SimpleMimeEntity ---

  public function testFieldChangeObserverCanSetContentTransferEncoding()
  {
    $header = $this->_getHeader('Content-Transfer-Encoding',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->setValue('7bit');
    $encoder = new Swift_Mime_MockContentEncoder();
    $encoder->setReturnValue('getName', 'quoted-printable');
    $header->fieldChanged('encoder', $encoder);
    $this->assertEqual('quoted-printable', $header->getValue());
  }

  public function testTransferEncodingFieldChangeIsIgnoredByOtherHeaders()
  {
    $header = $this->_getHeader('Subject',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->setValue('My subject');
    $encoder = new Swift_Mime_MockContentEncoder();
    $encoder->setReturnValue('getName', 'quoted-printable');
    $header->fieldChanged('encoding', $encoder);
    $this->assertEqual('My subject', $header->getValue());
  }

  public function testOtherFieldChangesAreIgnoredForTransferEncoding()
  {
    $header = $this->_getHeader('Content-Transfer-Encoding',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->setValue('7bit');
    foreach (array('subject', 'comments', 'x-foo') as $field)
    {
      $header->fieldChanged($field, 'xxxxx');
      $this->assertEqual('7bit', $header->getValue());
    }
  }

  public function testObserverInterfaceChangesContentDescription()
  {
    $header = $this->_getHeader('Content-Description',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->fieldChanged('description', 'testing');
    $this->assertEqual('testing', $header->getValue());
  }

  public function testDescriptionFieldChangeIsIgnoredByOtherHeaders()
  {
    $header = $this->_getHeader('Subject',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->setValue('subj');
    $header->fieldChanged('description', 'testing');
    $this->assertEqual('subj', $header->getValue());
  }

  public function testOtherFieldChangesAreIgnoredForContentDescription()
  {
    $header = $this->_getHeader('Content-Description',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->setValue('testing');
    foreach (array('subject', 'comments', 'x-foo') as $field)
    {
      $header->fieldChanged($field, 'xxxxx');
      $this->assertEqual('testing', $header->getValue());
    }
  }

  public function testFieldChangeObserverCanSetSubject()
  {
    $header = $this->_getHeader('Subject',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->fieldChanged('subject', 'testing');
    $this->assertEqual('testing', $header->getValue());
  }

  public function testSubjectFieldChangeIsIgnoredByOtherHeaders()
  {
    $header = $this->_getHeader('Content-Type',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->setValue('text/plain');
    $header->fieldChanged('subject', 'testing');
    $this->assertEqual('text/plain', $header->getValue());
  }

  public function testOtherFieldChangesAreIgnoredForSubject()
  {
    $header = $this->_getHeader('Subject',
      new Swift_Mime_MockHeaderEncoder()
      );
    $header->setValue('testing');
    foreach (array('charset', 'comments', 'x-foo') as $field)
    {
      $header->fieldChanged($field, 'xxxxx');
      $this->assertEqual('testing', $header->getValue());
    }
  }

  // -- Private methods

  private function _getHeader($name, $encoder)
  {
    $header = new Swift_Mime_Header_UnstructuredHeader($name, $encoder);
    $header->setCharset($this->_charset);
    return $header;
  }

}