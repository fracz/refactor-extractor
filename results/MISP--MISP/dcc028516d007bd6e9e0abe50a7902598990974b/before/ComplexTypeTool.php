<?php

class ComplexTypeTool {
	public function checkComplexRouter($input, $type) {
		switch ($type) {
			case 'File':
				return $this->checkComplexFile($input);
				break;
			case 'CnC':
				return $this->checkComplexCnC($input);
				break;
			case 'FreeText':
				return $this->checkFreetext($input);
				break;
			default:
				return false;
		}
	}

	// checks if the passed input matches a valid file description attribute's pattern (filename, md5, sha1, sha256, filename|md5, filename|sha1, filename|sha256)
	public function checkComplexFile($input) {
		$original = $input;
		$type = '';
		$composite = false;
		if (strpos($input, '|')) {
			$composite = true;
			$result = explode('|', $input);
			if (count($result) != 2) $type = 'other';
			if (!preg_match("#^.+#", $result[0])) $type = 'other';
			$type = 'filename|';
			$input = $result[1];
		}
		if (strlen($input) == 32 && preg_match("#[0-9a-f]{32}$#", $input)) $type .= 'md5';
		if (strlen($input) == 40 && preg_match("#[0-9a-f]{40}$#", $input)) $type .= 'sha1';
		if (strlen($input) == 64 && preg_match("#[0-9a-f]{64}$#", $input)) $type .= 'sha256';
		if ($type == '' && !$composite && preg_match("#^.+#", $input)) $type = 'filename';
		if ($type == '') $type = 'other';
		return array('type' => $type, 'value' => $original);
	}

	public function checkComplexCnC($input) {
		$type = '';
		$toReturn = array();
		// check if it's an IP address
		if (filter_var($input, FILTER_VALIDATE_IP)) return array('type' => 'ip-dst', 'value' => $input);
		if (preg_match("#^[A-Z0-9.-]+\.[A-Z]{2,4}$#i", $input)) {
			$result = explode('.', $input);
			if (count($result) > 2) {
				$toReturn['multi'][] = array('type' => 'hostname', 'value' => $input);
				 $pos = strpos($input, '.');
				 $toReturn['multi'][] = array('type' => 'domain', 'value' => substr($input, (1 + $pos)));
				 return $toReturn;
			}
			return array('type' => 'domain', 'value' => $input);
		}

		if (!preg_match("#\n#", $input)) return array('type' => 'url', 'value' => $input);
		return array('type' => 'other', 'value' => $input);
	}

	public function checkFreeText($input) {
		$iocArray = preg_split("/\r\n|\n|\r/", $input);
		$resultArray = array();
		foreach ($iocArray as $ioc) {
			$ioc = trim($ioc);
			$ioc = preg_replace('/[\x00-\x08\x0B\x0C\x0E-\x1F\x80-\x9F]/u', '', $ioc);
			if (empty($ioc)) continue;
			$typeArray = $this->__resolveType($ioc);
			$temp = $typeArray;
			$temp['value'] = $ioc;
			$resultArray[] = $temp;
		}
		return $resultArray;
	}

	private function __resolveType($input) {
		$result = array();
		$input = trim($input);
		$input = strtolower($input);

		// check for hashes
		if (strlen($input) == 32 && preg_match("#[0-9a-f]{32}$#", $input)) return array('types' => array('md5'), 'to_ids' => true, 'default_type' => 'md5');
		if (strlen($input) == 40 && preg_match("#[0-9a-f]{40}$#", $input)) return array('types' => array('sha1'), 'to_ids' => true, 'default_type' => 'sha1');
		if (strlen($input) == 64 && preg_match("#[0-9a-f]{64}$#", $input)) return array('types' => array('sha256'), 'to_ids' => true, 'default_type' => 'sha256');

		// check for IP
		if (filter_var($input, FILTER_VALIDATE_IP)) return array('types' => array('ip-dst', 'ip-src'), 'to_ids' => true, 'default_type' => 'ip-dst');
		if (strpos($input, '/')) {
			$temp = explode('/', $input);
			if (count($temp == 2)) {
				if (filter_var($temp[0], FILTER_VALIDATE_IP)) return array('types' => array('ip-dst', 'ip-src'), 'to_ids' => true, 'default_type' => 'ip-dst');
			}
		}


		// check for domain name, hostname, filename
		if (strpos($input, '.') !== false) {
			$extra = '';
			$temp = explode('.', $input);
			if (strpos($temp[0], ':')) {
				$extra = '([a-z0-9]+):\/\/';
			}

			// check if it is a URL
			if (filter_var($input, FILTER_VALIDATE_URL)) {
				return array('types' => array('url'), 'to_ids' => true, 'default_type' => 'url');
			}

			//if (filter_var($input, FILTER_VALIDATE_URL)) {
			if (preg_match('/^([-\pL\pN]+\.)+([a-z][a-z]|biz|cat|com|edu|gov|int|mil|net|org|pro|tel|aero|arpa|asia|coop|info|jobs|mobi|name|museum|travel)$/u', $input)) {
				if (count($temp) > 2) {
					return array('types' => array('hostname', 'domain'), 'to_ids' => true, 'default_type' => 'hostname');
				} else {
					return array('types' => array('domain'), 'to_ids' => true, 'default_type' => 'domain');
				}
			} else {
				if (!preg_match('/[?:<>|\\*:\/@]/', $input)) {
					return array('types' => array('filename'), 'to_ids' => true, 'default_type' => 'filename');
				}
			}
		}

		if (strpos($input, '\\') !== false) {
			$temp = explode('\\', $input);
			if (strpos($temp[count($temp)-1], '.')) {
				if (!preg_match('/[?:<>|\\*:\/]/', $temp[count($temp)-1])) {
					return array('types' => array('filename'), 'category' => 'Payload installation', 'to_ids' => false, 'default_type' => 'filename');
				}
			} else {
				return array('types' => array('regkey'), 'to_ids' => false, 'default_type' => 'regkey');
			}
		}

		if (strpos($input, '@') !== false) {
			if (filter_var($input, FILTER_VALIDATE_EMAIL)) return array('types' => array('email-src', 'email-dst'), 'to_ids' => true, 'default_type' => 'email-src');
		}

		// check for CVE
		if (preg_match("#^cve-[0-9]{4}-[0-9]{4,9}$#i", $input)) return array('types' => array('vulnerability'), 'category' => 'External analysis', 'to_ids' => false, 'default_type' => 'vulnerability');

		return array('types' => array('text'), 'category' => 'Other', 'to_ids' => false, 'default_type' => 'text');
	}
}