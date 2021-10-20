package pl.kuezeze;

import pl.kuezeze.bot.BotApplication;

public class Main {

    public static void main(String[] args) {
	    new Thread(BotApplication::new, "Discord Thread").start();
    }
}
