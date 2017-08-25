<?php
/* $Id$ */
/*
    index.php
    Copyright (C) 2004, 2005 Scott Ullrich
    All rights reserved.

    Originally part of m0n0wall (http://m0n0.ch/wall)
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
    oR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/

	## Load Essential Includes
	require_once('guiconfig.inc');
	require_once('notices.inc');

	## Load Functions Files
	require_once('includes/functions.inc.php');


	## Load AJAX, Initiate Class ###############################################
	require_once('includes/sajax.class.php');

	## Initiate Class and Set location of ajax file containing
	## the information that we need for this page. Also set functions
	## that SAJAX will be using.
	$oSajax = new sajax();
	$oSajax->sajax_remote_uri = 'sajax/index.sajax.php';
	$oSajax->sajax_request_type = 'POST';
	$oSajax->sajax_export("get_stats");
	$oSajax->sajax_handle_client_request();
	############################################################################


	## Check to see if we have a swap space,
	## if true, display, if false, hide it ...
	if(file_exists("/usr/sbin/swapinfo")) {
		$swapinfo = `/usr/sbin/swapinfo`;
		if(stristr($swapinfo,'%') == true) $showswap=true;
	}


	## User recently restored his config.
	## If packages are installed lets resync
	if(file_exists('/conf/needs_package_sync')) {
		if($config['installedpackages'] <> '') {
			conf_mount_rw();
			unlink('/conf/needs_package_sync');
			header('Location: pkg_mgr_install.php?mode=reinstallall');
			exit;
		}
	}


	## If it is the first time webConfigurator has been
	## accessed since initial install show this stuff.
	if(file_exists('/conf/trigger_initial_wizard')) {

		$pgtitle = array("{$g['product_name']} first time setup");
		include('head.inc');

		echo "<body link=\"#0000CC\" vlink=\"#0000CC\" alink=\"#0000CC\">\n";
		echo "<form>\n";
		echo "<center>\n";
		echo "<img src=\"/themes/{$g['theme']}/images/logo.gif\" border=\"0\"><p>\n";
		echo "<div \" style=\"width:700px;background-color:#ffffff\" id=\"nifty\">\n";
		echo "Welcome to {$g['product_name']}!<p>\n";
		echo "One moment while we start the initial setup wizard.<p>\n";
		echo "Embedded platform users: Please be patient, the wizard takes a little longer to run than the normal gui.<p>\n";
		echo "To bypass the wizard, click on the {$g['product_name']} wizard on the initial page.\n";
		echo "</div>\n";
		echo "<meta http-equiv=\"refresh\" content=\"1;url=wizard.php?xml=setup_wizard.xml\">\n";
		echo "<script type=\"text/javascript\">\n";
		echo "NiftyCheck();\n";
		echo "Rounded(\"div#nifty\",\"all\",\"#000\",\"#FFFFFF\",\"smooth\");\n";
		echo "</script>\n";
		exit;
	}


	## Find out whether there's hardware encryption or not
	unset($hwcrypto);
	$fd = @fopen("{$g['varlog_path']}/dmesg.boot", "r");
	if ($fd) {
		while (!feof($fd)) {
			$dmesgl = fgets($fd);
			if (preg_match("/^hifn.: (.*?),/", $dmesgl, $matches)) {
				$hwcrypto = $matches[1];
				break;
			}
		}
		fclose($fd);
	}

//build list of widgets
$directory = "widgets/widgets/";
$dirhandle  = opendir($directory);
$filename = "";
$widgetnames = array ();
$widgetfiles = array();

while (false !== ($filename = readdir($dirhandle))) {
	$periodpos = strpos($filename, ".");
	$widgetname = substr($filename, 0, $periodpos);
	$widgetnames[] = $widgetname;
	if ($widgetname != "system_information")
		$widgetfiles[] = $filename;
}
sort($widgetfiles);
array_unshift($widgetfiles, "system_information.widget.php");


if (!is_array($config['widgets'])) {
	$config['widgets'] = array();
}

$pconfig['sequence'] = $config['widgets']['sequence'];

if ($_POST){
	$config['widgets']['sequence'] = $_POST['sequence'];

	foreach ($widgetnames as $widget){
		if ($_POST[$widget . '-config']){
			$config['widgets'][$widget . '-config'] = $_POST[$widget . '-config'];
		}
	}

	write_config();
	header("Location: index.php");
	exit;
}


if ($config['widgets'])
{
	$widgetlist = $pconfig['sequence'];

	$colpos = array();
	$savedwidgetfiles = array();
	$widgetname = "";
	$widgetlist = explode(",",$widgetlist);
	foreach ($widgetlist as $widget){
		$dashpos = strpos($widget, "-");
		$widgetname = substr($widget, 0, $dashpos);
		$colposition = strpos($widget, ":");
		$displayposition = strrpos($widget, ":");
		$colpos[] = substr($widget,$colposition+1, $displayposition - $colposition-1);
		$displayarray[] = substr($widget,$displayposition+1);
		$savedwidgetfiles[] = $widgetname . ".widget.php";
	}
	foreach ($widgetnames as $widget){
		if ($config['widgets'][$widget . '-config']){
			$pconfig[$widget . '-config'] = $config['widgets'][$widget . '-config'];
		}
	}
}


//build list of php include files
$phpincludefiles = Array();
$directory = "widgets/include/";
$dirhandle  = opendir($directory);
$filename = "";
while (false !== ($filename = readdir($dirhandle))) {
	$phpincludefiles[] = $filename;
}
foreach($phpincludefiles as $includename) {
	if(!stristr($includename, ".inc"))
		continue;
	include($directory . $includename);
}

$jscriptstr = <<<EOD
<script language="javascript" type="text/javascript">


function widgetAjax(widget) {
			uri = "widgets/widgets/" + widget + ".widget.php";
			var opt = {
			    // Use GET
			    method: 'get',
				evalScripts: 'true',
			    asynchronous: true,
			    // Handle 404
			    on404: function(t) {
			        alert('Error 404: location "' + t.statusText + '" was not found.');
			    },
			    // Handle other errors
			    onFailure: function(t) {
			        alert('Error ' + t.status + ' -- ' + t.statusText);
			    }
			}
			new Ajax.Updater(widget, uri, opt);
		}


function addDiv(selectedDiv){
	selectedDiv2 = selectedDiv + "-container";
	d = document;
	textlink = d.getElementById(selectedDiv2);
	if (textlink.style.display != "none")
	{
		Effect.Shake(selectedDiv2);
	}
	else
	{
		widgetAjax(selectedDiv);
		Effect.Appear(selectedDiv2, {duration:1});
		selectIntLink = selectedDiv2 + "-input";
		textlink = d.getElementById(selectIntLink);
		textlink.value = "show";
		showSave();
	}
}

function configureDiv(selectedDiv){
	selectIntLink = selectedDiv + "-settings";
	d = document;
	textlink = d.getElementById(selectIntLink);
	if (textlink.style.display == "none")
		Effect.BlindDown(selectIntLink, {duration:1});
	else
		Effect.BlindUp(selectIntLink, {duration:1});
}

function showDiv(selectedDiv,swapButtons){
	//appear element
    Effect.BlindDown(selectedDiv, {duration:1});
    showSave();
	d = document;
    if (swapButtons){
	    selectIntLink = selectedDiv + "-min";
		textlink = d.getElementById(selectIntLink);
		textlink.style.display = "inline";


	    selectIntLink = selectedDiv + "-open";
		textlink = d.getElementById(selectIntLink);
		textlink.style.display = "none";

    }
	selectIntLink = selectedDiv + "-container-input";
	textlink = d.getElementById(selectIntLink);
	textlink.value = "show";

}

function minimizeDiv(selectedDiv,swapButtons){
	//fade element
    Effect.BlindUp(selectedDiv, {duration:1});
    showSave();
	d = document;
    if (swapButtons){
	    selectIntLink = selectedDiv + "-open";
		textlink = d.getElementById(selectIntLink);
		textlink.style.display = "inline";

	    selectIntLink = selectedDiv + "-min";
		textlink = d.getElementById(selectIntLink);
		textlink.style.display = "none";
    }
	selectIntLink = selectedDiv + "-container-input";
	textlink = d.getElementById(selectIntLink);
	textlink.value = "hide";

}

function closeDiv(selectedDiv){
	showSave();
	selectedDiv = selectedDiv + "-container";
	Effect.Fade(selectedDiv, {duration:1});
	d = document;
	selectIntLink = selectedDiv + "-input";
	textlink = d.getElementById(selectIntLink);
	textlink.value = "close";
}

function showSave(){
	d = document;
	selectIntLink = "submit";
	textlink = d.getElementById(selectIntLink);
	textlink.style.display = "inline";
}

function updatePref(){
	var widgets = document.getElementsByClassName('widgetdiv');
	var widgetSequence = "";
	var firstprint = false;
	d = document;
	for (i=0; i<widgets.length; i++){
		if (firstprint)
			widgetSequence += ",";
		var widget = widgets[i].id;
		widgetSequence += widget + ":" + widgets[i].parentNode.id + ":";
		widget = widget + "-input";
		textlink = d.getElementById(widget).value;
		widgetSequence += textlink;
		firstprint = true;
	}
	selectLink = "sequence";
	textlink = d.getElementById(selectLink);
	textlink.value = widgetSequence;
	return true;
}

function hideAllWidgets(){
		Effect.Fade('niftyOutter', {to: 0.2});
}

function showAllWidgets(){
		Effect.Fade('niftyOutter', {to: 1.0});
}


function changeTabDIV(selectedDiv){
	var dashpos = selectedDiv.indexOf("-");
	var tabclass = selectedDiv.substring(0,dashpos);
	d = document;

	//get deactive tabs first
	tabclass = tabclass + "-class-tabdeactive";
	var tabs = document.getElementsByClassName(tabclass);
	var incTabSelected = selectedDiv + "-deactive";
	for (i=0; i<tabs.length; i++){
		var tab = tabs[i].id;
		dashpos = tab.lastIndexOf("-");
		var tab2 = tab.substring(0,dashpos) + "-deactive";
		if (tab2 == incTabSelected){
			tablink = d.getElementById(tab2);
			tablink.style.display = "none";
			tab2 = tab.substring(0,dashpos) + "-active";
			tablink = d.getElementById(tab2);
			tablink.style.display = "table-cell";

			//now show main div associated with link clicked
			tabmain = d.getElementById(selectedDiv);
			tabmain.style.display = "block";
		}
		else
		{
			tab2 = tab.substring(0,dashpos) + "-deactive";
			tablink = d.getElementById(tab2);
			tablink.style.display = "table-cell";
			tab2 = tab.substring(0,dashpos) + "-active";
			tablink = d.getElementById(tab2);
			tablink.style.display = "none";

			//hide sections we don't want to see
			tab2 = tab.substring(0,dashpos);
			tabmain = d.getElementById(tab2);
			tabmain.style.display = "none";

		}
	}
}

</script>
EOD;
$closehead = false;

## Set Page Title and Include Header
$pgtitle = array("{$g['product_name']} Dashboard");
include("head.inc");


echo "<script type=\"text/javascript\" language=\"javascript\" src=\"/javascript/domTT/domLib.js\"></script>";
echo "<script type=\"text/javascript\" language=\"javascript\" src=\"/javascript/domTT/domTT.js\"></script>";
echo "<script type=\"text/javascript\" language=\"javascript\" src=\"/javascript/domTT/behaviour.js\"></script>";
echo "<script type=\"text/javascript\" language=\"javascript\" src=\"/javascript/domTT/fadomatic.js\"></script>";
?>

<body link="#0000CC" vlink="#0000CC" alink="#0000CC">
<form action="index.php" method="post">
<script language="javascript" type="text/javascript">
// <![CDATA[
columns = ['col1','col2'];
// ]]>

</script>


<script src="/javascript/scriptaculous/prototype.js" type="text/javascript"></script>
<script src="/javascript/scriptaculous/scriptaculous.js" type="text/javascript"></script>

<?php
include("fbegin.inc");
echo $jscriptstr;
	if(!file_exists("/usr/local/www/themes/{$g['theme']}/no_big_logo"))
		echo "<center><img src=\"./themes/".$g['theme']."/images/logobig.jpg\"></center><br>";
?>
<div id="widgetcontainer" style="display:none">
		<div id="content1"><h1>Available Widgets</h1><p><?php
			$widgetfiles_add = $widgetfiles;
			sort($widgetfiles_add);
			foreach($widgetfiles_add as $widget) {
				if(!stristr($widget, "widget.php"))
					continue;

				$periodpos = strpos($widget, ".");
				$widgetname = substr($widget, 0, $periodpos);
				$nicename = $widgetname;
				$nicename = str_replace("_", " ", $nicename);
				//make the title look nice
				$nicename = ucwords($nicename);

				$widgettitle = $widgetname . "_title";
				$widgettitlelink = $widgetname . "_title_link";
					if ($$widgettitle != "")
					{
						//echo widget title
						?>
						<span style="cursor: pointer;" onclick='return addDiv("<?php echo $widgetname; ?>")'>
						<u><?php echo $$widgettitle; ?></u></span><br>
						<?php
					}
					else {?>
						<span style="cursor: pointer;" onclick='return addDiv("<?php echo $widgetname; ?>")'>
						<u><?php echo $nicename; ?></u></span><br><?php
					}
			}
		?>
		</p>
	</div>
</div>

<div id="welcomecontainer" style="display:none">
		<div id="welcome-container">
			<h1>
				<div style="float:left;width:80%;padding: 2px">
					Welcome to the New Dashboard page!
				</div>
				<div onclick="domTT_close(this);showAllWidgets();" style="float:right;width:8%; cursor:pointer;padding: 5px;" >
					<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_close.gif" />
				</div>
				<div style="clear:both;"></div>
			</h1>
			<p>
			This page allows you to customize the information you want to be displayed!<br/>
			To get started click the <img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif"> icon to add widgets.<br/>
			<br/>
			You can move any widget around by clicking and dragging the title.
			</p>
	</div>
</div>



<input type="hidden" value="" name="sequence" id="sequence">
<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_plus.gif" alt="Click here to add widgets" style="cursor: pointer;" onmouseup="domTT_activate(this, event, 'content', document.getElementById('content1'), 'type', 'velcro', 'delay', 0, 'fade', 'both', 'fadeMax', 100, 'styleClass', 'niceTitle');" />

<img src="./themes/<?= $g['theme']; ?>/images/icons/icon_info_pkg.gif" alt="Click here for help" style="cursor: help;" onmouseup="hideAllWidgets();domTT_activate(this, event, 'content', document.getElementById('welcome-container'), 'type', 'sticky', 'closeLink', '','delay', 0, 'fade', 'both', 'fadeMax', 100, 'styleClass', 'niceTitle');" />


&nbsp;&nbsp;&nbsp;
		<input id="submit" name="submit" type="submit" style="display:none" onclick="return updatePref();" class="formbtn" value="Save Settings" />
</p>

<div id="niftyOutter">
	<?php
	$totalwidgets = count($widgetfiles);
	$halftotal = $totalwidgets / 2 -1;
	$widgetcounter = 0;
	$directory = "widgets/widgets/";
	$printed = false;
	$firstprint = false;
	?>
	<div id="col1" style="float:left;width:49%;padding: 2px;padding-bottom:40px">
	<?php

	if ($config['widgets']){
		$widgetlist = $savedwidgetfiles;

		//add widgets that may not be in the saved configuration, in case they want to be added later
		foreach ($widgetfiles as $defaultwidgets){

			if (!in_array($defaultwidgets, $savedwidgetfiles)){
				$widgetlist[] = $defaultwidgets;
			}
		}

	}
	else
		$widgetlist = $widgetfiles;

	foreach($widgetlist as $widget) {

		if(!stristr($widget, "widget.php"))
			continue;

		$periodpos = strpos($widget, ".");
		$widgetname = substr($widget, 0, $periodpos);
		if ($widgetname != ""){
		$nicename = $widgetname;
		$nicename = str_replace("_", " ", $nicename);

		//make the title look nice
		$nicename = ucwords($nicename);

		if ($config['widgets']){
			if ($displayarray[$widgetcounter] == "show"){
				$divdisplay = "block";
				$display = "block";
				$inputdisplay = "show";
				$showdiv = "none";
				$mindiv = "inline";
			}
			else if ($displayarray[$widgetcounter] == "hide") {
				$divdisplay = "block";
				$display = "none";
				$inputdisplay = "hide";
				$showdiv = "inline";
				$mindiv = "none";
			}
			else if ($displayarray[$widgetcounter] == "close"){
				$divdisplay = "none";
				$display = "block";
				$inputdisplay = "close";
				$showdiv = "none";
				$mindiv = "inline";
			}
			else{
				$divdisplay = "none";
				$display = "block";
				$inputdisplay = "none";
				$showdiv = "none";
				$mindiv = "inline";
			}
		}
		else
		{
			if ($firstprint == false){
				$divdisplay = "block";
				$display = "block";
				$inputdisplay = "show";
				$showdiv = "none";
				$mindiv = "inline";
				$firstprint = true;
			}
			else
			{
				$divdisplay = "none";
				$display = "block";
				$inputdisplay = "close";
				$showdiv = "none";
				$mindiv = "inline";
			}
		}


		if ($config['widgets']){
			if ($colpos[$widgetcounter] == "col2" && $printed == false)
			{
				$printed = true;
				?>
				</div>
				<div id="col2" style="float:right;width:49%;padding: 2px;padding-bottom:40px">
				<?php
			}
		}
		else if ($widgetcounter >= $halftotal && $printed == false){
			$printed = true;
			?>
			</div>
			<div id="col2" style="float:right;width:49%;padding: 2px;padding-bottom:40px">
			<?php
		}

		?>
		<div style="clear:both;"></div>
		<div  id="<?php echo $widgetname;?>-container" class="widgetdiv" style="display:<?php echo $divdisplay; ?>;">
			<input type="hidden" value="<?php echo $inputdisplay;?>" id="<?php echo $widgetname;?>-container-input" name="<?php echo $widgetname;?>-container-input">
			<div id="<?php echo $widgetname;?>topic" class="widgetheader" style="cursor:move">
				<div style="float:left;">
					<?php

					$widgettitle = $widgetname . "_title";
					$widgettitlelink = $widgetname . "_title_link";
					if ($$widgettitle != "")
					{
						//only show link if defined
						if ($$widgettitlelink != "") {?>
						<u><span onClick="location.href='/<?php echo $$widgettitlelink;?>'" style="cursor:pointer">
						<?php }
							//echo widget title
							echo $$widgettitle;
						if ($$widgettitlelink != "") { ?>
						</span></u>
						<?php }
					}
					else{
						if ($$widgettitlelink != "") {?>
						<u><span onClick="location.href='/<?php echo $$widgettitlelink;?>'" style="cursor:pointer">
						<?php }
						echo $nicename;
							if ($$widgettitlelink != "") { ?>
						</span></u>
						<?php }
					}
					?>
				</div>
				<div align="right" style="float:right;">
					<div id="<?php echo $widgetname;?>-configure" onclick='return configureDiv("<?php echo $widgetname;?>")' style="display:none; cursor:pointer" ><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_configure.gif" /></div>
					<div id="<?php echo $widgetname;?>-open" onclick='return showDiv("<?php echo $widgetname;?>",true)' style="display:<?php echo $showdiv;?>; cursor:pointer" ><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_open.gif" /></div>
					<div id="<?php echo $widgetname;?>-min" onclick='return minimizeDiv("<?php echo $widgetname;?>",true)' style="display:<?php echo $mindiv;?>; cursor:pointer" ><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_minus.gif"/></div>
					<div id="<?php echo $widgetname;?>-close" onclick='return closeDiv("<?php echo $widgetname;?>",true)' style="display:inline; cursor:pointer" ><img src="./themes/<?= $g['theme']; ?>/images/icons/icon_close.gif" /></div>
				</div>
				<div style="clear:both;"></div>
			</div>
			<div id="<?php echo $widgetname;?>" style="display:<?php echo $display; ?>;">
				<?php
					if ($displayarray[$widgetcounter] != "close")
					{
						include($directory . $widget);
					}
				 ?>
			</div>
			<div style="clear:both;"></div>
		</div>
		<?php
	$widgetcounter++;
		}
	}//end foreach
	?>
	</div><!-- end col -->
	<?php  if ($printed == false){
			$printed = true;
			?>
			</div>
			<div id="col2" style="float:right;width:49%;padding: 2px;padding-bottom:40px"></div>

			<?php } ?>
	<div style="clear:both;"></div>
</div>



<?php include("fend.inc"); ?>

<script type="text/javascript">

	<?php if (!$config['widgets']){ ?>
	window.onload = function(in_event)
	{
			hideAllWidgets();
			domTT_activate('welcome1', null, 'x', 287, 'y', 107, 'content', document.getElementById('welcome-container'), 'type', 'sticky', 'closeLink', '','delay', 1000, 'fade', 'both', 'fadeMax', 100, 'styleClass', 'niceTitle');
	}
	<?php } ?>
	// <![CDATA[
	Sortable.create("col1", {tag:'div',dropOnEmpty:true,containment:columns,handle:'widgetheader',constraint:false,only:'widgetdiv',onChange:showSave});
	Sortable.create("col2", {tag:'div',dropOnEmpty:true,containment:columns,handle:'widgetheader',constraint:false,only:'widgetdiv',onChange:showSave});
	// ]]>

	<?php
	//build list of javascript include files
	$jsincludefiles = array();
	$directory = "widgets/javascript/";
	$dirhandle  = opendir($directory);
	$filename = "";
	while (false !== ($filename = readdir($dirhandle))) {
   		$jsincludefiles[] = $filename;
	}
	foreach($jsincludefiles as $jsincludename) {
		if(!stristr($jsincludename, ".js"))
			continue;
		include($directory . $jsincludename);
	}
	?>
</script>
</form>
</body>
</html>