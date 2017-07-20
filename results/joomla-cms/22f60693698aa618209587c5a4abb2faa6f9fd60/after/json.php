<?php
/**
 * @package     Joomla.Administrator
 * @subpackage  com_config
 *
 * @copyright   Copyright (C) 2005 - 2013 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE.txt
 */

defined('_JEXEC') or die;


/**
 * View for the component configuration
 *
 * @package     Joomla.Administrator
 * @subpackage  com_config
 * @since       3.2
 */
class ConfigViewApplicationJson extends ConfigViewJson
{

	public $state;

	public $data;

	/**
	 * Display the view
	 *
	 * @param   string  $tpl  Layout
	 *
	 * @return  string
	 */
	public function render()
	{


		try
		{
			$this->data = $this->model->getData();
			$user = JFactory::getUser();
			$app = JFactory::getApplication();
		}
		catch (Exception $e)
		{
			JErrorPage::render($e);

			return false;
		}

		$this->userIsSuperAdmin = $user->authorise('core.admin');

		// Required data
		$requiredData = array("sitename" => null,
				"offline" => null,
				"access" => null,
				"list_limit" => null,
				"MetaDesc" => null,
				"MetaKeys" => null,
				"MetaRights" => null,
				"sef" => null,
				"sitename_pagetitles" => null,
				"debug" => null,
				"debug_lang" =>null,
				"error_reporting" => null,
				"mailfrom" => null,
				"fromname" => null
		);

		$this->data = array_intersect_key($this->data,$requiredData);

		return json_encode($this->data);
	}

}