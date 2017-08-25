<?php
/**
 * @author Clark Tomlinson <fallen013@gmail.com>
 * @author Thomas Müller <thomas.mueller@tmit.eu>
 *
 * @copyright Copyright (c) 2015, ownCloud, Inc.
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License, version 3,
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

namespace OCP\Encryption\Exceptions;

/**
 * Class GenericEncryptionException
 *
 * @package OCP\Encryption\Exceptions
 * @since 8.1.0
 */
class GenericEncryptionException extends \Exception {

	/** @var string */
	protected $hint;

	/**
	 * @param string $message
	 * @param int $code
	 * @param \Exception $previous
	 * @since 8.1.0
	 */
	public function __construct($message = '', $code = 0, \Exception $previous = null, $hint = '') {
		if (empty($message)) {
			$message = 'Unspecified encryption exception';
		}
		parent::__construct($message, $code, $previous);

		$this->hint = $hint;
	}

	public function getHint() {
		return $this->hint;
	}

}