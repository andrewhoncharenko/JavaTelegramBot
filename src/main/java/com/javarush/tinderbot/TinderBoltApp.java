package com.javarush.tinderbot;

import com.javarush.tinderbot.ChatGPTService;
import com.javarush.tinderbot.DialogMode;
import com.javarush.tinderbot.MultiSessionTelegramBot;
import com.javarush.tinderbot.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "andriih2310tinder_bot";
    public static final String TELEGRAM_BOT_TOKEN = "8125375686:AAEnN8pxQRw-j7NW4AzN8wfPQvswChPx9tc";
    public static final String OPEN_AI_TOKEN = "chat-gpt-token"; //TODO: додай токен ChatGPT у лапках

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
    	String messageText = getMessageText();
        if(messageText.equals("/start")) {
        	String text = loadMessage("main");
        	sendPhotoMessage("main");
        	sendTextMessage(text);
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
