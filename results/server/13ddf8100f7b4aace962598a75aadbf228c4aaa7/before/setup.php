<?php

if(isset($_POST['install']) and $_POST['install']=='true'){
	$errors=OC_SETUP::install($_POST);
	if(count($errors)>0){
		OC_TEMPLATE::printGuestPage( "", "error", array( "errors" => $errors ));
	}else{
		header( "Location: $WEBROOT");
		exit();
	}
}

class OC_SETUP{
	public static function install($options){
		$error=array();
		$dbtype=$options['dbtype'];
		if(empty($options['login'])){
			$error[]=array('error'=>'username not set');
		};
		if(empty($options['pass'])){
			$error[]=array('error'=>'password not set');
		};
		if(empty($options['directory'])){
			$error[]=array('error'=>'data directory not set');
		};
		if($dbtype=='mysql'){//mysql needs more config options
			if(empty($options['dbuser'])){
				$error[]=array('error'=>'database user not set');
			};
			if(empty($options['dbpass'])){
				$error[]=array('error'=>'database password not set');
			};
			if(empty($options['dbname'])){
				$error[]=array('error'=>'database name not set');
			};
			if(empty($options['dbhost'])){
				$error[]=array('error'=>'database host not set');
			};
			if(!isset($options['dbtableprefix'])){
				$error[]=array('error'=>'database table prefix not set');
			};
		}
		if(count($error)==0){ //no errors, good
			$username=$options['login'];
			$password=$options['pass'];
			$datadir=$options['directory'];

			//write the config file
			OC_CONFIG::setValue('datadirectory',$datadir);
			OC_CONFIG::setValue('dbtype',$dbtype);
			if($dbtype=='mysql'){
				$dbuser=$options['dbuser'];
				$dbpass=$options['dbpass'];
				$dbname=$options['dbname'];
				$dbhost=$options['dbhost'];
				$dbtableprefix=$options['dbtableprefix'];
				OC_CONFIG::setValue('dbname',$dbname);
				OC_CONFIG::setValue('dbhost',$dbhost);
				OC_CONFIG::setValue('dbtableprefix',$dbtableprefix);

				//check if the database user has admin right
				$connection=@mysql_connect($dbhost, $dbuser, $dbpass);
				if(!$connection) {
					$error[]=array('error'=>'mysql username and/or password not valid','hint'=>'you need to enter either an existing account, or the administrative account if you wish to create a new user for ownCloud');
				}else{
					$query="SELECT user FROM mysql.user WHERE user='$dbuser'";//this should be enough to check for admin rights in mysql
					if(mysql_query($query,$connection)){
						self::createDBUser($username,$password,$connection);
						//use the admin login data for the new database user
						OC_CONFIG::setValue('dbuser',$username);
						OC_CONFIG::setValue('dbpassword',$password);

						//create the database
						self::createDatabase($dbname,$username,$connection);
					}else{
						OC_CONFIG::setValue('dbuser',$dbuser);
						OC_CONFIG::setValue('dbpassword',$dbpass);

						//create the database
						self::createDatabase($dbname,$dbuser,$connection);
					}
					//fill the database if needed
					$query="SELECT * FROM $dbname.{$dbtableprefix}users";
					$result = mysql_query($query,$connection);
					if (!$result) {
						OC_DB::createDbFromStructure('db_structure.xml');
					}
					mysql_close($connection);
				}
			}else{
				//in case of sqlite, we can always fill the database
				OC_DB::createDbFromStructure('db_structure.xml');
			}
			if(count($error)==0){
				//create the user and group
				OC_USER::createUser($username,$password);
				OC_GROUP::createGroup('admin');
				OC_GROUP::addToGroup($username,'admin');

				//create htaccess files for apache hosts
				self::createHtaccess();//TODO detect if apache is used

				//and we are done
				OC_CONFIG::setValue('installed',true);
			}
		}
		return $error;
	}

	public static function createDatabase($name,$user,$connection){
		//we cant user OC_BD functions here because we need to connect as the administrative user.
		$query="CREATE DATABASE IF NOT EXISTS  `$name`";
		$result = mysql_query($query,$connection);
		if (!$result) {
			$entry='DB Error: "'.mysql_error($connection).'"<br />';
			$entry.='Offending command was: '.$query.'<br />';
			echo($entry);
		}
		$query="GRANT ALL PRIVILEGES ON  `$name` . * TO  '$user'";
		$result = mysql_query($query,$connection);//this query will fail if there aren't the right permissons, ignore the error
	}

	private static function createDBUser($name,$password,$connection){
		//we need to create 2 accounts, one for global use and one for local user. if we don't speccify the local one,
				//  the anonymous user would take precedence when there is one.
		$query="CREATE USER '$name'@'localhost' IDENTIFIED BY '$password'";
		$result = mysql_query($query,$connection);
		$query="CREATE USER '$name'@'%' IDENTIFIED BY '$password'";
		$result = mysql_query($query,$connection);
	}

	/**
	 * create .htaccess files for apache hosts
	 */
	private static function createHtaccess(){
		global $SERVERROOT;
		global $WEBROOT;
		$content="ErrorDocument 404 /$WEBROOT/templates/404.php\n";
		@file_put_contents($SERVERROOT.'/.htaccess',$content); //supress errors in case we don't have permissions for it

		$content="deny from all";
		file_put_contents(OC_CONFIG::getValue('datadirectory',$SERVERROOT.'/data').'/.htaccess',$content);
	}
}

?>