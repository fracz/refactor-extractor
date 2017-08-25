<?php

namespace Behat\Behat\Definition;

use Symfony\Component\Translation\TranslatorInterface;

use Behat\Gherkin\Node\StepNode,
    Behat\Gherkin\Node\PyStringNode,
    Behat\Gherkin\Node\TableNode;

use Behat\Behat\Definition\Proposal\DefinitionProposalDispatcher,
    Behat\Behat\Exception\Redundant,
    Behat\Behat\Exception\Ambiguous,
    Behat\Behat\Exception\Undefined,
    Behat\Behat\Context\ContextInterface;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Definition dispatcher.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class DefinitionDispatcher
{
    /**
     * Added transformations.
     *
     * @var     array
     */
    private $transformations = array();
    /**
     * Added definitions.
     *
     * @var     array
     */
    private $definitions     = array();
    /**
     * Proposals dispatcher
     *
     * @var     Behat\Behat\Definition\Proposal\DefinitionProposalDispatcher
     */
    private $proposalDispatcher;
    /**
     * Translator instance.
     *
     * @var     Symfony\Component\Translation\Translator
     */
    private $translator;

    /**
     * Initializes definition dispatcher.
     *
     * @param   Behat\Behat\Definition\Proposal\DefinitionProposalDispatcher    $proposalDispatcher
     * @param   Symfony\Component\Translation\TranslatorInterface               $translator
     */
    public function __construct(DefinitionProposalDispatcher $proposalDispatcher, TranslatorInterface $translator)
    {
        $this->proposalDispatcher   = $proposalDispatcher;
        $this->translator           = $translator;
    }

    /**
     * Adds definition to dispatcher.
     *
     * @param   Behat\Behat\Definition\DefinitionInterface  $definition definition instance
     */
    public function addDefinition(DefinitionInterface $definition)
    {
        $regex = $definition->getRegex();

        if (isset($this->definitions[$regex])) {
            throw new Redundant($definition, $this->definitions[$regex]);
        }

        $this->definitions[$regex] = $definition;
    }

    /**
     * Returns array of available definitions.
     *
     * @return  array   array of hashes => array(regex => definition)
     */
    public function getDefinitions()
    {
        return $this->definitions;
    }

    /**
     * Adds transformation to dispatcher.
     *
     * @param   Behat\Behat\Definition\TransformationInterface  $transformation definitions transformation
     */
    public function addTransformation(TransformationInterface $transformation)
    {
        $this->transformations[] = $transformation;
    }

    /**
     * Returns array of available transformations.
     *
     * @return  array   array of argument transformers
     */
    public function getTransformations()
    {
        return $this->transformations;
    }

    /**
     * Finds step definition, that match specified step.
     *
     * @param   Behat\Behat\Context\ContextInterface    $context    context instance
     * @param   Behat\Gherkin\Node\StepNode             $step       found step
     *
     * @return  Behat\Behat\Definition\Definition
     *
     * @uses    loadDefinitions()
     *
     * @throws  Behat\Behat\Exception\Ambiguous  if step description is ambiguous
     * @throws  Behat\Behat\Exception\Undefined  if step definition not found
     */
    public function findDefinition(ContextInterface $context, StepNode $step)
    {
        $text       = $step->getText();
        $multiline  = $step->getArguments();
        $matches    = array();

        // find step to match
        foreach ($this->getDefinitions() as $origRegex => $definition) {
            $transRegex = $this->translateDefinitionRegex($origRegex, $step->getLanguage());
            if (preg_match($origRegex, $text, $arguments)
            || ($origRegex !== $transRegex && preg_match($transRegex, $text, $arguments))) {
                // prepare callback arguments
                $arguments = $this->prepareCallbackArguments(
                    $context, $definition->getCallbackReflection(), array_slice($arguments, 1), $multiline
                );

                // transform arguments
                foreach ($arguments as $num => $argument) {
                    foreach ($this->getTransformations() as $transformation) {
                        if ($newArgument = $transformation->transform($context, $argument)) {
                            $arguments[$num] = $newArgument;
                        }
                    }
                }

                // set matched definition
                $definition->setMatchedText($text);
                $definition->setValues($arguments);
                $matches[] = $definition;
            }
        }

        if (count($matches) > 1) {
            throw new Ambiguous($text, $matches);
        }

        if (0 === count($matches)) {
            throw new Undefined($text);
        }

        return $matches[0];
    }

    /**
     * Returns step definition for step node.
     *
     * @param   Behat\Behat\Context\ContextInterface    $context    context instance
     * @param   Behat\Gherkin\Node\StepNode             $step       step node
     *
     * @return  array   hash (md5_key => definition)
     */
    public function proposeDefinition(ContextInterface $context, StepNode $step)
    {
        return $this->proposalDispatcher->propose($context, $step);
    }

    /**
     * Translates definition regex to provided language (if possible).
     *
     * @param   string  $regex      regex to translate
     * @param   string  $language   language
     *
     * @return  string
     */
    public function translateDefinitionRegex($regex, $language)
    {
        return $this->translator->trans($regex, array(), 'behat.definitions', $language);
    }

    /**
     * Merges found arguments with multiliners and maps them to the function callback signature.
     *
     * @param   Behat\Behat\Context\ContextInterface    $context    context instance
     * @param   ReflectionFunctionAbstract              $refl       callback reflection
     * @param   array                                   $arguments  found arguments
     * @param   array                                   $multiline  multiline arguments of the step
     *
     * @return  array
     */
    private function prepareCallbackArguments(ContextInterface $context, \ReflectionFunctionAbstract $refl,
                                              array $arguments, array $multiline)
    {
        $parametersRefl = $refl->getParameters();

        if ($refl->isClosure()) {
            array_shift($parametersRefl);
        }

        $resulting = array();
        foreach ($parametersRefl as $num => $parameterRefl) {
            if (isset($arguments[$parameterRefl->getName()])) {
                $resulting[] = $arguments[$parameterRefl->getName()];
            } elseif (isset($arguments[$num])) {
                $resulting[] = $arguments[$num];
            }
        }

        foreach ($multiline as $argument) {
            $resulting[] = $argument;
        }

        return $resulting;
    }
}