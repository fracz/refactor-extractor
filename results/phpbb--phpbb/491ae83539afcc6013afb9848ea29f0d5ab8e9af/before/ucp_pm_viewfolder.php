<?php
/**
*
* @package ucp
* @version $Id$
* @copyright (c) 2005 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

/**
* View message folder
* Called from ucp_pm with mode == 'view' && action == 'view_folder'
*/
function view_folder($id, $mode, $folder_id, $folder, $type)
{
	global $user, $template, $auth, $db, $cache;
	global $phpbb_root_path, $config, $phpEx, $SID;

	$submit_export = (isset($_POST['submit_export'])) ? true : false;

	if (!$submit_export)
	{
		$user->add_lang('viewforum');

		// Grab icons
		$icons = array();
		$cache->obtain_icons($icons);

		$color_rows = array('marked', 'replied', 'message_reported', 'friend', 'foe');

		foreach ($color_rows as $var)
		{
			$template->assign_block_vars('pm_colour_info', array(
				'IMG'	=> $user->img("pm_{$var}", ''),
				'CLASS' => "pm_{$var}_colour",
				'LANG'	=> $user->lang[strtoupper($var) . '_MESSAGE'])
			);
		}

		$mark_options = array('mark_important', 'delete_marked');

		$s_mark_options = '';
		foreach ($mark_options as $mark_option)
		{
			$s_mark_options .= '<option value="' . $mark_option . '">' . $user->lang[strtoupper($mark_option)] . '</option>';
		}

		$friend = $foe = array();

		// Get friends and foes
		$sql = 'SELECT *
			FROM ' . ZEBRA_TABLE . '
			WHERE user_id = ' . $user->data['user_id'];
		$result = $db->sql_query($sql);

		while ($row = $db->sql_fetchrow($result))
		{
			$friend[$row['zebra_id']] = $row['friend'];
			$foe[$row['zebra_id']] = $row['foe'];
		}
		$db->sql_freeresult($result);

		$template->assign_vars(array(
			'S_UNREAD'		=> ($type == 'unread'),
			'S_MARK_OPTIONS'=> $s_mark_options)
		);
	}

	$folder_info = get_pm_from($folder_id, $folder, $user->data['user_id'], "{$phpbb_root_path}ucp.$phpEx$SID", $type);

	// Okay, lets dump out the page ...
	if (sizeof($folder_info['pm_list']))
	{
		// Build Recipient List if in outbox/sentbox - max two additional queries
		$recipient_list = $address_list = $address = array();
		if ($folder_id == PRIVMSGS_OUTBOX || $folder_id == PRIVMSGS_SENTBOX)
		{
			foreach ($folder_info['rowset'] as $message_id => $row)
			{
				$address[$message_id] = rebuild_header(array('to' => $row['to_address'], 'bcc' => $row['bcc_address']));
				$_save = array('u', 'g');
				foreach ($_save as $save)
				{
					if (isset($address[$message_id][$save]) && sizeof($address[$message_id][$save]))
					{
						foreach (array_keys($address[$message_id][$save]) as $ug_id)
						{
							$recipient_list[$save][$ug_id] = array('name' => $user->lang['NA'], 'colour' => '');
						}
					}
				}
			}

			$_types = array('u', 'g');
			foreach ($_types as $ug_type)
			{
				if (isset($recipient_list[$ug_type]) && sizeof($recipient_list[$ug_type]))
				{
					$sql = ($ug_type == 'u') ? 'SELECT user_id as id, username as name, user_colour as colour FROM ' . USERS_TABLE . ' WHERE user_id' : 'SELECT group_id as id, group_name as name, group_colour as colour FROM ' . GROUPS_TABLE . ' WHERE group_id';
					$sql .= ' IN (' . implode(', ', array_keys($recipient_list[$ug_type])) . ')';

					$result = $db->sql_query($sql);

					while ($row = $db->sql_fetchrow($result))
					{
						$recipient_list[$ug_type][$row['id']] = array('name' => $row['name'], 'colour' => $row['colour']);
					}
					$db->sql_freeresult($result);
				}
			}

			foreach ($address as $message_id => $adr_ary)
			{
				foreach ($adr_ary as $type => $id_ary)
				{
					foreach ($id_ary as $ug_id => $_id)
					{
						$address_list[$message_id][] = (($type == 'u') ? "<a href=\"{$phpbb_root_path}memberlist.$phpEx$SID&amp;mode=viewprofile&amp;u=$ug_id\">" : "<a href=\"{$phpbb_root_path}groupcp.$phpEx$SID&amp;g=$ug_id\">") . (($recipient_list[$type][$ug_id]['colour']) ? '<span style="color:#' . $recipient_list[$type][$ug_id]['colour'] . '">' : '<span>') . $recipient_list[$type][$ug_id]['name'] . '</span></a>';
					}
				}
			}

			unset($recipient_list, $address);
		}

		$url = "{$phpbb_root_path}ucp.$phpEx$SID";

		$data = array();

		$export_type = request_var('export_option', '');
		$enclosure = request_var('enclosure', '');
		$delimiter = request_var('delimiter', '');

		foreach ($folder_info['pm_list'] as $message_id)
		{
			$row = &$folder_info['rowset'][$message_id];

			if ($submit_export && ($export_type !== 'CSV' || ($delimiter !== '' && $enclosure !== '')))
			{
				include_once($phpbb_root_path . 'includes/functions_posting.'.$phpEx);
				$sql = 'SELECT p.message_text, p.bbcode_uid
					FROM ' . PRIVMSGS_TO_TABLE . ' t, ' . PRIVMSGS_TABLE . ' p, ' . USERS_TABLE . ' u
					WHERE t.user_id = ' . $user->data['user_id'] . "
						AND p.author_id = u.user_id
						AND t.folder_id = $folder_id
						AND t.msg_id = p.msg_id
						AND p.msg_id = $message_id";
				$result = $db->sql_query_limit($sql, 1);
				$message_row = $db->sql_fetchrow($result);
				$db->sql_freeresult($result);

				decode_message($message_row['message_text'], $message_row['bbcode_uid']);

				$data[] = array('subject' => censor_text($row['message_subject']), 'from' => $row['username'], 'date' => $user->format_date($row['message_time']), 'to' => ($folder_id == PRIVMSGS_OUTBOX || $folder_id == PRIVMSGS_SENTBOX) ? implode(', ', $address_list[$message_id]) : '', 'message' => $message_row['message_text']);
			}
			else if (!$submit_export || $export_type !== 'CSV')
			{
				$folder_img = ($row['unread']) ? 'folder_new' : 'folder';
				$folder_alt = ($row['unread']) ? 'NEW_MESSAGES' : 'NO_NEW_MESSAGES';

				// Generate all URIs ...
				$message_author = "<a href=\"{$phpbb_root_path}memberlist.$phpEx$SID&amp;mode=viewprofile&amp;u=" . $row['author_id'] . '">' . $row['username'] . '</a>';
				$view_message_url = "$url&amp;i=$id&amp;mode=view&amp;f=$folder_id&amp;p=$message_id";
				$remove_message_url = "$url&amp;i=compose&amp;action=delete&amp;p=$message_id";

				$row_indicator = '';
				foreach ($color_rows as $var)
				{
					if (($var != 'friend' && $var != 'foe' && $row[$var])
						||
						(($var == 'friend' || $var == 'foe') && isset(${$var}[$row['author_id']]) && ${$var}[$row['author_id']]))
					{
						$row_indicator = $var;
						break;
					}
				}

				// Send vars to template
				$template->assign_block_vars('messagerow', array(
					'PM_CLASS'			=> ($row_indicator) ? 'pm_' . $row_indicator . '_colour' : '',

					'FOLDER_ID' 		=> $folder_id,
					'MESSAGE_ID'		=> $message_id,
					'MESSAGE_AUTHOR'	=> $message_author,
					'SENT_TIME'		 	=> $user->format_date($row['message_time']),
					'SUBJECT'			=> censor_text($row['message_subject']),
					'FOLDER'			=> (isset($folder[$row['folder_id']])) ? $folder[$row['folder_id']]['folder_name'] : '',
					'U_FOLDER'			=> (isset($folder[$row['folder_id']])) ? "$url&amp;folder=" . $row['folder_id'] : '',
					'PM_ICON_IMG'		=> (!empty($icons[$row['icon_id']])) ? '<img src="' . $config['icons_path'] . '/' . $icons[$row['icon_id']]['img'] . '" width="' . $icons[$row['icon_id']]['width'] . '" height="' . $icons[$row['icon_id']]['height'] . '" alt="" title="" />' : '',
					'FOLDER_IMG'		=> $user->img($folder_img, $folder_alt),
					'PM_IMG'	 		=> ($row_indicator) ? $user->img('pm_' . $row_indicator, '') : '',
					'ATTACH_ICON_IMG'	=> ($auth->acl_get('u_download') && $row['message_attachment'] && $config['allow_pm_attach'] && $config['auth_download_pm']) ? $user->img('icon_attach', $user->lang['TOTAL_ATTACHMENTS']) : '',

					'S_PM_REPORTED'		=> (!empty($row['message_reported']) && $auth->acl_get('m_')) ? true : false,
					'S_PM_DELETED'		=> ($row['deleted']) ? true : false,

					'U_VIEW_PM'			=> ($row['deleted']) ? '' : $view_message_url,
					'U_REMOVE_PM'		=> ($row['deleted']) ? $remove_message_url : '',
					'RECIPIENTS'		=> ($folder_id == PRIVMSGS_OUTBOX || $folder_id == PRIVMSGS_SENTBOX) ? implode(', ', $address_list[$message_id]) : '',
					'U_MCP_REPORT'		=> "{$phpbb_root_path}mcp.$phpEx?sid={$user->session_id}&amp;i=reports&amp;pm=$message_id")
	//				'U_MCP_QUEUE'		=> "mcp.$phpEx?sid={$user->session_id}&amp;i=mod_queue&amp;t=$topic_id")
				);
			}
		}
		unset($folder_info['rowset']);

		$template->assign_vars(array(
			'S_SHOW_RECIPIENTS'	=> ($folder_id == PRIVMSGS_OUTBOX || $folder_id == PRIVMSGS_SENTBOX) ? true : false,
			'S_SHOW_COLOUR_LEGEND'	=> true)
		);
	}

	// Ask the user what he wants
	if ($submit_export)
	{
		if ($export_type == 'CSV' && ($delimiter === '' || $enclosure === ''))
		{
			$template->assign_var('PROMPT', true);
		}
		else
		{
			switch ($export_type)
			{
				case 'CSV':
				case 'CSV_EXCEL':
					$mimetype = 'text/csv';
					$filetype = 'csv';
					if ($export_type == 'CSV_EXCEL')
					{
						$enclosure = '"';
						$delimiter = ',';
						$newline = "\r\n";
					}
					else
					{
						$newline = "\n";
					}
					$string = '';
					foreach ($data as $value)
					{
						foreach ($value as $text)
						{
							$cell = str_replace($enclosure, $enclosure . $enclosure, $text);

							if (strpos($cell, $enclosure) !== false || strpos($cell, $delimiter) !== false || strpos($cell, $newline) !== false)
							{
								$string .= $enclosure . $text . $enclosure . $delimiter;
							}
							else
							{
								$string .= $cell . $delimiter;
							}
						}
						$string = substr($string, 0, -1) . $newline;
					}
				break;
				case 'XML':
					$mimetype = 'text/xml';
					$filetype = 'xml';
					$string = '<?xml version="1.0"?>' . "\n";
					$string .= "<messages>\n";
					foreach ($data as $value)
					{
						$string .= "\t<privmsg>\n";
						foreach ($value as $tag => $text)
						{
							$string .= "\t\t<$tag>$text</$tag>\n";
						}
						$string .= "\t</privmsg>\n";
					}
					$string .= '</messages>';
			}
			header('Pragma: no-cache');
			header("Content-Type: $mimetype; name=\"data.$filetype\"");
			header("Content-disposition: attachment; filename=data.$filetype");
			echo $string;
			exit;
		}
	}
}

/**
* Get Messages from folder/user
*
* @param unread|new|folder $type type of message
*/
function get_pm_from($folder_id, $folder, $user_id, $url, $type = 'folder')
{
	global $user, $db, $template, $config, $auth, $_POST;

	$start = request_var('start', 0);

	// Additional vars later, pm ordering is mostly different from post ordering. :/
	$sort_days	= request_var('st', 0);
	$sort_key	= request_var('sk', 't');
	$sort_dir	= request_var('sd', 'd');

	// PM ordering options
	$limit_days = array(0 => $user->lang['ALL_MESSAGES'], 1 => $user->lang['1_DAY'], 7 => $user->lang['7_DAYS'], 14 => $user->lang['2_WEEKS'], 30 => $user->lang['1_MONTH'], 90 => $user->lang['3_MONTHS'], 180 => $user->lang['6_MONTHS'], 364 => $user->lang['1_YEAR']);
	$sort_by_text = array('a' => $user->lang['AUTHOR'], 't' => $user->lang['POST_TIME'], 's' => $user->lang['SUBJECT']);
	$sort_by_sql = array('a' => 'u.username', 't' => 'p.message_time', 's' => 'p.message_subject');

	$sort_key = (!in_array($sort_key, array('a', 't', 's'))) ? 't' : $sort_key;

	$s_limit_days = $s_sort_key = $s_sort_dir = $u_sort_param = '';
	gen_sort_selects($limit_days, $sort_by_text, $sort_days, $sort_key, $sort_dir, $s_limit_days, $s_sort_key, $s_sort_dir, $u_sort_param);

	if ($type != 'folder')
	{
		$folder_sql = ($type == 'unread') ? 't.unread = 1' : 't.new = 1';
		$folder_sql .= ' AND t.folder_id NOT IN (' . PRIVMSGS_HOLD_BOX . ', ' . PRIVMSGS_NO_BOX . ')';
		$folder_id = PRIVMSGS_INBOX;
	}
	else
	{
		$folder_sql = 't.folder_id = ' . (int) $folder_id;
	}

	// Limit pms to certain time frame, obtain correct pm count
	if ($sort_days)
	{
		$min_post_time = time() - ($sort_days * 86400);

		$sql = 'SELECT COUNT(t.msg_id) AS pm_count
			FROM ' . PRIVMSGS_TO_TABLE . ' t, ' . PRIVMSGS_TABLE . " p
			WHERE $folder_sql
				AND t.user_id = $user_id
				AND t.msg_id = p.msg_id
				AND p.message_time >= $min_post_time";
		$result = $db->sql_query_limit($sql, 1);

		if (isset($_POST['sort']))
		{
			$start = 0;
		}

		$pm_count = ($row = $db->sql_fetchrow($result)) ? $row['pm_count'] : 0;
		$db->sql_freeresult($result);

		$sql_limit_time = "AND p.message_time >= $min_post_time";
	}
	else
	{
		if ($type == 'folder')
		{
			$pm_count = $folder[$folder_id]['num_messages'];
		}
		else
		{
			if (in_array($folder_id, array(PRIVMSGS_INBOX, PRIVMSGS_OUTBOX, PRIVMSGS_SENTBOX)))
			{
				$sql = 'SELECT COUNT(t.msg_id) AS pm_count
					FROM ' . PRIVMSGS_TO_TABLE . ' t, ' . PRIVMSGS_TABLE . " p
					WHERE $folder_sql
						AND t.user_id = $user_id
						AND t.msg_id = p.msg_id";
			}
			else
			{
				$sql = 'SELECT pm_count
					FROM ' . PRIVMSGS_FOLDER_TABLE . "
					WHERE folder_id = $folder_id
						AND user_id = $user_id";
			}
			$result = $db->sql_query_limit($sql, 1);
			$pm_count = ($row = $db->sql_fetchrow($result)) ? $row['pm_count'] : 0;
			$db->sql_freeresult($result);
		}

		$sql_limit_time = '';
	}

	$template->assign_vars(array(
		'PAGINATION'	=> generate_pagination("$url&amp;i=pm&amp;mode=view&amp;action=view_folder&amp;f=$folder_id&amp;$u_sort_param", $pm_count, $config['topics_per_page'], $start),
		'PAGE_NUMBER'	=> on_page($pm_count, $config['topics_per_page'], $start),
		'TOTAL_MESSAGES'=> (($pm_count == 1) ? $user->lang['VIEW_PM_MESSAGE'] : sprintf($user->lang['VIEW_PM_MESSAGES'], $pm_count)),

		'POST_IMG'		=> (!$auth->acl_get('u_sendpm')) ? $user->img('btn_locked', 'PM_LOCKED') : $user->img('btn_post_pm', 'POST_PM'),

		'REPORTED_IMG'	=> $user->img('icon_reported', 'MESSAGE_REPORTED'),

		'L_NO_MESSAGES'	=> (!$auth->acl_get('u_sendpm')) ? $user->lang['POST_PM_LOCKED'] : $user->lang['NO_MESSAGES'],

		'S_SELECT_SORT_DIR'		=> $s_sort_dir,
		'S_SELECT_SORT_KEY'		=> $s_sort_key,
		'S_SELECT_SORT_DAYS'	=> $s_limit_days,
		'S_TOPIC_ICONS'			=> ($config['enable_pm_icons']) ? true : false,

		'U_POST_NEW_TOPIC'	=> ($auth->acl_get('u_sendpm')) ? "$url&amp;i=pm&amp;mode=compose" : '',
		'S_PM_ACTION'		=> "$url&amp;i=pm&amp;mode=view&amp;action=view_folder&amp;f=$folder_id")
	);

	// Grab all pm data
	$rowset = $pm_list = array();

	// If the user is trying to reach late pages, start searching from the end
	$store_reverse = false;
	$sql_limit = $config['topics_per_page'];
	if ($start > $pm_count / 2)
	{
		$store_reverse = true;

		if ($start + $config['topics_per_page'] > $pm_count)
		{
			$sql_limit = min($config['topics_per_page'], max(1, $pm_count - $start));
		}

		// Select the sort order
		$sql_sort_order = $sort_by_sql[$sort_key] . ' ' . (($sort_dir == 'd') ? 'ASC' : 'DESC');
		$sql_start = max(0, $pm_count - $sql_limit - $start);
	}
	else
	{
		// Select the sort order
		$sql_sort_order = $sort_by_sql[$sort_key] . ' ' . (($sort_dir == 'd') ? 'DESC' : 'ASC');
		$sql_start = $start;
	}

	$sql = 'SELECT t.*, p.author_id, p.root_level, p.message_time, p.message_subject, p.icon_id, p.message_reported, p.to_address, p.message_attachment, p.bcc_address, u.username
		FROM ' . PRIVMSGS_TO_TABLE . ' t, ' . PRIVMSGS_TABLE . ' p, ' . USERS_TABLE . " u
		WHERE t.user_id = $user_id
			AND p.author_id = u.user_id
			AND $folder_sql
			AND t.msg_id = p.msg_id
			$sql_limit_time
		ORDER BY $sql_sort_order";

	$result = $db->sql_query_limit($sql, $sql_limit, $sql_start);

	while ($row = $db->sql_fetchrow($result))
	{
		$rowset[$row['msg_id']] = $row;
		$pm_list[] = $row['msg_id'];
	}
	$db->sql_freeresult($result);

	$pm_list = ($store_reverse) ? array_reverse($pm_list) : $pm_list;

	return array(
		'pm_count'	=> $pm_count,
		'pm_list'	=> $pm_list,
		'rowset'	=> $rowset
	);
}

?>