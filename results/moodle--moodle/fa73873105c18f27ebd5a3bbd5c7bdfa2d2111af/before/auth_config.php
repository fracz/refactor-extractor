<?php
/**
 * Edit configuration for an individual auth plugin
 */

require_once '../config.php';
require_once $CFG->libdir.'/adminlib.php';

$adminroot = admin_get_root();
admin_externalpage_setup('userauthentication', $adminroot);
$auth = optional_param('auth', '', PARAM_SAFEDIR);
$authplugin = get_auth_plugin($auth);
$err = array();

// save configuration changes
if ($frm = data_submitted()) {

    if (!confirm_sesskey()) {
        error(get_string('confirmsesskeybad', 'error'));
    }

    if (method_exists($authplugin, 'validate_form')) {
        $authplugin->validate_form($frm, $err);
    }

    if (count($err) == 0) {

        // save plugin config
        if ($authplugin->process_config($frm)) {

            // save field lock configuration
            foreach ($frm as $name => $value) {
                if (preg_match('/^lockconfig_(.+?)$/', $name, $matches)) {
                    $plugin = "auth/$auth";
                    $name   = $matches[1];
                    if (!set_config($name, $value, $plugin)) {
                        notify("Problem saving config $name as $value for plugin $plugin");
                    }
                }
            }
            redirect("auth.php?sesskey=$USER->sesskey", get_string("changessaved"), 1);
            exit;
        }
    } else {
        foreach ($err as $key => $value) {
            $focus = "form.$key";
        }
    }
} else {
    $frm = get_config("auth/$auth");
}

$user_fields = array("firstname", "lastname", "email", "phone1", "phone2", "department", "address", "city", "country", "description", "idnumber", "lang");

$modules = get_list_of_plugins('auth');
foreach ($modules as $module) {
    $options[$module] = get_string("auth_{$module}title", 'auth');
}
asort($options);

// output configuration form
admin_externalpage_print_header($adminroot);

if (empty($CFG->framename) or $CFG->framename=='_top') {
    $target = '';
} else {
    $target = ' target="'.$CFG->framename.'"';
}
// choose an authentication method
echo "<form$target name=\"authmenu\" method=\"post\" action=\"auth_config.php\">\n";
echo "<input type=\"hidden\" name=\"sesskey\" value=\"".$USER->sesskey."\">\n";
echo "<div align=\"center\"><p><b>\n";
echo get_string('chooseauthmethod').': ';
choose_from_menu ($options, "auth", $auth, '', "document.location='auth_config.php?sesskey=$USER->sesskey&auth='+document.authmenu.auth.options[document.authmenu.auth.selectedIndex].value", '');
echo "</b></p></div>\n\n";

// auth plugin description
print_simple_box_start('center', '80%');
print_heading($options[$auth]);
print_simple_box_start('center', '60%', '', 5, 'informationbox');
print_string("auth_{$auth}description", 'auth');
print_simple_box_end();
echo "<hr />\n";
$authplugin->config_form($frm, $err);
print_simple_box_end();
echo '<center><p><input type="submit" value="' . get_string("savechanges") . "\"></p></center>\n";
echo "</form>\n";

admin_externalpage_print_footer($adminroot);
exit;

/// Functions /////////////////////////////////////////////////////////////////

// Good enough for most auth plugins
// but some may want a custom one if they are offering
// other options
// Note: lockconfig_ fields have special handling.
function print_auth_lock_options ($auth, $user_fields, $helptext, $retrieveopts, $updateopts) {

    echo '<tr><td colspan="3">';
    if ($retrieveopts) {
        print_heading(get_string('auth_data_mapping', 'auth'));
    } else {
        print_heading(get_string('auth_fieldlocks', 'auth'));
    }
    echo '</td></tr>';

    $lockoptions = array ('unlocked'        => get_string('unlocked', 'auth'),
                          'unlockedifempty' => get_string('unlockedifempty', 'auth'),
                          'locked'          => get_string('locked', 'auth'));
    $updatelocaloptions = array('oncreate'  => get_string('update_oncreate', 'auth'),
                                'onlogin'   => get_string('update_onlogin', 'auth'));
    $updateextoptions = array('0'  => get_string('update_never', 'auth'),
                              '1'   => get_string('update_onupdate', 'auth'));

    $pluginconfig = get_config("auth/$auth");

    // helptext is on a field with rowspan
    if (empty($helptext)) {
                $helptext = '&nbsp;';
    }

    foreach ($user_fields as $field) {

        // Define some vars we'll work with
        optional_variable($pluginconfig->{"field_map_$field"}, '');
        optional_variable($pluginconfig->{"field_updatelocal_$field"}, '');
        optional_variable($pluginconfig->{"field_updateremote_$field"}, '');
        optional_variable($pluginconfig->{"field_lock_$field"}, '');

        // define the fieldname we display to the  user
        $fieldname = $field;
        if ($fieldname === 'lang') {
            $fieldname = get_string('language');
        } elseif (preg_match('/^(.+?)(\d+)$/', $fieldname, $matches)) {
            $fieldname =  get_string($matches[1]) . ' ' . $matches[2];
        } else {
            $fieldname = get_string($fieldname);
        }

        echo '<tr valign="top"><td align="right">';
        echo $fieldname;
        echo '</td><td>';

        if ($retrieveopts) {
            $varname = 'field_map_' . $field;

            echo "<input name=\"lockconfig_{$varname}\" type=\"text\" size=\"30\" value=\"{$pluginconfig->$varname}\">";
            echo '<div align="right">';
            echo  get_string('auth_updatelocal', 'auth') . '&nbsp;&nbsp;';
            choose_from_menu($updatelocaloptions, "lockconfig_field_updatelocal_{$field}", $pluginconfig->{"field_updatelocal_$field"}, "");
            echo '<br />';
            if ($updateopts) {
                echo  get_string('auth_updateremote', 'auth') . '&nbsp;&nbsp;';
                 '&nbsp;&nbsp;';
                choose_from_menu($updateextoptions, "lockconfig_field_updateremote_{$field}", $pluginconfig->{"field_updateremote_$field"}, "");
                echo '<br />';


            }
            echo  get_string('auth_fieldlock', 'auth') . '&nbsp;&nbsp;';
            choose_from_menu($lockoptions, "lockconfig_field_lock_{$field}", $pluginconfig->{"field_lock_$field"}, "");
            echo '</div>';
        } else {
            choose_from_menu($lockoptions, "lockconfig_field_lock_{$field}", $pluginconfig->{"field_lock_$field"}, "");
        }
        echo '</td>';
        if (!empty($helptext)) {
            echo '<td rowspan="' . count($user_fields) . '">' . $helptext . '</td>';
            $helptext = '';
        }
        echo '</tr>';
    }
}

?>