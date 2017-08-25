<?php

namespace Behat\Behat\Definition\Proposal;

use Behat\Gherkin\Node\StepNode,
    Behat\Gherkin\Node\PyStringNode,
    Behat\Gherkin\Node\TableNode;

use Behat\Behat\Context\ContextInterface,
    Behat\Behat\Definition\DefinitionSnippet;

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Annotated definitions proposal.
 *
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class AnnotatedDefinitionProposal implements DefinitionProposalInterface
{
    /**
     * Total count of proposed methods.
     *
     * @var     integer
     */
    private static $proposedMethodsCount = 0;

    /**
     * @see     Behat\Behat\Definition\Proposal\DefinitionProposalInterface::supports()
     */
    public function supports(ContextInterface $context)
    {
        return true;
    }

    /**
     * @see     Behat\Behat\Definition\Proposal\DefinitionProposalInterface::propose()
     */
    public function propose(StepNode $step)
    {
        $text = $step->getText();
        $replacePatterns = array(
            '/\'([^\']*)\'/', '/\"([^\"]*)\"/', // Quoted strings
            '/(\d+)/',                          // Numbers
        );

        $regex = preg_replace('/([\/\[\]\(\)\\\^\$\.\|\?\*\+])/', '\\\\$1', $text);
        $regex = preg_replace(
            $replacePatterns,
            array(
                "\'([^\']*)\'", "\"([^\"]*)\"",
                "(\\d+)",
            ),
            $regex
        );
        $regex = preg_replace('/\'.*(?<!\')/', '\\\\$0', $regex); // Single quotes without matching pair (escape in resulting regex)
        preg_match('/' . $regex . '/', $text, $matches);
        $count = count($matches) - 1;

        $methodName = preg_replace($replacePatterns, '', $text);
        $methodName = preg_replace('/[^a-zA-Z\_\ ]/', '', $methodName);
        $methodName = str_replace(' ', '', ucwords($methodName));

        if (0 !== strlen($methodName)) {
            $methodName[0] = strtolower($methodName[0]);
        } else {
            $methodName = 'stepDefinition' . ++static::$proposedMethodsCount;
        }

        $args = array();
        for ($i = 0; $i < $count; $i++) {
            $args[] = "\$argument" . ($i + 1);
        }

        foreach ($step->getArguments() as $argument) {
            if ($argument instanceof PyStringNode) {
                $args[] = "PyStringNode \$string";
            } elseif ($argument instanceof TableNode) {
                $args[] = "TableNode \$table";
            }
        }

        $description = sprintf(<<<PHP
    /**
     * @%s /^%s$/
     */
    public function %s(%s)
    {
        throw new PendingException();
    }
PHP
          , '%s', $regex, $methodName, implode(', ', $args)
        );

        return new DefinitionSnippet($step, $description);
    }
}