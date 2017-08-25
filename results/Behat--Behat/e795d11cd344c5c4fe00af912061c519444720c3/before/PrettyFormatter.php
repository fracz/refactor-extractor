<?php

namespace Behat\Behat\Output\Formatter;

use Symfony\Component\DependencyInjection\Container,
    Symfony\Component\EventDispatcher\EventDispatcher,
    Symfony\Component\EventDispatcher\Event;

use Behat\Gherkin\Node\AbstractNode,
    Behat\Gherkin\Node\AbstractScenarioNode,
    Behat\Gherkin\Node\FeatureNode,
    Behat\Gherkin\Node\StepNode,
    Behat\Gherkin\Node\BackgroundNode,
    Behat\Gherkin\Node\ScenarioNode,
    Behat\Gherkin\Node\OutlineNode,
    Behat\Gherkin\Node\PyStringNode,
    Behat\Gherkin\Node\TableNode,
    Behat\Gherkin\Node\ExamplesNode;

use Behat\Behat\Exception\Pending,
    Behat\Behat\Tester\StepTester;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Pretty Console Formatter.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class PrettyFormatter extends ConsoleFormatter implements FormatterInterface, ContainerAwareFormatterInterface
{
    protected $container;
    protected $dispatcher;

    protected $backgroundPrinted            = false;
    protected $outlineStepsPrinted          = false;
    protected $maxDescriptionLength         = 0;
    protected $outlineSubresultExceptions   = array();

    /**
     * @see     FormatterInterface
     */
    public function registerListeners(EventDispatcher $dispatcher)
    {
        $this->dispatcher = $dispatcher;

        $dispatcher->connect('feature.run.before',      array($this, 'printFeatureHeader'),     10);

        $dispatcher->connect('outline.run.before',      array($this, 'printOutlineHeader'),     10);
        $dispatcher->connect('outline.sub.run.after',   array($this, 'printOutlineSubResult'),  10);
        $dispatcher->connect('outline.run.after',       array($this, 'printOutlineFooter'),     10);

        $dispatcher->connect('scenario.run.before',     array($this, 'printScenarioHeader'),    10);
        $dispatcher->connect('scenario.run.after',      array($this, 'printScenarioFooter'),    10);

        $dispatcher->connect('background.run.before',   array($this, 'printBackgroundHeader'),  10);
        $dispatcher->connect('background.run.after',    array($this, 'printBackgroundFooter'),  10);

        $dispatcher->connect('step.run.after',          array($this, 'printStep'),              10);

        $dispatcher->connect('suite.run.after',         array($this, 'printStatistics'),        10);
        $dispatcher->connect('suite.run.after',         array($this, 'printSnippets'),          10);
    }

    /**
     * @see     ContainerAwareFormatterInterface
     */
    public function setContainer(Container $container)
    {
        $this->container = $container;
    }

    /**
     * @see     ConsoleFormatter
     */
    protected function getDispatcher()
    {
        return $this->dispatcher;
    }

    /**
     * Listen to `feature.run.before` event & print feature header.
     *
     * @param   Event   $event  notified event
     */
    public function printFeatureHeader(Event $event)
    {
        $feature = $event->getSubject();

        // Flush variables
        $this->backgroundPrinted    = false;
        $this->outlineStepsPrinted  = false;

        // Print tags if had ones
        if ($feature->hasTags()) {
            $this->write($this->getTagsString($feature), 'tag');
        }

        // Print feature title
        $keyword     = $this->transGherkinKeyword('Feature', $feature->getLanguage());
        $title       = $feature->getTitle() ? ' ' . $feature->getTitle() : '';
        $description = sprintf("%s:%s", $keyword, $title);
        $this->write($description);

        // Print feature description
        if (null !== $feature->getDescription()) {
            foreach (explode("\n", $feature->getDescription()) as $description) {
                $this->write('  ' . $description);
            }
        }
        $this->write();

        // Run fake background to test if it runs without errors & print it output
        if ($feature->hasBackground()) {
            $this->container->get('behat.statistics_collector')->pause();

            $environment = $this->container->get('behat.environment_builder')->buildEnvironment();

            // Fire `scenario.before` hooks for 1st scenario
            $scenarios = $feature->getScenarios();
            $this->container->get('behat.hooks_container')->fireScenarioHooks(
                new Event($scenarios[0], 'scenario.run.before', array('environment' => $environment))
            );

            // Run fake background
            $tester = $this->container->get('behat.background_tester');
            $tester->setEnvironment($environment);
            $feature->getBackground()->accept($tester);

            $this->container->get('behat.statistics_collector')->resume();
            $this->backgroundPrinted = true;
        }
    }

    /**
      * Listen to `outline.run.before` event & print outline header.
      *
      * @param  Event   $event  notified event
      */
    public function printOutlineHeader(Event $event)
    {
        $outline    = $event->getSubject();
        $examples   = $outline->getExamples();

        // Recalc maximum description length (for filepath-like comments)
        $this->recalcMaxDescriptionLength($outline);

        // Print tags if had ones
        if ($outline->hasTags()) {
            $this->write('  ' . $this->getTagsString($outline), 'tag');
        }

        // Print outline description
        $keyword     = $this->transGherkinKeyword('Scenario Outline', $outline->getLanguage());
        $title       = $outline->getTitle() ? ' ' . $outline->getTitle() : '';
        $description = sprintf("  %s:%s", $keyword, $title);
        $this->write($description, null, false);

        // Print element path & line
        $this->printLineSourceComment(
            mb_strlen($description)
          , $outline->getFile()
          , $outline->getLine()
        );

        // Print outline steps
        $environment = $this->container->get('behat.environment_builder')->buildEnvironment();
        $this->container->get('behat.statistics_collector')->pause();
        foreach ($outline->getSteps() as $step) {
            $tester = $this->container->get('behat.step_tester');
            $tester->setEnvironment($environment);
            $tester->setTokens(current($examples->getHash()));
            $tester->skip();
            $step->accept($tester);
        }
        $this->container->get('behat.statistics_collector')->resume();

        $this->outlineStepsPrinted = true;

        // Print outline examples title
        $keyword = $this->transGherkinKeyword('Examples', $outline->getLanguage());
        $this->write(sprintf("\n    %s:", $keyword));

        // print outline examples header row
        $this->write(
            preg_replace(
                '/|([^|]*)|/'
              , $this->colorize('$1', 'skipped')
              , '      ' . $examples->getRowAsString(0)
            )
        );
    }

    /**
     * Listen to `outline.sub.run.after` event & print outline subscenario results.
     *
     * @param   Event   $event  notified event
     */
    public function printOutlineSubResult(Event $event)
    {
        $outline    = $event->getSubject();
        $examples   = $outline->getExamples();

        // print current scenario results row
        $this->write(
            preg_replace(
                '/|([^|]*)|/'
              , $this->colorize('$1', $event->get('result'))
              , '      ' . $examples->getRowAsString($event->get('iteration') + 1)
            )
        );

        // Print errors
        foreach ($this->outlineSubresultExceptions as $exception) {
            if ($this->verbose) {
                $error = (string) $exception;
            } else {
                $error = $exception->getMessage();
            }
            if ($exception instanceof Pending) {
                $status = 'pending';
            } else {
                $status = 'failed';
            }
            $this->write('        ' . strtr($error, array("\n" => "\n      ")), $status);
        }
        $this->outlineSubresultExceptions = array();
    }

    /**
      * Listen to `outline.run.after` event & print outline footer.
      *
      * @param   Event   $event  notified event
      */
    public function printOutlineFooter(Event $event)
    {
        $this->outlineStepsPrinted = false;
        $this->write();
    }

    /**
      * Listen to `scenario.run.before` event & print scenario header.
      *
      * @param   Event   $event  notified event
      */
    public function printScenarioHeader(Event $event)
    {
        $scenario = $event->getSubject();

        // Recalc maximum description length (for filepath-like comments)
        $this->recalcMaxDescriptionLength($scenario);

        // Print tags if had ones
        if ($scenario->hasTags()) {
            $this->write('  ' . $this->getTagsString($scenario), 'tag');
        }

        // Print scenario header
        $keyword     = $this->transGherkinKeyword('Scenario', $scenario->getLanguage());
        $title       = $scenario->getTitle() ? ' ' . $scenario->getTitle() : '';
        $description = sprintf("  %s:%s", $keyword, $title);
        $this->write($description, null, false);

        // Print element path & line
        $this->printLineSourceComment(
            mb_strlen($description)
          , $scenario->getFile()
          , $scenario->getLine()
        );
    }

    /**
      * Listen to `scenario.run.after` event & print scenario footer.
      *
      * @param   Event   $event  notified event
      */
    public function printScenarioFooter(Event $event)
    {
        $this->write();
    }

    /**
      * Listen to `background.run.before` event & print background header.
      *
      * @param   Event   $event  notified event
      */
    public function printBackgroundHeader(Event $event)
    {
        if (!$this->backgroundPrinted) {
            $background = $event->getSubject();

            // Recalc maximum description length (for filepath-like comments)
            $this->recalcMaxDescriptionLength($background);

            // Print description
            $keyword     = $this->transGherkinKeyword('Background', $background->getLanguage());
            $description = sprintf("  %s:", $keyword);
            $this->write($description, null, false);

            // Print element path & line
            $this->printLineSourceComment(
                mb_strlen($description)
              , $background->getFile()
              , $background->getLine()
            );
        }
    }

    /**
      * Listen to `background.run.after` event & print background footer.
      *
      * @param   Event   $event  notified event
      */
    public function printBackgroundFooter(Event $event)
    {
        if (!$this->backgroundPrinted) {
            $this->write();
        }
    }

    /**
      * Listen to `step.run.after` event & print step run information.
      *
      * @param   Event   $event  notified event
      */
    public function printStep(Event $event)
    {
        $step = $event->getSubject();

        if (!($step->getParent() instanceof BackgroundNode) || !$this->backgroundPrinted) {
            if (!($step->getParent() instanceof OutlineNode) || !$this->outlineStepsPrinted) {
                // Get step description
                $text           = $this->outlineStepsPrinted ? $step->getText() : $step->getCleanText();
                $printableText  = $text;
                $description    = sprintf('    %s %s', $step->getType(), $text);

                // Colorize arguments
                if (null !== $event->get('definition') && StepTester::UNDEFINED !== $event->get('result')) {
                    $argStartCode   = $this->colorizeStart($event->get('result') + 10);
                    $argFinishCode  = $this->colorizeFinish() . $this->colorizeStart($event->get('result'));
                    $printableText  = preg_replace_callback(
                        $event->get('definition')->getRegex()
                      , function ($matches) use($argStartCode, $argFinishCode) {
                          $text = array_shift($matches);
                          foreach ($matches as $match) {
                              $text = strtr($text, array(
                                  '"' . $match . '"'    => '"' . $argStartCode . $match . $argFinishCode . '"'
                                , '\'' . $match . '\''  => '\'' . $argStartCode . $match . $argFinishCode . '\''
                                , ' ' . $match . ' '    => ' ' . $argStartCode . $match . $argFinishCode . ' '
                                , ' ' . $match          => ' ' . $argStartCode . $match . $argFinishCode
                                , $match . ' '          => $argStartCode . $match . $argFinishCode . ' '
                              ));
                          }

                          return $text;
                      }
                      , $printableText
                    );
                }

                // Print step description
                $printableDescription = sprintf('    %s %s', $step->getType(), $printableText);
                $this->write($printableDescription, $event->get('result'), false);

                // Print definition path if found one
                if (null !== $event->get('definition')) {
                    $this->printLineSourceComment(
                        mb_strlen($description)
                      , $event->get('definition')->getFile()
                      , $event->get('definition')->getLine()
                    );
                } else {
                    $this->write();
                }

                // print step arguments
                if ($step->hasArguments()) {
                    foreach ($step->getArguments() as $argument) {
                        if ($argument instanceof PyStringNode) {
                            $this->write($this->getPyString($argument, 6), $event->get('result'));
                        } elseif ($argument instanceof TableNode) {
                            $this->write($this->getTableString($argument, 6), $event->get('result'));
                        }
                    }
                }

                // Print step exception
                if (null !== $event->get('exception')) {
                    if ($this->verbose) {
                        $error = (string) $event->get('exception');
                    } else {
                        $error = $event->get('exception')->getMessage();
                    }
                    $this->write(
                        '      ' . strtr($error, array("\n" => "\n      ")), $event->get('result')
                    );
                }
            } else {
                if (null !== $event->get('exception')) {
                    $this->outlineSubresultExceptions[] = $event->get('exception');
                }
            }
        }
    }

    /**
     * Recalculate max descriptions size for section elements.
     *
     * @param   BackgroundNode $scenario   element for calculations
     *
     * @return  integer                 description length
     */
    protected function recalcMaxDescriptionLength(AbstractScenarioNode $scenario)
    {
        $max    = $this->maxDescriptionLength;
        $type   = '';

        if ($scenario instanceof OutlineNode) {
            $type = $this->transGherkinKeyword('Scenario Outline', $scenario->getLanguage());
        } else if ($scenario instanceof ScenarioNode) {
            $type = $this->transGherkinKeyword('Scenario', $scenario->getLanguage());
        } else if ($scenario instanceof BackgroundNode) {
            $type = $this->transGherkinKeyword('Background', $scenario->getLanguage());
        }
        $scenarioDescription = !($scenario instanceof BackgroundNode) && $scenario->getTitle()
            ? $type . ': ' . $scenario->getTitle()
            : $type;

        if (($tmp = mb_strlen($scenarioDescription) + 2) > $max) {
            $max = $tmp;
        }

        foreach ($scenario->getSteps() as $step) {
            $stepDescription = $step->getType() . ' ' . $step->getCleanText();
            if (($tmp = mb_strlen($stepDescription) + 4) > $max) {
                $max = $tmp;
            }
        }

        $this->maxDescriptionLength = $max;
    }

    /**
     * Return formatted tag string, prepared for console output.
     *
     * @param   BackgroundNode $section    section instance
     *
     * @return  string
     */
    protected function getTagsString(AbstractNode $section)
    {
        $tags = array();
        foreach ($section->getTags() as $tag) {
            $tags[] = '@' . $tag;
        }

        return implode(' ', $tags);
    }

    /**
     * Return formatted PyString, prepared for console output.
     *
     * @param   PyStringNode    $pystring   PyString
     * @param   integer         $indent     indentation spaces count
     *
     * @return  string
     */
    protected function getPyString(PyStringNode $pystring, $indent = 6)
    {
        return strtr(
            sprintf("%s\"\"\"\n%s\n\"\"\"", str_repeat(' ', $indent), (string) $pystring),
            array("\n" => "\n" . str_repeat(' ', $indent))
        );
    }

    /**
     * Return formatted Table, prepared for console output.
     *
     * @param   TableNode   $table      Table instance
     * @param   string      $indent     indentation spaces count
     *
     * @return  string
     */
    protected function getTableString(TableNode $table, $indent = 6)
    {
        return strtr(
            sprintf(str_repeat(' ', $indent).'%s', $table),
            array("\n" => "\n".str_repeat(' ', $indent))
        );
    }
}