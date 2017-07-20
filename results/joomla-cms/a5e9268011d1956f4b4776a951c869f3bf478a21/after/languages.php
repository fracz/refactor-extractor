<?php
/**
 * @version		$Id$
 * @copyright	Copyright (C) 2005 - 2009 Open Source Matters, Inc. All rights reserved.
 * @license		GNU General Public License <http://www.gnu.org/copyleft/gpl.html>
 */

// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die;

jimport('joomla.application.component.modellist');

/**
 * Languages Component Languages Model
 *
 * @package		Joomla.Administrator
 * @subpackage	com_languages
 * @since		1.6
 */
class LanguagesModelLanguages extends JModelList
{
	/**
	 * @var object client object
	 */
	protected $client = null;

	/**
	 * @var object user object
	 */
	protected $user = null;

	/**
	 * @var boolean|JExeption True, if FTP settings should be shown, or an exeption
	 */
	protected $ftp = null;

	/**
	 * @var string option name
	 */
	protected $option = null;

	/**
	 * @var array languages description
	 */
	protected $data = null;

	/**
	 * @var int total number pf languages
	 */
	protected $total = null;

	/**
	 * @var array languages folders
	 */
	protected $folders = null;

	/**
	 * @var string language path
	 */
	protected $path = null;

	/**
	 * Method to get the client object
	 *
	 * @access public
	 * @return object
	 */
	function &getClient()
	{
		if (is_null($this->client)) {
			$this->client = &JApplicationHelper::getClientInfo($this->getState('client'));
		}
		return $this->client;
	}

	/**
	 * Method to get the user object
	 *
	 * @access public
	 * @return object
	 */
	function &getUser()
	{
		if (is_null($this->user)) {
			$this->user = &JFactory::getUser();
		}
		return $this->user;
	}

	/**
	 * Method to get the ftp credentials
	 *
	 * @access public
	 * @return object
	 */
	function &getFtp()
	{
		if (is_null($this->ftp))
		{
			jimport('joomla.client.helper');
			$this->ftp = &JClientHelper::setCredentialsFromRequest('ftp');
		}
		return $this->ftp;
	}

	/**
	 * Method to get the option
	 *
	 * @access public
	 * @return object
	 */
	function &getOption()
	{
		$option = $this->getState('option');
		return $option;
	}

	/**
	 * Method to get Languages item data
	 *
	 * @access public
	 * @return array
	 */
	function &getData()
	{
		if (is_null($this->data))
		{
			// Get information
			$folders = & $this->_getFolders();
			$path = & $this->_getPath();
			$client = & $this->getClient();

			// Compute all the languages
			$data	= array ();
			foreach ($folders as $folder)
			{
				$file = $path.DS.$folder.DS.$folder.'.xml';
				$info = & JApplicationHelper::parseXMLLangMetaFile($file);
				$row = new JObject();
				$row->language 	= $folder;

				if (!is_array($info)) {
					continue;
				}
				foreach($info as $key => $value) {
					$row->$key = $value;
				}
				// if current than set published
				$params = &JComponentHelper::getParams('com_languages');
				if ($params->get($client->name, 'en-GB') == $row->language) {
					$row->published	= 1;
				}
				else {
					$row->published = 0;
				}

				$row->checked_out = 0;
				$data[] = $row;
			}
			usort($data,array("LanguagesModelLanguages","_compareLanguages"));


			// Prepare data
			$limit = $this->getState('list.limit');
			$start = $this->getState('list.start');
			$total = $this->getTotal();

			if ($limit == 0)
			{
				$start = 0;
				$end = $total;
			}
			else
			{
				if ($start > $total) {
					$start = $total - $total % $limit;
				}
				$end = $start + $limit;
				if ($end > $total) {
					$end = $total;
				}
			}

			// Compute the displayed languages
			$this->data	= array ();
			for ($i = $start;$i < $end;$i++) {
				$this->data[] = & $data[$i];
			}
		}
		return $this->data;
	}

	/**
	 * Method to get the total number of Languages items
	 *
	 * @access public
	 * @return integer
	 */
	function &getTotal()
	{
		if (is_null($this->total))
		{
			$folders = & $this->_getFolders();
			$this->total = count($folders);
		}
		return $this->total;
	}

	/**
	 * Method to set the default language
	 *
	 * @access public
	 * return boolean
	 */
	function publish()
	{
		$cid = $this->getState('cid');
		if (count($cid)>0) {
			$client	= & $this->getClient();

			$params = & JComponentHelper::getParams('com_languages');
			$params->set($client->name, $cid[0]);

			$table = & JTable::getInstance('component');
			$table->loadByOption('com_languages');

			$table->params = $params->toString();
			// pre-save checks
			if (!$table->check()) {
				$this->setError($table->getError());
				return false;
			}

			// save the changes
			if (!$table->store()) {
				$this->setError($table->getError());
				return false;
			}
		}
		else {
			$this->setError(JText::_('Languages_No_Language_Selected'));
			return false;
		}
		return true;
	}

	/**
	 * Method to get the folders
	 *
	 * @access protected
	 * @return array languages folders
	 */
	protected function _getFolders()
	{
		if (is_null($this->folders))
		{
			$path = & $this->_getPath();
			jimport('joomla.filesystem.folder');
			$this->folders = &JFolder::folders($path, '.', false, false, array('.svn', 'CVS', '.DS_Store', '__MACOSX', 'pdf_fonts','overrides'));
		}
		return $this->folders;
	}

	/**
	 * Method to get the path
	 *
	 * @access protected
	 * @return string the path to the languages folders
	 */
	protected function _getPath()
	{
		if (is_null($this->path))
		{
			$client = & $this->getClient();
			$this->path = &JLanguage::getLanguagePath($client->path);
		}
		return $this->path;
	}

	/**
	 * Method to compare two languages in order to sort them
	 *
	 * @access protected
	 * @param object $lang1 the first language
	 * @param object $lang2 the second language
	 * @return integer
	 */
	protected function _compareLanguages($lang1,$lang2)
	{
		return strcmp($lang1->name,$lang2->name);
	}

	/**
	 * Method to auto-populate the model state.
	 *
	 * This method should only be called once per instantiation and is designed
	 * to be called on the first call to the getState() method unless the model
	 * configuration flag to ignore the request is set.
	 *
	 * @return	void
	 */
	protected function _populateState()
	{
		/**
		 * Compute the client state
		 */
		$client = & JRequest::getVar('client', 0, '', 'int');
		$this->setState('client',$client);
		/**
		 * Compute the option state
		 */
		$option = & JRequest::getCmd('option', 'com_languages');
		$this->setState('option',$option);
		/**
		 * Compute the pagination state
		 */
		$app = & JFactory::getApplication();
		$limit = $app->getUserStateFromRequest('global.list.limit', 'limit', $app->getCfg('list_limit'), 'int');
		$start = $app->getUserStateFromRequest($option.'.'.$client.'.limitstart', 'limitstart', 0, 'int');
		$this->setState('list.limit', $limit);
		$this->setState('list.start', $start);
		/**
		 * Compute the selected state
		 */
		$cid = JRequest::getVar('cid', array(), 'post', 'array');
		$this->setState('cid',$cid);
	}
}
