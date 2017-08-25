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
 * This is accessed remotly to allow outside scripts access to ampache information
 * as such it needs to verify the session id that is passed
 */

define('NO_SESSION','1');
require_once '../lib/init.php';

/**
 * Verify the existance of the Session they passed in we do allow them to
 * login via this interface so we do have an exception for action=login
 */
if (!Access::session_exists(array(),$_REQUEST['auth'],'api') AND $_REQUEST['action'] != 'handshake') {
	debug_event('Access Denied','Invalid Session or unthorized access attempt to API','5');
	exit();
}

/* Set the correct headers */
header("Content-type: text/xml; charset=" . Config::get('site_charset'));
header("Content-Disposition: attachment; filename=information.xml");

switch ($_REQUEST['action']) {
	case 'handshake':
		// Send the data we were sent to the API class so it can be chewed on
		$token = Api::handshake($_REQUEST['timestamp'],$_REQUEST['auth'],$_SERVER['REMOTE_ADDR'],$_REQUEST['user']);

		if (!$token) {
			echo xmlData::error('Error Invalid Handshake, attempt logged');
		}
		else {
			echo xmlData::single_string('auth',$token);
		}

	break;
	case 'artists':
		Browse::reset_filters();
		Browse::set_type('artist');
		Browse::set_sort('name','ASC');

		if ($_REQUEST['filter']) {
			Browse::set_filter('alpha_match',$_REQUEST['filter']);
		}
		$artists = Browse::get_objects();
		// echo out the resulting xml document
		echo xmlData::artists($artists);
	break;
	case 'artist_albums':
		$artist = new Artist($_REQUEST['filter']);

		$albums = $artist->get_albums();
		echo xmlData::albums($albums);
	break;
	case 'albums':
		Browse::reset_filters();
		Browse::set_type('album');
		Browse::set_sort('name','ASC');

		if ($_REQUEST['filter']) {
			Browse::set_filter('alpha_match',$_REQUEST['filter']);
		}
		$albums = Browse::get_objects();
		echo xmlData::albums($albums);
	break;
	case 'album_songs':
		$album = new Album($_REQUEST['filter']);
		$songs = $album->get_songs();

		echo xmlData::songs($songs);
	break;
	case 'genres':
		Browse::reset_filters();
		Browse::set_type('genre');
		Browse::set_sort('name','ASC');

		if ($_REQUEST['filter']) {
			Browse::set_filter('alpha_match',$_REQUEST['filter']);
		}
		$genres = Browse::get_objects();

		echo xmlData::genres($genres);
	break;
	case 'songs':
		Browse::reset_filters();
		Browse::set_type('song');
		Browse::set_sort('title','ASC');

		if ($_REQUEST['filter']) {
			Browse::set_filter('alpha_match',$_REQUEST['filter']);
		}
		$songs = Browse::get_objects();

		echo xmlData::songs($songs);
	break;
	default:
		// Rien a faire
	break;
} // end switch action
?>