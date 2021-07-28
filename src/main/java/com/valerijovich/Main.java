package com.valerijovich;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws SQLException, IOException, NullPointerException, ClassNotFoundException {

        Scanner scanner = new Scanner(System.in);

        // Подключение к БД
        Service.connectDB();
        // Создание таблиц Well и Equipment
        Service.createTable();

        // Переменная для номера команды из меню управления
        int command = 0;

        // Командное меню управления через консоль
        do {
            try {
                System.out.println("1 - Создание оборудования на скважине.");
                System.out.println("2 - Вывод общей информации об оборудовании на скважинах.");
                System.out.println("3 - Экспорт всех данных в XML файл.");
                System.out.println("4 - Выход.");

                System.out.println("Введите команду:");
                command = scanner.nextInt();

                switch (command) {
                    case (1):
                        System.out.println("Введите имя скважины:");
                        String wellName = scanner.next();
                        System.out.println("Введите количество оборудования:");
                        int equipmentAmount = scanner.nextInt();
                        // Заполнение таблиц Well и Equipment
                        Service.writeTable(wellName, equipmentAmount);
                        break;
                    case (2):
                        System.out.println("Введите имена скважин, разделяя их пробелами или запятыми:");
                        scanner.nextLine();
                        String[] wellsName = scanner.nextLine().split("[, ]");
                        // Вывод таблицы (имя скважины и количество оборудования в ней)
                        Service.readTable(wellsName);
                        break;
                    case (3):
                        System.out.println("Укажите имя XML файла:");
                        String xmlFileName = scanner.next();
                        // Создаём файл XML из нашей БД
                        Service.createXml(xmlFileName);
                        break;
                    case (4):
                        break;
                    default:
                        System.out.println("Неверный код команды.\n");
                        break;
                }
            }
            catch (InputMismatchException ex) {
                System.out.println("Неверный код команды.\n");
                scanner.nextLine();
            }
        } while (command != 4);

        // Закрытие соединения
        Service.closeDB();
    }
}
