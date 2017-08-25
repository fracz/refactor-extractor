<?php

// This file is part of Moodle - http://moodle.org/
//
// Moodle is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Moodle is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.

/**
 * This file is used to deliver a branch from the navigation structure
 * in XML format back to a page from an AJAX call
 *
 * @since 2.0
 * @package moodlecore
 * @copyright 2009 Sam Hemelryk
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

/** Include config */
require_once(dirname(__FILE__) . '/../../config.php');
/** Include course lib for its functions */
require_once($CFG->dirroot.'/course/lib.php');
// Start buffer capture so that we can `remove` any errors
ob_start();

// Require elementid (this identifies which HTML element it is being expanded
$elementid = optional_param('elementid', null, PARAM_INT);
// Require id This is the key for whatever branch we want to get
$branchid = optional_param('id', null, PARAM_INT);
// This identifies the type of the branch we want to get
$branchtype = optional_param('type', null, PARAM_INT);
// This identifies the block instance requesting AJAX extension
$instanceid = optional_param('instance', null, PARAM_INT);

// Create a global nav object
$navigation = new limited_global_navigation();
// If set to true then we need to call toggle display
$toggledisplay = false;
if ($instanceid!==null) {
    // Get the db record for the block instance
    $blockrecords = $DB->get_record('block_instances', array('id'=>$instanceid,'blockname'=>'global_navigation_tree'));
    if ($blockrecords!=false) {
            // Instantiate a block_instance object so we can access congif
        $block = block_instance('global_navigation_tree', $blockrecords);
        // Check if the expansion limit config option has been set and isn't the default [everything]
        if (!empty($block->config->expansionlimit) && $block->config->expansionlimit > '0') {
            // Set the expansion limit
            $navigation->expansionlimit = $block->config->expansionlimit;
            $toggledisplay = true;
        }
        if (empty($block->config->showemptybranches) || $block->config->showemptybranches=='no') {
            $navigation->showemptybranches = false;
        }
    }
}

// Create a navigation object to use, we can't guarantee PAGE will be complete
$expandable = $navigation->initialise($branchtype, $branchid);
$converter = new navigation_xml();

if ($toggledisplay) {
    // Toggle display of item types we dont' want to display
    $navigation->toggle_type_display($navigation->expansionlimit);
    $converter->set_expansionceiling($navigation->expansionlimit);
}
// Find the actuall branch we are looking for
$branch = $navigation->find_child($branchid, $branchtype);

// Stop buffering errors at this point
$html = ob_get_contents();
ob_end_clean();

// Check if the buffer contianed anything if it did ERROR!
if (trim($html)!=='') {
    die('Errors were encountered while producing the navigation branch'."\n\n\n".$html);
}
// Check that branch isn't empty... if it is ERROR!
if (empty($branch) || $branch->nodetype !== navigation_node::NODETYPE_BRANCH) {
    die('No further information available for this branch');
}

// Prepare an XML converter for the branch
$converter->set_expandable($expandable);
// Set XML headers
header('Content-type: text/xml');
echo '<?xml version="1.0" encoding="utf-8"?>';
// Convert and output the branch as XML
echo $converter->convert($branch);