package pl.cmclient.bot.audio;

import lombok.Getter;
import pl.cmclient.bot.BotApplication;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinkConverter {

    private final BotApplication bot;
    private @Getter boolean available;
    private SpotifyApi spotifyApi;

    public LinkConverter(BotApplication bot) {
        this.bot = bot;
        try {
            initSpotify();
        } catch (Exception ex) {
            bot.getLogger().error("Failed to init Spotify API", ex);
        }
    }

    private void initSpotify() throws Exception {
        String spotifyClientID = bot.getConfig().getSpotifyClientID();
        String spotifyClientSecret = bot.getConfig().getSpotifyClientSecret();

        if (spotifyClientID.isEmpty() || spotifyClientSecret.isEmpty()) {
            available = false;
            bot.getLogger().warn("Spotify API token is not defined!");
            return;
        }

        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyClientID)
                .setClientSecret(spotifyClientSecret)
                .build();

        ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
        ClientCredentials creds = request.grant_type("client_credentials").build().execute();
        spotifyApi.setAccessToken(creds.getAccessToken());
        available = spotifyApi != null;
    }

    public List<String> convert(String link) throws Exception {
        String[] firstSplit = link.split("/");
        String[] secondSplit;

        String type;
        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            type = firstSplit[3];
        }
        String id = secondSplit[0];
        List<String> listOfTracks = new ArrayList<>();

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
        ArtistSimplified[] artists = track.getArtists();

        return Arrays.stream(artists).map(i -> i.getName() + " ").collect(Collectors.joining("", track.getName() + " - ", ""));
    }
}