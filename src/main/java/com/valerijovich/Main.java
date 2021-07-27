package com.valerijovich;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws SQLException, IOException, NullPointerException {

        JDBC jdbc = new JDBC();

        Scanner sc = new Scanner(System.in);

        // Подключение к БД
        jdbc.connectDB();
        // Созданием таблиц
        jdbc.createTable();

        int command;

        do {
            System.out.println("1 - Создание оборудования на скважине.");
            System.out.println("2 - Вывод общей информации об оборудовании на скважинах.");
            System.out.println("3 - Экспорт всех данных в XML файл.");
            System.out.println("4 - Выход.");
            System.out.println("Введите команду:");
            command = sc.nextInt();

            switch (command) {
                case (1):
                    System.out.println("Введите имя скважины:");
                    String wellName = sc.next();
                    System.out.println("Введите количество оборудования:");
                    int equipmentAmount = sc.nextInt();
                    // Заполнение таблиц
                    jdbc.writeTable(wellName, equipmentAmount);
                    break;
                case (2):
                    System.out.println("Введите имена скважин, разделяя их пробелами или запятыми:");
                    // ПОГУГЛИТЬ ТЩАТЕЛЬНЕЙШИМ ОБРАЗОМ !!!
                    sc.nextLine();
                    String[] wellsName = sc.nextLine().split("[, ]");
                    // Вывод таблицы
                    jdbc.readTable(wellsName);
                    break;
                case (3):
                    System.out.println("Укажите имя XML файла:");
                    String xmlFileName = sc.next();
                    // Создаём файл XML из нашей БД
                    jdbc.createXml(xmlFileName);
                    break;
                case (4):
                    break;
                default:
                    System.out.println("Неверный код команды.\n");
                    break;
            }
        } while (command != 4);

        // Закрытие соединения
        jdbc.closeDB();
    }
}
