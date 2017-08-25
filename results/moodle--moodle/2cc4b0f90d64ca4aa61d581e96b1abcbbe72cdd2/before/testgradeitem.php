<?php // $Id$

///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.org                                            //
//                                                                       //
// Copyright (C) 1999-2004  Martin Dougiamas  http://dougiamas.com       //
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
 * Unit tests for grade_item object.
 *
 * @author nicolas@moodle.com
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package moodlecore
 */

if (!defined('MOODLE_INTERNAL')) {
    die('Direct access to this script is forbidden.');    ///  It must be included from a Moodle page
}

require_once($CFG->libdir.'/simpletest/fixtures/gradetest.php');

@set_time_limit(0);

class grade_item_test extends grade_test {

    function test_grade_item_construct() {
        $params = new stdClass();

        $params->courseid = $this->courseid;
        $params->categoryid = $this->grade_categories[1]->id;
        $params->itemname = 'unittestgradeitem4';
        $params->itemtype = 'mod';
        $params->itemmodule = 'database';
        $params->iteminfo = 'Grade item used for unit testing';

        $grade_item = new grade_item($params, false);

        $this->assertEqual($params->courseid, $grade_item->courseid);
        $this->assertEqual($params->categoryid, $grade_item->categoryid);
        $this->assertEqual($params->itemmodule, $grade_item->itemmodule);
    }

    function test_grade_item_insert() {
        $grade_item = new grade_item();
        $this->assertTrue(method_exists($grade_item, 'insert'));

        $grade_item->courseid = $this->courseid;
        $grade_item->categoryid = $this->grade_categories[1]->id;
        $grade_item->itemname = 'unittestgradeitem4';
        $grade_item->itemtype = 'mod';
        $grade_item->itemmodule = 'quiz';
        $grade_item->iteminfo = 'Grade item used for unit testing';

        // Check the grade_category's needsupdate variable first
        $category = $grade_item->get_category();
        $category->load_grade_item();
        $category->grade_item->needsupdate = false;
        $this->assertNotNull($category->grade_item);

        $grade_item->insert();

        // Now check the needsupdate variable, it should have been set to true
        $category->grade_item->update_from_db();
        $this->assertTrue($category->grade_item->needsupdate);

        $last_grade_item = end($this->grade_items);

        $this->assertEqual($grade_item->id, $last_grade_item->id + 1);
        $this->assertEqual(11, $grade_item->sortorder);
    }

    function test_grade_item_generate_itemnumber() {
        $grade_item = new grade_item($this->grade_items[0]);
        $copy_grade_item = fullclone($grade_item);
        $copy_grade_item->itemnumber = null;
        unset($copy_grade_item->id);
        $result_id = $copy_grade_item->insert();
        $this->assertEqual($grade_item->itemnumber+1, $copy_grade_item->itemnumber);

    }

    function test_grade_item_generate_idnumber() {

    }

    function test_grade_item_update_when_flagged_as_deleted() {

    }

    function test_grade_item_update_guess_outcomeid() {

    }

    function test_grade_item_update_default_gradetype() {

    }

    function test_grade_item_update_guess_scaleid() {

    }

    function test_grade_item_delete() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'delete'));

        // Check the grade_category's needsupdate variable first
        $category = $grade_item->get_category();
        $category->load_grade_item();
        $this->assertNotNull($category->grade_item);
        $category->grade_item->needsupdate = false;

        $this->assertTrue($grade_item->delete());

        // Now check the needsupdate variable, it should have been set to true
        $category->grade_item->update_from_db();
        $this->assertTrue($category->grade_item->needsupdate);

        $this->assertFalse(get_record('grade_items', 'id', $grade_item->id));
    }

    function test_grade_item_update() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'update'));

        $grade_item->iteminfo = 'Updated info for this unittest grade_item';

        // Check the grade_category's needsupdate variable first
        $category= $grade_item->get_category();
        $category->load_grade_item();
        $this->assertNotNull($category->grade_item);
        $category->grade_item->needsupdate = false;

        $this->assertTrue($grade_item->update());

        // Now check the needsupdate variable, it should NOT have been set to true, because insufficient changes to justify update.
        $this->assertFalse($category->grade_item->needsupdate);

        $grade_item->grademin = 14;
        $this->assertTrue($grade_item->qualifies_for_update());
        $this->assertTrue($grade_item->update(true));

        // Now check the needsupdate variable, it should have been set to true
        $category->grade_item->update_from_db();
        $this->assertTrue($category->grade_item->needsupdate);

        // Also check parent
        $category->load_parent_category();
        $category->parent_category->load_grade_item();
        $this->assertTrue($category->parent_category->grade_item->needsupdate);

        $iteminfo = get_field('grade_items', 'iteminfo', 'id', $this->grade_items[0]->id);
        $this->assertEqual($grade_item->iteminfo, $iteminfo);
    }

    function test_grade_item_qualifies_for_update() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'qualifies_for_update'));

        $grade_item->iteminfo = 'Updated info for this unittest grade_item';

        $this->assertFalse($grade_item->qualifies_for_update());

        $grade_item->grademin = 14;

        $this->assertTrue($grade_item->qualifies_for_update());
    }

    function test_grade_item_fetch() {
        $grade_item = new grade_item();
        $this->assertTrue(method_exists($grade_item, 'fetch'));

        $grade_item = grade_item::fetch('id', $this->grade_items[0]->id);
        $this->assertEqual($this->grade_items[0]->id, $grade_item->id);
        $this->assertEqual($this->grade_items[0]->iteminfo, $grade_item->iteminfo);

        $grade_item = grade_item::fetch('itemtype', $this->grade_items[1]->itemtype, 'itemmodule', $this->grade_items[1]->itemmodule);
        $this->assertEqual($this->grade_items[1]->id, $grade_item->id);
        $this->assertEqual($this->grade_items[1]->iteminfo, $grade_item->iteminfo);
    }

    function test_grade_item_fetch_all_using_this() {
        $grade_item = new grade_item();
        $grade_item->itemtype = 'mod';
        $this->assertTrue(method_exists($grade_item, 'fetch_all_using_this'));

        $grade_items = $grade_item->fetch_all_using_this();
        $this->assertEqual(5, count($grade_items));
        $first_grade_item = reset($grade_items);
        $this->assertEqual($this->grade_items[0]->id, $first_grade_item->id);
    }


    /**
     * Retrieve all final scores for a given grade_item.
     */
    function test_grade_item_get_all_finals() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'get_final'));

        $final_grades = $grade_item->get_final();
        $this->assertEqual(3, count($final_grades));
    }


    /**
     * Retrieve all final scores for a specific userid.
     */
    function test_grade_item_get_final() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'get_final'));
        $final_grade = $grade_item->get_final($this->userid);
        $this->assertEqual($this->grade_grades[0]->finalgrade, $final_grade->finalgrade);
    }

    function test_grade_item_get_calculation() {
        $grade_item = new grade_item($this->grade_items[1]);
        $this->assertTrue(method_exists($grade_item, 'get_calculation'));
        $grade_calculation = $grade_item->get_calculation();

        $this->assertEqual($this->grade_calculations[0]->id, $grade_calculation->id);
    }

    function test_grade_item_set_calculation() {
/*        $grade_item = new grade_item($this->grade_items[1]);
        $this->assertTrue(method_exists($grade_item, 'set_calculation'));
        $this->assertTrue(method_exists($grade_item, 'get_calculation'));

        $calculation = '=SUM([unittestgradeitem1], [unittestgradeitem3])';
        $grade_item->set_calculation($calculation);
        $new_calculation = $grade_item->get_calculation();

        $this->assertEqual($calculation, $new_calculation->calculation);
*/    }

    function test_grade_item_get_category() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'get_category'));

        $category = $grade_item->get_category();
        $this->assertEqual($this->grade_categories[1]->fullname, $category->fullname);
    }

    /**
     * Test update of all final grades
     */
    function test_grade_item_update_final_grades() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'update_final_grades'));
        $this->assertEqual(true, $grade_item->update_final_grades());
    }

    /**
     * Test the adjust_grade method
     */
    function test_grade_item_adjust_grade() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'adjust_grade'));
        $grade_raw = new stdClass();

        $grade_raw->rawgrade = 40;
        $grade_raw->grademax = 100;
        $grade_raw->grademin = 0;

        $grade_item->multfactor = 1;
        $grade_item->plusfactor = 0;
        $grade_item->grademax = 50;
        $grade_item->grademin = 0;

        $original_grade_raw  = clone($grade_raw);
        $original_grade_item = clone($grade_item);

        $this->assertEqual(20, $grade_item->adjust_grade($grade_raw->rawgrade, $grade_raw->grademin, $grade_raw->grademax));

        // Try a larger maximum grade
        $grade_item->grademax = 150;
        $grade_item->grademin = 0;
        $this->assertEqual(60, $grade_item->adjust_grade($grade_raw->rawgrade, $grade_raw->grademin, $grade_raw->grademax));

        // Try larger minimum grade
        $grade_item->grademin = 50;

        $this->assertEqual(90, $grade_item->adjust_grade($grade_raw->rawgrade, $grade_raw->grademin, $grade_raw->grademax));

        // Rescaling from a small scale (0-50) to a larger scale (0-100)
        $grade_raw->grademax = 50;
        $grade_raw->grademin = 0;
        $grade_item->grademax = 100;
        $grade_item->grademin = 0;

        $this->assertEqual(80, $grade_item->adjust_grade($grade_raw->rawgrade, $grade_raw->grademin, $grade_raw->grademax));

        // Rescaling from a small scale (0-50) to a larger scale with offset (40-100)
        $grade_item->grademax = 100;
        $grade_item->grademin = 40;

        $this->assertEqual(88, $grade_item->adjust_grade($grade_raw->rawgrade, $grade_raw->grademin, $grade_raw->grademax));

        // Try multfactor and plusfactor
        $grade_raw = clone($original_grade_raw);
        $grade_item = clone($original_grade_item);
        $grade_item->multfactor = 1.23;
        $grade_item->plusfactor = 3;

        $this->assertEqual(27.6, $grade_item->adjust_grade($grade_raw->rawgrade, $grade_raw->grademin, $grade_raw->grademax));

        // Try multfactor below 0 and a negative plusfactor
        $grade_raw = clone($original_grade_raw);
        $grade_item = clone($original_grade_item);
        $grade_item->multfactor = 0.23;
        $grade_item->plusfactor = -3;

        $this->assertEqual(round(1.6), round($grade_item->adjust_grade($grade_raw->rawgrade, $grade_raw->grademin, $grade_raw->grademax)));
    }

    function test_grade_item_adjust_scale_grade() {
/*        // Load grade item and its scale
        $grade_item = new grade_item(array('scaleid' => $this->scale[1]->id), false);
        $grade_item->gradetype = GRADE_TYPE_SCALE;
        $grade_item->insert();
        $grade_item->load_scale();
        $this->assertEqual('Very Good', $grade_item->scale->scale_items[1]);

        // Load raw grade and its scale
        $grade_raw = new grade_grades(array('scaleid' => $this->scale[0]->id), false);
        $grade_raw->rawgrade = 4;
        $grade_raw->itemid = $grade_item->id;
        $grade_raw->userid = 1;
        $grade_raw->insert();
        $grade_raw->load_scale();
        $this->assertEqual('Fairly neutral', $grade_raw->scale->scale_items[2]);

        // Test grade_item::adjust_scale
        $this->assertEqual(3, $grade_item->adjust_grade($grade_raw));
        $grade_raw->rawgrade = 6;
        $this->assertEqual(4, $grade_item->adjust_grade($grade_raw));
*/    }

    function test_grade_item_toggle_locking() {
/*        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'toggle_locking'));

        $this->assertFalse($grade_item->locked);
        $this->assertEqual(0, $grade_item->toggle_locking());
        $this->assertTrue($grade_item->locked);
        $grade_item->load_final();
        $this->assertFalse($grade_item->grade_grades[1]->locked);

        $grade_item->locked = false;
        $this->assertEqual(3, $grade_item->toggle_locking(true));
        $this->assertTrue($grade_item->locked);
        $this->assertTrue($grade_item->grade_grades[1]->locked);
        $this->assertTrue($grade_item->grade_grades[2]->locked);
        $this->assertTrue($grade_item->grade_grades[3]->locked);
    }

    function test_grade_item_toggle_hiding() {
        $grade_item = new grade_item($this->grade_items[0]);
        $this->assertTrue(method_exists($grade_item, 'toggle_hiding'));

        $this->assertFalse($grade_item->hidden);
        $this->assertEqual(0, $grade_item->toggle_hiding());
        $this->assertTrue($grade_item->hidden);
        $grade_item->load_final();
        $this->assertFalse($grade_item->grade_grades[1]->hidden);

        $grade_item->hidden = false;
        $this->assertEqual(3, $grade_item->toggle_hiding(true));
        $this->assertTrue($grade_item->hidden);
        $this->assertTrue($grade_item->grade_grades[1]->hidden);
        $this->assertTrue($grade_item->grade_grades[2]->hidden);
        $this->assertTrue($grade_item->grade_grades[3]->hidden);
*/    }

    function test_float_keys() {
    }
}
?>