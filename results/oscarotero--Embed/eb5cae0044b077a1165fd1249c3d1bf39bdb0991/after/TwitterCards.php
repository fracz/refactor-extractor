<?php
/**
 * Generic twitter cards provider.
 * Load the twitter cards data of an url and store it
 */
namespace Embed\Providers;

use Embed\Url;

class TwitterCards extends Provider {
	public function __construct (Url $Url) {
		$this->url = $Url->getUrl();

		$this->loadData($Url);
	}

	protected function loadData (Url $Url) {
		try {
			if (($response = $Url->getContent()) === '') {
				return false;
			}

			$errors = libxml_use_internal_errors(true);
			$Html = new \DOMDocument();
			$Html->loadHTML($response);
			libxml_use_internal_errors($errors);
		} catch (\Exception $E) {
			return false;
		}

		foreach ($Html->getElementsByTagName('meta') as $Tag) {
			if ($Tag->hasAttribute('name') && (strpos($Tag->getAttribute('name'), 'twitter:') === 0)) {
				$this->set(substr($Tag->getAttribute('name'), 8), $Tag->getAttribute('content'));
			}
		}
	}
}
?>