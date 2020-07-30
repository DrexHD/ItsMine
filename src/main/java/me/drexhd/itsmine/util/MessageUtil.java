package me.drexhd.itsmine.util;

import me.drexhd.itsmine.ItsMineConfig;
import net.minecraft.SharedConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageUtil {

    public static final Logger LOGGER = LogManager.getLogger("ItsMine");


    public static void sendPage(ServerCommandSource source, Text header, int entries, int page, String command, ArrayList<Text> content) {
        int pages = Math.floorDiv(content.size(), entries);
        if (content.size() % entries != 0) pages++;
        if (page < 1) return;
        if (page > pages) return;
        LiteralText message = new LiteralText("");
        message.append(header).append(new LiteralText("\n\n"));
        for (int i = 0; i < content.size(); i += entries) {
            content.subList(i, Math.min(content.size(), i + entries)).forEach(text -> {
                message.append(text).append(new LiteralText("\n"));
            });
        }

        message.append(new LiteralText("\n"));
        Text button_prev = new LiteralText("")
                .append(new LiteralText("<-").formatted(Formatting.WHITE).formatted(Formatting.BOLD))
                .append(new LiteralText(" ")).append(new LiteralText("Prev").formatted(Formatting.GOLD))
                .styled((style) -> {
                    if (page - 1 >= 0) {
                        return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText((page - 1 >= 0) ? "<<<" : "|<").formatted(Formatting.GRAY))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command.replace("%page%", String.valueOf(page - 1))));
                    }
                    return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText((page - 1 >= 0) ? "<<<" : "|<").formatted(Formatting.GRAY)));

                }).append(new LiteralText(" "));
        int finalPages = pages;
        Text button_next = new LiteralText(" ")
                .append(new LiteralText("Next").formatted(Formatting.GOLD))
                .append(new LiteralText(" ")).append(new LiteralText("->").formatted(Formatting.WHITE).formatted(Formatting.BOLD)).append(new LiteralText(" "))
                .styled((style) -> {
                    if (page + 1 <= finalPages) {
                        return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText((page + 1 <= finalPages) ? ">>>" : ">|").formatted(Formatting.GRAY))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command.replace("%page%", String.valueOf(page + 1))));
                    }
                    return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText((page + 1 <= finalPages) ? ">>>" : ">|").formatted(Formatting.GRAY)));
                });
        Text center = new LiteralText("").append(new LiteralText(String.valueOf(page)).formatted(Formatting.GREEN).append(new LiteralText("/").formatted(Formatting.GRAY)).append(new LiteralText(String.valueOf(pages)).formatted(Formatting.GREEN)));
        if (page > 1) message.append(button_prev);
        message.append(center);
        if (page < pages) message.append(button_next);
        sendText(source, message);
    }

    public static void sendText(ServerCommandSource source, Text text) {
        source.sendFeedback(text, false);
    }

    @Nullable
    public static Map<String, String> createMap(String... strings) {
        Map<String, String> map = new HashMap<>();
        if (strings.length % 2 == 1) {
        } else {
            for (int i = 0; i < strings.length; i = i + 2) {
                map.put(strings[i], strings[i + 1]);
            }
            return map;
        }
        return null;
    }

    @Deprecated
    @Nullable
    public static Map<String, Text> createTextMap(Object... objects) {
        Map<String, Text> map = new HashMap<>();
        if (objects.length % 2 == 1) {
        } else {
            for (int i = 0; i < objects.length; i = i + 2) {
                if (objects[i] instanceof String && objects[i + 1] instanceof Text) {
                    map.put((String) objects[i], (Text) objects[i + 1]);
                }
            }
            return map;
        }
        return null;
    }

    private static String getConfigString(Map<String, String> var, Object... path) {
        String message = getConfigValue(path);
        if (message != null) {
            for (Map.Entry<String, String> entry : var.entrySet()) {
                message = message.replaceAll(entry.getKey(), entry.getValue());
            }
            return message;
        } else {
            StringBuilder field = new StringBuilder();
            for (Object object : path) {
                field.append(object.toString()).append(" ");
            }
            return "&cInvalid config field!";
        }
    }

    private static String translate(Map<String, String> var, String message) {
        for (Map.Entry<String, String> entry : var.entrySet()) {
            message = message.replaceAll(entry.getKey(), entry.getValue());
        }
        return message;
    }


    @Deprecated
    private static Text getConfigText(Map<String, Text> var, Object... path) {
        String message = ChatColor.translateAlternateColorCodes('&', getConfigValue(path));
        String split[] = message.split(" ");
        MutableText text = new LiteralText("");
        for (String s : split) {
            String parts[] = s.split("(?<=[\\§][\\w])");
            for (String string : parts) {
                if (string.matches("%[\\w]+%")) {
                    for (Map.Entry<String, Text> entry : var.entrySet()) {
                        if (entry.getKey().equals(string)) {
                            text.append(entry.getValue()).append(" ");
                        }
                    }
                } else {
                }
            }
            text.append(new LiteralText(ChatColor.translateAlternateColorCodes('&', s + " ")));
        }
        return text;
    }

    private static String getConfigString(Object... path) {
        String message = getConfigValue(path);
        if (message != null) {
            return message;
        } else {
            StringBuilder field = new StringBuilder();
            for (Object object : path) {
                field.append(object.toString()).append(" ");
            }
            return "&cInvalid config field!";
        }
    }

    public static void sendTranslatableMessage(ServerCommandSource source, Object... path) {
        sendMessage(source, getConfigString(path));
    }

    public static void sendTranslatableMessage(ServerCommandSource source, Map<String, String> var, Object... path) {
        sendMessage(source, getConfigString(var, path));
    }

    public static void sendTranslatableMessage(ServerCommandSource source, Map<String, String> var, String string) {
        sendMessage(source, translate(var, string));
    }

    public static void sendTranslatableMessage(ServerCommandSource source, String string) {
        sendMessage(source, string);
    }

    public static void sendTranslatableMessage(PlayerEntity player, Object... path) {
        sendMessage(player, getConfigString(path));
    }

    public static void sendTranslatableMessage(PlayerEntity player, String string) {
        sendMessage(player, string);
    }

    public static void sendTranslatableMessage(PlayerEntity player, Map<String, String> var, Object... path) {
        sendMessage(player, getConfigString(var, path));
    }

    public static void sendMessage(ServerCommandSource source, String message) {
        sendMessage(source, message, true);
    }

    public static void sendMessage(PlayerEntity player, String message) {
        sendMessage(player, message, true);
    }

    public static void sendMessage(ServerCommandSource source, String message, boolean prefix) {
        source.sendFeedback(getLiteralText(message, prefix), false);
    }

    public static void sendMessage(PlayerEntity player, String message, boolean prefix) {
        player.sendMessage(getLiteralText(message, prefix), false);
    }



    private static Text getLiteralText(String message, boolean usePrefix) {
        String prefix = usePrefix ? getConfigValue("prefix") : "";
        return new LiteralText(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public static String getConfigValue(Object... path) {
        return ItsMineConfig.getMainNode().getNode(path).getString();
    }

    public static void debug(String debug) {
        if (SharedConstants.isDevelopment) LOGGER.info(debug);
    }

    public static void log(String log) {
        LOGGER.info(log);
    }

}
