<?php
if(!defined('DOKU_INC')) define('DOKU_INC',dirname(__FILE__).'/../../');

// fix when '<?xml' isn't on the very first line
if(isset($HTTP_RAW_POST_DATA)) $HTTP_RAW_POST_DATA = trim($HTTP_RAW_POST_DATA);


//EXPERIMENTAL CODE
die('remove me to get it work');

require_once(DOKU_INC.'inc/init.php');
require_once(DOKU_INC.'inc/common.php');
require_once(DOKU_INC.'inc/auth.php');
session_write_close();  //close session
require_once(DOKU_INC.'inc/IXR_Library.php');


/**
 * Contains needed wrapper functions and registers all available
 * XMLRPC functions.
 */
class dokuwiki_xmlrpc_server extends IXR_IntrospectionServer {
    var $methods = array();

    /**
     * Constructor. Register methods and run Server
     */
    function dokuwiki_xmlrpc_server(){
        $this->IXR_IntrospectionServer();

        /* DokuWiki's own methods */
        $this->addCallback(
            'dokuwiki.getVersion',
            'getVersion',
            array('string'),
            'Returns the running DokuWiki version'
        );

        /* Wiki API v2 http://www.jspwiki.org/wiki/WikiRPCInterface2 */
        $this->addCallback(
            'wiki.getRPCVersionSupported',
            'this:wiki_RPCVersion',
            array('int'),
            'Returns 2 with the supported RPC API version'
        );
        $this->addCallback(
            'wiki.getPage',
            'this:rawPage',
            array('string','string'),
            'Get the raw Wiki text of page, latest version.'
        );
        $this->addCallback(
            'wiki.getPageVersion',
            'this:rawPage',
            array('string','string','int'),
            'Get the raw Wiki text of page.'
        );
        $this->addCallback(
            'wiki.getPageHTML',
            'this:htmlPage',
            array('string','string'),
            'Return page in rendered HTML, latest version.'
        );
        $this->addCallback(
            'wiki.getPageHTMLVersion',
            'this:htmlPage',
            array('string','string','int'),
            'Return page in rendered HTML.'
        );
        $this->addCallback(
            'wiki.getAllPages',
            'this:listPages',
            array('struct'),
            'Returns a list of all pages. The result is an array of utf8 pagenames.'
        );
        $this->addCallback(
            'wiki.getBackLinks',
            'this:listBackLinks',
            array('struct','string'),
            'Returns the pages that link to this page.'
        );
        $this->addCallback(
            'wiki.getPageInfo',
            'this:pageInfo',
            array('struct','string'),
            'Returns a struct with infos about the page.'
        );
        $this->addCallback(
            'wiki.getPageInfoVersion',
            'this:pageInfo',
            array('struct','string','int'),
            'Returns a struct with infos about the page.'
        );
        $this->addCallback(
            'wiki.putPage',
            'this:putPage',
            array('int', 'string', 'string'),
            'Saves a wiki page'
        );
        $this->addCallback(
            'wiki.listLinks',
            'this:listLinks',
            array('struct','string'),
            'Lists all links contained in a wiki page'
        );
        $this->addCallback(
            'wiki.getRecentChanges',
            'this:getRecentChanges',
            array('struct','int'),
            'Returns a strukt about all recent changes since given timestamp.'
        );

        $this->serve();
    }

    /**
     * Return a raw wiki page
     */
    function rawPage($id,$rev=''){
        if(auth_quickaclcheck($id) < AUTH_READ){
            return new IXR_Error(1, 'You are not allowed to read this page');
        }
        return rawWiki($id,$rev);
    }

    /**
     * Return a wiki page rendered to html
     */
    function htmlPage($id,$rev=''){
        if(auth_quickaclcheck($id) < AUTH_READ){
            return new IXR_Error(1, 'You are not allowed to read this page');
        }
        return p_wiki_xhtml($id,$rev,false);
    }

    /**
     * List all pages - we use the indexer list here
     */
    function listPages(){
        require_once(DOKU_INC.'inc/fulltext.php');
        return ft_pageLookup('');
    }

    /**
     * Return a list of backlinks
     */
    function listBackLinks($id){
        require_once(DOKU_INC.'inc/fulltext.php');
        return ft_backlinks($id);
    }

    /**
     * Return some basic data about a page
     */
    function pageInfo($id,$rev=''){
        if(auth_quickaclcheck($id) < AUTH_READ){
            return new IXR_Error(1, 'You are not allowed to read this page');
        }
        $file = wikiFN($id,$rev);
        $time = @filemtime($file);
        if(!$time){
            return new IXR_Error(10, 'The requested page does not exist');
        }

        $info = getRevisionInfo($id, $time, 1024);

        $data = array(
            'name'         => $id,
            'lastModified' => new IXR_Date($time),
            'author'       => (($info['user']) ? $info['user'] : $info['ip']),
            'version'      => $time
        );

        return ($data);
    }

    /**
     * Save a wiki page
     * FIXME check ACL !!!
     */
    function putPage($put_id, $put_text, $put_summary='', $put_minor=0, $put_rev='', $put_pre='', $put_suf='') {
        global $TEXT;

        $TEXT = $put_text;

        // Check, if page is locked
        if (checklock($put_id) !== false)
            return 1;

        //spam check
        if(checkwordblock())
            return 2;

        lock($put_id);

        saveWikiText($put_id,con($put_pre,$put_text,$put_suf,1),$put_summary,$put_minor);

        unlock($put_id);

        return 0;
    }

    /**
     * Lists all links contained in a wiki page
     *
     * @author Michael Klier <chi@chimeric.de>
     */
    function listLinks($id) {
        if(auth_quickaclcheck($id) < AUTH_READ){
            return new IXR_Error(1, 'You are not allowed to read this page');
        }
        $links = array();

        // resolve page instructions
        $ins   = p_cached_instructions(wikiFN(cleanID($id)));

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
     * @author Michael Klier <chi@chimeric.de>
     */
    function getRecentChanges($timestamp) {
        global $conf;

        if(strlen($timestamp) != 10)
            return new IXR_Error(20, 'The provided value is not a valid timestamp');

        $changes = array();

        require_once(DOKU_INC.'inc/changelog.php');
        require_once(DOKU_INC.'inc/pageutils.php');

        // read changes
        $lines = @file($conf['changelog']);

        if(empty($lines))
            return new IXR_Error(10, 'The changelog could not be read');

        // we start searching at the end of the list
        $lines = array_reverse($lines);

        // cache seen pages and skip them
        $seen = array();

        foreach($lines as $line) {

            if(empty($line)) continue; // skip empty lines

            $logline = parseChangelogLine($line);

            if($logline === false) continue;

            // skip seen ones
            if(isset($seen[$logline['id']])) continue;

            // skip minors
            if($logline['type'] === DOKU_CHANGE_TYPE_MINOR_EDIT && ($flags & RECENTS_SKIP_MINORS)) continue;

            // remember in seen to skip additional sights
            $seen[$logline['id']] = 1;

            // check if it's a hidden page
            if(isHiddenPage($logline['id'])) continue;

            // check ACL
            if(auth_quickaclcheck($logline['id']) < AUTH_READ) continue;

            // check existance
            if((!@file_exists(wikiFN($logline['id']))) && ($flags & RECENTS_SKIP_DELETED)) continue;

            // check if logline is still in the queried time frame
            if($logline['date'] >= $timestamp) {
                $change['name']         = $logline['id'];
                $change['lastModified'] = $logline['date'];
                $change['author']       = $logline['user'];
                $change['version']      = $logline['date'];
                array_push($changes, $change);
            } else {
                $changes = array_reverse($changes);
                return ($changes);
            }
        }
        // in case we still have nothing at this point
        return new IXR_Error(30, 'There are no changes in the specified timeframe');
    }

    /**
     * The version of Wiki RPC API supported
     */
    function wiki_RPCVersion(){
        return 2;
    }

}

$server = new dokuwiki_xmlrpc_server();

// vim:ts=4:sw=4:enc=utf-8: