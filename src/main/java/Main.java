import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class Main {

    private static Connection connection;
    private static Statement statement;
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
    private static void createTable() throws SQLException
    {
        statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS 'Well' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' VARCHAR(32) NOT NULL UNIQUE);");
        statement.execute("CREATE TABLE IF NOT EXISTS 'Equipment' (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` VARCHAR(32) NOT NULL UNIQUE, `Well_id` INTEGER, FOREIGN KEY(`Well_id`) REFERENCES 'Well'(`id`));");
    }

    // Заполнение таблицы
    private static void writeTable(String wellName, int equipmentAmount) throws SQLException
    {

        int id = 0;

        resultSet = statement.executeQuery("SELECT `id` FROM `Well` WHERE `name` = '"+ wellName +"';");

        while(resultSet.next())
            id = resultSet.getInt("id");

        if (id == 0) {
            // SQL инъекции
            statement.execute("INSERT INTO 'Well' (`name`) VALUES ('" + wellName + "'); ");

            resultSet = statement.executeQuery("SELECT `id` FROM `Well` WHERE `name` = '"+ wellName +"';");

            while(resultSet.next())
                id = resultSet.getInt("id");
        }

        for (int i = 0; i < equipmentAmount; i++) {
            Random random = new Random();
            statement.execute("INSERT INTO 'Equipment' (`name`, `Well_id`) VALUES ('EQ" + random.nextInt(9999) + "', '"+ id +"'); ");
        }
    }

    // Вывод таблицы
    private static void readTable(String[] wellsName) throws SQLException
    {
        int id = 0;
        System.out.println("Имя скважины | Кол-во оборудования");
        System.out.println("==================================");
        for (String well: wellsName) {
            resultSet = statement.executeQuery("SELECT `id` FROM `Well` WHERE `name` = '"+ well +"';");
            while(resultSet.next())
                id = resultSet.getInt("id");

            resultSet = statement.executeQuery("SELECT COUNT(`id`) AS `count` FROM `Equipment` WHERE `Well_id` = '" + id + "'");

            // ПОЧИТАТЬ ПРО ДЖОИНЫ И УЗНАТЬ НУЖНО ЛИ ИХ ТУТ ПРИМЕНЯТЬ
            while(resultSet.next())
            {
                int count = resultSet.getInt("count");
                System.out.println(well + "                " + count);
                System.out.println("----------------------------------");
            }
        }
        System.out.println("==================================");
    }

    // Создаём файл XML из нашей БД
    private static void createXml(String xmlFileName) throws SQLException, IOException {
//        int count = 0;
//        resultSet = statement.executeQuery("SELECT COUNT(`id`) AS `count` FROM Well");
//        while(resultSet.next())
//            count = resultSet.getInt("count");

        ObjectMapper mapper = new XmlMapper();

        resultSet = statement.executeQuery("SELECT * FROM Well");
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int count = rsmd.getColumnCount();

//        for (int i = 0; i < count; i++) {
//            while(resultSet.next()){
//                int id = resultSet.getInt("id");
//                String name = resultSet.getString("name");
//                System.out.println(id);
//                System.out.println(name);
//            }
//        }

        mapper.writeValue(new File("test.xml"), resultSetToArrayList(resultSet));
    }

    // Закрытие соединения
    private static void closeDB() throws SQLException
    {
        connection.close();
        statement.close();
        resultSet.close();
    }

    public static List resultSetToArrayList(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        ArrayList list = new ArrayList();
        while (rs.next()){
            HashMap row = new HashMap(columns);
            for(int i=1; i<=columns; ++i){
                row.put(md.getColumnName(i),rs.getObject(i));
            }
            list.add(row);
        }

        return list;
    }
}
