<?php
/**
 * @version $Id$
 * @package		Joomla.Framework
 * @subpackage	Utilities
 * @copyright Copyright (C) 2005 - 2006 Open Source Matters. All rights reserved.
 * @license GNU/GPL, see LICENSE.php
 * Joomla! is free software. This version may have been modified pursuant to the
 * GNU General Public License, and as distributed it includes or is derivative
 * of works licensed under the GNU General Public License or other free or open
 * source software licenses. See COPYRIGHT.php for copyright notices and
 * details.
 */

/**
 * JUtility is a utility functions class
 *
 * @static
 * @author		Johan Janssens <johan.janssens@joomla.org>
 * @package 	Joomla.Framework
 * @subpackage 	Utilities
 * @since	1.5
 */

class JUtility
{
	/**
 	 * Mail function (uses phpMailer)
 	 *
 	 * @param string $from From e-mail address
 	 * @param string $fromname From name
 	 * @param mixed $recipient Recipient e-mail address(es)
 	 * @param string $subject E-mail subject
 	 * @param string $body Message body
 	 * @param boolean $mode false = plain text, true = HTML
 	 * @param mixed $cc CC e-mail address(es)
 	 * @param mixed $bcc BCC e-mail address(es)
 	 * @param mixed $attachment Attachment file name(s)
 	 * @param mixed $replyto Reply to email address(es)
 	 * @param mixed $replytoname Reply to name(s)
 	 * @return boolean True on success
  	 */
	function sendMail($from, $fromname, $recipient, $subject, $body, $mode=0, $cc=null, $bcc=null, $attachment=null, $replyto=null, $replytoname=null )
	{
		jimport('joomla.utilities.mail');

	 	// Get a JMail instance
		$mail =& JFactory::getMailer();

		$mail->setSender(array($from, $fromname));
		$mail->setSubject($subject);
		$mail->setBody($body);

		// Are we sending the email as HTML?
		if ( $mode ) {
			$mail->IsHTML(true);
		}

		$mail->addRecipient($recipient);
		$mail->addCC($cc);
		$mail->addBCC($bcc);
		$mail->addAttachment($attachment);

		// Take care of reply email addresses
		$numReplyTo = count($replyto);
		for ($i=0;$i < $numReplyTo; $i++) {
			$mail->addReplyTo(array($replyto[$i], $replytoname[$i]));
		}

		return  $mail->Send();
	}

	/**
     * Sends mail to administrator for approval of a user submission
 	 *
 	 * @param string $adminName Name of administrator
 	 * @param string $adminEmail Email address of administrator
 	 * @param string $email [NOT USED TODO: Deprecate?]
 	 * @param string $type Type of item to approve
 	 * @param string $title Title of item to approve
 	 * @param string $author Author of item to approve
 	 * @return boolean True on success
 	 */
	function sendAdminMail( $adminName, $adminEmail, $email, $type, $title, $author, $url = null )
	{
		$subject = JText::_( 'User Submitted' ) ." '". $type ."'";

		$message = sprintf ( JText::_( 'MAIL_MSG_ADMIN' ), $adminName, $type, $title, $author, $url, $url, 'administrator', $type);
		$message .= JText::_( 'MAIL_MSG') ."\n";

		eval ("\$message = \"$message\";");

		jimport('joomla.utilities.mail');

	 	// Get a JMail instance
		$mail =& JFactory::getMailer();
		$mail->addRecipient($adminEmail);
		$mail->setSubject($subject);
		$mail->setBody($message);

		return  $mail->Send();
	}

	/**
  	 * Provides a secure hash based on a seed
 	 *
 	 * @param string Seed string
 	 * @return string
 	 */
	function getHash( $seed )
	{
		$conf =& JFactory::getConfig();
		return md5( $conf->getValue('config.secret') . md5( $seed ) );
	}

	/**
 	 * Method to extract key/value pairs out of a string with xml style attributes
 	 *
 	 * @param	string	$string	String containing xml style attributes
 	 * @return	array	Key/Value pairs for the attributes
 	 * @since	1.5
 	 */
	function parseAttributes( $string )
	{
	 	//Initialize variables
		$attr		= array();
		$retarray	= array();

		// Lets grab all the key/value pairs using a regular expression
		preg_match_all( '/([\w]+)[\s]?=[\s]?"([^"]*)"/i', $string, $attr );

		if (is_array($attr))
		{
			$numPairs = count($attr[1]);
			for($i = 0; $i < $numPairs; $i++ )
			{
				$retarray[$attr[1][$i]] = $attr[2][$i];
			}
		}
		return $retarray;
	}

	/**
	 * Method to protect against spoofing attacks
	 *
	 * @return	boolean		True on success, false if a spoofing attack has been identified
	 * @since	1.5
	 */
	function spoofCheck()
	{
		// Lets make sure they saw the html form
		$hash	= JUtility::spoofKey();
		$valid	= JRequest::getVar( $hash, 0, 'post' );
		if (!$valid) {
			return false;
		}

		// Make sure request came from a client with a user agent string.
		if (!isset( $_SERVER['HTTP_USER_AGENT'] )) {
			return false;
		}

		// Check to make sure that the request was posted as well.
		$requestMethod = JArrayHelper::getValue( $_SERVER, 'REQUEST_METHOD' );
		if ($requestMethod != 'POST') {
			return false;
		}

		return true;
	}

	/**
	 * Method to determine a hash for anti-spoofing variable names
	 *
	 * @return	string	Hashed var name
	 * @since	1.5
	 * @static
	 */
	function spoofKey()
	{
		// the prefix ensures that the hash is non-numeric
		// otherwise it will be intercepted by JRequest::clean()
		$session =& JFactory::getSession();
		$hash = 'j' . JUtility::getHash( $session->getId() );
		return $hash;
	}
	/**
	 * Method to determine if the host OS is  Windows
	 *
	 * @return	true if Windows OS
	 * @since	1.5
	 * @static
	 */
	function isWinOS() {
		return strtoupper(substr(PHP_OS, 0, 3)) === 'WIN';
	}
}
?>