<?php
/**
 * @copyright Copyright (c) 2016 Arthur Schiwon <blizzz@arthur-schiwon.de>
 *
 * @author Arthur Schiwon <blizzz@arthur-schiwon.de>
 *
 * @license GNU AGPL version 3 or any later version
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

namespace OC\Settings;

use OCP\AppFramework\QueryException;
use OCP\Encryption\IManager as EncryptionManager;
use OCP\IConfig;
use OCP\IDBConnection;
use OCP\IL10N;
use OCP\ILogger;
use OCP\IUserManager;
use OCP\Settings\IAdmin;
use OCP\Settings\IManager;
use OCP\Settings\ISection;

class Manager implements IManager {
	const TABLE_ADMIN_SETTINGS = 'admin_settings';
	const TABLE_ADMIN_SECTIONS = 'admin_sections';

	/** @var ILogger */
	/** @var ILogger */
	private $log;

	/** @var IDBConnection */
	private $dbc;

	/** @var IL10N */
	private $l;

	/** @var IConfig */
	private $config;

	/** @var EncryptionManager */
	private $encryptionManager;

	/** @var IUserManager */
	private $userManager;

	public function __construct(
		ILogger $log,
		IDBConnection $dbc,
		IL10N $l,
		IConfig $config,
		EncryptionManager $encryptionManager,
		IUserManager $userManager
	) {
		$this->log = $log;
		$this->dbc = $dbc;
		$this->l = $l;
		$this->config = $config;
		$this->encryptionManager = $encryptionManager;
		$this->userManager = $userManager;
	}

	/**
	 * @inheritdoc
	 */
	public function setupSettings(array $settings) {
		if(isset($settings[IManager::KEY_ADMIN_SECTION])) {
			$this->setupAdminSection($settings[IManager::KEY_ADMIN_SECTION]);
		}
		if(isset($settings[IManager::KEY_ADMIN_SETTINGS])) {
			$this->setupAdminSettings($settings[IManager::KEY_ADMIN_SETTINGS]);
		}
	}

	private function setupAdminSection($sectionClassName) {
		if(!class_exists($sectionClassName)) {
			$this->log->debug('Could not find admin section class ' . $sectionClassName);
			return;
		}
		try {
			$section = $this->query($sectionClassName);
		} catch (QueryException $e) {
			// cancel
			return;
		}

		if(!$section instanceof ISection) {
			$this->log->error(
				'Admin section instance must implement \OCP\ISection. Invalid class: {class}',
				['class' => $sectionClassName]
			);
			return;
		}
		if(!$this->hasAdminSection(get_class($section))) {
			$this->addAdminSection($section);
		} else {
			$this->updateAdminSection($section);
		}
	}

	private function addAdminSection(ISection $section) {
		$this->add(self::TABLE_ADMIN_SECTIONS, [
			'id' => $section->getID(),
			'class' => get_class($section),
			'priority' => $section->getPriority(),
		]);
	}

	private function addAdminSettings(IAdmin $settings) {
		$this->add(self::TABLE_ADMIN_SETTINGS, [
			'class' => get_class($settings),
			'section' => $settings->getSection(),
			'priority' => $settings->getPriority(),
		]);
	}

	private function add($table, $values) {
		$query = $this->dbc->getQueryBuilder();
		$values = array_map(function($value) use ($query) {
			return $query->createNamedParameter($value);
		}, $values);
		$query->insert($table)->values($values);
		$query->execute();
	}

	private function updateAdminSettings(IAdmin $settings) {
		$this->update(
			self::TABLE_ADMIN_SETTINGS,
			'class',
			get_class($settings),
			[
				'section' => $settings->getSection(),
				'priority' => $settings->getPriority(),
			]
		);
	}

	private function updateAdminSection(ISection $section) {
		$this->update(
			self::TABLE_ADMIN_SECTIONS,
			'class',
			get_class($section),
			[
				'id'       => $section->getID(),
				'priority' => $section->getPriority(),
			]
		);
	}

	private function update($table, $idCol, $id, $values) {
		$query = $this->dbc->getQueryBuilder();
		$query->update($table);
		foreach($values as $key => $value) {
			$query->set($key, $query->createNamedParameter($value));
		}
		$query
			->where($query->expr()->eq($idCol, $query->createParameter($idCol)))
			->setParameter($idCol, $id)
			->execute();
	}

	/**
	 * @param string $className
	 * @return bool
	 */
	private function hasAdminSection($className) {
		return $this->has(self::TABLE_ADMIN_SECTIONS, $className);
	}

	/**
	 * @param string $className
	 * @return bool
	 */
	private function hasAdminSettings($className) {
		return $this->has(self::TABLE_ADMIN_SETTINGS, $className);
	}


	private function has($table, $className) {
		$query = $this->dbc->getQueryBuilder();
		$query->select('class')
			->from($table)
			->where($query->expr()->eq('class', $query->createNamedParameter($className)))
			->setMaxResults(1);

		$result = $query->execute();
		$row = $result->fetch();
		$result->closeCursor();

		return (bool) $row;
	}

	private function setupAdminSettings($settingsClassName) {
		if(!class_exists($settingsClassName)) {
			$this->log->debug('Could not find admin section class ' . $settingsClassName);
			return;
		}

		try {
			/** @var IAdmin $settings */
			$settings = $this->query($settingsClassName);
		} catch (QueryException $e) {
			// cancel
			return;
		}

		if(!$settings instanceof IAdmin) {
			$this->log->error(
				'Admin section instance must implement \OCP\ISection. Invalid class: {class}',
				['class' => $settingsClassName]
			);
			return;
		}
		if(!$this->hasAdminSettings(get_class($settings))) {
			$this->addAdminSettings($settings);
		} else {
			$this->updateAdminSettings($settings);
		}
	}

	private function query($className) {
		try {
			return \OC::$server->query($className);
		} catch (QueryException $e) {
			$this->log->logException($e);
			throw $e;
		}
	}

	/**
	 * returns a list of the admin sections
	 *
	 * @return ISection[]
	 */
	public function getAdminSections() {
		$query = $this->dbc->getQueryBuilder();
		$query->select(['class', 'priority'])
			->from(self::TABLE_ADMIN_SECTIONS);

		// built-in sections
		$sections = [
			 0 => [new Section('server',        $this->l->t('Server Settings'), 0)],
			 5 => [new Section('sharing',       $this->l->t('Sharing'), 0)],
			//15 => [new Section('collaboration', $this->l->t('Collaboration'), 0)],
			//30 => [new Section('theming',       $this->l->t('Theming'), 0)],
			45 => [new Section('encryption',    $this->l->t('Encryption'), 0)],
			90 => [new Section('logging',       $this->l->t('Logging'), 0)],
			98 => [new Section('additional',    $this->l->t('Additional Settings'), 0)],
			99 => [new Section('tips-tricks',   $this->l->t('Tips & Tricks'), 0)],
		];

		$result = $query->execute();
		while($row = $result->fetch()) {
			if(!isset($sections[$row['priority']])) {
				$sections[$row['priority']] = [];
			}
			try {
				$sections[$row['priority']][] = $this->query($row['class']);
			} catch (QueryException $e) {
				// skip
			}
		}
		$result->closeCursor();

		ksort($sections);
		return $sections;
	}

	private function getBuiltInAdminSettings($section) {
		$forms = [];
		try {
			if($section === 'server') {
				/** @var IAdmin $form */
				$form = new Admin\Server($this->dbc, $this->config);
				$forms[$form->getPriority()] = [$form];
			}
			if($section === 'encryption') {
				/** @var IAdmin $form */
				$form = new Admin\Encryption($this->encryptionManager, $this->userManager);
				$forms[$form->getPriority()] = [$form];
			}
			if($section === 'sharing') {
				/** @var IAdmin $form */
				$form = new Admin\Sharing($this->config);
				$forms[$form->getPriority()] = [$form];
			}
			if($section === 'logging') {
				/** @var IAdmin $form */
				$form = new Admin\Logging($this->config);
				$forms[$form->getPriority()] = [$form];
			}
			if($section === 'tips-tricks') {
				/** @var IAdmin $form */
				$form = new Admin\TipsTricks($this->config);
				$forms[$form->getPriority()] = [$form];
			}
		} catch (QueryException $e) {
			// skip
		}
		return $forms;
	}

	private function getAdminSettingsFromDB($section, &$settings) {
		$query = $this->dbc->getQueryBuilder();
		$query->select(['class', 'priority'])
			->from(self::TABLE_ADMIN_SETTINGS)
			->where($query->expr()->eq('section', $this->dbc->getQueryBuilder()->createParameter('section')))
			->setParameter('section', $section);

		$result = $query->execute();
		while($row = $result->fetch()) {
			if(!isset($settings[$row['priority']])) {
				$settings[$row['priority']] = [];
			}
			try {
				$settings[$row['priority']][] = $this->query($row['class']);
			} catch (QueryException $e) {
				// skip
			}
		}
		$result->closeCursor();

		ksort($settings);
	}

	public function getAdminSettings($section) {
		$settings = $this->getBuiltInAdminSettings($section);
		$this->getAdminSettingsFromDB($section, $settings);
		return $settings;
	}


}