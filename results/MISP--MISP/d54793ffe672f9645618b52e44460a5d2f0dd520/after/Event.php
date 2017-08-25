<?php
App::uses('AppModel', 'Model');

App::import('Controller', 'Attributes');
/**
 * Event Model
 *
 * @property User $User
 * @property Attribute $Attribute
 */
class Event extends AppModel {

	public $actsAs = array(
		'SysLogLogable.SysLogLogable' => array(	// TODO Audit, logable
			'userModel' => 'User',
			'userKey' => 'user_id',
			'change' => 'full'),
		'Trim',
		'Containable',
	);

/**
 * Display field
 *
 * @var string
 */
	public $displayField = 'id';

	public $virtualFields = array();

/**
 * Description field
 *
 * @var array
 */
	public $fieldDescriptions = array(
		'risk' => array('desc' => 'Risk levels: *low* means mass-malware, *medium* means APT malware, *high* means sophisticated APT malware or 0-day attack', 'formdesc' => 'Risk levels: low: mass-malware medium: APT malware high: sophisticated APT malware or 0-day attack'),
		'classification' => array('desc' => 'Set the Traffic Light Protocol classification. <ol><li><em>TLP:AMBER</em>- Share only within the organization on a need-to-know basis</li><li><em>TLP:GREEN:NeedToKnow</em>- Share within your constituency on the need-to-know basis.</li><li><em>TLP:GREEN</em>- Share within your constituency.</li></ol>'),
		'submittedgfi' => array('desc' => 'GFI sandbox: export upload', 'formdesc' => 'GFI sandbox: export upload'),
		'submittedioc' => array('desc' => '', 'formdesc' => ''),
		'analysis' => array('desc' => 'Analysis Levels: *Initial* means the event has just been created, *Ongoing* means that the event is being populated, *Complete* means that the event\'s creation is complete', 'formdesc' => 'Analysis levels: Initial: event has been started Ongoing: event population is in progress Complete: event creation has finished'),
		'distribution' => array('desc' => 'Describes who will have access to the event.')
	);

	public $riskDescriptions = array(
		'Undefined' => array('desc' => '*undefined* no risk', 'formdesc' => 'No risk'),
		'Low' => array('desc' => '*low* means mass-malware', 'formdesc' => 'Mass-malware'),
		'Medium' => array('desc' => '*medium* means APT malware', 'formdesc' => 'APT malware'),
		'High' => array('desc' => '*high* means sophisticated APT malware or 0-day attack', 'formdesc' => 'Sophisticated APT malware or 0-day attack')
	);

	public $analysisDescriptions = array(
		0 => array('desc' => '*Initial* means the event has just been created', 'formdesc' => 'Creation started'),
		1 => array('desc' => '*Ongoing* means that the event is being populated', 'formdesc' => 'Creation ongoing'),
		2 => array('desc' => '*Complete* means that the event\'s creation is complete', 'formdesc' => 'Creation complete')
	);

	public $distributionDescriptions = array(
		0 => array('desc' => 'This field determines the current distribution of the event', 'formdesc' => "This setting will only allow members of your organisation on this server to see it."),
		1 => array('desc' => 'This field determines the current distribution of the event', 'formdesc' => "Users that are part of your MISP community will be able to see the event. This includes your own organisation, organisations on this MISP server and organisations running MISP servers that synchronise with this server. Any other organisations connected to such linked servers will be restricted from seeing the event. Use this option if you are on the central hub of this community."), // former Community
		2 => array('desc' => 'This field determines the current distribution of the event', 'formdesc' => "Users that are part of your MISP community will be able to see the event. This includes all organisations on this MISP server, all organisations on MISP servers synchronising with this server and the hosting organisations of servers that connect to those afore mentioned servers (so basically any server that is 2 hops away from this one). Any other organisations connected to linked servers that are 2 hops away from this will be restricted from seeing the event. Use this option if this server isn't the central MISP hub of the community but is connected to it."),
		3 => array('desc' => 'This field determines the current distribution of the event', 'formdesc' => "This will share the event with all MISP communities, allowing the event to be freely propagated from one server to the next."),
	);

	public $analysisLevels = array(
		0 => 'Initial', 1 => 'Ongoing', 2 => 'Completed'
	);

	public $distributionLevels = array(
		0 => 'Your organisation only', 1 => 'This community only', 2 => 'Connected communities', 3 => 'All communities'
	);

	public $export_types = array(
			'xml' => array(
					'extension' => '.xml',
					'type' => 'XML',
					'description' => 'Click this to download all events and attributes that you have access to <small>(except file attachments)</small> in a custom XML format.',
			),
			'csv_sig' => array(
					'extension' => '.csv',
					'type' => 'CSV_Sig',
					'description' => 'Click this to download all attributes that are indicators and that you have access to <small>(except file attachments)</small> in CSV format.',
			),
			'csv_all' => array(
					'extension' => '.csv',
					'type' => 'CSV_All',
					'description' => 'Click this to download all attributes that you have access to <small>(except file attachments)</small> in CSV format.',
			),
			'suricata' => array(
					'extension' => '.rules',
					'type' => 'Suricata',
					'description' => 'Click this to download all network related attributes that you have access to under the Suricata rule format. Only published events and attributes marked as IDS Signature are exported. Administration is able to maintain a whitelist containing host, domain name and IP numbers to exclude from the NIDS export.',
			),
			'snort' => array(
					'extension' => '.rules',
					'type' => 'Snort',
					'description' => 'Click this to download all network related attributes that you have access to under the Snort rule format. Only published events and attributes marked as IDS Signature are exported. Administration is able to maintain a whitelist containing host, domain name and IP numbers to exclude from the NIDS export.',
			),
			'md5' => array(
					'extension' => '.txt',
					'type' => 'MD5',
					'description' => 'Click on one of these two buttons to download all MD5 checksums contained in file-related attributes. This list can be used to feed forensic software when searching for susipicious files. Only published events and attributes marked as IDS Signature are exported.',
			),
			'sha1' => array(
					'extension' => '.txt',
					'type' => 'SHA1',
					'description' => 'Click on one of these two buttons to download all SHA1 checksums contained in file-related attributes. This list can be used to feed forensic software when searching for susipicious files. Only published events and attributes marked as IDS Signature are exported.',
			),
			'text' => array(
					'extension' => '.txt',
					'type' => 'TEXT',
					'description' => 'Click on one of the buttons below to download all the attributes with the matching type. This list can be used to feed forensic software when searching for susipicious files. Only published events and attributes marked as IDS Signature are exported.'
			)
	);

/**
 * Validation rules
 *
 * @var array
 */
	public $validate = array(
		'org' => array(
			'notempty' => array(
				'rule' => array('notempty'),
				//'message' => 'Your custom message here',
				//'allowEmpty' => false,
				//'required' => false,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
			),
		),
		'orgc' => array(
			'notempty' => array(
				'rule' => array('notempty'),
				//'message' => 'Your custom message here',
				//'allowEmpty' => false,
				//'required' => false,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
			),
		),
		'date' => array(
			'date' => array(
				'rule' => array('date'),
				//'message' => 'Your custom message here',
				//'allowEmpty' => false,
				'required' => true,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
			),
		),
		'risk' => array(
				'rule' => array('inList', array('Undefined', 'Low','Medium','High')),
				'message' => 'Options : Undefined, Low, Medium, High',
				//'allowEmpty' => false,
				'required' => true,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
		),
		'distribution' => array(
			'rule' => array('inList', array('0', '1', '2', '3')),
			'message' => 'Options : Your organisation only, This community only, Connected communities, All communities',
			//'allowEmpty' => false,
			'required' => true,
			//'last' => false, // Stop validation after this rule
			//'on' => 'create', // Limit validation to 'create' or 'update' operations

		),
		'analysis' => array(
			'rule' => array('inList', array('0', '1', '2')),
				'message' => 'Options : 0, 1, 2',
				//'allowEmpty' => false,
				'required' => true,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
		),
		'info' => array(
			'notempty' => array(
				'rule' => array('notempty'),
				//'message' => 'Your custom message here',
				//'allowEmpty' => false,
				//'required' => false,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
			),
		),
		'user_id' => array(
			'numeric' => array(
				'rule' => array('numeric'),
				//'message' => 'Your custom message here',
				//'allowEmpty' => false,
				//'required' => false,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
			),
		),
		'user_id' => array(
			'numeric' => array(
				'rule' => array('numeric'),
				//'message' => 'Your custom message here',
				//'allowEmpty' => false,
				//'required' => false,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
			),
		),
		'published' => array(
			'boolean' => array(
				'rule' => array('boolean'),
				//'message' => 'Your custom message here',
				//'allowEmpty' => false,
				//'required' => false,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
			),
		),
		'uuid' => array(
			'uuid' => array(
				'rule' => array('uuid'),
				//'message' => 'Your custom message here',
				//'allowEmpty' => false,
				//'required' => false,
				//'last' => false, // Stop validation after this rule
				//'on' => 'create', // Limit validation to 'create' or 'update' operations
			),
		),
		//'classification' => array(
		//		'rule' => array('inList', array('TLP:AMBER', 'TLP:GREEN:NeedToKnow', 'TLP:GREEN')),
		//		//'message' => 'Your custom message here',
		//		//'allowEmpty' => false,
		//		'required' => true,
		//		//'last' => false, // Stop validation after this rule
		//		//'on' => 'create', // Limit validation to 'create' or 'update' operations
		//),
	);

	public function __construct($id = false, $table = null, $ds = null) {
		parent::__construct($id, $table, $ds);
		//$this->virtualFields = Set::merge($this->virtualFields, array(
//			'distribution' => 'IF (Event.private=true AND Event.cluster=false, "Your organization only", IF (Event.private=true AND Event.cluster=true, "This server-only", IF (Event.private=false AND Event.cluster=true, "This Community-only", IF (Event.communitie=true, "Connected communities" , "All communities"))))',
	//	));
	}

	//The Associations below have been created with all possible keys, those that are not needed can be removed

/**
 * belongsTo associations
 *
 * @var array
 */
	public $belongsTo = array(
		//'Org' => array(
		//	'className' => 'Org',
		//	'foreignKey' => 'org',
		//	'conditions' => '',
		//	'fields' => '',
		//	'order' => ''
		//)
		'User' => array(
			'className' => 'User',
			'foreignKey' => 'user_id',
			'conditions' => '',
			'fields' => '',
			'order' => ''
		)
	);

/**
 * hasMany associations
 *
 * @var array
 *
 * @throws InternalErrorException // TODO Exception
 */
	public $hasMany = array(
		'Attribute' => array(
			'className' => 'Attribute',
			'foreignKey' => 'event_id',
			'dependent' => true,	// cascade deletes
			'conditions' => '',
			'fields' => '',
			'order' => array('Attribute.category ASC', 'Attribute.type ASC'),
			'limit' => '',
			'offset' => '',
			'exclusive' => '',
			'finderQuery' => '',
			'counterQuery' => ''
		),
		'ShadowAttribute' => array(
				'className' => 'ShadowAttribute',
				'foreignKey' => 'event_id',
				'dependent' => true,	// cascade deletes
				'conditions' => '',
				'fields' => '',
				'order' => array('ShadowAttribute.old_id DESC', 'ShadowAttribute.old_id DESC'),
				'limit' => '',
				'offset' => '',
				'exclusive' => '',
				'finderQuery' => '',
				'counterQuery' => ''
		)
	);

	public function beforeDelete($cascade = true) {
		// delete event from the disk
		$this->read();	// first read the event from the db
		// FIXME secure this filesystem access/delete by not allowing to change directories or go outside of the directory container.
		// only delete the file if it exists
		$filepath = APP . "files" . DS . $this->data['Event']['id'];
		App::uses('Folder', 'Utility');
		$file = new Folder ($filepath);
		if (is_dir($filepath)) {
			if (!$this->destroyDir($filepath)) {
				throw new InternalErrorException('Delete of event file directory failed. Please report to administrator.');
			}
		}
	}

	public function destroyDir($dir) {
	if (!is_dir($dir) || is_link($dir)) return unlink($dir);
		foreach (scandir($dir) as $file) {
			if ($file == '.' || $file == '..') continue;
			if (!$this->destroyDir($dir . DS . $file)) {
				chmod($dir . DS . $file, 0777);
				if (!$this->destroyDir($dir . DS . $file)) return false;
			};
		}
		return rmdir($dir);
	}

	public function beforeValidate($options = array()) {
		parent::beforeValidate();
		// analysis - setting correct vars
		// TODO refactor analysis into an Enum (in the database)
		if (isset($this->data['Event']['analysis'])) {
			switch($this->data['Event']['analysis']){
			    case 'Initial':
			        $this->data['Event']['analysis'] = 0;
			        break;
			    case 'Ongoing':
			        $this->data['Event']['analysis'] = 1;
			        break;
			    case 'Completed':
			        $this->data['Event']['analysis'] = 2;
			        break;
			}
		}

		// generate UUID if it doesn't exist
		if (empty($this->data['Event']['uuid'])) {
			$this->data['Event']['uuid'] = String::uuid();
		}
		// generate timestamp if it doesn't exist
		if (empty($this->data['Event']['timestamp'])) {
			$date = new DateTime();
			$this->data['Event']['timestamp'] = $date->getTimestamp();
		}
	}

	public function isOwnedByOrg($eventid, $org) {
		return $this->field('id', array('id' => $eventid, 'org' => $org)) === $eventid;
	}

	public function getRelatedEvents($me, $isSiteAdmin = false, $eventId = null) {
		if ($eventId == null) $eventId = $this->data['Event']['id'];
		$this->Correlation = ClassRegistry::init('Correlation');
		// search the correlation table for the event ids of the related events
		if (!$isSiteAdmin) {
		    $conditionsCorrelation = array('AND' =>
		            array('Correlation.1_event_id' => $eventId),
		            array("OR" => array(
		                    'Correlation.org' => $me['org'],
		                    'Correlation.private' => 0),
		            ));
		} else {
		    $conditionsCorrelation = array('Correlation.1_event_id' => $eventId);
		}
		$correlations = $this->Correlation->find('all',array(
		        'fields' => 'Correlation.event_id',
		        'conditions' => $conditionsCorrelation,
		        'recursive' => 0,
		        'order' => array('Correlation.event_id DESC')));

		$relatedEventIds = array();
		foreach ($correlations as $correlation) {
			$relatedEventIds[] = $correlation['Correlation']['event_id'];
		}
		$relatedEventIds = array_unique($relatedEventIds);
		// now look up the event data for these attributes
		$conditions = array("Event.id" => $relatedEventIds);
		$relatedEvents = $this->find('all',
							array('conditions' => $conditions,
								'recursive' => 0,
								'order' => 'Event.date DESC',
								'fields' => 'Event.*'
								)
		);
		return $relatedEvents;
	}

	public function getRelatedAttributes($me, $isSiteAdmin = false, $id = null) {
		if ($id == null) $id = $this->data['Event']['id'];
		$this->Correlation = ClassRegistry::init('Correlation');
		// search the correlation table for the event ids of the related attributes
		if (!$isSiteAdmin) {
		    $conditionsCorrelation = array('AND' =>
		            array('Correlation.1_event_id' => $id),
		            array("OR" => array(
		                    'Correlation.org' => $me['org'],
		                    'Correlation.private' => 0),
		            ));
		} else {
		    $conditionsCorrelation = array('Correlation.1_event_id' => $id);
		}
		$correlations = $this->Correlation->find('all',array(
		        'fields' => 'Correlation.*',
		        'conditions' => $conditionsCorrelation,
		        'recursive' => 0,
		        'order' => array('Correlation.event_id DESC')));
		$relatedAttributes = array();
		foreach($correlations as $correlation) {
			$current = array(
		            'id' => $correlation['Correlation']['event_id'],
		            'org' => $correlation['Correlation']['org'],
		    		'info' => $correlation['Correlation']['info']
		    );
			if (empty($relatedAttributes[$correlation['Correlation']['1_attribute_id']]) || !in_array($current, $relatedAttributes[$correlation['Correlation']['1_attribute_id']])) {
		    	$relatedAttributes[$correlation['Correlation']['1_attribute_id']][] = $current;
			}
		}
		return $relatedAttributes;
	}

/**
 * Clean up an Event Array that was received by an XML request.
 * The structure needs to be changed a little bit to be compatible with what CakePHP expects
 *
 * This function receives the reference of the variable, so no return is required as it directly
 * modifies the original data.
 *
 * @param &$data The reference to the variable
 *
 * @throws InternalErrorException
 */
	public function cleanupEventArrayFromXML(&$data) {
		// Workaround for different structure in XML/array than what CakePHP expects
		if (isset($data['Event']['Attribute']) && is_array($data['Event']['Attribute']) && count($data['Event']['Attribute'])) {
			if (is_numeric(implode(array_keys($data['Event']['Attribute']), ''))) {
				// normal array of multiple Attributes
				$data['Attribute'] = $data['Event']['Attribute'];
			} else {
				// single attribute
				$data['Attribute'][0] = $data['Event']['Attribute'];
			}
		}
		unset($data['Event']['Attribute']);

		return $data;
	}

	public function uploadEventToServer($event, $server, $HttpSocket = null) {
		$updated = null;
		$newLocation = $newTextBody = '';
		$result = $this->restfullEventToServer($event, $server, null, $newLocation, $newTextBody, $HttpSocket);
		if ($result === 403) {
			return 'The distribution level of this event blocks it from being pushed.';
		}
		if (strlen($newLocation) || $result) { // HTTP/1.1 200 OK or 302 Found and Location: http://<newLocation>
			if (strlen($newLocation)) { // HTTP/1.1 302 Found and Location: http://<newLocation>
				//$updated = true;
				$result = $this->restfullEventToServer($event, $server, $newLocation, $newLocation, $newTextBody, $HttpSocket);
				if ($result === 405) {
					return 'You do not have permission to edit this event or the event is up to date.';
				}
			}
			try { // TODO Xml::build() does not throw the XmlException
				$xml = Xml::build($newTextBody);
			} catch (XmlException $e) {
				//throw new InternalErrorException();
				return false;
			}
			// get the remote event_id
			foreach ($xml as $xmlEvent) {
				foreach ($xmlEvent as $key => $value) {
					if ($key == 'id') {
						$remoteId = (int)$value;
						break;
					}
				}
			}
			// get the new attribute uuids in an array
			$newerUuids = array();
			foreach ($event['Attribute'] as $attribute) {
				$newerUuids[$attribute['id']] = $attribute['uuid'];
				$attribute['event_id'] = $remoteId;
			}
			// get the already existing attributes and delete the ones that are not there
			foreach ($xml->Event->Attribute as $attribute) {
				foreach ($attribute as $key => $value) {
					if ($key == 'uuid') {
						if (!in_array((string)$value, $newerUuids)) {
							$anAttr = ClassRegistry::init('Attribute');
							$anAttr->deleteAttributeFromServer((string)$value, $server, $HttpSocket);
						}
					}
				}
			}
		}
		return 'Success';
	}

/**
 * Uploads the event and the associated Attributes to another Server
 * TODO move this to a component
 *
 * @return bool true if success, false or error message if failed
 */
	public function restfullEventToServer($event, $server, $urlPath, &$newLocation, &$newTextBody, $HttpSocket = null) {
		if ($event['Event']['distribution'] < 2) { // never upload private events
			return 403; //"Event is private and non exportable";
		}

		$url = $server['Server']['url'];
		$authkey = $server['Server']['authkey'];
		if (null == $HttpSocket) {
			App::uses('HttpSocket', 'Network/Http');
			$HttpSocket = new HttpSocket();
		}
		$request = array(
				'header' => array(
						'Authorization' => $authkey,
						'Accept' => 'application/xml',
						'Content-Type' => 'application/xml',
						//'Connection' => 'keep-alive' // LATER followup cakephp ticket 2854 about this problem http://cakephp.lighthouseapp.com/projects/42648-cakephp/tickets/2854
				)
		);
		$uri = isset($urlPath) ? $urlPath : $url . '/events';
		// LATER try to do this using a separate EventsController and renderAs() function
		$xmlArray = array();
		// rearrange things to be compatible with the Xml::fromArray()
		$event['Event']['Attribute'] = $event['Attribute'];
		unset($event['Attribute']);

		// cleanup the array from things we do not want to expose
		//unset($event['Event']['org']);
		// remove value1 and value2 from the output
		foreach ($event['Event']['Attribute'] as $key => &$attribute) {
			// do not keep attributes that are private, nor cluster
			if ($attribute['distribution'] < 2) {
				unset($event['Event']['Attribute'][$key]);
				continue; // stop processing this
			}
			// Distribution, correct Connected Community to Community in Attribute
			if ($attribute['distribution'] == 2) {
				$attribute['distribution'] = 1;
			}
			// remove value1 and value2 from the output
			unset($attribute['value1']);
			unset($attribute['value2']);
			// also add the encoded attachment
			if ($this->Attribute->typeIsAttachment($attribute['type'])) {
				$encodedFile = $this->Attribute->base64EncodeAttachment($attribute);
				$attribute['data'] = $encodedFile;
			}
			// Passing the attribute ID together with the attribute could cause the deletion of attributes after a publish/push
			// Basically, if the attribute count differed between two instances, and the instance with the lower attribute
			// count pushed, the old attributes with the same ID got overwritten. Unsetting the ID before pushing it
			// solves the issue and a new attribute is always created.
			unset($attribute['id']);
		}
		// Distribution, correct All to Community in Event
		if ($event['Event']['distribution'] == 2) {
			$event['Event']['distribution'] = 1;
		}

		// display the XML to the user
		$xmlArray['Event'][] = $event['Event'];
		$xmlObject = Xml::fromArray($xmlArray, array('format' => 'tags'));
		$eventsXml = $xmlObject->asXML();
		// do a REST POST request with the server
		$data = $eventsXml;
		// LATER validate HTTPS SSL certificate
		$this->Dns = ClassRegistry::init('Dns');
		if ($this->Dns->testipaddress(parse_url($uri, PHP_URL_HOST))) {
			// TODO NETWORK for now do not know how to catch the following..
			// TODO NETWORK No route to host
			$response = $HttpSocket->post($uri, $data, $request);
			switch ($response->code) {
				case '200':	// 200 (OK) + entity-action-result
					if ($response->isOk()) {
						$newTextBody = $response->body();
						$newLocation = null;
						return true;
						//return isset($urlPath) ? $response->body() : true;
					} else {
						try {
							// parse the XML response and keep the reason why it failed
							$xmlArray = Xml::toArray(Xml::build($response->body));
						} catch (XmlException $e) {
							return true; // TODO should be false
						}
						if (strpos($xmlArray['response']['name'],"Event already exists")) {	// strpos, so i can piggyback some value if needed.
							return true;
						} else {
							return $xmlArray['response']['name'];
						}
					}
					break;
				case '302': // Found
					$newLocation = $response->headers['Location'];
					$newTextBody = $response->body();
					return true;
					//return isset($urlPath) ? $response->body() : $response->headers['Location'];
					break;
				case '404': // Not Found
					$newLocation = $response->headers['Location'];
					$newTextBody = $response->body();
					return 404;
					break;
				case '405':
					return 405;
					break;
				case '403': // Not authorised
					return 403;
					break;

			}
		}
	}

/**
 * Deletes the event and the associated Attributes from another Server
 * TODO move this to a component
 *
 * @return bool true if success, error message if failed
 */
	public function deleteEventFromServer($uuid, $server, $HttpSocket=null) {
		// TODO private and delete(?)

		$url = $server['Server']['url'];
		$authkey = $server['Server']['authkey'];
		if (null == $HttpSocket) {
			App::uses('HttpSocket', 'Network/Http');
			$HttpSocket = new HttpSocket();
		}
		$request = array(
				'header' => array(
						'Authorization' => $authkey,
						'Accept' => 'application/xml',
						'Content-Type' => 'application/xml',
						//'Connection' => 'keep-alive' // LATER followup cakephp ticket 2854 about this problem http://cakephp.lighthouseapp.com/projects/42648-cakephp/tickets/2854
				)
		);
		$uri = $url . '/events/0?uuid=' . $uuid;

		// LATER validate HTTPS SSL certificate
		$this->Dns = ClassRegistry::init('Dns');
		if ($this->Dns->testipaddress(parse_url($uri, PHP_URL_HOST))) {
			// TODO NETWORK for now do not know how to catch the following..
			// TODO NETWORK No route to host
			$response = $HttpSocket->delete($uri, array(), $request);
			// TODO REST, DELETE, some responce needed
		}
	}

/**
 * Download a specific event from a Server
 * TODO move this to a component
 * @return array|NULL
 */
	public function downloadEventFromServer($eventId, $server, $HttpSocket=null) {
		$url = $server['Server']['url'];
		$authkey = $server['Server']['authkey'];
		if (null == $HttpSocket) {
			App::uses('HttpSocket', 'Network/Http');
			//$HttpSocket = new HttpSocket(array(
			//		'ssl_verify_peer' => false
			//		));
			$HttpSocket = new HttpSocket();
		}
		$request = array(
				'header' => array(
						'Authorization' => $authkey,
						'Accept' => 'application/xml',
						'Content-Type' => 'application/xml',
						//'Connection' => 'keep-alive' // LATER followup cakephp ticket 2854 about this problem http://cakephp.lighthouseapp.com/projects/42648-cakephp/tickets/2854
				)
		);
		$uri = $url . '/events/' . $eventId;
		$response = $HttpSocket->get($uri, $data = '', $request);
		if ($response->isOk()) {
			$xmlArray = Xml::toArray(Xml::build($response->body));
			return $xmlArray['response'];
		} else {
			// TODO parse the XML response and keep the reason why it failed
			return null;
		}
	}

/**
 * Get an array of event_ids that are present on the remote server
 * TODO move this to a component
 * @return array of event_ids
 */
	public function getEventIdsFromServer($server, $HttpSocket=null) {
		$url = $server['Server']['url'];
		$authkey = $server['Server']['authkey'];

		if (null == $HttpSocket) {
			App::uses('HttpSocket', 'Network/Http');
			//$HttpSocket = new HttpSocket(array(
			//		'ssl_verify_peer' => false
			//		));
			$HttpSocket = new HttpSocket();
		}
		$request = array(
				'header' => array(
						'Authorization' => $authkey,
						'Accept' => 'application/xml',
						'Content-Type' => 'application/xml',
						//'Connection' => 'keep-alive' // LATER followup cakephp ticket 2854 about this problem http://cakephp.lighthouseapp.com/projects/42648-cakephp/tickets/2854
				)
		);
		$uri = $url . '/events/index/sort:id/direction:desc/limit:999'; // LATER verify if events are missing because we only selected the last 999
		try {
			$response = $HttpSocket->get($uri, $data = '', $request);
			if ($response->isOk()) {
				//debug($response->body);
				$xml = Xml::build($response->body);
				$eventArray = Xml::toArray($xml);
				// correct $eventArray if just one event
				if (is_array($eventArray['response']['Event']) && isset($eventArray['response']['Event']['id'])) {
					$tmp = $eventArray['response']['Event'];
					unset($eventArray['response']['Event']);
					$eventArray['response']['Event'][0] = $tmp;
				}

				$eventIds = array();
				// different actions if it's only 1 event or more
				// only one event.
				if (isset($eventArray['response']['Event']['id'])) {
					$eventIds[] = $eventArray['response']['Event']['id'];
				} else {
					// multiple events, iterate over the array
					foreach ($eventArray['response']['Event'] as &$event) {
						if (1 != $event['published']) {
							continue; // do not keep non-published events
						}
						$eventIds[] = $event['id'];
					}
				}
				return $eventIds;
			}
			if ($response->code == '403') {
				return 403;
			}
		} catch (SocketException $e){
			// FIXME refactor this with clean try catch over all http functions
			return $e->getMessage();
		}
		// error, so return null
		return null;
	}

	public function fetchEventIds($org, $isSiteAdmin) {
		$conditions = array();
		if (!$isSiteAdmin) {
			$conditions['OR'] = array(
					'Event.distribution >' => 0,
					'Event.org LIKE' => $org
			);
		}
		$fields = array('Event.id', 'Event.org', 'Event.distribution');
		$params = array(
			'conditions' => $conditions,
			'recursive' => -1,
			'fields' => $fields,
		);
		$results = $this->find('all', $params);
		return $results;
	}

	//Once the data about the user is gathered from the appropriate sources, fetchEvent is called from the controller.
	public function fetchEvent($eventid = null, $idList = null, $org, $isSiteAdmin, $bkgrProcess = null) {
		if (isset($eventid)) {
			$this->id = $eventid;
			if (!$this->exists()) {
				throw new NotFoundException(__('Invalid event'));
			}
			$conditions = array("Event.id" => $eventid);
		} else {
			$conditions = array();
		}
		$me['org'] = $org;
		// if we come from automation, we may not be logged in - instead we used an auth key in the URL.

		$conditionsAttributes = array();
		$conditionsShadowAttributes = array();
		//restricting to non-private or same org if the user is not a site-admin.
		if (!$isSiteAdmin) {
			$conditions['AND']['OR'] = array(
				'Event.distribution >' => 0,
				'Event.org LIKE' => $org
			);
			$conditionsAttributes['OR'] = array(
				'Attribute.distribution >' => 0,
				'(SELECT events.org FROM events WHERE events.id = Attribute.event_id) LIKE' => $org
			);
			$conditionsShadowAttributes['OR'] = array(
			// We are currently looking at events.org matching the user's org, but later on, once we start syncing shadow attributes, we may want to change this to orgc
			// Right now the org that currently owns the event on an instance can see, accept and decline these requests, but in the long run once we can distribute
			// the requests back to the creator, we may want to leave these decisions up to them.
				array('(SELECT events.org FROM events WHERE events.id = ShadowAttribute.event_id) LIKE' => $org),
				array('ShadowAttribute.org LIKE' => $org),
			);
		}

		if ($idList) {
			$conditions['AND'][] = array('Event.id' => $idList);
		}
		// removing this for now, we export the to_ids == 0 attributes too, since there is a to_ids field indicating it in the .xml
		// $conditionsAttributes['AND'] = array('Attribute.to_ids =' => 1);
		// Same idea for the published. Just adjust the tools to check for this
		// TODO: It is important to make sure that this is documented
		// $conditions['AND'][] = array('Event.published =' => 1);

		// do not expose all the data ...
		$fields = array('Event.id', 'Event.org', 'Event.date', 'Event.risk', 'Event.info', 'Event.published', 'Event.uuid', 'Event.attribute_count', 'Event.analysis', 'Event.timestamp', 'Event.distribution', 'Event.proposal_email_lock', 'Event.orgc', 'Event.user_id', 'Event.locked');
		$fieldsAtt = array('Attribute.id', 'Attribute.type', 'Attribute.category', 'Attribute.value', 'Attribute.to_ids', 'Attribute.uuid', 'Attribute.event_id', 'Attribute.distribution', 'Attribute.timestamp', 'Attribute.comment');
		$fieldsShadowAtt = array('ShadowAttribute.id', 'ShadowAttribute.type', 'ShadowAttribute.category', 'ShadowAttribute.value', 'ShadowAttribute.to_ids', 'ShadowAttribute.uuid', 'ShadowAttribute.event_id', 'ShadowAttribute.old_id');

		$params = array('conditions' => $conditions,
			'recursive' => 0,
			'fields' => $fields,
			'contain' => array(
				'Attribute' => array(
					'fields' => $fieldsAtt,
					'conditions' => $conditionsAttributes,
				),
				'ShadowAttribute' => array(
					'fields' => $fieldsShadowAtt,
					'conditions' => $conditionsShadowAttributes,
				),
			)
		);
		if ($isSiteAdmin) $params['contain']['User'] = array('fields' => 'email');
		$results = $this->find('all', $params);
		// Do some refactoring with the event
		foreach ($results as $eventKey => &$event) {
			// Let's find all the related events and attach it to the event itself
			$results[$eventKey]['RelatedEvent'] = $this->getRelatedEvents($me, $isSiteAdmin, $event['Event']['id']);
			// Let's also find all the relations for the attributes - this won't be in the xml export though
			$results[$eventKey]['RelatedAttribute'] = $this->getRelatedAttributes($me, $isSiteAdmin, $event['Event']['id']);
			foreach ($event['Attribute'] as $key => &$attribute) {
				$attribute['ShadowAttribute'] = array();
				// If a shadowattribute can be linked to an attribute, link it to it then remove it from the event
				// This is to differentiate between proposals that were made to an attribute for modification and between proposals for new attributes
				foreach ($event['ShadowAttribute'] as $k => &$sa) {
					if(!empty($sa['old_id'])) {
						if ($sa['old_id'] == $attribute['id']) {
							$results[$eventKey]['Attribute'][$key]['ShadowAttribute'][] = $sa;
							unset($results[$eventKey]['ShadowAttribute'][$k]);
						}
					}
				}
			}
		}
		return $results;
	}
	 public function csv($org, $isSiteAdmin, $eventid=0, $ignore=0, $attributeIDList = array()) {
	 	$final = array();

	 	$attributeList = array();
	 	$conditions = array();
	 	$econditions = array();
	 	$this->recursive = -1;
	 	// If we are not in the search result csv download function then we need to check what can be downloaded. CSV downloads are already filtered by the search function.
	 	if ($eventid !== 'search') {
	 		// This is for both single event downloads and for full downloads. Org has to be the same as the user's or distribution not org only - if the user is no siteadmin
	 		if(!$isSiteAdmin) {
	 			$econditions['AND']['OR'] = array('Event.distribution >' => 0, 'Event.org =' => $org);
	 		}
	 		if ($eventid == 0 && $ignore == 0) {
	 			$econditions['AND'][] = array('Event.published =' => 1);
	 		}
	 		// If it's a full download (eventid == null) and the user is not a site admin, we need to first find all the events that the user can see and save the IDs
	 		if ($eventid == 0) {
	 			$this->recursive = -1;
	 			// let's add the conditions if we're dealing with a non-siteadmin user
	 			$params = array(
	 					'conditions' => $econditions,
	 					'fields' => array('id', 'distribution', 'org', 'published'),
	 			);
	 			$events = $this->find('all', $params);
	 		}
	 		// if we have items in events, add their IDs to the conditions. If we're a site admin, or we have a single event selected for download, this should be empty
	 		if (isset($events)) {
	 			foreach ($events as $event) {
	 				$conditions['AND']['OR'][] = array('Attribute.event_id' => $event['Event']['id']);
	 			}
	 		}
	 		// if we're downloading a single event, set it as a condition
	 		if ($eventid!=0) {
	 			$conditions['AND'][] = array('Attribute.event_id' => $eventid);
	 		}
	 		//restricting to non-private or same org if the user is not a site-admin.
	 		if ($ignore == 0) {
	 			$conditions['AND'][] = array('Attribute.to_ids =' => 1);
	 		}
	 		if (!$isSiteAdmin) {
	 			$temp = array();
	 			$distribution = array();
	 			array_push($temp, array('Attribute.distribution >' => 0));
	 			array_push($temp, array('(SELECT events.org FROM events WHERE events.id = Attribute.event_id) LIKE' => $org));
	 			$conditions['OR'] = $temp;
	 		}
	 	}
	 	if ($eventid === 'search') {
		 	foreach ($attributeIDList as $aID) {
		 		$conditions['AND']['OR'][] = array('Attribute.id' => $aID);
		 	}
	 	}
	 	$params = array(
	 			'conditions' => $conditions, //array of conditions
	 			'fields' => array('Attribute.event_id', 'Attribute.distribution', 'Attribute.category', 'Attribute.type', 'Attribute.value', 'Attribute.uuid'),
	 	);
	 	$attributes = $this->Attribute->find('all', $params);
	 	foreach ($attributes as $attribute) {
	 		$attribute['Attribute']['value'] = str_replace("\r", "", $attribute['Attribute']['value']);
	 		$attribute['Attribute']['value'] = str_replace("\n", "", $attribute['Attribute']['value']);
	 	}
	 	return $attributes;
	 }
}