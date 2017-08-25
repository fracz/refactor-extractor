<?php
/*

 Copyright (c) Ampache.org
 All rights reserved.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License v2
 as published by the Free Software Foundation

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
/*

 This is the wrapper for opening music streams from this server.  This script
   will play the local version or redirect to the remote server if that be
   the case.  Also this will update local statistics for songs as well.
   This is also where it decides if you need to be downsampled.
*/
define('NO_SESSION','1');
require_once '../lib/init.php';
require_once Config::get('prefix') . '/modules/horde/Browser.php';
ob_end_clean();

/* These parameters had better come in on the url. */
$uid 		= scrub_in($_REQUEST['uid']);
$song_id 	= scrub_in($_REQUEST['song']);
$sid 		= scrub_in($_REQUEST['sid']);
$xml_rpc	= scrub_in($_REQUEST['xml_rpc']);

/* This is specifically for tmp playlist requests */
$demo_id	= scrub_in($_REQUEST['demo_id']);
$random		= scrub_in($_REQUEST['random']);

// Parse byte range request
$n = sscanf($_SERVER['HTTP_RANGE'], "bytes=%d-%d",$start,$end);

/* First things first, if we don't have a uid/song_id stop here */
if (empty($song_id) && empty($demo_id) && empty($random)) {
	debug_event('no_song',"Error: No Song UID Specified, nothing to play",'2');
	exit;
}

// If we're XML-RPC and it's enabled, use system user
if (isset($xml_rpc) AND Config::get('xml_rpc') AND !isset($uid)) {
	$uid = '-1';
}

if (!isset($uid)) {
	debug_event('no_user','Error: No User specified','2');
	exit;
}

/* Misc Housework */
$user = new User($uid);
Preference::init();

/* If the user has been disabled (true value) */
if (make_bool($GLOBALS['user']->disabled)) {
	debug_event('user_disabled',"Error $user->username is currently disabled, stream access denied",'3');
	echo "Error: User Disabled";
	exit;
}

// If require session is set then we need to make sure we're legit
if (Config::get('require_session')) {
	if (!Config::get('require_localnet_session') AND Config::get('access_control') AND Access::check_network('network',$_SERVER['REMOTE_ADDR'],$GLOBALS['user']->id,'5')) {
		// Localnet defined IP and require localnot session has been turned off we let this one through
		debug_event('LocalNet','Streaming Access Granted to Localnet defined IP ' . $_SERVER['REMOTE_ADDR'],'5');
	}
	elseif(!Stream::session_exists($sid)) {
		debug_event('session_expired',"Streaming Access Denied: " . $GLOBALS['user']->username . "'s session has expired",'3');
    		die(_("Session Expired: please log in again at") . " " . Config::get('web_path') . "/login.php");
	}

	// Now that we've confirmed the session is valid
	// extend it
	Stream::extend_session($sid,$uid);
}


/* Update the users last seen information */
$user->update_last_seen();

/* If we are in demo mode.. die here */
if (Config::get('demo_mode') || (!Access::check('interface','25') AND !isset($xml_rpc))) {
	debug_event('access_denied',"Streaming Access Denied:" .Config::get('demo_mode') . "is the value of demo_mode. Current user level is " . $GLOBALS['user']->access,'3');
	access_denied();
	exit;
}

/*
   If they are using access lists let's make sure
   that they have enough access to play this mojo
*/
if (Config::get('access_control')) {
	if (!Access::check_network('stream',$_SERVER['REMOTE_ADDR'],$GLOBALS['user']->id,'25') AND
		!Access::check_network('network',$_SERVER['REMOTE_ADDR'],$GLOBALS['user']->id,'25')) {
		debug_event('access_denied', "Streaming Access Denied: " . $_SERVER['REMOTE_ADDR'] . " does not have stream level access",'3');
		access_denied();
		exit;
	}
} // access_control is enabled

/**
 * If we've got a tmp playlist then get the
 * current song, and do any other crazyness
 * we need to
 */
if ($demo_id) {
	$democratic = new Democratic($demo_id);
	$democratic->set_parent();

	// If there is a cooldown we need to make sure this song isn't a repeat
	if (!$democratic->cooldown) {
		/* This takes into account votes etc and removes the */
		$song_id = $democratic->get_next_object();
	}
	else {
		// Pull history
		$song_id = $democratic->get_next_object($song_cool_check);
		$song_ids = $democratic->get_cool_songs();
		while (in_array($song_id,$song_ids)) {
			$song_cool_check++;
			$song_id = $democratic->get_next_object($song_cool_check);
			if ($song_cool_check >= '5') { break; }
		}

	} // end if we've got a cooldown
} // if democratic ID passed

/**
 * if we are doing random let's pull the random object
 */
if ($random) {
	if ($start < 1) {
		$song_id = Random::get_single_song($_REQUEST['type']);
		// Save this one incase we do a seek
		$_SESSION['random']['last'] = $song_id;
	}
	else {
		$song_id = $_SESSION['random']['last'];
	}
} // if random

/* Base Checks passed create the song object */
$song = new Song($song_id);
$song->format();
$catalog = new Catalog($song->catalog);

/* If the song is disabled */
if (!make_bool($song->enabled)) {
	debug_event('song_disabled',"Error: $song->file is currently disabled, song skipped",'5');
	exit;
}




// If we are running in Legalize mode, don't play songs already playing
if (Config::get('lock_songs')) {
	if (!check_lock_songs($song->id)) {
		debug_event('Denied','Song ' . $song->id . ' is currently being played and lock songs is enabled','1');
		exit();
	}
}

/* Check to see if this is a 'remote' catalog */
if ($catalog->catalog_type == 'remote') {

	preg_match("/(.+)\/play\/index.+/",$song->file,$match);

	$token = xmlRpcClient::ampache_handshake($match['1'],$catalog->key);

	// If we don't get anything back we failed and should bail now
	if (!$token) {
		debug_event('xmlrpc-stream','Error Unable to get Token from ' . $match['1'] . ' check target servers logs','1');
		exit;
	}

	$sid   = xmlRpcClient::ampache_create_stream_session($match['1'],$token);

	$extra_info = "&xml_rpc=1&sid=$sid";
	header("Location: " . $song->file . $extra_info);
	debug_event('xmlrpc-stream',"Start XML-RPC Stream - " . $song->file . $extra_info,'5');

	/* If this is a voting tmp playlist remove the entry, we do this regardless of play amount */
	if ($demo_id) {
	        $row_id = $democratic->get_uid_from_object_id($song_id,'song');
	        if ($row_id) {
	                debug_event('Democratic','Removing ' . $song->title . ' from Democratic Playlist','1');
	                $democratic->delete_votes($row_id);
	        }
	} // if tmp_playlist

	exit;
} // end if remote catalog

/* If we don't have a file, or the file is not readable */
if (!$song->file OR !is_readable($song->file)) {

	// We need to make sure this isn't democratic play, if it is then remove the song
	// from the vote list
	if (is_object($tmp_playlist)) {
		$tmp_playlist->delete_track($song_id);
	}

	debug_event('file_not_found',"Error song $song->file ($song->title) does not have a valid filename specified",'2');
	echo "Error: Invalid Song Specified, file not found or file unreadable";
	exit;
}

// make fread binary safe
set_magic_quotes_runtime(0);

// don't abort the script if user skips this song because we need to update now_playing
ignore_user_abort(TRUE);

// Format the song name
$song_name = $song->f_artist_full . " - " . $song->title . "." . $song->type;

/* If they are just trying to download make sure they have rights
 * and then present them with the download file
 */
if ($_GET['action'] == 'download' AND Config::get('download')) {

	// STUPID IE
	$song->format_pattern();
	$song_name = str_replace(array('?','/','\\'),"_",$song->f_file);

	// Use Horde's Browser class to send the headers
	header("Content-Length: " . $song->size);
	$browser = new Browser();
	$browser->downloadHeaders($song_name,$song->mime,false,$song->size);
	$fp = fopen($song->file,'rb');
	$bytesStreamed = 0;

	if (!is_resource($fp)) {
                debug_event('file_read_error',"Error: Unable to open $song->file for downloading",'2');
		exit();
        }

	// Check to see if we should be throttling because we can get away with it
	if (Config::get('rate_limit') > 0) {
		while (!feof($fp)) {
			echo fread($fp,round(Config::get('rate_limit')*1024));
			$bytesStreamed += round(Config::get('rate_limit')*1024);
			flush();
			sleep(1);
		}
	}
	else {
		fpassthru($fp);
	}

	// Make sure that a good chunk of the song has been played
	if ($bytesStreamed >= $song->size) {
        	debug_event('Stats','Downloaded, Registering stats for ' . $song->title,'5');
	        $user->update_stats($song->id);
	} // if enough bytes are streamed

	fclose($fp);
	exit();

} // if they are trying to download and they can

// Generate browser class for sending headers
$browser = new Browser();
header("Accept-Ranges: bytes" );

// Prevent the script from timing out
set_time_limit(0);

/* We're about to start record this persons IP */
if (Config::get('track_user_ip')) {
	$user->insert_ip_history();
}

// If we've got downsample remote enabled
if (Config::get('downsample_remote')) {
	if (!Access::check_network('network',$_SERVER['REMOTE_ADDR'],$GLOBALS['user']->id,'0')) {
		debug_event('downsample','Network Downsample ' . $_SERVER['REMOTE_ADDR'] . ' is not in Local definition','5');
		$not_local = true;
	}
} // if downsample remote is enabled

// If they are downsampling, or if the song is not a native stream or it's non-local
if ((Config::get('transcode') == 'always' || !$song->native_stream() || $not_local) && Config::get('transcode') != 'never') {
	debug_event('downsample','Starting Downsample...','5');
	$fp = Stream::start_downsample($song,$lastid,$song_name,$start);
	$song_name = $song->f_artist_full . " - " . $song->title . "." . $song->type;
	// Note that this is downsampling
	$downsampled_song = true;
} // end if downsampling
else {
	// Send file, possible at a byte offset
	$fp = fopen($song->file, 'rb');

	if (!is_resource($fp)) {
		debug_event('file_read_error',"Error: Unable to open $song->file for reading",'2');
		cleanup_and_exit($lastid);
	}
} // else not downsampling

// Put this song in the now_playing table
Stream::insert_now_playing($song->id,$uid,$song->time,$sid);

if ($start > 0) {

	// Calculate stream size from byte range
	if(isset($end)) {
		$end = min($end,$song->size-1);
		$stream_size = ($end-$start)+1;
	}
	else {
		$stream_size = $song->size - $start;
	}

	debug_event('seek','Content-Range header recieved, skipping ahead ' . $start . ' bytes out of ' . $song->size,'5');
	$browser->downloadHeaders($song_name, $song->mime, false, $song->size);
	if (!$downsampled_song) {
		fseek( $fp, $start );
	}
	$range = $start ."-". $end . "/" . $song->size;
	header("HTTP/1.1 206 Partial Content");
	header("Content-Range: bytes $range");
	header("Content-Length: ".($stream_size));
}

/* Last but not least pump em out */
else {
	debug_event('stream','Starting stream of ' . $song->file . ' with size ' . $song->size,'5');
	header("Content-Length: $song->size");
	$browser->downloadHeaders($song_name, $song->mime, false, $song->size);
	$stream_size = $song->size;
}


/* Let's force them to actually play a portion of the song before
 * we count it in the statistics
 */
$bytes_streamed = 0;
$min_bytes_streamed = $song->size / 2;

// Actually do the streaming
do {
	$buf = fread($fp, min(2048,$stream_size-$bytes_streamed));
	print($buf);
	$bytes_streamed += strlen($buf);
} while (!feof($fp) && (connection_status() == 0) AND $bytes_streamed < $stream_size);

// Need to make sure enough bytes were sent. Some players (Windows Media Player) won't work if specified content length is not sent.
if($bytes_streamed < $stream_size AND (connection_status() == 0)) {
	print(str_repeat(' ',$stream_size - $bytes_streamed));
}

// Make sure that a good chunk of the song has been played
if ($bytes_streamed > $min_bytes_streamed) {
	debug_event('Stats','Registering stats for ' . $song->title,'5');

        $user->update_stats($song->id);
	/* Set the Song as Played if it isn't already */
	$song->set_played();

} // if enough bytes are streamed
else {
	debug_event('stream',$bytes_streamed .' of ' . $song->size . ' streamed, less than ' . $min_bytes_streamed . ' not collecting stats','5');
}


/* If this is a voting tmp playlist remove the entry, we do this regardless of play amount */
if ($demo_id) {
	$row_id = $democratic->get_uid_from_object_id($song_id,'song');
        if ($row_id) {
		debug_event('Democratic','Removing ' . $song->title . ' from Democratic Playlist','1');
		$democratic->delete_votes($row_id);
	}
} // if tmp_playlist

/* Clean up any open ends */
if (Config::get('play_type') == 'downsample' || !$song->native_stream()) {
	@pclose($fp);
}
else {
	@fclose($fp);
}

// Note that the stream has ended
debug_event('stream','Stream Ended at ' . $bytes_streamed . ' bytes out of ' . $song->size,'5');

?>