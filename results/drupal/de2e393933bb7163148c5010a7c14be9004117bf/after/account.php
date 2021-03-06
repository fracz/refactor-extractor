<?

include_once "includes/common.inc";

function account_get_user($uname) {
  $result = db_query("SELECT * FROM users WHERE userid = '$uname'");
  return db_fetch_object($result);
}

function account_email() {
  $output .= "<P>Lost your password?  Fill out your username and e-mail address, and your password will be mailed to you.</P>\n";
  $output .= "<FORM ACTION=\"account.php\" METHOD=\"post\">\n";
  $output .= "<P>\n";
  $output .= " <B>Username:</B><BR>\n";
  $output .= " <INPUT NAME=\"userid\"><BR>\n";
  $output .= "</P>\n";
  $output .= "<P>\n";
  $output .= " <B>E-mail address:</B><BR>\n";
  $output .= " <INPUT NAME=\"email\"><BR>\n";
  $output .= "</P>\n";
  $output .= "<P>\n";
  $output .= " <INPUT NAME=\"op\" TYPE=\"submit\" VALUE=\"E-mail password\">\n";
  $output .= "</P>\n";
  $output .= "</FORM>\n";

  return $output;
}

function account_create($user = "", $error = "") {
  global $theme;

  if ($error) $output .= "<B><FONT COLOR=\"red\">Failed to create account:</FONT>$error</B>\n";
  else $output .= "<P>Registering allows you to comment on stories, to moderate comments and pending stories, to customize the look and feel of the site and generally helps you interact with the site more efficiently.</P><P>To create an account, simply fill out this form an click the `Create account' button below.  An e-mail will then be sent to you with instructions on how to validate your account.</P>\n";

  $output .= "<FORM ACTION=\"account.php\" METHOD=\"post\">\n";
  $output .= "<P>\n";
  $output .= " <B>Username:</B><BR>\n";
  $output .= " <INPUT NAME=\"userid\"><BR>\n";
  $output .= " <SMALL><I>Enter your desired username: only letters, numbers and common special characters are allowed.</I></SMALL><BR>\n";
  $output .= "</P>\n";
  $output .= "<P>\n";
  $output .= " <B>E-mail address:</B><BR>\n";
  $output .= " <INPUT NAME=\"email\"><BR>\n";
  $output .= " <SMALL><I>You will be sent instructions on how to validate your account via this e-mail address - please make sure it is accurate.</I></SMALL><BR>\n";
  $output .= "</P>\n";
  $output .= "<P>\n";
  $output .= " <INPUT NAME=\"op\" TYPE=\"submit\" VALUE=\"Create account\">\n";
  $output .= "</P>\n";
  $output .= "</FORM>\n";

  return $output;
}

function account_session_start($userid, $passwd) {
  global $user;

  $user = new User($userid, $passwd);
  if ($user->id) {
    session_register("user");
    watchdog("message", "session opened for user `$user->userid'");
  }
  else {
    watchdog("warning", "failed login for user `$userid'");
  }
}

function account_session_close() {
  global $user;
  watchdog("message", "session closed for user `$user->userid'");
  session_unset();
  session_destroy();
  unset($user);
}

function account_user_edit() {
  global $allowed_html, $theme, $user;

  if ($user->id) {
    // Generate output/content:
    $output .= "<FORM ACTION=\"account.php\" METHOD=\"post\">\n";
    $output .= "<B>Username:</B><BR>\n";
    $output .= "&nbsp; $user->userid<P>\n";
    $output .= "<I>Required, unique, and can not be changed.</I><P>\n";
    $output .= "<B>Real name:</B><BR>\n";
    $output .= "<INPUT NAME=\"edit[name]\" MAXLENGTH=\"55\" SIZE=\"30\" VALUE=\"$user->name\"><BR>\n";
    $output .= "<I>Optional.</I><P>\n";
    $output .= "<B>Real e-mail address:</B><BR>\n";
    $output .= "&nbsp; $user->real_email<P>\n";
    $output .= "<I>Required, unique, can not be changed and is never displayed publicly: only needed in case you lose your password.</I><P>\n";
    $output .= "<B>Fake e-mail address:</B><BR>\n";
    $output .= "<INPUT NAME=\"edit[fake_email]\" MAXLENGTH=\"55\" SIZE=\"30\" VALUE=\"$user->fake_email\"><BR>\n";
    $output .= "<I>Optional, and displayed publicly. You may spam proof your real e-mail address if you want.</I><P>\n";
    $output .= "<B>URL of homepage:</B><BR>\n";
    $output .= "<INPUT NAME=\"edit[url]\" MAXLENGTH=\"55\" SIZE=\"30\" VALUE=\"$user->url\"><BR>\n";
    $output .= "<I>Optional, but make sure you enter fully qualified URLs only. That is, remember to include \"http://\".</I><P>\n";
    $output .= "<B>Bio:</B> (255 char. limit)<BR>\n";
    $output .= "<TEXTAREA NAME=\"edit[bio]\" COLS=\"35\" ROWS=\"5\" WRAP=\"virtual\">$user->bio</TEXTAREA><BR>\n";
    $output .= "<I>Optional. This biographical information is publicly displayed on your user page.<BR>Allowed HTML tags: ". htmlspecialchars($allowed_html) .".</I><P>\n";
    $output .= "<B>Signature:</B> (255 char. limit)<BR>\n";
    $output .= "<TEXTAREA NAME=\"edit[signature]\" COLS=\"35\" ROWS=\"5\" WRAP=\"virtual\">$user->signature</TEXTAREA><BR>\n";
    $output .= "<I>Optional. This information will be publicly displayed at the end of your comments.<BR>Allowed HTML tags: ". htmlspecialchars($allowed_html) .".</I><P>\n";
    $output .= "<B>Password:</B><BR>\n";
    $output .= "<INPUT TYPE=\"password\" NAME=\"edit[pass1]\" SIZE=\"10\" MAXLENGTH=\"20\"> <INPUT TYPE=\"password\" NAME=\"edit[pass2]\" SIZE=\"10\" MAXLENGTH=\"20\"><BR>\n";
    $output .= "<I>Enter your new password twice if you want to change your current password or leave it blank if you are happy with your current password.</I><P>\n";
    $output .= "<INPUT TYPE=\"submit\" NAME=\"op\" VALUE=\"Save user information\"><BR>\n";
    $output .= "</FORM>\n";

    // Display output/content:
    $theme->header();
    $theme->box("Edit user information", $output);
    $theme->footer();
  }
  else {
    $theme->header();
    $theme->box("Create user account", account_create());
    $theme->box("E-mail password", account_email());
    $theme->footer();
  }
}

function account_user_save($edit) {
  global $user;

  if ($user->id) {
    $data[name] = $edit[name];
    $data[fake_email] = $edit[fake_email];
    $data[url] = $edit[url];
    $data[bio] = $edit[bio];
    $data[signature] = $edit[signature];

    if ($edit[pass1] && $edit[pass1] == $edit[pass2]) $data[passwd] = $edit[pass1];

    user_save($data, $user->id);
  }
}

function account_site_edit() {
  global $cmodes, $corder, $theme, $themes, $user;

  if ($user->id) {
    $output .= "<FORM ACTION=\"account.php\" METHOD=\"post\">\n";
    $output .= "<B>Theme:</B><BR>\n";
    foreach ($themes as $key=>$value) {
      $options1 .= " <OPTION VALUE=\"$key\"". (($user->theme == $key) ? " SELECTED" : "") .">$key - $value[1]</OPTION>\n";
    }
    $output .= "<SELECT NAME=\"edit[theme]\">\n$options1</SELECT><BR>\n";
    $output .= "<I>Selecting a different theme will change the look and feel of the site.</I><P>\n";
    $output .= "<B>Timezone:</B><BR>\n";
    $date = time() - date("Z");
    for ($zone = -43200; $zone <= 46800; $zone += 3600) {
      $options2 .= " <OPTION VALUE=\"$zone\"". (($user->timezone == $zone) ? " SELECTED" : "") .">". date("l, F dS, Y - h:i A", $date + $zone) ." (GMT ". $zone / 3600 .")</OPTION>\n";
    }
    $output .= "<SELECT NAME=\"edit[timezone]\">\n$options2</SELECT><BR>\n";
    $output .= "<I>Select what time you currently have and your timezone settings will be set appropriate.</I><P>\n";
    $output .= "<B>Maximum number of stories:</B><BR>\n";
    for ($stories = 10; $stories <= 30; $stories += 5) {
      $options3 .= "<OPTION VALUE=\"$stories\"". (($user->stories == $stories) ? " SELECTED" : "") .">$stories</OPTION>\n";
    }
    $output .= "<SELECT NAME=\"edit[stories]\">\n$options3</SELECT><BR>\n";
    $output .= "<I>The maximum number of stories that will be displayed on the main page.</I><P>\n";
    foreach ($cmodes as $key=>$value) {
      $options4 .= "<OPTION VALUE=\"$key\"". ($user->mode == $key ? " SELECTED" : "") .">$value</OPTION>\n";
    }
    $output .= "<B>Comment display mode:</B><BR>\n";
    $output .= "<SELECT NAME=\"edit[mode]\">$options4</SELECT><P>\n";
    foreach ($corder as $key=>$value) {
      $options5 .= "<OPTION VALUE=\"$key\"". ($user->sort == $key ? " SELECTED" : "") .">$value</OPTION>\n";
    }
    $output .= "<B>Comment sort order:</B><BR>\n";
    $output .= "<SELECT NAME=\"edit[sort]\">$options5</SELECT><P>\n";
    for ($i = -1; $i < 6; $i++) {
      $options6 .= " <OPTION VALUE=\"$i\"". ($user->threshold == $i ? " SELECTED" : "") .">Filter - $i</OPTION>";
    }
    $output .= "<B>Comment filter:</B><BR>\n";
    $output .= "<SELECT NAME=\"edit[threshold]\">$options6</SELECT><BR>\n";
    $output .= "<I>Comments that scored less than this threshold setting will be ignored.  Anonymous comments start at 0, comments of people logged on start at 1 and moderators can add and subtract points.</I><P>\n";
    $output .= "<INPUT TYPE=\"submit\" NAME=\"op\" VALUE=\"Save site settings\"><BR>\n";
    $output .= "</FORM>\n";

    $theme->header();
    $theme->box("Edit your preferences", $output);
    $theme->footer();
  }
  else {
    $theme->header();
    $theme->box("Create user account", account_create());
    $theme->box("E-mail password", account_email());
    $theme->footer();
  }
}

function account_site_save($edit) {
  global $user;

  if ($user->id) {
    $data[theme] = $edit[theme];
    $data[timezone] = $edit[timezone];
    $data[stories] = $edit[stories];
    $data[mode] = $edit[mode];
    $data[sort] = $edit[sort];
    $data[threshold] = $edit[threshold];
    user_save($data, $user->id);
  }
}

function account_content_edit() {
  global $theme, $user;

  if ($user->id) {
    $output .= "<FORM ACTION=\"account.php\" METHOD=\"post\">\n";

    $output .= "<B>Blocks in side bars:</B><BR>\n";

    $result = db_query("SELECT * FROM blocks WHERE status = 1 ORDER BY module");
    while ($block = db_fetch_object($result)) {
      $entry = db_fetch_object(db_query("SELECT * FROM layout WHERE block = '$block->name' AND user = '$user->id'"));
      $output .= "<INPUT TYPE=\"checkbox\" NAME=\"edit[$block->name]\"". ($entry->user ? " CHECKED" : "") ."> $block->name<BR>\n";
    }

    $output .= "<P><I>Enable the blocks you would like to see displayed in the side bars.</I></P>\n";
    $output .= "<P><INPUT TYPE=\"submit\" NAME=\"op\" VALUE=\"Save content settings\"></P>\n";
    $output .= "</FORM>\n";

    $theme->header();
    $theme->box("Edit site content", $output);
    $theme->footer();
  }
  else {
    $theme->header();
    $theme->box("Create user account", account_create());
    $theme->box("E-mail password", account_email());
    $theme->footer();
  }
}

function account_content_save($edit) {
  global $user;
  if ($user->id) {
    db_query("DELETE FROM layout WHERE user = $user->id");
    foreach (($edit ? $edit : array()) as $block=>$weight) {
      db_query("INSERT INTO layout (user, block) VALUES ('". check_input($user->id) ."', '". check_input($block) ."')");
    }
  }
}

function account_user($uname) {
  global $user, $theme;

  function module($name, $module, $username) {
    global $theme;
    if ($module["user"] && $block = $module["user"]($username, "user", "view")) {
      if ($block["content"]) $theme->box($block["subject"], $block["content"]);
    }
  }

  if ($user->id && $user->userid == $uname) {
    $output .= "<TABLE BORDER=\"0\" CELLPADDING=\"2\" CELLSPACING=\"2\">\n";
    $output .= " <TR><TD ALIGN=\"right\"><B>User ID:</B></TD><TD>$user->userid</TD></TR>\n";
    $output .= " <TR><TD ALIGN=\"right\"><B>Name:</B></TD><TD>". format_data($user->name) ."</TD></TR>\n";
    $output .= " <TR><TD ALIGN=\"right\"><B>E-mail:</B></TD><TD>". format_email($user->fake_email) ."</A></TD></TR>\n";
    $output .= " <TR><TD ALIGN=\"right\"><B>URL:</B></TD><TD>". format_url($user->url) ."</TD></TR>\n";
    $output .= " <TR><TD ALIGN=\"right\" VALIGN=\"top\"><B>Bio:</B></TD><TD>". format_data($user->bio) ."</TD></TR>\n";
    $output .= " <TR><TD ALIGN=\"right\" VALIGN=\"top\"><B>Signature:</B></TD><TD>". format_data($user->signature) ."</TD></TR>\n";
    $output .= "</TABLE>\n";

    // Display account information:
    $theme->header();
    $theme->box("View user settings", $output);
    $theme->footer();
  }
  elseif ($uname && $account = account_get_user($uname)) {
    $block1 .= "<TABLE BORDER=\"0\" CELLPADDING=\"1\" CELLSPACING=\"1\">\n";
    $block1 .= " <TR><TD ALIGN=\"right\"><B>Username:</B></TD><TD>$account->userid</TD></TR>\n";
    $block1 .= " <TR><TD ALIGN=\"right\"><B>E-mail:</B></TD><TD>". format_email($account->fake_email) ."</TD></TR>\n";
    $block1 .= " <TR><TD ALIGN=\"right\"><B>URL:</B></TD><TD>". format_url($account->url) ."</TD></TR>\n";
    $block1 .= " <TR><TD ALIGN=\"right\"><B>Bio:</B></TD><TD>". format_data($account->bio) ."</TD></TR>\n";
    $block1 .= "</TABLE>\n";

    $result = db_query("SELECT c.cid, c.pid, c.lid, c.subject, c.timestamp, s.subject AS story FROM comments c LEFT JOIN users u ON u.id = c.author LEFT JOIN stories s ON s.id = c.lid WHERE u.userid = '$uname' AND s.status = 2 AND c.link = 'story' AND s.timestamp > ". (time() - 1209600) ." ORDER BY cid DESC LIMIT 10");
    while ($comment = db_fetch_object($result)) {
      $block2 .= "<TABLE BORDER=\"0\" CELLPADDING=\"1\" CELLSPACING=\"1\">\n";
      $block2 .= " <TR><TD ALIGN=\"right\"><B>Comment:</B></TD><TD><A HREF=\"story.php?id=$comment->lid&cid=$comment->cid&pid=$comment->pid#$comment->cid\">". check_output($comment->subject) ."</A></TD></TR>\n";
      $block2 .= " <TR><TD ALIGN=\"right\"><B>Date:</B></TD><TD>". format_date($comment->timestamp) ."</TD></TR>\n";
      $block2 .= " <TR><TD ALIGN=\"right\"><B>Story:</B></TD><TD><A HREF=\"story.php?id=$comment->lid\">". check_output($comment->story) ."</A></TD></TR>\n";
      $block2 .= "</TABLE>\n";
      $block2 .= "<P>\n";
      $comments++;
    }

    // Display account information:
    $theme->header();
    if ($block1) $theme->box("User information for $uname", $block1);
    if ($block2) $theme->box("$uname has posted ". format_plural($comments, "comment", "comments") ." recently", $block2);
    module_iterate("module", $uname);
    $theme->footer();
  }
  else {
    // Display login form:
    $theme->header();
    $theme->box("Create user account", account_create());
    $theme->box("E-mail password", account_email());
    $theme->footer();
  }
}

function account_validate($user) {
  global $type2index;

  // Verify username and e-mail address:
  if (empty($user[real_email]) || (!eregi("^[_\.0-9a-z-]+@([0-9a-z][0-9a-z-]+\.)+[a-z]{2,3}$", $user[real_email]))) $error .= "<LI>the specified e-mail address is not valid.</LI>\n";
  if (empty($user[userid]) || (ereg("[^a-zA-Z0-9_-]", $user[userid]))) $error .= "<LI>the specified username is not valid.</LI>\n";
  if (strlen($user[userid]) > 15) $error .= "<LI>the specified username is too long: it must be less than 15 characters.</LI>\n";

  // Check to see whether the username or e-mail address are banned:
  if ($ban = ban_match($user[userid], $type2index[usernames])) $error .= "<LI>the specified username is banned  for the following reason: <I>$ban->reason</I>.</LI>\n";
  if ($ban = ban_match($user[real_email], $type2index[addresses])) $error .= "<LI>the specified e-mail address is banned for the following reason: <I>$ban->reason</I>.</LI>\n";

  // Verify whether username and e-mail address are unique:
  if (db_num_rows(db_query("SELECT userid FROM users WHERE LOWER(userid) = LOWER('$user[userid]')")) > 0) $error .= "<LI>the specified username is already taken.</LI>\n";
  if (db_num_rows(db_query("SELECT real_email FROM users WHERE LOWER(real_email)=LOWER('$user[real_email]')")) > 0) $error .= "<LI>the specified e-mail address is already registered.</LI>\n";

  return $error;
}

function account_email_submit($userid, $email) {
  global $theme, $site_name, $site_url;

  $result = db_query("SELECT id FROM users WHERE userid = '". check_output($userid) ."' AND real_email = '". check_output($email) ."'");

  if ($account = db_fetch_object($result)) {
    $passwd = account_password();
    $status = 1;
    $hash = substr(md5("$userid. ". time() .""), 0, 12);

    db_query("UPDATE users SET passwd = PASSWORD('$passwd'), hash = '$hash', status = '$status' WHERE userid = '$userid'");

    $link = $site_url ."account.php?op=confirm&name=$userid&hash=$hash";
    $message = "$userid,\n\n\nyou requested us to e-mail you a new password for your $site_name account.  Note that you will need to re-activate your account before you can login.  You can do so simply by visiting the URL below:\n\n    $link\n\nVisiting this URL will automatically re-activate your account.  Once activated you can login using the following information:\n\n    username: $userid\n    password: $passwd\n\n\n-- $site_name crew\n";

    watchdog("message", "new password: `$userid' &lt;$email&gt;");

    mail($email, "Account details for $site_name", $message, "From: noreply");

    $output = "Your password and further instructions have been sent to your e-mail address.";
  }
  else {
    watchdog("warning", "new password: '$userid' and &lt;$email&gt; do not match");
    $output = "Could not sent password: no match for the specified username and e-mail address.";
  }

  $theme->header();
  $theme->box("E-mail password", $output);
  $theme->footer();
}

function account_create_submit($userid, $email) {
  global $theme, $site_name, $site_url;

  $new[userid] = trim($userid);
  $new[real_email] = trim($email);

  if ($error = account_validate($new)) {
    $theme->header();
    $theme->box("Create user account", account_create($new, $error));
    $theme->footer();
  }
  else {
    $new[passwd] = account_password();
    $new[status] = 1;
    $new[hash] = substr(md5("$new[userid]. ". time() .""), 0, 12);

    user_save($new);

    $link = $site_url ."account.php?op=confirm&name=$new[userid]&hash=$new[hash]";
    $message = "$new[userid],\n\n\nsomeone signed up for a user account on $site_name and supplied this email address as their contact.  If it wasn't you, don't get your panties in a knot and simply ignore this mail.\n\nIf this was you, you have to activate your account first before you can login.  You can do so simply by visiting the URL below:\n\n    $link\n\nVisiting this URL will automatically activate your account.  Once activated you can login using the following information:\n\n    username: $new[userid]\n    password: $new[passwd]\n\n\n-- $site_name crew\n";

    watchdog("message", "new account: `$new[userid]' &lt;$new[real_email]&gt;");

    mail($new[real_email], "Account details for $site_name", $message, "From: noreply");

    $theme->header();
    $theme->box("Create user account", "Congratulations!  Your member account has been successfully created and further instructions on how to activate your account have been sent to your e-mail address.");
    $theme->footer();
  }
}

function account_create_confirm($name, $hash) {
  global $theme;

  $result = db_query("SELECT userid, hash, status FROM users WHERE userid = '$name'");

  if ($account = db_fetch_object($result)) {
    if ($account->status == 1) {
      if ($account->hash == $hash) {
        db_query("UPDATE users SET status = 2, hash = '' WHERE userid = '$name'");
        $output .= "Your account has been successfully confirmed.  You can click <A HREF=\"account.php?op=login\">here</A> to login.\n";
        watchdog("message", "$name: account confirmation successful");
      }
      else {
        $output .= "Confirmation failed: invalid confirmation hash.\n";
        watchdog("warning", "$name: invalid confirmation hash");
      }
    }
    else {
      $output .= "Confirmation failed: your account has already been confirmed.  You can click <A HREF=\"account.php?op=login\">here</A> to login.\n";
      watchdog("warning", "$name: attempt to re-confirm account");
    }
  }
  else {
    $output .= "Confirmation failed: no such account found.<BR>";
    watchdog("warning", "$name: attempt to confirm non-existing account");
  }

  $theme->header();
  $theme->box("Account confirmation", $output);
  $theme->footer();
}

function account_password($min_length=6) {
  mt_srand((double)microtime() * 1000000);
  $words = array("foo","bar","guy","neo","tux","moo","sun","asm","dot","god","axe","geek","nerd","fish","hack","star","mice","warp","moon","hero","cola","girl","fish","java","perl","boss","dark","sith","jedi","drop","mojo");
  while(strlen($password) < $min_length) $password .= $words[mt_rand(0, count($words))];
  return $password;
}

function account_track_comments() {
  global $theme, $user;

  $msg = "<P>This page might be helpful in case you want to keep track of your recent comments in any of the current discussions.  You are presented an overview of your comments in each of the stories you participated in along with the number of replies each comment got.\n<P>\n";

  $sresult = db_query("SELECT s.id, s.subject, COUNT(s.id) as count FROM comments c LEFT JOIN stories s ON c.lid = s.id WHERE c.author = $user->id GROUP BY s.id DESC LIMIT 5");

  while ($story = db_fetch_object($sresult)) {
    $output .= "<LI>". format_plural($story->count, comment, comments) ." attached to story `<A HREF=\"story.php?id=$story->id\">". check_output($story->subject) ."</A>`:</LI>\n";
    $output .= " <UL>\n";

    $cresult = db_query("SELECT * FROM comments WHERE author = $user->id AND lid = $story->id");
    while ($comment = db_fetch_object($cresult)) {
      $output .= "  <LI><A HREF=\"story.php?id=$story->id&cid=$comment->cid&pid=$comment->pid#$comment->cid\">". check_output($comment->subject) ."</A> - replies: ". comment_num_replies($comment->cid) ." - score: ". comment_score($comment) ."</LI>\n";
    }
    $output .= " </UL>\n";
  }

  $output = ($output) ? "$msg $output" : "$info <CENTER>You have not posted any comments recently.</CENTER>\n";

  $theme->header();
  $theme->box("Track your comments", $output);
  $theme->footer();
}

function account_track_stories() {
  global $theme, $user;

  $msg = "<P>This page might be helpful in case you want to keep track of the stories you contributed.  You are presented an overview of your stories along with the number of replies each story got.\n<P>\n";

  $result = db_query("SELECT s.id, s.subject, s.timestamp, s.section, COUNT(c.cid) as count FROM stories s LEFT JOIN comments c ON c.lid = s.id WHERE s.status = 2 AND s.author = $user->id GROUP BY s.id DESC");

  while ($story = db_fetch_object($result)) {
    $output .= "<TABLE BORDER=\"0\" CELLPADDING=\"1\" CELLSPACING=\"1\">\n";
    $output .= " <TR><TD ALIGN=\"right\"><B>Subject:</B></TD><TD><A HREF=\"story.php?id=$story->id\">". check_output($story->subject) ."</A> (". format_plural($story->count, "comment", "comments") .")</TD></TR>\n";
    $output .= " <TR><TD ALIGN=\"right\"><B>Section:</B></TD><TD><A HREF=\"search.php?section=". urlencode($story->section) ."\">". check_output($story->section) ."</A></TD></TR>\n";
    $output .= " <TR><TD ALIGN=\"right\"><B>Date:</B></TD><TD>". format_date($story->timestamp) ."</TD></TR>\n";
    $output .= "</TABLE>\n";
    $output .= "<P>\n";
  }

  $theme->header();
  $theme->box("Track your stories", ($output ? "$msg $output" : "$msg You have not posted any stories.\n"));
  $theme->footer();
}

function account_track_site() {
  global $theme, $user, $site_name;

  $result1 = db_query("SELECT c.cid, c.pid, c.lid, c.subject, u.userid, s.subject AS story FROM comments c LEFT JOIN users u ON u.id = c.author LEFT JOIN stories s ON s.id = c.lid WHERE s.status = 2 ORDER BY cid DESC LIMIT 10");

  while ($comment = db_fetch_object($result1)) {
    $block1 .= "<TABLE BORDER=\"0\" CELLPADDING=\"1\" CELLSPACING=\"1\">\n";
    $block1 .= " <TR><TD ALIGN=\"right\"><B>Comment:</B></TD><TD><A HREF=\"story.php?id=$comment->lid&cid=$comment->cid&pid=$comment->pid#$comment->cid\">". check_output($comment->subject) ."</A></TD></TR>\n";
    $block1 .= " <TR><TD ALIGN=\"right\"><B>Author:</B></TD><TD>". format_username($comment->userid) ."</TD></TR>\n";
    $block1 .= " <TR><TD ALIGN=\"right\"><B>Story:</B></TD><TD><A HREF=\"story.php?id=$comment->lid\">". check_output($comment->story) ."</A></TD></TR>\n";
    $block1 .= "</TABLE>\n";
    $block1 .= "<P>\n";
  }
  $block1 = ($block1) ? $block1 : "<CENTER>There have not posted any comments recently.</CENTER>\n";

  $users_total = db_result(db_query("SELECT COUNT(id) FROM users"));

  $stories_posted  = db_result(db_query("SELECT COUNT(id) FROM stories WHERE status = 2"));
  $stories_queued  = db_result(db_query("SELECT COUNT(id) FROM stories WHERE status = 1"));
  $stories_dumped = db_result(db_query("SELECT COUNT(id) FROM stories WHERE status = 0"));

  $result = db_query("SELECT u.userid, COUNT(s.author) AS count FROM stories s LEFT JOIN users u ON s.author = u.id GROUP BY s.author ORDER BY count DESC LIMIT 10");
  while ($poster = db_fetch_object($result)) $stories_posters .= format_username($poster->userid) .", ";

  $comments_total = db_result(db_query("SELECT COUNT(cid) FROM comments"));
  $comments_score = db_result(db_query("SELECT TRUNCATE(AVG(score / votes), 2) FROM comments WHERE votes > 0"));

  $result = db_query("SELECT u.userid, COUNT(c.author) AS count FROM comments c LEFT JOIN users u ON c.author = u.id GROUP BY c.author ORDER BY count DESC LIMIT 10");
  while ($poster = db_fetch_object($result)) $comments_posters .= format_username($poster->userid) .", ";

  $diaries_total = db_result(db_query("SELECT COUNT(id) FROM diaries"));

  $result = db_query("SELECT u.userid, COUNT(d.author) AS count FROM diaries d LEFT JOIN users u ON d.author = u.id GROUP BY d.author ORDER BY count DESC LIMIT 10");
  while ($poster = db_fetch_object($result)) $diaries_posters .= format_username($poster->userid) .", ";

  $block2 .= "<TABLE BORDER=\"0\" CELLPADDING=\"2\" CELLSPACING=\"1\">\n";
  $block2 .= " <TR><TD ALIGN=\"right\" VALIGN=\"top\"><B>Users:</B></TD><TD>$users_total users</TD></TR>\n";
  $block2 .= " <TR><TD ALIGN=\"right\" VALIGN=\"top\"><B>Stories:</B></TD><TD>$stories_posted posted, $stories_queued queued, $stories_dumped dumped<BR><I>[most frequent posters: $stories_posters ...]</I></TD></TR>\n";
  $block2 .= " <TR><TD ALIGN=\"right\" VALIGN=\"top\"><B>Comments:</B></TD><TD>$comments_total comments with an average score of $comments_score<BR><I>[most frequent posters: $comments_posters ...]</I></TD></TR>\n";
  $block2 .= "</TABLE>\n";

  $theme->header();
  $theme->box("Recent comments", $block1);
  $theme->box("Site statistics", $block2);
  $theme->footer();
}

// Security check:
if (strstr($name, " ") || strstr($hash, " ")) {
  watchdog("error", "account: attempt to provide malicious input through URI");
  exit();
}

switch ($op) {
  case "Login":
    account_session_start($userid, $passwd);
    header("Location: account.php?op=info");
    break;
  case "E-mail password":
    account_email_submit($userid, $email);
    break;
  case "Create account":
    account_create_submit($userid, $email);
    break;
  case "confirm":
    account_create_confirm($name, $hash);
    break;
  case "Save user information":
    account_user_save($edit);
    account_user($user->userid);
    break;
  case "Save site settings":
    account_site_save($edit);
    header("Location: account.php?op=info");
    break;
  case "Save content settings":
    account_content_save($edit);
    account_user($user->userid);
    break;
  case "logout":
    account_session_close();
    header("Location: account.php?op=info");
    break;
  case "view":
    switch ($topic) {
      case "info":
        account_user($user->userid);
        break;
      default:
        account_user($name);
    }
    break;
  case "track":
    switch ($topic) {
      case "site":
        account_track_site();
        break;
      case "stories":
        account_track_stories();
        break;
      default:
        account_track_comments();
    }
    break;
  case "edit":
    switch ($topic) {
      case "content":
        account_content_edit();
        break;
      case "site":
        account_site_edit();
        break;
      default:
        account_user_edit();
    }
    break;
  default:
    account_user($user->userid);
}

?>