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

if (!defined('IN_PHPBB'))
{
	die('Hacking attempt');
}

error_reporting(E_ERROR | E_WARNING | E_PARSE); // This will NOT report uninitialized variables
//error_reporting(E_ALL);
set_magic_quotes_runtime(0);

// If magic quotes is off, addslashes
if (!get_magic_quotes_gpc())
{
	$_GET = slash_input_data($_GET);
	$_POST = slash_input_data($_POST);
	$_COOKIE = slash_input_data($_COOKIE);
}

require($phpbb_root_path . 'config.'.$phpEx);

if (!defined('PHPBB_INSTALLED'))
{
	header('Location: install/install.'.$phpEx);
	exit;
}

// Load Extensions
if (!empty($load_extensions))
{
	$load_extensions = explode(',', $load_extensions);

	foreach ($load_extensions as $extension)
	{
		@dl(trim($extension));
	}
}

// Include files
require($phpbb_root_path . 'includes/acm/acm_' . $acm_type . '.'.$phpEx);
require($phpbb_root_path . 'includes/db/' . $dbms . '.'.$phpEx);
require($phpbb_root_path . 'includes/template.'.$phpEx);
require($phpbb_root_path . 'includes/session.'.$phpEx);
require($phpbb_root_path . 'includes/functions.'.$phpEx);

// User related
define('ANONYMOUS', 1);

define('USER_ACTIVATION_NONE', 0);
define('USER_ACTIVATION_SELF', 1);
define('USER_ACTIVATION_ADMIN', 2);
define('USER_ACTIVATION_DISABLE', 3);

define('USER_AVATAR_NONE', 0);
define('USER_AVATAR_UPLOAD', 1);
define('USER_AVATAR_REMOTE', 2);
define('USER_AVATAR_GALLERY', 3);

// ACL
define('ACL_NO', 0);
define('ACL_YES', 1);
define('ACL_UNSET', -1);

// Group settings
define('GROUP_OPEN', 0);
define('GROUP_CLOSED', 1);
define('GROUP_HIDDEN', 2);
define('GROUP_SPECIAL', 3);
define('GROUP_FREE', 4);

// Forum/Topic states
define('ITEM_UNLOCKED', 0);
define('ITEM_LOCKED', 1);
define('ITEM_MOVED', 2);

// Topic types
define('POST_NORMAL', 0);
define('POST_STICKY', 1);
define('POST_ANNOUNCE', 2);

// Lastread types
define('TRACK_NORMAL', 0); // not used at the moment
define('TRACK_POSTED', 1);

// Private messaging
define('PRIVMSGS_READ_MAIL', 0);
define('PRIVMSGS_NEW_MAIL', 1);
define('PRIVMSGS_UNREAD_MAIL', 5);

// Download Modes - Attachments
define('INLINE_LINK', 1);
define('PHYSICAL_LINK', 2);

// Categories - Attachments
define('NONE_CAT', 0);
define('IMAGE_CAT', 1); // Inline Images
define('WM_CAT', 2); // Windows Media Files - Streaming
define('RM_CAT', 3); // Real Media Files - Streaming
define('THUMB_CAT', 4); // Not used within the database, only while displaying posts
//define('SWF_CAT', 5); // Replaced by [flash] ? or an additional possibility ?

// BBCode UID length
define('BBCODE_UID_LEN', 5);

// Table names
define('ACL_GROUPS_TABLE', $table_prefix.'auth_groups');
define('ACL_OPTIONS_TABLE', $table_prefix.'auth_options');
define('ACL_DEPS_TABLE', $table_prefix.'auth_deps');
define('ACL_PRESETS_TABLE', $table_prefix.'auth_presets');
define('ACL_USERS_TABLE', $table_prefix.'auth_users');
define('ATTACHMENTS_TABLE', $table_prefix.'attachments');
define('ATTACHMENTS_DESC_TABLE', $table_prefix.'attach_desc');
define('BANLIST_TABLE', $table_prefix.'banlist');
define('CONFIG_TABLE', $table_prefix.'config');
define('CONFIRM_TABLE', $table_prefix.'confirm');
define('DISALLOW_TABLE', $table_prefix.'disallow'); //
define('EXTENSIONS_TABLE', $table_prefix.'extensions');
define('EXTENSION_GROUPS_TABLE', $table_prefix.'extension_groups');
define('FORUMS_TABLE', $table_prefix.'forums');
define('FORUMS_TRACK_TABLE', $table_prefix.'forums_marking');
define('FORUMS_WATCH_TABLE', $table_prefix.'forums_watch');
define('GROUPS_TABLE', $table_prefix.'groups');
define('GROUPS_MODERATOR_TABLE', $table_prefix.'groups_moderator');
define('ICONS_TABLE', $table_prefix.'icons');
define('LOG_ADMIN_TABLE', $table_prefix.'log_admin');
define('LOG_MOD_TABLE', $table_prefix.'log_moderator');
define('MODERATOR_TABLE', $table_prefix.'moderator_cache');
define('POSTS_TABLE', $table_prefix.'posts');
define('POSTS_TEXT_TABLE', $table_prefix.'posts_text');
define('PRIVMSGS_TABLE', $table_prefix.'privmsgs');
define('PRIVMSGS_TEXT_TABLE', $table_prefix.'privmsgs_text');
define('RANKS_TABLE', $table_prefix.'ranks');
define('RATINGS_TABLE', $table_prefix.'ratings');
define('REPORTS_TABLE', $table_prefix.'reports');
define('REASONS_TABLE', $table_prefix.'reports_reasons');
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
define('TOPICS_TRACK_TABLE', $table_prefix.'topics_marking');
define('TOPICS_WATCH_TABLE', $table_prefix.'topics_watch');
define('UCP_MODULES_TABLE', $table_prefix.'ucp_modules');
define('USER_GROUP_TABLE', $table_prefix.'user_group');
define('USERS_TABLE', $table_prefix.'users');
define('WORDS_TABLE', $table_prefix.'words');
define('POLL_OPTIONS_TABLE', $table_prefix.'poll_results');
define('POLL_VOTES_TABLE', $table_prefix.'poll_voters');

// Set PHP error handler to ours
set_error_handler('msg_handler');

// Instantiate some basic classes
$user = new user();
$auth = new auth();
$cache = new acm();
$template = new template();
$db = new sql_db($dbhost, $dbuser, $dbpasswd, $dbname, $dbport, false);

// Grab global variables, re-cache if necessary
if ($config = $cache->get('config'))
{
	$sql = 'SELECT *
		FROM ' . CONFIG_TABLE . '
		WHERE is_dynamic = 1';
	$result = $db->sql_query($sql);

	while ($row = $db->sql_fetchrow($result))
	{
		$config[$row['config_name']] = $row['config_value'];
	}
}
else
{
	$config = $cached_config = array();

	$sql = 'SELECT *
		FROM ' . CONFIG_TABLE;
	$result = $db->sql_query($sql);

	while ($row = $db->sql_fetchrow($result))
	{
		if (!$row['is_dynamic'])
		{
			$cached_config[$row['config_name']] = $row['config_value'];
		}

		$config[$row['config_name']] = $row['config_value'];
	}
	$db->sql_freeresult($result);

	$cache->put('config', $cached_config);
	unset($cached_config);
}

/*
if (time() - $config['cache_interval'] >= $config['cache_last_gc'])
{
	$cache->tidy($config['cache_gc']);
}
*/

// Show 'Board is disabled' message
if ($config['board_disable'] && !defined('IN_ADMIN') && !defined('IN_LOGIN'))
{
	$message = (!empty($config['board_disable_msg'])) ? $config['board_disable_msg'] : 'BOARD_DISABLE';
	trigger_error($message);
}

// addslashes to vars if magic_quotes_gpc is off
function slash_input_data(&$data)
{
	if (is_array($data))
	{
		foreach ($data as $k => $v)
		{
			$data[$k] = (is_array($v)) ? slash_input_data($v) : addslashes($v);
		}
	}
	return $data;
}

?>