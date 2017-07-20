<?php
/*
* 2007-2015 PrestaShop
*
* NOTICE OF LICENSE
*
* This source file is subject to the Open Software License (OSL 3.0)
* that is bundled with this package in the file LICENSE.txt.
* It is also available through the world-wide-web at this URL:
* http://opensource.org/licenses/osl-3.0.php
* If you did not receive a copy of the license and are unable to
* obtain it through the world-wide-web, please send an email
* to license@prestashop.com so we can send you a copy immediately.
*
* DISCLAIMER
*
* Do not edit or add to this file if you wish to upgrade PrestaShop to newer
* versions in the future. If you wish to customize PrestaShop for your
* needs please refer to http://www.prestashop.com for more information.
*
*  @author PrestaShop SA <contact@prestashop.com>
*  @copyright  2007-2015 PrestaShop SA
*  @version  Release: $Revision$
*  @license    http://opensource.org/licenses/osl-3.0.php  Open Software License (OSL 3.0)
*  International Registered Trademark & Property of PrestaShop SA
*/

namespace PrestaShop\PrestaShop\Tests\TestCase;

use ReflectionClass;
use PHPUnit_Framework_TestCase;

class ReflexionHelper extends PHPUnit_Framework_TestCase
{

	public static function invoke($object, $method)
	{
		$params = array_slice(func_get_args(), 2);

		$reflexion = new ReflectionClass(self::getClass($object));
		$reflexion_method = $reflexion->getMethod($method);
		$reflexion_method->setAccessible(true);

		return $reflexion_method->invokeArgs($object, $params);
	}

	public static function getProperty($object, $property)
	{
		$reflexion = new ReflectionClass(self::getClass($object));
		$reflexion_property = $reflexion->getProperty($property);
		$reflexion_property->setAccessible(true);

		return $reflexion_property->getValue($object);
	}

	public static function setProperty($object, $property, $value)
	{
		$reflexion = new ReflectionClass(self::getClass($object));
		$reflexion_property = $reflexion->getProperty($property);
		$reflexion_property->setAccessible(true);

		$reflexion_property->setValue($object, $value);
	}

    public static function getClass($object)
	{
		$namespace = explode('\\', get_class($object));
		return preg_replace('/(.*)(?:Core)?Test$/', '$1', end($namespace));
	}
}