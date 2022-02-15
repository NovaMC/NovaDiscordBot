package xyz.novaserver.discordbot.services;

import xyz.novaserver.discordbot.NovaBot;

public abstract class Service {
    private final NovaBot novaBot;

    public Service(NovaBot novaBot) {
        this.novaBot = novaBot;
    }

    public NovaBot getNovaBot() {
        return novaBot;
    }

    public abstract void register();
}
