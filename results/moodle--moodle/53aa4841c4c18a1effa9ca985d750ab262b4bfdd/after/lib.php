<?php
require_once($CFG->libdir.'/filelib.php');
require_once($CFG->dirroot.'/repository/boxnet/boxlibphp5.php');

class portfolio_plugin_boxnet extends portfolio_plugin_push_base {

    public $boxclient;
    private $ticket;
    private $authtoken;
    private $folders;
    private $accounttree;

    public static function get_name() {
        return get_string('pluginname', 'portfolio_boxnet');
    }

    public function prepare_package() {
        // if we need to create the folder, do it now
        if ($newfolder = $this->get_export_config('newfolder')) {
            if (!$created = $this->boxclient->createFolder($newfolder, array('share' => 0))) {
                throw new portfolio_plugin_exception('foldercreatefailed', 'portfolio_boxnet');
            }
            $this->folders[$created['folder_id']] = $created['folder_type'];
            $this->set_export_config(array('folder' => $created['folder_id']));
        }
        return true; // don't do anything else for this plugin, we want to send all files as they are.
    }

    public function send_package() {
        $ret = array();
        foreach ($this->exporter->get_tempfiles() as $file) {
            $return = $this->boxclient->uploadFile(
                array(
                    'file'      => $file,
                    'folder_id' => $this->get_export_config('folder'),
                    'share'     => 0,
                )
            );
            if (array_key_exists('status', $return) && $return['status'] == 'upload_ok'
                && array_key_exists('id', $return) && count($return['id']) == 1) {
                $return['rename'] = $this->rename_file($return['id'][array_pop(array_keys($return['id']))], $file->get_filename());
                $ret[] = $return;
            }
        }
        if ($this->boxclient->isError()) {
            return false;
        }
        return is_array($ret) && !empty($ret);
    }

    public function get_export_summary() {
        $allfolders = $this->get_folder_list();
        if ($newfolder = $this->get_export_config('newfolder')) {
            $foldername = $newfolder . ' (' . get_string('tobecreated', 'portfolio_boxnet') . ')';
        } else {
            $foldername = $allfolders[$this->get_export_config('folder')];
        }
        return array(
            get_string('targetfolder', 'portfolio_boxnet') => $foldername
        );
    }

    public function get_continue_url() {
        // @todo this was a *guess* based on what urls I got clicking around the interface.
        // the #0:f:<folderid> part seems fragile...
        // but I couldn't find a documented permalink scheme.
        return 'http://box.net/files#0:f:' . $this->get_export_config('folder');
    }

    public function expected_time($callertime) {
        return $callertime;
    }

    public static function has_admin_config() {
        return true;
    }

    public static function get_allowed_config() {
        return array('apikey');
    }

    public function has_export_config() {
        return true;
    }

    public function get_allowed_user_config() {
        return array('authtoken', 'authtokenctime');
    }

    public function get_allowed_export_config() {
        return array('folder', 'newfolder');
    }

    public function export_config_form(&$mform) {
        $folders = $this->get_folder_list();
        $strrequired = get_string('required');
        $mform->addElement('text', 'plugin_newfolder', get_string('newfolder', 'portfolio_boxnet'));
        if (empty($folders)) {
            $mform->addRule('plugin_newfolder', $strrequired, 'required', null, 'client');
        }
        else {
            $mform->addElement('select', 'plugin_folder', get_string('existingfolder', 'portfolio_boxnet'), $folders);
        }
    }

    public function export_config_validation($data) {
        if ((!array_key_exists('plugin_folder', $data) || empty($data['plugin_folder']))
            && (!array_key_exists('plugin_newfolder', $data) || empty($data['plugin_newfolder']))) {
            return array(
                'plugin_folder' => get_string('notarget', 'portfolio_boxnet'),
                'plugin_newfolder' => get_string('notarget', 'portfolio_boxnet'));
        }
        $allfolders = $this->get_folder_list();
        if (in_array($data['plugin_newfolder'], $allfolders)) {
            return array('plugin_newfolder' => get_string('folderclash', 'portfolio_boxnet'));
        }
    }

    public function admin_config_form(&$mform) {
        global $CFG;
        $strrequired = get_string('required');
        $mform->addElement('text', 'apikey', get_string('apikey', 'portfolio_boxnet'));
        $helpparams = array(
            'boxnet_apikey',
            get_string('apikeyhelp', 'portfolio_boxnet'),
            'portfolio',
        );
        $mform->setHelpButton('apikey', $helpparams);
        $mform->addRule('apikey', $strrequired, 'required', null, 'client');
    }

    public function steal_control($stage) {
        if ($stage != PORTFOLIO_STAGE_CONFIG) {
            return false;
        }
        if ($this->authtoken) {
            return false;
        }
        if (!$this->ensure_ticket()) {
            throw new portfolio_plugin_exception('noticket', 'portfolio_boxnet');
        }
        $token = $this->get_user_config('authtoken', $this->get('user')->id);
        $ctime= $this->get_user_config('authtokenctime', $this->get('user')->id);
        if (!empty($token) && (($ctime + 60*60*20) > time())) {
            $this->authtoken = $token;
            $this->boxclient->auth_token = $token;
            return false;
        }
        return 'http://www.box.net/api/1.0/auth/'.$this->ticket;
    }

    public function post_control($stage, $params) {
        if ($stage != PORTFOLIO_STAGE_CONFIG) {
            return;
        }
        if (!array_key_exists('auth_token', $params) || empty($params['auth_token'])) {
            throw new portfolio_plugin_exception('noauthtoken', 'portfolio_boxnet');
        }
        $this->authtoken = $params['auth_token'];
        $this->boxclient->auth_token = $this->authtoken;
        $this->set_user_config(array('authtoken' => $this->authtoken, 'authtokenctime' => time()), $this->get('user')->id);
    }

    private function ensure_ticket() {
        if (!empty($this->boxclient)) {
            return true;
        }
        $this->boxclient = new boxclient($this->get_config('apikey'), '');
        $ticket_return = $this->boxclient->getTicket();
        if ($this->boxclient->isError() || empty($ticket_return)) {
            throw new portfolio_plugin_exception('noticket', 'portfolio_boxnet');
        }
        $this->ticket = $ticket_return['ticket'];
        return $this->ticket;
    }

    private function ensure_account_tree() {
        if (!empty($this->accounttree)) {
            return;
        }
        if (empty($this->ticket)
            || empty($this->authtoken)
            || empty($this->boxclient)) {
            // if we don't have these we're pretty much screwed
            throw new portfolio_plugin_exception('folderlistfailed', 'portfolio_boxnet');
            return false;
        }
        $this->accounttree = $this->boxclient->getAccountTree();
        if ($this->boxclient->isError()) {
            throw new portfolio_plugin_exception('folderlistfailed', 'portfolio_boxnet');
        }
        if (!is_array($this->accounttree)) {
            return false;
        }


    }

    private function get_folder_list() {
        if (!empty($this->folders)) {
            return $this->folders;
        }
        $this->ensure_account_tree();
        $folders = array();
        foreach ($this->accounttree['folder_id'] as $key => $id) {
            if (empty($id)) {
                continue;
            }
            $name = $this->accounttree['folder_name'][$key];
            if (!empty($this->accounttree['shared'][$key])) {
                $name .= ' (' . get_string('sharedfolder', 'portfolio_boxnet') . ')';
            }
            $folders[$id] = $name;
        }
        $this->folders = $folders;
        return $folders;
    }

    private function rename_file($fileid, $newname) {
        // look at moving this to the boxnet client class
        $this->ensure_account_tree();
        $count = 1;
        $bits = explode('.', $newname);
        $suffix = '';
        if (count($bits) == 1) {
            $prefix = $newname;
        } else {
            $suffix = '.' . array_pop($bits);
            $prefix = implode('.', $bits);
        }
        while (true) {
            if (!array_key_exists('file_name', $this->accounttree) || !in_array($newname, $this->accounttree['file_name'])) {
                return $this->boxclient->renameFile($fileid, $newname);
            }
            $newname = $prefix . '(' . $count . ')' . $suffix;
            $count++;
        }
        return false;
    }

    public function instance_sanity_check() {
        if (!$this->get_config('apikey')) {
            return 'err_noapikey';
        }
    //@TODO see if we can verify the api key without actually getting an authentication token
    }

    public static function allows_multiple() {
        return false;
    }
}