<?

function discussion_score($comment) {
  $value = ($comments->votes) ? $comment->score / $comment->votes : $comments->score;
  return (strpos($value, ".")) ? substr($value ."00", 0, 4) : $value .".00";
}

function discussion_moderate($moderate) {
  global $user, $comment_votes;

  $na = $comment_votes[key($comment_votes)];

  foreach ($moderate as $id=>$vote) {
    if ($user && $vote != $comment_votes[$na] && !user_getHistory($user->history, "c$id")) {
      ### Update the comment's score:
      $result = db_query("UPDATE comments SET score = score $vote, votes = votes + 1 WHERE cid = $id");

      ### Update the user's history:
      user_setHistory($user, "c$id", $vote);
    }
  }
}

function discussion_kids($cid, $mode, $order = 0, $thold = 0, $level = 0, $dummy = 0) {
  global $user, $theme;

  $comments = 0;

  $result = db_query("SELECT c.*, u.* FROM comments c LEFT JOIN users u ON c.author = u.id WHERE c.pid = $cid ORDER BY c.timestamp, c.cid");

  if ($mode == "nested") {
    while ($comment = db_fetch_object($result)) {
      if ($comment->score >= $thold) {
        if ($level && !$comments) print "<UL>";
        $comments++;

        $link = "<A HREF=\"discussion.php?op=reply&sid=$comment->sid&pid=$comment->cid&mode=$mode&order=$order&thold=$thold\"><FONT COLOR=\"$theme->hlcolor2\">reply to this comment</FONT></A>";
        $theme->comment($comment->userid, stripslashes($comment->subject), stripslashes($comment->comment), $comment->timestamp, stripslashes($comment->url), stripslashes($comment->femail), $comment->score, $comment->cid, $link);

        discussion_kids($comment->cid, $mode, $order, $thold, $level + 1, $dummy + 1);
      }
    }
  }
  elseif ($mode == "flat") {
    while ($comment = db_fetch_object($result)) {
      if ($comment->score >= $thold) {
        $link = "<A HREF=\"discussion.php?op=reply&sid=$comment->sid&pid=$comment->cid&mode=$mode&order=$order&thold=$thold\"><FONT COLOR=\"$theme->hlcolor2\">reply to this comment</FONT></A>";
        $theme->comment($comment->userid, check($comment->subject), check($comment->comment), $comment->timestamp, $comment->url, $comment->femail, $comment->score, $comment->cid, $link);
      }
      discussion_kids($comment->cid, $mode, $order, $thold);
    }
  }
  elseif ($mode == "disabled") {
    // do nothing
  }
  else {
    print "ERROR: we should not get here!";
  }

  if ($level && $comments) {
    print "</UL>";
  }
}

function discussion_childs($cid, $mode, $order, $thold, $level = 0, $thread) {
  global $anonymous, $theme, $user;

  ### Perform SQL query:
  $result = db_query("SELECT c.*, u.* FROM comments c LEFT JOIN users u ON c.author = u.id WHERE c.pid = $cid ORDER BY c.timestamp, c.cid");

  if ($level == 0) $thread = "";
  $comments = 0;

  while ($comment = db_fetch_object($result)) {
    if ($level && !$comments) {
      $thread .= "<UL>";
    }

    $comments++;

    ### Compose link:
    $thread .= "<LI><A HREF=\"discussion.php?id=$comment->sid&cid=$comment->cid&pid=$comment->pid";
    $thread .= ($mode) ? "&mode=$mode" : "&mode=threaded";
    $thread .= ($order) ? "&order=$order" : "&order=0";
    $thread .= ($thold) ? "&thold=$thold" : "&thold=0";
    $thread .= "\">". check($comment->subject) ."</A> by ";
    $thread .= ($comment->userid) ? $comment->userid : $anonymous;
    $thread .= " <SMALL>(". discussion_score($comment) .")<SMALL></LI>";

    ### Recursive:
    discussion_childs($comment->cid, $mode, $order, $thold, $level + 1, &$thread);
  }

  if ($level && $comments) {
    $thread .= "</UL>";
  }

  return $thread;
}

function discussion_display($sid, $pid, $cid, $mode, $order, $thold, $level = 0) {
  global $user, $theme;

  ### Pre-process variables:
  $pid = (empty($pid)) ? 0 : $pid;
  $cid = (empty($pid)) ? 0 : $cid;

  ### Compose story-query:
  $result = db_query("SELECT stories.*, users.userid FROM stories LEFT JOIN users ON stories.author = users.id WHERE stories.status != 0 AND stories.id = $sid");
  $story = db_fetch_object($result);

  ### Display story:
  $theme->article($story, "[ <A HREF=\"\"><FONT COLOR=\"$theme->hlcolor2\">home</FONT></A> | <A HREF=\"discussion.php?op=reply&sid=$story->id&pid=0\"><FONT COLOR=\"$theme->hlcolor2\">add a comment</FONT></A> ]");

  ### Display `comment control'-box:
  $theme->commentControl($sid, $title, $thold, $mode, $order);

  ### Compose query:
  $query = "SELECT c.*, u.* FROM comments c LEFT JOIN users u ON c.author = u.id WHERE c.sid = $sid AND c.pid = $pid";
  if ($mode == 'threaded' || mode == 'nested') {
    if ($thold != "") $query .= " AND c.score >= $thold";
    else $query .= " AND c.score >= 0";
  }
  if ($order == 1) $query .= " ORDER BY c.timestamp DESC";
  if ($order == 2) $query .= " ORDER BY c.score DESC";
  $result = db_query("$query");

  print "<FORM METHOD=\"post\" ACTION=\"discussion.php\">\n";

  ### Display the comments:
  while ($comment = db_fetch_object($result)) {
    ### Dynamically compose the `reply'-link:
    if ($pid != 0) {
      list($pid) = mysql_fetch_row(mysql_query("SELECT pid FROM comments WHERE cid = $comment->pid"));
      $link = "<A HREF=\"discussion.php?id=$comment->sid&pid=$pid&mode=$mode&order=$order&thold=$thold\"><FONT COLOR=\"$theme->hlcolor2\">return to parent</FONT></A> | <A HREF=\"discussion.php?op=reply&sid=$comment->sid&pid=$comment->cid&mode=$mode&order=$order&thold=$thold\"><FONT COLOR=\"$theme->hlcolor2\">reply to this comment</FONT></A>";
    }
    else {
      $link = "<A HREF=\"discussion.php?op=reply&sid=$comment->sid&pid=$comment->cid&mode=$mode&order=$order&thold=$thold\"><FONT COLOR=\"$theme->hlcolor2\">reply to this comment</FONT></A> ";
    }

    ### Display the comments:
    if (empty($mode) || $mode == "threaded") {
      $thread = discussion_childs($comment->cid, $mode, $order, $thold);
      $theme->comment($comment->userid, check($comment->subject), check($comment->comment), $comment->timestamp, $comment->url, $comment->femail, $comment->score, $comment->cid, $link, $thread);
    }
    else {
      $theme->comment($comment->userid, check($comment->subject), check($comment->comment), $comment->timestamp, $comment->url, $comment->femail, $comment->score, $comment->cid, $link);
      discussion_kids($comment->cid, $mode, $order, $thold, $level);
    }
  }

  print " <INPUT TYPE=\"hidden\" NAME=\"id\" VALUE=\"$sid\">\n";
  print " <INPUT TYPE=\"submit\" NAME=\"op\" VALUE=\"Moderate comments\">\n";
  print "</FORM>\n";
}

function discussion_reply($pid, $sid, $mode, $order, $thold) {
  global $anonymous, $user, $theme;

  ### Extract parent-information/data:
  if ($pid) {
    $item = db_fetch_object(db_query("SELECT comments.*, users.userid FROM comments LEFT JOIN users ON comments.author = users.id WHERE comments.cid = $pid"));
    $theme->comment($item->userid, check(stripslashes($item->subject)), check(stripslashes($item->comment)), $item->timestamp, stripslashes($item->url), stripslashes($item->femail), $item->score, $item->cid, "reply to this comment");
  }
  else {
    $item = db_fetch_object(db_query("SELECT stories.*, users.userid FROM stories LEFT JOIN users ON stories.author = users.id WHERE stories.status != 0 AND stories.id = $sid"));
    $theme->article($item, "");
  }

  ### Build reply form:
  $output .= "<FORM ACTION=\"discussion.php\" METHOD=\"post\">\n";

  ### Name field:
  if ($user) {
    $output .= "<P>\n";
    $output .= " <B>Your name:</B><BR>\n";
    $output .= " <A HREF=\"account.php\">$user->userid</A> &nbsp; &nbsp; <FONT SIZE=\"2\">[ <A HREF=\"account.php?op=logout\">logout</A> ]</FONT>\n";
    $output .= "</P>\n";
  }
  else {
    $output .= "<P>\n";
    $output .= " <B>Your name:</B><BR>\n";
    $output .= " $anonymous\n";
    $output .= "</P>\n";
  }

  ### Subject field:
  $output .= "<P>\n";
  $output .= " <B>Subject:</B><BR>\n";
  if (!eregi("Re:",$item->subject)) $item->subject = "Re: $item->subject";
    // Only one 'Re:' will just do fine. ;)
  $output .= " <INPUT TYPE=\"text\" NAME=\"subject\" SIZE=\"50\" MAXLENGTH=\"60\" VALUE=\"". stripslashes($item->subject) ."\">\n";
  $output .= "</P>\n";

  ### Comment field:
  $output .= "<P>\n";
  $output .= " <B>Comment:</B><BR>\n";
  $output .= " <TEXTAREA WRAP=\"virtual\" COLS=\"50\" ROWS=\"10\" NAME=\"comment\">". stripslashes($user->signature) ."</TEXTAREA><BR>\n";
  $output .= "</P>\n";

  ### Hidden fields:
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"pid\" VALUE=\"$pid\">\n";
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"sid\" VALUE=\"$sid\">\n";
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"mode\" VALUE=\"$mode\">\n";
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"order\" VALUE=\"$order\">\n";
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"thold\" VALUE=\"$thold\">\n";

  ### Preview button:
  $output .= "<INPUT TYPE=\"submit\" NAME=\"op\" VALUE=\"Preview comment\"> (You must preview at least once before you can submit.)\n";
  $output .= "</FORM>\n";

  $theme->box("Reply", $output);
}

function comment_preview($pid, $sid, $subject, $comment, $mode, $order, $thold) {
  global $anonymous, $user, $theme;

  ### Preview comment:
  if ($user) $theme->comment("", check(stripslashes($subject)), check(stripslashes($comment)), time(), "", "", "na", "", "reply to this comment");
  else $theme->comment($user->userid,  check(stripslashes($subject)), check(stripslashes($comment)), time(), stripslashes($user->url), stripslashes($user->femail), "na", "", "reply to this comment");

  ### Build reply form:
  $output .= "<FORM ACTION=\"discussion.php\" METHOD=\"post\">\n";

  ### Name field:
  if ($user) {
    $output .= "<P>\n";
    $output .= " <B>Your name:</B><BR>\n";
    $output .= " <A HREF=\"account.php\">$user->userid</A> &nbsp; &nbsp; <FONT SIZE=\"2\">[ <A HREF=\"account.php?op=logout\">logout</A> ]</FONT>\n";
    $output .= "</P>\n";
  }
  else {
    $output .= "<P>\n";
    $output .= " <B>Your name:</B><BR>\n";
    $output .= " $anonymous\n";
    $output .= "</P>\n";
  }

  ### Subject field:
  $output .= "<P>\n";
  $output .= " <B>Subject:</B><BR>\n";
  $output .= " <INPUT TYPE=\"text\" NAME=\"subject\" SIZE=\"50\" MAXLENGTH=\"60\" VALUE=\"". stripslashes($subject) ."\">\n";
  $output .= "</P>\n";

  ### Comment field:
  $output .= "<P>\n";
  $output .= " <B>Comment:</B><BR>\n";
  $output .= " <TEXTAREA WRAP=\"virtual\" COLS=\"50\" ROWS=\"10\" NAME=\"comment\">". stripslashes($comment) ."</TEXTAREA><BR>\n";
  $output .= "</P>\n";

  ### Hidden fields:
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"pid\" VALUE=\"$pid\">\n";
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"sid\" VALUE=\"$sid\">\n";
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"mode\" VALUE=\"$mode\">\n";
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"order\" VALUE=\"$order\">\n";
  $output .= "<INPUT TYPE=\"hidden\" NAME=\"thold\" VALUE=\"$thold\">\n";

  ### Preview and submit buttons:
  if (empty($subject)) {
    $output .= "<P>\n";
    $output .= " <FONT COLOR=\"red\"><B>Warning:</B></FONT> you did not supply a <U>subject</U>.\n";
    $outout .= "</P>\n";
    $output .= "<P>\n";
    $output .= " <INPUT TYPE=\"submit\" NAME=\"op\" VALUE=\"Preview comment\">\n";
    $output .= "</P>\n";
  }
  else {
    $output .= "<INPUT TYPE=\"submit\" NAME=\"op\" VALUE=\"Preview comment\">\n";
    $output .= "<INPUT TYPE=\"submit\" NAME=\"op\" VALUE=\"Post comment\">\n";
    $output .= "</FORM>\n";
  }

  $theme->box("Reply", $output);
}

function comment_post($pid, $sid, $subject, $comment, $mode, $order, $thold) {
  global $user, $theme;

  ### Check for fake threads:
  $fake = db_result(db_query("SELECT COUNT(*) FROM stories WHERE id = $sid"), 0);

  ### Check for duplicate comments:
  $duplicate = db_result(db_query("SELECT COUNT(*) FROM comments WHERE pid = '$pid' AND sid = '$sid' AND subject = '". addslashes($subject) ."' AND comment = '". addslashes($comment) ."'"), 0);

  if ($fake != 1) {
    $theme->box("fake comment", "fake comment: $fake");
  }
  elseif ($duplicate != 0) {
    $theme->box("duplicate comment", "duplicate comment: $duplicate");
  }
  else {
    if ($user) {
      ### Add comment to database:
      db_insert("INSERT INTO comments (pid, sid, author, subject, comment, hostname, timestamp) VALUES ($pid, $sid, $user->id, '". addslashes($subject) ."', '". addslashes($comment) ."', '". getenv("REMOTE_ADDR") ."', '". time() ."')");

      ### Compose header:
      $header = "discussion.php?id=$sid";
      $header .= ($mode) ? "&mode=$mode" : "&mode=threaded";
      $header .= ($order) ? "&order=$order" : "&order=0";
      $header .= ($thold) ? "&thold=$thold" : "&thold=0";
    }
    else {
      ### Add comment to database:
      db_insert("INSERT INTO comments (pid, sid, subject, comment, hostname, timestamp) VALUES ($pid, $sid, '". addslashes($subject) ."', '". addslashes($comment) ."', '". getenv("REMOTE_ADDR") ."', '". time() ."')");

      ### Compose header:
      $header .= "discussion.php?id=$sid&mode=threaded&order=1&thold=0";
    }
    header("Location: $header");
  }
}

include "function.inc";
include "config.inc";
include "theme.inc";

if ($save) {
  db_query("UPDATE users SET umode = '$mode', uorder = '$order', thold = '$thold' where id = '$user->id'");
  $user->rehash();
}

switch($op) {
  case "Preview comment":
    $theme->header();
    comment_preview($pid, $sid, $subject, $comment, $mode, $order, $thold);
    $theme->footer();
    break;
  case "Post comment":
    comment_post($pid, $sid, $subject, $comment, $mode, $order, $thold);
    break;
  case "reply":
    $theme->header();
    discussion_reply($pid, $sid, $mode, $order, $thold);
    $theme->footer();
    break;
  case "Moderate comments":
    discussion_moderate($moderate);
  default:
    $theme->header();
    discussion_display($id, $pid, $sid, $mode, $order, $thold);
    $theme->footer();
}

?>