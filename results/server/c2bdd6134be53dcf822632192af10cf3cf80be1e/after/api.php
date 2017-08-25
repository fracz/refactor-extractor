<?php

/**
* ownCloud - ajax frontend
*
* @author Robin Appelman
* @copyright 2010 Robin Appelman icewind1991@gmail.com
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/
require_once('../inc/lib_base.php');

$arguments=$_POST;

foreach($arguments as &$argument){
	$argument=stripslashes($argument);
}
ob_clean();
switch($arguments['action']){
	case 'delete':
		OC_FILES::delete($arguments['dir'],$arguments['file']);
		break;
	case 'rename':
		OC_FILES::move($arguments['dir'],$arguments['file'],$arguments['dir'],$arguments['newname']);
		break;
	case 'new':
		OC_FILES::newfile($arguments['dir'],$arguments['name'],$arguments['type']);
		break;
	case 'move':
		OC_FILES::move($arguments['sourcedir'],$arguments['source'],$arguments['targetdir'],$arguments['target']);
		break;
	case 'get':
		OC_FILES::get($arguments['dir'],$arguments['file']);
		break;
}

?>