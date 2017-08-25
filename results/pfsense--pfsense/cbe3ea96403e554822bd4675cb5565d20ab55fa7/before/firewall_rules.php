<?php
/* $Id$ */
/*
	firewall_rules.php
	part of pfSense (http://www.pfsense.com)
        Copyright (C) 2005 Scott Ullrich (sullrich@gmail.com)

	originally part of m0n0wall (http://m0n0.ch/wall)
	Copyright (C) 2003-2004 Manuel Kasper <mk@neon1.net>.
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met:

	1. Redistributions of source code must retain the above copyright notice,
	   this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
	INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
	AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
	AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
	OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
	SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
	CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
	ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
	POSSIBILITY OF SUCH DAMAGE.
*/

$pgtitle = array("Firewall", "Rules");
require("guiconfig.inc");

if (!is_array($config['filter']['rule'])) {
	$config['filter']['rule'] = array();
}
filter_rules_sort();
$a_filter = &$config['filter']['rule'];

$if = $_GET['if'];
if ($_POST['if'])
	$if = $_POST['if'];

$iflist = array();

if($config['interfaces']['lan'])
	if(have_ruleint_access("lan"))
		$iflist['lan'] = "LAN";

if(have_ruleint_access("wan"))
	$iflist['wan'] = "WAN";

for ($i = 1; isset($config['interfaces']['opt' . $i]); $i++)
	if(have_ruleint_access("opt{$i}"))
		$iflist['opt' . $i] = $config['interfaces']['opt' . $i]['descr'];

if ($config['pptpd']['mode'] == "server")
	if(have_ruleint_access("pptp"))
		$iflist['pptp'] = "PPTP VPN";

if ($config['pppoe']['mode'] == "server")
	if(have_ruleint_access("pppoe"))
		$iflist['pppoe'] = "PPPoE VPN";

/* add ipsec interfaces */
if (isset($config['ipsec']['enable']) || isset($config['ipsec']['mobileclients']['enable']))
	if(have_ruleint_access("enc0"))
		$iflist["enc0"] = "IPsec";

/* add openvpn/tun interfaces */
if  ($config['installedpackages']["openvpnserver"] || $config['installedpackages']["openvpnclient"]) {
	if (is_array($config['installedpackages']["openvpnserver"]['config']) ||
		is_array($config['installedpackages']["openvpnclient"]['config']))
                	$iflist["openvpn"] = "OpenVPN";
}

if (!$if || !isset($iflist[$if])) {
	if ("any" == $if)
                $if = "FloatingRules";
        else if ("FloatingRules" != $if)
                $if = "wan";
}

$security_url = "firewall_rules.php?if=". strtolower($if);
if (!isSystemAdmin($HTTP_SERVER_VARS['AUTH_USER'])) {
	if(!in_array($security_url, $allowed)) {
		// User does not have access
//		echo "displaying error {$security_url}"; print_r($allowed);
		echo display_error_form("401", "Unauthorized. You do not have access to the page {$pagereq} for interface {$if}");
		exit;
	}
}

if ($_POST) {

	$pconfig = $_POST;

	if ($_POST['apply']) {
		$retval = 0;
		config_lock();
		$retval = filter_configure();
		config_unlock();

		if (file_exists($d_filterconfdirty_path))
			unlink($d_filterconfdirty_path);

		$savemsg = "The settings have been applied.  The firewall rules are now reloading in the background.  You can also <a href='status_filter_reload.php'>monitor</a> the reload progress.";
	}
}

if ($_GET['act'] == "del") {
        if ($a_filter[$_GET['id']]) {
                unset($a_filter[$_GET['id']]);
                write_config();
                touch($d_filterconfdirty_path);
                header("Location: firewall_rules.php?if={$if}");
                exit;
        }
}

if (isset($_POST['del_x'])) {
	/* delete selected rules */
	if (is_array($_POST['rule']) && count($_POST['rule'])) {
		foreach ($_POST['rule'] as $rulei) {
			unset($a_filter[$rulei]);
		}
		write_config();
		touch($d_filterconfdirty_path);
		header("Location: firewall_rules.php?if={$if}");
		exit;
	}
} else if ($_GET['act'] == "toggle") {
	if ($a_filter[$_GET['id']]) {
                if(isset($a_filter[$_GET['id']]['disabled']))
                        unset($a_filter[$_GET['id']]['disabled']);
                else
                        $a_filter[$_GET['id']]['disabled'] = true;
		write_config();
		touch($d_filterconfdirty_path);
		header("Location: firewall_rules.php?if={$if}");
		exit;
	}
} else {
	/* yuck - IE won't send value attributes for image buttons, while Mozilla does -
	   so we use .x/.y to fine move button clicks instead... */
	unset($movebtn);
	foreach ($_POST as $pn => $pd) {
		if (preg_match("/move_(\d+)_x/", $pn, $matches)) {
			$movebtn = $matches[1];
			break;
		}
	}
	/* move selected rules before this rule */
	if (isset($movebtn) && is_array($_POST['rule']) && count($_POST['rule'])) {
		$a_filter_new = array();

		/* copy all rules < $movebtn and not selected */
		for ($i = 0; $i < $movebtn; $i++) {
			if (!in_array($i, $_POST['rule']))
				$a_filter_new[] = $a_filter[$i];
		}

		/* copy all selected rules */
		for ($i = 0; $i < count($a_filter); $i++) {
			if ($i == $movebtn)
				continue;
			if (in_array($i, $_POST['rule']))
				$a_filter_new[] = $a_filter[$i];
		}

		/* copy $movebtn rule */
		if ($movebtn < count($a_filter))
			$a_filter_new[] = $a_filter[$movebtn];

		/* copy all rules > $movebtn and not selected */
		for ($i = $movebtn+1; $i < count($a_filter); $i++) {
			if (!in_array($i, $_POST['rule']))
				$a_filter_new[] = $a_filter[$i];
		}

		$a_filter = $a_filter_new;
		write_config();
		touch($d_filterconfdirty_path);
		header("Location: firewall_rules.php?if={$if}");
		exit;
	}
}
$closehead = false;

include("head.inc");

echo "<script type=\"text/javascript\" language=\"javascript\" src=\"/javascript/domTT/domLib.js\"></script>";
echo "<script type=\"text/javascript\" language=\"javascript\" src=\"/javascript/domTT/domTT.js\"></script>";
echo "<script type=\"text/javascript\" language=\"javascript\" src=\"/javascript/domTT/behaviour.js\"></script>";
echo "<script type=\"text/javascript\" language=\"javascript\" src=\"/javascript/domTT/fadomatic.js\"></script>";
?>
</head>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php include("fbegin.inc"); ?>
<form action="firewall_rules.php" method="post">
<script type="text/javascript" language="javascript" src="row_toggle.js">
</script>
<?php if ($savemsg) print_info_box($savemsg); ?>
<?php if (file_exists($d_filterconfdirty_path)): ?><p>
<?php print_info_box_np("The firewall rule configuration has been changed.<br>You must apply the changes in order for them to take effect.");?><br>
<?php endif; ?>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr><td class="tabnavtbl">
  <?php
	/* active tabs */
	$tab_array = array();
       if ("FloatingRules" == $if)
                        $active = true;
                else
                        $active = false;
        $tab_array[] = array("Floating", $active, "firewall_rules.php?if=FloatingRules");
	$tabscounter = 0; $i = 0; foreach ($iflist as $ifent => $ifname) {
		if ($ifent == $if)
			$active = true;
		else
			$active = false;
		$tab_array[] = array($ifname, $active, "firewall_rules.php?if={$ifent}");
	}
	display_top_tabs($tab_array);
  ?>
  </td></tr>
  <tr>
    <td>
	<div id="mainarea">
              <table class="tabcont" width="100%" border="0" cellpadding="0" cellspacing="0">
                <tr id="frheader">
                  <td width="3%" class="list">&nbsp;</td>
                  <td width="5%" class="list">&nbsp;</td>
                  <td width="6%" class="listhdrr">Proto</td>
                  <td width="15%" class="listhdrr">Source</td>
                  <td width="7%" class="listhdrr">Port</td>
                  <td width="15%" class="listhdrr">Destination</td>
                  <td width="7%" class="listhdrr">Port</td>
		  <td width="5%" class="listhdrr">Gateway</td>
		  <td width="10%" class="listhdrr">Queue</td>
		  <td width="5%" class="listhdrr">Schedule</td>
                  <td width="22%" class="listhdr">Description</td>
                  <td width="10%" class="list">
			<table border="0" cellspacing="0" cellpadding="1">
			   <tr>
				<?php
					$nrules = 0;
					for ($i = 0; isset($a_filter[$i]); $i++) {
						$filterent = $a_filter[$i];
						if ($filterent['interface'] != $if && !isset($filterent['floating']))
                                                       continue;
                                               if (isset($filterent['floating']) && "FloatingRules" != $if)
							continue;
						$nrules++;
					}
				?>
				<td>
				<?php if ($nrules == 0): ?>
				<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_x_d.gif" width="17" height="17" title="delete selected rules" border="0"><?php else: ?>
				<input name="del" type="image" src="./themes/<?= $g['theme']; ?>/images/icons/icon_x.gif" width="17" height="17" title="delete selected rules" onclick="return confirm('Do you really want to delete the selected rules?')"><?php endif; ?>
				</td>
				<td align="center" valign="middle"><a href="firewall_rules_edit.php?if=<?=$if;?>&after=-1"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" title="add new rule" width="17" height="17" border="0"></a></td>
			   </tr>
			</table>
		  </td>
		</tr>
<?php if (($if == "wan") && isset($config['interfaces']['wan']['blockpriv'])): ?>
                <tr valign="top" id="frrfc1918">
                  <td width="3%" class="list">&nbsp;</td>
                  <td class="listt" align="center"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_block.gif" width="11" height="11" border="0"></td>
                  <td class="listlr" style="background-color: #e0e0e0">*</td>
                  <td class="listr" style="background-color: #e0e0e0">RFC 1918 networks</td>
                  <td class="listr" style="background-color: #e0e0e0">*</td>
                  <td class="listr" style="background-color: #e0e0e0">*</td>
                  <td class="listr" style="background-color: #e0e0e0">*</td>
                  <td class="listr" style="background-color: #e0e0e0">*</td>
		<td class="listr" style="background-color: #e0e0e0">Default</td>
	 		 <td class="listr" style="background-color: #e0e0e0">*</td>
                  <td class="listbg" style="background-color: #990000"><font color="white">Block private networks</td>
                  <td valign="middle" nowrap class="list">
				    <table border="0" cellspacing="0" cellpadding="1">
					<tr>
					  <td><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_left_d.gif" width="17" height="17" title="move selected rules before this rule"></td>
					  <td><a href="interfaces_wan.php#rfc1918"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_e.gif" title="edit rule" width="17" height="17" border="0"></a></td>
					</tr>
					<tr>
					  <td align="center" valign="middle"></td>
					  <td><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus_d.gif" title="add a new rule based on this one" width="17" height="17" border="0"></td>
					</tr>
					</table>
				  </td>
				</tr>
<?php endif; ?>
<?php if (($if == "wan") && isset($config['interfaces']['wan']['blockbogons'])): ?>
                <tr valign="top" id="frrfc1918">
                  <td width="3%" class="list">&nbsp;</td>
                  <td class="listt" align="center"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_block.gif" width="11" height="11" border="0"></td>
                  <td class="listlr" style="background-color: #e0e0e0">*</td>
                  <td class="listr" style="background-color: #e0e0e0">Reserved/not assigned by IANA</td>
                  <td class="listr" style="background-color: #e0e0e0">*</td>
                  <td class="listr" style="background-color: #e0e0e0">*</td>
                  <td class="listr" style="background-color: #e0e0e0">*</td>
				  <td class="listr" style="background-color: #e0e0e0">*</td>
				   <td class="listr" style="background-color: #e0e0e0">*</td>
                  <td class="listbg" style="background-color: #990000"><font color="white">Block private networks</td>
                  <td valign="middle" nowrap class="list">
				    <table border="0" cellspacing="0" cellpadding="1">
					<tr>
					  <td><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_left_d.gif" width="17" height="17" title="move selected rules before this rule"></td>
					  <td><a href="interfaces_wan.php#rfc1918"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_e.gif" title="edit rule" width="17" height="17" border="0"></a></td>
					</tr>
					<tr>
					  <td align="center" valign="middle"></td>
					  <td><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus_d.gif" title="add a new rule based on this one" width="17" height="17" border="0"></td>
					</tr>
					</table>
				  </td>
				</tr>
<?php endif; ?>
				<?php $nrules = 0; for ($i = 0; isset($a_filter[$i]); $i++):
					$filterent = $a_filter[$i];
					if ($filterent['interface'] != $if && !isset($filterent['floating']))
                                               continue;
                                        if (isset($filterent['floating']) && "FloatingRules" != $if)
                                                continue;
				?>
                <tr valign="top" id="fr<?=$nrules;?>">
                  <td class="listt"><input type="checkbox" id="frc<?=$nrules;?>" name="rule[]" value="<?=$i;?>" onClick="fr_bgcolor('<?=$nrules;?>')" style="margin: 0; padding: 0; width: 15px; height: 15px;"></td>
                  <td class="listt" align="center">
				  <?php if ($filterent['type'] == "block")
				  			$iconfn = "block";
						else if ($filterent['type'] == "reject") {
							if ($filterent['protocol'] == "tcp" || $filterent['protocol'] == "udp" || $filterent['protocol'] == "tcp/udp")
								$iconfn = "reject";
							else
								$iconfn = "block";
						} else
							$iconfn = "pass";
						if (isset($filterent['disabled'])) {
							$textss = "<span class=\"gray\">";
							$textse = "</span>";
							$iconfn .= "_d";
						} else {
							$textss = $textse = "";
						}
				  ?>
				  <a href="?if=<?=$if;?>&act=toggle&id=<?=$i;?>"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_<?=$iconfn;?>.gif" width="11" height="11" border="0" title="click to toggle enabled/disabled status"></a>
				  <?php if (isset($filterent['log'])):
							$iconfnlog = "log_s";
						if (isset($filterent['disabled']))
							$iconfnlog .= "_d";
				  	?>
				  <br><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_<?=$iconfnlog;?>.gif" width="11" height="15" border="0">
				  <?php endif; ?>
				  </td>
				<?php

				//build Alias popup box
				$span_end = "";
				$alias_src_span_begin = "";
				$alias_src_port_span_begin = "";
				$alias_dst_span_begin = "";
				$alias_dst_port_span_begin = "";

				$alias_popup = rule_popup($filterent['source']['address'],pprint_port($filterent['source']['port']),$filterent['destination']['address'],pprint_port($filterent['destination']['port']));
				$span_end = "</U></span>";

				$alias_src_span_begin = $alias_popup["src"];

				$alias_src_port_span_begin = $alias_popup["srcport"];

				$alias_dst_span_begin = $alias_popup["dst"];

				$alias_dst_port_span_begin = $alias_popup["dstport"];

				//build Schedule popup box
				$a_schedules = &$config['schedules']['schedule'];
				$schedule_span_begin = "";
				$schedule_span_end = "";
				$sched_caption = "";
				$sched_content = "";
				$schedstatus = false;
				$dayArray = array ('Mon','Tues','Wed','Thur','Fri','Sat','Sun');
				$monthArray = array ('January','February','March','April','May','June','July','August','September','October','November','December');
				if($config['schedules']['schedule'] <> "" and is_array($config['schedules']['schedule'])){
					foreach ($a_schedules as $schedule)
					{
						if ($schedule['name'] == $filterent['sched'] ){
							$schedstatus = get_time_based_rule_status($schedule);

							foreach($schedule['timerange'] as $timerange) {
								$tempFriendlyTime = "";
								$tempID = "";
								$firstprint = false;
								if ($timerange){
									$dayFriendly = "";
									$tempFriendlyTime = "";

									//get hours
									$temptimerange = $timerange['hour'];
									$temptimeseparator = strrpos($temptimerange, "-");

									$starttime = substr ($temptimerange, 0, $temptimeseparator);
									$stoptime = substr ($temptimerange, $temptimeseparator+1);

									if ($timerange['month']){
										$tempmontharray = explode(",", $timerange['month']);
										$tempdayarray = explode(",",$timerange['day']);
										$arraycounter = 0;
										$firstDayFound = false;
										$firstPrint = false;
										foreach ($tempmontharray as $monthtmp){
											$month = $tempmontharray[$arraycounter];
											$day = $tempdayarray[$arraycounter];

											if (!$firstDayFound)
											{
												$firstDay = $day;
												$firstmonth = $month;
												$firstDayFound = true;
											}

											$currentDay = $day;
											$nextDay = $tempdayarray[$arraycounter+1];
											$currentDay++;
											if (($currentDay != $nextDay) || ($tempmontharray[$arraycounter] != $tempmontharray[$arraycounter+1])){
												if ($firstPrint)
													$dayFriendly .= ", ";
												$currentDay--;
												if ($currentDay != $firstDay)
													$dayFriendly .= $monthArray[$firstmonth-1] . " " . $firstDay . " - " . $currentDay ;
												else
													$dayFriendly .=  $monthArray[$month-1] . " " . $day;
												$firstDayFound = false;
												$firstPrint = true;
											}
											$arraycounter++;
										}
									}
									else
									{
										$tempdayFriendly = $timerange['position'];
										$firstDayFound = false;
										$tempFriendlyDayArray = explode(",", $tempdayFriendly);
										$currentDay = "";
										$firstDay = "";
										$nextDay = "";
										$counter = 0;
										foreach ($tempFriendlyDayArray as $day){
											if ($day != ""){
												if (!$firstDayFound)
												{
													$firstDay = $tempFriendlyDayArray[$counter];
													$firstDayFound = true;
												}
												$currentDay =$tempFriendlyDayArray[$counter];
												//get next day
												$nextDay = $tempFriendlyDayArray[$counter+1];
												$currentDay++;
												if ($currentDay != $nextDay){
													if ($firstprint)
														$dayFriendly .= ", ";
													$currentDay--;
													if ($currentDay != $firstDay)
														$dayFriendly .= $dayArray[$firstDay-1] . " - " . $dayArray[$currentDay-1];
													else
														$dayFriendly .= $dayArray[$firstDay-1];
													$firstDayFound = false;
													$firstprint = true;
												}
												$counter++;
											}
										}
									}
									$timeFriendly = $starttime . " - " . $stoptime;
									$description = $timerange['rangedescr'];
									$sched_content .= $dayFriendly . "; " . $timeFriendly . "<br>";
								}
							}
							$sched_caption = $schedule['descr'];
							$schedule_span_begin = "<span style=\"cursor: help;\" onmouseover=\"domTT_activate(this, event, 'content', '<h1>$sched_caption</h1><p>$sched_content</p>', 'trail', true, 'delay', 0, 'fade', 'both', 'fadeMax', 93, 'styleClass', 'niceTitle');\" onmouseout=\"this.style.color = ''; domTT_mouseout(this, event);\"><U>";
							$schedule_span_end = "</U></span>";
						}
					}
				}
				$printicon = false;
				$alttext = "";
				$image = "";
				if (!isset($filterent['disabled'])){
					 if ($schedstatus)
					 {
					 	if ($iconfn == "block" || $iconfn == "reject")
					 	{
					 		$image = "icon_block";
					 		$alttext = "Traffic matching this rule is currently being denied";
					 	}
					 	else
					 	{
					 		$image = "icon_pass";
					 		$alttext = "Traffic matching this rule is currently being allowed";
					 	}
					 	$printicon = true;
					  }
					  else if ($filterent['sched'])
					  {
					 	if ($iconfn == "block" || $iconfn == "reject")
					 	{
					 		$image = "icon_block_d";
					 		$alttext = "Traffic matching this rule is currently being allowed";
					 	}
					 	else
					 	{
					 		$image = "icon_block";
					 		$alttext = "Traffic matching this rule is currently being denied";
					 	}
					 	$printicon = true;
					  }
				}
				?>
                  <td class="listlr" onClick="fr_toggle(<?=$nrules;?>)" id="frd<?=$nrules;?>" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';">
                    <?=$textss;?><?php if (isset($filterent['protocol'])) echo strtoupper($filterent['protocol']); else echo "*"; ?><?=$textse;?>
                  </td>
                  <td class="listr" onClick="fr_toggle(<?=$nrules;?>)" id="frd<?=$nrules;?>" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';">
				    <?=$textss;?><?php echo $alias_src_span_begin;?><?php echo htmlspecialchars(pprint_address($filterent['source']));?><?php echo $alias_src_span_end;?><?=$textse;?>
                  </td>
                  <td class="listr" onClick="fr_toggle(<?=$nrules;?>)" id="frd<?=$nrules;?>" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';">
                    <?=$textss;?><?php echo $alias_src_port_span_begin;?><?php echo htmlspecialchars(pprint_port($filterent['source']['port'])); ?><?php echo $alias_src_port_span_end;?><?=$textse;?>
                  </td>
                  <td class="listr" onClick="fr_toggle(<?=$nrules;?>)" id="frd<?=$nrules;?>" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';">
				    <?=$textss;?><?php echo $alias_dst_span_begin;?><?php echo htmlspecialchars(pprint_address($filterent['destination'])); ?><?php echo $alias_dst_span_end;?><?=$textse;?>
                  </td>
	              <td class="listr" onClick="fr_toggle(<?=$nrules;?>)" id="frd<?=$nrules;?>" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';">
                    <?=$textss;?><?php echo $alias_dst_port_span_begin;?><?php echo htmlspecialchars(pprint_port($filterent['destination']['port'])); ?><?php echo $alias_dst_port_span_end;?><?=$textse;?>
                  </td>
                  <td class="listr" onClick="fr_toggle(<?=$nrules;?>)" id="frd<?=$nrules;?>" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';">
                    <?=$textss;?><?php if (isset($config['interfaces'][$filterent['gateway']]['descr'])) echo htmlspecialchars($config['interfaces'][$filterent['gateway']]['descr']); else  echo htmlspecialchars(pprint_port($filterent['gateway'])); ?><?=$textse;?>
                  </td>
<td class="listr" onClick="fr_toggle(<?=$nrules;?>)" id="frd<?=$nrules;?>" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';"><?=$textss;?>
                          <?php
							if (isset($filterent['ackqueue']) && isset($filterent['defaultqueue'])) {
								$desc = $filterent['ackqueue'] ;
							    echo "<a href=\"firewall_shaper_queues.php?queue={$filterent['ackqueue']}&action=show\">{$desc}</a>";
								$desc = $filterent['defaultqueue'];
							    echo "/<a href=\"firewall_shaper_queues.php?queue={$filterent['defaultqueue']}&action=show\">{$desc}</a>";
							} else if (isset($filterent['defaultqueue'])) {
								$desc = $filterent['defaultqueue'];
							    echo "<a href=\"firewall_shaper_queues.php?queue={$filterent['defaultqueue']}&action=show\">{$desc}</a>"; }
							else echo "none";
						  ?><?=$textse;?>
                        </td>
                  <td class="listr" onClick="fr_toggle(<?=$nrules;?>)" id="frd<?=$nrules;?>" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';"><font color="black">
                    <?php if ($printicon) { ?><img src="./themes/<?= $g['theme']; ?>/images/icons/<?php echo $image; ?>.gif" title="<?php echo $alttext;?>" border="0"><?php } ?>&nbsp;<?=$textss;?><?php echo $schedule_span_begin;?><?=htmlspecialchars($filterent['sched']);?><?php echo $schedule_span_end; ?><?=$textse;?>
                  </td>
                  <td class="listbg" onClick="fr_toggle(<?=$nrules;?>)" ondblclick="document.location='firewall_rules_edit.php?id=<?=$i;?>';" bcolor="#990000"><font color="white">
                    <?=$textss;?><?=htmlspecialchars($filterent['descr']);?>&nbsp;<?=$textse;?>
                  </td>
                  <td valign="middle" nowrap class="list">
				    <table border="0" cellspacing="0" cellpadding="1">
					<tr>
					  <td><input name="move_<?=$i;?>" type="image" src="./themes/<?= $g['theme']; ?>/images/icons/icon_left.gif" width="17" height="17" title="move selected rules before this rule" onMouseOver="fr_insline(<?=$nrules;?>, true)" onMouseOut="fr_insline(<?=$nrules;?>, false)"></td>
					  <td><a href="firewall_rules_edit.php?id=<?=$i;?>"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_e.gif" title="edit rule" width="17" height="17" border="0"></a></td>
					</tr>
					<tr>
					  <td align="center" valign="middle"><a href="firewall_rules.php?act=del&if=<?=$if;?>&id=<?=$i;?>"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_x.gif" width="17" height="17" border="0" title="delete rule" onclick="return confirm('Do you really want to delete this rule?')"></a></td>
					  <td><a href="firewall_rules_edit.php?dup=<?=$i;?>"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" title="add a new rule based on this one" width="17" height="17" border="0"></a></td>
					</tr>
					</table>
				  </td>
				</tr>
			  <?php $nrules++; endfor; ?>
			  <?php if ($nrules == 0): ?>
              <td class="listt"></td>
			  <td class="listt"></td>
			  <td class="listlr" colspan="9" align="center" valign="middle">
			  <span class="gray">
			  No rules are currently defined for this interface.<br>
			  All incoming connections on this interface will be blocked until you add pass rules.<br><br>
			  Click the <a href="firewall_rules_edit.php?if=<?=$if;?>"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" title="add new rule" border="0" width="17" height="17" align="absmiddle"></a> button to add a new rule.</span>
			  </td>
			  <?php endif; ?>
                <tr id="fr<?=$nrules;?>">
                  <td class="list"></td>
                  <td class="list"></td>
                  <td class="list">&nbsp;</td>
                  <td class="list">&nbsp;</td>
                  <td class="list">&nbsp;</td>
		  <td class="list">&nbsp;</td>
		  <td class="list">&nbsp;</td>
                  <td class="list">&nbsp;</td>
                  <td class="list">&nbsp;</td>
                  <td class="list">&nbsp;</td>
                  <td class="list">&nbsp;</td>
                  <td class="list">
				    <table border="0" cellspacing="0" cellpadding="1">
					<tr>
				      <td>
					  <?php if ($nrules == 0): ?><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_left_d.gif" width="17" height="17" title="move selected rules to end" border="0"><?php else: ?><input name="move_<?=$i;?>" type="image" src="./themes/<?= $g['theme']; ?>/images/icons/icon_left.gif" width="17" height="17" title="move selected rules to end" onMouseOver="fr_insline(<?=$nrules;?>, true)" onMouseOut="fr_insline(<?=$nrules;?>, false)"><?php endif; ?></td>
					  <td></td>
				    </tr>
					<tr>
					  <td>
					  <?php if ($nrules == 0): ?>
					  <img src="./themes/<?= $g['theme']; ?>/images/icons/icon_x_d.gif" width="17" height="17" title="delete selected rules" border="0"><?php else: ?>
					  <input name="del" type="image" src="./themes/<?= $g['theme']; ?>/images/icons/icon_x.gif" width="17" height="17" title="delete selected rules" onclick="return confirm('Do you really want to delete the selected rules?')"><?php endif; ?>
					  </td>
			                  <td><a href="firewall_rules_edit.php?if=<?=$if;?>"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" title="add new rule" width="17" height="17" border="0"></a></td>
					</tr>
				    </table>
				  </td>
				</tr>
              </table>
	      <table class="tabcont" width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td width="16"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_pass.gif" width="11" height="11"></td>
                  <td>pass</td>
                  <td width="14"></td>
                  <td width="16"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_block.gif" width="11" height="11"></td>
                  <td>block</td>
                  <td width="14"></td>
                  <td width="16"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_reject.gif" width="11" height="11"></td>
                  <td>reject</td>
                  <td width="14"></td>
                  <td width="16"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_log.gif" width="11" height="11"></td>
                  <td>log</td>
                </tr>
                <tr>
                  <td><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_pass_d.gif" width="11" height="11"></td>
                  <td nowrap>pass (disabled)</td>
                  <td>&nbsp;</td>
                  <td><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_block_d.gif" width="11" height="11"></td>
                  <td nowrap>block (disabled)</td>
                  <td>&nbsp;</td>
                  <td><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_reject_d.gif" width="11" height="11"></td>
                  <td nowrap>reject (disabled)</td>
                  <td>&nbsp;</td>
                  <td width="16"><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_log_d.gif" width="11" height="11"></td>
                  <td nowrap>log (disabled)</td>
                </tr>
		<tr>
		  <td colspan="10">
  <p>
  <strong><span class="red">Hint:<br>
  </span></strong>Rules are evaluated on a first-match basis (i.e.
  the action of the first rule to match a packet will be executed).
  This means that if you use block rules, you'll have to pay attention
  to the rule order. Everything that isn't explicitly passed is blocked
  by default.</p>
		 </td>
	        </tr>
              </table>
	</div>
    </td>
  </tr>
</table>
  <input type="hidden" name="if" value="<?=$if;?>">
</form>
<?php include("fend.inc"); ?>
</body>
</html>