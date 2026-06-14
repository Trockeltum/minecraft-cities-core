package com.minecraftcities.core.client;

import com.minecraftcities.core.config.CoreConfig;
import com.minecraftcities.core.currency.Currency;
import com.minecraftcities.core.currency.CurrencyAttachments;
import com.minecraftcities.core.currency.PlayerCurrencyData;
import com.minecraftcities.core.history.TransactionEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WalletScreen extends Screen {

    // Card dimensions
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 36;
    private static final int CARD_SPACING = 6;

    // Colors
    private static final int COLOR_GOLD    = 0xFFFFD700;
    private static final int COLOR_CITY    = 0xFF4FC3F7;
    private static final int COLOR_PREMIUM = 0xFFCE93D8;
    private static final int COLOR_CARD_BG   = 0xCC222233;
    private static final int COLOR_DETAIL_BG = 0xCC111122;
    private static final int COLOR_TEXT = 0xFFEEEEEE;
    private static final int COLOR_DIM  = 0xFFAAAAAA;

    private Currency expandedCard = Currency.GOLD;
    private boolean showHistory = false;

    // Detail panel buttons (Gold only)
    private Button tradeButton;
    private Button historyButton;

    public WalletScreen() {
        super(Component.translatable("minecraftcitiescore.wallet.title"));
    }

    @Override
    protected void init() {
        super.init();

        boolean premiumEnabled = CoreConfig.PREMIUM_ENABLED.get();
        int numCards = premiumEnabled ? 3 : 2;

        int totalWidth = numCards * CARD_WIDTH + (numCards - 1) * CARD_SPACING;
        int startX = (this.width - totalWidth) / 2;
        int cardY = this.height / 2 - 80;

        // Gold card
        final int goldX = startX;
        this.addRenderableWidget(
                Button.builder(Component.empty(), b -> {
                            expandedCard = Currency.GOLD;
                            showHistory = false;
                            updateDetailButtonVisibility();
                        })
                        .bounds(goldX, cardY, CARD_WIDTH, CARD_HEIGHT)
                        .build());

        // City card
        final int cityX = startX + CARD_WIDTH + CARD_SPACING;
        this.addRenderableWidget(
                Button.builder(Component.empty(), b -> {
                            expandedCard = Currency.CITY;
                            showHistory = false;
                            updateDetailButtonVisibility();
                        })
                        .bounds(cityX, cardY, CARD_WIDTH, CARD_HEIGHT)
                        .build());

        // Premium card (conditionally)
        if (premiumEnabled) {
            final int premX = startX + 2 * (CARD_WIDTH + CARD_SPACING);
            this.addRenderableWidget(
                    Button.builder(Component.empty(), b -> {
                                expandedCard = Currency.PREMIUM;
                                showHistory = false;
                                updateDetailButtonVisibility();
                            })
                            .bounds(premX, cardY, CARD_WIDTH, CARD_HEIGHT)
                            .build());
        }

        // Detail action buttons — visible only when Gold is expanded
        int detailY = cardY + CARD_HEIGHT + 8;
        tradeButton = Button.builder(
                        Component.translatable("minecraftcitiescore.wallet.trade"),
                        b -> openTrade())
                .bounds(this.width / 2 - 50, detailY + 60, 45, 16)
                .build();
        historyButton = Button.builder(
                        Component.translatable("minecraftcitiescore.wallet.history"),
                        b -> showHistory = !showHistory)
                .bounds(this.width / 2 + 5, detailY + 60, 45, 16)
                .build();
        this.addRenderableWidget(tradeButton);
        this.addRenderableWidget(historyButton);

        updateDetailButtonVisibility();
    }

    private void updateDetailButtonVisibility() {
        boolean isGold = expandedCard == Currency.GOLD;
        if (tradeButton != null)   tradeButton.visible   = isGold;
        if (historyButton != null) historyButton.visible = isGold;
    }

    private void openTrade() {
        // Close screen; Task 11 will wire up chat pre-fill with /pay
        this.minecraft.setScreen(null);
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        PlayerCurrencyData data = getData();
        boolean premiumEnabled = CoreConfig.PREMIUM_ENABLED.get();
        int numCards = premiumEnabled ? 3 : 2;
        int totalWidth = numCards * CARD_WIDTH + (numCards - 1) * CARD_SPACING;
        int startX = (this.width - totalWidth) / 2;
        int cardY = this.height / 2 - 80;
        int detailY = cardY + CARD_HEIGHT + 8;

        // Detail panel drawn first (behind buttons)
        drawDetailPanel(graphics, detailY, data);

        // Render widgets (buttons provide click areas)
        super.render(graphics, mouseX, mouseY, partialTick);

        // Cards drawn last so text appears on top of button backgrounds
        drawCard(graphics, startX,                             cardY, Currency.GOLD,    data, "GOLD",  COLOR_GOLD);
        drawCard(graphics, startX + CARD_WIDTH + CARD_SPACING, cardY, Currency.CITY,    data, "CITY",  COLOR_CITY);
        if (premiumEnabled) {
            drawCard(graphics, startX + 2 * (CARD_WIDTH + CARD_SPACING), cardY, Currency.PREMIUM, data, "PREM.", COLOR_PREMIUM);
        }
    }

    private void drawCard(GuiGraphics g, int x, int y, Currency currency,
                          PlayerCurrencyData data, String label, int accentColor) {
        boolean selected = expandedCard == currency;
        int border = selected ? accentColor : 0xFF444466;

        g.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, COLOR_CARD_BG);
        g.hLine(x,               x + CARD_WIDTH - 1, y,                border);
        g.hLine(x,               x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, border);
        g.vLine(x,               y, y + CARD_HEIGHT - 1, border);
        g.vLine(x + CARD_WIDTH - 1, y, y + CARD_HEIGHT - 1, border);

        g.drawCenteredString(this.font, label, x + CARD_WIDTH / 2, y + 6, accentColor);
        String bal = formatNumber(data != null ? data.get(currency) : 0);
        g.drawCenteredString(this.font, bal, x + CARD_WIDTH / 2, y + 20, COLOR_TEXT);
    }

    private void drawDetailPanel(GuiGraphics g, int y, PlayerCurrencyData data) {
        int panelW = 220;
        int panelX = (this.width - panelW) / 2;
        switch (expandedCard) {
            case GOLD    -> drawGoldDetail(g, panelX, y, panelW, data);
            case CITY    -> drawCityDetail(g, panelX, y, panelW, data);
            case PREMIUM -> drawPremiumDetail(g, panelX, y, panelW, data);
        }
    }

    private void drawGoldDetail(GuiGraphics g, int x, int y, int w, PlayerCurrencyData data) {
        int h = showHistory ? 120 : 80;
        g.fill(x, y, x + w, y + h, COLOR_DETAIL_BG);

        long balance = data != null ? data.get(Currency.GOLD) : 0;
        g.drawString(this.font, "▼ GOLD DETAILS", x + 6, y + 6, COLOR_GOLD);
        g.drawString(this.font, "Balance: " + formatNumber(balance), x + 6, y + 18, COLOR_TEXT);

        // Passive earn summary
        if (CoreConfig.EARN_ENABLED.get()) {
            long perTick = 0;
            if (CoreConfig.EARN_MINING_ENABLED.get())   perTick += CoreConfig.EARN_MINING_GOLD_PER_BLOCK.get();
            if (CoreConfig.EARN_MOB_KILL_ENABLED.get()) perTick += CoreConfig.EARN_MOB_KILL_GOLD_PER_KILL.get();
            g.drawString(this.font, "Earn: +" + formatNumber(perTick) + "/activity", x + 6, y + 30, COLOR_DIM);
        }

        if (showHistory) {
            g.drawString(this.font, "-- Recent transactions --", x + 6, y + 44, COLOR_DIM);
            List<TransactionEntry> entries = data != null ? data.history().entries() : List.of();
            int lineY = y + 54;
            int shown = 0;
            for (TransactionEntry e : entries) {
                if (shown >= 5) break;
                String dir = e.sent() ? "->" : "<-";
                String ts = new SimpleDateFormat("MM/dd HH:mm").format(new Date(e.timestamp()));
                String line = dir + " " + formatNumber(e.amount()) + " " + e.counterpartyName() + " " + ts;
                int col = e.sent() ? 0xFFFF6666 : 0xFF66FF66;
                g.drawString(this.font, line, x + 6, lineY, col);
                lineY += 10;
                shown++;
            }
            if (entries.isEmpty()) {
                g.drawString(this.font, "No transactions yet.", x + 6, y + 54, COLOR_DIM);
            }
        }
    }

    private void drawCityDetail(GuiGraphics g, int x, int y, int w, PlayerCurrencyData data) {
        g.fill(x, y, x + w, y + 60, COLOR_DETAIL_BG);

        long balance = data != null ? data.get(Currency.CITY) : 0;
        g.drawString(this.font, "▼ CITY TOKEN DETAILS", x + 6, y + 6, COLOR_CITY);
        g.drawString(this.font, "Balance: " + formatNumber(balance), x + 6, y + 18, COLOR_TEXT);

        // TODO: pass actual cities-founded count once City System is implemented
        long nextCost = CoreConfig.CITY_COST_FORMULA.get()
                .computeCost(CoreConfig.CITY_COST_BASE.get(), 0);
        g.drawString(this.font, "Next city costs: " + formatNumber(nextCost), x + 6, y + 30, COLOR_DIM);
    }

    private void drawPremiumDetail(GuiGraphics g, int x, int y, int w, PlayerCurrencyData data) {
        g.fill(x, y, x + w, y + 50, COLOR_DETAIL_BG);

        long balance = data != null ? data.get(Currency.PREMIUM) : 0;
        g.drawString(this.font, "▼ PREMIUM DETAILS", x + 6, y + 6, COLOR_PREMIUM);
        g.drawString(this.font, "Balance: " + formatNumber(balance), x + 6, y + 18, COLOR_TEXT);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PlayerCurrencyData getData() {
        var player = Minecraft.getInstance().player;
        if (player == null) return null;
        return player.getData(CurrencyAttachments.CURRENCY_DATA.get());
    }

    private static String formatNumber(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000)     return String.format("%.1fK", n / 1_000.0);
        return Long.toString(n);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
