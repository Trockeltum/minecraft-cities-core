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

    private static final int CARD_WIDTH   = 70;
    private static final int CARD_HEIGHT  = 36;
    private static final int CARD_SPACING = 6;
    private static final int PANEL_W      = 220;

    private static final int COLOR_GOLD      = 0xFFFFD700;
    private static final int COLOR_CITY      = 0xFF4FC3F7;
    private static final int COLOR_PREMIUM   = 0xFFCE93D8;
    private static final int COLOR_CARD_BG   = 0xFF222233;
    private static final int COLOR_DETAIL_BG = 0xFF111122;
    private static final int COLOR_BTN_BG    = 0xFF2A2A44;
    private static final int COLOR_BTN_HOVER = 0xFF3A3A55;
    private static final int COLOR_BTN_TEXT  = 0xFFCCCCCC;
    private static final int COLOR_TEXT      = 0xFFEEEEEE;
    private static final int COLOR_DIM       = 0xFFAAAAAA;

    private Currency expandedCard = Currency.GOLD;
    private boolean showHistory = false;

    // Computed each frame — used for mouse hit-testing
    private int tradeX, tradeY, historyX, historyY;
    private static final int BTN_W = 50, BTN_H = 14;

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
        int cardY = cardY();

        // Invisible click-area buttons for the currency cards
        final int gx = startX;
        addRenderableWidget(Button.builder(Component.empty(), b -> selectCard(Currency.GOLD))
                .bounds(gx, cardY, CARD_WIDTH, CARD_HEIGHT).build());

        final int cx = startX + CARD_WIDTH + CARD_SPACING;
        addRenderableWidget(Button.builder(Component.empty(), b -> selectCard(Currency.CITY))
                .bounds(cx, cardY, CARD_WIDTH, CARD_HEIGHT).build());

        if (premiumEnabled) {
            final int px = startX + 2 * (CARD_WIDTH + CARD_SPACING);
            addRenderableWidget(Button.builder(Component.empty(), b -> selectCard(Currency.PREMIUM))
                    .bounds(px, cardY, CARD_WIDTH, CARD_HEIGHT).build());
        }
    }

    private void selectCard(Currency c) {
        expandedCard = c;
        showHistory = false;
    }

    private int cardY()   { return this.height / 2 - 80; }
    private int detailY() { return cardY() + CARD_HEIGHT + 8; }
    private int panelX()  { return (this.width - PANEL_W) / 2; }

    // -------------------------------------------------------------------------
    // Rendering — everything custom is drawn AFTER super.render() so it sits
    // on top of the background blur that super.render() applies.
    // -------------------------------------------------------------------------

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);   // background + card click widgets

        PlayerCurrencyData data = getData();
        boolean premiumEnabled = CoreConfig.PREMIUM_ENABLED.get();
        int numCards = premiumEnabled ? 3 : 2;
        int totalWidth = numCards * CARD_WIDTH + (numCards - 1) * CARD_SPACING;
        int startX = (this.width - totalWidth) / 2;
        int cardY = cardY();
        int detailY = detailY();

        // Detail panel
        drawDetailPanel(g, detailY, data, mouseX, mouseY);

        // Cards on top (cover the invisible button backgrounds)
        drawCard(g, startX,                              cardY, Currency.GOLD,    data, "GOLD",  COLOR_GOLD);
        drawCard(g, startX + CARD_WIDTH + CARD_SPACING,  cardY, Currency.CITY,    data, "CITY",  COLOR_CITY);
        if (premiumEnabled) {
            drawCard(g, startX + 2 * (CARD_WIDTH + CARD_SPACING), cardY, Currency.PREMIUM, data, "PREM.", COLOR_PREMIUM);
        }
    }

    private void drawCard(GuiGraphics g, int x, int y, Currency currency,
                          PlayerCurrencyData data, String label, int accent) {
        boolean selected = expandedCard == currency;
        int border = selected ? accent : 0xFF444466;

        g.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, COLOR_CARD_BG);
        g.hLine(x,                 x + CARD_WIDTH - 1, y,                border);
        g.hLine(x,                 x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, border);
        g.vLine(x,                 y, y + CARD_HEIGHT - 1, border);
        g.vLine(x + CARD_WIDTH - 1, y, y + CARD_HEIGHT - 1, border);

        g.drawCenteredString(this.font, label, x + CARD_WIDTH / 2, y + 6, accent);
        String bal = formatNumber(data != null ? data.get(currency) : 0);
        g.drawCenteredString(this.font, bal, x + CARD_WIDTH / 2, y + 20, COLOR_TEXT);
    }

    private void drawDetailPanel(GuiGraphics g, int y, PlayerCurrencyData data, int mx, int my) {
        int x = panelX();
        switch (expandedCard) {
            case GOLD    -> drawGoldDetail(g, x, y, data, mx, my);
            case CITY    -> drawCityDetail(g, x, y, data);
            case PREMIUM -> drawPremiumDetail(g, x, y, data);
        }
    }

    private void drawGoldDetail(GuiGraphics g, int x, int y, PlayerCurrencyData data, int mx, int my) {
        int h = showHistory ? 145 : 100;
        g.fill(x, y, x + PANEL_W, y + h, COLOR_DETAIL_BG);

        long balance = data != null ? data.get(Currency.GOLD) : 0;
        g.drawString(this.font, "▼ GOLD DETAILS", x + 6, y + 6, COLOR_GOLD);
        g.drawString(this.font, "Balance: " + formatNumber(balance), x + 6, y + 18, COLOR_TEXT);

        if (CoreConfig.EARN_ENABLED.get()) {
            long rate = 0;
            if (CoreConfig.EARN_MINING_ENABLED.get())   rate += CoreConfig.EARN_MINING_GOLD_PER_BLOCK.get();
            if (CoreConfig.EARN_MOB_KILL_ENABLED.get()) rate += CoreConfig.EARN_MOB_KILL_GOLD_PER_KILL.get();
            g.drawString(this.font, "Earn: +" + rate + "/activity", x + 6, y + 30, COLOR_DIM);
        }

        if (showHistory) {
            g.drawString(this.font, "-- Recent transactions --", x + 6, y + 46, COLOR_DIM);
            List<TransactionEntry> entries = data != null ? data.history().entries() : List.of();
            int lineY = y + 58;
            int shown = 0;
            for (TransactionEntry e : entries) {
                if (shown >= 5) break;
                String dir = e.sent() ? "->" : "<-";
                String ts = new SimpleDateFormat("MM/dd HH:mm").format(new Date(e.timestamp()));
                g.drawString(this.font, dir + " " + formatNumber(e.amount()) + " " + e.counterpartyName() + " " + ts,
                        x + 6, lineY, e.sent() ? 0xFFFF6666 : 0xFF66FF66);
                lineY += 10;
                shown++;
            }
            if (entries.isEmpty()) {
                g.drawString(this.font, "No transactions yet.", x + 6, y + 58, COLOR_DIM);
            }
        }

        // Manual Trade / History buttons drawn inside the panel
        tradeX   = x + PANEL_W / 2 - BTN_W - 4;
        tradeY   = y + h - BTN_H - 8;
        historyX = x + PANEL_W / 2 + 4;
        historyY = tradeY;

        drawMiniButton(g, tradeX,   tradeY,   "Trade",   mx, my);
        drawMiniButton(g, historyX, historyY, "History", mx, my);
    }

    private void drawMiniButton(GuiGraphics g, int x, int y, String label, int mx, int my) {
        boolean hovered = mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H;
        g.fill(x, y, x + BTN_W, y + BTN_H, hovered ? COLOR_BTN_HOVER : COLOR_BTN_BG);
        g.hLine(x, x + BTN_W - 1, y,           0xFF555577);
        g.hLine(x, x + BTN_W - 1, y + BTN_H - 1, 0xFF555577);
        g.vLine(x,           y, y + BTN_H - 1, 0xFF555577);
        g.vLine(x + BTN_W - 1, y, y + BTN_H - 1, 0xFF555577);
        g.drawCenteredString(this.font, label, x + BTN_W / 2, y + 3, COLOR_BTN_TEXT);
    }

    private void drawCityDetail(GuiGraphics g, int x, int y, PlayerCurrencyData data) {
        g.fill(x, y, x + PANEL_W, y + 50, COLOR_DETAIL_BG);
        long balance = data != null ? data.get(Currency.CITY) : 0;
        g.drawString(this.font, "▼ CITY TOKEN DETAILS", x + 6, y + 6, COLOR_CITY);
        g.drawString(this.font, "Balance: " + formatNumber(balance), x + 6, y + 18, COLOR_TEXT);
        // TODO: pass actual cities-founded count once City System is implemented
        long nextCost = CoreConfig.CITY_COST_FORMULA.get().computeCost(CoreConfig.CITY_COST_BASE.get(), 0);
        g.drawString(this.font, "Next city costs: " + formatNumber(nextCost), x + 6, y + 30, COLOR_DIM);
    }

    private void drawPremiumDetail(GuiGraphics g, int x, int y, PlayerCurrencyData data) {
        g.fill(x, y, x + PANEL_W, y + 40, COLOR_DETAIL_BG);
        long balance = data != null ? data.get(Currency.PREMIUM) : 0;
        g.drawString(this.font, "▼ PREMIUM DETAILS", x + 6, y + 6, COLOR_PREMIUM);
        g.drawString(this.font, "Balance: " + formatNumber(balance), x + 6, y + 18, COLOR_TEXT);
    }

    // -------------------------------------------------------------------------
    // Click handling for manual buttons
    // -------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (expandedCard == Currency.GOLD) {
            if (hit(mx, my, tradeX, tradeY)) { openTrade(); return true; }
            if (hit(mx, my, historyX, historyY)) { showHistory = !showHistory; return true; }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private boolean hit(double mx, double my, int x, int y) {
        return mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H;
    }

    private void openTrade() {
        this.minecraft.setScreen(null);
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
    public boolean isPauseScreen() { return false; }
}
