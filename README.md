<p align="center">
  <img alt="RukaBot avatar" width="140" src="https://cdn.discordapp.com/avatars/900457114781241375/55cfd0fb9c0695a4f0786e3e3e938e4e.png?size=2048&quality=lossless">
</p>

<h1 align="center">RukaBot</h1>

<p align="center">
  <img alt="GitHub stars" src="https://img.shields.io/github/stars/cmclient/RukaBot?style=flat-square">
  <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/cmclient/RukaBot?style=flat-square">
  <img alt="GitHub issues" src="https://img.shields.io/github/issues/cmclient/RukaBot?style=flat-square">
</p>

RukaBot is an open-source Discord bot with music player coded in Java using JDA. Feel free to add a star ⭐ to the repository to promote the project.

## 🚧 Prerequisites
- [JDK 25+](https://www.azul.com/downloads/?version=java-25-lts&package=jdk#zulu)
- [Maven](https://maven.apache.org/download.cgi)
- [yt-dlp](https://github.com/yt-dlp/yt-dlp#installation) — needed for YouTube playback (see below)

## 📝 Building bot
```sh
git clone https://github.com/cmclient/RukaBot
cd RukaBot
mvn clean install
```

## ▶️ Running bot
```sh
java -jar target/rukabot-2.0.1-SNAPSHOT-jar-with-dependencies.jar
```

The first run creates `rukabot.cfg` next to the jar and then exits so you can fill it in. Add your bot
token, start the bot again, and it will print an invite link to the console.

## ⚙️ Configuration

Everything lives in `rukabot.cfg`. Only `token` is required to start.

| Option | What it does |
| --- | --- |
| `token` | Your Discord bot token from the [Developer Portal](https://discord.com/developers/applications). |
| `botName` | Name shown in the startup log. |
| `databaseName` | SQLite file used for per-server settings. |
| `youtubeApiKey` | [YouTube Data API key](https://console.cloud.google.com/apis/library/youtube.googleapis.com). Needed for `/play <artist - title>` search; links work without it. |
| `spotifyClientID` / `spotifyClientSecret` | [Spotify app credentials](https://developer.spotify.com/dashboard), so Spotify track links can be played. |
| `ytDlpPath` | Path to the yt-dlp executable. Leave empty to find it on `PATH` automatically. |
| `ytDlpCookiesPath` | Optional cookies file, only useful when hosting on a VPS (see below). |
| `ytDlpJsRuntimes` | Optional JS runtime for yt-dlp, e.g. `deno` or `node`. |
| `youtubeOAuthToken` | Optional, only used by the youtube-source fallback. |
| `youtubePoToken` / `youtubeVisitorData` | Optional, only used by the youtube-source fallback. |

You can also set the token with a `DISCORD_TOKEN` environment variable, which overrides the config file.

## 🎵 A note on YouTube playback

**Install yt-dlp and YouTube playback just works.** If `ytDlpPath` is empty, RukaBot looks for yt-dlp on
your `PATH`, so a normal install needs no configuration at all. You should see this on startup:

```
Using yt-dlp source manager (/usr/local/bin/yt-dlp)
```

If yt-dlp is missing, RukaBot falls back to the built-in youtube-source library and warns you. That
fallback currently **cannot play YouTube** — YouTube broke it, and the fix has to come from upstream, so
every track will fail with `All clients failed to load the item`. Keeping yt-dlp installed and reasonably
up to date is the fix:

```sh
yt-dlp -U
```

Hosting on a VPS or other datacenter IP? YouTube is stricter with those, so you may also need to point
`ytDlpCookiesPath` at a [cookies file](https://github.com/yt-dlp/yt-dlp/wiki/FAQ#how-do-i-pass-cookies-to-yt-dlp)
exported from a logged-in browser. On a home connection this is not needed.

Two things to know: YouTube **playlist** links play only the first video, and Spotify **playlist** links
are not supported — single tracks only. SoundCloud and direct audio links work regardless of yt-dlp.

## 💬 Usage

Invite the bot, join a voice channel, and use `/play`. Run `/help` to see all commands.

## Want to try the bot without hosting it by yourself?
- [Invite Link](https://discord.com/oauth2/authorize?client_id=900457114781241375&scope=bot&permissions=8)
