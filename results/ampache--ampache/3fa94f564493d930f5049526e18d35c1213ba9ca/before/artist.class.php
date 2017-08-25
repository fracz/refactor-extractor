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

/**
 * Artist Class
 */
class Artist {

	/* Variables from DB */
	public $id;
	public $name;
	public $songs;
	public $albums;
	public $prefix;

	/**
	 * Artist
	 * Artist class, for modifing a artist
	 * Takes the ID of the artist and pulls the info from the db
	 */
	function Artist($artist_id = 0) {

		/* If they failed to pass in an id, just run for it */
		if (!$artist_id) { return false; }

		/* Assign id for use in get_info() */
		$this->id = intval($artist_id);

		/* Get the information from the db */
		$info = $this->_get_info();

		foreach ($info as $key=>$value) {
			$this->$key = $value;
		} // foreach info

		return true;

	} //constructor

	/**
	 * _get_info
	 * get's the vars for $this out of the database taken from the object
	*/
	private function _get_info() {

		/* Grab the basic information from the catalog and return it */
		$sql = "SELECT * FROM artist WHERE id='" . Dba::escape($this->id) . "'";
		$db_results = Dba::query($sql);

		$results = Dba::fetch_assoc($db_results);

		return $results;

	} // _get_info

	/**
	 * get_albums
	 * gets the album ids that this artist is a part
	 * of
	 */
	public function get_albums() {

		$results = array();

		$sql = "SELECT `album`.`id` FROM album LEFT JOIN `song` ON `song`.`album`=`album`.`id` " .
			"WHERE `song`.`artist`='$this->id' GROUP BY `album`.`id` ORDER BY `album`.`name`,`album`.`year`";
		$db_results = Dba::query($sql);

		while ($r = Dba::fetch_assoc($db_results)) {
			$results[] = $r['id'];
		}

		return $results;

	} // get_albums

	/**
	 * get_songs
	 * gets the songs for this artist
	 */
	public function get_songs() {

		$sql = "SELECT `song`.`id` FROM `song` WHERE `song`.`artist`='" . Dba::escape($this->id) . "'";
		$db_results = Dba::query($sql);

		while ($r = Dba::fetch_assoc($db_results)) {
			$results[] = $r['id'];
		}

		return $results;

	} // get_songs

	/**
	 * get_song_ids
	 * This gets an array of song ids that are assoicated with this artist. This is great for using
	 * with the show_songs function
	 */
	function get_song_ids() {

		$sql = "SELECT id FROM song WHERE artist='" . sql_escape($this->id) . "' ORDER BY album, track";
		$db_results = mysql_query($sql, dbh());

		while ($r = mysql_fetch_assoc($db_results)) {
			$results[] = $r['id'];
		}

		return $results;

	} // get_song_ids

        /**
         * get_random_songs
	 * Gets the songs from this artist in a random order
         */
        public function get_random_songs() {

                $results = array();

                $sql = "SELECT `id` FROM `song` WHERE `artist`='$this->id' ORDER BY RAND()";
                $db_results = Dba::query($sql);

                while ($r = Dba::fetch_assoc($db_results)) {
                        $results[] = $r['id'];
                }

                return $results;

        } // get_random_songs

	/*!
		@function get_count
		@discussion gets the album and song count of
			this artist
	*/
	function get_count() {

		/* Define vars */
		$songs = 0;
		$albums = 0;

		$sql = "SELECT COUNT(song.id) FROM song WHERE song.artist='$this->id' GROUP BY song.album";
		$db_results = Dba::query($sql);

		while ($r = Dba::fetch_row($db_results)) {
			$songs += $r[0];
			$albums++;
		}

		/* Set Object Vars */
		$this->songs = $songs;
		$this->albums = $albums;

		return true;

	} // get_count

	/**
	 * format
         * this function takes an array of artist
	 * information and reformats the relevent values
	 * so they can be displayed in a table for example
	 * it changes the title into a full link.
 	 */
	public function format() {

		/* Combine prefix and name, trim then add ... if needed */
                $name = truncate_with_ellipsis(trim($this->prefix . " " . $this->name));
		$this->f_name = $name;

		//FIXME: This shouldn't be scrubing right here!!!!
		$this->full_name = scrub_out(trim($this->prefix . " " . $this->name));

	        $this->f_name_link = "<a href=\"" . Config::get('web_path') . "/artists.php?action=show&amp;artist=" . $this->id . "\" title=\"" . $this->full_name . "\">" . $name . "</a>";

		// Get the counts
		$this->get_count();

		return true;

	} // format

        /*!
                @function rename
                @discussion changes the name of the artist in the db,
                        and then merge()s songs
                @param $newname the artist's new name, either a new
                        artist will be created or songs added to existing
                        artist if name exists already
                @return the id of the new artist, or false if an error
        */
        function rename($newname) {

                /*
		 * There is this nifty function called check_artists in catalog that does exactly what we want it to do
                 * to use it, we first have to hax us a catalog
		 */
                $catalog = new Catalog();

                /* now we can get the new artist id in question */
                $newid = $catalog->check_artist($newname);

                /* check that it wasn't just whitespace that we were called to change */
                if ($newid == $this->id) {
			$GLOBALS['error']->add_error('artist_name',_("Error: Name Identical"));
                        return false;
                }

                /* now we can just call merge */
                if (!$this->merge($newid))
                	return false;

                //now return id
                return $newid;

        } // rename

        /*!
                @function merge
                @discussion changes the artist id of all songs by this artist
                        to the given id and deletes self from db
                @param $newid the new artist id that this artist's songs should have
                @return the name of the new artist on success, false if error
        */
        function merge($newid) {

		$catalog = new Catalog();

		/* Make sure this is a valid ID */
                if (!is_numeric($newid)) {
			$GLOBALS['error']->add_error('general',"Error: Invalid Artist ID");
                        return false;
		}

                // First check newid exists
                $check_exists_qstring = "SELECT name FROM artist WHERE id='" . $newid . "'"; //no need to escape newid, it's numeric
                $check_exists_query = mysql_query($check_exists_qstring, dbh());

                if ($check_exists_result = mysql_fetch_assoc($check_exists_query)) {
                        $NewName = $check_exists_result['name'];

                        // Now the query
                        $sql = "UPDATE song SET artist='" . $newid . "' " .
				"WHERE artist='" . sql_escape($this->id) . "'";
                        $db_results = mysql_query($sql, dbh());

                        $num_stats_changed = $catalog->merge_stats('artist',$this->id,$newid);

			/* If we've done the merege we need to clean up */
			$catalog->clean_artists();
			$catalog->clean_albums();

			return $NewName;
                }
		else {
			$GLOBALS['error']->add_error('general',"Error: No such artist to merge with");
                        return false;
                }
        } // merge

	/*!
		@function get_similar_artists
		@discussion returns an array of artist (id,name) arrays that are similar in name
			All whitespace and special chars are ignored
		@param extra arguments to normalize and compre, in that order
		@return array of artist, each element is (id,name)
	*/
	function get_similar_artists ($n_rep_uml,$n_filter,$n_ignore,$c_mode,$c_count_w,$c_percent_w,$c_distance_l) {
		//strip out just about everything, including whitespace, numbers and weird chars, and then
		//lowercase it
		$name = $this->normalize_name($this->name,$n_rep_uml,$n_filter,$n_ignore);

		//now for a bit of mysql query
		$sql = "SELECT id, name FROM artist WHERE id != '" . sql_escape($this->id) . "'";
		$query = mysql_query($sql, dbh());
		//loop it
		$similar_artists = array();
		while ($r = mysql_fetch_assoc($query)) {
			$artist_name = $this->normalize_name($r['name'],$n_rep_uml,$n_filter,$n_ignore);
			//echo "'" . $r['name'] . "' => '" . $artist_name . "'<br/>\n";
			if ($this->compare_loose($name,$artist_name,$c_mode,$c_count_w,$c_percent_w,$c_distance_l)) {
				//echo "***MATCH***<br/>\n";
				$similar_artists[] = array($r['id'],$r['name']);
			}
		}
		return $similar_artists;
	} // get_similar_artists


	/*!
		@function normalize_name
		@param artist name to normalize
		@param $replace_umlaut wether to replace umlauts and others with the plain letter, default true
		@param $filter what to filter out, defulat /[^a-z ]/
		@param $ignore terms to ignore, default /\s(the|an?)\s/ (name is padded with whitespace beforehand)
		@returns the normalized version of the given artist name, containing only letters and single spaces
	*/
	function normalize_name ($name,$replace_umlaut = NULL, $filter = NULL, $ignore = NULL) {
		if (is_null($replace_umlaut)) $replace_umlaut = true;
		if (is_null($filter)) $filter = "/[^a-z ]/";
		if (is_null($ignore)) $ignore = "/\s(the|an?)\s/";
		if ($replace_umlaut) {
			//convert �mlauts, idea from http://php.net/manual/en/function.str-replace.php#50081
			$umlauts = array("uml","acute","grave","cedil","ring","circ","tilde","lig","slash");
			$name = str_replace($umlauts,"",htmlentities($name));
			//now replace all &.; with .
			$name = preg_replace("/&(.);/","\$1",$name);
			//back to normal
			$name = html_entity_decode($name);
		}
		//lowercase
		$name = strtolower($name);
		//now rip out all the special chars and spaces
		$name = preg_replace($filter,"",$name);
		//now certains terms can be dropped completely
		//we have to add spaces on the sides though
		$name = " " . $name . " ";
		$name = preg_replace($ignore,"",$name);
		//now single spaces
		$name = preg_replace("/\s{2,}/"," ",$name);
		//return
		return trim($name);
	} //normalize_name

	/*!
		@function compare_loose
		@discussion percent and count are ORed together
		@param $name1 artist name
		@param $name2 artist name to compare against
		@param $mode the type of matching to perform, one of line or word, default word
		@param $countwords WORD MODE number of words that must be shared to match, 0 to disable, default 0
		@param $percentwords WORD MODE percentage of words that must be shared to match, 0 to disable, default 50%
		@param $distance LETTER MODE max levenshtein distance to pass as a match
		@return true if given params are similar, false if not
	*/
	function compare_loose ($name1,$name2,$mode = NULL,$countwords = NULL,$percentwords = NULL,$distance = NULL) {
		if (is_null($mode)) $mode = "word";
		if (is_null($countwords)) $countwords = 0;
		if (is_null($percentwords)) $percentwords = 50;
		if (is_null($distance)) $distance = 2;

		//echo "Compare '$name1' vs. '$name2'<br/>\n";

		$modes = array("line" => 0,"word" => 0,"letter" => 0);
		$mode = (isset($modes[$mode]) ? $mode : "word");
		switch ($mode) {
			case "line":
				//this is still relevant because of the normalize
				return $name1 == $name2;
			break;
			case "word":
				//echo "	COMPARE: Word mode<br/>\n";
				//first, count the number of terms in name1, and then the number that also appear in name2
				$words = explode(" ",$name1);
				$num_words = count($words);
				$num_words_shared = 0;
				foreach ($words as $word) {
					//echo "	Looking for word '$word'... ";
					if (strpos($name2,$word) !== false) {
						//echo "MATCHED";
						$num_words_shared++;
					} else {
						//echo " Nope";
					}
					//echo "<br/>\n";
				}
				//now make the descision
				return (
					($countwords > 0 && $num_words_shared >= $countwords) ||
					($percentwords > 0 && $num_words_shared > 0 && $num_words_shared/$num_words >= $percentwords/100)
				);
			break;
			case "letter":
				//simple
				return levenshtein($name1,$name2) <= $distance;
			break;
		}
	} // compare_loose

} // end of artist class
?>