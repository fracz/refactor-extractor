<?php
/***************************************************************************
 *                              admin_ban.php
 *                            -------------------
 *   begin                : Tuesday, Jul 31, 2001
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

if (!empty($setmodules))
{
	if (!$auth->acl_get('a_ban'))
	{
		return;
	}

	$filename = basename(__FILE__);
	$module['USER']['BAN_USERS'] = $filename . "$SID&amp;mode=user";
	$module['USER']['BAN_EMAILS'] = $filename . "$SID&amp;mode=email";
	$module['USER']['BAN_IPS'] = $filename . "$SID&amp;mode=ip";

	return;
}

define('IN_PHPBB', 1);
// Load default header
$phpbb_root_path = '../';
require($phpbb_root_path . 'extension.inc');
require('pagestart.' . $phpEx);

// Do we have ban permissions?
if (!$auth->acl_get('a_ban'))
{
	trigger_error($user->lang['NO_ADMIN']);
}


// Mode setting
$mode = (isset($_REQUEST['mode'])) ? $_REQUEST['mode'] : '';


$current_time = time();


// Start program
if (isset($_REQUEST['bansubmit']))
{
	// Grab the list of entries
	$ban = (!empty($_REQUEST['ban'])) ? $_REQUEST['ban'] : '';
	$ban_list = array_unique(explode("\n", $ban));
	$ban_list_log = implode(', ', $ban_list);


	$ban_exclude = (!empty($_POST['banexclude'])) ? 1 : 0;
	$ban_reason = (isset($_POST['banreason'])) ? $_POST['banreason'] : '';


	if (!empty($_POST['banlength']))
	{
		if ($_POST['banlength'] != -1 || empty($_POST['banlengthother']))
		{
			$ban_end = max($current_time, $current_time + (intval($_POST['banlength']) * 60));
		}
		else
		{
			$ban_other = explode('-', $_POST['banlengthother']);
			$ban_end = max($current_time, gmmktime(0, 0, 0, $ban_other[1], $ban_other[2], $ban_other[0]));
		}
	}
	else
	{
		$ban_end = 0;
	}


	$banlist = array();

	switch ($mode)
	{
		case 'user':
			$type = 'ban_userid';

			if (in_array('*', $ban_list))
			{
				$banlist[] = '*';
			}
			else
			{
				$sql = 'SELECT user_id
					FROM ' . USERS_TABLE . '
					WHERE username IN (' . implode(', ', array_diff(preg_replace('#^[\s]*(.*?)[\s]*$#', "'\\1'", $ban_list), array("''"))) . ')';
				$result = $db->sql_query($sql);

				if ($row = $db->sql_fetchrow($result))
				{
					do
					{
						$banlist[] = $row['user_id'];
					}
					while ($row = $db->sql_fetchrow($result));
				}
			}
			break;

		case 'ip':
			$type = 'ban_ip';

			foreach ($ban_list as $ban_item)
			{
				if (preg_match('#^([0-9]{1,3})\.([0-9]{1,3})\.([0-9]{1,3})\.([0-9]{1,3})[ ]*\-[ ]*([0-9]{1,3})\.([0-9]{1,3})\.([0-9]{1,3})\.([0-9]{1,3})$#', trim($ban_item), $ip_range_explode))
				{
					// Don't ask about all this, just don't ask ... !
					$ip_1_counter = $ip_range_explode[1];
					$ip_1_end = $ip_range_explode[5];

					while ($ip_1_counter <= $ip_1_end)
					{
						$ip_2_counter = ($ip_1_counter == $ip_range_explode[1]) ? $ip_range_explode[2] : 0;
						$ip_2_end = ($ip_1_counter < $ip_1_end) ? 254 : $ip_range_explode[6];

						if($ip_2_counter == 0 && $ip_2_end == 254)
						{
							$ip_2_counter = 256;
							$ip_2_fragment = 256;

							$banlist[] = "'$ip_1_counter.*'";
						}

						while ($ip_2_counter <= $ip_2_end)
						{
							$ip_3_counter = ($ip_2_counter == $ip_range_explode[2] && $ip_1_counter == $ip_range_explode[1]) ? $ip_range_explode[3] : 0;
							$ip_3_end = ($ip_2_counter < $ip_2_end || $ip_1_counter < $ip_1_end) ? 254 : $ip_range_explode[7];

							if ($ip_3_counter == 0 && $ip_3_end == 254)
							{
								$ip_3_counter = 256;
								$ip_3_fragment = 256;

								$banlist[] = "'$ip_1_counter.$ip_2_counter.*'";
							}

							while ($ip_3_counter <= $ip_3_end)
							{
								$ip_4_counter = ($ip_3_counter == $ip_range_explode[3] && $ip_2_counter == $ip_range_explode[2] && $ip_1_counter == $ip_range_explode[1]) ? $ip_range_explode[4] : 0;
								$ip_4_end = ($ip_3_counter < $ip_3_end || $ip_2_counter < $ip_2_end) ? 254 : $ip_range_explode[8];

								if ($ip_4_counter == 0 && $ip_4_end == 254)
								{
									$ip_4_counter = 256;
									$ip_4_fragment = 256;

									$banlist[] = "'$ip_1_counter.$ip_2_counter.$ip_3_counter.*'";
								}

								while ($ip_4_counter <= $ip_4_end)
								{
									$banlist[] = "'$ip_1_counter.$ip_2_counter.$ip_3_counter.$ip_4_counter'";
									$ip_4_counter++;
								}
								$ip_3_counter++;
							}
							$ip_2_counter++;
						}
						$ip_1_counter++;
					}
				}
				else if (preg_match('#^([\w\-_]\.?){2,}$#is', trim($ban_item)))
				{
					$ip_ary = gethostbynamel(trim($ban_item));

					foreach ($ip_ary as $ip)
					{
						if (!empty($ip))
						{
							$banlist[] = "'" . $ip . "'";
						}
					}
				}
				else if (preg_match('#^([0-9]{1,3})\.([0-9\*]{1,3})\.([0-9\*]{1,3})\.([0-9\*]{1,3})$#', trim($ban_item)) || preg_match('#^[a-f0-9:]+\*?$#i', trim($ban_item)))
				{
					$banlist[] = "'" . trim($ban_item) . "'";
				}
				else if (preg_match('#^\*$#', trim($ban_item)))
				{
					$banlist[] = "'*'";
				}
			}
			break;

		case 'email':
			$type = 'ban_email';

			foreach ($ban_list as $ban_item)
			{
				if (preg_match('#^.*?@*|(([a-z0-9\-]+\.)+([a-z]{2,3}))$#i', trim($ban_item)))
				{
					$banlist[] = "'" . trim($ban_item) . "'";
				}
			}
			break;
	}

	$sql = "SELECT $type
		FROM " . BANLIST_TABLE . "
		WHERE $type <> ''
			AND ban_exclude = $ban_exclude";
	$result = $db->sql_query($sql);

	if ($row = $db->sql_fetchrow($result))
	{
		$banlist_tmp = array();
		do
		{
			switch ($mode)
			{
				case 'user':
					$banlist_tmp[] = $row['ban_userid'];
					break;

				case 'ip':
					$banlist_tmp[] = "'" . $row['ban_ip'] . "'";
					break;

				case 'email':
					$banlist_tmp[] = "'" . $row['ban_email'] . "'";
					break;
			}
		}
		while ($row = $db->sql_fetchrow($result));

		$banlist = array_unique(array_diff($banlist, $banlist_tmp));
		unset($banlist_tmp);
	}

	if (sizeof($banlist))
	{
		$sql = '';
		foreach ($banlist as $ban_entry)
		{
			switch (SQL_LAYER)
			{
				case 'mysql':
				case 'mysql4':
					$sql .= (($sql != '') ? ', ' : '') . "($ban_entry, $current_time, $ban_end, $ban_exclude, '$ban_reason')";
					break;

				case 'mssql':
					$sql .= (($sql != '') ? ' UNION ALL ' : '') . " SELECT $ban_entry, $current_time, $ban_end, $ban_exclude, '$ban_reason'";
					break;

				default:
					$sql = "INSERT INTO " . BANLIST_TABLE . " ($type, ban_start, ban_end, ban_exclude, ban_reason)
						VALUES ($ban_entryx, $current_time, $ban_end, $ban_exclude, '$ban_reason')";
					$db->sql_query($sql);
					$sql = '';
			}
		}

		if ($sql != '')
		{
			$sql = "INSERT INTO " . BANLIST_TABLE . " ($type, ban_start, ban_end, ban_exclude, ban_reason)
				VALUES $sql";
			$db->sql_query($sql);
		}

		if (!$ban_exclude)
		{
			$sql = '';
			switch ($mode)
			{
				case 'user':
					$sql = "WHERE session_user_id IN (" . implode(', ', $banlist) . ")";
					break;

				case 'ip':
					$sql = "WHERE session_ip IN (" . implode(', ', $banlist) . ")";
					break;

				case 'email':
					$sql = "SELECT user_id
						FROM " . USERS_TABLE . "
						WHERE user_email IN (" . implode(', ', $banlist) . ")";
					$result = $db->sql_query($sql);

					$sql = '';
					if ($row = $db->sql_fetchrow($result))
					{
						do
						{
							$sql .= (($sql != '') ? ', ' : '') . $row['user_id'];
						}
						while ($row = $db->sql_fetchrow($result));

						$sql = "WHERE session_user_id IN (" . str_replace('*', '%', $sql) . ")";
					}
					break;
			}

			if ($sql != '')
			{
				$sql = "DELETE FROM " . SESSIONS_TABLE . "
					$sql";
				$db->sql_query($sql);
			}
		}

		// Update log
		$log_entry = ($ban_exclude) ? 'LOG_BAN_EXCLUDE_' : 'LOG_BAN_';
		add_log('admin', $log_entry . strtoupper($mode), $ban_reason, $ban_list_log);
	}

	trigger_error($user->lang['BAN_UPDATE_SUCESSFUL']);

}
else if (isset($_POST['unbansubmit']))
{
	$unban_sql = implode(', ', array_map('intval', $_POST['unban']));

	if ($unban_sql != '')
	{
		$l_unban_list = '';
		// Grab details of bans for logging information later
		switch ($mode)
		{
			case 'user':
				$sql = "SELECT u.username AS unban_info
					FROM " . USERS_TABLE . " u, " . BANLIST_TABLE . " b
					WHERE b.ban_id IN ($unban_sql)
						AND u.user_id = b.ban_userid";
				break;

			case 'email':
				$sql = "SELECT ban_email AS unban_info
					FROM " . BANLIST_TABLE . "
					WHERE ban_id IN ($unban_sql)";
				break;

			case 'ip':
				$sql = "SELECT ban_ip AS unban_info
					FROM " . BANLIST_TABLE . "
					WHERE ban_id IN ($unban_sql)";
				break;
		}
		$result = $db->sql_query($sql);

		while ($row = $db->sql_fetchrow($result))
		{
			$l_unban_list .= (($l_unban_list != '') ? ', ' : '') . $row['unban_info'];
		}

		$sql = "DELETE FROM " . BANLIST_TABLE . "
			WHERE ban_id IN ($unban_sql)";
		$db->sql_query($sql);

		add_log('admin', 'LOG_UNBAN_' . strtoupper($mode), $l_unban_list);
	}

	trigger_error($user->lang['BAN_UPDATE_SUCESSFUL']);
}

//
// Output relevant entry page
//

//
// Remove timed out bans
//
$sql = "DELETE FROM " . BANLIST_TABLE . "
	WHERE ban_end < " . time() . "
		AND ban_end <> 0";
$db->sql_query($sql);

//
// Ban length options
//
$ban_end_text = array(0 => $user->lang['PERMANENT'], 30 => $user->lang['30_MINS'], 60 => $user->lang['1_HOUR'], 360 => $user->lang['6_HOURS'], 1440 => $user->lang['1_DAY'], 10080 => $user->lang['7_DAYS'], 20160 => $user->lang['2_WEEKS'], 40320 => $user->lang['1_MONTH'], -1 => $user->lang['OTHER'] . ' -&gt; ');

$ban_end_options = '';
foreach ($ban_end_text as $length => $text)
{
	$ban_end_options .= '<option value="' . $length . '">' . $text . '</option>';
}

// Title
switch ($mode)
{
	case 'user':
		$l_title = $user->lang['BAN_USERS'];
		break;
	case 'email':
		$l_title = $user->lang['BAN_EMAILS'];
		break;
	case 'ip':
		$l_title = $user->lang['BAN_IPS'];
		break;
}

// Output page
page_header($l_title);

?>

<p><?php echo $user->lang['BAN_EXPLAIN']; ?></p>

<?php

switch ($mode)
{
	case 'user':

		$field = 'username';
		$l_ban_title = $user->lang['BAN_USERS'];
		$l_ban_explain = $user->lang['BAN_USERNAME_EXPLAIN'];
		$l_ban_exclude_explain = $user->lang['BAN_USER_EXCLUDE_EXPLAIN'];
		$l_unban_title = $user->lang['UNBAN_USERNAME'];
		$l_unban_explain = $user->lang['UNBAN_USERNAME_EXPLAIN'];
		$l_ban_cell = $user->lang['USERNAME'];
		$l_no_ban_cell = $user->lang['NO_BANNED_USERS'];
		$s_submit_extra = '<input type="submit" name="usersubmit" value="' . $user->lang['LOOK_UP_USER'] . '" class="liteoption" onclick="window.open(\'../memberlist.' . $phpEx . $SID . '&amp;mode=searchuser&amp;field=ban\', \'_phpbbsearch\', \'HEIGHT=500,resizable=yes,scrollbars=yes,WIDTH=740\');return false;" />';

		$sql = "SELECT b.*, u.user_id, u.username
			FROM " . BANLIST_TABLE . " b, " . USERS_TABLE . " u
			WHERE (b.ban_end >= " . time() . "
					OR b.ban_end = 0)
				AND u.user_id = b.ban_userid
				AND b.ban_userid <> 0
				AND u.user_id <> " . ANONYMOUS . "
			ORDER BY u.user_id ASC";
		break;

	case 'ip':

		$field = 'ban_ip';
		$l_ban_title = $user->lang['BAN_IPS'];
		$l_ban_explain = $user->lang['BAN_IP_EXPLAIN'];
		$l_ban_exclude_explain = $user->lang['BAN_IP_EXCLUDE_EXPLAIN'];
		$l_unban_title = $user->lang['UNBAN_IP'];
		$l_unban_explain = $user->lang['UNBAN_IP_EXPLAIN'];
		$l_ban_cell = $user->lang['IP_HOSTNAME'];
		$l_no_ban_cell = $user->lang['NO_BANNED_IP'];
		$s_submit_extra = '';

		$sql = "SELECT *
			FROM " . BANLIST_TABLE . "
			WHERE (ban_end >= " . time() . "
					OR ban_end = 0)
				AND ban_ip <> ''";
		break;

	case 'email':

		$field = 'ban_email';
		$l_ban_title = $user->lang['BAN_EMAILS'];
		$l_ban_explain = $user->lang['BAN_EMAIL_EXPLAIN'];
		$l_ban_exclude_explain = $user->lang['BAN_EMAIL_EXCLUDE_EXPLAIN'];
		$l_unban_title = $user->lang['UNBAN_EMAIL'];
		$l_unban_explain = $user->lang['UNBAN_EMAIL_EXPLAIN'];
		$l_ban_cell = $user->lang['EMAIL_ADDRESS'];
		$l_no_ban_cell = $user->lang['NO_BANNED_EMAIL'];
		$s_submit_extra = '';

		$sql = "SELECT *
			FROM " . BANLIST_TABLE . "
			WHERE (ban_end >= " . time() . "
					OR ban_end = 0)
				AND ban_email <> ''";
		break;
}
$result = $db->sql_query($sql);

$banned_options = '';
$ban_length = $ban_reasons = array();
if ($row = $db->sql_fetchrow($result))
{
	do
	{

		$banned_options .=  '<option' . (($row['ban_exclude']) ? ' class="sep"' : '') . ' value="' . $row['ban_id'] . '">' . $row[$field] . '</option>';

		$time_length = (!empty($row['ban_end'])) ? ($row['ban_end'] - $row['ban_start']) / 60 : 0;
		$ban_length[$row['ban_id']] = (!empty($ban_end_text[$time_length])) ? $ban_end_text[$time_length] : $user->lang['OTHER'] . ' -> ' . gmdate('Y-m-d', $row['ban_end']);

		$ban_reasons[$row['ban_id']] = addslashes($row['ban_reason']);
	}
	while ($row = $db->sql_fetchrow($result));
}
$db->sql_freeresult($result);

?>

<h1><?php echo $l_ban_title; ?></h1>

<p><?php echo $l_ban_explain; ?></p>

<script language="Javascript" type="text/javascript">
<!--

var ban_length = new Array();
<?php

	if (sizeof($ban_length))
	{
		foreach ($ban_length as $ban_id => $length)
		{
			echo "ban_length['$ban_id'] = \"$length\";\n";
		}
	}

?>

var ban_reason = new Array();
<?php

	if (sizeof($ban_reasons))
	{
		foreach ($ban_reasons as $ban_id => $reason)
		{
			echo "ban_reason['$ban_id'] = \"$reason\";\n";
		}
	}
?>

function display_details(option)
{
	document.forms[0].unbanreason.value = ban_reason[option];
	document.forms[0].unbanlength.value = ban_length[option];
}

//-->
</script>

<form method="post" action="<?php echo "admin_ban.$phpEx$SID&amp;mode=$mode"; ?>"><table class="bg" width="80%" cellspacing="1" cellpadding="4" border="0" align="center">
	<tr>
		<th colspan="2"><?php echo $l_ban_title; ?></th>
	</tr>
	<tr>
		<td class="row2" width="45%"><?php echo $l_ban_cell; ?>: </td>
		<td class="row1"><textarea cols="40" rows="3" name="ban"></textarea></td>
	</tr>
	<tr>
		<td class="row2" width="45%"><?php echo $user->lang['BAN_LENGTH']; ?>:</td>
		<td class="row1"><select name="banlength"><?php echo $ban_end_options; ?></select>&nbsp; <input type="text" name="banlengthother" maxlength="10" size="10" /></td>
	</tr>
	<tr>
		<td class="row2" width="45%"><?php echo $user->lang['BAN_EXCLUDE']; ?>: <br /><span class="gensmall"><?php echo $l_ban_exclude_explain;;?></span></td>
		<td class="row1"><input type="radio" name="banexclude" value="1" /> <?php echo $user->lang['YES']; ?> &nbsp; <input type="radio" name="banexclude" value="0" checked="checked" /> <?php echo $user->lang['NO']; ?></td>
	</tr>
	<tr>
		<td class="row2" width="45%"><?php echo $user->lang['BAN_REASON']; ?>:</td>
		<td class="row1"><input type="text" name="banreason" maxlength="255" size="40" /></td>
	</tr>
	<tr>
		<td class="cat" colspan="2" align="center"> <input type="submit" name="bansubmit" value="<?php echo $user->lang['SUBMIT']; ?>" class="mainoption" />&nbsp; <input type="reset" value="<?php echo $user->lang['RESET']; ?>" class="liteoption" />&nbsp; <?php echo $s_submit_extra; ?></td>
	</tr>
</table>

<h1><?php echo $l_unban_title; ?></h1>

<p><?php echo $l_unban_explain; ?></p>

<table class="bg" width="80%" cellspacing="1" cellpadding="4" border="0" align="center">
	<tr>
		<th colspan="2"><?php echo $l_unban_title; ?></th>
	</tr>
<?php

	if ($banned_options != '')
	{

?>
	<tr>
		<td class="row2" width="45%"><?php echo $l_ban_cell; ?>: <br /></td>
		<td class="row1"> <select name="unban[]" multiple="multiple" size="5" onchange="display_details(this.options[this.selectedIndex].value)"><?php echo $banned_options; ?></select></td>
	</tr>
	<tr>
		<td class="row2" width="45%"><?php echo $user->lang['BAN_REASON']; ?>:</td>
		<td class="row1"><input class="row1" style="border:0px" type="text" name="unbanreason" size="40" /></td>
	</tr>
	<tr>
		<td class="row2" width="45%"><?php echo $user->lang['BAN_LENGTH']; ?>:</td>
		<td class="row1"><input class="row1" style="border:0px" type="text" name="unbanlength" size="40" /></td>
	</tr>
	<tr>
		<td class="cat" colspan="2" align="center"><input type="submit" name="unbansubmit" value="<?php echo $user->lang['SUBMIT']; ?>" class="mainoption" />&nbsp; <input type="reset" value="<?php echo $user->lang['RESET']; ?>" class="liteoption" /></td>
	</tr>
<?php

	}
	else
	{

?>
	<tr>
		<td class="row1" colspan="2" align="center"><?php echo $l_no_ban_cell;  ?></td>
	</tr>
<?php

	}

?>
</table></form>

<?php

page_footer();

?>