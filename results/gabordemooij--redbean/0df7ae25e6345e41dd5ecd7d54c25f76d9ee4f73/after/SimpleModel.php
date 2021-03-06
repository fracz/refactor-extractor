<?php
/**
 * SimpleModel
 * Base Model For All RedBeanPHP Models using FUSE.
 *
 * @file    RedBean/SimpleModel.php
 * @desc    Part of FUSE
 * @author  Gabor de Mooij and the RedBeanPHP Team
 * @license	BSD/GPLv2
 *
 * copyright (c) G.J.G.T. (Gabor) de Mooij and the RedBeanPHP Community
 * This source file is subject to the BSD/GPLv2 License that is bundled
 * with this source code in the file license.txt.
 */
class RedBean_SimpleModel {

	/**
	 * @var RedBean_OODBBean
	 */
	protected $bean;

	/**
	 * Used by FUSE: the ModelHelper class to connect a bean to a model.
	 * This method loads a bean in the model.
	 *
	 * @param RedBean_OODBBean $bean bean
	 */
	public function loadBean(RedBean_OODBBean $bean) {
		$this->bean = $bean;
	}

	/**
	 * Magic Getter to make the bean properties available from
	 * the $this-scope.
	 *
	 * @param string $prop property
	 *
	 * @return mixed
	 */
	public function __get($prop) {
		return $this->bean->$prop;
	}

	/**
	 * Magic Setter
	 *
	 * @param string $prop  property
	 * @param mixed  $value value
	 */
	public function __set($prop, $value) {
		$this->bean->$prop = $value;
	}

	/**
	 * Isset implementation
	 *
	 * @param  string $key key
	 *
	 * @return
	 */
	public function __isset($key) {
		return (isset($this->bean->$key));
	}

	/**
	 * Box the bean using the current model.
	 *
	 * @return RedBean_SimpleModel
	 */
	public function box() {
		return $this;
	}

	/**
	 * Unbox the bean from the model.
	 *
	 * @return RedBean_OODBBean
	 */
	public function unbox(){
		return $this->bean;
	}

}