package me.drexhd.itsmine;

public interface ClaimPlayerEntity {
    void tickMessageCooldown();
    int getMessageCooldown();
    boolean shouldMessage();
    void setMessageCooldown();
    void setMessageCooldown(int cooldown);
}
