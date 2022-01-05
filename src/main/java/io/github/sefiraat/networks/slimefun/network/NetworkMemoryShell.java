package io.github.sefiraat.networks.slimefun.network;

import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.sefiraat.networks.slimefun.tools.CardInstance;
import io.github.sefiraat.networks.slimefun.tools.NetworkCard;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.Theme;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentAmountInstanceType;
import io.github.sefiraat.networks.utils.datatypes.PersistentCardInstanceType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class NetworkMemoryShell extends NetworkObject {

    public static final int INPUT_SLOT = 1;
    public static final int CARD_SLOT = 4;
    public static final int OUTPUT_SLOT = 7;

    private static final ItemStack BACK_INPUT = new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, Theme.PASSIVE + "Input");
    private static final ItemStack BACK_CARD = new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, Theme.PASSIVE + "Memory Card");
    private static final ItemStack BACK_OUTPUT = new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, Theme.PASSIVE + "Output");

    private static final int[] INPUT_SLOTS = new int[]{0, 2};
    private static final int[] CARD_SLOTS = new int[]{3, 5};
    private static final int[] OUTPUT_SLOTS = new int[]{6, 8};

    public NetworkMemoryShell(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.SHELL);
        this.getSlotsToDrop().add(CARD_SLOT);
        this.getSlotsToDrop().add(OUTPUT_SLOT);
        addItemHandler(new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return false;
            }

            @Override
            public void tick(Block b, SlimefunItem item, Config data) {
                addToRegistry(b);

                final BlockMenu blockMenu = BlockStorage.getInventory(b);

                if (blockMenu == null) {
                    return;
                }

                final ItemStack card = blockMenu.getItemInSlot(CARD_SLOT);

                // No card, quick exit
                if (card == null || card.getType() == Material.AIR) {
                    return;
                }

                final ItemStack input = blockMenu.getItemInSlot(INPUT_SLOT);
                if (input != null && input.getType() != Material.AIR) {
                    tryInputItem(card, input);
                }

                final ItemStack output = blockMenu.getItemInSlot(OUTPUT_SLOT);

                // No item in output, try to fill
                if (output == null || output.getType() == Material.AIR) {
                    fillEmptySlot(blockMenu, card);
                    return;
                }

                // Item in output exists but has no room left - escape
                if (output.getAmount() >= output.getMaxStackSize()) {
                    return;
                }

                fillFilledSlot(blockMenu, card, output);

            }
        });
    }

    @ParametersAreNonnullByDefault
    private void tryInputItem(ItemStack card, ItemStack input) {
        final SlimefunItem cardItem = SlimefunItem.getByItem(card);
        if (cardItem instanceof NetworkCard) {
            final CardInstance cardInstance = getCardInstance(card);
            if (cardInstance == null || !SlimefunUtils.isItemSimilar(cardInstance.getItemStack(), input, true, false)) {
                return;
            }
            cardInstance.increaseAmount(input.getAmount());
            input.setAmount(0);
            setCardInstance(card, cardInstance);
        }
    }

    @ParametersAreNonnullByDefault
    private void fillEmptySlot(BlockMenu blockMenu, ItemStack card) {
        final SlimefunItem cardItem = SlimefunItem.getByItem(card);
        if (cardItem instanceof NetworkCard) {
            final CardInstance amountInstance = getAmountInstance(card);

            if (amountInstance == null || amountInstance.getAmount() <= 0) {
                return;
            }

            final CardInstance cardInstance = getCardInstance(card);
            final ItemStack itemStack = cardInstance.withdrawStack();

            if (itemStack == null) {
                return;
            }

            blockMenu.pushItem(itemStack, OUTPUT_SLOT);
            setCardInstance(card, cardInstance);
        }
    }

    @ParametersAreNonnullByDefault
    private void fillFilledSlot(BlockMenu blockMenu, ItemStack card, ItemStack output) {
        final SlimefunItem cardItem = SlimefunItem.getByItem(card);
        if (cardItem instanceof NetworkCard) {
            final CardInstance cardInstance = getCardInstance(card);
            if (cardInstance == null || cardInstance.getAmount() <= 0) {
                return;
            }
            final int requestAmount = output.getMaxStackSize() - output.getAmount();
            final ItemStack itemStack = cardInstance.withdrawStack(requestAmount);

            if (itemStack == null) {
                return;
            }

            blockMenu.pushItem(itemStack, OUTPUT_SLOT);
            setCardInstance(card, cardInstance);
        }
    }

    @Nullable
    private CardInstance getCardInstance(@Nonnull ItemStack card) {
        final ItemMeta cardMeta = card.getItemMeta();
        return DataTypeMethods.getCustom(cardMeta, Keys.CARD_INSTANCE, PersistentCardInstanceType.TYPE);
    }

    @Nullable
    private CardInstance getAmountInstance(@Nonnull ItemStack card) {
        final ItemMeta cardMeta = card.getItemMeta();
        return DataTypeMethods.getCustom(cardMeta, Keys.CARD_INSTANCE, PersistentAmountInstanceType.TYPE);
    }

    private void setCardInstance(@Nonnull ItemStack card, CardInstance cardInstance) {
        final ItemMeta cardMeta = card.getItemMeta();
        cardInstance.updateLore(cardMeta);
        DataTypeMethods.setCustom(cardMeta, Keys.CARD_INSTANCE, PersistentCardInstanceType.TYPE, cardInstance);
        card.setItemMeta(cardMeta);
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {

            @Override
            public void init() {
                for (int i : INPUT_SLOTS) {
                    addItem(i, BACK_INPUT, (p, slot, item, action) -> false);
                }
                for (int i : CARD_SLOTS) {
                    addItem(i, BACK_CARD, (p, slot, item, action) -> false);
                }
                for (int i : OUTPUT_SLOTS) {
                    addItem(i, BACK_OUTPUT, (p, slot, item, action) -> false);
                }
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return NetworkSlimefunItems.NETWORK_CELL.canUse(player, false)
                    && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return new int[0];
            }

        };
    }

}
