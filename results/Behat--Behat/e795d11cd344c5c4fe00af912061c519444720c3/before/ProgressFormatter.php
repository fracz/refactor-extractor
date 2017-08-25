<?php

namespace Behat\Behat\Output\Formatter;

use Symfony\Component\EventDispatcher\EventDispatcher,
    Symfony\Component\EventDispatcher\Event;

use Behat\Gherkin\Node\ScenarioNode,
    Behat\Gherkin\Node\BackgroundNode;

use Behat\Behat\Tester\StepTester;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Progress Console Formatter.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class ProgressFormatter extends ConsoleFormatter implements FormatterInterface
{
    protected $dispatcher;

    protected $maxDescriptionLength = 0;

    /**
     * @see     Behat\Behat\Formatter\FormatterInterface
     */
    public function registerListeners(EventDispatcher $dispatcher)
    {
        $this->dispatcher = $dispatcher;

        $dispatcher->connect('step.run.after',          array($this, 'printStep'),          10);

        $dispatcher->connect('suite.run.after',         array($this, 'printEmptyLine'),     10);
        $dispatcher->connect('suite.run.after',         array($this, 'printFailedSteps'),   10);
        $dispatcher->connect('suite.run.after',         array($this, 'printPendingSteps'),  10);
        $dispatcher->connect('suite.run.after',         array($this, 'printStatistics'),    10);
        $dispatcher->connect('suite.run.after',         array($this, 'printSnippets'),      10);
    }

    /**
     * @see     ConsoleFormatter
     */
    protected function getDispatcher()
    {
        return $this->dispatcher;
    }

    /**
      * Listen to `step.run.after` event & print step run information.
      *
      * @param   Event   $event  notified event
      */
    public function printStep(Event $event)
    {
        $step = $event->getSubject();

        switch ($event->get('result')) {
            case StepTester::PASSED:
                $this->write('.', 'passed', false);
                break;
            case StepTester::SKIPPED:
                $this->write('-', 'skipped', false);
                break;
            case StepTester::PENDING:
                $this->write('P', 'pending', false);
                break;
            case StepTester::UNDEFINED:
                $this->write('U', 'undefined', false);
                break;
            case StepTester::FAILED:
                $this->write('F', 'failed', false);
                break;
        }
    }

    /**
      * Listen to `suite.run.after` event & print empty line.
      *
      * @param   Event   $event  notified event
      */
    public function printEmptyLine(Event $event)
    {
        $this->write("\n");
    }

    /**
      * Listen to `suite.run.after` event & print failed steps info.
      *
      * @param   Event   $event  notified event
      */
    public function printFailedSteps(Event $event)
    {
        $statistics = $event->getSubject()->get('behat.statistics_collector');

        if (count($statistics->getFailedStepsEvents())) {
            $this->write(sprintf("(::) %s (::)\n", $this->getTranslator()->trans('failed steps')), 'failed');

            foreach ($statistics->getFailedStepsEvents() as $number => $event) {
                // Print step exception
                if (null !== $event->get('exception')) {
                    if ($this->verbose) {
                        $error = (string) $event->get('exception');
                    } else {
                        $error = $event->get('exception')->getMessage();
                    }
                    $this->write(
                        sprintf("%s. %s"
                          , str_pad((string) ($number + 1), 2, '0', STR_PAD_LEFT)
                          , strtr($error, array("\n" => "\n    "))
                        )
                    , 'failed');
                }

                $this->printStepEventInformation($event, 'failed');
            }
        }
    }

    /**
      * Listen to `suite.run.after` event & print pending steps info.
      *
      * @param   Event   $event  notified event
      */
    public function printPendingSteps(Event $event)
    {
        $statistics = $event->getSubject()->get('behat.statistics_collector');

        if (count($statistics->getPendingStepsEvents())) {
            $this->write(sprintf("(::) %s (::)\n", $this->getTranslator()->trans('pending steps')), 'failed');

            foreach ($statistics->getPendingStepsEvents() as $number => $event) {
                // Print step exception
                if (null !== $event->get('exception')) {
                    if ($this->verbose) {
                        $error = (string) $event->get('exception');
                    } else {
                        $error = $event->get('exception')->getMessage();
                    }
                    $this->write(
                        sprintf("%s. %s"
                          , str_pad((string) ($number + 1), 2, '0', STR_PAD_LEFT)
                          , strtr($error, array("\n" => "\n    "))
                        )
                    , 'pending');
                }

                $this->printStepEventInformation($event, 'pending');
            }
        }
    }

    /**
     * Print step information (filepath, fileline, exception description).
     *
     * @param   Event   $event  step event
     * @param   string  $type   information type (pending/failed etc.)
     */
    protected function printStepEventInformation(Event $event, $type)
    {
        $step = $event->getSubject();

        // Print step information
        $description = $this->colorize(
            sprintf("    In step `%s %s'.", $step->getType(), $step->getText())
        , $type);
        $this->maxDescriptionLength = $this->maxDescriptionLength > mb_strlen($description)
            ? $this->maxDescriptionLength
            : mb_strlen($description);
        $this->write($description, null, false);
        if (null !== $event->get('definition')) {
            $this->printLineSourceComment(
                mb_strlen($description)
              , $event->get('definition')->getFile()
              , $event->get('definition')->getLine()
            );
        } else {
            $this->write();
        }

        // Print scenario information
        $item = $step->getParent();
        if ($item instanceof ScenarioNode) {
            $description    = $this->colorize(
                sprintf("    From scenario %s."
                  , $item->getTitle() ? sprintf("`%s'", $item->getTitle()) : '***'
                )
            , $type);
        } elseif ($item instanceof BackgroundNode) {
            $description    = $this->colorize('    From scenario background.', $type);
        }
        $this->maxDescriptionLength = $this->maxDescriptionLength > mb_strlen($description)
            ? $this->maxDescriptionLength
            : mb_strlen($description);
        $this->write($description, null, false);
        $this->printLineSourceComment(
            mb_strlen($description)
          , $item->getFile()
          , $item->getLine()
        );
        $this->write();
    }
}
