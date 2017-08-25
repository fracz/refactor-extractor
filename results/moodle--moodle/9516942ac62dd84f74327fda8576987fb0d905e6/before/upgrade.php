<?php

// This file keeps track of upgrades to
// the label module
//
// Sometimes, changes between versions involve
// alterations to database structures and other
// major things that may break installations.
//
// The upgrade function in this file will attempt
// to perform all the necessary actions to upgrade
// your older installation to the current version.
//
// If there's something it cannot do itself, it
// will tell you what you need to do.
//
// The commands in here will all be database-neutral,
// using the methods of database_manager class
//
// Please do not forget to use upgrade_set_timeout()
// before any action that may take longer time to finish.

function xmldb_label_upgrade($oldversion) {
    global $CFG, $DB;

    $dbman = $DB->get_manager();

//===== 1.9.0 upgrade line ======//

    if ($oldversion < 2007101510) {
        $sql = "UPDATE {log_display} SET mtable = 'label' WHERE module = 'label'";
        $DB->execute($sql);
        upgrade_mod_savepoint(true, 2007101510, 'label');
    }

    if ($oldversion < 2009042200) {

    /// Rename field content on table label to intro
        $table = new xmldb_table('label');
        $field = new xmldb_field('content', XMLDB_TYPE_TEXT, 'small', null, XMLDB_NOTNULL, null, null, 'name');

    /// Launch rename field content
        $dbman->rename_field($table, $field, 'intro');

    /// label savepoint reached
        upgrade_mod_savepoint(true, 2009042200, 'label');
    }

    if ($oldversion < 2009042201) {

    /// Define field introformat to be added to label
        $table = new xmldb_table('label');
        $field = new xmldb_field('introformat', XMLDB_TYPE_INTEGER, '4', XMLDB_UNSIGNED, null, null, '0', 'intro');

    /// Launch add field introformat
        $dbman->add_field($table, $field);

        // all existing lables in 1.9 are in HTML format
        $DB->set_field('label', 'introformat', FORMAT_HTML, array());

    /// label savepoint reached
        upgrade_mod_savepoint(true, 2009042201, 'label');
    }

    return true;
}

