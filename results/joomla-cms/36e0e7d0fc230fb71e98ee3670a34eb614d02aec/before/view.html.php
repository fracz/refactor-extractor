<?php
/**
 * @package     Joomla.Administrator
 * @subpackage  com_installer
 *
 * @copyright   Copyright (C) 2005 - 2016 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE.txt
 */

defined('_JEXEC') or die;

include_once __DIR__ . '/../default/view.php';

/**
 * Extension Manager Update Sites View
 *
 * @package     Joomla.Administrator
 * @subpackage  com_installer
 * @since       3.4
 */
class InstallerViewUpdatesites extends InstallerViewDefault
{
	protected $items;

	protected $pagination;

	protected $form;

	protected $state;

	/**
	 * Display the view
	 *
	 * @param   string  $tpl  Template
	 *
	 * @return  mixed|void
	 *
	 * @since   3.4
	 *
	 * @throws  Exception on errors
	 */
	public function display($tpl = null)
	{
		// Get data from the model
		$this->state = $this->get('State');
		$this->items = $this->get('Items');
		$this->pagination = $this->get('Pagination');
		$this->filterForm = $this->get('FilterForm');
		$this->activeFilters = $this->get('ActiveFilters');

		// Check for errors.
		if (count($errors = $this->get('Errors')))
		{
			throw new Exception(implode("\n", $errors), 500);
		}

		// Check if there are no matching items
		if (!count($this->items))
		{
			JFactory::getApplication()->enqueueMessage(
				JText::_('COM_INSTALLER_MSG_MANAGE_NOUPDATESITE'),
				'warning'
			);
		}

		// Include the component HTML helpers.
		JHtml::addIncludePath(JPATH_COMPONENT . '/helpers/html');

		// Display the view
		parent::display($tpl);
	}

	/**
	 * Add the page title and toolbar.
	 *
	 * @return  void
	 *
	 * @since   3.4
	 */
	protected function addToolbar()
	{
		$canDo = JHelperContent::getActions('com_installer');

		if ($canDo->get('core.edit.state'))
		{
			JToolbarHelper::publish('updatesites.publish', 'JTOOLBAR_ENABLE', true);
			JToolbarHelper::unpublish('updatesites.unpublish', 'JTOOLBAR_DISABLE', true);
			JToolbarHelper::divider();
		}

		JHtmlSidebar::setAction('index.php?option=com_installer&view=updatesites');

		parent::addToolbar();
		JToolbarHelper::help('JHELP_EXTENSIONS_EXTENSION_MANAGER_UPDATESITES');
	}
}