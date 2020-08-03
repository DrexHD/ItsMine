package me.drexhd.itsmine.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.claim.permission.Permission;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;

public abstract class Command {

    public static final SuggestionProvider<ServerCommandSource> TIME_PROVIDER = (source, builder) -> {
        List<String> strings = new ArrayList<>();
        if (builder.getRemaining().isEmpty()) {
            for (int i = 1; i < 10; i++) {
                strings.add(String.valueOf(i));
            }
        }
        if (builder.getRemaining().matches("[\\d]+")) {
            strings.add(builder.getRemaining() + "s");
            strings.add(builder.getRemaining() + "m");
            strings.add(builder.getRemaining() + "h");
            strings.add(builder.getRemaining() + "d");
            strings.add(builder.getRemaining() + "w");
        }
        return CommandSource.suggestMatching(strings, builder);
    };
    protected final String literal;
    public SuggestionProvider<ServerCommandSource> USER_PROVIDER = (source, builder) -> {
        List<String> strings = new ArrayList<>();
        for (ServerPlayerEntity player : server().getPlayerManager().getPlayerList()) {
            strings.add(player.getEntityName());
        }
        return CommandSource.suggestMatching(strings, builder);
    };
    protected boolean others = false;
    protected boolean admin = false;
    public SuggestionProvider<ServerCommandSource> OWNER_PROVIDER = (source, builder) -> {
        List<String> strings = new ArrayList<>();
        if (admin) strings.add("-server");
        for (ServerPlayerEntity player : server().getPlayerManager().getPlayerList()) {
            strings.add(player.getEntityName());
        }
        return CommandSource.suggestMatching(strings, builder);
    };
    protected boolean subzones = false;
    protected boolean suggestCurrent = true;
    public final SuggestionProvider<ServerCommandSource> CLAIM_PROVIDER = (source, builder) -> {
        UUID uuid = getOwner(source);
        ServerPlayerEntity player = source.getSource().getPlayer();
        List<String> names = new ArrayList<>();
        Claim current = ClaimManager.INSTANCE.getClaimAt(player.getBlockPos(), player.world.getDimension());
        if (current != null && suggestCurrent) names.add(current.getName());
        if (uuid != null) {
            for (Claim claim : ClaimManager.INSTANCE.getPlayerClaims(uuid)) {
                names.add(claim.getName());
            }
        }
        return CommandSource.suggestMatching(names, builder);
    };
    protected LiteralArgumentBuilder<ServerCommandSource> literalArgument;

    public Command(final String literal) {
        this.literal = literal;
        this.literalArgument = LiteralArgumentBuilder.literal(this.literal);
    }

    public static void validateClaim(Claim claim) throws CommandSyntaxException {
        if (claim == null) throw new SimpleCommandExceptionType(new LiteralText("Invalid claim!")).create();
    }

    public abstract Command copy();

    protected abstract void register(LiteralArgumentBuilder<ServerCommandSource> command);

    public void admin(boolean admin) {
        this.admin = admin;
    }

    public void others(boolean others) {
        this.others = others;
    }

    public void subzones(boolean subzones) {
        this.subzones = subzones;
    }

    public void suggestCurrent(boolean suggestCurrent) {
        this.suggestCurrent = suggestCurrent;
    }

    public CommandDispatcher<ServerCommandSource> dispatcher() {
        return CommandManager.dispatcher;
    }

    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendError(new LiteralText("Incomplete command!"));
        return 0;
    }

    public LiteralArgumentBuilder<ServerCommandSource> literal() {
        return this.literalArgument;
    }

    public LiteralArgumentBuilder<ServerCommandSource> literal(String string) {
        return LiteralArgumentBuilder.literal(string);
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> thenClaim(ArgumentBuilder command) {
        RequiredArgumentBuilder<ServerCommandSource, String> claims = claimArgument();
        claims.executes(this::execute);
        claims.then(command);
        if (others) {
            RequiredArgumentBuilder<ServerCommandSource, String> owner = ownerArgument();
            owner.then(claims);
            return owner;
        }
        return claims;
    }

    public void validatePermission(Claim claim, UUID uuid, String parent, @Nullable String child) throws CommandSyntaxException {

        if (!(claim.hasPermission(uuid, parent, child) || admin)) {
            throw new SimpleCommandExceptionType(new LiteralText("You don't have permission to do this!")).create();
        }
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> thenClaim() {
        RequiredArgumentBuilder<ServerCommandSource, String> claims = claimArgument();
        claims.executes(this::execute);
        if (others) {
            RequiredArgumentBuilder<ServerCommandSource, String> owner = ownerArgument();
            owner.then(claims);
            return owner;
        }
        return claims;
    }

    public Claim getClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            String name = StringArgumentType.getString(context, "claim");
            Claim claim = ClaimManager.INSTANCE.getClaim(getOwner(context), name);
            if (claim != null) return claim;
        } catch (IllegalArgumentException e) {
            if (suggestCurrent) return getClaimAt(context);
        }
        throw new SimpleCommandExceptionType(new LiteralText("You don't own a claim with that name, use /claim other if you want to modify a claim belonging to someone else!")).create();
    }

    public <V> boolean isArgumentSet(CommandContext<ServerCommandSource> context, String name, final Class<V> clazz) {
        try {
            context.getArgument(name, clazz);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }



    public Claim getClaimAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = ClaimManager.INSTANCE.getClaimAt(player.getBlockPos(), player.getServerWorld().getDimension());
        if (claim != null) return claim;
        throw new SimpleCommandExceptionType(new LiteralText("Couldn't find a claim at your position")).create();
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> claimArgument() {
        return argument("claim", word()).suggests(CLAIM_PROVIDER);
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> ownerArgument() {
        return argument("owner", word()).suggests(OWNER_PROVIDER);
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> timeArgument(String name) {
        return argument(name, word()).suggests(TIME_PROVIDER);
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> userArgument() {
        return argument("user", word()).suggests(USER_PROVIDER);
    }

    public String getName(UUID uuid) {
        GameProfile gameProfile = server().getUserCache().getByUuid(uuid);
        if (gameProfile != null && gameProfile.isComplete()) {
            return gameProfile.getName();
        } else if (uuid.equals(ClaimManager.serverUUID)) {
            return "Server";
        } else {
            return uuid.toString();
        }
    }

    @Nullable
    public UUID getOwner(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String user;
        try {
            user = StringArgumentType.getString(context, "owner");
        } catch (IllegalArgumentException e) {
            return context.getSource().getPlayer().getUuid();
        }
        if (user.equals("-server")) return ClaimManager.serverUUID;
        GameProfile gameProfile = context.getSource().getMinecraftServer().getUserCache().findByName(user);
        if (gameProfile != null && gameProfile.isComplete()) return gameProfile.getId();
        throw new SimpleCommandExceptionType(new LiteralText("Couldn't find the specified player!")).create();
    }

    @Nullable
    public UUID getUser(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String user;
        try {
            user = StringArgumentType.getString(context, "user");
        } catch (IllegalArgumentException e) {
            return context.getSource().getPlayer().getUuid();
        }
        GameProfile gameProfile = context.getSource().getMinecraftServer().getUserCache().findByName(user);
        if (gameProfile != null && gameProfile.isComplete()) return gameProfile.getId();
        throw new SimpleCommandExceptionType(new LiteralText("Couldn't find the specified player!")).create();
    }

    public Optional<ServerPlayerEntity> getOnlineUser(UUID uuid) {
        for (ServerPlayerEntity player : server().getPlayerManager().getPlayerList()) {
            if (player.getUuid().equals(uuid)) return Optional.of(player);
        }
        return Optional.empty();
    }

    public int showTrusted(ServerCommandSource source, Claim claim) {
        MutableText text = new LiteralText("\n");
        text.append(new LiteralText("Trusted players for Claim ").formatted(Formatting.YELLOW))
                .append(new LiteralText(claim.name).formatted(Formatting.GOLD)).append(new LiteralText("\n"));

        AtomicInteger atomicInteger = new AtomicInteger();
        claim.permissionManager.playerPermissions.forEach((uuid, perm) -> {
            atomicInteger.incrementAndGet();
            MutableText pText = new LiteralText("");
            MutableText owner;
            GameProfile profile = source.getMinecraftServer().getUserCache().getByUuid(uuid);
            if (profile != null) {
                owner = new LiteralText(profile.getName());
            } else {
                owner = new LiteralText(uuid.toString())
                        .styled((style) -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to Copy")))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString())));
            }

            pText.append(new LiteralText(atomicInteger.get() + ". ").formatted(Formatting.GOLD))
                    .append(owner.formatted(Formatting.YELLOW));

            MutableText hover = new LiteralText("");
            hover.append(new LiteralText("Permissions:").formatted(Formatting.WHITE)).append(new LiteralText("\n"));

            int allowed = 0;
            int i = 0;
            boolean nextColor = false;
            MutableText perms = new LiteralText("");

            for (Permission permission : Permission.values()) {
                if (claim.permissionManager.playerPermissions.get(uuid).hasPermission(permission.id)) {
                    perms.append(new LiteralText(permission.id + " ").formatted(Formatting.GREEN));
                    allowed++;
                } else {
                    perms.append(new LiteralText(permission.id + " ").formatted(Formatting.RED));
                }

                if (i % 3 == 0) perms.append(new LiteralText("\n"));
                i++;
                nextColor = !nextColor;
            }

            if (allowed == Permission.values().length) {
                hover.append(new LiteralText("All " + allowed + " Permissions").formatted(Formatting.YELLOW).formatted(Formatting.ITALIC));
            } else {
                hover.append(perms);
            }

            pText.append(new LiteralText(" ")
                    .append(new LiteralText("(").formatted(Formatting.GOLD))
                    .append(new LiteralText(String.valueOf(allowed)).formatted(Formatting.GREEN))
                    .append(new LiteralText("/").formatted(Formatting.GOLD))
                    .append(new LiteralText(String.valueOf(Permission.values().length)).formatted(Formatting.YELLOW))
                    .append(new LiteralText(")").formatted(Formatting.GOLD))
            );

            pText.styled((style) -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
            text.append(pText).append(new LiteralText("\n"));
        });

        source.sendFeedback(text, false);
        return 1;
    }

    public int parseTime(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, name);
        if (input.matches("[\\d]+(s|m|h|d|w)")) {
            String numbers = input.substring(0, input.length() - 1);
            char c = input.substring(input.length() - 1).charAt(0);
            int time = Integer.parseInt(numbers);
            switch (c) {
                case 's':
                    break;
                case 'm':
                    time = time * 60;
                    break;
                case 'h':
                    time = time * 60 * 60;
                    break;
                case 'd':
                    time = time * 60 * 60 * 24;
                    break;
                case 'w':
                    time = time * 60 * 60 * 24 * 4;
                    break;
            }
            return time;
        } else {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid time format!")).create();
        }
    }


    public MinecraftServer server() {
        return ClaimManager.server;
    }


}
