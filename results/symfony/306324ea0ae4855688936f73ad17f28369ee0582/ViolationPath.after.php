<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Component\Form\Extension\Validator\ViolationMapper;

use Symfony\Component\Form\Util\PropertyPath;
use Symfony\Component\Form\Util\PropertyPathIterator;
use Symfony\Component\Form\Util\PropertyPathInterface;

/**
 * @author Bernhard Schussek <bschussek@gmail.com>
 */
class ViolationPath implements \IteratorAggregate, PropertyPathInterface
{
    /**
     * @var array
     */
    private $elements = array();

    /**
     * @var array
     */
    private $positions = array();

    /**
     * @var array
     */
    private $isIndex = array();

    /**
     * @var array
     */
    private $mapsForm = array();

    /**
     * @var string
     */
    private $string = '';

    /**
     * @var integer
     */
    private $length = 0;

    /**
     * Creates a new violation path from a string.
     *
     * @param string $violationPath The property path of a {@link ConstraintViolation}
     *                              object.
     */
    public function __construct($violationPath)
    {
        $path = new PropertyPath($violationPath);
        $pathElements = $path->getElements();
        $pathPositions = $path->getPositions();
        $elements = array();
        $positions = array();
        $isIndex = array();
        $mapsForm = array();
        $data = false;

        for ($i = 0, $l = count($pathElements); $i < $l; ++$i) {
            if (!$data) {
                // The element "data" has not yet been passed
                if ('children' === $pathElements[$i] && $path->isProperty($i)) {
                    // Skip element "children"
                    ++$i;

                    // Next element must exist and must be an index
                    // Otherwise not a valid path
                    if ($i >= $l || !$path->isIndex($i)) {
                        return;
                    }

                    $elements[] = $pathElements[$i];
                    $positions[] = $pathPositions[$i];
                    $isIndex[] = true;
                    $mapsForm[] = true;
                } elseif ('data' === $pathElements[$i] && $path->isProperty($i)) {
                    // Skip element "data"
                    ++$i;

                    // End of path
                    if ($i >= $l) {
                        break;
                    }

                    $elements[] = $pathElements[$i];
                    $positions[] = $pathPositions[$i];
                    $isIndex[] = $path->isIndex($i);
                    $mapsForm[] = false;
                    $data = true;
                } else {
                    // Neither "children" nor "data" property found
                    // Be nice and consider this the end of the path
                    break;
                }
            } else {
                // Already after the "data" element
                // Pick everything as is
                $elements[] = $pathElements[$i];
                $positions[] = $pathPositions[$i];
                $isIndex[] = $path->isIndex($i);
                $mapsForm[] = false;
            }
        }

        $this->elements = $elements;
        $this->positions = $positions;
        $this->isIndex = $isIndex;
        $this->mapsForm = $mapsForm;
        $this->length = count($elements);
        $this->string = $violationPath;

        $this->resizeString();
    }

    /**
     * {@inheritdoc}
     */
    public function __toString()
    {
        return $this->string;
    }

    /**
     * {@inheritdoc}
     */
    public function getPositions()
    {
        return $this->positions;
    }

    /**
     * {@inheritdoc}
     */
    public function getLength()
    {
        return $this->length;
    }

    /**
     * {@inheritdoc}
     */
    public function getParent()
    {
        if ($this->length <= 1) {
            return null;
        }

        $parent = clone $this;

        --$parent->length;
        array_pop($parent->elements);
        array_pop($parent->isIndex);
        array_pop($parent->mapsForm);
        array_pop($parent->positions);

        $parent->resizeString();

        return $parent;
    }

    /**
     * {@inheritdoc}
     */
    public function getElements()
    {
        return $this->elements;
    }

    /**
     * {@inheritdoc}
     */
    public function getElement($index)
    {
        return $this->elements[$index];
    }

    /**
     * {@inheritdoc}
     */
    public function isProperty($index)
    {
        return !$this->isIndex[$index];
    }

    /**
     * {@inheritdoc}
     */
    public function isIndex($index)
    {
        return $this->isIndex[$index];
    }

    /**
     * Returns whether an element maps directly to a form.
     *
     * Consider the following violation path:
     *
     * <code>
     * children[address].children[office].data.street
     * </code>
     *
     * In this example, "address" and "office" map to forms, while
     * "street does not.
     *
     * @param  integer $index The element index.
     *
     * @return Boolean        Whether the element maps to a form.
     */
    public function mapsForm($index)
    {
        return $this->mapsForm[$index];
    }


    /**
     * Returns a new iterator for this path
     *
     * @return ViolationPathIterator
     */
    public function getIterator()
    {
        return new ViolationPathIterator($this);
    }

    /**
     * Resizes the string representation to match the number of elements.
     */
    private function resizeString()
    {
        $lastIndex = $this->length - 1;

        if ($lastIndex < 0) {
            $this->string = '';
        } else {
            // +1 for the dot/opening bracket
            $length = $this->positions[$lastIndex] + strlen($this->elements[$lastIndex]) + 1;

            if ($this->isIndex[$lastIndex]) {
                // +1 for the closing bracket
                ++$length;
            }

            $this->string = substr($this->string, 0, $length);
        }
    }
}