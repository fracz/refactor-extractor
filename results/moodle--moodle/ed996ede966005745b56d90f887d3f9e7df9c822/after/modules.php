<?PHP // $Id$
      // Allows the admin to manage activity modules

    require_once('../config.php');
    require_once('../course/lib.php');
    require_once($CFG->libdir.'/adminlib.php');
    require_once($CFG->libdir.'/tablelib.php');

    // defines
    define('MODULE_TABLE','module_administration_table');

    admin_externalpage_setup('managemodules');

    $show    = optional_param('show', '', PARAM_SAFEDIR);
    $hide    = optional_param('hide', '', PARAM_SAFEDIR);
    $delete  = optional_param('delete', '', PARAM_SAFEDIR);
    $confirm = optional_param('confirm', '', PARAM_BOOL);


/// Print headings

    $stractivities = get_string("activities");
    $strdelete = get_string("delete");
    $strversion = get_string("version");
    $strhide = get_string("hide");
    $strshow = get_string("show");
    $strsettings = get_string("settings");
    $stractivities = get_string("activities");
    $stractivitymodule = get_string("activitymodule");
    $strshowmodulecourse = get_string('showmodulecourse');

/// If data submitted, then process and store.

    if (!empty($hide) and confirm_sesskey()) {
        if (!$module = $DB->get_record("modules", array("name"=>$hide))) {
            print_error('moduledoesnotexist', 'error');
        }
        $DB->set_field("modules", "visible", "0", array("id"=>$module->id)); // Hide main module
        // Remember the visibility status in visibleold
        // and hide...
        $sql = "UPDATE {course_modules}
                   SET visibleold=visible, visible=0
                 WHERE module=?";
        $DB->execute($sql, array($module->id));
        // clear the course modinfo cache for courses
        // where we just deleted something
        $sql = "UPDATE {course}
                   SET modinfo=''
                 WHERE id IN (SELECT DISTINCT course
                                FROM {course_modules}
                               WHERE visibleold=1 AND module=?)";
        $DB->execute($sql, array($module->id));
        admin_get_root(true, false);  // settings not required - only pages
    }

    if (!empty($show) and confirm_sesskey()) {
        if (!$module = $DB->get_record("modules", array("name"=>$show))) {
            print_error('moduledoesnotexist', 'error');
        }
        $DB->set_field("modules", "visible", "1", array("id"=>$module->id)); // Show main module
        $DB->set_field('course_modules', 'visible', '1', array('visibleold'=>1, 'module'=>$module->id)); // Get the previous saved visible state for the course module.
        // clear the course modinfo cache for courses
        // where we just made something visible
        $sql = "UPDATE {course}
                   SET modinfo = ''
                 WHERE id IN (SELECT DISTINCT course
                                FROM {course_modules}
                               WHERE visible=1 AND module=?)";
        $DB->execute($sql, array($module->id));
        admin_get_root(true, false);  // settings not required - only pages
    }

    if (!empty($delete) and confirm_sesskey()) {
        admin_externalpage_print_header();
        echo $OUTPUT->heading($stractivities);

        $strmodulename = get_string("modulename", "$delete");

        if (!$confirm) {
            echo $OUTPUT->confirm(get_string("moduledeleteconfirm", "", $strmodulename), "modules.php?delete=$delete&confirm=1", "modules.php");
            echo $OUTPUT->footer();
            exit;

        } else {  // Delete everything!!

            if ($delete == "forum") {
                print_error("cannotdeleteforummodule", 'forum');
            }

            uninstall_plugin('mod', $delete);
            $a->module = $strmodulename;
            $a->directory = "$CFG->dirroot/mod/$delete";
            notice(get_string("moduledeletefiles", "", $a), "modules.php");
        }
    }

    admin_externalpage_print_header();
    echo $OUTPUT->heading($stractivities);

/// Get and sort the existing modules

    if (!$modules = $DB->get_records("modules")) {
        print_error('moduledoesnotexist', 'error');
    }

    foreach ($modules as $module) {
        $strmodulename = get_string("modulename", "$module->name");
        // Deal with modules which are lacking the language string
        if ($strmodulename == '[[modulename]]') {
            $strmodulename = $module->name;
        }
        $modulebyname[$strmodulename] = $module;
    }
    ksort($modulebyname, SORT_LOCALE_STRING);

/// Print the table of all modules
    // construct the flexible table ready to display
    $table = new flexible_table(MODULE_TABLE);
    $table->define_columns(array('name', 'instances', 'version', 'hideshow', 'delete', 'settings'));
    $table->define_headers(array($stractivitymodule, $stractivities, $strversion, "$strhide/$strshow", $strdelete, $strsettings));
    $table->define_baseurl($CFG->wwwroot.'/'.$CFG->admin.'/modules.php');
    $table->set_attribute('id', 'modules');
    $table->set_attribute('class', 'generaltable generalbox boxaligncenter boxwidthwide');
    $table->setup();

    foreach ($modulebyname as $modulename => $module) {

        // took out hspace="\10\", because it does not validate. don't know what to replace with.
        $icon = "<img src=\"" . $OUTPUT->mod_icon_url('icon', $module->name) . "\" class=\"icon\" alt=\"\" />";

        $delete = "<a href=\"modules.php?delete=$module->name&amp;sesskey=".sesskey()."\">$strdelete</a>";

        if (file_exists("$CFG->dirroot/mod/$module->name/settings.php") ||
                file_exists("$CFG->dirroot/mod/$module->name/settingstree.php")) {
            $settings = "<a href=\"settings.php?section=modsetting$module->name\">$strsettings</a>";
        } else {
            $settings = "";
        }

        $count = $DB->count_records_select($module->name, "course<>0");
        if ($count>0) {
            $countlink = "<a href=\"{$CFG->wwwroot}/course/search.php?modulelist=$module->name" .
                "&amp;sesskey=".sesskey()."\" title=\"$strshowmodulecourse\">$count</a>";
        }
        else {
            $countlink = "$count";
        }

        if ($module->visible) {
            $visible = "<a href=\"modules.php?hide=$module->name&amp;sesskey=".sesskey()."\" title=\"$strhide\">".
                       "<img src=\"" . $OUTPUT->old_icon_url('i/hide') . "\" class=\"icon\" alt=\"$strhide\" /></a>";
            $class = "";
        } else {
            $visible = "<a href=\"modules.php?show=$module->name&amp;sesskey=".sesskey()."\" title=\"$strshow\">".
                       "<img src=\"" . $OUTPUT->old_icon_url('i/show') . "\" class=\"icon\" alt=\"$strshow\" /></a>";
            $class = " class=\"dimmed_text\"";
        }
        if ($module->name == "forum") {
            $delete = "";
            $visible = "";
            $class = "";
        }

        $extra = '';
        if (!file_exists("$CFG->dirroot/mod/$module->name/lib.php")) {
            $extra = ' <span class="notifyproblem">('.get_string('missingfromdisk').')</span>';
        }

        $table->add_data(array(
            '<span'.$class.'>'.$icon.' '.$modulename.$extra.'</span>',
            $countlink,
            '<span'.$class.'>'.$module->version.'</span>',
            $visible,
            $delete,
            $settings
        ));
    }

    $table->print_html();

    echo $OUTPUT->footer();

?>