<?php
/**
 * Common DokuWiki functions
 *
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author     Andreas Gohr <andi@splitbrain.org>
 */

  if(!defined('DOKU_INC')) define('DOKU_INC',realpath(dirname(__FILE__).'/../').'/');
  require_once(DOKU_CONF.'dokuwiki.php');
  require_once(DOKU_INC.'inc/io.php');
  require_once(DOKU_INC.'inc/utf8.php');
  require_once(DOKU_INC.'inc/parserutils.php');

/**
 * Split a page into words
 *
 * Returns an array of of word counts, false if an error occured
 *
 * @author Andreas Gohr <andi@splitbrain.org>
 * @author Christopher Smith <chris@jalakai.co.uk>
 */
function idx_getPageWords($page){
    global $conf;
    $word_idx = file($conf['cachedir'].'/word.idx');
    $swfile   = DOKU_INC.'inc/lang/'.$conf['lang'].'/stopwords.txt';
    if(@file_exists($swfile)){
        $stopwords = file($swfile);
    }else{
        $stopwords = array();
    }

    $body   = rawWiki($page);
    $body   = strtr($body, "\r\n\t", '   ');
    $tokens = explode(' ', $body);
    $tokens = array_count_values($tokens);   // count the frequency of each token

    $words = array();
    foreach ($tokens as $word => $count) {
        $word = utf8_strtolower($word);

        // simple filter to restrict use of utf8_stripspecials
        if (preg_match('/\W/', $word)) {
            $arr = explode(' ', utf8_stripspecials($word,' ','._\-:'));
            $arr = array_count_values($arr);

            foreach ($arr as $w => $c) {
                if (!is_numeric($w) && strlen($w) < 3) continue;
                $words[$w] = $c + (isset($words[$w]) ? $words[$w] : 0);
            }
        } else {
            if (!is_numeric($w) && strlen($w) < 3) continue;
            $words[$word] = $count + (isset($words[$word]) ? $words[$word] : 0);
        }
    }

    // arrive here with $words = array(word => frequency)

    $index = array(); //resulting index
    foreach ($words as $word => $freq) {
	if (is_int(array_search("$word\n",$stopwords))) continue;
        $wid = array_search("$word\n",$word_idx);
        if(!is_int($wid)){
            $word_idx[] = "$word\n";
            $wid = count($word_idx)-1;
        }
        $index[$wid] = $freq;
    }

    // save back word index
    $fh = fopen($conf['cachedir'].'/word.idx','w');
    if(!$fh){
        trigger_error("Failed to write word.idx", E_USER_ERROR);
        return false;
    }
    fwrite($fh,join('',$word_idx));
    fclose($fh);

    return $index;
}

/**
 * Adds/updates the search for the given page
 *
 * This is the core function of the indexer which does most
 * of the work. This function needs to be called with proper
 * locking!
 *
 * @author Andreas Gohr <andi@splitbrain.org>
 */
function idx_addPage($page){
    global $conf;

    // load known words and documents
    $page_idx = file($conf['cachedir'].'/page.idx');

    // get page id (this is the linenumber in page.idx)
    $pid = array_search("$page\n",$page_idx);
    if(!is_int($pid)){
        $page_idx[] = "$page\n";
        $pid = count($page_idx)-1;
        // page was new - write back
        $fh = fopen($conf['cachedir'].'/page.idx','w');
        if(!$fh) return false;
        fwrite($fh,join('',$page_idx));
        fclose($fh);
    }

    // get word usage in page
    $words = idx_getPageWords($page);
    if($words === false) return false;
    if(!count($words)) return true;

    // Open index and temp file
    $idx = fopen($conf['cachedir'].'/index.idx','r');
    $tmp = fopen($conf['cachedir'].'/index.tmp','w');
    if(!$idx || !$tmp){
       trigger_error("Failed to open index files", E_USER_ERROR);
       return false;
    }

    // copy from index to temp file, modifying were needed
    $lno = 0;
    $line = '';
    while (!feof($idx)) {
        // read full line
        $line .= fgets($idx, 4096);
        if(substr($line,-1) != "\n") continue;

        // write a new Line to temp file
        idx_writeIndexLine($tmp,$line,$pid,$words[$lno]);

        $line = ''; // reset line buffer
        $lno++;     // increase linecounter
    }
    fclose($idx);

    // add missing lines (usually index and word should contain
    // the same number of lines, however if the page contained
    // new words the word file has some more lines which need to
    // be added here
    $word_idx = file($conf['cachedir'].'/word.idx');
    $wcnt = count($word_idx);
    for($lno; $lno<$wcnt; $lno++){
        idx_writeIndexLine($tmp,'',$pid,$words[$lno]);
    }

    // close the temp file and move it over to be the new one
    fclose($tmp);
    return rename($conf['cachedir'].'/index.tmp',
                  $conf['cachedir'].'/index.idx');
}

/**
 * Write a new index line to the filehandle
 *
 * This function writes an line for the index file to the
 * given filehandle. It removes the given document from
 * the given line and readds it when $count is >0.
 *
 * @author Andreas Gohr <andi@splitbrain.org>
 */
function idx_writeIndexLine($fh,$line,$pid,$count){
    $line = trim($line);

    if($line != ''){
        $parts = explode(':',$line);
        // remove doc from given line
        foreach($parts as $part){
            if($part == '') continue;
            list($doc,$cnt) = explode('*',$part);
            if($doc != $pid){
                fwrite($fh,"$doc*$cnt:");
            }
        }
    }

    // add doc
    if ($count){
        fwrite($fh,"$pid*$count");
    }

    // add newline
    fwrite($fh,"\n");
}

//Setup VIM: ex: et ts=4 enc=utf-8 :