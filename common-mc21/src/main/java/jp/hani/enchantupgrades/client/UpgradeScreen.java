package jp.hani.enchantupgrades.client;

import jp.hani.enchantupgrades.menu.UpgradeMenu;
import jp.hani.enchantupgrades.recipe.UpgradeRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import java.util.List;

public final class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {
    private static final int ROWS = 4;
    private final Button[] rows = new Button[ROWS];
    private Button previous, next, upgrade;
    private int page;
    private ResourceLocation selectedId;
    private List<UpgradeRecipe> visible = List.of();
    private Item lastTargetItem, lastMaterialItem;

    public UpgradeScreen(UpgradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 310;
        imageHeight = 250;
        inventoryLabelX = 70;
        inventoryLabelY = 155;
        titleLabelX = 12;
        titleLabelY = 10;
    }

    @Override protected void init() {
        super.init();
        for (int i = 0; i < ROWS; i++) {
            final int row = i;
            rows[i] = addRenderableWidget(new RecipeButton(leftPos + 92, topPos + 31 + i * 27, () -> {
                int index = page * ROWS + row;
                if (index < visible.size()) selectedId = visible.get(index).id();
                refresh();
            }, i));
        }
        previous = addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; refresh(); }).bounds(leftPos + 92, topPos + 140, 24, 14).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; refresh(); }).bounds(leftPos + 272, topPos + 140, 24, 14).build());
        upgrade = addRenderableWidget(Button.builder(Component.translatable("screen.enchant_upgrades.upgrade"), b -> {
            UpgradeRecipe recipe = selected();
            if (recipe != null && minecraft.gameMode != null) {
                // Vanilla menu button packets carry only an ID; the server recomputes every cost.
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, menu.allRecipes().indexOf(recipe));
            }
        }).bounds(leftPos + 12, topPos + 125, 72, 20).build());
        refresh();
    }

    private UpgradeRecipe selected() { return visible.stream().filter(r -> r.id().equals(selectedId)).findFirst().orElse(null); }

    private void refresh() {
        visible = menu.visibleRecipes();
        page = Math.max(0, Math.min(page, Math.max(0, (visible.size() - 1) / ROWS)));
        if (selected() == null || lastTargetItem != menu.target().getItem() || lastMaterialItem != menu.material().getItem()) {
            UpgradeRecipe preferred = visible.stream().filter(r -> r.material().test(menu.material())).findFirst()
                .orElse(visible.isEmpty() ? null : visible.get(0));
            selectedId = preferred == null ? null : preferred.id();
            page = preferred == null ? 0 : visible.indexOf(preferred) / ROWS;
        }
        lastTargetItem = menu.target().getItem();
        lastMaterialItem = menu.material().getItem();
        for (int i = 0; i < ROWS; i++) rows[i].visible = page * ROWS + i < visible.size();
        previous.active = page > 0;
        next.active = (page + 1) * ROWS < visible.size();
        UpgradeRecipe recipe = selected();
        upgrade.active = recipe != null && menu.blockedReason(recipe, minecraft.player).getString().isEmpty();
    }

    @Override protected void containerTick() { super.containerTick(); refresh(); }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF171C28);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF293244);
        graphics.fill(leftPos + 8, topPos + 27, leftPos + 86, topPos + 121, 0xFF1A2130);
        graphics.fill(leftPos + 8, topPos + 23, leftPos + 302, topPos + 24, 0xFF7692B4);
        for (var slot : menu.slots) {
            graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0xFF9AAABC);
            graphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0xFF151B27);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xF0E8D5, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xC6D3E2, false);
        graphics.drawString(font, Component.translatable("screen.enchant_upgrades.target"), 13, 55, 0xBCCBDC, false);
        graphics.drawString(font, Component.translatable("screen.enchant_upgrades.material"), 49, 55, 0xBCCBDC, false);
        graphics.drawString(font, Component.translatable("screen.enchant_upgrades.shelves", menu.shelves()), 12, 110, 0xBCD0E3, false);
        graphics.drawCenteredString(font, Component.literal((visible.isEmpty() ? 0 : page + 1) + " / " + Math.max(1, (visible.size() + ROWS - 1) / ROWS)), 194, 143, 0xC6D3E2);
        UpgradeRecipe recipe = selected();
        if (recipe != null) {
            int level = Math.min(recipe.currentLevel(menu.target()) + 1, recipe.enchantment().getMaxLevel());
            ItemStack[] materials = recipe.material().getItems();
            if (materials.length > 0) {
                ItemStack item = materials[(int) (System.currentTimeMillis() / 1000 % materials.length)].copy();
                item.setCount(recipe.materialCost(level));
                graphics.renderItem(item, 18, 77);
                graphics.renderItemDecorations(font, item, 18, 77);
            }
            graphics.drawString(font, recipe.levelCost(level) + " Lv", 43, 81, 0xA8E0A0, false);
            graphics.drawString(font, Component.translatable("screen.enchant_upgrades.required"), 12, 96, 0xA9BAD0, false);
        } else {
            graphics.drawWordWrap(font, Component.translatable("screen.enchant_upgrades.insert"), 96, 64, 190, 0xC6D3E2);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        UpgradeRecipe recipe = selected();
        if (recipe != null && mouseX >= leftPos + 12 && mouseX < leftPos + 84 && mouseY >= topPos + 125 && mouseY < topPos + 145) {
            Component reason = menu.blockedReason(recipe, minecraft.player);
            if (!reason.getString().isEmpty()) graphics.renderTooltip(font, reason, mouseX, mouseY);
        }
        if (recipe != null && mouseX >= leftPos + 18 && mouseX < leftPos + 34 && mouseY >= topPos + 77 && mouseY < topPos + 93) {
            graphics.renderTooltip(font, java.util.Arrays.stream(recipe.material().getItems()).map(ItemStack::getHoverName).toList(), java.util.Optional.empty(), mouseX, mouseY);
        }
        for (int i = 0; i < ROWS; i++) {
            int index = page * ROWS + i;
            if (index < visible.size() && rows[i].isHovered()) {
                UpgradeRecipe rowRecipe = visible.get(index);
                Component reason = menu.blockedReason(rowRecipe, minecraft.player);
                Component name = rowRecipe.fullName(Math.min(rowRecipe.currentLevel(menu.target()) + 1, rowRecipe.enchantment().getMaxLevel()));
                List<Component> tooltip = reason.getString().isEmpty() ? List.of(name) : List.of(name, reason);
                graphics.renderTooltip(font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double delta) {
        if (x >= leftPos + 92 && x < leftPos + 296 && y >= topPos + 31 && y < topPos + 154) {
            page += delta < 0 ? 1 : -1;
            refresh();
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, delta);
    }

    private final class RecipeButton extends Button {
        private final int row;
        RecipeButton(int x, int y, Runnable click, int row) {
            super(x, y, 204, 25, Component.empty(), b -> click.run(), DEFAULT_NARRATION);
            this.row = row;
        }
        @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int index = page * ROWS + row;
            if (index >= UpgradeScreen.this.visible.size()) return;
            UpgradeRecipe recipe = UpgradeScreen.this.visible.get(index);
            boolean selected = recipe.id().equals(selectedId);
            boolean allowed = menu.blockedReason(recipe, minecraft.player).getString().isEmpty();
            int level = recipe.currentLevel(menu.target());
            int target = Math.min(level + 1, recipe.enchantment().getMaxLevel());
            graphics.fill(getX(), getY(), getX() + width, getY() + height, selected ? 0xFF456484 : isHovered ? 0xFF394B63 : 0xFF202A3A);
            if (selected) graphics.fill(getX(), getY(), getX() + 2, getY() + height, 0xFFB6E7A3);
            String name = recipe.enchantment().description().getString() + " " + level + " → " + target;
            if (level >= recipe.enchantment().getMaxLevel()) name = recipe.enchantment().description().getString() + " / " + Component.translatable("screen.enchant_upgrades.max").getString();
            setMessage(Component.literal(name));
            graphics.drawString(font, font.plainSubstrByWidth(name, width - 10), getX() + 6, getY() + 3, allowed ? 0xE6F3DF : 0xCAD1DD, false);
            String costs = Component.translatable("screen.enchant_upgrades.cost", recipe.materialCost(target), recipe.levelCost(target), recipe.shelfCost(target)).getString();
            graphics.drawString(font, font.plainSubstrByWidth(costs, width - 10), getX() + 6, getY() + 14, 0xACBDD0, false);
        }
    }
}

