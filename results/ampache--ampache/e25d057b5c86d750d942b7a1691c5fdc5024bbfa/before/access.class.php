<?php
/*

 Copyright (c) Ampache.org
 All Rights Reserved

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License v2
 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
/**
 * Access Class
 * This class handles the access list mojo for Ampache, it is ment to restrict
 * access based on IP and maybe something else in the future
*/
class Access {

	/* Variables from DB */
	public $id;
	public $name;
	public $start;
	public $end;
	public $level;
	public $user;
	public $type;
	public $key;

	/**
	 * constructor
	 * Takes an ID of the access_id dealie :)
	 */
	public function __construct($access_id='') {

		if (!$access_id) { return false; }

		/* Assign id for use in get_info() */
		$this->id = intval($access_id);

		$info = $this->_get_info();
		foreach ($info as $key=>$value) {
			$this->$key = $value;
		}

		return true;

	} // Constructor

	/**
	 * _get_info
	 * get's the vars for $this out of the database
	 * Taken from the object
	 */
	private function _get_info() {

		/* Grab the basic information from the catalog and return it */
		$sql = "SELECT * FROM `access_list` WHERE `id`='" . Dba::escape($this->id) . "'";
		$db_results = Dba::query($sql);

		$results = Dba::fetch_assoc($db_results);

		return $results;

	} // _get_info

	/**
	 * update
	 * This function takes a named array as a datasource and updates the current access list entry
	 */
	public function update($data) {

		$name	= Dba::escape($data['name']);
		$type	= self::validate_type($data['type']);
		$start 	= sprintf("%u",ip2long($data['start']));
		$end	= sprintf("%u",ip2long($data['end']));
		$level	= Dba::escape($data['level']);
		$user	= $data['user'] ? Dba::escape($data['user']) : '-1';
		$key	= Dba::escape($data['key']);

		$sql = "UPDATE `access_list` " .
			"SET `start`='$start', `end`='$end', `level`='$level', `user`='$user', `key`='$key', " .
			"`name`='$name', `type`='$type' WHERE `id`='" . Dba::escape($this->id) . "'";
		$db_results = Dba::query($sql);

		return true;

	} // update

	/**
	 * create
	 * This takes a key'd array of data and trys to insert it as a
	 * new ACL entry
	 */
	public static function create($data) {

		/* We need to verify the incomming data a littlebit */

		$start 	= sprintf("%u",ip2long($data['start']));
		$end 	= sprintf("%u",ip2long($data['end']));
		$name	= Dba::escape($data['name']);
		$key	= Dba::escape($data['key']);
		$user	= $data['user'] ? Dba::escape($data['user']) : '-1';
		$level	= intval($data['level']);
		$type	= self::validate_type($data['type']);
		$dns	= ' ';

		$sql = "INSERT INTO `access_list` (`name`,`level`,`start`,`end`,`key`,`user`,`type`,`dns`) " .
			"VALUES ('$name','$level','$start','$end','$key','$user','$type','$dns')";
		$db_results = Dba::query($sql);

		return true;

	} // create

	/**
	 * delete
	 * deletes the specified access_list entry
	 */
	public static function delete($access_id) {

		$sql = "DELETE FROM `access_list` WHERE `id`='" . Dba::escape($access_id) . "'";
		$db_results = Dba::query($sql);

	} // delete

	/**
	 * check_function
	 * This checks if a specific functionality is enabled
	 * it takes a type only
	 */
	public static function check_function($type) {

		switch ($type) {
			case 'download':
				return Config::get('download');
			break ;
			case 'batch_download':
                                if (!function_exists('gzcompress')) {
                                        debug_event('gzcompress','ZLIB Extensions not loaded, batch download disabled','3');
                                        return false;
                                }
                                if (Config::get('allow_zip_download') AND $GLOBALS['user']->has_access(25)) {
                                        return Config::get('download');
                                }
                        break;
			default:
				return false;
			break;
		} // end switch

	} // check_function

	/**
	 * check_network
	 * This takes a type, ip, user, level and key
	 * and then returns true or false if they have access to this
	 * the IP is passed as a dotted quad
	 */
	public static function check_network($type,$ip='',$user,$level,$key='') {

		// They aren't using access control
		// lets just keep on trucking
		if (!Config::get('access_control')) {
			return true;
		}

		// Clean incomming variables
		$ip 	= $ip ? sprintf("%u",ip2long($ip)) : sprintf("%u",ip2long($_SERVER['REMOTE_ADDR']));
		$user 	= Dba::escape($user);
		$key 	= Dba::escape($key);
		$level	= Dba::escape($level);

		switch ($type) {
			/* This is here because we want to at least check IP before even creating the xml-rpc server
			 * however we don't have the key that was passed yet so we've got to do just ip
			 */
			case 'init-rpc':
			case 'init-xml-rpc':
				$sql = "SELECT `id` FROM `access_list`" .
					" WHERE `start` <= '$ip' AND `end` >= '$ip' AND `type`='rpc' AND `level` >= '$level'";
			break;
			case 'rpc':
			case 'xml-rpc':
				$sql = "SELECT `id` FROM `access_list`" .
					" WHERE `start` <= '$ip' AND `end` >= '$ip'" .
					" AND  `key` = '$key' AND `level` >= '$level' AND `type`='rpc'";
			break;
			case 'init-api':
				$type = 'rpc';
				if ($user) {
					$client = User::get_from_username($user);
					$user = $client->id;
				}
			case 'network':
			case 'interface':
			case 'stream':
			default:
				$sql = "SELECT `id` FROM `access_list`" .
					" WHERE `start` <= '$ip' AND `end` >= '$ip'" .
					" AND `level` >= '$level' AND `type` = '$type'";
				if (strlen($user)) { $sql .= " AND (`user` = '$user' OR `user` = '-1')"; }
				else { $sql .= " AND `user` = '-1'"; }
			break;
		} // end switch on type

		$db_results = Dba::query($sql);

		// Yah they have access they can use the mojo
		if (Dba::fetch_row($db_results)) {
			return true;
		}

		// No Access Sucks to be them.
		else {
			return false;
		}

	} // check_network

	/**
	 * check_access
	 * This is the global 'has_access' function it can check for any 'type' of object
	 * everything uses the global 0,5,25,50,75,100 stuff. GLOBALS['user'] is always used
	 */
	public static function check($type,$level) {

		if (!Config::get('use_auth') || Config::get('demo_mode')) { return true; }

		$level = intval($level);

		// Switch on the type
		switch ($type) {
			case 'localplay':
				// Check their localplay_level
				if (Config::get('localplay_level') >= $level OR $GLOBALS['user']->access >= '100') {
					return true;
				}
				else {
					return false;
				}
			break;
			case 'interface':
				// Check their standard user level
				if ($GLOBALS['user']->access >= $level) {
					return true;
				}
				else {
					return false;
				}
			break;
			default:
				return false;
			break;
		} // end switch on type

		// Default false
		return false;

	} // check

	/**
	 * validate_type
	 * This cleans up and validates the specified type
	 */
	public static function validate_type($type) {

		switch($type) {
			case 'rpc':
			case 'interface':
			case 'network':
				return $type;
			break;
			case 'xml-rpc':
				return 'rpc';
			break;
			default:
				return 'stream';
			break;
		} // end switch

	} // validate_type

	/**
	 * get_access_lists
	 * returns a full listing of all access rules on this server
	 */
	public static function get_access_lists() {

		$sql = "SELECT `id` FROM `access_list`";
		$db_results = Dba::query($sql);

		$results = array();

		// Man this is the wrong way to do it...
		while ($row = Dba::fetch_assoc($db_results)) {
			$results[] = $row['id'];
		} // end while access list mojo

		return $results;

	} // get_access_lists


	/**
	 * get_level_name
	 * take the int level and return a named level
	 */
	public function get_level_name() {

		if ($this->level == '75') {
			return _('All');
		}
		if ($this->level == '5') {
			return _('View');
		}
		if ($this->level == '25') {
			return _('Read');
		}
		if ($this->level == '50') {
			return _('Read/Write');
		}

	} // get_level_name

	/**
 	 * get_user_name
	 * Take a user and return their full name
	 */
	public function get_user_name() {

		$user = new User($this->user);
		if ($user->username) {
			return $user->fullname . " (" . $user->username . ")";
		}

		return _('All');

	} // get_user_name

	/**
	 * get_type_name
	 * This function returns the pretty name for our current type
	 */
	public function get_type_name() {

		switch ($this->type) {
			case 'xml-rpc':
			case 'rpc':
				return 'RPC';
			break;
			case 'network':
				return 'Local Network Definition';
			break;
			case 'interface':
				return 'Web Interface';
			break;
			case 'stream':
			default:
				return 'Stream Access';
			break;
		} // end switch

	} // get_type_name

	/**
	 * session_exists
	 * This checks to see if the specified session of the specified type
	 * exists, it also provides an array of key'd data that may be required
	 * based on the type
	 */
	public static function session_exists($data,$key,$type) {

		// Switch on the type they pass
		switch ($type) {
			case 'api':
				$key = Dba::escape($key);
				$time = time();
				$sql = "SELECT * FROM `session_api` WHERE `id`='$key' AND `expire` > '$time'";
				$db_results = Dba::query($sql);

				if (Dba::num_rows($db_results)) {
					$time = $time + 3600;
					$sql = "UPDATE `session_api` WHERE `id`='$key' SET `expire`='$time'";
					$db_results = Dba::query($sql);
					return true;
				}

				return false;

			break;
			case 'stream':

			break;
			case 'interface':

			break;
			default:
				return false;
			break;
		} // type


	} // session_exists

} //end of access class

?>