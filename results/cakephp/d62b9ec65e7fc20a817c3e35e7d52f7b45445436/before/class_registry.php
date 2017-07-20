<?php
/* SVN FILE: $Id$ */

/**
 * Class collections.
 *
 * A repository for class objects, each registered with a key.
 *
 * PHP versions 4 and 5
 *
 * CakePHP :  Rapid Development Framework <http://www.cakephp.org/>
 * Copyright (c) 2005, CakePHP Authors/Developers
 *
 * Author(s): Michal Tatarynowicz aka Pies <tatarynowicz@gmail.com>
 *            Larry E. Masters aka PhpNut <nut@phpnut.com>
 *            Kamil Dzielinski aka Brego <brego.dk@gmail.com>
 *
 *  Licensed under The MIT License
 *  Redistributions of files must retain the above copyright notice.
 *
 * @filesource
 * @author       CakePHP Authors/Developers
 * @copyright    Copyright (c) 2005, CakePHP Authors/Developers
 * @link         https://trac.cakephp.org/wiki/Authors Authors/Developers
 * @package      cake
 * @subpackage   cake.libs
 * @since        CakePHP v 0.9.2
 * @version      $Revision$
 * @modifiedby   $LastChangedBy$
 * @lastmodified $Date$
 * @license      http://www.opensource.org/licenses/mit-license.php The MIT License
 */

/**
 * Class Collections.
 *
 * A repository for class objects, each registered with a key.
 * If you try to add an object with the same key twice, nothing will come of it.
 * If you need a second instance of an object, give it another key.
 *
 * @package    cake
 * @subpackage cake.libs
 * @since      CakePHP v 0.9.2
 */
   class ClassRegistry
   {

/**
 * Names of classes with their objects.
 *
 * @var array
 * @access private
 */
   var $_objects = array();

/**
 * Return a singleton instance of the ClassRegistry.
 *
 * @return ClassRegistry instance
 */
   function &getInstance() {

       static $instance = array();
       if (!$instance)
       {
           $instance[0] =& new ClassRegistry;
       }
       return $instance[0];
   }

/**
 * Add $object to the registry, associating it with the name $key.
 *
 * @param string $key
 * @param mixed $object
 */
   function addObject($key, &$object)
   {
      $key = strtolower($key);

      if (array_key_exists($key, $this->_objects) === false)
      {
         $this->_objects[$key] =& $object;
      }
   }

/**
 * Returns true if given key is present in the ClassRegistry.
 *
 * @param string $key 	Key to look for
 * @return boolean 		Success
 */
   function isKeySet($key)
   {
      $key = strtolower($key);
      return array_key_exists($key, $this->_objects);
   }

/**
 * Return object which corresponds to given key.
 *
 * @param string $key
 * @return mixed
 */
   function &getObject($key)
   {
      $key = strtolower($key);
      return $this->_objects[$key];
   }
}
?>