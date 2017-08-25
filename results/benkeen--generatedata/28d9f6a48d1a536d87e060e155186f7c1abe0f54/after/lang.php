<?php
session_start();
header("Cache-control: private");
header("Cache-Control: no-cache, must-revalidate");
header("Expires: Sat, 26 Jul 1997 05:00:00 GMT");
header("Content-type: application/x-javascript");
require_once("../library.php");

$lines = array();
while (list($key, $value) = each($L))
{
	$lines[] = "\"$key\":\"" . addslashes($value) . "\"";
}

echo "var L={\n" . implode(",\n", $lines) . "}";