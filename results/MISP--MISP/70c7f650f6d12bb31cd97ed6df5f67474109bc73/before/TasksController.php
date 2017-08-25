<?php

App::uses('AppController', 'Controller');

/**
 * Jobs Controller
 *
 * @property Job $Job
*/
class TasksController extends AppController {
	public $components = array('Security' ,'RequestHandler', 'Session');

	public $paginate = array(
			'limit' => 20,
			'order' => array(
					'Task.id' => 'desc'
			)
	);

	public function beforeFilter() {
		parent::beforeFilter();
	}

	public function index() {
		if (!$this->_isSiteAdmin()) throw new MethodNotAllowedException();
		if (!Configure::read('MISP.background_jobs')) throw new NotFoundException('Background jobs are not enabled on this instance.');
		$this->__checkTasks();
		$this->recursive = 0;
		$tasks = $this->paginate();
		$this->set('list', $tasks);
		$this->set('time', time());
	}

	// checks if all the mandatory tasks exist, and if not, creates them
	// default tasks are:
	// 'cache_exports'
	private function __checkTasks() {
		foreach ($this->Task->tasks as $default_task) {
			if (!$this->Task->findByType($default_task['type'], array('id', 'type'))) {
				$this->Task->save($default_task);
			}
		}
	}

	public function setTask() {
		if (!$this->_isSiteAdmin()) {
			throw new MethodNotAllowedException('You are not authorised to do that.');
		}
		$today = $this->_getTodaysTimestamp();
		if ($this->request->is('post') || $this->request->is('put')) {
			$tasks = $this->Task->find('all', array('fields' => array('id', 'timer', 'scheduled_time', 'type', 'next_execution_time')));
			foreach ($tasks as $k => $task) {
				if ($this->request->data['Task'][$task['Task']['id']]['timer'] == $task['Task']['timer']) unset($this->request->data['Task'][$task['Task']['id']]['timer']);
				if ($this->request->data['Task'][$task['Task']['id']]['scheduled_time'] == $task['Task']['scheduled_time']) unset($this->request->data['Task'][$task['Task']['id']]['scheduled_time']);
				if (empty($this->request->data['Task'][$task['Task']['id']])) {
					unset($this->request->data['Task'][$task['Task']['id']]);
				} else {
					$this->request->data['Task'][$task['Task']['id']]['id'] = $task['Task']['id'];
					if (isset($this->request->data['Task'][$task['Task']['id']]['next_execution_time'])) {
						$temp = $this->request->data['Task'][$task['Task']['id']]['next_execution_time'];
					} else {
						$temp = date("Y-m-d", $task['Task']['next_execution_time']);
					}
					if (isset($this->request->data['Task'][$task['Task']['id']]['scheduled_time'])) {
						$this->request->data['Task'][$task['Task']['id']]['next_execution_time'] = strtotime($temp . ' ' . $this->request->data['Task'][$task['Task']['id']]['scheduled_time']);
					} else {
						$this->request->data['Task'][$task['Task']['id']]['next_execution_time'] = strtotime($temp . ' ' . $task['Task']['scheduled_time']);
					}
					// schedule task
					$this->_jobScheduler($task['Task']['type'], $this->request->data['Task'][$task['Task']['id']]['next_execution_time']);
					$this->Task->save($this->request->data['Task'][$task['Task']['id']]);
				}
			}
			$this->Session->setFlash('Task edited');
			$this->redirect(array('action' => 'index'));
		}
	}

	private function _getTodaysTimestamp() {
		return strtotime(date("d/m/Y") . ' 00:00:00');
	}

	private function _jobScheduler($type, $timestamp) {
		if ($type === 'cache_exports') $this->_cacheScheduler($timestamp);
	}

	private function _cacheScheduler($timestamp) {
		CakeResque::enqueueAt(
				$timestamp,
				'cache',
				'EventShell',
				array('enqueueCaching', $timestamp),
				true
		);
	}
}