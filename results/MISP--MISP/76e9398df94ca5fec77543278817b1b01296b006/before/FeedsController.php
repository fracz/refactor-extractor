<?php
App::uses('AppController', 'Controller');
App::uses('Xml', 'Utility');

class FeedsController extends AppController {

	public $components = array('Security' ,'RequestHandler');	// XXX ACL component

	public $paginate = array(
			'limit' => 60,
			'recursive' => -1,
			'contain' => array('Tag', 'SharingGroup'),
			'maxLimit' => 9999, // LATER we will bump here on a problem once we have more than 9999 events
			'order' => array(
					'Feed.default' => 'DESC',
					'Feed.id' => 'ASC'
			),
	);

	public $uses = array('Feed');

	public function beforeFilter() {
		parent::beforeFilter();
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException('You don\'t have the required privileges to do that.');
	}

	public function index() {
		$data = $this->paginate();
		$this->loadModel('Event');
		foreach ($data as $key => $value) {
			if ($value['Feed']['event_id'] != 0 && $value['Feed']['fixed_event']) {
				$event = $this->Event->find('first', array('conditions' => array('Event.id' => $value['Feed']['event_id']), 'recursive' => -1, 'fields' => array('Event.id')));
				if (empty($event)) {
					$data[$key]['Feed']['event_error'] = true;
				}
			}
		}
		if ($this->_isRest()) {
			foreach ($data as $k => $v) {
				unset($data[$k]['SharingGroup']);
				if (empty($data[$k]['Tag']['id'])) {
					unset($data[$k]['Tag']);
				}
			}
			return $this->RestResponse->viewData($data, $this->response->type());
		}
		$this->set('feeds', $data);
		$this->loadModel('Event');
		$this->set('feed_types', $this->Feed->feed_types);
		$this->set('distributionLevels', $this->Event->distributionLevels);
	}

	public function view($feedId) {
		$feed = $this->Feed->find('first', array('conditions' => array('Feed.id' => $feedId)));
	}

	public function add() {
		if ($this->request->is('post')) {
			$error = false;
			if (isset($this->request->data['Feed']['pull_rules'])) $this->request->data['Feed']['rules'] = $this->request->data['Feed']['pull_rules'];
			if ($this->request->data['Feed']['distribution'] != 4) $this->request->data['Feed']['sharing_group_id'] = 0;
			$this->request->data['Feed']['default'] = 0;
			if ($this->request->data['Feed']['source_format'] == 'freetext') {
				if ($this->request->data['Feed']['fixed_event'] == 1) {
					 if (is_numeric($this->request->data['Feed']['target_event'])) {
					 	$this->request->data['Feed']['event_id'] = $this->request->data['Feed']['target_event'];
					 }
				}
			}
			if (!isset($this->request->data['Feed']['settings'])) {
				$this->request->data['Feed']['settings'] = array();
			}
			if (isset($this->request->data['Feed']['settings']['separator']) && empty($this->request->data['Feed']['settings']['separator'])) {
				$this->request->data['Feed']['settings']['separator'] = ',';
			}
			if (empty($this->request->data['Feed']['target_event'])) {
				$this->request->data['Feed']['target_event'] = 0;
			}
			$this->request->data['Feed']['settings'] = json_encode($this->request->data['Feed']['settings']);
			$this->request->data['Feed']['event_id'] = !empty($this->request->data['Feed']['fixed_event']) ? $this->request->data['Feed']['target_event'] : 0;
			if (!$error) {
				$result = $this->Feed->save($this->request->data);
				if ($result) {
					$this->Session->setFlash('Feed added.');
					$this->redirect(array('controller' => 'feeds', 'action' => 'index'));
				}
				else $this->Session->setFlash('Feed could not be added. Invalid field: ' . array_keys($this->Feed->validationErrors)[0]);
			}
		}
		$this->loadModel('Event');
		$sgs = $this->Event->SharingGroup->fetchAllAuthorised($this->Auth->user(), 'name',  1);
		$distributionLevels = $this->Event->distributionLevels;
		if (empty($sgs)) unset($distributionLevels[4]);
		$this->set('distributionLevels', $distributionLevels);
		$this->set('sharingGroups', $sgs);
		$this->set('feed_types', $this->Feed->getFeedTypesOptions());
		$tags = $this->Event->EventTag->Tag->find('list', array('fields' => array('Tag.name'), 'order' => array('lower(Tag.name) asc')));
		$tags[0] = 'None';
		$this->set('tags', $tags);
	}

	public function edit($feedId) {
		$this->Feed->id = $feedId;
		if (!$this->Feed->exists()) throw new NotFoundException('Invalid feed.');
		$this->Feed->read();
		if (!empty($this->Feed->data['Feed']['settings'])) {
			$this->Feed->data['Feed']['settings'] = json_decode($this->Feed->data['Feed']['settings'], true);
		}
		if ($this->request->is('post') || $this->request->is('put')) {
			if (isset($this->request->data['Feed']['pull_rules'])) $this->request->data['Feed']['rules'] = $this->request->data['Feed']['pull_rules'];
			if ($this->request->data['Feed']['distribution'] != 4) $this->request->data['Feed']['sharing_group_id'] = 0;
			$this->request->data['Feed']['id'] = $feedId;
			if ($this->request->data['Feed']['source_format'] == 'freetext' || $this->request->data['Feed']['source_format'] == 'csv') {
				if ($this->request->data['Feed']['fixed_event'] == 1) {
					if (is_numeric($this->request->data['Feed']['target_event'])) {
						$this->request->data['Feed']['event_id'] = $this->request->data['Feed']['target_event'];
					} else {
						$this->request->data['Feed']['event_id'] = 0;
					}
				}
			}
			if (!isset($this->request->data['Feed']['settings'])) {
				$this->request->data['Feed']['settings'] = array();
			}
			if (isset($this->request->data['Feed']['settings']['separator']) && empty($this->request->data['Feed']['settings']['separator'])) {
				$this->request->data['Feed']['settings']['separator'] = ',';
			}
			$this->request->data['Feed']['settings'] = json_encode($this->request->data['Feed']['settings']);
			$fields = array('id', 'name', 'provider', 'enabled', 'rules', 'url', 'distribution', 'sharing_group_id', 'tag_id', 'fixed_event', 'event_id', 'publish', 'delta_merge', 'override_ids', 'settings');
			$feed = array();
			foreach ($fields as $field) {
				if (isset($this->request->data['Feed'][$field])) {
					$feed[$field] = $this->request->data['Feed'][$field];
				}
			}
			$result = $this->Feed->save($feed);
			if ($result) {
				$feedCache = APP . 'tmp' . DS . 'cache' . DS . 'feeds' . DS . intval($feed['Feed']['id']) . '.cache';
				if (file_exists($feedCache)) {
					unlink($feedCache);
				}
				$this->Session->setFlash('Feed updated.');
				$this->redirect(array('controller' => 'feeds', 'action' => 'index'));
			} else {
				$this->Session->setFlash('Feed could not be updated. Invalid fields: ' . implode(', ', array_keys($this->Feed->validationErrors)));
			}
		} else {
			if (!isset($this->request->data['Feed'])) {
				$this->request->data = $this->Feed->data;
				if ($this->Feed->data['Feed']['event_id']) {
					$this->request->data['Feed']['target_event'] = $this->Feed->data['Feed']['event_id'];
				}
			}
			$this->request->data['Feed']['pull_rules'] = $this->request->data['Feed']['rules'];
		}
		$this->loadModel('Event');
		$sgs = $this->Event->SharingGroup->fetchAllAuthorised($this->Auth->user(), 'name',  1);
		$distributionLevels = $this->Event->distributionLevels;
		if (empty($sgs)) unset($distributionLevels[4]);
		$this->set('distributionLevels', $distributionLevels);
		$this->set('sharingGroups', $sgs);
		$tags = $this->Event->EventTag->Tag->find('list', array('fields' => array('Tag.name'), 'order' => array('lower(Tag.name) asc')));
		$tags[0] = 'None';
		$this->set('feed_types', $this->Feed->getFeedTypesOptions());
		$this->set('tags', $tags);
	}

	public function delete($feedId) {
		if (!$this->request->is('post')) throw new MethodNotAllowedException('This action requires a post request.');
		$this->Feed->id = $feedId;
		if (!$this->Feed->exists()) throw new NotFoundException('Invalid feed.');
		if ($this->Feed->delete($feedId)) $this->Session->setFlash('Feed deleted.');
		else $this->Session->setFlash('Feed could not be deleted.');
		$this->redirect(array('controller' => 'feeds', 'action' => 'index'));
	}

	public function fetchFromFeed($feedId) {
		$this->Feed->id = $feedId;
		if (!$this->Feed->exists()) throw new NotFoundException('Invalid feed.');
		$this->Feed->read();
		if (!empty($this->Feed->data['Feed']['settings'])) {
			$this->Feed->data['Feed']['settings'] = json_decode($this->Feed->data['Feed']['settings'], true);
		}
		if (!$this->Feed->data['Feed']['enabled']) {
			$this->Session->setFlash('Feed is currently not enabled. Make sure you enable it.');
			$this->redirect(array('action' => 'index'));
		}
		if (Configure::read('MISP.background_jobs')) {
			$this->loadModel('Job');
			$this->Job->create();
			$data = array(
					'worker' => 'default',
					'job_type' => 'fetch_feed',
					'job_input' => 'Feed: ' . $feedId,
					'status' => 0,
					'retries' => 0,
					'org' => $this->Auth->user('Organisation')['name'],
					'message' => 'Starting fetch from Feed.',
			);
			$this->Job->save($data);
			$jobId = $this->Job->id;
			$process_id = CakeResque::enqueue(
					'default',
					'ServerShell',
					array('fetchFeed', $this->Auth->user('id'), $feedId, $jobId),
					true
			);
			$this->Job->saveField('process_id', $process_id);
			$message = 'Pull queued for background execution.';
		} else {
			$result = $this->Feed->downloadFromFeedInitiator($feedId, $this->Auth->user());
			if (!$result) {
				$this->Session->setFlash('Fetching the feed has failed.');
				$this->redirect(array('action' => 'index'));
			}
			$message = 'Fetching the feed has successfuly completed.';
			if ($this->Feed->data['Feed']['source_format'] == 'misp') {
				if (isset($result['add'])) $message .= ' Downloaded ' . count($result['add']) . ' new event(s).';
				if (isset($result['edit'])) $message .= ' Updated ' . count($result['edit']) . ' event(s).';
			}
		}
		$this->Session->setFlash($message);
		$this->redirect(array('action' => 'index'));
	}

	public function getEvent($feedId, $eventUuid, $all = false) {
		$this->Feed->id = $feedId;
		if (!$this->Feed->exists()) throw new NotFoundException('Invalid feed.');
		$this->Feed->read();
		if (!$this->Feed->data['Feed']['enabled']) {
			$this->Session->setFlash('Feed is currently not enabled. Make sure you enable it.');
			$this->redirect(array('action' => 'previewIndex', $feedId));
		}
		$result = $this->Feed->downloadAndSaveEventFromFeed($this->Feed->data, $eventUuid, $this->Auth->user());
		if (isset($result['action'])) {
			if ($result['result']) {
				if ($result['action'] == 'add') $message = 'Event added.';
				else {
					if ($result['result'] === 'No change') $message = 'Event already up to date.';
					else $message = 'Event updated.';
				}
			} else {
				$message = 'Could not ' . $result['action'] . ' event.';
			}
		} else $message = 'Download failed.';
		$this->Session->setFlash($message);
		$this->redirect(array('action' => 'previewIndex', $feedId));
	}

	public function previewIndex($feedId) {
		$this->Feed->id = $feedId;
		if (!$this->Feed->exists()) throw new NotFoundException('Invalid feed.');
		$this->Feed->read();
		if (!empty($this->Feed->data['Feed']['settings'])) {
			$this->Feed->data['Feed']['settings'] = json_decode($this->Feed->data['Feed']['settings'], true);
		}
		if ($this->Feed->data['Feed']['source_format'] == 'misp') {
			$this->__previewIndex($this->Feed->data);
		} else if (in_array($this->Feed->data['Feed']['source_format'], array('freetext', 'csv'))) {
			$this->__previewFreetext($this->Feed->data);
		}
	}

	private function __previewIndex($feed) {
		if (isset($this->passedArgs['pages'])) $currentPage = $this->passedArgs['pages'];
		else $currentPage = 1;
		$urlparams = '';
		$passedArgs = array();
		App::uses('SyncTool', 'Tools');
		$syncTool = new SyncTool();
		$HttpSocket = $syncTool->setupHttpSocketFeed($feed);
		$events = $this->Feed->getManifest($feed, $HttpSocket);
		if (isset($events['code'])) throw new NotFoundException('Feed could not be fetched. The HTTP error code returned was: ' .$events['code']);
		$pageCount = count($events);
		App::uses('CustomPaginationTool', 'Tools');
		$customPagination = new CustomPaginationTool();
		$params = $customPagination->createPaginationRules($events, $this->passedArgs, $this->alias);
		$this->params->params['paging'] = array($this->modelClass => $params);
		if (is_array($events)) $customPagination->truncateByPagination($events, $params);
		else ($events = array());

		$this->set('events', $events);
		$this->loadModel('Event');
		$threat_levels = $this->Event->ThreatLevel->find('all');
		$this->set('threatLevels', Set::combine($threat_levels, '{n}.ThreatLevel.id', '{n}.ThreatLevel.name'));
		$this->set('eventDescriptions', $this->Event->fieldDescriptions);
		$this->set('analysisLevels', $this->Event->analysisLevels);
		$this->set('distributionLevels', $this->Event->distributionLevels);
		$shortDist = array(0 => 'Organisation', 1 => 'Community', 2 => 'Connected', 3 => 'All', 4 => ' sharing Group');
		$this->set('shortDist', $shortDist);
		$this->set('id', $feed['Feed']['id']);
		$this->set('feed', $feed);
		$this->set('urlparams', $urlparams);
		$this->set('passedArgs', json_encode($passedArgs));
		$this->set('passedArgsArray', $passedArgs);
	}

	private function __previewFreetext($feed) {
		if (isset($this->passedArgs['page'])) $currentPage = $this->passedArgs['page'];
		else if (isset($this->passedArgs['page'])) $currentPage = $this->passedArgs['page'];
		else $currentPage = 1;
		$urlparams = '';
		App::uses('SyncTool', 'Tools');
		$syncTool = new SyncTool();
		if (!in_array($feed['Feed']['source_format'], array('freetext', 'csv'))) throw new MethodNotAllowedException('Invalid feed type.');
		$HttpSocket = $syncTool->setupHttpSocketFeed($feed);
		$params = array();
		// params is passed as reference here, the pagination happens in the method, which isn't ideal but considering the performance gains here it's worth it
		$resultArray = $this->Feed->getFreetextFeed($feed, $HttpSocket, $feed['Feed']['source_format'], $currentPage, 60, $params);
		$this->params->params['paging'] = array($this->modelClass => $params);
		$resultArray = $this->Feed->getFreetextFeedCorrelations($resultArray);
		// remove all duplicates
		foreach ($resultArray as $k => $v) {
			for ($i = 0; $i < $k; $i++) {
				if (isset($resultArray[$i]) && $v == $resultArray[$i]) unset($resultArray[$k]);
			}
		}
		$resultArray = array_values($resultArray);
		$this->loadModel('Attribute');
		$this->set('distributionLevels', $this->Attribute->distributionLevels);
		$this->set('feed', $feed);
		$this->set('attributes', $resultArray);
		$this->render('freetext_index');
	}

	private function __previewCSV($feed) {
		if (isset($this->passedArgs['pages'])) $currentPage = $this->passedArgs['pages'];
		else $currentPage = 1;
		App::uses('SyncTool', 'Tools');
		$syncTool = new SyncTool();
		if ($feed['Feed']['source_format'] != 'csv') throw new MethodNotAllowedException('Invalid feed type.');
		$HttpSocket = $syncTool->setupHttpSocketFeed($feed);
		$resultArray = $this->Feed->getFreetextFeed($feed, $HttpSocket, $feed['Feed']['source_format'], $currentPage);
		$resultArray = $this->Feed->getFreetextFeedCorrelations($resultArray);
		// remove all duplicates
		foreach ($resultArray as $k => $v) {
			for ($i = 0; $i < $k; $i++) {
				if (isset($resultArray[$i]) && $v == $resultArray[$i]) unset($resultArray[$k]);
			}
		}
		$resultArray = array_values($resultArray);
		$this->loadModel('Attribute');
		$this->set('distributionLevels', $this->Attribute->distributionLevels);
		$this->set('feed', $feed);
		$this->set('attributes', $resultArray);
		$this->render('freetext_index');
	}


	public function previewEvent($feedId, $eventUuid, $all = false) {
		$this->Feed->id = $feedId;
		if (!$this->Feed->exists()) throw new NotFoundException('Invalid feed.');
		$this->Feed->read();
		$event = $this->Feed->downloadEventFromFeed($this->Feed->data, $eventUuid, $this->Auth->user());
		if (is_array($event)) {
			$this->loadModel('Event');
			$params = $this->Event->rearrangeEventForView($event, $this->passedArgs, $all);
			$this->params->params['paging'] = array('Feed' => $params);
			$this->set('event', $event);
			$this->set('feed', $this->Feed->data);
			$this->loadModel('Event');
			$dataForView = array(
					'Attribute' => array('attrDescriptions' => 'fieldDescriptions', 'distributionDescriptions' => 'distributionDescriptions', 'distributionLevels' => 'distributionLevels'),
					'Event' => array('eventDescriptions' => 'fieldDescriptions', 'analysisLevels' => 'analysisLevels')
			);
			foreach ($dataForView as $m => $variables) {
				if ($m === 'Event') $currentModel = $this->Event;
				else if ($m === 'Attribute') $currentModel = $this->Event->Attribute;
				foreach ($variables as $alias => $variable) {
					$this->set($alias, $currentModel->{$variable});
				}
			}
			$threat_levels = $this->Event->ThreatLevel->find('all');
			$this->set('threatLevels', Set::combine($threat_levels, '{n}.ThreatLevel.id', '{n}.ThreatLevel.name'));
		} else {
			if ($event === 'blocked') throw new MethodNotAllowedException('This event is blocked by the Feed filters.');
			else throw new NotFoundException('Could not download the selected Event');
		}
	}

	public function enable($id) {
		$result = $this->__toggleEnable($id, true);
		$this->set('name', $result['message']);
		$this->set('message', $result['message']);
		$this->set('url', $this->here);
		if ($result) {
			$this->set('_serialize', array('name', 'message', 'url'));
		} else {
			$this->set('errors', $result);
			$this->set('_serialize', array('name', 'message', 'url', 'errors'));
		}
	}

	public function disable($id) {
		$result = $this->__toggleEnable($id, false);
		$this->set('name', $result['message']);
		$this->set('message', $result['message']);
		$this->set('url', $this->here);
		if ($result['result']) {
			$this->set('_serialize', array('name', 'message', 'url'));
		} else {
			$this->set('errors', $result);
			$this->set('_serialize', array('name', 'message', 'url', 'errors'));
		}
	}

	private function __toggleEnable($id, $enable = true) {
		if (!is_numeric($id)) throw new MethodNotAllowedException('Invalid Feed.');
		$this->Feed->id = $id;
		if (!$this->Feed->exists()) throw new MethodNotAllowedException('Invalid Feed.');
		$feed = $this->Feed->find('first', array(
				'conditions' => array('Feed.id' => $id),
				'recursive' => -1
		));
		$feed['Feed']['enabled'] = $enable;
		$result = array('result' => $this->Feed->save($feed));
		$fail = false;
		if (!$result['result']) {
			$fail = true;
			$result['result'] = $this->Feed->validationErrors;
		}
		$action = $enable ? 'enable' : 'disable';
		$result['message'] = $fail ? 'Could not ' . $action . ' feed.' : 'Feed ' . $action . 'd.';
		return $result;
	}

	public function fetchSelectedFromFreetextIndex($id) {
		if (!$this->request->is('Post')) {
			throw new MethodNotAllowedException('Only POST requests are allowed.');
		}
		$this->Feed->id = $id;
		if (!$this->Feed->exists()) {
			throw new NotFoundException('Feed not found.');
		}
		$feed = $this->Feed->read();
		if (!empty($feed['Feed']['settings'])) {
			$feed['Feed']['settings'] = json_decode($feed['Feed']['settings'], true);
		}
		$data = json_decode($this->request->data['Feed']['data'], true);
		$result = $this->Feed->saveFreetextFeedData($feed, $data, $this->Auth->user());
		if ($result === true) {
			$this->Session->setFlash('Data pulled.');
		} else {
			$this->Session->setFlash('Could not pull the selected data. Reason: ' . $result);
		}
		$this->redirect(array('controller' => 'feeds', 'action' => 'index'));
	}
}