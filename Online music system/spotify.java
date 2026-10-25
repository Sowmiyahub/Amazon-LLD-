
/*
========================================================
DESIGN: ONLINE MUSIC STREAMING SYSTEM (SPOTIFY-LIKE)
========================================================

1. MusicStreamingService
   - Main service/controller.
   - Manages songs, users, playlists, and playback.
   - Supports searching songs and creating playlists.

2. Song
   - Stores song ID, title, artist, album, and duration.

3. User
   - Stores user details.
   - Owns playlists and a personal library.

4. Playlist
   - Stores a collection of songs.
   - Supports adding, removing, and listing songs.

5. MusicLibrary
   - Stores songs saved by a user.
   - Supports saving and removing songs.

6. MusicPlayer
   - Handles play, pause, resume, and stop operations.
   - Tracks the currently playing song.

7. Artist
   - Stores artist ID and name.

8. PlaybackStatus (Enum)
   - PLAYING, PAUSED, STOPPED.

========================================================
CLASS RELATIONSHIPS
========================================================

MusicStreamingService
  ├── manages Songs
  ├── manages Users
  └── controls MusicPlayer

User
  ├── owns Playlists
  └── has a MusicLibrary

Playlist
  └── contains multiple Songs

MusicLibrary
  └── contains saved Songs

Song
  └── references an Artist

MusicPlayer
  └── plays one Song at a time

========================================================
MAIN FLOW
========================================================

1. Add songs to the catalog.
2. Register a user.
3. Search for a song.
4. Create a playlist and add songs.
5. Save songs to the user's library.
6. Play, pause, resume, or stop playback.

========================================================
*/

// 1. Main service/controller
import java.util.*;

class MusicStreamingService {
    private final Map<String, Song> songs = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();
    private final MusicPlayer player = new MusicPlayer();

    public void addSong(Song song) {
        songs.put(song.getId(), song);
    }

    public void registerUser(User user) {
        users.put(user.getId(), user);
    }

    public Song searchSong(String songId) {
        Song song = songs.get(songId);
        if (song == null) {
            throw new IllegalArgumentException("Song not found");
        }
        return song;
    }

    public List<Song> searchByTitle(String title) {
        List<Song> result = new ArrayList<>();

        for (Song song : songs.values()) {
            if (song.getTitle().toLowerCase()
                    .contains(title.toLowerCase())) {
                result.add(song);
            }
        }

        return result;
    }

    public void createPlaylist(String userId, String playlistName) {
        getUser(userId).createPlaylist(playlistName);
    }

    public void addSongToPlaylist(
            String userId, String playlistName, String songId) {
        User user = getUser(userId);
        Song song = searchSong(songId);
        user.getPlaylist(playlistName).addSong(song);
    }

    public void saveSongToLibrary(String userId, String songId) {
        User user = getUser(userId);
        Song song = searchSong(songId);
        user.getLibrary().saveSong(song);
    }

    public void playSong(String songId) {
        player.play(searchSong(songId));
    }

    public void pausePlayback() {
        player.pause();
    }

    public void resumePlayback() {
        player.resume();
    }

    public void stopPlayback() {
        player.stop();
    }

    private User getUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
    }
}

// 2. Song entity
class Song {
    private final String id;
    private final String title;
    private final Artist artist;
    private final String album;
    private final int durationSeconds;

    public Song(String id, String title, Artist artist,
                String album, int durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Invalid duration");
        }

        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.durationSeconds = durationSeconds;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public Artist getArtist() { return artist; }
    public String getAlbum() { return album; }
    public int getDurationSeconds() { return durationSeconds; }

    @Override
    public String toString() {
        return title + " - " + artist.getName();
    }
}

// 3. Artist entity
class Artist {
    private final String id;
    private final String name;

    public Artist(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}

// 4. User entity
class User {
    private final String id;
    private final String name;
    private final Map<String, Playlist> playlists = new HashMap<>();
    private final MusicLibrary library = new MusicLibrary();

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public MusicLibrary getLibrary() { return library; }

    public void createPlaylist(String name) {
        if (playlists.containsKey(name)) {
            throw new IllegalArgumentException("Playlist already exists");
        }
        playlists.put(name, new Playlist(name));
    }

    public Playlist getPlaylist(String name) {
        Playlist playlist = playlists.get(name);
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist not found");
        }
        return playlist;
    }
}

// 5. Playlist entity
class Playlist {
    private final String name;
    private final List<Song> songs = new ArrayList<>();

    public Playlist(String name) {
        this.name = name;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void removeSong(String songId) {
        songs.removeIf(song -> song.getId().equals(songId));
    }

    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public String getName() {
        return name;
    }
}

// 6. User's saved music library
class MusicLibrary {
    private final Map<String, Song> savedSongs = new HashMap<>();

    public void saveSong(Song song) {
        savedSongs.put(song.getId(), song);
    }

    public void removeSong(String songId) {
        savedSongs.remove(songId);
    }

    public List<Song> getSavedSongs() {
        return new ArrayList<>(savedSongs.values());
    }
}

// 7. Music player
class MusicPlayer {
    private Song currentSong;
    private PlaybackStatus status = PlaybackStatus.STOPPED;

    public void play(Song song) {
        currentSong = song;
        status = PlaybackStatus.PLAYING;
        System.out.println("Playing: " + song);
    }

    public void pause() {
        if (status == PlaybackStatus.PLAYING) {
            status = PlaybackStatus.PAUSED;
            System.out.println("Paused: " + currentSong);
        }
    }

    public void resume() {
        if (status == PlaybackStatus.PAUSED) {
            status = PlaybackStatus.PLAYING;
            System.out.println("Resumed: " + currentSong);
        }
    }

    public void stop() {
        currentSong = null;
        status = PlaybackStatus.STOPPED;
        System.out.println("Playback stopped");
    }

    public PlaybackStatus getStatus() {
        return status;
    }

    public Song getCurrentSong() {
        return currentSong;
    }
}

// 8. Playback status
enum PlaybackStatus {
    PLAYING,
    PAUSED,
    STOPPED
}

// 9. Example usage
public class Main {
    public static void main(String[] args) {
        MusicStreamingService service = new MusicStreamingService();

        Artist artist = new Artist("A1", "A. R. Rahman");
        Song song1 = new Song(
            "S1", "Song One", artist, "Album One", 240
        );
        Song song2 = new Song(
            "S2", "Song Two", artist, "Album Two", 210
        );

        service.addSong(song1);
        service.addSong(song2);

        User user = new User("U1", "Arun");
        service.registerUser(user);

        service.createPlaylist("U1", "My Favorites");
        service.addSongToPlaylist("U1", "My Favorites", "S1");
        service.addSongToPlaylist("U1", "My Favorites", "S2");

        service.saveSongToLibrary("U1", "S1");

        System.out.println(service.searchByTitle("Song"));

        service.playSong("S1");
        service.pausePlayback();
        service.resumePlayback();
        service.stopPlayback();
    }
}
