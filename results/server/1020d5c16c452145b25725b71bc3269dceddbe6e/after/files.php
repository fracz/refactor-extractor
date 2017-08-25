<?php

/**
 * ownCloud - Core
 *
 * @author Morris Jobke
 * @copyright 2013 Morris Jobke morris.jobke@gmail.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 */


namespace OCA\Files;

class Files {
	private $l10n;

	public function __construct() {
		$this->l10n = \OC_L10n::get('files');
	}

	/**
	 * rename a file
	 *
	 * @param string $dir
	 * @param string $oldname
	 * @param string $newname
	 * @return array
	 */
	public function rename($dir, $oldname, $newname) {
		$result = array(
			'success' 	=> false,
			'data'		=> NULL
		);

		// rename to "Shared" in root directory denied
		if( $dir === '/' and $newname === 'Shared' ) {
			$result['data'] = array(
				'message'	=> $this->t("Invalid folder name. Usage of 'Shared' is reserved by Owncloud")
			);
		} elseif(
			// rename to "." is denied
			$newname !== '.' and
			// rename to  "Shared" inside the root directory is denied
			!($dir === '/' and $file === 'Shared') and
			// THEN try to rename
			\OC\Files\Filesystem::rename(
				\OC\Files\Filesystem::normalizePath($dir . '/' . $oldname),
				\OC\Files\Filesystem::normalizePath($dir . '/' . $newname)
			)
		) {
			// successful rename
			$result['success'] = true;
			$result['data'] = array(
				'dir'		=> $dir,
				'file'		=> $oldname,
				'newname'	=> $newname
			);
		} else {
			// rename failed
			$result['data'] = array(
				'message'	=> $this->t('Unable to rename file')
			);
		}
		return $result;
	}

}