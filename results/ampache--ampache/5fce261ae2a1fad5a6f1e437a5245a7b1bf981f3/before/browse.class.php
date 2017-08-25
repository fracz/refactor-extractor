<?php
/*

 Copyright (c) Ampache.org
 All rights reserved.

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
 * Browse Class
 * This handles all of the sql/filtering
 * on the data before it's thrown out to the templates
 * it also handles pulling back the object_ids and then
 * calling the correct template for the object we are displaying
 */
class Browse {

	// Public static vars that are cached
	public static $sql;
	public static $start;
	public static $total_objects;
	public static $type;

	// Boolean if this is a simple browse method (use different paging code)
	public static $simple_browse;

	// Static Content, this is defaulted to false, if set to true then wen can't
	// apply any filters that would change the result set.
	public static $static_content = false;


	/**
	 * constructor
	 * This should never be called
	 */
	private function __construct() {

		// Rien a faire

	} // __construct


	/**
	 * set_filter
	 * This saves the filter data we pass it into the session
	 * This is done here so that it's easy to modify where the data
	 * is saved should I change my mind in the future. It also makes
	 * a single point for whitelist tweaks etc
	 */
	public static function set_filter($key,$value) {

		switch ($key) {
			case 'show_art':
				if ($_SESSION['browse']['filter'][$key]) {
					unset($_SESSION['browse']['filter'][$key]);
				}
				else {
				        $_SESSION['browse']['filter'][$key] = 1;
				}
			break;
                       case 'tag':
				if (is_array($value)) {
					$_SESSION['browse']['filter'][$key] = $value;
				}
				elseif (is_numeric($value)) {
					$_SESSION['browse']['filter'][$key] = array($value);
				}
				else {
					$_SESSION['browse']['filter'][$key] = array();
				}
			break;
			case 'artist':
			case 'album':
				$_SESSION['browse']['filter'][$key] = $value;
			break;
			case 'min_count':

			case 'unplayed':
			case 'rated':

			break;
			case 'alpha_match':
				if (self::$static_content) { return false; }
				$_SESSION['browse']['filter'][$key] = $value;
			break;
			case 'playlist_type':
				// They must be content managers to turn this off
				if ($_SESSION['browse']['filter'][$key] AND Access::check('interface','50')) { unset($_SESSION['browse']['filter'][$key]); }
				else { $_SESSION['browse']['filter'][$key] = '1'; }
			break;
                        default:
                                // Rien a faire
				return false;
                        break;
                } // end switch

		return true;

	} // set_filter

	/**
	 * reset_filter
	 * This is a wrapper function that resets the filters
	 */
	public static function reset_filters() {

		if (!is_array($_SESSION['browse']['filter'])) { return true; }

		foreach ($_SESSION['browse']['filter'] AS $key=>$value) {
			self::set_filter($key,'');
		}

	} // reset_filters

	/**
	 * reset_supplemental_objects
	 * This clears any sup objects we've added, normally called on every set_type
	 */
	public static function reset_supplemental_objects() {

		$_SESSION['browse']['supplemental'] = array();

	} // reset_supplemental_objects

	/**
	 * get_filter
	 * returns the specified filter value
	 */
	public static function get_filter($key) {

		// Simple enough, but if we ever move this crap
		return $_SESSION['browse']['filter'][$key];

	} // get_filter

	/**
	 * get_allowed_filters
	 * This returns an array of the allowed filters based on the type of object we are working
	 * with, this is used to display the 'filter' sidebar stuff, must be called post browse stuff
	 */
	public static function get_allowed_filters() {

		switch ($_SESSION['browse']['type']) {
			case 'album':
				$valid_array = array('show_art','alpha_match');
			break;
			case 'artist':
			case 'genre':
			case 'song':
			case 'live_stream':
				$valid_array = array('alpha_match');
			break;
			case 'playlist':
				$valid_array = array('alpha_match');
				if (Access::check('interface','50')) {
					array_push($valid_array,'playlist_type');
				}
			break;
			default:
				$valid_array = array();
			break;
		} // switch on the browsetype

		return $valid_array;

	} // get_allowed_filters

	/**
 	 * set_type
	 * This sets the type of object that we want to browse by
	 * we do this here so we only have to maintain a single whitelist
	 * and if I want to change the location I only have to do it here
	 */
	public static function set_type($type) {

		switch($type) {
			case 'user':
			case 'playlist':
			case 'playlist_song':
			case 'song':
			case 'flagged':
			case 'catalog':
			case 'album':
			case 'artist':
			case 'genre':
			case 'shoutbox':
			case 'live_stream':
				// Set it
				self::$type = $type;
				self::load_start();

				// Save it in the session
				$_SESSION['browse']['type'] = $type;


				// Resets the simple browse
				self::set_simple_browse(0);
				self::set_static_content(0);
				self::reset_supplemental_objects();
			break;
			default:
				// Rien a faire
			break;
		} // end type whitelist

	} // set_type

	/**
	 * set_sort
	 * This sets the current sort(s)
	 */
	public static function set_sort($sort,$order='') {


		switch ($_SESSION['browse']['type']) {
			case 'playlist_song':
			case 'song':
				$valid_array = array('title','year','track','time');
			break;
			case 'artist':
				$valid_array = array('name');
			break;
			case 'genre':
				$valid_array = array('name');
			break;
			case 'album':
				$valid_array = array('name','year');
			break;
			case 'playlist':
				$valid_array = array('name','user');
			break;
			case 'shoutbox':
				$valid_array = array('date','user','sticky');
			break;
			case 'live_stream':
				$valid_array = array('name','call_sign','frequency');
			break;
                        case 'user':
                                $valid_array = array('fullname','username','last_seen','create_date');
                        break;
		} // end switch

		// If it's not in our list, smeg off!
		if (!in_array($sort,$valid_array)) {
			return false;
		}

		if ($order) {
			$order = ($order == 'DESC') ? 'DESC' : 'ASC';
			$_SESSION['browse']['sort'] = array();
			$_SESSION['browse']['sort'][$sort] = $order;
		}
		elseif ($_SESSION['browse']['sort'][$sort] == 'DESC') {
			// Reset it till I can figure out how to interface the hotness
			$_SESSION['browse']['sort'] = array();
			$_SESSION['browse']['sort'][$sort] = 'ASC';
		}
		else {
			// Reset it till I can figure out how to interface the hotness
			$_SESSION['browse']['sort'] = array();
			$_SESSION['browse']['sort'][$sort] = 'DESC';
		}

		self::resort_objects();

	} // set_sort

	/**
	 * set_start
	 * This sets the start point for our show functions
	 * We need to store this in the session so that it can be pulled
	 * back, if they hit the back button
	 */
	public static function set_start($start) {

		if (!self::$static_content) {
			$_SESSION['browse'][self::$type]['start'] = intval($start);
		}
		self::$start = intval($start);

	} // set_start

	/**
	 * set_simple_browse
	 * This sets the current browse object to a 'simple' browse method
	 * which means use the base query provided and expand from there
	 */
	public static function set_simple_browse($value) {

		$value = make_bool($value);
		self::$simple_browse = $value;

		$_SESSION['browse']['simple'] = $value;

	} // set_simple_browse

	/**
	 * set_static_content
	 * This sets true/false if the content of this browse
	 * should be static, if they are then content filtering/altering
	 * methods will be skipped
	 */
	public static function set_static_content($value) {

		$value = make_bool($value);
		self::$static_content = $value;

		// We want to start at 0 it's static
		if ($value) {
			self::set_start('0');
		}

		$_SESSION['browse']['static'] = $value;

	} // set_static_content

	/**
	 * load_start
	 * This returns a stored start point for the browse mojo
	 */
	public static function load_start() {

		self::$start = intval($_SESSION['browse'][self::$type]['start']);

	} // end load_start

	/**
	 * get_saved
	 * This looks in the session for the saved
	 * stuff and returns what it finds
	 */
	public static function get_saved() {

		$objects = $_SESSION['browse']['save'];

		return $objects;

	} // get_saved

	/**
	 * get_objects
	 * This gets an array of the ids of the objects that we are
	 * currently browsing by it applies the sql and logic based
	 * filters
	 */
	public static function get_objects() {

		// First we need to get the SQL statement we are going to run
		// This has to run against any possible filters (dependent on type)
		$sql = self::get_sql();
		$db_results = Dba::query($sql);

		$results = array();
		while ($data = Dba::fetch_assoc($db_results)) {
			$results[] = $data;
		}

		$results = self::post_process($results);
		$filtered = array();
		foreach ($results as $data) {
			// Make sure that this object passes the logic filter
			if (self::logic_filter($data['id'])) {
				$filtered[] = $data['id'];
			}
		} // end while

		// Save what we've found and then return it
		self::save_objects($filtered);

		return $filtered;

	} // get_objects

	/**
	 * get_supplemental_objects
	 * This returns an array of 'class','id' for additional objects that need to be
	 * created before we start this whole browsing thing
	 */
	public static function get_supplemental_objects() {

		$objects = $_SESSION['browse']['supplemental'];

		if (!is_array($objects)) { $objects = array(); }

		return $objects;

	} // get_supplemental_objects

	/**
	 * add_supplemental_object
	 * This will add a suplemental object that has to be created
	 */
	public static function add_supplemental_object($class,$uid) {

		$_SESSION['browse']['supplemental'][$class] = intval($uid);

		return true;

	} // add_supplemental_object

	/**
	 * get_base_sql
	 * This returns the base SQL (select + from) for the different types
	 */
	private static function get_base_sql() {

                // Get our base SQL must always return ID
		$includetags = (is_array($_SESSION['browse']['filter']['tag'])
		  && sizeof($_SESSION['browse']['filter']['tag']));
		$megajoin = '';
		if ($includetags)
		  $megajoin = ', tags.id as tagid';
		$megajoin .= ' FROM song, artist, album ';
		if ($includetags)
		  $megajoin.= ', tags, tag_map ';
		$megajoin .= 'WHERE song.album = album.id AND
		  song.artist = artist.id AND ';
		if ($includetags)
		  $megajoin .= ' tag_map.tag_id = tags.id AND ';
	       $w = " WHERE 1=1 AND ";
                switch ($_SESSION['browse']['type']) {
                        case 'album':
                                $sql = "SELECT DISTINCT `album`.`id` "
				.$megajoin;
                        break;
                        case 'artist':
                                $sql = "SELECT DISTINCT `artist`.`id` "
				.$megajoin;
                        break;
                        case 'genre':
                                $sql = "SELECT `genre`.`id` FROM `genre` ".$w;
                        break;
                        case 'user':
                                $sql = "SELECT `user`.`id` FROM `user` ".$w;
                        break;
                        case 'live_stream':
                                $sql = "SELECT `live_stream`.`id` FROM `live_stream` ".$w;
                        break;
                        case 'playlist':
                                $sql = "SELECT `playlist`.`id` FROM `playlist` ".$w;
                        break;
			case 'flagged':
				$sql = "SELECT `flagged`.`id` FROM `flagged` "
				.$w;
			break;
			case 'shoutbox':
				$sql = "SELECT `user_shout`.`id` FROM `user_shout` ".$w;
			break;
			case 'playlist_song':
                        case 'song':
                        default:
                                $sql = "SELECT DISTINCT `song`.`id` ".$megajoin;
                        break;
                } // end base sql

		return $sql;

	} // get_base_sql

	/**
	 * get_sql
	 * This returns the sql statement we are going to use this has to be run
	 * every time we get the objects because it depends on the filters and the
	 * type of object we are currently browsing
	 */
	public static function get_sql() {

		$sql = self::get_base_sql();

		// No sense to go further if we don't have filters
		if (is_array($_SESSION['browse']['filter'])) {

			// Foreach the filters and see if any of them can be applied
			// as part of a where statement in this sql (type dependent)
			$where_sql = "";

			foreach ($_SESSION['browse']['filter'] as $key=>$value) {
				$where_sql .= self::sql_filter($key,$value);
			} // end foreach
			$sql .= $where_sql;
		} // if filters
		$sql = rtrim($sql,'AND ');
		// Now Add the Order
		$order_sql = " ORDER BY ";

		// If we don't have a sort, then go ahead and return it now
		if (!is_array($_SESSION['browse']['sort'])) { return $sql; }

		foreach ($_SESSION['browse']['sort'] as $key=>$value) {
			$order_sql .= self::sql_sort($key,$value);
		}
		// Clean her up
		$order_sql = rtrim($order_sql,"ORDER BY ");
		$order_sql = rtrim($order_sql,",");

		$sql = $sql . $order_sql;
		return $sql;

	} // get_sql

	/**
  	 * post_process
	 * This does some additional work on the results that we've received before returning them
	 */
	private static function post_process($results) {

		$tags = $_SESSION['browse']['filter']['tag'];

		if (!is_array($tags) || sizeof($tags) < 2) {
			return $results;
		}
		$cnt = sizeof($tags);
		$ar = array();

		foreach($results as $row) {
			$ar[$row['id']]++;
		}

		$res = array();

		foreach($ar as $k=>$v) {
			if ($v >= $cnt) {
				$res[] = array('id' => $k);
			}
		} // end foreach

		return $res;

	} // post_process

	/**
	 * sql_filter
	 * This takes a filter name and value and if it is possible
	 * to filter by this name on this type returns the approiate sql
	 * if not returns nothing
	 */
	private static function sql_filter($filter,$value) {

		$filter_sql = '';
		//tag
		if ($filter == 'tag' && (
		  $_SESSION['browse']['type'] == 'song'
		  || $_SESSION['browse']['type'] == 'artist'
		  || $_SESSION['browse']['type'] == 'album'
		  )) {
		   if (is_array($value) && sizeof($value))
		     $vals = '(' . implode(',',$value) . ')';
		   else if (is_integer($value))
		     $vals = '('.$value.')';
		   else return '';
		   $or_sql = '';
		   $object_type = $_SESSION['browse']['type'];
		   if ($object_type == 'artist' || $object_type == 'album')
		     $or_sql=" or (tag_map.object_id = song.id AND
		   tag_map.object_type='song' )";
		   if ($object_type == 'artist')
		     $or_sql.= " or (tag_map.object_id = album.id AND
		   tag_map.object_type='album' )";
		   $filter_sql = " `tags`.`id` in  $vals AND
		     (($object_type.id = `tag_map`.`object_id` AND tag_map.object_type='$object_type')  $or_sql) AND ";
		  }
		if ($_SESSION['browse']['type'] == 'song') {
			switch($filter) {
				case 'alpha_match':
					$filter_sql = " `song`.`title` LIKE '" . Dba::escape($value) . "%' AND ";
				break;
				case 'unplayed':
					$filter_sql = " `song`.`played`='0' AND ";
				break;
			        case 'album':
				  if ($value)
				    $filter_sql = " `album`.`id` = '".
				      Dba::escape($value) . "' AND ";
				break;
				case 'artist':
				  if ($value)
				    $filter_sql = " `artist`.`id` = '".
				      Dba::escape($value) . "' AND ";
				break;
				default:
					// Rien a faire
				break;
			} // end list of sqlable filters
		} // if it is a song
		elseif ($_SESSION['browse']['type'] == 'album') {
			switch($filter) {
				case 'alpha_match':
					$filter_sql = " `album`.`name` LIKE '" . Dba::escape($value) . "%' AND ";
				break;
				case 'min_count':

				break;
			        case 'artist':
				  if ($value)
				    $filter_sql = " `artist`.`id` = '".
				      Dba::escape($value) . "' AND ";
				break;
				default:
					// Rien a faire
				break;
			}
		} // end album
		elseif ($_SESSION['browse']['type'] == 'artist') {
			switch($filter) {
				case 'alpha_match':
					$filter_sql = " `artist`.`name` LIKE '" . Dba::escape($value) . "%' AND ";
				break;
				default:
					// Rien a faire
				break;
			} // end filter
		} // end artist
		elseif ($_SESSION['browse']['type'] == 'genre') {
			switch ($filter) {
				case 'alpha_match':
					$filter_sql = " `genre`.`name` LIKE '" . Dba::escape($value) . "%' AND ";
				break;
				default:
					// Rien a faire
				break;
			} // end filter
		} // end if genre
		elseif ($_SESSION['browse']['type'] == 'live_stream') {
			switch ($filter) {
				case 'alpha_match':
					$filter_sql = " `live_stream`.`name` LIKE '" . Dba::escape($value) . "%' AND ";
				break;
				default:
					// Rien a faire
				break;
			} // end filter
		} // end live_stream
		elseif ($_SESSION['browse']['type'] == 'playlist') {
			switch ($filter) {
				case 'alpha_match':
					$filter_sql = " `playlist`.`name` LIKE '" . Dba::escape($value) . "%' AND ";
				break;
				case 'playlist_type':
					$user_id = intval($GLOBALS['user']->id);
					$filter_sql = " (`playlist`.`type` = 'public' OR `playlist`.`user`='$user_id') AND ";
				break;
				default;
					// Rien a faire
				break;
			} // end filter
		} // end playlist

		return $filter_sql;

	} // sql_filter

	/**
	 * logic_filter
	 * This runs the filters that we can't easily apply
	 * to the sql so they have to be done after the fact
	 * these should be limited as they are often intensive and
	 * require additional queries per object... :(
	 */
	private static function logic_filter($object_id) {

		return true;

	} // logic_filter

	/**
	 * sql_sort
	 * This builds any order bys we need to do
	 * to sort the results as best we can, there is also
	 * a logic based sort that will come later as that's
	 * a lot more complicated
	 */
	private static function sql_sort($field,$order) {

		if ($order != 'DESC') { $order == 'ASC'; }

		switch ($_SESSION['browse']['type']) {
			case 'song':
				switch($field) {
					case 'title';
						$sql = "`song`.`title`";
					break;
					case 'year':
						$sql = "`song`.`year`";
					break;
					case 'time':
						$sql = "`song`.`time`";
					break;
					case 'track':
						$sql = "`song`.`track`";
					break;
					default:
						// Rien a faire
					break;
				} // end switch
			break;
			case 'album':
				switch($field) {
					case 'name':
						$sql = "`album`.`name` $order, `album`.`disk`";
					break;
					case 'year':
						$sql = "`album`.`year`";
					break;
				} // end switch
			break;
			case 'artist':
				switch ($field) {
					case 'name':
						$sql = "`artist`.`name`";
					break;
				} // end switch
			break;
			case 'playlist':
				switch ($field) {
					case 'type':
						$sql = "`playlist`.`type`";
					break;
					case 'name':
						$sql = "`playlist`.`name`";
					break;
					case 'user':
						$sql = "`playlist`.`user`";
					break;
				} // end switch
			break;
			case 'live_stream':
				switch ($field) {
					case 'name':
						$sql = "`live_stream`.`name`";
					break;
					case 'call_sign':
						$sql = "`live_stream`.`call_sign`";
					break;
					case 'frequency':
						$sql = "`live_stream`.`frequency`";
					break;
				} // end switch
			break;
			case 'genre':
				switch ($field) {
					case 'name':
						$sql = "`genre`.`name`";
					break;
				} // end switch
			break;
                        case 'user':
                                switch ($field) {
                                        case 'username':
                                                $sql = "`user`.`username`";
                                        break;
                                        case 'fullname':
                                                $sql = "`user`.`fullname`";
                                        break;
                                        case 'last_seen':
                                                $sql = "`user`.`last_seen`";
                                        break;
                                        case 'create_date':
                                                $sql = "`user`.`create_date`";
                                        break;
                                } // end switch
                        break;
			default:
				// Rien a faire
			break;
		} // end switch

		if ($sql) { $sql_sort = "$sql $order,"; }

		return $sql_sort;

	} // sql_sort

	/**
	 * show_objects
	 * This takes an array of objects
	 * and requires the correct template based on the
	 * type that we are currently browsing
	 */
	public static function show_objects($object_ids='', $ajax=false) {

		$object_ids = $object_ids ? $object_ids : self::get_saved();

		// Reset the total items
		self::$total_objects = count($object_ids);

		// Limit is based on the users preferences
		$limit = Config::get('offset_limit') ? Config::get('offset_limit') : '25';
		$all_ids = $object_ids;
		if (count($object_ids) > self::$start) {
			$object_ids = array_slice($object_ids,self::$start,$limit);
		}

		// Format any matches we have so we can show them to the masses
		$match = $_SESSION['browse']['filter']['alpha_match'] ? ' (' . $_SESSION['browse']['filter']['alpha_match'] . ')' : '';

		// Set the correct classes based on type
    		$class = "box browse_".$_SESSION['browse']['type'];

		// Load any additional object we need for this
		$extra_objects = self::get_supplemental_objects();
		foreach ($extra_objects as $class_name => $id) {
			${$class_name} = new $class_name($id);
		}

		if (!$ajax && Tag::validate_type($_SESSION['browse']['type'])) {
			$tagcloudList = Tag::get_many_tags($_SESSION['browse']['type'],  $all_ids);
			require_once Config::get('prefix') . '/templates/show_tagcloud.inc.php';
		}

		Ajax::start_container('browse_content');
		// Switch on the type of browsing we're doing
		switch ($_SESSION['browse']['type']) {
			case 'song':
				show_box_top(_('Songs') . $match, $class);
				Song::build_cache($object_ids);
				require_once Config::get('prefix') . '/templates/show_songs.inc.php';
				show_box_bottom();
			break;
			case 'album':
				show_box_top(_('Albums') . $match, $class);
				Album::build_cache($object_ids);
				require_once Config::get('prefix') . '/templates/show_albums.inc.php';
				show_box_bottom();
			break;
			case 'user':
				show_box_top(_('Manage Users') . $match, $class);
				require_once Config::get('prefix') . '/templates/show_users.inc.php';
				show_box_bottom();
			break;
			case 'artist':
				show_box_top(_('Artists') . $match, $class);
				Artist::build_cache($object_ids);
				require_once Config::get('prefix') . '/templates/show_artists.inc.php';
				show_box_bottom();
			break;
			case 'live_stream':
				show_box_top(_('Radio Stations') . $match, $class);
				require_once Config::get('prefix') . '/templates/show_live_streams.inc.php';
				show_box_bottom();
			break;
			case 'playlist':
				show_box_top(_('Playlists') . $match, $class);
				require_once Config::get('prefix') . '/templates/show_playlists.inc.php';
				show_box_bottom();
			break;
			case 'playlist_song':
				show_box_top(_('Playlist Songs') . $match,$class);
				require_once Config::get('prefix') . '/templates/show_playlist_songs.inc.php';
				show_box_bottom();
			break;
			case 'catalog':
				show_box_top(_('Catalogs'), $class);
				require_once Config::get('prefix') . '/templates/show_catalogs.inc.php';
				show_box_bottom();
			break;
			case 'shoutbox':
				show_box_top(_('Shoutbox Records'),$class);
				require_once Config::get('prefix') . '/templates/show_manage_shoutbox.inc.php';
				show_box_bottom();
			break;
			case 'flagged':
				show_box_top(_('Flagged Records'),$class);
				require_once Config::get('prefix') . '/templates/show_flagged.inc.php';
				show_box_bottom();
			break;
			default:
				// Rien a faire
			break;
		} // end switch on type

		Ajax::end_container();

	} // show_object

	/**
	 * save_objects
	 * This takes the full array of object ides, often passed into show and then
	 * if nessecary it saves them into the session
	 */
	public static function save_objects($object_ids) {

		// save these objects
		$_SESSION['browse']['save'] = $object_ids;
		self::$total_objects = count($object_ids);
		return true;

	} // save_objects

	/**
	 * resort_objects
	 * This takes the existing objects, looks at the current
	 * sort method and then re-sorts them This is internally
	 * called by the set_sort() function
	 */
	private static function resort_objects() {

		// There are two ways to do this.. the easy way...
		// and the vollmer way, hopefully we don't have to
		// do it the vollmer way
		if (self::$simple_browse) {
			$sql = self::get_base_sql();
		}
		else {
			// First pull the objects
			$objects = self::get_saved();

			// If there's nothing there don't do anything
			if (!count($objects)) { return false; }
			$type = $_SESSION['browse']['type'];
			$where_sql .= "`$type`.`id` IN (";

			foreach ($objects as $object_id) {
				$object_id = Dba::escape($object_id);
				$where_sql .= "'$object_id',";
			}
			$where_sql = rtrim($where_sql,',');

			$where_sql .= ")";

			$sql = self::get_base_sql();
			$sql .= $where_sql;
		}


		$order_sql = "ORDER BY ";

                foreach ($_SESSION['browse']['sort'] as $key=>$value) {
                        $order_sql .= self::sql_sort($key,$value);
                }
                // Clean her up
                $order_sql = rtrim($order_sql,"ORDER BY ");
                $order_sql = rtrim($order_sql,",");
                $sql = $sql . $order_sql;

		$db_results = Dba::query($sql);

		while ($row = Dba::fetch_assoc($db_results)) {
			$results[] = $row['id'];
		}

		self::save_objects($results);

		return true;

	} // resort_objects

	/**
	 * _auto_init
	 * this function reloads information back from the session
	 * it is called on creation of the class
	 */
	public static function _auto_init() {

		self::$simple_browse = make_bool($_SESSION['browse']['simple']);
		self::$static_content = make_bool($_SESSION['browse']['static']);
		self::$type = $_SESSION['browse']['type'];
		self::$start = intval($_SESSION['browse'][self::$type]['start']);

	} // _auto_init

	public static function set_filter_from_request($r)
	{
	  foreach ($r as $k=>$v) {
	    //reinterpret v as a list of int
	    $vl = explode(',', $v);
	    $ok = 1;
	    foreach($vl as $i) {
	      if (!is_numeric($i)) {
		$ok = 0;
		break;
	      }
	    }
	    if ($ok)
	      if (sizeof($vl) == 1)
	        Browse::set_filter($k, $vl[0]);
	      else
	        Browse::set_filter($k, $vl);
	  }
	}

} // browse