package pl.cmclient.bot.audio;

import lombok.Getter;
import pl.cmclient.bot.BotApplication;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.net.URI;
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
        String path = URI.create(link).getPath();
        if (path == null) {
            return null;
        }

        List<String> segments = Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        int index = 0;
        // Spotify sometimes prefixes links with a locale segment, e.g. "/intl-en/track/ID"
        if (index < segments.size() && segments.get(index).startsWith("intl-")) {
            index++;
        }
        // Embed links, e.g. "/embed/track/ID"
        if (index < segments.size() && segments.get(index).equals("embed")) {
            index++;
        }
        // Legacy playlist links, e.g. "/user/{userId}/playlist/ID"
        if (index < segments.size() && segments.get(index).equals("user")) {
            index += 2;
        }

        if (segments.size() < index + 2) {
            return null;
        }

        String type = segments.get(index);
        String id = segments.get(index + 1);
        List<String> listOfTracks = new ArrayList<>();

        if (type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }

        if (type.contentEquals("playlist")) {
            GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(id).build();
            Playlist playlist = playlistRequest.execute();
            Paging<PlaylistTrack> playlistPaging = playlist.getItems();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                IPlaylistItem item = i.getItem();
                if (item == null || item.getType() != ModelObjectType.TRACK) {
                    continue;
                }
                listOfTracks.add(getArtistAndName(item.getId()));
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