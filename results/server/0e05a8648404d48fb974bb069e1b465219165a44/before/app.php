<?php
/**
 * ownCloud - media plugin
 *
 * @author Robin Appelman
 * @copyright 2010 Robin Appelman icewind1991@gmail.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/
 *
 */

require_once('apps/media/lib_media.php');

OC_UTIL::addScript('media','music');
OC_UTIL::addScript('media','jquery.jplayer.min');
OC_UTIL::addStyle('media','style');
OC_UTIL::addStyle('media','jplayer');

OC_APP::register( array( 'order' => 3, 'id' => 'media', 'name' => 'Media' ));

OC_APP::addNavigationEntry( array( 'id' => 'media_index', 'order' => 2, 'href' => OC_HELPER::linkTo( 'media', 'index.php' ), 'icon' => OC_HELPER::imagePath( 'media', 'media.png' ), 'name' => 'Media' ));
OC_APP::addSettingsPage( array( 'id' => 'media_settings', 'order' => 5, 'href' => OC_HELPER::linkTo( 'media', 'settings.php' ), 'name' => 'Media', 'icon' => OC_HELPER::imagePath( 'files', 'media.png' )));
?>