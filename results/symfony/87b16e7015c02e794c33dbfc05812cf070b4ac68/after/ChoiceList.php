<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Component\Form\Extension\Core\ChoiceList;

use Symfony\Component\Form\Util\FormUtil;
use Symfony\Component\Form\Exception\UnexpectedTypeException;

/**
 * Base class for choice list implementations.
 *
 * @author Bernhard Schussek <bschussek@gmail.com>
 */
abstract class ChoiceList implements ChoiceListInterface
{
    /**
     * Strategy creating new indices/values by creating a copy of the choice.
     *
     * This strategy can only be used for index creation if choices are
     * guaranteed to only contain ASCII letters, digits and underscores.
     *
     * It can be used for value creation if choices can safely be cast into
     * a (unique) string.
     *
     * @var integer
     */
    const COPY_CHOICE = 0;

    /**
     * Strategy creating new indices/values by generating a new integer.
     *
     * This strategy can always be applied, but leads to loss of information
     * in the HTML source code.
     *
     * @var integer
     */
    const GENERATE = 1;

    /**
     * The choices with their indices as keys.
     *
     * @var array
     */
    private $choices = array();

    /**
     * The choice values with the indices of the matching choices as keys.
     *
     * @var array
     */
    private $values = array();

    /**
     * The choice labels with the indices of the matching choices as keys.
     *
     * @var array
     */
    private $labels = array();

    /**
     * The preferred values as flat array with the indices of the matching
     * choices as keys.
     *
     * @var array
     */
    private $preferredValues = array();

    /**
     * The non-preferred values as flat array with the indices of the matching
     * choices as keys.
     *
     * @var array
     */
    private $remainingValues = array();

    /**
     * The preferred values as hierarchy containing also the choice groups
     * with the indices of the matching choices as bottom-level keys.
     *
     * @var array
     */
    private $preferredValueHierarchy = array();

    /**
     * The non-preferred values as hierarchy containing also the choice groups
     * with the indices of the matching choices as bottom-level keys.
     *
     * @var array
     */
    private $remainingValueHierarchy = array();

    /**
     * The strategy used for creating choice indices.
     *
     * @var integer
     * @see COPY_CHOICE
     * @see GENERATE
     */
    private $indexStrategy;

    /**
     * The strategy used for creating choice values.
     *
     * @var integer
     * @see COPY_CHOICE
     * @see GENERATE
     */
    private $valueStrategy;

    /**
     * Creates a new choice list.
     *
     * @param array|\Traversable $choices The array of choices. Choices may also be given
     *                       as hierarchy of unlimited depth. Hierarchies are
     *                       created by creating nested arrays. The title of
     *                       the sub-hierarchy can be stored in the array
     *                       key pointing to the nested array.
     * @param array $labels  The array of labels. The structure of this array
     *                       should match the structure of $choices.
     * @param array $preferredChoices A flat array of choices that should be
     *                                presented to the user with priority.
     * @param integer $valueStrategy The strategy used to create choice values.
     *                               One of COPY_CHOICE and GENERATE.
     * @param integer $indexStrategy The strategy used to create choice indices.
     *                               One of COPY_CHOICE and GENERATE.
     */
    public function __construct($choices, array $labels,
        array $preferredChoices = array(), $valueStrategy, $indexStrategy)
    {
        $this->valueStrategy = $valueStrategy;
        $this->indexStrategy = $indexStrategy;

        $this->initialize($choices, $labels, $preferredChoices);
    }

    /**
     * Initializes the list with choices.
     *
     * Safe to be called multiple times. The list is cleared on every call.
     *
     * @param array|\Traversable $choices The choices to write into the list.
     * @param array $labels The labels belonging to the choices.
     * @param array $preferredChoices The choices to display with priority.
     */
    protected function initialize($choices, array $labels, array $preferredChoices)
    {
        if (!is_array($choices) && !$choices instanceof \Traversable) {
            throw new UnexpectedTypeException($choices, 'array or \Traversable');
        }

        $this->choices = array();
        $this->values = array();
        $this->labels = array();
        $this->preferredValues = array();
        $this->preferredValueHierarchy = array();
        $this->remainingValues = array();
        $this->remainingValueHierarchy = array();

        $this->addChoices(
            $this->preferredValueHierarchy,
            $this->remainingValueHierarchy,
            $choices,
            $labels,
            $preferredChoices
        );
    }

    /**
     * Returns the list of choices
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getChoices()
    {
        return $this->choices;
    }

    /**
     * Returns the labels for the choices
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getLabels()
    {
        return $this->labels;
    }

    /**
     * Returns the values for the choices
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getValues()
    {
        return $this->values;
    }

    /**
     * Returns the values of the choices that should be presented to the user
     * with priority.
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getPreferredValues()
    {
        return $this->preferredValues;
    }

    /**
     * Returns the values of the choices that should be presented to the user
     * with priority as nested array with the choice groups as top-level keys.
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getPreferredValueHierarchy()
    {
        return $this->preferredValueHierarchy;
    }

    /**
     * Returns the values of the choices that are not preferred.
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getRemainingValues()
    {
        return $this->remainingValues;
    }

    /**
     * Returns the values of the choices that are not preferred as nested array
     * with the choice groups as top-level keys.
     *
     * @return array A nested array containing the values with the corresponding
     *               choice indices as keys on the lower levels and the choice
     *               group names in the keys of the topmost level.
     *
     * @see getPreferredValueHierarchy
     */
    public function getRemainingValueHierarchy()
    {
        return $this->remainingValueHierarchy;
    }

    /**
     * Returns the choices corresponding to the given values.
     *
     * @param array $values
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getChoicesForValues(array $values)
    {
        $values = $this->fixValues($values);

        // If the values are identical to the choices, we can just return them
        // to improve performance a little bit
        if ($this->valueStrategy === self::COPY_CHOICE) {
            return $this->fixChoices(array_intersect($values, $this->values));
        }

        $choices = array();

        foreach ($this->values as $i => $value) {
            foreach ($values as $j => $givenValue) {
                if ($value === $givenValue) {
                    $choices[] = $this->choices[$i];
                    unset($values[$j]);

                    if (count($values) === 0) {
                        break 2;
                    }
                }
            }
        }

        return $choices;
    }

    /**
     * Returns the values corresponding to the given choices.
     *
     * @param array $choices
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getValuesForChoices(array $choices)
    {
        $choices = $this->fixChoices($choices);

        // If the values are identical to the choices, we can just return them
        // to improve performance a little bit
        if ($this->valueStrategy === self::COPY_CHOICE) {
            return $this->fixValues(array_intersect($choices, $this->choices));
        }

        $values = array();

        foreach ($this->choices as $i => $choice) {
            foreach ($choices as $j => $givenChoice) {
                if ($choice === $givenChoice) {
                    $values[] = $this->values[$i];
                    unset($choices[$j]);

                    if (count($choices) === 0) {
                        break 2;
                    }
                }
            }
        }

        return $values;
    }

    /**
     * Returns the indices corresponding to the given choices.
     *
     * @param array $choices
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getIndicesForChoices(array $choices)
    {
        $choices = $this->fixChoices($choices);
        $indices = array();

        foreach ($this->choices as $i => $choice) {
            foreach ($choices as $j => $givenChoice) {
                if ($choice === $this->fixChoice($givenChoice)) {
                    $indices[] = $i;
                    unset($choices[$j]);

                    if (count($choices) === 0) {
                        break 2;
                    }
                }
            }
        }

        return $indices;
    }

    /**
     * Returns the indices corresponding to the given values.
     *
     * @param array $values
     *
     * @return array
     *
     * @see Symfony\Component\Form\Extension\Core\ChoiceList\ChoiceListInterface
     */
    public function getIndicesForValues(array $values)
    {
        $values = $this->fixValues($values);
        $indices = array();

        foreach ($this->values as $i => $value) {
            foreach ($values as $j => $givenValue) {
                if ($value === $givenValue) {
                    $indices[] = $i;
                    unset($values[$j]);

                    if (count($values) === 0) {
                        break 2;
                    }
                }
            }
        }

        return $indices;
    }

    /**
     * Recursively adds the given choices to the list.
     *
     * @param array $bucketForPreferred The bucket where to store the preferred
     *                                  values.
     * @param array $bucketForRemaining The bucket where to store the
     *                                  non-preferred values.
     * @param array $choices The list of choices.
     * @param array $labels The labels corresponding to the choices.
     * @param array $preferredChoices The preferred choices.
     *
     * @throws UnexpectedTypeException If the structure of the $labels array
     *                                 does not match the structure of the
     *                                 $choices array.
     */
    protected function addChoices(&$bucketForPreferred, &$bucketForRemaining, $choices, $labels, array $preferredChoices)
    {
        // Add choices to the nested buckets
        foreach ($choices as $group => $choice) {
            if (is_array($choice)) {
                if (!is_array($labels)) {
                    throw new UnexpectedTypeException($labels, 'array');
                }

                // Don't do the work if the array is empty
                if (count($choice) > 0) {
                    $this->addChoiceGroup(
                        $group,
                        $bucketForPreferred,
                        $bucketForRemaining,
                        $choice,
                        $labels[$group],
                        $preferredChoices
                    );
                }
            } else {
                $this->addChoice(
                    $bucketForPreferred,
                    $bucketForRemaining,
                    $choice,
                    $labels[$group],
                    $preferredChoices
                );
            }
        }
    }

    /**
     * Recursively adds a choice group.
     *
     * @param string $group The name of the group.
     * @param array $bucketForPreferred The bucket where to store the preferred
     *                                  values.
     * @param array $bucketForRemaining The bucket where to store the
     *                                  non-preferred values.
     * @param array $choices The list of choices in the group.
     * @param array $labels The labels corresponding to the choices in the group.
     * @param array $preferredChoices The preferred choices.
     */
    protected function addChoiceGroup($group, &$bucketForPreferred, &$bucketForRemaining, $choices, $labels, array $preferredChoices)
    {
        // If this is a choice group, create a new level in the choice
        // key hierarchy
        $bucketForPreferred[$group] = array();
        $bucketForRemaining[$group] = array();

        $this->addChoices(
            $bucketForPreferred[$group],
            $bucketForRemaining[$group],
            $choices,
            $labels,
            $preferredChoices
        );

        // Remove child levels if empty
        if (empty($bucketForPreferred[$group])) {
            unset($bucketForPreferred[$group]);
        }
        if (empty($bucketForRemaining[$group])) {
            unset($bucketForRemaining[$group]);
        }
    }

    /**
     * Adds a new choice.
     *
     * @param array $bucketForPreferred The bucket where to store the preferred
     *                                  values.
     * @param array $bucketForRemaining The bucket where to store the
     *                                  non-preferred values.
     * @param mixed $choice The choice to add.
     * @param string $label The label for the choice.
     * @param array $preferredChoices The preferred choices.
     */
    protected function addChoice(&$bucketForPreferred, &$bucketForRemaining, $choice, $label, array $preferredChoices)
    {
        $index = $this->createIndex($choice);

        // Always store values as strings to facilitate comparisons
        $value = $this->fixValue($this->createValue($choice));

        $this->choices[$index] = $this->fixChoice($choice);
        $this->labels[$index] = $label;
        $this->values[$index] = $value;

        if ($this->isPreferred($choice, $preferredChoices)) {
            $bucketForPreferred[$index] = $value;
            $this->preferredValues[$index] = $value;
        } else {
            $bucketForRemaining[$index] = $value;
            $this->remainingValues[$index] = $value;
        }
    }

    /**
     * Returns whether the given choice should be preferred judging by the
     * given array of preferred choices.
     *
     * Extension point to optimize performance by changing the structure of the
     * $preferredChoices array.
     *
     * @param mixed $choice The choice to test.
     * @param array $preferredChoices An array of preferred choices.
     */
    protected function isPreferred($choice, $preferredChoices)
    {
        return false !== array_search($choice, $preferredChoices, true);
    }

    /**
     * Creates a new unique index for this choice.
     *
     * Extension point to change the indexing strategy.
     *
     * @param mixed $choice The choice to create an index for
     *
     * @return integer|string A unique index containing only ASCII letters,
     *                        digits and underscores.
     */
    protected function createIndex($choice)
    {
        if ($this->indexStrategy === self::COPY_CHOICE) {
            return $choice;
        }

        return count($this->choices);
    }

    /**
     * Creates a new unique value for this choice.
     *
     * Extension point to change the value strategy.
     *
     * @param mixed $choice The choice to create a value for
     *
     * @return integer|string A unique value without character limitations.
     */
    protected function createValue($choice)
    {
        if ($this->valueStrategy === self::COPY_CHOICE) {
            return $choice;
        }

        return count($this->values);
    }

    /**
     * Fixes the data type of the given choice value to avoid comparison
     * problems.
     *
     * @param mixed $value The choice value.
     *
     * @return string The value as string.
     */
    protected function fixValue($value)
    {
        return (string) $value;
    }

    /**
     * Fixes the data types of the given choice values to avoid comparison
     * problems.
     *
     * @param array $values The choice values.
     *
     * @return array The values as strings.
     */
    protected function fixValues(array $values)
    {
        return array_map(array($this, 'fixValue'), $values);
    }

    /**
     * Fixes the data type of the given choice index to avoid comparison
     * problems.
     *
     * @param mixed $index The choice index.
     *
     * @return integer|string The index as PHP array key.
     */
    protected function fixIndex($index)
    {
        if (is_bool($index) || (string) (int) $index === (string) $index) {
            return (int) $index;
        }

        return (string) $index;
    }

    /**
     * Fixes the data types of the given choice indices to avoid comparison
     * problems.
     *
     * @param array $indices The choice indices.
     *
     * @return array The indices as strings.
     */
    protected function fixIndices(array $indices)
    {
        return array_map(array($this, 'fixIndex'), $indices);
    }

    /**
     * Fixes the data type of the given choice to avoid comparison problems.
     *
     * Extension point. In this implementation, choices are guaranteed to
     * always maintain their type and thus can be typesafely compared.
     *
     * @param mixed $choice The choice.
     *
     * @return mixed The fixed choice.
     */
    protected function fixChoice($choice)
    {
        return $choice;
    }

    /**
    * Fixes the data type of the given choices to avoid comparison problems.
     *
    * @param array $choice The choices.
    *
    * @return array The fixed choices.
    *
    * @see fixChoice
    */
    protected function fixChoices(array $choices)
    {
        return $choices;
    }
}