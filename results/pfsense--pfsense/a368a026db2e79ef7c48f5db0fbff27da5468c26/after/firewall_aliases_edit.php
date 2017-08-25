<?php
/* $Id$ */
/*
	firewall_aliases_edit.php
	Copyright (C) 2004 Scott Ullrich
	All rights reserved.

	originially part of m0n0wall (http://m0n0.ch/wall)
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
##|*IDENT=page-firewall-alias-edit
##|*NAME=Firewall: Alias: Edit page
##|*DESCR=Allow access to the 'Firewall: Alias: Edit' page.
##|*MATCH=firewall_aliases_edit.php*
##|-PRIV


$pgtitle = array("Firewall","Aliases","Edit");

require("guiconfig.inc");

if (!is_array($config['aliases']['alias']))
	$config['aliases']['alias'] = array();

aliases_sort();
$a_aliases = &$config['aliases']['alias'];

$id = $_GET['id'];
if (isset($_POST['id']))
	$id = $_POST['id'];

if (isset($id) && $a_aliases[$id]) {
	$pconfig['name'] = $a_aliases[$id]['name'];
	$pconfig['detail'] = $a_aliases[$id]['detail'];
	$pconfig['address'] = $a_aliases[$id]['address'];
	$pconfig['descr'] = html_entity_decode($a_aliases[$id]['descr']);

	/* optional if list */
	$iflist = get_configured_interface_with_descr(true, true);
	foreach ($iflist as $if => $ifdesc)
		if($ifdesc == $pconfig['descr'])
			$input_errors[] = "Sorry, an interface is already named {$pconfig['descr']}.";

	$addresses = explode(' ', $pconfig['address']);
	$address = explode("/", $addresses[0]);
	if ($address[1])
		$addresssubnettest = true;
	else
		$addresssubnettest = false;

	if ($addresssubnettest)
		$pconfig['type'] = "network";
	else
		if (is_ipaddr($address[0]))
			$pconfig['type'] = "host";
		else
			$pconfig['type'] = "port";

	if($a_aliases[$id]['aliasurl'] <> "") {
		$pconfig['type'] = "url";
		if(is_array($a_aliases[$id]['aliasurl'])) {
			$isfirst = 0;
			$pconfig['address'] = "";
			foreach($a_aliases[$id]['aliasurl'] as $aa) {
				if($isfirst == 1)
					$pconfig['address'] .= " ";
				$isfirst = 1;
				$pconfig['address'] .= $aa;
			}
		} else {
			$pconfig['address'] = $a_aliases[$id]['aliasurl'];
		}
	}
}

if ($_POST) {

	unset($input_errors);
	$pconfig = $_POST;

	/* input validation */
	if(strtolower($_POST['name']) == "pptp")
		$input_errors[] = gettext("Aliases may not be named PPTP.");

	$x = is_validaliasname($_POST['name']);
	if (!isset($x)) {
		$input_errors[] = "Reserved word used for alias name.";
	} else {
		if (is_validaliasname($_POST['name']) == false)
			$input_errors[] = "The alias name may only consist of the characters a-z, A-Z, 0-9, _.";
	}
	/* check for name conflicts */
	foreach ($a_aliases as $alias) {
		if (isset($id) && ($a_aliases[$id]) && ($a_aliases[$id] === $alias))
			continue;

		if ($alias['name'] == $_POST['name']) {
			$input_errors[] = "An alias with this name already exists.";
			break;
		}
	}

	/* check for name interface description conflicts */
	foreach($config['interfaces'] as $interface) {
		if($interface['descr'] == $_POST['name']) {
			$input_errors[] = "An interface description with this name already exists.";
			break;
		}
	}

	$alias = array();
	$alias['name'] = $_POST['name'];
	if($_POST['type'] == "url") {
		$address = "";
		$isfirst = 0;
		$address_count = 2;

		/* item is a url type */
		for($x=0; isset($_POST['address'. $x]); $x++) {
			if($_POST['address' . $x]) {
				/* fetch down and add in */
				$isfirst = 0;
				$temp_filename = tempnam("/tmp/", "alias_import");
				unlink($temp_filename);
				$fda = fopen("/tmp/tmpfetch","w");
				fwrite($fda, "/usr/bin/fetch -q -o \"{$temp_filename}/aliases\" \"" . $_POST['address' . $x] . "\"");
				fclose($fda);
				mwexec("mkdir -p {$temp_filename}");
				mwexec("/usr/bin/fetch -q -o \"{$temp_filename}/aliases\" \"" . $_POST['address' . $x] . "\"");
				/* if the item is tar gzipped then extract */
				if(stristr($_POST['address' . $x], ".tgz"))
					process_alias_tgz($temp_filename);
				if(file_exists("{$temp_filename}/aliases")) {
					$file_contents = file_get_contents("{$temp_filename}/aliases");
					$file_contents = str_replace("#", "\n#", $file_contents);
					$file_contents_split = split("\n", $file_contents);
					foreach($file_contents_split as $fc) {
						$tmp = trim($fc);
						if(stristr($fc, "#")) {
							$tmp_split = split("#", $tmp);
							$tmp = trim($tmp_split[0]);
						}
						if(trim($tmp) <> "") {
							if($isfirst == 1)
								$address .= " ";
							$address .= $tmp;
							$isfirst = 1;
						}
					}
					if($isfirst == 0) {
						/* nothing was found */
						$input_errors[] = "You must provide a valid URL. Could not fetch usable data.";
						$dont_update = true;
						break;
					}
					$alias['aliasurl'][] = $_POST['address' . $x];
					mwexec("/bin/rm -rf {$temp_filename}");
				} else {
					$input_errors[] = "You must provide a valid URL.";
					$dont_update = true;
					break;
				}
			}
		}
	} else {
		$address = "";
		$isfirst = 0;
		/* item is a normal alias type */
		for($x=0; $x<4999; $x++) {
			if($_POST["address{$x}"] <> "") {
				if ($isfirst > 0)
					$address .= " ";
				$address .= $_POST["address{$x}"];
				if($_POST["address_subnet{$x}"] <> "")
					$address .= "/" . $_POST["address_subnet{$x}"];

	       			if($_POST["detail{$x}"] <> "") {
	       				$final_address_details .= $_POST["detail{$x}"];
	       			} else {
		       			$final_address_details .= "Entry added" . " ";
		       			$final_address_details .= date('r');
	       			}
	       			$final_address_details .= "||";
				$isfirst++;
			}
		}
	}

	if (!$input_errors) {
		$alias['address'] = $address;
		$alias['descr'] = mb_convert_encoding($_POST['descr'],"HTML-ENTITIES","auto");
		$alias['type'] = $_POST['type'];
		$alias['detail'] = $final_address_details;

		if (isset($id) && $a_aliases[$id])
			$a_aliases[$id] = $alias;
		else
			$a_aliases[] = $alias;

		mark_subsystem_dirty('aliases');

		write_config();
		filter_configure();

		header("Location: firewall_aliases.php");
		exit;
	}
	//we received input errors, copy data to prevent retype
	else
	{
		$pconfig['descr'] = mb_convert_encoding($_POST['descr'],"HTML-ENTITIES","auto");
		$pconfig['address'] = $address;
		$pconfig['type'] = $_POST['type'];
		$pconfig['detail'] = $final_address_details;
	}
}

include("head.inc");

$jscriptstr = <<<EOD

<script type="text/javascript">
function typesel_change() {
	switch (document.iform.type.selectedIndex) {
		case 0:	/* host */
			var cmd;

			newrows = totalrows;
			for(i=0; i<newrows; i++) {
				comd = 'document.iform.address_subnet' + i + '.disabled = 1;';
				eval(comd);
				comd = 'document.iform.address_subnet' + i + '.value = "";';
				eval(comd);
			}
			break;
		case 1:	/* network */
			var cmd;

			newrows = totalrows;
			for(i=0; i<newrows; i++) {
				comd = 'document.iform.address_subnet' + i + '.disabled = 0;';
				eval(comd);
			}
			break;
		case 2:	/* port */
			var cmd;

			newrows = totalrows;
			for(i=0; i<newrows; i++) {
				comd = 'document.iform.address_subnet' + i + '.disabled = 1;';
				eval(comd);
				comd = 'document.iform.address_subnet' + i + '.value = "32";';
				eval(comd);
			}
			break;
		case 3:	/* OpenVPN Users */
			var cmd;

			newrows = totalrows;
			for(i=0; i<newrows; i++) {
				comd = 'document.iform.address_subnet' + i + '.disabled = 1;';
				eval(comd);
				comd = 'document.iform.address_subnet' + i + '.value = "";';
				eval(comd);
			}
			break;

		case 4:	/* url */
			var cmd;
			newrows = totalrows;
			for(i=0; i<newrows; i++) {
				comd = 'document.iform.address_subnet' + i + '.disabled = 0;';
				eval(comd);
			}
			break;
	}
}

EOD;

$network_str = gettext("Network");
$networks_str = gettext("Network(s)");
$cidr_str = gettext("CIDR");
$description_str = gettext("Description");
$hosts_str = gettext("Host(s)");
$ip_str = gettext("IP");
$ports_str = gettext("Port(s)");
$port_str = gettext("Port");
$url_str = gettext("URL");
$update_freq_str = gettext("Update Freq.");

$networks_help = gettext("Networks are specified in CIDR format.  Select the CIDR mask that pertains to each entry. /32 specifies a single host, /24 specifies 255.255.255.0, etc. Hostnames (FQDNs) may also be specified, using a /32 mask.");
$hosts_help = gettext("Enter as many hosts as you would like.  Hosts must be specified by their IP address.");
$ports_help = gettext("Enter as many ports as you wish.  Port ranges can be expressed by seperating with a colon.");
$url_help = gettext("Enter as many urls as you wish.  Also set the time that you would like the url refreshed in days.  After saving {$g['product_name']} will download the URL and import the items into the alias.");

$openvpn_str = gettext("Username");
$openvpn_user_str = gettext("OpenVPN Users");
$openvpn_help = gettext("Enter as many usernames as you wish.");
$openvpn_freq = gettext("");

$jscriptstr .= <<<EOD

function update_box_type() {
	var indexNum = document.forms[0].type.selectedIndex;
	var selected = document.forms[0].type.options[indexNum].text;
	if(selected == '{$networks_str}') {
		document.getElementById ("addressnetworkport").firstChild.data = "{$networks_str}";
		document.getElementById ("onecolumn").firstChild.data = "{$network_str}";
		document.getElementById ("twocolumn").firstChild.data = "{$cidr_str}";
		document.getElementById ("threecolumn").firstChild.data = "{$description_str}";
		document.getElementById ("itemhelp").firstChild.data = "{$networks_help}";
	} else if(selected == '{$hosts_str}') {
		document.getElementById ("addressnetworkport").firstChild.data = "{$hosts_str}";
		document.getElementById ("onecolumn").firstChild.data = "{$ip_str}";
		document.getElementById ("twocolumn").firstChild.data = "";
		document.getElementById ("threecolumn").firstChild.data = "{$description_str}";
		document.getElementById ("itemhelp").firstChild.data = "{$hosts_help}";
	} else if(selected == '{$ports_str}') {
		document.getElementById ("addressnetworkport").firstChild.data = "{$ports_str}";
		document.getElementById ("onecolumn").firstChild.data = "{$port_str}";
		document.getElementById ("twocolumn").firstChild.data = "";
		document.getElementById ("threecolumn").firstChild.data = "{$description_str}";
		document.getElementById ("itemhelp").firstChild.data = "{$ports_help}";
	} else if(selected == '{$url_str}') {
		document.getElementById ("addressnetworkport").firstChild.data = "{$url_str}";
		document.getElementById ("onecolumn").firstChild.data = "{$url_str}";
		document.getElementById ("twocolumn").firstChild.data = "{$update_freq_str}";
		document.getElementById ("threecolumn").firstChild.data = "{$description_str}";
		document.getElementById ("itemhelp").firstChild.data = "{$url_help}";
	} else if(selected == '{$openvpn_user_str}') {
		document.getElementById ("addressnetworkport").firstChild.data = "{$openvpn_user_str}";
		document.getElementById ("onecolumn").firstChild.data = "{$openvpn_str}";
		document.getElementById ("twocolumn").firstChild.data = "{$openvpn_freq}";
		document.getElementById ("threecolumn").firstChild.data = "{$description_str}";
		document.getElementById ("itemhelp").firstChild.data = "{$openvpn_help}";
	}
}
</script>

EOD;

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC" onload="<?= $jsevents["body"]["onload"] ?>">
<?php
	include("fbegin.inc");
	echo $jscriptstr;
?>

<script type="text/javascript" src="/javascript/row_helper.js">
</script>

<input type='hidden' name='address_type' value='textbox' />
<input type='hidden' name='address_subnet_type' value='select' />

<script type="text/javascript">
	rowname[0] = "address";
	rowtype[0] = "textbox";
	rowsize[0] = "30";

	rowname[1] = "address_subnet";
	rowtype[1] = "select";
	rowsize[1] = "1";

	rowname[2] = "detail";
	rowtype[2] = "textbox";
	rowsize[2] = "50";
</script>

<?php if ($input_errors) print_input_errors($input_errors); ?>
<div id="inputerrors"></div>

<form action="firewall_aliases_edit.php" method="post" name="iform" id="iform">
<table width="100%" border="0" cellpadding="6" cellspacing="0">
  <tr>
	<td colspan="2" valign="top" class="listtopic">Alias Edit</td>
  </tr>
<?php if(is_alias_inuse($pconfig['name']) == true): ?>
  <tr>
    <td valign="top" class="vncellreq">Name</td>
    <td class="vtable"> <input name="name" type="hidden" id="name" size="40" value="<?=htmlspecialchars($pconfig['name']);?>" />
		  <?php echo $pconfig['name']; ?>
      <p>
        <span class="vexpl">NOTE: This alias is in use so the name may not be modified!</span>
      </p>
    </td>
  </tr>
<?php else: ?>
  <tr>
    <td valign="top" class="vncellreq">Name</td>
    <td class="vtable">
      <input name="name" type="text" id="name" class="formfld unknown" size="40" value="<?=htmlspecialchars($pconfig['name']);?>" />
      <br />
      <span class="vexpl">
        The name of the alias may only consist of the characters a-z, A-Z and 0-9.
      </span>
    </td>
  </tr>
<?php endif; ?>
  <tr>
    <td width="22%" valign="top" class="vncell">Description</td>
    <td width="78%" class="vtable">
      <input name="descr" type="text" class="formfld unknown" id="descr" size="40" value="<?=$pconfig['descr'];?>" />
      <br />
      <span class="vexpl">
        You may enter a description here for your reference (not parsed).
      </span>
    </td>
  </tr>
  <tr>
    <td valign="top" class="vncellreq">Type</td>
    <td class="vtable">
      <select name="type" class="formselect" id="type" onchange="update_box_type(); typesel_change();">
        <option value="host" <?php if ($pconfig['type'] == "host") echo "selected"; ?>>Host(s)</option>
        <option value="network" <?php if ($pconfig['type'] == "network") echo "selected"; ?>>Network(s)</option>
        <option value="port" <?php if ($pconfig['type'] == "port") echo "selected"; ?>>Port(s)</option>
        <option value="openvpn" <?php if ($pconfig['type'] == "openvpn") echo "selected"; ?>>OpenVPN Users</option>
      </select>
    </td>
  </tr>
  <tr>
    <td width="22%" valign="top" class="vncellreq"><div id="addressnetworkport">Host(s)</div></td>
    <td width="78%" class="vtable">
      <table id="maintable">
        <tbody>
          <tr>
            <td colspan="4">
      		    <div style="padding:5px; margin-top: 16px; margin-bottom: 16px; border:1px dashed #000066; background-color: #ffffff; color: #000000; font-size: 8pt;" id="itemhelp">Item information</div>
            </td>
          </tr>
          <tr>
            <td><div id="onecolumn">Network</div></td>
            <td><div id="twocolumn">CIDR</div></td>
           <td><div id="threecolumn">Description</div></td>
          </tr>

	<?php
	$counter = 0;
	$address = $pconfig['address'];
	if ($address <> "") {
		$item = explode(" ", $address);
		$item3 = explode("||", $pconfig['detail']);
		foreach($item as $ww) {
			$address = $item[$counter];
			$address_subnet = "";
			$item2 = explode("/", $address);
			foreach($item2 as $current) {
				if($item2[1] <> "") {
					$address = $item2[0];
					$address_subnet = $item2[1];
				}
			}
			$item4 = $item3[$counter];
			$tracker = $counter;
	?>
          <tr>
            <td>
              <input name="address<?php echo $tracker; ?>" type="text" class="formfld unknown" id="address<?php echo $tracker; ?>" size="30" value="<?=htmlspecialchars($address);?>" />
            </td>
            <td>
			        <select name="address_subnet<?php echo $tracker; ?>" class="formselect" id="address_subnet<?php echo $tracker; ?>">
			          <option></option>
			          <?php for ($i = 32; $i >= 1; $i--): ?>
			          <option value="<?=$i;?>" <?php if ($i == $address_subnet) echo "selected"; ?>><?=$i;?></option>
			          <?php endfor; ?>
			        </select>
			      </td>
            <td>
              <input name="detail<?php echo $tracker; ?>" type="text" class="formfld unknown" id="detail<?php echo $tracker; ?>" size="50" value="<?=$item4;?>" />
            </td>
            <td>
    		<input type="image" src="/themes/<?echo $g['theme'];?>/images/icons/icon_x.gif" onclick="removeRow(this); return false;" value="Delete" />
	      </td>
          </tr>
<?php
        	$counter++;

       		} // end foreach
	} // end if
?>
        </tbody>
        <tfoot>

        </tfoot>
		  </table>
			<a onclick="javascript:addRowTo('maintable'); typesel_change(); return false;" href="#">
        <img border="0" src="/themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" alt="" title="add another entry" />
      </a>
		</td>
  </tr>
  <tr>
    <td width="22%" valign="top">&nbsp;</td>
    <td width="78%">
      <input id="submit" name="submit" type="submit" class="formbtn" value="Save" />
      <a href="firewall_aliases.php"><input id="cancelbutton" name="cancelbutton" type="button" class="formbtn" value="Cancel" /></a>
      <?php if (isset($id) && $a_aliases[$id]): ?>
      <input name="id" type="hidden" value="<?=$id;?>" />
      <?php endif; ?>
    </td>
  </tr>
</table>
</form>

<script type="text/javascript">
	field_counter_js = 3;
	rows = 1;
	totalrows = <?php echo $counter; ?>;
	loaded = <?php echo $counter; ?>;
	typesel_change();
	update_box_type();
</script>

<?php include("fend.inc"); ?>
</body>
</html>

<?php
function process_alias_tgz($temp_filename) {
	mwexec("/bin/mv {$temp_filename}/aliases {$temp_filename}/aliases.tgz");
	mwexec("/usr/bin/tar xzf {$temp_filename}/aliases.tgz -C {$temp_filename}/aliases/");
	unlink("{$temp_filename}/aliases.tgz");
	$files_to_process = return_dir_as_array("{$temp_filename}/");
	/* foreach through all extracted files and build up aliases file */
	$fd = fopen("{$temp_filename}/aliases", "a");
	foreach($files_to_process as $f2p) {
		$file_contents = file_get_contents($f2p);
		fwrite($fd, $file_contents);
		unlink($f2p);
	}
	fclose($fd);
}
?>