<?php // $Id$

/**
 * Library of functions and constants for notes
 */

/**
 * Constants for ratings.
 */
define('NOTES_RATING_LOW', '1');
define('NOTES_RATING_BELOWNORMAL', '2');
define('NOTES_RATING_NORMAL', '3');
define('NOTES_RATING_ABOVENORMAL', '4');
define('NOTES_RATING_HIGH', '5');

/**
 * Constants for states.
 */
define('NOTES_STATE_DRAFT', 'draft');
define('NOTES_STATE_PUBLIC', 'public');
define('NOTES_STATE_SITE', 'site');

/**
 * Constants for note parts (flags used by note_print and note_print_list).
 */
define('NOTES_SHOW_FULL', 0x07);
define('NOTES_SHOW_HEAD', 0x02);
define('NOTES_SHOW_BODY', 0x01);
define('NOTES_SHOW_FOOT', 0x04);

/**
 * Retrieves a list of note objects with specific atributes.
 *
 * @param int    $courseid id of the course in which the notes were posted (0 means any)
 * @param int    $userid id of the user to which the notes refer (0 means any)
 * @param string $state state of the notes (i.e. draft, public, site) ('' means any)
 * @param int    $author id of the user who modified the note last time (0 means any)
 * @param string $order an order to sort the results in
 * @param int    $limitfrom number of records to skip (offset)
 * @param int    $limitnum number of records to fetch
 * @return array of note objects
 */
function note_list($courseid=0, $userid=0, $state = '', $author = 0, $order='lastmodified DESC', $limitfrom=0, $limitnum=0) {
    // setup filters
    $select = array();
    if($courseid) {
        $selects[] = 'courseid=' . $courseid;
    }
    if($userid) {
        $selects[] = 'userid=' . $userid;
    }
    if($author) {
        $selects[] = 'usermodified=' . $author;
    }
    if($state) {
        $selects[] = 'publishstate="' . $state . '"';
    }
    $selects[] = 'module="notes"';
    $select = implode(' AND ', $selects);
    $fields = 'id,courseid,userid,content,format,rating,created,lastmodified,usermodified,publishstate';
    // retrieve data
    $rs =& get_recordset_select('post', $select, $order, $fields, $limitfrom, $limitnum);
    return recordset_to_array($rs);
}

/**
 * Retrieves a note object based on its id.
 *
 * @param int    $note_id id of the note to retrieve
 * @return note object
 */
function note_load($note_id) {
    $fields = 'id,courseid,userid,content,format,rating,created,lastmodified,usermodified,publishstate';
    return get_record_select('post', 'id=' . $note_id . ' AND module="notes"', $fields);
}

/**
 * Saves a note object. The note object is passed by reference and its fields (i.e. id)
 * might change during the save.
 *
 * @param note   $note object to save
 * @return boolean true if the object was saved; false otherwise
 */
function note_save(&$note) {
    global $USER;
    // setup & clean fields
    $note->module = 'notes';
    $note->lastmodified = time();
    $note->usermodified = $USER->id;
    if(empty($note->rating)) {
        $note->rating = NOTES_RATING_NORMAL;
    }
    if(empty($note->format)) {
        $note->format = FORMAT_PLAIN;
    }
    if(empty($note->publishstate)) {
        $note->publishstate = NOTES_STATE_PUBLIC;
    }
    // save data
    if(empty($note->id)) {
        // insert new note
        $note->created = $note->lastmodified;
        if($id = insert_record('post', $note)) {
            $note->id = $id;
            $result = true;
        } else {
            $result = false;
        }
    } else {
        // update old note
        $result = update_record('post', $note);
    }
    unset($note->module);
    return $result;
}

/**
 * Deletes a note object based on its id.
 *
 * @param int    $note_id id of the note to delete
 * @return boolean true if the object was deleted; false otherwise
 */
function note_delete($noteid) {
    return delete_records_select('post', 'id=' . $noteid . ' AND module="notes"');
}

/**
 * Converts a rating value to its corespondent name
 *
 * @param int    $rating rating value to convert
 * @return string corespondent rating name
 */
function note_get_rating_name($rating) {
    // cache rating names
    static $ratings;
    if (empty($ratings)) {
        $ratings =& note_get_rating_names();
    }
    return @$ratings[$rating];
}

/**
 * Returns an array of mappings from rating values to rating names
 *
 * @return array of mappings
 */
function note_get_rating_names() {
    return array(
        1 => get_string('low', 'notes'),
        2 => get_string('belownormal', 'notes'),
        3 => get_string('normal', 'notes'),
        4 => get_string('abovenormal', 'notes'),
        5 => get_string('high', 'notes'),
    );
}

/**
 * Converts a state value to its corespondent name
 *
 * @param string  $state state value to convert
 * @return string corespondent state name
 */
function note_get_state_name($state) {
    // cache state names
    static $states;
    if (empty($states)) {
        $states = note_get_state_names();
    }
    return @$states[$state];
}

/**
 * Returns an array of mappings from state values to state names
 *
 * @return array of mappings
 */
function note_get_state_names() {
    return array(
        NOTES_STATE_DRAFT => get_string('personal', 'notes'),
        NOTES_STATE_PUBLIC => get_string('course', 'notes'),
        NOTES_STATE_SITE => get_string('site', 'notes'),
    );
}

/**
 * Prints a note object
 *
 * @param note  $note the note object to print
 * @param int   $detail OR-ed NOTES_SHOW_xyz flags that specify which note parts to print
 */
function note_print($note, $detail = NOTES_SHOW_FULL) {

    global $CFG, $USER;
    $user = get_record('user','id',$note->userid);
    $context = get_context_instance(CONTEXT_COURSE, $note->courseid);
    $sitecontext = get_context_instance(CONTEXT_SYSTEM);
    $authoring->name = fullname(get_record('user','id',$note->usermodified));
    $authoring->date = userdate($note->lastmodified);
    echo '<div class="notepost '. $note->publishstate . 'notepost' .
        ($note->usermodified == $USER->id ? ' ownnotepost' : '')  .
        '" id="note-'. $note->id .'">';

    // print note head (e.g. author, user refering to, rating, etc)
    if($detail & NOTES_SHOW_HEAD) {
        echo '<div class="header">';
        echo '<div class="user">';
        print_user_picture($user->id, $note->courseid, $user->picture);
        echo fullname($user) . '</div>';
        echo '<div class="rating rating' . $note->rating . '">' . get_string('rating', 'notes') . ': ' . note_get_rating_name($note->rating) . '</div>';
        echo '<div class="info">' .
            get_string('bynameondate', 'notes', $authoring) .
            ' (' . get_string('created', 'notes') . ': ' . userdate($note->created) . ')</div>';
        echo '</div>';
    }

    // print note content
    if($detail & NOTES_SHOW_BODY) {
        echo '<div class="content">';
        echo format_text($note->content, $note->format);
        echo '</div>';
    }

    // print note options (e.g. delete, edit)
    if($detail & NOTES_SHOW_FOOT) {
        if (has_capability('moodle/notes:manage', $sitecontext) && $note->publishstate == NOTES_STATE_SITE ||
            has_capability('moodle/notes:manage', $context) && ($note->publishstate == NOTES_STATE_PUBLIC || $note->usermodified == $USER->id)) {
            echo '<div class="footer">';
            echo '<a href="'.$CFG->wwwroot.'/notes/edit.php?note='.$note->id. '">'. get_string('edit') .'</a>';
            echo '<a href="'.$CFG->wwwroot.'/notes/delete.php?note='.$note->id. '">'. get_string('delete') .'</a>';
            echo '</div>';
        }
    }
    echo '</div>';
}

/**
 * Prints a list of note objects
 *
 * @param array  $notes array of note objects to print
 * @param int   $detail OR-ed NOTES_SHOW_xyz flags that specify which note parts to print
 */
function note_print_list($notes, $detail = NOTES_SHOW_FULL) {

    /// Start printing of the note
    echo '<div class="notelist">';
    foreach ($notes as $note) {
        note_print($note, $detail);
    }
    echo '</div>';
}

/**
 * Retrieves and prints a list of note objects with specific atributes.
 *
 * @param string  $header HTML to print above the list
 * @param object  $context context in which the notes will be displayed (used to check capabilities)
 * @param int     $courseid id of the course in which the notes were posted (0 means any)
 * @param int     $userid id of the user to which the notes refer (0 means any)
 * @param string  $state state of the notes (i.e. draft, public, site) ('' means any)
 * @param int     $author id of the user who modified the note last time (0 means any)
 */
function note_print_notes($header, $context, $courseid = 0, $userid = 0, $state = '', $author = 0)
{
    global $CFG;
    if ($header) {
        echo '<h3 id="notestitle">' . $header . '</h3>';
    }
    if (has_capability('moodle/notes:view', $context)) {
        $notes =& note_list($courseid, $userid, $state, $author);
        if($notes) {
            note_print_list($notes);
        } else {
            echo '<p>' . get_string('nonotes', 'notes') . '</p>';
        }
    } else {
        echo '<p>' . get_string('notesnotvisible', 'notes') . '</p>';
    }
}