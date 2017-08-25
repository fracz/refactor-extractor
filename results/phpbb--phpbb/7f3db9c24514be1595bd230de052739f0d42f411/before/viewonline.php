<?php
/***************************************************************************
 *                               viewonline.php
 *                            -------------------
 *   begin                : Saturday, Feb 13, 2001
 *   copyright            : (C) 2001 The phpBB Group
 *   email                : support@phpbb.com
 *
 *   $Id$
 *
 *
 ***************************************************************************/


/***************************************************************************
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *
 ***************************************************************************/
$phpbb_root_path = "./";
include($phpbb_root_path . 'extension.inc');
include($phpbb_root_path . 'common.'.$phpEx);

$pagetype = "viewonline";
$page_title = "Who's Online";

//
// Start session management
//
$userdata = session_pagestart($user_ip, PAGE_VIEWONLINE, $session_length);
init_userprefs($userdata);
//
// End session management
//

//
// Output page header and load
// viewonline template
//
include($phpbb_root_path . 'includes/page_header.'.$phpEx);

$template->set_filenames(array(
	"body" => "viewonline_body.tpl",
	"jumpbox" => "jumpbox.tpl")
);

$jumpbox = make_jumpbox();
$template->assign_vars(array(
	"L_GO" => $lang['Go'],
	"L_JUMP_TO" => $lang['Jump_to'],
	"L_SELECT_FORUM" => $lang['Select_forum'],
	"JUMPBOX_LIST" => $jumpbox,
    "SELECT_NAME" => POST_FORUM_URL)
);
$template->assign_var_from_handle("JUMPBOX", "jumpbox");
//
// End header
//

$sql = "SELECT u.username, u.user_id, u.user_allow_viewonline, s.session_page, s.session_logged_in, s.session_time
	FROM " . USERS_TABLE . " u, " . SESSIONS_TABLE . " s
	WHERE u.user_id = s.session_user_id
		AND s.session_time >= " . (time()-300) . "
	ORDER BY s.session_time DESC";
if(!$result = $db->sql_query($sql))
{
	message_die(GENERAL_ERROR, "Couldn't obtain user/online information.", "", __LINE__, __FILE__, $sql);
}
$onlinerow = $db->sql_fetchrowset($result);

$sql = "SELECT forum_name, forum_id
	FROM " . FORUMS_TABLE;
if($forums_result = $db->sql_query($sql))
{
	while($forumsrow = $db->sql_fetchrow($forums_result))
	{
		$forum_data[$forumsrow['forum_id']] = $forumsrow['forum_name'];
	}
}
else
{
	message_die(GENERAL_ERROR, "Couldn't obtain user/online forums information.", "", __LINE__, __FILE__, $sql);
}

$template->assign_vars(array(
	"L_WHOSONLINE" => $lang['Who_is_online'],
	"L_ONLINE_EXPLAIN" => $lang['Online_explain'],
	"L_USERNAME" => $lang['Username'],
	"L_LOCATION" => $lang['Location'],
	"L_LAST_UPDATE" => $lang['Last_updated'])
);

$active_users = 0;
$guest_users = 0;

$online_count = $db->sql_numrows($result);
if($online_count)
{
	$count_reg = 0;
	$count_anon = 0;

	for($i = 0; $i < $online_count; $i++)
	{
		if($onlinerow[$i]['user_id'] != ANONYMOUS)
		{
			if($onlinerow[$i]['session_logged_in'])
			{
				if($onlinerow[$i]['user_allow_viewonline'])
				{
					$username = $onlinerow[$i]['username'];
					$hidden = FALSE;
					$logged_on = TRUE;
					$active_users++;
				}
				else
				{
					$username = $onlinerow[$i]['username'];
					$hidden = TRUE;
					$logged_on = TRUE;
					$hidden_users++;
				}
			}
			else
			{
				if($onlinerow[$i]['user_allow_viewonline'])
				{
					$username = $onlinerow[$i]['username'];
					$hidden = FALSE;
					$logged_on = FALSE;
					$guest_users++;
				}
				else
				{
					$username = $onlinerow[$i]['username'];
					$hidden = TRUE;
					$logged_on = FALSE;
					$guest_users++;
				}
			}
		}
		else
		{
			$username = $lang['Anonymous'];
			$hidden = FALSE;
			$logged_on = FALSE;
			$guest_users++;
		}

		if($onlinerow[$i]['session_page'] < 1)
		{
			switch($onlinerow[$i]['session_page'])
			{
				case PAGE_INDEX:
					$location = $lang['Forum_index'];
					$location_url = "index.$phpEx";
					break;
				case PAGE_POSTING:
					$location = $lang['Posting_message'];
					$location_url = "index.$phpEx";
					break;
				case PAGE_LOGIN:
					$location = $lang['Logging_on'];
					$location_url = "index.$phpEx";
					break;
				case PAGE_SEARCH:
					$location = $lang['Searching_forums'];
					$location_url = "search.$phpEx";
					break;
				case PAGE_PROFILE:
					$location = $lang['Viewing_profile'];
					$location_url = "index.$phpEx";
					break;
				case PAGE_VIEWONLINE:
					$location = $lang['Viewing_online'];
					$location_url = "viewonline.$phpEx";
					break;
				case PAGE_VIEWMEMBERS:
					$location = $lang['Viewing_member_list'];
					$location_url = "memberlist.$phpEx";
					break;
				case PAGE_PRIVMSGS:
					$location = $lang['Viewing_priv_msgs'];
					$location_url = "privmsg.$phpEx";
					break;
				case PAGE_FAQ:
					$location = $lang['Viewing_FAQ'];
					$location_url = "faq.$phpEx";
					break;
				default:
					$location = $lang['Forum_index'];
					$location_url = "index.$phpEx";
			}
		}
		else
		{
			$location_url = append_sid("viewforum.$phpEx?" . POST_FORUM_URL . "=" . $onlinerow[$i]['session_page']);
			$location = $forum_data[$onlinerow[$i]['session_page']];
		}

		if( $logged_on && ( !$hidden || $userdata['user_level'] == ADMIN ) )
		{
			$row_color = ( !($count_reg % 2) ) ? $theme['td_color1'] : $theme['td_color2'];
			$row_class = ( !($count_reg % 2) ) ? $theme['td_class1'] : $theme['td_class2'];
			$count_reg++;

			$template->assign_block_vars("reguserrow", array(
				"ROW_COLOR" => "#" . $row_color,
				"ROW_CLASS" => $row_class,
				"USERNAME" => $username,
				"LASTUPDATE" => create_date($board_config['default_dateformat'], $onlinerow[$i]['session_time'], $board_config['default__timezone']),
				"LOCATION" => $location,

				"U_USER_PROFILE" => append_sid("profile.$phpEx?mode=viewprofile&amp;" . POST_USERS_URL . "=" . $onlinerow[$i]['user_id']),
				"U_FORUM_LOCATION" => append_sid($location_url))
			);
		}
		else if( !$hidden || $userdata['user_level'] == ADMIN )
		{
			$row_color = ( !($count_reg % 2) ) ? $theme['td_color1'] : $theme['td_color2'];
			$row_class = ( !($count_reg % 2) ) ? $theme['td_class1'] : $theme['td_class2'];
			$count_reg++;

			$template->assign_block_vars("anonuserrow", array(
				"ROW_COLOR" => "#" . $row_color,
				"ROW_CLASS" => $row_class,
				"USERNAME" => $lang['Guest'],
				"LASTUPDATE" => create_date($board_config['default_dateformat'], $onlinerow[$i]['session_time'], $board_config['default__timezone']),
				"LOCATION" => $location,

				"U_FORUM_LOCATION" => append_sid($location_url))
			);
		}
	}

	$template->assign_vars(array(
		"ACTIVE_USERS" => $active_users,
		"HIDDEN_USERS" => $hidden_users,
		"GUEST_USERS" => $guest_users)
	);

	$template->pparse("body");
}
else
{
	message_die(GENERAL_MESSAGE, "There are no users currently browsing this forum");
}

include($phpbb_root_path . 'includes/page_tail.'.$phpEx);

?>