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
 * This file keeps track of upgrades to
 * the lesson module
 *
 * Sometimes, changes between versions involve
 * alterations to database structures and other
 * major things that may break installations.
 *
 * The upgrade function in this file will attempt
 * to perform all the necessary actions to upgrade
 * your older installation to the current version.
 *
 * If there's something it cannot do itself, it
 * will tell you what you need to do.
 *
 * The commands in here will all be database-neutral,
 * using the methods of database_manager class
 *
 * Please do not forget to use upgrade_set_timeout()
 * before any action that may take longer time to finish.
 *
 * @package    mod
 * @subpackage lesson
 * @copyright  1999 onwards Martin Dougiamas  {@link http://moodle.com}
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL v3 o
 */

defined('MOODLE_INTERNAL') || die();

function xmldb_lesson_upgrade($oldversion) {
    global $CFG, $DB, $OUTPUT;

    $dbman = $DB->get_manager();

//===== 1.9.0 upgrade line ======//

    if ($oldversion < 2007072201) {

        $table = new xmldb_table('lesson');
        $field = new xmldb_field('usegrademax');
        $field2 = new xmldb_field('usemaxgrade');

    /// Rename lesson->usegrademax to lesson->usemaxgrade. Some old sites can have it incorrect. MDL-13177
        if ($dbman->field_exists($table, $field) && !$dbman->field_exists($table, $field2)) {
        /// Set field specs
            $field->set_attributes(XMLDB_TYPE_INTEGER, '3', null, XMLDB_NOTNULL, null, '0', 'ongoing');
        /// Launch rename field usegrademax to usemaxgrade
            $dbman->rename_field($table, $field, 'usemaxgrade');
        }

        upgrade_mod_savepoint(true, 2007072201, 'lesson');
    }

    if ($oldversion < 2008112601) {
        //NOTE: this is a hack, we can not call module lib.php in the middle of upgrade, the necessary db structures/data may not exist yet!
        require_once($CFG->dirroot.'/mod/lesson/lib.php');

        lesson_upgrade_grades();

        upgrade_mod_savepoint(true, 2008112601, 'lesson');
    }

    if ($oldversion < 2009111600) {
        /**
         * Change the grade field within lesson_answers to an unsigned int and increment
         * the length by one to ensure that no values are changed (reduced)
         */
        $table = new xmldb_table('lesson_answers');
        $field = new xmldb_field('grade');
        $field->set_attributes(XMLDB_TYPE_INTEGER, '4', false, XMLDB_NOTNULL, null, '0', 'jumpto');
        $dbman->change_field_type($table, $field);
        upgrade_mod_savepoint(true, 2009111600, 'lesson');
    }

    if ($oldversion < 2009120400) {

        $sqlfrom = "FROM {lesson} l
                    JOIN {modules} m ON m.name = 'lesson'
                    JOIN {course_modules} cm ON (cm.module = m.id AND cm.instance = l.id)
                   WHERE l.mediafile <> :empty";
        $params = array('empty'=>$DB->sql_empty());

        $count = $DB->count_records_sql("SELECT COUNT('x') $sqlfrom", $params);

        if ($count > 0) {
            $rs = $DB->get_recordset_sql("SELECT l.id, l.mediafile, l.course, cm.id AS cmid $sqlfrom ORDER BY l.course, l.id", $params);

            $pbar = new progress_bar('migratelessonfiles', 500, true);
            $fs = get_file_storage();

            $i = 0;
            foreach ($rs as $lesson) {
                $i++;
                upgrade_set_timeout(60); // set up timeout, may also abort execution
                $pbar->update($i, $count, "Migrating lesson mediafiles - $i/$count.");

                $filepath = $CFG->dataroot.'/'.$lesson->course.'/'.$CFG->moddata.'/lesson/'.$lesson->mediafile;
                if (!is_readable($filepath)) {
                    //file missing??
                    echo $OUTPUT->notification("File not readable, skipping: ".$filepath);
                    $DB->set_field('lesson', 'mediafile', '', array('id'=>$lesson->id));
                    continue;
                }

                $filename = clean_param($lesson->mediafile, PARAM_FILE);
                if ($filename === '') {
                    echo $OUTPUT->notification("Unsupported lesson filename, skipping: ".$filepath);
                    $DB->set_field('lesson', 'mediafile', '', array('id'=>$lesson->id));
                    continue;
                }

                $context = get_context_instance(CONTEXT_MODULE, $lesson->cmid);
                if (!$fs->file_exists($context->id, 'mod_lesson', 'mediafile', 0, '/', $filename)) {
                    $file_record = array('contextid'=>$context->id, 'component'=>'mod_lesson', 'filearea'=>'mediafile', 'itemid'=>0, 'filepath'=>'/', 'filename'=>$filename);
                    if ($fs->create_file_from_pathname($file_record, $filepath)) {
                        if ($DB->set_field('lesson', 'mediafile', $filename, array('id'=>$lesson->id))) {
                            unlink($filepath);
                        }
                    }
                }

                // remove dir if empty
                @rmdir("$CFG->dataroot/$post->course/$CFG->moddata/lesson");
            }
            $rs->close();
        }

        upgrade_mod_savepoint(true, 2009120400, 'lesson');
    }

    if ($oldversion < 2009120800) {
        /**
         * Drop the lesson_default table, as of Moodle 2.0 it is no longer used
         * the module now has a settings.php instead
         */
        $table = new xmldb_table('lesson_default');
        $dbman->drop_table($table);
        upgrade_mod_savepoint(true, 2009120800, 'lesson');
    }

    if ($oldversion < 2009120801) {

    /// Define field contentsformat to be added to lesson_pages
        $table = new xmldb_table('lesson_pages');
        $field = new xmldb_field('contentsformat', XMLDB_TYPE_INTEGER, '2', XMLDB_UNSIGNED, XMLDB_NOTNULL, null, FORMAT_MOODLE, 'contents');

    /// Conditionally launch add field contentsformat
        if (!$dbman->field_exists($table, $field)) {
            $dbman->add_field($table, $field);
        }

        // conditionally migrate to html format in contents
        if ($CFG->texteditors !== 'textarea') {
            $rs = $DB->get_recordset('lesson_pages', array('contentsformat'=>FORMAT_MOODLE), '', 'id,contents,contentsformat');
            foreach ($rs as $lp) {
                $lp->contents       = text_to_html($lp->contents, false, false, true);
                $lp->contentsformat = FORMAT_HTML;
                $DB->update_record('lesson_pages', $lp);
                upgrade_set_timeout();
            }
            $rs->close();
        }
    /// lesson savepoint reached
        upgrade_mod_savepoint(true, 2009120801, 'lesson');
    }

    if ($oldversion < 2010072000) {
        // Define field answerformat to be added to lesson_answers
        $table = new xmldb_table('lesson_answers');
        $field = new xmldb_field('answerformat', XMLDB_TYPE_INTEGER, '2', XMLDB_UNSIGNED, XMLDB_NOTNULL, null, '0', 'answer');

        // Launch add field answerformat
        $dbman->add_field($table, $field);

        // lesson savepoint reached
        upgrade_mod_savepoint(true, 2010072000, 'lesson');
    }

    if ($oldversion < 2010072001) {
        // Define field responseformat to be added to lesson_answers
        $table = new xmldb_table('lesson_answers');
        $field = new xmldb_field('responseformat', XMLDB_TYPE_INTEGER, '2', XMLDB_UNSIGNED, XMLDB_NOTNULL, null, '0', 'response');

        // Launch add field responseformat
        $dbman->add_field($table, $field);

        // lesson savepoint reached
        upgrade_mod_savepoint(true, 2010072001, 'lesson');
    }

    if ($oldversion < 2010072003) {
        $rs = $DB->get_recordset('lesson_answers', array());
        foreach ($rs as $answer) {
            $flags = intval($answer->flags);
            $update = false;
            if ($flags & 1) {
                $answer->answer       = text_to_html($answer->answer, false, false, true);
                $answer->answerformat = FORMAT_HTML;
                $update = true;
            }
            if ($flags & 2) {
                $answer->response       = text_to_html($answer->response, false, false, true);
                $answer->responseformat = FORMAT_HTML;
                $update = true;
            }
            if ($update) {
                $DB->update_record('lesson_answers', $answer);
            }
        }
        $rs->close();
        upgrade_mod_savepoint(true, 2010072003, 'lesson');
    }


    return true;
}

