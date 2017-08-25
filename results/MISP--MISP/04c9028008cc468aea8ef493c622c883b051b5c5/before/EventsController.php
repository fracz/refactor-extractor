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

    public $components = array('Security', 'Email');
    public $paginate = array(
        'limit' => 50,
        'order' => array(
            'Event.date' => 'DESC'
        )
    );


    function beforeFilter() {
        // what pages are allowed for non-logged-in users
        $this->Auth->allow('xml');
        $this->Auth->allow('nids');
        $this->Auth->allow('text');

        // These variables are required for every view
        $this->set('me', $this->Auth->user());
        $this->set('isAdmin', $this->_isAdmin());
    }

    public function isAuthorized($user) {
        // Admins can access everything
        if (parent::isAuthorized($user)) {
            return true;
        }
        // Only on own events for these actions
        if (in_array($this->action, array('edit', 'delete', 'alert'))) {
            $eventid = $this->request->params['pass'][0];
            return $this->Event->isOwnedByOrg($eventid, $this->Auth->user('org'));
        }
        // the other pages are allowed by logged in users
        return true;
    }

/**
 * index method
 *
 * @return void
 */
    function index() {
        // list the events
        $this->Event->recursive = 0;
        $this->set('events', $this->paginate());

        if (!$this->Auth->user('gpgkey')) {
            $this->Session->setFlash('No GPG key set in your profile. To receive emails, submit your public key in your profile.');
        }
    }

/**
 * view method
 *
 * @param int $id
 * @return void
 */
	public function view($id = null) {
	    $this->Event->id = $id;
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event'));
		}
		$this->set('event', $this->Event->read(null, $id));
        $this->set('relatedEvents', $this->Event->getRelatedEvents());

        $related_signatures = array();
        $this->loadModel('Signature');
        foreach ($this->Event->data['Signature'] as $signature) {
            $related_signatures[$signature['id']] = $this->Signature->getRelatedSignatures($signature);
        }
        $this->set('relatedSignatures', $related_signatures);

        $this->set('categories', $this->Signature->validate['category']['rule'][1]);
	}

/**
 * add method
 *
 * @return void
 */
	public function add() {
		if ($this->request->is('post')) {
		    // force check userid and orgname to be from yourself
		    $this->request->data['Event']['user_id'] = $this->Auth->user('id');
		    $this->request->data['Event']['org'] = $this->Auth->user('org');
		    $this->request->data['Event']['uuid'] = String::uuid();
		    $this->Event->create();
			if ($this->Event->save($this->request->data)) {
				$this->Session->setFlash(__('The event has been saved'));
				$this->redirect(array('action' => 'view', $this->Event->getId()));
			} else {
				$this->Session->setFlash(__('The event could not be saved. Please, try again.'), 'default', array(), 'error');
			}
		}
		// combobox for risks
        $risks = $this->Event->validate['risk']['rule'][1];
        $risks = $this->_arrayToValuesIndexArray($risks);
        $this->set('risks',compact('risks'));
	}

/**
 * edit method
 *
 * @param int $id
 * @return void
 */
	public function edit($id = null) {
		$this->Event->id = $id;
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event'));
		}
		// only edit own events verified by isAuthorized

		if ($this->request->is('post') || $this->request->is('put')) {
		    // always force the user and org, but do not force it for admins
		    if (!$this->_isAdmin()) {
    		    $this->request->data['Event']['user_id'] = $this->Auth->user('id');
    		    $this->request->data['Event']['org'] = $this->Auth->user('org');
		    } else {
		        $this->request->data['Event']['user_id'] = $old_event['Event']['id'];
		        $this->request->data['Event']['org'] = $old_event['Event']['org'];
		    }
		    // we probably also want to remove the alerted flag
		    $this->request->data['Event']['alerted'] = 0;

		    // say what fields are to be updated
		    $fieldList=array('user_id', 'org', 'date', 'risk', 'info', 'alerted', 'private');
			if ($this->Event->save($this->request->data, true, $fieldList)) {
				$this->Session->setFlash(__('The event has been saved'));
				$this->redirect(array('action' => 'view', $id));
			} else {
				$this->Session->setFlash(__('The event could not be saved. Please, try again.'));
			}
		} else {
			$this->request->data = $this->Event->read(null, $id);
		}

        // combobox for types
        $risks = $this->Event->validate['risk']['rule'][1];
        $risks = $this->_arrayToValuesIndexArray($risks);
        $this->set('risks',compact('risks'));
	}

/**
 * delete method
 *
 * @param int $id
 * @return void
 */
	public function delete($id = null) {
		if (!$this->request->is('post')) {
			throw new MethodNotAllowedException();
		}
		$this->Event->id = $id;
		if (!$this->Event->exists()) {
			throw new NotFoundException(__('Invalid event'));
		}
		// only edit own events verified by isAuthorized

		if ($this->Event->delete()) {
			$this->Session->setFlash(__('Event deleted'));
			$this->redirect(array('action' => 'index'));
		}
		$this->Session->setFlash(__('Event was not deleted'));
		$this->redirect(array('action' => 'index'));
	}

	/**
	 * Send out an alert email to all the users that wanted to be notified.
	 * Users with a GPG key will get the mail encrypted, other users will get the mail unencrypted
	 */
	function alert($id = null) {
	    $this->Event->id = $id;
	    if (!$this->Event->exists()) {
	        throw new NotFoundException(__('Invalid event'));
	    }

	    // only allow form submit CSRF protection.
	    if ($this->request->is('post') || $this->request->is('put')) {

    	    $this->Event->id = $id;
    	    $this->Event->read();

    	    // only allow alert for own events verified by isAuthorized

    	    // fetch the event and build the body
    	    if (1 == $this->Event->data['Event']['alerted']) {
    	        $this->Session->setFlash(__('Everyone has already been alerted for this event. To alert again, first edit this event.', true), 'default', array(), 'error');
    	        $this->redirect(array('action' => 'view', $id));
    	    }

    	    // The mail body, Sanitize::html() is NOT needed as we are sending plain-text mails.
    	    $body = "";
    	    $appendlen = 20;
    	    $body .= 'URL         : '.Configure::read('CyDefSIG.baseurl').'/events/view/'.$this->Event->data['Event']['id']."\n";
    	    $body .= 'Event       : '.$this->Event->data['Event']['id']."\n";
    	    $body .= 'Date        : '.$this->Event->data['Event']['date']."\n";
    	    if ('true' == Configure::read('CyDefSIG.showorg')) {
    	        $body .= 'Reported by : '.$this->Event->data['Event']['org']."\n";
    	    }
    	    $body .= 'Risk        : '.$this->Event->data['Event']['risk']."\n";
    	    $relatedEvents = $this->Event->getRelatedEvents($id);
    	    if (!empty($relatedEvents)) {
    	        foreach ($relatedEvents as $relatedEvent){
    	            $body .= 'Related to  : '.Configure::read('CyDefSIG.baseurl').'/events/view/'.$relatedEvent['Event']['id'].' ('.$relatedEvent['Event']['date'].')'."\n" ;

    	        }
    	    }
    	    $body .= 'Info  : '."\n";
    	    $body .= $this->Event->data['Event']['info']."\n";
    	    $body .= "\n";
    	    $body .= 'Attributes  :'."\n";
    	    $body_temp_other = "";

    	    if (isset($this->Event->data['Signature'])) {
    	        foreach ($this->Event->data['Signature'] as $signature){
    	            $line = '- '.$signature['type'].str_repeat(' ', $appendlen - 2 - strlen( $signature['type'])).': '.$signature['value']."\n";
    	            if ('other' == $signature['type']) // append the 'other' attribute types to the bottom.
    	                $body_temp_other .= $line;
    	            else $body .= $line;
    	        }
    	    }
    	    $body .= "\n";
    	    $body .= $body_temp_other;  // append the 'other' attribute types to the bottom.

    	    // sign the body
    	    require_once 'Crypt/GPG.php';
    	    $gpg = new Crypt_GPG(array('homedir' => Configure::read('GnuPG.homedir')));
    	    $gpg->addSignKey(Configure::read('GnuPG.email'), Configure::read('GnuPG.password'));
    	    $body_signed = $gpg->sign($body, Crypt_GPG::SIGN_MODE_CLEAR);

    	    $this->loadModel('User');

    	    //
    	    // Build a list of the recipients that get a non-encrypted mail
    	    // But only do this if it is allowed in the bootstrap.php file.
    	    //
    	    if ('false' == Configure::read('GnuPG.onlyencrypted')) {
    	        $alert_users = $this->User->find('all', array(
    	                'conditions' => array('User.autoalert' => 1,
    	                        'User.gpgkey =' => ""),
    	                'recursive' => 0,
    	        ) );
    	        $alert_emails = Array();
    	        foreach ($alert_users as $user) {
    	            $alert_emails[] = $user['User']['email'];
    	        }
    	        // prepare the the unencrypted email
    	        $this->Email->from = Configure::read('CyDefSIG.email');
    	        //$this->Email->to = "CyDefSIG <sig@cyber-defence.be>"; TODO check if it doesn't break things to not set a to , like being spammed away
    	        $this->Email->bcc = $alert_emails;
    	        $this->Email->subject = "[CyDefSIG] Event ".$id." - ".$this->Event->data['Event']['risk']." - TLP Amber";
    	        $this->Email->template = 'body';
    	        $this->Email->sendAs = 'text';        // both text or html
    	        $this->set('body', $body_signed);
    	        // send it
    	        $this->Email->send();
    	        // If you wish to send multiple emails using a loop, you'll need
    	        // to reset the email fields using the reset method of the Email component.
    	        $this->Email->reset();
    	    }

    	    //
    	    // Build a list of the recipients that wish to receive encrypted mails.
    	    //
    	    $alert_users = $this->User->find('all', array(
    	            'conditions' => array(  'User.autoalert' => 1,
                                            'User.gpgkey !=' => ""),
                            	            'recursive' => 0,
    	                                    )
    	            );
    	    // encrypt the mail for each user and send it separately
    	    foreach ($alert_users as $user) {
    	        // send the email
    	        $this->Email->from = Configure::read('CyDefSIG.email');
    	        $this->Email->to = $user['User']['email'];
    	        $this->Email->subject = "[CyDefSIG] Event ".$id." - ".$this->Event->data['Event']['risk']." - TLP Amber";
    	        $this->Email->template = 'body';
    	        $this->Email->sendAs = 'text';        // both text or html

    	        // import the key of the user into the keyring
    	        // this is not really necessary, but it enables us to find
    	        // the correct key-id even if it is not the same as the emailaddress
    	        $key_import_output = $gpg->importKey($user['User']['gpgkey']);
    	        // say what key should be used to encrypt
    	        $gpg = new Crypt_GPG();
    	        $gpg->addEncryptKey($key_import_output['fingerprint']); // use the key that was given in the import

    	        $body_enc_sig = $gpg->encrypt($body_signed, true);

    	        $this->set('body', $body_enc_sig);
    	        $this->Email->send();
    	        // If you wish to send multiple emails using a loop, you'll need
    	        // to reset the email fields using the reset method of the Email component.
    	        $this->Email->reset();
    	    }

    	    // update the DB to set the alerted flag
    	    $this->Event->saveField('alerted', 1);

    	    // redirect to the view event page
    	    $this->Session->setFlash(__('Email sent to all participants.', true));
    	    $this->redirect(array('action' => 'view', $id));
	    }
	}


	/**
	 * Send out an contact email to the person who posted the event.
	 * Users with a GPG key will get the mail encrypted, other users will get the mail unencrypted
	 */
	public function contact($id = null) {
	    $this->Event->id = $id;
	    if (!$this->Event->exists()) {
	        throw new NotFoundException(__('Invalid event'));
	    }

	    // User has filled in his contact form, send out the email.
	    if ($this->request->is('post') || $this->request->is('put')) {
	        $message = $this->request->data['Event']['message'];
	        if ($this->_sendContactEmail($id, $message)) {
	            // LATER when a user is deleted this will create problems.
	            // LATER send the email to all the people who are in the org that created the event
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


	/**
	 *
	 * Sends out an email with the request to be contacted about a specific event.
	 * @todo move _sendContactEmail($id, $message) to a better place. (components?)
	 *
	 * @param unknown_type $id The id of the event for wich you want to contact the person.
	 * @param unknown_type $message The custom message that will be appended to the email.
	 * @return True if success, False if error
	 */
	private function _sendContactEmail($id, $message) {
	    // fetch the event
	    $event = $this->Event->read(null, $id);
	    $reporter = $event['User']; // email, gpgkey

	    // The mail body, Sanitize::html() is NOT needed as we are sending plain-text mails.
	    $body = "";
	    $body .="Hello, \n";
	    $body .="\n";
	    $body .="Someone wants to get in touch with you concerning a CyDefSIG event. \n";
	    $body .="\n";
	    $body .="You can reach him at ".$this->Auth->user('email')."\n";
	    if (!$this->Auth->user('gpgkey'))
	        $body .="His GPG/PGP key is added as attachment to this email. \n";
	    $body .="\n";
	    $body .="He wrote the following message: \n";
	    $body .=$message."\n";
	    $body .="\n";
	    $body .="\n";
	    $body .="The event is the following: \n";

	    // print the event in mail-format
	    // LATER place event-to-email-layout in a function
	    $appendlen = 20;
	    $body .= 'URL         : '.Configure::read('CyDefSIG.baseurl').'/events/view/'.$event['Event']['id']."\n";
	    $body .= 'Event       : '.$event['Event']['id']."\n";
	    $body .= 'Date        : '.$event['Event']['date']."\n";
	    if ('true' == Configure::read('CyDefSIG.showorg')) {
	        $body .= 'Reported by : '.$event['Event']['org']."\n";
	    }
	    $body .= 'Risk        : '.$event['Event']['risk']."\n";
	    $relatedEvents = $this->Event->getRelatedEvents($id);
	    if (!empty($relatedEvents)) {
	        foreach ($relatedEvents as $relatedEvent){
	            $body .= 'Related to  : '.Configure::read('CyDefSIG.baseurl').'/events/view/'.$relatedEvent['Event']['id'].' ('.$relatedEvent['Event']['date'].')'."\n" ;

	        }
	    }
	    $body .= 'Info  : '."\n";
	    $body .= $event['Event']['info']."\n";
	    $body .= "\n";
	    $body .= 'Attributes  :'."\n";
	    $body_temp_other = "";
	    if (!empty($event['Signature'])) {
	        foreach ($event['Signature'] as $signature){
	            $line = '- '.$signature['type'].str_repeat(' ', $appendlen - 2 - strlen( $signature['type'])).': '.$signature['value']."\n";
	            if ('other' == $signature['type']) // append the 'other' attribute types to the bottom.
	                $body_temp_other .= $line;
	            else $body .= $line;
	        }
	    }
	    $body .= "\n";
	    $body .= $body_temp_other;  // append the 'other' attribute types to the bottom.

	    // sign the body
	    require_once 'Crypt/GPG.php';
	    $gpg = new Crypt_GPG(array('homedir' => Configure::read('GnuPG.homedir')));
	    $gpg->addSignKey(Configure::read('GnuPG.email'), Configure::read('GnuPG.password'));
	    $body_signed = $gpg->sign($body, Crypt_GPG::SIGN_MODE_CLEAR);

	    if (!empty($reporter['gpgkey'])) {
	        // import the key of the user into the keyring
	        // this isn't really necessary, but it gives it the fingerprint necessary for the next step
	        $key_import_output = $gpg->importKey($reporter['gpgkey']);
	        // say what key should be used to encrypt
	        $gpg = new Crypt_GPG();
	        $gpg->addEncryptKey($key_import_output['fingerprint']); // use the key that was given in the import

	        $body_enc_sig = $gpg->encrypt($body_signed, true);
	    } else {
	        $body_enc_sig = $body_signed;
	        // FIXME should I allow sending unencrypted "contact" mails to people if they didn't import they GPG key?
	    }

	    // prepare the email
	    $this->Email->from = Configure::read('CyDefSIG.email');
	    $this->Email->to = $reporter['email'];
	    $this->Email->subject = "[CyDefSIG] Need info about event ".$id." - TLP Amber";
        //$this->Email->delivery = 'debug';   // do not really send out mails, only display it on the screen
	    $this->Email->template = 'body';
	    $this->Email->sendAs = 'text';        // both text or html
	    $this->set('body', $body_enc_sig);

	    // Add the GPG key of the user as attachment
	    // LATER sign the attached GPG key
	    if (!empty($me_user['gpgkey'])) {
	        // save the gpg key to a temporary file
	        $tmpfname = tempnam(TMP, "GPGkey");
	        $handle = fopen($tmpfname, "w");
	        fwrite($handle, $me_user['gpgkey']);
	        fclose($handle);
	        // attach it
	        $this->Email->attachments = array(
	                'gpgkey.asc' => $tmpfname
	        );
	    }

	    // send it
	    $result = $this->Email->send();

	    // remove the temporary gpg file
	    if (!empty($me_user['gpgkey']))
	        unlink($tmpfname);

	    return $result;
	}


	public function export() {
	    // Simply display a static view

	    // generate the list of Attribute types
	    $this->loadModel('Signature');
	    $this->set('sig_types', $this->Signature->validate['type']['rule'][1]);

	}


	public function xml($key) {
	    // FIXME implement XML output
	    // check if the key is valid -> search for users based on key
	    $this->loadModel('User');
	    // no input sanitization necessary, it's done by model
	    $user = $this->User->findByAuthkey($key);
	    if (empty($user)) {
	        throw new UnauthorizedException('Incorrect authentication key');
	    }
	    // display the full xml
	    $this->header('Content-Type: text/xml');    // set the content type
	    $this->layout = 'xml/default';
// 	    $this->header('Content-Disposition: attachment; filename="cydefsig.xml"');

	    $conditions = array("Event.alerted" => 1);
	    $fields = array('Event.id', 'Event.date', 'Event.risk', 'Event.info');
	    if ('true' == Configure::read('CyDefSIG.showorg')) {
	        $fields[] = 'Event.org';
	    }
	    //         $this->Event->Behaviors->attach('Containable');
	    //         $contain = array('Signature.id', 'Signature.type', 'Signature.value', 'Signature.to_snort');
	    $params = array('conditions' => $conditions,
	                        'recursive' => 1,
	                        'fields' => $fields,
	    //                         'contain' => $contain
	    );
	    $results = $this->Event->find('all', $params);


/* 	    $xml = Xml::build('<?xml version="1.0" encoding="UTF-8" ?><CyDefSIG></CyDefSIG>'); */

	    $myXmlOriginal = '<?xml version="1.0"?><root><child>value</child></root>';
	    $xml = Xml::build($myXmlOriginal);
	    $xml->root->addChild('young', 'new value');

// 	    foreach ($results as $result) {
// 	        debug($result);
// 	        $xml->CyDefSIG->addChild('f', 'b');
// 	        debug($xml);
// 	    }
// 	        debug($results);
// 	        $xml= Xml::fromArray(array('event' =>$results), array('format' => 'tags', 'return' => 'domdocument'));
// 	        debug($xml->saveXML());

	}


	public function nids($key) {
	    // check if the key is valid -> search for users based on key
	    $this->loadModel('User');
	    // no input sanitization necessary, it's done by model
	    $user = $this->User->findByAuthkey($key);
	    if (empty($user)) {
	        throw new UnauthorizedException('Incorrect authentication key');
	    }
	    // display the full snort rulebase
	    $this->header('Content-Type: text/plain');    // set the content type
	    $this->header('Content-Disposition: attachment; filename="cydefsig.rules"');
	    $this->layout = 'text/default';

	    $rules= array();

	    // find events that are published
	    $events = $this->Event->findAllByAlerted(1);
	    $classtype = 'targeted-attack';

	    foreach ($events as $event) {
	        # proto src_ip src_port direction dst_ip dst_port msg rule_content tag sid rev
	        $rule_format_msg = 'msg: "CyDefSIG %s, Event '.$event['Event']['id'].', '.$event['Event']['risk'].'"';
	        $rule_format_reference = 'reference:url,'.Configure::read('CyDefSIG.baseurl').'/events/view/'.$event['Event']['id'];
	        $rule_format = 'alert %s %s %s %s %s %s ('.$rule_format_msg.'; %s %s classtype:'.$classtype.'; sid:%d; rev:%d; '.$rule_format_reference.';) ';

	        $sid = $user['User']['nids_sid']+($event['Event']['id']*100); // LATER this will cause issues with events containing more than 99 attributes
	        //debug($event);
	        foreach ($event['Signature'] as $signature) {
	            if (0 == $signature['to_ids']) continue; // attribute is not to be exported to IDS. // LATER filter out to_ids=0 in the query

	            $sid++;
	            switch ($signature['type']) {
	                // LATER test all the snort signatures
	                // LATER add the tag keyword in the rules to capture network traffic
	                // LATER sanitize every $signature['value'] to not conflict with snort
	                case 'ip-dst':
	                    $rules[] = sprintf($rule_format,
	                            'ip',                           // proto
	                            '$HOME_NET',                    // src_ip
	                            'any',                          // src_port
	                            '->',                           // direction
	                            $signature['value'],            // dst_ip
	                            'any',                          // dst_port
	                            'Outgoing To Bad IP',           // msg
	                            '',                             // rule_content
	                            '',                             // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    break;
	                case 'ip-src':
	                    $rules[] = sprintf($rule_format,
	                            'ip',                           // proto
	                            $signature['value'],            // src_ip
	                            'any',                          // src_port
	                            '->',                           // direction
	                            '$HOME_NET',                    // dst_ip
	                            'any',                          // dst_port
	                            'Incoming From Bad IP',         // msg
	                            '',                             // rule_content
	                            '',                             // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    break;
	                case 'email-src':
	                    $rules[] = sprintf($rule_format,
	                            'tcp',                          // proto
	                            '$EXTERNAL_NET',                // src_ip
	                            'any',                          // src_port
	                            '<>',                           // direction
	                            '$SMTP_SERVERS',                // dst_ip
	                            '25',                           // dst_port
	                            'Bad Source Email Address',     // msg
	                            'flow:established,to_server; content:"MAIL FROM|3a|"; nocase; content:"'.$signature['value'].'"; nocase;',  // rule_content
	                            'tag:session,600,seconds;',     // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    break;
	                case 'email-dst':
	                    $rules[] = sprintf($rule_format,
	                            'tcp',                          // proto
	                            '$EXTERNAL_NET',                // src_ip
	                            'any',                          // src_port
	                            '<>',                           // direction
	                            '$SMTP_SERVERS',                // dst_ip
	                            '25',                           // dst_port
	                            'Bad Destination Email Address',// msg
	                            'flow:established,to_server; content:"RCPT TO|3a|"; nocase; content:"'.$signature['value'].'"; nocase;',  // rule_content
	                        	'tag:session,600,seconds;',     // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    break;
	                case 'email-subject':
	                    // LATER email-subject rule might not match because of line-wrapping
	                    $rules[] = sprintf($rule_format,
	                            'tcp',                          // proto
	                            '$EXTERNAL_NET',                // src_ip
	                            'any',                          // src_port
	                            '<>',                           // direction
	                            '$SMTP_SERVERS',                // dst_ip
	                            '25',                           // dst_port
	                            'Bad Email Subject',            // msg
	                            'flow:established,to_server; content:"Subject|3a|"; nocase; content:"'.$signature['value'].'"; nocase;',  // rule_content
	                    		'tag:session,600,seconds;',     // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    break;
	                case 'email-attachment':
	                    // LATER email-attachment rule might not match because of line-wrapping
	                    $rules[] = sprintf($rule_format,
	                            'tcp',                          // proto
	                            '$EXTERNAL_NET',                // src_ip
	                            'any',                          // src_port
	                            '<>',                           // direction
	                            '$SMTP_SERVERS',                // dst_ip
	                            '25',                           // dst_port
	                            'Bad Email Attachment',         // msg
	                            'flow:established,to_server; content:"Content-Disposition: attachment|3b| filename=|22|"; content:"'.$signature['value'].'|22|";',  // rule_content   // LATER test and finetune this snort rule https://secure.wikimedia.org/wikipedia/en/wiki/MIME#Content-Disposition
	                    		'tag:session,600,seconds;',     // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    break;
	                case 'domain':
	                    $rules[] = sprintf($rule_format,
	                            'udp',                          // proto
	                            'any',                          // src_ip
	                            'any',                          // src_port
	                            '->',                           // direction
	                            'any',                          // dst_ip
	                            '53',                           // dst_port
	                            'Lookup Of Bad Domain',         // msg
	                            'content:"'.$this->_dnsNameToRawFormat($signature['value']).'"; nocase;',  // rule_content
	                        	'',                             // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    $sid++;
	                    $rules[] = sprintf($rule_format,
	                            'tcp',                          // proto
	                            'any',                          // src_ip
	                            'any',                          // src_port
	                            '->',                           // direction
	                            'any',                          // dst_ip
	                            '53',                           // dst_port
	                            'Lookup Of Bad Domain',         // msg
	                            'content:"'.$this->_dnsNameToRawFormat($signature['value']).'"; nocase;',  // rule_content
	                        	'',                             // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    $sid++;
	                    //break; // domain should also detect the domain name in a url
	                case 'url':
	                    $rules[] = sprintf($rule_format,
	                            'tcp',                          // proto
	                            '$HOME_NET',                    // src_ip
	                            'any',                          // src_port
	                            '->',                           // direction
	                            '$EXTERNAL_NET',                // dst_ip
	                            '$HTTP_PORTS',                  // dst_port
	                            'Outgoing Bad HTTP URL',        // msg
	                            'flow:to_server,established; uricontent:"'.$signature['value'].'"; nocase;',  // rule_content
	                        	'tag:session,600,seconds;',     // tag
        	                    $sid,                           // sid
        	                    1                               // rev
	                    );
	                    break;
	                case 'user-agent':
	                    $rules[] = "";
	                    // TODO write snort user-agent rule
	                    break;
	                case 'snort':
	                    $tmp_rule = $signature['value'];

	                    // rebuild the rule by overwriting the different keywords using preg_replace()
	                    //   sid       - '/sid\s*:\s*[0-9]+\s*;/'
	                    //   rev       - '/rev\s*:\s*[0-9]+\s*;/'
	                    //   classtype - '/classtype:[a-zA-Z_-]+;/'
	                    //   msg       - '/msg\s*:\s*".*"\s*;/'
	                    //   reference - '/reference\s*:\s*.+;/'
	                    $replace_count=array();
	                    $tmp_rule = preg_replace('/sid\s*:\s*[0-9]+\s*;/', 'sid:'.$sid.';', $tmp_rule, -1, $replace_count['sid']);
	                    if (null == $tmp_rule ) break;  // don't output the rule on error with the regex
	                    $tmp_rule = preg_replace('/rev\s*:\s*[0-9]+\s*;/', 'rev:1;', $tmp_rule, -1, $replace_count['rev']);
	                    if (null == $tmp_rule ) break;  // don't output the rule on error with the regex
	                    $tmp_rule = preg_replace('/classtype:[a-zA-Z_-]+;/', 'classtype:'.$classtype.';', $tmp_rule, -1, $replace_count['classtype']);
	                    if (null == $tmp_rule ) break;  // don't output the rule on error with the regex
	                    $tmp_message = sprintf($rule_format_msg, 'snort-rule');
	                    $tmp_rule = preg_replace('/msg\s*:\s*".*"\s*;/', $tmp_message.';', $tmp_rule, -1, $replace_count['msg']);
	                    if (null == $tmp_rule ) break;  // don't output the rule on error with the regex
	                    $tmp_rule = preg_replace('/reference\s*:\s*.+;/', $rule_format_reference.';', $tmp_rule, -1, $replace_count['reference']);
	                        if (null == $tmp_rule ) break;  // don't output the rule on error with the regex

	                    // some values were not replaced, so we need to add them ourselves, and insert them in the rule
	                    $extra_for_rule="";
	                    if (0 == $replace_count['sid']) {
	                    $extra_for_rule .= 'sid:'.$sid.';';
        	            } if (0 == $replace_count['rev']) {
        	                $extra_for_rule .= 'rev:1;';
        	            } if (0 == $replace_count['classtype']) {
        	                $extra_for_rule .= 'classtype:'.$classtype.';';
        	            } if (0 == $replace_count['msg']) {
        	                $extra_for_rule .= $tmp_message.';';
        	            } if (0 == $replace_count['reference']) {
        	                $extra_for_rule .= $rule_format_reference.';';
        	            }
        	            $tmp_rule = preg_replace('/;\s*\)/', '; '.$extra_for_rule.')', $tmp_rule);

    	                // finally the rule is cleaned up and can be outputed
    	                $rules[] = $tmp_rule;

    	                // TODO test using lots of snort rules.
    	            default:
    	                break;
	            }

            }

        }
        print ("#<h1>This part is not finished and might be buggy. Please report any issues.</h1>\n");

        print "#<pre> \n";
        foreach ($rules as $rule)
        print $rule."\n";
        print "#</pre>\n";

        $this->set('rules', $rules);

	}


	public function text($key, $type="") {
	    // check if the key is valid -> search for users based on key
	    $this->loadModel('User');
	    // no input sanitization necessary, it's done by model
	    $user = $this->User->findByAuthkey($key);
	    if (empty($user)) {
	        throw new UnauthorizedException('Incorrect authentication key');
	    }

	    $this->header('Content-Type: text/plain');    // set the content type
	    $this->layout = 'text/default';

	    $this->loadModel('Signature');
	    $params = array(
	        	'conditions' => array('Signature.type' => $type), //array of conditions
	        	'recursive' => 0, //int
	        	'fields' => array('Signature.value'), //array of field names
	         	'order' => array('Signature.value'), //string or array defining order
	        	'group' => array('Signature.value'), //fields to GROUP BY
	    );
	    $signatures = $this->Signature->find('all', $params);

	    $this->set('signatures', $signatures);
	}


	/**
	* // LATER move _dnsNameToRawFormat($name) function to a better place
	 * Converts a DNS name to a raw format usable in NIDS like Snort.
	*   example: foobar.com becomes |06|foobar|03|com|00|
	* @param string $name dns name to be converted
	* @return string raw snort compatible format of the dns name
	*/
	private function _dnsNameToRawFormat($name) {
    	$rawName = "";
    	// explode using the dot
    	$explodedNames = explode('.', $name);
    	// for each part
    	foreach ($explodedNames as $explodedName) {
        	// count the lenght of the part, and add |length| before
        	$length = strlen($explodedName);
        	if ($length > 255) exit('ERROR: dns name is to long for RFC'); // LATER log correctly without dying
        	$hexLength = dechex($length);
            if (1 == strlen($hexLength)) $hexLength = '0'.$hexLength;
        	$rawName .= '|'.$hexLength.'|'.$explodedName;
    	}
    	// put all together
    	$rawName .= '|00|';
    	// and append |00| to terminate the name
    	return $rawName;
	}



}