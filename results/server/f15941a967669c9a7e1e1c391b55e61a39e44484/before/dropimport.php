<?php
/**
 * Copyright (c) 2012 Georg Ehrke <ownclouddev@georgswebsite.de>
 * This file is licensed under the Affero General Public License version 3 or
 * later.
 * See the COPYING-README file.
 */
$data = $_POST['data'];
$data = explode(',', $data);
$data = end($data);
$data = base64_decode($data);
OCP\JSON::checkLoggedIn();
OCP\App::checkAppEnabled('calendar');
$import = new OC_Calendar_Import($data);
$import->setUserID(OCP\User::getUser());
$import->setTimeZone(OC_Calendar_App::$tz);
$import->disableProgressCache();
if(!$import->isValid()){
	OCP\JSON::error();
	exit;
}
$newid = OC_Calendar_Calendar::addCalendar(OCP\User::getUser(),strip_tags($import->createCalendarName()),'VEVENT,VTODO,VJOURNAL',null,0,$import->createCalendarColor());
$import->setCalendarID($newid);
$import->import();
OCP\JSON::success();