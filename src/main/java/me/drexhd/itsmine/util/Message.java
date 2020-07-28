package me.drexhd.itsmine.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;

public class Message {

    private final boolean prefix;
    private String text;
    private Translate translations = Translate.empty();

    public Message(String text) {
        this.text = text;
        this.prefix = false;
    }

    public MutableText translateColors(String string) {
        MutableText text = new LiteralText("");
        ArrayList<String> strings0 = new ArrayList<>();
        String[] split = string.split("(?<=(&.))");
        for (String split2 : split) {
            strings0.addAll(Arrays.asList(split2.split("(?=(&.))")));
        }
        ArrayList<String> strings = new ArrayList<>();
        //Append the formatting codes to each other
        int i = 0;
        for (String s : strings0) {
            if (s.matches("(&.)") && i > 0 && strings.get(i - 1).matches("(&.)")) {
                String val = strings.get(i - 1);
                val += s;
                strings.add(i - 1, val);
            } else {
                strings.add(i, s);
                i++;
            }
        }
        int j = 0;
        Formatting[] formattings = new Formatting[0];
        MutableText t = new LiteralText("");

        for (String s1 : strings) {
            if (s1.matches("(&.)+")) {
                formattings = toFormattings(s1);
            } else {
                t = new LiteralText(s1);
            }
            j++;
            if (j != 0 && j % 2 == 0) {
                t.formatted(formattings);
                text.append(t);
            }
        }
        if(j==1) return t;
        return text;
    }



    private Formatting[] toFormattings(String string) {
        Formatting[] formattings = new Formatting[string.length() / 2];
        int i = 0;
        for (String s1 : string.split("&")) {
            if (!s1.isEmpty()) {
                Formatting formatting = toFormatting(s1.charAt(0));
                if (formatting != null) formattings[i] = formatting;
                i++;
            }
        }
        return formattings;
    }

    private Formatting toFormatting(char c) {
        switch (c) {
            case 'a':
                return Formatting.GREEN;
            case 'b':
                return Formatting.AQUA;
            case 'c':
                return Formatting.RED;
            case 'd':
                return Formatting.LIGHT_PURPLE;
            case 'e':
                return Formatting.YELLOW;
            case 'f':
                return Formatting.WHITE;
            case '0':
                return Formatting.BLACK;
            case '1':
                return Formatting.DARK_BLUE;
            case '2':
                return Formatting.DARK_GREEN;
            case '3':
                return Formatting.DARK_AQUA;
            case '4':
                return Formatting.DARK_RED;
            case '5':
                return Formatting.DARK_PURPLE;
            case '6':
                return Formatting.GOLD;
            case '7':
                return Formatting.GRAY;
            case '8':
                return Formatting.DARK_GRAY;
            case '9':
                return Formatting.BLUE;
            case 'k':
                return Formatting.OBFUSCATED;
            case 'l':
                return Formatting.BOLD;
            case 'm':
                return Formatting.STRIKETHROUGH;
            case 'n':
                return Formatting.UNDERLINE;
            case 'o':
                return Formatting.ITALIC;
            case 'r':
                return Formatting.RESET;
            default:
                return Formatting.WHITE;
        }
    }

    public void append(String text) {
        this.text += text;
    }

    public void addVar(String var, String value) {
        translations.add(var, value);
    }

    public Text build() {
        String message = translations.translate(text);
        return translateColors(text);
    }


}
