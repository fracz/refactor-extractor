<?php

/**
 * ApiGen - API Generator.
 *
 * Copyright (c) 2010 David Grudl (http://davidgrudl.com)
 * Copyright (c) 2011 Ondřej Nešpor (http://andrewsville.cz)
 * Copyright (c) 2011 Jaroslav Hanslík (http://kukulich.cz)
 *
 * This source file is subject to the "Nette license", and/or
 * GPL license. For more information please see http://nette.org
 */

namespace Apigen;

use TokenReflection, TokenReflection\IReflectionConstant, TokenReflection\IReflectionFunction, TokenReflection\Broker\Backend\Memory;
use RuntimeException, ReflectionMethod;

/**
 * Customized TokenReflection broker backend.
 *
 * Adds internal classes from @param, @var, @return, @throws annotations as well
 * as parent classes to the overall class list.
 *
 * @author Ondřej Nešpor
 * @author Jaroslav Hanslík
 */
class Backend extends Memory
{
	/**
	 * Generator instance.
	 *
	 * @var \Apigen\Generator
	 */
	private $generator;

	/**
	 * Constructor.
	 *
	 * @param \Apigen\Generator $config Generator instance
	 */
	public function __construct(Generator $generator)
	{
		$this->generator = $generator;
	}

	/**
	 * Prepares and returns used class lists.
	 *
	 * @return array
	 */
	protected function parseClassLists()
	{
		$declared = array_flip(array_merge(get_declared_classes(), get_declared_interfaces()));

		$allClasses = parent::parseClassLists();
		foreach ($allClasses[self::TOKENIZED_CLASSES] as $name => $class) {
			$class = new ReflectionClass($class, $this->generator);
			$allClasses[self::TOKENIZED_CLASSES][$name] = $class;
			if (!$class->isDocumented()) {
				continue;
			}

			foreach ($class->getOwnMethods() as $method) {
				$allClasses = $this->processFunction($declared, $allClasses, $method);
			}

			foreach ($class->getOwnProperties() as $property) {
				$annotations = $property->getAnnotations();

				if (!isset($annotations['var'])) {
					continue;
				}

				foreach ($annotations['var'] as $doc) {
					foreach (explode('|', preg_replace('#\s.*#', '', $doc)) as $name) {
						$allClasses = $this->addClass($declared, $allClasses, $name);
					}
				}
			}
		}

		foreach ($this->getFunctions() as $function) {
			$allClasses = $this->processFunction($declared, $allClasses, $function);
		}

		array_walk_recursive($allClasses, function(&$reflection, $name, Generator $generator) {
			if (!$reflection instanceof ReflectionClass) {
				$reflection = new ReflectionClass($reflection, $generator);
			}
		}, $this->generator);

		return $allClasses;
	}

	/**
	 * Processes a function/method and adds classes from annotations to the overall class array.
	 *
	 * @param array $declared Array of declared classes
	 * @param array $allClasses Array with all classes parsed so far
	 * @param \Apigen\ReflectionFunction|\TokenReflection\IReflectionFunctionBase $function Function/method reflection
	 * @return array
	 */
	private function processFunction(array $declared, array $allClasses, $function)
	{
		static $parsedAnnotations = array('param', 'return', 'throws');

		foreach ($parsedAnnotations as $annotation) {
			$annotations = $function->getAnnotations();

			if (!isset($annotations[$annotation])) {
				continue;
			}

			foreach ($annotations[$annotation] as $doc) {
				foreach (explode('|', preg_replace('#\s.*#', '', $doc)) as $name) {
					$allClasses = $this->addClass($declared, $allClasses, $name);
				}
			}
		}

		foreach ($function->getParameters() as $param) {
			if ($hint = $param->getClass()) {
				$allClasses = $this->addClass($declared, $allClasses, $hint->getName());
			}
		}

		return $allClasses;
	}

	/**
	 * Adds a class to list of classes.
	 *
	 * @param array $declared Array of declared classes
	 * @param array $allClasses Array with all classes parsed so far
	 * @param string $name Class name
	 * @return array
	 */
	private function addClass(array $declared, array $allClasses, $name)
	{
		$name = ltrim($name, '\\');

		if (!isset($declared[$name]) || isset($allClasses[self::TOKENIZED_CLASSES][$name]) || isset($allClasses[self::INTERNAL_CLASSES][$name]) || isset($allClasses[self::NONEXISTENT_CLASSES][$name])) {
			return $allClasses;
		}

		$parameterClass = $this->getBroker()->getClass($name);
		if ($parameterClass->isTokenized()) {
			throw new RuntimeException(sprintf('Error. Trying to add a tokenized class %s. It should be already in the class list.', $name));
		}

		if ($parameterClass->isInternal()) {
			$allClasses[self::INTERNAL_CLASSES][$name] = $parameterClass;
			foreach (array_merge($parameterClass->getInterfaces(), $parameterClass->getParentClasses()) as $parentClass) {
				if (!isset($allClasses[self::INTERNAL_CLASSES][$parentName = $parentClass->getName()])) {
					$allClasses[self::INTERNAL_CLASSES][$parentName] = $parentClass;
				}
			}
		} else {
			$allClasses[self::NONEXISTENT_CLASSES][$name] = $parameterClass;
		}

		return $allClasses;
	}

	/**
	 * Returns all constants from all namespaces.
	 *
	 * @return array
	 */
	public function getConstants()
	{
		$generator = $this->generator;
		return array_map(function(IReflectionConstant $constant) use ($generator) {
			return new ReflectionConstant($constant, $generator);
		}, parent::getConstants());
	}

	/**
	 * Returns all functions from all namespaces.
	 *
	 * @return array
	 */
	public function getFunctions()
	{
		$generator = $this->generator;
		return array_map(function(IReflectionFunction $function) use ($generator) {
			return new ReflectionFunction($function, $generator);
		}, parent::getFunctions());
	}
}