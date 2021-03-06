/*
 * MIT License
 *
 * Copyright (c) 2021 Evyn Price
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.evyn.bot.commands.info;

import me.evyn.bot.commands.Command;
import me.evyn.bot.commands.CommandHandler;
import me.evyn.bot.commands.CommandType;
import me.evyn.bot.util.EmbedCreator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class Usage implements Command {

    /**
     * Provides information on command usage and aliases
     * @param event Discord API message event
     * @param prefix Specific guild bot prefix
     * @param embed Guild embed setting
     * @param args Command arguments
     */
    @Override
    public void run(MessageReceivedEvent event, String prefix, boolean embed, String[] args) {

        User bot = event.getJDA().getSelfUser();

        // if no arguments are present, send error and return
        if (args.length == 0) {
            String desc = "Invalid command usage. Try running `" + prefix + "usage [command]`";
            if (embed) {
                EmbedBuilder eb = EmbedCreator.newErrorEmbedMessage(bot, desc);
                event.getChannel()
                        .sendMessage(eb.build())
                        .queue();
                return;
            } else {
                event.getChannel()
                        .sendMessage("ERROR: " + desc)
                        .queue();
            }
        } else {
            // attempt to find command
            String cmd = args[0];

            Command command = CommandHandler.findCommand(cmd);

            // if command cannot be found, send error and return
            if (command == null) {
                String desc = "That command could not be found.";
                if (embed) {
                    EmbedBuilder eb = EmbedCreator.newErrorEmbedMessage(bot, desc);
                    event.getChannel()
                            .sendMessage(eb.build())
                            .queue();
                } else {
                    event.getChannel()
                            .sendMessage("ERROR: " + desc)
                            .queue();
                }

            } else {
                // found command, generate embed
                StringBuilder sb = new StringBuilder();

                // set command aliases
                if (command.getAliases().size() == 0) {
                    sb.append("None");
                } else {
                    for (String alias : command.getAliases()) {
                        sb.append(alias).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                }

                if (embed) {
                    EmbedBuilder eb = EmbedCreator.newCommandEmbedMessage(bot)
                            .setTitle("Usage: " + command.getName())
                            .setDescription("() Optional Argument" + "\n" + "[] Required Argument")
                            .addField("Description", command.getDescription(), false)
                            .addField("Command Usage", prefix + command.getUsage(), false)
                            .addField("Aliases", sb.toString(), false);

                    event.getChannel()
                            .sendMessage(eb.build())
                            .queue();
                } else {
                    event.getChannel()
                            .sendMessage("**Usage: " + command.getName() + "**\n" + "() Optional Argument" +
                                    "\n" + "[] Required Argument" + "\n\n" + "**Description:** " +
                                    command.getDescription() + "\n" + "**Command Usage:** " + prefix +
                                    command.getUsage() + "\n" + "**Aliases:** " + sb.toString())
                            .queue();
                }
            }
        }
    }

    @Override
    public String getName() {
        return "usage";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList();
    }

    @Override
    public String getDescription() {
        return "Provides the proper usage of a specific command";
    }

    @Override
    public String getUsage() {
        return "usage [command]";
    }

    @Override
    public CommandType getType() {
        return CommandType.INFO;
    }
}
