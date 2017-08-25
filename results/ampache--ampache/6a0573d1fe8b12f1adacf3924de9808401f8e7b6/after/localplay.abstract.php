<?php
/*

 Copyright (c) 2001 - 2007 Ampache.org
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

/*
 * This is the abstract class for any localplay controller
 */
abstract class localplay_controller {

	// Required Functions
	abstract public function add($objects); // Takes an array of song_ids
	abstract public function delete_track($object_id); // Takes a single object_id and removes it from the playlist
	abstract public function play();
	abstract public function stop();
	abstract public function get();
	abstract public function connect();
	abstract public function status();
	abstract public function get_version(); // Returns the version of this plugin
	abstract public function get_description(); // Returns the description
	abstract public function is_installed(); // Returns an boolean t/f
	abstract public function install();
	abstract public function uninstall();

	// For display we need the following 'instance' functions
	abstract public function add_instance($data);
	abstract public function delete_instance($id);
	abstract public function update_instance($id,$post);
	abstract public function get_instances();
	abstract public function instance_fields();
	abstract public function set_active_instance($uid);
	abstract public function get_active_instance();

	/**
	 * get_url
	 * This returns the URL for the passed object
	 */
	public function get_url($object) {

		// This might not be an object!
		if (!is_object($object)) {
			// Stupiidly we'll just blindly add it for now
			return $object;
		}

		// This can get a little complicated
		switch ($object_type) {
			case 'random':

			break;
			case 'radio':
			case 'song':
			default:
				$url = $object->get_url(Stream::get_session());
			break;
		} // end switch on objecttype

		return $url;

	} // get_url

	/**
	 * get_file
	 * This returns the Filename for the passed object, not
	 * always possible
	 */
	public function get_file($object) {


	} // get_file

	/**
	 * parse_url
	 * This takes an Ampache URL and then returns the 'primary' part of it
	 * So that it's easier for localplay module sto return valid song information
	 */
	public function parse_url($url) {

		// Define possible 'primary' keys
		$primary_array = array('song','demo_id');

                // Delete everything before the first ?
                $file = preg_replace("/.*\?(.+)/",'$1',$url);

                // Split on & symbol
                $data = explode("&",$file);

                foreach ($data as $pair) {
                        $elements = explode("=",$pair);
	                $key = $elements['0'];
                        $value = $elements['1'];
	                $results[$key] = $value;

			if (in_array($key,$primary_array)) {
				$primary = $key;
			}

        	} // end foreach

		$results['primary_key'] = $primary;

		return $results;

	} // parse_url

} // end localplay_controller interface