package me.drexhd.itsmine.util;

import java.util.HashMap;
import java.util.Map;

public class Translate {

    private HashMap<String, String> translations = new HashMap<>();

    private Translate() { }

    public static Translate of(String var, String value) {
        Translate translate = new Translate();
        translate.add(var, value);
        return translate;
    }

    public static Translate empty() {
        return new Translate();
    }

    public void add(String var, String value) {
        translations.put(var, value);
    }

    public String translate(String input) {
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            input = input.replaceAll(entry.getKey(), entry.getValue());
        }
        return input;
    }

}
