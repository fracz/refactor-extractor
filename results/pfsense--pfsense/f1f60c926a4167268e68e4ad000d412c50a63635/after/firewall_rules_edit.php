<?php
/* $Id$ */
/*
	firewall_rules_edit.php
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

require("guiconfig.inc");

$specialsrcdst = explode(" ", "any wanip lanip lan pptp pppoe");

if (!is_array($config['filter']['rule'])) {
	$config['filter']['rule'] = array();
}
filter_rules_sort();
$a_filter = &$config['filter']['rule'];

$id = $_GET['id'];
if (is_numeric($_POST['id']))
	$id = $_POST['id'];

$after = $_GET['after'];

if (isset($_POST['after']))
	$after = $_POST['after'];

if (isset($_GET['dup'])) {
	$id = $_GET['dup'];
	$after = $_GET['dup'];
}

if($id > -1) {
	$if = $a_filter[$id]['interface'];
	$security_url = "firewall_rules_edit.php?if=". strtolower($if);
	if (!isSystemAdmin($HTTP_SERVER_VARS['AUTH_USER'])) {
		log_error("Checking for {$security_url}");
		if(!in_array($security_url, $allowed)) {
			// User does not have access
	//		echo "displaying error {$security_url}"; print_r($allowed);
			echo display_error_form("401", "Unauthorized. You do not have access to edit rules on the interface {$if}");
			exit;
		}
	}
}

if (isset($id) && $a_filter[$id]) {
	$pconfig['interface'] = $a_filter[$id]['interface'];

	if (!isset($a_filter[$id]['type']))
		$pconfig['type'] = "pass";
	else
		$pconfig['type'] = $a_filter[$id]['type'];

	if (isset($a_filter[$id]['floating']) || $if == "FloatingRules") {
		$pconfig['floating'] = $a_filter[$id]['floating'];
		if (isset($a_filter[$id]['interface']) && $a_filter[$id]['interface'] <> "")
			$pconfig['interface'] = $a_filter[$id]['interface'];
	}

	if (isset($a_filter['floating']))
		$pconfig['floating'] = "yes";

	if (isset($a_filter[$id]['direction']))
                $pconfig['direction'] = $a_filter[$id]['direction'];

	if (isset($a_filter[$id]['protocol']))
		$pconfig['proto'] = $a_filter[$id]['protocol'];
	else
		$pconfig['proto'] = "any";

	if ($a_filter[$id]['protocol'] == "icmp")
		$pconfig['icmptype'] = $a_filter[$id]['icmptype'];

	address_to_pconfig($a_filter[$id]['source'], $pconfig['src'],
		$pconfig['srcmask'], $pconfig['srcnot'],
		$pconfig['srcbeginport'], $pconfig['srcendport']);

	if($a_filter[$id]['os'] <> "")
		$pconfig['os'] = $a_filter[$id]['os'];

	address_to_pconfig($a_filter[$id]['destination'], $pconfig['dst'],
		$pconfig['dstmask'], $pconfig['dstnot'],
		$pconfig['dstbeginport'], $pconfig['dstendport']);

	if ($a_filter[$id]['dscp'] <> "")
		$pconfig['dscp'] = $a_filter[$id]['dscp'];

	$pconfig['disabled'] = isset($a_filter[$id]['disabled']);
	$pconfig['log'] = isset($a_filter[$id]['log']);
	$pconfig['descr'] = $a_filter[$id]['descr'];

	if (isset($a_filter[$id]['tag']) && $a_filter[$id]['tag'] <> "")
		$pconfig['tag'] = $a_filter[$id]['tag'];
	if (isset($a_filter[$id]['tagged']) && $a_filter[$id]['tag'] <> "")
        	$pconfig['tagged'] = $a_filter[$id]['tagged'];
	if (isset($a_filter[$id]['quick']) && $a_filter[$id]['quick'])
		$pconfig['quick'] = $a_filter[$id]['quick'];

	/* advanced */
        $pconfig['max-src-nodes'] = $a_filter[$id]['max-src-nodes'];
        $pconfig['max-src-states'] = $a_filter[$id]['max-src-states'];
        $pconfig['statetype'] = $a_filter[$id]['statetype'];
	$pconfig['statetimeout'] = $a_filter[$id]['statetimeout'];

	$pconfig['nosync'] = isset($a_filter[$id]['nosync']);

	/* advanced - new connection per second banning*/
	$pconfig['max-src-conn-rate'] = $a_filter[$id]['max-src-conn-rate'];
	$pconfig['max-src-conn-rates'] = $a_filter[$id]['max-src-conn-rates'];

	/* Multi-WAN next-hop support */
	$pconfig['gateway'] = $a_filter[$id]['gateway'];

	/* Shaper support */
	$pconfig['defaultqueue'] = $a_filter[$id]['defaultqueue'];
	$pconfig['ackqueue'] = $a_filter[$id]['ackqueue'];

	//schedule support
	$pconfig['sched'] = $a_filter[$id]['sched'];

} else {
	/* defaults */
	if ($_GET['if'])
		$pconfig['interface'] = $_GET['if'];
	$pconfig['type'] = "pass";
	$pconfig['src'] = "any";
	$pconfig['dst'] = "any";
}
/* Allow the FlotingRules to work */
$if = $pconfig['interface'];

if (isset($_GET['dup']))
	unset($id);

if ($_POST) {

	if ($_POST['type'] == "reject" && $_POST['proto'] <> "tcp")
		$input_errors[] = "Reject type rules only works when the protocol is set to TCP.";

	if (($_POST['proto'] != "tcp") && ($_POST['proto'] != "udp") && ($_POST['proto'] != "tcp/udp")) {
		$_POST['srcbeginport'] = 0;
		$_POST['srcendport'] = 0;
		$_POST['dstbeginport'] = 0;
		$_POST['dstendport'] = 0;
	} else {

		if ($_POST['srcbeginport_cust'] && !$_POST['srcbeginport'])
			$_POST['srcbeginport'] = $_POST['srcbeginport_cust'];
		if ($_POST['srcendport_cust'] && !$_POST['srcendport'])
			$_POST['srcendport'] = $_POST['srcendport_cust'];

		if ($_POST['srcbeginport'] == "any") {
			$_POST['srcbeginport'] = 0;
			$_POST['srcendport'] = 0;
		} else {
			if (!$_POST['srcendport'])
				$_POST['srcendport'] = $_POST['srcbeginport'];
		}
		if ($_POST['srcendport'] == "any")
			$_POST['srcendport'] = $_POST['srcbeginport'];

		if ($_POST['dstbeginport_cust'] && !$_POST['dstbeginport'])
			$_POST['dstbeginport'] = $_POST['dstbeginport_cust'];
		if ($_POST['dstendport_cust'] && !$_POST['dstendport'])
			$_POST['dstendport'] = $_POST['dstendport_cust'];

		if ($_POST['dstbeginport'] == "any") {
			$_POST['dstbeginport'] = 0;
			$_POST['dstendport'] = 0;
		} else {
			if (!$_POST['dstendport'])
				$_POST['dstendport'] = $_POST['dstbeginport'];
		}
		if ($_POST['dstendport'] == "any")
			$_POST['dstendport'] = $_POST['dstbeginport'];
	}

	if (is_specialnet($_POST['srctype'])) {
		$_POST['src'] = $_POST['srctype'];
		$_POST['srcmask'] = 0;
	} else if ($_POST['srctype'] == "single") {
		$_POST['srcmask'] = 32;
	}
	if (is_specialnet($_POST['dsttype'])) {
		$_POST['dst'] = $_POST['dsttype'];
		$_POST['dstmask'] = 0;
	}  else if ($_POST['dsttype'] == "single") {
		$_POST['dstmask'] = 32;
	}

	unset($input_errors);
	$pconfig = $_POST;

	/*  run through $_POST items encoding HTML entties so that the user
	 *  cannot think he is slick and perform a XSS attack on the unwilling
	 */
	foreach ($_POST as $key => $value) {
		$temp = $value;
		if (isset($_POST['floating']) && $key == "interface")
			continue;
		$newpost = htmlentities($temp);
		if($newpost <> $temp)
			$input_errors[] = "Invalid characters detected ($temp).  Please remove invalid characters and save again.";
	}

	/* input validation */
	$reqdfields = explode(" ", "type proto src dst");
	$reqdfieldsn = explode(",", "Type,Protocol,Source,Destination");


	if($_POST['statetype'] == "modulate state" or $_POST['statetype'] == "synproxy state") {
		if( $_POST['proto'] != "tcp" )
			$input_errors[] = "{$_POST['statetype']} is only valid with protocol tcp.";
		if(($_POST['statetype'] == "synproxy state") && ($_POST['gateway'] != ""))
			$input_errors[] = "{$_POST['statetype']} is only valid if the gateway is set to 'default'.";
	}


	if (!(is_specialnet($_POST['srctype']) || ($_POST['srctype'] == "single"))) {
		$reqdfields[] = "srcmask";
		$reqdfieldsn[] = "Source bit count";
	}
	if (!(is_specialnet($_POST['dsttype']) || ($_POST['dsttype'] == "single"))) {
		$reqdfields[] = "dstmask";
		$reqdfieldsn[] = "Destination bit count";
	}

	do_input_validation($_POST, $reqdfields, $reqdfieldsn, &$input_errors);

	if (!$_POST['srcbeginport']) {
		$_POST['srcbeginport'] = 0;
		$_POST['srcendport'] = 0;
	}
	if (!$_POST['dstbeginport']) {
		$_POST['dstbeginport'] = 0;
		$_POST['dstendport'] = 0;
	}

	if (($_POST['srcbeginport'] && !alias_expand($_POST['srcbeginport']) && !is_port($_POST['srcbeginport']))) {
		$input_errors[] = "The start source port must be an alias or integer between 1 and 65535.";
	}
	if (($_POST['srcendport'] && !alias_expand($_POST['srcendport']) && !is_port($_POST['srcendport']))) {
		$input_errors[] = "The end source port must be an alias or integer between 1 and 65535.";
	}
	if (($_POST['dstbeginport'] && !alias_expand($_POST['dstbeginport']) && !is_port($_POST['dstbeginport']))) {
		$input_errors[] = "The start destination port must be an alias or integer between 1 and 65535.";
	}
	if (($_POST['dstendport'] && !alias_expand($_POST['dstbeginport']) && !is_port($_POST['dstendport']))) {
		$input_errors[] = "The end destination port must be an alias or integer between 1 and 65535.";
	}

	/* if user enters an alias and selects "network" then disallow. */
	if($_POST['srctype'] == "network") {
		if(is_alias($_POST['src']))
			$input_errors[] = "You must specify single host or alias for alias entries.";
	}
	if($_POST['dsttype'] == "network") {
		if(is_alias($_POST['dst']))
			$input_errors[] = "You must specify single host or alias for alias entries.";
	}

	if (!is_specialnet($_POST['srctype'])) {
		if (($_POST['src'] && !is_ipaddroranyalias($_POST['src']))) {
			$input_errors[] = "A valid source IP address or alias must be specified.";
		}
		if (($_POST['srcmask'] && !is_numericint($_POST['srcmask']))) {
			$input_errors[] = "A valid source bit count must be specified.";
		}
	}
	if (!is_specialnet($_POST['dsttype'])) {
		if (($_POST['dst'] && !is_ipaddroranyalias($_POST['dst']))) {
			$input_errors[] = "A valid destination IP address or alias must be specified.";
		}
		if (($_POST['dstmask'] && !is_numericint($_POST['dstmask']))) {
			$input_errors[] = "A valid destination bit count must be specified.";
		}
	}

	if ($_POST['srcbeginport'] > $_POST['srcendport']) {
		/* swap */
		$tmp = $_POST['srcendport'];
		$_POST['srcendport'] = $_POST['srcbeginport'];
		$_POST['srcbeginport'] = $tmp;
	}
	if ($_POST['dstbeginport'] > $_POST['dstendport']) {
		/* swap */
		$tmp = $_POST['dstendport'];
		$_POST['dstendport'] = $_POST['dstbeginport'];
		$_POST['dstbeginport'] = $tmp;
	}
	if ($_POST['os'])
		if( $_POST['proto'] != "tcp" )
			$input_errors[] = "OS detection is only valid with protocol tcp.";

	if ($_POST['ackqueue'] && $_POST['ackqueue'] != "none") {
		if ($_POST['defaultqueue'] == "none" )
			$input_errors[] = "You have to select a queue when you select an acknowledge queue too.";
		else if ($_POST['ackqueue'] == $_POST['defaultqueue'])
			$input_errors[] = "Acknokledge queue and Queue cannot be the same.";
	}

	if (!$input_errors) {
		$filterent = array();
		$filterent['type'] = $_POST['type'];
		if (isset($_POST['interface'] ))
			$filterent['interface'] = $_POST['interface'];

		if ($if == "FloatingRules" || isset($_POST['floating'])) {
			if (isset($_POST['tag']))
				$filterent['tag'] = $_POST['tag'];
			if (isset($_POST['tagged']))
            			$filterent['tagged'] = $_POST['tagged'];
			$filterent['direction'] = $_POST['direction'];
			if (isset($_POST['quick']) && $_POST['quick'] <> "")
				$filterent['quick'] = $_POST['quick'];
			$filterent['floating'] = "yes";
			if (isset($_POST['interface']) && count($_POST['interface']) > 0)  {
					$filterent['interface'] = implode(",", $_POST['interface']);
			}
		}

		/* Advanced options */
		$filterent['max-src-nodes'] = $_POST['max-src-nodes'];
		$filterent['max-src-states'] = $_POST['max-src-states'];
		$filterent['statetimeout'] = $_POST['statetimeout'];
		$filterent['statetype'] = $_POST['statetype'];
		$filterent['os'] = $_POST['os'];

		/* Nosync directive - do not xmlrpc sync this item */
		if($_POST['nosync'] <> "")
			$filterent['nosync'] = true;
		else
			unset($filterent['nosync']);

		/* unless both values are provided, unset the values - ticket #650 */
		if($_POST['max-src-conn-rate'] <> "" and $_POST['max-src-conn-rates'] <> "") {
			$filterent['max-src-conn-rate'] = $_POST['max-src-conn-rate'];
			$filterent['max-src-conn-rates'] = $_POST['max-src-conn-rates'];
		} else {
			unset($filterent['max-src-conn-rate']);
			unset($filterent['max-src-conn-rates']);
		}

		if ($_POST['proto'] != "any")
			$filterent['protocol'] = $_POST['proto'];
		else
			unset($filterent['protocol']);

		if ($_POST['proto'] == "icmp" && $_POST['icmptype'])
			$filterent['icmptype'] = $_POST['icmptype'];
		else
			unset($filterent['icmptype']);

		pconfig_to_address($filterent['source'], $_POST['src'],
			$_POST['srcmask'], $_POST['srcnot'],
			$_POST['srcbeginport'], $_POST['srcendport']);

		pconfig_to_address($filterent['destination'], $_POST['dst'],
			$_POST['dstmask'], $_POST['dstnot'],
			$_POST['dstbeginport'], $_POST['dstendport']);

                if ($_POST['disabled'])
                        $filterent['disabled'] = true;
                else
                        unset($filterent['disabled']);

		if ($_POST['dscp'])
			$filterent['dscp'] = $_POST['dscp'];

                if ($_POST['log'])
                        $filterent['log'] = true;
                else
                        unset($filterent['log']);
		strncpy($filterent['descr'], $_POST['descr'], 52);

		if ($_POST['gateway'] != "") {
			$filterent['gateway'] = $_POST['gateway'];
		}

		if (isset($_POST['defaultqueue']) && $_POST['defaultqueue'] != "none") {
			$filterent['defaultqueue'] = $_POST['defaultqueue'];
			if (isset($_POST['ackqueue']) && $_POST['ackqueue'] != "none")
				$filterent['ackqueue'] = $_POST['ackqueue'];
		}

		if ($_POST['sched'] != "") {
			$filterent['sched'] = $_POST['sched'];
		}

		if (isset($id) && $a_filter[$id])
			$a_filter[$id] = $filterent;
		else {
			if (is_numeric($after))
				array_splice($a_filter, $after+1, 0, array($filterent));
			else
				$a_filter[] = $filterent;
		}

		write_config();
		touch($d_filterconfdirty_path);

		if (isset($_POST['floating']))
			header("Location: firewall_rules.php?if=FloatingRules");
		else
			header("Location: firewall_rules.php?if=" . $_POST['interface']);
		exit;
	}
}

$pgtitle = array("Firewall","Rules","Edit");
$closehead = false;

$page_filename = "firewall_rules_edit.php";
include("head.inc");

?>

</head>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php include("fbegin.inc"); ?>
<?php if ($input_errors) print_input_errors($input_errors); ?>

<form action="firewall_rules_edit.php" method="post" name="iform" id="iform">
	<table width="100%" border="0" cellpadding="6" cellspacing="0">
    	<tr>
			<td width="22%" valign="top" class="vncellreq">Action</td>
			<td width="78%" class="vtable">
				<select name="type" class="formselect">
					<?php $types = explode(" ", "Pass Block Reject"); foreach ($types as $type): ?>
					<option value="<?=strtolower($type);?>" <?php if (strtolower($type) == strtolower($pconfig['type'])) echo "selected"; ?>>
					<?=htmlspecialchars($type);?>
					</option>
					<?php endforeach; ?>
				</select>
				<br/>
				<span class="vexpl">
					Choose what to do with packets that match the criteria specified below. <br/>
					Hint: the difference between block and reject is that with reject, a packet (TCP RST or ICMP port unreachable for UDP) is returned to the sender, whereas with block the packet is dropped silently. In either case, the original packet is discarded. Reject only works when the protocol is set to either TCP or UDP (but not &quot;TCP/UDP&quot;) below.
				</span>
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncellreq">Disabled</td>
			<td width="78%" class="vtable">
				<input name="disabled" type="checkbox" id="disabled" value="yes" <?php if ($pconfig['disabled']) echo "checked"; ?>>
				<strong>Disable this rule</strong><br />
				<span class="vexpl">Set this option to disable this rule without removing it from the list.</span>
			</td>
		</tr>
<?php if ($if == "FloatingRules" || isset($pconfig['floating'])): ?>
		<tr>
                        <td width="22%" valign="top" class="vncellreq"><?=gettext("Quick");?></td>
                        <td width="78%" class="vtable">
                                <input name="quick" type="checkbox" id="quick" value="yes" <?php if ($pconfig['quick']) echo "checked=\"checked\""; ?> />
                                <strong><?=gettext("Apply the action immediately on match.");?></strong><br />
                                <span class="vexpl"><?=gettext("Set this option if you need to apply this action to traffic that matches this rule immediately.");?></span>
                        </td>
                </tr>
<? endif; ?>
		<tr>
			<td width="22%" valign="top" class="vncellreq">Interface</td>
			<td width="78%" class="vtable">
<?php if ($if == "FloatingRules" || isset($pconfig['floating'])): ?>
				<select name="interface[]" multiple="true" class="formselect" size="3">
<? else: ?>
				<select name="interface" class="formselect">
<?php
   endif;
				$ifdescs = get_configured_interface_with_descr();

				foreach ($ifdescs as $ifent => $ifdesc)
        				if(have_ruleint_access($ifent))
                				$interfaces[$ifent] = $ifdesc;

					if ($config['pptpd']['mode'] == "server")
						if(have_ruleint_access("pptp"))
							$interfaces['pptp'] = "PPTP VPN";

					if ($config['pppoe']['mode'] == "server")
						if(have_ruleint_access("pppoe"))
							$interfaces['pppoe'] = "PPPoE VPN";

					/* add ipsec interfaces */
					if (isset($config['ipsec']['enable']) || isset($config['ipsec']['mobileclients']['enable']))
						if(have_ruleint_access("enc0"))
							$interfaces["enc0"] = "IPsec";

					/* add openvpn/tun interfaces */
					if  ($config['installedpackages']["openvpnserver"] || $config['installedpackages']["openvpnclient"]) {
					        if (is_array($config['installedpackages']["openvpnserver"]['config']) ||
                					is_array($config['installedpackages']["openvpnclient"]['config']))
        					$interfaces["openvpn"] = "OpenVPN";
					}


					foreach ($interfaces as $iface => $ifacename): ?>
						<option value="<?=$iface;?>" <?php if ($pconfig['interface'] <> "" && stristr($pconfig['interface'], $iface)) echo "selected"; ?>><?=gettext($ifacename);?></option>
<?php 				endforeach; ?>
				</select>
				<br />
				<span class="vexpl">Choose on which interface packets must come in to match this rule.</span>
			</td>
		</tr>
<?php if ($if == "FloatingRules" || isset($pconfig['floating'])): ?>
                <tr>
                        <td width="22%" valign="top" class="vncellreq"><?=gettext("Direction");?></td>
                        <td width="78%" class="vtable">
                                 <select name="direction" class="formselect">
                                  <?php      $directions = array('any', 'in', 'out');
                                        foreach ($directions as $direction): ?>
                                                <option value="<?=$direction;?>"
                                                <?php if ($direction == $pconfig['direction']): ?>
                                                        selected="selected"
						<?php endif; ?>
                                                ><?=$direction;?></option>
                  	                <?php endforeach; ?>
                                </select>
                        </td>
                <tr>
<?php endif; ?>
		<tr>
			<td width="22%" valign="top" class="vncellreq">Protocol</td>
			<td width="78%" class="vtable">
				<select name="proto" class="formselect" onchange="proto_change()">
<?php
				$protocols = explode(" ", "TCP UDP TCP/UDP ICMP ESP AH GRE IGMP any carp pfsync");
				foreach ($protocols as $proto): ?>
					<option value="<?=strtolower($proto);?>" <?php if (strtolower($proto) == $pconfig['proto']) echo "selected"; ?>><?=htmlspecialchars($proto);?></option>
<?php 			endforeach; ?>
				</select>
				<br />
				<span class="vexpl">Choose which IP protocol this rule should match. <br /> Hint: in most cases, you should specify <em>TCP</em> &nbsp;here.</span>
			</td>
		</tr>
		<tr id="icmpbox" name="icmpbox">
			<td valign="top" class="vncell">ICMP type</td>
			<td class="vtable">
				<select name="icmptype" class="formselect">
<?php
				$icmptypes = array(
				"" => "any",
				"echorep" => "Echo reply",
				"unreach" => "Destination unreachable",
				"squench" => "Source quench",
				"redir" => "Redirect",
				"althost" => "Alternate Host",
				"echoreq" => "Echo",
				"routeradv" => "Router advertisement",
				"routersol" => "Router solicitation",
				"timex" => "Time exceeded",
				"paramprob" => "Invalid IP header",
				"timereq" => "Timestamp",
				"timerep" => "Timestamp reply",
				"inforeq" => "Information request",
				"inforep" => "Information reply",
				"maskreq" => "Address mask request",
				"maskrep" => "Address mask reply"
				);

				foreach ($icmptypes as $icmptype => $descr): ?>
					<option value="<?=$icmptype;?>" <?php if ($icmptype == $pconfig['icmptype']) echo "selected"; ?>><?=htmlspecialchars($descr);?></option>
<?php 			endforeach; ?>
			</select>
			<br />
			<span class="vexpl">If you selected ICMP for the protocol above, you may specify an ICMP type here.</span>
		</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncellreq">Source</td>
			<td width="78%" class="vtable">
				<input name="srcnot" type="checkbox" id="srcnot" value="yes" <?php if ($pconfig['srcnot']) echo "checked"; ?>>
				<strong>not</strong>
				<br />
				Use this option to invert the sense of the match.
				<br />
				<br />
				<table border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td>Type:&nbsp;&nbsp;</td>
						<td>
							<select name="srctype" class="formselect" onChange="typesel_change()">
<?php
								$sel = is_specialnet($pconfig['src']); ?>
								<option value="any"     <?php if ($pconfig['src'] == "any") { echo "selected"; } ?>>any</option>
								<option value="single"  <?php if (($pconfig['srcmask'] == 32) && !$sel) { echo "selected"; $sel = 1; } ?>>Single host or alias</option>
								<option value="network" <?php if (!$sel) echo "selected"; ?>>Network</option>
								<?php if(have_ruleint_access("wan")): ?>
								<option value="wanip" 	<?php if ($pconfig['src'] == "wanip") { echo "selected"; } ?>>WAN address</option>
								<?php endif; ?>
								<?php if(have_ruleint_access("lan")): ?>
								<option value="lanip" 	<?php if ($pconfig['src'] == "lanip") { echo "selected"; } ?>>LAN address</option>
								<?php endif; ?>
								<?php if(have_ruleint_access("lan")): ?>
								<option value="lan"     <?php if ($pconfig['src'] == "lan") { echo "selected"; } ?>>LAN subnet</option>
								<?php endif; ?>
								<?php if(have_ruleint_access("pptp")): ?>
								<option value="pptp"    <?php if ($pconfig['src'] == "pptp") { echo "selected"; } ?>>PPTP clients</option>
								<?php endif; ?>
								<?php if(have_ruleint_access("pppoe")): ?>
								<option value="pppoe"   <?php if ($pconfig['src'] == "pppoe") { echo "selected"; } ?>>PPPoE clients</option>
								<?php endif; ?>
<?php
								$ifdisp = get_configured_interface_with_descr();
								foreach ($ifdisp as $ifent => $ifdesc): ?>
								<?php if(have_ruleint_access($ifent)): ?>
									<option value="<?=$ifent;?>" <?php if ($pconfig['src'] == $ifent) { echo "selected"; } ?>><?=htmlspecialchars($ifdesc);?> subnet</option>
									<option value="<?=$ifent;?>ip"<?php if ($pconfig['src'] ==  $ifent . "ip") { echo "selected"; } ?>>
										<?=$ifdesc?> address
									</option>
								<?php endif; ?>
<?php 							endforeach; ?>
							</select>
						</td>
					</tr>
					<tr>
						<td>Address:&nbsp;&nbsp;</td>
						<td>
							<input autocomplete='off' name="src" type="text" class="formfldalias" id="src" size="20" value="<?php if (!is_specialnet($pconfig['src'])) echo htmlspecialchars($pconfig['src']);?>"> /
							<select name="srcmask" class="formselect" id="srcmask">
<?php						for ($i = 31; $i > 0; $i--): ?>
								<option value="<?=$i;?>" <?php if ($i == $pconfig['srcmask']) echo "selected"; ?>><?=$i;?></option>
<?php 						endfor; ?>
							</select>
						</td>
					</tr>
				</table>
				<div id="showadvancedboxspr">
					<p>
					<input type="button" onClick="show_source_port_range()" value="Advanced"></input> - Show source port range</a>
				</div>
			</td>
		</tr>
		<tr style="display:none" id="sprtable" name="sprtable">
			<td width="22%" valign="top" class="vncellreq">Source port range</td>
			<td width="78%" class="vtable">
				<table border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td>from:&nbsp;&nbsp;</td>
						<td>
							<select name="srcbeginport" class="formselect" onchange="src_rep_change();ext_change()">
								<option value="">(other)</option>
								<option value="any" <?php $bfound = 0; if ($pconfig['srcbeginport'] == "any") { echo "selected"; $bfound = 1; } ?>>any</option>
<?php 							foreach ($wkports as $wkport => $wkportdesc): ?>
									<option value="<?=$wkport;?>" <?php if ($wkport == $pconfig['srcbeginport']) { echo "selected"; $bfound = 1; } ?>><?=htmlspecialchars($wkportdesc);?></option>
<?php 							endforeach; ?>
							</select>
							<input autocomplete='off' class="formfldalias" name="srcbeginport_cust" id="srcbeginport_cust" type="text" size="5" value="<?php if (!$bfound && $pconfig['srcbeginport']) echo $pconfig['srcbeginport']; ?>">
						</td>
					</tr>
					<tr>
						<td>to:</td>
						<td>
							<select name="srcendport" class="formselect" onchange="ext_change()">
								<option value="">(other)</option>
								<option value="any" <?php $bfound = 0; if ($pconfig['srcendport'] == "any") { echo "selected"; $bfound = 1; } ?>>any</option>
<?php							foreach ($wkports as $wkport => $wkportdesc): ?>
									<option value="<?=$wkport;?>" <?php if ($wkport == $pconfig['srcendport']) { echo "selected"; $bfound = 1; } ?>><?=htmlspecialchars($wkportdesc);?></option>
<?php							endforeach; ?>
							</select>
							<input autocomplete='off' class="formfldalias" name="srcendport_cust" id="srcendport_cust" type="text" size="5" value="<?php if (!$bfound && $pconfig['srcendport']) echo $pconfig['srcendport']; ?>">
						</td>
					</tr>
				</table>
				<br />
				<span class="vexpl">Specify the port or port range for the source of the packet for this rule. This is usually not equal to the destination port range (and is often &quot;any&quot;). <br /> Hint: you can leave the <em>'to'</em> field empty if you only want to filter a single port</span><br/>
				<span class="vexpl"><B>NOTE:</B> You will not need to enter anything here in 99.99999% of the circumstances.  If you're unsure, do not enter anything here!</span>
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncellreq">Source OS</td>
			<td width="78%" class="vtable">OS Type:&nbsp;
				<select name="os" id="os" class="formselect">
<?php
		           $ostypes = array(
						 "" => "any",
		                 "AIX" => "AIX",
		                 "Linux" => "Linux",
		                 "FreeBSD" => "FreeBSD",
		                 "NetBSD" => "NetBSD",
		                 "OpenBSD" => "OpenBSD",
		                 "Solaris" => "Solaris",
		                 "MacOS" => "MacOS",
		                 "Windows" => "Windows",
		                 "Novell" => "Novell",
		                 "NMAP" => "NMAP"
		           );

					foreach ($ostypes as $ostype => $descr): ?>
						<option value="<?=$ostype;?>" <?php if ($ostype == $pconfig['os']) echo "selected"; ?>><?=htmlspecialchars($descr);?></option>
<?php				endforeach; ?>
				</select>
				<br />
				Note: this only works for TCP rules
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncellreq">Destination</td>
			<td width="78%" class="vtable">
				<input name="dstnot" type="checkbox" id="dstnot" value="yes" <?php if ($pconfig['dstnot']) echo "checked"; ?>>
				<strong>not</strong>
					<br />
				Use this option to invert the sense of the match.
					<br />
					<br />
				<table border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td>Type:&nbsp;&nbsp;</td>
						<td>
							<select name="dsttype" class="formselect" onChange="typesel_change()">
<?php
								$sel = is_specialnet($pconfig['dst']); ?>
								<option value="any" <?php if ($pconfig['dst'] == "any") { echo "selected"; } ?>>any</option>
								<option value="single" <?php if (($pconfig['dstmask'] == 32) && !$sel) { echo "selected"; $sel = 1; } ?>>Single host or alias</option>
								<option value="network" <?php if (!$sel) echo "selected"; ?>>Network</option>
								<?php if(have_ruleint_access("wan")): ?>
								<option value="wanip" <?php if ($pconfig['dst'] == "wanip") { echo "selected"; } ?>>WAN address</option>
								<?php endif; ?>
								<?php if(have_ruleint_access("lan")): ?>
								<option value="lanip" <?php if ($pconfig['dst'] == "lanip") { echo "selected"; } ?>>LAN address</option>
								<?php endif; ?>
								<?php if(have_ruleint_access("lan")): ?>
								<option value="lan" <?php if ($pconfig['dst'] == "lan") { echo "selected"; } ?>>LAN subnet</option>
								<?php endif; ?>
								<?php if(have_ruleint_access("pptp")): ?>
								<option value="pptp" <?php if ($pconfig['dst'] == "pptp") { echo "selected"; } ?>>PPTP clients</option>
								<?php endif; ?>
								<?php if(have_ruleint_access("pppoe")): ?>
								<option value="pppoe" <?php if ($pconfig['dst'] == "pppoe") { echo "selected"; } ?>>PPPoE clients</option>
								<?php endif; ?>


<?php 							foreach ($ifdisp as $if => $ifdesc): ?>
								<?php if(have_ruleint_access($if)): ?>
									<option value="<?=$if;?>" <?php if ($pconfig['dst'] == $if) { echo "selected"; } ?>><?=htmlspecialchars($ifdesc);?> subnet</option>
									<option value="<?=$if;?>ip"<?php if ($pconfig['dst'] == $if . "ip") { echo "selected"; } ?>>
										<?=$ifdesc;?> address
									</option>
								<?php endif; ?>
<?php 							endforeach; ?>
							</select>
						</td>
					</tr>
					<tr>
						<td>Address:&nbsp;&nbsp;</td>
						<td>
							<input name="dst" type="text" class="formfldalias" id="dst" size="20" value="<?php if (!is_specialnet($pconfig['dst'])) echo htmlspecialchars($pconfig['dst']);?>">
							/
							<select name="dstmask" class="formselect" id="dstmask">
<?php
							for ($i = 31; $i > 0; $i--): ?>
								<option value="<?=$i;?>" <?php if ($i == $pconfig['dstmask']) echo "selected"; ?>><?=$i;?></option>
<?php						endfor; ?>
							</select>
						</td>
					</tr>
				</table>

			</td>
		</tr>
		<tr id="dprtr" name="dprtr">
			<td width="22%" valign="top" class="vncellreq">Destination port range </td>
			<td width="78%" class="vtable">
				<table border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td>from:&nbsp;&nbsp;</td>
						<td>
							<select name="dstbeginport" class="formselect" onchange="dst_rep_change();ext_change()">
								<option value="">(other)</option>
								<option value="any" <?php $bfound = 0; if ($pconfig['dstbeginport'] == "any") { echo "selected"; $bfound = 1; } ?>>any</option>
<?php 							foreach ($wkports as $wkport => $wkportdesc): ?>
									<option value="<?=$wkport;?>" <?php if ($wkport == $pconfig['dstbeginport']) { echo "selected"; $bfound = 1; }?>><?=htmlspecialchars($wkportdesc);?></option>
<?php 							endforeach; ?>
							</select>
							<input autocomplete='off' class="formfldalias" name="dstbeginport_cust" id="dstbeginport_cust" type="text" size="5" value="<?php if (!$bfound && $pconfig['dstbeginport']) echo $pconfig['dstbeginport']; ?>">
						</td>
					</tr>
					<tr>
						<td>to:</td>
						<td>
							<select name="dstendport" class="formselect" onchange="ext_change()">
								<option value="">(other)</option>
								<option value="any" <?php $bfound = 0; if ($pconfig['dstendport'] == "any") { echo "selected"; $bfound = 1; } ?>>any</option>
<?php							foreach ($wkports as $wkport => $wkportdesc): ?>
									<option value="<?=$wkport;?>" <?php if ($wkport == $pconfig['dstendport']) { echo "selected"; $bfound = 1; } ?>><?=htmlspecialchars($wkportdesc);?></option>
<?php 							endforeach; ?>
							</select>
							<input autocomplete='off' class="formfldalias" name="dstendport_cust" id="dstendport_cust" type="text" size="5" value="<?php if (!$bfound && $pconfig['dstendport']) echo $pconfig['dstendport']; ?>">
						</td>
					</tr>
				</table>
				<br />
				<span class="vexpl">
					Specify the port or port range for the destination of the packet for this rule.
						<br />
					Hint: you can leave the <em>'to'</em> field empty if you only want to filter a single port
				</span>
			</td>
		</tr>
                <tr>
                        <td width="22%" valign="top" class="vncellreq">Diffserv Code Point</td>
                        <td width="78%" class="vtable">
                                <input name="dscp" id="dscp" value="<?=htmlspecialchars($pconfig['dscp']);?>">
                                        <br />
                                <span class="vexpl">Valid values are: af11, af12, af13, af21, af22, af23, af31, af32, af33, af41, af42, af43, EF, 1-64, 0x04-0xfc.</span>
                        </td>
                </tr>
		<tr>
			<td width="22%" valign="top" class="vncellreq">Log</td>
			<td width="78%" class="vtable">
				<input name="log" type="checkbox" id="log" value="yes" <?php if ($pconfig['log']) echo "checked"; ?>>
				<strong>Log packets that are handled by this rule</strong>
					<br />
				<span class="vexpl">Hint: the firewall has limited local log space. Don't turn on logging for everything. If you want to do a lot of logging, consider using a remote syslog server (see the <a href="diag_logs_settings.php">Diagnostics: System logs: Settings</a> page).</span>
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncell">Advanced Options</td>
			<td width="78%" class="vtable">
			<div id="aoadv" name="aoadv">
				<input type="button" onClick="show_aodiv();" value="Advanced"> - Show advanced options
			</div>
			<div id="aodivmain" name="aodivmain" style="display:none">
<?php if ($if == "FloatingRules" || isset($pconfig['floating'])): ?>
                <input type="hidden" id="floating" name="floating" value="floating">

                                <input name="tag" id="tag" value="<?=htmlspecialchars($pconfig['tag']);?>">
                                <br /><span class="vexpl"><?=gettext("You can mark a packet matching this rule and
use this mark to match on other rules. It is called <b>Policy filtering</b>");?>
                                </span><p>
<?php endif; ?>
                                <input name="tagged" id="tagged" value="<?=htmlspecialchars($pconfig['tagged']);?>"
>
                                <br /><span class="vexpl"><?=gettext("You can match packet on a mark placed before
on another rule.")?>
                                </span> <p>

				<input name="max-src-nodes" id="max-src-nodes" value="<?php echo $pconfig['max-src-nodes'] ?>"><br> Simultaneous client connection limit<p>
				<input name="max-src-states" id="max-src-states" value="<?php echo $pconfig['max-src-states'] ?>"><br> Maximum state entries per host<p>
				<input name="max-src-conn-rate" id="max-src-conn-rate" value="<?php echo $pconfig['max-src-conn-rate'] ?>"> /
				<select name="max-src-conn-rates" id="max-src-conn-rates">
					<option value=""<?php if(intval($pconfig['max-src-conn-rates']) < 1) echo " selected"; ?>></option>
<?php				for($x=1; $x<255; $x++) {
						if($x == $pconfig['max-src-conn-rates']) $selected = " selected"; else $selected = "";
						echo "<option value=\"{$x}\"{$selected}>{$x}</option>\n";
					} ?>
				</select><br />
				Maximum new connections / per second
				<p>

				<input name="statetimeout" value="<?php echo $pconfig['statetimeout'] ?>"><br>
				State Timeout in seconds
				<p />

				<p><strong>NOTE: Leave these fields blank to disable this feature.</strong>
			  </div>
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncell">State Type</td>
			<td width="78%" class="vtable">
				<div id="showadvstatebox">
					<input type="button" onClick="show_advanced_state()" value="Advanced"></input> - Show state</a>
				</div>
				<div id="showstateadv" style="display:none">
					<select name="statetype">
						<option value="keep state" <?php if(!isset($pconfig['statetype']) or $pconfig['statetype'] == "keep state") echo "selected"; ?>>keep state</option>
						<option value="modulate state" <?php if($pconfig['statetype'] == "modulate state")  echo "selected"; ?>>modulate state</option>
						<option value="synproxy state"<?php if($pconfig['statetype'] == "synproxy state")  echo "selected"; ?>>synproxy state</option>
						<option value="none"<?php if($pconfig['statetype'] == "none") echo "selected"; ?>>none</option>
					</select><br>HINT: Select which type of state tracking mechanism you would like to use.  If in doubt, use keep state.
					<p>
					<table width="90%">
						<tr><td width="25%"><ul><li>keep state</li></td><td>Works with all IP protocols.</ul></td></tr>
						<tr><td width="25%"><ul><li>modulate state</li></td><td>Works only with TCP. {$g['product_name']} will generate strong Initial Sequence Numbers (ISNs) for packets matching this rule.</li></ul></td></tr>
						<tr><td width="25%"><ul><li>synproxy state</li></td><td>Proxies incoming TCP connections to help protect servers from spoofed TCP SYN floods. This option includes the functionality of keep state and modulate state combined.</ul></td></tr>
						<tr><td width="25%"><ul><li>none</li></td><td>Do not use state mechanisms to keep track.  This is only useful if you're doing advanced queueing in certain situations.  Please check the documentation.</ul></td></tr>
					</table>
					</p>
			  </div>
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncell">No XMLRPC Sync</td>
			<td width="78%" class="vtable">
				<input type="checkbox" name="nosync"<?php if($pconfig['nosync']) echo " CHECKED"; ?>><br>
				HINT: This prevents the rule from automatically syncing to other carp members.
			</td>
		</tr>
		<?php
			//build list of schedules
			$schedules = array();
			$schedules[] = "none";//leave none to leave rule enabled all the time
			if(is_array($config['schedules']['schedule'])) {
				foreach ($config['schedules']['schedule'] as $schedule) {
					if ($schedule['name'] <> "")
						$schedules[] = $schedule['name'];
				}
			}
		?>
		<tr>
			<td width="22%" valign="top" class="vncell">Schedule</td>
			<td width="78%" class="vtable">
				<select name='sched'>
<?php
				foreach($schedules as $schedule) {
					if($schedule == $pconfig['sched']) {
						$selected = " SELECTED";
					} else {
						$selected = "";
					}
					if ($schedule == "none") {
						echo "<option value=\"\" {$selected}>{$schedule}</option>\n";
					} else {
						echo "<option value=\"{$schedule}\" {$selected}>{$schedule}</option>\n";
					}
				}?>
				</select>
				<p>Leave as 'none' to leave the rule enabled all the time.</p>
				<strong>NOTE:  schedule logic can be a bit different.  Click <a target="_new" href='firewall_rules_schedule_logic.php'>here</a> for more information.</strong>
			</td>
		</tr>

<?php
			/* build a list of gateways */
			$gateways = array();
			$gateways[] = "default"; // default to don't use this feature :)
			if (is_array($config['gateways']['gateway_item'])) {
				foreach($config['gateways']['gateway_item'] as $gw_item) {
				if($gw_item['gateway'] <> "")
					$gateways[] = $gw_item['name'];
				}
			}

?>
		<tr>
			<td width="22%" valign="top" class="vncell">Gateway</td>
			<td width="78%" class="vtable">
				<select name='gateway'>
<?php
				foreach($gateways as $gw) {
					if($gw == "")
						continue;
					if($gw == $pconfig['gateway']) {
						$selected = " SELECTED";
					} else {
						$selected = "";
					}
					if ($gw == "default") {
						echo "<option value=\"\" {$selected}>{$gw}</option>\n";
					} else {
						$gwip = lookup_gateway_ip_by_name($gw);
						echo "<option value=\"{$gw}\" {$selected}>{$gw} - {$gwip}</option>\n";
					}
				}
				/* add gateway groups to the list */
				if (is_array($config['gateways']['gateway_group'])) {
					foreach($config['gateways']['gateway_group'] as $gw_group) {
						if($gw_group['name'] == "")
							continue;
						if($pconfig['gateway'] == $gw_group['name']) {
							echo "<option value=\"{$gw_group['name']}\" SELECTED>{$gw_group['name']}</option>\n";
						} else {
							echo "<option value=\"{$gw_group['name']}\">{$gw_group['name']}</option>\n";
						}
					}
				}
				$iflist = get_configured_interface_with_descr();
				foreach ($iflist as $ifent => $ifdesc) {
					if (in_array($config['interfaces'][$ifent]['ipaddr'],
						 array("dhcp", "pppoe", "pptp"))) {
						if ($pconfig['gateway'] == $ifent) {
							$selected = " SELECTED";
						} else {
							$selected = "";
						}
						if($ifdesc <> "")
							echo "<option value=\"{$ifent}\" {$selected}>".strtoupper($if)." - {$ifdesc}</option>\n";
					}
				}
?>
				</select>
				<p><strong>Leave as 'default' to use the system routing table.  Or choose a gateway to utilize policy based routing.</strong></p>
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncell">Ackqueue/Queue</td>
			<td width="78%" class="vtable">
			<select name="ackqueue">
<?php
		read_altq_config(); /* XXX: */
		$qlist =& get_unique_queue_list();
		if (!is_array($qlist))
			$qlist = array();
		echo "<option value=\"none\"";
		if (!$qselected) echo " SELECTED";
		echo " >none</option>";
		foreach ($qlist as $q => $qkey) {
			if($q == "")
				continue;
			echo "<option value=\"$q\"";
			if ($q == $pconfig['ackqueue']) {
				$qselected = 1;
				echo " SELECTED";
			}
			echo ">{$q}</option>";
		}
?>
			</select> /
			<select name="defaultqueue">
<?php
		$qselected = 0;
		echo "<option value=\"none\"";
		if (!$qselected) echo " SELECTED";
		echo " >none</option>";
		foreach ($qlist as $q => $qkey) {
			if($q == "")
				continue;
			echo "<option value=\"$q\"";
			if ($q == $pconfig['defaultqueue']) {
				$qselected = 1;
				echo " SELECTED";
			}
			echo ">{$q}</option>";
		}
?>
			</select>
				<br />
				<span class="vexpl">Choose the Acknowledge Queue only if you have selected Queue.</span>
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top" class="vncell">Description</td>
			<td width="78%" class="vtable">
				<input name="descr" type="text" class="formfld unknown" id="descr" size="52" maxlength="52" value="<?=htmlspecialchars($pconfig['descr']);?>">
				<br />
				<span class="vexpl">You may enter a description here for your reference (not parsed).</span>
			</td>
		</tr>
		<tr>
			<td width="22%" valign="top">&nbsp;</td>
			<td width="78%">
				<input name="Submit" type="submit" class="formbtn" value="Save">  <input type="button" class="formbtn" value="Cancel" onclick="history.back()">
<?php			if (isset($id) && $a_filter[$id]): ?>
					<input name="id" type="hidden" value="<?=$id;?>">
<?php 			endif; ?>
				<input name="after" type="hidden" value="<?=$after;?>">
			</td>
		</tr>
	</table>
</form>
<script language="JavaScript">
<!--
	ext_change();
	typesel_change();
	proto_change();

<?php
	$isfirst = 0;
	$aliases = "";
	$addrisfirst = 0;
	$aliasesaddr = "";
	if($config['aliases']['alias'] <> "" and is_array($config['aliases']['alias']))
		foreach($config['aliases']['alias'] as $alias_name) {
			if(!stristr($alias_name['address'], ".")) {
				if($isfirst == 1) $aliases .= ",";
				$aliases .= "'" . $alias_name['name'] . "'";
				$isfirst = 1;
			} else {
				if($addrisfirst == 1) $aliasesaddr .= ",";
				$aliasesaddr .= "'" . $alias_name['name'] . "'";
				$addrisfirst = 1;
			}
		}
?>

	var addressarray=new Array(<?php echo $aliasesaddr; ?>);
	var customarray=new Array(<?php echo $aliases; ?>);

//-->
</script>


<?php include("fend.inc"); ?>
</body>
</html>
