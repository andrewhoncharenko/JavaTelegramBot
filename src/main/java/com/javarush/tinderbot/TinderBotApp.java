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
import java.util.List;

public class TinderBotApp extends MultiSessionTelegramBot {
	private static String telegramBotName;
	private static String telegramBotToken;
	private static String openAiToken;
	private DialogMode mode = DialogMode.MAIN;
	private ChatGPTService gptService;
	private List<String> chat;
	private UserInfo myInfo, personInfo;
	private int questionNumber;

	public TinderBotApp() {
		super(telegramBotName, telegramBotToken);
		gptService = new ChatGPTService(openAiToken);
	}

	@Override
	public void onUpdateEventReceived(Update update) {
		String message = getMessageText();
		switch (message) {
		case "/start" -> {
			mode = DialogMode.MAIN;
			showMainMenu("головне меню бота", "/start", "Генерація Tinder-профілю 😎", "/profile",
					"Повідомлення для знайомства 🥰", "/opener", "Листування від вашого імені 🖂", "/message",
					"Листування із зірками 🔥", "/date", "Поставити запитання чату GPT 🧠", "/gpt");
			String response = loadMessage("main");
			sendPhotoMessage("main");
			sendTextMessage(response);
			return;
		}
		case "/profile" -> {
			String response = loadMessage("profile");
			mode = DialogMode.PROFILE;
			myInfo = new UserInfo();
			questionNumber = 1;
			sendPhotoMessage("profile");
			sendTextMessage(response);
			sendTextMessage("Введіть ім'я");
			return;
		}
		case "/opener" -> {
			String response = loadMessage("opener");
			mode = DialogMode.OPENER;
			personInfo = new UserInfo();
			questionNumber = 1;
			sendPhotoMessage("opener");
			sendTextMessage(response);
			sendTextMessage("Введіть ім'я");
			return;
		}
		case "/message" -> {
			String response = loadMessage("message");
			chat = new ArrayList<>();
			mode = DialogMode.MESSAGE;
			sendPhotoMessage("message");
			sendTextButtonsMessage(response, "Наступне повідомлення", "message_next", "Запросити на побаченння",
					"message_date");
			return;
		}
		case "/date" -> {
			mode = DialogMode.DATE;
			String response = loadMessage("date");
			sendPhotoMessage("date");
			sendTextButtonsMessage(response, "Аріана Гранде 🔥", "date_grande", "Марго Роббі 🔥🔥", "date_robbie",
					"Зендея 🔥🔥🔥", "date_zendaya", "Райан Гослінг 😎", "date_gosling", "Том Харді 😎😎",
					"date_hardy");
			return;
		}
		case "/gpt" -> {
			mode = DialogMode.GPT;
			String response = loadMessage("gpt");
			sendPhotoMessage("gpt");
			sendTextMessage(response);
			return;
		}
		}

		switch (mode) {
		case MESSAGE -> {
			String query = getCallbackQueryButtonKey();
			chat.add(message);
			if (query.startsWith("message_")) {
				String prompt = loadPrompt(query);
				String history = String.join("\n\n", chat);
				Message msg = sendTextMessage("ChatGPT друкує.....");
				String answer = gptService.sendMessage(prompt, history);
				updateTextMessage(msg, answer);
				return;
			}
		}
		case PROFILE -> {
			if(questionNumber <= 6) {
				askQuestion(message, myInfo, "profile");
			}
			return;
		}
		case OPENER -> {
			if(questionNumber <= 6) {
				askQuestion(message, personInfo, "opener");
			}
			return;
		}
		case DATE -> {
			String query = getCallbackQueryButtonKey();
			if (query.startsWith("date_")) {
				sendPhotoMessage(query);
				String prompt = loadPrompt(query);
				gptService.setPrompt(prompt);
			} else {
				String answer = gptService.addMessage(message);
				sendTextMessage(answer);
			}
			return;
		}
		case GPT -> {
			String prompt = loadPrompt("gpt");
			String answer = gptService.sendMessage(prompt, message);
			sendTextMessage(answer);
		}
		}
	}
	
	private void askQuestion(String message, UserInfo user, String profileName) {
		switch(questionNumber) {
		case 1 -> {
			user.name = message;
			questionNumber = 2;
			sendTextMessage("Введіть вік");
			return;
		}
		case 2 -> {
			user.age = message;
			questionNumber = 3;
			sendTextMessage("Введіть місто");
			return;
		}
		case 3 -> {
			user.city = message;
			questionNumber = 4;
			sendTextMessage("Введіть професію");
			return;
		}
		case 4 -> {
			user.occupation = message;
			questionNumber = 5;
			sendTextMessage("Введіть хоббі?");
			return;
		}
		case 5 -> {
			user.hobby = message;
			questionNumber = 6;
			sendTextMessage("Введіть цілі для знайомства?");
			return;
		}
		case 6 -> {
			String prompt = loadPrompt(profileName);
			user.goals = message;
			Message msg = sendTextMessage("ChatGPT друкує.....");
			String answer = gptService.sendMessage(prompt, user.toString());
			updateTextMessage(msg, answer);
		}
		}
	}

	public static void main(String[] args) throws TelegramApiException {
		Dotenv envvalues = Dotenv.load();
		telegramBotName = envvalues.get("TELEGRAM_BOT_NAME");
		telegramBotToken = envvalues.get("TELEGRAM_BOT_TOKEN");
		openAiToken = envvalues.get("CHAT_GPT_TOKEN");
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		telegramBotsApi.registerBot(new TinderBotApp());
	}
}
