<?php  // $Id$

require_once('HTML/QuickForm/element.php');

//TODO:
//  * locking
//  * freezing
//  * ajax format conversion
//  * better area files handling

class MoodleQuickForm_editor extends HTML_QuickForm_element {
    protected $_helpbutton = '';
    protected $_options    = array('subdirs'=>0, 'maxbytes'=>0, 'maxfiles'=>0, 'changeformat'=>0);
    protected $_values     = array('text'=>null, 'format'=>null, 'itemid'=>null);

    function MoodleQuickForm_editor($elementName=null, $elementLabel=null, $attributes=null, $options=null) {
        global $CFG;

        $options = (array)$options;
        foreach ($options as $name=>$value) {
            if (array_key_exists($name, $this->_options)) {
                $this->_options[$name] = $value;
            }
        }
        if (!empty($options['maxbytes'])) {
            $this->_options['maxbytes'] = get_max_upload_file_size($CFG->maxbytes, $options['maxbytes']);
        }
        parent::HTML_QuickForm_element($elementName, $elementLabel, $attributes);
    }

    function setName($name) {
        $this->updateAttributes(array('name'=>$name));
    }

    function getName() {
        return $this->getAttribute('name');
    }

    function setValue($values) {
        $values = (array)$values;
        foreach ($values as $name=>$value) {
            if (array_key_exists($name, $this->_values)) {
                $this->_values[$name] = $value;
            }
        }
    }

    function getValue() {
        return $this->getAttribute('value');
    }

    function getMaxbytes() {
        return $this->_options['maxbytes'];
    }

    function setMaxbytes($maxbytes) {
        global $CFG;
        $this->_options['maxbytes'] = get_max_upload_file_size($CFG->maxbytes, $maxbytes);
    }

    function getMaxfiles() {
        return $this->_options['maxfiles'];
    }

    function setMaxfiles($num) {
        $this->_options['maxfiles'] = $num;
    }

    function getSubdirs() {
        return $this->_options['subdirs'];
    }

    function setSubdirs($allow) {
        $this->_options['subdirs'] = $allow;
    }

    function setHelpButton($_helpbuttonargs, $function='_helpbutton') {
        if (!is_array($_helpbuttonargs)) {
            $_helpbuttonargs = array($_helpbuttonargs);
        } else {
            $_helpbuttonargs = $_helpbuttonargs;
        }
        //we do this to to return html instead of printing it
        //without having to specify it in every call to make a button.
        if ('_helpbutton' == $function){
            $defaultargs = array('', '', 'moodle', true, false, '', true);
            $_helpbuttonargs = $_helpbuttonargs + $defaultargs ;
        }
        $this->_helpbutton=call_user_func_array($function, $_helpbuttonargs);
    }

    function getHelpButton() {
        return $this->_helpbutton;
    }

    function getElementTemplateType() {
        if ($this->_flagFrozen){
            return 'nodisplay';
        } else {
            return 'default';
        }
    }

    function toHtml() {
        global $CFG, $COURSE;

        if ($this->_flagFrozen) {
            return $this->getFrozenHtml();
        }

        $id           = $this->_attributes['id'];
        $elname       = $this->_attributes['name'];

        $subdirs      = $this->_options['subdirs'];
        $maxbytes     = $this->_options['maxbytes'];
        $maxfiles     = $this->_options['maxfiles'];
        $changeformat = $this->_options['changeformat']; // TO DO: implement as ajax calls

        $text         = $this->_values['text'];
        $format       = $this->_values['format'];
        $draftitemid  = $this->_values['itemid'];

        // security - never ever allow guest/not logged in user to upload anything
        if (isguestuser() or !isloggedin()) {
            $maxfiles = 0;
        }

        $str = $this->_getTabs();
        $str .= '<div>';

        $editor = get_preferred_texteditor($format);
        $strformats = format_text_menu();
        $formats =  $editor->get_supported_formats();
        foreach ($formats as $fid) {
            $formats[$fid] = $strformats[$fid];
        }

    /// print text area - TODO: add on-the-fly switching, size configuration, etc.
        $editorclass = $editor->get_editor_element_class();
        $editor->use_editor($id);

        $str .= '<div><textarea class="'.$editorclass.'" id="'.$id.'" name="'.$elname.'[text]" rows="15" cols="80">';
        $str .= s($text);
        $str .= '</textarea></div>';

        $str .= '<div>';
        $str .= '<select name="'.$elname.'[format]">';
        foreach ($formats as $key=>$desc) {
            $selected = ($format == $key) ? 'selected="selected"' : '';
            $str .= '<option value="'.s($key).'" '.$selected.'>'.$desc.'</option>';
        }
        $str .= '</select>';
        $str .= '</div>';

        if ($maxfiles != 0 ) { // 0 means no files, -1 unlimited
            if (empty($draftitemid)) {
                // no existing area info provided - let's use fresh new draft area
                require_once("$CFG->libdir/filelib.php");
                $this->setValue(array('itemid'=>file_get_unused_draft_itemid()));
                $draftitemid = $this->_values['itemid'];
            }
            $str .= '<div><input type="hidden" name="'.$elname.'[itemid]" value="'.$draftitemid.'" /></div>';

        /// embedded image files - TODO: hide on the fly when switching editors
            $str .= '<div id="'.$id.'_filemanager">';
            $editorurl = "$CFG->wwwroot/files/draftfiles.php?itemid=$draftitemid&amp;subdirs=$subdirs&amp;maxbytes=$maxbytes";
            $str .= '<object type="text/html" data="'.$editorurl.'" height="160" width="600" style="border:1px solid #000">Error</object>'; // TODO: localise, fix styles, etc.
            $str .= '</div>';

        require_once($CFG->dirroot.'/repository/lib.php');
        if (empty($COURSE->context)) {
            $ctx = get_context_instance(CONTEXT_SYSTEM);
        } else {
            $ctx = $COURSE->context;
        }
        $client_id = uniqid();
        $ret = repository_get_client($ctx, $client_id, array('image', 'video', 'media'), '*');

        $str .= $ret['css'].$ret['js'];
        $str .= <<<EOD
<script type="text/javascript">
id2clientid['$id'] = '$client_id';
id2itemid['$id']   = '$draftitemid';
</script>
EOD;

        if ($editor->supports_repositories()) {
            $str .= <<<EOD
<script type="text/javascript">
//<![CDATA[
var fileman = document.getElementById("{$id}_filemanager");
fileman.style.visibility = "hidden";
fileman.style.height = "0";
//]]>
</script>
EOD;
            }
        }

        $str .= '</div>';

        return $str;
    }

}