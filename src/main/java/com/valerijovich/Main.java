package com.valerijovich;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;

import com.thoughtworks.xstream.XStream;

public class Main {

    private static Connection connection;
    private static Statement statement;
    // Возможно нужно resultSet сделать не объектом всего класса
    private static ResultSet resultSet;

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

        Scanner sc = new Scanner(System.in);

        connectDB();
        createTable();

        int command;
        do {
            System.out.println("Введите команду: 1 - создание, 2 - вывод информации, 3 - экспорт в XML, 4 - выход");
            command = sc.nextInt();

            switch (command) {
                case (1):
                    System.out.println("Введите имя скважины:");
                    String wellName = sc.next();
                    System.out.println("Введите количество оборудования:");
                    int equipmentAmount = sc.nextInt();
                    writeTable(wellName, equipmentAmount);
                    break;
                case (2):
                    System.out.println("Введите имена скважин, разделяя их пробелами или запятыми:");
                    // ПОГУГЛИТЬ ТЩАТЕЛЬНЕЙШИМ ОБРАЗОМ !!!
                    sc.nextLine();
                    String[] wellsName = sc.nextLine().split("[, ]");
                    readTable(wellsName);
                    break;
                case (3):
                    System.out.println("Укажите имя XML файла:");
                    String xmlFileName = sc.next();
                    createXml(xmlFileName);
                    break;
                default:
                    break;
            }
        } while (command != 4);

        closeDB();
    }

    private static void connectDB() throws ClassNotFoundException, SQLException {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    // Создание таблицы
    private static void createTable() throws SQLException {
        statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS 'Well' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' VARCHAR(32) NOT NULL UNIQUE);");
        statement.execute("CREATE TABLE IF NOT EXISTS 'Equipment' (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` VARCHAR(32) NOT NULL UNIQUE, `Well_id` INTEGER, FOREIGN KEY(`Well_id`) REFERENCES 'Well'(`id`));");
    }

    // Заполнение таблицы
    private static void writeTable(String wellName, int equipmentAmount) throws SQLException {

        int id = 0;

        resultSet = statement.executeQuery("SELECT `id` FROM `Well` WHERE `name` = '" + wellName + "';");

        while (resultSet.next())
            id = resultSet.getInt("id");

        if (id == 0) {
            // SQL инъекции
            statement.execute("INSERT INTO 'Well' (`name`) VALUES ('" + wellName + "'); ");

            resultSet = statement.executeQuery("SELECT `id` FROM `Well` WHERE `name` = '" + wellName + "';");

            while (resultSet.next())
                id = resultSet.getInt("id");
        }

        for (int i = 0; i < equipmentAmount; i++) {
            Random random = new Random();
            statement.execute("INSERT INTO Equipment (`name`, `Well_id`) VALUES ('EQ" + random.nextInt(9999) + "', '" + id + "'); ");
        }
    }

    // Вывод таблицы
    private static void readTable(String[] wellsName) throws SQLException {
        int id = 0;
        System.out.println("Имя скважины | Кол-во оборудования");
        System.out.println("==================================");
        for (String well : wellsName) {
            resultSet = statement.executeQuery("SELECT `id` FROM Well WHERE `name` = '" + well + "';");
            while (resultSet.next())
                id = resultSet.getInt("id");

            resultSet = statement.executeQuery("SELECT COUNT(`id`) AS `count` FROM Equipment WHERE `Well_id` = '" + id + "'");

            // ПОЧИТАТЬ ПРО ДЖОИНЫ И УЗНАТЬ НУЖНО ЛИ ИХ ТУТ ПРИМЕНЯТЬ
            while (resultSet.next()) {
                int count = resultSet.getInt("count");
                System.out.println(well + "                " + count);
                System.out.println("----------------------------------");
            }
        }
        System.out.println("==================================");
    }

    // Создаём файл XML из нашей БД
    private static void createXml(String xmlFileName) throws SQLException, IOException {
        Statement statementReserve = connection.createStatement();

        XStream xs = new XStream();
        String xml = "";
        DbInfo dbInfo = new DbInfo();
        List<Well> wellList = new ArrayList<>();

        xs.alias("dbinfo", DbInfo.class);
        xs.addImplicitCollection(DbInfo.class, "wellList");

        xs.alias("well", Well.class);
        xs.useAttributeFor(Well.class, "wellName");
        xs.aliasAttribute("name", "wellName");
        xs.useAttributeFor(Well.class, "wellId");
        xs.aliasAttribute("id", "wellId");
        xs.addImplicitCollection(Well.class, "equipmentList");

        xs.alias("equipment", Equipment.class);
        xs.useAttributeFor(Equipment.class, "equipmentName");
        xs.aliasAttribute("name", "equipmentName");
        xs.useAttributeFor(Equipment.class, "equipmentId");
        xs.aliasAttribute("id", "equipmentId");
        xs.omitField(Equipment.class, "equipmentWellId");

        resultSet = statement.executeQuery("SELECT * FROM Well;");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");

            Well well = new Well();
            well.setWellId(id);
            well.setWellName(name);

            ResultSet resultSetEquipment = statementReserve.executeQuery("SELECT `id`, `name` FROM Equipment WHERE `Well_id` = '" + id + "';");

            List<Equipment> equipmentList = new ArrayList<>();

            while (resultSetEquipment.next()) {
                int idEquipment = resultSetEquipment.getInt("id");
                String nameEquipment = resultSetEquipment.getString("name");

                Equipment equipment = new Equipment();
                equipment.setEquipmentId(idEquipment);
                equipment.setEquipmentName(nameEquipment);

                equipmentList.add(equipment);
            }

            well.setEquipmentList(equipmentList);
            wellList.add(well);
        }

        dbInfo.setWellList(wellList);

        xml = xml.concat(xs.toXML(dbInfo));

        Path pathXMLFile = Paths.get("" + xmlFileName + ".xml");
        Files.write(pathXMLFile, xml.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    // Закрытие соединения
    private static void closeDB() throws SQLException {
        connection.close();
        statement.close();
        resultSet.close();
    }
}
