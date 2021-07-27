package com.valerijovich;

import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Service {

    private static Connection connection;

    public static void connectDB() throws SQLException
    {
        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    public static void createTable() throws SQLException
    {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS 'Well' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' VARCHAR(32) NOT NULL UNIQUE);");
        statement.execute("CREATE TABLE IF NOT EXISTS 'Equipment' (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` VARCHAR(32) NOT NULL UNIQUE, `Well_id` INTEGER, FOREIGN KEY(`Well_id`) REFERENCES 'Well'(`id`));");

        statement.close();
    }

    public static void writeTable(String wellName, int equipmentAmount) throws SQLException
    {
        PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT `id` FROM Well WHERE `name` = ?");
        PreparedStatement preparedStatement2 = connection.prepareStatement("INSERT INTO Well (`name`) VALUES (?)");
        PreparedStatement preparedStatement3 = connection.prepareStatement("SELECT `id` FROM Well WHERE `name` = ?");
        PreparedStatement preparedStatement4 = connection.prepareStatement("INSERT INTO Equipment (`name`, `Well_id`) VALUES (?, ?)");
        Statement statement = connection.createStatement();

        int id = 0;

        preparedStatement1.setString(1, wellName);
        ResultSet resultSet = preparedStatement1.executeQuery();

        while (resultSet.next())
            id = resultSet.getInt("id");

        if (id == 0)
        {
            preparedStatement2.setString(1, wellName);
            preparedStatement2.executeUpdate();

            preparedStatement3.setString(1, wellName);
            resultSet = preparedStatement3.executeQuery();

            id = resultSet.getInt("id");
        }

        resultSet = statement.executeQuery("SELECT COUNT(`id`) AS `count` FROM Equipment");

        int count = resultSet.getInt("count");

        for (int i = 1; i <= equipmentAmount; i++)
        {
            String equipmentName = "EQ" + String.format("%04d", count + i);
            preparedStatement4.setString(1, equipmentName);
            preparedStatement4.setInt(2, id);
            preparedStatement4.executeUpdate();
        }

        System.out.println("На скважине " + wellName + " успешно создано " + equipmentAmount + " единиц оборудования.\n");

        preparedStatement1.close();
        preparedStatement2.close();
        preparedStatement3.close();
        preparedStatement4.close();
        statement.close();
        resultSet.close();
    }

    public static void readTable(String[] wellsName) throws SQLException
    {
        PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT `id` FROM Well WHERE `name` = ?");
        PreparedStatement preparedStatement2 = connection.prepareStatement("SELECT COUNT(`id`) AS `count` FROM Equipment WHERE `Well_id` = ?");

        System.out.println("Имя скважины | Кол-во оборудования");
        System.out.println("==================================");

        for (String well : wellsName)
        {
            preparedStatement1.setString(1, well);
            ResultSet resultSet = preparedStatement1.executeQuery();

            int id = resultSet.getInt("id");

            preparedStatement2.setInt(1, id);
            resultSet = preparedStatement2.executeQuery();

            while (resultSet.next()) {
                int count = resultSet.getInt("count");
                System.out.println("   " + well + "             " + count);
                System.out.println("----------------------------------");
            }

            resultSet.close();
        }
        System.out.println("==================================\n");

        preparedStatement1.close();
        preparedStatement2.close();
    }

    public static void createXml(String xmlFileName) throws SQLException, IOException
    {
        Statement statement = connection.createStatement();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT `id`, `name` FROM Equipment WHERE `Well_id` = ?");

        String xml = "";
        DbInfo dbInfo = new DbInfo();
        List<Well> wellList = new ArrayList<>();
        XStream xs = new XStream();
        xs.autodetectAnnotations(true);

        ResultSet resultSet1 = statement.executeQuery("SELECT * FROM Well;");

        while (resultSet1.next())
        {
            int id = resultSet1.getInt("id");
            String name = resultSet1.getString("name");

            Well well = new Well();
            well.setWellId(id);
            well.setWellName(name);

            preparedStatement.setInt(1, id);
            ResultSet resultSet2 = preparedStatement.executeQuery();

            List<Equipment> equipmentList = new ArrayList<>();

            while (resultSet2.next())
            {
                int idEquipment = resultSet2.getInt("id");
                String nameEquipment = resultSet2.getString("name");

                Equipment equipment = new Equipment();
                equipment.setEquipmentId(idEquipment);
                equipment.setEquipmentName(nameEquipment);

                equipmentList.add(equipment);
            }

            well.setEquipmentList(equipmentList);
            wellList.add(well);

            resultSet2.close();
        }

        dbInfo.setWellList(wellList);

        xml = xml.concat(xs.toXML(dbInfo));

        Path pathXMLFile = Paths.get("" + xmlFileName + ".xml");
        Files.write(pathXMLFile, xml.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        System.out.println("Файл " + xmlFileName + ".xml успешно создан.\n");

        statement.close();
        preparedStatement.close();
        resultSet1.close();
    }

    public static void closeDB() throws SQLException
    {
        connection.close();
    }
}
