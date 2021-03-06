<?php
/**
 * Moodle - Modular Object-Oriented Dynamic Learning Environment
 *          http://moodle.org
 * Copyright (C) 1999 onwards Martin Dougiamas  http://dougiamas.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @package    moodle
 * @subpackage portfolio
 * @author     Penny Leach <penny@catalyst.net.nz>
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL
 * @copyright  (C) 1999 onwards Martin Dougiamas  http://dougiamas.com
 *
 * This file contains all the portfolio exception classes.
 */

/**
* top level portfolio exception.
* sometimes caught and rethrown as {@see portfolio_export_exception}
*/
class portfolio_exception extends moodle_exception {}

/**
* exception to throw during an export - will clean up session and tempdata
*/
class portfolio_export_exception extends portfolio_exception {

    /**
    * constructor.
    * @param object $exporter instance of portfolio_exporter (will handle null case)
    * @param string $errorcode language string key
    * @param string $module language string module (optional, defaults to moodle)
    * @param string $continue url to continue to (optional, defaults to wwwroot)
    * @param mixed $a language string data (optional, defaults to  null)
    */
    public function __construct($exporter, $errorcode, $module=null, $continue=null, $a=null) {
        if (!empty($exporter) && $exporter instanceof portfolio_exporter) {
            if (empty($continue)) {
                $caller = $exporter->get('caller');
                if (!empty($caller) && $caller instanceof portfolio_caller_base) {
                    $continue = $exporter->get('caller')->get_return_url();
                }
            }
            if (!defined('FULLME') || FULLME != 'cron') {
                $exporter->process_stage_cleanup();
            }
        } else {
            global $SESSION;
            if (!empty($SESSION->portfolioexport)) {
                debugging(get_string('exportexceptionnoexporter', 'portfolio'));
            }
        }
        parent::__construct($errorcode, $module, $continue, $a);
    }
}

/**
* exception for callers to throw when they have a problem.
* usually caught and rethrown as {@see portfolio_export_exception}
*/
class portfolio_caller_exception extends portfolio_exception {}

/**
* exception for portfolio plugins to throw when they have a problem.
* usually caught and rethrown as {@see portfolio_export_exception}
*/
class portfolio_plugin_exception extends portfolio_exception {}

/**
* exception for interacting with the button class
*/
class portfolio_button_exception extends portfolio_exception {}
?>