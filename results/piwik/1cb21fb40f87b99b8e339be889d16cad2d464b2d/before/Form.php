<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html Gpl v3 or later
 * @version $Id$
 *
 * @package Piwik_Login
 */

require_once "modules/Form.php";

/**
 *
 * @package Piwik_Login
 */
class Piwik_Login_Form extends Piwik_Form
{
	function __construct()
	{
		parent::__construct();
	}

	function init()
	{
		$urlToGoAfter = Piwik_Url::getCurrentUrl();

		// if the current url to redirect contains module=login we insteaed redirect to the referer url
		if(stripos($urlToGoAfter,'module=login') !== false)
		{
			$urlToGoAfter = Piwik_Url::getReferer();
		}

		$formElements = array(
			array('text', 'form_login', 'login:'),
			array('password', 'form_password', 'pass:'),
			array('hidden', 'form_url', $urlToGoAfter),
		);
		$this->addElements( $formElements );

		$formRules = array(
			array('form_login', sprintf('%s required', 'login'), 'required'),
			array('form_password', sprintf('%s required', 'password'), 'required'),
		);
		$this->addRules( $formRules );

		$this->addElement('submit', 'submit', 'Go!');
		$this->addElement('submit', 'back', 'Cancel');

	}


}
