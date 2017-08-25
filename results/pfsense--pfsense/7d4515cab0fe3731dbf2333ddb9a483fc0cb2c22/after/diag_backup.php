<?php
/* $Id$ */
/*
	diag_backup.php
	Copyright (C) 2004,2005,2006 Scott Ullrich
	All rights reserved.

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

/* Allow additional execution time 0 = no limit. */
ini_set('max_execution_time', '3600');
ini_set('max_input_time', '3600');

/* omit no-cache headers because it confuses IE with file downloads */
$omit_nocacheheaders = true;
require("guiconfig.inc");

function remove_bad_chars($string) {
	return preg_replace('/[^a-z|_|0-9]/i','',$string);
}

function spit_out_select_items($area) {
	$select = <<<EOD
	<select name="{$area}">
		<option VALUE="">ALL</option>
		<option VALUE="aliases">Aliases</option>
		<option VALUE="shaper">Traffic Shaper</option>
		<option VALUE="filter">Firewall Rules</option>
		<option VALUE="nat">NAT</option>
		<option VALUE="pptpd">PPTP Server</option>
		<option VALUE="ipsec">IPsec VPN</option>
		<option VALUE="captiveportal">Captive Portal</option>
		<option VALUE="installedpackages">Package Manager</option>
		<option VALUE="interfaces">Interfaces</option>
		<option VALUE="dhcpd">DHCP Server</option>
		<option VALUE="syslog">Syslog</option>
		<option VALUE="system">System</option>
		<option VALUE="staticroutes">Static routes</option>
	</select>
EOD;
	echo $select;

}

if ($_POST) {
	unset($input_errors);
	if (stristr($_POST['Submit'], "Restore configuration"))
		$mode = "restore";
	else if (stristr($_POST['Submit'], "Reinstall"))
		$mode = "reinstallpackages";
	else if (stristr($_POST['Submit'], "Download"))
		$mode = "download";
	else if (stristr($_POST['Submit'], "Restore version"))
		$mode = "restore_ver";

	if ($_POST["nopackages"] <> "")
		$options = "nopackages";

	if ($_POST["ver"] <> "")
		$ver2restore = $_POST["ver"];

	if ($mode) {
		if ($mode == "download") {
			config_lock();
			$fn = "config-" . $config['system']['hostname'] . "." .
				$config['system']['domain'] . "-" . date("YmdHis") . ".xml";
			if($options == "nopackages") {
				exec("sed '/<installedpackages>/,/<\/installedpackages>/d' /conf/config.xml > /tmp/config.xml.nopkg");
				$fs = filesize("{$g['tmp_path']}/config.xml.nopkg");
				header("Content-Type: application/octet-stream");
                        	header("Content-Disposition: attachment; filename=$fn");
                        	header("Content-Length: $fs");
				readfile("{$g['tmp_path']}/config.xml.nopkg");
			} else {
				if($_POST['backuparea'] <> "") {
					/* user wishes to backup specific area of configuration */
					$current_trafficshaper_section = backup_config_section($_POST['backuparea']);
					/* generate aliases xml */
					$fout = fopen("{$g['tmp_path']}/backup_section.txt","w");
					fwrite($fout, $current_trafficshaper_section);
					fclose($fout);
					$fs = filesize($g['tmp_path'] . "/backup_section.txt");
					header("Content-Type: application/octet-stream");
					$fn = $_POST['backuparea'] . "-" . $fn;
					header("Content-Disposition: attachment; filename=$fn");
					header("Content-Length: $fs");
					readfile($g['tmp_path'] . "/backup_section.txt");
					unlink($g['tmp_path'] . "/backup_section.txt");
				} else {
					$fs = filesize($g['conf_path'] . "/config.xml");
					header("Content-Type: application/octet-stream");
					header("Content-Disposition: attachment; filename=$fn");
					header("Content-Length: $fs");
					readfile($g['conf_path'] . "/config.xml");
				}
			}
			config_unlock();
			exit;
		} else if ($mode == "restore") {
			if (is_uploaded_file($_FILES['conffile']['tmp_name'])) {
				$fd = fopen($_FILES['conffile']['tmp_name'], "r");
				if(!$fd) {
					log_error("Warning, could not open " . $_FILES['conffile']['tmp_name']);
					return 1;
				}
				while(!feof($fd)) {
					    $tmp .= fread($fd,49);
				}
				fclose($fd);
				if(stristr($tmp, "m0n0wall") == true) {
					log_error("Upgrading m0n0wall configuration to pfsense.");
					/* m0n0wall was found in config.  convert it. */
					$upgradedconfig = str_replace("m0n0wall", "pfsense", $tmp);
					$fd = fopen($_FILES['conffile']['tmp_name'], "w");
					fwrite($fd, $upgradedconfig);
					fclose($fd);
					$m0n0wall_upgrade = true;
				}
				if($_POST['restorearea'] <> "") {
					/* restore a specific area of the configuration */
					$rules = file_get_contents($_FILES['conffile']['tmp_name']);
					if(stristr($rules, $_POST['restorearea']) == false) {
						$input_errors[] = "You have selected to restore a area but we could not locate the correct xml tag.";
					} else {
						restore_config_section($_POST['restorearea'], $rules);
						filter_configure();
						$savemsg = "The configuration area has been restored.  You may need to reboot the firewall.";
					}
				} else {
					$rules = file_get_contents($_FILES['conffile']['tmp_name']);
					if(stristr($rules, "pfsense") == false) {
						$input_errors[] = "You have selected to restore the full configuration but we could not locate a pfsense tag.";
					} else {
						/* restore the entire configuration */
						if (config_install($_FILES['conffile']['tmp_name']) == 0) {
							/* this will be picked up by /index.php */
							conf_mount_rw();
							if($g['platform'] <> "cdrom")
								touch("/needs_package_sync");
							$reboot_needed = true;
							$savemsg = "The configuration has been restored. The firewall is now rebooting.";
							/* remove cache, we will force a config reboot */
							if(file_exists("/tmp/config.cache"))
								unlink("/tmp/config.cache");
							$config = parse_config(true);
							if($m0n0wall_upgrade == true) {
								if($config['system']['gateway'] <> "")
									$config['interfaces']['wan']['gateway'] = $config['system']['gateway'];
								unset($config['shaper']);
								/* optional if list */
								$ifdescrs = get_configured_interface_list(true, true);
								/* remove special characters from interface descriptions */
								if(is_array($ifdescrs))
									foreach($ifdescrs as $iface)
										$config['interfaces'][$iface]['descr'] = remove_bad_chars($config['interfaces'][$iface]['descr']);
								unlink_if_exists("/tmp/config.cache");
								write_config();
								conf_mount_ro();
								$savemsg = "The m0n0wall configuration has been restored and upgraded to pfSense.<p>The firewall is now rebooting.";
								$reboot_needed = true;
							}
							if(isset($config['captiveportal']['enable'])) {
								/* for some reason ipfw doesn't init correctly except on bootup sequence */
								$savemsg = "The configuration has been restored.<p>The firewall is now rebooting.";
								$reboot_needed = true;
							}
							setup_serial_port();
							if(is_interface_mismatch() == true) {
								touch("/var/run/interface_mismatch_reboot_needed");
								$reboot_needed = false;
								header("Location: interfaces_assign.php");
							}
						} else {
							$input_errors[] = "The configuration could not be restored.";
						}
					}
				}
			} else {
				$input_errors[] = "The configuration could not be restored (file upload error).";
			}
		} else if ($mode == "reinstallpackages") {
			header("Location: pkg_mgr_install.php?mode=reinstallall");
			exit;
                } else if ($mode == "restore_ver") {
			$input_errors[] = "XXX - this feature may hose your config (do NOT backrev configs!) - billm";
			if ($ver2restore <> "") {
				$conf_file = "{$g['cf_conf_path']}/bak/config-" . strtotime($ver2restore) . ".xml";
                                if (config_install($conf_file) == 0) {
									$reboot_needed = true;
                                    $savemsg = "The configuration has been restored. The firewall is now rebooting.";
                                } else {
                                	$input_errors[] = "The configuration could not be restored.";
                                }
                        } else {
                                $input_errors[] = "No version selected.";
                        }
		}
	}
}

$id = rand() . '.' . time();

$mth = ini_get('upload_progress_meter.store_method');
$dir = ini_get('upload_progress_meter.file.filename_template');

$pgtitle = array("Diagnostics","Backup/restore");
include("head.inc");

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php include("fbegin.inc"); ?>
<form action="diag_backup.php" method="post" enctype="multipart/form-data">
<?php if ($input_errors) print_input_errors($input_errors); ?>
<?php if ($savemsg) print_info_box($savemsg); ?>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
<?php
		$tab_array = array();
		$tab_array[0] = array("Config History", false, "diag_confbak.php");
		$tab_array[1] = array("Backup/Restore", true, "diag_backup.php");
		display_top_tabs($tab_array);
?>
		</td>
	</tr>
	<tr>
		<td>
			<div id="mainarea">
			<table class="tabcont" align="center" width="100%" border="0" cellpadding="6" cellspacing="0">
				<tr>
					<td colspan="2" class="listtopic">Backup configuration</td>
				</tr>
				<tr>
					<td width="22%" valign="baseline" class="vncell">&nbsp;</td>
					<td width="78%" class="vtable">
						<p>Click this button to download the system configuration in XML format.<br /><br /> Backup area: <?php spit_out_select_items("backuparea"); ?></p>
						<p><input name="nopackages" type="checkbox" class="formcheckbox" id="nopackages">Do not backup package information.</p>
						<p><input name="Submit" type="submit" class="formbtn" id="download" value="Download configuration"></p>
					</td>
				</tr>
				<tr>
					<td colspan="2" class="list" height="12">&nbsp;</td>
                </tr>
                <tr>
					<td colspan="2" class="listtopic">Restore configuration</td>
				</tr>
				<tr>
					<td width="22%" valign="baseline" class="vncell">&nbsp;</td>
					<td width="78%" class="vtable">
						Open a <?=$g['[product_name']?> configuration XML file and click the button below to restore the configuration. <br /><br /> Restore area: <?php spit_out_select_items("restorearea"); ?>
						<p><input name="conffile" type="file" class="formfld unknown" id="conffile" size="40"></p>
						<p><input name="Submit" type="submit" class="formbtn" id="restore" value="Restore configuration"></p>
                      	<p><strong><span class="red">Note:</span></strong><br />The firewall may need a reboot after restoring the configuration.<br /></p>
					</td>
				</tr>
				<?php if($config['installedpackages']['package'] != "") { ?>
				<tr>
					<td colspan="2" class="list" height="12">&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2" class="listtopic">Reinstall packages</td>
				</tr>
				<tr>
					<td width="22%" valign="baseline" class="vncell">&nbsp;</td>
					<td width="78%" class="vtable">
						<p>Click this button to reinstall all system packages.  This may take a while. <br /><br />
		  				<input name="Submit" type="submit" class="formbtn" id="reinstallpackages" value="Reinstall packages">
					</td>
				</tr>
				<?php } ?>
			</table>
			</div>
		</td>
	</tr>
</table>
</form>

<?php include("fend.inc"); ?>
</body>
</html>

<?php

if($reboot_needed == true) {
	ob_flush();
	flush();
	sleep(5);
	while(file_exists("{$g['varrun_path']}/config.lock"))
		sleep(3);
	mwexec("/sbin/shutdown -r now");
	exit;
}

?>