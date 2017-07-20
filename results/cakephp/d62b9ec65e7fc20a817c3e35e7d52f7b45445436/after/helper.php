<?php
/* SVN FILE: $Id$ */

/**
 * Short description for file.
 *
 * Long description for file
 *
 * PHP versions 4 and 5
 *
 * CakePHP :  Rapid Development Framework <http://www.cakephp.org/>
 * Copyright (c) 2005, CakePHP Authors/Developers
 *
 * Author(s): Michal Tatarynowicz aka Pies <tatarynowicz@gmail.com>
 *            Larry E. Masters aka PhpNut <nut@phpnut.com>
 *            Kamil Dzielinski aka Brego <brego.dk@gmail.com>
 *
 *  Licensed under The MIT License
 *  Redistributions of files must retain the above copyright notice.
 *
 * @filesource
 * @author       CakePHP Authors/Developers
 * @copyright    Copyright (c) 2005, CakePHP Authors/Developers
 * @link         https://trac.cakephp.org/wiki/Authors Authors/Developers
 * @package      cake
 * @subpackage   cake.cake.libs.view
 * @since        CakePHP v 0.2.9
 * @version      $Revision$
 * @modifiedby   $LastChangedBy$
 * @lastmodified $Date$
 * @license      http://www.opensource.org/licenses/mit-license.php The MIT License
 */

/**
 * Backend for helpers.
 *
 * Long description for class
 *
 * @package    cake
 * @subpackage cake.cake.libs.view
 * @since      CakePHP v 0.9.2
 *
 */
class Helper extends Object
{
    /*************************************************************************
    * Public variables
    *************************************************************************/

    /**#@+
    * @access public
    */


    /**
     * Holds tag templates.
     *
     * @access public
     * @var array
     */
    var $tags = array();

    /**#@-*/

    /*************************************************************************
    * Public methods
    *************************************************************************/

    /**#@+
    * @access public
    */

    /**
     * Constructor.
     *
     * Parses tag templates into $this->tags.
     *
     * @return void
     */
    function Helper()
    {
        $this->tags = $this->readConfigFile(CONFIGS.'tags.ini.php');
    }

    /**
     * Decides wheter to output or return a string.
     *
     * Based on AUTO_OUTPUT and $return's value, this method decides wheter to
     * output a string, or return it.
     *
     * @param  string  $str    String to be outputed or returned.
     * @param  boolean $return Wheter this method should return a value or
     *                         output it. This overrides AUTO_OUTPUT.
     * @return mixed   Either string or boolean value, depends on AUTO_OUTPUT
     *                 and $return.
     */
    function output($str, $return = false)
    {
        if (AUTO_OUTPUT && $return === false)
        {
            if (print $str)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return $str;
        }
    }

    /**
     * Assigns values to tag templates.
     *
     * Finds a tag template by $keyName, and replaces $values's keys with
     * $values's keys.
     *
     * @param  string $keyName Name of the key in the tag array.
     * @param  array  $values  Values to be inserted into tag.
     * @return string Tag with inserted values.
     */
    function assign($keyName, $values)
    {
        return str_replace('%%'.array_keys($values).'%%', array_values($values),
        $this->tags[$keyName]);
    }

/**
 * Enter description here...
 *
 * @param unknown_type $fileName
 * @return unknown
 */
    function readConfigFile ($fileName)
    {
        $fileLineArray = file($fileName);

        foreach ($fileLineArray  as $fileLine)
        {
            $dataLine = trim($fileLine);
            $firstChar = substr($dataLine, 0, 1);
            if ($firstChar != ';' && $dataLine != '')
            {
                if ($firstChar == '[' && substr($dataLine, -1, 1) == ']')
                {
                    // [section block] we might use this later do not know for sure
                    // this could be used to add a key with the section block name
                    // but it adds another array level
                }
                else
                {
                    $delimiter = strpos($dataLine, '=');
                    if ($delimiter > 0)
                    {
                        $key = strtolower(trim(substr($dataLine, 0, $delimiter)));
                        $value = trim(substr($dataLine, $delimiter + 1));
                        if (substr($value, 0, 1) == '"' && substr($value, -1) == '"')
                        {
                            $value = substr($value, 1, -1);
                        }
                        $iniSetting[$key] = stripcslashes($value);
                    }
                    else
                    {
                        $iniSetting[strtolower(trim($dataLine))]='';
                    }
                }
            }
            else
            {
            }
        }
        return $iniSetting;
    }

    /**#@-*/
}

?>