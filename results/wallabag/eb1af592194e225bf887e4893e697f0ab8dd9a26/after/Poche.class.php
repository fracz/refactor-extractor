<?php
/**
 * poche, a read it later open source system
 *
 * @category   poche
 * @author     Nicolas Lœuillet <support@inthepoche.com>
 * @copyright  2013
 * @license    http://www.wtfpl.net/ see COPYING file
 */

class Poche
{
    public $store;
    public $tpl;

    function __construct($storage_type)
    {
        $this->store = new $storage_type();
        $this->init();

        # installation
        if(!$this->store->isInstalled())
        {
            $this->install();
        }

        $this->saveUser();
    }

    private function init()
    {
        # l10n
        putenv('LC_ALL=' . LANG);
        setlocale(LC_ALL, LANG);
        bindtextdomain(LANG, LOCALE);
        textdomain(LANG);

        # template engine
        $loader = new Twig_Loader_Filesystem(TPL);
        $this->tpl = new Twig_Environment($loader, array(
            'cache' => CACHE,
        ));
        $this->tpl->addExtension(new Twig_Extensions_Extension_I18n());

        Tools::initPhp();
        Session::init();
    }

    private function install()
    {
        Tools::logm('poche still not installed');
        echo $this->tpl->render('install.twig', array(
            'token' => Session::getToken(),
        ));
        if (isset($_GET['install'])) {
            if (($_POST['password'] == $_POST['password_repeat'])
                && $_POST['password'] != "" && $_POST['login'] != "") {
                # let's rock, install poche baby !
                $this->store->install($_POST['login'], Tools::encodeString($_POST['password'] . $_POST['login']));
                Session::logout();
                Tools::redirect();
            }
        }
        exit();
    }

    private function saveUser()
    {
        $_SESSION['login'] = (isset ($_SESSION['login'])) ? $_SESSION['login'] : $this->store->getLogin();
        $_SESSION['pass'] = (isset ($_SESSION['pass'])) ? $_SESSION['pass'] : $this->store->getPassword();
    }

    /**
     * Call action (mark as fav, archive, delete, etc.)
     */
    public function action($action, Url $url, $id)
    {
        switch ($action)
        {
            case 'add':
                if($parametres_url = $url->fetchContent()) {
                    if ($this->store->add($url->getUrl(), $parametres_url['title'], $parametres_url['content'])) {
                        Tools::logm('add link ' . $url->getUrl());
                        $last_id = $this->store->getLastId();
                        if (DOWNLOAD_PICTURES) {
                            $content = filtre_picture($parametres_url['content'], $url->getUrl(), $last_id);
                        }
                        #$msg->add('s', _('the link has been added successfully'));
                    }
                    else {
                        #$msg->add('e', _('error during insertion : the link wasn\'t added'));
                        Tools::logm('error during insertion : the link wasn\'t added');
                    }
                }
                else {
                    #$msg->add('e', _('error during url preparation : the link wasn\'t added'));
                    Tools::logm('error during content fetch');
                }
                break;
            case 'delete':
                if ($this->store->deleteById($id)) {
                    if (DOWNLOAD_PICTURES) {
                        remove_directory(ABS_PATH . $id);
                    }
                    #$msg->add('s', _('the link has been deleted successfully'));
                    Tools::logm('delete link #' . $id);
                }
                else {
                    #$msg->add('e', _('the link wasn\'t deleted'));
                    Tools::logm('error : can\'t delete link #' . $id);
                }
                break;
            case 'toggle_fav' :
                $this->store->favoriteById($id);
                Tools::logm('mark as favorite link #' . $id);
                break;
            case 'toggle_archive' :
                $this->store->archiveById($id);
                Tools::logm('archive link #' . $id);
                break;
            default:
                break;
        }
    }

    function displayView($view, $id = 0)
    {
        $tpl_vars = array();

        switch ($view)
        {
            case 'install':
                Tools::logm('install mode');
                break;
            case 'import';
                Tools::logm('import mode');
                break;
            case 'export':
                $entries = $this->store->retrieveAll();
                // $tpl->assign('export', Tools::renderJson($entries));
                // $tpl->draw('export');
                Tools::logm('export view');
                break;
            case 'config':
                Tools::logm('config view');
                break;
            case 'view':
                $entry = $this->store->retrieveOneById($id);
                if ($entry != NULL) {
                    Tools::logm('view link #' . $id);
                    $content = $entry['content'];
                    if (function_exists('tidy_parse_string')) {
                        $tidy = tidy_parse_string($content, array('indent'=>true, 'show-body-only' => true), 'UTF8');
                        $tidy->cleanRepair();
                        $content = $tidy->value;
                    }
                    $tpl_vars = array(
                        'entry' => $entry,
                        'content' => $content,
                    );
                }
                else {
                    Tools::logm('error in view call : entry is NULL');
                }
                break;
            default: # home view
                $entries = $this->store->getEntriesByView($view);
                $tpl_vars = array(
                    'entries' => $entries,
                );
                break;
        }

        return $tpl_vars;
    }
}