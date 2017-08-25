<?php
/*
	vpn_ipsec_phase2.php
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

require("guiconfig.inc");

if (!is_array($config['ipsec']['client']))
	$config['ipsec']['client'] = array();

$a_client = &$config['ipsec']['client'];

if (!is_array($config['ipsec']['phase2']))
	$config['ipsec']['phase2'] = array();

$a_phase2 = &$config['ipsec']['phase2'];

if($config['interfaces']['lan'])
	$specialsrcdst = explode(" ", "lan");

$p2index = $_GET['p2index'];
if (isset($_POST['p2index']))
	$p2index = $_POST['p2index'];

if (isset($_GET['dup']))
	$p2index = $_GET['dup'];

if (isset($p2index) && $a_phase2[$p2index])
{
	$pconfig['ikeid'] = $a_phase2[$p2index]['ikeid'];
	$pconfig['disabled'] = isset($a_phase2[$p2index]['disabled']);
	$pconfig['descr'] = $a_phase2[$p2index]['descr'];

	idinfo_to_pconfig("local",$a_phase2[$p2index]['localid'],$pconfig);
	idinfo_to_pconfig("remote",$a_phase2[$p2index]['remoteid'],$pconfig);

	$pconfig['proto'] = $a_phase2[$p2index]['protocol'];
	ealgos_to_pconfig($a_phase2[$p2index]['encryption-algorithm-option'],$pconfig);
	$pconfig['halgos'] = $a_phase2[$p2index]['hash-algorithm-option'];
	$pconfig['pfsgroup'] = $a_phase2[$p2index]['pfsgroup'];
	$pconfig['lifetime'] = $a_phase2[$p2index]['lifetime'];

	if (isset($a_phase2[$p2index]['mobile']))
		$pconfig['mobile'] = true;
}
else
{
	$pconfig['ikeid'] = $_GET['ikeid'];

	/* defaults */
	$pconfig['localid_type'] = "lan";
	$pconfig['remoteid_type'] = "network";
	$pconfig['proto'] = "esp";
	$pconfig['ealgos'] = explode(",", "3des,blowfish,cast128,aes");
	$pconfig['halgos'] = explode(",", "hmac_sha1,hmac_md5");
	$pconfig['pfsgroup'] = "0";
	$pconfig['lifetime'] = "3600";

    /* mobile client */
    if($_GET['mobile'])
        $pconfig['mobile']=true;
}

if (isset($_GET['dup']))
	unset($p2index);

if ($_POST) {

	unset($input_errors);
	$pconfig = $_POST;

	if (!isset( $_POST['ikeid']))
		$input_errors[] = "A valid ikeid must be specified.";

	/* input validation */
	$reqdfields = explode(" ", "localid_type halgos");
	$reqdfieldsn = explode(",", "Local network type,P2 Hash Algorithms");
	if (!isset($pconfig['mobile'])){
		$reqdfields[] = "remoteid_type";
		$reqdfieldsn[] = "Remote network type";
	}

	do_input_validation($_POST, $reqdfields, $reqdfieldsn, &$input_errors);

	switch ($pconfig['localid_type']) {
		case "network":
			if (!$pconfig['localid_netbits'] || !is_numeric($pconfig['localid_netbits']))
				$input_errors[] = "A valid local network bit count must be specified..";
		case "address":
			if (!$pconfig['localid_address'] || !is_ipaddr($pconfig['localid_address']))
				$input_errors[] = "A valid local network IP address must be specified.";
			break;
	}

	switch ($pconfig['remoteid_type']) {
		case "network":
			if (!$pconfig['remoteid_netbits'] || !is_numeric($pconfig['remoteid_netbits']))
				$input_errors[] = "A valid remote network bit count must be specified..";
		case "address":
			if (!$pconfig['remoteid_address'] || !is_ipaddr($pconfig['remoteid_address']))
				$input_errors[] = "A valid remote network IP address must be specified.";
			break;
	}

/* TODO : Validate enabled phase2's are not duplicates */

	$ealgos = pconfig_to_ealgos($pconfig);

	if (!count($ealgos)) {
		$input_errors[] = "At least one encryption algorithm must be selected.";
	}
	if (($_POST['lifetime'] && !is_numeric($_POST['lifetime']))) {
		$input_errors[] = "The P2 lifetime must be an integer.";
	}

	if (!$input_errors) {

		$ph2ent['ikeid'] = $pconfig['ikeid'];
		$ph2ent['disabled'] = $pconfig['disabled'] ? true : false;

		$ph2ent['localid'] = pconfig_to_idinfo("local",$pconfig);
		$ph2ent['remoteid'] = pconfig_to_idinfo("remote",$pconfig);

		$ph2ent['protocol'] = $pconfig['proto'];
		$ph2ent['encryption-algorithm-option'] = $ealgos;
		$ph2ent['hash-algorithm-option'] = $pconfig['halgos'];
		$ph2ent['pfsgroup'] = $pconfig['pfsgroup'];
		$ph2ent['lifetime'] = $pconfig['lifetime'];
		$ph2ent['descr'] = $pconfig['descr'];

		if (isset($pconfig['mobile']))
			$ph2ent['mobile'] = true;

		if (isset($p2index) && $a_phase2[$p2index])
			$a_phase2[$p2index] = $ph2ent;
		else
			$a_phase2[] = $ph2ent;

		write_config();
		touch($d_ipsecconfdirty_path);

		header("Location: vpn_ipsec.php");
		exit;
	}
}

if ($pconfig['mobile'])
    $pgtitle = array("VPN","IPsec","Edit Phase 2", "Mobile Client");
else
    $pgtitle = array("VPN","IPsec","Edit Phase 2");

include("head.inc");

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php include("fbegin.inc"); ?>
<script language="JavaScript">
<!--
function typesel_change_local(bits) {

	if (!bits)
		bits = 24;

	switch (document.iform.localid_type.selectedIndex) {
		case 0:	/* single */
			document.iform.localid_address.disabled = 0;
			document.iform.localid_netbits.value = 0;
			document.iform.localid_netbits.disabled = 1;
			break;
		case 1:	/* network */
			document.iform.localid_address.disabled = 0;
			document.iform.localid_netbits.value = bits;
			document.iform.localid_netbits.disabled = 0;
			break;
		default:
			document.iform.localid_address.value = "";
			document.iform.localid_address.disabled = 1;
			document.iform.localid_netbits.value = 0;
			document.iform.localid_netbits.disabled = 1;
			break;
	}
}

<?php if (isset($pconfig['mobile'])): ?>

function typesel_change_remote(bits) {

	document.iform.remoteid_address.disabled = 1;
	document.iform.remoteid_netbits.disabled = 1;
}

<?php else: ?>

function typesel_change_remote(bits) {

	if (!bits)
		bits = 24;

	switch (document.iform.remoteid_type.selectedIndex) {
		case 0:	/* single */
			document.iform.remoteid_address.disabled = 0;
			document.iform.remoteid_netbits.value = 0;
			document.iform.remoteid_netbits.disabled = 1;
			break;
		case 1:	/* network */
			document.iform.remoteid_address.disabled = 0;
			document.iform.remoteid_netbits.value = bits;
			document.iform.remoteid_netbits.disabled = 0;
			break;
		default:
			document.iform.remoteid_address.value = "";
			document.iform.remoteid_address.disabled = 1;
			document.iform.remoteid_netbits.value = 0;
			document.iform.remoteid_netbits.disabled = 1;
			break;
	}
}

<?php endif; ?>

//-->

</script>
<?php if ($input_errors) print_input_errors($input_errors); ?>
            <form action="vpn_ipsec_phase2.php" method="post" name="iform" id="iform">
              <table width="100%" border="0" cellpadding="6" cellspacing="0">
                <tr>
                  <td width="22%" valign="top" class="vncellreq">Mode</td>
                  <td width="78%" class="vtable"> Tunnel</td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncellreq">Disabled</td>
                  <td width="78%" class="vtable">
                    <input name="disabled" type="checkbox" id="disabled" value="yes" <?php if ($pconfig['disabled']) echo "checked"; ?>>
                    <strong>Disable this phase2 entry</strong><br>
                    <span class="vexpl">Set this option to disable this phase2 entry without
                      removing it from the list.
                    </span>
                  </td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncellreq">Local Network</td>
                  <td width="78%" class="vtable">
                    <table border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>Type:&nbsp;&nbsp;</td>
                        <td></td>
                        <td>
                          <select name="localid_type" class="formselect" onChange="typesel_change_local()">
                            <option value="address" <?php if ($pconfig['localid_type'] == "address") echo "selected";?>>Address</option>
                            <option value="network" <?php if ($pconfig['localid_type'] == "network") echo "selected";?>>Network</option>
                            <option value="lan" <?php if ($pconfig['localid_type'] == "lan" ) echo "selected";?>>LAN subnet</option>
                          </select>
                        </td>
                      </tr>
                      <tr>
                        <td>Address:&nbsp;&nbsp;</td>
                        <td><?=$mandfldhtmlspc;?></td>
                        <td>
                          <input name="localid_address" type="text" class="formfld unknown" id="localid_address" size="20" value="<?=$pconfig['localid_address'];?>">
                          /
                          <select name="localid_netbits" class="formselect" id="localid_netbits">
                            <?php for ($i = 32; $i >= 0; $i--): ?>
                            <option value="<?=$i;?>" <?php if ($i == $pconfig['localid_netbits']) echo "selected"; ?>>
                              <?=$i;?>
                            </option>
                            <?php endfor; ?>
                          </select>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <?php if (!isset($pconfig['mobile'])): ?>
				<tr>
                  <td width="22%" valign="top" class="vncellreq">Remote Network</td>
                  <td width="78%" class="vtable">
                    <table border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>Type:&nbsp;&nbsp;</td>
                        <td></td>
                        <td>
                          <select name="remoteid_type" class="formselect" onChange="typesel_change_remote()">
                            <option value="address" <?php if ($pconfig['remoteid_type'] == "address") echo "selected"; ?>>Address</option>
                            <option value="network" <?php if ($pconfig['remoteid_type'] == "network") echo "selected"; ?>>Network</option>
                          </select>
                        </td>
                      </tr>
                      <tr>
                        <td>Address:&nbsp;&nbsp;</td>
                        <td><?=$mandfldhtmlspc;?></td>
                        <td>
                          <input name="remoteid_address" type="text" class="formfld unknown" id="remoteid_address" size="20" value="<?=$pconfig['remoteid_address'];?>">
                          /
                          <select name="remoteid_netbits" class="formselect" id="remoteid_netbits">
                            <?php for ($i = 32; $i >= 0; $i--): ?>
                            <option value="<?=$i;?>" <?php if ($i == $pconfig['remoteid_netbits']) echo "selected"; ?>>
                              <?=$i;?>
                            </option>
                            <?php endfor; ?>
                          </select>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <?php endif; ?>
                <tr>
                  <td width="22%" valign="top" class="vncell">Description</td>
                  <td width="78%" class="vtable">
                    <input name="descr" type="text" class="formfld unknown" id="descr" size="40" value="<?=htmlspecialchars($pconfig['descr']);?>">
                    <br> <span class="vexpl">You may enter a description here
                    for your reference (not parsed).</span>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="list" height="12"></td>
                </tr>
                <tr>
                  <td colspan="2" valign="top" class="listtopic">Phase 2 proposal
                    (SA/Key Exchange)
                  </td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncellreq">Protocol</td>
                  <td width="78%" class="vtable">
                    <select name="proto" class="formselect">
                      <?php foreach ($p2_protos as $proto => $protoname): ?>
                      <option value="<?=$proto;?>" <?php if ($proto == $pconfig['proto']) echo "selected"; ?>>
                        <?=htmlspecialchars($protoname);?>
                      </option>
                      <?php endforeach; ?>
                    </select>
                    <br>
                    <span class="vexpl">ESP is encryption, AH is authentication only </span>
                  </td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncellreq">Encryption algorithms</td>
                  <td width="78%" class="vtable">
                    <table border="0" cellspacing="0" cellpadding="0">
                      <?php
                        foreach ($p2_ealgos as $algo => $algodata):
                        $checked = '';
                        if (in_array($algo,$pconfig['ealgos']))
                          $checked = " checked";
                      ?>
                      <tr>
                        <td>
                          <input type="checkbox" name="ealgos[]?>" value="<?=$algo;?>"<?=$checked?>>
						</td>
                        <td>
                          <?=htmlspecialchars($algodata['name']);?>
                        </td>
                        <td>
                          <?php if(is_array($algodata['keysel'])): ?>
                          &nbsp;&nbsp;
                          <select name="keylen_<?=$algo;?>" class="formselect">
                            <option value="auto">auto</option>
                            <?php
                              $key_hi = $algodata['keysel']['hi'];
                              $key_lo = $algodata['keysel']['lo'];
                              $key_step = $algodata['keysel']['step'];
                              for ($keylen = $key_hi; $keylen >= $key_lo; $keylen -= $key_step):
                                $selected = '';
//                                if ($checked && in_array("keylen_".$algo,$pconfig))
                                  if ($keylen == $pconfig["keylen_".$algo])
                                    $selected = " selected";
                             ?>
                            <option value="<?=$keylen;?>"<?=$selected;?>><?=$keylen;?> bits</option>
                            <?php endfor; ?>
                          </select>
                          <?php endif; ?>
                        </td>
                      </tr>
                      <?php endforeach; ?>
                    </table>
                    <br>
                    Hint: use 3DES for best compatibility or if you have a hardware
                    crypto accelerator card. Blowfish is usually the fastest in
                    software encryption.
                  </td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncellreq">Hash algorithms</td>
                  <td width="78%" class="vtable">
                    <?php foreach ($p2_halgos as $algo => $algoname): ?>
                    <input type="checkbox" name="halgos[]" value="<?=$algo;?>" <?php if (in_array($algo, $pconfig['halgos'])) echo "checked"; ?>>
                    <?=htmlspecialchars($algoname);?>
                    <br>
                    <?php endforeach; ?>
                  </td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncellreq">PFS key group</td>
                  <td width="78%" class="vtable">
					<?php if (!isset($pconfig['mobile']) || !isset($a_client['pfs_group'])): ?>
                    <select name="pfsgroup" class="formselect">
                      <?php foreach ($p2_pfskeygroups as $keygroup => $keygroupname): ?>
                      <option value="<?=$keygroup;?>" <?php if ($keygroup == $pconfig['pfsgroup']) echo "selected"; ?>>
                        <?=htmlspecialchars($keygroupname);?>
                      </option>
                      <?php endforeach; ?>
                    </select>
                    <br>
                    <span class="vexpl"><em>1 = 768 bit, 2 = 1024 bit, 5 = 1536 bit</em></span>
					<?php else: ?>
                    <select class="formselect" disabled>
                      <option selected><?=$p2_pfskeygroups[$a_client['pfs_group']];?></option>
                    </select>
                    <input name="pfsgroup" type="hidden" value="<?=$pconfig['pfsgroup'];?>">
                    <br>
                    <span class="vexpl"><em>Set globally in mobile client options</em></span>
					<?php endif; ?>
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
                  <td width="22%" valign="top">&nbsp;</td>
                  <td width="78%">
                    <?php if (isset($p2index) && $a_phase2[$p2index]): ?>
                    <input name="p2index" type="hidden" value="<?=$p2index;?>">
                    <?php endif; ?>
                    <?php if ($pconfig['mobile']): ?>
                    <input name="mobile" type="hidden" value="true">
                    <input name="remoteid_type" type="hidden" value="mobile">
                    <?php endif; ?>
                    <input name="Submit" type="submit" class="formbtn" value="Save">
                    <input name="ikeid" type="hidden" value="<?=$pconfig['ikeid'];?>">
                  </td>
                </tr>
              </table>
</form>
<script lannguage="JavaScript">
<!--
typesel_change_local(<?=$pconfig['localid_netbits']?>);
typesel_change_remote(<?=$pconfig['remoteid_netbits']?>);
//-->
</script>
<?php include("fend.inc"); ?>
</body>
</html>

<?php

/* local utility functions */

function pconfig_to_ealgos(& $pconfig) {

	global $p2_ealgos;

	$ealgos = array();
	foreach ($p2_ealgos as $algo_name => $algo_data) {
		if (in_array($algo_name,$pconfig['ealgos'])) {
			$ealg = array();
			$ealg['name'] = $algo_name;
			if (is_array($algo_data['keysel']))
				$ealg['keylen'] = $_POST["keylen_".$algo_name];
			$ealgos[] = $ealg;
		}
	}

	return $ealgos;
}

function ealgos_to_pconfig(& $ealgos,& $pconfig) {

	$pconfig['ealgos'] = array();
	foreach ($ealgos as $algo_data) {
		$pconfig['ealgos'][] = $algo_data['name'];
		if (isset($algo_data['keylen']))
			$pconfig["keylen_".$algo_data['name']] = $algo_data['keylen'];
	}

	return $ealgos;
}

function pconfig_to_idinfo($prefix,& $pconfig) {

	$type = $pconfig[$prefix."id_type"];
	$address = $pconfig[$prefix."id_address"];
	$netbits = $pconfig[$prefix."id_netbits"];

	switch( $type )
	{
		case "address":
			return array('type' => $type, 'address' => $address);
		case "network":
			return array('type' => $type, 'address' => $address, 'netbits' => $netbits);
		default:
			return array('type' => $type );
	}
}

function idinfo_to_pconfig($prefix,& $idinfo,& $pconfig) {

	switch( $idinfo['type'] )
	{
		case "address":
			$pconfig[$prefix."id_type"] = $idinfo['type'];
			$pconfig[$prefix."id_address"] = $idinfo['address'];
			break;
		case "network":
			$pconfig[$prefix."id_type"] = $idinfo['type'];
			$pconfig[$prefix."id_address"] = $idinfo['address'];
			$pconfig[$prefix."id_netbits"] = $idinfo['netbits'];
			break;
		default:
			$pconfig[$prefix."id_type"] = $idinfo['type'];
			break;
	}
}

?>
