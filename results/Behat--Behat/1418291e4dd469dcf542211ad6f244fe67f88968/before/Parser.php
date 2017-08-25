<?php

namespace Everzet\Gherkin;

use \Everzet\Gherkin\ParserException;
use \Everzet\Gherkin\InlineStructures\PyString;
use \Everzet\Gherkin\InlineStructures\Table;
use \Everzet\Gherkin\InlineStructures\Examples;

/*
 * This file is part of the behat package.
 * (c) 2010 Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Gherkin Parser.
 *
 * @package     behat
 * @subpackage  Gherkin
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class Parser
{
    protected $lines            = array();
    protected $currentLineNb    = -1;
    protected $currentLine      = '';
    protected $regex            = null;
    protected $feature          = null;

    public function parse($value)
    {
        $this->currentLineNb = -1;
        $this->currentLine = '';
        $this->lines = explode("\n", $this->cleanup($value));

        if (preg_match('#^\#\s*language\:\s*(?P<lang>[\w]+?)\s*$#', $this->lines[0], $values)) {
            $class = sprintf("Everzet\Gherkin\\I18n\\%s", $values['lang']);
            $this->regex = new $class;
        } else {
            $this->regex = new \Everzet\Gherkin\I18n\en;
        }

        if (function_exists('mb_internal_encoding') && ((int) ini_get('mbstring.func_overload')) & 2) {
            $mbEncoding = mb_internal_encoding();
            mb_internal_encoding('ASCII');
        }

        while ($this->moveToNextLine()) {
            if ($this->isCurrentLineEmpty()) {
                continue;
            }

            // feature?
            if (preg_match($this->regex->getFeatureRegex(), $this->currentLine, $values)) {
                $this->feature = new Feature;
                $this->feature->setTitle(isset($values['title']) ? $values['title'] : '');
                $this->feature->addTags($this->getPreviousTags());
                $this->feature->addDescriptions($this->getNextDescriptions());
            }

            // background?
            if (preg_match($this->regex->getBackgroundRegex(), $this->currentLine, $values)) {
                $background = new Background;
                $background->setTitle($this->getNextTitle(
                    isset($values['title']) ? $values['title'] : ''
                ));
                $background->addSteps($this->getNextSteps());

                $this->feature->addBackground($background);
            }

            // scenario?
            if (preg_match($this->regex->getScenarioRegex(), $this->currentLine, $values)) {
                $scenario = new Scenario;
                $scenario->setTitle($this->getNextTitle(
                    isset($values['title']) ? $values['title'] : ''
                ));
                $scenario->addTags($this->getPreviousTags());
                $scenario->addSteps($this->getNextSteps());

                $this->feature->addScenario($scenario);
            }

            // scenario outline?
            if (preg_match($this->regex->getScenarioOutlineRegex(), $this->currentLine, $values)) {
                $outline = new ScenarioOutline;
                $outline->setTitle($this->getNextTitle(
                    isset($values['title']) ? $values['title'] : ''
                ));
                $outline->addTags($this->getPreviousTags());
                $outline->addSteps($this->getNextSteps());
                if (null === ($examples = $this->getNextExamples())) {
                    throw new ParserException(
                        sprintf('No examples in %s outline', $outline->getTitle())
                    );
                }
                $outline->setExamples($examples);

                $this->feature->addScenario($outline);
            }
        }

        if (isset($mbEncoding)) {
            mb_internal_encoding($mbEncoding);
        }

        return $this->feature;
    }

    protected function getPreviousTags()
    {
        $tags = array();

        if (preg_match($this->regex->getTagsRegex(), $this->getPreviousLine(), $values)) {
            $tags = array_map(function($item) {
                return trim($item);
            }, explode($this->regex->getTagKeyword(), $values['tags']));
        }

        return $tags;
    }

    protected function getNextTitle($title = '')
    {
        while($this->moveToNextLine()) {
            if ($this->isCurrentLineEmpty()) {
                continue;
            }
            if (
                preg_match($this->regex->getStepsRegex(), $this->currentLine) ||
                preg_match($this->regex->getTableRegex(), $this->currentLine)
            ) {
                break;
            }
            $title .= empty($title) ? trim($this->currentLine) : ' ' . trim($this->currentLine);
        }
        $this->moveToPreviousLine();

        return $title;
    }

    protected function getNextDescriptions()
    {
        $lines = array();

        while ($this->moveToNextLine()) {
            if ($this->isCurrentLineEmpty()) {
                continue;
            }
            if (!preg_match($this->regex->getDescriptionRegex(), $this->currentLine, $values)) {
                break;
            }

            $description = trim($values['description']);
            if (!empty($description)) {
                $lines[] = $values['description'];
            }
        }
        $this->moveToPreviousLine();

        return $lines;
    }

    protected function getNextSteps()
    {
        $steps = array();

        while ($this->moveToNextLine()) {
            if ($this->isCurrentLineEmpty()) {
                continue;
            }
            if (!preg_match($this->regex->getStepsRegex(), $this->currentLine, $values)) {
                break;
            }

            $step = new Step($values['type'], $values['step']);
            if (null !== ($pystring = $this->getNextPyString())) {
                $step->addArgument($pystring);
            }
            if (null !== ($table = $this->getNextTable())) {
                $step->addArgument($table);
            }
            $steps[] = $step;
        }
        $this->moveToPreviousLine();

        return $steps;
    }

    protected function getNextPyString()
    {
        $pystring = null;

        if (
            $this->moveToNextLine() &&
            preg_match($this->regex->getPyStringStarterRegex(), $this->currentLine, $values)
        ) {
            $pystring = new PyString(strlen($values['indent']));

            while (
                $this->moveToNextLine() &&
                !preg_match($this->regex->getPyStringStarterRegex(), $this->currentLine)
            ) {
                $pystring->addLine($this->currentLine);
            }
        } else {
            $this->moveToPreviousLine();
        }

        return $pystring;
    }

    protected function getNextTable()
    {
        $table = null;

        while ($this->moveToNextLine()) {
            if ($this->isCurrentLineEmpty()) {
                continue;
            }
            if (!preg_match($this->regex->getTableRegex(), $this->currentLine, $values)) {
                break;
            }

            if (null === $table) {
                $table = new Table($this->regex->getTableSplitter());
            }
            $table->addRow($values['row']);
        }
        $this->moveToPreviousLine();

        return $table;
    }

    protected function getNextExamples()
    {
        $examples = null;

        while($this->moveToNextLine()) {
            if (!$this->isCurrentLineEmpty()) {
                break;
            }
        }
        if (preg_match($this->regex->getExamplesRegex(), $this->currentLine, $values)) {
            $examples = new Examples(
                $this->getNextTitle(isset($values['title']) ? $values['title'] : '')
            );
            $examples->setTable($this->getNextTable());
        }
        $this->moveToPreviousLine();

        return $examples;
    }

    protected function getPreviousLine()
    {
        if ($this->currentLineNb != 0) {
            return $this->lines[$this->currentLineNb - 1];
        } else {
            return $this->currentLine;
        }
    }

    protected function getNextLine()
    {
        if ($this->currentLineNb != count($this->lines)) {
            return $this->lines[$this->currentLineNb + 1];
        } else {
            return $this->currentLine;
        }
    }

    protected function moveToNextLine()
    {
        if ($this->currentLineNb >= count($this->lines) - 1) {
            return false;
        }

        $this->currentLine = $this->lines[++$this->currentLineNb];

        return true;
    }

    protected function moveToPreviousLine()
    {
        $this->currentLine = $this->lines[--$this->currentLineNb];
    }

    protected function isCurrentLineEmpty()
    {
        return $this->isCurrentLineBlank() || $this->isCurrentLineComment();
    }

    protected function isCurrentLineBlank()
    {
        return '' == trim($this->currentLine, ' ');
    }

    protected function isCurrentLineComment()
    {
        //checking explicitly the first char of the trim is faster than loops or strpos
        $ltrimmedLine = ltrim($this->currentLine, ' ');
        return $ltrimmedLine[0] === '#';
    }

    protected function cleanup($value)
    {
        $value = str_replace(array("\r\n", "\r"), "\n", $value);

        if (!preg_match("#\n$#", $value)) {
            $value .= "\n";
        }

        return $value;
    }
}