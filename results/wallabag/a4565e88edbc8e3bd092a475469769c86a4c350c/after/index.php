<?php
/**
 * poche, a read it later open source system
 *
 * @category   poche
 * @author     Nicolas Lœuillet <support@inthepoche.com>
 * @copyright  2013
 * @license    http://www.wtfpl.net/ see COPYING file
 */

include dirname(__FILE__).'/inc/config.php';

pocheTool::initPhp();

# XSRF protection with token
if (!empty($_POST)) {
    if (!Session::isToken($_POST['token'])) {
        die(_('Wrong token.'));
    }
    unset($_SESSION['tokens']);
}

$referer = empty($_SERVER['HTTP_REFERER']) ? '' : $_SERVER['HTTP_REFERER'];

if (isset($_GET['login'])) {
    // Login
    if (!empty($_POST['login']) && !empty($_POST['password'])) {
        if (Session::login($_SESSION['login'], $_SESSION['pass'], $_POST['login'], encode_string($_POST['password'] . $_POST['login']))) {
            pocheTool::logm('login successful');
            if (!empty($_POST['longlastingsession'])) {
                $_SESSION['longlastingsession'] = 31536000;
                $_SESSION['expires_on'] = time() + $_SESSION['longlastingsession'];
                session_set_cookie_params($_SESSION['longlastingsession']);
            } else {
                session_set_cookie_params(0); // when browser closes
            }
            session_regenerate_id(true);

            pocheTool::redirect($referer);
        }
        pocheTool::logm('login failed');
        die(_("Login failed !"));
    } else {
        pocheTool::logm('login failed');
    }
}
elseif (isset($_GET['logout'])) {
    pocheTool::logm('logout');
    Session::logout();
    pocheTool::redirect();
}
elseif  (isset($_GET['config'])) {
    if (isset($_POST['password']) && isset($_POST['password_repeat'])) {
        if ($_POST['password'] == $_POST['password_repeat'] && $_POST['password'] != "") {
            pocheTool::logm('password updated');
            if (!MODE_DEMO) {
                $store->updatePassword(encode_string($_POST['password'] . $_SESSION['login']));
                #your password has been updated
            }
            else {
                #in demo mode, you can\'t update password
            }
        }
        #else
        #your password can\'t be empty and you have to repeat it in the second field
    }
}

# Traitement des paramètres et déclenchement des actions
$view               = (isset ($_REQUEST['view'])) ? htmlentities($_REQUEST['view']) : 'index';
$full_head          = (isset ($_REQUEST['full_head'])) ? htmlentities($_REQUEST['full_head']) : 'yes';
$action             = (isset ($_REQUEST['action'])) ? htmlentities($_REQUEST['action']) : '';
$_SESSION['sort']   = (isset ($_REQUEST['sort'])) ? htmlentities($_REQUEST['sort']) : 'id';
$id                 = (isset ($_REQUEST['id'])) ? htmlspecialchars($_REQUEST['id']) : '';
$url                = (isset ($_GET['url'])) ? $_GET['url'] : '';

$tpl_vars = array(
    'isLogged' => Session::isLogged(),
    'referer' => $referer,
    'view' => $view,
    'poche_url' => pocheTool::getUrl(),
    'demo' => MODE_DEMO,
    'title' => _('poche, a read it later open source system'),
);

if (Session::isLogged()) {
    action_to_do($action, $url, $id);
    display_view($view, $id, $full_head);
}
else {
    $template = $twig->loadTemplate('login.twig');
}

echo $template->render($tpl_vars);