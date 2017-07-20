<?php
/**
 * @package     Joomla.Site
 * @subpackage  Layout
 *
 * @copyright   Copyright (C) 2005 - 2013 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE.txt
 */

defined('_JEXEC') or die;

// JLayout for standard handling of the edit modules:

$moduleHtml   =& $displayData['moduleHtml'];
$mod          =  $displayData['module'];
$position     =  $displayData['position'];
$menusEditing =  $displayData['menusediting'];


if (preg_match('/<(?:div|span|nav|ul|ol|h\d) [^>]*class="[^"]* jmoddiv"/', $moduleHtml))
{
	// Module has already module edit button:
	return;
}

// Add css class jmoddiv and data attributes for module-editing URL and for the tooltip:
$editUrl = JURI::base() . 'administrator/index.php?option=com_modules&view=module&layout=edit&id=' . (int) $mod->id;

// Add class, editing URL and tooltip, and if module of type menu, also the tooltip for editing the menu item:
$count = 0;
$moduleHtml = preg_replace('/^(<(?:div|span|nav|ul|ol|h\d) [^>]*class="[^"]*)"/',
	'\\1 jmoddiv" data-jmodediturl="' . $editUrl. '"'
	. ' data-jmodtip="'
	. JHtml::tooltipText(JText::_('JLIB_HTML_EDIT_MODULE'), htmlspecialchars($mod->title) . '<br />' . sprintf(JText::_('JLIB_HTML_EDIT_MODULE_IN_POSITION'), htmlspecialchars($position)), 0)
	. '"'
	. ($menusEditing && $mod->module == 'mod_menu' ? '" data-jmenuedittip="' . JHtml::tooltipText('JLIB_HTML_EDIT_MENU_ITEM', 'JLIB_HTML_EDIT_MENU_ITEM_ID') . '"' : ''),
	$moduleHtml, 1, $count);

static $jsOut = false;

if ($count && !$jsOut)
{
	// Load once booststrap tooltip and add stylesheet and javascript to head:
	$jsOut = true;
	JHtml::_('bootstrap.tooltip');
	JHtml::_('bootstrap.popover');

	JFactory::getDocument()->addStyleSheet('media/system/css/frontediting.css')
		->addScript('media/system/js/frontediting.js');
}

?>