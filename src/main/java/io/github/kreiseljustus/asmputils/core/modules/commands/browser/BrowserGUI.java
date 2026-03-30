package io.github.kreiseljustus.asmputils.core.modules.commands.browser;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.kreiseljustus.asmputils.core.modules.commands.browser.BrowserCommand.refreshRemMinutes;
import static io.github.kreiseljustus.asmputils.core.modules.commands.browser.BrowserCommand.refreshRemSeconds;

public class BrowserGUI {

    public static int currentFilter = 0;
    private static final String[] FILTERS = {"Selling", "Buying", "Out of Stock"};

    public static int currentDimensionFilter = 0;
    private static final String[] DIMENSION_FILTERS = {"Overworld", "Nether", "The End"};

    public static int currentSortingIndex = 0;
    private static final String[] SORTING_TEXT = {"Sort by lowest Price", "Sort by highest Price", "Sort by Amount", "Sort by latest update time"};

    public static ItemStack buildArrow(boolean left) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        String texture = left
                ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGUxZGZjMTFhODM3MTExZDIyYjAwMWExNDQ2MWY5YTdmYzA5MzUyMmY4OGM1OGZhZWZkNmFkZWZmY2Q0ZTlhYiJ9fX0="
                : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2M2OWQ0MTA3NmE4ZGVhNGYwNmQzZjFhOWFjNDdjYzk5Njk4OGI3NGEwOTEzYWIyYWMxYTc0Y2FmNzA4MTkxOCJ9fX0=";

        PropertyMap properties = new PropertyMap();
        properties.put("textures", new Property("textures", texture));

        ProfileComponent profile = new ProfileComponent(
                Optional.of(left ? "left_arrow" : "right_arrow"),
                Optional.empty(),
                properties
        );

        head.set(DataComponentTypes.PROFILE, profile);
        head.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(left ? "◀ Previous Page" : "Next Page ▶")
                        .styled(s -> s.withColor(Formatting.YELLOW).withItalic(false)));

        return head;
    }
    public static ItemStack buildFilterItem() {
        ItemStack filter = new ItemStack(Items.HOPPER);

        List<Text> lore = new ArrayList<>();
        for (int i = 0; i < FILTERS.length; i++) {
            if (i == currentFilter) {
                lore.add(Text.literal("» " + FILTERS[i]).styled(s -> s.withColor(Formatting.AQUA).withBold(true)));
            } else {
                lore.add(Text.literal("   " + FILTERS[i]).styled(s -> s.withColor(Formatting.GRAY).withBold(false)));
            }
        }

        filter.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Filter").styled(s -> s.withColor(Formatting.AQUA).withItalic(false)));
        filter.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return filter;
    }

    public static ItemStack buildDimensionFilter() {

        ItemStack dimensionFilter = switch (currentDimensionFilter) {
            case 0 -> new ItemStack(Items.GRASS_BLOCK);
            case 1 -> new ItemStack(Items.NETHERRACK);
            case 2 -> new ItemStack(Items.ENDER_EYE);
            default -> null;
        };

        List<Text> lore = new ArrayList<>();
        for(int i = 0; i < DIMENSION_FILTERS.length; i++) {
            if (i == currentDimensionFilter) {
                lore.add(Text.literal("» " + DIMENSION_FILTERS[i]).styled(s -> s.withColor(Formatting.AQUA).withBold(true)));
            } else {
                lore.add(Text.literal("   " + DIMENSION_FILTERS[i]).styled(s -> s.withColor(Formatting.GRAY).withBold(false)));
            }
        }

        dimensionFilter.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Dimension Filter").styled(s->s.withColor(Formatting.DARK_PURPLE).withItalic(false)));
        dimensionFilter.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return dimensionFilter;
    }

    public static ItemStack buildSorting() {
        ItemStack sorting = switch(currentSortingIndex) {
            case 0 -> new ItemStack(Items.GOLD_NUGGET);
            case 1 -> new ItemStack(Items.GOLD_BLOCK);
            case 2 -> new ItemStack(Items.CLOCK);
            case 3 -> new ItemStack(Items.CHEST);
            default -> null;
        };

        List<Text> lore = new ArrayList<>();
        for(int i = 0; i < SORTING_TEXT.length; i++) {
            if(i == currentSortingIndex) {
                lore.add(Text.literal("» " + SORTING_TEXT[i]).styled(s -> s.withColor(Formatting.AQUA).withBold(true)));
            } else {
                lore.add(Text.literal("   " + SORTING_TEXT[i]).styled(s -> s.withColor(Formatting.GRAY).withBold(false)));
            }
        }

        sorting.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sorting").styled(s->s.withColor(Formatting.YELLOW).withItalic(false)));
        sorting.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return sorting;
    }

    public static ItemStack buildRefresh() {
        ItemStack refresh = new ItemStack(Items.NETHER_STAR);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Refreshes the shop list!").styled(s->s.withColor(Formatting.LIGHT_PURPLE)));
        lore.add(Text.literal("This might take a while and has a cooldown.").styled(s->s.withColor(Formatting.RED)));

        if(refreshRemMinutes == 0 && refreshRemSeconds == 0) {
            lore.add(Text.literal("Ready").styled(s->s.withColor(Formatting.GREEN).withBold(true)));
        } else if(refreshRemMinutes > 0) {
            lore.add(Text.literal("Ready in " + refreshRemMinutes + " minutes and " + refreshRemSeconds + " seconds!").styled(s->s.withColor(Formatting.RED)));
        } else {
            lore.add(Text.literal("Ready in " + refreshRemSeconds + " seconds").styled(s->s.withColor(Formatting.RED)));
        }

        refresh.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Refresh").styled(s->s.withColor(Formatting.GOLD).withItalic(false)));
        refresh.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return refresh;
    }
}
