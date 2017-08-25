<?php
/* $Id$ */
/*
	status_rrd_graph_img.php
	Part of pfSense
	Copyright (C) 2009 Seth Mos <seth.mos@xs4all.nl>
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
	pfSense_BUILDER_BINARIES:	/usr/bin/find	/bin/rm	/usr/local/bin/rrdtool
	pfSense_MODULE:	system
*/

require("guiconfig.inc");
require_once("shaper.inc");
require_once("rrd.inc");

$pgtitle = array("System","RRD Graphs","Image viewer");

if ($_GET['database']) {
	$curdatabase = $_GET['database'];
} else {
	$curdatabase = "wan-traffic.rrd";
}

if ($_GET['style']) {
	$curstyle = $_GET['style'];
} else {
	$curstyle = "inverse";
}

if ($_GET['interval']) {
	$interval = $_GET['interval'];
} else {
	$interval = "4h";
}

/* Deduce a interface if possible and use the description */
$curif = split("-", $curdatabase);
$curif = "$curif[0]";
$friendly = convert_friendly_interface_to_friendly_descr(strtolower($curif));
if($friendly == "") {
	$friendly = $curif;
}
$search = array("-", ".rrd", $curif);
$replace = array(" :: ", "", $friendly);
$prettydb = ucwords(str_replace($search, $replace, $curdatabase));

$periods = array("4h", "16h", "48h", "32d", "6m", "1y", "4y");

$found = 0;
foreach($periods as $period) if($period == $interval) $found = 1;
if($found == 0) {
	PRINT "Graph interval $interval is not valid <br />\n";
	exit();
}

$graphs['4h']['seconds'] = 14400;
$graphs['4h']['average'] = 60;
$graphs['4h']['scale'] = "MINUTE:5:MINUTE:10:MINUTE:30:0:%H%:%M";
$graphs['16h']['seconds'] = 57600;
$graphs['16h']['average'] = 60;
$graphs['16h']['scale'] = "MINUTE:30:HOUR:1:HOUR:1:0:%H";
$graphs['48h']['seconds'] = 172800;
$graphs['48h']['average'] = 300;
$graphs['48h']['scale'] = "HOUR:1:HOUR:6:HOUR:2:0:%H";
$graphs['32d']['seconds'] = 2764800;
$graphs['32d']['average'] = 3600;
$graphs['32d']['scale'] = "DAY:1:WEEK:1:WEEK:1:0:Week %W";
$graphs['6m']['seconds'] = 16070400;
$graphs['6m']['average'] = 43200;
$graphs['6m']['scale'] = "WEEK:1:MONTH:1:MONTH:1:0:%b";
$graphs['1y']['seconds'] = 31622400;
$graphs['1y']['average'] = 43200;
$graphs['1y']['scale'] = "MONTH:1:MONTH:3:MONTH:1:0:%b";
$graphs['4y']['seconds'] = 126489600;
$graphs['4y']['average'] = 86400;
$graphs['4y']['scale'] = "MONTH:1:YEAR:1:MONTH:3:0:%b";

/* generate the graphs when we request the page. */
$seconds = $graphs[$interval]['seconds'];
$average = $graphs[$interval]['average'];
$scale = $graphs[$interval]['scale'];

$rrddbpath = "/var/db/rrd/";
$rrdtmppath = "/tmp/";
$rrdtool = "/usr/bin/nice -n20 /usr/local/bin/rrdtool";
$uptime = "/usr/bin/uptime";
$sed = "/usr/bin/sed";

$havg = humantime($average);
$hperiod = humantime($seconds);
$data = true;

/* XXX: (billm) do we have an exec() type function that does this type of thing? */
exec("cd $rrddbpath;/usr/bin/find -name *.rrd", $databases);
rsort($databases);

/* compare bytes/sec counters, divide bps by 8 */
read_altq_config();
if ($altq_list_queues[$curif]) {
	$altq =& $altq_list_queues[$curif];
	switch ($altq->GetBwscale()) {
        case "Gb":
                $factor = 1024 * 1024 * 1024;
        break;
        case "Mb":
                $factor = 1024 * 1024;
        break;
        case "Kb":
                $factor = 1024;
        break;
        case "b":
        default:
                $factor = 1;
        break;
        }
	$upstream = (($altq->GetBandwidth()*$factor)/8);
	$downstream = $upstream; /* XXX: Ugly hack */
	$upif = $curif;
	$downif = "lan"; /* XXX should this be set to something else?! */
} else {
	$altq = null;
	$downstream = 12500000;
	$upstream = 12500000;
	$upif = "wan";
	$downif = "lan";
}

$speedlimit = ($upstream + $downstream);

/* select theme colors if the inclusion file exists */
$rrdcolors = "./themes/{$g['theme']}/rrdcolors.inc.php";
if(file_exists($rrdcolors)) {
	include($rrdcolors);
} else {
	log_error("rrdcolors.inc.php for theme {$g['theme']} does not exist, using defaults!");
	$colortrafficup = array("666666", "CCCCCC");
	$colortrafficdown = array("990000", "CC0000");
	$colorpacketsup = array("666666", "CCCCCC");
	$colorpacketsdown = array("990000", "CC0000");
	$colorstates = array('990000','a83c3c','b36666','bd9090','cccccc','000000');
	$colorprocessor = array('990000','a83c3c','b36666','bd9090','cccccc','000000');
	$colormemory = array('990000','a83c3c','b36666','bd9090','cccccc','000000');
	$colorqueuesup = array('000000','7B0000','990000','BB0000','CC0000','D90000','EE0000','FF0000','CC0000');
	$colorqueuesdown = array('000000','7B7B7B','999999','BBBBBB','CCCCCC','D9D9D9','EEEEEE','FFFFFF','CCCCCC');
	$colorqueuesdropup = array('000000','7B0000','990000','BB0000','CC0000','D90000','EE0000','FF0000','CC0000');
	$colorqueuesdropdown = array('000000','7B7B7B','999999','BBBBBB','CCCCCC','D9D9D9','EEEEEE','FFFFFF','CCCCCC');
	$colorqualityrtt = array('990000','a83c3c','b36666','bd9090','cccccc','000000');
	$colorqualityloss = "ee0000";
	$colorwireless = array('990000','a83c3c','b36666');
	$colorspamdtime = array('DDDDFF', 'AAAAFF', 'DDDDFF', '000066');
	$colorspamdconn = array('00AA00BB', 'FFFFFFFF', '00660088', 'FFFFFF88', '006600');
}

switch ($curstyle) {
case "absolute":
	$multiplier = 1;
	$AREA = "LINE1";
	break;
default:
	$multiplier = -1;
	$AREA = "AREA";
	break;
}

function humantime($timestamp){
	$difference = $timestamp;
	$periods = array("second", "minute", "hour", "day", "week", "month", "year", "decade");
	$lengths = array("60","60","24","7","4.35","12","10");
	for($j = 0; $difference >= $lengths[$j]; $j++) {
		$difference /= $lengths[$j];
		$difference = round($difference);
	}
	if($difference != 1) {
		$periods[$j].= "s";
	}
	$text = "$difference $periods[$j]";
	return $text;
}

if((strstr($curdatabase, "-traffic.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* define graphcmd for traffic stats */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average --vertical-label \"bits/sec\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"{$scale}\" ";
	$graphcmd .= "DEF:$curif-in_bytes_pass=$rrddbpath$curdatabase:inpass:AVERAGE ";
	$graphcmd .= "DEF:$curif-out_bytes_pass=$rrddbpath$curdatabase:outpass:AVERAGE ";
	$graphcmd .= "DEF:$curif-in_bytes_block=$rrddbpath$curdatabase:inblock:AVERAGE ";
	$graphcmd .= "DEF:$curif-out_bytes_block=$rrddbpath$curdatabase:outblock:AVERAGE ";

	$graphcmd .= "CDEF:\"$curif-in_bits_pass=$curif-in_bytes_pass,8,*\" ";
	$graphcmd .= "CDEF:\"$curif-out_bits_pass=$curif-out_bytes_pass,8,*\" ";
	$graphcmd .= "CDEF:\"$curif-in_bits_block=$curif-in_bytes_block,8,*\" ";
	$graphcmd .= "CDEF:\"$curif-out_bits_block=$curif-out_bytes_block,8,*\" ";

	$graphcmd .= "CDEF:\"$curif-in_bytes=$curif-in_bytes_pass,$curif-in_bytes_block,+\" ";
	$graphcmd .= "CDEF:\"$curif-out_bytes=$curif-out_bytes_pass,$curif-out_bytes_block,+\" ";
	$graphcmd .= "CDEF:\"$curif-in_bits=$curif-in_bits_pass,$curif-in_bits_block,+\" ";
	$graphcmd .= "CDEF:\"$curif-out_bits=$curif-out_bits_pass,$curif-out_bits_block,+\" ";

	$graphcmd .= "CDEF:\"$curif-bits_io=$curif-in_bits,$curif-out_bits,+\" ";
	$graphcmd .= "CDEF:\"$curif-out_bits_block_neg=$curif-out_bits_block,$multiplier,*\" ";
	$graphcmd .= "CDEF:\"$curif-out_bits_pass_neg=$curif-out_bits_pass,$multiplier,*\" ";

	$graphcmd .= "CDEF:\"$curif-bytes_in_pass=$curif-in_bytes_pass,0,$speedlimit,LIMIT,UN,0,$curif-in_bytes_pass,IF,$average,*\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_out_pass=$curif-out_bytes_pass,0,$speedlimit,LIMIT,UN,0,$curif-out_bytes_pass,IF,$average,*\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_in_block=$curif-in_bytes_block,0,$speedlimit,LIMIT,UN,0,$curif-in_bytes_block,IF,$average,*\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_out_block=$curif-out_bytes_block,0,$speedlimit,LIMIT,UN,0,$curif-out_bytes_block,IF,$average,*\" ";

	$graphcmd .= "CDEF:\"$curif-bytes_pass=$curif-bytes_in_pass,$curif-bytes_out_pass,+\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_block=$curif-bytes_in_block,$curif-bytes_out_block,+\" ";

	$graphcmd .= "CDEF:\"$curif-bytes_in_t_pass=$curif-in_bytes_pass,0,$speedlimit,LIMIT,UN,0,$curif-in_bytes_pass,IF,$seconds,*\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_out_t_pass=$curif-out_bytes_pass,0,$speedlimit,LIMIT,UN,0,$curif-out_bytes_pass,IF,$seconds,*\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_in_t_block=$curif-in_bytes_block,0,$speedlimit,LIMIT,UN,0,$curif-in_bytes_block,IF,$seconds,*\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_out_t_block=$curif-out_bytes_block,0,$speedlimit,LIMIT,UN,0,$curif-out_bytes_block,IF,$seconds,*\" ";

	$graphcmd .= "CDEF:\"$curif-bytes_t_pass=$curif-bytes_in_t_pass,$curif-bytes_out_t_pass,+\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_t_block=$curif-bytes_in_t_block,$curif-bytes_out_t_block,+\" ";
	$graphcmd .= "CDEF:\"$curif-bytes_t=$curif-bytes_in_t_pass,$curif-bytes_out_t_block,+\" ";

	$graphcmd .= "AREA:\"$curif-in_bits_block#{$colortrafficdown[1]}:$curif-in-block\" ";
	$graphcmd .= "AREA:\"$curif-in_bits_pass#{$colortrafficdown[0]}:$curif-in-pass:STACK\" ";
	$graphcmd .= "{$AREA}:\"$curif-out_bits_block_neg#{$colortrafficup[1]}:$curif-out-block\" ";
	$graphcmd .= "{$AREA}:\"$curif-out_bits_pass_neg#{$colortrafficup[0]}:$curif-out-pass:STACK\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t  maximum       average       current        period\\n\" ";
	$graphcmd .= "COMMENT:\"in-pass\t\" ";
	$graphcmd .= "GPRINT:\"$curif-in_bits_pass:MAX:%7.2lf %sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-in_bits_pass:AVERAGE:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-in_bits_pass:LAST:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-bytes_in_t_pass:AVERAGE:%7.2lf %sB i\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"out-pass\t\" ";
	$graphcmd .= "GPRINT:\"$curif-out_bits_pass:MAX:%7.2lf %sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-out_bits_pass:AVERAGE:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-out_bits_pass:LAST:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-bytes_out_t_pass:AVERAGE:%7.2lf %sB o\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"in-block\t\" ";
	$graphcmd .= "GPRINT:\"$curif-in_bits_block:MAX:%7.2lf %sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-in_bits_block:AVERAGE:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-in_bits_block:LAST:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-bytes_in_t_block:AVERAGE:%7.2lf %sB i\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"out-block\t\" ";
	$graphcmd .= "GPRINT:\"$curif-out_bits_block:MAX:%7.2lf %sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-out_bits_block:AVERAGE:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-out_bits_block:LAST:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"$curif-bytes_out_t_block:AVERAGE:%7.2lf %sB o\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif(strstr($curdatabase, "-throughput.rrd")) {
	/* define graphcmd for throughput stats */
	/* this gathers all interface statistics, the database does not actually exist */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--vertical-label \"bits/sec\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"{$scale}\" ";

	$iflist = get_configured_interface_list();
	$g = 0;
	$operand = "";
	$comma = "";
	$graphtputbip = "";
	$graphtputbop = "";
	$graphtputbtp = "";
	$graphtputbib = "";
	$graphtputbob = "";
	$graphtputbtb = "";
	$graphtputbyip = "";
	$graphtputbyop = "";
	$graphtputbytp = "";
	$graphtputbyib = "";
	$graphtputbyob = "";
	$graphtputbytb = "";
	foreach($iflist as $ifname) {
		/* collect all interface stats */
		$graphcmd .= "DEF:\"{$ifname}-in_bytes_pass={$rrddbpath}{$ifname}-traffic.rrd:inpass:AVERAGE\" ";
		$graphcmd .= "DEF:\"{$ifname}-out_bytes_pass={$rrddbpath}{$ifname}-traffic.rrd:outpass:AVERAGE\" ";
		$graphcmd .= "DEF:\"{$ifname}-in_bytes_block={$rrddbpath}{$ifname}-traffic.rrd:inblock:AVERAGE\" ";
		$graphcmd .= "DEF:\"{$ifname}-out_bytes_block={$rrddbpath}{$ifname}-traffic.rrd:outblock:AVERAGE\" ";

		$graphcmd .= "CDEF:\"{$ifname}-in_bytes={$ifname}-in_bytes_pass,{$ifname}-in_bytes_block,+\" ";
		$graphcmd .= "CDEF:\"{$ifname}-out_bytes={$ifname}-out_bytes_pass,{$ifname}-out_bytes_block,+\" ";

		$graphcmd .= "CDEF:\"{$ifname}-in_bits_pass={$ifname}-in_bytes_pass,8,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-out_bits_pass={$ifname}-out_bytes_pass,8,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bits_io_pass={$ifname}-in_bits_pass,{$ifname}-out_bits_pass,+\" ";

		$graphcmd .= "CDEF:\"{$ifname}-in_bits_block={$ifname}-in_bytes,8,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-out_bits_block={$ifname}-out_bytes,8,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bits_io_block={$ifname}-in_bits_block,{$ifname}-out_bits_block,+\" ";

		$graphcmd .= "CDEF:\"{$ifname}-bytes_in_pass={$ifname}-in_bytes_pass,0,$speedlimit,LIMIT,UN,0,{$ifname}-in_bytes_pass,IF,$average,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_out_pass={$ifname}-out_bytes_pass,0,$speedlimit,LIMIT,UN,0,{$ifname}-out_bytes_pass,IF,$average,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_in_block={$ifname}-in_bytes_block,0,$speedlimit,LIMIT,UN,0,{$ifname}-in_bytes_block,IF,$average,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_out_block={$ifname}-out_bytes_block,0,$speedlimit,LIMIT,UN,0,{$ifname}-out_bytes_block,IF,$average,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_pass={$ifname}-bytes_in_pass,{$ifname}-bytes_out_pass,+\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_block={$ifname}-bytes_in_pass,{$ifname}-bytes_out_block,+\" ";

		$graphcmd .= "CDEF:\"{$ifname}-bytes_in_t_pass={$ifname}-in_bytes,0,$speedlimit,LIMIT,UN,0,{$ifname}-in_bytes_pass,IF,$seconds,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_in_t_block={$ifname}-in_bytes,0,$speedlimit,LIMIT,UN,0,{$ifname}-in_bytes_block,IF,$seconds,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_out_t_pass={$ifname}-out_bytes,0,$speedlimit,LIMIT,UN,0,{$ifname}-out_bytes_pass,IF,$seconds,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_out_t_block={$ifname}-out_bytes,0,$speedlimit,LIMIT,UN,0,{$ifname}-out_bytes_block,IF,$seconds,*\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_t_pass={$ifname}-bytes_in_t_pass,{$ifname}-bytes_out_t_pass,+\" ";
		$graphcmd .= "CDEF:\"{$ifname}-bytes_t_block={$ifname}-bytes_in_t_block,{$ifname}-bytes_out_t_block,+\" ";
		if ($g > 0) {
			$operand .= ",+";
			$comma = ",";
		}
		$graphtputbip .= "{$comma}{$ifname}-in_bits_pass";
		$graphtputbop .= "{$comma}{$ifname}-out_bits_pass";
		$graphtputbtp .= "{$comma}{$ifname}-bits_io_pass";
		$graphtputbib .= "{$comma}{$ifname}-in_bits_block";
		$graphtputbob .= "{$comma}{$ifname}-out_bits_block";
		$graphtputbtb .= "{$comma}{$ifname}-bits_io_block";
		$graphtputbyip .= "{$comma}{$ifname}-bytes_in_t_pass";
		$graphtputbyop .= "{$comma}{$ifname}-bytes_out_t_pass";
		$graphtputbyib .= "{$comma}{$ifname}-bytes_in_t_block";
		$graphtputbyob .= "{$comma}{$ifname}-bytes_out_t_block";
		$graphtputbytp .= "{$comma}{$ifname}-bytes_t_pass";
		$graphtputbytb .= "{$comma}{$ifname}-bytes_t_block";
		$g++;
	}
	$graphcmd .= "CDEF:\"tput-in_bits_pass={$graphtputbip}{$operand}\" ";
	$graphcmd .= "CDEF:\"tput-out_bits_pass={$graphtputbop}{$operand}\" ";
	$graphcmd .= "CDEF:\"tput-bits_io_pass={$graphtputbtp}{$operand}\" ";

	$graphcmd .= "CDEF:\"tput-in_bits_block={$graphtputbib}{$operand}\" ";
	$graphcmd .= "CDEF:\"tput-out_bits_block={$graphtputbob}{$operand}\" ";
	$graphcmd .= "CDEF:\"tput-bits_io_block={$graphtputbtb}{$operand}\" ";

	$graphcmd .= "CDEF:\"tput-out_bits_pass_neg=tput-out_bits_pass,$multiplier,*\" ";
	$graphcmd .= "CDEF:\"tput-out_bits_block_neg=tput-out_bits_block,$multiplier,*\" ";

	$graphcmd .= "CDEF:\"tput-bytes_in_t_pass={$graphtputbyip}{$operand}\" ";
	$graphcmd .= "CDEF:\"tput-bytes_out_t_pass={$graphtputbyop}{$operand}\" ";
	$graphcmd .= "CDEF:\"tput-bytes_t_pass={$graphtputbytp}{$operand}\" ";

	$graphcmd .= "CDEF:\"tput-bytes_in_t_block={$graphtputbyib}{$operand}\" ";
	$graphcmd .= "CDEF:\"tput-bytes_out_t_block={$graphtputbyob}{$operand}\" ";
	$graphcmd .= "CDEF:\"tput-bytes_t_block={$graphtputbytb}{$operand}\" ";

	$graphcmd .= "AREA:\"tput-in_bits_block#{$colortrafficdown[0]}:in-block \" ";
	$graphcmd .= "AREA:\"tput-in_bits_pass#{$colortrafficdown[1]}:in-pass \" ";

	$graphcmd .= "{$AREA}:\"tput-out_bits_block_neg#{$colortrafficup[1]}:out-block \" ";
	$graphcmd .= "{$AREA}:\"tput-out_bits_pass_neg#{$colortrafficup[0]}:out-pass \" ";

	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t  maximum       average       current        period\\n\" ";
	$graphcmd .= "COMMENT:\"in-pass\t\" ";
	$graphcmd .= "GPRINT:\"tput-in_bits_pass:MAX:%7.2lf %sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-in_bits_pass:AVERAGE:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-in_bits_pass:LAST:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-bytes_in_t_pass:AVERAGE:%7.2lf %sB i\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"out-pass\t\" ";
	$graphcmd .= "GPRINT:\"tput-out_bits_pass:MAX:%7.2lf %sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-out_bits_pass:AVERAGE:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-out_bits_pass:LAST:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-bytes_out_t_pass:AVERAGE:%7.2lf %sB o\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"in-block\t\" ";
	$graphcmd .= "GPRINT:\"tput-in_bits_block:MAX:%7.2lf %sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-in_bits_block:AVERAGE:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-in_bits_block:LAST:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-bytes_in_t_block:AVERAGE:%7.2lf %sB i\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"out-block\t\" ";
	$graphcmd .= "GPRINT:\"tput-out_bits_block:MAX:%7.2lf %sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-out_bits_block:AVERAGE:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-out_bits_block:LAST:%7.2lf %Sb/s\" ";
	$graphcmd .= "GPRINT:\"tput-bytes_out_t_block:AVERAGE:%7.2lf %sB o\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif((strstr($curdatabase, "-packets.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* define graphcmd for packets stats */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--vertical-label \"packets/sec\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"$scale\" ";
	$graphcmd .= "DEF:\"$curif-in_pps_pass=$rrddbpath$curdatabase:inpass:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-out_pps_pass=$rrddbpath$curdatabase:outpass:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-in_pps_block=$rrddbpath$curdatabase:inblock:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-out_pps_block=$rrddbpath$curdatabase:outblock:AVERAGE\" ";

	$graphcmd .= "CDEF:\"$curif-in_pps=$curif-in_pps_pass,$curif-in_pps_block,+\" ";
	$graphcmd .= "CDEF:\"$curif-out_pps=$curif-out_pps_pass,$curif-out_pps_block,+\" ";
	$graphcmd .= "CDEF:\"$curif-out_pps_pass_neg=$curif-out_pps_pass,$multiplier,*\" ";
	$graphcmd .= "CDEF:\"$curif-out_pps_block_neg=$curif-out_pps_block,$multiplier,*\" ";

	$graphcmd .= "CDEF:\"$curif-pps_in_pass=$curif-in_pps_pass,0,12500000,LIMIT,UN,0,$curif-in_pps_pass,IF,$average,*\" ";
	$graphcmd .= "CDEF:\"$curif-pps_out_pass=$curif-out_pps_pass,0,12500000,LIMIT,UN,0,$curif-out_pps_pass,IF,$average,*\" ";
	$graphcmd .= "CDEF:\"$curif-pps_in_block=$curif-in_pps_block,0,12500000,LIMIT,UN,0,$curif-in_pps_block,IF,$average,*\" ";
	$graphcmd .= "CDEF:\"$curif-pps_out_block=$curif-out_pps_block,0,12500000,LIMIT,UN,0,$curif-out_pps_block,IF,$average,*\" ";

	$graphcmd .= "CDEF:\"$curif-pps_io=$curif-in_pps,$curif-out_pps,+\" ";
	$graphcmd .= "CDEF:\"$curif-pps_pass=$curif-pps_in_pass,$curif-pps_out_pass,+\" ";
	$graphcmd .= "CDEF:\"$curif-pps_block=$curif-pps_in_block,$curif-pps_out_block,+\" ";

	$graphcmd .= "CDEF:\"$curif-pps_in_t_pass=$curif-in_pps_pass,0,12500000,LIMIT,UN,0,$curif-in_pps_pass,IF,$seconds,*\" ";
	$graphcmd .= "CDEF:\"$curif-pps_out_t_pass=$curif-out_pps_pass,0,12500000,LIMIT,UN,0,$curif-out_pps_pass,IF,$seconds,*\" ";
	$graphcmd .= "CDEF:\"$curif-pps_in_t_block=$curif-in_pps_block,0,12500000,LIMIT,UN,0,$curif-in_pps_block,IF,$seconds,*\" ";
	$graphcmd .= "CDEF:\"$curif-pps_out_t_block=$curif-out_pps_block,0,12500000,LIMIT,UN,0,$curif-out_pps_block,IF,$seconds,*\" ";

	$graphcmd .= "CDEF:\"$curif-pps_t_pass=$curif-pps_in_t_pass,$curif-pps_out_t_pass,+\" ";
	$graphcmd .= "CDEF:\"$curif-pps_t_block=$curif-pps_in_t_block,$curif-pps_out_t_block,+\" ";

	$graphcmd .= "AREA:\"$curif-in_pps_block#{$colorpacketsdown[1]}:$curif-in-block\" ";
	$graphcmd .= "AREA:\"$curif-in_pps_pass#{$colorpacketsdown[0]}:$curif-in-pass:STACK\" ";

	$graphcmd .= "$AREA:\"$curif-out_pps_block_neg#{$colorpacketsup[1]}:$curif-out-block\" ";
	$graphcmd .= "$AREA:\"$curif-out_pps_pass_neg#{$colorpacketsup[0]}:$curif-out-pass:STACK\" ";

	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t  maximum       average       current        period\\n\" ";
	$graphcmd .= "COMMENT:\"in-pass\t\" ";
	$graphcmd .= "GPRINT:\"$curif-in_pps_pass:MAX:%7.2lf %s pps\" ";
	$graphcmd .= "GPRINT:\"$curif-in_pps_pass:AVERAGE:%7.2lf %S pps\" ";
	$graphcmd .= "GPRINT:\"$curif-in_pps_pass:LAST:%7.2lf %S pps\" ";
	$graphcmd .= "GPRINT:\"$curif-pps_in_t_pass:AVERAGE:%7.2lf %s pkts\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"out-pass\t\" ";
	$graphcmd .= "GPRINT:\"$curif-out_pps_pass:MAX:%7.2lf %s pps\" ";
	$graphcmd .= "GPRINT:\"$curif-out_pps_pass:AVERAGE:%7.2lf %S pps\" ";
	$graphcmd .= "GPRINT:\"$curif-out_pps_pass:LAST:%7.2lf %S pps\" ";
	$graphcmd .= "GPRINT:\"$curif-pps_out_t_pass:AVERAGE:%7.2lf %s pkts\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"in-block\t\" ";
	$graphcmd .= "GPRINT:\"$curif-in_pps_block:MAX:%7.2lf %s pps\" ";
	$graphcmd .= "GPRINT:\"$curif-in_pps_block:AVERAGE:%7.2lf %S pps\" ";
	$graphcmd .= "GPRINT:\"$curif-in_pps_block:LAST:%7.2lf %S pps\" ";
	$graphcmd .= "GPRINT:\"$curif-pps_in_t_block:AVERAGE:%7.2lf %s pkts\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"out-pass\t\" ";
	$graphcmd .= "GPRINT:\"$curif-out_pps_block:MAX:%7.2lf %s pps\" ";
	$graphcmd .= "GPRINT:\"$curif-out_pps_block:AVERAGE:%7.2lf %S pps\" ";
	$graphcmd .= "GPRINT:\"$curif-out_pps_block:LAST:%7.2lf %S pps\" ";
	$graphcmd .= "GPRINT:\"$curif-pps_out_t_block:AVERAGE:%7.2lf %s pkts\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif((strstr($curdatabase, "-wireless.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* define graphcmd for packets stats */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--vertical-label \"snr/channel/rate\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"$scale\" ";
	$graphcmd .= "DEF:\"$curif-snr=$rrddbpath$curdatabase:snr:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-rate=$rrddbpath$curdatabase:rate:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-channel=$rrddbpath$curdatabase:channel:AVERAGE\" ";
	$graphcmd .= "LINE1:\"$curif-snr#{$colorwireless[0]}:$curif-snr\" ";
	$graphcmd .= "LINE1:\"$curif-rate#{$colorwireless[1]}:$curif-rate\" ";
	$graphcmd .= "LINE1:\"$curif-channel#{$colorwireless[2]}:$curif-channel\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t  maximum       average       current        period\\n\" ";
	$graphcmd .= "COMMENT:\"SNR\t\t\" ";
	$graphcmd .= "GPRINT:\"$curif-snr:MAX:%7.2lf %s dBi\" ";
	$graphcmd .= "GPRINT:\"$curif-snr:AVERAGE:%7.2lf %S dBi\" ";
	$graphcmd .= "GPRINT:\"$curif-snr:LAST:%7.2lf %S dBi\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"RATE\t\t\" ";
	$graphcmd .= "GPRINT:\"$curif-rate:MAX:%7.2lf %s Mb \" ";
	$graphcmd .= "GPRINT:\"$curif-rate:AVERAGE:%7.2lf %S Mb \" ";
	$graphcmd .= "GPRINT:\"$curif-rate:LAST:%7.2lf %S Mb \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Channel\t\" ";
	$graphcmd .= "GPRINT:\"$curif-channel:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-channel:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-channel:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif((strstr($curdatabase, "-states.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* define graphcmd for states stats */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--vertical-label \"states, ip\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"$scale\" ";
	$graphcmd .= "DEF:\"$curif-pfrate=$rrddbpath$curdatabase:pfrate:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-pfstates=$rrddbpath$curdatabase:pfstates:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-pfnat=$rrddbpath$curdatabase:pfnat:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-srcip=$rrddbpath$curdatabase:srcip:AVERAGE\" ";
	$graphcmd .= "DEF:\"$curif-dstip=$rrddbpath$curdatabase:dstip:AVERAGE\" ";
	$graphcmd .= "CDEF:\"$curif-pfrate_t=$curif-pfrate,0,1000000,LIMIT,UN,0,$curif-pfrate,IF,$seconds,*\" ";
	$graphcmd .= "LINE1:\"$curif-pfrate#{$colorstates[0]}:$curif-pfrate\" ";
	$graphcmd .= "LINE1:\"$curif-pfstates#{$colorstates[1]}:$curif-pfstates\" ";
	$graphcmd .= "LINE1:\"$curif-pfnat#{$colorstates[2]}:$curif-pfnat\" ";
	$graphcmd .= "LINE1:\"$curif-srcip#{$colorstates[3]}:$curif-srcip\" ";
	$graphcmd .= "LINE1:\"$curif-dstip#{$colorstates[4]}:$curif-dstip\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t      minimum        average        maximum        current         period\\n\" ";
	$graphcmd .= "COMMENT:\"state changes\" ";
	$graphcmd .= "GPRINT:\"$curif-pfrate:MIN:%7.2lf %s cps\" ";
	$graphcmd .= "GPRINT:\"$curif-pfrate:AVERAGE:%7.2lf %s cps\" ";
	$graphcmd .= "GPRINT:\"$curif-pfrate:MAX:%7.2lf %s cps\" ";
	$graphcmd .= "GPRINT:\"$curif-pfrate:LAST:%7.2lf %S cps\" ";
	$graphcmd .= "GPRINT:\"$curif-pfrate_t:AVERAGE:%7.2lf %s chg\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"filter states\" ";
	$graphcmd .= "GPRINT:\"$curif-pfstates:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-pfstates:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-pfstates:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-pfstates:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"nat states   \" ";
	$graphcmd .= "GPRINT:\"$curif-pfnat:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-pfnat:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-pfnat:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-pfnat:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Source addr. \" ";
	$graphcmd .= "GPRINT:\"$curif-srcip:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-srcip:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-srcip:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-srcip:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Dest. addr.  \" ";
	$graphcmd .= "GPRINT:\"$curif-dstip:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-dstip:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-dstip:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"$curif-dstip:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif((strstr($curdatabase, "-processor.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* define graphcmd for processor stats */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--vertical-label \"utilization, number\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"$scale\" ";
	$graphcmd .= "DEF:\"user=$rrddbpath$curdatabase:user:AVERAGE\" ";
	$graphcmd .= "DEF:\"nice=$rrddbpath$curdatabase:nice:AVERAGE\" ";
	$graphcmd .= "DEF:\"system=$rrddbpath$curdatabase:system:AVERAGE\" ";
	$graphcmd .= "DEF:\"interrupt=$rrddbpath$curdatabase:interrupt:AVERAGE\" ";
	$graphcmd .= "DEF:\"processes=$rrddbpath$curdatabase:processes:AVERAGE\" ";
	$graphcmd .= "AREA:\"user#{$colorprocessor[0]}:user\" ";
	$graphcmd .= "AREA:\"nice#{$colorprocessor[1]}:nice:STACK\" ";
	$graphcmd .= "AREA:\"system#{$colorprocessor[2]}:system:STACK\" ";
	$graphcmd .= "AREA:\"interrupt#{$colorprocessor[3]}:interrupt:STACK\" ";
	$graphcmd .= "LINE2:\"processes#{$colorprocessor[4]}:processes\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t      minimum        average        maximum        current\\n\" ";
	$graphcmd .= "COMMENT:\"User util.   \" ";
	$graphcmd .= "GPRINT:\"user:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"user:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"user:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"user:LAST:%7.2lf %S    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Nice util.   \" ";
	$graphcmd .= "GPRINT:\"nice:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"nice:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"nice:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"nice:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"System util. \" ";
	$graphcmd .= "GPRINT:\"system:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"system:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"system:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"system:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Interrupt    \" ";
	$graphcmd .= "GPRINT:\"interrupt:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"interrupt:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"interrupt:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"interrupt:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Processes    \" ";
	$graphcmd .= "GPRINT:\"processes:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"processes:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"processes:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"processes:LAST:%7.2lf %s    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif((strstr($curdatabase, "-memory.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* define graphcmd for memory usage stats */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--vertical-label \"utilization, percent\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"$scale\" ";
	$graphcmd .= "DEF:\"active=$rrddbpath$curdatabase:active:AVERAGE\" ";
	$graphcmd .= "DEF:\"inactive=$rrddbpath$curdatabase:inactive:AVERAGE\" ";
	$graphcmd .= "DEF:\"free=$rrddbpath$curdatabase:free:AVERAGE\" ";
	$graphcmd .= "DEF:\"cache=$rrddbpath$curdatabase:cache:AVERAGE\" ";
	$graphcmd .= "DEF:\"wire=$rrddbpath$curdatabase:wire:AVERAGE\" ";
	$graphcmd .= "LINE2:\"active#{$colormemory[0]}:active\" ";
	$graphcmd .= "LINE2:\"inactive#{$colormemory[1]}:inactive\" ";
	$graphcmd .= "LINE2:\"free#{$colormemory[2]}:free\" ";
	$graphcmd .= "LINE2:\"cache#{$colormemory[3]}:cache\" ";
	$graphcmd .= "LINE2:\"wire#{$colormemory[4]}:wire\" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t      minimum        average        maximum        current\\n\" ";
	$graphcmd .= "COMMENT:\"Active.      \" ";
	$graphcmd .= "GPRINT:\"active:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"active:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"active:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"active:LAST:%7.2lf %S    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Inactive.    \" ";
	$graphcmd .= "GPRINT:\"inactive:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"inactive:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"inactive:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"inactive:LAST:%7.2lf %S    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Free.        \" ";
	$graphcmd .= "GPRINT:\"free:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"free:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"free:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"free:LAST:%7.2lf %S    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Cached.      \" ";
	$graphcmd .= "GPRINT:\"cache:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"cache:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"cache:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"cache:LAST:%7.2lf %S    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"Wired.       \" ";
	$graphcmd .= "GPRINT:\"wire:MIN:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"wire:AVERAGE:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"wire:MAX:%7.2lf %s    \" ";
	$graphcmd .= "GPRINT:\"wire:LAST:%7.2lf %S    \" ";
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif((strstr($curdatabase, "-queues.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* define graphcmd for queue stats */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--vertical-label \"bits/sec\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"$scale\" ";
	if ($altq) {
		$a_queues =& $altq->get_queue_list();
	} else {
		$a_queues = array();
		$i = 0;
		$t = 0;
	}
	foreach ($a_queues as $name => $q) {
		$color = "$colorqueuesup[$t]";
		if($t > 0) { $stack = ":STACK"; }
		$graphcmd .= "DEF:\"$name=$rrddbpath$curdatabase:$name:AVERAGE\" ";
		$graphcmd .= "CDEF:\"$name-bytes_out=$name,0,$speedlimit,LIMIT,UN,0,$name,IF\" ";
		$graphcmd .= "CDEF:\"$name-bits_out=$name-bytes_out,8,*\" ";
		$graphcmd .= "$AREA:\"$name-bits_out#${color}:$name\" ";
		$t++;
		if($t > 7) { $t = 0; }
	}
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif((strstr($curdatabase, "-queuedrops.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* define graphcmd for queuedrop stats */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--vertical-label \"drops / sec\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--height 200 --width 620 -x \"$scale\" ";
	if ($altq) {
		$a_queues =& $altq->get_queue_list();
	} else {
        	$a_queues = array();
		$i = 0;
		$t = 0;
	}
	foreach ($a_queues as $name => $q) {
		$color = "$colorqueuesdropup[$t]";
		if($t > 0) { $stack = ":STACK"; }
		$graphcmd .= "DEF:\"$name=$rrddbpath$curdatabase:$name:AVERAGE\" ";
		$graphcmd .= "CDEF:\"$name-bytes_out=$name,0,$speedlimit,LIMIT,UN,0,$name,IF\" ";
		$graphcmd .= "CDEF:\"$name-bits_out=$name-bytes_out,8,*\" ";
		$graphcmd .= "CDEF:\"$name-bits_out_neg=$name-bits_out,$multiplier,*\" ";
		$graphcmd .= "$AREA:\"$name-bits_out_neg#${color}:$name\" ";
		$t++;
		if($t > 7) { $t = 0; }
	}
	$graphcmd .= "COMMENT:\"\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
}
elseif((strstr($curdatabase, "-quality.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* make a link quality graphcmd, we only have WAN for now, others too follow */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png \\
		--start -$seconds -e -$average \\
		--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" \\
		--color SHADEA#eeeeee --color SHADEB#eeeeee \\
		--vertical-label \"ms / %\" \\
		--height 200 --width 620 \\
		-x \"$scale\" --lower-limit 0 \\
		DEF:delayraw=$rrddbpath$curdatabase:delay:AVERAGE \\
		DEF:loss=$rrddbpath$curdatabase:loss:AVERAGE \\
		\"CDEF:delay=delayraw,1000,*\" \\
		\"CDEF:roundavg=delay,PREV(delay),+,2,/\" \\
		\"CDEF:loss10=loss,$multiplier,*\" \\
		\"CDEF:r0=delay,20,MIN\" \\
		\"CDEF:r1=delay,60,MIN\" \\
		\"CDEF:r2=delay,180,MIN\" \\
		\"CDEF:r3=delay,420,MIN\" \\
		COMMENT:\"\t\t\t\t\tDelay\t\t\tPacket loss\\n\" \\
		AREA:delay#$colorqualityrtt[0]:\"> 420      ms\" \\
		GPRINT:delay:MIN:\"\t\tMin\\:  %7.2lf ms\" \\
		GPRINT:loss:MIN:\"\tMin\\: %3.1lf %%\\n\" \\
    		AREA:r3#$colorqualityrtt[1]:\"180-420    ms\" \\
		GPRINT:delay:AVERAGE:\"\t\tAvg\\:  %7.2lf ms\" \\
		GPRINT:loss:AVERAGE:\"\tAvg\\: %3.1lf %%\\n\" \\
		AREA:r2#$colorqualityrtt[2]:\"60-180     ms\" \\
		GPRINT:delay:MAX:\"\t\tMax\\:  %7.2lf ms\" \\
		GPRINT:loss:MAX:\"\tMax\\: %3.1lf %%\\n\" \\
		AREA:r1#$colorqualityrtt[3]:\"20-60      ms\\n\" \\
		AREA:r0#$colorqualityrtt[4]:\"< 20       ms\" \\
		GPRINT:delay:LAST:\"\t\tLast\\: %7.2lf ms\" \\
		GPRINT:loss:LAST:\"\tLast\: %3.1lf %%\\n\" \\
		AREA:loss10#$colorqualityloss:\"Packet loss\\n\" \\
		LINE1:delay#$colorqualityrtt[5]:\"Delay average\\n\" \\
		COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\"";
}
elseif((strstr($curdatabase, "spamd.rrd")) && (file_exists("$rrddbpath$curdatabase"))) {
	/* graph a spamd statistics graph */
	$graphcmd = "$rrdtool graph $rrdtmppath$curdatabase-$interval.png ";
	$graphcmd .= "--start -$seconds -e -$average ";
	$graphcmd .= "--title \"`hostname` - {$prettydb} - {$hperiod} - {$havg} average\" ";
	$graphcmd .= "--color SHADEA#eeeeee --color SHADEB#eeeeee ";
	$graphcmd .= "--vertical-label=\"Conn / Time, sec.\" ";
	$graphcmd .= "--height 200 --width 620 --no-gridfit ";
	$graphcmd .= "-x \"$scale\" --lower-limit 0 ";
	$graphcmd .= "DEF:\"consmin=$rrddbpath$curdatabase:conn:MIN\" ";
	$graphcmd .= "DEF:\"consavg=$rrddbpath$curdatabase:conn:AVERAGE\" ";
	$graphcmd .= "DEF:\"consmax=$rrddbpath$curdatabase:conn:MAX\" ";
	$graphcmd .= "DEF:\"timemin=$rrddbpath$curdatabase:time:MIN\" ";
	$graphcmd .= "DEF:\"timeavg=$rrddbpath$curdatabase:time:AVERAGE\" ";
	$graphcmd .= "DEF:\"timemax=$rrddbpath$curdatabase:time:MAX\" ";
	$graphcmd .= "CDEF:\"timeminadj=timemin,0,86400,LIMIT,UN,0,timemin,IF\" ";
	$graphcmd .= "CDEF:\"timeavgadj=timeavg,0,86400,LIMIT,UN,0,timeavg,IF\" ";
	$graphcmd .= "CDEF:\"timemaxadj=timemax,0,86400,LIMIT,UN,0,timemax,IF\" ";
	$graphcmd .= "CDEF:\"t1=timeminadj,timeavgadj,+,2,/,timeminadj,-\" ";
	$graphcmd .= "CDEF:\"t2=timeavgadj,timemaxadj,+,2,/,timeminadj,-,t1,-\" ";
	$graphcmd .= "CDEF:\"t3=timemaxadj,timeminadj,-,t1,-,t2,-\" ";
	$graphcmd .= "AREA:\"timeminadj\" ";
	$graphcmd .= "AREA:\"t1#$colorspamdtime[0]::STACK\" ";
	$graphcmd .= "AREA:\"t2#$colorspamdtime[1]::STACK\" ";
	$graphcmd .= "AREA:\"t3#$colorspamdtime[2]::STACK\" ";
	$graphcmd .= "LINE2:\"timeavgadj#$colorspamdtime[3]:\"Time \" ";
	$graphcmd .= "GPRINT:\"timeminadj:MIN:\"Min\\:%6.2lf\\t\" ";
	$graphcmd .= "GPRINT:\"timeavgadj:AVERAGE:\"Avg\\:%6.2lf\\t\" ";
	$graphcmd .= "GPRINT:\"timemaxadj:MAX:\"Max\\:%6.2lf\\n\" ";
	$graphcmd .= "AREA:\"consmax#$colorspamdconn[0]\" ";
	$graphcmd .= "AREA:\"consmin#$colorspamdconn[1]\" ";
	$graphcmd .= "LINE1:\"consmin#$colorspamdconn[2]\" ";
	$graphcmd .= "LINE1:\"consmax#$colorspamdconn[3]\" ";
	$graphcmd .= "LINE1:\"consavg#$colorspamdconn[4]:\"Cons \" ";
	$graphcmd .= "GPRINT:\"consmin:MIN:\"Min\\:%6.2lf\\t\" ";
	$graphcmd .= "GPRINT:\"consavg:AVERAGE:\"Avg\\:%6.2lf\\t\" ";
	$graphcmd .= "GPRINT:\"consmax:MAX:\"Max\\:%6.2lf\\n\" ";
	$graphcmd .= "COMMENT:\"\t\t\t\t\t\t\t\t\t\t\t\t\t`date +\"%b %d %H\:%M\:%S %Y\"`\" ";
} else {
	$data = false;
	log_error("Sorry we do not have data to graph for $curdatabase");
}

/* check modification time to see if we need to generate image */
if (file_exists("$rrdtmppath$curdatabase-$interval.png")) {
	if((time() - filemtime("$rrdtmppath$curdatabase-$interval.png")) >= 55 ) {
		if($data)
			exec("$graphcmd 2>&1", $graphcmdoutput, $graphcmdreturn);
			$graphcmdoutput = implode(" ", $graphcmdoutput) . $graphcmd;
			flush();
			usleep(500);
	}
} else {
	if($data)
		exec("$graphcmd 2>&1", $graphcmdoutput, $graphcmdreturn);
		$graphcmdoutput = implode(" ", $graphcmdoutput) . $graphcmd;
		flush();
		usleep(500);
}
if(($graphcmdreturn <> 0) || (! $data)) {
	log_error("Failed to create graph with error code $graphcmdreturn, the error is: $graphcmdoutput");
	if(strstr($curdatabase, "queues")) {
		log_error("failed to create graph from $rrddbpath$curdatabase, removing database");
		exec("/bin/rm -f $rrddbpath$curif$queues");
		flush();
		usleep(500);
		enable_rrd_graphing();
	}
	if(strstr($curdatabase, "queuesdrop")) {
		log_error("failed to create graph from $rrddbpath$curdatabase, removing database");
		exec("/bin/rm -f $rrddbpath$curdatabase");
		flush();
		usleep(500);
		enable_rrd_graphing();
	}
	header("Content-type: image/png");
	header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
	header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
	header("Cache-Control: no-store, no-cache, must-revalidate");
	header("Cache-Control: post-check=0, pre-check=0", false);
	header("Pragma: no-cache");
	$file= "/usr/local/www/themes/{$g['theme']}/images/misc/rrd_error.png";
	readfile($file);
} else {
	$file = "$rrdtmppath$curdatabase-$interval.png";
	if(file_exists("$file")) {
		header("Content-type: image/png");
		header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
		header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
		header("Cache-Control: no-store, no-cache, must-revalidate");
		header("Cache-Control: post-check=0, pre-check=0", false);
		header("Pragma: no-cache");
		readfile($file);
	}
}

?>