<?php

/*
 An ID Mime Header in Swift Mailer.

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

//@require 'Swift/Mime/Header/AbstractHeader.php';

/**
 * An ID MIME Header for something like Message-ID or Content-ID.
 * @package Swift
 * @subpackage Mime
 * @author Chris Corbyn
 */
class Swift_Mime_Header_IdentificationHeader
  extends Swift_Mime_Header_AbstractHeader
{

  /**
   * The IDs used in the value of this Header.
   * This may hold multiple IDs or just a single ID.
   * @var string[]
   * @access private
   */
  private $_ids = array();

  /**
   * Creates a new IdentificationHeader with the given $name and $id.
   * @param string $name
   */
  public function __construct($name)
  {
    $this->setFieldName($name);
    $this->initializeGrammar();
  }

  /**
   * Set the model for the field body.
   * This method takes a string ID, or an array of IDs
   * @param mixed $model
   */
  public function setFieldBodyModel($model)
  {
    $this->setId($model);
  }

  /**
   * Get the model for the field body.
   * This method returns an array of IDs
   * @return array
   */
  public function getFieldBodyModel()
  {
    return $this->getIds();
  }

  /**
   * Set the ID used in the value of this header.
   * @param string $id
   */
  public function setId($id)
  {
    return $this->setIds(array($id));
  }

  /**
   * Get the ID used in the value of this Header.
   * If multiple IDs are set only the first is returned.
   * @return string
   */
  public function getId()
  {
    if (count($this->_ids) > 0)
    {
      return $this->_ids[0];
    }
  }

  /**
   * Set a collection of IDs to use in the value of this Header.
   * @param string[] $ids
   */
  public function setIds(array $ids)
  {
    $actualIds = array();

    foreach ($ids as $k => $id)
    {
      if (preg_match(
        '/^' . $this->getGrammar('id-left') . '@' .
        $this->getGrammar('id-right') . '$/D',
        $id
        ))
      {
        $actualIds[] = $id;
      }
      else
      {
        throw new Exception('Invalid ID given <' . $id . '>');
      }
    }

    $this->_ids = $actualIds;
    $this->setCachedValue(null);
  }

  /**
   * Get the list of IDs used in this Header.
   * @return string[]
   */
  public function getIds()
  {
    return $this->_ids;
  }

  /**
   * Get the string value of the body in this Header.
   * This is not necessarily RFC 2822 compliant since folding white space will
   * not be added at this stage (see {@link toString()} for that).
   * @return string
   * @see toString()
   */
  public function getFieldBody()
  {
    if (!$this->getCachedValue())
    {
      $angleAddrs = array();

      foreach ($this->_ids as $id)
      {
        $angleAddrs[] = '<' . $id . '>';
      }

      $this->setCachedValue(implode(' ', $angleAddrs));
    }
    return $this->getCachedValue();
  }

}