<?php
/*
	vpn_ipsec.php
	part of m0n0wall (http://m0n0.ch/wall)

	Copyright (C) 2003-2005 Manuel Kasper <mk@neon1.net>.
	Copyright (C) 2008 Shrew Soft Inc
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
##|*IDENT=page-vpn-ipsec
##|*NAME=VPN: IPsec page
##|*DESCR=Allow access to the 'VPN: IPsec' page.
##|*MATCH=vpn_ipsec.php*
##|-PRIV


require("guiconfig.inc");

if (!is_array($config['ipsec']['phase1']))
	$config['ipsec']['phase1'] = array();

if (!is_array($config['ipsec']['phase2']))
	$config['ipsec']['phase2'] = array();

$a_phase1 = &$config['ipsec']['phase1'];
$a_phase2 = &$config['ipsec']['phase2'];

$wancfg = &$config['interfaces']['wan'];

$pconfig['enable'] = isset($config['ipsec']['enable']);

if ($_POST) {

	if ($_POST['apply']) {
		$retval = 0;
		$retval = vpn_ipsec_refresh_policies();
		$retval = vpn_ipsec_configure();
		/* reload the filter in the background */
		filter_configure();
		$savemsg = get_std_save_message($retval);
		if ($retval == 0) {
			if (is_subsystem_dirty('ipsec'))
				clear_subsystem_dirty('ipsec');
		}
	} else if ($_POST['submit']) {
		$pconfig = $_POST;

		$config['ipsec']['enable'] = $_POST['enable'] ? true : false;

		write_config();
	}
}

if ($_GET['act'] == "delph1")
{
	if ($a_phase1[$_GET['p1index']]) {
		/* remove static route if interface is not WAN */
		if ($a_phase1[$_GET['p1index']]['interface'] <> "wan")
			mwexec("/sbin/route delete -host {$$a_phase1[$_GET['p1index']]['remote-gateway']}");

		/* remove all phase2 entries that match the ikeid */
		$ikeid = $a_phase1[$_GET['p1index']]['ikeid'];
		foreach ($a_phase2 as $p2index => $ph2tmp)
			if ($ph2tmp['ikeid'] == $ikeid)
				unset($a_phase2[$p2index]);

		/* remove the phase1 entry */
		unset($a_phase1[$_GET['p1index']]);
		vpn_ipsec_refresh_policies();
		vpn_ipsec_configure();
		write_config();
		filter_configure();
		header("Location: vpn_ipsec.php");
		exit;
	}
}

if ($_GET['act'] == "delph2")
{
	if ($a_phase2[$_GET['p2index']]) {
		/* remove the phase2 entry */
		unset($a_phase2[$_GET['p2index']]);
		vpn_ipsec_refresh_policies();
		vpn_ipsec_configure();
		filter_configure();
		write_config();
		header("Location: vpn_ipsec.php");
		exit;
	}
}

$pgtitle = array("VPN","IPsec");
include("head.inc");

?>


<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php include("fbegin.inc"); ?>
<form action="vpn_ipsec.php" method="post">
<?php
	if ($savemsg)
		print_info_box($savemsg);
	if ($pconfig['enable'] && is_subsystem_dirty('ipsec'))
		print_info_box_np("The IPsec tunnel configuration has been changed.<br>You must apply the changes in order for them to take effect.");
?>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td class="tabnavtbl">
			<?php
				$tab_array = array();
				$tab_array[0] = array("Tunnels", true, "vpn_ipsec.php");
				$tab_array[1] = array("Mobile clients", false, "vpn_ipsec_mobile.php");
				display_top_tabs($tab_array);
			?>
		</td>
	</tr>
	<tr>
		<td>
			<div id="mainarea">
				<table border='1'>
				<table class="tabcont" width="100%" border="0" cellpadding="6" cellspacing="0">
					<tr>
						<td class="vtable">
							<table border="0" cellspacing="2" cellpadding="0">
								<tr>
									<td>
										<input name="enable" type="checkbox" id="enable" value="yes" <?php if ($pconfig['enable']) echo "checked";?>>
									</td>
									<td>
										<strong>Enable IPsec</strong>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td>
							<input name="submit" type="submit" class="formbtn" value="Save">
						</td>
					</tr>
				</table>
				<table class="tabcont" width="100%" border="0" cellpadding="0" cellspacing="0">
					<?php
						$i = 0;
						foreach ($a_phase1 as $ph1ent) {
							if (isset( $ph1ent['disabled'])) {
								$spans = "<span class=\"gray\">";
								$spane = "</span>";
							}
							else
								$spans = $spane = "";

						show_ipsec_header($ph1ent);
						$counter++; // used to deterimine if we need to output header manually (no records exist)
					?>
					<tr valign="top" ondblclick="document.location='vpn_ipsec_phase1.php?p1index=<?=$i;?>'">
						<td class="listlr">
							<?=$spans;?>
							<?php
								if ($ph1ent['interface']) {
									$iflabels = get_configured_interface_with_descr();
									$carpips = find_number_of_needed_carp_interfaces();
									for( $j=0; $j<$carpips; $j++ ) {
										$carpip = find_interface_ip("carp" . $j);
										$iflabels['carp' . $j] = "CARP{$j} ({$carpip})";
									}
									$if = htmlspecialchars($iflabels[$ph1ent['interface']]);
								}
								else
									$if = "WAN";

								if (!isset($ph1ent['mobile']))
									echo $if."<br>".$ph1ent['remote-gateway'];
								else
									echo $if."<br><strong>Mobile Client</strong>";
							?>
							<?=$spane;?>
						</td>
						<td class="listr">
							<?=$spans;?>
							<?=$ph1ent['mode'];?>
							<?=$spane;?>
						</td>
						<td class="listr">
							<?=$spans;?>
							<?=$p1_ealgos[$ph1ent['encryption-algorithm']['name']]['name'];?>
							<?php
								if ($ph1ent['encryption-algorithm']['keylen']) {
									if ($ph1ent['encryption-algorithm']['keylen']=="auto")
										echo " (auto)";
									else
										echo " ({$ph1ent['encryption-algorithm']['keylen']} bits)";
								}
							?>
							<?=$spane;?>
						</td>
						<td class="listr">
							<?=$spans;?>
							<?=$p1_halgos[$ph1ent['hash-algorithm']];?>
							<?=$spane;?>
						</td>
						<td class="listbg">
							<?=$spans;?>
							<?=htmlspecialchars($ph1ent['descr']);?>&nbsp;
							<?=$spane;?>
						</td>
						<td valign="middle" nowrap class="list">
							<table border="0" cellspacing="0" cellpadding="1">
								<tr>
									<td>
										<a href="vpn_ipsec_phase1.php?p1index=<?=$i;?>">
											<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_e.gif" title="edit phase1 entry" width="17" height="17" border="0">
										</a>
									</td>
									<td>
										<a href="vpn_ipsec.php?act=delph1&p1index=<?=$i;?>" onclick="return confirm('Do you really want to delete this phase1 and all associated phase2 entries?')">
											<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_x.gif" title="delete phase1 entry" width="17" height="17" border="0">
										</a>
									</td>
								</tr>
								<?php if (!isset($ph1ent['mobile'])): ?>
								<tr>
									<td>
									</td>
									<td>
										<a href="vpn_ipsec_phase1.php?dup=<?=$i;?>">
											<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" title="copy phase1 entry" width="17" height="17" border="0">
										</a>
									</td>
								</tr>
								<?php endif; ?>
							</table>
						</td>
					</tr>
					<tr>
						<td class="listrborder" colspan="5">
							<div id="shph2but-<?=$i?>">
								<?php
									$phase2count=0;
									foreach ($a_phase2 as $ph2ent) {
										if ($ph2ent['ikeid'] != $ph1ent['ikeid'])
											continue;
										if (isset( $ph2ent['disabled']) || isset($ph1ent['disabled']))
											continue;
										$phase2count++;
									}
								?>
								<input  type="button" onClick="show_phase2('tdph2-<?=$i?>','shph2but-<?=$i?>')" value="+"></input> - Show <?=$phase2count?> Phase-2 entries</a>
							</div>
							<table class="tabcont" width="100%" height="100%" border="0" cellspacing="0" cellpadding="0" id="tdph2-<?=$i?>" style="display:none">
								<tr>
									<td class="listhdrr">Mode</td>
									<?php if($ph2ent['mode'] == "tunnel"): ?>
									<td class="listhdrr">Local Subnet</td>
									<td class="listhdrr">Remote Subnet</td>
									<?php endif; ?>
									<td class="listhdrr">P2 Protocol</td>
									<td class="listhdrr">P2 Transforms</td>
									<td class="listhdrr">P2 Auth Methods</td>
									<td class ="list">
										<a href="vpn_ipsec_phase2.php?ikeid=<?=$ph1ent['ikeid'];?><?php if (isset($ph1ent['mobile'])) echo "&mobile=true";?>">
											<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" title="add phase2 entry" width="17" height="17" border="0">
										</a>
									</td>
								</tr>
								<?php
									$j = 0;
									foreach ($a_phase2 as $ph2ent) {
										if ($ph2ent['ikeid'] != $ph1ent['ikeid']) {
											$j++;
											continue;
										}

										if (isset( $ph2ent['disabled']) || isset($ph1ent['disabled'])) {
											$spans = "<span class=\"gray\">";
											$spane = "</span>";
										}
										else
											$spans = $spane = "";
								?>
								<tr valign="top" ondblclick="document.location='vpn_ipsec_phase2.php?p2index=<?=$i;?>'">

									<td nowrap class="listlr">
										<?=$spans;?>
											<?=$ph2ent['mode'];?>
										<?=$spane;?>
									</td>
									<?php if($ph2ent['mode'] == "tunnel"): ?>
									<td nowrap class="listr">
										<?=$spans;?>
											<?=ipsec_idinfo_to_text($ph2ent['localid']); ?>
										<?=$spane;?>
									</td>
									<td nowrap class="listr">
										<?=$spans;?>
											<?=ipsec_idinfo_to_text($ph2ent['remoteid']); ?>
										<?=$spane;?>
									</td>
									<?php endif; ?>
									<td nowrap class="listr">
										<?=$spans;?>
											<?php echo $p2_protos[$ph2ent['protocol']];	?>
										<?=$spane;?>
									</td>
									<td class="listr">
										<?=$spans;?>
										<?php
											$k = 0;
											foreach ($ph2ent['encryption-algorithm-option'] as $ph2ea) {
												if ($k++)
													echo ", ";
												echo $p2_ealgos[$ph2ea['name']]['name'];
												if ($ph2ea['keylen']) {
													if ($ph2ea['keylen']=="auto")
														echo " (auto)";
													else
														echo " ({$ph2ea['keylen']} bits)";
												}
											}
										?>
										<?=$spane;?>
									</td>
									<td nowrap class="listr">
										<?=$spans;?>
										<?php
											$k = 0;
											foreach ($ph2ent['hash-algorithm-option'] as $ph2ha) {
												if ($k++)
													echo ", ";
												echo $p2_halgos[$ph2ha];
											}
										?>
										<?=$spane;?>
									</td>
									<td nowrap class="list">
										<a href="vpn_ipsec_phase2.php?p2index=<?=$j;?>">
											<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_e.gif" title="edit phase2 entry" width="17" height="17" border="0">
										</a>
										<a href="vpn_ipsec.php?act=delph2&p2index=<?=$j;?>" onclick="return confirm('Do you really want to delete this phase2 entry?')">
											<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_x.gif" title="delete phase2 entry" width="17" height="17" border="0">
										</a>
									</td>
								</tr>

								<?php
										$j++;
									}
								?>
							</table>
						</td>
					</tr>
					<tr>
						<td>
							&nbsp;
						</td>
					</tr>
					<?php
							$i++;
						}
					if(!$counter)
						show_ipsec_header();
					?>
					<tr>
						<td class="list" colspan="5"></td>
						<td class="list">
							<table border="0" cellspacing="0" cellpadding="1">
								<tr>
									<td width="17"></td>
									<td>
										<a href="vpn_ipsec_phase1.php">
											<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" title="add phase1 entry" width="17" height="17" border="0">
										</a>
									</td>
								</tr>
							</table>
						<td>
					</tr>
					<tr>
						<td colspan="4">
							<p>
								<span class="vexpl">
									<span class="red">
										<strong>Note:<br></strong>
									</span>
									You can check your IPsec status at <a href="diag_ipsec.php">Status:IPsec</a>.
								</span>
							</p>
						</td>
					</tr>
				</table>
			</div>
		</td>
	</tr>
</table>
</form>
<?php include("fend.inc"); ?>
<script type="text/javascript">
function show_phase2(id, buttonid) {
	document.getElementById(buttonid).innerHTML='';
	aodiv = document.getElementById(id);
	aodiv.style.display = "block";
}
</script>
</body>
</html>

<?php

function show_ipsec_header() {
	if (isset($ph1ent['mobile'])) $mobile = "&mobile=true";
	echo <<<EOF
	<tr>
		<td class="listhdrr">Remote Gateway</td>
		<td class="listhdrr">Mode</td>
		<td class="listhdrr">P1 Protocol</td>
		<td class="listhdrr">P1 Transforms</td>
		<td class="listhdrr">P1 Description</td>
		<td class ="list">
			<a href="vpn_ipsec_phase2.php?ikeid={$ph1ent['ikeid']}{$mobile}">
				<img src="./themes/{$g['theme']}/images/icons/icon_plus.gif" title="add phase2 entry" width="17" height="17" border="0">
			</a>
		</td>
	</tr>

EOF;

}

?>