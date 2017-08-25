<?php

/**
 * library.php
 * -----------
 *
 * The main settings & code for the script. All your custom environment settings like your database
 * connection info, are stored in a settings.php file in this same folder. That file is created
 * automatically by the installation script.
 */

require_once(dirname(__FILE__) . "/classes/Account.class.php");
require_once(dirname(__FILE__) . "/classes/AjaxRequest.class.php");
require_once(dirname(__FILE__) . "/classes/Core.class.php");
require_once(dirname(__FILE__) . "/classes/DataType.abstract.class.php");
require_once(dirname(__FILE__) . "/classes/Database.class.php");
require_once(dirname(__FILE__) . "/classes/DataTypes.class.php");
require_once(dirname(__FILE__) . "/classes/ExportTypes.class.php");
require_once(dirname(__FILE__) . "/classes/Installation.class.php");
require_once(dirname(__FILE__) . "/classes/Language.class.php");
require_once(dirname(__FILE__) . "/classes/SessionManager.class.php");
require_once(dirname(__FILE__) . "/classes/Translations.class.php");
require_once(dirname(__FILE__) . "/classes/Utils.class.php");
require_once(dirname(__FILE__) . "/smarty/Smarty.class.php");

Core::init();