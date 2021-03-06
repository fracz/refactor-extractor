<?php
/**
 * Lithium: the most rad php framework
 *
 * @copyright     Copyright 2009, Union of RAD (http://union-of-rad.org)
 * @license       http://opensource.org/licenses/bsd-license.php The BSD License
 */

namespace lithium\test;

use \Exception;
use \lithium\core\Libraries;
use \lithium\util\Inflector;

/**
 * Reporter class to handle test report output
 *
 * @param class \lithium\test\Report
 */
class Reporter extends \lithium\core\Object {

	/**
	 * undocumented function
	 *
	 * @param object $report \lithium\test\Report
	 * @return void
	 */
	public function stats($stats) {
		$defaults = array(
			'asserts' => null, 'passes' => array(), 'fails' => array(),
			'errors' => array(), 'exceptions' => array(),
		);
		$stats = (array) $stats + $defaults;

		$asserts = $stats['asserts'];
		$passes = count($stats['passes']);
		$fails = count($stats['fails']);
		$errors = count($stats['errors']);
		$exceptions = count($stats['exceptions']);
		$success = ($passes === $asserts && $errors === 0);
		$aggregate = compact('asserts', 'passes', 'fails', 'errors', 'exceptions', 'success');
		$result = array($this->_result($aggregate));

		foreach ((array)$stats['errors'] as $error) {
			switch ($error['result']) {
				case 'fail':
					$error += array('class' => 'unknown', 'method' => 'unknown');
					$result[] = $this->_fail($error);
				break;
				case 'exception':
					$result[] = $this->_exception($error);
				break;
			}
		}
		return join("\n", $result);
	}
	/**
	 * return menu as a string to be used as render
	 *
	 * @params array options
	 *               - format: type of reporter class. eg: html default: text
	 *               - tree: true to convert classes to tree structure
	 */
	public function menu($classes, $options = array()) {
		$defaults = array('format' => 'text', 'tree' => false);
		$options += $defaults;

		if ($options['tree']) {
			$data = array();
			$assign = function(&$data, $class, $i = 0) use (&$assign) {
				isset($data[$class[$i]]) ?: $data[] = $class[$i];
				$end = (count($class) <= $i + 1);

				if (!$end && ($offset = array_search($class[$i], $data)) !== false) {
					$data[$class[$i]] = array();
					unset($data[$offset]);
				}
				ksort($data);
				$end ?: $assign($data[$class[$i]], $class, $i + 1);
			};

			foreach ($classes as $class) {
				$assign($data, explode('\\', str_replace('\tests', '', $class)));
			}
			$classes = $data;
		}
		ksort($classes);

		$result = null;

		if ($options['tree']) {
			$self = $this;
			$menu = function ($data, $parent = null) use (&$menu, &$self, $result) {
				foreach ($data as $key => $row) {
					if (is_array($row) && is_string($key)) {
						$key = strtolower($key);
						$next = $parent . '\\' . $key;
						$result .= $self->invokeMethod('_item', array('group', array(
							'namespace' => $next, 'name' => $key, 'menu' => $menu($row, $next)
						)));
					} else {
						$next = $parent . '\\' . $key;
						$result .= $self->invokeMethod('_item', array('case', array(
							'namespace' => $parent, 'name' => $row,
						)));
					}
				}
				return $self->invokeMethod('_item', array(null, array('menu' => $result)));
			};

			foreach ($classes as $library => $tests) {
				$group = "\\{$library}\\tests";
				$result .= $this->_item(null, array('menu' => $this->_item('group', array(
					'namespace' => $group, 'name' => $library, 'menu' => $menu($tests, $group)
				))));
			}
			return $result;
		}

		foreach ($classes as $test) {
			$parts = explode('\\', $test);
			$name = array_pop($parts);
			$namespace = join('\\', $parts);
			$result .= $this->_item('case', compact('namespace', 'name'));
		}
		return $this->_item(null, array('menu' => $result));
	}

	/**
	 * undocumented function
	 *
	 * @param string $filters
	 * @return void
	 */
	public function filters($filters) {

	}

	/**
	 * undocumented function
	 *
	 * @param array $data
	 * @return void
	 */
	protected function _result($data) {

	}

	/**
	 * undocumented function
	 *
	 * @param array $data
	 * @return void
	 */
	protected function _fails($data) {

	}

	/**
	 * undocumented function
	 *
	 * @param array $data
	 * @return void
	 */
	protected function _exception($data) {

	}

	/**
	 * undocumented function
	 *
	 * @param array $data
	 * @return void
	 */
	protected function _item($data) {

	}


}

?>