package org.example;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto; // Добавлен импорт для SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "7047045728:AAGhMnyn9rDX9f9JLXHsi3kbG_XovfZlvXk";
    private static final String BOT_USERNAME = "saienestbot";

    private final Map<String, AppInfo> appsData = new HashMap<>();

    public Main() {
        // Инициализация данных о приложениях
        appsData.put("app1", new AppInfo(
                "Dailybox",
                "Повысьте свою производительность и самочувствие с Dailybox.\n" +
                        "\n" +
                        " • Создавайте цели: мгновенно получайте свежие персонализированные задачи одним нажатием.\n" +
                        "\n" +
                        " • Отслеживайте свой прогресс: легко отмечайте выполненные цели и наблюдайте, как растут ваши ежедневные и еженедельные достижения.\n" +
                        "\n" +
                        " • Сохраняйте мотивацию: отмечайте свои вехи с помощью понятных сводок прогресса и вдохновляющих сообщений.\n" +
                        "\n" +
                        " • Достигайте большего: вырабатывайте позитивные привычки и достигайте новых высот на своем личном пути.",
                "src/main/resources/Dailybox v1.0.apk",
                "https://sites.google.com/view/yaroslav-we/",
                "src/main/resources/dailybox_preview1.jpg"
        ));

        appsData.put("app2", new AppInfo(
                "Exolute Converter",
                "Приложение для конвертации валют.\n" +
                        "\n • Поддерживает все валюты мира" +
                        "\n • Поддержка 5-ти языков" +
                        "\n • Можно настроить валюты по умолчанию" +
                        "\n • История последних запросов" +
                        "\n • Удобный интерфейс" +
                        "\n •  И многое другое.." +
                        "\n\n • Скачать можно ниже",
                "src/main/resources/Exolute v2.26.apk",
                "https://sites.google.com/view/yaroslav-we/",
                "src/main/resources/exolute_preview1.jpg"
        ));
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start") || messageText.equals("/menu")) {
                sendAppSelectionMenu(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callbackData.startsWith("app_")) {
                String appId = callbackData.substring(4);
                sendAppDetails(chatId, messageId, appId);
            }
        }
    }

    private void sendAppSelectionMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выбери приложение из списка ниже чтобы узнать подробнее о нем или скачать.");

        InlineKeyboardMarkup markupInline = getInlineKeyboardMarkup();
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private InlineKeyboardMarkup getInlineKeyboardMarkup() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (Map.Entry<String, AppInfo> entry : appsData.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(entry.getValue().getName());
            button.setCallbackData("app_" + entry.getKey());
            rowInline.add(button);
        }
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private void sendAppDetails(long chatId, int messageIdToDelete, String appId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageIdToDelete);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        AppInfo appInfo = appsData.get(appId);
        if (appInfo == null) {
            SendMessage errorMessage = new SendMessage();
            errorMessage.setChatId(String.valueOf(chatId));
            errorMessage.setText("К сожалению, этого приложения нет.");
            try {
                execute(errorMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        // Отправляем фото превью, если оно есть
        if (appInfo.getImagePath() != null && !appInfo.getImagePath().isEmpty()) {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(String.valueOf(chatId));
            File photoFile = new File(appInfo.getImagePath());
            if (photoFile.exists()) {
                sendPhoto.setPhoto(new InputFile(photoFile));
                try {
                    execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    SendMessage errorMessage = new SendMessage();
                    errorMessage.setChatId(String.valueOf(chatId));
                    errorMessage.setText("Произошла ошибка при отправке фото превью.");
                    try {
                        execute(errorMessage);
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setChatId(String.valueOf(chatId));
                errorMessage.setText("Файл фото превью не найден по указанному пути.");
                try {
                    execute(errorMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        SendMessage descriptionMessage = new SendMessage();
        descriptionMessage.setChatId(String.valueOf(chatId));
        descriptionMessage.setText("*" + appInfo.getName() + "*\n\n" + appInfo.getDescription());
        descriptionMessage.setParseMode("Markdown");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        if (appInfo.getWebsiteUrl() != null && !appInfo.getWebsiteUrl().isEmpty()) {
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton websiteButton = new InlineKeyboardButton();
            websiteButton.setText("Посетить сайт разработчика");
            websiteButton.setUrl(appInfo.getWebsiteUrl());
            rowInline.add(websiteButton);
            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);
        }

        try {
            execute(descriptionMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));
        File apkFile = new File(appInfo.getApkFilePath());
        if (apkFile.exists()) {
            sendDocument.setDocument(new InputFile(apkFile));
            sendDocument.setCaption("Скачать " + appInfo.getName() + " APK");
            if (appInfo.getWebsiteUrl() != null && !appInfo.getWebsiteUrl().isEmpty()) {
                sendDocument.setReplyMarkup(markupInline);
            }
            try {
                execute(sendDocument);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                SendMessage errorMessage = new SendMessage();
                errorMessage.setChatId(String.valueOf(chatId));
                errorMessage.setText("Произошла ошибка при отправке файла APK.");
                try {
                    execute(errorMessage);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            SendMessage errorMessage = new SendMessage();
            errorMessage.setChatId(String.valueOf(chatId));
            errorMessage.setText("Файл APK не найден по указанному пути.");
            try {
                execute(errorMessage);
                // Отправляем сообщение об ошибке, если APK не найден
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private static class AppInfo {
        private String name;
        private String description;
        private String apkFilePath;
        private String websiteUrl;
        private String imagePath; // Добавлено поле для пути к фото

        public AppInfo(String name, String description, String apkFilePath, String websiteUrl, String imagePath) {
            this.name = name;
            this.description = description;
            this.apkFilePath = apkFilePath;
            this.websiteUrl = websiteUrl;
            this.imagePath = imagePath;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getApkFilePath() { return apkFilePath; }
        public String getWebsiteUrl() { return websiteUrl; }
        public String getImagePath() { return imagePath; } // Геттер для пути к фото
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Main());
            System.out.println("Бот запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}