<?php
/*
	vpn_ipsec_phase1.php
	part of m0n0wall (http://m0n0.ch/wall)

	Copyright (C) 2008 Shrew Soft Inc
	Copyright (C) 2003-2005 Manuel Kasper <mk@neon1.net>.
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

##|+PRIV
##|*IDENT=page-vpn-ipsec-editphase1
##|*NAME=VPN: IPsec: Edit Phase 1 page
##|*DESCR=Allow access to the 'VPN: IPsec: Edit Phase 1' page.
##|*MATCH=vpn_ipsec_phase1.php*
##|-PRIV


require("guiconfig.inc");

if (!is_array($config['ipsec']['phase1']))
	$config['ipsec']['phase1'] = array();

if (!is_array($config['ipsec']['phase2']))
	$config['ipsec']['phase2'] = array();

$a_phase1 = &$config['ipsec']['phase1'];
$a_phase2 = &$config['ipsec']['phase2'];

if($config['interfaces']['lan'])
		$specialsrcdst = explode(" ", "lan");

$p1index = $_GET['p1index'];
if (isset($_POST['p1index']))
	$p1index = $_POST['p1index'];

if (isset($_GET['dup'])) {
	$p1index = $_GET['dup'];
}

if (isset($p1index) && $a_phase1[$p1index])
{
	// don't copy the ikeid on dup
	if (!isset($_GET['dup']))
		$pconfig['ikeid'] = $a_phase1[$p1index]['ikeid'];
	$old_ph1ent = $a_phase1[$p1index];

	$pconfig['disabled'] = isset($a_phase1[$p1index]['disabled']);

	if ($a_phase1[$p1index]['interface'])
		$pconfig['interface'] = $a_phase1[$p1index]['interface'];
	else
		$pconfig['interface'] = "wan";

	list($pconfig['remotenet'],$pconfig['remotebits']) = explode("/", $a_phase1[$p1index]['remote-subnet']);

	if (isset($a_phase1[$p1index]['mobile']))
		$pconfig['mobile'] = 'true';
	else
		$pconfig['remotegw'] = $a_phase1[$p1index]['remote-gateway'];

	$pconfig['mode'] = $a_phase1[$p1index]['mode'];
	$pconfig['myid_type'] = $a_phase1[$p1index]['myid_type'];
	$pconfig['myid_data'] = $a_phase1[$p1index]['myid_data'];
	$pconfig['peerid_type'] = $a_phase1[$p1index]['peerid_type'];
	$pconfig['peerid_data'] = $a_phase1[$p1index]['peerid_data'];
	$pconfig['ealgo'] = $a_phase1[$p1index]['encryption-algorithm'];
	$pconfig['halgo'] = $a_phase1[$p1index]['hash-algorithm'];
	$pconfig['dhgroup'] = $a_phase1[$p1index]['dhgroup'];
	$pconfig['lifetime'] = $a_phase1[$p1index]['lifetime'];
	$pconfig['authentication_method'] = $a_phase1[$p1index]['authentication_method'];

	if (($pconfig['authentication_method'] == "pre_shared_key")||
		($pconfig['authentication_method'] == "xauth_psk_server")) {
		$pconfig['pskey'] = $a_phase1[$p1index]['pre-shared-key'];
	} else {
		$pconfig['certref'] = $a_phase1[$p1index]['certref'];
	}

	$pconfig['descr'] = $a_phase1[$p1index]['descr'];
	$pconfig['nat_traversal'] = $a_phase1[$p1index]['nat_traversal'];

	if ($a_phase1[$p1index]['dpd_delay'] &&	$a_phase1[$p1index]['dpd_maxfail']) {
		$pconfig['dpd_enable'] = true;
		$pconfig['dpd_delay'] = $a_phase1[$p1index]['dpd_delay'];
		$pconfig['dpd_maxfail'] = $a_phase1[$p1index]['dpd_maxfail'];
	}
}
else
{
	/* defaults */
	$pconfig['interface'] = "wan";
	if($config['interfaces']['lan'])
		$pconfig['localnet'] = "lan";
	$pconfig['mode'] = "aggressive";
	$pconfig['myid_type'] = "myaddress";
	$pconfig['peerid_type'] = "peeraddress";
	$pconfig['authentication_method'] = "pre_shared_key";
	$pconfig['ealgo'] = array( name => "3des" );
	$pconfig['halgo'] = "sha1";
	$pconfig['dhgroup'] = "2";
	$pconfig['lifetime'] = "28800";
	$pconfig['nat_traversal'] = "on";
	$pconfig['dpd_enable'] = true;

	/* mobile client */
	if($_GET['mobile'])
		$pconfig['mobile']=true;
}

if (isset($_GET['dup']))
	unset($p1index);

if ($_POST) {
	unset($input_errors);
	$pconfig = $_POST;

	/* input validation */

	$method = $pconfig['authentication_method'];
	if (($method == "pre_shared_key")||($method == "xauth_psk_server")) {
		$reqdfields = explode(" ", "pskey");
		$reqdfieldsn = explode(",", "Pre-Shared Key");
	} else {
		$reqdfields = explode(" ", "certref");
		$reqdfieldsn = explode(",", "My Certificate");
	}
	if (!$pconfig['mobile']) {
		$reqdfields[] = "remotegw";
		$reqdfieldsn[] = "Remote gateway";
	}

	do_input_validation($pconfig, $reqdfields, $reqdfieldsn, &$input_errors);

	if (($pconfig['lifetime'] && !is_numeric($pconfig['lifetime'])))
		$input_errors[] = "The P1 lifetime must be an integer.";

	if (($pconfig['remotegw'] && !is_ipaddr($pconfig['remotegw']) && !is_domain($pconfig['remotegw'])))
		$input_errors[] = "A valid remote gateway address or host name must be specified.";

	if (($pconfig['remotegw'] && is_ipaddr($pconfig['remotegw']) && !isset($pconfig['disabled']) )) {
		$t = 0;
		foreach ($a_phase1 as $ph1tmp) {
			if ($p1index <> $t) {
				$tremotegw = $pconfig['remotegw'];
				if (($ph1tmp['remote-gateway'] == $tremotegw) && !isset($ph1tmp['disabled'])) {
					$input_errors[] = "The remote gateway \"$tremotegw\" is already used by phase1 \"${ph1tmp['descr']}\".";
				}
			}
			$t++;
		}
	}

	/* My identity */

	if ($pconfig['myid_type'] == "myaddress")
		$pconfig['myid_data'] = "";

	if ($pconfig['myid_type'] == "address" and $pconfig['myid_data'] == "")
		$input_errors[] = gettext("Please enter an address for 'My Identifier'");

	if ($pconfig['myid_type'] == "keyid tag" and $pconfig['myid_data'] == "")
		$input_errors[] = gettext("Please enter a keyid tag for 'My Identifier'");

	if ($pconfig['myid_type'] == "fqdn" and $pconfig['myid_data'] == "")
		$input_errors[] = gettext("Please enter a fully qualified domain name for 'My Identifier'");

	if ($pconfig['myid_type'] == "user_fqdn" and $pconfig['myid_data'] == "")
		$input_errors[] = gettext("Please enter a user and fully qualified domain name for 'My Identifier'");

	if ($pconfig['myid_type'] == "dyn_dns" and $pconfig['myid_data'] == "")
		$input_errors[] = gettext("Please enter a dynamic domain name for 'My Identifier'");

	if ((($pconfig['myid_type'] == "address") && !is_ipaddr($pconfig['myid_data'])))
		$input_errors[] = "A valid IP address for 'My identifier' must be specified.";

	if ((($pconfig['myid_type'] == "fqdn") && !is_domain($pconfig['myid_data'])))
		$input_errors[] = "A valid domain name for 'My identifier' must be specified.";

	if ($pconfig['myid_type'] == "fqdn")
		if (is_domain($pconfig['myid_data']) == false)
			$input_errors[] = "A valid FQDN for 'My identifier' must be specified.";

	if ($pconfig['myid_type'] == "user_fqdn") {
		$user_fqdn = explode("@",$pconfig['myid_data']);
		if (is_domain($user_fqdn[1]) == false)
			$input_errors[] = "A valid User FQDN in the form of user@my.domain.com for 'My identifier' must be specified.";
	}

	if ($pconfig['myid_type'] == "dyn_dns")
		if (is_domain($pconfig['myid_data']) == false)
			$input_errors[] = "A valid Dynamic DNS address for 'My identifier' must be specified.";

	/* Peer identity */

	if ($pconfig['myid_type'] == "peeraddress")
		$pconfig['peerid_data'] = "";

	if ($pconfig['peerid_type'] == "address" and $pconfig['peerid_data'] == "")
		$input_errors[] = gettext("Please enter an address for 'Peer Identifier'");

	if ($pconfig['peerid_type'] == "keyid tag" and $pconfig['peerid_data'] == "")
		$input_errors[] = gettext("Please enter a keyid tag for 'Peer Identifier'");

	if ($pconfig['peerid_type'] == "fqdn" and $pconfig['peerid_data'] == "")
		$input_errors[] = gettext("Please enter a fully qualified domain name for 'Peer Identifier'");

	if ($pconfig['peerid_type'] == "user_fqdn" and $pconfig['peerid_data'] == "")
		$input_errors[] = gettext("Please enter a user and fully qualified domain name for 'Peer Identifier'");

	if ((($pconfig['peerid_type'] == "address") && !is_ipaddr($pconfig['peerid_data'])))
		$input_errors[] = "A valid IP address for 'Peer identifier' must be specified.";

	if ((($pconfig['peerid_type'] == "fqdn") && !is_domain($pconfig['peerid_data'])))
		$input_errors[] = "A valid domain name for 'Peer identifier' must be specified.";

	if ($pconfig['peerid_type'] == "fqdn")
		if (is_domain($pconfig['peerid_data']) == false)
			$input_errors[] = "A valid FQDN for 'Peer identifier' must be specified.";

	if ($pconfig['peerid_type'] == "user_fqdn") {
		$user_fqdn = explode("@",$pconfig['peerid_data']);
		if (is_domain($user_fqdn[1]) == false)
			$input_errors[] = "A valid User FQDN in the form of user@my.domain.com for 'Peer identifier' must be specified.";
	}

	if ($pconfig['dpd_enable']) {
		if (!is_numeric($pconfig['dpd_delay']))
			$input_errors[] = "A numeric value must be specified for DPD delay.";

		if (!is_numeric($pconfig['dpd_maxfail']))
			$input_errors[] = "A numeric value must be specified for DPD retries.";
	}

	/* build our encryption algorithms array */
	$pconfig['ealgo'] = array();
	$pconfig['ealgo']['name'] = $_POST['ealgo'];
	if($pconfig['ealgo_keylen'])
		$pconfig['ealgo']['keylen'] = $_POST['ealgo_keylen'];

	if (!$input_errors) {
		$ph1ent['ikeid'] = $pconfig['ikeid'];
		$ph1ent['disabled'] = $pconfig['disabled'] ? true : false;
		$ph1ent['interface'] = $pconfig['interface'];
		/* if the remote gateway changed and the interface is not WAN then remove route */
		/* the vpn_ipsec_configure() handles adding the route */
		if ($pconfig['interface'] <> "wan") {
			if($ph1ent['remote-gateway'] <> $pconfig['remotegw']) {
				mwexec("/sbin/route delete -host {$ph1ent['remote-gateway']}");
			}
		}

		if ($pconfig['mobile'])
			$ph1ent['mobile'] = true;
		else
			$ph1ent['remote-gateway'] = $pconfig['remotegw'];

		$ph1ent['mode'] = $pconfig['mode'];

		$ph1ent['myid_type'] = $pconfig['myid_type'];
		$ph1ent['myid_data'] = $pconfig['myid_data'];
		$ph1ent['peerid_type'] = $pconfig['peerid_type'];
		$ph1ent['peerid_data'] = $pconfig['peerid_data'];

		$ph1ent['encryption-algorithm'] = $pconfig['ealgo'];
		$ph1ent['hash-algorithm'] = $pconfig['halgo'];
		$ph1ent['dhgroup'] = $pconfig['dhgroup'];
		$ph1ent['lifetime'] = $pconfig['lifetime'];
		$ph1ent['pre-shared-key'] = $pconfig['pskey'];
		$ph1ent['private-key'] = base64_encode($pconfig['privatekey']);
		$ph1ent['certref'] = $pconfig['certref'];
		$ph1ent['authentication_method'] = $pconfig['authentication_method'];

		$ph1ent['descr'] = $pconfig['descr'];
		$ph1ent['nat_traversal'] = $pconfig['nat_traversal'];

		if (isset($pconfig['dpd_enable'])) {
			$ph1ent['dpd_delay'] = $pconfig['dpd_delay'];
			$ph1ent['dpd_maxfail'] = $pconfig['dpd_maxfail'];
		}

		/* generate unique phase1 ikeid */
		if ($ph1ent['ikeid'] == 0)
			$ph1ent['ikeid'] = ipsec_ikeid_next();

		if (isset($p1index) && $a_phase1[$p1index])
			$a_phase1[$p1index] = $ph1ent;
		else
			$a_phase1[] = $ph1ent;

		/* now we need to find all phase2 entries for this host */
		if (is_array($a_phase2) && (count($a_phase2))) {
			foreach ($a_phase2 as $phase2) {
				if($phase2['ikeid'] == $ph1ent['ikeid']) {
					log_error("Reload {$ph1ent['descr']} tunnels");
					$old_ph1ent['remote-gateway'] = resolve_retry($old_ph1ent['remote-gateway']);
					$old_phase2 = $phase2;
					reload_tunnel_spd_policy ($ph1ent, $phase2, $old_ph1ent, $old_phase2);
				}
			}
		}
		write_config();
		touch($d_ipsecconfdirty_path);

		header("Location: vpn_ipsec.php");
		exit;
	}
}

if ($pconfig['mobile'])
	$pgtitle = array("VPN","IPsec","Edit Phase 1", "Mobile Client");
else
	$pgtitle = array("VPN","IPsec","Edit Phase 1");

include("head.inc");

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php include("fbegin.inc"); ?>
<script language="JavaScript">
<!--

function myidsel_change() {
	index = document.iform.myid_type.selectedIndex;
	value = document.iform.myid_type.options[index].value;
	if (value == 'myaddress')
			document.getElementById('myid_data').style.visibility = 'hidden';
	else
			document.getElementById('myid_data').style.visibility = 'visible';
}

function peeridsel_change() {
	index = document.iform.peerid_type.selectedIndex;
	value = document.iform.peerid_type.options[index].value;
	if (value == 'peeraddress')
			document.getElementById('peerid_data').style.visibility = 'hidden';
	else
			document.getElementById('peerid_data').style.visibility = 'visible';
}

function methodsel_change() {
	index = document.iform.authentication_method.selectedIndex;
	value = document.iform.authentication_method.options[index].value;

	switch (value) {
		case 'hybrid_rsa_server':
			document.getElementById('opt_psk').style.display = 'none';
			document.getElementById('opt_cert').style.display = '';
			break;
		case 'xauth_rsa_server':
		case 'rsasig':
			document.getElementById('opt_psk').style.display = 'none';
			document.getElementById('opt_cert').style.display = '';
			break;
		default: /* psk modes*/
			document.getElementById('opt_psk').style.display = '';
			document.getElementById('opt_cert').style.display = 'none';
			break;
	}
}

/* PHP generated java script for variable length keys */
function ealgosel_change(bits) {
	switch (document.iform.ealgo.selectedIndex) {
<?php
  $i = 0;
  foreach ($p1_ealgos as $algo => $algodata) {
    if (is_array($algodata['keysel'])) {
      echo "		case {$i}:\n";
      echo "			document.iform.ealgo_keylen.style.visibility = 'visible';\n";
      echo "			document.iform.ealgo_keylen.options.length = 0;\n";
//      echo "			document.iform.ealgo_keylen.options[document.iform.ealgo_keylen.options.length] = new Option( 'auto', 'auto' );\n";

      $key_hi = $algodata['keysel']['hi'];
      $key_lo = $algodata['keysel']['lo'];
      $key_step = $algodata['keysel']['step'];

      for ($keylen = $key_hi; $keylen >= $key_lo; $keylen -= $key_step)
        echo "			document.iform.ealgo_keylen.options[document.iform.ealgo_keylen.options.length] = new Option( '{$keylen} bits', '{$keylen}' );\n";
      echo "			break;\n";
    } else {
      echo "		case {$i}:\n";
      echo "			document.iform.ealgo_keylen.style.visibility = 'hidden';\n";
      echo "			document.iform.ealgo_keylen.options.length = 0;\n";
      echo "			break;\n";
    }
    $i++;
  }
?>
	}

	if( bits )
		document.iform.ealgo_keylen.value = bits;
}

function dpdchkbox_change() {
	if( document.iform.dpd_enable.checked )
		document.getElementById('opt_dpd').style.display = '';
	else
		document.getElementById('opt_dpd').style.display = 'none';

	if (!document.iform.dpd_delay.value)
		document.iform.dpd_delay.value = "10";

	if (!document.iform.dpd_maxfail.value)
		document.iform.dpd_maxfail.value = "5";
}

//-->
</script>

<form action="vpn_ipsec_phase1.php" method="post" name="iform" id="iform">

<?php
	if ($input_errors)
		print_input_errors($input_errors);
?>

<table width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr class="tabnavtbl">
		<td id="tabnav">
			<?php
				$tab_array = array();
				$tab_array[0] = array("Tunnels", true, "vpn_ipsec.php");
				$tab_array[1] = array("Mobile clients", false, "vpn_ipsec_mobile.php");
				display_top_tabs($tab_array);
			?>
		</td>
	</tr>
	<tr>
		<td id="mainarea">
			<div class="tabcont">
				<table width="100%" border="0" cellpadding="6" cellspacing="0">
					<tr>
						<td colspan="2" valign="top" class="listtopic">General information</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">Disabled</td>
						<td width="78%" class="vtable">
							<input name="disabled" type="checkbox" id="disabled" value="yes" <?php if ($pconfig['disabled']) echo "checked"; ?>>
							<strong>Disable this phase1 entry</strong><br>
							<span class="vexpl">
								Set this option to disable this phase1 without
								removing it from the list.
							</span>
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">Interface</td>
						<td width="78%" class="vtable">
							<select name="interface" class="formselect">
							<?php
								$interfaces = get_configured_interface_with_descr();
								$carpips = find_number_of_needed_carp_interfaces();
								for ($i=0; $i<$carpips; $i++) {
									$carpip = find_interface_ip("carp" . $i);
									$interfaces['carp' . $i] = "CARP{$i} ({$carpip})";
								}
								foreach ($interfaces as $iface => $ifacename):
							?>
								<option value="<?=$iface;?>" <?php if ($iface == $pconfig['interface']) echo "selected"; ?>>
									<?=htmlspecialchars($ifacename);?>
								</option>
							<?php endforeach; ?>
							</select>
							<br>
							<span class="vexpl">Select the interface for the local endpoint of this phase1 entry.</span>
						</td>
					</tr>

					<?php if (!$pconfig['mobile']): ?>

					<tr>
						<td width="22%" valign="top" class="vncellreq">Remote gateway</td>
						<td width="78%" class="vtable">
							<?=$mandfldhtml;?><input name="remotegw" type="text" class="formfld unknown" id="remotegw" size="20" value="<?=$pconfig['remotegw'];?>">
							<br>
							Enter the public IP address or host name of the remote gateway
						</td>
					</tr>

					<?php endif; ?>

					<tr>
						<td width="22%" valign="top" class="vncell">Description</td>
						<td width="78%" class="vtable">
							<input name="descr" type="text" class="formfld unknown" id="descr" size="40" value="<?=htmlspecialchars($pconfig['descr']);?>">
							<br>
							<span class="vexpl">
								You may enter a description here
								for your reference (not parsed).
							</span>
						</td>
					</tr>
					<tr>
						<td colspan="2" class="list" height="12"></td>
					</tr>
					<tr>
						<td colspan="2" valign="top" class="listtopic">
							Phase 1 proposal (Authentication)
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">Negotiation mode</td>
						<td width="78%" class="vtable">
							<select name="mode" class="formselect">
							<?php
								$modes = explode(" ", "main aggressive");
								foreach ($modes as $mode):
							?>
								<option value="<?=$mode;?>" <?php if ($mode == $pconfig['mode']) echo "selected"; ?>>
									<?=htmlspecialchars($mode);?>
								</option>
							<?php endforeach; ?>
							</select> <br> <span class="vexpl">Aggressive is more flexible, but less secure.</span>
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">My identifier</td>
						<td width="78%" class="vtable">
							<select name="myid_type" class="formselect" onChange="myidsel_change()">
							<?php foreach ($my_identifier_list as $id_type => $id_params): ?>
								<option value="<?=$id_type;?>" <?php if ($id_type == $pconfig['myid_type']) echo "selected"; ?>>
									<?=htmlspecialchars($id_params['desc']);?>
								</option>
							<?php endforeach; ?>
							</select>
							<input name="myid_data" type="text" class="formfld unknown" id="myid_data" size="30" value="<?=$pconfig['myid_data'];?>">
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">Peer identifier</td>
						<td width="78%" class="vtable">
							<select name="peerid_type" class="formselect" onChange="peeridsel_change()">
							<?php
								foreach ($peer_identifier_list as $id_type => $id_params):
									if ($pconfig['mobile'] && !$id_params['mobile'])
										continue;
							?>
							<option value="<?=$id_type;?>" <?php if ($id_type == $pconfig['peerid_type']) echo "selected"; ?>>
								<?=htmlspecialchars($id_params['desc']);?>
							</option>
							<?php endforeach; ?>
							</select>
							<input name="peerid_data" type="text" class="formfld unknown" id="peerid_data" size="30" value="<?=$pconfig['peerid_data'];?>">
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">Encryption algorithm</td>
						<td width="78%" class="vtable">
							<select name="ealgo" class="formselect" onChange="ealgosel_change()">
							<?php
								foreach ($p1_ealgos as $algo => $algodata):
									$selected = '';
									if ($algo == $pconfig['ealgo']['name'])
										$selected = ' selected';
							?>
								<option value="<?=$algo;?>"<?=$selected?>>
									<?=htmlspecialchars($algodata['name']);?>
								</option>
							<?php endforeach; ?>
							</select>
							<select name="ealgo_keylen" width="30" class="formselect">
							</select>
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">Hash algorithm</td>
						<td width="78%" class="vtable">
							<select name="halgo" class="formselect">
							<?php foreach ($p1_halgos as $algo => $algoname): ?>
								<option value="<?=$algo;?>" <?php if ($algo == $pconfig['halgo']) echo "selected"; ?>>
									<?=htmlspecialchars($algoname);?>
								</option>
							<?php endforeach; ?>
							</select>
							<br>
							<span class="vexpl">
								Must match the setting chosen on the remote side.
							</span>
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">DH key group</td>
						<td width="78%" class="vtable">
							<select name="dhgroup" class="formselect">
							<?php $keygroups = explode(" ", "1 2 5"); foreach ($keygroups as $keygroup): ?>
								<option value="<?=$keygroup;?>" <?php if ($keygroup == $pconfig['dhgroup']) echo "selected"; ?>>
									<?=htmlspecialchars($keygroup);?>
								</option>
							<?php endforeach; ?>
							</select>
							<br>
							<span class="vexpl">
								<em>1 = 768 bit, 2 = 1024 bit, 5 = 1536 bit</em>
								<br>
								Must match the setting chosen on the remote side.
							</span>
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncell">Lifetime</td>
						<td width="78%" class="vtable">
							<input name="lifetime" type="text" class="formfld unknown" id="lifetime" size="20" value="<?=$pconfig['lifetime'];?>">
							seconds
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncellreq">Authentication method</td>
						<td width="78%" class="vtable">
							<select name="authentication_method" class="formselect" onChange="methodsel_change()">
							<?php
								foreach ($p1_authentication_methods as $method_type => $method_params):
									if (!$pconfig['mobile'] && $method_params['mobile'])
										continue;
							?>
								<option value="<?=$method_type;?>" <?php if ($method_type == $pconfig['authentication_method']) echo "selected"; ?>>
									<?=htmlspecialchars($method_params['name']);?>
								</option>
							<?php endforeach; ?>
							</select>
							<br>
							<span class="vexpl">
								Must match the setting chosen on the remote side.
							</span>
						</td>
					</tr>
					<tr id="opt_psk">
						<td width="22%" valign="top" class="vncellreq">Pre-Shared Key</td>
						<td width="78%" class="vtable">
							<?=$mandfldhtml;?>
							<input name="pskey" type="text" class="formfld unknown" id="pskey" size="40" value="<?=htmlspecialchars($pconfig['pskey']);?>">
							<span class="vexpl">
							<br>
								Input your pre-shared key string.
							</span>
						</td>
					</tr>
					<tr id="opt_cert">
						<td width="22%" valign="top" class="vncellreq">My Certificate</td>
						<td width="78%" class="vtable">
							<select name='certref' class="formselect">
							<?php
								foreach ($config['system']['cert'] as $cert):
									$selected = "";
									if ($pconfig['certref'] == $cert['refid'])
										$selected = "selected";
							?>
								<option value="<?=$cert['refid'];?>" <?=$selected;?>><?=$cert['name'];?></option>
							<?php endforeach; ?>
							</select>
							<br>
							<span class="vexpl">
								Select a certificate previously configured in the Certificate Manager.
							</span>
						</td>
					</tr>
					<tr>
						<td colspan="2" class="list" height="12"></td>
					</tr>
					<tr>
						<td colspan="2" valign="top" class="listtopic">Advanced Options</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncell">NAT Traversal</td>
						<td width="78%" class="vtable">
							<select name="nat_traversal" class="formselect">
								<option value="off" <?php if ($pconfig['nat_traversal'] == "off") echo "selected"; ?>>Disable</option>
								<option value="on" <?php if ($pconfig['nat_traversal'] == "on") echo "selected"; ?>>Enable</option>
								<option value="force" <?php if ($pconfig['nat_traversal'] == "force") echo "selected"; ?>>Force</option>
							</select>
							<br/>
							<span class="vexpl">
								Set this option to enable the use of NAT-T (i.e. the encapsulation of ESP in UDP packets) if needed,
								which can help with clients that are behind restrictive firewalls.
							</span>
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top" class="vncell">Dead Peer Detection</td>
						<td width="78%" class="vtable">
							<input name="dpd_enable" type="checkbox" id="dpd_enable" value="yes" <?php if (isset($pconfig['dpd_enable'])) echo "checked"; ?> onClick="dpdchkbox_change()">
							Enable DPD<br>
							<div id="opt_dpd">
								<br>
								<input name="dpd_delay" type="text" class="formfld unknown" id="dpd_delay" size="5" value="<?=$pconfig['dpd_delay'];?>">
								seconds<br>
								<span class="vexpl">
									Delay between requesting peer acknowledgement.
								</span><br>
								<br>
								<input name="dpd_maxfail" type="text" class="formfld unknown" id="dpd_maxfail" size="5" value="<?=$pconfig['dpd_maxfail'];?>">
								retries<br>
								<span class="vexpl">
									Number of consecutive failures allowed before disconnect.
								</span>
								<br>
							</div>
						</td>
					</tr>
					<tr>
						<td width="22%" valign="top">&nbsp;</td>
						<td width="78%">
							<?php if (isset($p1index) && $a_phase1[$p1index]): ?>
							<input name="p1index" type="hidden" value="<?=$p1index;?>">
							<?php endif; ?>
							<?php if ($pconfig['mobile']): ?>
							<input name="mobile" type="hidden" value="true">
							<?php endif; ?>
							<input name="ikeid" type="hidden" value="<?=$pconfig['ikeid'];?>">
							<input name="Submit" type="submit" class="formbtn" value="Save">
						</td>
					</tr>
				</table>
			</div>
		</td>
	</tr>
</table>
</form>

<script lannguage="JavaScript">
<!--
<?php
	/* determine if we should init the key length */
	$keyset = '';
	if (isset($pconfig['ealgo']['keylen']))
		if (is_numeric($pconfig['ealgo']['keylen']))
			$keyset = $pconfig['ealgo']['keylen'];
?>
myidsel_change();
peeridsel_change();
methodsel_change();
ealgosel_change(<?=$keyset;?>);
dpdchkbox_change();
//-->
</script>
<?php include("fend.inc"); ?>
</body>
</html>