<?php
/* $Id$ */
/*
	system_gateways_edit.php
	part of pfSense (http://pfsense.com)

	Copyright (C) 2007 Seth Mos <seth.mos@xs4all.nl>.
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

if (!is_array($config['gateways']['gateway_item']))
	$config['gateways']['gateway_item'] = array();

$a_gateways = &$config['gateways']['gateway_item'];

$id = $_GET['id'];
if (isset($_POST['id']))
	$id = $_POST['id'];

if (isset($_GET['dup'])) {
	$id = $_GET['dup'];
}

if (isset($id) && $a_gateways[$id]) {
	$pconfig['name'] = $a_gateways[$id]['name'];
	$pconfig['interface'] = $a_gateways[$id]['interface'];
	$pconfig['gateway'] = $a_gateways[$id]['gateway'];
	$pconfig['defaultgw'] = $a_gateways[$id]['defaultgw'];
	$pconfig['monitor'] = $a_gateways[$id]['monitor'];
	$pconfig['descr'] = $a_gateways[$id]['descr'];
}

if (isset($_GET['dup']))
	unset($id);

if ($_POST) {

	unset($input_errors);
	$pconfig = $_POST;

	/* input validation */
	$reqdfields = explode(" ", "interface name gateway");
	$reqdfieldsn = explode(",", "Interface,Name,Gateway");

	do_input_validation($_POST, $reqdfields, $reqdfieldsn, &$input_errors);

	if (! isset($_POST['name'])) {
		$input_errors[] = "A valid gateway name must be specified.";
	}
	if (($_POST['gateway'] && !is_ipaddr($_POST['gateway']))) {
		$input_errors[] = "A valid gateway IP address must be specified.";
	}
	if ((($_POST['monitor'] <> "") && !is_ipaddr($_POST['monitor']))) {
		$input_errors[] = "A valid monitor IP address must be specified.";
	}

	if ($_POST['defaultgw'] == "yes") {
		$i=0;
		foreach ($a_gateways as $gateway) {
			if($id != $i) {
	                        unset($config['gateways']['gateway_item'][$i]['defaultgw']);
			} else {
	                        $config['gateways']['gateway_item'][$i]['defaultgw'] = true;
			}
			$i++;
		}
	} else {
		$i=0;
		foreach ($a_gateways as $gateway) {
			if($id == $i) {
	                        unset($config['gateways']['gateway_item'][$i]['defaultgw']);
			}
			$i++;
		}
	}

	/* check for overlaps */
	foreach ($a_gateways as $gateway) {
		if (isset($id) && ($a_gateways[$id]) && ($a_gateways[$id] === $gateway))
			continue;

		if (($gateway['name'] <> "") && (in_array($gateway, $_POST['name']))) {
			$input_errors[] = "The name \"{$_POST['name']}\" already exists.";
			break;
		}
		if (($gateway['gateway'] <> "") && (in_array($gateway, $_POST['gateway']))) {
			$input_errors[] = "The IP address \"{$_POST['gateway']}\" already exists.";
			break;
		}
		if (($gateway['monitor'] <> "") && (in_array($gateway, $gateway['monitor']))) {
			$input_errors[] = "The IP address \"{$_POST['monitor']}\" already exists.";
			break;
		}
	}

	if (!$input_errors) {
		$gateway = array();
		$gateway['interface'] = $_POST['interface'];
		$gateway['name'] = $_POST['name'];
		$gateway['gateway'] = $_POST['gateway'];
		$gateway['monitor'] = $_POST['monitor'];
		$gateway['descr'] = $_POST['descr'];

		if($_POST['defaultgw'] == "yes") {
			$i = 0;
			foreach($a_gateways as $gw) {
				unset($config['gateways'][$i]['defaultgw']);
				$i++;
			}
			$gateway['defaultgw'] = true;
		} else {
			unset($gateway['defaultgw']);
		}

		if (isset($id) && $a_gateways[$id])
			$a_gateways[$id] = $gateway;
		else
			$a_gateways[] = $gateway;

		touch($d_staticroutesdirty_path);

		write_config();

		header("Location: system_gateways.php");
		exit;
	}
}

$pgtitle = array("System","Gateways","Edit gateway");
include("head.inc");

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php include("fbegin.inc"); ?>
<?php if ($input_errors) print_input_errors($input_errors); ?>
            <form action="system_gateways_edit.php" method="post" name="iform" id="iform">
              <table width="100%" border="0" cellpadding="6" cellspacing="0">
                <tr>
                  <td width="22%" valign="top" class="vncellreq">Interface</td>
                  <td width="78%" class="vtable">
			<select name="interface" class="formselect">
                      <?php $interfaces = array('wan' => 'WAN', 'lan' => 'LAN');
					  for ($i = 1; isset($config['interfaces']['opt' . $i]['enable']); $i++) {
					  	$interfaces['opt' . $i] = $config['interfaces']['opt' . $i]['descr'];
					  }
					  foreach ($interfaces as $iface => $ifacename): ?>
                      <option value="<?=$iface;?>" <?php if ($iface == $pconfig['interface']) echo "selected"; ?>>
                      <?=htmlspecialchars($ifacename);?>
                      </option>
                      <?php
						endforeach;
						if (is_package_installed("openbgpd") == 1) {
							echo "<option value=\"bgpd\"";
							if($pconfig['interface'] == "bgpd")
								echo " selected";
							echo ">Use BGPD</option>";
						}
 					  ?>
                    </select> <br>
                    <span class="vexpl">Choose which interface this gateway applies to.</span></td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncellreq">Name</td>
                  <td width="78%" class="vtable">
                    <input name="name" type="text" class="formfld unknown" id="name" size="20" value="<?=htmlspecialchars($pconfig['name']);?>">
                    <br> <span class="vexpl">Gateway name</span></td>
                </tr>
		<tr>
                  <td width="22%" valign="top" class="vncellreq">Gateway</td>
                  <td width="78%" class="vtable">
                    <input name="gateway" type="text" class="formfld host" id="gateway" size="40" value="<?=htmlspecialchars($pconfig['gateway']);?>">
                    <br> <span class="vexpl">Gateway IP address</span></td>
                </tr>
		<tr>
		  <td width="22%" valign="top" class="vncell">Default Gateway</td>
		  <td width="78%" class="vtable">
			<input name="defaultgw" type="checkbox" id="defaultgw" value="yes" <?php if (isset($pconfig['defaultgw'])) echo "checked"; ?> onclick="enable_change(false)" />
			<strong>Default Gateway</strong><br />
			This will select the above gateway as the default gateway
		  </td>
		</tr>
		<tr>
		  <td width="22%" valign="top" class="vncell">Monitor IP</td>
		  <td width="78%" class="vtable">
			<input name="monitor" type="text" id="monitor" value="<?php echo ($pconfig['monitor']) ; ?>" />
			<strong>Alternative monitor IP</strong> <br />
			Enter a alternative address here to be used to monitor the link. This is used for the
			quality RRD graphs as well as the load balancer entries. Use this if the gateway does not respond
			to icmp requests.</strong>
			<br />
		  </td>
		</tr>
		<tr>
                  <td width="22%" valign="top" class="vncell">Description</td>
                  <td width="78%" class="vtable">
                    <input name="descr" type="text" class="formfld unknown" id="descr" size="40" value="<?=htmlspecialchars($pconfig['descr']);?>">
                    <br> <span class="vexpl">You may enter a description here
                    for your reference (not parsed).</span></td>
                </tr>
                <tr>
                  <td width="22%" valign="top">&nbsp;</td>
                  <td width="78%">
                    <input name="Submit" type="submit" class="formbtn" value="Save"> <input type="button" value="Cancel" class="formbtn"  onclick="history.back()">
                    <?php if (isset($id) && $a_gateways[$id]): ?>
                    <input name="id" type="hidden" value="<?=$id;?>">
                    <?php endif; ?>
                  </td>
                </tr>
              </table>
</form>
<?php include("fend.inc"); ?>
<script language="JavaScript">
	enable_change();
</script>
</body>
</html>