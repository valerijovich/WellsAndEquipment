package com.valerijovich;

import com.thoughtworks.xstream.XStream;
import com.valerijovich.model.DbInfo;
import com.valerijovich.model.Equipment;
import com.valerijovich.model.Well;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBC {

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;

    public static void connectDB() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    public static void createTable() throws SQLException {
        statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS 'Well' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' VARCHAR(32) NOT NULL UNIQUE);");
        statement.execute("CREATE TABLE IF NOT EXISTS 'Equipment' (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` VARCHAR(32) NOT NULL UNIQUE, `Well_id` INTEGER, FOREIGN KEY(`Well_id`) REFERENCES 'Well'(`id`));");
    }

    public static void writeTable(String wellName, int equipmentAmount) throws SQLException {

        int id = 0;

        resultSet = statement.executeQuery("SELECT `id` FROM `Well` WHERE `name` = '" + wellName + "';");

        while (resultSet.next())
            id = resultSet.getInt("id");

        if (id == 0) {
            // SQL инъекции
            statement.execute("INSERT INTO Well (`name`) VALUES ('" + wellName + "'); ");

            resultSet = statement.executeQuery("SELECT `id` FROM Well WHERE `name` = '" + wellName + "';");

            id = resultSet.getInt("id");
        }

        resultSet = statement.executeQuery("SELECT COUNT(`id`) AS `count` FROM Equipment");

        int count = resultSet.getInt("count");

        for (int i = 1; i <= equipmentAmount; i++) {
            statement.execute("INSERT INTO Equipment (`name`, `Well_id`) VALUES ('EQ" + String.format("%04d", count + i) + "', '" + id + "'); ");
        }

        System.out.println("На скважине " + wellName + " успешно создано " + equipmentAmount + " единиц оборудования.\n");
    }

    public static void readTable(String[] wellsName) throws SQLException {
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
                System.out.println("   " + well + "             " + count);
                System.out.println("----------------------------------");
            }
        }
        System.out.println("==================================\n");
    }

    public static void createXml(String xmlFileName) throws SQLException, IOException {
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

        System.out.println("Файл " + xmlFileName + ".xml успешно создан.\n");
    }

    public static void closeDB() throws SQLException {
        connection.close();
        statement.close();
        if (resultSet != null)
            resultSet.close();
    }
}
