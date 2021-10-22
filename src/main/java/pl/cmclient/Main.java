package pl.cmclient;

import pl.cmclient.bot.BotApplication;

public class Main {

    private Main() {}

    public static void main(String[] args) {
	    new Thread(BotApplication::new, "Discord Thread").start();
    }
}
