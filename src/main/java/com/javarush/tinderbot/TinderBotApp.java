package com.javarush.tinderbot;

import com.javarush.tinderbot.ChatGPTService;
import com.javarush.tinderbot.DialogMode;
import com.javarush.tinderbot.MultiSessionTelegramBot;
import com.javarush.tinderbot.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class TinderBotApp extends MultiSessionTelegramBot {
    private static String telegramBotName;
    private static String telegramBotToken;
    private static String openAiToken;
    private DialogMode mode = DialogMode.MAIN;
    private ChatGPTService gptService;

    public TinderBotApp() {
    	super(telegramBotName, telegramBotToken);
    	gptService = new ChatGPTService(openAiToken);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
    	String messageText = getMessageText();
        if(messageText.equals("/start")) {
        	mode = DialogMode.MAIN;
        	showMainMenu("головне меню бота", "/start",
        			"Генерація Tinder-профілю 😎","/profile",
        			"Повідомлення для знайомства 🥰", "/opener",
        			"Листування від вашого імені 🖂", "/message",
        			"Листування із зірками 🔥", "/date",
        			"Поставити запитання чату GPT 🧠", "/gpt");
        	String response = loadMessage("main");
        	sendPhotoMessage("main");
        	sendTextMessage(response);
        }
        else if(messageText.equals("/gpt")) {
        	mode = DialogMode.GPT;
        	String response = loadMessage("gpt");
        	sendPhotoMessage("gpt");
        	sendTextMessage(response);
        }
        else if(mode == DialogMode.GPT) {
        	String prompt = loadPrompt("gpt");
        	String answer = gptService.sendMessage(prompt, messageText);
        	sendTextMessage(answer);
        }
    }

    public static void main(String[] args) throws TelegramApiException {
    	Dotenv envvalues = Dotenv.load();
        telegramBotName =  envvalues.get("TELEGRAM_BOT_NAME");
        telegramBotToken = envvalues.get("TELEGRAM_BOT_TOKEN");
        openAiToken = envvalues.get("CHAT_GPT_TOKEN");
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBotApp());
    }
}
