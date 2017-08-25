<?php
App::uses('AppController', 'Controller');
App::uses('Xml', 'Utility');

/**
 * Servers Controller
 *
 * @property Server $Server
 *
 * @throws ConfigureException // TODO Exception
 */
class ServersController extends AppController {

	public $components = array('Security' ,'RequestHandler');	// XXX ACL component

	public $paginate = array(
			'limit' => 60,
			'maxLimit' => 9999, // LATER we will bump here on a problem once we have more than 9999 events
			'order' => array(
					'Server.url' => 'ASC'
			)
	);

	public $uses = array('Server', 'Event');

	public function beforeFilter() {
		parent::beforeFilter();

		// permit reuse of CSRF tokens on some pages.
		switch ($this->request->params['action']) {
			case 'push':
			case 'pull':
				$this->Security->csrfUseOnce = false;
		}
	}

/**
 * index method
 *
 * @return void
 */
	public function index() {
		$this->Server->recursive = 0;
		if ($this->_isSiteAdmin()) {
			$this->paginate = array(
							'conditions' => array(),
			);
		} else {
			if (!$this->userRole['perm_sync'] && !$this->userRole['perm_admin']) $this->redirect(array('controller' => 'events', 'action' => 'index'));
			$conditions['Server.org LIKE'] = $this->Auth->user('org');
			$this->paginate = array(
					'conditions' => array($conditions),
			);
		}
		$this->set('servers', $this->paginate());
	}

/**
 * add method
 *
 * @return void
 */
	public function add() {
		if (!$this->_isAdmin()) $this->redirect(array('controller' => 'servers', 'action' => 'index'));
		if ($this->request->is('post')) {
			// force check userid and orgname to be from yourself
			$this->request->data['Server']['org'] = $this->Auth->user('org');
			if ($this->Server->save($this->request->data)) {
				if (isset($this->request->data['Server']['submitted_cert'])) {
					$this->__saveCert($this->request->data, $this->Server->id);
				}
				$this->Session->setFlash(__('The server has been saved'));
				$this->redirect(array('action' => 'index'));
			} else {
				$this->Session->setFlash(__('The server could not be saved. Please, try again.'));
			}
		}
	}

/**
 * edit method
 *
 * @param string $id
 * @return void
 * @throws NotFoundException
 */
	public function edit($id = null) {
		$this->Server->id = $id;
		if (!$this->Server->exists()) {
			throw new NotFoundException(__('Invalid server'));
		}
		$s = $this->Server->read(null, $id);
		if (!$this->_isSiteAdmin() && !($s['Server']['org'] == $this->Auth->user('org') && $this->_isAdmin())) $this->redirect(array('controller' => 'servers', 'action' => 'index'));
		if ($this->request->is('post') || $this->request->is('put')) {
			// say what fields are to be updated
			$fieldList = array('id', 'url', 'push', 'pull', 'organization', 'self_signed', 'cert_file');
			$this->request->data['Server']['id'] = $id;
			if ("" != $this->request->data['Server']['authkey'])
				$fieldList[] = 'authkey';
			// Save the data
			if ($this->Server->save($this->request->data, true, $fieldList)) {
				if (isset($this->request->data['Server']['submitted_cert']) && $this->request->data['Server']['submitted_cert']['size'] != 0) {
					$this->__saveCert($this->request->data, $this->Server->id);
				}
				$this->Session->setFlash(__('The server has been saved'));
				$this->redirect(array('action' => 'index'));
			} else {
				$this->Session->setFlash(__('The server could not be saved. Please, try again.'));
			}
		} else {
			$this->Server->read(null, $id);
			$this->Server->set('authkey', '');
			$this->request->data = $this->Server->data;
		}
	}

/**
 * delete method
 *
 * @param string $id
 * @return void
 * @throws MethodNotAllowedException
 * @throws NotFoundException
 */
	public function delete($id = null) {
		if (!$this->request->is('post')) {
			throw new MethodNotAllowedException();
		}
		$this->Server->id = $id;
		if (!$this->Server->exists()) {
			throw new NotFoundException(__('Invalid server'));
		}
		$s = $this->Server->read(null, $id);
		if (!$this->_isSiteAdmin() && !($s['Server']['org'] == $this->Auth->user('org') && $this->_isAdmin())) $this->redirect(array('controller' => 'servers', 'action' => 'index'));
		if ($this->Server->delete()) {
			$this->Session->setFlash(__('Server deleted'));
			$this->redirect(array('action' => 'index'));
		}
		$this->Session->setFlash(__('Server was not deleted'));
		$this->redirect(array('action' => 'index'));
	}

	/**
	 * Pull one or more events with attributes from a remote instance.
	 * Set $technique to
	 * 		full - download everything
	 * 		incremental - only new events
	 * 		<int>	- specific id of the event to pull
	 * For example to download event 10 from server 2 to /servers/pull/2/5
	 * @param int $id The id of the server
	 * @param unknown_type $technique
	 * @throws MethodNotAllowedException
	 * @throws NotFoundException
	 */
	public function pull($id = null, $technique=false) {
		$this->Server->id = $id;
		if (!$this->Server->exists()) {
			throw new NotFoundException(__('Invalid server'));
		}
		$s = $this->Server->read(null, $id);
		if (!$this->_isSiteAdmin() && !($s['Server']['org'] == $this->Auth->user('org') && $this->_isAdmin())) $this->redirect(array('controller' => 'servers', 'action' => 'index'));
		$this->Server->id = $id;
		if (!$this->Server->exists()) {
			throw new NotFoundException(__('Invalid server'));
		}

		if (false == $this->Server->data['Server']['pull']) {
			$this->Session->setFlash(__('Pull setting not enabled for this server.'));
			$this->redirect(array('action' => 'index'));
		}
		if (!Configure::read('MISP.background_jobs')) {
			$result = $this->Server->pull($this->Auth->user(), $id, $technique, $s);
			// error codes
			if (isset($result[0]) && is_numeric($result[0])) {
				switch ($result[0]) {
					case '1' :
						$this->Session->setFlash(__('Not authorised. This is either due to an invalid auth key, or due to the sync user not having authentication permissions enabled on the remote server. Another reason could be an incorrect sync server setting.'));
						$this->redirect(array('action' => 'index'));
						break;
					case '2' :
						$this->Session->setFlash($result[1]);
						$this->redirect(array('action' => 'index'));
						break;
					case '3' :
						throw new NotFoundException('Sorry, this is not yet implemented');
						break;
					case '4' :
						$this->redirect(array('action' => 'index'));
						break;

				}
			} else {
				$this->set('successes', $result[0]);
				$this->set('fails', $result[1]);
				$this->set('pulledProposals', $result[2]);
				$this->set('lastpulledid', $result[3]);
			}
		} else {
			$this->loadModel('Job');
			$this->Job->create();
			$data = array(
					'worker' => 'default',
					'job_type' => 'pull',
					'job_input' => 'Server: ' . $id,
					'status' => 0,
					'retries' => 0,
					'org' => $this->Auth->user('org'),
					'message' => 'Pulling.',
			);
			$this->Job->save($data);
			$jobId = $this->Job->id;
			$process_id = CakeResque::enqueue(
					'default',
					'ServerShell',
					array('pull', $this->Auth->user('id'), $id, $technique, $jobId)
			);
			$this->Job->saveField('process_id', $process_id);
			$this->Session->setFlash('Pull queued for background execution.');
			$this->redirect(array('action' => 'index'));
		}
	}

	public function push($id = null, $technique=false) {
		$this->Server->id = $id;
		if (!$this->Server->exists()) {
			throw new NotFoundException(__('Invalid server'));
		}
		$s = $this->Server->read(null, $id);
		if (!$this->_isSiteAdmin() && !($s['Server']['org'] == $this->Auth->user('org') && $this->_isAdmin())) $this->redirect(array('controller' => 'servers', 'action' => 'index'));
		if (!Configure::read('MISP.background_jobs')) {
			$server = $this->Server->read(null, $id);
			App::uses('SyncTool', 'Tools');
			$syncTool = new SyncTool();
			$HttpSocket = $syncTool->setupHttpSocket($server);
			$result = $this->Server->push($id, $technique, false, $HttpSocket, $this->Auth->user('email'));
			$this->set('successes', $result[0]);
			$this->set('fails', $result[1]);
		} else {
			$this->loadModel('Job');
			$this->Job->create();
			$data = array(
					'worker' => 'default',
					'job_type' => 'push',
					'job_input' => 'Server: ' . $id,
					'status' => 0,
					'retries' => 0,
					'org' => $this->Auth->user('org'),
					'message' => 'Pushing.',
			);
			$this->Job->save($data);
			$jobId = $this->Job->id;
			$process_id = CakeResque::enqueue(
					'default',
					'ServerShell',
					array('push', $id, $technique, $jobId, $this->Auth->user('id'))
			);
			$this->Job->saveField('process_id', $process_id);
			$this->Session->setFlash('Push queued for background execution.');
			$this->redirect(array('action' => 'index'));
		}
	}

	public function __saveCert($server, $id) {
		$ext = '';
		App::uses('File', 'Utility');
		App::uses('Folder', 'Utility');
		$file = new File($server['Server']['submitted_cert']['name']);
		$ext = $file->ext();
		if (($ext != 'pem') || !$server['Server']['submitted_cert']['size'] > 0) {
			$this->Session->setFlash('Incorrect extension of empty file.');
			$this->redirect(array('action' => 'index'));
		}
		$pemData = fread(fopen($server['Server']['submitted_cert']['tmp_name'], "r"),
				$server['Server']['submitted_cert']['size']);
		$destpath = APP . "files" . DS . "certs" . DS;
		$dir = new Folder(APP . "files" . DS . "certs", true);
		if (!preg_match('@^[\w-,\s,\.]+\.[A-Za-z0-9_]{2,4}$@', $server['Server']['submitted_cert']['name'])) throw new Exception ('Filename not allowed');
		$pemfile = new File ($destpath . $id . '.' . $ext);
		$result = $pemfile->write($pemData);
		$s = $this->Server->read(null, $id);
		$s['Server']['cert_file'] = $s['Server']['id'] . '.' . $ext;
		if ($result) $this->Server->save($s);
	}

	public function serverSettings($tab=false) {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		if ($this->request->is('Get')) {
			$tabs = array(
					'MISP' => array('count' => 0, 'errors' => 0, 'severity' => 5),
					'GnuPG' => array('count' => 0, 'errors' => 0, 'severity' => 5),
					'Proxy' => array('count' => 0, 'errors' => 0, 'severity' => 5),
					'Security' => array('count' => 0, 'errors' => 0, 'severity' => 5),
					'misc' => array('count' => 0, 'errors' => 0, 'severity' => 5)
			);
			$writeableErrors = array(0 => 'OK', 1 => 'Directory doesn\'t exist', 2 => 'Directory is not writeable');
			$gpgErrors = array(0 => 'OK', 1 => 'FAIL: settings not set', 2 => 'FAIL: bad GnuPG.*', 3 => 'FAIL: encrypt failed');
			$proxyErrors = array(0 => 'OK', 1 => 'not configured (so not tested)', 2 => 'Getting URL via proxy failed');
			$stixOperational = array(0 => 'STIX or CyBox library not installed correctly', 1 => 'OK');
			$stixVersion = array(0 => 'Incorrect STIX version installed, found $current, expecting $expected', 1 => 'OK');
			$cyboxVersion = array(0 => 'Incorrect CyBox version installed, found $current, expecting $expected', 1 => 'OK');

			$finalSettings = $this->Server->serverSettingsRead();
			$issues = array(
				'errors' => array(
						0 => array(
								'value' => 0,
								'description' => 'MISP will not operate correctly or will be unsecure until these issues are resolved.'
						),
						1 => array(
								'value' => 0,
								'description' => 'Some of the features of MISP cannot be utilised until these issues are resolved.'
						),
						2 => array(
								'value' => 0,
								'description' => 'There are some optional tweaks that could be done to improve the looks of your MISP instance.'
						),
				),
				'deprecated' => array(),
				'overallHealth' => 3,
			);
			$dumpResults = array();
			foreach ($finalSettings as $k => $result) {
				if ($result['level'] == 3) $issues['deprecated']++;
				$tabs[$result['tab']]['count']++;
				if (isset($result['error']) && $result['level'] < 3) {
					$issues['errors'][$result['level']]['value']++;
					if ($result['level'] < $issues['overallHealth']) $issues['overallHealth'] = $result['level'];
					$tabs[$result['tab']]['errors']++;
					if ($result['level'] < $tabs[$result['tab']]['severity']) $tabs[$result['tab']]['severity'] = $result['level'];
				}
				$dumpResults[] = $result;
				if ($result['tab'] != $tab) unset($finalSettings[$k]);
			}
			// Diagnostics portion
			$diagnostic_errors = 0;
			App::uses('File', 'Utility');
			App::uses('Folder', 'Utility');

			// Only run this check on the diagnostics tab
			if ($tab == 'diagnostics') {
				// check if the current version of MISP is outdated or not
				$version = $this->__checkVersion();
				$this->set('version', $version);
				if ($version && (!$version['upToDate'] || $version['upToDate'] == 'older')) $diagnostic_errors++;
			}

			if ($tab == 'files') {
				$files = $this->__manageFiles();
				$this->set('files', $files);
			}

			// check whether the files are writeable
			$writeableDirs = $this->Server->writeableDirsDiagnostics($diagnostic_errors);

			// check if the STIX and Cybox libraries are working and the correct version using the test script stixtest.py
			$stix = $this->Server->stixDiagnostics($diagnostic_errors, $stixVersion, $cyboxVersion);

			// if GPG is set up in the settings, try to encrypt a test message
			$gpgStatus = $this->Server->gpgDiagnostics($diagnostic_errors);

			// if Proxy is set up in the settings, try to connect to a test URL
			$proxyStatus = $this->Server->proxyDiagnostics($diagnostic_errors);

			$viewVars = array(
					'gpgStatus', 'proxyStatus', 'diagnostic_errors', 'tabs', 'tab', 'issues', 'finalSettings', 'writeableErrors','gpgErrors', 'proxyErrors', 'stixOperational', 'stixVersion', 'cyboxVersion', 'stix', 'writeableDirs'
			);

			foreach ($viewVars as $viewVar) $this->set($viewVar, ${$viewVar});

			if (Configure::read('MISP.background_jobs')) {
				$worker_array = array(
					'cache' => array(),
					'default' => array(),
					'email' => array(),
					'_schdlr_' => array()
				);
				// disable notice errors, getWorkers() is meant to be run from the command line and throws a notice
				// because STDIN is not defined - since we don't actually log anything this is safe to ignore.
				$error_reporting = error_reporting();
				error_reporting(0);
				$results = CakeResque::getWorkers();
				error_reporting($error_reporting);
				foreach ($results as $result) {
					$result = (array)$result;
					if (in_array($result["\0*\0queues"][0], array_keys($worker_array))) {
						$worker_array[$result["\0*\0queues"][0]][] = $result["\0*\0id"];
					}
				}
				$workerIssueCount = 0;
				foreach ($worker_array as $k => $queue) {
					if (empty($queue)) $workerIssueCount++;
				}
				$this->set('worker_array', $worker_array);
			} else {
				$workerIssueCount = 4;
				$this->set('worker_array', array());
			}
			if ($tab == 'download') {
				foreach ($dumpResults as &$dr) {
					unset($dr['description']);
				}
				$dump = array('gpgStatus' => $gpgErrors[$gpgStatus], 'proxyStatus' => $proxyErrors[$proxyStatus], 'stix' => $stix, 'writeableDirs' => $writeableDirs, 'finalSettings' => $dumpResults);
				$this->response->body(json_encode($dump, JSON_PRETTY_PRINT));
				$this->response->type('json');
				$this->response->download('MISP.report.json');
				return $this->response;
			}
			$priorities = array(0 => 'Critical', 1 => 'Recommended', 2 => 'Optional', 3 => 'Deprecated');
			$priorityErrorColours = array(0 => 'red', 1 => 'yellow', 2 => 'green');
			$this->set('priorities', $priorities);
			$this->set('workerIssueCount', $workerIssueCount);
			$this->set('priorityErrorColours', $priorityErrorColours);
		}
	}

	private function __checkVersion() {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		App::uses('SyncTool', 'Tools');
		$syncTool = new SyncTool();
		try {
			$HttpSocket = $syncTool->setupHttpSocket();
			$response = $HttpSocket->get('https://api.github.com/repos/MISP/MISP/tags');
			$tags = $response->body;
		} catch (Exception $e) {
			return false;
		}
		if ($response->isOK() && !empty($tags)) {
			$json_decoded_tags = json_decode($tags);

			// find the latest version tag in the v[major].[minor].[hotfix] format
			for ($i = 0; $i < count($json_decoded_tags); $i++) {
				if (preg_match('/^v[0-9]+\.[0-9]+\.[0-9]+$/', $json_decoded_tags[$i]->name)) break;
			}
			return $this->Server->checkVersion($json_decoded_tags[$i]->name);
		} else {
			return false;
		}

	}

	public function serverSettingsEdit($setting, $id, $forceSave = false) {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		if (!isset($setting) || !isset($id)) throw new MethodNotAllowedException();
		$this->set('id', $id);
		$relevantSettings = (array_intersect_key(Configure::read(), $this->Server->serverSettings));
		$found = null;
		foreach ($this->Server->serverSettings as $k => $s) {
			if (isset($s['branch'])) {
				foreach ($s as $ek => $es) {
					if ($ek != 'branch') {
						if ($setting == $k . '.' . $ek) {
							$found = $es;
							continue 2;
						}
					}
				}
			} else {
				if ($setting == $k) {
					$found = $s;
					continue;
				}
			}
		}
		if ($this->request->is('get')) {
			if ($found != null) {
				$value = Configure::read($setting);
				if ($value) $found['value'] = $value;
				$found['setting'] = $setting;
			}
			$this->set('setting', $found);
			$this->render('ajax/server_settings_edit');
		}
		if ($this->request->is('post')) {
			if ($found['type'] == 'boolean') {
				$this->request->data['Server']['value'] = ($this->request->data['Server']['value'] ? true : false);
			}
			if ($found['type'] == 'numeric') {
				$this->request->data['Server']['value'] = intval($this->request->data['Server']['value']);
			}
			$testResult = $this->Server->{$found['test']}($this->request->data['Server']['value']);
			if (!$forceSave && $testResult !== true) {
				if ($testResult === false) $errorMessage = $found['errorMessage'];
				else $errorMessage = $testResult;
				return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => $errorMessage)),'status'=>200));
			} else {
				$oldValue = Configure::read($setting);
				$this->Server->serverSettingsSaveValue($setting, $this->request->data['Server']['value']);
				$this->loadModel('Log');
				$this->Log->create();
				$result = $this->Log->save(array(
						'org' => $this->Auth->user('org'),
						'model' => 'Server',
						'model_id' => 0,
						'email' => $this->Auth->user('email'),
						'action' => 'serverSettingsEdit',
						'user_id' => $this->Auth->user('id'),
						'title' => 'Server setting changed',
						'change' => $setting . ' (' . $oldValue . ') => (' . $this->request->data['Server']['value'] . ')',
				));
				$this->autoRender = false;
				return new CakeResponse(array('body'=> json_encode(array('saved' => true, 'success' => 'Field updated.')),'status'=>200));
			}
		}
	}

	public function restartWorkers() {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		shell_exec(APP . 'Console' . DS . 'worker' . DS . 'start.sh > /dev/null &');
		$this->redirect(array('controller' => 'servers', 'action' => 'serverSettings', 'workers'));
	}

	private function __manageFiles() {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		$files = $this->Server->grabFiles();
		return $files;
	}

	public function deleteFile($type, $filename) {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		if ($this->request->is('post')) {
			$validItems = $this->Server->getFileRules();
			App::uses('File', 'Utility');
			$existingFile = new File($validItems[$type]['path'] . DS . $filename);
			if (!$existingFile->exists()) {
				$this->Session->setFlash(__('File not found.', true), 'default', array(), 'error');
				$this->redirect(array('controller' => 'servers', 'action' => 'serverSettings', 'files'));
			}
			if ($existingFile->delete()) {
				$this->Session->setFlash('File deleted.');
			} else {
				$this->Session->setFlash(__('File could not be deleted.', true), 'default', array(), 'error');
			}
			$this->redirect(array('controller' => 'servers', 'action' => 'serverSettings', 'files'));
		} else {
			throw new MethodNotAllowedException('This action expects a POST request.');
		}
	}

	public function uploadFile($type) {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		$validItems = $this->Server->getFileRules();

		// Check if there were problems with the file upload
		// only keep the last part of the filename, this should prevent directory attacks
		$filename = basename($this->request->data['Server']['file']['name']);
		if (!preg_match("/" . $validItems[$type]['regex'] . "/", $filename)) {
			$this->Session->setFlash(__($validItems[$type]['regex_error'], true), 'default', array(), 'error');
			$this->redirect(array('controller' => 'servers', 'action' => 'serverSettings', 'files'));
		}
		if (empty($this->request->data['Server']['file']['tmp_name']) || !is_uploaded_file($this->request->data['Server']['file']['tmp_name'])) {
			$this->Session->setFlash(__('Upload failed.', true), 'default', array(), 'error');
			$this->redirect(array('controller' => 'servers', 'action' => 'serverSettings', 'files'));
		}

		// check if the file already exists
		App::uses('File', 'Utility');
		$existingFile = new File($validItems[$type]['path'] . DS . $filename);
		if ($existingFile->exists()) {
			$this->Session->setFlash(__('File already exists. If you would like to replace it, remove the old one first.', true), 'default', array(), 'error');
			$this->redirect(array('controller' => 'servers', 'action' => 'serverSettings', 'files'));
		}

		$result = move_uploaded_file($this->request->data['Server']['file']['tmp_name'], $validItems[$type]['path'] . DS . $filename);
		if ($result) {
			$this->Session->setFlash('File uploaded.');
		} else {
			$this->Session->setFlash(__('Upload failed.', true), 'default', array(), 'error');
		}
		$this->redirect(array('controller' => 'servers', 'action' => 'serverSettings', 'files'));
	}
}