<?php

/**
* ownCloud
*
* @author Frank Karlitschek
* @copyright 2010 Frank Karlitschek karlitschek@kde.org
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
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

require_once('inc/lib_base.php');

if(isset($_GET['dir'])) $dir=$_GET['dir']; else $dir='';

if(isset($_GET['file'])) {

  OC_FILES::get($dir,$_GET['file']);
OC_FILES::get($dir,$_GET['file']);
OC_FILES::get($dir,$_GET['file']);
echo('heya');
}else{

  OC_UTIL::addscript('js/ajax.js');
  OC_UTIL::showheader();

  OC_FILES::showbrowser($CONFIG_DATADIRECTORY,$dir);
echo('hi');
  echo('<br /><br /><p class="hint">Hint: Mount it via webdav like this: <a href="webdav://'.$_SERVER["HTTP_HOST"].$WEBROOT.'/webdav/owncloud.php">webdav://'.$_SERVER["HTTP_HOST"].$WEBROOT.'/webdav/owncloud.php</a></p>');

  OC_UTIL::showfooter();

}

?>