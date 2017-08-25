<?php
/* $Id$ */
/*
	system_firmware.php
	Copyright (C) 2008 Scott Ullrich <sullrich@gmail.com>
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

##|+PRIV
##|*IDENT=page-system-firmware-manualupdate
##|*NAME=System: Firmware: Manual Update page
##|*DESCR=Allow access to the 'System: Firmware: Manual Update' page.
##|*MATCH=system_firmware.php*
##|-PRIV

$d_isfwfile = 1;
require_once("guiconfig.inc");

$curcfg = $config['system']['firmware'];

require_once("xmlrpc_client.inc");

/* Allow additional execution time 0 = no limit. */
ini_set('max_execution_time', '9999');
ini_set('max_input_time', '9999');

/* if upgrade in progress, alert user */
if(is_subsystem_dirty('firmwarelock')) {
	$pgtitle = array("System","Firmware","Manual Update");
	include("head.inc");
	echo "<body link=\"#0000CC\" vlink=\"#0000CC\" alink=\"#0000CC\">\n";
	include("fbegin.inc");
	echo "<div>\n";
	print_info_box("An upgrade is currently in progress.<p>The firewall will reboot when the operation is complete.<p><center><img src='/themes/{$g['theme']}/images/icons/icon_fw-update.gif'>");
	echo "</div>\n";
	include("fend.inc");
	echo "</body>";
	echo "</html>";
	exit;
}

if($_POST['kerneltype']) {
	if($_POST['kerneltype'] == "single")
		system("touch /boot/kernel/pfsense_kernel.txt");
	else
		system("echo {$_POST['kerneltype']} > /boot/kernel/pfsense_kernel.txt");
}

/* Handle manual upgrade */
if ($_POST && !is_subsystem_dirty('firmwarelock')) {

	unset($input_errors);
	unset($sig_warning);

	if (stristr($_POST['Submit'], "Enable"))
		$mode = "enable";
	else if (stristr($_POST['Submit'], "Disable"))
		$mode = "disable";
	else if (stristr($_POST['Submit'], "Upgrade") || $_POST['sig_override'])
		$mode = "upgrade";
	else if ($_POST['sig_no']) {
		if(file_exists("{$g['upload_path']}/firmware.tgz"))
				unlink("{$g['upload_path']}/firmware.tgz");
	}
	if ($mode) {
		if ($mode == "enable") {
			exec_rc_script("/etc/rc.firmware enable");
			conf_mount_rw();
			mark_subsystem_dirty('firmware');
		} else if ($mode == "disable") {
			exec_rc_script("/etc/rc.firmware disable");
			conf_mount_ro();
			clear_subsystem_dirty('firmware');
		} else if ($mode == "upgrade") {
			if (is_uploaded_file($_FILES['ulfile']['tmp_name'])) {
				/* verify firmware image(s) */
				if (!stristr($_FILES['ulfile']['name'], $g['platform']) && !$_POST['sig_override'])
					$input_errors[] = "The uploaded image file is not for this platform ({$g['platform']}).";
				else if (!file_exists($_FILES['ulfile']['tmp_name'])) {
					/* probably out of memory for the MFS */
					$input_errors[] = "Image upload failed (out of memory?)";
					exec_rc_script("/etc/rc.firmware disable");
					clear_subsystem_dirty('firmware');
				} else {
					/* move the image so PHP won't delete it */
					rename($_FILES['ulfile']['tmp_name'], "{$g['upload_path']}/firmware.tgz");

					/* check digital signature */
					$sigchk = verify_digital_signature("{$g['upload_path']}/firmware.tgz");

					if ($sigchk == 1)
						$sig_warning = "The digital signature on this image is invalid.";
					else if ($sigchk == 2)
						$sig_warning = "This image is not digitally signed.";
					else if (($sigchk == 3) || ($sigchk == 4))
						$sig_warning = "There has been an error verifying the signature on this image.";

					if (!verify_gzip_file("{$g['upload_path']}/firmware.tgz")) {
						$input_errors[] = "The image file is corrupt.";
						unlink("{$g['upload_path']}/firmware.tgz");
					}
				}
			}

			run_plugins("/usr/local/pkg/firmware_upgrade");

            /* Check for input errors, firmware locks, warnings, then check for firmware if sig_override is set */
            if (!$input_errors && !is_subsystem_dirty('firmwarelock') && (!$sig_warning || $_POST['sig_override'])) {
                    if (file_exists("{$g['upload_path']}/firmware.tgz")) {
                            /* fire up the update script in the background */
				mark_subsystem_dirty('firmwarelock');
                            $savemsg = "The firmware is now being updated. The firewall will reboot automatically.";
							if(stristr($_FILES['ulfile']['name'],"nanobsd"))
								mwexec_bg("/etc/rc.firmware pfSenseNanoBSDupgrade {$g['upload_path']}/firmware.tgz");
							else if(stristr($_FILES['ulfile']['name'],"bdiff"))
                            	mwexec_bg("/etc/rc.firmware delta_update {$g['upload_path']}/firmware.tgz");
							else
								mwexec_bg("/etc/rc.firmware pfSenseupgrade {$g['upload_path']}/firmware.tgz");
                    } else {
                            $savemsg = "Firmware image missing or other error, please try again.";
                    }
            }
		}
	}
}

$pgtitle = array("Diagnostics","Firmware");
include("head.inc");

?>
<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<form action="system_firmware.php" method="post" enctype="multipart/form-data">
<?php
	/* Construct an upload_id for this session */
	$upload_id = "up". $_SESSION['Username'];
?>
<input type="hidden" name="UPLOAD_IDENTIFIER" value="<?php echo $upload_id;?>" />
<?php include("fbegin.inc"); ?>
<?php if ($input_errors) print_input_errors($input_errors); ?>
<?php if ($savemsg) print_info_box($savemsg); ?>
<?php if ($fwinfo <> "") print_info_box($fwinfo); ?>
<?php if ($sig_warning && !$input_errors): ?>
<?php
	$sig_warning = "<strong>" . $sig_warning . "</strong><br>This means that the image you uploaded " .
		"is not an official/supported image and may lead to unexpected behavior or security " .
		"compromises. Only install images that come from sources that you trust, and make sure ".
		"that the image has not been tampered with.<br><br>".
		"Do you want to install this image anyway (on your own risk)?";
print_info_box($sig_warning);
?>
<input name="sig_override" type="submit" class="formbtn" id="sig_override" value=" Yes ">
<input name="sig_no" type="submit" class="formbtn" id="sig_no" value=" No ">
<?php else: ?>
<?php if (!is_subsystem_dirty('firmwarelock')): ?>
	<table width="100%" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td>
<?php
	$tab_array = array();
	$tab_array[0] = array("Manual Update", true, "system_firmware.php");
	$tab_array[1] = array("Auto Update", false, "system_firmware_check.php");
	$tab_array[2] = array("Updater Settings", false, "system_firmware_settings.php");
	display_top_tabs($tab_array);
?>
			</td>
		</tr>
		<tr>
			<td>
				<div id="mainarea">
					<table class="tabcont" width="100%" border="0" cellpadding="6" cellspacing="0">
                	<tr>
		 				<td colspan="2" class="listtopic">Invoke <?=$g['product_name']?> Manual Upgrade</td>
					</tr>
					<tr>
		  				<td width="22%" valign="baseline" class="vncell">&nbsp;</td>
                  		<td width="78%" class="vtable">
						<p>
							Click &quot;Enable firmware
							upload&quot; below, then choose the image file (<?=$g['firmware_update_text'];?>)
							to be uploaded.
							<br>
							Click &quot;Upgrade firmware&quot;
							to start the upgrade process.
						</p>
						<?php if (!is_subsystem_dirty('rebootreq')): ?>
						<?php if (!is_subsystem_dirty('firmware')): ?>
							<input name="Submit" type="submit" class="formbtn" value="Enable firmware upload">
						<?php else: ?>
				  			<input name="Submit" type="submit" class="formbtn" value="Disable firmware upload">
							<br><br>
							<strong>Firmware image file: </strong>&nbsp;
							<input name="ulfile" type="file" class="formfld">
							<br><br>
							<?php
						  		if(!file_exists("/boot/kernel/pfsense_kernel.txt")) {
						  			if($g['platform'] == "pfSense") {
										echo "Please select kernel type: ";
										echo "<select name='kerneltype'>";
										echo "<option value='SMP'>Multiprocessor kernel</option>";
										echo "<option value='single'>Uniprocessor kernel</option>";
										echo "<option value='wrap'>Embedded kernel</option>";
										echo "<option value='Developers'>Developers kernel</option>";
										echo "</select>";
										echo "<br><br>";
									}
								}
							?>
							<?php
								/*
								<input name="Submit" type="submit" class="formbtn" value="Upgrade firmware" onClick="window.open('upload_progress.php?upload_id=<?=$upload_id?>','UploadMeter','width=370,height=115', true); return true;">
								*/
							?>
							<input name="Submit" type="submit" class="formbtn" value="Upgrade firmware">
						<?php endif; else: ?>
							<strong>You must reboot the system before you can upgrade the firmware.</strong>
						<?php endif; ?>
					</td>
				</td>
			</tr>
			<tr>
				<td width="22%" valign="top">&nbsp;</td>
				<td width="78%"><span class="vexpl"><span class="red"><strong>Warning:<br>
				</strong></span>DO NOT abort the firmware upgrade once it
				has started. The firewall will reboot automatically after
				storing the new firmware. The configuration will be maintained.</span></td>
			</table>
		</div>
	</tr>
</table>

<?php endif; endif; ?>
<?php include("fend.inc"); ?>
</body>
</html>