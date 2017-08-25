<?php
/**
 * Copyright (c) 2013 Georg Ehrke georg@ownCloud.com
 * This file is licensed under the Affero General Public License version 3 or
 * later.
 * See the COPYING-README file.
 */
namespace OC\Preview;

if (extension_loaded('imagick')) {

	class SVG extends Provider {

		public function getMimeType() {
			return '/image\/svg\+xml/';
		}

		public function getThumbnail($path,$maxX,$maxY,$scalingup,$fileview) {
			$svg = new \Imagick();
			$svg->setResolution($maxX, $maxY);

			$content = stream_get_contents($fileview->fopen($path, 'r'));
			if(substr($content, 0, 5) !== '<?xml') {
				$content = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>' . $content;
			}

			$svg->readImageBlob($content);
			$svg->setImageFormat('jpg');

			//new image object
			$image = new \OC_Image($svg);
			//check if image object is valid
			if (!$image->valid()) return false;

			return $image;
		}
	}

	\OC\Preview::registerProvider('OC\Preview\SVG');

}