<?php

require_once("library.php");

Core::init();

// if the script is already installed, redirect them to the index page.
if (Core::checkIsInstalled()) {
	header("location: index.php");
	exit;
}

$currentPage = 1;
if (Core::checkSettingsFileExists()) {
	$currentPage = 3;
	if (Settings::getSetting("installationStepComplete_Core") == "yes") {
		$currentPage = 4;
	}
}

$params = array();
$params["randomPassword"] = Utils::generateRandomAlphanumericStr("CVxxCxV");
$params["tablePrefix"]    = Core::getDbTablePrefix();
$params["currentPage"]    = $currentPage;

Utils::displayPage("resources/templates/install.tpl", $params);