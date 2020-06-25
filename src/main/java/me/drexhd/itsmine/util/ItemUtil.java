package me.drexhd.itsmine.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

public class ItemUtil {

    public static String toName(ItemStack itemStack) {
        return toName(itemStack.getItem());
    }

    public static String toName(Item item) {
        return Registry.ITEM.getId(item).getPath();
    }



    public static String toName(ItemStack itemStack, int amount) {
        return toName(itemStack.getItem(), amount);
    }

    public static String toName(Item item, int amount) {
        /*Get item id*/
        String id = Registry.ITEM.getId(item).getPath();
        String words[] = id.split("_");
        StringBuilder stringBuilder = new StringBuilder();
        for(String word : words) {
            /*Capitalize first letter*/
            word = word.substring(0, 1).toUpperCase() + word.substring(1);
            stringBuilder.append(word + " ");
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String string = stringBuilder.toString();
        /*handle grammar*/
        return amount > 1 ? string + "s" : string;
    }

}
