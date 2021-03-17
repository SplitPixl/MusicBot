package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.utils.NameIconManager;
import net.dv8tion.jda.api.entities.ISnowflake;

public class NameIconCmd extends Command {
    private final Bot bot;
    private final Long[] permitted = new Long[]{};
    private final NameIconManager iconman;
    private final long staffId;

    public NameIconCmd(Bot bot, String name, NameIconManager iconman, long staffId) {
        this.iconman = iconman;
        this.staffId = staffId;
        this.name = name;
        this.help = "nameiconmanager";
        this.guildOnly = false;
        this.bot = bot;
        this.children = new Command[] {
                new UpdateCmd(bot)
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(String.format("Used %s of %s icons. (%.1f%% of bag used)", iconman.getUsedIcons().size(), iconman.getTotal(), ((float)iconman.getUsedIcons().size() / iconman.getTotal()) * 100));
    }

    private class UpdateCmd extends Command {
        private final Bot bot;

        public UpdateCmd(Bot bot) {
            this.name = "update";
            this.guildOnly = true;
            this.bot = bot;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getMember().getRoles().stream().map(ISnowflake::getIdLong).anyMatch(l -> l == staffId)) {
                iconman.update(false);
                event.replySuccess("Name and icon changed!");
            } else {
                event.replyError(">:(");
            }
        }
    }
}
