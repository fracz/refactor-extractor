<?php
/***************************************************************************
 *                                common.php
 *                            -------------------
 *   begin                : Saturday, Feb 23, 2001
 *   copyright            : (C) 2001 The phpBB Group
 *   email                : support@phpbb.com
 *
 *   $Id$
 *
 ***************************************************************************/

/***************************************************************************
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 ***************************************************************************/

if ( !defined('IN_PHPBB') )
{
	die('Hacking attempt');
}

error_reporting  (E_ERROR | E_WARNING | E_PARSE); // This will NOT report uninitialized variables
set_magic_quotes_runtime(0);

require($phpbb_root_path . 'config.'.$phpEx);

if ( !defined('PHPBB_INSTALLED') )
{
	header('Location: install/install.'.$phpEx);
	exit;
}

//set_error_handler('message');

// Define some constants/variables
define('ANONYMOUS', 0);

// User related
define('USER_ACTIVATION_NONE', 0);
define('USER_ACTIVATION_SELF', 1);
define('USER_ACTIVATION_ADMIN', 2);
define('USER_ACTIVATION_DISABLE', 3);

define('USER_AVATAR_NONE', 0);
define('USER_AVATAR_UPLOAD', 1);
define('USER_AVATAR_REMOTE', 2);
define('USER_AVATAR_GALLERY', 3);

// ACL
define('ACL_PREVENT', 1);
define('ACL_DENY', 2);
define('ACL_ALLOW', 4);
define('ACL_PERMIT', 8);

// Group settings
define('GROUP_OPEN', 0);
define('GROUP_CLOSED', 1);
define('GROUP_HIDDEN', 2);

// Forum/Topic states
define('ITEM_UNLOCKED', 0);
define('ITEM_LOCKED', 1);
define('ITEM_MOVED', 2);
define('ITEM_CATEGORY', 3);

// Topic types
define('POST_NORMAL', 0);
define('POST_STICKY', 1);
define('POST_ANNOUNCE', 2);

// Error codes
define('MESSAGE', 200);
define('ERROR', 201);

// Private messaging
define('PRIVMSGS_READ_MAIL', 0);
define('PRIVMSGS_NEW_MAIL', 1);
define('PRIVMSGS_UNREAD_MAIL', 5);

// Table names
define('ACL_GROUPS_TABLE', $table_prefix.'auth_groups');
define('ACL_OPTIONS_TABLE', $table_prefix.'auth_options');
define('ACL_PREFETCH_TABLE', $table_prefix.'auth_prefetch');
define('ACL_USERS_TABLE', $table_prefix.'auth_users');
define('BANLIST_TABLE', $table_prefix.'banlist');
define('CATEGORIES_TABLE', $table_prefix.'categories'); //
define('CONFIG_TABLE', $table_prefix.'config');
define('CONFIG_USER_TABLE', $table_prefix.'config_defaults');
define('DISALLOW_TABLE', $table_prefix.'disallow'); //
define('FORUMS_TABLE', $table_prefix.'forums');
define('FORUMS_WATCH_TABLE', $table_prefix.'forums_watch');
define('GROUPS_TABLE', $table_prefix.'groups');
define('ICONS_TABLE', $table_prefix.'icons');
define('LOG_ADMIN_TABLE', $table_prefix.'log_admin');
define('LOG_MOD_TABLE', $table_prefix.'log_moderator');
define('POSTS_TABLE', $table_prefix.'posts');
define('POSTS_TEXT_TABLE', $table_prefix.'posts_text');
define('PRIVMSGS_TABLE', $table_prefix.'privmsgs');
define('PRIVMSGS_TEXT_TABLE', $table_prefix.'privmsgs_text');
define('RANKS_TABLE', $table_prefix.'ranks');
define('SEARCH_TABLE', $table_prefix.'search_results');
define('SEARCH_WORD_TABLE', $table_prefix.'search_wordlist');
define('SEARCH_MATCH_TABLE', $table_prefix.'search_wordmatch');
define('SESSIONS_TABLE', $table_prefix.'sessions');
define('SMILIES_TABLE', $table_prefix.'smilies');
define('STYLES_TABLE', $table_prefix.'styles');
define('STYLES_TPL_TABLE', $table_prefix.'styles_template');
define('STYLES_CSS_TABLE', $table_prefix.'styles_theme');
define('STYLES_IMAGE_TABLE', $table_prefix.'styles_imageset');
define('TOPICS_TABLE', $table_prefix.'topics');
define('TOPICS_RATINGS_TABLE', $table_prefix.'topics_rating');
define('TOPICS_WATCH_TABLE', $table_prefix.'topics_watch');
define('USER_GROUP_TABLE', $table_prefix.'user_group');
define('USERS_TABLE', $table_prefix.'users');
define('WORDS_TABLE', $table_prefix.'words');
define('VOTE_DESC_TABLE', $table_prefix.'vote_desc');
define('VOTE_RESULTS_TABLE', $table_prefix.'vote_results');
define('VOTE_USERS_TABLE', $table_prefix.'vote_voters');

// If magic quotes is off, addslashes
if ( !get_magic_quotes_gpc() )
{
	$HTTP_GET_VARS = slash_input_data($HTTP_GET_VARS);
	$HTTP_POST_VARS = slash_input_data($HTTP_POST_VARS);
	$HTTP_COOKIE_VARS = slash_input_data($HTTP_COOKIE_VARS);
}

$board_config = array();
$userdata = array();
$theme = array();
$images = array();
$lang = array();

// Include files
require($phpbb_root_path . 'includes/template.'.$phpEx);
require($phpbb_root_path . 'includes/session.'.$phpEx);
require($phpbb_root_path . 'includes/functions.'.$phpEx);
require($phpbb_root_path . 'db/' . $dbms . '.'.$phpEx);

// Instantiate some basic classes
$session = new session();
$auth = new auth();
$template = new Template();
$db = new sql_db($dbhost, $dbuser, $dbpasswd, $dbname, $dbport, false);

// Obtain users IP
if ( $_SERVER['HTTP_X_FORWARDED_FOR'] != '' || $_ENV['HTTP_X_FORWARDED_FOR'] != '' )
{
	$user_ip = ( !empty($_SERVER['REMOTE_ADDR']) ) ? $_SERVER['REMOTE_ADDR'] : ( ( !empty($_ENV['REMOTE_ADDR']) ) ? $_ENV['REMOTE_ADDR'] : $REMOTE_ADDR );
	$x_ip = ( !empty($_SERVER['HTTP_X_FORWARDED_FOR']) ) ? $_SERVER['HTTP_X_FORWARDED_FOR'] : $_ENV['HTTP_X_FORWARDED_FOR'];

	if ( preg_match('/^([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+)/', $x_ip, $ip_list) )
	{
		$private_ip = array('/^0\./', '/^127\.0\.0\.1/', '/^192\.168\..*/', '/^172\.16\..*/', '/^10\..*/', '/^224\..*/', '/^240\..*/');
		$user_ip = preg_replace($private_ip, $client_ip, $ip_list[1]);
	}
}
else
{
	$user_ip = ( !empty($_SERVER['REMOTE_ADDR']) ) ? $_SERVER['REMOTE_ADDR'] : ( ( !empty($_ENV['REMOTE_ADDR']) ) ? $_ENV['REMOTE_ADDR'] : $REMOTE_ADDR );
}

// Setup forum wide options, if this fails we output a CRITICAL_ERROR since
// basic forum information is not available
$sql = "SELECT *
	FROM " . CONFIG_TABLE;
$result = $db->sql_query($sql, false);

while ( $row = $db->sql_fetchrow($result) )
{
	$board_config[$row['config_name']] = $row['config_value'];
}

// Show 'Board is disabled' message
if ( $board_config['board_disable'] && !defined('IN_ADMIN') && !defined('IN_LOGIN') )
{
	$message = ( !empty($board_config['board_disable_msg']) ) ? $board_config['board_disable_msg'] : 'Board_disable';
	message_die(MESSAGE, $message, 'Information');
	//trigger_error($message);
}

// addslashes to vars if magic_quotes_gpc is off
function slash_input_data(&$data)
{
	if ( is_array($data) )
	{
		foreach ( $data as $k => $v )
		{
			$data[$k] = ( is_array($v) ) ? slash_input_data($v) : addslashes($v);
		}
	}
	return $data;
}

?>