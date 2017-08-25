<?php
/**
 * phpDocumentor
 *
 * PHP Version 5.3
 *
 * @copyright 2010-2013 Mike van Riel / Naenius (http://www.naenius.com)
 * @license   http://www.opensource.org/licenses/mit-license.php MIT
 * @link      http://phpdoc.org
 */

namespace phpDocumentor\Compiler\Pass;

use phpDocumentor\Compiler\CompilerPassInterface;
use phpDocumentor\Descriptor\Collection;
use phpDocumentor\Descriptor\DescriptorAbstract;
use phpDocumentor\Descriptor\Interfaces\ClassInterface;
use phpDocumentor\Descriptor\Interfaces\InterfaceInterface;
use phpDocumentor\Descriptor\Interfaces\TraitInterface;
use phpDocumentor\Descriptor\ProjectDescriptor;

/**
 * This class constructs the index 'elements' and populates it with all Structural Elements.
 *
 * Please note that due to a conflict between namespace FQSEN's and that of classes, interfaces, traits and functions
 * will the namespace FQSEN be prefixed with a tilde (~).
 */
class ElementsIndexBuilder implements CompilerPassInterface
{
    const COMPILER_PRIORITY = 15000;

    /**
     * {@inheritDoc}
     */
    public function execute(ProjectDescriptor $project)
    {
        $elementCollection = new Collection();
        $project->getIndexes()->set('elements', $elementCollection);

        foreach ($project->getFiles() as $file) {
            /** @var DescriptorAbstract[] $elements */
            $elements = array_merge(
                $file->getConstants()->getAll(),
                $file->getFunctions()->getAll(),
                $file->getClasses()->getAll(),
                $file->getInterfaces()->getAll(),
                $file->getTraits()->getAll()
            );

            foreach ($elements as $element) {
                $elementCollection->set($element->getFullyQualifiedStructuralElementName(), $element);

                // process all sub-elements as well
                $subElements = $this->getSubElements($element);
                foreach ($subElements as $subElement) {
                    $elementCollection->set($subElement->getFullyQualifiedStructuralElementName(), $subElement);
                }
            }
        }
    }

    /**
     * Returns any sub-elements for the given element.
     *
     * This method checks whether the given element is a class, interface or trait and returns
     * their methods, properties and constants accordingly, or an empty array if no sub-elements
     * are applicable.
     *
     * @param DescriptorAbstract $element
     *
     * @return DescriptorAbstract[]
     */
    protected function getSubElements(DescriptorAbstract $element)
    {
        $subElements = array();

        if ($element instanceof ClassInterface) {
            $subElements = array_merge(
                $element->getMethods()->getAll(),
                $element->getConstants()->getAll(),
                $element->getProperties()->getAll()
            );
        }

        if ($element instanceof InterfaceInterface) {
            $subElements = array_merge(
                $element->getMethods()->getAll(),
                $element->getConstants()->getAll()
            );
        }

        if ($element instanceof TraitInterface) {
            $subElements = array_merge(
                $element->getMethods()->getAll(),
                $element->getProperties()->getAll()
            );
        }

        return $subElements;
    }
}