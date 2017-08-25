<?php
/*
	services_captiveportal_ip_edit.php
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
	pfSense_BUILDER_BINARIES:	/sbin/ipfw
	pfSense_MODULE:	captiveportal
*/

##|+PRIV
##|*IDENT=page-services-captiveportal-editallowedips
##|*NAME=Services: Captive portal: Edit Allowed IPs page
##|*DESCR=Allow access to the 'Services: Captive portal: Edit Allowed IPs' page.
##|*MATCH=services_captiveportal_ip_edit.php*
##|-PRIV

function allowedipscmp($a, $b) {
	return strcmp($a['ip'], $b['ip']);
}

function allowedips_sort() {
        global $g, $config;

        usort($config['captiveportal']['allowedip'],"allowedipscmp");
}

$pgtitle = array("Services","Captive portal","Edit allowed IP address");
require("guiconfig.inc");
require("functions.inc");
require("filter.inc");
require("shaper.inc");
require("captiveportal.inc");

if (!is_array($config['captiveportal']['allowedip']))
	$config['captiveportal']['allowedip'] = array();

$a_allowedips = &$config['captiveportal']['allowedip'];

$id = $_GET['id'];
if (isset($_POST['id']))
	$id = $_POST['id'];

if (isset($id) && $a_allowedips[$id]) {
	$pconfig['ip'] = $a_allowedips[$id]['ip'];
	$pconfig['descr'] = $a_allowedips[$id]['descr'];
	$pconfig['dir'] = $a_allowedips[$id]['dir'];
}

if ($_POST) {

	unset($input_errors);
	$pconfig = $_POST;

	/* input validation */
	$reqdfields = explode(" ", "ip dir");
	$reqdfieldsn = explode(",", "Allowed IP address,Direction");

	do_input_validation($_POST, $reqdfields, $reqdfieldsn, &$input_errors);

	if (($_POST['ip'] && !is_ipaddr($_POST['ip']))) {
		$input_errors[] = "A valid IP address must be specified. [".$_POST['ip']."]";
	}

	foreach ($a_allowedips as $ipent) {
		if (isset($id) && ($a_allowedips[$id]) && ($a_allowedips[$id] === $ipent))
			continue;

		if (($ipent['dir'] == $_POST['dir']) && ($ipent['ip'] == $_POST['ip'])){
			$input_errors[] = "[" . $_POST['ip'] . "] already allowed." ;
			break ;
		}
	}

	if (!$input_errors) {
		$ip = array();
		$ip['ip'] = $_POST['ip'];
		$ip['descr'] = $_POST['descr'];
		$ip['dir'] = $_POST['dir'];

		if (isset($id) && $a_allowedips[$id])
			$a_allowedips[$id] = $ip;
		else
			$a_allowedips[] = $ip;
		allowedips_sort();

		write_config();

		if (isset($config['captiveportal']['enable'])) {
			if ($ip['dir'] == "from")
				mwexec("/sbin/ipfw table 1 add " . $ip['ip']);
			else
				mwexec("/sbin/ipfw table 2 add " . $ip['ip']);
		}

		header("Location: services_captiveportal_ip.php");
		exit;
	}
}

include("head.inc");

?>
<?php include("fbegin.inc"); ?>
<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<?php if ($input_errors) print_input_errors($input_errors); ?>
            <form action="services_captiveportal_ip_edit.php" method="post" name="iform" id="iform">
              <table width="100%" border="0" cellpadding="6" cellspacing="0">
				<tr>
                  <td width="22%" valign="top" class="vncellreq">Direction</td>
                  <td width="78%" class="vtable">
					<select name="dir" class="formselect">
					<?php
					$dirs = explode(" ", "From To") ;
					foreach ($dirs as $dir): ?>
					<option value="<?=strtolower($dir);?>" <?php if (strtolower($dir) == strtolower($pconfig['dir'])) echo "selected";?> >
					<?=htmlspecialchars($dir);?>
					</option>
					<?php endforeach; ?>
					</select>
                    <br>
                    <span class="vexpl">Use <em>From</em> to always allow an IP address through the captive portal (without authentication).
                    Use <em>To</em> to allow access from all clients (even non-authenticated ones) behind the portal to this IP address.</span></td>
                </tr>
				<tr>
                  <td width="22%" valign="top" class="vncellreq">IP address</td>
                  <td width="78%" class="vtable">
                    <?=$mandfldhtml;?><input name="ip" type="text" class="formfld unknown" id="ip" size="17" value="<?=htmlspecialchars($pconfig['ip']);?>">
                    <br>
                    <span class="vexpl">IP address</span></td>
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
                    <?php if (isset($id) && $a_allowedips[$id]): ?>
                    <input name="id" type="hidden" value="<?=$id;?>">
                    <?php endif; ?>
                  </td>
                </tr>
              </table>
</form>
<?php include("fend.inc"); ?>
</body>
</html>