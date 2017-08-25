<?php

/*
 The default Message class Swift Mailer.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

//@require 'Swift/Mime/Message.php';
//@require 'Swift/Mime/MimePart.php';
//@require 'Swift/Mime/MimeEntity.php';
//@require 'Swift/Mime/ContentEncoder.php';

/**
 * The default email message class.
 * @package Swift
 * @subpackage Mime
 * @author Chris Corbyn
 */
class Swift_Mime_SimpleMessage extends Swift_Mime_MimePart
  implements Swift_Mime_Message
{

  /**
   * Fields which must always be displayed.
   * @var string[]
   * @access private
   */
  private $_requiredFields = array('date', 'from', 'message-id');

  /**
   * Creates a new MimePart with $headers and $encoder.
   * @param string[] $headers
   * @param Swift_Mime_ContentEncoder $encoder
   * @param string $charset, optional
   */
  public function __construct(array $headers,
    Swift_Mime_ContentEncoder $encoder, $charset = null)
  {
    parent::__construct($headers, $encoder, $charset);
    $this->setNestingLevel(self::LEVEL_TOP);
    $this->setDate(time());
  }

  /**
   * Set the Message-ID.
   * @param string $id
   */
  public function setId($id)
  {
    return $this->_setHeaderModel('message-id', $id);
  }

  /**
   * Get the Message-ID.
   * @return string
   */
  public function getId()
  {
    $model = (array) $this->_getHeaderModel('message-id');
    return current($model);
  }

  /**
   * Set the subject of the message.
   * @param string $subject
   */
  public function setSubject($subject)
  {
    return $this->_setHeaderModel('subject', $subject);
  }

  /**
   * Get the subject of the message.
   * @return string
   */
  public function getSubject()
  {
    return $this->_getHeaderModel('subject');
  }

  /**
   * Set the origination date of the message as a UNIX timestamp.
   * @param int $date
   */
  public function setDate($date)
  {
    return $this->_setHeaderModel('date', (int) $date);
  }

  /**
   * Get the origination date of the message as a UNIX timestamp.
   * @return int
   */
  public function getDate()
  {
    return $this->_getHeaderModel('date');
  }

  /**
   * Set the return-path (bounce-detect) address.
   * @param string $address
   */
  public function setReturnPath($address)
  {
    if (!is_null($address))
    {
      $address = (string) $address;
    }
    return $this->_setHeaderModel('return-path', $address);
  }

  /**
   * Get the return-path (bounce-detect) address.
   * @return string
   */
  public function getReturnPath()
  {
    return $this->_getHeaderModel('return-path');
  }

  /**
   * Set the sender of this message.
   * If multiple addresses are present in the From field, this SHOULD be set.
   * According to RFC 2822 it is a requirement when there are multiple From
   * addresses, but Swift itself does not require it directly.
   * An associative array (with one element!) can be used to provide a display-
   * name: i.e. array('email@address' => 'Real Name').
   * @param mixed $address
   */
  public function setSender($address)
  {
    $address = $this->_normalizeMailboxes((array) $address);
    return $this->_setHeaderModel('sender', $address);
  }

  /**
   * Get the sender address for this message.
   * This has a higher significance than the From address.
   * This method always returns an associative array where the key provides the address.
   * @return string[]
   */
  public function getSender()
  {
    return (array) $this->_getHeaderModel('sender');
  }

  /**
   * Set the From address of this message.
   * It is permissible for multiple From addresses to be set using an array.
   * If multiple From addresses are used, you SHOULD set the Sender address and
   * according to RFC 2822, MUST set the sender address.
   * An array can be used if display names are to be provided: i.e.
   * array('email@address.com' => 'Real Name').
   * @param mixed $addresses
   */
  public function setFrom($addresses)
  {
    $addresses = $this->_normalizeMailboxes((array) $addresses);
    return $this->_setHeaderModel('from', $addresses);
  }

  /**
   * Get the From address(es) of this message.
   * This method always returns an associative array where the keys are the addresses.
   * @return string[]
   */
  public function getFrom()
  {
    return (array) $this->_getHeaderModel('from');
  }

  /**
   * Set the Reply-To address(es).
   * Any replies from the receiver will be sent to this address.
   * It is permissible for multiple reply-to addresses to be set using an array.
   * This method has the same synopsis as {@link setFrom()} and {@link setTo()}.
   * @param mixed $addresses
   */
  public function setReplyTo($addresses)
  {
    $addresses = $this->_normalizeMailboxes((array) $addresses);
    return $this->_setHeaderModel('reply-to', $addresses);
  }

  /**
   * Get the Reply-To addresses for this message.
   * This method always returns an associative array where the keys provide the
   * email addresses.
   * @return string[]
   */
  public function getReplyTo()
  {
    return (array) $this->_getHeaderModel('reply-to');
  }

  /**
   * Set the To address(es).
   * Recipients set in this field will receive a copy of this message.
   * This method has the same synopsis as {@link setFrom()} and {@link setCc()}.
   * @param mixed $addresses
   */
  public function setTo($addresses)
  {
    $addresses = $this->_normalizeMailboxes((array) $addresses);
    return $this->_setHeaderModel('to', $addresses);
  }

  /**
   * Get the To addresses for this message.
   * This method always returns an associative array, whereby the keys provide
   * the actual email addresses.
   * @return string[]
   */
  public function getTo()
  {
    return (array) $this->_getHeaderModel('to');
  }

  /**
   * Set the Cc address(es).
   * Recipients set in this field will receive a 'carbon-copy' of this message.
   * This method has the same synopsis as {@link setFrom()} and {@link setTo()}.
   * @param mixed $addresses
   */
  public function setCc($addresses)
  {
    $addresses = $this->_normalizeMailboxes((array) $addresses);
    return $this->_setHeaderModel('cc', $addresses);
  }

  /**
   * Get the Cc addresses for this message.
   * This method always returns an associative array, whereby the keys provide
   * the actual email addresses.
   * @return string[]
   */
  public function getCc()
  {
    return (array) $this->_getHeaderModel('cc');
  }

  /**
   * Set the Bcc address(es).
   * Recipients set in this field will receive a 'blind-carbon-copy' of this message.
   * In other words, they will get the message, but any other recipients of the
   * message will have no such knowledge of their receipt of it.
   * This method has the same synopsis as {@link setFrom()} and {@link setTo()}.
   * @param mixed $addresses
   */
  public function setBcc($addresses)
  {
    $addresses = $this->_normalizeMailboxes((array) $addresses);
    return $this->_setHeaderModel('bcc', $addresses);
  }

  /**
   * Get the Bcc addresses for this message.
   * This method always returns an associative array, whereby the keys provide
   * the actual email addresses.
   * @return string[]
   */
  public function getBcc()
  {
    return (array) $this->_getHeaderModel('bcc');
  }

  /**
   * Attach an individual child entity to this message.
   * This may be an attachment, a mime part, an embedded file etc.
   * Semantically this is the same as {@link setChildren()} except it only
   * adds the child and doesn't overwrite existing children.
   * This method returns the instance it belongs to so a fluid interface can
   * be used.
   * @param Swift_Mime_MimeEntity $entity
   * @return Swift_Mime_Message
   */
  public function attach(Swift_Mime_MimeEntity $entity)
  {
    $this->setChildren(array_merge($this->getChildren(), array($entity)));
    return $this;
  }

  /**
   * Detach an individual child entity from this message.
   * @param Swift_Mime_MimeEntity $entity
   * @return Swift_Mime_Message
   * @see attach(), detachId()
   */
  public function detach(Swift_Mime_MimeEntity $entity)
  {
    $this->detachId($entity->getId());
    return $this;
  }

  /**
   * Detach an individual child entity from this message based on its ID.
   * @param string $id
   * @return Swift_Mime_Message
   * @see attach(), embed(), detach()
   */
  public function detachId($id)
  {
    $children = $this->getChildren();
    foreach ($children as $index => $child)
    {
      if ($child->getId() == $id)
      {
        unset($children[$index]);
      }
    }
    $this->setChildren(array_values($children));
    return $this;
  }

  /**
   * Attach an individual child entity to this message and return a cid
   * string for use when embedding content.
   * This method performs the same operation as attach, except it returns a
   * string of the form "cid:< id of child >" which can be used as a src attribute
   * in a HTML message.
   * @param Swift_Mime_MimeEntity $entity
   * @return string
   */
  public function embed(Swift_Mime_MimeEntity $entity)
  {
    $this->attach($entity);
    return 'cid:' . $entity->getId();
  }

  /**
   * Add a list of header field names which should always be shown.
   * The default behaviour is to hide non-required fields if their bodies are
   * empty.
   * @param string[] $required
   */
  public function addRequiredFields(array $required)
  {
    foreach ($required as $req)
    {
      $this->_requiredFields[] = strtolower($req);
    }
  }

  /**
   * Get a list of (lowercased) header field names which will always be displayed.
   * This is actually a point of extension.
   * @return string[]
   */
  public function getRequiredFields()
  {
    return $this->_requiredFields;
  }

  // -- Private methods

  /**
   * Normalizes a user-input list of mailboxes into consistent key=>value pairs.
   * @param string[] $mailboxes
   * @return string[]
   * @access private
   */
  private function _normalizeMailboxes(array $mailboxes)
  {
    $actualMailboxes = array();
    foreach ($mailboxes as $key => $value)
    {
      if (is_string($key)) //key is email addr
      {
        $address = $key;
        $name = $value;
      }
      else
      {
        $address = $value;
        $name = null;
      }
      $actualMailboxes[$address] = $name;
    }
    return $actualMailboxes;
  }

}