<?

include "includes/theme.inc";

$output = "
  <DL>
   <DT><B>What is a FAQ?</B></DT>
   <DD>
    The <I>Online Jargon Files</I> written by Eric Raymond define a FAQ as:
    <P><B>FAQ</B> /F-A-Q/ or /fak/ n.<BR>[Usenet] 1. A Frequently Asked Question. 2. A compendium of accumulated lore, posted periodically to high-volume newsgroups in an attempt to forestall such questions.  Some people prefer the term FAQ list or FAQL /fa'kl/, reserving FAQ' for sense 1.</P>
    <P><B>RTFAQ</B> /R-T-F-A-Q/ imp.<BR>[Usenet: primarily written, by analogy with <A HREF=\"#RTFM\">RTFM</A>] Abbreviation for \"Read The FAQ!\", an exhortation that the person addressed ought to read the newsgroup's FAQ list before posting questions.</P>
    <P><B>RTFM</B> /R-T-F-M/ imp.<BR>[Unix] Abbreviation for \"Read The Fucking Manual\". 1. Used by gurus to brush off questions they consider trivial or annoying.  2. Used when reporting a problem to indicate that you aren't just asking out of randomness. \"No, I can't figure out how to interface Unix to my toaster, and yes, I have RTFM.\"  Unlike sense 1, this use is considered polite.</P>
    <P><B>User</B> n.<BR>1. Someone doing `real work' with the computer, using it as a means rather than an end. Someone who pays to use a computer.  2. A programmer who will believe anything you tell him.  One who asks silly questions. [GLS observes: This is slightly unfair.  It is true that users ask questions (of necessity). Sometimes they are thoughtful or deep.  Very often they are annoying or downright stupid, apparently because the user failed to think for two seconds or look in the documentation before bothering the maintainer.]  3. Someone who uses a program from the outside, however skillfully, without getting into the internals of the program.  One who reports bugs instead of just going ahead and fixing them.</P>
   </DD>

   <DT><B>What is $site_name all about?</B></DT>
   <DD>$site_name is a news and discussion forum.  It is a <I>tool</I>.  And as a tool, it aims to create an environment to make discussions possible.  $site_name is not an organization, but a non-profit hobby project instead.<P>People log in to the site, read the news, select one or more items and start to discuss and post their reactions.  $site_name is a community site that relies solely on reader-contributed content.<P>First off, we strive towards discussion rather then to deliver news on its own: discussion can really make a story considerably better as user comments frequently `upstage' the story itself.<P>Secondly, we aim towards a high quality of content and content posters by using and experimenting with several techniques and systems such as trust metrics, scoring, moderation and collaborative filtering.<P></DD>

   <DT><B>Isn't $site_name similar to slashdot.org and kuro5hin.org?</B></DT>
   <DD>To some extend, yes.  Credit where credit is due: we borrowed many ideas (like the interface and moderation) from <A HREF=\"http://slashdot.org/\">slashdot.org</A> and <A HREF=\"http://kuro5hin.org/\">kuro5hin.org</A> because they have many good ideas about discussion forums.  We do not use their code or any permutation thereof as $site_name has been written entirely from scratch.  Our goal was to create a similar base initially, and to go from there and to transform $site_name into something different by adding new and unique features.<P></DD>

   <DT><B><A NAME=\"moderation\">Why moderatiom, trust metrics and collaborative filtering?</A></B></DT>
   <DD>To help individuals and communities address the challenges of information overload.<P>As each new piece of information competes for attention, people quickly tend to become overwhelmed and seek assistance in identifying the most interesting, worthwhile, valuable or enteraining items.  Not to mention the fact, reader-contributed content and other levels of interactivity tend to become chaotic, bloated and disreputable.<P>Therefore, we decided to develop a public system powered by a community that aims to bring quality content to everyone's attention and to filter out all junk: to <I>sort the wheat from the chaff</I>.  The output should be something clean and homogenized featuring quality content, and should slide down the gullet far more easily.  Another objective is to provide a customized service according to public and individual preferences, whether expressed or inferred.<P>Yes, you are right.  It all sounds a bit idealistic, not to mention hypothetical.  However, don't get this wrong: this isn't a new concept, various such systems exist nowadays (like <A HREF=\"http://slashdot.org/\">slashdot.org</A> or <A HREF=\"http://www.kuro5hin.org/\">kuro5hin.org</A>).  We just happen to want our own system.<P>Last but not least we, the $site_name team, don't want the responsibility to manually review each post and to select the ones worthy.  Systematic editing by individual editors is nice and dandy, if you get paid for it or if you have some time to kill.  Afterall, we are not writers, critics nor reviewers for that matter; we are programmers, designers and technicians.<P></DD>

   <DT><B>How does submission moderation work?</B></DT>
   <DD>Anyone who happens by, and has some news or some thoughts they'd like to share, can submit new content for consideration.  After someone has submitted something, their story is added to a queue.  All registered users can access this list of pending stories, that is, stories that have been submitted, but do not yet appear on the public front page. Those registered users can vote whether they think the story should be posted or not. When enough people vote to post a story, the story is pushed over the threshold and up it goes on the public page. On the other hand, when too many people voted to drop a story, the story will get trashed.<P></DD>

   <DT><B>How does comment moderation work?</B></DT>
   <DD>If you create an account and log in, you will be able to moderate comments.  This lets you assign a score to a comment on how good you think the comment was or how visible you think it should be.  When more then one person rates a comment, the overall rating is just a simple average of all ratings.  Comments with a high ratings are more visible then comments with a lower rating.  Like that, comments that gain the approbation of participants will gradually move up through statistical effects and pointless comments will sink into oblivion.<P>Hence, the purpose of comment moderation is two-fold: <UL><LI>To bring the really good comments to everyone's attention.</LI><LI>To hide or get get rid of spam, flamebait and trolls.</LI></UL>In the latter, comment moderation provides a technical solution to a social problem.<P></DD>

   <DT><B>Why would I want to create a user account?</B></DT>
   <DD>No $site_name participant can use his own name or handle to post comments until they sign up and submit their e-mail address.  Those who do not may participate as `$anonymous', but they will suffer numerous disadvantages, not the least that their posts begin at a lower score.<P>In contrast, those with a user account can use their own name or handle and are granted various priveleges: the most important is probably the ability to moderate new submissions and to rate comments.  Also, registered users can tune the site to their personal needs.  For instance: they can select one of the available themes to alter the look-and-feel of the site or they can fine-tune the values of various settings to their likings.<P></DD>

   <DT><B>I forgot my password, what do I do?</B></DT>
   <DD>--- under construction ---<P></DD>

   <DT><B>I have a cool story that you should post, what do I do?</B></DT>
   <DD>Check out the <A HREF=\"submit.php\">submission form</A>.  If you fill out that form, your contribution gets shipped off to the submission queue for evaluation, <A HREF=\"#moderation\">moderation</A>, and possibly even posting.<P></DD>

   <DT><B>How reliable are the stories and comments?</B></DT>
   <DD>There is one big problem with user-contributed news and comments: `reliability and quality'.<P>The stories are not likely to suffer from this problem as they tend to provide links to other sites that are reputable and have dependable information that can be trusted.  The problem with reliability is probably in the comments people post.  Sometimes they are helpful, accurate, and even amusing, but other times they are not.  Comments stand out as a good idea and can really make a story considerably better, but we have to face the facts.  Not all comments add value to a discussion: some are redundant, off-topic or even completly fake.<P>We try to address this problem by experimenting with trust metrics, moderation and collaborative filtering.  Thus, we aim towards a high quality of content and content posters by using and experimenting with several such techniques and systems.<P></DD>

   <DT><B>Why did my comment get deleted?</B></DT>
   <DD>It probably didn't.  It probably just got moderated down by our army of moderators. Try browsing at a lower threshold and see if your comment becomes visible.<P></DD>

   <DT><B>I found a bug or encountered a problem, what do I do?</B></DT>
   <DD>Write us a bugreport or send us a patch!  Writing a good bug report takes patience, but doing it right the first time saves time for us and for you.  It is most helpful when a good description of the problem is included in the bug report. That is, a good example of all the things you did that led to the problem and the problem itself exactly described. The best reports are those that include a full example showing how to reproduce the bug or problem.<P></DD>

   <DT><B>Is the source code of this site available?</B></DT>
   <DD>This site is powered by <A HREF=\"http://www.fsf.org/\">Free Software</A>; including <A HREF=\"http://www.apache.org/\">Apache</A>, <A HREF=\"http://www.php.net/\">PHP</A>, <A HREF=\"http://www.mysql.com/\">MySQL</A> and <A HREF=\"http://www.linux.com/\">Linux</A>, and is inspired by several <A HREF=\"http://www.fsf.org/\">Free Software</A> projects.  Therefor we have decided to make the software engine of this site available under terms of GPL.<P>However, the sources are <B>not</B> available yet at this time, but will be released as soon we have a first, well-rounded source tree that has proven to be stable.  If you can't wait or in case you have big plans (like `total domination') with the engine, don't hesitate to contact us and we might grant you CVS access.<P></DD>

   <DT><B>What features does the engine have?</B></DT>
   <DD>
    <LI>A theme system: the entire website is fully themable in terms of colors, layout, look-and-feel and markup.</LI>
    <LI>A user account system with session management, secure authentication, human-readable password generator, user and page preferences, comment tracker and so on.</LI>
    <LI>A discussion system: supports different display (<I>threaded</I>, <I>flat</I>, <I>netsted</I>) and order (<I>newest first</I>, <I>oldest first</I>, <I>highest scorings first</I>), comment moderation, customable HTML-support, etc.</LI>
    <LI>A submission queue and submission moderation.</LI>
    <LI>An embedded diary system.</LI>
    <LI>Anonymous reader/poster support across the entire site in case visitors prefers to remain anonymous or in case cookies are disabled.</LI>
    <LI>An administrator section which is considered the control center of the website.</LI>
    <LI>A database abstraction layer: allows the website to run on top of different database systems.</LI>
    <LI>A RDF/RSS backend which allows third party websites to become <I>channels</I> in your website with a minimum of extra work.</LI>
    <LI>...</LI>
    <P>
   </DD>

   <DT><B>What is your disclaimer?</B></DT>
   <DD>All messages made available (including any discussion goups and bulletin boards) and any opinions, advice, statements or other information contained in any messages posted or transmitted by any third party are the responsibility of the author of that message and not of $site_name. The fact that a particular message is posted on or transmitted using this website does not mean that $site_name has endorsed that message in any way or verified the accuracy, completeness or usefulness of any message.<P>Furthermore, all persons who use $site_name are advised not to use them for solicitations or any other commercial purposes.  We make no representation regarding the merchantability or fitness for any particular purpose of any message posted by any third party. Users are encouraged to seek the advice of their appropriate professional advisors, when evaluating the information contained in any message. $site_name is not responsible for any defamatory, offensive or illegal conduct of any user.<P></DD>

   <DT><B>What are your terms and conditions?</B></DT>
   <DD>
    This website includes threaded discussion groups and discussion forums which allow feedback to $site_name and real-time interaction between the persons who use this service.  The responsibility for any opinions, advice, statements or other information contained in any messages posted or transmitted by any third party on this website resides solely with the author.<P>
    <U>1. When using the discussion forums, you may NOT:</U><UL><LI>Keep any other person from using and enjoying the discussion forums.</LI><LI>Post or transmit any messages (or content in general) that would constitute or encourage conduct that would constitute a criminal offense, give rise to civil liability or otherwise violate any local, state, national or international law, including any threatening, abusive libelous, defamatory, obscene, vulgar, pornographic, profane or indecent messages of any kind.</LI><LI>Post or transmit any information, software or other materials which violate or infringe upon the rights of others, including their privacy or publicity rights, or which is protected by copyright, trademark or other proprietary rights, without first obtaining permission from the person who owns or holds that right.</LI><LI>Use the discussion forum in any way for solicitations or other commercial purposes.</LI></UL><P>
    <U>2. $site_name's right to monitor the discussion forums:</U><BR>You understand that $site_name has no obligation to monitor the discussion forum.  However $site_name reserves the right at all times to edit, refuse to post or to remove any information or materials, in whole or in part, that in $site_name's sole discretion are objectionable or in violation of these terms and conditions and to disclose any information necessary to satisfy or governmental request.<P>
   </DD>
  </DL>";

$theme->header();
$theme->box("Frequently Asked Questions", $output);
$theme->footer();

?>