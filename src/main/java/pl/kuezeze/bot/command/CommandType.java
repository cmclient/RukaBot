package pl.kuezeze.bot.command;

public enum CommandType {
    GLOBAL(0, "\uD83C\uDF10 Global"),
    ADMINISTRATION(1, "\uD83D\uDD11 Administration"),
    MODERATION(2, "\uD83D\uDEE0 Moderation"),
    ECONOMY(3, "\uD83D\uDCB0 Economy"),
    FUN(4, "\uD83D\uDE42 Fun"),
    GENERAL(5, "\uD83E\uDE84 General"),
    OWNER(6, "\uD83D\uDC51 Owner");

    private final int id;
    private final String name;

    CommandType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
