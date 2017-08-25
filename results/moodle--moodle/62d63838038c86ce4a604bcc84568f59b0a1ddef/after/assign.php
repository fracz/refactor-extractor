<?php
/**
 * Add/remove group from grouping.
 * @package groups
 */
require_once('../config.php');
require_once('lib.php');

$groupingid = required_param('id', PARAM_INT);

if (!$grouping = get_record('groupings', 'id', $groupingid)) {
    error('Incorrect group id');
}

if (! $course = get_record('course', 'id', $grouping->courseid)) {
    print_error('invalidcourse');
}
$courseid = $course->id;

require_login($course);
$context = get_context_instance(CONTEXT_COURSE, $courseid);
require_capability('moodle/course:managegroups', $context);

$returnurl = $CFG->wwwroot.'/group/groupings.php?id='.$courseid;


if ($frm = data_submitted() and confirm_sesskey()) {

    if (isset($frm->cancel)) {
        redirect($returnurl);

    } else if (isset($frm->add) and !empty($frm->addselect)) {
        foreach ($frm->addselect as $groupid) {
            $groupid = (int)$groupid;
            if (record_exists('groupings_groups', 'groupingid', $grouping->id, 'groupid', $groupid)) {
                continue;
            }
            $assign = new object();
            $assign->groupingid = $grouping->id;
            $assign->groupid = $groupid;
            $assign->timeadded = time();
            insert_record('groupings_groups', $assign);
        }

    } else if (isset($frm->remove) and !empty($frm->removeselect)) {

        foreach ($frm->removeselect as $groupid) {
            $groupid = (int)$groupid;
            delete_records('groupings_groups', 'groupingid', $grouping->id, 'groupid', $groupid);
        }
    }
}


$currentmembers = array();
$potentialmembers  = array();

if ($groups = get_records('groups', 'courseid', $courseid, 'name')) {
    if ($assignment = get_records('groupings_groups', 'groupingid', $grouping->id)) {
        foreach ($assignment as $ass) {
            $currentmembers[$ass->groupid] = $groups[$ass->groupid];
            unset($groups[$ass->groupid]);
        }
    }
    $potentialmembers = $groups;
}

$currentmembersoptions = '';
$currentmemberscount = 0;
if ($currentmembers) {
    foreach($currentmembers as $group) {
        $currentmembersoptions .= '<option value="'.$group->id.'.">'.format_string($group->name).'</option>';
        $currentmemberscount ++;
    }
} else {
    $currentmembersoptions .= '<option>&nbsp;</option>';
}

$potentialmembersoptions = '';
$potentialmemberscount = 0;
if ($potentialmembers) {
    foreach($potentialmembers as $group) {
        $potentialmembersoptions .= '<option value="'.$group->id.'.">'.format_string($group->name).'</option>';
        $potentialmemberscount ++;
    }
} else {
    $potentialmembersoptions .= '<option>&nbsp;</option>';
}

// Print the page and form
$strgroups = get_string('groups');
$strparticipants = get_string('participants');

$groupingname = format_string($grouping->name);

print_header("$course->shortname: $strgroups",
             $course->fullname,
             "<a href=\"$CFG->wwwroot/course/view.php?id=$courseid\">$course->shortname</a> ".
             "-> <a href=\"$CFG->wwwroot/user/index.php?id=$courseid\">$strparticipants</a> ".
             "-> <a href=\"$CFG->wwwroot/group/index.php?id=$courseid\">$strgroups</a>".
             '-> '. get_string('addgroupstogroupings', 'group'), '', '', true, '', user_login_string($course, $USER));

?>
<div id="addmembersform">
    <h3 class="main"><?php print_string('addgroupstogroupings', 'group'); echo ": $groupingname"; ?></h3>

    <form id="assignform" method="post" action="">
    <div>
    <input type="hidden" name="sesskey" value="<?php p(sesskey()); ?>" />
    <input type="hidden" name="group" value="<?php echo $groupid; ?>" />

    <table summary="" cellpadding="5" cellspacing="0">
    <tr>
      <td valign="top">
          <label for="removeselect"><?php print_string('existingmembers', 'group', $currentmemberscount); //count($contextusers) ?></label>
          <br />
          <select name="removeselect[]" size="20" id="removeselect" multiple="multiple"
                  onfocus="document.getElementById('assignform').add.disabled=true;
                           document.getElementById('assignform').remove.disabled=false;
                           document.getElementById('assignform').addselect.selectedIndex=-1;">
          <?php echo $currentmembersoptions ?>
          </select></td>
      <td valign="top">
<?php // Hidden assignment? ?>

        <?php check_theme_arrows(); ?>
        <p class="arrow_button">
            <input name="add" id="add" type="submit" value="<?php echo '&nbsp;'.$THEME->larrow.' &nbsp; &nbsp; '.get_string('add'); ?>" title="<?php print_string('add'); ?>" />
            <br />
            <input name="remove" id="remove" type="submit" value="<?php echo '&nbsp; '.$THEME->rarrow.' &nbsp; &nbsp; '.get_string('remove'); ?>" title="<?php print_string('remove'); ?>" />
        </p>
      </td>
      <td valign="top">
          <label for="addselect"><?php print_string('potentialmembers', 'group', $potentialmemberscount); //$usercount ?></label>
          <br />
          <select name="addselect[]" size="20" id="addselect" multiple="multiple"
                  onfocus="document.getElementById('assignform').add.disabled=false;
                           document.getElementById('assignform').remove.disabled=true;
                           document.getElementById('assignform').removeselect.selectedIndex=-1;">
         <?php echo $potentialmembersoptions ?>
         </select>
         <br />
       </td>
    </tr>
    <tr><td>
        <input type="submit" name="cancel" value="<?php print_string('backtogroupings', 'group'); ?>" />
    </td></tr>
    </table>
    </div>
    </form>
</div>

<?php
    print_footer($course);


?>