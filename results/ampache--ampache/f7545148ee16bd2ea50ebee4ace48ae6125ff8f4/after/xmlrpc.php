<?php
/*

 Copyright (c) 2001 - 2005 Ampache.org
 All rights reserved.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

/**
 * XML-RPC Library
 * If you want an honest answer NFC. Copy and paste baby!
 * @package XMLRPC
 * @catagory Server
 * @author Karl Vollmer
 * @copyright Ampache.org 2001 - 2005
 */

/**
 * remote_server_query
 * this is the initial contact and response to a xmlrpc request from another ampache server
 * this returns a list of catalog names
 * @package XMLRPC
 * @catagory Server
 */
function remote_server_query($m) {

        $result = array();

        // we only want to send the local entries
        $sql = "SELECT name FROM catalog WHERE catalog_type='local'";
	$db_result = mysql_query($sql, dbh());

        while ( $i = mysql_fetch_row($db_result) ) {
                $result[] = $i;
        }

	set_time_limit(0);
        $encoded_array = php_xmlrpc_encode($result);

	if (conf('debug')) { log_event($_SESSION['userdata']['username'],'xml-rpc_encoded',$encoded_array); }

        return new xmlrpcresp($encoded_array);

} // remote_server_query

/**
 * remote_song_query
 * return all local songs and their information it pulls all local catalogs, then all enabled songs
 * then it strings togeather their information sepearted by :: and then base64 encodes it before sending it along
 * @package XMLRPC
 * @catagory Server
 * @todo Add catalog level access control
 * @todo fix for the smallint(1) change of song.status
 */
function remote_song_query() {

	// Get me a list of all local catalogs
	$sql = "SELECT catalog.id FROM catalog WHERE catalog_type='local'";
	$db_results = mysql_query($sql, dbh());

	$results = array();

	$sql = "SELECT song.id FROM song WHERE song.status='enabled' AND (";

	// Get the catalogs and build the query!
	while ($r = mysql_fetch_object($db_results)) {
			$sql .= " song.catalog='$r->id' OR";
	} // build query

	$sql = rtrim($sql,"OR");

	$sql .= ")";

	$db_results = mysql_query($sql, dbh());

	// Recurse through the songs and build a results
	// array that is base64_encoded
	while ($r = mysql_fetch_object($db_results)) {

		$song 		= new Song($r->id);
		$song->album 	= $song->get_album_name();
		$song->artist 	= $song->get_artist_name();
		$song->genre	= $song->get_genre_name();

		// Format the output
		$output = '';
		$output = $song->artist . "::" . $song->album . "::" . $song->title . "::" . $song->comment .
			  "::" . $song->year . "::" . $song->bitrate . "::" . $song->rate . "::" . $song->mode .
			  "::" . $song->size . "::" . $song->time . "::" . $song->track . "::" . $song->genre . "::" . $r->id;
		$output = base64_encode($output);
		$results[] = $output;

		// Prevent Timeout
		set_time_limit(0);

	} // while songs

	set_time_limit(0);
	$encoded_array = php_xmlrpc_encode($results);
	if (conf('debug')) { log_event($_SESSION['userdata']['username'],' xmlrpc-server ',"Encoded: $encoded_array"); }
	return new xmlrpcresp($encoded_array);

} // remote_song_query

/**
 * remote_server_denied
 * Access Denied Sucka!
 * @package XMLRPC
 * @catagory Server
 */
function remote_server_denied() {

        $result = array();

        $result['access_denied'] = "Access Denied: Sorry, but " . $_SERVER['REMOTE_ADDR'] . " does not have access to " .
				"this server's catalog. Please make sure that you have been added to this server's access list.\n";

        $encoded_array = php_xmlrpc_encode($result);

	if (conf('debug')) { log_event($_SESSION['userdata']['username'], 'xmlrpc-server',"Access Denied: " . $_SERVER['REMOTE_ADDR']); }

        return new xmlrpcresp($encoded_array);

} // remote_server_denied

?>