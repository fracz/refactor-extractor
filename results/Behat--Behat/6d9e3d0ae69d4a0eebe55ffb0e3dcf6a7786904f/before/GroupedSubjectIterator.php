<?php

/*
 * This file is part of the Behat Testwork.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Behat\Testwork\Subject\Iterator;

use Behat\Testwork\Suite\Suite;

/**
 * Testwork grouped subjects iterator.
 *
 * Iterates over subject iterators grouped by their suites.
 *
 * @author Konstantin Kudryashov <ever.zet@gmail.com>
 */
class GroupedSubjectIterator implements SubjectIterator
{
    /**
     * @var Suite
     */
    private $suite;
    /**
     * @var SubjectIterator[]
     */
    private $subjects;
    /**
     * @var integer
     */
    private $position = 0;

    /**
     * Initializes subjects.
     *
     * @param Suite             $suite
     * @param SubjectIterator[] $subjects
     */
    public function __construct(Suite $suite, array $subjects)
    {
        $this->suite = $suite;
        $this->subjects = $subjects;
    }

    /**
     * Groups subjects by their suite.
     *
     * @param SubjectIterator[] $suitesSubjects
     *
     * @return GroupedSubjectIterator[]
     */
    public static function group(array $suitesSubjects)
    {
        $groupedSubjects = array();
        foreach ($suitesSubjects as $suiteSubjects) {
            $groupedSubjects[$suiteSubjects->getSuite()->getName()][] = $suiteSubjects;
        }

        return array_map(
            function ($subjects) {
                return new GroupedSubjectIterator($subjects[0]->getSuite(), $subjects);
            }, $groupedSubjects
        );
    }

    /**
     * Returns suite that was used to load subjects.
     *
     * @return Suite
     */
    public function getSuite()
    {
        return $this->suite;
    }

    /**
     * Rewinds the Iterator to the first element.
     */
    public function rewind()
    {
        $this->position = 0;
        if (isset($this->subjects[$this->position])) {
            $this->subjects[$this->position]->rewind();
        }
    }

    /**
     * Moves forward to the next element.
     */
    public function next()
    {
        $this->subjects[$this->position]->next();
        if (!$this->subjects[$this->position]->valid()) {
            $this->position++;

            if (isset($this->subjects[$this->position])) {
                $this->subjects[$this->position]->rewind();
            }
        }
    }

    /**
     * Checks if current position is valid.
     *
     * @return Boolean
     */
    public function valid()
    {
        return isset($this->subjects[$this->position]) && $this->subjects[$this->position]->valid();
    }

    /**
     * Returns the current element.
     *
     * @return null|mixed
     */
    public function current()
    {
        return $this->subjects[$this->position]->current();
    }

    /**
     * Returns the key of the current element.
     *
     * @return string
     */
    public function key()
    {
        return $this->position + $this->subjects[$this->position]->key();
    }
}