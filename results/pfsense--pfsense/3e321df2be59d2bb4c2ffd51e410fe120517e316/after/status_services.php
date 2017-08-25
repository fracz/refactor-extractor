<?php
/*
    services_status.php
    Copyright (C) 2004, 2005 Scott Ullrich
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
    INClUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
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

function gentitle_pkg($pgname) {
	global $config;
	return $config['system']['hostname'] . "." . $config['system']['domain'] . " - " . $pgname;
}

function get_pkg_descr($package_name) {
	global $config;
	foreach($config['installedpackages']['package'] as $pkg) {
		if($pkg['name'] == $package_name)
			return $pkg['descr'];
	}
	return "Not available.";
}

if($_GET['mode'] == "restartservice" and $_GET['service']) {
	switch($_GET['service']) {
		case 'bsnmpd':
			services_snmpd_configure();
			break;
		case 'dnsmasq':
			services_dnsmasq_configure();
			break;
		case 'dhcpd':
			services_dhcpd_configure();
			break;
		case 'miniupnpd':
			upnp_action('restart');
			break;
		case 'racoon':
			exec("killall -9 racoon");
			sleep(1);
			vpn_ipsec_force_reload();
			break;
		case 'openvpn':
			$vpnmode = $_GET['vpnmode'];
			if (($vpnmode == "server") or ($vpnmode == "client")) {
				$id = $_GET['id'];
				if (is_numeric($id)) {
					$pidfile = $g['varrun_path'] . "/openvpn_{$vpnmode}{$id}.pid";
					killbypid($pidfile);
					sleep(1);
					$configfile = $g['varetc_path'] . "/openvpn_{$vpnmode}{$id}.conf";
					mwexec_bg("openvpn --config $configfile");
				}
			}
			break;
		default:
			restart_service($_GET['service']);
			break;
	}
	$savemsg = "{$_GET['service']} has been restarted.";
	sleep(5);
}

if($_GET['mode'] == "startservice" and $_GET['service']) {
	switch($_GET['service']) {
		case 'bsnmpd':
			services_snmpd_configure();
			break;
		case 'dnsmasq':
			services_dnsmasq_configure();
			break;
		case 'dhcpd':
			services_dhcpd_configure();
			break;
		case 'miniupnpd':
			upnp_action('start');
			break;
		case 'racoon':
			exec("killall -9 racoon");
			sleep(1);
			vpn_ipsec_force_reload();
			break;
		case 'openvpn':
			$vpnmode = $_GET['vpnmode'];
			if (($vpnmode == "server") or ($vpnmode == "client")) {
				$id = $_GET['id'];
				if (is_numeric($id)) {
					$configfile = $g['varetc_path'] . "/openvpn_{$vpnmode}{$id}.conf";
					mwexec_bg("openvpn --config $configfile");
				}
			}
			break;
		default:
			start_service($_GET['service']);
			break;
	}
	$savemsg = "{$_GET['service']} has been started.";
	sleep(5);
}

/* stop service */
if($_GET['mode'] == "stopservice" && $_GET['service']) {
	switch($_GET['service']) {
		case 'bsnmpd':
			killbypid("{$g['varrun_path']}/snmpd.pid");
			break;
		case 'choparp':
			killbyname("choparp");
			break;
		case 'dhcpd':
			killbyname("dhcpd");
			break;
		case 'dhcrelay':
			killbypid("{$g['varrun_path']}/dhcrelay.pid");
			break;
		case 'dnsmasq':
			killbypid("{$g['varrun_path']}/dnsmasq.pid");
			break;
		case 'miniupnpd':
			upnp_action('stop');
			break;
		case 'ntpd':
			killbyname("ntpd");
			break;
		case 'sshd':
			killbyname("sshd");
			break;
		case 'racoon':
			exec("killall -9 racoon");
			break;
		case 'openvpn':
			$vpnmode = $_GET['vpnmode'];
			if (($vpnmode == "server") or ($vpnmode == "client")) {
				$id = $_GET['id'];
				if (is_numeric($id)) {
					$pidfile = $g['varrun_path'] . "/openvpn_{$vpnmode}{$id}.pid";
					killbypid($pidfile);
				}
			}
			break;
		default:
			stop_service($_GET['service']);
			break;
	}
	$savemsg = "{$_GET['service']} " . gettext("has been stopped.");
	sleep(5);
}

/* batch mode, allow other scripts to call this script */
if($_GET['batch']) exit;

$pgtitle = array("Status","Services");
include("head.inc");

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php
include("fbegin.inc");
?>
<form action="status_services.php" method="post">
<?php if ($savemsg) print_info_box($savemsg); ?>

<p>

<div id="boxarea">
<table class="tabcont" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td>
    <table width="100%" border="0" cellpadding="6" cellspacing="0">
	<tr>
	  <td class="listhdrr"><b><center>Service</center></b></td>
	  <td class="listhdrr"><b><center>Description</center></b></td>
	  <td class="listhdrr"><b><center>Status</center></b></td>
	</tr>

<?php

exec("/bin/ps ax | awk '{ print $5 }'", $psout);
array_shift($psout);
foreach($psout as $line) {
	$ps[] = trim(array_pop(explode(' ', array_pop(explode('/', $line)))));
}

$services = $config['installedpackages']['service'];

/*    Add services that are in the base.
 *
 */
if(isset($config['dnsmasq']['enable'])) {
	$pconfig['name'] = "dnsmasq";
	$pconfig['description'] = "DNS Forwarder";
	$services[] = $pconfig;
	unset($pconfig);
}

if(isset($config['captiveportal']['enable'])) {
	$pconfig['name'] = "lighttpd";
	$pconfig['description'] = "Captive Portal";
	$services[] = $pconfig;
	$pconfig = "";
	unset($pconfig);
}

$iflist = array();
$ifdescrs = get_configured_interface_list();
foreach ($ifdescrs as $if) {
	$oc = $config['interfaces'][$if];
	if ($oc['if'] && (!$oc['bridge']))
		$iflist[$if] = $if;
}
$show_dhcprelay = false;
foreach($iflist as $if) {
	if(isset($config['dhcrelay'][$if]['enable']))
		$show_dhcprelay = true;
}

if($show_dhcprelay == true) {
	$pconfig['name'] = "dhcrelay";
	$pconfig['description'] = "DHCP Relay";
	$services[] = $pconfig;
	unset($pconfig);
}

if(is_dhcp_server_enabled()) {
	$pconfig['name'] = "dhcpd";
	$pconfig['description'] = "DHCP Service";
	$services[] = $pconfig;
	unset($pconfig);
}

if(isset($config['snmpd']['enable'])) {
	$pconfig['name'] = "bsnmpd";
	$pconfig['description'] = "SNMP Service";
	$services[] = $pconfig;
	unset($pconfig);
}

if(isset($config['proxyarp']['proxyarpnet'])) {
	$pconfig['name'] = "choparp";
	$pconfig['description'] = "Proxy ARP";
	$services[] = $pconfig;
	unset($pconfig);
}

if($config['installedpackages']['miniupnpd']['config'][0]['enable']) {
	$pconfig['name'] = "miniupnpd";
	$pconfig['description'] = gettext("UPnP Service");
	$services[] = $pconfig;
	unset($pconfig);
}

if (isset($config['ipsec']['enable'])) {
	$pconfig['name'] = "racoon";
	$pconfig['description'] = gettext("IPsec VPN");
	$services[] = $pconfig;
	unset($pconfig);
}

foreach (array('server', 'client') as $mode) {
	if (is_array($config['installedpackages']["openvpn$mode"]['config'])) {
		foreach ($config['installedpackages']["openvpn$mode"]['config'] as $id => $settings) {
			$setting = $config['installedpackages']["openvpn$mode"]['config'][$id];
			if (!$setting['disable']) {
				$pconfig['name'] = "openvpn";
				$pconfig['mode'] = $mode;
				$pconfig['id'] = $id;
				$pconfig['description'] = "OpenVPN ".$mode.": ".htmlspecialchars($setting['description']);
				$services[] = $pconfig;
				unset($pconfig);
			}
		}
	}
}


if($services) {
	foreach($services as $service) {
		if(!$service['name']) continue;
		if(!$service['description']) $service['description'] = get_pkg_descr($service['name']);
		echo '<tr><td class="listlr">' . $service['name'] . '</td>';
		echo '<td class="listr">' . $service['description'] . '</td>';
		if ($service['name'] == "openvpn") {
			$running =  (is_pid_running($g['varrun_path'] . "/openvpn_{$service['mode']}{$service['id']}.pid") );
		} else {
			$running = (is_service_running($service['name'], $ps) or is_process_running($service['name']) );
		}
		if($running) {
			echo '<td class="listr"><center>';
			echo "<img src=\"/themes/" . $g["theme"] . "/images/icons/icon_pass.gif\"> Running</td>";
		} else {
			echo '<td class="listbg"><center>';
			echo "<img src=\"/themes/" . $g["theme"] . "/images/icons/icon_block.gif\"> <font color=\"white\">Stopped</td>";
		}
		echo '<td valign="middle" class="list" nowrap>';
		if($running) {
			if ($service['name'] == "openvpn") {
				echo "<a href='status_services.php?mode=restartservice&service={$service['name']}&vpnmode={$service['mode']}&id={$service['id']}'>";
			} else {
				echo "<a href='status_services.php?mode=restartservice&service={$service['name']}'>";
			}
			echo "<img title='Restart Service' border='0' src='./themes/".$g['theme']."/images/icons/icon_service_restart.gif'></a> ";
			if ($service['name'] == "openvpn") {
				echo "<a href='status_services.php?mode=stopservice&service={$service['name']}&vpnmode={$service['mode']}&id={$service['id']}'>";
			} else {
				echo "<a href='status_services.php?mode=stopservice&service={$service['name']}'> ";
			}
			echo "<img title='Stop Service' border='0' src='./themes/".$g['theme']."/images/icons/icon_service_stop.gif'> ";
			echo "</a>";
		} else {
			if ($service['name'] == "openvpn") {
				echo "<a href='status_services.php?mode=startservice&service={$service['name']}&vpnmode={$service['mode']}&id={$service['id']}'>";
			} else {
				echo "<a href='status_services.php?mode=startservice&service={$service['name']}'> ";
			}

			echo "<img title='Start Service' border='0' src='./themes/".$g['theme']."/images/icons/icon_service_start.gif'></a> ";
		}
		echo '</td>';
		echo '</tr>';
	}
} else {
	echo "<tr><td colspan=\"3\"><center>No services found.</td></tr>";
}

?>
</table>

</td>
</tr></table>
</div>

<?php include("fend.inc"); ?>
</body>
</html>