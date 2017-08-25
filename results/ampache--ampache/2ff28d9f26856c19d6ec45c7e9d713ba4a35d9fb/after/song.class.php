<?php
/*

 Copyright (c) 2001 - 2007 Ampache.org
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

class Song {

	/* Variables from DB */
	public $id;
	public $file;
	public $album; // album.id (Int)
	public $artist; // artist.id (Int)
	public $title;
	public $year;
	public $bitrate;
	public $rate;
	public $mode;
	public $size;
	public $time;
	public $track;
	public $genre; // genre.id (Int)
	public $type;
	public $mime;
	public $played;
	public $enabled;
	public $addition_time;
	public $update_time;

	/* Setting Variables */
	public $_transcoded = false;
	public $_fake = false; // If this is a 'construct_from_array' object

	/**
	 * Constructor
	 * Song class, for modifing a song.
	 */
	public function __construct($id='') {

		/* Assign id for use in get_info() */
		$this->id = intval($id);

		if (!$this->id) { return false; }

		/* Get the information from the db */
		if ($info = $this->_get_info()) {

			foreach ($info as $key=>$value) {
				$this->$key = $value;
			}
			// Format the Type of the song
			$this->format_type();
		}

		return true;

	} // constructor

	/*!
		@function _get_info
		@discussion get's the vars for $this out of the database
		@param $this->id	Taken from the object
	*/
	private function _get_info() {

		/* Grab the basic information from the catalog and return it */
		$sql = "SELECT song.id,file,catalog,album,year,artist,".
			"title,bitrate,rate,mode,size,time,track,genre,played,song.enabled,update_time,".
			"addition_time FROM `song` WHERE `song`.`id` = '$this->id'";

		$db_results = Dba::query($sql);

		$results = Dba::fetch_assoc($db_results);

		return $results;

	} // _get_info

	/**
 	 * _get_ext_info
	 * This function gathers information from the song_ext_info table and adds it to the
	 * current object
	 */
	public function _get_ext_info() {

		$sql = "SELECT * FROM song_data WHERE `song_id`='" . Dba::escape($this->id) . "'";
		$db_results = Dba::query($sql);

		$results = Dba::fetch_assoc($db_results);

		return $results;

	} // _get_ext_info

	/**
 	 * fill_ext_info
	 * This calls the _get_ext_info and then sets the correct vars
	 */
	public function fill_ext_info() {

		$info = $this->_get_ext_info();

		foreach ($info as $key=>$value) {
			if ($key != 'song_id') {
				$this->$key = $value;
			}
		} // end foreach

	} // fill_ext_info

	/**
	 * format_type
	 * gets the type of song we are trying to
	 * play, used to set mime headers and to trick
	 * players into playing them correctly
	 */
	public function format_type($override='') {

		// If we pass an override for downsampling or whatever then use it
		if (!empty($override)) {
			$this->type = $override;
		}
		else {
			preg_match('/\.([A-Za-z0-9]+)$/', $this->file,$results);
			$this->type = strtolower($results['1']);
		}

		switch ($this->type) {
			case 'spx':
			case 'ogg':
				$this->mime = "application/ogg";
			break;
			case 'wma':
			case 'asf':
				$this->mime = "audio/x-ms-wma";
			break;
			case 'mp3':
			case 'mpeg3':
				$this->mime = "audio/mpeg";
			break;
			case 'rm':
			case 'ra':
				$this->mime = "audio/x-realaudio";
			break;
			case 'flac';
				$this->mime = "audio/x-flac";
			break;
			case 'wv':
				$this->mime = 'audio/x-wavpack';
			break;
			case 'aac':
			case 'mp4':
			case 'm4a':
				$this->mime = "audio/mp4";
			break;
			case 'mpc':
				$this->mime = "audio/x-musepack";
				$this->type = "MPC";
			break;
			default:
				$this->mime = "audio/mpeg";
			break;
		}

		return true;

	} // format_type

	/*!
		@function get_album_songs
		@discussion gets an array of song objects based on album
	*/
	function get_album_songs($album_id) {

		$sql = "SELECT id FROM song WHERE album='$album_id'";
		$db_results = mysql_query($sql, dbh());

		while ($r = mysql_fetch_object($db_results)) {
			$results[] = new Song($r->id);
		}

		return $results;

	} // get_album_songs

	/**
	 * get_album_name
	 * gets the name of $this->album, allows passing of id
	 */
	function get_album_name($album_id=0) {

		if (!$album_id) { $album_id = $this->album; }

		$sql = "SELECT `name`,`prefix` FROM `album` WHERE `id`='" . Dba::escape($album_id) . "'";
		$db_results = Dba::query($sql);

		$results = Dba::fetch_assoc($db_results);

		if ($results['prefix']) {
			return $results['prefix'] . " " .$results['name'];
		}
		else {
			return $results['name'];
		}

	} // get_album_name

	/**
	 * get_artist_name
	 * gets the name of $this->artist, allows passing of id
	 */
	function get_artist_name($artist_id=0) {

		if (!$artist_id) { $artist_id = $this->artist; }

		$sql = "SELECT name,prefix FROM artist WHERE id='" . Dba::escape($artist_id) . "'";
		$db_results = Dba::query($sql);

		$results = Dba::fetch_assoc($db_results);

		if ($results['prefix']) {
			return $results['prefix'] . " " . $results['name'];
		}
		else {
			return $results['name'];
		}

	} // get_album_name

	/**
	 * get_genre_name
	 * gets the name of the genre, allow passing of a specified
	 * id
	 */
	function get_genre_name($genre_id=0) {

		if (!$genre_id) { $genre_id = $this->genre; }

		$sql = "SELECT name FROM genre WHERE id='" . Dba::escape($genre_id) . "'";
		$db_results = Dba::query($sql);

		$results = Dba::fetch_assoc($db_results);

		return $results['name'];

	} // get_genre_name

	/**
	 * get_flags
	 * This gets any flag information this song may have, it always
	 * returns an array as it may be possible to have more then
	 * one flag
	 */
	function get_flags() {

		$sql = "SELECT id,flag,comment FROM flagged WHERE object_type='song' AND object_id='$this->id'";
		$db_results = mysql_query($sql, dbh());

		$results = array();

		while ($r = mysql_fetch_assoc($db_results)) {
			$results[] = $r;
		}

		return $results;

	} // get_flag

	/**
	 * has_flag
	 * This just returns true or false depending on if this song is flagged for something
	 * We don't care what so we limit the SELECT to 1
	 */
	public function has_flag() {

		$sql = "SELECT `id` FROM `flagged` WHERE `object_type`='song' AND `object_id`='$this->id' LIMIT 1";
		$db_results = Dba::query($sql);

		if (Dba::fetch_assoc($db_results)) {
			return true;
		}

		return false;

	} // has_flag

	/**
	 * set_played
	 * this checks to see if the current object has been played
	 * if not then it sets it to played
	 */
	public function set_played() {

		if ($this->played) {
			return true;
		}

		/* If it hasn't been played, set it! */
		self::update_played('1',$this->id);

		return true;

	} // set_played

	/**
	 * compare_song_information
	 * this compares the new ID3 tags of a file against
	 * the ones in the database to see if they have changed
	 * it returns false if nothing has changes, or the true
	 * if they have. Static because it doesn't need this
	 */
	public static function compare_song_information($song,$new_song) {

		// Remove some stuff we don't care about
		unset($song->catalog,$song->played,$song->enabled,$song->addition_time,$song->update_time,$song->type);

		$string_array = array('title','comment','lyrics');

		// Pull out all the currently set vars
		$fields = get_object_vars($song);

		// Foreach them
		foreach ($fields as $key=>$value) {
			// If it's a stringie thing
			if (in_array($key,$string_array)) {
				if (trim(stripslashes($song->$key)) != trim(stripslashes($new_song->$key))) {
					$array['change'] = true;
					$array['element'][$key] = 'OLD: ' . $song->$key . ' <---> ' . $new_song->$key;
				}
			} // in array of stringies

			else {
				if ($song->$key != $new_song->$key) {
					$array['change'] = true;
					$array['element'][$key] = '' . $song->$key . ' <---> ' . $new_song->$key;
				}
			} // end else

		} // end foreach

		if ($array['change']) {
			debug_event('song-diff',print_r($array['element'],1),'5','ampache-catalog');
		}

		return $array;

	} // compare_song_information

	/**
	 * update
	 * This takes a key'd array of data does any cleaning it needs to
	 * do and then calls the helper functions as needed. This will also
	 * cause the song to be flagged
	 */
	public function update($data) {

		foreach ($data as $key=>$value) {
			switch ($key) {
				case 'title':
				case 'track':
					// Check to see if it needs to be updated
					if ($value != $this->$key) {
						$function = 'update_' . $key;
						self::$function($value,$this->id);
						$this->$key = $value;
						$updated = 1;
					}
				break;
				case 'artist':
				case 'album':
				case 'genre':
					if ($value != $this->$key) {
						if ($value == -1) {
							// Add new data
							$fn = "check_$key";
							$value = Catalog::$fn($data["{$key}_name"]);
						}
						if ($value) {
							$fn = "update_$key";
							self::$fn($value, $this->id);
							$this->$key = $value;
							$updated = 1;
						}
					}
				break;
				default:
					// Rien a faire
				break;
			} // end whitelist
		} // end foreach

		// If a field was changed then we need to flag this mofo
		if ($updated) {
			Flag::add($this->id,'song','retag','Interface Update');
		}

		return true;

	} // update

	/**
	 * update_song
	 * this is the main updater for a song it actually
	 * calls a whole bunch of mini functions to update
	 * each little part of the song... lastly it updates
	 * the "update_time" of the song
	 */
	public static function update_song($song_id, $new_song) {

		$title 		= Dba::escape($new_song->title);
		$bitrate	= Dba::escape($new_song->bitrate);
		$rate		= Dba::escape($new_song->rate);
		$mode		= Dba::escape($new_song->mode);
		$size		= Dba::escape($new_song->size);
		$time		= Dba::escape($new_song->time);
		$track		= Dba::escape($new_song->track);
		$artist		= Dba::escape($new_song->artist);
		$genre		= Dba::escape($new_song->genre);
		$album		= Dba::escape($new_song->album);
		$year		= Dba::escape($new_song->year);
		$song_id	= Dba::escape($song_id);
		$update_time	= time();


		$sql = "UPDATE `song` SET `album`='$album', `year`='$year', `artist`='$artist', " .
			"`title`='$title', `bitrate`='$bitrate', `rate`='$rate', `mode`='$mode', " .
			"`size`='$size', `time`='$time', `track`='$track', `genre`='$genre', " .
			"`update_time`='$update_time' WHERE `id`='$song_id'";
		$db_results = Dba::query($sql);


		$comment 	= Dba::escape($new_song->comment);
		$language	= Dba::escape($new_song->language);
		$lyrics		= Dba::escape($new_song->lyrics);

		$sql = "UPDATE `song_data` SET `lyrics`='$lyrics', `language`='$language', `comment`='$comment' " .
			"WHERE `song_id`='$song_id'";
		$db_results = Dba::query($sql);

	} // update_song

	/**
	 * update_year
	 * update the year tag
	 */
	public static function update_year($new_year,$song_id) {

		self::_update_item('year',$new_year,$song_id,'50');

	} // update_year

	/**
	 * update_language
	 * This updates the language tag of the song
	 */
	public static function update_language($new_lang,$song_id) {

		self::_update_ext_item('language',$new_lang,$song_id,'50');

	} // update_language

	/**
	 * update_comment
	 * updates the comment field
	 */
	public static function update_comment($new_comment,$song_id) {

		self::_update_ext_item('comment',$new_comment,$song_id,'50');

	} // update_comment

	/**
 	 * update_lyrics
	 * updates the lyrics field
	 */
	public static function update_lyrics($new_lyrics,$song_id) {

		self::_update_ext_item('lyrics',$new_lyrics,$song_id,'50');

	} // update_lyrics

	/**
	 * update_title
	 * updates the title field
	 */
	public static function update_title($new_title,$song_id) {

		self::_update_item('title',$new_title,$song_id,'50');

	} // update_title

	/**
	 * update_bitrate
	 * updates the bitrate field
	 */
	public static function update_bitrate($new_bitrate,$song_id) {

		self::_update_item('bitrate',$new_bitrate,$song_id,'50');

	} // update_bitrate

	/**
	 * update_rate
	 * updates the rate field
	 */
	public static function update_rate($new_rate,$song_id) {

		self::_update_item('rate',$new_rate,$song_id,'50');

	} // update_rate

	/**
	 * update_mode
	 * updates the mode field
	 */
	public static function update_mode($new_mode,$song_id) {

		self::_update_item('mode',$new_mode,$song_id,'50');

	} // update_mode

	/**
	 * update_size
	 * updates the size field
	 */
	public static function update_size($new_size,$song_id) {

		self::_update_item('size',$new_size,$song_id,'50');

	} // update_size

	/**
	 * update_time
	 * updates the time field
	 */
	public static function update_time($new_time,$song_id) {

		self::_update_item('time',$new_time,$song_id,'50');

	} // update_time

	/**
	 * update_track
	 * this updates the track field
	 */
	public static function update_track($new_track,$song_id) {

		self::_update_item('track',$new_track,$song_id,'50');

	} // update_track

	/**
	 * update_artist
	 * updates the artist field
	 */
	public static function update_artist($new_artist,$song_id) {

		self::_update_item('artist',$new_artist,$song_id,'50');

	} // update_artist

	/**
	 * update_genre
	 * updates the genre field
	 */
	public static function update_genre($new_genre,$song_id) {

		self::_update_item('genre',$new_genre,$song_id,'50');

	} // update_genre

	/**
	 * update_album
	 * updates the album field
	 */
	public static function update_album($new_album,$song_id) {

		self::_update_item('album',$new_album,$song_id,'50');

	} // update_album

	/**
	 * update_utime
	 * sets a new update time
	 */
	public static function update_utime($song_id,$time=0) {

		if (!$time) { $time = time(); }

		self::_update_item('update_time',$time,$song_id,'75');

	} // update_utime

	/**
	 * update_played
	 * sets the played flag
	 */
	public static function update_played($new_played,$song_id) {

		self::_update_item('played',$new_played,$song_id,'25');

	} // update_played

	/**
	 * update_enabled
	 * sets the enabled flag
	 */
	public static function update_enabled($new_enabled,$song_id) {

		self::_update_item('enabled',$new_enabled,$song_id,'75');

	} // update_enabled

	/**
	 * _update_item
	 * This is a private function that should only be called from within the song class.
	 * It takes a field, value song id and level. first and foremost it checks the level
	 * against $GLOBALS['user'] to make sure they are allowed to update this record
	 * it then updates it and sets $this->{$field} to the new value
	 */
	private static function _update_item($field,$value,$song_id,$level) {

		/* Check them Rights! */
		if (!Access::check('interface',$level)) { return false; }

		/* Can't update to blank */
		if (!strlen(trim($value)) && $field != 'comment') { return false; }

		$value = Dba::escape($value);

		$sql = "UPDATE `song` SET `$field`='$value' WHERE `id`='$song_id'";
		$db_results = Dba::query($sql);

		return true;

	} // _update_item

	/**
	 * _update_ext_item
	 * This updates a song record that is housed in the song_ext_info table
	 * These are items that aren't used normally, and often large/informational only
	 */
	private static function _update_ext_item($field,$value,$song_id,$level) {

		/* Check them rights boy! */
		if (!Access::check('interface',$level)) { return false; }

		$value = Dba::escape($value);

		$sql = "UPDATE `song_data` SET `$field`='$value' WHERE `song_id`='$song_id'";
		$db_results = Dba::query($sql);

		return true;

	} // _update_ext_item

	/**
	 * format
	 * This takes the current song object
	 * and does a ton of formating on it creating f_??? variables on the current
	 * object
	 */
	public function format() {

		$this->fill_ext_info();

		// Format the filename
		preg_match("/^.*\/(.*?)$/",$this->file, $short);
		$this->f_file = htmlspecialchars($short[1]);

		// Format the album name
		$this->f_album_full = $this->get_album_name();
		$this->f_album = truncate_with_ellipsis($this->f_album_full,Config::get('ellipse_threshold_album'));

		// Format the artist name
		$this->f_artist_full = $this->get_artist_name();
		$this->f_artist = truncate_with_ellipsis($this->f_artist_full,Config::get('ellipse_threshold_artist'));

		// Format the title
		$this->f_title = truncate_with_ellipsis($this->title,Config::get('ellipse_threshold_title'));

		// Create Links for the different objects
		$this->f_link = "<a href=\"" . Config::get('web_path') . "/song.php?action=show_song&amp;song_id=" . $this->id . "\"> " . scrub_out($this->f_title) . "</a>";
		$this->f_album_link = "<a href=\"" . Config::get('web_path') . "/albums.php?action=show&amp;album=" . $this->album . "\"> " . scrub_out($this->f_album) . "</a>";
		$this->f_artist_link = "<a href=\"" . Config::get('web_path') . "/artists.php?action=show&amp;artist=" . $this->artist . "\"> " . scrub_out($this->f_artist) . "</a>";

		// Format the Bitrate
		$this->f_bitrate = intval($this->bitrate/1000) . "-" . strtoupper($this->mode);

		// Format Genre
		$this->f_genre = $this->get_genre_name();
		$this->f_genre_link = "<a href=\"" . Config::get('web_path') . "/genre.php?action=show_genre&amp;genre_id=" . $this->genre . "\">$this->f_genre</a>";

		// Format the Time
		$min = floor($this->time/60);
		$sec = sprintf("%02d", ($this->time%60) );
		$this->f_time = $min . ":" . $sec;

		// Format the track (there isn't really anything to do here)
		$this->f_track = $this->track;

		// Format the size
		$this->f_size = sprintf("%.2f",($this->size/1048576));

		return true;

	} // format

	/**
	 * format_pattern
	 * This reformates the song information based on the catalog
	 * rename patterns
	 */
	public function format_pattern() {

		$catalog = new Catalog($this->catalog);

	        $extension = ltrim(substr($this->file,strlen($this->file)-4,4),".");

	        /* Create the filename that this file should have */
	        $album  = $this->f_album_full;
	        $artist = $this->f_artist_full;
	        $genre  = $this->f_genre;
	        $track  = $this->track;
	        $title  = $this->title;
	        $year   = $this->year;

	        /* Start replacing stuff */
	        $replace_array = array('%a','%A','%t','%T','%y','%g','/','\\');
	        $content_array = array($artist,$album,$title,$track,$year,$genre,'-','-');

	        $rename_pattern = str_replace($replace_array,$content_array,$catalog->rename_pattern);

	        $rename_pattern = preg_replace("[\-\:\!]","_",$rename_pattern);

		$this->f_pattern	= $rename_pattern;
	        $this->f_file 		= $rename_pattern . "." . $extension;

	} // format_pattern

	/**
	 *       @function       get_rel_path
	 *       @discussion    returns the path of the song file stripped of the catalog path
	 *			used for mpd playback
	 */
	function get_rel_path($file_path=0,$catalog_id=0) {

		if (!$file_path) {
			$info = $this->_get_info();
			$file_path = $info->file;
		}
		if (!$catalog_id) {
			$catalog_id = $info->catalog;
		}
	        $catalog = new Catalog( $catalog_id );
                $info = $catalog->_get_info();
                $catalog_path = $info->path;
		$catalog_path = rtrim($catalog_path, "/");
                return( str_replace( $catalog_path . "/", "", $file_path ) );

	} // get_rel_path


	/*!
		@function fill_info
		@discussion this takes the $results from getid3 and attempts to fill
			as much information as possible from the file name using the
			pattern set in the current catalog
	*/
	function fill_info($results,$pattern,$catalog_id,$key) {

		$filename = $this->get_rel_path($results['file'],$catalog_id);

		if (!strlen($results[$key]['title'])) {
			$results[$key]['title']		= $this->get_info_from_filename($filename,$pattern,"%t");
		}
		if (!strlen($results[$key]['track'])) {
			$results[$key]['track']		= $this->get_info_from_filename($filename,$pattern,"%T");
		}
		if (!strlen($results[$key]['year'])) {
			$results[$key]['year']		= $this->get_info_from_filename($filename,$pattern,"%y");
		}
		if (!strlen($results[$key]['album'])) {
			$results[$key]['album']		= $this->get_info_from_filename($filename,$pattern,"%A");
		}
		if (!strlen($results[$key]['artist'])) {
			$results[$key]['artist']	= $this->get_info_from_filename($filename,$pattern,"%a");
		}
		if (!strlen($results[$key]['genre'])) {
			$results[$key]['genre']		= $this->get_info_from_filename($filename,$pattern,"%g");
		}

		return $results;

	} // fill_info

	/*!
		@function get_info_from_filename
		@discussion get information from a filename based on pattern
	*/
	function get_info_from_filename($file,$pattern,$tag) {

                $preg_pattern = str_replace("$tag","(.+)",$pattern);
                $preg_pattern = preg_replace("/\%\w/",".+",$preg_pattern);
                $preg_pattern = "/" . str_replace("/","\/",$preg_pattern) . "\..+/";

		preg_match($preg_pattern,$file,$matches);

		return stripslashes($matches[1]);

	} // get_info_from_filename

	/**
	 * get_url
	 * This function takes all the song information and correctly formats
	 * a stream URL taking into account the downsampling mojo and everything
	 * else, this is used or will be used by _EVERYTHING_
	 */
	function get_url($session_id='',$force_http='') {

		/* Define Variables we are going to need */
		$user_id 	= $GLOBALS['user']->id ? scrub_out($GLOBALS['user']->id) : '-1';
		$song_id	= $this->id;

		if (Config::get('require_session')) {
			if ($session_id) {
				$session_string = "&sid=" . $session_id;
			}
			else {
				$session_string	= "&sid=" . Stream::get_session();
			}
		} // if they are requiring a session

		$type		= $this->type;

		/* Account for retarded players */
		if ($this->type == 'flac') { $type = 'ogg'; }

		$this->format();
		$song_name = rawurlencode($this->f_artist_full . " - " . $this->title . "." . $type);

		$web_path = Config::get('web_path');

                if (Config::get('force_http_play') OR !empty($force_http)) {
                        $port = Config::get('http_port');
			if (preg_match("/:\d+/",$web_path)) {
	                        $web_path = str_replace("https://", "http://",$web_path);
			}
			else {
	                        $web_path = str_replace("https://", "http://",$web_path);
			}
                }

		$url = $web_path . "/play/index.php?song=$song_id&uid=$user_id$session_string$ds_string&name=/$song_name";

		return $url;

	} // get_url

	/**
	 * native_stream
	 * This returns true/false if this can be nativly streamed
	 */
	public function native_stream() {

		if ($this->_transcode) { return false; }

		$conf_var 	= 'transcode_' . $this->type;
		$conf_type	= 'transcode_' . $this->type . '_target';

		if (Config::get($conf_var)) {
			$this->_transcode = true;
			debug_event('auto_transcode','Transcoding to ' . $this->type,'5');
			return false;
		}

		return true;

	} // end native_stream

	/**
	 * stream_cmd
	 * test if the song type streams natively and
	 * if not returns a transcoding command from the config
	 * we can't use this->type because its been formated for the
	 * downsampling
	 */
	function stream_cmd() {

		// Find the target for this transcode
		$conf_type      = 'transcode_' . $this->type . '_target';
		$stream_cmd = 'transcode_cmd_' . $this->type;
		$this->format_type(Config::get($conf_type));

		if (Config::get($stream_cmd)) {
			return $stream_cmd;
		}
		else {
			debug_event('Downsample','Error: Transcode ' . $stream_cmd . ' for ' . $this->type . ' not found, using downsample','2');
		}

		return false;

	} // end stream_cmd

        /**
         * get_sql_from_match
         * This is specificly for browsing it takes the match and returns the sql call that we want to use
         * @package Song
         * @catagory Class
         */
        function get_sql_from_match($match) {

                switch ($match) {
			case 'Show_all':
                        case 'Show_All':
                        case 'show_all':
                                $sql = "SELECT id FROM song";
                        break;
                        case 'Browse':
                        case 'show_genres':
                                $sql = "SELECT id FROM song";
                        break;
                        default:
                                $sql = "SELECT id FROM song WHERE title LIKE '" . sql_escape($match) . "%'";
                        break;
                } // end switch on match

                return $sql;

        } // get_sql_from_match

        /**
         * get_genres
         * this returns an array of songs based on a sql statement that's passed
         * @package songs
         * @catagory Class
         */
        function get_songs($sql) {

                $db_results = mysql_query($sql, dbh());

                $results = array();

                while ($r = mysql_fetch_assoc($db_results)) {
                        $results[] = $r['id'];
                }

                return $results;

        } // get_genres


} // end of song class

?>