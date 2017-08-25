<?php
/***************************************************************************
 *                               functions.php
 *                            -------------------
 *   begin                : Saturday, Feb 13, 2001
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

function set_config($config_name, $config_value, $is_dynamic = FALSE)
{
	global $db, $cache, $config;

	if (isset($config[$config_name]))
	{
		$sql = 'UPDATE ' . CONFIG_TABLE . "
			SET config_value = '" . $db->sql_escape($config_value) . "'
			WHERE config_name = '$config_name'";
		$db->sql_query($sql);
	}
	else
	{
		$db->sql_query('DELETE FROM ' . CONFIG_TABLE . "
			WHERE config_name = '" . $config_name . "'");

		$sql = 'INSERT INTO ' . CONFIG_TABLE . " (config_name, config_value)
			VALUES ('$config_name', '" . $db->sql_escape($config_value) . "')";
		$db->sql_query($sql);
	}

	$config[$config_name] = $config_value;

	if (!$is_dynamic)
	{
		$cache->put('config', $config);
	}
}

function get_userdata($user)
{
	global $db;

	$sql = "SELECT *
		FROM " . USERS_TABLE . "
		WHERE ";
	$sql .= ((is_int($user)) ? "user_id = $user" : "username = '" .  $db->sql_escape($user) . "'") . " AND user_id <> " . ANONYMOUS;
	$result = $db->sql_query($sql);

	return ($row = $db->sql_fetchrow($result)) ? $row : false;
}

function get_forum_branch($forum_id, $type = 'all', $order = 'descending', $include_forum = TRUE)
{
	global $db;

	switch ($type)
	{
		case 'parents':
			$condition = 'f1.left_id BETWEEN f2.left_id AND f2.right_id';
			break;

		case 'children':
			$condition = 'f2.left_id BETWEEN f1.left_id AND f1.right_id';
			break;

		default:
			$condition = 'f2.left_id BETWEEN f1.left_id AND f1.right_id OR f1.left_id BETWEEN f2.left_id AND f2.right_id';
	}

	$rows = array();

	$sql = 'SELECT f2.*
		FROM (' . FORUMS_TABLE . ' f1
		LEFT JOIN ' . FORUMS_TABLE . " f2 ON $condition)
		WHERE f1.forum_id = $forum_id
		ORDER BY f2.left_id " . (($order == 'descending') ? 'ASC' : 'DESC');
	$result = $db->sql_query($sql);

	while ($row = $db->sql_fetchrow($result))
	{
		if (!$include_forum && $row['forum_id'] == $forum_id)
		{
			continue;
		}

		$rows[] = $row;
	}
	$db->sql_freeresult($result);

	return $rows;
}

// Create forum navigation links for given forum, create parent
// list if currently null, assign basic forum info to template
function generate_forum_nav(&$forum_data)
{
	global $db, $user, $template, $phpEx, $SID;

	// Get forum parents
	$forum_parents = get_forum_parents($forum_data);

	// Build navigation links
	foreach ($forum_parents as $parent_forum_id => $parent_name)
	{
		$template->assign_block_vars('navlinks', array(
			'FORUM_NAME'	=>	$parent_name,
			'U_VIEW_FORUM'	=>	"viewforum.$phpEx$SID&amp;f=$parent_forum_id")
		);
	}

	$template->assign_block_vars('navlinks', array(
		'FORUM_NAME'	=>	$forum_data['forum_name'],
		'U_VIEW_FORUM'	=>	"viewforum.$phpEx$SID&amp;f=" . $forum_data['forum_id'])
	);

	$template->assign_vars(array(
		'FORUM_ID' 		=> $forum_data['forum_id'],
		'FORUM_NAME'	=> $forum_data['forum_name'],
		'FORUM_DESC'	=> $forum_data['forum_desc'])
	);

	return;
}

// Returns forum parents as an array. Get them from forum_data if available, or update the database otherwise
function get_forum_parents($forum_data)
{
	global $db;

	$forum_parents = array();
	if ($forum_data['parent_id'] > 0)
	{
		if ($forum_data['forum_parents'] == '')
		{
			$sql = 'SELECT forum_id, forum_name
				FROM ' . FORUMS_TABLE . '
				WHERE left_id < ' . $forum_data['left_id'] . '
					AND right_id > ' . $forum_data['right_id'] . '
				ORDER BY left_id ASC';

			$result = $db->sql_query($sql);
			while ($row = $db->sql_fetchrow($result))
			{
				$forum_parents[$row['forum_id']] = $row['forum_name'];
			}
			$db->sql_freeresult($result);

			$sql = 'UPDATE ' . FORUMS_TABLE . "
				SET forum_parents = '" . $db->sql_escape(serialize($forum_parents)) . "'
				WHERE parent_id = " . $forum_data['parent_id'];
			$db->sql_query($sql);
		}
		else
		{
			$forum_parents = unserialize($forum_data['forum_parents']);
		}
	}

	return $forum_parents;
}

// Obtain list of moderators of each forum
function get_moderators(&$forum_moderators, $forum_id = false)
{
	global $config, $template, $db, $phpEx, $SID;

	// Have we disabled the display of moderators? If so, then return
	// from whence we came ...
	if (empty($config['load_moderators']))
	{
		return;
	}

	if (!empty($forum_id) && is_array($forum_id))
	{
		$forum_sql = 'AND forum_id IN (' . implode(', ', $forum_id) . ')';
	}
	else
	{
		$forum_sql = ($forum_id) ? 'AND forum_id = ' . $forum_id : '';
	}

	$sql = 'SELECT *
		FROM ' . MODERATOR_TABLE . "
		WHERE display_on_index = 1
			$forum_sql";
	$result = $db->sql_query($sql);

	while ($row = $db->sql_fetchrow($result))
	{
		$forum_moderators[$row['forum_id']][] = (!empty($row['user_id'])) ? '<a href="ucp.' . $phpEx . $SID . '&amp;mode=viewprofile&amp;u=' . $row['user_id'] . '">' . $row['username'] . '</a>' : '<a href="groupcp.' . $phpEx . $SID . '&amp;g=' . $row['group_id'] . '">' . $row['groupname'] . '</a>';
	}
	$db->sql_freeresult($result);

	return;
}

// User authorisation levels output
function gen_forum_rules($mode, &$forum_id)
{
	global $SID, $template, $auth, $user;

	$rules = array('post', 'reply', 'edit', 'delete', 'attach', 'download');

	foreach ($rules as $rule)
	{
		$template->assign_block_vars('rules', array(
			'RULE' => ($auth->acl_get('f_' . $rule, intval($forum_id))) ? $user->lang['RULES_' . strtoupper($rule) . '_CAN'] : $user->lang['RULES_' . strtoupper($rule) . '_CANNOT'])
		);
	}

	return;
}

function gen_sort_selects(&$limit_days, &$sort_by_text, &$sort_days, &$sort_key, &$sort_dir, &$s_limit_days, &$s_sort_key, &$s_sort_dir, &$u_sort_param)
{
	global $user;

	$sort_dir_text = array('a' => $user->lang['ASCENDING'], 'd' => $user->lang['DESCENDING']);

	$s_limit_days = '<select name="st">';
	foreach ($limit_days as $day => $text)
	{
		$selected = ($sort_days == $day) ? ' selected="selected"' : '';
		$s_limit_days .= '<option value="' . $day . '"' . $selected . '>' . $text . '</option>';
	}
	$s_limit_days .= '</select>';

	$s_sort_key = '<select name="sk">';
	foreach ($sort_by_text as $key => $text)
	{
		$selected = ($sort_key == $key) ? ' selected="selected"' : '';
		$s_sort_key .= '<option value="' . $key . '"' . $selected . '>' . $text . '</option>';
	}
	$s_sort_key .= '</select>';

	$s_sort_dir = '<select name="sd">';
	foreach ($sort_dir_text as $key => $value)
	{
		$selected = ($sort_dir == $key) ? ' selected="selected"' : '';
		$s_sort_dir .= '<option value="' . $key . '"' . $selected . '>' . $value . '</option>';
	}
	$s_sort_dir .= '</select>';

	$u_sort_param = "st=$sort_days&amp;sk=$sort_key&amp;sd=$sort_dir";

	return;
}

function make_jumpbox($action, $forum_id = false, $select_all = false)
{
	global $auth, $template, $user, $db, $nav_links, $phpEx, $SID;

	$boxstring = '';
	$sql = 'SELECT forum_id, forum_name, forum_type, left_id, right_id
		FROM ' . FORUMS_TABLE . '
		ORDER BY left_id ASC';
	$result = $db->sql_query($sql, 600);

	$right = $cat_right = 0;
	$padding = $forum_list = $holding = '';
	while ($row = $db->sql_fetchrow($result))
	{
		if ($row['forum_type'] == FORUM_CAT && ($row['left_id'] + 1 == $row['right_id']))
		{
			// Non-postable forum with no subforums, don't display
			continue;
		}

		if (!$auth->acl_get('f_list', $row['forum_id']))
		{
			// if the user does not have permissions to list this forum skip
			continue;
		}

		if ($row['left_id'] < $right)
		{
			$padding .= '&nbsp; &nbsp;';
		}
		else if ($row['left_id'] > $right + 1)
		{
			$padding = substr($padding, 0, -13 * ($row['left_id'] - $right + 1));
		}

		$right = $row['right_id'];

		$selected = ($row['forum_id'] == $forum_id) ? ' selected="selected"' : '';

		if ($row['left_id'] > $cat_right)
		{
			$holding = '';
		}

		if ($row['right_id'] - $row['left_id'] > 1)
		{
			$cat_right = max($cat_right, $row['right_id']);

			$holding .= '<option value="' . $row['forum_id'] . '"' . $selected . '>' . $padding . '+ ' . $row['forum_name'] . '</option>';
		}
		else
		{
			$boxstring .= $holding . '<option value="' . $row['forum_id'] . '"' . $selected . '>' . $padding . '- ' . $row['forum_name'] . '</option>';
			$holding = '';
		}

		$nav_links['chapter forum'][$row['forum_id']] = array (
			'url' => "viewforum.$phpEx$SID&f=" . $row['forum_id'],
			'title' => $row['forum_name']
		);
	}
	$db->sql_freeresult($result);

	if ($boxstring != '')
	{
		$boxstring = (($select_all) ? '<option value="0">' . $user->lang['ALL_FORUMS'] : '<option value="-1">' . $user->lang['SELECT_FORUM']) . '</option><option value="-1">-----------------</option>' . $boxstring;
	}

	$template->assign_vars(array(
		'S_JUMPBOX_OPTIONS' => $boxstring,
		'S_JUMPBOX_ACTION' => $action)
	);

	return;
}

// Pick a language, any language ...
function language_select($default = '')
{
	global $phpbb_root_path, $phpEx;

	$dir = @opendir($phpbb_root_path . 'language');

	$user = array();
	while ($file = readdir($dir))
	{
		$path = $phpbb_root_path . 'language/' . $file;

		if (is_file($path) || is_link($path) || $file == '.' || $file == '..')
		{
			continue;
		}

		if (file_exists($path . '/iso.txt'))
		{
			list($displayname) = @file($path . '/iso.txt');
			$lang[$displayname] = $file;
		}
	}
	@closedir($dir);

	@asort($lang);
	@reset($lang);

	foreach ($lang as $displayname => $filename)
	{
		$selected = (strtolower($default) == strtolower($filename)) ? ' selected="selected"' : '';
		$user_select .= '<option value="' . $filename . '"' . $selected . '>' . ucwords($displayname) . '</option>';
	}

	return $user_select;
}

// Pick a template/theme combo,
function style_select($default = '')
{
	global $db;

	$sql = "SELECT style_id, style_name
		FROM " . STYLES_TABLE . "
		ORDER BY style_name, style_id";
	$result = $db->sql_query($sql);

	while ($row = $db->sql_fetchrow($result))
	{
		$selected = ($row['style_id'] == $default) ? ' selected="selected"' : '';

		$style_select .= '<option value="' . $row['style_id'] . '"' . $selected . '>' . $row['style_name'] . '</option>';
	}

	return $style_select;
}

// Pick a timezone
function tz_select($default = '')
{
	global $sys_timezone, $user;

	foreach ($user->lang['tz'] as $offset => $zone)
	{
		if (is_numeric($offset))
		{
			$selected = ($offset === $default) ? ' selected="selected"' : '';
			$tz_select .= '<option value="' . $offset . '"' . $selected . '>' . $zone . '</option>';
		}
	}

	return $tz_select;
}

// Topic and forum watching common code
function watch_topic_forum($mode, &$s_watching, &$s_watching_img, $user_id, $match_id, $notify_status = 'unset')
{
	global $template, $db, $user, $phpEx, $SID, $start;

	$table_sql = ($mode == 'forum') ? FORUMS_WATCH_TABLE : TOPICS_WATCH_TABLE;
	$where_sql = ($mode == 'forum') ? 'forum_id' : 'topic_id';
	$u_url = ($mode == 'forum') ? 'f' : 't';

	// Is user watching this thread?
	if ($user_id)
	{
		$can_watch = TRUE;

		if ($notify_status == 'unset')
		{
			$sql = "SELECT notify_status
				FROM $table_sql
				WHERE $where_sql = $match_id
					AND user_id = $user_id";
			$result = $db->sql_query($sql);

			$notify_status = ($row = $db->sql_fetchrow($result)) ? $row['notify_status'] : NULL;
			$db->sql_freeresult($result);
		}

		if (!is_null($notify_status))
		{
			if (isset($_GET['unwatch']))
			{
				if ($_GET['unwatch'] == $mode)
				{
					$is_watching = 0;

					$sql = "DELETE FROM " . $table_sql . "
						WHERE $where_sql = $match_id
							AND user_id = $user_id";
					$db->sql_query($sql);
				}

				meta_refresh(3, "view$mode.$phpEx$SID&amp;$u_url=$match_id&amp;start=$start");
				$message = $user->lang['NOT_WATCHING_' . strtoupper($mode)] . '<br /><br />' . sprintf($user->lang['RETURN_' . strtoupper($mode)], '<a href="' . "view$mode.$phpEx$SID&amp;" . $u_url . "=$match_id&amp;start=$start" . '">', '</a>');
				trigger_error($message);
			}
			else
			{
				$is_watching = TRUE;

				if ($notify_status)
				{
					$sql = "UPDATE " . $table_sql . "
						SET notify_status = 0
						WHERE $where_sql = $match_id
							AND user_id = $user_id";
					$db->sql_query($sql);
				}
			}
		}
		else
		{
			if (isset($_GET['watch']))
			{
				if ($_GET['watch'] == $mode)
				{
					$is_watching = TRUE;

					$sql = "INSERT INTO " . $table_sql . " (user_id, $where_sql, notify_status)
						VALUES ($user_id, $match_id, 0)";
					$db->sql_query($sql);
				}

				meta_refresh(3, "view$mode.$phpEx$SID&amp;$u_url=$match_id&amp;start=$start");
				$message = $user->lang['ARE_WATCHING_' . strtoupper($mode)] . '<br /><br />' . sprintf($user->lang['RETURN_' . strtoupper($mode)], '<a href="' . "view$mode.$phpEx$SID&amp;" . $u_url . "=$match_id&amp;start=$start" . '">', '</a>');
				trigger_error($message);
			}
			else
			{
				$is_watching = 0;
			}
		}
	}
	else
	{
		if (isset($_GET['unwatch']))
		{
			if ($_GET['unwatch'] == $mode)
			{
				login_box(preg_replace('#.*?([a-z]+?\.' . $phpEx . '.*?)$#i', '\1', htmlspecialchars($_SERVER['REQUEST_URI'])));
			}
		}
		else
		{
			$can_watch = 0;
			$is_watching = 0;
		}
	}

	if ($can_watch)
	{
		$s_watching = ($is_watching) ? "<a href=\"view$mode.$phpEx$SID&amp;$u_url=$match_id&amp;unwatch=$mode&amp;start=$start\">" . $user->lang['STOP_WATCHING_' . strtoupper($mode)] . '</a>' : "<a href=\"view$mode.$phpEx$SID&amp;$u_url=$match_id&amp;watch=$mode&amp;start=$start\">" . $user->lang['START_WATCHING_' . strtoupper($mode)] . '</a>';
	}

	return;
}

// Marks a topic or form as read
function markread($mode, $forum_id = 0, $topic_id = 0, $marktime = false)
{
	global $config, $db, $user;

	if ($user->data['user_id'] == ANONYMOUS)
	{
		return;
	}

	// Default tracking type
	$type = TRACK_NORMAL;
	$current_time = ($marktime) ? $marktime : time();

	switch ($mode)
	{
		case 'mark':
			if ($config['load_db_lastread'])
			{
				// Mark one forum as read.
				// Do this by inserting a record with -$forum_id in the 'forum_id' field.
				// User has marked this topic as read before: Update the record
				$db->sql_return_on_error = true;

				$sql = 'UPDATE ' . FORUMS_TRACK_TABLE . "
					SET mark_time = $current_time
					WHERE user_id = " . $user->data['user_id'] . "
						AND forum_id = $forum_id";
				if (!$db->sql_query($sql) || !$db->sql_affectedrows())
				{
					// User is marking this forum for the first time.
					// Insert dummy topic_id to satisfy PRIMARY KEY (user_id, topic_id)
					// dummy id = -forum_id
					$sql = 'INSERT INTO ' . FORUMS_TRACK_TABLE . ' (user_id, forum_id, mark_time)
						VALUES (' . $user->data['user_id'] . ", $forum_id, $current_time)";
					$db->sql_query($sql);
				}

				$db->sql_return_on_error = false;
			}
			else
			{
				$tracking_forums = (isset($_COOKIE[$config['cookie_name'] . '_f'])) ? unserialize($_COOKIE[$config['cookie_name'] . '_f']) : array();
				$tracking_forums[$forum_id] = time();

				setcookie($config['cookie_name'] . '_f', serialize($tracking_forums), time() + 31536000, $config['cookie_path'], $config['cookie_domain'], $config['cookie_secure']);
				unset($tracking_forums);
			}
			break;

		case 'markall':
			// Mark all forums as read

			if ($config['load_db_lastread'])
			{
				$sql = 'UPDATE ' . FORUMS_TRACK_TABLE . '
					SET mark_time = ' . $current_time . '
					WHERE user_id = ' . $user->data['user_id'];
				$db->sql_query($sql);
			}
			else
			{
				$tracking_forums = array();
			}

			// Select all forum_id's that are not yet in the lastread table
			switch (SQL_LAYER)
			{
				case 'oracle':
					break;

				default:
					$sql = ($config['load_db_lastread']) ? 'SELECT f.forum_id FROM (' . FORUMS_TABLE . ' f LEFT JOIN ' . FORUMS_TRACK_TABLE . ' ft ON (ft.user_id = ' . $user->data['user_id'] . ' AND ft.forum_id = f.forum_id)) WHERE ft.forum_id IS NULL' : 'SELECT forum_id FROM ' . FORUMS_TABLE;
			}
			$result = $db->sql_query($sql);

			$db->sql_return_on_error = true;
			if ($row = $db->sql_fetchrow($result))
			{
				do
				{
					if ($config['load_db_lastread'])
					{
						$sql = '';
						// Some forum_id's are missing. We are not taking into account
						// the auth data, even forums the user can't see are marked as read.
						switch (SQL_LAYER)
						{
							case 'mysql':
							case 'mysql4':
								$sql .= (($sql != '') ? ', ' : '') . '(' . $user->data['user_id'] . ', ' . $row['forum_id'] . ", $current_time)";
								break;

							case 'mssql':
								$sql = (($sql != '') ? ' UNION ALL ' : '') . ' SELECT ' . $user->data['user_id'] . ', ' . $row['forum_id'] . ", $current_time";
								break;

							default:
								$sql = 'INSERT INTO ' . FORUMS_TRACK_TABLE . ' (user_id, forum_id, mark_time)
									VALUES (' . $user->data['user_id'] . ', ' . $row['forum_id'] . ", $current_time)";
								$db->sql_query($sql);
								$sql = '';
						}

						if ($sql != '')
						{
							$sql = 'INSERT INTO ' . FORUMS_TRACK_TABLE . ' (user_id, forum_id, mark_time)
								VALUES ' . $sql;
							$db->sql_query($sql);
						}
					}
					else
					{
						$tracking_forums[$row['forum_id']] = $current_time;
					}
				}
				while ($row = $db->sql_fetchrow($result));
				$db->sql_freeresult($result);

				$db->sql_return_on_error = false;

				if (!$config['load_db_lastread'])
				{
					setcookie($config['cookie_name'] . '_f', serialize($tracking_forums), time() + 31536000, $config['cookie_path'], $config['cookie_domain'], $config['cookie_secure']);
					unset($tracking_forums);
				}
			}
			break;

		case 'post':
			// Mark a topic as read and mark it as a topic where the user has made a post.
			$type = TRACK_POSTED;

		case 'topic':
			// Mark a topic as read
			if ($config['load_db_lastread'] || ($config['load_db_track'] && $type == TRACK_POSTED))
			{
				$sql = 'UPDATE ' . TOPICS_TRACK_TABLE . "
					SET mark_type = $type, mark_time = " . time() . "
					WHERE topic_id = $topic_id
						AND user_id = " . $user->data['user_id'];
				if (!$db->sql_query($sql) || !$db->sql_affectedrows())
				{
					$sql = 'INSERT INTO ' . TOPICS_TRACK_TABLE . ' (user_id, topic_id, mark_type, mark_time)
						VALUES (' . $user->data['user_id'] . ", $topic_id, $type, " . time() . ")";
					$db->sql_query($sql);
				}
			}

			if (!$config['load_db_lastread'])
			{
				$tracking = (isset($_COOKIE[$config['cookie_name'] . '_t'])) ? unserialize($_COOKIE[$config['cookie_name'] . '_t']) : array();
				$tracking[$topic_id] = $current_time;

				setcookie($config['cookie_name'] . '_t', serialize($tracking), time() + 31536000, $config['cookie_path'], $config['cookie_domain'], $config['cookie_secure']);
				unset($tracking);
			}
			break;
	}
}


// Pagination routine, generates page number sequence
function generate_pagination($base_url, $num_items, $per_page, $start_item, $add_prevnext_text = TRUE)
{
	global $user;

	$total_pages = ceil($num_items/$per_page);

	if ($total_pages == 1 || !$num_items)
	{
		return '';
	}

	$on_page = floor($start_item / $per_page) + 1;

	$page_string = ($on_page == 1) ? '<b>1</b>' : '<a href="' . $base_url . "&amp;start=" . (($on_page - 2) * $per_page) . '">' . $user->lang['PREVIOUS'] . '</a>&nbsp;&nbsp;<a href="' . $base_url . '">1</a>';

	if ($total_pages > 5)
	{
		$start_cnt = min(max(1, $on_page - 4), $total_pages - 5);
		$end_cnt = max(min($total_pages, $on_page + 4), 6);

		$page_string .= ($start_cnt > 1) ? ' ... ' : ', ';

		for($i = $start_cnt + 1; $i < $end_cnt; $i++)
		{
			$page_string .= ($i == $on_page) ? '<b>' . $i . '</b>' : '<a href="' . $base_url . "&amp;start=" . (($i - 1) * $per_page) . '">' . $i . '</a>';
			if ($i < $end_cnt - 1)
			{
				$page_string .= ', ';
			}
		}

		$page_string .= ($end_cnt < $total_pages) ? ' ... ' : ', ';
	}
	else
	{
		$page_string .= ', ';

		for($i = 2; $i < $total_pages; $i++)
		{
			$page_string .= ($i == $on_page) ? '<b>' . $i . '</b>' : '<a href="' . $base_url . "&amp;start=" . (($i - 1) * $per_page) . '">' . $i . '</a>';
			if ($i < $total_pages)
			{
				$page_string .= ', ';
			}
		}
	}

	$page_string .= ($on_page == $total_pages) ? '<b>' . $total_pages . '</b>' : '<a href="' . $base_url . '&amp;start=' . (($total_pages - 1) * $per_page) . '">' . $total_pages . '</a>&nbsp;&nbsp;<a href="' . $base_url . "&amp;start=" . ($on_page * $per_page) . '">' . $user->lang['NEXT'] . '</a>';

	$page_string = $user->lang['GOTO_PAGE'] . ' ' . $page_string;

	return $page_string;
}

function on_page($num_items, $per_page, $start)
{
	global $user;

	return sprintf($user->lang['PAGE_OF'], floor($start / $per_page) + 1, max(ceil($num_items / $per_page), 1));
}

// Obtain list of naughty words and build preg style replacement arrays for use by the
// calling script, note that the vars are passed as references this just makes it easier
// to return both sets of arrays
function obtain_word_list(&$censors)
{
	global $db, $cache;

	if ($cache->exists('word_censors'))
	{
		$censors = $cache->get('word_censors'); // transfer to just if (!(...)) ? works fine for me
	}
	else
	{
		$sql = "SELECT word, replacement
			FROM  " . WORDS_TABLE;
		$result = $db->sql_query($sql);

		$censors = array();
		if ($row = $db->sql_fetchrow($result))
		{
			do
			{
				$censors['match'][] = '#(' . str_replace('\*', '\w*?', preg_quote($row['word'], '#')) . ')#i';
				$censors['replace'][] = $row['replacement'];
			}
			while ($row = $db->sql_fetchrow($result));
		}
		$db->sql_freeresult($result);

		$cache->put('word_censors', $censors);
	}

	return true;
}

// Obtain currently listed icons, re-caching if necessary
function obtain_icons(&$icons)
{
	global $db, $cache;

	if ($cache->exists('icons'))
	{
		$icons = $cache->get('icons');
	}
	else
	{
		// Topic icons
		$sql = "SELECT *
			FROM " . ICONS_TABLE . "
			ORDER BY icons_order";
		$result = $db->sql_query($sql);

		$icons = array();
		while ($row = $db->sql_fetchrow($result))
		{
			$icons[$row['icons_id']]['img'] = $row['icons_url'];
			$icons[$row['icons_id']]['width'] = $row['icons_width'];
			$icons[$row['icons_id']]['height'] = $row['icons_height'];
			$icons[$row['icons_id']]['display'] = $row['display_on_posting'];
		}
		$db->sql_freeresult($result);

		$cache->put('icons', $icons);
	}

	return;
}

// Obtain allowed extensions
function obtain_attach_extensions(&$extensions)
{
	global $db, $cache;

	if ($cache->exists('extensions'))
	{
		$extensions = $cache->get('extensions');
	}
	else
	{
		// Don't count on forbidden extensions table, because it is not allowed to allow forbidden extensions at all
		$sql = "SELECT e.extension, g.*
			FROM " . EXTENSIONS_TABLE . " e, " . EXTENSION_GROUPS_TABLE . " g
			WHERE e.group_id = g.group_id
				AND g.allow_group = 1";
		$result = $db->sql_query($sql);

		$extensions = array();
		while ($row = $db->sql_fetchrow($result))
		{
			$extension = strtolower(trim($row['extension']));

			$extensions['_allowed_'][] = $extension;
			$extensions[$extension]['display_cat'] = intval($row['cat_id']);
			$extensions[$extension]['download_mode'] = intval($row['download_mode']);
			$extensions[$extension]['upload_icon'] = trim($row['upload_icon']);
			$extensions[$extension]['max_filesize'] = intval($row['max_filesize']);
		}
		$db->sql_freeresult($result);

		$cache->put('extensions', $extensions);
	}

	return;
}

function generate_board_url()
{
	global $config;

	return (($config['cookie_secure']) ? 'https://' : 'http://') . preg_replace('#^/?(.*?)/?$#', '\1', trim($config['server_name'])) . (($config['server_port'] <> 80) ? ':' . trim($config['server_port']) . '/' : '/') . preg_replace('#^/?(.*?)/?$#', '\1', trim($config['script_path']));
}

// Redirects the user to another page then exits the script nicely
function redirect($url)
{
	global $db, $cache, $config, $user;

	if (isset($db))
	{
		$db->sql_close();
	}

	if (isset($cache))
	{
		$cache->unload();
	}

	// Local redirect? If not, prepend the boards url
	$url = (!strstr($url, '://')) ? (generate_board_url() . preg_replace('#^/?(.*?)/?$#', '/\1', trim($url))) : $url;

	// Redirect via an HTML form for PITA webservers
	if (@preg_match('#Microsoft|WebSTAR|Xitami#', getenv('SERVER_SOFTWARE')))
	{
		header('Refresh: 0; URL=' . $url);
		echo '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"><html><head><meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"><meta http-equiv="refresh" content="0; url=' . $url . '"><title>Redirect</title></head><body><div align="center">' . sprintf($user->lang['URL_REDIRECT'], '<a href="' . $url . '">', '</a>') . '</div></body></html>';
		exit;
	}

	// Behave as per HTTP/1.1 spec for others
	header('Location: ' . $url);
	exit;
}

// Meta refresh assignment
function meta_refresh($time, $url)
{
	global $template;

	$template->assign_vars(array(
		'META' => '<meta http-equiv="refresh" content="' . $time . ';url=' . $url . '">')
	);
}


// Generate login box or verify password
function login_box($s_action, $s_hidden_fields = '', $login_explain = '')
{
	global $SID, $db, $user, $template, $auth, $phpbb_root_path, $phpEx;

	$err = '';
	if (isset($_POST['login']))
	{
		$autologin = (!empty($_POST['autologin'])) ? TRUE : FALSE;
		$viewonline = (!empty($_POST['viewonline'])) ? 0 : 1;

		if (($result = $auth->login($_POST['username'], $_POST['password'], $autologin, $viewonline)) === true)
		{
			return true;
		}

		// If we get a non-numeric (e.g. string) value we output an error
		if (is_string($result))
		{
			trigger_error($result, E_USER_ERROR);
		}

		// If we get an integer zero then we are inactive, else the username/password is wrong
		$err = ($result === 0) ? $user->lang['ACTIVE_ERROR'] :  $user->lang['LOGIN_ERROR'];
	}

	$template->assign_vars(array(
		'LOGIN_ERROR'		=> $err,
		'LOGIN_EXPLAIN'		=> $login_explain,

		'U_SEND_PASSWORD' 	=> "ucp.$phpEx$SID&amp;mode=sendpassword",
		'U_TERMS_USE'		=> "ucp.$phpEx$SID&amp;mode=terms",
		'U_PRIVACY'			=> "ucp.$phpEx$SID&amp;mode=privacy",

		'S_LOGIN_ACTION'	=> $s_action,
		'S_HIDDEN_FIELDS' 	=> $s_hidden_fields)
	);

	$page_title = $user->lang['LOGIN'];
	include($phpbb_root_path . 'includes/page_header.'.$phpEx);

	$template->set_filenames(array(
		'body' => 'login_body.html')
	);
	make_jumpbox('viewforum.'.$phpEx);

	include($phpbb_root_path . 'includes/page_tail.'.$phpEx);
}


// Error and message handler, call with trigger_error if reqd
function msg_handler($errno, $msg_text, $errfile, $errline)
{
	global $cache, $db, $auth, $template, $config, $user, $nav_links;
	global $phpEx, $phpbb_root_path, $starttime;

	switch ($errno)
	{
		case E_WARNING:
			if (defined('DEBUG_EXTRA'))
			{
//				echo "PHP Warning on line <b>$errline</b> in <b>$errfile</b> :: <b>$msg_text</b><br />";
			}
			break;

		case E_NOTICE:
			if (defined('DEBUG_EXTRA'))
			{
				echo "PHP Notice on line <b>$errline</b> in <b>$errfile</b> :: <b>$msg_text</b><br />";
			}
			break;

		case E_USER_ERROR:
			if (isset($db))
			{
				$db->sql_close();
			}
			if (isset($cache))
			{
				$cache->unload();
			}

			if (!defined('HEADER_INC'))
			{
				echo '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"><html><head><meta http-equiv="Content-Type" content="text/html; charset=iso-8869-1"><meta http-equiv="Content-Style-Type" content="text/css"><link rel="stylesheet" href="' . $phpbb_root_path . 'adm/subSilver.css" type="text/css"><style type="text/css">' . "\n";
				echo 'th { background-image: url(\'' . $phpbb_root_path . 'adm/images/cellpic3.gif\') }' . "\n";
				echo 'td.cat	{ background-image: url(\'' . $phpbb_root_path . 'adm/images/cellpic1.gif\') }' . "\n";
				echo '</style><title>' . $msg_title . '</title></head><body>';
				echo '<table width="100%" cellspacing="0" cellpadding="0" border="0"><tr><td><img src="' . $phpbb_root_path . 'adm/images/header_left.jpg" width="200" height="60" alt="phpBB Logo" title="phpBB Logo" border="0"/></td><td width="100%" background="' . $phpbb_root_path . 'adm/images/header_bg.jpg" height="60" align="right" nowrap="nowrap"><span class="maintitle">General Error</span> &nbsp; &nbsp; &nbsp;</td></tr></table>';
			}
			echo '<br clear="all" /><table width="85%" cellspacing="0" cellpadding="0" border="0" align="center"><tr><td><br clear="all" />' . $msg_text . '<hr />Please notify the board administrator or webmaster : <a href="mailto:' . $config['board_contact'] . '">' . $config['board_contact'] . '</a></td></tr></table><br clear="all" /></body></html>';

			exit;
			break;

		case E_USER_NOTICE:
			// 20021125 Bartvb (todo)
			// This is a hack just to show something useful.
			// $msg_text won't contain anything if $user isn't there yet.
			// I ran into this problem when installing without makeing config_cache.php writable
			if (!isset($user))
			{
				die("Unable to show notice, \$user class hasn't been instantiated yet.<br />Error triggered in: " . $errfile .":". $errline);
			}

			if (empty($user->data))
			{
				$user->start();
			}
			if (empty($user->lang))
			{
				$user->setup();
			}

			$msg_text = (!empty($user->lang[$msg_text])) ? $user->lang[$msg_text] : $msg_text;

			if (!defined('HEADER_INC'))
			{
				if (defined('IN_ADMIN'))
				{
					page_header('', '', false);
				}
				else
				{
					include($phpbb_root_path . 'includes/page_header.' . $phpEx);
				}
			}

			if (defined('IN_ADMIN'))
			{
				page_message($msg_title, $msg_text, $display_header);
				page_footer();
			}
			else
			{
				$template->set_filenames(array(
					'body' => 'message_body.html')
				);

				$template->assign_vars(array(
					'MESSAGE_TITLE'	=> $msg_title,
					'MESSAGE_TEXT'	=> $msg_text)
				);

				include($phpbb_root_path . 'includes/page_tail.' . $phpEx);
			}
			exit;
			break;
	}
}
/*
//
function page_header($page_title = '')
{
	global $db, $config, $template, $user, $auth, $cache;

	define('HEADER_INC', TRUE);

	// gzip_compression
	if ($config['gzip_compress'])
	{
		if (extension_loaded('zlib') && !headers_sent())
		{
			ob_start('ob_gzhandler');
		}
	}

	// Generate logged in/logged out status
	if ($user->data['user_id'] != ANONYMOUS)
	{
		$u_login_logout = 'ucp.'.$phpEx. $SID . '&amp;mode=logout';
		$l_login_logout = sprintf($user->lang['LOGOUT_USER'], $user->data['username']);
	}
	else
	{
		$u_login_logout = 'ucp.'.$phpEx . $SID . '&amp;mode=login';
		$l_login_logout = $user->lang['LOGIN'];
	}

	// Last visit date/time
	$s_last_visit = ($user->data['user_id'] != ANONYMOUS) ? $user->format_date($user->data['session_last_visit']) : '';

	// Get users online list ... if required
	$l_online_users = $online_userlist = $l_online_record = '';
	if (!empty($config['load_online']) && !empty($config['load_online_time']))
	{
		$userlist_ary = $userlist_visible = array();
		$logged_visible_online = $logged_hidden_online = $guests_online = 0;

		$prev_user_id = 0;
		$prev_user_ip = $reading_sql = '';
		if (!empty($_REQUEST['f']))
		{
			$reading_sql = "AND s.session_page LIKE '%f=" . intval($_REQUEST['f']) . "%'";
		}

		$sql = "SELECT u.username, u.user_id, u.user_allow_viewonline, u.user_colour, s.session_ip, s.session_allow_viewonline
			FROM " . USERS_TABLE . " u, " . SESSIONS_TABLE ." s
			WHERE s.session_time >= " . (time() - (intval($config['load_online_time']) * 60)) . "
				$reading_sql
				AND u.user_id = s.session_user_id
			ORDER BY u.username ASC, s.session_ip ASC";
		$result = $db->sql_query($sql, false);

		while ($row = $db->sql_fetchrow($result))
		{
			// User is logged in and therefor not a guest
			if ($row['user_id'] != ANONYMOUS)
			{
				// Skip multiple sessions for one user
				if ($row['user_id'] != $prev_user_id)
				{
					if ($row['user_colour'])
					{
						$row['username'] = '<b style="color:#' . $row['user_colour'] . '">' . $row['username'] . '</b>';
					}

					if ($row['user_allow_viewonline'] && $row['session_allow_viewonline'])
					{
						$user_online_link = $row['username'];
						$logged_visible_online++;
					}
					else
					{
						$user_online_link = '<i>' . $row['username'] . '</i>';
						$logged_hidden_online++;
					}

					if ($row['user_allow_viewonline'] || $auth->acl_get('u_viewonline'))
					{
						$user_online_link = '<a href="' . "memberlist.$phpEx$SID&amp;mode=viewprofile&amp;u=" . $row['user_id'] . '">' . $user_online_link . '</a>';
						$online_userlist .= ($online_userlist != '') ? ', ' . $user_online_link : $user_online_link;
					}
				}

				$prev_user_id = $row['user_id'];
			}
			else
			{
				// Skip multiple sessions for one user
				if ($row['session_ip'] != $prev_session_ip)
				{
					$guests_online++;
				}
			}

			$prev_session_ip = $row['session_ip'];
		}

		if ($online_userlist == '')
		{
			$online_userlist = $user->lang['NONE'];
		}

		if (empty($_REQUEST['f']))
		{
			$online_userlist = $user->lang['Registered_users'] . ' ' . $online_userlist;
		}
		else
		{
			$l_online = ($guests_online == 1) ? $user->lang['Browsing_forum_guest'] : $user->lang['Browsing_forum_guests'];
			$online_userlist = sprintf($l_online, $online_userlist, $guests_online);
		}

		$total_online_users = $logged_visible_online + $logged_hidden_online + $guests_online;

		if ($total_online_users > $config['record_online_users'])
		{
			set_config('record_online_users', $total_online_users, TRUE);
			set_config('record_online_date', time(), TRUE);
		}

		// Build online listing
		$vars_online = array(
			'ONLINE'=> array('total_online_users', 'l_t_user_s'),
			'REG'	=> array('logged_visible_online', 'l_r_user_s'),
			'HIDDEN'=> array('logged_hidden_online', 'l_h_user_s'),
			'GUEST'	=> array('guests_online', 'l_g_user_s')
		);

		foreach ($vars_online as $l_prefix => $var_ary)
		{
			switch ($$var_ary[0])
			{
				case 0:
					$$var_ary[1] = $user->lang[$l_prefix . '_USERS_ZERO_TOTAL'];
					break;

				case 1:
					$$var_ary[1] = $user->lang[$l_prefix . '_USER_TOTAL'];
					break;

				default:
					$$var_ary[1] = $user->lang[$l_prefix . '_USERS_TOTAL'];
					break;
			}
		}
		unset($vars_online);

		$l_online_users = sprintf($l_t_user_s, $total_online_users);
		$l_online_users .= sprintf($l_r_user_s, $logged_visible_online);
		$l_online_users .= sprintf($l_h_user_s, $logged_hidden_online);
		$l_online_users .= sprintf($l_g_user_s, $guests_online);
		$l_online_record = sprintf($user->lang['RECORD_ONLINE_USERS'], $config['record_online_users'], $user->format_date($config['record_online_date']));
		$l_online_time = ($config['load_online_time'] == 1) ? 'VIEW_ONLINE_TIME' : 'VIEW_ONLINE_TIMES';
		$l_online_time = sprintf($user->lang[$l_online_time], $config['load_online_time']);
	}

	// Obtain number of new private messages if user is logged in
	if ($user->data['user_id'] != ANONYMOUS)
	{
		if ($user->data['user_new_privmsg'])
		{
			$l_message_new = ($user->data['user_new_privmsg'] == 1) ? $user->lang['New_pm'] : $user->lang['New_pms'];
			$l_privmsgs_text = sprintf($l_message_new, $user->data['user_new_privmsg']);

			if ($user->data['user_last_privmsg'] > $user->data['session_last_visit'])
			{
				$sql = "UPDATE " . USERS_TABLE . "
					SET user_last_privmsg = " . $user->data['session_last_visit'] . "
					WHERE user_id = " . $user->data['user_id'];
				$db->sql_query($sql);

				$s_privmsg_new = 1;
			}
			else
			{
				$s_privmsg_new = 0;
			}
		}
		else
		{
			$l_privmsgs_text = $user->lang['No_new_pm'];
			$s_privmsg_new = 0;
		}

		if ($user->data['user_unread_privmsg'])
		{
			$l_message_unread = ($user->data['user_unread_privmsg'] == 1) ? $user->lang['Unread_pm'] : $user->lang['Unread_pms'];
			$l_privmsgs_text_unread = sprintf($l_message_unread, $user->data['user_unread_privmsg']);
		}
		else
		{
			$l_privmsgs_text_unread = $user->lang['No_unread_pm'];
		}
	}

	// Generate HTML required for Mozilla Navigation bar
	$nav_links_html = '';
	*
	$nav_link_proto = '<link rel="%s" href="%s" title="%s" />' . "\n";
	foreach ($nav_links as $nav_item => $nav_array)
	{
		if (!empty($nav_array['url']))
		{
			$nav_links_html .= sprintf($nav_link_proto, $nav_item, $nav_array['url'], $nav_array['title']);
		}
		else
		{
			// We have a nested array, used for items like <link rel='chapter'> that can occur more than once.
			foreach ($nav_array as $key => $nested_array)
			{
				$nav_links_html .= sprintf($nav_link_proto, $nav_item, $nested_array['url'], $nested_array['title']);
			}
		}
	}
	*

	// Which timezone?
	$tz = ($user->data['user_id'] != ANONYMOUS) ? strval(doubleval($user->data['user_timezone'])) : strval(doubleval($config['board_timezone']));

	// The following assigns all _common_ variables that may be used at any point
	// in a template.
	$template->assign_vars(array(
		'SITENAME' 						=> $config['sitename'],
		'SITE_DESCRIPTION' 				=> $config['site_desc'],
		'PAGE_TITLE' 					=> $page_title,
		'LAST_VISIT_DATE' 				=> sprintf($user->lang['YOU_LAST_VISIT'], $s_last_visit),
		'CURRENT_TIME' 					=> sprintf($user->lang['CURRENT_TIME'], $user->format_date(time())),
		'TOTAL_USERS_ONLINE' 			=> $l_online_users,
		'LOGGED_IN_USER_LIST' 			=> $online_userlist,
		'RECORD_USERS' 					=> $l_online_record,
		'PRIVATE_MESSAGE_INFO' 			=> $l_privmsgs_text,
		'PRIVATE_MESSAGE_NEW_FLAG'		=> $s_privmsg_new,
		'PRIVATE_MESSAGE_INFO_UNREAD' 	=> $l_privmsgs_text_unread,

		'L_LOGIN_LOGOUT' 	=> $l_login_logout,
		'L_INDEX' 			=> $user->lang['FORUM_INDEX'],
		'L_ONLINE_EXPLAIN'	=> $l_online_time,

		'U_PRIVATEMSGS'	=> 'ucp.'.$phpEx.$SID.'&amp;mode=pm&amp;folder=inbox',
		'U_MEMBERLIST' 	=> 'memberlist.'.$phpEx.$SID,
		'U_VIEWONLINE' 	=> 'viewonline.'.$phpEx.$SID,
		'U_MEMBERSLIST' => 'memberlist.'.$phpEx.$SID,
		'U_GROUP_CP' 	=> 'groupcp.'.$phpEx.$SID,
		'U_LOGIN_LOGOUT'=> $u_login_logout,
		'U_INDEX' 		=> 'index.'.$phpEx.$SID,
		'U_SEARCH' 		=> 'search.'.$phpEx.$SID,
		'U_REGISTER' 	=> 'ucp.'.$phpEx.$SID.'&amp;mode=register',
		'U_PROFILE' 	=> 'ucp.'.$phpEx.$SID.'&amp;mode=editprofile',
		'U_MODCP' 		=> 'mcp.'.$phpEx.$SID,
		'U_FAQ' 		=> 'faq.'.$phpEx.$SID,
		'U_SEARCH_SELF'	=> 'search.'.$phpEx.$SID.'&amp;search_id=egosearch',
		'U_SEARCH_NEW' 	=> 'search.'.$phpEx.$SID.'&amp;search_id=newposts',
		'U_SEARCH_UNANSWERED'	=> 'search.'.$phpEx.$SID.'&amp;search_id=unanswered',

		'S_USER_LOGGED_IN' 		=> ($user->data['user_id'] != ANONYMOUS) ? true : false,
		'S_USER_PM_POPUP' 		=> (!empty($user->data['user_popup_pm'])) ? true : false,
		'S_USER_BROWSER' 		=> $user->data['session_browser'],
		'S_CONTENT_DIRECTION' 	=> $user->lang['DIRECTION'],
		'S_CONTENT_ENCODING' 	=> $user->lang['ENCODING'],
		'S_CONTENT_DIR_LEFT' 	=> $user->lang['LEFT'],
		'S_CONTENT_DIR_RIGHT' 	=> $user->lang['RIGHT'],
		'S_TIMEZONE' 			=> ($user->data['user_dst'] || ($user->data['user_id'] == ANONYMOUS && $config['board_dst'])) ? sprintf($user->lang['ALL_TIMES'], $user->lang[$tz], $user->lang['tz']['dst']) : sprintf($user->lang['ALL_TIMES'], $user->lang[$tz], ''),
		'S_DISPLAY_ONLINE_LIST'	=> (!empty($config['load_online'])) ? 1 : 0,
		'S_DISPLAY_SEARCH'		=> (!empty($config['load_search'])) ? 1 : 0,
		'S_DISPLAY_PM'			=> (empty($config['privmsg_disable'])) ? 1 : 0,
		'S_DISPLAY_MEMBERLIST'	=> (isset($auth)) ? $auth->acl_get('u_viewprofile') : 0,

		'T_STYLESHEET_DATA'	=> $user->theme['css_data'],
		'T_STYLESHEET_LINK' => 'templates/' . $user->theme['css_external'],

		'NAV_LINKS' => $nav_links_html)
	);

	if ($config['send_encoding'])
	{
		header ('Content-type: text/html; charset: ' . $user->lang['ENCODING']);
	}
	header ('Cache-Control: private, no-cache="set-cookie", pre-check=0, post-check=0');
	header ('Expires: 0');
	header ('Pragma: no-cache');

	return;
}

//
function page_footer()
{
	global $db, $config, $template, $user, $auth, $cache, $starttime;

	// Close our DB connection.
	$db->sql_close();

	// Unload cache
	if (!empty($cache))
	{
		$cache->unload();
	}

	// Output page creation time
	if (defined('DEBUG'))
	{
		$mtime = explode(' ', microtime());
		$totaltime = $mtime[0] + $mtime[1] - $starttime;

		if (!empty($_REQUEST['explain']) && $auth->acl_get('a_'))
		{
			echo $db->sql_report;
			echo "<pre><b>Page generated in $totaltime seconds with " . $db->num_queries . " queries,\nspending " . $db->sql_time . ' doing SQL queries and ' . ($totaltime - $db->sql_time) . ' doing PHP things.</b></pre>';

			exit;
		}

		$debug_output = sprintf('<br /><br />[ Time : %.3fs | ' . $db->sql_num_queries() . ' Queries | GZIP : ' .  ( ( $config['gzip_compress'] ) ? 'On' : 'Off' ) . ' | Load : '  . (($user->load) ? $user->load : 'N/A'), $totaltime);

		if ($auth->acl_get('a_'))
		{
			$debug_output .= ' | <a href="' . htmlspecialchars($_SERVER['REQUEST_URI']) . '&amp;explain=1">Explain</a>';
		}
		$debug_output .= ' ]';
	}

	$template->assign_vars(array(
		'PHPBB_VERSION'	=> $config['version'],
		'ADMIN_LINK' 	=> ($auth->acl_get('a_')) ? sprintf($user->lang['ACP'], '<a href="' . "adm/index.$phpEx?sid=" . $user->data['session_id'] . '">', '</a>') . '<br /><br />' : '',
		'DEBUG_OUTPUT'	=> (defined('DEBUG')) ? $debug_output : '')
	);

	$template->display('body');

	exit;
}
*/

?>