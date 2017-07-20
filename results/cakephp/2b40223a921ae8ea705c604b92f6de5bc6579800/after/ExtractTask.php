<?php
/**
 * Language string extractor
 *
 * CakePHP(tm) : Rapid Development Framework (http://cakephp.org)
 * Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright     Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 * @link          http://cakephp.org CakePHP(tm) Project
 * @since         1.2.0
 * @license       http://www.opensource.org/licenses/mit-license.php MIT License
 */
namespace Cake\Console\Command\Task;

use Cake\Console\Shell;
use Cake\Core\App;
use Cake\Core\Plugin;
use Cake\Utility\File;
use Cake\Utility\Folder;
use Cake\Utility\Hash;
use Cake\Utility\Inflector;

/**
 * Language string extractor
 *
 */
class ExtractTask extends Shell {

/**
 * Paths to use when looking for strings
 *
 * @var string
 */
	protected $_paths = [];

/**
 * Files from where to extract
 *
 * @var array
 */
	protected $_files = [];

/**
 * Merge all domain and category strings into the default.pot file
 *
 * @var bool
 */
	protected $_merge = false;

/**
 * Current file being processed
 *
 * @var string
 */
	protected $_file = null;

/**
 * Contains all content waiting to be write
 *
 * @var string
 */
	protected $_storage = [];

/**
 * Extracted tokens
 *
 * @var array
 */
	protected $_tokens = [];

/**
 * Extracted strings indexed by category and domain.
 *
 * @var array
 */
	protected $_translations = [];

/**
 * Destination path
 *
 * @var string
 */
	protected $_output = null;

/**
 * An array of directories to exclude.
 *
 * @var array
 */
	protected $_exclude = [];

/**
 * Holds the validation string domain to use for validation messages when extracting
 *
 * @var bool
 */
	protected $_validationDomain = 'default';

/**
 * Holds whether this call should extract the CakePHP Lib messages
 *
 * @var bool
 */
	protected $_extractCore = false;

/**
 * Method to interact with the User and get path selections.
 *
 * @return void
 */
	protected function _getPaths() {
		$defaultPath = APP;
		while (true) {
			$currentPaths = count($this->_paths) > 0 ? $this->_paths : ['None'];
			$message = __d(
				'cake_console',
				"Current paths: %s\nWhat is the path you would like to extract?\n[Q]uit [D]one",
				implode(', ', $currentPaths)
			);
			$response = $this->in($message, null, $defaultPath);
			if (strtoupper($response) === 'Q') {
				$this->out(__d('cake_console', 'Extract Aborted'));
				return $this->_stop();
			} elseif (strtoupper($response) === 'D' && count($this->_paths)) {
				$this->out();
				return;
			} elseif (strtoupper($response) === 'D') {
				$this->err(__d('cake_console', '<warning>No directories selected.</warning> Please choose a directory.'));
			} elseif (is_dir($response)) {
				$this->_paths[] = $response;
				$defaultPath = 'D';
			} else {
				$this->err(__d('cake_console', 'The directory path you supplied was not found. Please try again.'));
			}
			$this->out();
		}
	}

/**
 * Execution method always used for tasks
 *
 * @return void
 */
	public function main() {
		if (!empty($this->params['exclude'])) {
			$this->_exclude = explode(',', $this->params['exclude']);
		}
		if (isset($this->params['files']) && !is_array($this->params['files'])) {
			$this->_files = explode(',', $this->params['files']);
		}
		if (isset($this->params['paths'])) {
			$this->_paths = explode(',', $this->params['paths']);
		} elseif (isset($this->params['plugin'])) {
			$plugin = Inflector::camelize($this->params['plugin']);
			if (!Plugin::loaded($plugin)) {
				Plugin::load($plugin);
			}
			$this->_paths = [Plugin::path($plugin)];
			$this->params['plugin'] = $plugin;
		} else {
			$this->_getPaths();
		}

		if (isset($this->params['extract-core'])) {
			$this->_extractCore = !(strtolower($this->params['extract-core']) === 'no');
		} else {
			$response = $this->in(__d('cake_console', 'Would you like to extract the messages from the CakePHP core?'), ['y', 'n'], 'n');
			$this->_extractCore = strtolower($response) === 'y';
		}

		if (!empty($this->params['exclude-plugins']) && $this->_isExtractingApp()) {
			$this->_exclude = array_merge($this->_exclude, App::path('Plugin'));
		}

		if (!empty($this->params['validation-domain'])) {
			$this->_validationDomain = $this->params['validation-domain'];
		}

		if ($this->_extractCore) {
			$this->_paths[] = CAKE;
			$this->_exclude = array_merge($this->_exclude, [
				CAKE . 'Test',
				CAKE . 'Console' . DS . 'Templates'
			]);
		}

		if (isset($this->params['output'])) {
			$this->_output = $this->params['output'];
		} elseif (isset($this->params['plugin'])) {
			$this->_output = $this->_paths[0] . DS . 'Locale';
		} else {
			$message = __d('cake_console', "What is the path you would like to output?\n[Q]uit", $this->_paths[0] . DS . 'Locale');
			while (true) {
				$response = $this->in($message, null, rtrim($this->_paths[0], DS) . DS . 'Locale');
				if (strtoupper($response) === 'Q') {
					$this->out(__d('cake_console', 'Extract Aborted'));
					return $this->_stop();
				} elseif ($this->_isPathUsable($response)) {
					$this->_output = $response . DS;
					break;
				} else {
					$this->err(__d('cake_console', 'The directory path you supplied was not found. Please try again.'));
				}
				$this->out();
			}
		}

		if (isset($this->params['merge'])) {
			$this->_merge = !(strtolower($this->params['merge']) === 'no');
		} else {
			$this->out();
			$response = $this->in(__d('cake_console', 'Would you like to merge all domain and category strings into the default.pot file?'), ['y', 'n'], 'n');
			$this->_merge = strtolower($response) === 'y';
		}

		if (empty($this->_files)) {
			$this->_searchFiles();
		}

		$this->_output = rtrim($this->_output, DS) . DS;
		if (!$this->_isPathUsable($this->_output)) {
			$this->err(__d('cake_console', 'The output directory %s was not found or writable.', $this->_output));
			return $this->_stop();
		}

		$this->_extract();
	}

/**
 * Add a translation to the internal translations property
 *
 * Takes care of duplicate translations
 *
 * @param string $category
 * @param string $domain
 * @param string $msgid
 * @param array $details
 * @return void
 */
	protected function _addTranslation($category, $domain, $msgid, $details = []) {
		if (empty($this->_translations[$category][$domain][$msgid])) {
			$this->_translations[$category][$domain][$msgid] = [
				'msgid_plural' => false
			];
		}

		if (isset($details['msgid_plural'])) {
			$this->_translations[$category][$domain][$msgid]['msgid_plural'] = $details['msgid_plural'];
		}

		if (isset($details['file'])) {
			$line = 0;
			if (isset($details['line'])) {
				$line = $details['line'];
			}
			$this->_translations[$category][$domain][$msgid]['references'][$details['file']][] = $line;
		}
	}

/**
 * Extract text
 *
 * @return void
 */
	protected function _extract() {
		$this->out();
		$this->out();
		$this->out(__d('cake_console', 'Extracting...'));
		$this->hr();
		$this->out(__d('cake_console', 'Paths:'));
		foreach ($this->_paths as $path) {
			$this->out('   ' . $path);
		}
		$this->out(__d('cake_console', 'Output Directory: ') . $this->_output);
		$this->hr();
		$this->_extractTokens();
		$this->_buildFiles();
		$this->_writeFiles();
		$this->_paths = $this->_files = $this->_storage = [];
		$this->_translations = $this->_tokens = [];
		$this->out();
		$this->out(__d('cake_console', 'Done.'));
	}

/**
 * Gets the option parser instance and configures it.
 *
 * @return \Cake\Console\ConsoleOptionParser
 */
	public function getOptionParser() {
		$parser = parent::getOptionParser();
		$parser->description(
			__d('cake_console', 'CakePHP Language String Extraction:')
		)->addOption('app', [
			'help' => __d('cake_console', 'Directory where your application is located.')
		])->addOption('paths', [
			'help' => __d('cake_console', 'Comma separated list of paths.')
		])->addOption('merge', [
			'help' => __d('cake_console', 'Merge all domain and category strings into the default.po file.'),
			'choices' => ['yes', 'no']
		])->addOption('output', [
			'help' => __d('cake_console', 'Full path to output directory.')
		])->addOption('files', [
			'help' => __d('cake_console', 'Comma separated list of files.')
		])->addOption('exclude-plugins', [
			'boolean' => true,
			'default' => true,
			'help' => __d('cake_console', 'Ignores all files in plugins if this command is run inside from the same app directory.')
		])->addOption('plugin', [
			'help' => __d('cake_console', 'Extracts tokens only from the plugin specified and puts the result in the plugin\'s Locale directory.')
		])->addOption('ignore-model-validation', [
			'boolean' => true,
			'default' => false,
			'help' => __d('cake_console', 'Ignores validation messages in the $validate property.' .
				' If this flag is not set and the command is run from the same app directory,' .
				' all messages in model validation rules will be extracted as tokens.')
		])->addOption('validation-domain', [
			'help' => __d('cake_console', 'If set to a value, the localization domain to be used for model validation messages.')
		])->addOption('exclude', [
			'help' => __d('cake_console', 'Comma separated list of directories to exclude.' .
				' Any path containing a path segment with the provided values will be skipped. E.g. test,vendors')
		])->addOption('overwrite', [
			'boolean' => true,
			'default' => false,
			'help' => __d('cake_console', 'Always overwrite existing .pot files.')
		])->addOption('extract-core', [
			'help' => __d('cake_console', 'Extract messages from the CakePHP core libs.'),
			'choices' => ['yes', 'no']
		]);

		return $parser;
	}

/**
 * Extract tokens out of all files to be processed
 *
 * @return void
 */
	protected function _extractTokens() {
		foreach ($this->_files as $file) {
			$this->_file = $file;
			$this->out(__d('cake_console', 'Processing %s...', $file));

			$code = file_get_contents($file);
			$allTokens = token_get_all($code);

			$this->_tokens = [];
			foreach ($allTokens as $token) {
				if (!is_array($token) || ($token[0] != T_WHITESPACE && $token[0] != T_INLINE_HTML)) {
					$this->_tokens[] = $token;
				}
			}
			unset($allTokens);
			$this->_parse('__', ['singular']);
			$this->_parse('__n', ['singular', 'plural']);
			$this->_parse('__d', ['domain', 'singular']);
			$this->_parse('__c', ['singular', 'category']);
			$this->_parse('__dc', ['domain', 'singular', 'category']);
			$this->_parse('__dn', ['domain', 'singular', 'plural']);
			$this->_parse('__dcn', ['domain', 'singular', 'plural', 'count', 'category']);
		}
	}

/**
 * Parse tokens
 *
 * @param string $functionName Function name that indicates translatable string (e.g: '__')
 * @param array $map Array containing what variables it will find (e.g: category, domain, singular, plural)
 * @return void
 */
	protected function _parse($functionName, $map) {
		$count = 0;
		$categories = ['LC_ALL', 'LC_COLLATE', 'LC_CTYPE', 'LC_MONETARY', 'LC_NUMERIC', 'LC_TIME', 'LC_MESSAGES'];
		$tokenCount = count($this->_tokens);

		while (($tokenCount - $count) > 1) {
			$countToken = $this->_tokens[$count];
			$firstParenthesis = $this->_tokens[$count + 1];
			if (!is_array($countToken)) {
				$count++;
				continue;
			}

			list($type, $string, $line) = $countToken;
			if (($type == T_STRING) && ($string == $functionName) && ($firstParenthesis === '(')) {
				$position = $count;
				$depth = 0;

				while (!$depth) {
					if ($this->_tokens[$position] === '(') {
						$depth++;
					} elseif ($this->_tokens[$position] === ')') {
						$depth--;
					}
					$position++;
				}

				$mapCount = count($map);
				$strings = $this->_getStrings($position, $mapCount);

				if ($mapCount == count($strings)) {
					extract(array_combine($map, $strings));
					$category = isset($category) ? $category : 6;
					$category = intval($category);
					$categoryName = $categories[$category];
					$domain = isset($domain) ? $domain : 'default';
					$details = [
						'file' => $this->_file,
						'line' => $line,
					];
					if (isset($plural)) {
						$details['msgid_plural'] = $plural;
					}
					$this->_addTranslation($categoryName, $domain, $singular, $details);
				} else {
					$this->_markerError($this->_file, $line, $functionName, $count);
				}
			}
			$count++;
		}
	}

/**
 * Build the translate template file contents out of obtained strings
 *
 * @return void
 */
	protected function _buildFiles() {
		$paths = $this->_paths;
		$paths[] = realpath(APP) . DS;
		foreach ($this->_translations as $category => $domains) {
			foreach ($domains as $domain => $translations) {
				foreach ($translations as $msgid => $details) {
					$plural = $details['msgid_plural'];
					$files = $details['references'];
					$occurrences = [];
					foreach ($files as $file => $lines) {
						$lines = array_unique($lines);
						$occurrences[] = $file . ':' . implode(';', $lines);
					}
					$occurrences = implode("\n#: ", $occurrences);
					$header = '#: ' . str_replace(DS, '/', str_replace($paths, '', $occurrences)) . "\n";

					if ($plural === false) {
						$sentence = "msgid \"{$msgid}\"\n";
						$sentence .= "msgstr \"\"\n\n";
					} else {
						$sentence = "msgid \"{$msgid}\"\n";
						$sentence .= "msgid_plural \"{$plural}\"\n";
						$sentence .= "msgstr[0] \"\"\n";
						$sentence .= "msgstr[1] \"\"\n\n";
					}

					$this->_store($category, $domain, $header, $sentence);
					if (($category !== 'LC_MESSAGES' || $domain !== 'default') && $this->_merge) {
						$this->_store('LC_MESSAGES', 'default', $header, $sentence);
					}
				}
			}
		}
	}

/**
 * Prepare a file to be stored
 *
 * @param string $category
 * @param string $domain
 * @param string $header
 * @param string $sentence
 * @return void
 */
	protected function _store($category, $domain, $header, $sentence) {
		if (!isset($this->_storage[$category])) {
			$this->_storage[$category] = [];
		}
		if (!isset($this->_storage[$category][$domain])) {
			$this->_storage[$category][$domain] = [];
		}
		if (!isset($this->_storage[$category][$domain][$sentence])) {
			$this->_storage[$category][$domain][$sentence] = $header;
		} else {
			$this->_storage[$category][$domain][$sentence] .= $header;
		}
	}

/**
 * Write the files that need to be stored
 *
 * @return void
 */
	protected function _writeFiles() {
		$overwriteAll = false;
		if (!empty($this->params['overwrite'])) {
			$overwriteAll = true;
		}
		foreach ($this->_storage as $category => $domains) {
			foreach ($domains as $domain => $sentences) {
				$output = $this->_writeHeader();
				foreach ($sentences as $sentence => $header) {
					$output .= $header . $sentence;
				}

				$filename = $domain . '.pot';
				if ($category === 'LC_MESSAGES') {
					$File = new File($this->_output . $filename);
				} else {
					new Folder($this->_output . $category, true);
					$File = new File($this->_output . $category . DS . $filename);
				}
				$response = '';
				while ($overwriteAll === false && $File->exists() && strtoupper($response) !== 'Y') {
					$this->out();
					$response = $this->in(
						__d('cake_console', 'Error: %s already exists in this location. Overwrite? [Y]es, [N]o, [A]ll', $filename),
						['y', 'n', 'a'],
						'y'
					);
					if (strtoupper($response) === 'N') {
						$response = '';
						while (!$response) {
							$response = $this->in(__d('cake_console', "What would you like to name this file?"), null, 'new_' . $filename);
							$File = new File($this->_output . $response);
							$filename = $response;
						}
					} elseif (strtoupper($response) === 'A') {
						$overwriteAll = true;
					}
				}
				$File->write($output);
				$File->close();
			}
		}
	}

/**
 * Build the translation template header
 *
 * @return string Translation template header
 */
	protected function _writeHeader() {
		$output = "# LANGUAGE translation of CakePHP Application\n";
		$output .= "# Copyright YEAR NAME <EMAIL@ADDRESS>\n";
		$output .= "#\n";
		$output .= "#, fuzzy\n";
		$output .= "msgid \"\"\n";
		$output .= "msgstr \"\"\n";
		$output .= "\"Project-Id-Version: PROJECT VERSION\\n\"\n";
		$output .= "\"POT-Creation-Date: " . date("Y-m-d H:iO") . "\\n\"\n";
		$output .= "\"PO-Revision-Date: YYYY-mm-DD HH:MM+ZZZZ\\n\"\n";
		$output .= "\"Last-Translator: NAME <EMAIL@ADDRESS>\\n\"\n";
		$output .= "\"Language-Team: LANGUAGE <EMAIL@ADDRESS>\\n\"\n";
		$output .= "\"MIME-Version: 1.0\\n\"\n";
		$output .= "\"Content-Type: text/plain; charset=utf-8\\n\"\n";
		$output .= "\"Content-Transfer-Encoding: 8bit\\n\"\n";
		$output .= "\"Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;\\n\"\n\n";
		return $output;
	}

/**
 * Get the strings from the position forward
 *
 * @param int $position Actual position on tokens array
 * @param int $target Number of strings to extract
 * @return array Strings extracted
 */
	protected function _getStrings(&$position, $target) {
		$strings = [];
		$count = count($strings);
		while ($count < $target && ($this->_tokens[$position] === ',' || $this->_tokens[$position][0] == T_CONSTANT_ENCAPSED_STRING || $this->_tokens[$position][0] == T_LNUMBER)) {
			$count = count($strings);
			if ($this->_tokens[$position][0] == T_CONSTANT_ENCAPSED_STRING && $this->_tokens[$position + 1] === '.') {
				$string = '';
				while ($this->_tokens[$position][0] == T_CONSTANT_ENCAPSED_STRING || $this->_tokens[$position] === '.') {
					if ($this->_tokens[$position][0] == T_CONSTANT_ENCAPSED_STRING) {
						$string .= $this->_formatString($this->_tokens[$position][1]);
					}
					$position++;
				}
				$strings[] = $string;
			} elseif ($this->_tokens[$position][0] == T_CONSTANT_ENCAPSED_STRING) {
				$strings[] = $this->_formatString($this->_tokens[$position][1]);
			} elseif ($this->_tokens[$position][0] == T_LNUMBER) {
				$strings[] = $this->_tokens[$position][1];
			}
			$position++;
		}
		return $strings;
	}

/**
 * Format a string to be added as a translatable string
 *
 * @param string $string String to format
 * @return string Formatted string
 */
	protected function _formatString($string) {
		$quote = substr($string, 0, 1);
		$string = substr($string, 1, -1);
		if ($quote === '"') {
			$string = stripcslashes($string);
		} else {
			$string = strtr($string, ["\\'" => "'", "\\\\" => "\\"]);
		}
		$string = str_replace("\r\n", "\n", $string);
		return addcslashes($string, "\0..\37\\\"");
	}

/**
 * Indicate an invalid marker on a processed file
 *
 * @param string $file File where invalid marker resides
 * @param int $line Line number
 * @param string $marker Marker found
 * @param int $count Count
 * @return void
 */
	protected function _markerError($file, $line, $marker, $count) {
		$this->out(__d('cake_console', "Invalid marker content in %s:%s\n* %s(", $file, $line, $marker));
		$count += 2;
		$tokenCount = count($this->_tokens);
		$parenthesis = 1;

		while ((($tokenCount - $count) > 0) && $parenthesis) {
			if (is_array($this->_tokens[$count])) {
				$this->out($this->_tokens[$count][1], false);
			} else {
				$this->out($this->_tokens[$count], false);
				if ($this->_tokens[$count] === '(') {
					$parenthesis++;
				}

				if ($this->_tokens[$count] === ')') {
					$parenthesis--;
				}
			}
			$count++;
		}
		$this->out("\n", true);
	}

/**
 * Search files that may contain translatable strings
 *
 * @return void
 */
	protected function _searchFiles() {
		$pattern = false;
		if (!empty($this->_exclude)) {
			$exclude = [];
			foreach ($this->_exclude as $e) {
				if (DS !== '\\' && $e[0] !== DS) {
					$e = DS . $e;
				}
				$exclude[] = preg_quote($e, '/');
			}
			$pattern = '/' . implode('|', $exclude) . '/';
		}
		foreach ($this->_paths as $path) {
			$Folder = new Folder($path);
			$files = $Folder->findRecursive('.*\.(php|ctp|thtml|inc|tpl)', true);
			if (!empty($pattern)) {
				foreach ($files as $i => $file) {
					if (preg_match($pattern, $file)) {
						unset($files[$i]);
					}
				}
				$files = array_values($files);
			}
			$this->_files = array_merge($this->_files, $files);
		}
	}

/**
 * Returns whether this execution is meant to extract string only from directories in folder represented by the
 * APP constant, i.e. this task is extracting strings from same application.
 *
 * @return bool
 */
	protected function _isExtractingApp() {
		return $this->_paths === [APP];
	}

/**
 * Checks whether or not a given path is usable for writing.
 *
 * @param string $path Path to folder
 * @return bool true if it exists and is writable, false otherwise
 */
	protected function _isPathUsable($path) {
		return is_dir($path) && is_writable($path);
	}
}