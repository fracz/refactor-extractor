<?php
/**
* @version $Id: Placeholder.php 1563 2005-12-27 20:09:40Z Jinx $
* @package Joomla
* @copyright Copyright (C) 2005 - 2006 Open Source Matters. All rights reserved.
* @license http://www.gnu.org/copyleft/gpl.html GNU/GPL, see LICENSE.php
* Joomla! is free software. This version may have been modified pursuant
* to the GNU General Public License, and as distributed it includes or
* is derivative of works licensed under the GNU General Public License or
* other free or open source software licenses.
* See COPYRIGHT.php for copyright notices and details.
*/

/**
 * JDocument Include function
 *
 * @author		Johan Janssens <johan.janssens@joomla.org>
 * @package		Joomla.Framework
 * @subpackage	Document
 * @since		1.5
 */
class patTemplate_Function_Include extends patTemplate_Function
{
   /**
	* name of the function
	* @access	private
	* @var		string
	*/
	var $_name	=	'include';

	/**
	* reference to the JDocument object that instantiated the module
	*
	* @access	protected
	* @var	object
	*/
	var	$_tmpl;


   /**
	* call the function
	*
	* @access	public
	* @param	array	parameters of the function (= attributes of the tag)
	* @param	string	content of the tag
	* @return	string	content to insert into the template
	*/
	function call( $params, $content )
	{
		global $mainframe;

		$doc =& $mainframe->getDocument();

		if(!isset($params['type'])) {
			return false;
		}

		$type = isset($params['type']) ? strtolower( $params['type'] ) : null;
		unset($params['type']);

		$name = isset($params['name']) ? strtolower( $params['name'] ) : null;
		unset($params['name']);

		switch($type)
		{
			case 'modules'  		:
			{
				$modules =& JModuleHelper::getModules($name);

				$total = count($modules);
				for($i = 0; $i < $total; $i++) {
					foreach($params as $param => $value) {
						$modules[$i]->$param = $value;
					}
				}

				$doc->_addRenderer($type, $name);

			} break;
			case 'module' 		:
			{
				$module =& JModuleHelper::getModule($name);

				foreach($params as $param => $value) {
					$module->$param = $value;
				}

				$doc->_addRenderer($type, $name);
			} break;

			default : $doc->_addRenderer($type, $name);
		}

		//dirty fix for unusedvar="none" template setting
		//$this->_tmpl->addVar('document', $type.'_'.$name, " ");

		return '{'.strtoupper($type).'_'.strtoupper($name).'}';
	}

	 /**
	* set a reference to the JDocument object that instantiated the function
	*
	* @access	public
	* @param	object		JDocument object
	*/
	function setTemplateReference( &$tmpl )
	{
		$this->_tmpl = &$tmpl;
	}
}
?>