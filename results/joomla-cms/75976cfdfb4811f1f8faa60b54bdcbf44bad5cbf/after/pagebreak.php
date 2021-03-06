<?php
/**
* @version $Id: mospage.btn.php 1671 2006-01-06 10:20:01Z webImagery $
* @package Joomla
* @copyright Copyright (C) 2005 - 2006 Open Source Matters. All rights reserved.
* @license http://www.gnu.org/copyleft/gpl.html GNU/GPL, see LICENSE.php
* Joomla! is free software. This version may have been modified pursuant
* to the GNU General Public License, and as distributed it includes or
* is derivative of works licensed under the GNU General Public License or
* other free or open source software licenses.
* See COPYRIGHT.php for copyright notices and details.
*/

// no direct access
defined( '_JEXEC' ) or die( 'Restricted access' );

$mainframe->registerEvent( 'onCustomEditorButton', 'pluginPagebreakButton' );

/**
* mospage button
* @return array A two element array of ( imageName, textToInsert )
*/
function pluginPagebreakButton() {
	global $mainframe;

	$option = $mainframe->getOption();
	$doc = & $mainframe->getDocument();
	$template = $mainframe->getTemplate();
	$url = $mainframe->isAdmin() ? $mainframe->getSiteURL() : $mainframe->getBaseURL();
	// button is not active in specific content components
	switch ( $option ) {
		case 'com_sections':
		case 'com_categories':
		case 'com_modules':
			$button = array( false );
			break;

		default:
			$css = "\t.button1-left .pagebreak { background: url($url/plugins/editors-xtd/pagebreak.gif) 100% 0 no-repeat; }";
			$doc->addStyleDeclaration($css);
			$button = array( "jInsertEditorText('{pagebreak}')", JText::_('Pagebreak'), 'pagebreak' );
			break;
	}

	return $button;
}
?>