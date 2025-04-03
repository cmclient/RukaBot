package pl.cmclient.bot.audio;

import pl.cmclient.bot.BotApplication;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LinkConverter {

    private final BotApplication bot;
    private SpotifyApi spotifyApi;
    private String id;
    private String type;

    public LinkConverter(BotApplication bot) {
        this.bot = bot;
        try {
            initSpotify();
        } catch (Exception ex) {
            bot.getLogger().error("Failed to init Spotify API", ex);
        }
    }

    private void initSpotify() throws Exception {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(bot.getConfig().getSpotifyClientID())
                .setClientSecret(bot.getConfig().getSpotifyClientSecret())
                .build();

        ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
        ClientCredentials creds = request.grant_type("client_credentials").build().execute();
        spotifyApi.setAccessToken(creds.getAccessToken());
    }

    public ArrayList<String> convert(String link) throws Exception {
        String[] firstSplit = link.split("/");
        String[] secondSplit;

        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            this.type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            this.type = firstSplit[3];
        }
        this.id = secondSplit[0];
        ArrayList<String> listOfTracks = new ArrayList<>();

        if (type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }

        if (type.contentEquals("playlist")) {
            GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(id).build();
            Playlist playlist = playlistRequest.execute();
            Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                Track track = (Track) i.getTrack();
                String trackID = track.getId();
                listOfTracks.add(getArtistAndName(trackID));
            }

            return listOfTracks;
        }

        return null;
    }

    private String getArtistAndName(String trackID) throws Exception {
        GetTrackRequest trackRequest = spotifyApi.getTrack(trackID).build();

        Track track = trackRequest.execute();
        String artistNameAndTrackName;

        ArtistSimplified[] artists = track.getArtists();
        artistNameAndTrackName = Arrays.stream(artists).map(i -> i.getName() + " ").collect(Collectors.joining("", track.getName() + " - ", ""));

        return artistNameAndTrackName;
    }
}