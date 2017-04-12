<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Component\Routing\Loader;

use Symfony\Component\Routing\RouteCollection;
use Symfony\Component\Routing\Route;
use Symfony\Component\Config\Resource\FileResource;
use Symfony\Component\Config\Loader\FileLoader;

/**
 * XmlFileLoader loads XML routing files.
 *
 * @author Fabien Potencier <fabien@symfony.com>
 *
 * @api
 */
class XmlFileLoader extends FileLoader
{
    const NAMESPACE_URI = 'http://symfony.com/schema/routing';
    const SCHEME_PATH = '/schema/routing/routing-1.0.xsd';

    /**
     * Loads an XML file.
     *
     * @param string      $file An XML file path
     * @param string|null $type The resource type
     *
     * @return RouteCollection A RouteCollection instance
     *
     * @throws \InvalidArgumentException When a tag can't be parsed
     *
     * @api
     */
    public function load($file, $type = null)
    {
        $path = $this->locator->locate($file);

        $xml = $this->loadFile($path);

        $collection = new RouteCollection();
        $collection->addResource(new FileResource($path));

        // process routes and imports
        foreach ($xml->documentElement->childNodes as $node) {
            if (!$node instanceof \DOMElement) {
                continue;
            }

            $this->parseNode($collection, $node, $path, $file);
        }

        return $collection;
    }

    /**
     * Parses a node from a loaded XML file.
     *
     * @param RouteCollection  $collection the collection to associate with the node
     * @param \DOMElement      $node       the node to parse
     * @param string           $path       the path of the XML file being processed
     * @param string           $file
     *
     * @throws \InvalidArgumentException When a tag can't be parsed
     */
    protected function parseNode(RouteCollection $collection, \DOMElement $node, $path, $file)
    {
        if (self::NAMESPACE_URI !== $node->namespaceURI) {
            return;
        }

        switch ($node->localName) {
            case 'route':
                $this->parseRoute($collection, $node, $path);
                break;
            case 'import':
                $resource = $node->getAttribute('resource');
                $type = $node->getAttribute('type');
                $prefix = $node->getAttribute('prefix');
                $hostnamePattern = $node->hasAttribute('hostname-pattern') ? $node->getAttribute('hostname-pattern') : null;

                $defaults = array();
                $requirements = array();
                $options = array();

                foreach ($node->getElementsByTagNameNS(self::NAMESPACE_URI, '*') as $n) {
                    switch ($n->localName) {
                        case 'default':
                            $defaults[$n->getAttribute('key')] = trim($n->textContent);
                            break;
                        case 'requirement':
                            $requirements[$n->getAttribute('key')] = trim($n->textContent);
                            break;
                        case 'option':
                            $options[$n->getAttribute('key')] = trim($n->textContent);
                            break;
                        default:
                            throw new \InvalidArgumentException(sprintf('Unknown tag "%s" used in file "%s". Expected "default", "requirement" or "option".', $n->localName, $file));
                    }
                }

                $this->setCurrentDir(dirname($path));

                $subCollection = $this->import($resource, ('' !== $type ? $type : null), false, $file);
                /* @var $subCollection RouteCollection */
                $subCollection->addPrefix($prefix);
                if (null !== $hostnamePattern) {
                    $subCollection->setHostnamePattern($hostnamePattern);
                }
                $subCollection->addDefaults($defaults);
                $subCollection->addRequirements($requirements);
                $subCollection->addOptions($options);

                $collection->addCollection($subCollection);
                break;
            default:
                throw new \InvalidArgumentException(sprintf('Unknown tag "%s" used in file "%s". Expected "route" or "import".', $node->localName, $file));
        }
    }

    /**
     * {@inheritdoc}
     *
     * @api
     */
    public function supports($resource, $type = null)
    {
        return is_string($resource) && 'xml' === pathinfo($resource, PATHINFO_EXTENSION) && (!$type || 'xml' === $type);
    }

    /**
     * Parses a route and adds it to the RouteCollection.
     *
     * @param RouteCollection $collection A RouteCollection instance
     * @param \DOMElement     $definition Route definition
     * @param string          $file       An XML file path
     *
     * @throws \InvalidArgumentException When the definition cannot be parsed
     */
    protected function parseRoute(RouteCollection $collection, \DOMElement $definition, $file)
    {
        $defaults = array();
        $requirements = array();
        $options = array();

        foreach ($definition->getElementsByTagNameNS(self::NAMESPACE_URI, '*') as $node) {
            switch ($node->localName) {
                case 'default':
                    $defaults[$node->getAttribute('key')] = trim($node->textContent);
                    break;
                case 'option':
                    $options[$node->getAttribute('key')] = trim($node->textContent);
                    break;
                case 'requirement':
                    $requirements[$node->getAttribute('key')] = trim($node->textContent);
                    break;
                default:
                    throw new \InvalidArgumentException(sprintf('Unknown tag "%s" used in file "%s". Expected "default", "requirement" or "option".', $node->localName, $file));
            }
        }

        $route = new Route($definition->getAttribute('pattern'), $defaults, $requirements, $options, $definition->getAttribute('hostname-pattern'));

        $collection->add($definition->getAttribute('id'), $route);
    }

    /**
     * Loads an XML file.
     *
     * @param string $file An XML file path
     *
     * @return \DOMDocument
     *
     * @throws \InvalidArgumentException When loading of XML file returns error
     */
    protected function loadFile($file)
    {
        $internalErrors = libxml_use_internal_errors(true);
        $disableEntities = libxml_disable_entity_loader(true);
        libxml_clear_errors();

        $dom = new \DOMDocument();
        $dom->validateOnParse = true;
        if (!$dom->loadXML(file_get_contents($file), LIBXML_NONET | (defined('LIBXML_COMPACT') ? LIBXML_COMPACT : 0))) {
            libxml_disable_entity_loader($disableEntities);

            throw new \InvalidArgumentException(implode("\n", $this->getXmlErrors($internalErrors)));
        }
        $dom->normalizeDocument();

        libxml_use_internal_errors($internalErrors);
        libxml_disable_entity_loader($disableEntities);

        foreach ($dom->childNodes as $child) {
            if ($child->nodeType === XML_DOCUMENT_TYPE_NODE) {
                throw new \InvalidArgumentException('Document types are not allowed.');
            }
        }

        $this->validate($dom);

        return $dom;
    }

    /**
     * Validates a loaded XML file.
     *
     * @param \DOMDocument $dom A loaded XML file
     *
     * @throws \InvalidArgumentException When XML doesn't validate its XSD schema
     */
    protected function validate(\DOMDocument $dom)
    {
        $current = libxml_use_internal_errors(true);
        libxml_clear_errors();

        if (!$dom->schemaValidate(__DIR__ . static::SCHEME_PATH)) {
            throw new \InvalidArgumentException(implode("\n", $this->getXmlErrors($current)));
        }
        libxml_use_internal_errors($current);
    }

    /**
     * Retrieves libxml errors and clears them.
     *
     * @return array An array of libxml error strings
     */
    private function getXmlErrors($internalErrors)
    {
        $errors = array();
        foreach (libxml_get_errors() as $error) {
            $errors[] = sprintf('[%s %s] %s (in %s - line %d, column %d)',
                LIBXML_ERR_WARNING == $error->level ? 'WARNING' : 'ERROR',
                $error->code,
                trim($error->message),
                $error->file ? $error->file : 'n/a',
                $error->line,
                $error->column
            );
        }

        libxml_clear_errors();
        libxml_use_internal_errors($internalErrors);

        return $errors;
    }
}