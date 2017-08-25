<?php
/**
 * repository_flickr_public class
 * This one is used to create public repository
 * You can set up a public account in admin page, so everyone can
 * access photos in this public account
 *
 * @author Dongsheng Cai <dongsheng@moodle.com>
 * @version $Id$
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 */

require_once($CFG->libdir.'/flickrlib.php');

/**
 *
 */
class repository_flickr_public extends repository {
    private $flickr;
    public $photos;

    /**
     *
     * @param <type> $options
     * @return <type>
     */
    public function set_option($options = array()) {
        if (!empty($options['api_key'])) {
            set_config('api_key', trim($options['api_key']), 'flickr_public');
        }
        unset($options['api_key']);
        $ret = parent::set_option($options);
        return $ret;
    }

    /**
     *
     * @param <type> $config
     * @return <type>
     */
    public function get_option($config = '') {
        if ($config==='api_key') {
            return trim(get_config('flickr_public', 'api_key'));
        } else {
            $options['api_key'] = trim(get_config('flickr_public', 'api_key'));
        }
        $options = parent::get_option($config);
        return $options;
    }

    /**
     *
     * @return <type>
     */
    public function global_search() {
        if (empty($this->flickr_account)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * @global <type> $CFG
     * @param <type> $repositoryid
     * @param <type> $context
     * @param <type> $options
     * @param <type> $readonly
     */
    public function __construct($repositoryid, $context = SITEID, $options = array(), $readonly=0) {
        global $CFG;
        $options['page'] = optional_param('p', 1, PARAM_INT);
        parent::__construct($repositoryid, $context, $options,$readonly);
        $this->api_key = $this->get_option('api_key');
        $this->flickr = new phpFlickr($this->api_key);
        $this->flickr_account = $this->get_option('email_address');

        // when flickr account hasn't been set by admin, user can
        // submit a flickr account here.
        $account = optional_param('flickr_account', '', PARAM_RAW);
        if (!empty($account)) {
            $people = $this->flickr->people_findByEmail($account);
            if (!empty($people)) {
                $this->flickr_account = $account;
                set_user_preference('flickr_mail_'.$this->id, $account);
            } else {
                throw new repository_exception('invalidemail', 'repository_flickr_public');
            }
        }

        $user_mail = get_user_preferences('flickr_mail_'.$this->id, '');
        if (empty($this->flickr_account) && !empty($user_mail)) {
            $this->flickr_account = $user_mail;
        }
    }

    /**
     *
     * @return <type>
     */
    public function check_login() {
        return !empty($this->flickr_account);
    }

    /**
     *
     * @param <type> $ajax
     * @return <type>
     */
    public function print_login($ajax = true) {
        if ($ajax) {
            $ret = array();
            $email_field->label = get_string('username', 'repository_flickr_public').': ';
            $email_field->id    = 'account';
            $email_field->type = 'text';
            $email_field->name = 'flickr_account';
            $ret['login'] = array($email_field);
            return $ret;
        }
    }

    /**
     *
     * @return <type>
     */
    public function logout() {
        set_user_preference('flickr_mail_'.$this->id, '');
        return $this->print_login();
    }

    /**
     *
     * @param <type> $search_text
     * @return <type>
     */
    public function search($search_text) {
        global $SESSION;
        $people = $this->flickr->people_findByEmail($this->flickr_account);
        $sess_tag = 'flickr_public_'.$this->id.'_tag';
        $sess_text = 'flickr_public_'.$this->id.'_text';
        $this->nsid = $people['nsid'];
        $tag = optional_param('tag', '', PARAM_CLEANHTML);
        $is_paging = optional_param('search_paging', '', PARAM_RAW);
        $page = 1;
        if (!empty($is_paging)) {
            $page = optional_param('p', '', PARAM_INT);
            if (!empty($SESSION->$sess_tag)) {
                $tag = $SESSION->$sess_tag;
            }
            if (!empty($SESSION->$sess_text)) {
                $search_text = $SESSION->$sess_text;
            }
        }
        if (!empty($tag)) {
            $photos = $this->flickr->photos_search(array(
                'tags'=>$tag,
                'page'=>$page
                ));
            $SESSION->$sess_tag = $tag;

        } else {
            $photos = $this->flickr->photos_search(array(
                'user_id'=>$this->nsid,
                'text'=>$search_text));
            $SESSION->$sess_text = $search_text;
        }
        $ret = array();
        return $this->build_list($photos, $page, $ret);
    }

    /**
     *
     * @param <type> $path
     * @return <type>
     */
    public function get_listing($path = '1') {
        $people = $this->flickr->people_findByEmail($this->flickr_account);
        $this->nsid = $people['nsid'];
        $photos = $this->flickr->people_getPublicPhotos($people['nsid'], 'original_format', 25, $path);
        $ret = array();

        return $this->build_list($photos, $path, $ret);
    }

    /**
     *
     * @param <type> $photos
     * @param <type> $path
     * @return <type>
     */
    private function build_list($photos, $path = 1, &$ret) {
        $photos_url = $this->flickr->urls_getUserPhotos($this->nsid);
        $ret['manage'] = $photos_url;
        $ret['list']  = array();
        $ret['pages'] = $photos['pages'];
        if (is_int($path) && $path <= $ret['pages']) {
            $ret['page'] = $path;
        } else {
            $ret['page'] = 1;
        }
        if (!empty($photos['photo'])) {
            foreach ($photos['photo'] as $p) {
                if(empty($p['title'])) {
                    $p['title'] = get_string('notitle', 'repository_flickr');
                }
                if (isset($p['originalformat'])) {
                    $format = $p['originalformat'];
                } else {
                    $format = 'jpg';
                }
                $ret['list'][] = array('title'=>$p['title'].'.'.$format,'source'=>$p['id'],
                    'id'=>$p['id'],'thumbnail'=>$this->flickr->buildPhotoURL($p, 'Square'),
                    'date'=>'', 'size'=>'unknown', 'url'=>'http://www.flickr.com/photos/'.$p['owner'].'/'.$p['id']);
            }
        }
        return $ret;
    }

    /**
     *
     * @return <type>
     */
    public function print_listing() {
        return false;
    }

    /**
     *
     * @return <type>
     */
    public function print_search() {
        parent::print_search();
        echo '<label>Tag: </label><br /><input type="text" name="tag" /><br />';
        return true;
    }

    /**
     *
     * @global <type> $CFG
     * @param <type> $photo_id
     * @param <type> $file
     * @return <type>
     */
    public function get_file($photo_id, $file = '') {
        global $CFG;
        $result = $this->flickr->photos_getSizes($photo_id);
        $url = '';
        if (!empty($result[4])) {
            $url = $result[4]['source'];
        } elseif(!empty($result[3])) {
            $url = $result[3]['source'];
        } elseif(!empty($result[2])) {
            $url = $result[2]['source'];
        }
        if (!file_exists($CFG->dataroot.'/repository/download')) {
            mkdir($CFG->dataroot.'/repository/download/', 0777, true);
        }
        if(is_dir($CFG->dataroot.'/repository/download')) {
            $dir = $CFG->dataroot.'/repository/download/';
        }

        if (empty($file)) {
            $file = $photo_id.'_'.time().'.jpg';
        }
        if (file_exists($dir.$file)) {
            $file = uniqid('m').$file;
        }
        $fp = fopen($dir.$file, 'w');
        $c = new curl;
        $c->download(array(array('url'=>$url, 'file'=>$fp)));

        return $dir.$file;
    }

    /**
     * Add Instance settings input to Moodle form
     * @param <type> $
     */
    public function instance_config_form(&$mform) {
        $mform->addElement('text', 'email_address', get_string('emailaddress', 'repository_flickr_public'));
        //$mform->addRule('email_address', get_string('required'), 'required', null, 'client');
    }

    /**
     * Names of the instance settings
     * @return <type>
     */
    public static function get_instance_option_names() {
        return array('email_address');
    }

    /**
     * Add Plugin settings input to Moodle form
     * @param <type> $
     */
    public function type_config_form(&$mform) {
        $api_key = get_config('flickr_public', 'api_key');
        if (empty($api_key)) {
            $api_key = '';
        }
        $strrequired = get_string('required');
        $mform->addElement('text', 'api_key', get_string('apikey', 'repository_flickr_public'), array('value'=>$api_key,'size' => '40'));
        $mform->addRule('api_key', $strrequired, 'required', null, 'client');
        $mform->addElement('static', null, '',  get_string('information','repository_flickr_public'));
    }

    /**
     * Names of the plugin settings
     * @return <type>
     */
    public static function get_type_option_names() {
        return array('api_key');
    }

    /**
     * is run when moodle administrator add the plugin
     */
    public static function plugin_init() {
        //here we create a default instance for this type
        repository_static_function('flickr_public','create', 'flickr_public', 0, get_system_context(), array('name' => get_string('repositoryname', 'repository_flickr_public'),'email_address' => null));
    }

}
