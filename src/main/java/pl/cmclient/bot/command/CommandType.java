package pl.cmclient.bot.command;

import lombok.Getter;

@Getter
public enum CommandType {

    GLOBAL(0, "\uD83C\uDF10 Global"),
    GENERAL(1, "\uD83E\uDE84 General"),
    ADMINISTRATION(2, "\uD83D\uDD11 Administration"),
    MODERATION(3, "\uD83D\uDEE0 Moderation"),
    ECONOMY(4, "\uD83D\uDCB0 Economy"),
    FUN(5, "\uD83D\uDE42 Fun"),
    MUSIC(6,"\uD83C\uDFB5 Music"),
    OWNER(7, "\uD83D\uDC51 Owner");

    private final int id;
    private final String name;

    CommandType(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
