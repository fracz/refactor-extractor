<?php
App::uses('AppController', 'Controller');

class SightingsController extends AppController {
	public $components = array('Session', 'RequestHandler');

	public function beforeFilter() {
		parent::beforeFilter();
		if (Configure::read('Plugin.Sightings_enable') === false) throw new MethodNotAllowedException('This feature is not enabled on this instance.');
	}

	public $paginate = array(
			'limit' => 60,
			'maxLimit' => 9999,	// LATER we will bump here on a problem once we have more than 9999 events <- no we won't, this is the max a user van view/page.
			'order' => array('Sighting.date_sighting' => 'DESC'),
	);

	// takes an attribute ID or UUID
	public function add($id = false) {
		if (!$this->userRole['perm_add']) throw new MethodNotAllowedException('You are not authorised to add sightings data as you don\'t have write access.');
		if (!$this->request->is('post')) throw new MethodNotAllowedException('This action can only be accessed via a post request.');
		$now = time();
		$values = false;
		$timestamp = false;
		$error = false;
		if ($id === 'stix') {
			$result = $this->Sighting->handleStixSighting(file_get_contents('php://input'));
			if ($result['success']) {
				$result['data'] = json_decode($result['data'], true);
				$timestamp = isset($result['data']['timestamp']) ? strtotime($result['data']['timestamp']) : $now;
				if (isset($result['data']['values'])) $values = $result['data']['values'];
				else $error = 'No valid values found could be extracted from the sightings document.';
			} $error = $result['message'];
		} else {
			if (isset($this->request->data['request'])) $this->request->data = $this->request->data['request'];
			if (isset($this->request->data['Sighting'])) $this->request->data = $this->request->data['Sighting'];
			$timestamp = isset($this->request->data['timestamp']) ? $this->request->data['timestamp'] : $now;
			if (isset($this->request->data['value'])) $this->request->data['values'] = array($this->request->data['value']);
			$values = isset($this->request->data['values']) ? $this->request->data['values'] : false;
			if (!$id && isset($this->request->data['id'])) $id = $this->request->data['id'];
		}
		if (!$error) $result = $this->Sighting->saveSightings($id, $values, $timestamp, $this->Auth->user());
		if ($result == 0) $error = 'No valid attributes found that would match the sighting criteria.';

		if ($this->request->is('ajax')) {
			if ($error) {
				$error_message = 'Could not add the Sighting. Reason: ' . $error;
				return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => $error_message)), 'status' => 200));
			} else {
				return new CakeResponse(array('body'=> json_encode(array('saved' => true, 'success' => $result . ' sighting' . (($result == 1) ? '' : 's') . '  added.')), 'status' => 200));
			}
		} else {
			if ($error) {
				return $this->RestResponse->saveFailResponse('Sighting', 'add', $id, $error);
			} else {
				return $this->RestResponse->saveSuccessResponse('Sighting', 'add', $id, false, $result . ' sighting' . (($result == 1) ? '' : 's') . ' successfuly added.');
			}
		}
	}

	// takes a sighting ID
	public function delete($id) {
		if (!$this->userRole['perm_modify_org']) throw new MethodNotAllowedException('You are not authorised to remove sightings data as you don\'t have permission to modify your organisation\'s data.');
		if (!$this->request->is('post')) throw new MethodNotAllowedException('This action can only be accessed via a post request.');
		$sighting = $this->Sighting->find('first', array('conditions' => array('Sighting.id' => $id), 'recursive' => -1));
		if (empty($sighting)) throw new NotFoundException('Invalid sighting.');
		if (!$this->_isSiteAdmin()) {
			if ($sighting['Sighting']['org_id'] != $this->Auth->user('org_id')) throw new NotFoundException('Invalid sighting.');
		}
		$result = $this->Sighting->delete($sighting['Sighting']['id']);
		if (!$result) {
			return $this->RestResponse->saveFailResponse('Sighting', 'delete', $id, 'Could not delete the Sighting.');
		} else {
			return $this->RestResponse->saveSuccessResponse('Sighting', 'delete', $id, false, 'Sighting successfuly deleted.');
		}
	}

	public function index($eventId = false, $type = 'simple') {
		$this->loadModel('Event');
		$rules = array(
			'recursive' => -1,
			'fields' => array('Sighting.event_id'),
			'group' => array('Sighting.event_id'),
			'conditions' => array()
		);
		if ($this->_isSiteAdmin() || !Configure::read('Plugin.Sightings_anonymise') && $type != 'simple') {
			$rules['fields'][] = 'Sighting.org_id';
			$rules['group'][] = 'Sighting.org_id';
		}
		if (!$this->_isSiteAdmin() && (Configure::read('Plugin.Sightings_policy') == 0 || Configure::read('Plugin.Sightings_policy') == false)) {
			$eventIds = $this->Event->find('list', array('conditions' => array('Event.orgc_id' => $this->Auth->user('org_id')), 'fields' => array('Event.id')));
			$rules['conditions']['OR'][] = array('Sighting.event_id' => $eventIds, 'Sighting.org_id' => $this->Auth->user('org_id'));
		} else if (!$this->_isSiteAdmin() && Configure::read('Plugin.Sightings_policy') == 1) {
			$rules['conditions'][] = array('Sighting.org_id' => $this->Auth->user('org_id'));
		} else if (!$this->_isSiteAdmin() && Configure::read('Plugin.Sightings_policy') == 2) {
			$eventIds = $this->Event->fetchEventIds($this->Auth->user(), false, false, false, true);
			$rules['conditions']['OR'][] = array('Sighting.event_id' => $eventIds);
		}
		debug($rules);
		$this->Sighting->virtualFields['count'] = 0;
		$this->Sighting->virtualFields['latest'] = 0;
		$sightings = $this->Sighting->find('all', $rules);
		debug($sightings);
	}
}