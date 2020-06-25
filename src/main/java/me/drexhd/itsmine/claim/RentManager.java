package me.drexhd.itsmine.claim;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class RentManager {

    /*Tenant of the rent*/
    private UUID tenant = null;
    /*Indicates whether a claim be rented or not*/
    private boolean rentable = false;
    /*Minimum rent time*/
    private int min = 0;
    /*Maximum rent time*/
    private int max = 0;
    /*Revenue made from all rents (gets cleared when collected)*/
    private ArrayList<ItemStack> revenue = new ArrayList<>();
    /*Rent price (item and count)*/
    private ItemStack currency = ItemStack.EMPTY;
    /*Unix timestamp in ms*/
    private int until = 0;

    public ArrayList<ItemStack> getRevenue() {
        return revenue;
    }

    public void addRevenue(ItemStack revenue) {
        this.revenue.add(revenue);
    }

    public void clearRevenue() {
        revenue.clear();
    }

    //Max rent time
    public int getMax() {
        return max;
    }

    public int getUntil() {
        return until;
    }

    public int getTimeLeft() {
        return Math.max(0, until - getUnixTime());
    }


    public int getMin() {
        return min;
    }

    //Returns seconds passed (Unix time)
    public int getUnixTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public UUID getTenant() {
        return tenant;
    }

    public boolean isRentable() {
        return rentable;
    }

    public int getAmount() {
        return currency.getCount();
    }

    public void setAmount(int amount) {
        currency.setCount(amount);
    }

    public ItemStack getCurrency() {
        return currency;
    }

    public boolean isReady() {
        return this.currency != ItemStack.EMPTY && this.min != 0 && this.max != 0;
    }

    /**
     * Method for checking whether or not a player has enough items in hand (returns null if not)
     *
     * @param itemStack ItemStack that should get removed
     * @param time      The time which a claim is rented / extended for
     * @return itemstack with reduced amount
     */
    @Nullable
    public ItemStack removeItemStack(ItemStack itemStack, int time) {
        int amount = itemStack.getCount();
        int rentPeriods = time / this.min;
        amount = amount - this.currency.getCount() * rentPeriods;
        if (amount > 0) {
            itemStack.setCount(amount);
            return itemStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public boolean hasEnough(ItemStack itemStack, int time) {
        int amount = itemStack.getCount();
        int rentPeriods = time / this.min;
        amount = amount - this.currency.getCount() * rentPeriods;
        return amount >= 0;
    }

    public boolean isRented() {
        return this.tenant != null && this.until != 0;
    }

    public boolean isTimeValid(int time) {
        return time >= this.min && time % this.min == 0;
    }

    public boolean isTimeValid(int min, int max) {
        return max % min == 0 && max > min;
    }


    public void end() {
        this.tenant = null;
        this.until = 0;
    }

    public void shouldEnd() {
        if (isRented() && getTimeLeft() <= 0) {
            end();
        }
    }

    /**
     * @param uuid UUID of the tenant
     * @param time How long the claim will be rented for
     * @return How many items it will cost
     */
    public int rent(UUID uuid, int time) {
        /*Revenue*/
        ItemStack revenue = currency.copy();
        int amount = revenue.getCount();
        int rentPeriods = time / this.min;
        amount = amount * rentPeriods;
        revenue.setCount(amount);
        this.addRevenue(revenue);

        /*Tenant*/
        this.tenant = uuid;

        /*Time*/
        this.until = this.getUnixTime() + time;
        return amount;
    }

    /*Claim extending*/
    public int extend(int time) {
        this.until = this.until + time;

        /*Revenue*/
        ItemStack revenue = currency.copy();
        int amount = revenue.getCount();
        int rentPeriods = time / this.min;
        amount = amount * rentPeriods;
        revenue.setCount(amount);
        this.addRevenue(revenue);
        return amount;
    }

    public boolean wouldExceed(int time) {
        return (this.getTimeLeft() + time) > this.max;
    }

    public boolean canExtend(UUID uuid) {
        return this.tenant.equals(uuid);
    }


    public void makeRentable(ItemStack currency, int rentTime, int maxrentTime) {
        this.rentable = true;
        this.currency = currency;
        this.min = rentTime;
        this.max = maxrentTime;
    }

    /**
     * @return the new state (true = rentable)
     */
    public boolean toggle() {
        this.rentable = !this.rentable;
        return this.rentable;
    }

    public CompoundTag toTag() {

        CompoundTag rent = new CompoundTag();

        /*General data about the rent*/
        CompoundTag general = new CompoundTag();
        if (this.tenant != null) general.putUuid("tenant", this.tenant);
        if (this.until != 0) general.putInt("until", this.until);
        general.putBoolean("rentable", this.rentable);
        if (this.min != 0) general.putInt("min", this.min);
        if (this.max != 0) general.putInt("max", this.max);
        CompoundTag currency = new CompoundTag();
        if (this.currency != ItemStack.EMPTY) this.currency.toTag(currency);
        general.put("currency", currency);
        if (!general.isEmpty()) rent.put("general", general);

        /*Claim revenue*/
        if (!this.revenue.isEmpty()) {
            CompoundTag revenue = new CompoundTag();
            int i = 0;
            for (ItemStack itemStack : this.revenue) {
                CompoundTag revenueEntry = new CompoundTag();
                itemStack.toTag(revenueEntry);
                i++;
                revenue.put(String.valueOf(i), revenueEntry);
            }
            rent.put("revenue", revenue);
        }
        rent.put("general", general);
        return rent;
    }

    public void fromTag(CompoundTag tag) {
        CompoundTag general = tag.getCompound("general");
        if (general.containsUuid("tenant")) this.tenant = general.getUuid("tenant");
        if (general.contains("until")) this.until = general.getInt("until");
        this.rentable = general.getBoolean("rentable");
        if (general.contains("min")) this.min = general.getInt("min");
        if (general.contains("max")) this.max = general.getInt("max");
        if (general.contains("currency")) {
            CompoundTag currency = general.getCompound("currency");
            this.currency = ItemStack.fromTag(currency);
        }
        if (tag.contains("revenue")) {
            CompoundTag revenue = tag.getCompound("revenue");
            for (int i = 1; i <= revenue.getSize(); i++) {
                CompoundTag revenueTag = revenue.getCompound(String.valueOf(i));
                this.revenue.add(ItemStack.fromTag(revenueTag));
            }
        }
    }
}
