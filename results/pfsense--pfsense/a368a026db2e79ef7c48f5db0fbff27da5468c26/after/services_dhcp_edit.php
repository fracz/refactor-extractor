<?php
/* $Id$ */
/*
	services_dhcp_edit.php
	part of m0n0wall (http://m0n0.ch/wall)

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

##|+PRIV
##|*IDENT=page-services-dhcpserver-editstaticmapping
##|*NAME=Services: DHCP Server : Edit static mapping page
##|*DESCR=Allow access to the 'Services: DHCP Server : Edit static mapping' page.
##|*MATCH=services_dhcp_edit.php*
##|-PRIV

require_once('globals.inc');

if(!$g['services_dhcp_server_enable']) {
	Header("Location: /");
	exit;
}

require("guiconfig.inc");

$if = $_GET['if'];
if ($_POST['if'])
	$if = $_POST['if'];

if (!$if) {
	header("Location: services_dhcp.php");
	exit;
}

if (!is_array($config['dhcpd'][$if]['staticmap'])) {
	$config['dhcpd'][$if]['staticmap'] = array();
}

$static_map_enabled=isset($config['dhcpd'][$if]['staticarp']);

staticmaps_sort($if);
$a_maps = &$config['dhcpd'][$if]['staticmap'];
$ifcfgip = get_interface_ip($if);
$ifcfgsn = get_interface_subnet($if);
$ifcfgdescr = convert_friendly_interface_to_friendly_descr($if);

$id = $_GET['id'];
if (isset($_POST['id']))
	$id = $_POST['id'];

if (isset($id) && $a_maps[$id]) {
        $pconfig['mac'] = $a_maps[$id]['mac'];
		$pconfig['hostname'] = $a_maps[$id]['hostname'];
        $pconfig['ipaddr'] = $a_maps[$id]['ipaddr'];
        $pconfig['descr'] = $a_maps[$id]['descr'];
} else {
        $pconfig['mac'] = $_GET['mac'];
		$pconfig['hostname'] = $_GET['hostname'];
        $pconfig['descr'] = $_GET['descr'];
}

if ($_POST) {

	unset($input_errors);
	$pconfig = $_POST;

	/* input validation */
	$reqdfields = explode(" ", "mac");
	$reqdfieldsn = explode(",", "MAC address");

	do_input_validation($_POST, $reqdfields, $reqdfieldsn, &$input_errors);

	/* normalize MAC addresses - lowercase and convert Windows-ized hyphenated MACs to colon delimited */
	$_POST['mac'] = strtolower(str_replace("-", ":", $_POST['mac']));

	if (($_POST['host'] && !is_hostname($_POST['host']))) {
		$input_errors[] = "A valid host name must be specified.";
	}
	if (($_POST['ipaddr'] && !is_ipaddr($_POST['ipaddr']))) {
		$input_errors[] = "A valid IP address must be specified.";
	}
	if (($_POST['mac'] && !is_macaddr($_POST['mac']))) {
		$input_errors[] = "A valid MAC address must be specified.";
	}
	if($static_map_enabled && !$_POST['ipaddr']) {
		$input_errors[] = "Static map is enabled.  You must specify an IP address.";
	}

	/* check for overlaps */
	foreach ($a_maps as $mapent) {
		if (isset($id) && ($a_maps[$id]) && ($a_maps[$id] === $mapent))
			continue;

		if ((($mapent['hostname'] == $_POST['hostname']) && $mapent['hostname'])  || ($mapent['mac'] == $_POST['mac'])) {
			$input_errors[] = "This Hostname, IP or MAC address already exists.";
			break;
		}
	}

	/* make sure it's not within the dynamic subnet */
	if ($_POST['ipaddr']) {
		$dynsubnet_start = ip2long($config['dhcpd'][$if]['range']['from']);
		$dynsubnet_end = ip2long($config['dhcpd'][$if]['range']['to']);
		$lansubnet_start = (ip2long($ifcfgip) & gen_subnet_mask_long($ifcfgsn));
		$lansubnet_end = (ip2long($ifcfgip) | (~gen_subnet_mask_long($ifcfgsn)));
		if ((ip2long($_POST['ipaddr']) < $lansubnet_start) ||
			(ip2long($_POST['ipaddr']) > $lansubnet_end)) {
			$input_errors[] = "The IP address must lie in the {$ifcfgdescr} subnet.";
		}
	}

	if (!$input_errors) {
		$mapent = array();
		$mapent['mac'] = $_POST['mac'];
		$mapent['ipaddr'] = $_POST['ipaddr'];
		$mapent['hostname'] = $_POST['hostname'];
		$mapent['descr'] = $_POST['descr'];

		if (isset($id) && $a_maps[$id])
			$a_maps[$id] = $mapent;
		else
			$a_maps[] = $mapent;

		write_config();

		if(isset($config['dhcpd'][$if]['enable'])) {
			mark_subsystem_dirty('staticmaps');
			if (isset($config['dnsmasq']['regdhcpstatic']))
				mark_subsystem_dirty('hosts');
		}

		header("Location: services_dhcp.php?if={$if}");
		exit;
	}
}

$pgtitle = array("Services","DHCP","Edit static mapping");
include("head.inc");

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php include("fbegin.inc"); ?>
<?php if ($input_errors) print_input_errors($input_errors); ?>
            <form action="services_dhcp_edit.php" method="post" name="iform" id="iform">
              <table width="100%" border="0" cellpadding="6" cellspacing="0">
				<tr>
					<td colspan="2" valign="top" class="listtopic">Static DHCP Mapping</td>
				</tr>
                <tr>
                  <td width="22%" valign="top" class="vncellreq">MAC address</td>
                  <td width="78%" class="vtable">
                    <input name="mac" type="text" class="formfld unknown" id="mac" size="30" value="<?=htmlspecialchars($pconfig['mac']);?>">
		    <?php
			$ip = getenv('REMOTE_ADDR');
			$mac = `/usr/sbin/arp -an | grep {$ip} | cut -d" " -f4`;
			$mac = str_replace("\n","",$mac);
		    ?>
		    <a OnClick="document.forms[0].mac.value='<?=$mac?>';" href="#">Copy my MAC address</a>
                    <br>
                    <span class="vexpl">Enter a MAC address in the following format:
                    xx:xx:xx:xx:xx:xx</span></td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncell">IP address</td>
                  <td width="78%" class="vtable">
                    <input name="ipaddr" type="text" class="formfld unknown" id="ipaddr" size="20" value="<?=htmlspecialchars($pconfig['ipaddr']);?>">
                    <br>
                    If no IP address is given, one will be dynamically allocated  from the pool.</td>
                </tr>
                <tr>
                  <td width="22%" valign="top" class="vncell">Hostname</td>
                  <td width="78%" class="vtable">
                    <input name="hostname" type="text" class="formfld unknown" id="hostname" size="20" value="<?=htmlspecialchars($pconfig['hostname']);?>">
                    <br> <span class="vexpl">Name of the host, without domain part.</span></td>
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
                    <input name="Submit" type="submit" class="formbtn" value="Save"> <input class="formbtn" type="button" value="Cancel" onclick="history.back()">
                    <?php if (isset($id) && $a_maps[$id]): ?>
                    <input name="id" type="hidden" value="<?=$id;?>">
                    <?php endif; ?>
                    <input name="if" type="hidden" value="<?=$if;?>">
                  </td>
                </tr>
              </table>
</form>
<?php include("fend.inc"); ?>
</body>
</html>