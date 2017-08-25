<?php
/*
 This library handles album related functions.... wooo!
*/

/*!
	@function get_albums
	@discussion pass a sql statement, and it gets full album info and returns
		an array of the goods.. can be set to format them as well
*/
function get_albums($sql, $action=0) {

	$db_results = mysql_query($sql, dbh());
	while ($r = mysql_fetch_array($db_results)) {
		$album = new Album($r[0]);
		$album->format_album();
		$albums[] = $album;
	}

	return $albums;


} // get_albums

/**
 * get_image_from_source
 * This gets an image for the album art from a source as
 * defined in the passed array. Because we don't know where
 * its comming from we are a passed an array that can look like
 * ['url']	= URL *** OPTIONAL ***
 * ['file']	= FILENAME *** OPTIONAL ***
 * ['raw']	= Actual Image data, already captured
 */
function get_image_from_source($data) {

	// Already have the data, this often comes from id3tags
	if (isset($data['raw'])) {
		return $data['raw'];
	}

	// Check to see if it's a URL
	if (isset($data['url'])) {
		$snoopy = new Snoopy();
		$snoopy->fetch($data['url']);
		return $snoopy->results;
	}

	// Check to see if it's a FILE
	if (isset($data['file'])) {
		$handle = fopen($data['file'],'rb');
		$image_data = fread($handle,filesize($data['file']));
		fclose($handle);
		return $image_data;
	}

	// Check to see if it is embedded in id3 of a song
	if (isset($data['song'])) {
        	// If we find a good one, stop looking
		$getID3 = new getID3();
		$id3 = $getID3->analyze($data['song']);

		if ($id3['format_name'] == "WMA") {
			return $id3['asf']['extended_content_description_object']['content_descriptors']['13']['data'];
		}
		elseif (isset($id3['id3v2']['APIC'])) {
			// Foreach incase they have more then one
			foreach ($id3['id3v2']['APIC'] as $image) {
				return $image['data'];
			}
		}
	} // if data song

	return false;

} // get_image_from_source

/**
 * get_random_albums
 * This returns a random number of albums from the catalogs
 * this is used by the index to return some 'potential' albums to play
 */
function get_random_albums($count=6) {
        // There's a slight chance with this logic that the number of albums
        // returned will be less than the number requested if the id's for the
        // albums have signifigant gaps, but the speed increase is probably
        // worth it
        // - Vlet

        $sql = 'SELECT ';

        for ($i = 0; $i < ceil($count * 1.5); $i++) {
                if ($i > 0) $sql .= ', ';

                $sql .= 'floor(rand() * count(id))';
        }
        $sql .= ' FROM `album`';
        $db_results = Dba::query($sql);

        $sql = '';

        $row = Dba::fetch_row($db_results);

	for ($i = 0; $i < ceil($count * 1.5); $i++) {
                if ($i > 0) $sql .= ' UNION ';
                $sql .= "SELECT `id` FROM (SELECT `id` FROM `album` LIMIT $row[$i],1) t".$i ;
        }

	$db_results = Dba::query($sql);

        $results = array();

        for ($i = 0; $i < $count; $i++) {
                $row = Dba::fetch_assoc($db_results);
                $results[] = $row['id'];
        }

        return $results;
} // get_random_albums

?>