<?php

// This file is part of Moodle - http://moodle.org/
//
// Moodle is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Moodle is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.
/**
 * File containing the Class Declaration Test.
 *
 * @package   lib-pear-php-codesniffer-standards-moodle-sniffs-classes
 * @copyright 2008 Nicolas Connault
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

/**
 * Class Declaration Test.
 *
 * Checks the declaration of the class is correct.
 *
 * @copyright 2009 Nicolas Connault
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */
class moodle_sniffs_classes_classdeclarationsniff implements php_codesniffer_sniff
{


    /**
     * Returns an array of tokens this test wants to listen for.
     *
     * @return array
     */
    public function register()
    {
        return array(
                T_CLASS,
                T_INTERFACE,
               );

    }


    /**
     * Processes this test, when one of its tokens is encountered.
     *
     * @param PHP_CodeSniffer_File $phpcsfile The file being scanned.
     * @param int                  $stackptr  The position of the current token in the
     *                                        stack passed in $tokens.
     *
     * @return void
     */
    public function process(PHP_CodeSniffer_File $phpcsfile, $stackptr)
    {
        $tokens = $phpcsfile->gettokens();

        if (isset($tokens[$stackptr]['scope_opener']) === false) {
            $error  = 'Possible parse error: ';
            $error .= $tokens[$stackptr]['content'];
            $error .= ' missing opening or closing brace';
            $phpcsfile->addwarning($error, $stackptr);
            return;
        }

        $curlyBrace  = $tokens[$stackptr]['scope_opener'];
        $lastcontent = $phpcsfile->findPrevious(T_WHITESPACE, ($curlyBrace - 1), $stackptr, true);
        $classline   = $tokens[$lastcontent]['line'];
        $braceline   = $tokens[$curlyBrace]['line'];
        if ($braceline != $classline) {
            $error  = 'Opening brace of a ';
            $error .= $tokens[$stackptr]['content'];
            $error .= ' must be on the same line as the definition';
            $phpcsfile->adderror($error, $curlyBrace);
            return;
        }

        if ($tokens[($curlyBrace - 1)]['code'] === T_WHITESPACE) {
            $prevcontent = $tokens[($curlyBrace - 1)]['content'];
            if ($prevcontent !== $phpcsfile->eolChar) {
                $blankSpace = substr($prevcontent, strpos($prevcontent, $phpcsfile->eolChar));
                $spaces     = strlen($blankSpace);
                if ($spaces !== 1) {
                    $error = "Expected 1 space before opening brace; $spaces found";
                    $phpcsfile->adderror($error, $curlyBrace);
                }
            }
        }

    }


}

?>