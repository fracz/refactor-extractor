<?php

/**
* ownCloud
*
* @author Frank Karlitschek
* @copyright 2010 Frank Karlitschek karlitschek@kde.org
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
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/


/**
 * Class for logging features
 *
 */
class OC_LOG {

	/**
	 * array to define different log types
	 *
	 */
	public static $TYPE = array (
		1=>'login',
		2=>'logout',
		3=>'read',
		4=>'write' );


	/**
	 * log an event
	 *
	 * @param username $user
	 * @param type $type
	 * @param message $message
	 */
	public static function event($user,$type,$message){
			global $CONFIG_DBTABLEPREFIX;
		$result = OC_DB::query('INSERT INTO `' . $CONFIG_DBTABLEPREFIX . 'log` (`timestamp`,`user`,`type`,`message`) VALUES ('.time().',\''.addslashes($user).'\','.addslashes($type).',\''.addslashes($message).'\');');
	}


	/**
	 * get log entries
	 */
	public static function get(){
		global $CONFIG_DATEFORMAT;
		global $CONFIG_DBTABLEPREFIX;

		$result;

		if(OC_USER::ingroup($_SESSION['username_clean'],'admin')){
			$result = OC_DB::select('select `timestamp`,`user`,`type`,`message` from '.$CONFIG_DBTABLEPREFIX.'log order by timestamp desc limit 20');
		}
		else{
			$user=$_SESSION['username_clean'];
			$result = OC_DB::select('select `timestamp`,`user`,`type`,`message` from '.$CONFIG_DBTABLEPREFIX.'log where user=\''.$user.'\' order by timestamp desc limit 20');
		}

		return $result;
	}
}



?>