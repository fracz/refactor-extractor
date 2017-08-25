<?php
namespace Former\Traits;

use Former\Former;
use Former\Helpers;
use HtmlObject\Element;

/**
 * Base Former object that allows chained attributes setting, adding
 * classes to the existing ones, and provide types helpers
 */
abstract class FormerObject extends Element
{
  /**
   * The field's name
   *
   * @var string
   */
  protected $name;

  /**
   * A list of class properties to be added to attributes
   *
   * @var array
   */
  protected $injectedProperties = array('name');

  ////////////////////////////////////////////////////////////////////
  /////////////////////////// ID AND LABELS //////////////////////////
  ////////////////////////////////////////////////////////////////////

  /**
   * Set the matching ID on a field if possible
   */
  protected function setId()
  {
    if (!array_key_exists('id', $this->attributes) and
      in_array($this->name, Former::$labels)) {
        $this->attributes['id'] = $this->name;
    }
  }

  public function render()
  {
    $this->setId();

    return parent::render();
  }

  ////////////////////////////////////////////////////////////////////
  ////////////////////////////// GETTERS /////////////////////////////
  ////////////////////////////////////////////////////////////////////

  /**
   * Get the object's name
   *
   * @return string
   */
  public function getName()
  {
    return $this->name;
  }

  ////////////////////////////////////////////////////////////////////
  //////////////////////////// OBJECT TYPE ///////////////////////////
  ////////////////////////////////////////////////////////////////////

  /**
   * Get the object's type
   *
   * @return string
   */
  public function getType()
  {
    return $this->type;
  }

  /**
   * Change a object's type
   *
   * @param string $type
   */
  public function setType($type)
  {
    $this->type = $type;
  }

  /**
   * Check if an object is of a certain type
   *
   * @param  string $types* The type(s) to check for
   * @return boolean
   */
  public function isOfType()
  {
    $types = func_get_args();

    return in_array($this->type, $types);
  }
}