<?php

class BroExport {

	public $rules = array();

	public $header = "#fields indicator\tindicator_type\tmeta.source\tmeta.url\tmeta.do_notice\tmeta.if_in";

	// mapping from misp attribute type to the bro intel type
	// alternative mechanisms are:
	// - alternate: array containing a detection regex and a replacement bro type
	// - composite: for composite misp attributes (domain|ip), use the provided bro type if the second value is queried
	// - replace: run a replacement regex on the value before generating the bro rule
	private $mapping = array(
		'ip-dst' => array('brotype' => 'ADDR', 'alternate' => array('#/#', 'SUBNET')),
		'ip-src' => array('brotype' => 'ADDR', 'alternate' => array('#/#', 'SUBNET')),
		'email-src' => array('brotype' => 'EMAIL'),
		'email-dst' => array('brotype' => 'EMAIL'),
		'email-attachment' => array('brotype' => 'FILE_NAME'),
		'filename' => array('brotype' => 'FILE_NAME'),
		'hostname' => array('brotype' => 'DOMAIN'),
		'domain' => array('brotype' => 'DOMAIN'),
		'domain|ip' => array('brotype' => 'DOMAIN', 'composite' => 'ADDR'),
		'url' => array('brotype' => 'URL', 'replace' => array('#^https?://#', '')),
		'user-agent' => array('brotype' => 'SOFTWARE'),
		'md5' => array('brotype' => 'FILE_HASH'),
		'malware-sample' => array('brotype' => 'FILE_NAME', 'composite' => 'FILE_HASH'),
		'filename|md5' => array('brotype' => 'FILE_NAME', 'composite' => 'FILE_HASH'),
		'sha1' => array('brotype' => 'FILE_HASH'),
		'filename|sha1' => array('brotype' => 'FILE_NAME', 'composite' => 'FILE_HASH'),
		'sha256' => array('brotype' => 'FILE_HASH'),
		'filename|sha256' => array('brotype' => 'FILE_NAME', 'composite' => 'FILE_HASH'),
		'x509-fingerprint-sha1' => array('brotype' => 'CERT_HASH'),
	);

	// export group to misp type mapping
	// the mapped type is in an array format, first value being the misp type, second being the value field used
	public $mispTypes = array(
		'ip' => array(
			array('ip-src', 1),
			array('ip-dst', 1),
			array('domain|ip', 2)
		),
		'url' => array(
			array('url', 1)
		),
		'domain' => array(
			array('hostname', 1),
			array('domain', 1),
			array('domain|ip', 1)
		),
		'email' => array(
			array('email-src', 1),
			array('email-dst', 1)
		),
		'filename' => array(
			array('filename', 1),
			array('email-attachment', 1),
			array('attachment', 1),
			array('filename|md5', 1),
			array('filename|sha1', 1),
			array('filename|sha256', 1),
			array('malware-sample', 1)
		),
		'filehash' => array(
			array('md5', 1),
			array('sha1', 1),
			array('sha256', 1),
			array('filename|md5', 2),
			array('filename|sha1', 2),
			array('filename|sha256', 2),
			array('malware-sample', 2)
		),
		'certhash' => array(
			array('x509-fingerprint-sha1', 1)
		),
		'software' => array(
			array('user-agent', 1)
		)
	);

	private $whitelist = null;

	public function export($items, $orgs, $valueField, $intel, $whitelist, $instanceString) {
		//For bro format organisation
		$orgsName = array();
		// generate the rules
		foreach ($items as $item) {
			if (!isset($orgs[$item['Event']['orgc_id']])) {
				continue;
			} else {
				$orgName = $instanceString . ' (' . $item['Event']['uuid'] . ')' . ' - ' . $orgs[$item['Event']['orgc_id']];
			}
			$ruleFormatReference = Configure::read('MISP.baseurl') . '/events/view/' . $item['Event']['id'];
			$ruleFormat = "%s\t%s\t" . $orgName . "\t" . $ruleFormatReference . "\t%s\t%s";
			$rule = $this->__generateRule($item['Attribute'], $ruleFormat, $valueField, $whitelist);
			if (!empty($rule)) {
				if (!in_array($rule, $intel)) {
					$intel[] = $rule;
				}
			}
		}
		return $intel;
	}

	private function __generateRule($attribute, $ruleFormat, $valueField, $whitelist) {
		if (isset($this->mapping[$attribute['type']])) {
			$brotype = $this->mapping[$attribute['type']]['brotype'];
			$overruled = $this->checkWhitelist($attribute['value'], $whitelist);
			if (isset($this->mapping[$attribute['type']]['alternate'])) {
				if (preg_match($this->mapping[$attribute['type']]['alternate'][0], $attribute['value'])) {
					$brotype = $this->mapping[$attribute['type']]['alternate'][1];
				}
			}
			if ($valueField == 2 && isset($this->mapping[$attribute['type']]['composite'])) {
				$brotype = $this->mapping[$attribute['type']]['composite'];
			}
			$attribute['value'] = $this->replaceIllegalChars($attribute['value']);  // substitute chars not allowed in rule
			if (isset($this->mapping[$attribute['type']]['replace'])) {
				$attribute['value'] = preg_replace(
					$this->mapping[$attribute['type']]['replace'][0],
					$this->mapping[$attribute['type']]['replace'][1],
					$attribute['value']
				);
			}
			return sprintf($ruleFormat,
					($overruled) ? '#OVERRULED BY WHITELIST# ' :
					$attribute['value'],	// value - for composite values only the relevant element is taken
					'Intel::' . $brotype,	// type
					'T',	// meta.do_notice
					'-'  // meta.if_in
			);
		}
		return false;
	}

	/**
	 * Replaces characters that are not allowed in a signature.
	 *   example: " is converted to |22|
	 * @param unknown_type $value
	 */
	public static function replaceIllegalChars($value) {
		$replace_pairs = array(
				'|' => '|7c|', // Needs to stay on top !
				'"' => '|22|',
				';' => '|3b|',
				':' => '|3a|',
				'\\' => '|5c|',
				'0x' => '|30 78|'
		);
		return strtr($value, $replace_pairs);
	}

	public function checkWhitelist($value, $whitelist) {
		foreach ($whitelist as $wlitem) {
			if (preg_match($wlitem, $value)) {
				return true;
			}
		}
		return false;
	}

	public function getMispTypes($type) {
		$mispTypes = array();
		if (isset($this->mispTypes[$type])) {
			$mispTypes = $this->mispTypes[$type];
		}
		return $mispTypes;
	}
}