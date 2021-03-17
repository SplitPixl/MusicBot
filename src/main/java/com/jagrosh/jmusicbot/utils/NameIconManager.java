package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jmusicbot.Bot;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NameIconManager {
    private ScheduledExecutorService threadpool = Executors.newSingleThreadScheduledExecutor();
    private Bot bot;
    private List<Long> usedIcons = new ArrayList<>();
    private int total = 0;
    private long channelId = 0;
    private long serverId = 0;

    public NameIconManager(Bot bot, long channelId, long serverId) {
        this.bot = bot;
        threadpool.schedule(() -> update(true), 30, TimeUnit.SECONDS);
        this.channelId = channelId;
        this.serverId = serverId;
    }

    public void update(boolean queueNext) {
        try {
            JDA jda = bot.getJDA();
            TextChannel tc = jda.getTextChannelById(channelId);
            if (tc != null) {
                tc.getHistoryFromBeginning(100).queue(m -> doThingWithMessages(m, queueNext), e -> getHistoryError(e, queueNext));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doThingWithMessages(MessageHistory messageHistory, boolean queueNext) {
        total = messageHistory.size();
        if (messageHistory.size() == usedIcons.size()) {
            usedIcons.clear();
        }
        List<Message> msgs = messageHistory.getRetrievedHistory().stream().filter(msg -> !usedIcons.contains(msg.getIdLong())).collect(Collectors.toList());
        Message msg = msgs.get((int)(Math.random()*msgs.size()));
        usedIcons.add(msg.getIdLong());
        JDA jda = bot.getJDA();
        try {
            String iconLink = msg.getAttachments().get(0).getUrl();
            HttpResponse<InputStream> req = Unirest.get(iconLink).asBinary();
            Objects.requireNonNull(jda.getGuildById(serverId)).getManager()
                    .setName(msg.getContentRaw())
                    .setIcon(Icon.from(req.getBody()))
                    .queue(unused -> {}, Throwable::printStackTrace);
        } catch (IOException | UnirestException e) {
            e.printStackTrace();
        }
        if (queueNext)
            threadpool.schedule(() -> update(true), 90 + rand(-30L, 30L), TimeUnit.MINUTES);
    }

    private void getHistoryError(Throwable throwable, boolean queueNext) {
        throwable.printStackTrace();
        if (queueNext)
            threadpool.schedule(() -> update(true), 5, TimeUnit.MINUTES);
    }

    long rand(long min, long max) {
        return (long) Math.floor((Math.random() * (max - min)) + min);
    }

    public List<Long> getUsedIcons() {
        return usedIcons;
    }

    public int getTotal() {
        return total;
    }
}
