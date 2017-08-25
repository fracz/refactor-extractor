<?php  //$Id$

    require_once('../../config.php');

    $contextid = required_param('contextid', PARAM_INT);   // context id
    $roleid    = optional_param('roleid', 0, PARAM_INT);   // requested role id
    $userid    = optional_param('userid', 0, PARAM_INT);   // needed for user tabs
    $courseid  = optional_param('courseid', 0, PARAM_INT); // needed for user tabs
    $cancel    = optional_param('cancel', 0, PARAM_BOOL);

    if (!$context = $DB->get_record('context', array('id'=>$contextid))) {
        print_error('wrongcontextid', 'error');
    }

    if (!$sitecontext = get_context_instance(CONTEXT_SYSTEM)) {
        print_error('nositeid', 'error');
    }

    if ($context->id == $sitecontext->id) {
        print_error('cannotoverridebaserole', 'error');
    }

    $canoverride = has_capability('moodle/role:override', $context);

    if (!$canoverride and !has_capability('moodle/role:safeoverride', $context)) {
        print_error('nopermissions', 'error', '', 'change overrides in this context!');
    }

    if ($courseid) {
        if (!$course = $DB->get_record('course', array('id'=>$courseid))) {
            print_error('invalidcourse');
        }
    } else {
        $course = clone($SITE);
        $courseid = SITEID;
    }

    require_login($course);

    $baseurl = 'override.php?contextid='.$context->id;
    if (!empty($userid)) {
        $baseurl .= '&amp;userid='.$userid;
    }
    if ($courseid != SITEID) {
        $baseurl .= '&amp;courseid='.$courseid;
    }

    if ($cancel) {
        redirect($baseurl);
    }

/// needed for tabs.php
    $overridableroles = get_overridable_roles($context, ROLENAME_BOTH);
    $assignableroles  = get_assignable_roles($context, ROLENAME_BOTH);

/// Get some language strings

    $strroletooverride = get_string('roletooverride', 'role');
    $straction         = get_string('overrideroles', 'role');
    $strcurrentrole    = get_string('currentrole', 'role');
    $strparticipants   = get_string('participants');

/// Make sure this user can override that role

    if ($roleid) {
        if (!isset($overridableroles[$roleid])) {
            error ('you can not override this role in this context');
        }
    }

    if ($userid) {
        $user = $DB->get_record('user', array('id'=>$userid));
        $fullname = fullname($user, has_capability('moodle/site:viewfullnames', $context));
    }

/// get all cababilities
    $safeoverridenotice = false;
    if ($roleid) {
        if ($capabilities = fetch_context_capabilities($context)) {
            // find out if we need to lock some capabilities
            foreach ($capabilities as $capname=>$capability) {
                $capabilities[$capname]->locked = false;
                if ($canoverride) {
                    //ok no locking at all
                    continue;
                }
                //only limited safe overrides - spam only allowed
                if ((RISK_DATALOSS & (int)$capability->riskbitmask)
                 or (RISK_MANAGETRUST & (int)$capability->riskbitmask)
                 or (RISK_CONFIG & (int)$capability->riskbitmask)
                 or (RISK_XSS & (int)$capability->riskbitmask)
                 or (RISK_PERSONAL & (int)$capability->riskbitmask)) {
                    $capabilities[$capname]->locked = true;
                    $safeoverridenotice = true;
                }
            }
        }
    } else {
        $capabilities = null;
    }

/// Process incoming role override
    if ($data = data_submitted() and $roleid and confirm_sesskey()) {
        $allowed_values = array(CAP_INHERIT, CAP_ALLOW, CAP_PREVENT, CAP_PROHIBIT);

        $localoverrides = $DB->get_records_select('role_capabilities', "roleid = ? AND contextid = ?", array($roleid, $context->id),
                                             '', 'capability, permission, id');

        foreach ($capabilities as $cap) {
            if ($cap->locked) {
                //user not allowed to change this cap
                continue;
            }

            if (!isset($data->{$cap->name})) {
                //cap not specified in form
                continue;
            }

            if (islegacy($data->{$cap->name})) {
                continue;
            }

            $capname = $cap->name;
            $value = clean_param($data->{$cap->name}, PARAM_INT);
            if (!in_array($value, $allowed_values)) {
                 continue;
            }

            if (isset($localoverrides[$capname])) {
                // Something exists, so update it
                assign_capability($capname, $value, $roleid, $context->id, true);
            } else { // insert a record
                if ($value != CAP_INHERIT) {    // Ignore inherits
                    assign_capability($capname, $value, $roleid, $context->id);
                }
            }
        }

        // force accessinfo refresh for users visiting this context...
        mark_context_dirty($context->path);
        $rolename = $DB->get_field('role', 'name', array('id'=>$roleid));
        add_to_log($course->id, 'role', 'override', 'admin/roles/override.php?contextid='.$context->id.'&roleid='.$roleid, $rolename, '', $USER->id);
        redirect($baseurl);
    }


/// Print the header and tabs
    require_js(array('yui_yahoo', 'yui_dom', 'yui_event'));
    require_js($CFG->admin . '/roles/roles.js');
    if ($context->contextlevel == CONTEXT_USER) {
        $navlinks = array();
        /// course header
        if ($course->id != SITEID) {
            if (has_capability('moodle/course:viewparticipants', get_context_instance(CONTEXT_COURSE, $course->id))) {
                $navlinks[] = array('name' => $strparticipants, 'link' => "$CFG->wwwroot/user/index.php?id=$course->id", 'type' => 'misc');
            }
            $navlinks[] = array('name' => $fullname, 'link' => "$CFG->wwwroot/user/view.php?id=$userid&amp;course=$courseid", 'type' => 'misc');
            $navlinks[] = array('name' => $straction, 'link' => null, 'type' => 'misc');
            $navigation = build_navigation($navlinks);
            print_header("$fullname", "$fullname", $navigation, "", "", true, "&nbsp;", navmenu($course));

        /// site header
        } else {
            $navlinks[] = array('name' => $fullname, 'link' => "$CFG->wwwroot/user/view.php?id=$userid&amp;course=$courseid", 'type' => 'misc');
            $navlinks[] = array('name' => $straction, 'link' => null, 'type' => 'misc');
            $navigation = build_navigation($navlinks);
            print_header("$course->fullname: $fullname", $course->fullname, $navigation, "", "", true, "&nbsp;", navmenu($course));
        }
        $showroles = 1;
        $currenttab = 'override';
        include_once($CFG->dirroot.'/user/tabs.php');
    } else if ($context->contextlevel==CONTEXT_COURSE and $context->instanceid == SITEID) {
        require_once($CFG->libdir.'/adminlib.php');
        admin_externalpage_setup('frontpageroles');
        admin_externalpage_print_header();
        $currenttab = 'override';
        include_once('tabs.php');
    } else {
        $currenttab = 'override';
        include_once('tabs.php');
    }

    print_heading_with_help(get_string('overridepermissionsin', 'role', print_context_name($context)), 'overrides');

    if ($roleid) {
    /// prints a form to swap roles
        echo '<div class="selector">';
        $overridableroles = array('0'=>get_string('listallroles', 'role').'...') + $overridableroles;
        popup_form("$CFG->wwwroot/$CFG->admin/roles/override.php?userid=$userid&amp;courseid=$courseid&amp;contextid=$contextid&amp;roleid=",
            $overridableroles, 'switchrole', $roleid, '', '', '', false, 'self', $strroletooverride);
        echo '</div>';

        $parentcontexts = get_parent_contexts($context);
        if (!empty($parentcontexts)) {
            $parentcontext = array_shift($parentcontexts);
            $parentcontext = get_context_instance_by_id($parentcontext);
        } else {
            $parentcontext = $context; // site level in override??
        }

        $r_caps = role_context_capabilities($roleid, $parentcontext);

        $localoverrides = $DB->get_records_select('role_capabilities', "roleid = ? AND contextid = ?", array($roleid, $context->id),
                                             '', 'capability, permission, id');

        $lang = str_replace('_utf8', '', current_language());

        if (!empty($capabilities)) {
            // Print the capabilities overrideable in this context
            print_simple_box_start('center');
            include('override.html');
            print_simple_box_end();

        } else {
            notice(get_string('nocapabilitiesincontext', 'role'),
                    $CFG->wwwroot.'/'.$CFG->admin.'/roles/'.$baseurl);
        }

    } else {   // Print overview table

        $table->tablealign = 'center';
        $table->cellpadding = 5;
        $table->cellspacing = 0;
        $table->width = '60%';
        $table->head = array(get_string('roles', 'role'), get_string('description'), get_string('overrides', 'role'));
        $table->wrap = array('nowrap', '', 'nowrap');
        $table->align = array('right', 'left', 'center');

        foreach ($overridableroles as $roleid => $rolename) {
            $countusers = 0;
            $overridecount = $DB->count_records_select('role_capabilities', "roleid = ? AND contextid = ?", array($roleid, $context->id));
            $description = format_string($DB->get_field('role', 'description', array('id'=>$roleid)));
            $table->data[] = array('<a href="'.$baseurl.'&amp;roleid='.$roleid.'">'.$rolename.'</a>', $description, $overridecount);
        }

        print_table($table);
    }

    print_footer($course);

?>