<?php

App::uses('AppModel', 'Model');

/**
 * Log Model
 *
 */
class Log extends AppModel {

	public $validate = array(
			'action' => array(
			'rule' => array('inList', array(
							'login',
							'logout',
							'add',
							'edit',
							'change_pw',
							'delete',
							'publish',
							'accept',
							'discard',
							'pull',
							'push'
						)),
			'message' => 'Options : ...'
		)
	);

	public $actionDefinitions = array(
		'login' => array('desc' => 'Login action', 'formdesc' => "Login action"),
		'logout' => array('desc' => 'Logout action', 'formdesc' => "Logout action"),
		'add' => array('desc' => 'Add action', 'formdesc' => "Add action"),
		'edit' => array('desc' => 'Edit action', 'formdesc' => "Edit action"),
		'change_pw' => array('desc' => 'Change_pw action', 'formdesc' => "Change_pw action"),
		'delete' => array('desc' => 'Delete action', 'formdesc' => "Delete action"),
		'publish' => array('desc' => "Publish action", 'formdesc' => "Publish action")
	);

	public function returnDates($org = 'all') {
		$conditions = array();
		if ($org !== 'all') $conditions['org'] = $org;
		$validDates = $this->find('all', array(
				'fields' => array('UNIX_TIMESTAMP(DATE(created)) AS Date', 'count(id) AS count'),
				'conditions' => $conditions,
				'group' => array('DAY(created)'),
				'order' => array('Date')
		));

		$data = array();
		foreach ($validDates as $k => $date) {
			$data[$date[0]['Date']] = intval($date[0]['count']);
		}
		return $data;
	}

	public function maxDateActivity() {
		$validDates = $this->find('all', array(
				'fields' => array('count(id) AS count'),
				'group' => array('DAY(created)'),
		));
		$max = 0;
		foreach ($validDates as $v) {
			if ($v[0]['count'] > $max) $max = $v[0]['count'];
		}
		return $max;
	}
}