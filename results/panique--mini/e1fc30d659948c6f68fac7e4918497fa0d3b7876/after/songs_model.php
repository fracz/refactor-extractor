<?php

class Songs_Model
{
    /**
     * Every model needs a database connection, passed to the model
     */
    function __construct($db) {
        try {
            $this->db = $db;
        } catch (PDOException $e) {
            exit('Database connection could not be established.');
        }
    }

    /**
     * Get all songs from database
     */
    public function getAllSongs()
    {
        $sql = "SELECT id, artist, track, link FROM song";
        $query = $this->db->prepare($sql);
        $query->execute();

        // fetchAll() is the PDO method that gets all result rows
        return $query->fetchAll();
    }

    /**
     * Add a song to database
     * @param $artist string Artist
     * @param $track string Track
     * @param $link string Link
     */
    public function addSong($artist, $track, $link)
    {
        // clean the input from javascript code for example
        $artist = strip_tags($artist);
        $track = strip_tags($track);
        $link = strip_tags($link);

        $sql = "INSERT INTO song (artist, track, link) VALUES (:artist, :track, :link)";
        $query = $this->db->prepare($sql);
        $query->execute(array(':artist' => $artist, ':track' => $track, ':link' => $link));
    }

    /**
     * Delete a song in the database
     * @param $song_id integer ID of song
     */
    public function deleteSong($song_id)
    {
        $sql = "DELETE FROM song WHERE id = :song_id";
        $query = $this->db->prepare($sql);
        $query->execute(array(':song_id' => $song_id));
    }
}