package com.example.mygame;

import java.io.*;
import java.nio.file.*;

public class RecordManager {

    // Файл сохраняется рядом с игрой
    private static final String FILE_PATH = "record.txt";

    // Получить рекорд из файла
    public static int getRecord() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path)) {
                return 0; // Файла нет - рекорд 0
            }
            String content = Files.readString(path).trim();
            return Integer.parseInt(content);
        } catch (Exception e) {
            return 0; // Если что-то пошло не так - рекорд 0
        }
    }

    // Сохранить новый рекорд (только если он больше старого)
    public static void saveRecord(int newScore) {
        try {
            int currentRecord = getRecord();
            if (newScore > currentRecord) {
                Files.writeString(Paths.get(FILE_PATH), String.valueOf(newScore));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}