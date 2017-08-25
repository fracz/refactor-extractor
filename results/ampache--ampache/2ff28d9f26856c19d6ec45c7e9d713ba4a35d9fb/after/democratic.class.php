<?php
/*

 Copyright (c) 2001 - 2008 Ampache.org
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
 * Democratic
 * This class handles democratic play, which is a fancy
 * name for voting based playback. This extends the tmpplaylist
 */
class Democratic extends tmpPlaylist {

	/**
	 * constructor
	 * We need a constructor for this class. It does it's own thing now
	 */
	public function __construct($id='') {

		if (!$id) { return false; }

		$info = $this->get_info($id);

		foreach ($info as $key=>$value) {
			$this->$key = $value;
		}

	} // constructor


	/**
 	 * get_info
	 * This returns the data from the database
	 */
	private function get_info($id) {

		$id = Dba::escape($id);

		$sql = "SELECT * FROM `democratic` WHERE `id`='$id'";
		$db_results = Dba::query($sql);

		$row = Dba::fetch_assoc($db_results);

		return $row;

	} // get_info

	/**
	 * format
	 * This makes the objects variables all purrty so that they can be displayed
	 */
	public function format() {

		$this->f_cooldown	= $this->cooldown . ' ' . _('minutes');
		$this->f_primary	= $this->primary ? _('Primary') : '';

		switch ($this->level) {
			case '5':
				$this->f_level = _('Guest');
			break;
			case '25':
				$this->f_level = _('User');
			break;
			case '50':
				$this->f_level = _('Content Manager');
			break;
			case '75':
				$this->f_level = _('Catalog Manager');
			break;
			case '100':
				$this->f_level = _('Admin');
			break;
		}

	} // format

	/**
	 * get_playlists
	 * This returns all of the current valid 'Democratic' Playlists
	 * that have been created.
	 */
	public static function get_playlists() {

		// Pull all tmp playlsits with a session of < 0 (as those are fake)
		// This is kind of hackish, should really think about tweaking the db
		// and doing this right.
		$sql = "SELECT `id` FROM `democratic` ORDER BY `name`";
		$db_results = Dba::query($sql);

		$results = array();

		while ($row = Dba::fetch_assoc($db_results)) {
			$results[] = $row['id'];
		}

		return $results;

	} // get_playlists

	/**
	 * get_current_playlist
	 * This returns the curren users current playlist, or if specified
	 * this current playlist of the user
	 */
	public static function get_current_playlist() {

		$democratic_id = Config::get('democratic_id');

		if (!$democratic_id) {
			$level = Dba::escape($GLOBALS['user']->access);
			$sql = "SELECT `id` FROM `democratic` WHERE `level` <= '$level' " .
				" ORDER BY `level` DESC,`primary` DESC";
			$db_results = Dba::query($sql);
			$row = Dba::fetch_assoc($db_results);
			$democratic_id = $row['id'];
		}

		$object = new Democratic($democratic_id);

		return $object;

	} // get_current_playlist

        /**
         * get_items
         * This returns an array of all object_ids currently in this tmpPlaylist. This
         * has gotten a little more complicated because of type, the values are an array
         * 0 being ID 1 being TYPE
	 * FIXME: This is too complex, it makes my brain hurt
	 * [VOTE COUNT]
	 * 	[DATE OF NEWEST VOTE]
	 *		[ROW ID]
	 * 			[OBJECT_ID]
	 *			[OBJECT_TYPE]
	 *
	 * Sorting does the following
	 * sort largest VOTE COUNT to top
	 * sort smallest DATE OF NEWEST VOTE]
         */
        public function get_items() {

                $order          = "ORDER BY `user_vote`.`date` ASC";
                $vote_join 	= "INNER JOIN `user_vote` ON `user_vote`.`object_id`=`tmp_playlist_data`.`id`";

                /* Select all objects from this playlist */
                $sql = "SELECT `tmp_playlist_data`.`id`,`tmp_playlist_data`.`object_type`, `user_vote`.`date`, `tmp_playlist_data`.`object_id` " .
                        "FROM `tmp_playlist_data` $vote_join " .
                        "WHERE `tmp_playlist_data`.`tmp_playlist`='" . Dba::escape($this->id) . "' $order";
                $db_results = Dba::query($sql);

                /* Define the array */
                $items = array();
		$votes = array();
		// Itterate and build the sortable array
                while ($results = Dba::fetch_assoc($db_results)) {

			// First build a variable that holds the number of votes for an object
			$name		= 'vc_' . $results['object_id'];

			// Check if the vote is older then our current vote for this object
			if ($votes[$results['object_id']] < $results['date'] OR !isset($votes[$results['object_id']])) {
				$votes[$results['object_id']] = $results['date'];
			}


			// Append oen to the vote
			${$name}++;
			$primary_key 	= ${$name};
			$secondary_key	= $votes[$results['object_id']];
			$items[$primary_key][$secondary_key][$results['id']] = array($results['object_id'],$results['object_type'],$results['id']);
                }

		// Sort highest voted stuff to the top
		krsort($items);

		// re-collapse the array
		foreach ($items as $vote_count=>$date_array) {
			ksort($date_array);
			foreach ($date_array as $object_array) {
				foreach ($object_array as $key=>$sorted_array) {
					$sorted_items[$key] = $sorted_array;
				}
			}
		}

                return $sorted_items;

        } // get_items

        /**
         * get_url
         * This returns the special play URL for democratic play, only open to ADMINs
         */
        public function get_url() {

                $link = Config::get('web_path') . '/play/index.php?demo_id=' . scrub_out($this->id) .
                        '&sid=' . Stream::get_session() . '&uid=' . scrub_out($GLOBALS['user']->id);
                return $link;

        } // get_url

        /**
         * get_next_object
         * This returns the next object in the tmp_playlist most of the time this
         * will just be the top entry, but if there is a base_playlist and no
         * items in the playlist then it returns a random entry from the base_playlist
         */
        public function get_next_object($offset='') {

		$offset = $offset ? intval($offset) : '0';

		// We have to get all because of the pysco sorting
		$items = self::get_items();

		if (count($items)) {
			$array = array_slice($items,$offset,1);
			$item = array_shift($array);
			$results['object_id'] = $item['0'];
		}

                /* If nothing was found and this is a voting playlist then get from base_playlist */
                if (!$results['object_id']) {

                        /* Check for a playlist */
                        if ($this->base_playlist) {
                                /* We need to pull a random one from the base_playlist */
                                $base_playlist = new Playlist($this->base_playlist);
				$data = $base_playlist->get_random_items(1);
                                $results['object_id'] = $data['0']['object_id'];
                        }
                        else {
                                $sql = "SELECT `id` as `object_id` FROM `song` WHERE `enabled`='1' ORDER BY RAND() LIMIT 1";
                                $db_results = Dba::query($sql);
                                $results = Dba::fetch_assoc($db_results);
                        }
                }

                return $results['object_id'];

        } // get_next_object

	/**
	 * get_uid_from_object_id
	 * This takes an object_id and an object type and returns the ID for the row
	 */
	public function get_uid_from_object_id($object_id,$object_type='') {

		$object_id	= Dba::escape($object_id);
		$object_type	= $object_type ? Dba::escape($object_type) : 'song';
		$tmp_id		= Dba::escape($this->id);

		$sql = "SELECT `tmp_playlist_data`.`id` FROM `tmp_playlist_data` WHERE `object_type`='$object_type' AND " .
			"`tmp_playlist`='$tmp_id' AND `object_id`='$object_id'";
		$db_results = Dba::query($sql);

		$row = Dba::fetch_assoc($db_results);

		return $row['id'];

	} // get_uid_from_object_id

	/**
         * vote
         * This function is called by users to vote on a system wide playlist
         * This adds the specified objects to the tmp_playlist and adds a 'vote'
         * by this user, naturally it checks to make sure that the user hasn't
         * already voted on any of these objects
         */
        public function vote($items) {

                /* Itterate through the objects if no vote, add to playlist and vote */
                foreach ($items as $type=>$object_id) {
			//FIXME: This is a hack until we fix everything else
			if (intval($type) == $type) { $type = 'song'; }
                        if (!$this->has_vote($object_id,$type)) {
                                $this->add_vote($object_id,$type);
                        }
                } // end foreach

        } // vote

        /**
         * has_vote
         * This checks to see if the current user has already voted on this object
         */
        public function has_vote($object_id,$type='') {

                $tmp_id		= Dba::escape($this->id);
		$object_id 	= Dba::escape($object_id);
		$type		= $type ? Dba::escape($type) : 'song';
		$user_id	= Dba::escape($GLOBALS['user']->id);

                /* Query vote table */
                $sql = "SELECT tmp_playlist_data.object_id FROM `user_vote` " .
                        "INNER JOIN tmp_playlist_data ON tmp_playlist_data.id=user_vote.object_id " .
                        "WHERE user_vote.user='$user_id' AND tmp_playlist_data.object_type='$type' " .
                        "AND tmp_playlist_data.object_id='$object_id' " .
                        "AND tmp_playlist_data.tmp_playlist='$tmp_id'";
                $db_results = Dba::query($sql);

                /* If we find  row, they've voted!! */
                if (Dba::num_rows($db_results)) {
                        return true;
                }

                return false;

        } // has_vote

        /**
         * add_vote
         * This takes a object id and user and actually inserts the row
         */
        public function add_vote($object_id,$object_type='') {

                $object_id      = Dba::escape($object_id);
                $tmp_playlist   = Dba::escape($this->id);
		$object_type	= $object_type ? Dba::escape($object_type) : 'song';

                /* If it's on the playlist just vote */
                $sql = "SELECT `id` FROM `tmp_playlist_data` " .
                        "WHERE `tmp_playlist_data`.`object_id`='$object_id' AND `tmp_playlist_data`.`tmp_playlist`='$tmp_playlist'";
                $db_results = Dba::query($sql);

                /* If it's not there, add it and pull ID */
                if (!$results = Dba::fetch_assoc($db_results)) {
                        $sql = "INSERT INTO `tmp_playlist_data` (`tmp_playlist`,`object_id`,`object_type`) " .
                                "VALUES ('$tmp_playlist','$object_id','$object_type')";
                        $db_results = Dba::query($sql);
                        $results['id'] = Dba::insert_id();
                }

                /* Vote! */
                $time = time();
                $sql = "INSERT INTO user_vote (`user`,`object_id`,`date`) " .
                        "VALUES ('" . Dba::escape($GLOBALS['user']->id) . "','" . $results['id'] . "','$time')";
                $db_results = Dba::query($sql);

                return true;

        } // add_vote

        /**
         * remove_vote
         * This is called to remove a vote by a user for an object, it uses the object_id
         * As that's what we'll have most the time, no need to check if they've got an existing
         * vote for this, just remove anything that is there
         */
        public function remove_vote($row_id) {

                $object_id      = Dba::escape($row_id);
                $user_id        = Dba::escape($GLOBALS['user']->id);

		$sql = "DELETE FROM `user_vote` WHERE `object_id`='$object_id'";
                $db_results = Dba::query($sql);

                /* Clean up anything that has no votes */
                self::prune_tracks();

                return true;

        } // remove_vote

	/**
	 * delete_votes
	 * This removes the votes for the specified object on the current playlist
	 */
	public function delete_votes($row_id) {

		$row_id		= Dba::escape($row_id);

		$sql = "DELETE FROM `user_vote` WHERE `object_id`='$row_id'";
		$db_results = Dba::query($sql);

		$sql = "DELETE FROM `tmp_playlist_data` WHERE `id`='$row_id'";
		$db_results = Dba::query($sql);

		return true;

	} // delete_votes

	/**
	 * delete
	 * This deletes a democratic playlist
	 */
	public static function delete($democratic_id) {

		$democratic_id = Dba::escape($democratic_id);

		$sql = "DELETE FROM `democratic` WHERE `id`='$democratic_id'";
		$db_results = Dba::query($sql);

		return true;

	} // delete

	/**
	 * update
	 * This updates an existing democratic playlist item. It takes a key'd array just like the create
	 */
	public function update($data) {




	} // update

	/**
	 * create
	 * This is the democratic play create function it inserts this into the democratic table
	 */
	public static function create($data) {

		// Clean up the input
		$name 	= Dba::escape($data['name']);
		$base 	= Dba::escape($data['democratic']);
		$cool	= Dba::escape($data['cooldown']);
		$level	= Dba::escape($data['level']);
		$default = Dba::escape($data['make_default']);
		$user	= Dba::escape($GLOBALS['user']->id);

		$sql = "INSERT INTO `democratic` (`name`,`base_playlist`,`cooldown`,`level`,`user`,`primary`) " .
			"VALUES ('$name','$base','$cool','$level','$user','$default')";
		$db_results = Dba::query($sql);

		if ($db_results) {
			$insert_id = Dba::insert_id();
			parent::create($insert_id,'vote','song');
		}


		return $db_results;

	} // create

	/**
	 * prune_tracks
	 * This replaces the normal prune tracks and correctly removes the votes
	 * as well
	 */
	public static function prune_tracks() {

                // This deletes data without votes, if it's a voting democratic playlist
                $sql = "DELETE FROM tmp_playlist_data USING tmp_playlist_data " .
                        "LEFT JOIN user_vote ON tmp_playlist_data.id=user_vote.object_id " .
                        "LEFT JOIN tmp_playlist ON tmp_playlist.id=tmp_playlist.tmp_playlist " .
                        "WHERE user_vote.object_id IS NULL AND tmp_playlist.type = 'vote'";
                $db_results = Dba::query($sql);

                return true;

	} // prune_tracks

        /**
         * clear
         * This is really just a wrapper function, it clears the entire playlist
         * including all votes etc.
         */
        public function clear() {

                $tmp_id = Dba::escape($this->id);

                /* Clear all votes then prune */
                $sql = "DELETE FROM user_vote USING user_vote " .
                        "LEFT JOIN tmp_playlist_data ON user_vote.object_id = tmp_playlist_data.id " .
                        "WHERE tmp_playlist_data.tmp_playlist='$tmp_id'";
                $db_results = Dba::query($sql);

                // Prune!
                self::prune_tracks();

                return true;

        } // clear_playlist


} // Democratic class
?>