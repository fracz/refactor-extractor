<?php
/**
 * @package     Joomla.Administrator
 * @subpackage  com_installer
 *
 * @copyright   Copyright (C) 2005 - 2016 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE.txt
 */

defined('_JEXEC') or die;

require_once __DIR__ . '/extension.php';

use Joomla\Utilities\ArrayHelper;

/**
 * Installer Update Sites Model
 *
 * @package     Joomla.Administrator
 * @subpackage  com_installer
 * @since       3.4
 */
class InstallerModelUpdatesites extends InstallerModel
{
	/**
	 * Constructor.
	 *
	 * @param   array  $config  An optional associative array of configuration settings.
	 *
	 * @see     JController
	 * @since   3.4
	 */
	public function __construct($config = array())
	{
		if (empty($config['filter_fields']))
		{
			$config['filter_fields'] = array(
				'update_site_name',
				'name',
				'client_id',
				'status',
				'type',
				'folder',
				'update_site_id',
				'enabled',
			);
		}

		parent::__construct($config);
	}

	/**
	 * Method to auto-populate the model state.
	 *
	 * Note. Calling getState in this method will result in recursion.
	 *
	 * @param   string  $ordering   An optional ordering field.
	 * @param   string  $direction  An optional direction (asc|desc).
	 *
	 * @return  void
	 *
	 * @since   3.4
	 */
	protected function populateState($ordering = 'name', $direction = 'asc')
	{
		// Load the filter state.
		$this->setState('filter.search', $this->getUserStateFromRequest($this->context . '.filter.search', 'filter_search', '', 'string'));
		$this->setState('filter.client_id', $this->getUserStateFromRequest($this->context . '.filter.client_id', 'filter_client_id', null, 'int'));
		$this->setState('filter.enabled', $this->getUserStateFromRequest($this->context . '.filter.enabled', 'filter_enabled', '', 'string'));
		$this->setState('filter.type', $this->getUserStateFromRequest($this->context . '.filter.type', 'filter_type', '', 'string'));
		$this->setState('filter.folder', $this->getUserStateFromRequest($this->context . '.filter.folder', 'filter_folder', '', 'string'));

		parent::populateState($ordering, $direction);
	}

	/**
	 * Enable/Disable an extension.
	 *
	 * @param   array  &$eid   Extension ids to un/publish
	 * @param   int    $value  Publish value
	 *
	 * @return  boolean  True on success
	 *
	 * @since   3.4
	 *
	 * @throws  Exception on ACL error
	 */
	public function publish(&$eid = array(), $value = 1)
	{
		$user = JFactory::getUser();

		if (!$user->authorise('core.edit.state', 'com_installer'))
		{
			throw new Exception(JText::_('JLIB_APPLICATION_ERROR_EDITSTATE_NOT_PERMITTED'), 403);
		}

		$result = true;

		// Ensure eid is an array of extension ids
		if (!is_array($eid))
		{
			$eid = array($eid);
		}

		// Get a table object for the extension type
		$table = JTable::getInstance('Updatesite');

		// Enable the update site in the table and store it in the database
		foreach ($eid as $i => $id)
		{
			$table->load($id);
			$table->enabled = $value;

			if (!$table->store())
			{
				$this->setError($table->getError());
				$result = false;
			}
		}

		return $result;
	}

	/**
	 * Deletes an update site.
	 *
	 * @param   array  $ids  Extension ids to delete.
	 *
	 * @return  void
	 *
	 * @since   __DEPLOY_VERSION__
	 *
	 * @throws  Exception on ACL error
	 */
	public function delete($ids = array())
	{
		$user = JFactory::getUser();

		if (!$user->authorise('core.delete', 'com_installer'))
		{
			throw new Exception(JText::_('JLIB_APPLICATION_ERROR_DELETE_NOT_PERMITTED'), 403);
		}

		// Ensure eid is an array of extension ids
		if (!is_array($ids))
		{
			$ids = array($ids);
		}

		$db  = JFactory::getDbo();
		$app = JFactory::getApplication();

		$count = 0;

		// Gets the update site names.
		$query = $db->getQuery(true)
			->select($db->qn(array('update_site_id', 'name')))
			->from($db->qn('#__update_sites'))
			->where($db->qn('update_site_id') . ' IN (' . implode(', ', $ids) . ')');
		$db->setQuery($query);
		$updateSitesNames = $db->loadObjectList('update_site_id');

		// Gets Joomla core update sites Ids.
		$joomlaUpdateSitesIds = $this->getJoomlaUpdateSitesIds();

		// Enable the update site in the table and store it in the database
		foreach ($ids as $i => $id)
		{
			// Don't allow to delete Joomla Core update sites.
			if (in_array((int) $id, $joomlaUpdateSitesIds))
			{
				$app->enqueueMessage(JText::sprintf('COM_INSTALLER_MSG_UPDATESITES_DELETE_CANNOT_DELETE', $updateSitesNames[$id]->name), 'error');
				continue;
			}

			// Delete the update site from all tables.
			try
			{
				$query = $db->getQuery(true)
					->delete($db->qn('#__update_sites'))
					->where($db->qn('update_site_id') . ' = ' . (int) $id);
				$db->setQuery($query);
				$db->execute();

				$query = $db->getQuery(true)
					->delete($db->qn('#__update_sites_extensions'))
					->where($db->qn('update_site_id') . ' = ' . (int) $id);
				$db->setQuery($query);
				$db->execute();

				$query = $db->getQuery(true)
					->delete($db->qn('#__updates'))
					->where($db->qn('update_site_id') . ' = ' . (int) $id);
				$db->setQuery($query);
				$db->execute();

				$count++;
			}
			catch (RuntimeException $e)
			{
				$app->enqueueMessage(JText::sprintf('COM_INSTALLER_MSG_UPDATESITES_DELETE_ERROR', $updateSitesNames[$id]->name, $e->getMessage()), 'error');
			}
		}

		if ($count > 0)
		{
			$app->enqueueMessage(JText::plural('COM_INSTALLER_MSG_UPDATESITES_DELETE_UPDATESITES_DELETED', $count), 'message');
		}
	}

	/**
	 * Rebuild update sites tables.
	 *
	 * @return  void
	 *
	 * @since   __DEPLOY_VERSION__
	 *
	 * @throws  Exception on ACL error
	 */
	public function rebuild()
	{
		$user = JFactory::getUser();

		if (!$user->authorise('core.admin', 'com_installer'))
		{
			throw new Exception(JText::_('COM_INSTALLER_MSG_UPDATESITES_REBUILD_NOT_PERMITTED'), 403);
		}

		$db  = JFactory::getDbo();
		$app = JFactory::getApplication();

		$clients               = array(JPATH_SITE, JPATH_ADMINISTRATOR);
		$extensionGroupFolders = array('components', 'modules', 'plugins', 'templates', 'language', 'manifests');

		$pathsToSearch = array();

		// Identifies which folders to search for manifest files.
		foreach ($clients as $clientPath)
		{
			foreach ($extensionGroupFolders as $extensionGroupFolderName)
			{
				// Components, modules, plugins, templates, languages and manifest (files, libraries, etc)
				if ($extensionGroupFolderName != 'plugins')
				{
					foreach (glob($clientPath . '/' . $extensionGroupFolderName . '/*', GLOB_NOSORT | GLOB_ONLYDIR) as $extensionFolderPath)
					{
						array_push($pathsToSearch, $extensionFolderPath);
					}
				}

				// Plugins (another directory level is needed)
				else
				{
					foreach (glob($clientPath . '/' . $extensionGroupFolderName . '/*', GLOB_NOSORT | GLOB_ONLYDIR) as $pluginGroupFolderPath)
					{
						foreach (glob($pluginGroupFolderPath . '/*', GLOB_NOSORT | GLOB_ONLYDIR) as $extensionFolderPath)
						{
							array_push($pathsToSearch, $extensionFolderPath);
						}
					}
				}
			}
		}

		// Gets Joomla core update sites Ids.
		$joomlaUpdateSitesIds = implode(', ', $this->getJoomlaUpdateSitesIds());

		// Delete from all tables (except joomla core update sites).
		$query = $db->getQuery(true)
			->delete($db->qn('#__update_sites'))
			->where($db->qn('update_site_id') . ' NOT IN (' . $joomlaUpdateSitesIds . ')');
		$db->setQuery($query);
		$db->execute();

		$query = $db->getQuery(true)
			->delete($db->qn('#__update_sites_extensions'))
			->where($db->qn('update_site_id') . ' NOT IN (' . $joomlaUpdateSitesIds . ')');
		$db->setQuery($query);
		$db->execute();

		$query = $db->getQuery(true)
			->delete($db->qn('#__updates'))
			->where($db->qn('update_site_id') . ' NOT IN (' . $joomlaUpdateSitesIds . ')');
		$db->setQuery($query);
		$db->execute();

		$count = 0;

		// Search for updateservers in manifest files inside the folders to search.
		foreach ($pathsToSearch as $extensionFolderPath)
		{
			$tmpInstaller = new JInstaller;

			$tmpInstaller->setPath('source', $extensionFolderPath);

			// Main folder manifests (higher priority)
			$parentXmlfiles = JFolder::files($tmpInstaller->getPath('source'), '.xml$', false, true);

			// Search for children manifests (lower priority)
			$allXmlFiles    = JFolder::files($tmpInstaller->getPath('source'), '.xml$', 1, true);

			// Create an unique array of files ordered by priority
			$xmlfiles = array_unique(array_merge($parentXmlfiles, $allXmlFiles));

			if (!empty($xmlfiles))
			{
				foreach ($xmlfiles as $file)
				{
					// Is it a valid Joomla installation manifest file?
					$manifest = $tmpInstaller->isManifest($file);

					if (!is_null($manifest))
					{
						$query = $db->getQuery(true)
							->select($db->qn('extension_id'))
							->from($db->qn('#__extensions'))
							->where($db->qn('name') . ' = ' . $db->q($manifest->name))
							->where($db->qn('type') . ' = ' . $db->q($manifest['type']));
						$db->setQuery($query);
						$eid = (int) $db->loadResult();

						if ($eid && $manifest->updateservers)
						{
							// Load the extension plugin (if not loaded yet).
							$plugin = JPluginHelper::importPlugin('extension', 'joomla');

							// Fire the onExtensionAfterUpdate
							JEventDispatcher::getInstance()->trigger('onExtensionAfterUpdate', array('installer' => $tmpInstaller, 'eid' => $eid));

							$count++;
						}
					}
				}
			}
		}

		if ($count > 0)
		{
			$app->enqueueMessage(JText::_('COM_INSTALLER_MSG_UPDATESITES_REBUILD_SUCCESS'), 'message');
		}
		else
		{
			$app->enqueueMessage(JText::_('COM_INSTALLER_MSG_UPDATESITES_REBUILD_WARNING'), 'warning');
		}
	}

	/**
	 * Fetch the Joomla update sites ids.
	 *
	 * @return  array  Array with joomla core update site ids.
	 *
	 * @since   __DEPLOY_VERSION__
	 */
	protected function getJoomlaUpdateSitesIds()
	{
		$db  = JFactory::getDbo();

		// Fetch the Joomla core Joomla update sites ids.
		$query = $db->getQuery(true);
		$query->select($db->qn('update_site_id'))
			->from($db->qn('#__update_sites'))
			->where($db->qn('location') . ' LIKE \'%update.joomla.org%\'');
		$db->setQuery($query);

		return $db->loadColumn();
	}

	/**
	 * Method to get the database query
	 *
	 * @return  JDatabaseQuery  The database query
	 *
	 * @since   3.4
	 */
	protected function getListQuery()
	{
		$query = JFactory::getDbo()->getQuery(true)
			->select(
				array(
					's.update_site_id',
					's.name AS update_site_name',
					's.type AS update_site_type',
					's.location',
					's.enabled',
					'e.extension_id',
					'e.name',
					'e.type',
					'e.element',
					'e.folder',
					'e.client_id',
					'e.state',
					'e.manifest_cache',
				)
			)
			->from('#__update_sites AS s')
			->innerJoin('#__update_sites_extensions AS se ON (se.update_site_id = s.update_site_id)')
			->innerJoin('#__extensions AS e ON (e.extension_id = se.extension_id)')
			->where('state = 0');

		// Process select filters.
		$enabled  = $this->getState('filter.enabled');
		$type     = $this->getState('filter.type');
		$clientId = $this->getState('filter.client_id');
		$folder   = $this->getState('filter.folder');

		if ($enabled != '')
		{
			$query->where('s.enabled = ' . (int) $enabled);
		}

		if ($type)
		{
			$query->where('e.type = ' . $this->_db->quote($type));
		}

		if ($clientId != '')
		{
			$query->where('client_id = ' . (int) $clientId);
		}

		if ($folder != '' && in_array($type, array('plugin', 'library', '')))
		{
			$query->where('folder = ' . $this->_db->quote($folder == '*' ? '' : $folder));
		}

		// Process search filter (update site id).
		$search = $this->getState('filter.search');

		if (!empty($search) && stripos($search, 'id:') === 0)
		{
			$query->where('s.update_site_id = ' . (int) substr($search, 3));
		}

		return $query;
	}

	/**
	 * Returns an object list
	 *
	 * @param   string  $query       The query
	 * @param   int     $limitstart  Offset
	 * @param   int     $limit       The number of records
	 *
	 * @return  array
	 *
	 * @since   3.4
	 */
	protected function _getList($query, $limitstart = 0, $limit = 0)
	{
		$ordering  = $this->getState('list.ordering', 'name');
		$direction = $this->getState('list.direction', 'asc');
		$search    = $this->getState('filter.search');

		// Replace slashes so preg_match will work
		$search = str_replace('/', ' ', $search);
		$db     = $this->getDbo();

		if ($ordering == 'name' || (!empty($search) && stripos($search, 'id:') !== 0))
		{
			$db->setQuery($query);
			$result = $db->loadObjectList();
			$this->translate($result);

			if (!empty($search) && (stripos($search, 'id:') !== 0))
			{
				foreach ($result as $i => $item)
				{
					if (!preg_match("/$search/i", $item->name) && !preg_match("/$search/i", $item->update_site_name))
					{
						unset($result[$i]);
					}
				}
			}

			$result = ArrayHelper::sortObjects($result, $ordering, strtolower($direction) === 'desc' ? -1 : 1, true, true);

			$total = count($result);
			$this->cache[$this->getStoreId('getTotal')] = $total;

			if ($total < $limitstart)
			{
				$limitstart = 0;
				$this->setState('list.start', 0);
			}

			return array_slice($result, $limitstart, $limit ? $limit : null);
		}

		$query->order($db->quoteName($ordering) . ' ' . $direction);
		$result = parent::_getList($query, $limitstart, $limit);
		$this->translate($result);

		return $result;
	}
}