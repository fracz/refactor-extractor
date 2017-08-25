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

use Nette;
use Apigen\Reflection as ApiReflection, Apigen\Generator;
use TokenReflection\IReflectionClass as ReflectionClass, TokenReflection\IReflectionProperty as ReflectionProperty, TokenReflection\IReflectionMethod as ReflectionMethod, TokenReflection\IReflectionConstant as ReflectionConstant, TokenReflection\IReflectionParameter as ReflectionParameter;
use TokenReflection\ReflectionAnnotation, TokenReflection\ReflectionBase;

class Template extends Nette\Templating\FileTemplate
{
	/**
	 * Config.
	 *
	 * @var \Apigen\Config
	 */
	private $config;

	/**
	 * List of classes.
	 *
	 * @var array
	 */
	private $classes;

	/**
	 * Creates template.
	 *
	 * @param \Apigen\Generator $generator
	 */
	public function __construct(Generator $generator)
	{
		$this->config = $generator->getConfig();
		$this->classes = $generator->getClasses();

		$that = $this;

		$latte = new Nette\Latte\Engine;
		$latte->parser->macros['try'] = '<?php try { ?>';
		$latte->parser->macros['/try'] = '<?php } catch (\Exception $e) {} ?>';
		$this->registerFilter($latte);

		// common operations
		$this->registerHelperLoader('Nette\Templating\DefaultHelpers::loader');
		$this->registerHelper('ucfirst', 'ucfirst');
		$this->registerHelper('values', 'array_values');
		$this->registerHelper('map', function($arr, $callback) {
			return array_map(create_function('$value', $callback), $arr);
		});
		$this->registerHelper('replaceRE', 'Nette\Utils\Strings::replace');

		// PHP source highlight
		$fshl = new \fshlParser('HTML_UTF8');
		$this->registerHelper('highlightPHP', function($source) use ($fshl) {
			return $fshl->highlightString('PHP', (string) $source);
		});
		$this->registerHelper('highlightValue', function($definition) use ($that) {
			return $that->highlightPHP(preg_replace('#^(?:[ ]{4}|\t)#m', '', $definition));
		});

		// links
		$this->registerHelper('packageLink', new Nette\Callback($this, 'getPackageLink'));
		$this->registerHelper('namespaceLink', new Nette\Callback($this, 'getNamespaceLink'));
		$this->registerHelper('classLink', new Nette\Callback($this, 'getClassLink'));
		$this->registerHelper('methodLink', new Nette\Callback($this, 'getMethodLink'));
		$this->registerHelper('propertyLink', new Nette\Callback($this, 'getPropertyLink'));
		$this->registerHelper('constantLink', new Nette\Callback($this, 'getConstantLink'));
		$this->registerHelper('sourceLink', new Nette\Callback($this, 'getSourceLink'));
		$this->registerHelper('manualLink', new Nette\Callback($this, 'getManualLink'));

		// types
		$this->registerHelper('getTypes', new Nette\Callback($this, 'getTypes'));
		$this->registerHelper('resolveType', function($variable) {
			return is_object($variable) ? get_class($variable) : gettype($variable);
		});
		$this->registerHelper('resolveClass', new Nette\Callback($this, 'resolveClass'));
		$this->registerHelper('resolveConstant', new Nette\Callback($this, 'resolveConstant'));

		// docblock
		$texy = new \Texy;
		$linkModule = new \TexyLinkModule($texy);
		$linkModule->shorten = FALSE;
		$texy->linkModule = $linkModule;
		$texy->mergeLines = FALSE;
		$texy->allowedTags = array_flip($this->config->allowedHtml);
		$texy->allowed['list/definition'] = FALSE;
		$texy->allowed['phrase/em-alt'] = FALSE;
		$texy->allowed['longwords'] = FALSE;
		// highlighting <code>, <pre>
		$texy->registerBlockPattern(
			function($parser, $matches, $name) use ($fshl) {
				$content = $matches[1] === 'code' ? $fshl->highlightString('PHP', $matches[2]) : htmlSpecialChars($matches[2]);
				$content = $parser->getTexy()->protect($content, \Texy::CONTENT_BLOCK);
				return \TexyHtml::el('pre', $content);
			},
			'#<(code|pre)>(.+?)</\1>#s',
			'codeBlockSyntax'
		);

		// Documentation formatting
		$this->registerHelper('resolveLinks', function($text, ApiReflection $class = null) use ($that) {
			if (null === $class) {
				return $text;
			}
			return preg_replace_callback('~{@link\\s+([^}]+)}~', function ($matches) use ($class, $that) {
				return $that->resolveClassLink($matches[1], $class) ?: $matches[0];
			}, $text);
		});
		$this->registerHelper('docline', function($text) use ($texy) {
			return $texy->processLine($text);
		});
		$this->registerHelper('docblock', function($text) use ($texy) {
			return $texy->process($text);
		});
		$this->registerHelper('doclabel', function($doc, $namespace, ReflectionParameter $parameter = null) use ($that) {
			@list($names, $label) = preg_split('#\s+#', $doc, 2);
			$res = '';
			foreach (explode('|', $names) as $name) {
				if (null !== $parameter && $name === $parameter->getOriginalClassName()) {
					$name = $parameter->getClassName();
				}

				$class = $that->resolveClass($name, $namespace);
				$res .= $class !== null ? sprintf('<a href="%s">%s</a>', $that->classLink($class), $that->escapeHtml($class)) : $that->escapeHtml($that->resolveName($name));
				$res .= '|';
			}

			if (null !== $parameter) {
				$label = preg_replace('~^(\\$?' . $parameter->getName() . ')(\s+|$)~i', '\\2', $label, 1);
			}

			return rtrim($res, '|') . (!empty($label) ? '<br />' . $that->escapeHtml($label) : '');
		});

		// Docblock descriptions
		$this->registerHelper('longDescription', function($element, $shortIfNone = false) {
			$short = $element->getAnnotation(ReflectionAnnotation::SHORT_DESCRIPTION);
			$long = $element->getAnnotation(ReflectionAnnotation::LONG_DESCRIPTION);

			if ($long) {
				$short .= "\n\n" . $long;
			}

			return $short;
		});
		$this->registerHelper('shortDescription', function($element) {
			return $element->getAnnotation(ReflectionAnnotation::SHORT_DESCRIPTION);
		});

		// individual annotations processing
		$this->registerHelper('annotation', function($value, $name, ApiReflection $parent) use ($that) {
			switch ($name) {
				case 'package':
					return '<a href="' . $that->packageLink($value) . '">' . $that->escapeHtml($value) . '</a>';
					break;
				case 'see':
				case 'uses':
					return $that->resolveClassLink($value, $parent) ?: $that->docline($value);
					break;
				default:
					return $that->docline($value);
			}


		});

		$todo = $this->config->todo;
		$this->registerHelper('annotationFilter', function(array $annotations, array $filter = array()) use ($todo) {
			// Unsupported or deprecated annotations
			static $unsupported = array('property', 'property-read', 'property-write', 'method', 'abstract', 'access', 'final', 'filesource', 'global', 'name', 'static', 'staticvar');
			foreach ($unsupported as $annotation) {
				unset($annotations[$annotation]);
			}

			// Custom filter
			foreach ($filter as $annotation) {
				unset($annotations[$annotation]);
			}

			// Show/hide todo
			if (!$todo) {
				unset($annotations['todo']);
			}

			return $annotations;
		});

		$this->registerHelper('annotationSort', function(array $annotations) {
			uksort($annotations, function($a, $b) {
				static $order = array(
					'deprecated' => 0, 'category' => 1, 'package' => 2, 'subpackage' => 3, 'copyright' => 4,
					'license' => 5, 'author' => 6, 'version' => 7, 'since' => 8, 'see' => 9, 'uses' => 10,
					'link' => 11, 'example' => 12, 'tutorial' => 13, 'todo' => 14
				);
				$orderA = isset($order[$a]) ? $order[$a] : 99;
				$orderB = isset($order[$b]) ? $order[$b] : 99;
				return $orderA - $orderB;
			});
			return $annotations;
		});

		// static files versioning
		$destination = $this->config->destination;
		$this->registerHelper('staticFile', function($name, $line = null) use ($destination) {
			static $versions = array();

			$filename = $destination . '/' . $name;
			if (!isset($versions[$filename]) && file_exists($filename)) {
				$versions[$filename] = sprintf('%u', crc32(file_get_contents($filename)));
			}
			if (isset($versions[$filename])) {
				$name .= '?' . $versions[$filename];
			}
			return $name;
		});
	}

	/**
	 * Returns a link to a namespace summary file.
	 *
	 * @param  string|\Apigen\Reflection|IReflectionNamespace
	 * @return string
	 */
	public function getNamespaceLink($class)
	{
		$namespace = ($class instanceof ApiReflection) ? $class->getNamespaceName() : $class;
		return sprintf($this->config->templates['main']['namespace']['filename'], $namespace ? preg_replace('#[^a-z0-9_]#i', '.', $namespace) : 'None');
	}

	/**
	 * Returns a link to a package summary file.
	 *
	 * @param string|\Apigen\Reflection
	 * @return string
	 */
	public function getPackageLink($class)
	{
		$package = ($class instanceof ApiReflection) ? $class->getPackageName() : $class;
		return sprintf($this->config->templates['main']['package']['filename'], $package ? preg_replace('#[^a-z0-9_]#i', '.', $package) : 'None');
	}

	/**
	 * Returns a link to class summary file.
	 *
	 * @param string|\Apigen\Reflection $class
	 * @return string
	 */
	public function getClassLink($class)
	{
		if ($class instanceof ApiReflection) {
			$class = $class->getName();
		}

		return sprintf($this->config->templates['main']['class']['filename'], preg_replace('#[^a-z0-9_]#i', '.', $class));
	}

	/**
	 * Returns a link to method in class summary file.
	 *
	 * @param IReflectionMethod $method
	 * @return string
	 */
	public function getMethodLink(ReflectionMethod $method)
	{
		return $this->getClassLink($method->getDeclaringClassName()) . '#_' . $method->getName();
	}

	/**
	 * Returns a link to property in class summary file.
	 *
	 * @param IReflectionProperty $property
	 * @return string
	 */
	public function getPropertyLink(ReflectionProperty $property)
	{
		return $this->getClassLink($property->getDeclaringClassName()) . '#$' . $property->getName();
	}

	/**
	 * Returns a link to constant in class summary file.
	 *
	 * @param IReflectionConstant $constant
	 * @return string
	 */
	public function getConstantLink(ReflectionConstant $constant)
	{
		return $this->getClassLink($constant->getDeclaringClassName()) . '#' . $constant->getName();
	}

	/**
	 * Returns a link to a element source code.
	 *
	 * @param \Apigen\Reflection|IReflectionMethod|IReflectionProperty|IReflectionConstant $element
	 * @return string
	 */
	public function getSourceLink($element, $withLine = true)
	{
		$class = $element instanceof ApiReflection ? $element : $element->getDeclaringClass();

		$file = str_replace('\\', '/', $class->getName());

		$line = null;
		if ($withLine) {
			$line = $element->getStartLine();
			if ($doc = $element->getDocComment()) {
				$line -= substr_count($doc, "\n") + 1;
			}
		}

		return sprintf($this->config->templates['main']['source']['filename'], preg_replace('#[^a-z0-9_]#i', '.', $file)) . (isset($line) ? "#$line" : '');
	}

	/**
	 * Returns a link to a element documentation at php.net.
	 *
	 * @param \Apigen\Reflection|IReflectionMethod|IReflectionProperty|IReflectionConstant $element
	 * @return string
	 */
	public function getManualLink($element)
	{
		static $manual = 'http://php.net/manual';
		static $reservedClasses = array('stdClass', 'Closure', 'Directory');

		$class = $element instanceof ApiReflection ? $element : $element->getDeclaringClass();

		if (in_array($class->getName(), $reservedClasses)) {
			return $manual . '/reserved.classes.php';
		}

		$className = strtolower($class->getName());
		$classLink = sprintf('%s/class.%s.php', $manual, $className);
		$elementName = strtolower(strtr(ltrim($element->getName(), '_'), '_', '-'));

		if ($element instanceof ApiReflection) {
			return $classLink;
		} elseif ($element instanceof ReflectionMethod) {
			return sprintf('%s/%s.%s.php', $manual, $className, $elementName);
		} elseif ($element instanceof ReflectionProperty) {
			return sprintf('%s#%s.props.%s', $classLink, $className, $elementName);
		} elseif ($element instanceof ReflectionConstant) {
			return sprintf('%s#%s.constants.%s', $classLink, $className, $elementName);
		}
	}

	public function getTypes($element, $position = NULL)
	{
		$annotation = array();
		if ($element instanceof ReflectionProperty) {
			$annotation = $element->getAnnotation('var');
			if (null === $annotation && !$element->isTokenized()) {
				$value = $element->getDefaultValue();
				if (null !== $value) {
					$annotation = gettype($value);
				}
			}
		} elseif ($element instanceof ReflectionMethod) {
			$annotation = $position === null ? $element->getAnnotation('return') : @$element->annotations['param'][$position];
		}

		$namespace = $element->getDeclaringClass()->getNamespaceName();
		$types = array();
		foreach (preg_replace('#\s.*#', '', (array) $annotation) as $s) {
			foreach (explode('|', $s) as $name) {
				$class = $this->resolveClass($name, $namespace);
				$types[] = (object) array('name' => $class ?: $this->resolveName($name), 'class' => $class);
			}
		}
		return $types;
	}

	public function resolveName($name)
	{
		static $names = array(
			'int' => 'integer',
			'bool' => 'boolean',
			'double' => 'float',
			'void' => '',
			'FALSE' => 'false',
			'TRUE' => 'true',
			'NULL' => 'null'
		);

		$name = ltrim($name, '\\');
		if (isset($names[$name])) {
			return $names[$name];
		}
		return $name;
	}

	/**
	 * Tries to resolve type as class or interface name.
	 *
	 * @param string Class name description
	 * @param string Namespace name
	 * @return string
	 */
	public function resolveClass($className, $namespace = NULL)
	{
		if (substr($className, 0, 1) === '\\') {
			$namespace = '';
			$className = substr($className, 1);
		}

		$name = isset($this->classes["$namespace\\$className"]) ? "$namespace\\$className" : (isset($this->classes[$className]) ? $className : null);
		if (null !== $name && !$this->classes[$name]->isDocumented()) {
			$name = null;
		}
		return $name;
	}

	public function resolveConstant($definition)
	{
		if (false === strpos($definition, '::')) {
			return null;
		}
		list($className, $constantName) = explode('::', $definition);
		$className = $this->resolveClass($className);
		if (null === $className) {
			return null;
		}

		try {
			return $this->classes[$className]->getConstantReflection($constantName);
		} catch (\Exception $e) {
			return null;
		}
	}

	/**
	 * Tries to parse a link to a class/method/property and returns the appropriate link if successful.
	 *
	 * @param string $link Link definition
	 * @param \Apigen\Reflection $context Link context
	 * @return \Apigen\Reflection|\TokenReflection\IReflection|null
	 */
	public function resolveClassLink($link, ApiReflection $context = null)
	{
		if (($pos = strpos($link, '::')) || ($pos = strpos($link, '->'))) {
			// Class::something or Class->something
			$className = $this->resolveClass(substr($link, 0, $pos), null !== $context ? $context->getNamespaceName() : null);

			if (null === $className) {
				$className = $this->resolveClass(ReflectionBase::resolveClassFQN(substr($link, 0, $pos), $context->getNamespaceAliases(), $context->getNamespaceName()));
			}

			if (null === $className) {
				return null;
			} else {
				$context = $this->classes[$className];
			}

			$link = substr($link, $pos + 2);
		} elseif ((null !== $context && null !== ($className = $this->resolveClass(ReflectionBase::resolveClassFQN($link, $context->getNamespaceAliases(), $context->getNamespaceName()), $context->getNamespaceName())))
			|| null !== ($className = $this->resolveClass($link, null !== $context ? $context->getNamespaceName() : null))) {
			// Class
			$context = $this->classes[$className];
			return !$context->isDocumented() ? null : '<a href="' . $this->classLink($context) . '">' . $this->escapeHtml($className) . '</a>';
		}

		if (null === $context || !$context->isDocumented()) {
			return null;
		} elseif ($context->hasProperty($link)) {
			// Class property
			$reflection = $context->getProperty($link);
		} elseif ('$' === $link{0} && $context->hasProperty(substr($link, 1))) {
			// Class $property
			$reflection = $context->getProperty(substr($link, 1));
		} elseif ($context->hasMethod($link)) {
			// Class method
			$reflection = $context->getMethod($link);
		} elseif (('()' === substr($link, -2) && $context->hasMethod(substr($link, 0, -2)))) {
			// Class method()
			$reflection = $context->getMethod(substr($link, 0, -2));
		} elseif ($context->hasConstant($link)) {
			// Class constant
			$reflection = $context->getConstantReflection($link);
		} else {
			return null;
		}

		$value = $reflection->getDeclaringClassName();
		if ($reflection instanceof ReflectionProperty) {
			$link = $this->propertyLink($reflection);
			$value .= '::$' . $reflection->getName();
		} elseif ($reflection instanceof ReflectionMethod) {
			$link = $this->methodLink($reflection);
			$value .= '::' . $reflection->getName() . '()';
		} elseif ($reflection instanceof ReflectionConstant) {
			$link = $this->constantLink($reflection);
			$value .= '::' . $reflection->getName();
		}

		return '<a href="' . $link . '">' . $this->escapeHtml($value) . '</a>';
	}
}