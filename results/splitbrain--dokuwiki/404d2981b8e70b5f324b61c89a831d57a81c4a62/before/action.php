<?php
/**
 * Action Plugin:
 *   Upgrades the plugin directory from the "old style" of disabling plugins to the new style
 *
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author     Christopher Smith <chris@jalakai.co.uk>
 */

if(!defined('DOKU_INC')) define('DOKU_INC',realpath(dirname(__FILE__).'/../../').'/');
if(!defined('DOKU_PLUGIN')) define('DOKU_PLUGIN',DOKU_INC.'lib/plugins/');
require_once(DOKU_PLUGIN.'action.php');

/**
 * All DokuWiki plugins to extend the parser/rendering mechanism
 * need to inherit from this class
 */
class action_plugin_upgradeplugindirectory extends DokuWiki_Action_Plugin {

    /**
     * return some info
     */
    function getInfo(){
      return array(
        'author' => 'Christopher Smith',
        'email'  => 'chris@jalakai.co.uk',
        'date'   => '2009-01-18',
        'name'   => 'Upgrade Plugin Directory',
        'desc'   => 'Silently updates plugin disabled indicator to new more efficient format',
        'url'    => 'http://wiki.splitbrain.org/plugin:upgradeplugindirectory',
      );
    }

    /*
     * plugin should use this method to register its handlers with the dokuwiki's event controller
     */
    function register(&$controller) {
      $controller->register_hook('DOKUWIKI_STARTED','BEFORE', $this, 'handle_upgrade','before');
    }

    function handle_upgrade(&$event, $param) {
      global $plugin_controller;
      $attempts = 0;
      $success = 0;
      $updated = 0;
      $failures = array();
      $badclean = array();

      if (empty($plugin_controller)) return;

      if(!is_writable(DOKU_INC.'lib/plugins')) {
          msg("Plugin Directory Upgrade, lib/plugins isn't writable for the webserver, no action taken!", -1);
          return;
      }

      $plugins = $plugin_controller->getList('',true);    // get all plugins
      foreach ($plugins as $plugin) {
      	if ($this->plugin_isdisabled_oldstyle($plugin)) {
      	  $attempts++;
      	  if (@$plugin_controller->disable($plugin)) {
      	  	$updated++;
      	  	if ($this->plugin_clean($plugin)) {
      	  	  $success++;
      	  	} else {
      	  	  $badclean[] = $plugin;
      	  	}
      	  } else {
      	  	$failures[] = $plugin;
      	  }
      	}
      }

      if ($attempts && auth_isAdmin()) {
      	$level = $failures ? -1 : ($badclean ? 2 : 1);
      	msg("Plugin Directory Upgrade, $updated/$attempts plugins updated, $success/$attempts cleaned.",$level);
      	if ($badclean) msg("- the following disabled plugins were updated, but their directories couldn't be cleaned: ".join(',',$badclean),$level);
      	if ($failures) {
      	  msg("- the following disabled plugins couldn't be updated, please update by hand: ".join(',',$failures),$level);
        }
        msg("For more information see http://www.dokuwiki.org/update",$level);
      }

      // no failures, our job is done, disable ourself
      if (!$failures) {
        $plugin_controller->disable($this->getPluginName());
        // redirect to let dokuwiki start cleanly with plugins disabled.
        act_redirect($ID,'upgradeplugindirectory');
      }

    }

    /* old style plugin isdisabled function */
    function plugin_isdisabled_oldstyle($name) {
      return @file_exists(DOKU_PLUGIN.$name.'/disabled');
    }

    function plugin_clean($name) {
      return @unlink(DOKU_PLUGIN.$name.'.disabled/disabled');
    }
}

//Setup VIM: ex: et ts=4 enc=utf-8 :