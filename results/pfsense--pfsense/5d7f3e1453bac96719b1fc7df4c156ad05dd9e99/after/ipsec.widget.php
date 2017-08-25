<?php
/*
        $Id$
        Copyright 2007 Scott Dale
        Part of pfSense widgets (www.pfsense.com)
        originally based on m0n0wall (http://m0n0.ch/wall)

        Copyright (C) 2004-2005 T. Lechat <dev@lechat.org>, Manuel Kasper <mk@neon1.net>
        and Jonathan Watt <jwatt@jwatt.org>.
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

?>

<div>&nbsp;</div>
<?php
	$tab_array = array();
	$tab_array[0] = array("Overview", true, "ipsec-Overview");
	$tab_array[1] = array("Tunnel Status", false, "ipsec-tunnel");
	display_widget_tabs($tab_array);

	/* query SAD */
	$fd = @popen("/sbin/setkey -D", "r");
	$sad = array();
	if ($fd) {
		while (!feof($fd)) {
			$line = chop(fgets($fd));
			if (!$line)
				continue;
			if ($line == "No SAD entries.")
				break;
			if ($line[0] != "\t") {
				if (is_array($cursa))
					$sad[] = $cursa;
				$cursa = array();
				list($cursa['src'],$cursa['dst']) = explode(" ", $line);
				$i = 0;
			} else {
				$linea = explode(" ", trim($line));
				if ($i == 1) {
					$cursa['proto'] = $linea[0];
					$cursa['spi'] = substr($linea[2], strpos($linea[2], "x")+1, -1);
				} else if ($i == 2) {
					$cursa['ealgo'] = $linea[1];
				} else if ($i == 3) {
					$cursa['aalgo'] = $linea[1];
				}
			}
			$i++;
		}
		if (is_array($cursa) && count($cursa))
			$sad[] = $cursa;
		pclose($fd);
	}

	$activecounter = 0;
	$inactivecounter = 0;

	$ipsec_detail_array = array();

	foreach ($config['ipsec']['tunnel'] as $tunnel){
		$ipsecstatus = false;

		$tun_disabled = "false";
		$foundsrc = false;
		$founddst = false;

		foreach($sad as $sa) {
			if (!$foundsrc){
				$sourceIF = find_ip_interface($sa['src']);
				$sourceIF = convert_real_interface_to_friendly_interface_name($sourceIF);

				if($sourceIF == $tunnel['interface'])
					$foundsrc = true;
			}
			if($sa['dst'] == $tunnel['remote-gateway'])
				$founddst = true;
		}

		if($foundsrc && $founddst) {
			/* tunnel is up */
			$iconfn = "true";
			$activecounter++;
		} else {
			/* tunnel is down */
			$iconfn = "false";
			$inactivecounter++;
		}

		if (isset($tunnel['disabled']))
			$tun_disabled = "true";

		$ipsec_detail_array[] = array('src' => $tunnel['interface'], 'dest' => $tunnel['remote-gateway'], 'remote-subnet' => $tunnel['remote-subnet'], 'descr' => $tunnel['descr'], 'status' => $iconfn, 'disabled' => $tun_disabled);

	}

?>
<div id="ipsec-Overview" style="display:block;background-color:#EEEEEE;">
	<div>
	  <table class="tabcont" width="100%" border="0" cellpadding="6" cellspacing="0">
		  <tr>
		        <td nowrap class="listhdrr">Active Tunnels</td>
		        <td nowrap class="listhdrr">Inactive Tunnels</td>
			</tr>
			<tr>
				<td class="listlr"><?=$activecounter;?></td>
				<td class="listr"><?=$inactivecounter;?></td>
			</tr>
		  <tr>
		    <td colspan="4">
				<p>
				<span class="vexpl">
			          <span class="red">
			            <strong>
			              Note:<br />
			            </strong>
			          </span>
			          You can configure your IPSEC
			          <a href="vpn_ipsec.php">here</a>.
				</span>
		      </p>
			</td>
		  </tr>
		</table>
	</div>
</div>

<div id="ipsec-tunnel" style="display:none;background-color:#EEEEEE;">
	<div style="padding: 10px">
		<div style="display:table-row;">
	                <div class="widgetsubheader" style="display:table-cell;width:40px">Source</div>
	                <div class="widgetsubheader" style="display:table-cell;width:100px">Destination</div>
	                <div class="widgetsubheader" style="display:table-cell;width:90px">Description</div>
	                <div class="widgetsubheader" style="display:table-cell;width:30px">Status</div>
		</div>
		<div style="max-height:105px;overflow:auto;">
	<?php
	foreach ($ipsec_detail_array as $ipsec):

		if ($ipsec['disabled'] == "true"){
			$spans = "<span class=\"gray\">";
			$spane = "</span>";
		}
		else {
			$spans = $spane = "";
		}

		?>

		<div style="display:table-row;">
			<div class="listlr" style="display:table-cell;width:39px">
				<?=$spans;?>
					<?=htmlspecialchars($ipsec['src']);?>
				<?=$spane;?>
			</div>
			<div class="listr"  style="display:table-cell;width:100px"><?=$spans;?>
				<?=$ipsec['remote-subnet'];?>
				<br/>
				(<?=htmlspecialchars($ipsec['dest']);?>)<?=$spane;?>
			</div>
			<div class="listr"  style="display:table-cell;width:90px"><?=$spans;?><?=htmlspecialchars($ipsec['descr']);?><?=$spane;?></div>
			<div class="listr"  style="display:table-cell;width:37px"><?=$spans;?><center>
			<?php

			if($ipsec['status'] == "true") {
				/* tunnel is up */
				$iconfn = "interface_up";
			} else {
				/* tunnel is down */
				$iconfn = "interface_down";
			}

			echo "<img src ='/themes/{$g['theme']}/images/icons/icon_{$iconfn}.gif'>";

			?></center><?=$spane;?></div>
		</div>
	<?php endforeach; ?>
	</div>

	<div style="display:block">
	 <table class="tabcont" width="100%" border="0" cellpadding="0" cellspacing="0">
	  <tr>
	    <td colspan="4">
			  <p>
	        <span class="vexpl">
	          <span class="red">
	            <strong>
	              Note:<br />
	            </strong>
	          </span>
	          You can configure your IPSEC
	          <a href="vpn_ipsec.php">here</a>.
	        </span>
	      </p>
		</td>
	  </tr>
	</table>
	</div>
	</div>
</div>
