<?php
/**
 * @link http://www.yiiframework.com/
 * @copyright Copyright (c) 2008 Yii Software LLC
 * @license http://www.yiiframework.com/license/
 */

namespace yii\i18n;

use yii\base\Component;
use yii\base\NotSupportedException;

/**
 * MessageFormatter enhances the message formatter class provided by PHP intl extension.
 *
 * The following enhancements are provided:
 *
 * - Accepts named arguments and mixed numeric and named arguments.
 * - Issues no error when an insufficient number of arguments have been provided. Instead, the placeholders will not be
 *   substituted.
 * - Fixes PHP 5.5 weird placeholder replacement in case no arguments are provided at all (https://bugs.php.net/bug.php?id=65920).
 * - Offers limited support for message formatting in case PHP intl extension is not installed.
 *   However it is highly recommended that you install [PHP intl extension](http://php.net/manual/en/book.intl.php) if you want
 *   to use MessageFormatter features.
 *
 * FallbackMessageFormatter is a fallback implementation for the PHP intl MessageFormatter that is
 * used in case PHP intl extension is not installed.
 *
 * Do not use this class directly. Use [[MessageFormatter]] instead, which will automatically detect
 * installed version of PHP intl and use the fallback if it is not installed.
 *
 * It is highly recommended that you install [PHP intl extension](http://php.net/manual/en/book.intl.php) if you want to use
 * MessageFormatter features.
 *
 * This implementation only supports to following message formats:
 * - plural formatting for english
 * - select format
 * - simple parameters
 *
 * The pattern also does NOT support the ['apostrophe-friendly' syntax](http://www.php.net/manual/en/messageformatter.formatmessage.php).
 *
 * Messages that can be parsed are also not necessarily compatible with the ICU formatting method so do not expect messages that work without intl installed
 * to work with intl
 *
 * @author Alexander Makarov <sam@rmcreative.ru>
 * @author Carsten Brandt <mail@cebe.cc>
 * @since 2.0
 */
class MessageFormatter extends Component
{
	private $_errorCode = 0;
	private $_errorMessage = '';

	/**
	 * Get the error text from the last operation
	 * @link http://php.net/manual/en/messageformatter.geterrorcode.php
	 * @return string Description of the last error.
	 */
	public function getErrorCode()
	{
		return $this->_errorCode;
	}

	/**
	 * Get the error text from the last operation
	 * @link http://php.net/manual/en/messageformatter.geterrormessage.php
	 * @return string Description of the last error.
	 */
	public function getErrorMessage()
	{
		return $this->_errorMessage;
	}

	/**
	 * Formats a message via ICU message format.
	 *
	 * @link http://php.net/manual/en/messageformatter.formatmessage.php
	 * @param string $language The locale to use for formatting locale-dependent parts
	 * @param string $message The pattern string to insert things into.
	 * @param array $params The array of values to insert into the format string
	 * @return string|boolean The formatted pattern string or `FALSE` if an error occurred
	 */
	public function format($language, $message, $params)
	{
		$this->_errorCode = 0;
		$this->_errorMessage = '';

		if ($params === []) {
			return $message;
		}

		if (!class_exists('MessageFormatter', false)) {
			return $this->fallbackFormat($language, $message, $params);
		}

		if (version_compare(PHP_VERSION, '5.5.0', '<')) {
			$message = $this->replaceNamedArguments($message, $params);
			$params = array_values($params);
		}
		$formatter = new \MessageFormatter($language, $message);
		if ($formatter === null) {
			$this->_errorMessage = "Unable to format message in language '$language'.";
			return false;
		}
		$result = $formatter->format($params);
		if ($result === false) {
			$this->_errorCode = $formatter->getErrorCode();
			$this->_errorMessage = $formatter->getErrorMessage();
			return false;
		} else {
			return $result;
		}
	}

	/**
	 * Parse input string according to a message pattern
	 *
	 * @link http://www.php.net/manual/en/messageformatter.parsemessage.php
	 * @param string $language The locale to use for formatting locale-dependent parts
	 * @param string $pattern The pattern with which to parse the message.
	 * @param array $message The message to parse, conforming to the pattern.
	 * @return string|boolean An array containing items extracted, or `FALSE` on error.
	 * @throws \yii\base\NotSupportedException when PHP intl extension is not installed.
	 */
	public function parse($language, $pattern, $message)
	{
		$this->_errorCode = 0;
		$this->_errorMessage = '';

		if (!class_exists('MessageFormatter', false)) {
			throw new NotSupportedException('You have to install PHP intl extension to use this feature.');
		}

		// TODO try to support named args
//		if (version_compare(PHP_VERSION, '5.5.0', '<')) {
//			$message = $this->replaceNamedArguments($message, $params);
//			$params = array_values($params);
//		}
		$formatter = new \MessageFormatter($language, $pattern);
		if ($formatter === null) {
			$this->_errorMessage = "Unable to parse message in language '$language'.";
			return false;
		}
		$result = $formatter->parse($message);
		if ($result === false) {
			$this->_errorCode = $formatter->getErrorCode();
			$this->_errorMessage = $formatter->getErrorMessage();
			return false;
		} else {
			return $result;
		}

	}

	/**
	 * Replace named placeholders with numeric placeholders and quote unused.
	 *
	 * @param string $pattern The pattern string to replace things into.
	 * @param array $args The array of values to insert into the format string.
	 * @return string The pattern string with placeholders replaced.
	 */
	private static function replaceNamedArguments($pattern, $args)
	{
		$map = array_flip(array_keys($args));

		// parsing pattern based on ICU grammar:
		// http://icu-project.org/apiref/icu4c/classMessageFormat.html#details
		$parts = explode('{', $pattern);
		$c = count($parts);
		$pattern = $parts[0];
		$d = 0;
		$stack = [];
		for($i = 1; $i < $c; $i++) {
			if (preg_match('~^(\s*)([\d\w]+)(\s*)([},])(\s*)(.*)$~us', $parts[$i], $matches)) {
				// if we are not inside a plural or select this is a message
				if (!isset($stack[$d]) || $stack[$d] != 'plural' && $stack[$d] != 'select') {
					$d++;
					// replace normal arg if it is available
					if (isset($map[$matches[2]])) {
						$q = '';
						$pattern .= '{' . $matches[1] . $map[$matches[2]] . $matches[3];
					} else {
						// quote unused args
						$q = ($matches[4] == '}') ? "'" : "";
						$pattern .= "$q{" . $matches[1] . $matches[2] . $matches[3];
					}
					$pattern .= ($term = $matches[4] . $q . $matches[5] . $matches[6]);
					// store type of current level
					$stack[$d] = ($matches[4] == ',') ? substr($matches[6], 0, 6) : 'none';
					// if it's plural or select, the next bracket is NOT begin of a message then!
					if ($stack[$d] == 'plural' || $stack[$d] == 'select') {
						$i++;
						$d -= substr_count($term, '}');
					} else {
						$d -= substr_count($term, '}');
						continue;
					}
				}
			}
			$pattern .= '{' . $parts[$i];
			$d += 1 - substr_count($parts[$i], '}');
		}
		return $pattern;
	}

	/**
	 * Fallback implementation for MessageFormatter::formatMessage
	 * @param string $language The locale to use for formatting locale-dependent parts
	 * @param string $message The pattern string to insert things into.
	 * @param array $params The array of values to insert into the format string
	 * @return string|boolean The formatted pattern string or `FALSE` if an error occurred
	 */
	protected function fallbackFormat($locale, $pattern, $args = [])
	{
		if (($tokens = $this->tokenizePattern($pattern)) === false) {
			return false;
		}
		foreach($tokens as $i => $token) {
			if (is_array($token)) {
				if (($tokens[$i] = $this->parseToken($token, $args, $locale)) === false) {
					return false;
				}
			}
		}
		return implode('', $tokens);
	}

	/**
	 * Tokenizes a pattern by separating normal text from replaceable patterns
	 * @param string $pattern patter to tokenize
	 * @return array|bool array of tokens or false on failure
	 */
	private function tokenizePattern($pattern)
	{
		$depth = 1;
		if (($start = $pos = mb_strpos($pattern, '{')) === false) {
			return [$pattern];
		}
		$tokens = [mb_substr($pattern, 0, $pos)];
		while(true) {
			$open = mb_strpos($pattern, '{', $pos + 1);
			$close = mb_strpos($pattern, '}', $pos + 1);
			if ($open === false && $close === false) {
				break;
			}
			if ($open === false) {
				$open = mb_strlen($pattern);
			}
			if ($close > $open) {
				$depth++;
				$pos = $open;
			} else {
				$depth--;
				$pos = $close;
			}
			if ($depth == 0) {
				$tokens[] = explode(',', mb_substr($pattern, $start + 1, $pos - $start - 1), 3);
				$start = $pos + 1;
				$tokens[] = mb_substr($pattern, $start, $open - $start);
				$start = $open;
			}
		}
		if ($depth != 0) {
			return false;
		}
		return $tokens;
	}

	/**
	 * Parses a token
	 * @param array $token the token to parse
	 * @param array $args arguments to replace
	 * @param string $locale the locale
	 * @return bool|string parsed token or false on failure
	 * @throws \yii\base\NotSupportedException when unsupported formatting is used.
	 */
	private function parseToken($token, $args, $locale)
	{
		$param = trim($token[0]);
		if (isset($args[$param])) {
			$arg = $args[$param];
		} else {
			return '{' . implode(',', $token) . '}';
		}
		$type = isset($token[1]) ? trim($token[1]) : 'none';
		switch($type)
		{
			case 'number':
			case 'date':
			case 'time':
			case 'spellout':
			case 'ordinal':
			case 'duration':
			case 'choice':
			case 'selectordinal':
				throw new NotSupportedException("Message format '$type' is not supported. You have to install PHP intl extension to use this feature.");
			case 'none': return $arg;
			case 'select':
				/* http://icu-project.org/apiref/icu4c/classicu_1_1SelectFormat.html
				selectStyle = (selector '{' message '}')+
				*/
				$select = static::tokenizePattern($token[2]);
				$c = count($select);
				$message = false;
				for($i = 0; $i + 1 < $c; $i++) {
					if (is_array($select[$i]) || !is_array($select[$i + 1])) {
						return false;
					}
					$selector = trim($select[$i++]);
					if ($message === false && $selector == 'other' || $selector == $arg) {
						$message = implode(',', $select[$i]);
					}
				}
				if ($message !== false) {
					return $this->fallbackFormat($locale, $message, $args);
				}
			break;
			case 'plural':
				/* http://icu-project.org/apiref/icu4c/classicu_1_1PluralFormat.html
				pluralStyle = [offsetValue] (selector '{' message '}')+
				offsetValue = "offset:" number
				selector = explicitValue | keyword
				explicitValue = '=' number  // adjacent, no white space in between
				keyword = [^[[:Pattern_Syntax:][:Pattern_White_Space:]]]+
				message: see MessageFormat
				*/
				$plural = static::tokenizePattern($token[2]);
				$c = count($plural);
				$message = false;
				$offset = 0;
				for($i = 0; $i + 1 < $c; $i++) {
					if (is_array($plural[$i]) || !is_array($plural[$i + 1])) {
						return false;
					}
					$selector = trim($plural[$i++]);
					if ($i == 1 && substr($selector, 0, 7) == 'offset:') {
						$offset = (int) trim(mb_substr($selector, 7, ($pos = mb_strpos(str_replace(["\n", "\r", "\t"], ' ', $selector), ' ', 7)) - 7));
						$selector = trim(mb_substr($selector, $pos + 1));
					}
					if ($message === false && $selector == 'other' ||
						$selector[0] == '=' && (int) mb_substr($selector, 1) == $arg ||
						$selector == 'zero' && $arg - $offset == 0 ||
						$selector == 'one' && $arg - $offset == 1 ||
						$selector == 'two' && $arg - $offset == 2
					) {
						$message = implode(',', str_replace('#', $arg - $offset, $plural[$i]));
					}
				}
				if ($message !== false) {
					return $this->fallbackFormat($locale, $message, $args);
				}
				break;
		}
		return false;
	}
}