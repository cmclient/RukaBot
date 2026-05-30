package pl.cmclient.bot;

import dev.lavalink.youtube.YoutubeAudioSourceManager;

public class GenYoutubeOauth2 {

    public static void main(String[] args) throws InterruptedException {
        YoutubeAudioSourceManager source = new YoutubeAudioSourceManager();
        source.useOauth2(null, false);  // Triggers device flow

        // The poller runs on a daemon thread — keep the main thread alive
        // until the refresh token is received, then print it.
        while (source.getOauth2RefreshToken() == null) {
            Thread.sleep(2000);
        }

        System.out.println("===================================================");
        System.out.println("Refresh token: " + source.getOauth2RefreshToken());
        System.out.println("===================================================");
    }
}
