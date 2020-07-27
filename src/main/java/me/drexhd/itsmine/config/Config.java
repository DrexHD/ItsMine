package me.drexhd.itsmine.config;

import me.drexhd.itsmine.config.sections.DefaultClaimBlockSection;
import me.drexhd.itsmine.config.sections.MessageSection;
import me.drexhd.itsmine.config.sections.RentSection;
import me.drexhd.itsmine.config.sections.SpawnSection;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;


@ConfigSerializable
public class Config {

    public static final String HEADER = "ItsMine! Main Configuration File";

    @Setting(value = "debug")
    public boolean debug = false;

    @Setting(value = "prefix")
    public String prefix = "&f[&aClaims&f] ";

    @Setting(value = "permissionManager", comment = "Values: luckperms, vanilla")
    public String permissionManager = "luckperms";

    @Setting(value = "claims2d", comment = "If this is enabled, claims reach from 0 to 256 (claim blocks ignore the y-axis)")
    public boolean claims2d = true;

    @Setting(value = "defaultClaimBlocks", comment = "Adjust the amount of claimblocks players get upon joining")
    public DefaultClaimBlockSection defaultClaimBlockSection = new DefaultClaimBlockSection();

    @Setting(value = "messages")
    private MessageSection messageSection = new MessageSection();

    @Setting(value = "rent")
    private RentSection rentSection = new RentSection();

    @Setting(value = "banLocation", comment = "The location to where people get teleported, if they are banned in a claim")
    private SpawnSection spawnSection = new SpawnSection();


    public MessageSection message(){
        return messageSection;
    }

    public SpawnSection spawnSection(){
        return spawnSection;
    }


    public DefaultClaimBlockSection claimBlock(){
        return defaultClaimBlockSection;
    }

    public RentSection rent(){
        return rentSection;
    }

}
