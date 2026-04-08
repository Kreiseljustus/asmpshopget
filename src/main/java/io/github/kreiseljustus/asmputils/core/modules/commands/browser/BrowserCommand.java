package io.github.kreiseljustus.asmputils.core.modules.commands.browser;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.kreiseljustus.asmputils.core.Utils;
import io.github.kreiseljustus.asmputils.core.data.ShopDataHolder;
import io.github.kreiseljustus.asmputils.core.modules.commands.ICommand;
import io.github.kreiseljustus.asmputils.core.modules.shop.ServerValidator;
import io.github.kreiseljustus.asmputils.core.modules.waypoints.WaypointModule;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.kreiseljustus.asmputils.Asmputils.tickDelay;
import static io.github.kreiseljustus.asmputils.core.modules.commands.browser.BrowserGUI.*;

public class BrowserCommand implements ICommand {

    private static List<ShopDataHolder> s_ServerShops = null;
    private static List<ShopDataHolder> lastFiltered = new ArrayList<>();

    private static String searchString = null;
    private static int pageIndex = 1;

    public static int refreshRemSeconds = 0;
    public static int refreshRemMinutes = 0;

    private ScheduledFuture<?> cooldownTask;

    @Override
    public String getCommandName() {
        return "browse";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder
                .executes(this::executeChecked)
                );
    }

    @Override
    public int execute(CommandContext<FabricClientCommandSource> context) {
        s_ServerShops = ServerValidator.s_ServerShops;
        pageIndex = 1;
        searchString = null;
        MinecraftClient client = MinecraftClient.getInstance();
        openBrowserScreen(client);
        Utils.debug("Done running /browse");
        return 0;
    }

    private BrowserScreen currentBrowserScreen = null;

    private void openBrowserScreen(MinecraftClient client) {
        SimpleInventory inventory = new SimpleInventory(54);

        inventory.setStack(45, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
        inventory.setStack(46, buildDimensionFilter());
        inventory.setStack(47, buildFilterItem());
        inventory.setStack(48, buildArrow(true));

        ItemStack search = new ItemStack(Items.SPYGLASS);
        search.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Search").styled(s -> s.withColor(Formatting.BOLD).withItalic(false)));
        if (searchString != null) {
            search.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal(searchString).styled(s -> s.withColor(Formatting.GRAY).withItalic(false))
            )));
        }
        inventory.setStack(49, search);

        inventory.setStack(50, buildArrow(false));
        inventory.setStack(51, buildSorting());
        inventory.setStack(52, buildRefresh());
        inventory.setStack(53, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));

        buildShopItems(inventory);

        tickDelay.schedule(() -> {
            client.execute(() -> {
                BrowserScreenHandler handler = new BrowserScreenHandler(0, client.player.getInventory(), inventory,
                        (slotIndex) -> {
                            if (slotIndex < 0 || slotIndex >= 54) return;
                            if (slotIndex == 45 || slotIndex == 53) return;

                            //I don't like switch (should use it though)

                            if (slotIndex == 46) {
                                currentDimensionFilter = (currentDimensionFilter + 1) % 3;
                                inventory.setStack(46, buildDimensionFilter());
                            } else if (slotIndex == 47) {
                                currentFilter = (currentFilter + 1) % 3;
                                inventory.setStack(47, buildFilterItem());
                            } else if (slotIndex == 48) {
                                pageIndex--;
                            } else if (slotIndex == 49) {
                                if (currentBrowserScreen != null) {
                                    if (currentBrowserScreen.getSearchText() != null && !currentBrowserScreen.getSearchText().isEmpty()) {
                                    }
                                    currentBrowserScreen.activateSearch(searchString, () -> {
                                        String result = currentBrowserScreen.getSearchText();
                                        searchString = (result == null || result.isEmpty()) ? null : result;
                                        currentBrowserScreen.deactivateSearch();
                                        // update the search item lore
                                        ItemStack searchItem = new ItemStack(Items.SPYGLASS);
                                        searchItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Search").styled(s -> s.withColor(Formatting.BOLD).withItalic(false)));
                                        if (searchString != null) {
                                            searchItem.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                                                    Text.literal(searchString).styled(s -> s.withColor(Formatting.GRAY).withItalic(false))
                                            )));
                                        }
                                        inventory.setStack(49, searchItem);
                                        buildShopItems(inventory);
                                    });
                                }
                                return;
                            } else if (slotIndex == 50) {
                                pageIndex++;
                            } else if (slotIndex == 51) {
                                currentSortingIndex = (currentSortingIndex + 1) % 4;
                                inventory.setStack(51, buildSorting());
                            } else if (slotIndex == 52) {
                                inventory.setStack(52, buildRefresh());
                                if (refreshRemMinutes == 0 && refreshRemSeconds == 0) {
                                    startCooldown(inventory);
                                    ServerValidator.forceRefresh = true;
                                }
                            } else {
                                int shopIndex = (pageIndex - 1) * 45 + slotIndex;
                                if (shopIndex < lastFiltered.size()) {
                                    ShopDataHolder shop = lastFiltered.get(shopIndex);
                                    WaypointModule.getWaypointServer().createWaypointIfAllowed(
                                            client,
                                            shop.item + " - " + shop.Owner,
                                            shop.position[0],
                                            shop.position[1],
                                            shop.position[2],
                                            Utils.dimensionFromInt(shop.dimension)
                                    );
                                }
                            }

                            buildShopItems(inventory);
                        });

                currentBrowserScreen = new BrowserScreen(handler, client.player.getInventory());
                client.player.currentScreenHandler = handler;
                client.setScreen(currentBrowserScreen);
                Utils.debug("Screen set to: " + client.currentScreen);
            });
        }, 50, TimeUnit.MILLISECONDS);
    }

    private void startCooldown(SimpleInventory inventory) {
        if (cooldownTask != null && !cooldownTask.isDone()) {
            cooldownTask.cancel(false);
        }

        refreshRemMinutes = 5;
        refreshRemSeconds = 0;
        inventory.setStack(52, buildRefresh());

        cooldownTask = tickDelay.scheduleAtFixedRate(() -> {
            if (refreshRemSeconds == 0) {
                if (refreshRemMinutes == 0) {
                    cooldownTask.cancel(false);
                    return;
                }
                refreshRemMinutes--;
                refreshRemSeconds = 59;
            } else {
                refreshRemSeconds--;
            }

            MinecraftClient.getInstance().execute(() -> {
                inventory.setStack(52, buildRefresh());
            });

        }, 1, 1, TimeUnit.SECONDS);
    }

    private void buildShopItems(SimpleInventory inventory) {
        if (s_ServerShops == null) { Utils.debug("Nothing to build!"); return; }

        List<ShopDataHolder> filtered = s_ServerShops.stream()
                .filter(shop -> shop.dimension == currentDimensionFilter)
                .filter(shop -> {
                    int mappedAction = switch (currentFilter) {
                        case 0 -> 1;
                        case 1 -> 0;
                        case 2 -> 2;
                        default -> -1;
                    };
                    return shop.action == mappedAction;
                })
                .filter(shop -> searchString == null || shop.item.toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        Comparator<ShopDataHolder> comparator = switch (currentSortingIndex) {
            case 0 -> Comparator.comparingDouble(s -> s.price);
            case 1 -> Comparator.comparingDouble((ShopDataHolder s) -> s.price).reversed();
            case 2 -> Comparator.comparingInt((ShopDataHolder s) -> s.amount).reversed();
            case 3 -> Comparator.comparing((ShopDataHolder s) -> s.updateTime,
                    Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator.comparingDouble(s -> s.price);
        };
        filtered.sort(comparator);

        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil(filtered.size() / (double) itemsPerPage);
        pageIndex = Math.max(1, Math.min(pageIndex, Math.max(1, totalPages)));

        int start = (pageIndex - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filtered.size());

        for (int i = 0; i < itemsPerPage; i++) {
            inventory.setStack(i, ItemStack.EMPTY);
        }

        for (int i = start; i < end; i++) {
            ShopDataHolder shop = filtered.get(i);
            int slot = i - start;

            ItemStack stack;
            try {
                var id = net.minecraft.util.Identifier.of(shop.item.toLowerCase().replace(" ", "_"));
                var item = net.minecraft.registry.Registries.ITEM.get(id);
                stack = new ItemStack(item == Items.AIR ? Items.PAPER : item);
            } catch (Exception e) {
                stack = new ItemStack(Items.PAPER);
            }

            List<net.minecraft.text.Text> lore = new ArrayList<>();
            lore.add(Text.literal("Owner: " + shop.Owner).styled(s -> s.withColor(Formatting.GRAY).withItalic(false)));
            lore.add(Text.literal("Price: " + shop.price).styled(s -> s.withColor(Formatting.GOLD).withItalic(false)));
            lore.add(Text.literal("Amount: " + shop.amount).styled(s -> s.withColor(Formatting.YELLOW).withItalic(false)));

            String actionText = switch (shop.action) {
                case 0 -> "Buying";
                case 1 -> "Selling";
                case 2 -> "Out of Stock";
                default -> "Unknown";
            };
            Formatting actionColor = switch (shop.action) {
                case 0 -> Formatting.GREEN;
                case 1 -> Formatting.AQUA;
                case 2 -> Formatting.RED;
                default -> Formatting.WHITE;
            };
            lore.add(Text.literal(actionText).styled(s -> s.withColor(actionColor).withItalic(false)));

            String dimText = switch (shop.dimension) {
                case 0 -> "Overworld";
                case 1 -> "Nether";
                case 2 -> "The End";
                default -> "Unknown";
            };
            lore.add(Text.literal("Dimension: " + dimText).styled(s -> s.withColor(Formatting.DARK_PURPLE).withItalic(false)));
            lore.add(Text.literal("Position: " + shop.position[0] + ", " + shop.position[1] + ", " + shop.position[2]).styled(s -> s.withColor(Formatting.GRAY).withItalic(false)));

            String formattedTime = shop.updateTime != null
                    ? shop.updateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                    : "Unknown";

            lore.add(Text.literal("Last update: " + formattedTime).styled(s -> s.withColor(Formatting.GRAY).withItalic(false)));
            lore.add(Text.literal("Click to add Waypoint!").styled(s -> s.withColor(Formatting.DARK_GREEN).withItalic(true).withBold(true)));

            stack.set(DataComponentTypes.CUSTOM_NAME,
                    Text.literal(shop.item).styled(s -> s.withColor(Formatting.WHITE).withItalic(false)));
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

            inventory.setStack(slot, stack);
        }

        inventory.setStack(48, pageIndex <= 1 ? buildDisabledArrow(true) : buildArrow(true));
        inventory.setStack(50, pageIndex >= totalPages ? buildDisabledArrow(false) : buildArrow(false));

        lastFiltered = filtered;
    }

    private ItemStack buildDisabledArrow(boolean left) {
        ItemStack pane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        pane.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(left ? "◀ No previous page" : "No next page ▶")
                        .styled(s -> s.withColor(Formatting.GRAY).withItalic(false)));
        return pane;
    }
}