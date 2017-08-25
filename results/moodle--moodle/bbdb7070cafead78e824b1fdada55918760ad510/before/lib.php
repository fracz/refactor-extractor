<?php  // $Id$

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
 * Library code used by the roles administration interfaces.
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package roles
 *//** */

require_once($CFG->libdir.'/adminlib.php');
require_once($CFG->dirroot.'/user/selector/lib.php');

// Classes for producing tables with one row per capability ====================

/**
 * This class represents a table with one row for each of a list of capabilities
 * where the first cell in the row contains the capability name, and there is
 * arbitrary stuff in the rest of the row. This class is used by
 * admin/roles/manage.php, override.php and explain.php.
 *
 * An ajaxy search UI shown at the top, if JavaScript is on.
 */
abstract class capability_table_base {
    /** The context this table relates to. */
    protected $context;

    /** The capabilities to display. Initialised as fetch_context_capabilities($context). */
    protected $capabilities = array();

    /** Added as an id="" attribute to the table on output. */
    protected $id;

    /** Added to the class="" attribute on output. */
    protected $classes = array('rolecap');

    /** Default number of capabilities in the table for the search UI to be shown. */
    const NUM_CAPS_FOR_SEARCH = 12;

    /**
     * Constructor
     * @param object $context the context this table relates to.
     * @param string $id what to put in the id="" attribute.
     */
    public function __construct($context, $id) {
        $this->context = $context;
        $this->capabilities = fetch_context_capabilities($context);
        $this->id = $id;
    }

    /**
     * Use this to add class="" attributes to the table. You get the rolecap by
     * default.
     * @param array $classnames of class names.
     */
    public function add_classes($classnames) {
        $this->classes = array_unique(array_merge($this->classes, $classnames));
    }

    /**
     * Display the table.
     */
    public function display() {
        echo '<table class="' . implode(' ', $this->classes) . '" id="' . $this->id . '">' . "\n<thead>\n";
        echo '<tr><th class="name" align="left" scope="col">' . get_string('capability','role') . '</th>';
        $this->add_header_cells();
        echo "</tr>\n</thead>\n<tbody>\n";

    /// Loop over capabilities.
        $contextlevel = 0;
        $component = '';
        foreach ($this->capabilities as $capability) {
            if ($this->skip_row($capability)) {
                continue;
            }

        /// Prints a breaker if component or name or context level has changed
            if (component_level_changed($capability, $component, $contextlevel)) {
                $this->print_heading_row($capability);
            }
            $contextlevel = $capability->contextlevel;
            $component = $capability->component;

        /// Start the row.
            echo '<tr class="' . implode(' ', array_unique(array_merge(array('rolecap'),
                    $this->get_row_classes($capability)))) . '">';

        /// Table cell for the capability name.
            echo '<th scope="row" class="name"><span class="cap-desc">' . get_capability_docs_link($capability) .
                    '<span class="cap-name">' . $capability->name . '</span></span></td>';

        /// Add the cells specific to this table.
            $this->add_row_cells($capability);

        /// End the row.
            echo "</tr>\n";
        }

    /// End of the table.
            echo "</tbody>\n</table>\n";
            if (count($this->capabilities) > capability_table_base::NUM_CAPS_FOR_SEARCH) {
                global $CFG;
                require_js(array('yui_yahoo', 'yui_dom', 'yui_event'));
                require_js($CFG->admin . '/roles/roles.js');
                print_js_call('cap_table_filter.init',
                        array($this->id, get_string('search'), get_string('clear')));
            }
    }

    /**
     * Used to output a heading rows when the context level or component changes.
     * @param object $capability gives the new component and contextlevel.
     */
    protected function print_heading_row($capability) {
        echo '<tr class="rolecapheading header"><td colspan="' . (1 + $this->num_extra_columns()) . '" class="header"><strong>' .
                get_component_string($capability->component, $capability->contextlevel) .
                '</strong></td></tr>';

    }

    /** For subclasses to override, output header cells, after the initial capability one. */
    protected abstract function add_header_cells();

    /** For subclasses to override, return the number of cells that add_header_cells/add_row_cells output. */
    protected abstract function num_extra_columns();

    /**
     * For subclasses to override. Allows certain capabilties (e.g. legacy capabilities)
     * to be left out of the table.
     *
     * @param object $capability the capability this row relates to.
     * @return boolean. If true, this row is omitted from the table.
     */
    protected function skip_row($capability) {
        return false;
    }

    /**
     * For subclasses to override. A change to reaturn class names that are added
     * to the class="" attribute on the &lt;tr> for this capability.
     *
     * @param object $capability the capability this row relates to.
     * @return array of class name strings.
     */
    protected function get_row_classes($capability) {
        return array();
    }

    /**
     * For subclasses to override. Output the data cells for this capability. The
     * capability name cell will already have been output.
     *
     * You can rely on get_row_classes always being called before add_row_cells.
     *
     * @param object $capability the capability this row relates to.
     */
    protected abstract function add_row_cells($capability);
}

/**
 * Subclass of capability_table_base for use on the Check permissions page.
 *
 * We have two additional columns, Allowed, which contains yes/no, and Explanation,
 * which contains a pop-up link to explainhascapability.php.
 */
class explain_capability_table extends capability_table_base {
    protected $user;
    protected $fullname;
    protected $baseurl;
    protected $contextname;
    protected $stryes;
    protected $strno;
    protected $strexplanation;
    private $hascap;

    /**
     * Constructor
     * @param object $context the context this table relates to.
     * @param object $user the user we are generating the results for.
     * @param string $contextname print_context_name($context) - to save recomputing.
     */
    public function __construct($context, $user, $contextname) {
        global $CFG;
        parent::__construct($context, 'explaincaps');
        $this->user = $user;
        $this->fullname = fullname($user);
        $this->contextname = $contextname;
        $this->baseurl = $CFG->wwwroot . '/' . $CFG->admin .
                '/roles/explainhascapabiltiy.php?user=' . $user->id .
                '&amp;contextid=' . $context->id . '&amp;capability=';
        $this->stryes = get_string('yes');
        $this->strno = get_string('no');
        $this->strexplanation = get_string('explanation');
    }

    protected function add_header_cells() {
        echo '<th>' . get_string('allowed', 'role') . '</th>';
        echo '<th>' . $this->strexplanation . '</th>';
    }

    protected function num_extra_columns() {
        return 2;
    }

    protected function skip_row($capability) {
        return $capability->name != 'moodle/site:doanything' && is_legacy($capability->name);
    }

    protected function get_row_classes($capability) {
        $this->hascap = has_capability($capability->name, $this->context, $this->user->id);
        if ($this->hascap) {
            return array('yes');
        } else {
            return array('no');
        }
    }

    protected function add_row_cells($capability) {
        if ($this->hascap) {
            $result = $this->stryes;
            $tooltip = 'whydoesuserhavecap';
        } else {
            $result = $this->strno;
            $tooltip = 'whydoesusernothavecap';
        }
        $a = new stdClass;
        $a->fullname = $this->fullname;
        $a->capability = $capability->name;
        $a->context = $this->contextname;
        echo '<td>' . $result . '</td>';
        echo '<td>';
        link_to_popup_window($this->baseurl . $capability->name, 'hascapabilityexplanation',
                $this->strexplanation, 600, 600, get_string($tooltip, 'role', $a));
        echo '</td>';
    }
}

/**
 * This subclass is the bases for both the define roles and override roles
 * pages. As well as adding the risks columns, this also provides generic
 * facilities for showing a certain number of permissions columns, and
 * recording the current and submitted permissions for each capability.
 */
abstract class capability_table_with_risks extends capability_table_base {
    protected $allrisks;
    protected $allpermissions; // We don't need perms ourself, but all our subclasses do.
    protected $strperms; // Language string cache.
    protected $risksurl; // URL in moodledocs about risks.
    protected $riskicons = array(); // Cache to avoid regenerating the HTML for each risk icon.
    /** The capabilities to highlight as default/interited. */
    protected $parentpermissions;
    protected $displaypermissions;
    protected $permissions;
    protected $changed;

    public function __construct($context, $id) {
        parent::__construct($context, $id);

        $this->allrisks = get_all_risks();
        $this->risksurl = get_docs_url(s(get_string('risks', 'role')));

        $this->allpermissions = array(
            CAP_INHERIT => 'inherit',
            CAP_ALLOW => 'allow',
            CAP_PREVENT => 'prevent' ,
            CAP_PROHIBIT => 'prohibit',
        );

        $this->strperms = array();
        foreach ($this->allpermissions as $permname) {
            $this->strperms[$permname] =  get_string($permname, 'role');
        }

        $this->load_current_permissions();

    /// Fill in any blank permissions with an explicit CAP_INHERIT, and init a locked field.
        foreach ($this->capabilities as $capid => $cap) {
            if (!isset($this->permissions[$cap->name])) {
                $this->permissions[$cap->name] = CAP_INHERIT;
            }
            $this->capabilities[$capid]->locked = false;
        }
    }

    protected function load_current_permissions() {
        global $DB;

    /// Load the overrides/definition in this context.
        $this->permissions = $DB->get_records_menu('role_capabilities', array('roleid' => $this->roleid,
                'contextid' => $this->context->id), '', 'capability,permission');
    }

    protected abstract function load_parent_permissions();

    public abstract function save_changes();

    public function read_submitted_permissions() {
        /// Update $this->permissions based on submitted data.
        foreach ($this->capabilities as $cap) {
            if ($cap->locked || $this->skip_row($cap)) {
            /// The user is not allowed to change the permission for this capapability
                continue;
            }

            $permission = optional_param($cap->name, null, PARAM_PERMISSION);
            if (is_null($permission)) {
            /// A permission was not specified in submitted data.
                continue;
            }

        /// If the permission has changed, update $this->permissions and
        /// Record the fact there is data to save.
            if ($this->permissions[$cap->name] != $permission) {
                $this->permissions[$cap->name] = $permission;
                $this->changed[] = $cap->name;
            }
        }
    }

    public function display() {
        $this->load_parent_permissions();
        foreach ($this->capabilities as $cap) {
            if (!isset($this->parentpermissions[$cap->name])) {
                $this->parentpermissions[$cap->name] = CAP_INHERIT;
            }
        }
        parent::display();
    }

    protected function add_header_cells() {
        echo '<th colspan="' . count($this->displaypermissions) . '" scope="col">' . get_string('permission', 'role') . '</th>';
        echo '<th class="risk" colspan="' . count($this->allrisks) . '" scope="col">' . get_string('risks','role') . '</th>';
    }

    protected function num_extra_columns() {
        return count($this->displaypermissions) + count($this->allrisks);
    }

    protected function get_row_classes($capability) {
        $rowclasses = array();
        foreach ($this->allrisks as $riskname => $risk) {
            if ($risk & (int)$capability->riskbitmask) {
                $rowclasses[] = $riskname;
            }
        }
        return $rowclasses;
    }

    protected abstract function add_permission_cells($capability);

    protected function add_row_cells($capability) {
        $this->add_permission_cells($capability);
    /// One cell for each possible risk.
        foreach ($this->allrisks as $riskname => $risk) {
            echo '<td class="risk ' . str_replace('risk', '', $riskname) . '">';
            if ($risk & (int)$capability->riskbitmask) {
                echo $this->get_risk_icon($riskname);
            }
            echo '</td>';
        }
    }

    /**
     * Print a risk icon, as a link to the Risks page on Moodle Docs.
     *
     * @param string $type the type of risk, will be one of the keys from the
     *      get_all_risks array. Must start with 'risk'.
     */
    function get_risk_icon($type) {
        global $CFG;
        if (!isset($this->riskicons[$type])) {
            $iconurl = $CFG->pixpath . '/i/' . str_replace('risk', 'risk_', $type) . '.gif';
            $this->riskicons[$type] = link_to_popup_window($this->risksurl, 'docspopup',
                    '<img src="' . $iconurl . '" alt="' . get_string($type . 'short', 'admin') . '" />',
                    0, 0, get_string($type, 'admin'), null, true);
        }
        return $this->riskicons[$type];
    }
}

class define_role_table_advanced extends capability_table_with_risks {
    protected $roleid;

    public function __construct($context, $roleid) {
        $this->roleid = $roleid;
        parent::__construct($context, 'defineroletable');
        $this->displaypermissions = $this->allpermissions;
        $this->strperms[$this->allpermissions[CAP_INHERIT]] = get_string('notset', 'role');
    }

    protected function load_current_permissions() {
        if (!$this->roleid) {
            $this->permissions = array();
        } else {
            parent::load_current_permissions();
        }
    }

    protected function load_parent_permissions() {
    /// Get the default permissions, based on legacy role type.
        // TODO
        if ($this->roleid) {
            $legacy = get_legacy_type($this->roleid);
        } else {
            $legacy = '';
        }
        if (!empty($legacy)) {
            $this->parentpermissions = get_default_capabilities($legacy);
        } else {
            $this->parentpermissions = array();
        }
    }

    /**
     * Save any overrides that have been changed.
     */
    public function save_changes() {
        foreach ($this->changed as $changedcap) {
            assign_capability($changedcap, $this->permissions[$changedcap],
                    $this->roleid, $this->context->id, true);
        }

        // force accessinfo refresh for users visiting this context...
        mark_context_dirty($this->context->path);
    }

    protected function skip_row($capability) {
        return is_legacy($capability->name);
    }

    protected function add_permission_cells($capability) {
    /// One cell for each possible permission.
        foreach ($this->displaypermissions as $perm => $permname) {
            $strperm = $this->strperms[$permname];
            $extraclass = '';
            if ($perm == $this->parentpermissions[$capability->name]) {
                $extraclass = ' capdefault';
            }
            $checked = '';
            if ($this->permissions[$capability->name] == $perm) {
                $checked = ' checked="checked"';
            }
            echo '<td class="' . $permname . $extraclass . '">';
            echo '<label><input type="radio" name="' . $capability->name .
                    '" value="' . $perm . '"' . $checked . ' /> ';
            echo '<span class="note">' . $strperm . '</span>';
            echo '</label></td>';
        }
    }
}

class define_role_table_basic extends define_role_table_advanced {
    protected $stradvmessage;
    protected $strallow;

    public function __construct($context, $roleid) {
        parent::__construct($context, $roleid);
        $this->displaypermissions = array(CAP_ALLOW => $this->allpermissions[CAP_ALLOW]);
        $this->stradvmessage = get_string('useshowadvancedtochange', 'role');
        $this->strallow = $this->strperms[$this->allpermissions[CAP_ALLOW]];
    }

    protected function add_permission_cells($capability) {
        $perm = $this->permissions[$capability->name];
        $permname = $this->allpermissions[$perm];
        echo '<td class="' . $permname . '">';
        if ($perm == CAP_ALLOW || $perm == CAP_INHERIT) {
            $checked = '';
            if ($perm == CAP_ALLOW) {
                $checked = 'checked="checked" ';
            }
            echo '<input type="hidden" name="' . $capability->name . '" value="' . CAP_INHERIT . '" />';
            echo '<label><input type="checkbox" name="' . $capability->name .
                    '" value="' . CAP_ALLOW . '"' . $checked . ' /> ' . $this->strallow . '</label>';
        } else {
            echo '<input type="hidden" name="' . $capability->name . '" value="' . $perm . '" />';
            echo $this->strperms[$permname] . '<span class="note">' . $this->stradvmessage . '</span>';
        }
        echo '</td>';
    }
}
class view_role_definition_table extends define_role_table_advanced {
    public function __construct($context, $roleid) {
        parent::__construct($context, $roleid);
        $this->displaypermissions = array(CAP_ALLOW => $this->allpermissions[CAP_ALLOW]);
    }

    protected function add_permission_cells($capability) {
        $perm = $this->permissions[$capability->name];
        $permname = $this->allpermissions[$perm];
        echo '<td class="' . $permname . '">' . $this->strperms[$permname] . '</td>';

    }
}

class override_permissions_table_advanced extends capability_table_with_risks {
    protected $roleid;
    protected $strnotset;
    protected $haslockedcapabiltites = false;

    /**
     * Constructor
     *
     * This method loads loads all the information about the current state of
     * the overrides, then updates that based on any submitted data. It also
     * works out which capabilities should be locked for this user.
     *
     * @param object $context the context this table relates to.
     * @param integer $roleid the role being overridden.
     * @param boolean $safeoverridesonly If true, the user is only allowed to override
     *      capabilities with no risks.
     */
    public function __construct($context, $roleid, $safeoverridesonly) {
        $this->roleid = $roleid;
        parent::__construct($context, 'overriderolestable');
        $this->displaypermissions = $this->allpermissions;
        $this->strnotset = get_string('notset', 'role');

    /// Determine which capabilities should be locked.
        if ($safeoverridesonly) {
            foreach ($this->capabilities as $capid => $cap) {
                if (!is_safe_capability($capability)) {
                    $this->capabilities[$capid]->locked = true;
                    $this->haslockedcapabiltites = true;
                }
            }
        }
    }

    protected function load_parent_permissions() {
        global $DB;

    /// Get the capabiltites from the parent context, so that can be shown in the interface.
        $parentcontext = get_context_instance_by_id(get_parent_contextid($this->context));
        $this->parentpermissions = role_context_capabilities($this->roleid, $parentcontext);
    }

    /**
     * Save any overrides that have been changed.
     */
    public function save_changes() {
        foreach ($this->changed as $changedcap) {
            assign_capability($changedcap, $this->permissions[$changedcap],
                    $this->roleid, $this->context->id, true);
        }

        // force accessinfo refresh for users visiting this context...
        mark_context_dirty($this->context->path);
    }

    public function has_locked_capabiltites() {
        return $this->haslockedcapabiltites;
    }

    protected function skip_row($capability) {
        return is_legacy($capability->name);
    }

    protected function add_permission_cells($capability) {
        $disabled = '';
        if ($capability->locked || $this->parentpermissions[$capability->name] == CAP_PROHIBIT) {
            $disabled = ' disabled="disabled"';
        }

    /// One cell for each possible permission.
        foreach ($this->displaypermissions as $perm => $permname) {
            $strperm = $this->strperms[$permname];
            $extraclass = '';
            if ($perm != CAP_INHERIT && $perm == $this->parentpermissions[$capability->name]) {
                $extraclass = ' capcurrent';
            }
            $checked = '';
            if ($this->permissions[$capability->name] == $perm) {
                $checked = ' checked="checked"';
            }
            echo '<td class="' . $permname . $extraclass . '">';
            echo '<label><input type="radio" name="' . $capability->name .
                    '" value="' . $perm . '"' . $checked . $disabled . ' /> ';
            if ($perm == CAP_INHERIT) {
                $inherited = $this->parentpermissions[$capability->name];
                if ($inherited == CAP_INHERIT) {
                    $inherited = $this->strnotset;
                } else {
                    $inherited = $this->strperms[$this->allpermissions[$inherited]];
                }
                $strperm .= ' (' . $inherited . ')';
            }
            echo '<span class="note">' . $strperm . '</span>';
            echo '</label></td>';
        }
    }
}

class override_permissions_table_basic extends override_permissions_table_advanced {
    protected $stradvmessage;

    public function __construct($context, $roleid, $safeoverridesonly) {
        parent::__construct($context, $roleid, $safeoverridesonly);
        unset($this->displaypermissions[CAP_PROHIBIT]);
        $this->stradvmessage = get_string('useshowadvancedtochange', 'role');
    }

    protected function add_permission_cells($capability) {
        if ($this->permissions[$capability->name] == CAP_PROHIBIT) {
            $permname = $this->allpermissions[CAP_PROHIBIT];
            echo '<td class="' . $permname . '" colspan="' . count($this->displaypermissions) . '">';
            echo '<input type="hidden" name="' . $capability->name . '" value="' . CAP_PROHIBIT . '" />';
            echo $this->strperms[$permname] . '<span class="note">' . $this->stradvmessage . '</span>';
            echo '</td>';
        } else {
            parent::add_permission_cells($capability);
        }
    }
}

// User selectors for managing role assignments ================================

/**
 * Base class to avoid duplicating code.
 */
abstract class role_assign_user_selector_base extends user_selector_base {
    const MAX_USERS_PER_PAGE = 100;

    protected $roleid;
    protected $context;

    /**
     * @param string $name control name
     * @param array $options should have two elements with keys groupid and courseid.
     */
    public function __construct($name, $options) {
        global $CFG;
        parent::__construct($name, $options);
        $this->roleid = $options['roleid'];
        if (isset($options['context'])) {
            $this->context = $options['context'];
        } else {
            $this->context = get_context_instance_by_id($options['contextid']);
        }
        require_once($CFG->dirroot . '/group/lib.php');
    }

    protected function get_options() {
        global $CFG;
        $options = parent::get_options();
        $options['file'] = $CFG->admin . '/roles/lib.php';
        $options['roleid'] = $this->roleid;
        $options['contextid'] = $this->context->id;
        return $options;
    }
}

/**
 * User selector subclass for the list of potential users on the assign roles page,
 * when we are assigning in a context below the course level. (CONTEXT_MODULE and
 * CONTEXT_BLOCK).
 *
 * In this case we replicate part of get_users_by_capability() get the users
 * with moodle/course:view (or moodle/site:doanything). We can't use
 * get_users_by_capability() becuase
 *   1) get_users_by_capability() does not deal with searching by name
 *   2) exceptions array can be potentially large for large courses
 */
class potential_assignees_below_course extends role_assign_user_selector_base {
    public function find_users($search) {
        global $DB;

        // Get roles with some assignement to the 'moodle/course:view' capability.
        $possibleroles = get_roles_with_capability('moodle/course:view', CAP_ALLOW, $this->context);
        if (empty($possibleroles)) {
            // If there aren't any, we are done.
            return array();
        }

        // Now exclude the admin roles, and check the actual permission on
        // 'moodle/course:view' to make sure it is allow.
        $doanythingroles = get_roles_with_capability('moodle/site:doanything',
                CAP_ALLOW, get_context_instance(CONTEXT_SYSTEM));
        $validroleids = array();

        foreach ($possibleroles as $possiblerole) {
            if (isset($doanythingroles[$possiblerole->id])) {
                    continue;
            }

            if ($caps = role_context_capabilities($possiblerole->id, $this->context, 'moodle/course:view')) { // resolved list
                if (isset($caps['moodle/course:view']) && $caps['moodle/course:view'] > 0) { // resolved capability > 0
                    $validroleids[] = $possiblerole->id;
                }
            }
        }

        // If there are no valid roles, we are done.
        if (!$validroleids) {
            return array();
        }

        // Now we have to go to the database.
        list($wherecondition, $params) = $this->search_sql($search, 'u');
        if ($wherecondition) {
            $wherecondition = ' AND ' . $wherecondition;
        }
        $roleids =  '('.implode(',', $validroleids).')';

        $fields      = 'SELECT ' . $this->required_fields_sql('u');
        $countfields = 'SELECT COUNT(1)';

        $sql   = " FROM {user} u
                   JOIN {role_assignments} ra ON ra.userid = u.id
                   JOIN {role} r ON r.id = ra.roleid
                  WHERE ra.contextid " . get_related_contexts_string($this->context)."
                        $wherecondition
                        AND ra.roleid IN $roleids
                        AND u.id NOT IN (
                           SELECT u.id
                             FROM {role_assignments} r, {user} u
                            WHERE r.contextid = ?
                                  AND u.id = r.userid
                                  AND r.roleid = ?)";
        $order = ' ORDER BY lastname ASC, firstname ASC';

        $params[] = $this->context->id;
        $params[] = $this->roleid;

        // Check to see if there are too many to show sensibly.
        if (!$this->is_validating()) {
            $potentialmemberscount = $DB->count_records_sql($countfields . $sql, $params);
            if ($potentialmemberscount > role_assign_user_selector_base::MAX_USERS_PER_PAGE) {
                return $this->too_many_results($search, $potentialmemberscount);
            }
        }

        // If not, show them.
        $availableusers = $DB->get_records_sql($fields . $sql . $order, $params);

        if (empty($availableusers)) {
            return array();
        }

        if ($search) {
            $groupname = get_string('potusersmatching', 'role', $search);
        } else {
            $groupname = get_string('potusers', 'role');
        }

        return array($groupname => $availableusers);
    }
}

/**
 * User selector subclass for the list of potential users on the assign roles page,
 * when we are assigning in a context at or above the course level. In this case we
 * show all the users in the system who do not already have the role.
 */
class potential_assignees_course_and_above extends role_assign_user_selector_base {
    public function find_users($search) {
        global $DB;

        list($wherecondition, $params) = $this->search_sql($search, '');

        $fields      = 'SELECT ' . $this->required_fields_sql('');
        $countfields = 'SELECT COUNT(1)';

        $sql = " FROM {user}
                WHERE $wherecondition
                      AND id NOT IN (
                         SELECT u.id
                           FROM {role_assignments} r, {user} u
                          WHERE r.contextid = ?
                                AND u.id = r.userid
                                AND r.roleid = ?)";
        $order = ' ORDER BY lastname ASC, firstname ASC';

        $params[] = $this->context->id;
        $params[] = $this->roleid;

        if (!$this->is_validating()) {
            $potentialmemberscount = $DB->count_records_sql($countfields . $sql, $params);
            if ($potentialmemberscount > role_assign_user_selector_base::MAX_USERS_PER_PAGE) {
                return $this->too_many_results($search, $potentialmemberscount);
            }
        }

        $availableusers = $DB->get_records_sql($fields . $sql . $order, $params);

        if (empty($availableusers)) {
            return array();
        }

        if ($search) {
            $groupname = get_string('potusersmatching', 'role', $search);
        } else {
            $groupname = get_string('potusers', 'role');
        }

        return array($groupname => $availableusers);
    }
}

/**
 * User selector subclass for the list of users who already have the role in
 * question on the assign roles page.
 */
class existing_role_holders extends role_assign_user_selector_base {
    protected $strhidden;

    public function __construct($name, $options) {
        parent::__construct($name, $options);
        $this->strhidden = get_string('hiddenassign');
    }

    public function find_users($search) {
        list($wherecondition, $params) = $this->search_sql($search, 'u');
        $contextusers = get_role_users($this->roleid, $this->context, false,
                $this->required_fields_sql('u') . ', ra.hidden', 'u.lastname, u.firstname',
                true, '', '', '', $wherecondition, $params);

        if (empty($contextusers)) {
            return array();
        }

        if ($search) {
            $groupname = get_string('extusersmatching', 'role', $search);
        } else {
            $groupname = get_string('extusers', 'role');
        }

        return array($groupname => $contextusers);
    }

    // Override to add (hidden) to hidden role assignments.
    public function output_user($user) {
        $output = parent::output_user($user);
        if ($user->hidden) {
            $output .= ' (' . $this->strhidden . ')';
        }
        return $output;
    }
}

/**
 * A special subclass to use when unassigning admins at site level. Disables
 * the option for admins to unassign themselves.
 */
class existing_role_holders_site_admin extends existing_role_holders {
    public function find_users($search) {
        global $USER;
        $groupedusers = parent::find_users($search);
        foreach ($groupedusers as $group) {
            foreach ($group as &$user) {
                if ($user->id == $USER->id) {
                    $user->disabled = true;
                }
            }
        }
        return $groupedusers;
    }
}

?>