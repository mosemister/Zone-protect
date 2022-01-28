package org.zone.region.flag.meta.eco.price.player;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.zone.region.flag.meta.eco.price.Price;
import org.zone.region.flag.meta.eco.price.PriceBuilder;
import org.zone.region.flag.meta.eco.price.PriceType;

import java.math.BigDecimal;
import java.util.Optional;

public class PlayerEcoPrice implements Price.PlayerPrice<BigDecimal>, Price.EcoPrice<Player> {

    private final @NotNull Currency currency;
    private final @NotNull BigDecimal amount;

    public PlayerEcoPrice(@NotNull Currency currency, @NotNull BigDecimal decimal) {
        this.amount = decimal;
        this.currency = currency;
    }

    @Override
    public @NotNull Currency getCurrency() {
        return this.currency;
    }

    @Override
    public @NotNull BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public PriceType getType() {
        return PriceType.ECO;
    }

    @Override
    public boolean hasEnough(@NotNull Player player) {
        Optional<EconomyService> opService = Sponge.serviceProvider().provide(EconomyService.class);
        if (opService.isEmpty()) {
            return false;
        }
        if (!opService.get().hasAccount(player.uniqueId())) {
            return false;
        }
        Optional<UniqueAccount> opAccount = opService.get().findOrCreateAccount(player.uniqueId());
        if (opAccount.isEmpty()) {
            return false;
        }
        return opAccount.get().balance(this.currency).compareTo(this.amount) > 0;
    }

    @Override
    public float getPercentLeft(@NotNull Player player) {
        Optional<EconomyService> opService = Sponge.serviceProvider().provide(EconomyService.class);
        if (opService.isEmpty()) {
            return 0;
        }
        if (!opService.get().hasAccount(player.uniqueId())) {
            return 0;
        }
        Optional<UniqueAccount> opAccount = opService.get().findOrCreateAccount(player.uniqueId());
        if (opAccount.isEmpty()) {
            return 0;
        }
        BigDecimal decimal = opAccount.get().balance(this.currency);
        BigDecimal difference = decimal.min(this.amount);
        return (float) (difference.doubleValue() * 100 / decimal.doubleValue());
    }

    @Override
    public PriceBuilder asBuilder() {
        return new PriceBuilder().setType(PriceType.ECO).setAmount(this.amount.doubleValue()).setCurrency(this.currency);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return this.currency.symbol().append(Component.text(this.amount.toString()));
    }
}
