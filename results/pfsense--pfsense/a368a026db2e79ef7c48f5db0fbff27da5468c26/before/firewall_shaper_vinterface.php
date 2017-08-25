<?php
/* $Id$ */
/*
	firewall_shaper.php
	Copyright (C) 2004, 2005 Scott Ullrich
	Copyright (C) 2008 Ermal Lu�i
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
##|*IDENT=page-firewall-trafficshaper-limiter
##|*NAME=Firewall: Traffic Shaper: Limiter page
##|*DESCR=Allow access to the 'Firewall: Traffic Shaper: Limiter' page.
##|*MATCH=firewall_shaper_vinterface.php*
##|-PRIV


require("guiconfig.inc");

if($_GET['reset'] <> "") {
        mwexec("killall -9 pfctl php");
	exit;
}

$pgtitle = array("Firewall","Traffic Shaper", "Limiter");

read_dummynet_config();
/*
 * The whole logic in these code maybe can be specified.
 * If you find a better way contact me :).
 */

if ($_GET) {
	if ($_GET['queue'])
        	$qname = trim($_GET['queue']);
        if ($_GET['pipe'])
                $pipe = trim($_GET['pipe']);
        if ($_GET['action'])
                $action = $_GET['action'];
}
if ($_POST) {
	if ($_POST['name'])
        	$qname = trim($_POST['name']);
        if ($_POST['pipe'])
        	$pipe = trim($_POST['pipe']);
	else
		$pipe = trim($_POST['name']);
	if ($_POST['parentqueue'])
		$parentqueue = trim($_POST['parentqueue']);
}

if ($pipe) {
	$dnpipe = $dummynet_pipe_list[$pipe];
	if ($dnpipe) {
		$queue =& $dnpipe->find_queue($pipe, $qname);
	} else $addnewpipe = true;
}

$dontshow = false;
$newqueue = false;
$output_form = "";

if ($_GET) {
	switch ($action) {
	case "delete":
			if ($queue) {
				$queue->delete_queue();
				write_config();
				touch($d_shaperconfdirty_path);
			}
			header("Location: firewall_shaper_vinterface.php");
			exit;
		break;
	case "resetall":
			foreach ($dummynet_pipe_list as $dn)
				$dn->delete_queue();
			unset($dummynet_pipe_list);
			$dummynet_pipe_list = array();
			unset($config['dnshaper']['queue']);
			unset($queue);
			unset($pipe);
			$can_add = false;
			$can_enable = false;
			$dontshow = true;
			foreach ($config['filter']['rule'] as $key => $rule) {
				if (isset($rule['dnpipe']))
					unset($config['filter']['rule'][$key]['dnpipe']);
				if (isset($rule['pdnpipe']))
					unset($config['filter']['rule'][$key]['pdnpipe']);
			}
			write_config();

			$retval = 0;
                        $retval = filter_configure();
                        $savemsg = get_std_save_message($retval);

                        if (stristr($retval, "error") <> true)
	                        $savemsg = get_std_save_message($retval);
                        else
       	                	$savemsg = $retval;

			$output_form = $dn_default_shaper_message;

		break;
	case "add":
		if ($dnpipe) {
			$q = new dnqueue_class();
			$q->SetPipe($pipe);
			$output_form .= "<input type=\"hidden\" name=\"parentqueue\" id=\"parentqueue\"";
			$output_form .= " value=\"".$pipe."\">";
		} else if ($addnewpipe) {
			$q = new dnpipe_class();
			$q->SetQname($pipe);
		} else
			$input_errors[] = "Could not create new queue/discipline!";

			if ($q) {
				$output_form .= $q->build_form();
                unset($q);
				$newqueue = true;
			}
		break;
	case "show":
		if ($queue)
                       $output_form .= $queue->build_form();
		else
			$input_errors[] = "Queue not found!";
		break;
	case "enable":
		if ($queue) {
				$queue->SetEnabled("on");
				$output_form .= $queue->build_form();
				write_config();
				touch($d_shaperconfdirty_path);
		} else
				$input_errors[] = "Queue not found!";
		break;
	case "disable":
		if ($queue) {
				$queue->SetEnabled("");
				$output_form .= $queue->build_form();
				write_config();
				touch($d_shaperconfdirty_path);
		} else
				$input_errors[] = "Queue not found!";
		break;
	default:
		$output_form .= "<p class=\"pgtitle\">" . $dn_default_shaper_msg."</p>";
		$dontshow = true;
		break;
	}
} else if ($_POST) {
	unset($input_errors);

	if ($addnewpipe) {
		$dnpipe =& new dnpipe_class();

		$dnpipe->ReadConfig($_POST);
		$dnpipe->validate_input($_POST, &$input_errors);
		if (!$input_errors) {
			unset($tmppath);
			$tmppath[] = $dnpipe->GetQname();
			$dnpipe->SetLink(&$tmppath);
			$dnpipe->wconfig();
			write_config();
			touch($d_shaperconfdirty_path);
			$can_enable = true;
       		     	$can_add = true;
		}
		read_dummynet_config();
		$output_form .= $dnpipe->build_form();

	} else if ($parentqueue) { /* Add a new queue */
		if ($dnpipe) {
			$tmppath =& $dnpipe->GetLink();
			array_push($tmppath, $qname);
			$tmp =& $dnpipe->add_queue($pipe, $_POST, $tmppath, &$input_errors);
			if (!$input_errors) {
				array_pop($tmppath);
				$tmp->wconfig();
				write_config();
				$can_enable = true;
                		$can_add = false;
				touch($d_shaperconfdirty_path);
				$can_enable = true;
			}
			read_dummynet_config();
			$output_form .= $tmp->build_form();
		} else
			$input_errors[] = "Could not add new queue.";
	} else if ($_POST['apply']) {
			write_config();

			$retval = 0;
			$retval = filter_configure();
			$savemsg = get_std_save_message($retval);

			if (stristr($retval, "error") <> true)
					$savemsg = get_std_save_message($retval);
			else
					$savemsg = $retval;

 		/* XXX: TODO Make dummynet pretty graphs */
		//	enable_rrd_graphing();

            unlink($d_shaperconfdirty_path);

			if ($queue) {
				$output_form .= $queue->build_form();
				$dontshow = false;
			}
			else {
				$output_form .= $dn_default_shaper_message;
				$dontshow = true;
			}

	} else if ($queue) {
                $queue->validate_input($_POST, &$input_errors);
                if (!$input_errors) {
                            	$queue->update_dn_data($_POST);
                            	$queue->wconfig();
				write_config();
				touch($d_shaperconfdirty_path);
				$dontshow = false;
                }
		read_dummynet_config();
		$output_form .= $queue->build_form();
	} else  {
		$output_form .= "<p class=\"pgtitle\">" . $dn_default_shaper_msg."</p>";
		$dontshow = true;
	}
} else {
	$output_form .= "<p class=\"pgtitle\">" . $dn_default_shaper_msg."</p>";
	$dontshow = true;
}

if ($queue) {
                        if ($queue->GetEnabled())
                                $can_enable = true;
                        else
                                $can_enable = false;
                        if ($queue->CanHaveChilds()) {
                       		$can_add = true;
                        } else
                                $can_add = false;
}

$tree = "<ul class=\"tree\" >";
if (is_array($dummynet_pipe_list)) {
        foreach ($dummynet_pipe_list as $tmpdn) {
                $tree .= $tmpdn->build_tree();
        }
}
$tree .= "</ul>";

if (!$dontshow || $newqueue) {

$output_form .= "<tr><td width=\"22%\" valign=\"top\" class=\"vncellreq\">";
$output_form .= "Queue Actions";
$output_form .= "</td><td valign=\"top\" class=\"vncellreq\" width=\"78%\">";

$output_form .= "<input type=\"submit\" name=\"Submit\" value=\"" . gettext("Save") . "\" class=\"formbtn\" />";
if ($can_add || $addnewaltq) {
	$output_form .= "<a href=\"firewall_shaper_vinterface.php?pipe=";
	$output_form .= $pipe;
	if ($queue) {
		$output_form .= "&queue=" . $queue->GetQname();
	}
	$output_form .= "&action=add\">";
	$output_form .= "<input type=\"button\" class=\"formbtn\" name=\"add\" value=\"Add new queue\">";
	$output_form .= "</a>";
}
$output_form .= "<a href=\"firewall_shaper_vinterface.php?pipe=";
$output_form .= $pipe . "&queue=";
if ($queue) {
	$output_form .= "&queue=" . $queue->GetQname();
}
$output_form .= "&action=delete\">";
$output_form .= "<input type=\"button\" class=\"formbtn\" name=\"delete\"";
if ($queue)
	$output_form .= " value=\"Delete this queue\">";
else
	$output_form .= " value=\"Delete virtual interface\">";
$output_form .= "</a>";
$output_form .= "</td></tr>";
$output_form .= "</div>";
}
else
	$output_form .= "</div>";

$output = "<div id=\"shaperarea\" style=\"position:relative\">";
if (!$dontshow) {
if ($queue || $dnpipe || $newqueue) {
	$output .= "<tr><td valign=\"top\" class=\"vncellreq\"><br>";
	$output .= "Enable/Disable";
	$output .= "</td><td class=\"vncellreq\">";
	$output .= " <input type=\"checkbox\" id=\"enabled\" name=\"enabled\"";
	if ($queue)
		if ($queue->GetEnabled() <> "")
       			$output .=  " CHECKED";
	$output .= " ><span class=\"vexpl\"> Enable/Disable queue and its childs</span>";
	$output .= "</td></tr>";
}
}
$output .= $output_form;

include("head.inc");
?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC" >
<link rel="stylesheet" type="text/css" media="all" href="./tree/tree.css" />
<script type="text/javascript" src="./tree/tree.js"></script>
<script type="text/javascript">
function show_source_port_range() {
        document.getElementById("sprtable").style.display = '';
	document.getElementById("sprtable1").style.display = '';
	document.getElementById("sprtable2").style.display = '';
	document.getElementById("sprtable5").style.display = '';
	document.getElementById("sprtable4").style.display = 'none';
	document.getElementById("showadvancedboxspr").innerHTML='';
}
</script>

<?php
include("fbegin.inc");
?>
<div id="inputerrors"></div>
<?php if ($input_errors) print_input_errors($input_errors); ?>

<form action="firewall_shaper_vinterface.php" method="post" id="iform" name="iform">

<?php if ($savemsg) print_info_box($savemsg); ?>
<?php if (file_exists($d_shaperconfdirty_path)): ?><p>
<?php print_info_box_np("The traffic shaper configuration has been changed.<br>You must apply the changes in order for them to take effect.");?><br>
<?php endif; ?>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr><td>
<?php
	$tab_array = array();
	$tab_array[0] = array("By Interface", false, "firewall_shaper.php");
	$tab_array[1] = array("By Queue", false, "firewall_shaper_queues.php");
	$tab_array[2] = array("Limiter", true, "firewall_shaper_vinterface.php");
	$tab_array[3] = array("Layer7", false, "firewall_shaper_layer7.php");
	$tab_array[4] = array("Wizards", false, "firewall_shaper_wizards.php");
	display_top_tabs($tab_array);
?>
  </td></tr>
  <tr>
    <td>
	<div id="mainarea">
              <table class="tabcont" width="100%" border="0" cellpadding="0" cellspacing="0">
<?php if (count($dummynet_pipe_list) > 0): ?>
                        <tr class="tabcont"><td width="25%" align="left">
                        </td><td width="75%"> </td></tr>
<? endif; ?>
			<tr>
			<td width="25%" valign="top" algin="left">
			<?php
				echo $tree;
			?>
			<br/><br/>
			<a href="firewall_shaper_vinterface.php?pipe=new&action=add">
			<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" title="Create new limiter" width="17" height="17" border="0">  Create new limiter
			</a><br/>
			</td>
			<td width="75%" valign="top" align="center">
			<table>
			<?
				echo $output;
			?>
			</table>

		      </td></tr>
                    </table>
		</div>
	  </td>
	</tr>
</table>
            </form>
<?php include("fend.inc");
?>
</body>
</html>