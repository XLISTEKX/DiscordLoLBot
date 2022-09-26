package me.listek;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.champion_mastery.dto.ChampionMastery;
import net.rithms.riot.api.endpoints.league.dto.LeagueEntry;
import net.rithms.riot.api.endpoints.league.dto.LeagueList;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.static_data.constant.ChampionListTags;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;
import net.rithms.riot.api.endpoints.static_data.dto.ChampionList;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.api.endpoints.summoner.methods.GetSummoner;
import net.rithms.riot.constant.Platform;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SlashCommands extends ListenerAdapter{
    EmbedBuilder embedBuilder = new EmbedBuilder();
    Platform platform = Platform.EUNE;


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()){
            return;
        }
        platform = Platform.EUNE;
        embedBuilder.clear();
        String[] args = event.getMessage().getContentDisplay().split(" ");

        switch (args[0]){
            case "!info" ->
            {
                //Info about all commands
                event.getChannel().sendTyping().queue();
                embedBuilder.setTitle("All Commands: ")
                        .setColor(Color.green)
                        .addField("!info", "Shows all commands", true)
                        .addField("!summoner [SummonerName] [Region(deafault EUNE)]", "Shows all stats of Summoner ", false)
                        .addField("!build [Champion]", "Shows build for a champion", false)
                        .addField("!mastery [SummonerName] [Champion]", "Shows build for a champion", false);
                event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
                break;
            }
            case "!mastery" ->
            {
                if(args.length == 2){
                    showWholeMastery(event, args);

                    return;
                } else if (args.length > 3) {
                    showError(event, 101, "Something went wrong...");
                    return;
                }

                //Get Summoner ID
                Summoner summoner;
                try {
                    summoner = getSummoner(args[1]);
                } catch (RiotApiException e) {
                    showError(event, 404, "Summoner not found");
                    return;
                }
                //Find Champion
                args[2] = args[2].toLowerCase();
                String temp = args[2].substring(0, 1).toUpperCase();
                args[2] =  temp + args[2].substring(1);

                //Look for a champion
                int championID = 0;
                if(DiscordBot.jsonLeagueData.getJSONObject("data").has(args[2])){
                    String tempChampionID = (String) DiscordBot.jsonLeagueData.getJSONObject("data").getJSONObject(args[2]).get("key");
                    championID = Integer.parseInt(tempChampionID);
                }
                else{
                    showError(event, 404, "Champion not found !");
                    return;
                }
                //Find Champion Mastery
                ChampionMastery championMastery = null;
                try {
                    championMastery = DiscordBot.riotApi.getChampionMasteriesBySummonerByChampion(platform, summoner.getId(), championID);
                } catch (RiotApiException e) {
                    showError(event, 404, "Champion not found !");
                    return;
                }

                //Show mastery
                event.getChannel().sendTyping().queue();
                embedBuilder.setTitle("Mastery: " + args[2])
                        .setColor(Color.green)
                        .addField("Level: " + championMastery.getChampionLevel(), "Points: " + championMastery.getChampionPoints(), true)
                        .setThumbnail("http://ddragon.leagueoflegends.com/cdn/12.18.1/img/champion/" + args[2] + ".png");
                event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
                break;
                DiscordBot.riotApi.m
            }
            case "!build" ->
            {
                if(args.length != 2){
                    showError(event, 101, "Something went wrong...");
                    return;
                }
                //Capitalization
                args[1] = args[1].toLowerCase();
                String temp = args[1].substring(0, 1).toUpperCase();
                args[1] =  temp + args[1].substring(1);

                //Look for a champion
                if(DiscordBot.jsonLeagueData.getJSONObject("data").has(args[1])){

                    event.getChannel().sendTyping().queue();
                    embedBuilder.setTitle("Champion: **"+ DiscordBot.jsonLeagueData.getJSONObject("data").getJSONObject(args[1]).get("id") +"**")
                            .setColor(Color.green)
                            .addField("U.gg:","https://u.gg/lol/champions/"+ args[1] +"/build",true)
                            .setThumbnail("http://ddragon.leagueoflegends.com/cdn/12.18.1/img/champion/" + args[1] + ".png");
                    event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
                }
                else{
                    showError(event, 404, "Champion not found !");
                }
                break;



            }
            case "!summoner" ->
            {
                if(args.length == 1){
                    showError(event, 101, "You need to add Summoner name !");
                    return;
                }
                else{
                    if (args.length > 3){
                        showError(event, 102, "Too many arguments !");
                        return;
                    }
                    if(args.length == 3 ){
                        switch (args[2].toUpperCase()){
                            default -> {
                                showError(event, 103, "Region not found !");
                                return;
                            }

                            case "EUNE"->{
                                platform = Platform.EUNE;
                                break;
                            }
                            case "EUW"->{
                                platform = Platform.EUW;
                                break;
                            }
                            case "KR"->{
                                platform = Platform.KR;
                                break;
                            }
                            case "NA"->{
                                platform = Platform.NA;
                                break;
                            }
                        }
                    }
                    Summoner summoner;
                    Set<LeagueEntry> leagueLists;
                    try {
                        summoner = getSummoner(args[1]);
                        leagueLists = DiscordBot.riotApi.getLeagueEntriesBySummonerId(platform, summoner.getId());
                    } catch (RiotApiException e) {
                        System.out.println(e);
                        showError(event, 404, "Summoner not found");
                        return;
                    }
                    String rank = null;
                    String division = null;
                    float winrate = 0;
                    float wins = 0;
                    float loses = 0;
                    float lp = 0;
                    for (LeagueEntry leagueList : leagueLists) {
                        rank = leagueList.getRank();
                        division = leagueList.getTier();
                        wins = leagueList.getWins();
                        loses = leagueList.getLosses();
                        float all = wins + loses;
                        lp = leagueList.getLeaguePoints();
                        winrate = wins * 100 / all;

                    }

                    event.getChannel().sendTyping().queue();
                        embedBuilder.setTitle("Summoner: **" + summoner.getName() + "**")
                                .setColor(Color.green)
                                .addField("LEVEL", String.valueOf(summoner.getSummonerLevel()), true)
                                .addField("W: " + wins + " L: " + loses, "*Win rate: " + String.format("%.02f", winrate) + "*", true)
                                .addField("Current rank: **" + rank+ " " + division+ "**", "LP: "+ lp , false)
                                .addField("https://u.gg/lol/profile/eun1/" + args[1] +"/overview", "", false);
                        embedBuilder.setThumbnail("http://ddragon.leagueoflegends.com/cdn/12.18.1/img/profileicon/" +summoner.getProfileIconId()+ ".png");
                        event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();


                }
                break;
            }
            default -> {
                return;
            }
        }

    }

    public Summoner getSummoner(String summonerName) throws RiotApiException {
        return DiscordBot.riotApi.getSummonerByName(platform, summonerName);

    }

    public void showError(MessageReceivedEvent event, int errorID, String errorDescription){
        event.getChannel().sendTyping().queue();
        embedBuilder.setColor(Color.red)
                .addField("ERROR: " + errorID, errorDescription, true);
        event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void showWholeMastery(MessageReceivedEvent event, String[] arguments) {
        //Get Summoner
        Summoner summoner;
        try {
            summoner = getSummoner(arguments[1]);
        } catch (RiotApiException e) {
            showError(event, 404, "Summoner not found");
            return;
        }

        //Get all points
        Iterator<String> keys = DiscordBot.jsonLeagueData.getJSONObject("data").keys();
        int allMasteryPoints = 0;
        int allMasteryLevels = 0;
        while (keys.hasNext()) {
            String key = keys.next();
            System.out.println(key);
            if (DiscordBot.jsonLeagueData.getJSONObject("data").get(key) instanceof JSONObject) {
                String name = (String) DiscordBot.jsonLeagueData.getJSONObject("data").getJSONObject(key).get("key");
                int champID = Integer.parseInt(name);

                try {
                    allMasteryPoints += DiscordBot.riotApi.getChampionMasteriesBySummonerByChampion(platform, summoner.getId(), champID).getChampionPoints();
                    allMasteryLevels += DiscordBot.riotApi.getChampionMasteriesBySummonerByChampion(platform, summoner.getId(), champID).getChampionLevel();
                } catch (RiotApiException e) {
                    System.out.println(e);
                    showError(event, 404, "Summoner or Champion not found");
                    //return;
                }

            }
        }

        //Show results
        event.getChannel().sendTyping().queue();
        embedBuilder.setTitle("Summoner: **" + summoner.getName() + "**")
                .setColor(Color.green)
                .addField("All Masteries: " + allMasteryLevels, "All mastery points: " + allMasteryPoints, true)
                .setThumbnail("http://ddragon.leagueoflegends.com/cdn/12.18.1/img/profileicon/" +summoner.getProfileIconId()+ ".png");
        event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

}
