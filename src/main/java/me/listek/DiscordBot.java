package me.listek;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;

import net.rithms.riot.api.endpoints.static_data.constant.ChampionListTags;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;
import net.rithms.riot.api.endpoints.static_data.dto.ChampionList;
import net.rithms.riot.constant.Platform;
import org.apache.commons.io.IOUtils;

import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;


public class DiscordBot extends ListenerAdapter implements EventListener {

    public static JDA jda;
    public static JSONObject jsonLeagueData;
    public static RiotApi riotApi;
    public static String prefix = "!";

    public static void main(String[] argst) throws LoginException, InterruptedException, RiotApiException, IOException {


        // Discord Bot
        jda = JDABuilder.createDefault("TOKEN")
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("!Info"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();
        jda.addEventListener(new SlashCommands());

        // Riot Games API
        ApiConfig apiConfig = new ApiConfig().setKey("RIOT_KEY");
        riotApi = new RiotApi(apiConfig);

        // Get JSON Champion Data Base
        URL url = new URL("http://ddragon.leagueoflegends.com/cdn/12.18.1/data/en_US/champion.json");
        jsonLeagueData = getJson(url);

    }

    public static JSONObject getJson(URL url) throws IOException {
        String json = IOUtils.toString(url, Charset.forName("UTF-8"));
        return new JSONObject(json);
    }




}
