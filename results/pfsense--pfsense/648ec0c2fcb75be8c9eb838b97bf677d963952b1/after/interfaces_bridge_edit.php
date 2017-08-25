<?php
/* $Id$ */
/*
	interfaces_bridge_edit.php
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

require("guiconfig.inc");

if (!is_array($config['bridges']['bridge']))
	$config['bridges']['bridge'] = array();

$a_bridges = &$config['bridges']['bridge'];

$ifacelist = get_configured_interface_with_descr();

$id = $_GET['id'];
if (isset($_POST['id']))
	$id = $_POST['id'];

if (isset($id) && $a_bridges[$id]) {
	$pconfig['enablestp'] = isset($a_bridges[$id]['enablestp']);
	$pconfig['descr'] = $a_bridges[$id]['descr'];
	$pconfig['bridgeif'] = $a_bridges[$id]['bridgeif'];
	$pconfig['members'] = $a_bridges[$id]['members'];
	$pconfig['maxaddr'] = $a_bridges[$id]['maxaddr'];
	$pconfig['timeout'] = $a_bridges[$id]['timeout'];
	if ($a_bridges[$id]['static'])
		$pconfig['static'] = $a_bridges[$id]['static'];
	if ($a_bridges[$id]['private'])
		$pconfig['private'] = $a_bridges[$id]['private'];
	if (isset($a_bridges[$id]['stp']))
		$pconfig['stp'] = $a_bridges[$id]['stp'];
	$pconfig['maxage'] = $a_bridges[$id]['maxage'];
	$pconfig['fwdelay'] = $a_bridges[$id]['fwdelay'];
	$pconfig['hellotime'] = $a_bridges[$id]['hellotime'];
	$pconfig['priority'] = $a_bridges[$id]['priority'];
	$pconfig['proto'] = $a_bridges[$id]['proto'];
	$pconfig['holdcount'] = $a_bridges[$id]['holdcount'];
	$pconfig['ifpriority'] = explode(",", $a_bridges[$id]['ifpriority']);
	$ifpriority = array();
	foreach ($pconfig['ifpriority'] as $cfg) {
		$embcfg = explode(":", $cfg);
		foreach ($embcfg as $key => $value)
			$ifpriority[$key] = $value;
	}
	$pconfig['ifpriority'] = $ifpriority;
	$pconfig['ifpathcost'] = explode(",", $a_bridges[$id]['ifpathcost']);
	$ifpathcost = array();
	foreach ($pconfig['ifpathcost'] as $cfg) {
		$embcfg = explode(":", $cfg);
		foreach ($embcfg as $key => $value)
			$ifpathcost[$key] = $value;
	}
	$pconfig['ifpathcost'] = $ifpathcost;
	$pconfig['span'] = $a_bridges[$id]['span'];
	if (isset($a_bridges[$id]['edge']))
		$pconfig['edge'] = $a_bridges[$id]['edge'];
	if (isset($a_bridges[$id]['autoedge']))
		$pconfig['autoedge'] = $a_bridges[$id]['autoedge'];
	if (isset($a_bridges[$id]['ptp']))
		$pconfig['ptp'] = $a_bridges[$id]['ptp'];
	if (isset($a_bridges[$id]['autoptp']))
		$pconfig['autoptp'] = $a_bridges[$id]['autoptp'];
}

if ($_POST) {

	unset($input_errors);
	$pconfig = $_POST;

	/* input validation */
	$reqdfields = explode(" ", "members");
	$reqdfieldsn = explode(",", "Member Interfaces");

	do_input_validation($_POST, $reqdfields, $reqdfieldsn, &$input_errors);

	if ($_POST['maxage'] && !is_numeric($_POST['maxage']))
		$input_errors[] = "Maxage needs to be an interger between 6 and 40.";
	if ($_POST['maxaddr'] && !is_numeric($_POST['maxaddr']))
		$input_errors[] = "Maxaddr needs to be an interger.";
	if ($_POST['timeout'] && !is_numeric($_POST['timeout']))
		$input_errors[] = "Timeout needs to be an interger.";
	if ($_POST['fwdelay'] && !is_numeric($_POST['fwdelay']))
		$input_errors[] = "Forward Delay needs to be an interger between 4 and 30.";
	if ($_POST['hellotime'] && !is_numeric($_POST['hellotime']))
		$input_errors[] = "Hello time for STP needs to be an interger between 1 and 2.";
	if ($_POST['priority'] && !is_numeric($_POST['priority']))
		$input_errors[] = "Priority for STP needs to be an interger between 0 and 61440.";
	if ($_POST['holdcnt'] && !is_numeric($_POST['holdcnt']))
		$input_errors[] = "Transmit Hold Count for STP needs to be an interger between 1 and 10.";
	foreach ($ifacelist as $ifn => $ifdescr) {
		if ($_POST[$ifn] <> "" && !is_numeric($_POST[$ifn]))
			$input_errors[] = "{$ifdescr} interface priority for STP needs to be an interger between 0 and 240.";
	}
	$i = 0;
	foreach ($ifacelist as $ifn => $ifdescr) {
		if ($_POST["{$ifn}{$i}"] <> "" && !is_numeric($_POST["{$ifn}{$i}"]))
			$input_errors[] = "{$ifdescr} interface path cost for STP needs to be an interger between 1 and 200000000.";
		$i++;
	}
	if (!$input_errors) {
		$bridge = array();
		$bridge['members'] = implode(',', $_POST['members']);
		$bridge['enablestp'] = isset($_POST['enablestp']);
		$bridge['descr'] = $_POST['descr'];
		$bridge['maxaddr'] = $_POST['maxaddr'];
		$bridge['timeout'] = $_POST['timeout'];
		if ($_POST['static'])
			$bridge['static'] = implode(',', $_POST['static']);
		if ($_POST['private'])
			$bridge['private'] = implode(',', $_POST['private']);
		if (isset($_POST['stp']))
			$bridge['stp'] = implode(',', $_POST['stp']);
		$bridge['maxage'] = $_POST['maxage'];
		$bridge['fwdelay'] = $_POST['fwdelay'];
		$bridge['hellotime'] = $_POST['hellotime'];
		$bridge['priority'] = $_POST['priority'];
		$bridge['proto'] = $_POST['proto'];
		$bridge['holdcount'] = $_POST['holdcount'];
		$i = 0;
		$ifpriority = "";
		$ifpathcost = "";
		foreach ($ifacelist as $ifn => $ifdescr) {
			if ($_POST[$ifn] <> "") {
				if ($i > 0)
					$ifpriority .= ",";
				$ifpriority .= $ifn.":".$_POST[$ifn];
			}
			if ($_POST["{$ifn}{$i}"] <> "") {
				if ($i > 0)
					$ifpathcost .= ",";
				$ifpathcost .= $ifn.":".$_POST[$ifn];
			}
			$i++;
		}
		$bridge['ifpriority'] = $ifpriority;
		$bridge['ifpathcost'] = $ifpathcost;
		$bridge['span'] = $_POST['span'];
		if (isset($_POST['edge']))
			$bridge['edge'] = implode(',', $_POST['edge']);
		if (isset($_POST['autoedge']))
			$bridge['autoedge'] = implode(',', $_POST['autoedge']);
		if (isset($_POST['ptp']))
			$bridge['ptp'] = implode(',', $_POST['ptp']);
		if (isset($_POST['autoptp']))
			$bridge['autoptp'] = implode(',', $_POST['autoptp']);

		$bridge['bridgeif'] = $_POST['bridgeif'];
                $bridge['bridgeif'] = interface_bridge_configure($bridge);
                if ($bridge['bridgeif'] == "" || !stristr($bridge['bridgeif'], "bridge"))
                        $input_errors[] = "Error occured creating interface, please retry.";
                else {
                        if (isset($id) && $a_bridges[$id])
                                $a_bridges[$id] = $bridge;
                        else
                                $a_bridges[] = $bridge;

                        write_config();

			header("Location: interfaces_bridge.php");
			exit;
		}
	}
}

$pgtitle = array("Firewall","Bridge","Edit");
include("head.inc");

?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<script type="text/javascript">
function show_source_port_range() {
        document.getElementById("sprtable").style.display = 'none';
        document.getElementById("sprtable1").style.display = '';
        document.getElementById("sprtable2").style.display = '';
        document.getElementById("sprtable3").style.display = '';
        document.getElementById("sprtable4").style.display = '';
        document.getElementById("sprtable5").style.display = '';
        document.getElementById("sprtable6").style.display = '';
        document.getElementById("sprtable7").style.display = '';
        document.getElementById("sprtable8").style.display = '';
        document.getElementById("sprtable9").style.display = '';
        document.getElementById("sprtable10").style.display = '';
}
</script>

<?php include("fbegin.inc"); ?>
<?php if ($input_errors) print_input_errors($input_errors); ?>
            <form action="interfaces_bridge_edit.php" method="post" name="iform" id="iform">
              <table width="100%" border="0" cellpadding="6" cellspacing="0">
				<tr>
                  <td width="22%" valign="top" class="vncellreq">Member interfaces</td>
                  <td width="78%" class="vtable">
				  <select name="members[]" multiple="true" class="formselect" size="3">
                      <?php
					  	foreach ($ifacelist as $ifn => $ifinfo) {
							echo "<option value=\"{$ifn}\"";
							if (stristr($pconfig['members'], $ifn))
								echo "selected";
							echo ">{$ifinfo}</option>";
						}
		      		?>
                    </select>
			<br/>
			<span class="vexpl">Interfaces participating in the bridge.</span>
			</td>
            </tr>
			<tr>
                  <td width="22%" valign="top" class="vncellreq">Description</td>
                  <td width="78%" class="vtable">
				  <input type="text" name="descr" id="descr" class="formfld unknown" size="50" value="<?=$pconfig['descr'];?>">
				 	</td>
				</tr>
            <tr id="sprtable" name="sprtable">
                <td></td>
                <td>
                <p><input type="button" onClick="show_source_port_range()" value="Show advanced options"></p>
                </td>
			</tr>
                <tr style="display:none" id="sprtable1" name="sprtable1">
                  <td valign="top" class="vncell" align="middle">RSTP/STP  </td>
                  <td class="vtable">
					<input type="checkbox" name="enablestp" id="enablestp" <?php if ($pconfig['enablestp']) echo "selected";?>>
					<span class="vexpl"><strong>Enable spanning tree options for this bridge. </strong></span>
					<br/><br/>
				  	<table id="stpoptions" name="stpoptions" border="0" cellpadding="6" cellspacing="0">
					<tr><td valign="top" class="vncell" width="20%">Proto</td>
					<td class="vtable" width="80%">
				  	<select name="proto" id="proto">
						<?php
							foreach (array("rstp", "stp") as $proto) {
								echo "<option value=\"{$proto}\"";
								if ($pconfig['proto'] == $proto)
									echo "selected";
								echo ">".strtoupper($proto)."</option>";
							}
						?>
					</select>
                    <br/>
                    <span class="vexpl">Protocol used fro spanning tree. </span></td>
					</td></tr>
					<tr> <td valign="top" class="vncell" width="20%">STP interfaces</td>
					<td class="vtable" width="80%">
				  	<select name="stp[]" class="formselect" multiple="true" size="3">
						<?php
							foreach ($ifacelist as $ifn => $ifdescr) {
								echo "<option value=\"{$ifn}\"";
								if (stristr($pconfig['stp'], $ifn))
									echo "selected";
								echo ">{$ifdescr}</option>";
							}
						?>
					</select>
					<br/>
					<span class="vexpl" >
	     Enable Spanning Tree protocol on interface.  The if_bridge(4)
	     driver has support for the IEEE 802.1D Spanning Tree protocol
	     (STP).  Spanning Tree is used to detect and remove loops in a
	     network topology.
					</span>
					</td></tr>
					<tr><td valign="top" class="vncell" width="20%">Valid time</td>
					<td class="vtable" width="80%">
					<input name="maxage" type="text" class="formfld unkown" id="maxage" size="8" value="<?=$pconfig['maxage'];?>"> seconds
					<br/>
					<span class="vexpl">
	     Set the time that a Spanning Tree protocol configuration is
	     valid.  The default is 20 seconds.  The minimum is 6 seconds and
	     the maximum is 40 seconds.
					</span>
					</td></tr>
					<tr><td valign="top" class="vncell" width="20%">Forward time </td>
					<td class="vtable" width="80%">
					<input name="fwdelay" type="text" class="formfld unkown" id="fwdelay" size="8" value="<?=$pconfig['fwdelay'];?>"> seconds
					<br/>
					<span class="vexpl">
	     Set the time that must pass before an interface begins forwarding
	     packets when Spanning Tree is enabled.  The default is 15 sec-
	     onds.  The minimum is 4 seconds and the maximum is 30 seconds.
					</span>
					</td></tr>
					<tr><td valign="top" class="vncell" width="20%">Hello time</td>
					<td class="vtable" width="80%">
					<input name="hellotime" type="text" class="formfld unkown" size="8" id="hellotime" value="<?=$pconfig['hellotime'];?>"> seconds
					<br/>
					<span class="vexpl">
	     Set the time between broadcasting of Spanning Tree protocol con-
	     figuration messages.  The hello time may only be changed when
	     operating in legacy stp mode.  The default is 2 seconds.  The
	     minimum is 1 second and the maximum is 2 seconds.
					</span>
					</td></tr>
					<tr><td valign="top" class="vncell" width="20%">Priority</td>
					<td class="vtable" width="80%">
					<input name="priority" type="text" class="formfld unkown" id="priority" value="<?=$pconfig['priority'];?>">
					<br/>
					<span class="vexpl">
	     Set the bridge priority for Spanning Tree.  The default is 32768.
	     The minimum is 0 and the maximum is 61440.
					</span>
					</td></tr>
					<tr><td valign="top" class="vncell" width="20%">Hold count</td>
					<td class="vtable" width="80%">
					<input name="holdcnt" type="text" class="formfld unkown" id="holdcnt" value="<?=$pconfig['holdcnt'];?>">
					<br/>
					<span class="vexpl">
	     Set the transmit hold count for Spanning Tree.  This is the num-
	     ber of packets transmitted before being rate limited.  The
	     default is 6.  The minimum is 1 and the maximum is 10.
					</span>
					</td></tr>
					<tr><td valign="top" class="vncell" width="20%">Priority</td>
					<td class="vtable" width="80%">
					<table>
					<?php foreach ($ifacelist as $ifn => $ifdescr)
							echo "<tr><td>{$ifdescr}</td><td><input size=\"5\" name=\"{$ifn}\" type=\"text\" class=\"formfld unkown\" id=\"{$ifn}\" value=\"{$ifpriority[$ifn]}\"></td></tr>";
					?>
					</table>
					<br/>
					<span class="vexpl" >
	     Set the Spanning Tree priority of interface to value.  The
	     default is 128.  The minimum is 0 and the maximum is 240.
					</span>
					</td></tr>
					<tr><td valign="top" class="vncell" width="20%">Path cost</td>
					<td class="vtable" width="80%">
					<table>
					<?php $i = 0; foreach ($ifacelist as $ifn => $ifdescr)
							echo "<tr><td>{$ifdescr}</td><td><input size=\"8\" name=\"{$ifn}{$i}\" type=\"text\" class=\"formfld unkown\" id=\"{$ifn}{$i}\" value=\"{$ifpathcost[$ifn]}\"></td></tr>";
					?>
					</table>
					<br/>
					<span class="vexpl" >
	     Set the Spanning Tree path cost of interface to value.  The
	     default is calculated from the link speed.  To change a previ-
	     ously selected path cost back to automatic, set the cost to 0.
	     The minimum is 1 and the maximum is 200000000.
					</span>
					</td></tr>

			    </table>
				</tr>
                <tr style="display:none" id="sprtable2" name="sprtable2">
                  <td valign="top" class="vncell">Cache size</td>
					<td class="vtable">
						<input name="maxaddr" size="10" type="text" class="formfld unkown" id="maxaddr" value="<?=$pconfig['maxaddr'];?>"> entries
					<br/><span class="vexpl">
Set the size of the bridge address cache to size.	The default is
	     100 entries.
					</span>
					</td>
				</tr>
                <tr style="display:none" id="sprtable3" name="sprtable3">
                  <td valign="top" class="vncell">Cache entry expire time</td>
				  <td>
					<input name="timeout" type="text" class="formfld unkown" id="timeout" size="10" value="<?=$pconfig['timeout'];?>"> seconds
					<br/><span class="vexpl">
	     Set the timeout of address cache entries to seconds seconds.  If
	     seconds is zero, then address cache entries will not be expired.
	     The default is 240 seconds.
					</span>
					</td>
				</tr>
                <tr style="display:none" id="sprtable4" name="sprtable4">
                  <td valign="top" class="vncell">Span port</td>
					<td class="vtable">
				  	<select name="span" class="formselect" id="span">
						<?php
							foreach ($ifacelist as $ifn => $ifdescr) {
								echo "<option value=\"{$ifn}\"";
								if ($ifn == $pconfig['span'])
									echo "selected";
								echo ">{$ifdescr}</option>";
							}
						?>
					</select>
					<br/><span class="vexpl">
	     Add the interface named by interface as a span port on the
	     bridge.  Span ports transmit a copy of every frame received by
	     the bridge.  This is most useful for snooping a bridged network
	     passively on another host connected to one of the span ports of
	     the bridge.
					</span>
					</td>
				</tr>
                <tr style="display:none" id="sprtable5" name="sprtable5">
                  <td valign="top" class="vncell">Edge ports</td>
                  <td class="vtable">
				  	<select name="edge[]" class="formselect" multiple="true" size="3">
						<?php
							foreach ($ifacelist as $ifn => $ifdescr) {
								echo "<option value=\"{$ifn}\"";
								if (stristr($pconfig['edge'], $ifn))
									echo "selected";
								echo ">{$ifdescr}</option>";
							}
						?>
					</select>
                    <br>
                    <span class="vexpl">
	     Set interface as an edge port.  An edge port connects directly to
	     end stations cannot create bridging loops in the network, this
	     allows it to transition straight to forwarding.
					</span></td>
			    </tr>
                <tr style="display:none" id="sprtable6" name="sprtable6">
                  <td valign="top" class="vncell">Auto Edge ports</td>
                  <td class="vtable">
				  	<select name="autoedge[]" class="formselect" multiple="true" size="3">
						<?php
							foreach ($ifacelist as $ifn => $ifdescr) {
								echo "<option value=\"{$ifn}\"";
								if (stristr($pconfig['autoedge'], $ifn))
									echo "selected";
								echo ">{$ifdescr}</option>";
							}
						?>
					</select>
                    <br>
                    <span class="vexpl">
	     Allow interface to automatically detect edge status.  This is the
	     default for all interfaces added to a bridge.
		 <p class="vexpl"><span class="red"><strong>
				  Note:<br>
				  </strong></span>
		 This will disable the autoedge status of interfaces.
					</span></td>
			    </tr>
                <tr style="display:none" id="sprtable7" name="sprtable7">
                  <td valign="top" class="vncell">PTP ports</td>
                  <td class="vtable">
				  	<select name="ptp[]" class="formselect" multiple="true" size="3">
						<?php
							foreach ($ifacelist as $ifn => $ifdescr) {
								echo "<option value=\"{$ifn}\"";
								if (stristr($pconfig['ptp'], $ifn))
									echo "selected";
								echo ">{$ifdescr}</option>";
							}
						?>
					</select>
                    <br>
                    <span class="vexpl">
	     Set the interface as a point to point link.  This is required for
	     straight transitions to forwarding and should be enabled on a
	     direct link to another RSTP capable switch.
					</span></td>
			    </tr>
                <tr style="display:none" id="sprtable8" name="sprtable8">
                  <td valign="top" class="vncell">Auto PTP ports</td>
                  <td class="vtable">
				  	<select name="autoptp[]" class="formselect" multiple="true" size="3">
						<?php
							foreach ($ifacelist as $ifn => $ifdescr) {
								echo "<option value=\"{$ifn}\"";
								if (stristr($pconfig['autoptp'], $ifn))
									echo "selected";
								echo ">{$ifdescr}</option>";
							}
						?>
					</select>
                    <br>
                    <span class="vexpl">
	     Automatically detect the point to point status on interface by
	     checking the full duplex link status.  This is the default for
	     interfaces added to the bridge.
		 		 <p class="vexpl"><span class="red"><strong>
				  Note:<br>
				  </strong></span>
		 The interfaces selected here will be removed from default autoedge status.
					</span></td>
			    </tr>
                <tr style="display:none" id="sprtable9" name="sprtable9">
                  <td valign="top" class="vncell">Sticky ports</td>
                  <td class="vtable">
				  	<select name="static[]" class="formselect" multiple="true" size="3">
						<?php
							foreach ($ifacelist as $ifn => $ifdescr) {
								echo "<option value=\"{$ifn}\"";
								if (stristr($pconfig['static'], $ifn))
									echo "selected";
								echo ">{$ifdescr}</option>";
							}
						?>
					</select>
                    <br>
                    <span class="vexpl">
	     Mark an interface as a ``sticky'' interface.  Dynamically learned
	     address entries are treated at static once entered into the
	     cache.  Sticky entries are never aged out of the cache or
	     replaced, even if the address is seen on a different interface.
					</span></td>
			    </tr>
                <tr style="display:none" id="sprtable10" name="sprtable10">
                  <td valign="top" class="vncell">Private ports</td>
                  <td class="vtable">
				  	<select name="private[]" class="formselect" multiple="true" size="3">
						<?php
							foreach ($ifacelist as $ifn => $ifdescr) {
								echo "<option value=\"{$ifn}\"";
								if (stristr($pconfig['private'], $ifn))
									echo "selected";
								echo ">{$ifdescr}</option>";
							}
						?>
					</select>
                    <br>
                    <span class="vexpl">
	     Mark an interface as a ``private'' interface.  A private inter-
	     face does not forward any traffic to any other port that is also
	     a private interface.
					</span></td>
			    </tr>
                <tr>
                  <td width="22%" valign="top">&nbsp;</td>
                  <td width="78%">
		    <input type="hidden" name="bridgeif" value="<?=$pconfig['bridgeif']; ?>">
                    <input name="Submit" type="submit" class="formbtn" value="Save"> <input type="button" value="Cancel" onclick="history.back()">
                    <?php if (isset($id) && $a_bridges[$id]): ?>
                    <input name="id" type="hidden" value="<?=$id;?>">
                    <?php endif; ?>
                  </td>
                </tr>
              </table>
</form>
<?php include("fend.inc"); ?>
</body>
</html>