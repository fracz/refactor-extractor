<?php
App::uses('AppController', 'Controller');
App::uses('Xml', 'Utility');
/**
 * Events Controller
 *
 * @property Event $Event
*/
class EventsController extends AppController {

	/**
	 * Components
	 *
	 * @var array
	 */
	public $components = array(
			'Security',
			'Email',
			'RequestHandler',
			'IOCExport',
			'IOCImport',
			'Cidr'
	);

	public $paginate = array(
			'limit' => 60,
			'maxLimit' => 9999,	// LATER we will bump here on a problem once we have more than 9999 events <- no we won't, this is the max a user van view/page.
			'order' => array(
					'Event.timestamp' => 'DESC'
			),
			'contain' => array(
					'Org' => array('fields' => array('id', 'name')),
					'Orgc' => array('fields' => array('id', 'name')),
					'SharingGroup' => array('fields' => array('id', 'name'))
			)
	);

	public $helpers = array('Js' => array('Jquery'));

	public $paginationFunctions = array('index', 'proposalEventIndex');

	public function beforeFilter() {
		parent::beforeFilter();

		// what pages are allowed for non-logged-in users
		$this->Auth->allow('xml');
		$this->Auth->allow('csv');
		$this->Auth->allow('nids');
		$this->Auth->allow('hids_md5');
		$this->Auth->allow('hids_sha1');
		$this->Auth->allow('text');
		$this->Auth->allow('dot');
		$this->Auth->allow('restSearch');
		$this->Auth->allow('stix');

		// TODO Audit, activate logable in a Controller
		if (count($this->uses) && $this->{$this->modelClass}->Behaviors->attached('SysLogLogable')) {
			$this->{$this->modelClass}->setUserData($this->activeUser);
		}

		// convert uuid to id if present in the url, and overwrite id field
		if (isset($this->params->query['uuid'])) {
			$params = array(
					'conditions' => array('Event.uuid' => $this->params->query['uuid']),
					'recursive' => 0,
					'fields' => 'Event.id'
			);
			$result = $this->Event->find('first', $params);
			if (isset($result['Event']) && isset($result['Event']['id'])) {
				$id = $result['Event']['id'];
				$this->params->addParams(array('pass' => array($id))); // FIXME find better way to change id variable if uuid is found. params->url and params->here is not modified accordingly now
			}
		}

		// if not admin or own org, check private as well..
		if (!$this->_isSiteAdmin() && in_array($this->action, $this->paginationFunctions)) {
			$sgids = $this->Event->SharingGroup->fetchAllAuthorised($this->Auth->user());
			if (empty($sgids)) $sgids = array(-1);
			$conditions = array(
				'AND' => array(
					array(
						"OR" => array(
							array(
								'Event.org_id' => $this->Auth->user('org_id')
							),
							array(
								'AND' => array(
										'Event.distribution >' => 0,
										'Event.distribution <' => 4,
										Configure::read('MISP.unpublishedprivate') ? array('Event.published =' => 1) : array(),
								),
							),
							array(
								'AND' => array(
										'Event.distribution' => 4,
										'Event.sharing_group_id' => $sgids,
										Configure::read('MISP.unpublishedprivate') ? array('Event.published =' => 1) : array(),
								),
							)
						)
					)
				)
			);
			if ($this->userRole['perm_sync'] && $this->Auth->user('Server')['push_rules']) {
				$conditions['AND'][] = $this->Event->filterRulesToConditions($this->Auth->user('Server')['push_rules']);
			}
			$this->paginate = Set::merge($this->paginate,array('conditions' => $conditions));
		}
	}

	private function __filterOnAttributeValue($value) {
		// dissect the value
		$pieces = explode('|', $value);
		$test = array();
		$include = array();
		$exclude = array();
		$includeIDs = array();
		$excludeIDs = array();
		foreach ($pieces as $piece) {
			if ($piece[0] == '!') {
				$exclude[] =  '%' . strtolower(substr($piece, 1)) . '%';
			} else {
				$include[] = '%' . strtolower($piece) . '%';
			}
		}
		if (!empty($include)) {
			// get all of the attributes that should be included
			$includeQuery = array(
					'recursive' => -1,
					'fields' => array('id', 'event_id', 'distribution', 'value1', 'value2'),
					'conditions' => array(),
			);
			foreach ($include as $i) {
				$includeQuery['conditions']['OR'][] = array('lower(Attribute.value1) LIKE' => $i);
				$includeQuery['conditions']['OR'][] = array('lower(Attribute.value2) LIKE' => $i);
			}
			$includeHits = $this->Event->Attribute->find('all', $includeQuery);

			// convert it into an array that uses the event ID as a key

			foreach ($includeHits as $iH) {
				$includeIDs[$iH['Attribute']['event_id']][] = array('attribute_id' => $iH['Attribute']['id'], 'distribution' => $iH['Attribute']['distribution']);
			}
		}

		if (!empty($exclude)) {
			// get all of the attributes that should be excluded
			$excludeQuery = array(
				'recursive' => -1,
				'fields' => array('id', 'event_id', 'distribution', 'value1', 'value2'),
				'conditions' => array(),
			);
			foreach ($exclude as $e) {
				$excludeQuery['conditions']['OR'][] = array('lower(Attribute.value1) LIKE' => $e);
				$excludeQuery['conditions']['OR'][] = array('lower(Attribute.value2) LIKE' => $e);
			}
			$excludeHits = $this->Event->Attribute->find('all', $excludeQuery);

			// convert it into an array that uses the event ID as a key
			foreach ($excludeHits as $eH) {
				$excludeIDs[$eH['Attribute']['event_id']][] = array('attribute_id' => $eH['Attribute']['id'], 'distribution' => $eH['Attribute']['distribution']);
			}
		}
		$includeIDs = array_keys($includeIDs);
		$excludeIDs = array_keys($excludeIDs);
		// return -1 as the only value in includedIDs if both arrays are empty. This will mean that no events will be shown if there was no hit
		if (empty($includeIDs) && empty($excludeIDs)) $includeIDs[] = -1;
		return array($includeIDs, $excludeIDs);
	}

	private function __quickFilter($value) {
		$result = array();

		// get all of the attributes that have a hit on the search term, in either the value or the comment field
		// This is not perfect, the search will be case insensitive, but value1 and value 2 are searched separately. lower() doesn't seem to work on virtualfields
		$attributeHits = $this->Event->Attribute->find('all', array(
				'recursive' => -1,
				'fields' => array('event_id', 'comment', 'distribution', 'value1', 'value2'),
				'conditions' => array(
					'OR' => array(
						'lower(value1) LIKE' => '%' . strtolower($value) . '%',
						'lower(value2) LIKE' => '%' . strtolower($value) . '%',
						'lower(comment) LIKE' => '%' . strtolower($value) . '%',
					),
				),
		));
		// rearrange the data into an array where the keys are the event IDs
		$eventsWithAttributeHits = array();
		foreach ($attributeHits as $aH) {
			$eventsWithAttributeHits[$aH['Attribute']['event_id']][] = $aH['Attribute'];
		}

		// Using the keys from the previously obtained ordered array, let's fetch all of the events involved
		$events = $this->Event->find('all', array(
				'recursive' => -1,
				'fields' => array('id', 'distribution', 'org_id'),
				'conditions' => array('id' => array_keys($eventsWithAttributeHits)),
		));

		foreach ($events as $event) {
			$result[] = $event['Event']['id'];
		}

		// we now have a list of event IDs that match on an attribute level, and the user can see it. Let's also find all of the events that match on other criteria!
		// What is interesting here is that we no longer have to worry about the event's releasability. With attributes this was a different case,
		// because we might run into a situation where a user can see an event but not a specific attribute
		// returning a hit on such an attribute would allow users to enumerate hidden attributes
		// For anything beyond this point the default pagination restrictions will apply!

		// First of all, there are tags that might be interesting for us
		$tags = $this->Event->EventTag->Tag->find('all', array(
				'conditions' => array('lower(name) LIKE' => '%' . strtolower($value) . '%'),
				'fields' => array('name', 'id'),
				'contain' => array('EventTag'),
		));
		foreach ($tags as $tag) {
			foreach ($tag['EventTag'] as $eventTag) {
				if (!in_array($eventTag['event_id'], $result)) $result[] = $eventTag['event_id'];
			}
		}

		// Finally, let's search on the event metadata!
		$conditions = array();
		$orgs = $this->Event->Org->find('list', array(
				'conditions' => array('lower(name) LIKE' => '%' .  strtolower($value) . '%'),
				'recursive' => -1,
				'fields' => array('id')
		));
		if (!empty($orgs)) $conditions['OR']['orgc_id'] = array_values($orgs);
		$conditions['OR']['lower(info) LIKE'] = '%' . strtolower($value) .'%';
		$otherEvents = $this->Event->find('all', array(
				'recursive' => -1,
				'fields' => array('id', 'orgc_id', 'info'),
				'conditions' => $conditions,
		));
		foreach ($otherEvents as $oE) {
			if (!in_array($oE['Event']['id'], $result)) $result[] = $oE['Event']['id'];
		}
		return $result;
	}

	/**
	 * index method
	 *
	 * @return void
	 */
	public function index() {
		// list the events
		$passedArgsArray = array();
		$urlparams = "";
		$overrideAbleParams = array('all', 'attribute', 'published', 'eventid', 'Datefrom', 'Dateuntil', 'org', 'eventinfo', 'tag', 'distribution', 'analysis', 'threatlevel');
		$passedArgs = $this->passedArgs;
		if (isset($this->request->data)) {
			if (isset($this->request->data['request'])) $this->request->data = $this->request->data['request'];
			foreach ($overrideAbleParams as &$oap) {
				if (isset($this->request->data['search' . $oap])) $this->request->data[$oap] = $this->request->data['search' . $oap];
				if (isset($this->request->data[$oap])) $passedArgs['search' . $oap] = $this->request->data[$oap];
			}
		}
		$this->set('passedArgs', json_encode($passedArgs));
		// check each of the passed arguments whether they're a filter (could also be a sort for example) and if yes, add it to the pagination conditions
		foreach ($passedArgs as $k => $v) {
			if (substr($k, 0, 6) === 'search') {
				if ($urlparams != "") $urlparams .= "/";
				$urlparams .= $k . ":" . $v;
				$searchTerm = substr($k, 6);
				switch ($searchTerm) {
					case 'all' :
						if (!empty($passedArgs['searchall'])) $this->paginate['conditions']['AND'][] = array('Event.id' => $this->__quickFilter($passedArgs['searchall']));
						break;
					case 'attribute' :
						$event_id_arrays = $this->__filterOnAttributeValue($v);
						foreach ($event_id_arrays[0] as $event_id) $this->paginate['conditions']['AND']['OR'][] = array('Event.id' => $event_id);
						foreach ($event_id_arrays[1] as $event_id) $this->paginate['conditions']['AND'][] = array('Event.id !=' => $event_id);
						break;
					case 'published' :
						if ($v == 2) continue 2;
						$this->paginate['conditions']['AND'][] = array('Event.' . substr($k, 6) . ' =' => $v);
						break;
					case 'eventid':
						if ($v == "") continue 2;
						$pieces = explode('|', $v);
						$temp = array();
						foreach ($pieces as $piece) {
							$piece = trim($piece);
							if ($piece[0] == '!') {
								if (strlen($piece) == 37) {
									$this->paginate['conditions']['AND'][] = array('Event.uuid !=' => substr($piece, 1));
								} else {
									$this->paginate['conditions']['AND'][] = array('Event.id !=' => substr($piece, 1));
								}
							} else {
								if (strlen($piece) == 36) {
									$temp['OR'][] = array('Event.uuid' => $piece);
								} else {
									$temp['OR'][] = array('Event.id' => $piece);
								}
							}
						}
						$this->paginate['conditions']['AND'][] = $temp;
						break;
					case 'Datefrom' :
						if ($v == "") continue 2;
						$this->paginate['conditions']['AND'][] = array('Event.date >=' => $v);
						break;
					case 'Dateuntil' :
						if ($v == "") continue 2;
						$this->paginate['conditions']['AND'][] = array('Event.date <=' => $v);
						break;
					case 'org' :
						if ($v == "") continue 2;
						if (!Configure::read('MISP.showorg')) continue 2;
						$orgArray = $this->Event->Org->find('list', array('fields' => array('Org.name')));
						$orgArray = array_map('strtoupper', $orgArray);
						// if the first character is '!', search for NOT LIKE the rest of the string (excluding the '!' itself of course)
						$pieces = explode('|', $v);
						$test = array();
						foreach ($pieces as $piece) {
							if ($piece[0] == '!') {
								if (is_numeric(substr($piece, 1))) {
									$this->paginate['conditions']['AND'][] = array('Event.orgc_id !=' => substr($piece, 1));
								} else {
									$org_id = array_search(strtoupper(substr($piece, 1)), $orgArray);
									if ($org_id) $this->paginate['conditions']['AND'][] = array('Event.orgc_id !=' => $org_id);
								}
							} else {
								if (is_numeric($piece)) {
									$test['OR'][] = array('Event.orgc_id' => array('Event.orgc_id' => $piece));
								} else {
									$org_id = array_search(strtoupper($piece), $orgArray);
									if ($org_id) $test['OR'][] = array('Event.orgc_id' => $org_id);
									else $test['OR'][] = array('Event.orgc_id' => -1);
								}
							}
						}
						$this->paginate['conditions']['AND'][] = $test;
						break;
					case 'eventinfo' :
						if ($v == "") continue 2;
						// if the first character is '!', search for NOT LIKE the rest of the string (excluding the '!' itself of course)
						$pieces = explode('|', $v);
						$test = array();
						foreach ($pieces as $piece) {
							if ($piece[0] == '!') {
								$this->paginate['conditions']['AND'][] = array('lower(Event.info)' . ' NOT LIKE' => '%' . strtolower(substr($piece, 1)) . '%');
							} else {
								$test['OR'][] = array('lower(Event.info)' . ' LIKE' => '%' . strtolower($piece) . '%');
							}
						}
						$this->paginate['conditions']['AND'][] = $test;
						break;
					case 'tag' :
						if (!$v || !Configure::read('MISP.tagging') || $v === 0) continue 2;
						$pieces = explode('|', $v);
						$filterString = "";
						$expectOR = false;
						$setOR = false;
						foreach ($pieces as $piece) {
							if ($piece[0] == '!') {
								if (is_numeric(substr($piece, 1))) $conditions = array('OR' => array('Tag.id' => substr($piece, 1)));
								else $conditions = array('OR' => array('Tag.name' => substr($piece, 1)));

								$tagName = $this->Event->EventTag->Tag->find('first', array(
									'conditions' => $conditions,
									'fields' => array('id', 'name'),
									'recursive' => -1,
								));

								if (empty($tagName)) {
									if ($filterString != "") $filterString .= "|";
									$filterString .= '!' . $piece;
									continue;
								}


								$block = $this->Event->EventTag->find('all', array(
										'conditions' => array('EventTag.tag_id' => $tagName['Tag']['id']),
										'fields' => 'event_id',
										'recursive' => -1,
								));
								foreach ($block as $b) {
									$this->paginate['conditions']['AND'][] = array('Event.id !=' => $b['EventTag']['event_id']);
								}
								if ($filterString != "") $filterString .= "|";
								$filterString .= '!' . (isset($tagName['Tag']['name']) ? $tagName['Tag']['name'] : $piece);
							} else {
								$expectOR = true;
								if (is_numeric($piece)) $conditions = array('OR' => array('Tag.id' => $piece));
								else $conditions = array('OR' => array('Tag.name' => $piece));

								$tagName = $this->Event->EventTag->Tag->find('first', array(
										'conditions' => $conditions,
										'fields' => array('id', 'name'),
										'recursive' => -1,
								));

								if (empty($tagName)) {
									if ($filterString != "") $filterString .= "|";
									$filterString .= $piece;
									continue;
								}

								$allow = $this->Event->EventTag->find('all', array(
										'conditions' => array('EventTag.tag_id' => $tagName['Tag']['id']),
										'fields' => 'event_id',
										'recursive' => -1,
								));
								if (!empty($allow)) {
									foreach ($allow as $a) {
										$setOR = true;
										$this->paginate['conditions']['AND']['OR'][] = array('Event.id' => $a['EventTag']['event_id']);
									}
								}
								if ($filterString != "") $filterString .= "|";
								$filterString .= isset($tagName['Tag']['name']) ? $tagName['Tag']['name'] : $piece;
							}
						}
						// If we have a list of OR-d arguments, we expect to end up with a list of allowed event IDs
						// If we don't however, it means that none of the tags was found. To prevent displaying the entire event index in this case:
						if ($expectOR && !$setOR) $this->paginate['conditions']['AND'][] = array('Event.id' => -1);
						$v = $filterString;
						break;
					case 'distribution' :
					case 'analysis' :
					case 'threatlevel' :
						if ($v == "") continue 2;
						$terms = array();
						$filterString = "";
						$searchTermInternal = $searchTerm;
						if ($searchTerm == 'threatlevel') {
							$searchTermInternal = 'threat_level_id';
							$threatLevels = $this->Event->ThreatLevel->find('all', array(
								'recursive' => -1,
								'fields' => array('id', 'name'),
							));
							foreach ($threatLevels as &$tl) $terms[$tl['ThreatLevel']['id']] =$tl['ThreatLevel']['name'];
						} else if ($searchTerm == 'analysis') {
							$terms = $this->Event->analysisLevels;
						} else {
							$terms = $this->Event->distributionLevels;
						}
						$pieces = explode('|', $v);
						$test = array();
						foreach ($pieces as $piece) {
							if ($filterString != "") $filterString .= '|';
							if ($piece[0] == '!') {
								$filterString .= $terms[substr($piece, 1)];
								$this->paginate['conditions']['AND'][] = array('Event.' . $searchTermInternal . ' !=' => substr($piece, 1));
							} else {
								$filterString .= $terms[$piece];
								$test['OR'][] = array('Event.' . $searchTermInternal => $piece);
							}
						}
						$this->paginate['conditions']['AND'][] = $test;
						$v = $filterString;
						break;
					default:
						continue 2;
						break;
				}
				$passedArgsArray[$searchTerm] = $v;
			}
		}

		if (Configure::read('MISP.tagging') && !$this->_isRest()) {
			$this->Event->contain(array('User.email', 'EventTag' => array('Tag')));
			$tags = $this->Event->EventTag->Tag->find('all', array('recursive' => -1));
			$tagNames = array('None');
			foreach ($tags as $k => $v) {
				$tagNames[$v['Tag']['id']] = $v['Tag']['name'];
			}
			$this->set('tags', $tagNames);
		} else {
			$this->Event->contain('User.email');
		}
		$this->set('urlparams', $urlparams);
		$this->set('passedArgsArray', $passedArgsArray);
		$this->paginate = Set::merge($this->paginate, array('contain' => array(
			'ThreatLevel' => array(
				'fields' => array(
					'ThreatLevel.name'))
			),
		));
		// for rest, don't use the pagination. With this, we'll escape the limit of events shown on the index.
		if ($this->_isRest()) {
			$rules = array();
			$fieldNames = array_keys($this->Event->getColumnTypes());
			$directions = array('ASC', 'DESC');
			if (isset($passedArgs['sort']) && in_array($passedArgs['sort'], $fieldNames)) {
				if (isset($passedArgs['direction']) && in_array(strtoupper($passedArgs['direction']), $directions)) {
					$rules['order'] = array($passedArgs['sort'] => $passedArgs['direction']);
				} else {
					$rules['order'] = array($passedArgs['sort'] => 'ASC');
				}
			} else {
				$rules['order'] = array('Event.id' => 'DESC');
			}
			if (isset($passedArgs['limit'])) {
				$rules['limit'] = intval($passedArgs['limit']);
			}
			$rules['contain'] = $this->paginate['contain'];
			if (Configure::read('MISP.tagging')) {
				$rules['contain']['EventTag'] = array('Tag' => array('fields' => array('id', 'name', 'colour', 'exportable'), 'conditions' => array('Tag.exportable' => true)));
			}
			if (isset($this->paginate['conditions'])) $rules['conditions'] = $this->paginate['conditions'];
			$events = $this->Event->find('all', $rules);
			foreach ($events as $k => &$event) {
				foreach ($event['EventTag'] as $k2 => &$et) {
					if (empty($et['Tag'])) unset ($events[$k]['EventTag'][$k2]);
				}
				$event['EventTag'] = array_values($event['EventTag']);
			}
			$this->set('events', $events);
		} else {
			$events = $this->paginate();
			if (Configure::read('MISP.showCorrelationsOnIndex')) $this->Event->attachCorrelationCountToEvents($this->Auth->user(), $events);
			$this->set('events', $events);
		}

		if (!$this->Event->User->getPGP($this->Auth->user('id')) && Configure::read('GnuPG.onlyencrypted')) {
			$this->Session->setFlash(__('No GPG key set in your profile. To receive emails, submit your public key in your profile.'));
		} elseif ($this->Auth->user('autoalert') && !$this->Event->User->getPGP($this->Auth->user('id')) && Configure::read('GnuPG.bodyonlyencrypted')) {
			$this->Session->setFlash(__('No GPG key set in your profile. To receive attributes in emails, submit your public key in your profile.'));
		}
		$this->set('eventDescriptions', $this->Event->fieldDescriptions);
		$this->set('analysisLevels', $this->Event->analysisLevels);
		$this->set('distributionLevels', $this->Event->distributionLevels);
		$this->set('shortDist', $this->Event->shortDist);
		$this->set('ajax', $this->request->is('ajax'));
	}

	public function filterEventIndex() {
		$passedArgsArray = array();

		$filtering = array(
			'published' => 2,
			'org' => array('OR' => array(), 'NOT' => array()),
			'tag' => array('OR' => array(), 'NOT' => array()),
			'eventid' => array('OR' => array(), 'NOT' => array()),
			'date' => array('from' => "", 'until' => ""),
			'eventinfo' => array('OR' => array(), 'NOT' => array()),
			'threatlevel' => array('OR' => array(), 'NOT' => array()),
			'distribution' => array('OR' => array(), 'NOT' => array()),
			'analysis' => array('OR' => array(), 'NOT' => array()),
			'attribute' => array('OR' => array(), 'NOT' => array()),
		);

		foreach ($this->passedArgs as $k => $v) {
			if (substr($k, 0, 6) === 'search') {
				$searchTerm = substr($k, 6);
				switch ($searchTerm) {
					case 'published' :
						$filtering['published'] = $v;
						break;
					case 'Datefrom' :
						$filtering['date']['from'] = $v;
						break;
					case 'Dateuntil' :
						$filtering['date']['until'] = $v;
						break;
					case 'org' :
					case 'eventid' :
					case 'tag' :
					case 'eventinfo' :
					case 'attribute' :
					case 'threatlevel' :
					case 'distribution' :
					case 'analysis' :
						if ($v == "") continue 2;

						$pieces = explode('|', $v);
						foreach ($pieces as $piece) {
							if ($piece[0] == '!') {
								$filtering[$searchTerm]['NOT'][] = substr($piece, 1);
							} else {
								$filtering[$searchTerm]['OR'][] = $piece;
							}
						}
						break;
				}
				$passedArgsArray[$searchTerm] = $v;
			}
		}
		$this->set('filtering', json_encode($filtering));
		$tags = $this->Event->EventTag->Tag->find('all', array('recursive' => -1));
		$tagNames = array();
		$tagJSON = array();
		foreach ($tags as $k => $v) {
			$tagNames[$v['Tag']['id']] = $v['Tag']['name'];
			$tagJSON[] = array('id' => $v['Tag']['id'], 'value' => h($v['Tag']['name']));
		}
		$conditions = array();
		if (!$this->_isSiteAdmin()) {
			$eIds = $this->Event->fetchEventIds($this->Auth->user(), false, false, false, true);
			$conditions['AND'][] = array('Event.id' => $eIds);
		}
		$rules = array('published', 'eventid', 'tag', 'date', 'eventinfo', 'threatlevel', 'distribution', 'analysis', 'attribute');
		if (Configure::read('MISP.showorg')){
			$orgs = $this->Event->find('list', array(
					'recursive' => -1,
					'fields' => array('Orgc.name'),
					'contain' => array('Orgc'),
					'conditions' => $conditions,
					'group' => 'LOWER(Orgc.name)'
			));
			$this->set('showorg', true);
			$this->set('orgs', $this->_arrayToValuesIndexArray($orgs));
			$rules[] = 'org';
		} else {
			$this->set('showorg', false);
		}
		$rules = $this->_arrayToValuesIndexArray($rules);
		$this->set('tags', $tagNames);
		$this->set('tagJSON', json_encode($tagJSON));
		$this->set('rules', $rules);
		$this->set('baseurl', Configure::read('MISP.baseurl'));
		$this->layout = 'ajax';
	}

	public function viewEventAttributes($id, $all = false) {
		$results = $this->Event->fetchEvent($this->Auth->user(), array('eventid' => $id));
		if (empty($results)) throw new NotFoundException('Invalid event');
		$event = &$results[0];
		$params = $this->Event->rearrangeEventForView($event, $this->passedArgs, $all);
		$this->params->params['paging'] = array($this->modelClass => $params);
		$this->set('event', $event);
		$dataForView = array(
				'Attribute' => array('attrDescriptions', 'typeDefinitions', 'categoryDefinitions', 'distributionDescriptions', 'distributionLevels', 'shortDist'),
				'Event' => array('fieldDescriptions')
		);
		foreach ($dataForView as $m => $variables) {
			if ($m === 'Event') $currentModel = $this->Event;
			else if ($m === 'Attribute') $currentModel = $this->Event->Attribute;
			foreach ($variables as $variable) {
				$this->set($variable, $currentModel->{$variable});
			}
		}
		if (Configure::read('Plugin.Enrichment_services_enable')) {
			$this->loadModel('Server');
			$modules = $this->Server->getEnabledModules();
			$this->set('modules', $modules);
		}
		$this->set('typeGroups', array_keys($this->Event->Attribute->typeGroupings));
		$this->disableCache();
		$this->layout = 'ajax';
		$this->set('currentUri', $this->params->here);
		$this->render('/Elements/eventattribute');
	}

	private function __viewUI($event, $continue, $fromEvent) {
		if (isset($this->params['named']['attributesPage'])) $page = $this->params['named']['attributesPage'];
		else $page = 1;
		// set the data for the contributors / history field
		$org_ids = $this->Event->ShadowAttribute->getEventContributors($event['Event']['id']);
		$contributors = $this->Event->Org->find('list', array('fields' => array('Org.name'), 'conditions' => array('Org.id' => $org_ids)));

		if ($this->userRole['perm_publish'] && $event['Event']['orgc_id'] == $this->Auth->user('org_id')) {
			$proposalStatus = false;
			if (isset($event['ShadowAttribute']) && !empty($event['ShadowAttribute'])) $proposalStatus = true;
			if (!$proposalStatus && !empty($event['Attribute'])) {
				foreach ($event['Attribute'] as &$temp) {
					if (isset($temp['ShadowAttribute']) && !empty($temp['ShadowAttribute'])) {
						$proposalStatus = true;
						continue;
					}
				}
			}
			if ($proposalStatus && empty($this->Session->read('Message'))) $this->Session->setFlash('This event has active proposals for you to accept or discard.');
		}
		// set the pivot data
		$this->helpers[] = 'Pivot';
		if ($continue) {
			$data = $this->__continuePivoting($event['Event']['id'], $event['Event']['info'], $event['Event']['date'], $fromEvent);
		} else {
			$data = $this->__startPivoting($event['Event']['id'], $event['Event']['info'], $event['Event']['date']);
		}
		$pivot = $this->Session->read('pivot_thread');
		$this->__arrangePivotVertical($pivot);
		$this->__setDeletable($pivot, $event['Event']['id'], true);
		$this->set('allPivots', $this->Session->read('pivot_thread'));
		$this->set('pivot', $pivot);

		// set data for the view, the event is already set in view()
		$dataForView = array(
				'Attribute' => array('attrDescriptions' => 'fieldDescriptions', 'distributionDescriptions' => 'distributionDescriptions', 'distributionLevels' => 'distributionLevels', 'shortDist' => 'shortDist'),
				'Event' => array('eventDescriptions' => 'fieldDescriptions', 'analysisDescriptions' => 'analysisDescriptions', 'analysisLevels' => 'analysisLevels')
		);
		foreach ($dataForView as $m => $variables) {
			if ($m === 'Event') $currentModel = $this->Event;
			else if ($m === 'Attribute') $currentModel = $this->Event->Attribute;
			foreach ($variables as $alias => $variable) {
				$this->set($alias, $currentModel->{$variable});
			}
		}
		$params = $this->Event->rearrangeEventForView($event);
		$this->params->params['paging'] = array($this->modelClass => $params);
		$this->set('event', $event);
		$dataForView = array(
				'Attribute' => array('attrDescriptions', 'typeDefinitions', 'categoryDefinitions', 'distributionDescriptions', 'distributionLevels'),
				'Event' => array('fieldDescriptions')
		);
		foreach ($dataForView as $m => $variables) {
			if ($m === 'Event') $currentModel = $this->Event;
			else if ($m === 'Attribute') $currentModel = $this->Event->Attribute;
			foreach ($variables as $variable) {
				$this->set($variable, $currentModel->{$variable});
			}
		}
		if (Configure::read('MISP.delegation')) {
			$this->loadModel('EventDelegation');
			$delegationConditions = array('EventDelegation.event_id' => $event['Event']['id']);
			if (!$this->_isSiteAdmin() && $this->userRole['perm_publish']) $delegationConditions['OR'] = array('EventDelegation.org_id' => $this->Auth->user('org_id'), 'EventDelegation.requester_org_id' => $this->Auth->user('org_id'));
			$this->set('delegationRequest', $this->EventDelegation->find('first', array('conditions' => $delegationConditions, 'recursive' => -1, 'contain' => array('Org', 'RequesterOrg'))));
		}

		if (Configure::read('Plugin.Enrichment_services_enable')) {
			$this->loadModel('Server');
			$modules = $this->Server->getEnabledModules();
			$this->set('modules', $modules);
		}
		$this->set('contributors', $contributors);
		$this->set('typeGroups', array_keys($this->Event->Attribute->typeGroupings));
	}

	/**
	 * view method
	 *
	 * @param int $id
	 * @return void
	 * @throws NotFoundException
	 */

	public function view($id = null, $continue=false, $fromEvent=null) {
		// If the length of the id provided is 36 then it is most likely a Uuid - find the id of the event, change $id to it and proceed to read the event as if the ID was entered.
		$perm_publish = $this->userRole['perm_publish'];
		if (strlen($id) == 36) {
			$this->Event->recursive = -1;
			$temp = $this->Event->findByUuid($id);
			if ($temp == null) throw new NotFoundException('Invalid event');
			$id = $temp['Event']['id'];
		}

		$this->Event->id = $id;
		if(!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event.'));
		}

		$conditions = array('eventid' => $id);
		if (!$this->_isRest()) {
			$conditions['includeAllTags'] = true;
		} else {
			$conditions['includeAttachments'] = true;
		}
		$results = $this->Event->fetchEvent($this->Auth->user(), $conditions);
		if (empty($results)) throw new NotFoundException('Invalid event');
		$event = &$results[0];
		if ($this->_isRest()) {
			$this->set('event', $event);
		}
		if (!$this->_isRest()) $this->__viewUI($event, $continue, $fromEvent);
	}

	private function __startPivoting($id, $info, $date){
		$this->Session->write('pivot_thread', null);
		$initial_pivot = array('id' => $id, 'info' => $info, 'date' => $date, 'depth' => 0, 'height' => 0, 'children' => array(), 'deletable' => true);
		$this->Session->write('pivot_thread', $initial_pivot);
	}

	private function __continuePivoting($id, $info, $date, $fromEvent){
		$pivot = $this->Session->read('pivot_thread');
		$newPivot = array('id' => $id, 'info' => $info, 'date' => $date, 'depth' => null, 'children' => array(), 'deletable' => true);
		if (!$this->__checkForPivot($pivot, $id)) {
			$pivot = $this->__insertPivot($pivot, $fromEvent, $newPivot, 0);
		}
		$this->Session->write('pivot_thread', $pivot);
	}

	private function __insertPivot($pivot, $oldId, $newPivot, $depth) {
		$depth++;
		if ($pivot['id'] == $oldId) {
			$newPivot['depth'] = $depth;
			$pivot['children'][] = $newPivot;
			return $pivot;
		}
		foreach($pivot['children'] as $k => $v) {
			$pivot['children'][$k] = $this->__insertPivot($v, $oldId, $newPivot, $depth);
		}
		return $pivot;
	}

	private function __checkForPivot($pivot, $id) {
		if ($id == $pivot['id']) return true;
		foreach ($pivot['children'] as $k => $v) {
			if ($this->__checkForPivot($v, $id)) {
				return true;
			}
		}
		return false;
	}

	private function __arrangePivotVertical(&$pivot) {
		if (empty($pivot)) return null;
		$max = count($pivot['children']) - 1;
		if ($max < 0) $max = 0;
		$temp = 0;
		$pivot['children'] = array_values($pivot['children']);
		foreach ($pivot['children'] as $k => $v) {
			$pivot['children'][$k]['height'] = ($temp+$k)*50;
			$temp += $this->__arrangePivotVertical($pivot['children'][$k]);
			if ($k == $max) $temp = $pivot['children'][$k]['height'] / 50;
		}
		return $temp;
	}

	public function removePivot($id, $eventId, $self = false) {
		$pivot = $this->Session->read('pivot_thread');
		if ($pivot['id'] == $id) {
			$pivot = null;
			$this->Session->write('pivot_thread', null);
			$this->redirect(array('controller' => 'events', 'action' => 'view', $eventId));
		} else {
			$pivot = $this->__doRemove($pivot, $id);
		}
		$this->Session->write('pivot_thread', $pivot);
		$pivot = $this->__arrangePivotVertical($pivot);
		$this->redirect(array('controller' => 'events', 'action' => 'view', $eventId, true, $eventId));
	}

	private function __removeChildren(&$pivot, $id) {
		if ($pivot['id'] == $id) {
			$pivot['children'] = array();
		} else {
			foreach ($pivot['children'] as $k => $v) {
				$this->__removeChildren($v, $id);
			}
		}
	}

	private function __doRemove(&$pivot, $id) {
		foreach ($pivot['children'] as $k => $v) {
			if ($v['id'] == $id) {
				unset ($pivot['children'][$k]);
				return $pivot;
			} else {
				$pivot['children'][$k] = $this->__doRemove($pivot['children'][$k], $id);
			}
		}
		return $pivot;
	}

	private function __setDeletable(&$pivot, $id, $root=false) {
		if ($pivot['id'] == $id && !$root) {
			$pivot['deletable'] = false;
			return true;
		}
		$containsCurrent = false;
		foreach ($pivot['children'] as $k => $v) {
			$containsCurrent = $this->__setDeletable($pivot['children'][$k], $id);
			if ($containsCurrent && !$root) $pivot['deletable'] = false;
		}
		return !$pivot['deletable'];
	}

	/**
	 * add method
	 *
	 * @return void
	 */
	public function add() {
		if (!$this->userRole['perm_add']) {
			throw new MethodNotAllowedException('You don\'t have permissions to create events');
		}
		if ($this->userRole['perm_sync']) $sguuids = $this->Event->SharingGroup->fetchAllAuthorised($this->Auth->user(), 'uuid',  1);
		$sgs = $this->Event->SharingGroup->fetchAllAuthorised($this->Auth->user(), 'name',  1);
		if ($this->request->is('post')) {
			if ($this->_isRest()) {

				// rearrange the response if the event came from an export
				if(isset($this->request->data['response'])) $this->request->data = $this->request->data['response'];

				// Distribution, reporter for the events pushed will be the owner of the authentication key
				$this->request->data['Event']['user_id'] = $this->Auth->user('id');
			}
			if (!empty($this->data)) {
				$ext = '';
				if (isset($this->data['Event']['submittedgfi'])) {
					App::uses('File', 'Utility');
					$file = new File($this->data['Event']['submittedgfi']['name']);
					$ext = $file->ext();
				}
				if (isset($this->data['Event']['submittedgfi']) && ($ext != 'zip') && $this->data['Event']['submittedgfi']['size'] > 0 &&
						is_uploaded_file($this->data['Event']['submittedgfi']['tmp_name'])) {
					$this->Session->setFlash(__('You may only upload GFI Sandbox zip files.'));
				} else {
					// If the distribution is set to sharing group, check if the id provided is really visible to the user, if not throw an error.
					if ($this->request->data['Event']['distribution'] == 4) {
						if ($this->userRole['perm_sync'] && $this->_isRest()) {
							if (!$this->Event->SharingGroup->checkIfAuthorisedToSave($this->Auth->user(), $this->request->data['Event']['SharingGroup'])) throw new MethodNotAllowedException('Invalid Sharing Group or not authorised. (Sync user is not contained in the Sharing group)');
						} else {
							if (!isset($sgs[$this->request->data['Event']['sharing_group_id']])) throw new MethodNotAllowedException('Invalid Sharing Group or not authorised.');
						}
					} else {
						// If the distribution is set to something "traditional", set the SG id to 0.
						$this->request->data['Event']['sharing_group_id'] = 0;
					}
					if ($this->_isRest()) {
						// $this->request->data = $this->Event->updateXMLArray($this->request->data, false);
						if (isset($this->request->data['Event']['orgc_id']) && !$this->userRole['perm_sync']) {
							$this->request->data['Event']['orgc_id'] = $this->Auth->user('org_id');
							if (isset($this->request->data['Event']['Orgc'])) unset($this->request->data['Event']['Orgc']);
						}
					}
					$validationErrors = array();
					$created_id = 0;
					$add = $this->Event->_add($this->request->data, $this->_isRest(), $this->Auth->user(), '', null, false, null, $created_id, $validationErrors);
					if ($add === true && !is_numeric($add)) {
						if ($this->_isRest()) {
							if ($add === 'blocked') {
								throw new ForbiddenException('Event blocked by local blacklist.');
							}
							// REST users want to see the newly created event
							// REST users want to see the newly created event
							$results = $this->Event->fetchEvent($this->Auth->user(), array('eventid' => $created_id));
							$event = &$results[0];
							if (!empty($validationErrors)) {
								$event['errors'] = $validationErrors;
							}
							$this->set('event', $event);
							$this->render('view');
							return true;
						} else {
							// TODO now save uploaded attributes using $this->Event->getId() ..
							if (isset($this->data['Event']['submittedgfi'])) $this->_addGfiZip($this->Event->getId());

							// redirect to the view of the newly created event
							if (!CakeSession::read('Message.flash')) {
								$this->Session->setFlash(__('The event has been saved'));
							} else {
								$existingFlash = CakeSession::read('Message.flash');
								$this->Session->setFlash(__('The event has been saved. ' . $existingFlash['message']));
							}
							$this->redirect(array('action' => 'view', $this->Event->getId()));
						}
					} else {
						if ($this->_isRest()) { // TODO return error if REST
							if(is_numeric($add)) {
								$this->response->header('Location', Configure::read('MISP.baseurl') . '/events/' . $add);
								$this->response->send();
								throw new NotFoundException('Event already exists, if you would like to edit it, use the url in the location header.');
							}
							$this->set('name', 'Add event failed.');
							$this->set('message', 'The event could not be saved.');
							$this->set('errors', $validationErrors);
							$this->set('url', '/events/add');
							$this->set('_serialize', array('name', 'message', 'url', 'errors'));
							return false;
						} else {
							if ($add === 'blocked') $this->Session->setFlash('A blacklist entry is blocking you from creating any events. Please contact the administration team of this instance' . (Configure::read('MISP.contact') ? ' at ' . Configure::read('MISP.contact') : '') . '.');
							else $this->Session->setFlash(__('The event could not be saved. Please, try again.'), 'default', array(), 'error');
						}
					}
				}
			}
		}

		$this->request->data['Event']['date'] = date('Y-m-d');

		// combobox for distribution
		$distributions = array_keys($this->Event->distributionDescriptions);
		$distributions = $this->_arrayToValuesIndexArray($distributions);
		$this->set('distributions', $distributions);
		// tooltip for distribution
		$this->set('distributionDescriptions', $this->Event->distributionDescriptions);
		$distributionLevels = $this->Event->distributionLevels;
		if (empty($sgs)) unset ($distributionLevels[4]);
		$this->set('distributionLevels', $distributionLevels);

		// combobox for risks
		$threat_levels = $this->Event->ThreatLevel->find('all');
		$this->set('threatLevels', Set::combine($threat_levels, '{n}.ThreatLevel.id', '{n}.ThreatLevel.name'));
		$this->set('riskDescriptions', Set::combine($threat_levels, '{n}.ThreatLevel.id', '{n}.ThreatLevel.form_description'));

		// combobox for analysis
		$analysiss = $this->Event->validate['analysis']['rule'][1];
		$analysiss = $this->_arrayToValuesIndexArray($analysiss);
		$this->set('sharingGroups', $sgs);
		$this->set('analysiss',$analysiss);
		// tooltip for analysis
		$this->set('analysisDescriptions', $this->Event->analysisDescriptions);
		$this->set('analysisLevels', $this->Event->analysisLevels);

		$this->set('eventDescriptions', $this->Event->fieldDescriptions);
	}

	public function addIOC($id) {
		$this->Event->recursive = -1;
		$this->Event->read(null, $id);
		if (!$this->_isSiteAdmin() && ($this->Event->data['Event']['orgc_id'] != $this->_checkOrg() || !$this->userRole['perm_modify'])) {
			throw new UnauthorizedException('You do not have permission to do that.');
		}
		if ($this->request->is('post')) {
			if (!empty($this->data)) {
				$ext = '';
				if (isset($this->data['Event']['submittedioc'])) {
					App::uses('File', 'Utility');
					$file = new File($this->data['Event']['submittedioc']['name']);
					$ext = $file->ext();
				}
				if (isset($this->data['Event']['submittedioc'])) $this->_addIOCFile($id);

				// redirect to the view of the newly created event
				if (!CakeSession::read('Message.flash')) {
					$this->Session->setFlash(__('The event has been saved'));
				} else {
					$existingFlash = CakeSession::read('Message.flash');
					$this->Session->setFlash(__('The event has been saved. ' . $existingFlash['message']));
				}
			}
		}
		// set the id
		$this->set('id', $id);
		// set whether it is published or not
		$this->set('published', $this->Event->data['Event']['published']);
	}

	public function add_misp_export() {
		if (!$this->userRole['perm_modify']) {
			throw new UnauthorizedException('You do not have permission to do that.');
		}
		if ($this->request->is('post')) {
			if (!empty($this->data)) {
				$ext = '';
				if (isset($this->data['Event']['submittedfile'])) {
					App::uses('File', 'Utility');
					$file = new File($this->data['Event']['submittedfile']['name']);
					$ext = $file->ext();
				}
				if (isset($this->data['Event']['submittedfile']) && ($ext != 'xml' && $ext != 'json') && $this->data['Event']['submittedfile']['size'] > 0 &&
				is_uploaded_file($this->data['Event']['submittedxml']['tmp_name'])) {
					$this->Session->setFlash(__('You may only upload MISP XML or MISP JSON files.'));
				}
				if (isset($this->data['Event']['submittedfile'])) {
					if (Configure::read('MISP.take_ownership_xml_import')
						&& (isset($this->data['Event']['takeownership']) && $this->data['Event']['takeownership'] == 1)) {
						$results = $this->_addMISPExportFile($ext, true);
					} else {
						$results = $this->_addMISPExportFile($ext);
					}
				}
			}
			$this->set('results', $results);
			$this->render('add_misp_export_result');
		}
	}

	private function __searchUuidInAttributeArray($uuid, &$attr_array) {
		foreach ($attr_array['Attribute'] as &$attr) {
			if ($attr['uuid'] == $uuid)	return array('Attribute' => $attr);
		}
		return false;
	}

	/**
	 * edit method
	 *
	 * @param int $id
	 * @return void
	 * @throws NotFoundException
	 */
	public function edit($id = null) {
		$this->Event->id = $id;
		$date = new DateTime();
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event'));
		}
		$this->Event->read(null, $id);
		// check for if private and user not authorised to edit, go away
		if (!$this->_isSiteAdmin() && !($this->userRole['perm_sync'] && $this->_isRest())) {
			if (($this->Event->data['Event']['orgc_id'] != $this->_checkOrg()) || !($this->userRole['perm_modify'])) {
				$this->Session->setFlash(__('You are not authorised to do that. Please considering using the propose attribute feature.'));
				$this->redirect(array('controller' => 'events', 'action' => 'index'));
			}
		}
		if ($this->request->is('post') || $this->request->is('put')) {
			if ($this->_isRest()) {
				if ($this->_isRest()) {
					if (isset($this->request->data['response'])) $this->request->data = $this->Event->updateXMLArray($this->request->data, true);
					else $this->request->data = $this->Event->updateXMLArray($this->request->data, false);
				}
				// Workaround for different structure in XML/array than what CakePHP expects
				if (isset($this->request->data['response'])) $this->request->data = $this->request->data['response'];
				$result = $this->Event->_edit($this->request->data, $this->Auth->user(), $id);
				if ($result === true) {
					// REST users want to see the newly created event
					$results = $this->Event->fetchEvent($this->Auth->user(), array('eventid' => $id));
					$event = &$results[0];
					$this->set('event', $event);
					$this->render('view');
					return true;
				} else {
					$message = 'Error';
					if ($this->_isRest()) {
						App::uses('JSONConverterTool', 'Tools');
						$converter = new JSONConverterTool();
						if (isset($result['error'])) $errors = $result['error'];
						else $errors = $converter->arrayPrinter($result);
						$this->set('name', 'Edit event failed.');
						$this->set('message', $message);
						$this->set('errors', $errors);
						$this->set('url', '/events/edit/' . $id);
						$this->set('_serialize', array('name', 'message', 'url', 'errors'));
					} else {
						$this->set(array('message' => $message,'_serialize' => array('message')));	// $this->Event->validationErrors
						$this->render('edit');
					}
					return false;
				}
			}
			// say what fields are to be updated
			$fieldList = array('date', 'threat_level_id', 'analysis', 'info', 'published', 'distribution', 'timestamp', 'sharing_group_id');

			$this->Event->read();
			// always force the org, but do not force it for admins
			if (!$this->_isSiteAdmin()) {
				// set the same org as existed before
				$this->request->data['Event']['org_id'] = $this->Event->data['Event']['org_id'];
			}
			// we probably also want to remove the published flag
			$this->request->data['Event']['published'] = 0;
			$date = new DateTime();
			$this->request->data['Event']['timestamp'] = $date->getTimestamp();
			if ($this->Event->save($this->request->data, true, $fieldList)) {
				$this->Session->setFlash(__('The event has been saved'));
				$this->redirect(array('action' => 'view', $id));
			} else {
				$this->Session->setFlash(__('The event could not be saved. Please, try again.'));
			}
		} else {
			if(!$this->userRole['perm_modify']) $this->redirect(array('controller' => 'events', 'action' => 'index', 'admin' => false));
			$this->request->data = $this->Event->read(null, $id);
		}

		// combobox for distribution
		$distributions = array_keys($this->Event->distributionDescriptions);
		$distributions = $this->_arrayToValuesIndexArray($distributions);
		$this->set('distributions', $distributions);

		// tooltip for distribution
		$this->set('distributionDescriptions', $this->Event->distributionDescriptions);

		// even if the SG is not local, we still want the option to select the currently assigned SG
		$sgs = $this->Event->SharingGroup->fetchAllAuthorised($this->Auth->user(), 'name',  1);
		$this->set('sharingGroups', $sgs);

		$distributionLevels = $this->Event->distributionLevels;
		if (empty($sgs)) unset ($distributionLevels[4]);
		$this->set('distributionLevels', $distributionLevels);

		// combobox for types
		$threat_levels = $this->Event->ThreatLevel->find('all');
		$this->set('threatLevels', Set::combine($threat_levels, '{n}.ThreatLevel.id', '{n}.ThreatLevel.name'));
		$this->set('riskDescriptions', Set::combine($threat_levels, '{n}.ThreatLevel.id', '{n}.ThreatLevel.form_description'));

		// combobox for analysis
		$analysiss = $this->Event->validate['analysis']['rule'][1];
		$analysiss = $this->_arrayToValuesIndexArray($analysiss);
		$this->set('analysiss',$analysiss);

		// tooltip for analysis
		$this->set('analysisDescriptions', $this->Event->analysisDescriptions);
		$this->set('analysisLevels', $this->Event->analysisLevels);

		$this->set('eventDescriptions', $this->Event->fieldDescriptions);
		$this->set('event', $this->Event->data);
	}

	/**
	 * delete method
	 *
	 * @param int $id
	 * @return void
	 * @throws MethodNotAllowedException
	 * @throws NotFoundException
	 */

	public function delete($id = null) {
		if (!$this->request->is('post') && !$this->_isRest()) {
			throw new MethodNotAllowedException();
		}

		$this->Event->id = $id;
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event'));
		}

		// find the uuid
		$result = $this->Event->findById($id);
		$uuid = $result['Event']['uuid'];
		$this->Event->read();

		if (!$this->_isSiteAdmin()) {
			if ($this->Event->data['Event']['orgc_id'] != $this->_checkOrg() || !$this->userRole['perm_modify']) {
				throw new MethodNotAllowedException();
			}
		}
		if ($this->Event->delete()) {
			if ($this->_isRest() || $this->response->type() === 'application/json') {
				$this->set('message', 'Event deleted.');
				$this->set('_serialize', array('message'));
			} else {
				// delete the event from remote servers
				//if ('true' == Configure::read('MISP.sync')) {	// TODO test..(!$this->_isRest()) &&
				//	$this->__deleteEventFromServers($uuid);
				//}
				$this->Session->setFlash(__('Event deleted'));

				// if coming from index, redirect to referer (to have the filter working)
				// else redirect to index
				if (strpos($this->referer(), '/view') !== FALSE)
					$this->redirect(array('action' => 'index'));
				else
					$this->redirect($this->referer(array('action' => 'index')));
			}
		} else {
			if ($this->_isRest() || $this->response->type() === 'application/json') {
				throw new Exception('Event was not deleted');
			} else {
				$this->Session->setFlash(__('Event was not deleted'));
				$this->redirect(array('action' => 'index'));
			}
		}
	}

	/**
	 * Delets this specific event to all remote servers
	 * TODO move this to a component(?)
	 */
	private function __deleteEventFromServers($uuid) {
		// get a list of the servers
		$this->loadModel('Server');
		$servers = $this->Server->find('all', array(
				'conditions' => array('Server.push' => true)
		));

		// iterate over the servers and upload the event
		if(empty($servers))
			return;

		App::uses('SyncTool', 'Tools');
		foreach ($servers as &$server) {
			$syncTool = new SyncTool();
			$HttpSocket = $syncTool->setupHttpSocket($server);
			$this->Event->deleteEventFromServer($uuid, $server, $HttpSocket);
		}
	}

	/**
	 * Publishes the event without sending an alert email
	 *
	 * @throws NotFoundException
	 */
	public function publish($id = null) {
		$this->Event->id = $id;
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event'));
		}
		// update the event and set the from field to the current instance's organisation from the bootstrap. We also need to save id and info for the logs.
		$this->Event->recursive = -1;
		$event = $this->Event->read(null, $id);
		if (!$this->_isSiteAdmin()) {
			if (!$this->userRole['perm_publish'] || $this->Auth->user('org_id') !== $this->Event->data['Event']['orgc_id']) {
				throw new MethodNotAllowedException('You don\'t have the permission to do that.');
			}
		}
		// only allow form submit CSRF protection.
		if ($this->request->is('post') || $this->request->is('put')) {
			// Performs all the actions required to publish an event
			$result = $this->Event->publishRouter($id, null, $this->Auth->user());
			if (!Configure::read('MISP.background_jobs')) {
				if (!is_array($result)) {
					// redirect to the view event page
					$this->Session->setFlash(__('Event published without alerts.', true));
				} else {
					$lastResult = array_pop($result);
					$resultString = (count($result) > 0) ? implode(', ', $result) . ' and ' . $lastResult : $lastResult;
					$this->Session->setFlash(__(sprintf('Event published but not pushed to %s, re-try later. If the issue persists, make sure that the correct sync user credentials are used for the server link and that the sync user on the remote server has authentication privileges.', $resultString), true));
				}
			} else {
				// update the DB to set the published flag
				// for background jobs, this should be done already
				$fieldList = array('published', 'id', 'info', 'publish_timestamp');
				$event['Event']['published'] = 1;
				$event['Event']['publish_timestamp'] = time();
				$this->Event->save($event, array('fieldList' => $fieldList));
				$this->Session->setFlash(__('Job queued.'));
			}
			$this->redirect(array('action' => 'view', $id));
		} else {
			$this->set('id', $id);
			$this->set('type', 'publish');
			$this->render('ajax/eventPublishConfirmationForm');
		}
	}

	/**
	 * Send out an alert email to all the users that wanted to be notified.
	 * Users with a GPG key will get the mail encrypted, other users will get the mail unencrypted
	 *
	 * @throws NotFoundException
	 */
	public function alert($id = null) {
		$this->Event->id = $id;
		$this->Event->recursive = 0;
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event'));
		}
		$this->Event->recursive = -1;
		$this->Event->read(null, $id);
		if (!$this->_isSiteAdmin()) {
			if (!$this->userRole['perm_publish'] || $this->Auth->user('org_id') !== $this->Event->data['Event']['orgc_id']) {
				throw new MethodNotAllowedException('You don\'t have the permission to do that.');
			}
		}
		// only allow form submit CSRF protection.
		if ($this->request->is('post') || $this->request->is('put')) {
			// send out the email
			$emailResult = $this->Event->sendAlertEmailRouter($id, $this->Auth->user());
			if (is_bool($emailResult) && $emailResult == true) {
				// Performs all the actions required to publish an event
				$result = $this->Event->publishRouter($id, null, $this->Auth->user());
				if (!is_array($result)) {

					// redirect to the view event page
					if (Configure::read('MISP.background_jobs')) {
						$this->Session->setFlash(__('Job queued.', true));
					} else {
						$this->Session->setFlash(__('Email sent to all participants.', true));
					}
				} else {
					$lastResult = array_pop($result);
					$resultString = (count($result) > 0) ? implode(', ', $result) . ' and ' . $lastResult : $lastResult;
					$this->Session->setFlash(__(sprintf('Not published given no connection to %s but email sent to all participants.', $resultString), true));
				}
			} elseif (!is_bool($emailResult)) {
				// Performs all the actions required to publish an event
				$result = $this->Event->publishRouter($id, null, $this->Auth->user());
				if (!is_array($result)) {

					// redirect to the view event page
					$this->Session->setFlash(__('Published but no email sent given GnuPG is not configured.', true));
				} else {
					$lastResult = array_pop($result);
					$resultString = (count($result) > 0) ? implode(', ', $result) . ' and ' . $lastResult : $lastResult;
					$this->Session->setFlash(__(sprintf('Not published given no connection to %s but no email sent given GnuPG is not configured.', $resultString), true));
				}
			} else {
				$this->Session->setFlash(__('Sending of email failed', true), 'default', array(), 'error');
			}
			$this->redirect(array('action' => 'view', $id));
		} else {
			$this->set('id', $id);
			$this->set('type', 'alert');
			$this->render('ajax/eventPublishConfirmationForm');
		}
	}

	/**
	 * Send out an contact email to the person who posted the event.
	 * Users with a GPG key will get the mail encrypted, other users will get the mail unencrypted
	 *
	 * @throws NotFoundException
	 */
	public function contact($id = null) {
		$this->Event->id = $id;
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event'));
		}

		// User has filled in his contact form, send out the email.
		if ($this->request->is('post') || $this->request->is('put')) {
			$message = $this->request->data['Event']['message'];
			$creator_only = $this->request->data['Event']['person'];
			$user = $this->Auth->user();
			$user['gpgkey'] = $this->Event->User->getPGP($user['id']);
			if ($this->Event->sendContactEmailRouter($id, $message, $creator_only, $user, $this->_isSiteAdmin())) {
				// redirect to the view event page
				$this->Session->setFlash(__('Email sent to the reporter.', true));
			} else {
				$this->Session->setFlash(__('Sending of email failed', true), 'default', array(), 'error');
			}
			$this->redirect(array('action' => 'view', $id));
		}
		// User didn't see the contact form yet. Present it to him.
		if (empty($this->data)) {
			$this->data = $this->Event->read(null, $id);
		}
	}

	public function automation() {
		// Simply display a static view
		if (!$this->userRole['perm_auth']) {
			$this->redirect(array('controller' => 'events', 'action' => 'index'));
		}
		// generate the list of Attribute types
		$this->loadModel('Attribute');
		$this->set('sigTypes', array_keys($this->Attribute->typeDefinitions));
		$this->loadModel('Server');
		$rpzSettings = $this->Server->retrieveCurrentSettings('Plugin', 'RPZ_');
		$this->set('rpzSettings', $rpzSettings);
		$this->set('hashTypes', array_keys($this->Event->Attribute->hashTypes));
	}

	public function export() {
		// Check if the background jobs are enabled - if not, fall back to old export page.
		if (Configure::read('MISP.background_jobs')) {
			$now = time();

			// as a site admin we'll use the ADMIN identifier, not to overwrite the cached files of our own org with a file that includes too much data.
			if ($this->_isSiteAdmin()) {
				$useOrg = 'ADMIN';
				$useOrg_id = 0;
				$conditions = null;
			} else {
				$useOrg = $this->Auth->User('Organisation')['name'];
				$useOrg_id = $this->Auth->User('org_id');
				$conditions['OR'][] = array('id' => $this->Event->fetchEventIds($this->Auth->user, false, false, true, true));
			}
			$this->Event->recursive = -1;
			$newestEvent = $this->Event->find('first', array(
				'conditions' => $conditions,
				'fields' => 'timestamp',
				'order' => 'Event.timestamp DESC',
			));
			$this->loadModel('Job');
			foreach ($this->Event->export_types as $k => $type) {
				$job = $this->Job->find('first', array(
						'fields' => array('id', 'progress'),
						'conditions' => array(
								'job_type' => 'cache_' . $k,
								'org_id' => $useOrg_id
							),
						'order' => array('Job.id' => 'desc')
				));
				$dir = new Folder(APP . 'tmp/cached_exports/' . $k);
				if ($k === 'text') {
					// Since all of the text export files are generated together, we might as well just check for a single one md5.
					$file = new File($dir->pwd() . DS . 'misp.text_md5.' . $useOrg . $type['extension']);
				} else {
					$file = new File($dir->pwd() . DS . 'misp.' . $k . '.' . $useOrg . $type['extension']);
				}
				if (!$file->exists()) {
					$lastModified = 'N/A';
					$this->Event->export_types[$k]['recommendation'] = 1;
				} else {
					$fileChange = $file->lastChange();
					$lastModified = $this->__timeDifference($now, $fileChange);
					if ($fileChange > $newestEvent['Event']['timestamp']) {
						$this->Event->export_types[$k]['recommendation'] = 0;
					} else {
						$this->Event->export_types[$k]['recommendation'] = 1;
					}
				}

				$this->Event->export_types[$k]['lastModified'] = $lastModified;
				if (!empty($job)) {
					$this->Event->export_types[$k]['job_id'] = $job['Job']['id'];
					$this->Event->export_types[$k]['progress'] = $job['Job']['progress'];
				} else {
					$this->Event->export_types[$k]['job_id'] = -1;
					$this->Event->export_types[$k]['progress'] = 0;
				}
				//$this->Event->export_types[$k]['recommendation']
			}
			$this->set('useOrg', $useOrg);
			$this->set('export_types', $this->Event->export_types);
			// generate the list of Attribute types
			$this->loadModel('Attribute');
			//$lastModified = strftime("%d, %m, %Y, %T", $lastModified);
			$this->set('sigTypes', array_keys($this->Attribute->typeDefinitions));
		} else {
			// generate the list of Attribute types
			$this->loadModel('Attribute');
			//$lastModified = strftime("%d, %m, %Y, %T", $lastModified);
			$this->set('sigTypes', array_keys($this->Attribute->typeDefinitions));
			$this->render('/Events/export_alternate');
		}
	}


	public function downloadExport($type, $extra = null) {
		if ($this->_isSiteAdmin()) $org = 'ADMIN';
		else $org = $this->Auth->user('Organisation')['name'];
		$this->autoRender = false;
		if ($extra != null) $extra = '_' . $extra;
		$this->response->type($this->Event->export_types[$type]['extension']);
		$path = 'tmp/cached_exports/' . $type . DS . 'misp.' . strtolower($this->Event->export_types[$type]['type']) . $extra . '.' . $org . $this->Event->export_types[$type]['extension'];
		$this->response->file($path, array('download' => true));
	}

	private function __timeDifference($now, $then) {
		$periods = array("second", "minute", "hour", "day", "week", "month", "year");
		$lengths = array("60","60","24","7","4.35","12");
		$difference = $now - $then;
		for($j = 0; $difference >= $lengths[$j] && $j < count($lengths)-1; $j++) {
			$difference /= $lengths[$j];
		}
		$difference = round($difference);
		if($difference != 1) {
			$periods[$j].= "s";
		}
		return $difference . " " . $periods[$j] . " ago";
	}

	public function xml($key, $eventid=false, $withAttachment = false, $tags = false, $from = false, $to = false, $last = false) {
		App::uses('XMLConverterTool', 'Tools');
		$converter = new XMLConverterTool();
		$this->loadModel('Whitelist');

		// request handler for POSTed queries. If the request is a post, the parameters (apart from the key) will be ignored and replaced by the terms defined in the posted xml object.
		// The correct format for a posted xml is a "request" root element, as shown by the examples below:
		// For XML: <request><value>7.7.7.7&amp;&amp;1.1.1.1</value><type>ip-src</type></request>
		if ($this->request->is('post')) {
			if (empty($this->request->data)) {
				throw new BadRequestException('Either specify the search terms in the url, or POST an xml (with the root element being "request".');
			} else {
				$data = $this->request->data;
			}
			$paramArray = array('eventid', 'withAttachment', 'tags', 'from', 'to', 'last');
			foreach ($paramArray as $p) {
				if (isset($data['request'][$p])) ${$p} = $data['request'][$p];
				else ${$p} = null;
			}
		}

		$simpleFalse = array('tags', 'eventid', 'withAttachment', 'from', 'to', 'last');
		foreach ($simpleFalse as $sF) {
			if (!is_array(${$sF}) && (${$sF} === 'null' || ${$sF} == '0' || ${$sF} === false || strtolower(${$sF}) === 'false')) ${$sF} = false;
		}
		if ($from) $from = $this->Event->dateFieldCheck($from);
		if ($to) $to = $this->Event->dateFieldCheck($to);
		if ($tags) $tags = str_replace(';', ':', $tags);
		if ($last) $last = $this->Event->resolveTimeDelta($last);
		$eventIdArray = array();

		if ($eventid) {
			if (!is_numeric($eventid)) throw new MethodNotAllowedException('Invalid Event ID.');
			$eventIdArray[] = $eventid;
		}

		if ($key != 'download') {
			// check if the key is valid -> search for users based on key
			$user = $this->checkAuthUser($key);
			if (!$user) {
				throw new UnauthorizedException('This authentication key is not authorized to be used for exports. Contact your administrator.');
			}
		} else {
			if (!$this->Auth->user('id')) {
				throw new UnauthorizedException('You have to be logged in to do that.');
			}
			$user = $this->Auth->user();
		}

		if ($eventid) {
			$final_filename='misp.event' . $eventid . '.export.xml';
		} else {
			$final_filename='misp.export.xml';
		}
		$final = "";
		$final .= '<?xml version="1.0" encoding="UTF-8"?>' . PHP_EOL . '<response>' . PHP_EOL;
		$validEvents = 0;
		if (!$eventid) $eventIdArray = $this->Event->fetchEventIds($user, $from, $to, $last, true);
		foreach ($eventIdArray as $currentEventId) {
			$result = $this->Event->fetchEvent($user, array('eventid' => $currentEventId, 'tags' => $tags, 'from' => $from, 'to' => $to, 'last' => $last));
			if (empty($result)) continue;
			$validEvents++;
			if ($withAttachment) {
				foreach ($result[0]['Attribute'] as &$attribute) {
					if ($this->Event->Attribute->typeIsAttachment($attribute['type'])) {
						$encodedFile = $this->Event->Attribute->base64EncodeAttachment($attribute);
						$attribute['data'] = $encodedFile;
					}
				}
			}
			$result = $this->Whitelist->removeWhitelistedFromArray($result, false);
			$final .= $converter->event2XML($result[0]) . PHP_EOL;
		}
		if ($validEvents == 0) throw new NotFoundException('No events found that match the passed parameters.');
		$final .= '</response>' . PHP_EOL;
		$this->response->body($final);
		$this->response->type('xml');
		$this->response->download($final_filename);
		return $this->response;
	}

	// Grab an event or a list of events for the event view or any of the XML exports. The returned object includes an array of events (or an array that only includes a single event if an ID was given)
	// Included with the event are the attached attributes, shadow attributes, related events, related attribute information for the event view and the creating user's email address where appropriate
	private function __fetchEvent($eventid = false, $idList = false, $user = false, $tags = false, $from=false, $to=false) {
		// if we come from automation, we may not be logged in - instead we used an auth key in the URL.
		if (empty($user)) {
			$user = $this->Auth->user();
		}
		$results = $this->Event->fetchEvent($user, array('eventid' => $eventid, 'idList' => $idList, 'tags' => $tags, 'from' => $from, 'to' => $to));
		return $results;
	}

	public function nids($format = 'suricata', $key = 'download', $id = false, $continue = false, $tags = false, $from = false, $to = false, $last = false) {
		$simpleFalse = array('id', 'continue', 'tags', 'from', 'to', 'last');
		foreach ($simpleFalse as $sF) {
			if (!is_array(${$sF}) && (${$sF} === 'null' || ${$sF} == '0' || ${$sF} === false || strtolower(${$sF}) === 'false')) ${$sF} = false;
		}

		if ($from) $from = $this->Event->dateFieldCheck($from);
		if ($to) $to = $this->Event->dateFieldCheck($to);
		if ($tags) $tags = str_replace(';', ':', $tags);
		if ($last) $last = $this->Event->resolveTimeDelta($last);
		// backwards compatibility, swap key and format
		if ($format != 'snort' && $format != 'suricata') {
			$key = $format;
			$format = 'suricata'; // default format
		}
		$this->response->type('txt');	// set the content type
		$filename = 'misp.' . $format . '.rules';
		if ($id) $filename = 'misp.' . $format . '.event' . $id . '.rules';
		$this->header('Content-Disposition: download; filename="' . $filename . '"');
		$this->layout = 'text/default';
		if ($key != 'download') {
			// check if the key is valid -> search for users based on key
			$user = $this->checkAuthUser($key);
			if (!$user) {
				throw new UnauthorizedException('This authentication key is not authorized to be used for exports. Contact your administrator.');
			}
		} else {
			// check if there's a user logged in or not
			if (!$this->Auth->user('id')) {
				throw new UnauthorizedException('You have to be logged in to do that.');
			}
			$user = $this->Auth->user();
		}

		// display the full snort rulebase
		$this->loadModel('Attribute');
		$rules = $this->Attribute->nids($user, $format, $id, $continue, $tags, $from, $to, $last);
		$this->set('rules', $rules);
	}

	public function hids($type, $key='download', $tags = false, $from = false, $to = false, $last = false) {
		$simpleFalse = array('tags', 'from', 'to', 'last');
		foreach ($simpleFalse as $sF) {
			if (!is_array(${$sF}) && (${$sF} === 'null' || ${$sF} == '0' || ${$sF} === false || strtolower(${$sF}) === 'false')) ${$sF} = false;
		}
		if ($from) $from = $this->Event->dateFieldCheck($from);
		if ($to) $to = $this->Event->dateFieldCheck($to);
		if ($tags) $tags = str_replace(';', ':', $tags);
		if ($last) $last = $this->Event->resolveTimeDelta($last);
		$this->response->type('txt');	// set the content type
		$this->header('Content-Disposition: download; filename="misp.' . $type . '.rules"');
		$this->layout = 'text/default';
		if ($key != 'download') {
			// check if the key is valid -> search for users based on key
			$user = $this->checkAuthUser($key);
			if (!$user) {
				throw new UnauthorizedException('This authentication key is not authorized to be used for exports. Contact your administrator.');
			}
		} else {
			// check if there's a user logged in or not
			if (!$this->Auth->user('id')) {
				throw new UnauthorizedException('You have to be logged in to do that.');
			}
			$user = $this->Auth->user();
		}
		$this->loadModel('Attribute');
		$rules = $this->Attribute->hids($this->Auth->user(), $type, $tags, $from, $to, $last);
		$this->set('rules', $rules);
	}

	// csv function
	// Usage: csv($key, $eventid)   - key can be a valid auth key or the string 'download'. Download requires the user to be logged in interactively and will generate a .csv file
	// $eventid can be one of 3 options: left empty it will get all the visible to_ids attributes,
	// $ignore is a flag that allows the export tool to ignore the ids flag. 0 = only IDS signatures, 1 = everything.
	public function csv($key, $eventid=false, $ignore=false, $tags = false, $category=false, $type=false, $includeContext=false, $from=false, $to=false, $last = false) {
		$simpleFalse = array('eventid', 'ignore', 'tags', 'category', 'type', 'includeContext', 'from', 'to', 'last');
		foreach ($simpleFalse as $sF) {
			if (!is_array(${$sF}) && (${$sF} === 'null' || ${$sF} == '0' || ${$sF} === false || strtolower(${$sF}) === 'false')) ${$sF} = false;
		}
		$exportType = $eventid;
		if ($from) $from = $this->Event->dateFieldCheck($from);
		if ($to) $to = $this->Event->dateFieldCheck($to);
		if ($tags) $tags = str_replace(';', ':', $tags);
		if ($last) $last = $this->Event->resolveTimeDelta($last);
		$list = array();
		if ($key != 'download') {
			// check if the key is valid -> search for users based on key
			$user = $this->checkAuthUser($key);
			if (!$user) {
				throw new UnauthorizedException('This authentication key is not authorized to be used for exports. Contact your administrator.');
			}
		} else {
			if (!$this->Auth->user('id')) {
				throw new UnauthorizedException('You have to be logged in to do that.');
			}
			$user = $this->Auth->user();
		}
		// if it's a search, grab the attributeIDList from the session and get the IDs from it. Use those as the condition
		// We don't need to look out for permissions since that's filtered by the search itself
		// We just want all the attributes found by the search
		if ($eventid === 'search') {
			$ioc = $this->Session->read('paginate_conditions_ioc');
			$paginateConditions = $this->Session->read('paginate_conditions');
			$attributes = $this->Event->Attribute->find('all', array(
				'conditions' => $paginateConditions['conditions'],
				'contain' => $paginateConditions['contain'],
			));
			if ($ioc) {
				$this->loadModel('Whitelist');
				$attributes = $this->Whitelist->removeWhitelistedFromArray($attributes, true);
			}
			$list = array();
			foreach ($attributes as &$attribute) {
				$list[] = $attribute['Attribute']['id'];
			}
		} else if ($eventid === false) {
			$events = $this->Event->fetchEventIds($this->Auth->user(), $from, $to, $last, true);
			if (empty($events)) $events = array(0 => -1);
		} else {
			$events = array($eventid);
		}
		$final = array();
		$this->loadModel('Whitelist');
		if ($tags) {
			$args = $this->Event->Attribute->dissectArgs($tags);
			$tagArray = $this->Event->EventTag->Tag->fetchEventTagIds($args[0], $args[1]);
			$temp = array();
			if (!empty($tagArray[0])) $events = array_intersect($events, $tagArray[0]);
			if (!empty($tagArray[1])) {
				foreach ($events as $k => $eventid) {
					if (in_array($eventid, $tagArray[1])) unset($events[$k]);
				}
			}
		}
		if (isset($events)) {
			foreach ($events as $eventid) {
				$attributes = $this->Event->csv($user, $eventid, $ignore, $list, false, $category, $type, $includeContext);
				$attributes = $this->Whitelist->removeWhitelistedFromArray($attributes, true);
				foreach ($attributes as $attribute) {
					$line = $attribute['Attribute']['uuid'] . ',' . $attribute['Attribute']['event_id'] . ',' . $attribute['Attribute']['category'] . ',' . $attribute['Attribute']['type'] . ',' . $attribute['Attribute']['value'] . ',' . $attribute['Attribute']['comment'] . ',' . intval($attribute['Attribute']['to_ids']) . ',' . $attribute['Attribute']['timestamp'];
					if ($includeContext) {
						foreach($this->Event->csv_event_context_fields_to_fetch as $header => $field) {
							if ($field['object']) $line .= ',' . $attribute['Event'][$field['object']][$field['var']];
							else $line .= ',' . $attribute['Event'][$field['var']];
						}
					}
					$final[] = $line;
				}
			}
		}
		$this->response->type('csv');	// set the content type
		if (!$exportType) {
			$this->header('Content-Disposition: download; filename="misp.all_attributes.csv"');
		} else if ($exportType === 'search') {
			$this->header('Content-Disposition: download; filename="misp.search_result.csv"');
		} else {
			$this->header('Content-Disposition: download; filename="misp.event_' . $exportType . '.csv"');
		}
		$this->layout = 'text/default';
		$headers = array('uuid', 'event_id', 'category', 'type', 'value', 'comment', 'to_ids', 'date');
		if ($includeContext) $headers = array_merge($headers, array_keys($this->Event->csv_event_context_fields_to_fetch));
		$this->set('headers', $headers);
		$this->set('final', $final);
	}

	//public function dot($key) {
	//	// check if the key is valid -> search for users based on key
	//	$this->loadModel('User');
	//	// no input sanitization necessary, it's done by model
	//	$this->User->recursive=0;
	//	$user = $this->User->findByAuthkey($key);
	//	if (empty($user)) {
	//		throw new UnauthorizedException('Incorrect authentication key');
	//	}
	//	// display the full snort rulebase
	//	$this->response->type('txt');	// set the content type
	//	$this->header('Content-Disposition: inline; filename="MISP.rules"');
	//	$this->layout = 'text/default';

	//	$rules= array();
	//	$this->loadModel('Attribute');

	//	$params = array(
	//			'recursive' => 0,
	//			'fields' => array('Attribute.*')
	//	);
	//	$items = $this->Attribute->find('all', $params);

	//	$composite_types = $this->Attribute->getCompositeTypes();
	//	// rebuild the array with the correct data
	//	foreach ($items as &$item) {
	//		if (in_array($item['Attribute']['type'], $composite_types)) {
	//			// create a new item that will contain value2
	//			$new_item = $item;
	//			// set the correct type for the first item
	//			$pieces = explode('|', $item['Attribute']['type']);
	//			$item['Attribute']['type'] = $pieces[0];
	//			// set the correct data for the new item
	//			$new_item['Attribute']['type'] = (isset($pieces[1]))? $pieces[1] : 'md5';
	//			$new_item['Attribute']['value'] = $item['Attribute']['value2'];
	//			unset($new_item['Attribute']['value1']);
	//			unset($new_item['Attribute']['value2']);
	//			// store the new item
	//			$items[] = $new_item;
	//		}
	//		// set the correct fields for the attribute
	//		if (isset($item['Attribute']['value1'])) {
	//			$item['Attribute']['value'] = $item['Attribute']['value1'];
	//		}
	//		unset($item['Attribute']['value1']);
	//		unset($item['Attribute']['value2']);
	//	}
	//	debug($items);

	//	// iterate over the array to build the GV links
	//	require_once 'Image/GraphViz.php';
	//	$gv = new Image_GraphViz();
	//	$gv->addEdge(array('wake up'		=> 'visit bathroom'));
	//	$gv->addEdge(array('visit bathroom' => 'make coffee'));
	//	foreach ($items as &$item) {
	//		$gv->addNode('Node 1',
	//				array(''));
	//	}
	//	debug($gv);
	//	$gv->image();
	//}

	public function _addGfiZip($id) {
		if (!empty($this->data) && $this->data['Event']['submittedgfi']['size'] > 0 &&
				is_uploaded_file($this->data['Event']['submittedgfi']['tmp_name'])) {
			$zipData = fread(fopen($this->data['Event']['submittedgfi']['tmp_name'], "r"),
					$this->data['Event']['submittedgfi']['size']);

			// write
			$rootDir = APP . "files" . DS . $id . DS;
			App::uses('Folder', 'Utility');
			$dir = new Folder($rootDir, true);
			$destpath = $rootDir;
			$file = new File ($destpath);
			if (!preg_match('@^[\w-,\s,\.]+\.[A-Za-z0-9_]{2,4}$@', $this->data['Event']['submittedgfi']['name'])) throw new Exception ('Filename not allowed');
			if (PHP_OS == 'WINNT') {
				$zipfile = new File ($destpath . DS . $this->data['Event']['submittedgfi']['name']);
			} else {
				$zipfile = new File ($destpath . $this->data['Event']['submittedgfi']['name']);
			}

			$result = $zipfile->write($zipData);
			if (!$result) $this->Session->setFlash(__('Problem with writing the zip file. Please report to administrator.'));
			// extract zip..
			$execRetval = '';
			$execOutput = array();
			exec("unzip " . $zipfile->path . ' -d ' . $rootDir, $execOutput, $execRetval);
			if ($execRetval != 0) {	// not EXIT_SUCCESS
				// do some?
				throw new Exception('An error has occured while attempting to unzip the GFI sandbox .zip file. We apologise for the inconvenience.');
			}

			// now open the xml..
			if (PHP_OS == 'WINNT') {
				$xml = $rootDir . 'Analysis' . DS . 'analysis.xml';
			} else {
				$xml = $rootDir . DS . 'Analysis' . DS . 'analysis.xml';
			}
			$fileData = fread(fopen($xml, "r"), filesize($xml));

			// read XML
			$this->_readGfiXML($fileData, $id);
		}
	}

	public function _addIOCFile($id) {
		if (!empty($this->data) && $this->data['Event']['submittedioc']['size'] > 0 &&
				is_uploaded_file($this->data['Event']['submittedioc']['tmp_name'])) {
			$iocData = fread(fopen($this->data['Event']['submittedioc']['tmp_name'], "r"),
					$this->data['Event']['submittedioc']['size']);
			// write
			$rootDir = APP . "files" . DS . $id . DS;
			App::uses('Folder', 'Utility');
			$dir = new Folder($rootDir . 'ioc', true);
			$destpath = $rootDir . 'ioc';
			$file = new File ($destpath);
			if (!preg_match('@^[\w-,\s,\.]+\.[A-Za-z0-9_]{2,4}$@', $this->data['Event']['submittedioc']['name'])) throw new Exception ('Filename not allowed');
			$iocfile = new File ($destpath . DS . $this->data['Event']['submittedioc']['name']);
			$result = $iocfile->write($iocData);
			if (!$result) $this->Session->setFlash(__('Problem with writing the ioc file. Please report to administrator.'));

			// now open the xml..
			$xml = $rootDir . DS . 'Analysis' . DS . 'analysis.xml';
			$fileData = fread(fopen($destpath . DS . $this->data['Event']['submittedioc']['name'], "r"), $this->data['Event']['submittedioc']['size']);
			// Load event and populate the event data
			$this->Event->id = $id;
			$this->Event->recursive = -1;
			if (!$this->Event->exists()) {
				throw new NotFoundException(__('Invalid event'));
			}
			$this->Event->read(null, $id);
			$saveEvent['Event'] = $this->Event->data['Event'];
			$saveEvent['Event']['published'] = false;
			$dist = '3';
			if (Configure::read('MISP.default_attribute_distribution') != null) {
				if (Configure::read('MISP.default_attribute_distribution') === 'event') {
					$dist = $this->Event->data['Event']['distribution'];
				} else {
					$dist = '';
					$dist .= Configure::read('MISP.default_attribute_distribution');
				}
			}
			// read XML
			$event = $this->IOCImport->readXML($fileData, $id, $dist, $this->data['Event']['submittedioc']['name']);

			// make some changes to have $saveEvent in the format that is needed to save the event together with its attributes
			$fails = $event['Fails'];
			$saveEvent['Attribute'] = $event['Attribute'];
			// we've already stored these elsewhere, unset them so we can extract the event related data
			unset($event['Attribute']);
			unset($event['Fails']);

			// add the original openIOC file as an attachment
			$saveEvent['Attribute'][] = array(
				'category' => 'External analysis',
				'uuid' =>  $this->Event->generateUuid(),
				'type' => 'attachment',
				'value' => $this->data['Event']['submittedioc']['name'],
				'to_ids' => false,
				'distribution' => $dist,
				'data' => base64_encode($fileData),
				'comment' => 'OpenIOC import source file'
			);

			// Keep this for later if we want to let an ioc create the event data automatically in a later version
			// save the event related data into $saveEvent['Event']
			//$saveEvent['Event'] = $event;
			//$saveEvent['Event']['id'] = $id;

			$fieldList = array(
					'Event' => array('published', 'timestamp'),
					'Attribute' => array('event_id', 'category', 'type', 'value', 'value1', 'value2', 'to_ids', 'uuid', 'distribution', 'timestamp', 'comment')
			);
			// Save it all
			$saveResult = $this->Event->saveAssociated($saveEvent, array('validate' => true, 'fieldList' => $fieldList));
			// set stuff for the view and render the showIOCResults view.
			$this->set('attributes', $saveEvent['Attribute']);
			if (isset($fails)) {
				$this->set('fails', $fails);
			}
			$this->set('eventId', $id);
			$this->set('graph', $event['Graph']);
			$this->set('saveEvent', $saveEvent);
			$this->render('showIOCResults');
		}
	}

	public function _addMISPExportFile($ext, $take_ownership = false) {
		$data = fread(fopen($this->data['Event']['submittedfile']['tmp_name'], "r"), $this->data['Event']['submittedfile']['size']);
		if ($ext == 'xml') {
			App::uses('Xml', 'Utility');
			$dataArray = Xml::toArray(Xml::build($data));
		} else {
			$dataArray = json_decode($data, true);
			if (isset($dataArray['response'][0])) {
				foreach ($dataArray['response'] as $k => &$temp) {
					$dataArray['Event'][] = $temp['Event'];
					unset ($dataArray['response'][$k]);
				}
			}
		}
		// In case we receive an event that is not encapsulated in a response. This should never happen (unless it's a copy+paste fail),
		// but just in case, let's clean it up anyway.
		if (isset($dataArray['Event'])) {
			$dataArray['response']['Event'] = $dataArray['Event'];
			unset($dataArray['Event']);
		}
		if (!isset($dataArray['response']) || !isset($dataArray['response']['Event'])) {
			throw new Exception('This is not a valid MISP XML file.');
		}
		$dataArray = $this->Event->updateXMLArray($dataArray);
		$results = array();
		$validationIssues = array();
		if (isset($dataArray['response']['Event'][0])) {
			foreach ($dataArray['response']['Event'] as $k => $event) {
				$result = array('info' => $event['info']);
				if ($take_ownership) {
					$event['orgc_id'] = $this->Auth->user('org_id');
					unset($event['Orgc']);
				}
				$event = array('Event' => $event);
				$created_id = 0;
				$result['result'] = $this->Event->_add($event, true, $this->Auth->user(), '', null, false, null, $created_id, $validationIssues);
				$result['id'] = $created_id;
				$result['validationIssues'] = $validationIssues;
				$results[] = $result;
			}
		} else {
			$temp['Event'] = $dataArray['response']['Event'];
			if ($take_ownership)  {
				$temp['Event']['orgc_id'] = $this->Auth->user('org_id');
				unset($temp['Event']['Orgc']);
			}
			$created_id = 0;
			$result = $this->Event->_add($temp, true, $this->Auth->user(), '', null, false, null, $created_id, $validationIssues);
			$results = array(0 => array('info' => $temp['Event']['info'], 'result' => $result, 'id' => $created_id, 'validationIssues' => $validationIssues));
		}
		return $results;
	}

	public function _readGfiXML($data, $id) {
		$this->loadModel('Attribute');
		$this->Event->recursive = -1;
		$this->Event->read(array('id', 'uuid', 'distribution'), $id);

		// import XML class
		App::uses('Xml', 'Utility');
		// now parse it
		$parsedXml = Xml::build($data, array('return' => 'simplexml'));

		// xpath..

		if (Configure::read('MISP.default_attribute_distribution') != null) {
			if (Configure::read('MISP.default_attribute_distribution') === 'event') {
				$dist = $this->Event->data['Event']['distribution'];
			} else {
				$dist = '';
				$dist .= Configure::read('MISP.default_attribute_distribution');
			}
		}

		//Payload delivery -- malware-sample
		$results = $parsedXml->xpath('/analysis');
		foreach ($results as $result) {
			foreach ($result[0]->attributes() as $key => $val) {
				if ((string)$key == 'filename') $realFileName = (string)$val;
			}
		}
		$realMalware = $realFileName;
		$rootDir = APP . "files" . DS . $id . DS;
		$malware = $rootDir . DS . 'sample';
		$this->Event->Attribute->uploadAttachment($malware,	$realFileName,	true, $id, null, '', $this->Event->data['Event']['uuid'] . '-sample', $dist, true);

		//Network activity -- .pcap
		$realFileName = 'analysis.pcap';
		$rootDir = APP . "files" . DS . $id . DS;
		$malware = $rootDir . DS . 'Analysis' . DS . 'analysis.pcap';
		$this->Event->Attribute->uploadAttachment($malware,	$realFileName,	false, $id, 'Network activity', '', $this->Event->data['Event']['uuid'] . '-analysis.pcap', $dist, true);

		//Artifacts dropped -- filename|md5
		$files = array();
		// TODO what about stored_modified_file ??
		$results = $parsedXml->xpath('/analysis/processes/process/stored_files/stored_created_file');
		foreach ($results as $result) {
			$arrayItemKey = '';
			$arrayItemValue = '';
			foreach ($result[0]->attributes() as $key => $val) {
				if ($key == 'filename') $arrayItemKey = (string)$val;
				if ($key == 'md5') $arrayItemValue = (string)$val;
				if ($key == 'filesize') $arrayItemSize = $val;
			}
			//$files[$arrayItemKey] = $arrayItemValue;
			if ($arrayItemSize > 0) {
				$files[] = array('key' => $arrayItemKey, 'val' => $arrayItemValue);
			}
		}
		//$files = array_unique($files);
		// write content..
		foreach ($files as $file) {
			$keyName = $file['key'];
			if (!strpos($file['key'], $realMalware)) {
				$itsType = 'malware-sample';
			} else {
				$itsType = 'filename|md5';
			}

			// the actual files..
			// seek $val in dirs and add..
			$ext = substr($file['key'], strrpos($file['key'], '.'));
			$actualFileName = $file['val'] . $ext;
			$actualFileNameBase = str_replace('\\', '/', $file['key']);
			$actualFileNameArray[] = basename($actualFileNameBase);
			$tempExplode = explode('\\', $file['key']);
			$realFileName = end($tempExplode);
			// have the filename, now look at parents parent for the process number
			$express = "/analysis/processes/process/stored_files/stored_created_file[@md5='" . $file['val'] . "']/../..";
			$results = $parsedXml->xpath($express);
			foreach ($results as $result) {
				foreach ($result[0]->attributes() as $key => $val) {
					if ((string)$key == 'index') $index = (string)$val;
				}
			}
			$actualFile = $rootDir . DS . 'Analysis' . DS . 'proc_' . $index . DS . 'modified_files' . DS . $actualFileName;
			$extraPath = 'Analysis' . DS . 'proc_' . $index . DS . 'modified_files' . DS;
			$file = new File($actualFile);
			if ($file->exists()) { // TODO put in array for test later
				$this->Event->Attribute->uploadAttachment($actualFile, $realFileName, true, $id, null, $extraPath, $keyName, $dist, true); // TODO was false
			} else {
			}
		}

		//Network activity -- ip-dst
		$ips = array();
		$hostnames = array();
		$results = $parsedXml->xpath('/analysis/processes/process/networkpacket_section/connect_to_computer');
		foreach ($results as $result) {
			foreach ($result[0]->attributes() as $key => $val) {
				if ($key == 'remote_ip') $ips[] = (string)$val;
				if ($key == 'remote_hostname') $hostnames[] = (string)$val;
			}
		}
		// write content..
		// ip-s
		foreach ($ips as $ip) {
			// add attribute..
			$this->Attribute->create();
			$this->Attribute->save(array(
					'event_id' => $id,
					'category' => 'Network activity',
					'type' => 'ip-dst',
					'value' => $ip,
					'to_ids' => false,
					'distribution' => $dist,
					'comment' => 'GFI import',
					));
		}
		foreach ($hostnames as $hostname) {
			// add attribute..
			$this->Attribute->create();
			$this->Attribute->save(array(
					'event_id' => $id,
					'category' => 'Network activity',
					'type' => 'hostname',
					'value' => $hostname,
					'to_ids' => false,
					'distribution' => $dist,
					'comment' => 'GFI import',
			));
		}
		// Persistence mechanism -- regkey|value
		$regs = array();
		$results = $parsedXml->xpath('/analysis/processes/process/registry_section/set_value');
		foreach ($results as $result) {
			$arrayItemKey = '';
			$arrayItemValue = '';
			foreach ($result[0]->attributes() as $key => $val) {
				if ($key == 'key_name') $arrayItemKey = (string)$val;
				if ($key == 'data') $arrayItemValue = (string)$val;
			}
			$regs[$arrayItemKey] = str_replace('(UNICODE_0x00000000)', '', $arrayItemValue);
		}
		//$regs = array_unique($regs);

		// write content..
		foreach ($regs as $key => $val) {
			// add attribute..
			$this->Attribute->create();

			if ($this->strposarray($val,$actualFileNameArray)) {
				$this->Attribute->save(array(
					'event_id' => $id,
					'comment' => 'GFI import',
					'category' => 'Persistence mechanism', // 'Persistence mechanism'
					'type' => 'regkey|value',
					'value' => $key . '|' . $val,
					'distribution' => $dist,
					'to_ids' => false
				));
			}
		}
	}

	public function strposarray($string, $array) {
		$toReturn = false;
		foreach ($array as $item) {
			if (strpos($string,$item)) {
				$toReturn = true;
			}
		}
		return $toReturn;
	}

	public function downloadSearchResult() {
		$ioc = $this->Session->read('paginate_conditions_ioc');
		$paginateConditions = $this->Session->read('paginate_conditions');
		$attributes = $this->Event->Attribute->fetchAttributes($this->Auth->user(), array(
			'conditions' => $paginateConditions['conditions'],
			'contain' => $paginateConditions['contain'],
		));
		if ($ioc) {
			$this->loadModel('Whitelist');
			$attributes = $this->Whitelist->removeWhitelistedFromArray($attributes, true);
		}
		$idList = array();
		foreach ($attributes as &$attribute) {
			if (!in_array($attribute['Attribute']['event_id'], $idList)) {
				$idList[] = $attribute['Attribute']['event_id'];
			}
		}

		if ($this->response->type() === 'application/json') {

		} else {
			// display the full xml
			$this->response->type('xml');	// set the content type
			$this->layout = 'xml/default';
			$this->header('Content-Disposition: download; filename="misp.search.results.xml"');
		}
		$results = $this->__fetchEvent(null, $idList);
		$this->set('results', $results);
		$this->render('xml');
	}

	// Use the rest interface to search for  attributes or events. Usage:
	// MISP-base-url/events/restSearch/[api-key]/[value]/[type]/[category]/[orgc]
	// value, type, category, orgc are optional
	// target can be either "event" or "attribute"
	// the last 4 fields accept the following operators:
	// && - you can use && between two search values to put a logical OR between them. for value, 1.1.1.1&&2.2.2.2 would find attributes with the value being either of the two.
	// ! - you can negate a search term. For example: google.com&&!mail would search for all attributes with value google.com but not ones that include mail. www.google.com would get returned, mail.google.com wouldn't.
	public function restSearch($key='download', $value=false, $type=false, $category=false, $org=false, $tags=false, $searchall=false, $from=false, $to=false, $last=false, $eventid=false, $withAttachments = false) {
		if ($key!='download') {
			$user = $this->checkAuthUser($key);
		} else {
			if (!$this->Auth->user()) throw new UnauthorizedException('You are not authorized. Please send the Authorization header with your auth key along with an Accept header for application/xml.');
			$user = $this->checkAuthUser($this->Auth->user('authkey'));
		}
		if (!$user) {
			throw new UnauthorizedException('This authentication key is not authorized to be used for exports. Contact your administrator.');
		}
		$value = str_replace('|', '/', $value);
		// request handler for POSTed queries. If the request is a post, the parameters (apart from the key) will be ignored and replaced by the terms defined in the posted json or xml object.
		// The correct format for both is a "request" root element, as shown by the examples below:
		// For Json: {"request":{"value": "7.7.7.7&&1.1.1.1","type":"ip-src"}}
		// For XML: <request><value>7.7.7.7&amp;&amp;1.1.1.1</value><type>ip-src</type></request>
		// the response type is used to determine the parsing method (xml/json)
		if ($this->request->is('post')) {
			if ($this->response->type() === 'application/json') {
				$data = $this->request->input('json_decode', true);
			} elseif ($this->response->type() === 'application/xml') {
				$data = $this->request->data;
			} else {
				throw new BadRequestException('Either specify the search terms in the url, or POST a json array / xml (with the root element being "request" and specify the correct headers based on content type.');
			}
			$paramArray = array('value', 'type', 'category', 'org', 'tags', 'searchall', 'from', 'to', 'last', 'eventid', 'withAttachments');
			foreach ($paramArray as $p) {
				if (isset($data['request'][$p])) ${$p} = $data['request'][$p];
				else ${$p} = null;
			}
		}
		$simpleFalse = array('value' , 'type', 'category', 'org', 'tags', 'searchall', 'from', 'to', 'last', 'eventid', 'withAttachments');
		foreach ($simpleFalse as $sF) {
			if (!is_array(${$sF}) && (${$sF} === 'null' || ${$sF} == '0' || ${$sF} === false || strtolower(${$sF}) === 'false')) ${$sF} = false;
		}

		if ($from) $from = $this->Event->dateFieldCheck($from);
		if ($to) $to = $this->Event->dateFieldCheck($to);
		if ($tags) $tags = str_replace(';', ':', $tags);
		if ($last) $last = $this->Event->resolveTimeDelta($last);
		if ($searchall === 'true') $searchall = "1";
		$conditions['AND'] = array();
		$subcondition = array();
		$this->loadModel('Attribute');
		// add the values as specified in the 2nd parameter to the conditions
		$values = explode('&&', $value);
		if (isset($searchall) && ($searchall == 1 || $searchall === true || $searchall == 'true')) {
			$eventIds = $this->__quickFilter($value);
		} else {
			$parameters = array('value', 'type', 'category', 'org', 'eventid');
			foreach ($parameters as $k => $param) {
				if (isset(${$parameters[$k]})) {
					if (is_array(${$parameters[$k]})) $elements = ${$parameters[$k]};
					else $elements = explode('&&', ${$parameters[$k]});
					foreach($elements as $v) {
						if ($v == '') continue;
						if (substr($v, 0, 1) == '!') {
							if ($parameters[$k] === 'value' && preg_match('@^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\/(\d|[1-2]\d|3[0-2]))$@', substr($v, 1))) {
								$cidrresults = $this->Cidr->CIDR(substr($v, 1));
								foreach ($cidrresults as $result) {
									$subcondition['AND'][] = array('Attribute.value NOT LIKE' => $result);
								}
							} else {
								if ($parameters[$k] === 'org') {
									$found_orgs = $this->Event->Org->find('all', array(
										'recursive' => -1,
										'conditions' => array('LOWER(name) LIKE' => '%' . strtolower(substr($v, 1)) . '%'),
									));
									foreach ($found_orgs as $o) $subcondition['AND'][] = array('Event.orgc_id !=' => $o['Org']['id']);
								} elseif ($parameters[$k] === 'eventid') {
									$subcondition['AND'][] = array('Attribute.event_id !=' => substr($v, 1));
								} else {
									$subcondition['AND'][] = array('Attribute.' . $parameters[$k] . ' NOT LIKE' => '%'.substr($v, 1).'%');
								}
							}
						} else {
							if ($parameters[$k] === 'value' && preg_match('@^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\/(\d|[1-2]\d|3[0-2]))$@', substr($v, 1))) {
								$cidrresults = $this->Cidr->CIDR($v);
								foreach ($cidrresults as $result) {
									if (!empty($result)) $subcondition['OR'][] = array('Attribute.value LIKE' => $result);
								}
							} else {
								if ($parameters[$k] === 'org') {
									$found_orgs = $this->Event->Org->find('all', array(
											'recursive' => -1,
											'conditions' => array('LOWER(name) LIKE' => '%' . strtolower($v) . '%'),
									));
									foreach ($found_orgs as $o) $subcondition['OR'][] = array('Event.orgc_id' => $o['Org']['id']);
								} elseif ($parameters[$k] === 'eventid') {
									$subcondition['OR'][] = array('Attribute.event_id' => $v);
								} else {
									if (!empty($v)) $subcondition['OR'][] = array('Attribute.' . $parameters[$k] . ' LIKE' => '%'.$v.'%');
								}
							}
						}
					}
					if (!empty($subcondition)) array_push ($conditions['AND'], $subcondition);
					$subcondition = array();
				}
			}

			// If we sent any tags along, load the associated tag names for each attribute
			if ($tags) {
				$args = $this->Event->Attribute->dissectArgs($tags);
				$this->loadModel('Tag');
				$tagArray = $this->Tag->fetchEventTagIds($args[0], $args[1]);
				$temp = array();
				foreach ($tagArray[0] as $accepted) {
					$temp['OR'][] = array('Event.id' => $accepted);
				}
				$conditions['AND'][] = $temp;
				$temp = array();
				foreach ($tagArray[1] as $rejected) {
					$temp['AND'][] = array('Event.id !=' => $rejected);
				}
				$conditions['AND'][] = $temp;
			}

			if ($from) $conditions['AND'][] = array('Event.date >=' => $from);
			if ($to) $conditions['AND'][] = array('Event.date <=' => $to);
			if ($last) $conditions['AND'][] = array('Event.publish_timestamp >=' => $last);
			$params = array(
					'conditions' => $conditions,
					'fields' => array('DISTINCT(Attribute.event_id)'),
					'contain' => array()
			);
			$attributes = $this->Event->Attribute->fetchAttributes($this->Auth->user(), $params);
			$eventIds = array();
			foreach ($attributes as $attribute) {
				if (!in_array($attribute['Attribute']['event_id'], $eventIds)) $eventIds[] = $attribute['Attribute']['event_id'];
			}
		}
		if (!empty($eventIds)) {
			$this->loadModel('Whitelist');
			if ((!isset($this->request->params['ext']) || $this->request->params['ext'] !== 'json') && $this->response->type() !== 'application/json') {
				App::uses('XMLConverterTool', 'Tools');
				$converter = new XMLConverterTool();
				$final = "";
				$final .= '<?xml version="1.0" encoding="UTF-8"?>' . PHP_EOL . '<response>' . PHP_EOL;
				foreach ($eventIds as $currentEventId) {
					$result = $this->Event->fetchEvent($this->Auth->user(), array('eventid' => $currentEventId, 'includeAttachments' => $withAttachments));
					if (!empty($result)) {
						$result = $this->Whitelist->removeWhitelistedFromArray($result, false);
						$final .= $converter->event2XML($result[0]) . PHP_EOL;
					}
				}
				$final .= '</response>' . PHP_EOL;
				$final_filename="misp.search.events.results.xml";
				$this->response->body($final);
				$this->response->type('xml');
				$this->response->download($final_filename);
			} else {
				App::uses('JSONConverterTool', 'Tools');
				$converter = new JSONConverterTool();
				$temp = array();
				$final = '{"response":[';
				foreach ($eventIds as $k => $currentEventId) {
					$result = $this->Event->fetchEvent($this->Auth->user(), array('eventid' => $currentEventId, 'includeAttachments' => $withAttachments));
					$final .= $converter->event2JSON($result[0]);
					if ($k < count($eventIds) -1 ) $final .= ',';
				}
				$final .= ']}';
				$final_filename="misp.search.events.results.json";
				$this->response->body($final);
				$this->response->type('json');
				$this->response->download($final_filename);
			}
		} else {
			throw new NotFoundException('No matches.');
		}
		return $this->response;
	}

	public function downloadOpenIOCEvent($eventid) {

		// return a downloadable text file called misp.openIOC.<eventId>.ioc for individual events
		// TODO implement mass download of all events - maybe in a zip file?
		$this->response->type('text');	// set the content type
		if ($eventid == null) {
			throw new Exception('Not yet implemented');
			// $this->header('Content-Disposition: download; filename="misp.openIOC.ioc"');
		} else {
			$this->header('Content-Disposition: download; filename="misp.openIOC' . $eventid . '.ioc"');
		}
		$this->layout = 'text/default';

		// get the event if it exists and load it together with its attributes
		$this->Event->id = $eventid;
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event or not authorised.'));
		}
		$event = $this->Event->fetchEvent($this->Auth->user(), $options = array('eventid' => $eventid, 'to_ids' => 1));
		if (empty($event)) throw new NotFoundException('Invalid event or not authorised.');
		$this->loadModel('Whitelist');
		$temp = $this->Whitelist->removeWhitelistedFromArray(array($event[0]), false);
		$event = $temp[0];
		//$event['Attribute'] = $this->Whitelist->removeWhitelistedFromArray($event['Attribute'], false);

		// send the event and the vars needed to check authorisation to the Component
		$final = $this->IOCExport->buildAll($this->Auth->user(), $event);
		$this->set('final', $final);
	}

	public function create_dummy_event() {
		if (!$this->_isSiteAdmin() || !$this->request->is('post')) throw new MethodNotAllowedException('You don\'t have the privileges to access this.');
		$date = new DateTime();
		$data['Event']['info'] = 'Test event showing every category-type combination';
		$data['Event']['date'] = '2013-10-09';
		$data['Event']['threat_level_id'] = 4; //'Undefined'
		$data['Event']['analysis'] = '0';
		$data['Event']['distribution'] = '0';

		$defaultValues = array(
				'md5' => '098f6bcd4621d373cade4e832627b4f6',
				'sha1' => 'a7645200866fd00bde529733ceac8506ab1f5518',
				'sha256' => '0f58957831a9cf0b768451ee6b236555f519c04f0da5a5ea87538fd0990b29d1',
				'filename' => 'test.exe',
				'filename|md5' => 'test.exe|8886be8e4e862189a68d27e8fc7a6144',
				'filename|sha1' => 'test.exe|a7645200866fd00bde529733ceac8506ab1f5518',
				'filename|sha256' => 'test.exe|0f58957831a9cf0b768451ee6b236555f519c04f0da5a5ea87538fd0990b29d1',
				'ip-src' => '1.1.1.1',
				'ip-dst' => '2.2.2.2',
				'hostname' => 'www.futuremark.com',
				'domain' => 'evildomain.org',
				'email-src' => 'bla@bla.com',
				'email-dst' => 'hmm@hmm.com',
				'email-subject' => 'Some made-up email subject',
				'email-attachment' => 'filename.exe',
				'url' => 'http://www.evilsite.com/test',
				'http-method' => 'POST',
				'user-agent' => 'Microsoft Internet Explorer',
				'regkey' => 'HKLM\Software\Microsoft\Windows\CurrentVersion\Run\fishy',
				'regkey|value' => 'HKLM\Software\Microsoft\Windows\CurrentVersion\Run\fishy|%ProgramFiles%\Malicios\malware.exe',
				'AS' => '45566',
				'snort' => 'alert ip 1.1.1.1 any -> $HOME_NET any (msg: "MISP e1 Incoming From IP: 1.1.1.1"; classtype:trojan-activity; sid:21; rev:1; priority:1; reference:url,http://localhost:8888/events/view/1;)',
				'pattern-in-file' => 'Somestringinfile',
				'pattern-in-traffic' => 'Somestringintraffic',
				'pattern-in-memory' => 'Somestringinmemory',
				'yara' => 'rule silent_banker : banker{meta:description = "This is just an example" thread_level = 3 in_the_wild = true strings: $a = {6A 40 68 00 30 00 00 6A 14 8D 91} $b = {8D 4D B0 2B C1 83 C0 27 99 6A 4E 59 F7 F9} $c = "UVODFRYSIHLNWPEJXQZAKCBGMT" condition:}',
				'vulnerability' => 'CVE-2011-0001',
				'attachment' => 'file.txt',
				'malware-sample' => 'test.exe|8886be8e4e862189a68d27e8fc7a6144',
				'link' => 'http://www.somesite.com/',
				'comment' => 'Comment',
				'text' => 'Any text',
				'other' => 'Could be anything',
				'named pipe' => '\\.\pipe\PipeName',
				'mutex' => 'mutexstring',
				'target-user' => 'user1',
				'target-email' => 'someone@something.com',
				'target-machine' => 'machinename',
				'target-org' => 'EA games',
				'target-location' => 'Hell',
				'target-external' => 'some target'
		);
		$this->loadModel('Attribute');
		foreach ($this->Attribute->categoryDefinitions as $category => $v) {
			foreach ($v['types'] as $k => $type) {
				$data['Attribute'][] = array(
					'category' => $category,
					'type' => $type,
					'value' => $defaultValues[$type],
					'to_ids' => '0',
					'distribution' => '0',
				);
			}
		}
		$this->Event->_add($data, false, $this->Auth->user());
	}

	// for load testing, it's slow, execution time is set at 1 hour maximum
	public function create_massive_dummy_events() {
		if (!$this->_isSiteAdmin() || !$this->request->is('post')) throw new MethodNotAllowedException('You don\'t have the privileges to access this.');
		ini_set('max_execution_time', 3600);
		$this->Event->Behaviors->unload('SysLogLogable.SysLogLogable');
		$date = new DateTime();
		$ts =  $date->getTimestamp();
		$default = array('Event' => array(
			'info' => 'A junk event for load testing',
			'date' => '2014-09-01',
			'threat_level_id' => 4,
			'distribution' => 0,
			'analysis' => 0,
			'org_id' => $this->Auth->user('org_id'),
			'orgc_id' => $this->Auth->user('org_id'),
			'timestamp' => $ts,
			'uuid' => $this->Event->generateUuid(),
			'user_id' => $this->Auth->user('id'),
		));
		$default['Event']['info'] = 'A junk event for load testing';
		$default['Event']['date'] = '2013-10-09';
		$default['Event']['threat_level_id'] = 4; //'Undefined'
		$default['Event']['analysis'] = '0';
		$default['Event']['distribution'] = '0';
		for ($i = 0; $i < 50; $i++) {
			$data = $default;
			for ($j = 0; $j < 3000; $j++) {
				$value = mt_rand();
				$data['Attribute'][] = array(
						'category' => 'Other',
						'type' => 'text',
						'value' => $value,
						'to_ids' => '0',
						'distribution' => '0',
						'value1' => $value,
						'value2' => '',
						'comment' => '',
						'uuid' => $this->Event->generateUuid(),
						'timestamp' => $ts,
				);
			}
			$this->Event->create();
			$this->Event->saveAssociated($data, array('validate' => false));
		}
	}

	public function proposalEventIndex() {
		$this->loadModel('ShadowAttribute');
		$this->ShadowAttribute->recursive = -1;
		$conditions = array('ShadowAttribute.deleted' => 0);
		if (!$this->_isSiteAdmin()) $conditions[] = array('ShadowAttribute.event_org_id' => $this->Auth->user('org_id'));
		$result = $this->ShadowAttribute->find('all', array(
				'fields' => array('event_id'),
				'group' => 'event_id',
				'conditions' => $conditions
		));
		$this->Event->recursive = -1;
		$conditions = array();
		foreach ($result as $eventId) {
				$conditions['OR'][] = array('Event.id =' => $eventId['ShadowAttribute']['event_id']);
		}
		if (empty($result)) {
			$conditions['OR'][] = array('Event.id =' => -1);
		}
		$this->paginate = array(
				'fields' => array('Event.id', 'Event.org_id', 'Event.orgc_id', 'Event.publish_timestamp', 'Event.distribution', 'Event.info', 'Event.date', 'Event.published'),
				'conditions' => $conditions,
				'contain' => array(
					'User' => array(
							'fields' => array(
								'User.email'
					)),
					'ShadowAttribute'=> array(
						'fields' => array(
							'ShadowAttribute.id', 'ShadowAttribute.org_id', 'ShadowAttribute.event_id'
						),
						'conditions' => array(
							'ShadowAttribute.deleted' => 0
						),
					),
		));
		$events = $this->paginate();
		foreach ($events as $k => $event) {
			$orgs = array();
			foreach ($event['ShadowAttribute'] as $sa) {
				if (!in_array($sa['org_id'], $orgs)) $orgs[] = $sa['org_id'];
			}
			$events[$k]['orgArray'] = $orgs;
		}
		$this->set('events', $events);
		$this->set('eventDescriptions', $this->Event->fieldDescriptions);
		$this->set('analysisLevels', $this->Event->analysisLevels);
		$this->set('distributionLevels', $this->Event->distributionLevels);
	}

	private function __setHeaderForAdd($eventId) {
		$this->response->header('Location', Configure::read('MISP.baseurl') . '/events/' . $eventId);
		$this->response->send();
	}

	public function reportValidationIssuesEvents() {
		// search for validation problems in the events
		if (!self::_isSiteAdmin()) throw new NotFoundException();
		$results = $this->Event->reportValidationIssuesEvents();
		$result = $results[0];
		$count = $results[1];
		$this->set('result', $result);
		$this->set('count', $count);
	}

	public function addTag($id = false, $tag_id = false) {
		if (!$this->request->is('post')) {
			return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'You don\'t have permission to do that.')), 'status'=>200));
		}
		if (isset($this->request->data['request'])) $this->request->data = $this->request->data['request'];
		if ($tag_id === false) $tag_id = $this->request->data['Event']['tag'];
		if (!is_numeric($tag_id)) {
			$tag = $this->Event->EventTag->Tag->find('first', array('recursive' => -1, 'conditions' => array('Tag.name' => trim($tag_id))));
			if (empty($tag)) return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'Invalid Tag.')), 'status'=>200));
			$tag_id = $tag['Tag']['id'];
		}
		if (!is_numeric($id)) $id = $this->request->data['Event']['id'];
		$this->Event->recurisve = -1;
		$event = $this->Event->read(array('id', 'org_id', 'orgc_id', 'distribution', 'sharing_group_id'), $id);

		if (!$this->_isSiteAdmin() && !$this->userRole['perm_sync']) {
			if (!$this->userRole['perm_tagger'] || ($this->Auth->user('org_id') !== $event['Event']['org_id'] && $this->Auth->user('org_id') !== $event['Event']['orgc_id'])) {
				return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'You don\'t have permission to do that.')), 'status'=>200));
			}
		}
		$this->Event->EventTag->Tag->id = $tag_id;
		if(!$this->Event->EventTag->Tag->exists()) {
			return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'Invalid Tag.')), 'status'=>200));
		}
		$found = $this->Event->EventTag->find('first', array(
			'conditions' => array(
				'event_id' => $id,
				'tag_id' => $tag_id
			),
			'recursive' => -1,
		));
		$this->autoRender = false;
		if (!empty($found)) return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'Tag is already attached to this event.')), 'status'=>200));
		$this->Event->EventTag->create();
		if ($this->Event->EventTag->save(array('event_id' => $id, 'tag_id' => $tag_id))) {
			$log = ClassRegistry::init('Log');
			$log->createLogEntry($this->Auth->user(), 'tag', 'Event', $id, 'Attached tag (' . $tag_id . ') to event (' . $id . ')', 'Event (' . $id . ') tagged as Tag (' . $tag_id . ')');
			return new CakeResponse(array('body'=> json_encode(array('saved' => true, 'success' => 'Tag added.')), 'status'=>200));
		} else {
			return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'Tag could not be added.')),'status'=>200));
		}
	}

	public function removeTag($id = false, $tag_id = false) {
		if (!$this->request->is('post')) {
			return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'You don\'t have permission to do that.')), 'status'=>200));
		}
		if ($tag_id === false) $tag_id = $this->request->data['Event']['tag'];
		if (!is_numeric($tag_id)) {
			$tag = $this->Event->EventTag->Tag->find('first', array('recursive' => -1, 'conditions' => array('Tag.name' => trim($tag_id))));
			$tag_id = $tag['Tag']['id'];
		}
		if (!is_numeric($id)) $id = $this->request->data['Event']['id'];
		$this->Event->recurisve = -1;
		$event = $this->Event->read(array('id', 'org_id', 'orgc_id', 'distribution'), $id);
		// org should allow to tag too, so that an event that gets pushed can be tagged locally by the owning org
		if ((($this->Auth->user('org_id') !== $event['Event']['org_id'] && $this->Auth->user('org_id') !== $event['Event']['orgc_id'] && $event['Event']['distribution'] == 0) || (!$this->userRole['perm_tagger'])) && !$this->_isSiteAdmin()) {
			return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'You don\'t have permission to do that.')),'status'=>200));
		}
		$eventTag = $this->Event->EventTag->find('first', array(
			'conditions' => array(
				'event_id' => $id,
				'tag_id' => $tag_id
			),
			'recursive' => -1,
		));
		$this->autoRender = false;
		if (empty($eventTag)) return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'Invalid event - tag combination.')),'status'=>200));
		if ($this->Event->EventTag->delete($eventTag['EventTag']['id'])) {
			return new CakeResponse(array('body'=> json_encode(array('saved' => true, 'success' => 'Tag removed.')), 'status'=>200));
		} else {
			return new CakeResponse(array('body'=> json_encode(array('saved' => false, 'errors' => 'Tag could not be removed.')),'status'=>200));
		}
	}

	public function freeTextImport($id) {
		if (!$this->userRole['perm_add']) {
			throw new MethodNotAllowedException('Event not found or you don\'t have permissions to create attributes');
		}
		$event = $this->Event->find('first', array(
				'conditions' => array('Event.id' => $id),
				'fields' => array('id', 'orgc_id'),
				'recursive' => -1
		));
		$this->set('event_id', $id);
		if ($this->request->is('get')) {
			$this->layout = 'ajax';
			$this->request->data['Attribute']['event_id'] = $id;
		}

		if ($this->request->is('post')) {
			App::uses('ComplexTypeTool', 'Tools');
			$complexTypeTool = new ComplexTypeTool();
			$resultArray = $complexTypeTool->checkComplexRouter($this->request->data['Attribute']['value'], 'FreeText');
			foreach ($resultArray as &$r) {
				$temp = array();
				foreach ($r['types'] as $type) {
					$temp[$type] = $type;
				}
				$r['types'] = $temp;
			}

			// remove all duplicates
			foreach ($resultArray as $k => $v) {
				for ($i = 0; $i < $k; $i++) {
					if (isset($resultArray[$i]) && $v == $resultArray[$i]) unset ($resultArray[$k]);
				}
			}
			foreach ($resultArray as &$result) {
				$options = array(
					'conditions' => array('OR' => array('Attribute.value1' => $result['value'], 'Attribute.value2' => $result['value'])),
					'fields' => array('Attribute.type', 'Attribute.category', 'Attribute.value', 'Attribute.comment'),
					'order' => false
				);
				$result['related'] = $this->Event->Attribute->fetchAttributes($this->Auth->user(), $options);
			}
			$resultArray = array_values($resultArray);
			$typeCategoryMapping = array();
			foreach ($this->Event->Attribute->categoryDefinitions as $k => $cat) {
				foreach ($cat['types'] as $type) {
					$typeCategoryMapping[$type][$k] = $k;
				}
			}
			$this->set('event', $event);
			$this->set('typeList', array_keys($this->Event->Attribute->typeDefinitions));
			$this->set('defaultCategories', $this->Event->Attribute->defaultCategories);
			$this->set('typeCategoryMapping', $typeCategoryMapping);
			$this->set('resultArray', $resultArray);
			$this->set('title', 'Freetext Import Results');
			$this->render('resolved_attributes');
		}
	}

	public function saveFreeText($id) {
		if (!$this->userRole['perm_add']) {
			throw new MethodNotAllowedException('Event not found or you don\'t have permissions to create attributes');
		}
		if ($this->request->is('post')) {
			$event = $this->Event->find('first', array(
				'conditions' => array('id' => $id),
				'recursive' => -1,
				'fields' => array('orgc_id', 'id', 'distribution', 'published', 'uuid'),
			));
			if (!$this->_isSiteAdmin() && !empty($event) && $event['Event']['orgc_id'] != $this->Auth->user('org_id')) $objectType = 'ShadowAttribute';
			else if ($this->_isSiteAdmin() && isset($this->request->data['Attribute']['force']) && $this->request->data['Attribute']['force']) $objectType = 'ShadowAttribute';
			else $objectType = 'Attribute';
			$saved = 0;
			$failed = 0;
			$attributes = json_decode($this->request->data['Attribute']['JsonObject'], true);
			$attributeSources = array('attributes', 'ontheflyattributes');
			$ontheflyattributes = array();
			foreach ($attributeSources as $source) {
				foreach (${$source} as $k => $attribute) {
					if ($attribute['type'] == 'ip-src/ip-dst') {
						$types = array('ip-src', 'ip-dst');
					} else if ($attribute['type'] == 'malware-sample') {
						$result = $this->Event->Attribute->handleMaliciousBase64($id, $attribute['value'], $attribute['data'], array('md5', 'sha1', 'sha256'), $objectType == 'ShadowAttribute' ? true : false);
						$shortValue = $attribute['value'];
						$attribute['value'] = $shortValue . '|' . $result['md5'];
						$attribute['data'] = $result['data'];
						$additionalHashes = array('sha1', 'sha256');
						foreach ($additionalHashes as $hash) {
							$temp = $attribute;
							$temp['type'] = 'filename|' . $hash;
							$temp['value'] = $shortValue . '|' . $result[$hash];
							unset($temp['data']);
							$ontheflyattributes[] = $temp;
						}
						$types = array($attribute['type']);
					} else {
						$types = array($attribute['type']);
					}
					foreach ($types as $type) {
						$this->Event->$objectType->create();
						$attribute['type'] = $type;
						$attribute['distribution'] = 5;
						if (empty($attribute['comment'])) $attribute['comment'] = 'Imported via the freetext import.';
						$attribute['event_id'] = $id;
						if ($objectType == 'ShadowAttribute') {
							$attribute['org_id'] = $this->Auth->user('org_id');
							$attribute['event_org_id'] = $event['Event']['orgc_id'];
							$attribute['email'] = $this->Auth->user('email');
							$attribute['event_uuid'] = $event['Event']['uuid'];
						}
						if ($this->Event->$objectType->save($attribute)) {
							$saved++;
						} else {
							$failed++;
						}
					}
				}
			}
			if ($saved > 0) {
				$event = $this->Event->find('first', array(
						'conditions' => array('Event.id' => $id),
						'recursive' => -1
				));
				if ($event['Event']['published'] == 1) {
					$event['Event']['published'] = 0;
				}
				$date = new DateTime();
				$event['Event']['timestamp'] = $date->getTimestamp();
				$this->Event->save($event);
			}
			if ($failed > 0) {
				$this->Session->setFlash($saved . ' attributes created. ' . $failed . ' attributes could not be saved. This may be due to attributes with similar values already existing.');
			} else {
				$this->Session->setFlash($saved . ' attributes created.');
			}
			$this->redirect(array('controller' => 'events', 'action' => 'view', $id));
		} else {
			throw new MethodNotAllowedException();
		}
	}

	public function stix($key, $id = false, $withAttachments = false, $tags = false, $from = false, $to = false, $last = false) {
		if ($key != 'download') {
			// check if the key is valid -> search for users based on key
			$user = $this->checkAuthUser($key);
			if (!$user) {
				throw new UnauthorizedException('This authentication key is not authorized to be used for exports. Contact your administrator.');
			}
			$isSiteAdmin = $user['User']['siteAdmin'];
		} else {
			if (!$this->Auth->user('id')) {
				throw new UnauthorizedException('You have to be logged in to do that.');
			}
			$isSiteAdmin = $this->_isSiteAdmin();
		}

		// request handler for POSTed queries. If the request is a post, the parameters (apart from the key) will be ignored and replaced by the terms defined in the posted xml object.
		// The correct format for a posted xml is a "request" root element, as shown by the examples below:
		// For XML: <request><id>!3&amp;!4</id><tags>OSINT</tags></request>
		// This would return all OSINT tagged events except for event #3 and #4
		if ($this->request->is('post')) {
			if (empty($this->request->data)) {
				throw new BadRequestException('Either specify the search terms in the url, or POST an xml (with the root element being "request".');
			} else {
				$data = $this->request->data;
			}
			$paramArray = array('id', 'withAttachment', 'tags', 'from', 'to', 'last');
			foreach ($paramArray as $p) {
				if (isset($data['request'][$p])) ${$p} = $data['request'][$p];
				else ${$p} = null;
			}
		}

		$simpleFalse = array('id', 'withAttachments', 'tags', 'from', 'to', 'last');
		foreach ($simpleFalse as $sF) {
			if (!is_array(${$sF}) && (${$sF} === 'null' || ${$sF} == '0' || ${$sF} === false || strtolower(${$sF}) === 'false')) ${$sF} = false;
		}
		if ($from) $from = $this->Event->dateFieldCheck($from);
		if ($to) $to = $this->Event->dateFieldCheck($to);
		if ($last) $last = $this->Event->resolveTimeDelta($last);

		// set null if a null string is passed
		$numeric = false;
		if (is_numeric($id)) $numeric = true;
		// set the export type based on the request
		if ($this->response->type() === 'application/json') $returnType = 'json';
		else {
			$returnType = 'xml';
			$this->response->type('xml');	// set the content type
			$this->layout = 'xml/default';
		}
		$result = $this->Event->stix($id, $tags, $withAttachments, $this->Auth->user(), $returnType, $from, $to, $last);
		if ($result['success'] == 1) {
			// read the output file and pass it to the view
			if (!$numeric) {
				$this->header('Content-Disposition: download; filename="misp.stix.event.collection.' . $returnType . '"');
			} else {
				$this->header('Content-Disposition: download; filename="misp.stix.event' . $id . '.' . $returnType . '"');
			}
			$this->set('data', $result['data']);
		} else {
			throw new Exception(h($result['message']));
		}
	}

	public function filterEventIdsForPush() {
		if (!$this->userRole['perm_sync']) throw new MethodNotAllowedException('You do not have the permission to do that.');
		if ($this->request->is('post')) {
			$incomingIDs = array();
			$incomingEvents = array();
			foreach ($this->request->data as $event) {
				$incomingIDs[] = $event['Event']['uuid'];
				$incomingEvents[$event['Event']['uuid']] = $event['Event']['timestamp'];
			}
			$events = $this->Event->find('all', array(
				'conditions' => array('Event.uuid' => $incomingIDs),
				'recursive' => -1,
				'fields' => array('Event.uuid', 'Event.timestamp', 'Event.locked'),
			));
			foreach ($events as $k => $v) {
				if ($v['Event']['timestamp'] >= $incomingEvents[$v['Event']['uuid']]) {
					unset($incomingEvents[$v['Event']['uuid']]);
					continue;
				}
				if ($v['Event']['locked'] == 0) {
					unset($incomingEvents[$v['Event']['uuid']]);
				}
			}
			$this->set('result', array_keys($incomingEvents));
		}
	}

	public function checkuuid($uuid) {
		if (!$this->userRole['perm_sync']) throw new MethodNotAllowedException('You do not have the permission to do that.');
		$events = $this->Event->find('first', array(
				'conditions' => array('Event.uuid' => $uuid),
				'recursive' => -1,
				'fields' => array('Event.uuid'),
		));
		$this->set('result', array('result' => empty($events)));
	}

	public function pushProposals($uuid) {
		$message= "";
		$success = true;
		$counter = 0;
		if (!$this->userRole['perm_sync']) throw new MethodNotAllowedException('You do not have the permission to do that.');
		if ($this->request->is('post')) {
			$event = $this->Event->find('first', array(
					'conditions' => array('Event.uuid' => $uuid),
					'contains' => array('ShadowAttribute', 'Attribute' => array(
						'fields' => array('id', 'uuid', 'event_id'),
					)),
					'fields' => array('Event.uuid', 'Event.id'),
			));
			if (empty($event)) {
				$message = "Event not found.";
				$success = false;
			} else {
				foreach ($this->request->data as $k => $sa) {
					if (isset($event['ShadowAttribute'])) {
						foreach ($event['ShadowAttribute'] as $oldk => $oldsa) {
							$temp = json_encode($oldsa);
							if ($sa['event_uuid'] == $oldsa['event_uuid'] && $sa['value'] == $oldsa['value'] && $sa['type'] == $oldsa['type'] && $sa['category'] == $oldsa['category'] && $sa['to_ids'] == $oldsa['to_ids']) {
								if ($oldsa['timestamp'] < $sa['timestamp']) $this->Event->ShadowAttribute->delete($oldsa['id']);
								else continue 2;
							}
						}
					}
					$sa['event_id'] = $event['Event']['id'];
					if ($sa['old_id'] != 0) {
						foreach($event['Attribute'] as $attribute) {
							if ($sa['uuid'] == $attribute['uuid']) {
								$sa['old_id'] = $attribute['id'];
							}
						}
					}
					if (isset($sa['id'])) unset($sa['id']);
					$this->Event->ShadowAttribute->create();
					if (!$this->Event->ShadowAttribute->save(array('ShadowAttribute' => $sa))) {
						$message = "Some of the proposals could not be saved.";
						$success = false;
					} else {
						$counter++;
					}
					if (!$sa['deleted']) $this->Event->ShadowAttribute->__sendProposalAlertEmail($event['Event']['id']);
				}
			}
			if ($success) {
				if ($counter) {
					$message = $counter . " Proposal(s) added.";
				} else {
					$message = "Nothing to update.";
				}
			}
			$this->set('data', array('success' => $success, 'message' => $message, 'counter' => $counter));
			$this->set('_serialize', 'data');
		}
	}

	public function exportChoice($id) {
		if (!is_numeric($id)) throw new MethodNotAllowedException('Invalid ID');
		$event = $this->Event->fetchEvent($this->Auth->user(), array('eventid' => $id));
		if (empty($event)) throw new NotFoundException('Event not found or you are not authorised to view it.');
		$event = $event[0];
		$exports = array(
			'xml' => array(
					'url' => '/events/restsearch/download/false/false/false/false/false/false/false/false/false/' . $id . '/false.xml',
					'text' => 'MISP XML (metadata + all attributes)',
					'requiresPublished' => false,
					'checkbox' => true,
					'checkbox_text' => 'Encode Attachments',
					'checkbox_set' => '/events/restsearch/download/false/false/false/false/false/false/false/false/false/' . $id . '/true.xml',
					'checkbox_default' => true
			),
			'json' => array(
					'url' => '/events/restsearch/download/false/false/false/false/false/false/false/false/false/' . $id . '/false.json',
					'text' => 'MISP JSON (metadata + all attributes)',
					'requiresPublished' => false,
					'checkbox' => true,
					'checkbox_text' => 'Encode Attachments',
					'checkbox_set' => '/events/restsearch/download/false/false/false/false/false/false/false/false/false/' . $id . '/true.json',
					'checkbox_default' => true
			),
			'openIOC' => array(
					'url' => '/events/downloadOpenIOCEvent/' . $id,
					'text' => 'OpenIOC (all indicators marked to IDS)',
					'requiresPublished' => true,
					'checkbox' => false,
			),
			'csv' => array(
					'url' => '/events/csv/download/' . $id,
					'text' => 'CSV',
					'requiresPublished' => true,
					'checkbox' => true,
					'checkbox_text' => 'Include non-IDS marked attributes',
					'checkbox_set' => '/events/csv/download/' . $id . '/1'
			),
			'stix_xml' => array(
					'url' => '/events/stix/download/' . $id . '.xml',
					'text' => 'STIX XML (metadata + all attributes)',
					'requiresPublished' => true,
					'checkbox' => true,
					'checkbox_text' => 'Encode Attachments',
					'checkbox_set' => '/events/stix/download/' . $id . '/true.xml'
			),
			'stix_json' => array(
					'url' => '/events/stix/download/' . $id . '.json',
					'text' => 'STIX JSON (metadata + all attributes)',
					'requiresPublished' => true,
					'checkbox' => true,
					'checkbox_text' => 'Encode Attachments',
					'checkbox_set' => '/events/stix/download/' . $id . '/true.json'
			),
			'rpz' => array(
					'url' => '/attributes/rpz/download/false/' . $id,
					'text' => 'RPZ Zone file',
					'requiresPublished' => true,
					'checkbox' => false,
			),
			'suricata' => array(
					'url' => '/events/nids/suricata/download/' . $id,
					'text' => 'Download Suricata rules',
					'requiresPublished' => true,
					'checkbox' => false,
			),
			'snort' => array(
					'url' => '/events/nids/snort/download/' . $id,
					'text' => 'Download Snort rules',
					'requiresPublished' => true,
					'checkbox' => false,
			),
			'text' => array(
					'url' => '/attributes/text/download/all/false/' . $id,
					'text' => 'Export all attribute values as a text file',
					'requiresPublished' => true,
					'checkbox' => true,
					'checkbox_text' => 'Include non-IDS marked attributes',
					'checkbox_set' => '/attributes/text/download/all/false/' . $id . '/true'
			),
		);
		if ($event['Event']['published'] == 0) {
			foreach ($exports as $k => $export) {
				if ($export['requiresPublished']) unset($exports[$k]);
			}
			$exports['csv'] = array(
				'url' => '/events/csv/download/' . $id . '/1',
				'text' => 'CSV (event not published, IDS flag ignored)',
				'requiresPublished' => false,
				'checkbox' => false
			);
		}
		$this->set('exports', $exports);
		$this->set('id', $id);
		$this->render('ajax/exportChoice');
	}

	// API for pushing samples to MISP
	// Either send it to an existing event, or let MISP create a new one automatically
	public function upload_sample($event_id = null) {
		$this->loadModel('Log');
		$hashes = array('md5' => 'malware-sample', 'sha1' => 'filename|sha1', 'sha256' => 'filename|sha256');
		$categoryDefinitions = $this->Event->Attribute->categoryDefinitions;
		$types = array();
		foreach ($categoryDefinitions as $k => $v) {
			if (in_array('malware-sample', $v['types']) && !in_array($k, $types)) $types[] = $k;
		}
		$parameter_options = array(
				'distribution' => array('valid_options' => array(0, 1, 2, 3), 'default' => 0),
				'threat_level_id' => array('valid_options' => array(1, 2, 3, 4), 'default' => 4),
				'analysis' => array('valid_options' => array(0, 1, 2), 'default' => 0),
				'info' => array('default' =>  'Malware samples uploaded on ' . date('Y-m-d')),
				'to_ids' => array('valid_options' => array(0, 1), 'default' => 1),
				'category' => array('valid_options' => $types, 'default' => 'Payload installation')
		);


		if (!$this->userRole['perm_auth']) throw new MethodNotAllowedException('This functionality requires API key access.');
		if (!$this->request->is('post')) throw new MethodNotAllowedException('Please POST the samples as described on the automation page.');
		$isJson = false;
		if ($this->response->type() === 'application/json') {
			$isJson = true;
			$data = $this->request->input('json_decode', true);
		} elseif ($this->response->type() === 'application/xml') {
			$data = $this->request->data;
		} else {
			throw new BadRequestException('Please POST the samples as described on the automation page.');
		}

		if (isset($data['request'])) $data = $data['request'];

		foreach ($parameter_options as $k => $v) {
			if (isset($data[$k])) {
				if (isset($v['valid_options']) && !in_array($data[$k], $v['valid_options'])) $data[$k] = $v['default'];
			} else {
				$data[$k] = $v['default'];
			}
		}

		if (isset($data['files'])) {
			foreach ($data['files'] as $k => $file) {
				if (!isset($file['filename']) || !isset($file['data'])) unset ($data['files'][$k]);
				else $data['files'][$k]['md5'] = md5(base64_decode($file['data']));
			}
		}

		if (empty($data['files'])) throw new BadRequestException('No samples received, or samples not in the correct format. Please refer to the API documentation on the automation page.');
		if (isset($event_id)) $data['event_id'] = $event_id;

		// check if the user has permission to create attributes for an event, if the event ID has been passed
		// If not, create an event
		if (isset($data['event_id']) && !empty($data['event_id']) && is_numeric($data['event_id'])) {
			$conditions = array();
			if (!$this->_isSiteAdmin()) {
				$conditions = array('Event.orgc_id' => $this->Auth->user('org_id'));
				if (!$this->userRole['perm_modify_org']) $conditions[] = array('Event.user_id' => $this->Auth->user('id'));
			}
			$event = $this->Event->find('first', array(
				'recursive' => -1,
				'conditions' => $conditions,
				'fields' => array('id'),
			));
			if (empty($event)) throw new MethodNotFoundException('Event not found.');
			$this->Event->id = $data['event_id'];
			$this->Event->saveField('published', 0);
		} else {
			$this->Event->create();
			$result = $this->Event->save(
				array(
					'info' => $data['info'],
					'analysis' => $data['analysis'],
					'threat_level_id' => $data['threat_level_id'],
					'distribution' => $data['distribution'],
					'date' => date('Y-m-d'),
					'orgc_id' => $this->Auth->user('org_id'),
					'org_id' => $this->Auth->user('org_id'),
					'user_id' => $this->Auth->user('id'),
				)
			);
			if (!$result) {
				$this->Log->save(array(
						'org' => $this->Auth->user('Organisation')['name'],
						'model' => 'Event',
						'model_id' => 0,
						'email' => $this->Auth->user('email'),
						'action' => 'upload_sample',
						'user_id' => $this->Auth->user('id'),
						'title' => 'Error: Failed to create event using the upload sample functionality',
						'change' => 'There was an issue creating an event (' . $data['info'] . '). The validation errors were: ' . json_encode($this->Event->validationErrors),
				));
				throw new BadRequestException('The creation of a new event with the supplied information has failed.');
			}
			$data['event_id'] = $this->Event->id;
		}

		if (!isset($data['to_ids']) || !in_array($data['to_ids'], array('0', '1', 0, 1))) $data['to_ids'] = 1;
		$successCount = 0;
		$errors = array();
		foreach ($data['files'] as $file) {
			$temp = $this->Event->Attribute->handleMaliciousBase64($data['event_id'], $file['filename'], $file['data'], array_keys($hashes));
			if ($temp['success']) {
				foreach ($hashes as $hash => $typeName) {
					if ($temp[$hash] == false) continue;
					$file[$hash] = $temp[$hash];
					$file['data'] = $temp['data'];
					$this->Event->Attribute->create();
					$attribute = array(
							'value' => $file['filename'] . '|' . $file[$hash],
							'distribution' => $data['distribution'],
							'category' => $data['category'],
							'type' => $typeName,
							'event_id' => $data['event_id'],
							'to_ids' => $data['to_ids']
					);
					if ($hash == 'md5') $attribute['data'] = $file['data'];
					$result = $this->Event->Attribute->save($attribute);
					if (!$result) {
						$this->Log->save(array(
								'org' => $this->Auth->user('Organisation')['name'],
								'model' => 'Event',
								'model_id' => $data['event_id'],
								'email' => $this->Auth->user('email'),
								'action' => 'upload_sample',
								'user_id' => $this->Auth->user('id'),
								'title' => 'Error: Failed to create attribute using the upload sample functionality',
								'change' => 'There was an issue creating an attribute (' . $typeName . ': ' . $file['filename'] . '|' . $file[$hash] . '). ' . 'The validation errors were: ' . json_encode($this->Event->Attribute->validationErrors),
						));
						if ($typeName == 'malware-sample') $errors[] = array('filename' => $file['filename'], 'hash' => $file[$hash], 'error' => $this->Event->Attribute->validationErrors);
					} else if ($typeName == 'malware-sample') $successCount++;
				}
			} else {
				$errors[] = array('filename' => $file['filename'], 'hash' => $file['hash'], 'error' => 'Failed to encrypt and compress the file.');
			}
		}
		if (!empty($errors)) {
			$this->set('errors', $errors);
			if ($successCount > 0) {
				$this->set('name', 'Partial success');
				$this->set('message', 'Successfuly saved ' . $successCount . ' sample(s), but some samples could not be saved.');
				$this->set('url', '/events/view/' . $data['event_id']);
				$this->set('id', $data['event_id']);
				$this->set('_serialize', array('name', 'message', 'url', 'id', 'errors'));
			} else {
				$this->set('name', 'Failed');
				$this->set('message', 'Failed to save any of the supplied samples.');
				$this->set('_serialize', array('name', 'message', 'errors'));
			}
		} else {
			$this->set('name', 'Success');
			$this->set('message', 'Success, saved all attributes.');
			$this->set('url', '/events/view/' . $data['event_id']);
			$this->set('id', $data['event_id']);
			$this->set('_serialize', array('name', 'message', 'url', 'id'));
		}
		$this->view($data['event_id']);
		$this->render('view');
	}

	public function viewGraph($id) {
		$event = $this->Event->fetchEvent($this->Auth->user(), array('eventid' => $id));
		if (empty($event)) throw new MethodNotAllowedException('Invalid Event.');
		$this->set('event', $event[0]);
		//$this->layout = 'graph';
		$this->set('id', $id);
	}

	public function updateGraph($id) {
		if ($this->request->is('post')) {
			$oldArray = $this->request->data;
			$json = $this->__buildGraphJson($id, $this->request->data);
		} else {
			$json = $this->__buildGraphJson($id);
		}
		$this->set('json', $json);
		$this->set('_serialize', 'json');
	}

	private function __buildGraphJson($id, $json = array()) {
		$event = $this->Event->fetchEvent($this->Auth->user(), array('eventid' => $id));
		if (empty($event)) return $json;
		$json = $this->__cleanLinks($json);
		$old_event = $this->__graphJsonContains('event', $event[0]['Event'], $json);
		if ($old_event !== false) {
			$json['nodes'][$old_event]['expanded'] = 1;
			$current_event_id = $old_event;
		} else {
			if ($this->__orgImgExists($event[0]['Orgc']['name'])) $image = Configure::read('MISP.baseurl') . '/img/orgs/' . h($event[0]['Orgc']['name']) . '.png';
			else $image = Configure::read('MISP.baseurl') . '/img/orgs/MISP.png';
			$json['nodes'][] = array(
					'name' => 'Event ' . $id,
					'type' => 'event',
					'id' => $id, 'expanded' => 1,
					'image' => $image,
					'info' => $event[0]['Event']['info'],
					'org' => $event[0]['Orgc']['name'],
					'analysis' => $this->Event->analysisLevels[$event[0]['Event']['analysis']],
					'distribution' => $this->Event->distributionLevels[$event[0]['Event']['distribution']],
					'date' => $event[0]['Event']['date']
			);
			$current_event_id = count($json['nodes'])-1;
		}
		$relatedEvents = array();
		if (!empty($event[0]['RelatedEvent'])) foreach ($event[0]['RelatedEvent'] as &$re) {
			$relatedEvents[$re['Event']['id']] = $re;
		}
		foreach ($event[0]['Attribute'] as $k => $att) {
			if (isset($event[0]['RelatedAttribute'][$att['id']])) {
				$current_attribute_id = $this->__graphJsonContains('attribute', $att, $json);
				if ($current_attribute_id === false) {
					$json['nodes'][] = array(
							'name' => $att['value'],
							'type' => 'attribute',
							'id' => $att['id'],
							'att_category' => $att['category'],
							'att_type' => $att['type'],
							'image' => '/img/indicator.png',
							'att_ids' => $att['to_ids'],
							'comment' => $att['comment']
					);
					$current_attribute_id = count($json['nodes'])-1;
				}
				$l1 = $this->__graphJsonContainsLink($current_event_id, $current_attribute_id, $json);
				if ($l1 === false) $json['links'][] = array('source' => $current_event_id, 'target' => $current_attribute_id);
				foreach($event[0]['RelatedAttribute'][$att['id']] as $relation) {
					$found = $this->__graphJsonContains('event', $relation, $json);
					if ($found !== false) {
						$l3 = $this->__graphJsonContainsLink($found, $current_attribute_id, $json);
						if ($l3 === false) $json['links'][] = array('source' => $found, 'target' => $current_attribute_id);
					} else {
						$current_relation_id = $this->__graphJsonContains('event', $relation, $json);
						if ($current_relation_id === false) {
							if ($this->__orgImgExists($relatedEvents[$relation['id']]['Orgc']['name'])) $image = '/img/orgs/' . $relatedEvents[$relation['id']]['Orgc']['name'] . '.png';
							else $image = '/img/orgs/MISP.png';
							$json['nodes'][] = array(
									'name' => 'Event ' . $relation['id'],
									'type' => 'event', 'id' => $relation['id'],
									'expanded' => 0, 'image' => $image,
									'info' => $relatedEvents[$relation['id']]['Event']['info'],
									'org' => $relatedEvents[$relation['id']]['Orgc']['name'],
									'analysis' => $this->Event->analysisLevels[$relatedEvents[$relation['id']]['Event']['analysis']],
									'date' => $relatedEvents[$relation['id']]['Event']['date']
							);
							$current_relation_id = count($json['nodes'])-1;
						}
						$l2 = $this->__graphJsonContainsLink($current_attribute_id, $current_relation_id, $json);
						if ($l2 === false) $json['links'][] = array('source' => $current_attribute_id, 'target' => $current_relation_id);
					}
				}
			}
		}
		return $json;
	}

	private function __cleanLinks($json) {
		if (isset($json['nodes']) && isset($json['links'])) {
			$links = array();
			foreach ($json['links'] as $link) {
				$temp = array();
				foreach ($json['nodes'] as $k => $node) {
					if ($link['source'] == $node) $temp['source'] = $k;
					if ($link['target'] == $node) $temp['target'] = $k;
				}
				$links[] = $temp;
			}
			$json['links'] = $links;
		}
		return $json;
	}

	private function __orgImgExists($org) {
		if (file_exists(APP . 'webroot' . DS . 'img' . DS . 'orgs' . DS . $org . '.png')) return true;
		return false;
	}

	private function __graphJsonContains($type, $att, $json) {
		if (!isset($json['nodes'])) return false;
		foreach ($json['nodes'] as $k => $node) {
			if ($type == 'event' && $node['type'] == 'event' && $node['id'] == $att['id']) return $k;
			if ($type == 'attribute' &&
			$node['type'] == 'attribute' &&
			$node['name'] == $att['value']) {
				return $k;
			}
		}
		return false;
	}
	private function __graphJsonContainsLink($id1, $id2, $json) {
		if (!isset($json['links'])) return false;
		foreach ($json['links'] as $k => $link) {
			if (($link['source'] == $id1 && $link['target'] == $id2) || ($link['source'] == $id2 && $link['target'] == $id1)) {
				return $k;
			}
		}
		return false;
	}

	public function delegation_index() {
		$this->loadmodel('EventDelegation');
		$delegatedEvents = $this->EventDelegation->find('list', array(
				'conditions' => array('EventDelegation.org_id' => $this->Auth->user('org_id')),
				'fields' => array('event_id')
		));
		$this->Event->contain(array('User.email', 'EventTag' => array('Tag')));
		$tags = $this->Event->EventTag->Tag->find('all', array('recursive' => -1));
		$tagNames = array('None');
		foreach ($tags as $k => $v) {
			$tagNames[$v['Tag']['id']] = $v['Tag']['name'];
		}
		$this->set('tags', $tagNames);
		$this->paginate = array(
			'limit' => 60,
			'maxLimit' => 9999,	// LATER we will bump here on a problem once we have more than 9999 events <- no we won't, this is the max a user van view/page.
			'order' => array(
					'Event.timestamp' => 'DESC'
			),
			'contain' => array(
					'Org' => array('fields' => array('id', 'name')),
					'Orgc' => array('fields' => array('id', 'name')),
					'SharingGroup' => array('fields' => array('id', 'name')),
					'ThreatLevel' => array('fields' => array('ThreatLevel.name'))

			),
			'conditions' => array('Event.id' => $delegatedEvents),
		);

		$this->set('events', $this->paginate());
		$threat_levels = $this->Event->ThreatLevel->find('all');
		$this->set('threatLevels', Set::combine($threat_levels, '{n}.ThreatLevel.id', '{n}.ThreatLevel.name'));
		$this->set('eventDescriptions', $this->Event->fieldDescriptions);
		$this->set('analysisLevels', $this->Event->analysisLevels);
		$this->set('distributionLevels', $this->Event->distributionLevels);

		$shortDist = array(0 => 'Organisation', 1 => 'Community', 2 => 'Connected', 3 => 'All', 4 => ' sharing Group');
		$this->set('shortDist', $shortDist);
		$this->set('ajax', false);
		$this->set('simple', true);
		$this->Event->contain(array('User.email', 'EventTag' => array('Tag')));
		$tags = $this->Event->EventTag->Tag->find('all', array('recursive' => -1));
		$tagNames = array('None');
		foreach ($tags as $k => $v) {
			$tagNames[$v['Tag']['id']] = $v['Tag']['name'];
		}
		$this->set('tags', $tagNames);
		$this->render('index');
	}

	// expects an attribute ID and the module to be used
	public function queryEnrichment($attribute_id, $module = false) {
		if (!Configure::read('Plugin.Enrichment_services_enable')) throw new MethodNotAllowedException('Enrichment services are not enabled.');
		$attribute = $this->Event->Attribute->fetchAttributes($this->Auth->user(), array('conditions' => array('Attribute.id' => $attribute_id)));
		if (empty($attribute)) throw new MethodNotAllowedException('Attribute not found or you are not authorised to see it.');
		if ($this->request->is('ajax')) {
			$this->loadModel('Server');
			$modules = $this->Server->getEnabledModules();
			if (!is_array($modules) || empty($modules)) throw new MethodNotAllowedException('No valid enrichment options found for this attribute.');
			$temp = array();
			foreach ($modules['modules'] as &$module) {
				if (in_array($attribute[0]['Attribute']['type'], $module['mispattributes']['input'])) {
					$temp[] = array('name' => $module['name'], 'description' => $module['meta']['description']);
				}
			}
			$modules = &$temp;
			foreach (array('attribute_id', 'modules') as $viewVar) $this->set($viewVar, $$viewVar);
			$this->render('ajax/enrichmentChoice');
		} else {
			$this->loadModel('Server');
			$modules = $this->Server->getEnabledModules();
			if (!is_array($modules) || empty($modules)) throw new MethodNotAllowedException('No valid enrichment options found for this attribute.');
			$options = array();
			$found = false;
			foreach ($modules['modules'] as &$temp) {
				if ($temp['name'] == $module) {
					$found = true;
					if (isset($temp['meta']['config'])) {
						foreach ($temp['meta']['config'] as $conf) $options[$conf] = Configure::read('Plugin.Enrichment_' . $module . '_' . $conf);
					}
				}
			}
			if (!$found) throw new MethodNotAllowedException('No valid enrichment options found for this attribute.');
			$url = Configure::read('Plugin.Enrichment_services_url') ? Configure::read('Plugin.Enrichment_services_url') : $this->Server->serverSettings['Plugin']['Enrichment_services_url']['value'];
			$port = Configure::read('Plugin.Enrichment_services_port') ? Configure::read('Plugin.Enrichment_services_port') : $this->Server->serverSettings['Plugin']['Enrichment_services_port']['value'];
			App::uses('HttpSocket', 'Network/Http');
			$httpSocket = new HttpSocket();
			$data = array('module' => $module, $attribute[0]['Attribute']['type'] => $attribute[0]['Attribute']['value']);
			if (!empty($options)) $data['config'] = $options;
			$data = json_encode($data);
			try {
				$response = $httpSocket->post($url . ':' . $port . '/query', $data);
				$result = json_decode($response->body, true);
			} catch (Exception $e) {
				return 'Enrichment service not reachable.';
			}
			if (!is_array($result)) throw new Exception($result);
			$resultArray = array();
			if (isset($result['results']) && !empty($result['results'])) {
				foreach ($result['results'] as $result) {
					if (!is_array($result['values'])) $result['values'] = array($result['values']);
					foreach ($result['values'] as $value) {
						 $temp = array(
							'event_id' => $attribute[0]['Attribute']['event_id'],
							'types' => $result['types'],
							'default_type' => $result['types'][0],
							'comment' => isset($result['comment']) ? $result['comment'] : false,
							'to_ids' => isset($result['to_ids']) ? $result['to_ids'] : false,
							'value' => $value
						);
						if (isset($result['data'])) $temp['data'] = $result['data'];
						$resultArray[] = $temp;
					}
				}
			}
			$typeCategoryMapping = array();
			foreach ($this->Event->Attribute->categoryDefinitions as $k => $cat) {
				foreach ($cat['types'] as $type) {
					$typeCategoryMapping[$type][$k] = $k;
				}
			}
			foreach ($resultArray as &$result) {
				$options = array(
						'conditions' => array('OR' => array('Attribute.value1' => $result['value'], 'Attribute.value2' => $result['value'])),
						'fields' => array('Attribute.type', 'Attribute.category', 'Attribute.value', 'Attribute.comment'),
						'order' => false
				);
				$result['related'] = $this->Event->Attribute->fetchAttributes($this->Auth->user(), $options);
			}
			$this->set('event', array('Event' => $attribute[0]['Event']));
			$this->set('resultArray', $resultArray);
			$this->set('typeList', array_keys($this->Event->Attribute->typeDefinitions));
			$this->set('defaultCategories', $this->Event->Attribute->defaultCategories);
			$this->set('typeCategoryMapping', $typeCategoryMapping);
			$this->set('title', 'Enrichment Results');
			$this->render('resolved_attributes');
		}
	}
}