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

    // Подключение к БД
    public static void connectDB() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    // Создание таблиц Well и Equipment
    public static void createTable() throws SQLException
    {
        Statement statement = connection.createStatement();

        // Создаём таблицу Well со скважинами, если она ещё не создана
        statement.execute("CREATE TABLE IF NOT EXISTS Well ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' VARCHAR(32) NOT NULL UNIQUE);");
        // Создаём таблицу Equipment с оборудованием, если она ещё не создана
        statement.execute("CREATE TABLE IF NOT EXISTS Equipment (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` VARCHAR(32) NOT NULL UNIQUE, `Well_id` INTEGER, FOREIGN KEY(`Well_id`) REFERENCES 'Well'(`id`));");

        statement.close();
    }

    // Заполнение таблиц Well и Equipment
    public static void writeTable(String wellName, int equipmentAmount) throws SQLException
    {
        // Вычисляем id для скважины wellName и записываем этот id в переменную idWell
        PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT `id` FROM Well WHERE `name` = ?");
        preparedStatement1.setString(1, wellName);
        ResultSet resultSet = preparedStatement1.executeQuery();
        int idWell = 0;
        while (resultSet.next())
            idWell = resultSet.getInt("id");

        // Если idWell равен 0, то скважины wellName нет в нашей таблице Well и нужно её создать
        if (idWell == 0)
        {
            // Создаём новую скважину в таблице Well с именем wellName
            PreparedStatement preparedStatement2 = connection.prepareStatement("INSERT INTO Well (`name`) VALUES (?)");
            preparedStatement2.setString(1, wellName);
            preparedStatement2.executeUpdate();

            // Извлекаем id только что созданной скважины wellName и сохраняем в переменную idWell
            PreparedStatement preparedStatement3 = connection.prepareStatement("SELECT `id` FROM Well WHERE `name` = ?");
            preparedStatement3.setString(1, wellName);
            resultSet = preparedStatement3.executeQuery();
            idWell = resultSet.getInt("id");

            preparedStatement2.close();
            preparedStatement3.close();
        }

        // Считаем количество оборудования в таблице Equipment и сохраняем в переменную countEq
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT COUNT(`id`) AS `count` FROM Equipment");
        int countEq = resultSet.getInt("count");

        // Добавляем equipmentAmount количество раз новое оборудование в таблицу Equipment
        PreparedStatement preparedStatement4 = connection.prepareStatement("INSERT INTO Equipment (`name`, `Well_id`) VALUES (?, ?)");

        for (int i = 1; i <= equipmentAmount; i++)
        {
            String equipmentName = "EQ" + String.format("%04d", countEq + i);
            preparedStatement4.setString(1, equipmentName);
            preparedStatement4.setInt(2, idWell);
            preparedStatement4.executeUpdate();
        }

        System.out.println("На скважине " + wellName + " успешно создано " + equipmentAmount + " единиц оборудования.\n");

        preparedStatement1.close();
        preparedStatement4.close();
        statement.close();
        resultSet.close();
    }

    // Вывод таблицы (имя скважины и количество оборудования в ней)
    public static void readTable(String[] wellsName) throws SQLException
    {
        PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT `id` FROM Well WHERE `name` = ?");
        PreparedStatement preparedStatement2 = connection.prepareStatement("SELECT COUNT(`id`) AS `count` FROM Equipment WHERE `Well_id` = ?");

        System.out.println("Имя скважины | Кол-во оборудования");
        System.out.println("==================================");

        // Проходимся по списку скважин wellsName
        for (String well : wellsName)
        {
            // Вычисляем id в таблице Well для скважины well
            preparedStatement1.setString(1, well);
            ResultSet resultSet = preparedStatement1.executeQuery();
            int id = resultSet.getInt("id");

            // Считаем количество оборудования из таблицы Equipment на скважине well по её id и
            // записываем в переменную count
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

    // Создаём файл XML из нашей БД
    public static void createXml(String xmlFileName) throws SQLException, IOException
    {
        Statement statement = connection.createStatement();
        PreparedStatement preparedStatement = connection.prepareStatement
                ("SELECT `id`, `name` FROM Equipment WHERE `Well_id` = ?");

        DbInfo dbInfo = new DbInfo();
        List<Well> wellList = new ArrayList<>();
        XStream xs = new XStream();
        xs.autodetectAnnotations(true);

        // Извлекаем все поля из таблицы Well (список скважин) и сохраняем в resultSet1
        ResultSet resultSet1 = statement.executeQuery("SELECT * FROM Well;");

        // Построчно идём по списку скважин
        while (resultSet1.next())
        {
            // Сохраняем id и имя скважины в соответствующие переменные
            int id = resultSet1.getInt("id");
            String name = resultSet1.getString("name");

            // Создаём новую скважину well с текущими id и именем name
            Well well = new Well();
            well.setWellId(id);
            well.setWellName(name);

            // Находим всё оборудование из таблицы Equipment, которое установлено на скважине well
            preparedStatement.setInt(1, id);
            ResultSet resultSet2 = preparedStatement.executeQuery();

            // Создаём список, в который будем сохранять оборудование для скважины well
            List<Equipment> equipmentList = new ArrayList<>();

            // Построчно идём по списку оборудования, которое установлено на скважине well
            while (resultSet2.next())
            {
                // Извлекаем id и имя оборудования
                int idEquipment = resultSet2.getInt("id");
                String nameEquipment = resultSet2.getString("name");

                // Создаём новое оборудование с текущими idEquipment и именем nameEquipment
                Equipment equipment = new Equipment();
                equipment.setEquipmentId(idEquipment);
                equipment.setEquipmentName(nameEquipment);

                // Добавляем текущее оборудование в список
                equipmentList.add(equipment);
            }

            // Добавляем полученный список оборудования в текущий объект well
            well.setEquipmentList(equipmentList);

            // Добавляем текущий объект well в список скважин
            wellList.add(well);

            resultSet2.close();
        }

        // Добавляем список скважин в объект класса dbInfo
        dbInfo.setWellList(wellList);

        // Сериализуем объект класса dbInfo в XML строку
        String xml = xs.toXML(dbInfo);

        // Сохраняем строку xml в файл XML с именем xmlFileName
        Path pathXMLFile = Paths.get("" + xmlFileName + ".xml");
        Files.write(pathXMLFile, xml.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        System.out.println("Файл " + xmlFileName + ".xml успешно создан.\n");

        statement.close();
        preparedStatement.close();
        resultSet1.close();
    }

    // Закрытие соединения
    public static void closeDB() throws SQLException
    {
        connection.close();
    }
}
