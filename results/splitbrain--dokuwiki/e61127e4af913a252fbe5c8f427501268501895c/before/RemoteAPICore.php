<?php

class RemoteAPICore {

    private $api;

    public function __construct(RemoteAPI $api) {
        $this->api = $api;
    }

    function __getRemoteInfo() {
        return array(
            'dokuwiki.getVersion' => array(
                'args' => array(),
                'return' => 'string',
                'doc' => 'Returns the running DokuWiki version.'
            ), 'dokuwiki.login' => array(
                'args' => array(
                    'username' => 'string',
                    'password' => 'string'
                ),
                'return' => 'int',
                'doc' => 'Tries to login with the given credentials and sets auth cookies.',
                'public' => '1'
            ), 'dokuwiki.getPagelist' => array(
                'args' => array(
                    'namespace' => 'string',
                    'options' => 'array'
                ),
                'return' => 'array',
                'doc' => 'List all pages within the given namespace.',
                'name' => 'readNamespace'
            ), 'dokuwiki.search' => array(
                'args' => array(
                    'search' => 'string'
                ),
                'return' => 'array',
                'doc' => 'Perform a fulltext search and return a list of matching pages'
            ), 'dokuwiki.getTime' => array(
                'args' => array(),
                'return' => 'int',
                'doc' =>  'Returns the current time at the remote wiki server as Unix timestamp.',
            ), 'dokuwiki.setLocks' => array(
                'args' => array('lock' => 'array'),
                'return' => 'array',
                'doc' => 'Lock or unlock pages.'
            ), 'dokuwiki.getTitle' => array(
                'args' => array(),
                'return' => 'string',
                'doc' => 'Returns the wiki title.'
            ), 'dokuwiki.appendPage' => array(
                'args' => array(
                    'pagename' => 'string',
                    'rawWikiText' => 'string',
                    'attrs' => 'array'
                ),
                'return' => 'int',
                'doc' => 'Append text to a wiki page.'
            ),  'wiki.getPage' => array(
                'args' => array(
                    'id' => 'string'
                ),
                'return' => 'string',
                'doc' => 'Get the raw Wiki text of page, latest version.',
                'name' => 'rawPage',
            ), 'wiki.getPageVersion' => array(
                'args' => array(
                    'id' => 'string',
                    'rev' => 'int',
                ),
                'name' => 'rawPage',
                'return' => 'string',
                'doc' => 'Return a raw wiki page'
            ), 'wiki.getPageHTML' => array(
                'args' => array(
                    'id' => 'string'
                ),
                'return' => 'string',
                'doc' => 'Return page in rendered HTML, latest version.',
                'name' => 'htmlPage'
            ), 'wiki.getPageHTMLVersion' => array(
                'args' => array(
                    'id' => 'string',
                    'rev' => 'int'
                ),
                'return' => 'string',
                'doc' => 'Return page in rendered HTML.',
                'name' => 'htmlPage'
            ), 'wiki.getAllPages' => array(
                'args' => array(),
                'return' => 'array',
                'doc' => 'Returns a list of all pages. The result is an array of utf8 pagenames.',
                'name' => 'listPages'
            ), 'wiki.getAttachments' => array(
                'args' => array(
                    'namespace' => 'string',
                    'options' => 'array'
                ),
                'return' => 'array',
                'doc' => 'Returns a list of all media files.',
                'name' => 'listAttachments'
            ), 'wiki.getBackLinks' => array(
                'args' => array(
                    'id' => 'string'
                ),
                'return' => 'array',
                'doc' => 'Returns the pages that link to this page.',
                'name' => 'listBackLinks'
            ), 'wiki.getPageInfo' => array(
                'args' => array('id' => 'string'),
                'return' => 'array',
                'doc' => 'Returns a struct with infos about the page.',
                'name' => 'pageInfo'
            ), 'wiki.getPageInfoVersion' => array(
                'args' => array(
                    'id' => 'string',
                    'version' => 'int',
                ),
                'return' => 'array',
                'doc' => 'Returns a struct with infos about the page.',
                'name' => 'pageInfo'
            ), 'wiki.getPageVersions' => array(
                'args' => array(
                    'id' => 'string',
                    'offset' => 'int'
                ),
                'return' => 'array',
                'doc' => 'Returns the available revisions of the page.',
                'name' => 'pageVersions'
            ), 'wiki.putPage' => array(
                'args' => array(
                    'id' => 'string',
                    'rawText' => 'string',
                    'attrs' => 'array'
                ),
                'return' => 'int',
                'doc' => 'Saves a wiki page.'
            ), 'wiki.listLinks' => array(
                'args' => array('id' => 'string'),
                'return' => 'array',
                'doc' => 'Lists all links contained in a wiki page.'
            ), 'wiki.getRecentChanges' => array(
                'args' => array('timestamp' => 'int'),
                'return' => 'array',
                'Returns a struct about all recent changes since given timestamp.'
            ), 'wiki.getRecentMediaChanges' => array(
                'args' => array('timestamp' => 'int'),
                'return' => 'array',
                'Returns a struct about all recent media changes since given timestamp.'
            ), 'wiki.aclCheck' => array(
                'args' => array('id' => 'string'),
                'return' => 'int',
                'doc' => 'Returns the permissions of a given wiki page.'
            ), 'wiki.putAttachment' => array(
                'args' => array(
                    'id' => 'string',
                    'data' => 'file',
                    'params' => 'array'
                ),
                'return' => 'array',
                'doc' => 'Upload a file to the wiki.'
            ), 'wiki.deleteAttachment' => array(
                'args' => array('id' => 'string'),
                'return' => 'int',
                'doc' => 'Delete a file from the wiki.'
            ), 'wiki.getAttachment' => array(
                'args' => array('id' => 'string'),
                'doc' => 'Return a media file',
                'return' => 'file',
                'name' => 'getAttachment',
            ), 'wiki.getAttachmentInfo' => array(
                'args' => array('id' => 'string'),
                'return' => 'array',
                'doc' => 'Returns a struct with infos about the attachment.'
            ),

        );
    }

    function getVersion() {
        return getVersion();
    }

    function getTime() {
        return time();
    }

    /**
     * Return a raw wiki page
     * @param string $id wiki page id
     * @param string $rev revision number of the page
     * @return page text.
     */
    function rawPage($id,$rev=''){
        $id = cleanID($id);
        if(auth_quickaclcheck($id) < AUTH_READ){
            throw new RemoteAccessDenied();
        }
        $text = rawWiki($id,$rev);
        if(!$text) {
            return pageTemplate($id);
        } else {
            return $text;
        }
    }

    /**
     * Return a media file
     *
     * @author Gina Haeussge <osd@foosel.net>
     * @param string $id file id
     * @return media file
     */
    function getAttachment($id){
        $id = cleanID($id);
        if (auth_quickaclcheck(getNS($id).':*') < AUTH_READ) {
            throw new RemoteAccessDenied();
        }

        $file = mediaFN($id);
        if (!@ file_exists($file)) {
            throw new RemoteException('The requested file does not exist');
        }

        $data = io_readFile($file, false);
        return $this->api->toFile($data);
    }

    /**
     * Return info about a media file
     *
     * @author Gina Haeussge <osd@foosel.net>
     */
    function getAttachmentInfo($id){
        $id = cleanID($id);
        $info = array(
            'lastModified' => $this->api->toDate(0),
            'size' => 0,
        );

        $file = mediaFN($id);
        if ((auth_quickaclcheck(getNS($id).':*') >= AUTH_READ) && file_exists($file)){
            $info['lastModified'] = $this->api->toDate(filemtime($file));
            $info['size'] = filesize($file);
        }

        return $info;
    }

    /**
     * Return a wiki page rendered to html
     */
    function htmlPage($id,$rev=''){
        $id = cleanID($id);
        if(auth_quickaclcheck($id) < AUTH_READ){
            throw new RemoteAccessDenied(1, 'You are not allowed to read this page');
        }
        return p_wiki_xhtml($id,$rev,false);
    }

    /**
     * List all pages - we use the indexer list here
     */
    function listPages(){
        $list  = array();
        $pages = idx_get_indexer()->getPages();
        $pages = array_filter(array_filter($pages,'isVisiblePage'),'page_exists');

        foreach(array_keys($pages) as $idx) {
            $perm = auth_quickaclcheck($pages[$idx]);
            if($perm < AUTH_READ) {
                continue;
            }
            $page = array();
            $page['id'] = trim($pages[$idx]);
            $page['perms'] = $perm;
            $page['size'] = @filesize(wikiFN($pages[$idx]));
            $page['lastModified'] = $this->api->toDate(@filemtime(wikiFN($pages[$idx])));
            $list[] = $page;
        }

        return $list;
    }

    /**
     * List all pages in the given namespace (and below)
     */
    function readNamespace($ns,$opts){
        global $conf;

        if(!is_array($opts)) $opts=array();

        $ns = cleanID($ns);
        $dir = utf8_encodeFN(str_replace(':', '/', $ns));
        $data = array();
        $opts['skipacl'] = 0; // no ACL skipping for XMLRPC
        search($data, $conf['datadir'], 'search_allpages', $opts, $dir);
        return $data;
    }

    /**
     * List all pages in the given namespace (and below)
     */
    function search($query){
        require_once(DOKU_INC.'inc/fulltext.php');

        $regex = '';
        $data  = ft_pageSearch($query,$regex);
        $pages = array();

        // prepare additional data
        $idx = 0;
        foreach($data as $id => $score){
            $file = wikiFN($id);

            if($idx < FT_SNIPPET_NUMBER){
                $snippet = ft_snippet($id,$regex);
                $idx++;
            }else{
                $snippet = '';
            }

            $pages[] = array(
                'id'      => $id,
                'score'   => intval($score),
                'rev'     => filemtime($file),
                'mtime'   => filemtime($file),
                'size'    => filesize($file),
                'snippet' => $snippet,
            );
        }
        return $pages;
    }

    /**
     * Returns the wiki title.
     */
    function getTitle(){
        global $conf;
        return $conf['title'];
    }

    /**
     * List all media files.
     *
     * Available options are 'recursive' for also including the subnamespaces
     * in the listing, and 'pattern' for filtering the returned files against
     * a regular expression matching their name.
     *
     * @author Gina Haeussge <osd@foosel.net>
     */
    function listAttachments($ns, $options = array()) {
        global $conf;

        $ns = cleanID($ns);

        if (!is_array($options)) $options = array();
        $options['skipacl'] = 0; // no ACL skipping for XMLRPC


        if(auth_quickaclcheck($ns.':*') >= AUTH_READ) {
            $dir = utf8_encodeFN(str_replace(':', '/', $ns));

            $data = array();
            search($data, $conf['mediadir'], 'search_media', $options, $dir);
            $len = count($data);
            if(!$len) return array();

            for($i=0; $i<$len; $i++) {
                unset($data[$i]['meta']);
                $data[$i]['lastModified'] = $this->api->toDate($data[$i]['mtime']);
            }
            return $data;
        } else {
            throw new RemoteAccessDenied(1, 'You are not allowed to list media files.');
        }
    }

    /**
     * Return a list of backlinks
     */
    function listBackLinks($id){
        return ft_backlinks(cleanID($id));
    }

    /**
     * Return some basic data about a page
     */
    function pageInfo($id,$rev=''){
        $id = cleanID($id);
        if(auth_quickaclcheck($id) < AUTH_READ){
            throw new RemoteAccessDenied(1, 'You are not allowed to read this page');
        }
        $file = wikiFN($id,$rev);
        $time = @filemtime($file);
        if(!$time){
            throw new RemoteException(10, 'The requested page does not exist');
        }

        $info = getRevisionInfo($id, $time, 1024);

        $data = array(
            'name'         => $id,
            'lastModified' => $this->api->toDate($time),
            'author'       => (($info['user']) ? $info['user'] : $info['ip']),
            'version'      => $time
        );

        return ($data);
    }

    /**
     * Save a wiki page
     *
     * @author Michael Klier <chi@chimeric.de>
     */
    function putPage($id, $text, $params) {
        global $TEXT;
        global $lang;

        $id    = cleanID($id);
        $TEXT  = cleanText($text);
        $sum   = $params['sum'];
        $minor = $params['minor'];

        if(empty($id)) {
            throw new RemoteException(1, 'Empty page ID');
        }

        if(!page_exists($id) && trim($TEXT) == '' ) {
            throw new RemoteException(1, 'Refusing to write an empty new wiki page');
        }

        if(auth_quickaclcheck($id) < AUTH_EDIT) {
            throw new RemoteAccessDenied(1, 'You are not allowed to edit this page');
        }

        // Check, if page is locked
        if(checklock($id)) {
            throw new RemoteException(1, 'The page is currently locked');
        }

        // SPAM check
        if(checkwordblock()) {
            throw new RemoteException(1, 'Positive wordblock check');
        }

        // autoset summary on new pages
        if(!page_exists($id) && empty($sum)) {
            $sum = $lang['created'];
        }

        // autoset summary on deleted pages
        if(page_exists($id) && empty($TEXT) && empty($sum)) {
            $sum = $lang['deleted'];
        }

        lock($id);

        saveWikiText($id,$TEXT,$sum,$minor);

        unlock($id);

        // run the indexer if page wasn't indexed yet
        idx_addPage($id);

        return 0;
    }

    /**
     * Appends text to a wiki page.
     */
    function appendPage($id, $text, $params) {
        $currentpage = $this->rawPage($id);
        if (!is_string($currentpage)) {
            return $currentpage;
        }
        return $this->putPage($id, $currentpage.$text, $params);
    }

    /**
     * Uploads a file to the wiki.
     *
     * Michael Klier <chi@chimeric.de>
     */
    function putAttachment($id, $file, $params) {
        $id = cleanID($id);
        $auth = auth_quickaclcheck(getNS($id).':*');

        if(!isset($id)) {
            throw new RemoteException(1, 'Filename not given.');
        }

        global $conf;

        $ftmp = $conf['tmpdir'] . '/' . md5($id.clientIP());

        // save temporary file
        @unlink($ftmp);
        io_saveFile($ftmp, $file->getValue());

        $res = media_save(array('name' => $ftmp), $id, $params['ow'], $auth, 'rename');
        if (is_array($res)) {
            throw new RemoteException(-$res[1], $res[0]);
        } else {
            return $res;
        }
    }

    /**
     * Deletes a file from the wiki.
     *
     * @author Gina Haeussge <osd@foosel.net>
     */
    function deleteAttachment($id){
        $id = cleanID($id);
        $auth = auth_quickaclcheck(getNS($id).':*');
        $res = media_delete($id, $auth);
        if ($res & DOKU_MEDIA_DELETED) {
            return 0;
        } elseif ($res & DOKU_MEDIA_NOT_AUTH) {
            throw new RemoteAccessDenied(1, "You don't have permissions to delete files.");
        } elseif ($res & DOKU_MEDIA_INUSE) {
            throw new RemoteException(1, 'File is still referenced');
        } else {
            throw new RemoteException(1, 'Could not delete file');
        }
    }

    /**
    * Returns the permissions of a given wiki page
    */
    function aclCheck($id) {
        $id = cleanID($id);
        return auth_quickaclcheck($id);
    }

    /**
     * Lists all links contained in a wiki page
     *
     * @author Michael Klier <chi@chimeric.de>
     */
    function listLinks($id) {
        $id = cleanID($id);
        if(auth_quickaclcheck($id) < AUTH_READ){
            throw new RemoteAccessDenied(1, 'You are not allowed to read this page');
        }
        $links = array();

        // resolve page instructions
        $ins   = p_cached_instructions(wikiFN($id));

        // instantiate new Renderer - needed for interwiki links
        include(DOKU_INC.'inc/parser/xhtml.php');
        $Renderer = new Doku_Renderer_xhtml();
        $Renderer->interwiki = getInterwiki();

        // parse parse instructions
        foreach($ins as $in) {
            $link = array();
            switch($in[0]) {
                case 'internallink':
                    $link['type'] = 'local';
                    $link['page'] = $in[1][0];
                    $link['href'] = wl($in[1][0]);
                    array_push($links,$link);
                    break;
                case 'externallink':
                    $link['type'] = 'extern';
                    $link['page'] = $in[1][0];
                    $link['href'] = $in[1][0];
                    array_push($links,$link);
                    break;
                case 'interwikilink':
                    $url = $Renderer->_resolveInterWiki($in[1][2],$in[1][3]);
                    $link['type'] = 'extern';
                    $link['page'] = $url;
                    $link['href'] = $url;
                    array_push($links,$link);
                    break;
            }
        }

        return ($links);
    }

    /**
     * Returns a list of recent changes since give timestamp
     *
     * @author Michael Hamann <michael@content-space.de>
     * @author Michael Klier <chi@chimeric.de>
     */
    function getRecentChanges($timestamp) {
        if(strlen($timestamp) != 10) {
            throw new RemoteException(20, 'The provided value is not a valid timestamp');
        }

        $recents = getRecentsSince($timestamp);

        $changes = array();

        foreach ($recents as $recent) {
            $change = array();
            $change['name']         = $recent['id'];
            $change['lastModified'] = $this->api->toDate($recent['date']);
            $change['author']       = $recent['user'];
            $change['version']      = $recent['date'];
            $change['perms']        = $recent['perms'];
            $change['size']         = @filesize(wikiFN($recent['id']));
            array_push($changes, $change);
        }

        if (!empty($changes)) {
            return $changes;
        } else {
            // in case we still have nothing at this point
            return new RemoteException('There are no changes in the specified timeframe');
        }
    }

    /**
     * Returns a list of recent media changes since give timestamp
     *
     * @author Michael Hamann <michael@content-space.de>
     * @author Michael Klier <chi@chimeric.de>
     */
    function getRecentMediaChanges($timestamp) {
        if(strlen($timestamp) != 10)
            throw new RemoteException(20, 'The provided value is not a valid timestamp');

        $recents = getRecentsSince($timestamp, null, '', RECENTS_MEDIA_CHANGES);

        $changes = array();

        foreach ($recents as $recent) {
            $change = array();
            $change['name']         = $recent['id'];
            $change['lastModified'] = $this->api->toDate($recent['date']);
            $change['author']       = $recent['user'];
            $change['version']      = $recent['date'];
            $change['perms']        = $recent['perms'];
            $change['size']         = @filesize(mediaFN($recent['id']));
            array_push($changes, $change);
        }

        if (!empty($changes)) {
            return $changes;
        } else {
            // in case we still have nothing at this point
            throw new RemoteException(30, 'There are no changes in the specified timeframe');
        }
    }

    /**
     * Returns a list of available revisions of a given wiki page
     *
     * @author Michael Klier <chi@chimeric.de>
     */
    function pageVersions($id, $first) {
        $id = cleanID($id);
        if(auth_quickaclcheck($id) < AUTH_READ) {
            throw new RemoteAccessDenied(1, 'You are not allowed to read this page');
        }
        global $conf;

        $versions = array();

        if(empty($id)) {
            throw new RemoteException(1, 'Empty page ID');
        }

        $revisions = getRevisions($id, $first, $conf['recent']+1);

        if(count($revisions)==0 && $first!=0) {
            $first=0;
            $revisions = getRevisions($id, $first, $conf['recent']+1);
        }

        if(count($revisions)>0 && $first==0) {
            array_unshift($revisions, '');  // include current revision
            array_pop($revisions);          // remove extra log entry
        }

        if(count($revisions) > $conf['recent']) {
            array_pop($revisions); // remove extra log entry
        }

        if(!empty($revisions)) {
            foreach($revisions as $rev) {
                $file = wikiFN($id,$rev);
                $time = @filemtime($file);
                // we check if the page actually exists, if this is not the
                // case this can lead to less pages being returned than
                // specified via $conf['recent']
                if($time){
                    $info = getRevisionInfo($id, $time, 1024);
                    if(!empty($info)) {
                        $data['user'] = $info['user'];
                        $data['ip']   = $info['ip'];
                        $data['type'] = $info['type'];
                        $data['sum']  = $info['sum'];
                        $data['modified'] = $this->api->toDate($info['date']);
                        $data['version'] = $info['date'];
                        array_push($versions, $data);
                    }
                }
            }
            return $versions;
        } else {
            return array();
        }
    }

    /**
     * The version of Wiki RPC API supported
     */
    function wiki_RPCVersion(){
        return 2;
    }


    /**
     * Locks or unlocks a given batch of pages
     *
     * Give an associative array with two keys: lock and unlock. Both should contain a
     * list of pages to lock or unlock
     *
     * Returns an associative array with the keys locked, lockfail, unlocked and
     * unlockfail, each containing lists of pages.
     */
    function setLocks($set){
        $locked     = array();
        $lockfail   = array();
        $unlocked   = array();
        $unlockfail = array();

        foreach((array) $set['lock'] as $id){
            $id = cleanID($id);
            if(auth_quickaclcheck($id) < AUTH_EDIT || checklock($id)){
                $lockfail[] = $id;
            }else{
                lock($id);
                $locked[] = $id;
            }
        }

        foreach((array) $set['unlock'] as $id){
            $id = cleanID($id);
            if(auth_quickaclcheck($id) < AUTH_EDIT || !unlock($id)){
                $unlockfail[] = $id;
            }else{
                $unlocked[] = $id;
            }
        }

        return array(
            'locked'     => $locked,
            'lockfail'   => $lockfail,
            'unlocked'   => $unlocked,
            'unlockfail' => $unlockfail,
        );
    }

    function getAPIVersion(){
        return DOKU_XMLRPC_API_VERSION;
    }

    function login($user,$pass){
        global $conf;
        global $auth;
        if(!$conf['useacl']) return 0;
        if(!$auth) return 0;

        @session_start(); // reopen session for login
        if($auth->canDo('external')){
            $ok = $auth->trustExternal($user,$pass,false);
        }else{
            $evdata = array(
                'user'     => $user,
                'password' => $pass,
                'sticky'   => false,
                'silent'   => true,
            );
            $ok = trigger_event('AUTH_LOGIN_CHECK', $evdata, 'auth_login_wrapper');
        }
        session_write_close(); // we're done with the session

        return $ok;
    }


}