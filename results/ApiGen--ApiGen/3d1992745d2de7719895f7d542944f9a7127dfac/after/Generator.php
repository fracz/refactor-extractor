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
use Apigen\Reflection as ApiReflection, Apigen\Exception, Apigen\Config, Apigen\Template, Apigen\Backend;
use TokenReflection\Broker;
use TokenReflection\IReflectionClass as ReflectionClass, TokenReflection\IReflectionProperty as ReflectionProperty, TokenReflection\IReflectionMethod as ReflectionMethod, TokenReflection\IReflectionConstant as ReflectionConstant, TokenReflection\IReflectionParameter as ReflectionParameter;
use TokenReflection\ReflectionAnnotation;

/**
 * Generates a HTML API documentation.
 *
 * @author David Grudl
 * @author Ondřej Nešpor
 */
class Generator extends Nette\Object
{
	/**
	 * Library version.
	 *
	 * @var string
	 */
	const VERSION = '2.0 beta';

	/**
	 * Configuration.
	 *
	 * @var \Apigen\Config
	 */
	private $config;

	/**
	 * Progressbar
	 *
	 * @var \Console_ProgressBar
	 */
	private $progressBar;

	/**
	 * Array of reflection envelopes.
	 *
	 * @var \ArrayObject
	 */
	private $classes = array();

	/**
	 * Sets configuration.
	 *
	 * @param array $config
	 */
	public function __construct(Config $config)
	{
		$this->config = $config;
	}

	/**
	 * Scans and parses PHP files.
	 *
	 * @return array
	 */
	public function parse()
	{
		$broker = new Broker(new Backend(), !empty($this->config->undocumented));

		$files = array();
		foreach ($this->config->source as $source) {
			$entries = array();
			if (is_dir($source)) {
				foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($source)) as $entry) {
					if (!$entry->isFile()) {
						continue;
					}
					$entries[] = $entry;
				}
			} else {
				$entries[] = new \SplFileInfo($source);
			}

			foreach ($entries as $entry) {
				if (!preg_match('~\\.php$~i', $entry->getFilename())) {
					continue;
				}
				foreach ($this->config->exclude as $mask) {
					if (fnmatch($mask, $entry->getPathName(), FNM_NOESCAPE | FNM_PATHNAME)) {
						continue 2;
					}
				}

				$files[$entry->getPathName()] = $entry->getSize();
			}
		}

		if (empty($files)) {
			throw new Exception("No PHP files found.");
		}

		if ($this->config->progressbar) {
			$this->prepareProgressBar(array_sum($files));
		}

		foreach ($files as $file => $size) {
			$broker->processFile($file);
			$this->incrementProgressBar($size);
		}

		$this->classes = new \ArrayObject();
		foreach ($broker->getClasses(Backend::TOKENIZED_CLASSES | Backend::INTERNAL_CLASSES | Backend::NONEXISTENT_CLASSES) as $className => $class) {
			$this->classes->offsetSet($className, new ApiReflection($class, $this));
		}

		return array(
			count($broker->getClasses(Backend::TOKENIZED_CLASSES)),
			count($broker->getClasses(Backend::INTERNAL_CLASSES))
		);
	}

	/**
	 * Returns configuration.
	 *
	 * @return mixed
	 */
	public function getConfig()
	{
		return $this->config;
	}

	/**
	 * Returns parsed class list.
	 *
	 * @return \ArrayObject
	 */
	public function getClasses()
	{
		return $this->classes;
	}

	/**
	 * Wipes out the destination directory.
	 *
	 * @return boolean
	 */
	public function wipeOutDestination()
	{
		// resources
		foreach ($this->config->resources as $resource) {
			$path = $this->config->destination . '/' . $resource;
			if (is_dir($path) && !$this->deleteDir($path)) {
				return false;
			} elseif (is_file($path) && !@unlink($path)) {
				return false;
			}
		}

		// common files
		$filenames = array_keys($this->config->templates['common']);
		foreach (Nette\Utils\Finder::findFiles($filenames)->from($this->config->destination) as $item) {
			if (!@unlink($item)) {
				return false;
			}
		}

		// optional files
		foreach ($this->config->templates['optional'] as $optional) {
			$file = $this->config->destination . '/' . $optional['filename'];
			if (is_file($file) && !@unlink($file)) {
				return false;
			}
		}

		// main files
		$masks = array_map(function($config) {
			return preg_replace('~%[^%]*?s~', '*', $config['filename']);
		}, $this->config->templates['main']);
		$filter = function($item) use ($masks) {
			foreach ($masks as $mask) {
				if (fnmatch($mask, $item->getFilename())) {
					return true;
				}
			}

			return false;
		};

		foreach (Nette\Utils\Finder::findFiles('*')->filter($filter)->from($this->config->destination) as $item) {
			if (!@unlink($item)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Generates API documentation.
	 */
	public function generate()
	{
		@mkdir($this->config->destination);
		if (!is_dir($this->config->destination)) {
			throw new Exception("Directory {$this->config->destination} doesn't exist.");
		}

		$destination = $this->config->destination;
		$templates = $this->config->templates;
		$templatePath = $this->config->templateDir . '/' . $this->config->template;

		// copy resources
		foreach ($this->config->resources as $resourceSource => $resourceDestination) {
			// File
			$resourcePath = $templatePath . '/' . $resourceSource;
			if (is_file($resourcePath)) {
				copy($resourcePath, $this->forceDir("$destination/$resourceDestination"));
				continue;
			}

			// Dir
			foreach ($iterator = Nette\Utils\Finder::findFiles('*')->from($resourcePath)->getIterator() as $item) {
				copy($item->getPathName(), $this->forceDir("$destination/$resourceDestination/" . $iterator->getSubPathName()));
			}
		}

		// categorize by packages and namespaces
		$packages = array();
		$namespaces = array();
		$classes = array();
		$interfaces = array();
		$exceptions = array();
		foreach ($this->classes as $class) {
			if ($class->isDocumented()) {
				$packageName = $class->getPackageName();
				$namespaceName = (string) $class->getNamespaceName();
				$className = $class->getName();

				$packages[$packageName]['classes'][$className] = $class;
				if ('' !== $namespaceName) {
					$packages[$packageName]['namespaces'][$namespaceName] = true;
					$namespaces[$namespaceName]['classes'][$class->getShortName()] = $class;
					$namespaces[$namespaceName]['packages'][$packageName] = true;
				}

				if ($class->isInterface()) {
					$interfaces[$className] = $class;
				} elseif ($class->isException()) {
					$exceptions[$className] = $class;
				} else {
					$classes[$className] = $class;
				}
			}
		}

		// add missing parent namespaces
		foreach ($namespaces as $name => $namespace) {
			$parent = '';
			foreach (explode('\\', $name) as $part) {
				$parent = ltrim($parent . '\\' . $part, '\\');
				if (!isset($namespaces[$parent])) {
					$namespaces[$parent] = array('classes' => array(), 'packages' => array());
				}
			}
		}

		uksort($packages, 'strcasecmp');
		uksort($namespaces, 'strcasecmp');
		uksort($classes, 'strcasecmp');
		uksort($interfaces, 'strcasecmp');
		uksort($exceptions, 'strcasecmp');

		$undocumentedEnabled = !empty($this->config->undocumented);
		$deprecated = $this->config->deprecated && isset($templates['optional']['deprecated']);
		$todo = $this->config->todo && isset($templates['optional']['todo']);
		$sitemap = !empty($this->config->baseUrl) && isset($templates['optional']['sitemap']);
		$opensearch = !empty($this->config->googleCse) && !empty($this->config->baseUrl) && isset($templates['optional']['opensearch']);
		$autocomplete = !empty($this->config->googleCse) && isset($templates['optional']['autocomplete']);

		$classFilter = function($class) {return !$class->isInterface() && !$class->isException();};
		$interfaceFilter = function($class) {return $class->isInterface();};
		$exceptionFilter = function($class) {return $class->isException();};

		if ($this->config->progressbar) {
			$max = count($packages)
				+ count($namespaces)
				+ count($classes)
				+ count($interfaces)
				+ count($exceptions)
				+ count($templates['common'])
				+ (int) $undocumentedEnabled
				+ 7 * (int) $deprecated // generating splitted to 7 steps
				+ 7 * (int) $todo // generating splitted to 7 steps
				+ (int) $sitemap
				+ (int) $opensearch
				+ (int) $autocomplete
				+ 1 // classes, iterators and exceptions tree
			;

			if ($this->config->sourceCode) {
				$tokenizedFilter = function(ApiReflection $class) {return $class->isTokenized();};
				$max += count(array_filter($classes, $tokenizedFilter))
					+ count(array_filter($interfaces, $tokenizedFilter))
					+ count(array_filter($exceptions, $tokenizedFilter));
				unset($tokenizedFilter);
			}

			$this->prepareProgressBar($max);
		}

		// create tmp directory
		$tmp = $destination . DIRECTORY_SEPARATOR . 'tmp';
		@mkdir($tmp, 0755, true);

		// prepare template
		$template = new Template($this);
		$template->setCacheStorage(new Nette\Templating\PhpFileStorage($tmp));
		$template->version = self::VERSION;
		$template->config = $this->config;
		$template->deprecated = $deprecated;
		$template->todo = $todo;

		// generate summary files
		$namespaceNames = array_keys($namespaces);
		$template->namespace = null;
		$template->namespaces = $namespaceNames;
		$template->package = null;
		$template->packages = array_keys($packages);
		$template->class = null;
		$template->classes = $classes;
		$template->interfaces = $interfaces;
		$template->exceptions = $exceptions;
		foreach ($templates['common'] as $dest => $source) {
			$template->setFile($templatePath . '/' . $source)->save($this->forceDir("$destination/$dest"));

			$this->incrementProgressBar();
		}

		// optional files
		if ($sitemap) {
			$template->setFile($templatePath . '/' . $templates['optional']['sitemap']['template'])->save($this->forceDir($destination . '/' . $templates['optional']['sitemap']['filename']));
			$this->incrementProgressBar();
		}
		if ($opensearch) {
			$template->setFile($templatePath . '/' . $templates['optional']['opensearch']['template'])->save($this->forceDir($destination . '/' . $templates['optional']['opensearch']['filename']));
			$this->incrementProgressBar();
		}
		if ($autocomplete) {
			$template->setFile($templatePath . '/' . $templates['optional']['autocomplete']['template'])->save($this->forceDir($destination . '/' . $templates['optional']['autocomplete']['filename']));
			$this->incrementProgressBar();
		}

		// list of undocumented elements
		if ($undocumentedEnabled) {
			$label = function($element) {
				if ($element instanceof ApiReflection) {
					return 'class';
				} elseif ($element instanceof ReflectionMethod) {
					return sprintf('method %s()', $element->getName());
				} elseif ($element instanceof ReflectionConstant) {
					return sprintf('constant %s', $element->getName());
				} elseif ($element instanceof ReflectionProperty) {
					return sprintf('property $%s', $element->getName());
				} elseif ($element instanceof ReflectionParameter) {
					return sprintf('parameter $%s', $element->getName());
				} else {
					return $element->getName();
				}
			};
			$normalize = function($string) {
				return preg_replace('~\s+~', ' ', $string);
			};

			$undocumented = array();
			foreach (array('classes', 'interfaces', 'exceptions') as $type) {
				foreach ($$type as $class) {
					// Check only "documented" classes (except internal - no documentation)
					if (!$class->isDocumented() || $class->isInternal()) {
						continue;
					}

					foreach (array_merge(array($class), array_values($class->getOwnMethods()), array_values($class->getOwnConstants()), array_values($class->getOwnProperties())) as $element) {
						$annotations = $element->getAnnotations();

						// Documentation
						if (empty($annotations)) {
							$undocumented[$class->getName()][] = sprintf('Missing documentation of the %s.', $label($element));
							continue;
						}

						// Description
						if (!isset($annotations[ReflectionAnnotation::SHORT_DESCRIPTION])) {
							$undocumented[$class->getName()][] = sprintf('Missing description of the %s.', $label($element));
						}

						// Documentation of method
						if ($element instanceof ReflectionMethod) {
							// Parameters
							foreach ($element->getParameters() as $no => $parameter) {
								if (!isset($annotations['param'][$no])) {
									$undocumented[$class->getName()][] = sprintf('Missing documentation of the %s of the %s.', $label($parameter), $label($element));
									continue;
								}

								if (!preg_match('~^[\w\\\\]+(?:\|[\w\\\\]+)*\s+\$' . $parameter->getName() . '(?:\s+.+)?$~s', $annotations['param'][$no])) {
									$undocumented[$class->getName()][] = sprintf('Invalid documentation "%s" of the %s of the %s.', $normalize($annotations['param'][$no]), $label($parameter), $label($element));
								}

								unset($annotations['param'][$no]);
							}
							if (isset($annotations['param'])) {
								foreach ($annotations['param'] as $annotation) {
									$undocumented[$class->getName()][] = sprintf('Existing documentation "%s" of nonexistent parameter of the %s.', $normalize($annotation), $label($element));
								}
							}

							$tokens = $element->getBroker()->getFileTokens($element->getFileName());

							// Return values
							$return = false;
							$tokens->seek($element->getStartPosition())
								->find(T_FUNCTION);
							while ($tokens->next() && $tokens->key() < $element->getEndPosition()) {
								$type = $tokens->getType();
								if (T_FUNCTION === $type) {
									// Skip annonymous functions
									$tokens->find('{')->findMatchingBracket();
								} elseif (T_RETURN === $type && !$tokens->skipWhitespaces()->is(';')) {
									// Skip return without return value
									$return = true;
									break;
								}
							}
							if ($return) {
								if (!isset($annotations['return'])) {
									$undocumented[$class->getName()][] = sprintf('Missing documentation of the return value of the %s.', $label($element));
								} elseif (!preg_match('~^[\w\\\\]+(?:\|[\w\\\\]+)*~s', $annotations['return'][0])) {
									$undocumented[$class->getName()][] = sprintf('Invalid documentation "%s" of the return value of the %s.', $normalize($annotations['return'][0]), $label($element));
								}
							} else {
								if (isset($annotations['return']) && 'void' !== $annotations['return'][0] && !$class->isInterface() && !$element->isAbstract()) {
									$undocumented[$class->getName()][] = sprintf('Existing documentation "%s" of nonexistent return value of the %s.', $normalize($annotations['return'][0]), $label($element));
								}
							}
							if (isset($annotations['return'][1])) {
								$undocumented[$class->getName()][] = sprintf('Duplicate documentation "%s" of the return value of the %s.', $normalize($annotations['return'][1]), $label($element));
							}

							unset($tokens);
						}

						// Data type of constants & properties
						if ($element instanceof ReflectionProperty || $element instanceof ReflectionConstant) {
							if (!isset($annotations['var'])) {
								$undocumented[$class->getName()][] = sprintf('Missing documentation of the data type of the %s.', $label($element));
							} elseif (!preg_match('~^[\w\\\\]+(?:\|[\w\\\\]+)*~', $annotations['var'][0])) {
								$undocumented[$class->getName()][] = sprintf('Invalid documentation "%s" of the data type of the %s.', $normalize($annotations['var'][0]), $label($element));
							}

							if (isset($annotations['var'][1])) {
								$undocumented[$class->getName()][] = sprintf('Duplicate documentation "%s" of the data type of the %s.', $normalize($annotations['var'][1]), $label($element));
							}
						}
					}
				}
			}
			uksort($undocumented, 'strcasecmp');

			$fp = @fopen($this->config->undocumented, 'w');
			if (false === $fp) {
				throw new Exception(sprintf('File %s isn\'t writable.', $this->config->undocumented));
			}
			foreach ($undocumented as $className => $elements) {
				fwrite($fp, sprintf("%s\n%s\n", $className, str_repeat('-', strlen($className))));
				foreach ($elements as $text) {
					fwrite($fp, sprintf("\t%s\n", $text));
				}
				fwrite($fp, "\n");
			}
			fclose($fp);

			$this->incrementProgressBar();

			unset($undocumented);
		}

		// list of deprecated elements
		if ($deprecated) {
			$deprecatedFilter = function($element) {return $element->isDeprecated();};
			$template->deprecatedClasses = array_filter($classes, $deprecatedFilter);
			$this->incrementProgressBar();
			$template->deprecatedInterfaces = array_filter($interfaces, $deprecatedFilter);
			$this->incrementProgressBar();
			$template->deprecatedExceptions = array_filter($exceptions, $deprecatedFilter);
			$this->incrementProgressBar();

			$template->deprecatedMethods = array();
			$template->deprecatedConstants = array();
			$template->deprecatedProperties = array();
			foreach (array('classes', 'interfaces', 'exceptions') as $type) {
				foreach ($$type as $class) {
					if ($class->isDeprecated()) {
						continue;
					}

					$template->deprecatedMethods += array_filter($class->getOwnMethods(), $deprecatedFilter);
					$template->deprecatedConstants += array_filter($class->getOwnConstants(), $deprecatedFilter);
					$template->deprecatedProperties += array_filter($class->getOwnProperties(), $deprecatedFilter);
				}
				$this->incrementProgressBar();
			}

			$template->setFile($templatePath . '/' . $templates['optional']['deprecated']['template'])->save($this->forceDir($destination . '/' . $templates['optional']['deprecated']['filename']));

			$this->incrementProgressBar();

			unset($deprecatedFilter);
			unset($template->deprecatedClasses);
			unset($template->deprecatedInterfaces);
			unset($template->deprecatedExceptions);
			unset($template->deprecatedMethods);
			unset($template->deprecatedConstants);
			unset($template->deprecatedProperties);
		}

		// list of tasks
		if ($todo) {
			$todoFilter = function($element) {return $element->hasAnnotation('todo');};
			$template->todoClasses = array_filter($classes, $todoFilter);
			$this->incrementProgressBar();
			$template->todoInterfaces = array_filter($interfaces, $todoFilter);
			$this->incrementProgressBar();
			$template->todoExceptions = array_filter($exceptions, $todoFilter);
			$this->incrementProgressBar();

			$template->todoMethods = array();
			$template->todoConstants = array();
			$template->todoProperties = array();
			foreach (array('classes', 'interfaces', 'exceptions') as $type) {
				foreach ($$type as $class) {
					$template->todoMethods += array_filter($class->getOwnMethods(), $todoFilter);
					$template->todoConstants += array_filter($class->getOwnConstants(), $todoFilter);
					$template->todoProperties += array_filter($class->getOwnProperties(), $todoFilter);
				}
				$this->incrementProgressBar();
			}

			$template->setFile($templatePath . '/' . $templates['optional']['todo']['template'])->save($this->forceDir($destination . '/' . $templates['optional']['todo']['filename']));

			$this->incrementProgressBar();

			unset($todoFilter);
			unset($template->todoClasses);
			unset($template->todoInterfaces);
			unset($template->todoExceptions);
			unset($template->todoMethods);
			unset($template->todoConstants);
			unset($template->todoProperties);
		}

		// classes/interfaces/exceptions tree
		$classTree = array();
		$interfaceTree = array();
		$exceptionTree = array();

		$processed = array();
		foreach ($this->classes as $className => $reflection) {
			if (!$reflection->isDocumented() || isset($processed[$className])) {
				continue;
			}

			if (null === $reflection->getParentClassName()) {
				// No parent classes
				if ($reflection->isInterface()) {
					$t = &$interfaceTree;
				} elseif ($reflection->isException()) {
					$t = &$exceptionTree;
				} else {
					$t = &$classTree;
				}
			} else {
				foreach (array_values(array_reverse($reflection->getParentClasses())) as $level => $parent) {
					if (0 === $level) {
						// The topmost parent decides about the reflection type
						if ($parent->isInterface()) {
							$t = &$interfaceTree;
						} elseif ($parent->isException()) {
							$t = &$exceptionTree;
						} else {
							$t = &$classTree;
						}
					}
					$parentName = $parent->getName();

					if (!isset($t[$parentName])) {
						$t[$parentName] = array();
						$processed[$parentName] = true;
						ksort($t, SORT_STRING);
					}

					$t = &$t[$parentName];
				}
			}
			$t[$className] = array();
			ksort($t, SORT_STRING);
			$processed[$className] = true;
			unset($t);
		}

		$template->classTree = new Tree($classTree, $this->classes);
		$template->interfaceTree = new Tree($interfaceTree, $this->classes);
		$template->exceptionTree = new Tree($exceptionTree, $this->classes);

		$template->setFile($templatePath . '/' . $templates['main']['tree']['template'])->save($this->forceDir($destination . '/' . $templates['main']['tree']['filename']));

		unset($template->classTree);
		unset($template->interfaceTree);
		unset($template->exceptionTree);
		unset($processed);

		$this->incrementProgressBar();

		// generate package summary
		$this->forceDir($destination . '/' . $templates['main']['package']['filename']);
		$template->namespace = null;
		foreach ($packages as $package => $definition) {
			$pClasses = isset($definition['classes']) ? $definition['classes'] : array();
			uksort($pClasses, 'strcasecmp');
			$pNamespaces = isset($definition['namespaces']) ? array_keys($definition['namespaces']) : array();
			usort($pNamespaces, 'strcasecmp');
			$template->package = $package;
			$template->packages = array($package);
			$template->namespaces = $pNamespaces;
			$template->classes = array_filter($pClasses, $classFilter);
			$template->interfaces = array_filter($pClasses, $interfaceFilter);
			$template->exceptions = array_filter($pClasses, $exceptionFilter);
			$template->setFile($templatePath . '/' . $templates['main']['package']['template'])->save($destination . '/' . $template->getPackageLink($package));

			$this->incrementProgressBar();
		}
		unset($packages);
		unset($pNamespaces);
		unset($pClasses);

		// generate namespace summary
		$this->forceDir($destination . '/' . $templates['main']['namespace']['filename']);
		$template->package = null;
		foreach ($namespaces as $namespace => $definition) {
			$nClasses = isset($definition['classes']) ? $definition['classes'] : array();
			uksort($nClasses, 'strcasecmp');
			$nPackages = isset($definition['packages']) ? array_keys($definition['packages']) : array();
			usort($nPackages, 'strcasecmp');
			$template->package = 1 === count($nPackages) ? $nPackages[0] : null;
			$template->packages = $nPackages;
			$template->namespace = $namespace;
			$template->namespaces = array_filter($namespaceNames, function($item) use($namespace) {
				return strpos($item, $namespace) === 0 || strpos($namespace, $item) === 0;
			});
			$template->classes = array_filter($nClasses, $classFilter);
			$template->interfaces = array_filter($nClasses, $interfaceFilter);
			$template->exceptions = array_filter($nClasses, $exceptionFilter);
			$template->setFile($templatePath . '/' . $templates['main']['namespace']['template'])->save($destination . '/' . $template->getNamespaceLink($namespace));

			$this->incrementProgressBar();
		}
		unset($namespaces);
		unset($nPackages);
		unset($nClasses);

		unset($classFilter);
		unset($interfaceFilter);
		unset($exceptionFilter);

		// generate class & interface files
		$fshl = new \fshlParser('HTML_UTF8', P_TAB_INDENT | P_LINE_COUNTER);
		$this->forceDir($destination . '/' . $templates['main']['class']['filename']);
		$this->forceDir($destination . '/' . $templates['main']['source']['filename']);
		foreach (array('exceptions', 'interfaces', 'classes') as $type) {
			foreach ($$type as $class) {
				$template->package = $package = $class->getPackageName();
				$template->namespace = $namespace = $class->getNamespaceName();
				if ($namespace) {
					$template->namespaces = array_filter($namespaceNames, function($item) use($namespace) {
						return strpos($item, $namespace) === 0 || strpos($namespace, $item) === 0;
					});
				} else {
					$template->namespaces = array();
				}
				$template->packages = array($package);
				$template->tree = array_merge(array_reverse($class->getParentClasses()), array($class));
				$template->classes = !$class->isInterface() && !$class->isException() ? array($class) : array();
				$template->interfaces = $class->isInterface() ? array($class) : array();
				$template->exceptions = $class->isException() ? array($class) : array();

				$template->directSubClasses = $class->getDirectSubClasses();
				uksort($template->directSubClasses, 'strcasecmp');
				$template->indirectSubClasses = $class->getIndirectSubClasses();
				uksort($template->indirectSubClasses, 'strcasecmp');

				$template->directImplementers = $class->getDirectImplementers();
				uksort($template->directImplementers, 'strcasecmp');
				$template->indirectImplementers = $class->getIndirectImplementers();
				uksort($template->indirectImplementers, 'strcasecmp');

				$template->ownMethods = $class->getOwnMethods();
				$template->ownConstants = $class->getOwnConstants();
				$template->ownProperties = $class->getOwnProperties();

				if ($class->isTokenized()) {
					$template->fileName = null;
					$file = $class->getFileName();
					foreach ($this->config->source as $source) {
						if (0 === strpos($file, $source)) {
							$template->fileName = str_replace('\\', '/', substr($file, strlen($source) + 1));
							break;
						}
					}
					if (null === $template->fileName) {
						throw new Exception(sprintf('Could not determine class %s relative path', $class->getName()));
					}
				}

				$template->class = $class;
				$template->setFile($templatePath . '/' . $templates['main']['class']['template'])->save($destination . '/' . $template->getClassLink($class));

				$this->incrementProgressBar();

				// generate source codes
				if ($this->config->sourceCode && $class->isTokenized()) {
					$source = file_get_contents($class->getFileName());
					$source = str_replace(array("\r\n", "\r"), "\n", $source);

					$template->source = $fshl->highlightString('PHP', $source);
					$template->setFile($templatePath . '/' . $templates['main']['source']['template'])->save($destination . '/' . $template->getSourceLink($class, false));

					$this->incrementProgressBar();
				}
			}
			unset($$type);
		}

		// delete tmp directory
		$this->deleteDir($tmp);
	}

	/**
	 * Prints message if printing is enabled.
	 */
	public function output($message)
	{
		if (!$this->config->quiet) {
			echo $message;
		}
	}

	/**
	 * Returns header.
	 *
	 * @return string
	 */
	public static function getHeader()
	{
		$name = sprintf('ApiGen %s', self::VERSION);
		return $name . "\n" . str_repeat('-', strlen($name)) . "\n";
	}

	/**
	 * Prepares the progressbar.
	 *
	 * @param $maximum Maximum progressbar value
	 */
	private function prepareProgressBar($maximum = 1)
	{
		$this->progressBar = new \Console_ProgressBar(
			'[%bar%] %percent%',
			'=>',
			' ',
			80,
			$maximum
		);
	}

	/**
	 * Increments the progressbar by one.
	 *
	 * @param integer $increment Progressbar increment
	 */
	private function incrementProgressBar($increment = 1)
	{
		if ($this->config->progressbar) {
			$this->progressBar->update($this->progressBar->getProgress() + $increment);
		}
	}

	/**
	 * Ensures a directory is created.
	 *
	 * @param string Directory path
	 * @return string
	 */
	private function forceDir($path)
	{
		@mkdir(dirname($path), 0755, true);
		return $path;
	}

	/**
	 * Deletes a directory.
	 *
	 * @param string $path Directory path
	 * @return boolean
	 */
	private function deleteDir($path)
	{
		foreach (Nette\Utils\Finder::find('*')->from($path)->childFirst() as $item) {
			if ($item->isDir()) {
				if (!@rmdir($item)) {
					return false;
				}
			} elseif ($item->isFile()) {
				if (!@unlink($item)) {
					return false;
				}
			}
		}
		if (!@rmdir($path)) {
			return false;
		}

		return true;
	}
}