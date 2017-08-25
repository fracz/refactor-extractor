<?php

App::uses('AppController', 'Controller');

/**
 * Jobs Controller
 *
 * @property Job $Job
*/
class JobsController extends AppController {
	public $components = array('Security' ,'RequestHandler', 'Session');

	public $paginate = array(
			'limit' => 20,
			'order' => array(
					'Job.id' => 'desc'
			)
	);

	public function beforeFilter() {
		parent::beforeFilter();
	}

	public function index() {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		$this->recursive = 0;
		$this->set('list', $this->paginate());
	}

	public function retry($id) {

	}

	public function getGenerateCorrelationProgress($id) {
		//if (!self::_isSiteAdmin()) throw new NotFoundException();
		$progress = $this->Job->findById($id);
		if (!$progress) {
			$progress = 0;
		} else {
			$progress = $progress['Job']['progress'];
		}
		return new CakeResponse(array('body' => json_encode($progress)));
	}

	public function getProgress($type) {
		$org = $this->Auth->user('org');
		if ($this->_isSiteAdmin()) $org = 'ADMIN';
		$progress = $this->Job->find('first', array(
			'conditions' => array(
				'job_type' => $type,
				'org' => $org
				),
			'fields' => array('id', 'progress'),
			'order' => array('Job.id' => 'desc'),
		));
		if (!$progress) {
			$progress = 0;
		} else {
			$progress = $progress['Job']['progress'];
		}
		return new CakeResponse(array('body' => json_encode($progress)));
	}

	public function cache($type) {
		if ($this->_isSiteAdmin()) {
			$target = 'All events.';
			$jobOrg = 'ADMIN';
		} else {
			$target = 'Events visible to: '.$this->Auth->user('org');
			$jobOrg = $this->Auth->user('org');
		}
		$extra = null;
		$shell = 'Event';
		$this->Job->create();
		$data = array(
				'worker' => 'default',
				'job_type' => 'cache_' . $type,
				'job_input' => $target,
				'status' => 0,
				'retries' => 0,
				'org' => $jobOrg,
				'message' => 'Fetching events.',
		);
		if ($type === 'md5' || $type === 'sha1') {
			$extra = $type;
			$type = 'hids';
		}
		if ($type === 'csv_all' || $type === 'csv_sig') {
			$extra = $type;
			$type = 'csv';
		}
		$this->Job->save($data);
		$id = $this->Job->id;
		CakeResque::enqueue(
		'default',
		$shell . 'Shell',
		array('cache' . $type, $this->Auth->user('org'), $this->_isSiteAdmin(), $id, $extra)
		);
		return new CakeResponse(array('body' => json_encode($id)));
	}

}