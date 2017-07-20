<?php
/**
 * @version		$Id$
 * @copyright	Copyright (C) 2005 - 2009 Open Source Matters, Inc. All rights reserved.
 * @license		GNU General Public License version 2 or later; see LICENSE.txt
 */

// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die;

jimport('joomla.application.component.view');

/**
 * HTML View class for the Templates component
 *
 * @static
 * @package		Joomla.Administrator
 * @subpackage	Templates
 * @since 1.0
 */
class TemplatesViewTemplates extends JView
{
	protected $rows;
	protected $pagination;
	protected $client;

	public function display($tpl = null)
	{
		// Get data from the model
		$rows		= & $this->get('Data');
		$total		= & $this->get('Total');
		$pagination = & $this->get('Pagination');
		$client		= & $this->get('Client');

		// Check for errors.
		if (count($errors = $this->get('Errors')))
		{
			JError::raiseError(500, implode("\n", $errors));
			return false;
		}

		$this->assignRef('rows',		$rows);
		$this->assignRef('pagination',	$pagination);
		$this->assignRef('client',		$client);

		$this->_setToolbar();
		parent::display($tpl);
	}

	/**
	 * Setup the Toolbar.
	 */
	protected function _setToolbar()
	{
		$state	= $this->get('State');
		$canDo	= TemplatesHelper::getActions();

		JToolBarHelper::title(JText::_('Templates_Manager_Templates'), 'thememanager');
		if ($canDo->get('core.edit')) {
			JToolBarHelper::editList('style.edit');
		}
		if ($canDo->get('core.admin')) {
			JToolBarHelper::preferences('com_templates');
		}
		JToolBarHelper::help('screen.templates');
	}
}