<?php
/**
 * Server create and edit view
 *
 * @package    phpMyAdmin-setup
 * @license    http://www.gnu.org/licenses/gpl.html GNU GPL 2.0
 * @version    $Id$
 */

if (!defined('PHPMYADMIN')) {
    exit;
}

/**
 * Core libraries.
 */
require_once './libraries/config/Form.class.php';
require_once './libraries/config/FormDisplay.class.php';
require_once './setup/lib/form_processing.lib.php';

require './setup/lib/forms.inc.php';

$mode = filter_input(INPUT_GET, 'mode');
$id = filter_input(INPUT_GET, 'id', FILTER_VALIDATE_INT);

$cf = ConfigFile::getInstance();
$server_exists = !empty($id) && $cf->get("Servers/$id") !== null;

if ($mode == 'edit' && $server_exists) {
    $page_title = __('Edit server')
        . ' ' . $id . ' <small>(' . $cf->getServerDSN($id) . ')</small>';
} elseif ($mode == 'remove' && $server_exists) {
    $cf->removeServer($id);
    header('Location: index.php');
    exit;
} elseif ($mode == 'revert' && $server_exists) {
    // handled by process_formset()
} else {
    $page_title = __('Add a new server');
    $id = 0;
}
if (isset($page_title)) {
    echo '<h2>' . $page_title . '</h2>';
}
$form_display = new FormDisplay();
$form_display->registerForm('Server', $forms['Server'], $id);
$form_display->registerForm('Server_login_options', $forms['Server_login_options'], $id);
$form_display->registerForm('Server_config', $forms['Server_config'], $id);
$form_display->registerForm('Server_pmadb', $forms['Server_pmadb'], $id);
$form_display->registerForm('Server_tracking', $forms['Server_tracking'], $id);
process_formset($form_display);
?>