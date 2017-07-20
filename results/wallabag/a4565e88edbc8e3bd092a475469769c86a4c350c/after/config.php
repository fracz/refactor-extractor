<?php
/**
 * poche, a read it later open source system
 *
 * @category   poche
 * @author     Nicolas Lœuillet <nicolas@loeuillet.org>
 * @copyright  2013
 * @license    http://www.wtfpl.net/ see COPYING file
 */

define ('POCHE_VERSION', '0.3');
define ('MODE_DEMO', FALSE);
define ('CONVERT_LINKS_FOOTNOTES', FALSE);
define ('REVERT_FORCED_PARAGRAPH_ELEMENTS', FALSE);
define ('DOWNLOAD_PICTURES', FALSE);
define ('SALT', '464v54gLLw928uz4zUBqkRJeiPY68zCX');
define ('ABS_PATH', 'assets/');
define ('TPL', './tpl');
define ('LOCALE', './locale');
define ('CACHE', './cache');
define ('LANG', 'fr_FR.UTF8');

$storage_type = 'sqlite'; # sqlite or file

# /!\ Be careful if you change the lines below /!\

require_once 'pocheCore.php';
require_once 'Readability.php';
require_once 'Encoding.php';
require_once 'pocheTool.class.php';
require_once 'Session.class.php';
require_once 'Twig/Autoloader.php';
require_once 'store/store.class.php';
require_once 'store/' . $storage_type . '.class.php';

if (DOWNLOAD_PICTURES) {
    require_once 'pochePicture.php';
}

# i18n
putenv('LC_ALL=' . LANG);
setlocale(LC_ALL, LANG);
bindtextdomain(LANG, LOCALE);
textdomain(LANG);

# template engine
Twig_Autoloader::register();
$loader = new Twig_Loader_Filesystem(TPL);
$twig = new Twig_Environment($loader, array(
    'cache' => CACHE,
));
$twig->addExtension(new Twig_Extensions_Extension_I18n());

Session::init();
$store = new $storage_type();

# installation
if(!$store->isInstalled())
{
    pocheTool::logm('poche still not installed');
    echo $twig->render('install.twig', array(
        'token' => Session::getToken(),
    ));
    if (isset($_GET['install'])) {
        if (($_POST['password'] == $_POST['password_repeat'])
            && $_POST['password'] != "" && $_POST['login'] != "") {
            # let's rock, install poche baby !
            $store->install($_POST['login'], encode_string($_POST['password'] . $_POST['login']));
            Session::logout();
            pocheTool::redirect();
        }
    }
    exit();
}

$_SESSION['login'] = (isset ($_SESSION['login'])) ? $_SESSION['login'] : $store->getLogin();
$_SESSION['pass']  = (isset ($_SESSION['pass'])) ? $_SESSION['pass'] : $store->getPassword();