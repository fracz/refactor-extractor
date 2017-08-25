<?php //$Id$

///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.org                                            //
//                                                                       //
// Copyright (C) 1999 onwards Martin Dougiamas  http://dougiamas.com     //
//                                                                       //
// This program is free software; you can redistribute it and/or modify  //
// it under the terms of the GNU General Public License as published by  //
// the Free Software Foundation; either version 2 of the License, or     //
// (at your option) any later version.                                   //
//                                                                       //
// This program is distributed in the hope that it will be useful,       //
// but WITHOUT ANY WARRANTY; without even the implied warranty of        //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //
// GNU General Public License for more details:                          //
//                                                                       //
//          http://www.gnu.org/copyleft/gpl.html                         //
//                                                                       //
///////////////////////////////////////////////////////////////////////////

/**
 * This library includes all the necessary stuff to use blocks on pages in Moodle.
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package pages
 */

define('BLOCK_MOVE_LEFT',   0x01);
define('BLOCK_MOVE_RIGHT',  0x02);
define('BLOCK_MOVE_UP',     0x04);
define('BLOCK_MOVE_DOWN',   0x08);
define('BLOCK_CONFIGURE',   0x10);

define('BLOCK_POS_LEFT',  'l');
define('BLOCK_POS_RIGHT', 'r');

define('BLOCKS_PINNED_TRUE',0);
define('BLOCKS_PINNED_FALSE',1);
define('BLOCKS_PINNED_BOTH',2);

require_once($CFG->libdir.'/pagelib.php');

/**
 * This class keeps track of the block that should appear on a moodle_page.
 * The page to work with as passed to the constructor.
 * The only fields of moodle_page that is uses are ->context, ->pagetype and
 * ->subpage, so instead of passing a full moodle_page object, you may also
 * pass a stdClass object with those three fields. These field values are read
 * only at the point that the load_blocks() method is called. It is the caller's
 * responsibility to ensure that those fields do not subsequently change.
 */
class block_manager {

/// Field declarations =========================================================

    protected $page;

    protected $regions = array();

    protected $defaultregion;

    protected $allblocks = null; // Will be get_records('blocks');

    protected $addableblocks = null; // Will be a subset of $allblocks.

    protected $blocksbyregion = null; // Will be an array region-name => array(block_instances);

/// Constructor ================================================================

    /**
     * Constructor.
     * @param object $page the moodle_page object object we are managing the blocks for,
     * or a reasonable faxilimily. (See the comment at the top of this classe
     * and http://en.wikipedia.org/wiki/Duck_typing)
     */
    public function __construct($page) {
        $this->page = $page;
    }

/// Getter methods =============================================================

    /**
     * @return array the internal names of the regions on this page where block may appear.
     */
    public function get_regions() {
        return array_keys($this->regions);
    }

    /**
     * @return string the internal names of the region where new blocks are added
     * by default, and where any blocks from an unrecognised region are shown.
     * (Imagine that blocks were added with one theme selected, then you switched
     * to a theme with different block positions.)
     */
    public function get_default_region() {
        return $this->defaultregion;
    }

    /**
     * The list of block types that may be added to this page.
     * @return array block id => record from block table.
     */
    public function get_addable_blocks() {
        $this->check_is_loaded();

        if (!is_null($this->addableblocks)) {
            return $this->addableblocks;
        }

        // Lazy load.
        $this->addableblocks = array();

        $allblocks = blocks_get_record();
        if (empty($allblocks)) {
            return $this->addableblocks;
        }

        $pageformat = $page->pagetype;
        foreach($allblocks as $block) {
            if ($block->visible &&
                    ($block->multiple || !$this->is_block_present($block->id)) &&
                    blocks_name_allowed_in_format($block->name, $pageformat)) {
                $this->addableblocks[$block->id] = $block;
            }
        }

        return $this->addableblocks;
    }

    public function is_block_present($blocktypeid) {
        // TODO
    }

    /**
     * @param string $blockname the name of ta type of block.
     * @param boolean $includeinvisible if false (default) only check 'visible' blocks, that is, blocks enabled by the admin.
     * @return boolean true if this block in installed.
     */
    public function is_known_block_type($blockname, $includeinvisible = false) {
        $blocks = $this->get_installed_blocks();
        foreach ($blocks as $block) {
            if ($block->name == $blockname && ($includeinvisible || $block->visible)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param string $region a region name
     * @return boolean true if this retion exists on this page.
     */
    public function is_known_region($region) {
        return array_key_exists($region, $this->regions);
    }

    /**
     * @param $region a block region that exists on this page.
     * @return array of block instances.
     */
    public function get_blocks_for_region($region) {
        $this->check_is_loaded();
        $this->check_region_is_known($region);
        return $this->blocksbyregion[$region];
    }

    /**
     * Get the list of all installed blocks.
     * @return array contents of the block table.
     */
    public function get_installed_blocks() {
        global $DB;
        if (is_null($this->allblocks)) {
            $this->allblocks = $DB->get_records('block');
        }
        return $this->allblocks;
    }

/// Setter methods =============================================================

    /**
     * @param string $region add a named region where blocks may appear on the
     * current page. This is an internal name, like 'side-pre', not a string to
     * display in the UI.
     */
    public function add_region($region) {
        $this->check_not_yet_loaded();
        $this->regions[$region] = 1;
    }

    /**
     * @param array $regions this utility method calls add_region for each array element.
     */
    public function add_regions($regions) {
        foreach ($regions as $region) {
            $this->add_region($region);
        }
    }

    /**
     * @param string $defaultregion the internal names of the region where new
     * blocks should be added by default, and where any blocks from an
     * unrecognised region are shown.
     */
    public function set_default_region($defaultregion) {
        $this->check_not_yet_loaded();
        $this->check_region_is_known($defaultregion);
        $this->defaultregion = $defaultregion;
    }

/// Actions ====================================================================

    /**
     * This method actually loads the blocks for our page from the database.
     */
    public function load_blocks($includeinvisible = NULL) {
        global $DB;
        $this->check_not_yet_loaded();

        if (is_null($includeinvisible)) {
            $includeinvisible = $this->page->user_is_editing();
        }
        if ($includeinvisible) {
            $visiblecheck = 'AND (bp.visible = 1 OR bp.visible IS NULL)';
        } else {
            $visiblecheck = '';
        }

        $context = $this->page->context;
        $contexttest = 'bi.contextid = :contextid2';
        $parentcontextparams = array();
        $parentcontextids = get_parent_contexts($context);
        if ($parentcontextids) {
            list($parentcontexttest, $parentcontextparams) =
                    $DB->get_in_or_equal($parentcontextids, SQL_PARAMS_NAMED, 'parentcontext0000');
            $contexttest = "($contexttest OR (bi.showinsubcontexts = 1 AND bi.contextid $parentcontexttest))";
        }

        $pagetypepatterns = $this->matching_page_type_patterns($this->page->pagetype);
        list($pagetypepatterntest, $pagetypepatternparams) =
                $DB->get_in_or_equal($pagetypepatterns, SQL_PARAMS_NAMED, 'pagetypepatterntest0000');

        $params = array(
            'subpage1' => $this->page->subpage,
            'subpage2' => $this->page->subpage,
            'contextid1' => $context->id,
            'contextid2' => $context->id,
            'pagetype' => $this->page->pagetype,
        );
        $sql = "SELECT
                    bi.id,
                    bi.blockname,
                    bi.contextid,
                    bi.showinsubcontexts,
                    bi.pagetypepattern,
                    bi.subpagepattern,
                    bp.visible,
                    COALESCE(bp.region, bi.defaultregion) AS region,
                    COALESCE(bp.weight, bi.defaultweight) AS weight,
                    bi.configdata

                FROM {block_instances} bi
                JOIN {block} b ON bi.blockname = b.name
                LEFT JOIN {block_positions} bp ON bp.blockinstanceid = bi.id
                                                  AND bp.contextid = :contextid1
                                                  AND bp.pagetype = :pagetype
                                                  AND bp.subpage = :subpage1

                WHERE
                $contexttest
                AND bi.pagetypepattern $pagetypepatterntest
                AND (bi.subpagepattern IS NULL OR bi.subpagepattern = :subpage2)
                $visiblecheck
                AND b.visible = 1

                ORDER BY
                    COALESCE(bp.region, bi.defaultregion),
                    COALESCE(bp.weight, bi.defaultweight),
                    bi.id";
        $blockinstances = $DB->get_recordset_sql($sql, $params + $parentcontextparams + $pagetypepatternparams);

        $this->blocksbyregion = array();
        foreach ($this->regions as $region => $notused) {
            $this->blocksbyregion[$region] = array();
        }
        $unknown = array();

        foreach ($blockinstances as $bi) {
            if ($this->is_known_region($bi->region)) {
                $this->blocksbyregion[$bi->region][] = $bi;
            } else {
                $unknown[] = $bi;
            }
        }
        $this->blocksbyregion[$this->defaultregion] = array_merge($this->blocksbyregion[$this->defaultregion], $unknown);
    }

    /**
     * Add a block to the current page, or related pages. The block is added to
     * context $this->page->contextid. If $pagetypepattern $subpagepattern
     * @param string $blockname The type of block to add.
     * @param string $region the block region on this page to add the block to.
     * @param integer $weight determines the order where this block appears in the region.
     * @param boolean $showinsubcontexts whether this block appears in subcontexts, or just the current context.
     * @param string|null $pagetypepattern which page types this block should appear on. Defaults to just the current page type.
     * @param string|null $subpagepattern which subpage this block should appear on. NULL = any (the default), otherwise only the specified subpage.
     */
    public function add_block($blockname, $region, $weight, $showinsubcontexts, $pagetypepattern = NULL, $subpagepattern = NULL) {
        global $DB;
        $this->check_known_block_type($blockname);
        $this->check_region_is_known($region);

        if (empty($pagetypepattern)) {
            $pagetypepattern = $this->page->pagetype;
        }

        $blockinstance = new stdClass;
        $blockinstance->blockname = $blockname;
        $blockinstance->contextid = $this->page->context->id;
        $blockinstance->showinsubcontexts = !empty($showinsubcontexts);
        $blockinstance->pagetypepattern = $pagetypepattern;
        $blockinstance->subpagepattern = $subpagepattern;
        $blockinstance->defaultregion = $region;
        $blockinstance->defaultweight = $weight;
        $blockinstance->configdata = '';
        $DB->insert_record('block_instances', $blockinstance);
    }

/// Inner workings =============================================================

    /**
     * Given a specific page type, return all the page type patterns that might
     * match it.
     * @param string $pagetype for example 'course-view-weeks' or 'mod-quiz-view'.
     * @return array an array of all the page type patterns that might match this page type.
     */
    protected function matching_page_type_patterns($pagetype) {
        $patterns = array($pagetype, '*');
        $bits = explode('-', $pagetype);
        if (count($bits) == 3 && $bits[0] == 'mod') {
            if ($bits[2] == 'view') {
                $patterns[] = 'mod-*-view';
            } else if ($bits[2] == 'index') {
                $patterns[] = 'mod-*-index';
            }
        }
        while (count($bits) > 0) {
            $patterns[] = implode('-', $bits) . '-*';
            array_pop($bits);
        }
        return $patterns;
    }

    protected function check_not_yet_loaded() {
        if (!is_null($this->blocksbyregion)) {
            throw new coding_exception('block_manager has already loaded the blocks, to it is too late to change things that might affect which blocks are visible.');
        }
    }

    protected function check_is_loaded() {
        if (is_null($this->blocksbyregion)) {
            throw new coding_exception('block_manager has not yet loaded the blocks, to it is too soon to request the information you asked for.');
        }
    }

    protected function check_known_block_type($blockname, $includeinvisible = false) {
        if (!$this->is_known_block_type($blockname, $includeinvisible)) {
            if ($this->is_known_block_type($blockname, true)) {
                throw new coding_exception('Unknown block type ' . $blockname);
            } else {
                throw new coding_exception('Block type ' . $blockname . ' has been disabled by the administrator.');
            }
        }
    }

    protected function check_region_is_known($region) {
        if (!$this->is_known_region($region)) {
            throw new coding_exception('Trying to reference an unknown block region ' . $region);
        }
    }
}

/// Helper functions for working with block classes ============================

/**
 * Call a class method (one that does not requrie a block instance) on a block class.
 * @param string $blockname the name of the block.
 * @param string $method the method name.
 * @param array $param parameters to pass to the method.
 * @return mixed whatever the method returns.
 */
function block_method_result($blockname, $method, $param = NULL) {
    if(!block_load_class($blockname)) {
        return NULL;
    }
    return call_user_func(array('block_'.$blockname, $method), $param);
}

/**
 * Creates a new object of the specified block class.
 * @param string $blockname the name of the block.
 * @param $instance block_instances DB table row (optional).
 * @return block_base the requested block instance.
 */
function block_instance($blockname, $instance = NULL) {
    if(!block_load_class($blockname)) {
        return false;
    }
    $classname = 'block_'.$blockname;
    $retval = new $classname;
    if($instance !== NULL) {
        $retval->_load_instance($instance);
    }
    return $retval;
}

/**
 * Load the block class for a particular type of block.
 * @param string $blockname the name of the block.
 * @return boolean success or failure.
 */
function block_load_class($blockname) {
    global $CFG;

    if(empty($blockname)) {
        return false;
    }

    $classname = 'block_'.$blockname;

    if(class_exists($classname)) {
        return true;
    }

    require_once($CFG->dirroot.'/blocks/moodleblock.class.php');
    @include_once($CFG->dirroot.'/blocks/'.$blockname.'/block_'.$blockname.'.php'); // do not throw errors if block code not present

    return class_exists($classname);
}

/// Functions that have been deprecated by block_manager =======================

/**
 * @deprecated since Moodle 2.0 - use $page->blocks->get
 * This function returns an array with the IDs of any blocks that you can add to your page.
 * Parameters are passed by reference for speed; they are not modified at all.
 * @param $page the page object.
 * @param $pageblocks Not used.
 * @return array of block type ids.
 */
function blocks_get_missing(&$page, &$pageblocks) {
    return array_keys($page->blocks->get_addable_blocks());
}

function blocks_remove_inappropriate($page) {
    $pageblocks = blocks_get_by_page($page);

    if(empty($pageblocks)) {
        return;
    }

    if(($pageformat = $page->pagetype) == NULL) {
        return;
    }

    foreach($pageblocks as $position) {
        foreach($position as $instance) {
            $block = blocks_get_record($instance->blockid);
            if(!blocks_name_allowed_in_format($block->name, $pageformat)) {
               blocks_delete_instance($instance);
            }
        }
    }
}

function blocks_name_allowed_in_format($name, $pageformat) {
    $accept = NULL;
    $maxdepth = -1;
    $formats = block_method_result($name, 'applicable_formats');
    if (!$formats) {
        $formats = array();
    }
    foreach ($formats as $format => $allowed) {
        $formatregex = '/^'.str_replace('*', '[^-]*', $format).'.*$/';
        $depth = substr_count($format, '-');
        if (preg_match($formatregex, $pageformat) && $depth > $maxdepth) {
            $maxdepth = $depth;
            $accept = $allowed;
        }
    }
    if ($accept === NULL) {
        $accept = !empty($formats['all']);
    }
    return $accept;
}

function blocks_delete_instance($instance,$pinned=false) {
    global $DB;

    // Get the block object and call instance_delete() if possible
    if($record = blocks_get_record($instance->blockid)) {
        if($obj = block_instance($record->name, $instance)) {
            // Return value ignored
            $obj->instance_delete();
        }
    }

    if (!empty($pinned)) {
         $DB->delete_records('block_pinned_old', array('id'=>$instance->id));
        // And now, decrement the weight of all blocks after this one
        $sql = "UPDATE {block_pinned_old}
                   SET weight = weight - 1
                 WHERE pagetype = ? AND position = ? AND weight > ?";
        $params = array($instance->pagetype, $instance->position, $instance->weight);
        $DB->execute($sql, $params);
    } else {
        // Now kill the db record;
        $DB->delete_records('block_instance_old', array('oldid'=>$instance->id));
        delete_context(CONTEXT_BLOCK, $instance->id);
        // And now, decrement the weight of all blocks after this one
        $sql = "UPDATE {block_instance_old}
                   SET weight = weight - 1
                 WHERE pagetype = ? AND pageid = ?
                       AND position = ? AND weight > ?";
        $params = array($instance->pagetype, $instance->pageid, $instance->position, $instance->weight);
        $DB->execute($sql, $params);
    }
    return true;
}

// Accepts an array of block instances and checks to see if any of them have content to display
// (causing them to calculate their content in the process). Returns true or false. Parameter passed
// by reference for speed; the array is actually not modified.
function blocks_have_content(&$pageblocks, $position) {

    if (empty($pageblocks) || !is_array($pageblocks) || !array_key_exists($position,$pageblocks)) {
        return false;
    }
    // use a for() loop to get references to the array elements
    // foreach() cannot fetch references in PHP v4.x
    for ($n=0; $n<count($pageblocks[$position]);$n++) {
        $instance = &$pageblocks[$position][$n];
        if (empty($instance->visible)) {
            continue;
        }
        if(!$record = blocks_get_record($instance->blockid)) {
            continue;
        }
        if(!$obj = block_instance($record->name, $instance)) {
            continue;
        }
        if(!$obj->is_empty()) {
            // cache rec and obj
            // for blocks_print_group()
            $instance->rec = $record;
            $instance->obj = $obj;
            return true;
        }
    }

    return false;
}

// This function prints one group of blocks in a page
// Parameters passed by reference for speed; they are not modified.
function blocks_print_group(&$page, &$pageblocks, $position) {
    global $COURSE, $CFG, $USER;

    if (empty($pageblocks[$position])) {
        $groupblocks = array();
        $maxweight = 0;
    } else {
        $groupblocks = $pageblocks[$position];
        $maxweight = max(array_keys($groupblocks));
    }


    foreach ($groupblocks as $instance) {
        if (!empty($instance->pinned)) {
            $maxweight--;
        }
    }

    $isediting = $page->user_is_editing();


    foreach($groupblocks as $instance) {


        // $instance may have ->rec and ->obj
        // cached from when we walked $pageblocks
        // in blocks_have_content()
        if (empty($instance->rec)) {
            if (empty($instance->blockid)) {
                continue;   // Can't do anything
            }
            $block = blocks_get_record($instance->blockid);
        } else {
            $block = $instance->rec;
        }

        if (empty($block)) {
            // Block doesn't exist! We should delete this instance!
            continue;
        }

        if (empty($block->visible)) {
            // Disabled by the admin
            continue;
        }

        if (empty($instance->obj)) {
            if (!$obj = block_instance($block->name, $instance)) {
                // Invalid block
                continue;
            }
        } else {
            $obj = $instance->obj;
        }

        $editalways = false;


        if (($isediting  && empty($instance->pinned)) || !empty($editalways)) {
            $options = 0;
            // The block can be moved up if it's NOT the first one in its position. If it is, we look at the OR clause:
            // the first block might still be able to move up if the page says so (i.e., it will change position)
            $options |= BLOCK_MOVE_UP    * ($instance->weight != 0          || ($page->blocks_move_position($instance, BLOCK_MOVE_UP)   != $instance->position));
            // Same thing for downward movement
            $options |= BLOCK_MOVE_DOWN  * ($instance->weight != $maxweight || ($page->blocks_move_position($instance, BLOCK_MOVE_DOWN) != $instance->position));
            // For left and right movements, it's up to the page to tell us whether they are allowed
            $options |= BLOCK_MOVE_RIGHT * ($page->blocks_move_position($instance, BLOCK_MOVE_RIGHT) != $instance->position);
            $options |= BLOCK_MOVE_LEFT  * ($page->blocks_move_position($instance, BLOCK_MOVE_LEFT ) != $instance->position);
            // Finally, the block can be configured if the block class either allows multiple instances, or if it specifically
            // allows instance configuration (multiple instances override that one). It doesn't have anything to do with what the
            // administrator has allowed for this block in the site admin options.
            $options |= BLOCK_CONFIGURE * ( $obj->instance_allow_multiple() || $obj->instance_allow_config() );
            $obj->_add_edit_controls($options);
        }

        if (!$instance->visible && empty($COURSE->javascriptportal)) {
            if ($isediting) {
                $obj->_print_shadow();
            }
        } else {
            global $COURSE;
            if(!empty($COURSE->javascriptportal)) {
                 $COURSE->javascriptportal->currentblocksection = $position;
            }
            $obj->_print_block();
        }
        if (!empty($COURSE->javascriptportal)
                    && (empty($instance->pinned) || !$instance->pinned)) {
            $COURSE->javascriptportal->block_add('inst'.$instance->id, !$instance->visible);
        }
    } // End foreach

    //  Check if
    //    we are on the default position/side AND
    //    we're editing the page AND
    //    (
    //      we have the capability to manage blocks OR
    //      we are in myMoodle page AND have the capibility to manage myMoodle blocks
    //    )

    // for constant PAGE_MY_MOODLE
    include_once($CFG->dirroot.'/my/pagelib.php');

    $coursecontext = get_context_instance(CONTEXT_COURSE, $COURSE->id);
    $myownblogpage = (isset($page->filtertype) && isset($page->filterselect) && $page->pagetype=='blog-view' && $page->filtertype=='user' && $page->filterselect == $USER->id);

    $managecourseblocks = has_capability('moodle/site:manageblocks', $coursecontext);
    $editmymoodle = $page->pagetype == PAGE_MY_MOODLE && has_capability('moodle/my:manageblocks', $coursecontext);

    if ($page->blocks->get_default_region() == $position &&
        $page->user_is_editing() &&
        ($managecourseblocks || $editmymoodle || $myownblogpage || defined('ADMIN_STICKYBLOCKS'))) {

        blocks_print_adminblock($page, $pageblocks);
    }
}

// This iterates over an array of blocks and calculates the preferred width
// Parameter passed by reference for speed; it's not modified.
function blocks_preferred_width(&$instances) {
    $width = 0;

    if(empty($instances) || !is_array($instances)) {
        return 0;
    }

    $blocks = blocks_get_record();

    foreach($instances as $instance) {
        if(!$instance->visible) {
            continue;
        }

        if (!array_key_exists($instance->blockid, $blocks)) {
            // Block doesn't exist! We should delete this instance!
            continue;
        }

        if(!$blocks[$instance->blockid]->visible) {
            continue;
        }
        $pref = block_method_result($blocks[$instance->blockid]->name, 'preferred_width');
        if($pref === NULL) {
            continue;
        }
        if($pref > $width) {
            $width = $pref;
        }
    }
    return $width;
}

/**
 * Get the block record for a particulr blockid.
 * @param $blockid block type id. If null, an array of all block types is returned.
 * @param $notusedanymore No longer used.
 * @return array|object row from block table, or all rows.
 */
function blocks_get_record($blockid = NULL, $notusedanymore = false) {
    global $PAGE;
    $blocks = $PAGE->blocks->get_installed_blocks();
    if ($blockid === NULL) {
        return $blocks;
    } else if (isset($blocks[$blockid])) {
        return $blocks[$blockid];
    } else {
        return false;
    }
}

function blocks_find_block($blockid, $blocksarray) {
    if (empty($blocksarray)) {
        return false;
    }
    foreach($blocksarray as $blockgroup) {
        if (empty($blockgroup)) {
            continue;
        }
        foreach($blockgroup as $instance) {
            if($instance->blockid == $blockid) {
                return $instance;
            }
        }
    }
    return false;
}

function blocks_find_instance($instanceid, $blocksarray) {
    foreach($blocksarray as $subarray) {
        foreach($subarray as $instance) {
            if($instance->id == $instanceid) {
                return $instance;
            }
        }
    }
    return false;
}

// Simple entry point for anyone that wants to use blocks
function blocks_setup(&$PAGE,$pinned=BLOCKS_PINNED_FALSE) {
    switch ($pinned) {
    case BLOCKS_PINNED_TRUE:
        $pageblocks = blocks_get_pinned($PAGE);
        break;
    case BLOCKS_PINNED_BOTH:
        $pageblocks = blocks_get_by_page_pinned($PAGE);
        break;
    case BLOCKS_PINNED_FALSE:
    default:
        $pageblocks = blocks_get_by_page($PAGE);
        break;
    }
    blocks_execute_url_action($PAGE, $pageblocks,($pinned==BLOCKS_PINNED_TRUE));
    return $pageblocks;
}

function blocks_execute_action($page, &$pageblocks, $blockaction, $instanceorid, $pinned=false, $redirect=true) {
    global $CFG, $USER, $DB;

    if (is_int($instanceorid)) {
        $blockid = $instanceorid;
    } else if (is_object($instanceorid)) {
        $instance = $instanceorid;
    }

    switch($blockaction) {
        case 'config':
            $block = blocks_get_record($instance->blockid);
            // Hacky hacky tricky stuff to get the original human readable block title,
            // even if the block has configured its title to be something else.
            // Create the object WITHOUT instance data.
            $blockobject = block_instance($block->name);
            if ($blockobject === false) {
                break;
            }

            // First of all check to see if the block wants to be edited
            if(!$blockobject->user_can_edit()) {
                break;
            }

            // Now get the title and AFTER that load up the instance
            $blocktitle = $blockobject->get_title();
            $blockobject->_load_instance($instance);

            // Define the data we're going to silently include in the instance config form here,
            // so we can strip them from the submitted data BEFORE serializing it.
            $hiddendata = array(
                'sesskey' => sesskey(),
                'instanceid' => $instance->id,
                'blockaction' => 'config'
            );

            // To this data, add anything the page itself needs to display
            $hiddendata = $page->url->params($hiddendata);

            if ($data = data_submitted()) {
                $remove = array_keys($hiddendata);
                foreach($remove as $item) {
                    unset($data->$item);
                }
                if(!$blockobject->instance_config_save($data, $pinned)) {
                    print_error('cannotsaveblock');
                }
                // And nothing more, continue with displaying the page
            }
            else {
                // We need to show the config screen, so we highjack the display logic and then die
                $strheading = get_string('blockconfiga', 'moodle', $blocktitle);
                $page->print_header(get_string('pageheaderconfigablock', 'moodle'), array($strheading => ''));

                echo '<div class="block-config" id="'.$block->name.'">';   /// Make CSS easier

                print_heading($strheading);
                echo '<form method="post" name="block-config" action="'. $page->url->out(false) .'">';
                echo '<p>';
                foreach($hiddendata as $name => $val) {
                    echo '<input type="hidden" name="'. $name .'" value="'. $val .'" />';
                }
                echo '</p>';
                $blockobject->instance_config_print();
                echo '</form>';

                echo '</div>';
                $PAGE->set_pagetype('blocks-' . $block->name);
                print_footer();
                die(); // Do not go on with the other page-related stuff
            }
        break;
        case 'toggle':
            if(empty($instance))  {
                print_error('invalidblockinstance', '', '', $blockaction);
            }
            $instance->visible = ($instance->visible) ? 0 : 1;
            if (!empty($pinned)) {
                $DB->update_record('block_pinned_old', $instance);
            } else {
                $DB->update_record('block_instance_old', $instance);
            }
        break;
        case 'delete':
            if(empty($instance))  {
                print_error('invalidblockinstance', '', '', $blockaction);
            }
            blocks_delete_instance($instance, $pinned);
        break;
        case 'moveup':
            if(empty($instance))  {
                print_error('invalidblockinstance', '', '', $blockaction);
            }

            if($instance->weight == 0) {
                // The block is the first one, so a move "up" probably means it changes position
                // Where is the instance going to be moved?
                $newpos = $page->blocks_move_position($instance, BLOCK_MOVE_UP);
                $newweight = (empty($pageblocks[$newpos]) ? 0 : max(array_keys($pageblocks[$newpos])) + 1);

                blocks_execute_repositioning($instance, $newpos, $newweight, $pinned);
            }
            else {
                // The block is just moving upwards in the same position.
                // This configuration will make sure that even if somehow the weights
                // become not continuous, block move operations will eventually bring
                // the situation back to normal without printing any warnings.
                if(!empty($pageblocks[$instance->position][$instance->weight - 1])) {
                    $other = $pageblocks[$instance->position][$instance->weight - 1];
                }
                if(!empty($other)) {
                    ++$other->weight;
                    if (!empty($pinned)) {
                        $DB->update_record('block_pinned_old', $other);
                    } else {
                        $DB->update_record('block_instance_old', $other);
                    }
                }
                --$instance->weight;
                if (!empty($pinned)) {
                    $DB->update_record('block_pinned_old', $instance);
                } else {
                    $DB->update_record('block_instance_old', $instance);
                }
            }
        break;
        case 'movedown':
            if(empty($instance))  {
                print_error('invalidblockinstance', '', '', $blockaction);
            }

            if($instance->weight == max(array_keys($pageblocks[$instance->position]))) {
                // The block is the last one, so a move "down" probably means it changes position
                // Where is the instance going to be moved?
                $newpos = $page->blocks_move_position($instance, BLOCK_MOVE_DOWN);
                $newweight = (empty($pageblocks[$newpos]) ? 0 : max(array_keys($pageblocks[$newpos])) + 1);

                blocks_execute_repositioning($instance, $newpos, $newweight, $pinned);
            }
            else {
                // The block is just moving downwards in the same position.
                // This configuration will make sure that even if somehow the weights
                // become not continuous, block move operations will eventually bring
                // the situation back to normal without printing any warnings.
                if(!empty($pageblocks[$instance->position][$instance->weight + 1])) {
                    $other = $pageblocks[$instance->position][$instance->weight + 1];
                }
                if(!empty($other)) {
                    --$other->weight;
                    if (!empty($pinned)) {
                        $DB->update_record('block_pinned_old', $other);
                    } else {
                        $DB->update_record('block_instance_old', $other);
                    }
                }
                ++$instance->weight;
                if (!empty($pinned)) {
                    $DB->update_record('block_pinned_old', $instance);
                } else {
                    $DB->update_record('block_instance_old', $instance);
                }
            }
        break;
        case 'moveleft':
            if(empty($instance))  {
                print_error('invalidblockinstance', '', '', $blockaction);
            }

            // Where is the instance going to be moved?
            $newpos = $page->blocks_move_position($instance, BLOCK_MOVE_LEFT);
            $newweight = (empty($pageblocks[$newpos]) ? 0 : max(array_keys($pageblocks[$newpos])) + 1);

            blocks_execute_repositioning($instance, $newpos, $newweight, $pinned);
        break;
        case 'moveright':
            if(empty($instance))  {
                print_error('invalidblockinstance', '', '', $blockaction);
            }

            // Where is the instance going to be moved?
            $newpos    = $page->blocks_move_position($instance, BLOCK_MOVE_RIGHT);
            $newweight = (empty($pageblocks[$newpos]) ? 0 : max(array_keys($pageblocks[$newpos])) + 1);

            blocks_execute_repositioning($instance, $newpos, $newweight, $pinned);
        break;
        case 'add':
            // Add a new instance of this block, if allowed
            $block = blocks_get_record($blockid);

            if(empty($block) || !$block->visible) {
                // Only allow adding if the block exists and is enabled
                break;
            }

            if(!$block->multiple && blocks_find_block($blockid, $pageblocks) !== false) {
                // If no multiples are allowed and we already have one, return now
                break;
            }

            if(!block_method_result($block->name, 'user_can_addto', $page)) {
                // If the block doesn't want to be added...
                break;
            }

            $newpos = $page->blocks->get_default_region();
            if (!empty($pinned)) {
                $sql = "SELECT 1, MAX(weight) + 1 AS nextfree
                          FROM {block_pinned_old}
                         WHERE pagetype = ? AND position = ?";
                $params = array($page->pagetype, $newpos);

            } else {
                $sql = "SELECT 1, MAX(weight) + 1 AS nextfree
                          FROM {block_instance_old}
                         WHERE pageid = ? AND pagetype = ? AND position = ?";
                $params = array($page->get_id(), $page->pagetype, $newpos);
            }
            $weight = $DB->get_record_sql($sql, $params);

            $newinstance = new stdClass;
            $newinstance->blockid    = $blockid;
            if (empty($pinned)) {
                $newinstance->pageid = $page->get_id();
            }
            $newinstance->pagetype   = $page->pagetype;
            $newinstance->position   = $newpos;
            $newinstance->weight     = empty($weight->nextfree) ? 0 : $weight->nextfree;
            $newinstance->visible    = 1;
            $newinstance->configdata = '';
            if (!empty($pinned)) {
                $newinstance->id = $DB->insert_record('block_pinned_old', $newinstance);
            } else {
                $newinstance->id = $DB->insert_record('block_instance_old', $newinstance);
            }

            // If the new instance was created, allow it to do additional setup
            if($newinstance && ($obj = block_instance($block->name, $newinstance))) {
                // Return value ignored
                $obj->instance_create();
            }

        break;
    }

    if ($redirect) {
        // In order to prevent accidental duplicate actions, redirect to a page with a clean url
        redirect($page->url->out());
    }
}

// You can use this to get the blocks to respond to URL actions without much hassle
function blocks_execute_url_action(&$PAGE, &$pageblocks,$pinned=false) {
    $blockaction = optional_param('blockaction', '', PARAM_ALPHA);

    if (empty($blockaction) || !$PAGE->user_allowed_editing() || !confirm_sesskey()) {
        return;
    }

    $instanceid  = optional_param('instanceid', 0, PARAM_INT);
    $blockid     = optional_param('blockid',    0, PARAM_INT);

    if (!empty($blockid)) {
        blocks_execute_action($PAGE, $pageblocks, strtolower($blockaction), $blockid, $pinned);

    }
    else if (!empty($instanceid)) {
        $instance = blocks_find_instance($instanceid, $pageblocks);
        blocks_execute_action($PAGE, $pageblocks, strtolower($blockaction), $instance, $pinned);
    }
}

// This shouldn't be used externally at all, it's here for use by blocks_execute_action()
// in order to reduce code repetition.
function blocks_execute_repositioning(&$instance, $newpos, $newweight, $pinned=false) {
    global $DB;

    // If it's staying where it is, don't do anything, unless overridden
    if ($newpos == $instance->position) {
        return;
    }

    // Close the weight gap we 'll leave behind
    if (!empty($pinned)) {
        $sql = "UPDATE {block_instance_old}
                   SET weight = weight - 1
                 WHERE pagetype = ? AND position = ? AND weight > ?";
        $params = array($instance->pagetype, $instance->position, $instance->weight);

    } else {
        $sql = "UPDATE {block_instance_old}
                   SET weight = weight - 1
                 WHERE pagetype = ? AND pageid = ?
                       AND position = ? AND weight > ?";
        $params = array($instance->pagetype, $instance->pageid, $instance->position, $instance->weight);
    }
    $DB->execute($sql, $params);

    $instance->position = $newpos;
    $instance->weight   = $newweight;

    if (!empty($pinned)) {
        $DB->update_record('block_pinned_old', $instance);
    } else {
        $DB->update_record('block_instance_old', $instance);
    }
}


/**
 * Moves a block to the new position (column) and weight (sort order).
 * @param $instance - The block instance to be moved.
 * @param $destpos - BLOCK_POS_LEFT or BLOCK_POS_RIGHT. The destination column.
 * @param $destweight - The destination sort order. If NULL, we add to the end
 *                      of the destination column.
 * @param $pinned - Are we moving pinned blocks? We can only move pinned blocks
 *                  to a new position withing the pinned list. Likewise, we
 *                  can only moved non-pinned blocks to a new position within
 *                  the non-pinned list.
 * @return boolean (success or failure).
 */
function blocks_move_block($page, &$instance, $destpos, $destweight=NULL, $pinned=false) {
    global $CFG, $DB;

    if ($pinned) {
        $blocklist = blocks_get_pinned($page);
    } else {
        $blocklist = blocks_get_by_page($page);
    }

    if ($blocklist[$instance->position][$instance->weight]->id != $instance->id) {
        // The source block instance is not where we think it is.
        return false;
    }

    // First we close the gap that will be left behind when we take out the
    // block from it's current column.
    if ($pinned) {
        $closegapsql = "UPDATE {block_instance_old}
                           SET weight = weight - 1
                         WHERE weight > ? AND position = ? AND pagetype = ?";
        $params = array($instance->weight, $instance->position, $instance->pagetype);
    } else {
        $closegapsql = "UPDATE {block_instance_old}
                           SET weight = weight - 1
                         WHERE weight > ? AND position = ?
                               AND pagetype = ? AND pageid = ?";
        $params = array($instance->weight, $instance->position, $instance->pagetype, $instance->pageid);
    }
    if (!$DB->execute($closegapsql, $params)) {
        return false;
    }

    // Now let's make space for the block being moved.
    if ($pinned) {
        $opengapsql = "UPDATE {block_instance_old}
                           SET weight = weight + 1
                         WHERE weight >= ? AND position = ? AND pagetype = ?";
        $params = array($destweight, $destpos, $instance->pagetype);
    } else {
        $opengapsql = "UPDATE {block_instance_old}
                          SET weight = weight + 1
                        WHERE weight >= ? AND position = ?
                              AND pagetype = ? AND pageid = ?";
        $params = array($destweight, $destpos, $instance->pagetype, $instance->pageid);
    }
    if (!$DB->execute($opengapsql, $params)) {
        return false;
    }

    // Move the block.
    $instance->position = $destpos;
    $instance->weight   = $destweight;

    if ($pinned) {
        $table = 'block_pinned_old';
    } else {
        $table = 'block_instance_old';
    }
    return $DB->update_record($table, $instance);
}


/**
 * Returns an array consisting of 2 arrays:
 * 1) Array of pinned blocks for position BLOCK_POS_LEFT
 * 2) Array of pinned blocks for position BLOCK_POS_RIGHT
 */
function blocks_get_pinned($page) {
    global $DB;

    $visible = true;
    $select = "pagetype = ?";
    $params = array($page->pagetype);

     if ($visible) {
        $select .= " AND visible = 1";
     }

    $blocks = $DB->get_records_select('block_pinned_old', $select, $params, 'position, weight');

    $positions = $page->blocks->get_regions();
    $arr = array();

    foreach($positions as $key => $position) {
        $arr[$position] = array();
    }

    if(empty($blocks)) {
        return $arr;
    }

    foreach($blocks as $block) {
        $block->pinned = true; // so we know we can't move it.
        // make up an instanceid if we can..
        $block->pageid = $page->get_id();
        $arr[$block->position][$block->weight] = $block;
    }

    return $arr;
}


/**
 * Similar to blocks_get_by_page(), except that, the array returned includes
 * pinned blocks as well. Pinned blocks are always appended before normal
 * block instances.
 */
function blocks_get_by_page_pinned($page) {
    $pinned = blocks_get_pinned($page);
    $user = blocks_get_by_page($page);

    $weights = array();

    foreach ($pinned as $pos => $arr) {
        $weights[$pos] = count($arr);
    }

    foreach ($user as $pos => $blocks) {
        if (!array_key_exists($pos,$pinned)) {
             $pinned[$pos] = array();
        }
        if (!array_key_exists($pos,$weights)) {
            $weights[$pos] = 0;
        }
        foreach ($blocks as $block) {
            $pinned[$pos][$weights[$pos]] = $block;
            $weights[$pos]++;
        }
    }
    return $pinned;
}


/**
 * Returns an array of blocks for the page. Pinned blocks are excluded.
 */
function blocks_get_by_page($page) {
    global $DB;

    $blocks = $DB->get_records_select('block_instance_old', "pageid = ? AND ? LIKE (" . $DB->sql_concat('pagetype', "'%'") . ")",
            array($page->get_id(), $page->pagetype), 'position, weight');

    $positions = $page->blocks->get_regions();
    $arr = array();
    foreach($positions as $key => $position) {
        $arr[$position] = array();
    }

    if(empty($blocks)) {
        return $arr;
    }

    foreach($blocks as $block) {
        $arr[$block->position][$block->weight] = $block;
    }
    return $arr;
}


//This function prints the block to admin blocks as necessary
function blocks_print_adminblock(&$page, &$pageblocks) {
    global $USER;

    $missingblocks = array_keys($page->blocks->get_addable_blocks());

    if (!empty($missingblocks)) {
        $strblocks = '<div class="title"><h2>';
        $strblocks .= get_string('blocks');
        $strblocks .= '</h2></div>';
        $stradd    = get_string('add');
        foreach ($missingblocks as $blockid) {
            $block = blocks_get_record($blockid);
            $blockobject = block_instance($block->name);
            if ($blockobject === false) {
                continue;
            }
            if(!$blockobject->user_can_addto($page)) {
                continue;
            }
            $menu[$block->id] = $blockobject->get_title();
        }
        asort($menu);

        $target = $page->url->out(array('sesskey' => sesskey(), 'blockaction' => 'add'));
        $content = popup_form($target.'&amp;blockid=', $menu, 'add_block', '', $stradd .'...', '', '', true);
        print_side_block($strblocks, $content, NULL, NULL, NULL, array('class' => 'block_adminblock'));
    }
}

/**
 * Delete all the blocks from a particular page.
 *
 * @param string $pagetype the page type.
 * @param integer $pageid the page id.
 * @return success of failure.
 */
function blocks_delete_all_on_page($pagetype, $pageid) {
    global $DB;
    if ($instances = $DB->get_records('block_instance_old', array('pageid' => $pageid, 'pagetype' => $pagetype))) {
        foreach ($instances as $instance) {
            delete_context(CONTEXT_BLOCK, $instance->id); // Ingore any failures here.
        }
    }
    return $DB->delete_records('block_instance_old', array('pageid' => $pageid, 'pagetype' => $pagetype));
}

// Dispite what this function is called, it seems to be mostly used to populate
// the default blocks when a new course (or whatever) is created.
function blocks_repopulate_page($page) {
    global $CFG, $DB;

    $allblocks = blocks_get_record();

    if(empty($allblocks)) {
        print_error('cannotgetblock');
    }

    // Assemble the information to correlate block names to ids
    $idforname = array();
    foreach($allblocks as $block) {
        $idforname[$block->name] = $block->id;
    }

    /// If the site override has been defined, it is the only valid one.
    if (!empty($CFG->defaultblocks_override)) {
        $blocknames = $CFG->defaultblocks_override;
    }
    else {
        $blocknames = $page->blocks_get_default();
    }

    $positions = $page->blocks->get_regions();
    $posblocks = explode(':', $blocknames);

    // Now one array holds the names of the positions, and the other one holds the blocks
    // that are going to go in each position. Luckily for us, both arrays are numerically
    // indexed and the indexes match, so we can work straight away... but CAREFULLY!

    // Ready to start creating block instances, but first drop any existing ones
    blocks_delete_all_on_page($page->pagetype, $page->get_id());

    // Here we slyly count $posblocks and NOT $positions. This can actually make a difference
    // if the textual representation has undefined slots in the end. So we only work with as many
    // positions were retrieved, not with all the page says it has available.
    $numpositions = count($posblocks);
    for($i = 0; $i < $numpositions; ++$i) {
        $position = $positions[$i];
        $blocknames = explode(',', $posblocks[$i]);
        $weight = 0;
        foreach($blocknames as $blockname) {
            $newinstance = new stdClass;
            $newinstance->blockid    = $idforname[$blockname];
            $newinstance->pageid     = $page->get_id();
            $newinstance->pagetype   = $page->pagetype;
            $newinstance->position   = $position;
            $newinstance->weight     = $weight;
            $newinstance->visible    = 1;
            $newinstance->configdata = '';

            if(!empty($newinstance->blockid)) {
                // Only add block if it was recognized
                $DB->insert_record('block_instance_old', $newinstance);
                ++$weight;
            }
        }
    }

    return true;
}

?>