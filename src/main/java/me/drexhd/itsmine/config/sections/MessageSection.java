package me.drexhd.itsmine.config.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MessageSection {

    @Setting(value = "messageCooldown")
    public int messageCooldown = 20;

    @Setting(value = "eventStayTicks", comment = "Sets how many ticks an event message will stay on action bar, Default: -1")
    public int eventStayTicks = -1;

    @Setting(value = "noPermission")
    public String noPermission = "&cSorry but you don't have permission to do this!";

    @Setting(value = "interactEntity")
    public String interactEntity = "&cSorry but you can't interact with this entity!";

    @Setting(value = "interactBlock")
    public String interactBlock = "&cSorry but you can't interact with this block!";

    @Setting(value = "useItem")
    public String useItem = "&cSorry but you can't use this item!";

    @Setting(value = "breakBlock")
    public String breakBlock = "&cSorry but you can't break this block!";

    @Setting(value = "placeBlock")
    public String placeBlock = "&cSorry but you can't place this block!";

    @Setting(value = "attackEntity")
    public String attackEntity = "&cSorry but you can't attack this entity!";

    @Setting(value = "enterDefault", comment = "Variables: %claim% %player%")
    public String enterDefault = "&eNow entering &6%claim%";

    @Setting(value = "leaveDefault", comment = "Variables: %claim% %player%")
    public String leaveDefault = "&eNow leaving &6%claim%";

    @Setting(value = "cantEnter")
    public String cantEnter = "&cSorry but you don't have permission to enter this claim!";

    @Setting(value = "cantUse")
    public String cantUse = "&cSorry but you can't to use that here!";

    @Setting(value = "longName")
    public String longName = "&cThe name of the claim must be less than 30 characters!";

    @Setting(value = "cantDo")
    public String cantDo ="&cSorry but you can't do that!";

    @Setting(value = "adminBypass")
    public String adminBypass = "&4You are modifying a claim using admin privileges";

    @Setting(value = "makeRentable")
    public String makeRentable = "&eClaim &6%claim% is now available for %amount% %item% per %time%";

    @Setting(value = "toggleRent")
    public String toggleRent = "&eRenting for &6%claim% &ehas been %value%";

    @Setting(value = "rentNoValues")
    public String rentNoValues = "&eUse &6/claim rentable <claim> <item> <count> <rent> <max> &eto make a claim rentable\neg: &6/claim rentable House diamond 5 7d 28d";

    @Setting(value = "alreadyRented")
    public String alreadyRented = "&cThis claim is already rented!";

    @Setting(value = "notForRent")
    public String notForRent = "&c%claim% is not for rent!";

    @Setting(value = "extendRent")
    public String extendRent = "&eRent for &6%claim% has been extended by &6%time% &efor &6%amount% %item%";

    @Setting(value = "rent")
    public String rent = "&6%claim% &ehas been rented for &6%time% &efor &6%amount% %item%";

    @Setting(value = "notEnough")
    public String notEnough = "&cYou don't have enough %item% in your main hand";

    @Setting(value = "invalidRentTime")
    public String invalidRentTime = "&cInvalid rent time (Maximum rent time has to be bigger than minimum)";

    @Setting(value = "invalidRentExtendTime1")
    public String invalidRentExtendTime1 = "&cInvalid rent time &c&o(Exceeds maximum rent time)&c!";

    @Setting(value = "invalidRentExtendTime2")
    public String invalidRentExtendTime2 = "&cInvalid rent time &c&o(Has to be a multiple of minimum rent time)&c!";

    @Setting(value = "invalidClaim")
    public String invalidClaim = "&cInvalid claim!";

    @Setting(value = "invalidPermission")
    public String invalidPermission = "&cInvalid permission!";

    @Setting(value = "invalidFlag")
    public String invalidFlag = "&cInvalid flag!";

    @Setting(value = "defaultPermissionSet", comment = "Variables: %permission% %value% %claim%")
    public String defaultPermissionSet = "&eSet permission &6%permission% &eto %value% &ein &6%claim%";

    @Setting(value = "permissionSet", comment = "Variables: %permission% %value% %claim%")
    public String permissionSet = "&eSet permission &6%permission% &eto %value% &efor &6%player% &ein &6%claim%";

    @Setting(value = "permissionQuery", comment = "Variables: %permission% %value% %claim% %player%")
    public String permissionQuery = "&ePermission &6%permission% &eis set to %value% &efor &6%player% &ein &6%claim%";

    @Setting(value = "permissionReset", comment = "Variables: %permission% %claim% %player%")
    public String permissionReset = "&ePermission &6%permission% &efor &6%player% &ein &6%claim% &ehas been &6reset";

    @Setting(value = "flagSet", comment = "Variables: %flag% %value% %claim%")
    public String flagSet = "&eSet Flag &6%flag% &ein &6%claim% &eto %value%";

    @Setting(value = "flagQuery", comment = "Variables: %flag% %value% %claim%")
    public String flagQuery = "&eFlag &6%flag% &eis set to %value% &ein &6%claim%";

    @Setting(value = "flagReset", comment = "Variables: %flag% %claim%")
    public String flagReset = "&eFlag &6%flag% &ein &6%claim% &ehas been &6reset";

    @Setting(value = "vtrue")
    private String vtrue = "&atrue";

    @Setting(value = "vfalse")
    private String vfalse = "&cfalse";

    public String getTrue() { return vtrue; }

    public String getFalse() { return vfalse; }


}
