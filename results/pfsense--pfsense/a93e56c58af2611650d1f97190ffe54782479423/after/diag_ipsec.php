<?php
/* $Id$ */
/*
	diag_ipsec.php
	Copyright (C) 2007 Scott Ullrich
	Copyright (C) 2008 Shrew Soft Inc <mgrooms@shrew.net>.
	All rights reserved.

	Parts of this code was originally based on vpn_ipsec_sad.php
	Copyright (C) 2003-2004 Manuel Kasper

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

global $g;

$pgtitle = array("Status","IPsec");

require("guiconfig.inc");
include("head.inc");

if (!is_array($config['ipsec']['phase2']))
    $config['ipsec']['phase2'] = array();

$a_phase2 = &$config['ipsec']['phase2'];

$spd = ipsec_dump_spd();
$sad = ipsec_dump_sad();

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC" onload="<?= $jsevents["body"]["onload"] ?>">
<?php include("fbegin.inc"); ?>
<div id="inputerrors"></div>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td>
			<?php
				$tab_array = array();
				$tab_array[0] = array("Overview", true, "diag_ipsec.php");
				$tab_array[1] = array("SAD", false, "diag_ipsec_sad.php");
				$tab_array[2] = array("SPD", false, "diag_ipsec_spd.php");
				display_top_tabs($tab_array);
			?>
		</td>
	</tr>
	<tr>
    	<td>
			<div id="mainarea">
				<table class="tabcont" width="100%" border="0" cellpadding="6" cellspacing="0">
					<?php if (count($sad)):	?>
					<tr>
						<td nowrap class="listhdrr">Local IP</td>
						<td nowrap class="listhdrr">Remote IP</a></td>
						<td nowrap class="listhdrr">Local Network</td>
						<td nowrap class="listhdrr">Remote Network</a></td>
						<td nowrap class="listhdrr">Description</a></td>
						<td nowrap class="listhdrr">Status</td>
					</tr>
					<?php
						foreach ($a_phase2 as $ph2ent) {
							if (!isset($ph2ent['disabled'])) {
								ipsec_lookup_phase1($ph2ent,$ph1ent);
								if(ipsec_phase2_status($spd,$sad,$ph1ent,$ph2ent))
									$icon = "pass";
								else
									$icon = "reject";
					?>
					<tr>
						<td class="listlr">
							<?=htmlspecialchars(ipsec_get_phase1_src($ph1ent));?>
						</td>
						<td class="listr">
							<?=htmlspecialchars($ph1ent['remote-gateway']);?>
						</td>
						<td class="listr">
							<?php echo ipsec_idinfo_to_text($ph2ent['localid']); ?>
						</td>
						<td class="listr">
							<?php echo ipsec_idinfo_to_text($ph2ent['remoteid']); ?>
						</td>
						<td class="listr"><?=htmlspecialchars($ph2ent['descr']);?></td>
						<td class="listr">
							<img src ="/themes/<?=$g['theme']?>/images/icons/icon_<?=$icon?>.gif">
						</td>
					</tr>
					<?php
							}
						}
					?>
					<?php else: ?>
					<tr>
						<td>
							<p>
								<strong>No IPsec security associations.</strong>
							</p>
						</td>
					</tr>
					<?php endif; ?>
					<tr>
						<td colspan="4">
							<p>
								<span class="vexpl">
									<span class="red">
										<strong>Note:<br /></strong>
									</span>
									You can configure your IPsec
									<a href="vpn_ipsec.php">here</a>.
								</span>
							</p>
						</td>
					</tr>
				</table>
			</div>
		</td>
	</tr>
</table>

<?php include("fend.inc"); ?>
</body>
</html>
