<?php

/*
 * This file is part of the Behat Testwork.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Behat\Testwork\EventDispatcher\Event;

use Behat\Testwork\Tester\Result\TestResult;
use Symfony\Component\EventDispatcher\Event;

/**
 * Testwork exercise completed event.
 *
 * @author Konstantin Kudryashov <ever.zet@gmail.com>
 */
class ExerciseCompleted extends Event
{
    const BEFORE = 'tester.exercise_completed.before';
    const AFTER = 'tester.exercise_completed.after';

    /**
     * @var null|TestResult
     */
    private $testResult;
    /**
     * @var Boolean
     */
    private $stopped = false;

    /**
     * Initializes event.
     *
     * @param null|TestResult $testResult
     * @param Boolean         $stopped
     */
    public function __construct(TestResult $testResult = null, $stopped = false)
    {
        $this->testResult = $testResult;
        $this->stopped = $stopped;
    }

    /**
     * Checks whether exercise was completed successfully.
     *
     * @return Boolean
     */
    public function isSuccessfullyCompleted()
    {
        return !$this->stopped;
    }

    /**
     * Returns exercise test result (if tested).
     *
     * @return null|TestResult
     */
    public function getTestResult()
    {
        return $this->testResult;
    }

    /**
     * Returns step tester result status.
     *
     * @return integer
     */
    public function getResultCode()
    {
        if (null === $this->testResult) {
            return null;
        }

        return $this->testResult->getResultCode();
    }
}