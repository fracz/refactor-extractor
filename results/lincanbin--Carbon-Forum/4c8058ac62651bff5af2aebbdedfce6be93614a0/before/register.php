<?php
include(__DIR__ . '/common.php');
require(__DIR__ . '/language/' . ForumLanguage . '/register.php');
$UserName   = '';
$Email      = '';
$Password   = '';
$VerifyCode = '';
$Error    = '';
$ErrorCode     = 104000;
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
	if (!ReferCheck(Request('Post', 'FormHash'))) {
		AlertMsg($Lang['Error_Unknown_Referer'], $Lang['Error_Unknown_Referer'], 403);
	}
	$UserName   = strtolower(Request('Post', 'UserName'));
	$Email      = strtolower(Request('Post', 'Email'));
	$Password   = Request('Post', 'Password');
	$VerifyCode = intval(Request('Post', 'VerifyCode'));
	do{
		if (!($UserName && $Email && $Password && $VerifyCode)) {
			$Error = $Lang['Forms_Can_Not_Be_Empty'];
			$ErrorCode = 104001;
			break;
		}


		if (!IsName($UserName)) {
			$Error = $Lang['UserName_Error'];
			$ErrorCode = 104002;
			break;
		}


		if (!IsEmail($Email)) {
			$Error = $Lang['Email_Error'];
			$ErrorCode = 104003;
			break;
		}


		session_start();
		if (!isset($_SESSION[$Prefix . 'VerificationCode']) ||
			$VerifyCode !== intval($_SESSION[$Prefix . 'VerificationCode'])) {
			$Error = $Lang['VerificationCode_Error'];
			$ErrorCode = 104004;
			break;
		}
		unset($_SESSION[$Prefix . 'VerificationCode']);
		// 释放session防止阻塞
		session_write_close();


		$UserExist = $DB->single("SELECT ID FROM " . $Prefix . "users WHERE UserName = :UserName", array(
			'UserName' => $UserName
		));
		if ($UserExist) {
			$Error = $Lang['This_User_Name_Already_Exists'];
			$ErrorCode = 104005;
			break;
		}

		$NewUserSalt     = mt_rand(100000, 999999);
		$NewUserPassword = md5($Password . $NewUserSalt);
		$NewUserData     = array(
			'ID' => null,
			'UserName' => $UserName,
			'Salt' => $NewUserSalt,
			'Password' => $NewUserPassword,
			'UserMail' => $Email,
			'UserHomepage' => '',
			'PasswordQuestion' => '',
			'PasswordAnswer' => '',
			'UserSex' => 0,
			'NumFavUsers' => 0,
			'NumFavTags' => 0,
			'NumFavTopics' => 0,
			'NewMessage' => 0,
			'Topics' => 0,
			'Replies' => 0,
			'Followers' => 0,
			'DelTopic' => 0,
			'GoodTopic' => 0,
			'UserPhoto' => '',
			'UserMobile' => '',
			'UserLastIP' => $CurIP,
			'UserRegTime' => $TimeStamp,
			'LastLoginTime' => $TimeStamp,
			'LastPostTime' => $TimeStamp,
			'BlackLists' => '',
			'UserFriend' => '',
			'UserInfo' => '',
			'UserIntro' => '',
			'UserIM' => '',
			'UserRoleID' => 1,
			'UserAccountStatus' => 1,
			'Birthday' => date("Y-m-d", $TimeStamp)
		);

		$DB->query('INSERT INTO `' . $Prefix . 'users`
			(
				`ID`, `UserName`, `Salt`, `Password`, `UserMail`,
				`UserHomepage`, `PasswordQuestion`, `PasswordAnswer`,
				`UserSex`, `NumFavUsers`, `NumFavTags`, `NumFavTopics`,
				`NewMessage`, `Topics`, `Replies`, `Followers`,
				`DelTopic`, `GoodTopic`, `UserPhoto`, `UserMobile`,
				`UserLastIP`, `UserRegTime`, `LastLoginTime`, `LastPostTime`,
				`BlackLists`, `UserFriend`, `UserInfo`, `UserIntro`, `UserIM`,
				`UserRoleID`, `UserAccountStatus`, `Birthday`
			)
			VALUES
			(
				:ID, :UserName, :Salt, :Password, :UserMail,
				:UserHomepage, :PasswordQuestion, :PasswordAnswer,
				:UserSex, :NumFavUsers, :NumFavTags,
				:NumFavTopics, :NewMessage, :Topics, :Replies, :Followers,
				:DelTopic, :GoodTopic, :UserPhoto, :UserMobile,
				:UserLastIP, :UserRegTime, :LastLoginTime, :LastPostTime,
				:BlackLists, :UserFriend, :UserInfo, :UserIntro, :UserIM,
				:UserRoleID, :UserAccountStatus, :Birthday
			)', $NewUserData);
		$CurUserID      = $DB->lastInsertId();
		//更新全站统计数据
		$NewConfig      = array(
			"NumUsers" => $Config["NumUsers"] + 1,
			"DaysUsers" => $Config["DaysUsers"] + 1
		);
		UpdateConfig($NewConfig);
		$TemporaryUserExpirationTime = 30 * 86400 + $TimeStamp;//默认保持30天登陆状态
		SetCookies(array(
			'UserID' => $CurUserID,
			'UserExpirationTime' => $TemporaryUserExpirationTime,
			'UserCode' => md5($NewUserPassword . $NewUserSalt . $TemporaryUserExpirationTime . $SALT)
		), 30);
		if ($CurUserID == 1) {
			$DB->query("UPDATE `" . $Prefix . "users` SET UserRoleID=5 WHERE `ID`=?", array(
				$CurUserID
			));
		}
		if(extension_loaded('gd')){
			require(__DIR__ . "/includes/MaterialDesign.Avatars.class.php");
			$Avatar = new MDAvtars(mb_substr($UserName, 0, 1, "UTF-8"), 256);
			$Avatar->Save('upload/avatar/large/' . $CurUserID . '.png', 256);
			$Avatar->Save('upload/avatar/middle/' . $CurUserID . '.png', 48);
			$Avatar->Save('upload/avatar/small/' . $CurUserID . '.png', 24);
			$Avatar->Free();
		}
		header('location: ' . $Config['WebsitePath'] . '/');
	}while(false);
}

$DB->CloseConnection();
// 页面变量
$PageTitle   = $Lang['Sign_Up'];
$ContentFile = $TemplatePath . 'register.php';
include($TemplatePath . 'layout.php');