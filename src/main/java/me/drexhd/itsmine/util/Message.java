package me.drexhd.itsmine.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class Message {

    private final boolean prefix;
    private final String text;
    private Translate translations = Translate.empty();

    public Message(String text) {
        this.text = text;
        this.prefix = false;
    }

    public void addVar(String var, String value) {
        translations.add(var, value);
    }


    public Text build() {
        String message = translations.translate(text);
        ChatColor.translate(message);
        return new LiteralText(text);
    }


}
