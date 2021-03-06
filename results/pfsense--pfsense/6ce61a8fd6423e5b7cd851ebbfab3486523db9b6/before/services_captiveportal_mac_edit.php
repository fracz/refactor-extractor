<?php
/*
	services_captiveportal_mac_edit.php
	part of m0n0wall (http://m0n0.ch/wall)

	Copyright (C) 2004 Dinesh Nair <dinesh@alphaque.com>
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
/*
	pfSense_MODULE:	captiveportal
*/

##|+PRIV
##|*IDENT=page-services-captiveportal-editmacaddresses
##|*NAME=Services: Captive portal: Edit MAC Addresses page
##|*DESCR=Allow access to the 'Services: Captive portal: Edit MAC Addresses' page.
##|*MATCH=services_captiveportal_mac_edit.php*
##|-PRIV

function passthrumacscmp($a, $b) {
	return strcmp($a['mac'], $b['mac']);
}

function passthrumacs_sort() {
        global $config;

        usort($config['captiveportal']['passthrumac'],"passthrumacscmp");
}

$pgtitle = array("Services","Captive portal","Edit pass-through MAC address");
require("guiconfig.inc");
require("functions.inc");
require("filter.inc");
require("shaper.inc");
require("captiveportal.inc");

if (!is_array($config['captiveportal']['passthrumac']))
	$config['captiveportal']['passthrumac'] = array();

$a_passthrumacs = &$config['captiveportal']['passthrumac'];

$id = $_GET['id'];
if (isset($_POST['id']))
	$id = $_POST['id'];

if (isset($id) && $a_passthrumacs[$id]) {
	$pconfig['mac'] = $a_passthrumacs[$id]['mac'];
	$pconfig['descr'] = $a_passthrumacs[$id]['descr'];
}

if ($_POST) {

	unset($input_errors);
	$pconfig = $_POST;

	/* input validation */
	$reqdfields = explode(" ", "mac");
	$reqdfieldsn = explode(",", "MAC address");

	do_input_validation($_POST, $reqdfields, $reqdfieldsn, &$input_errors);

	$_POST['mac'] = str_replace("-", ":", $_POST['mac']);

	if (($_POST['mac'] && !is_macaddr($_POST['mac']))) {
		$input_errors[] = "A valid MAC address must be specified. [".$_POST['mac']."]";
	}

	foreach ($a_passthrumacs as $macent) {
		if (isset($id) && ($a_passthrumacs[$id]) && ($a_passthrumacs[$id] === $macent))
			continue;

		if ($macent['mac'] == $_POST['mac']){
			$input_errors[] = "[" . $_POST['mac'] . "] already allowed." ;
			break;
		}
	}

	if (!$input_errors) {
		$mac = array();
		$mac['mac'] = $_POST['mac'];
		$mac['descr'] = $_POST['descr'];

		if (isset($id) && $a_passthrumacs[$id])
			$a_passthrumacs[$id] = $mac;
		else
			$a_passthrumacs[] = $mac;
		passthrumacs_sort();

		write_config();

		mark_subsystem_dirty('passthrumac');

		header("Location: services_captiveportal_mac.php");
		exit;
	}
}
include("head.inc");
?>
<?php include("fbegin.inc"); ?>
<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php if ($input_errors) print_input_errors($input_errors); ?>
            <form action="services_captiveportal_mac_edit.php" method="post" name="iform" id="iform">
              <table width="100%" border="0" cellpadding="6" cellspacing="0">
				<tr>
                  <td width="22%" valign="top" class="vncellreq">MAC address</td>
                  <td width="78%" class="vtable">
                    <?=$mandfldhtml;?><input name="mac" type="text" class="formfld unknown" id="mac" size="17" value="<?=htmlspecialchars($pconfig['mac']);?>">
                    <br>
                    <span class="vexpl">MAC address (6 hex octets separated by colons)</span></td>
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
                    <input name="Submit" type="submit" class="formbtn" value="Save">
                    <?php if (isset($id) && $a_passthrumacs[$id]): ?>
                    <input name="id" type="hidden" value="<?=$id;?>">
                    <?php endif; ?>
                  </td>
                </tr>
              </table>
</form>
<?php include("fend.inc"); ?>
</body>
</html>