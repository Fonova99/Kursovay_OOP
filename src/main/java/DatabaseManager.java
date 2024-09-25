import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mariadb://localhost:3306/MyDatabase";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public String[][] createTableFromDatabase() {
        String query = "SELECT * FROM Administration";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             Statement statement = connection.createStatement(); //Создает объект Statement для выполнения SQL-запросов
             ResultSet resultSet = statement.executeQuery(query)) { //Выполняет SQL-запрос и возвращает результат в виде ResultSet

            ResultSetMetaData metaData = resultSet.getMetaData(); //Возвращение мета-данных по SQL-запросу

            int columnCount = metaData.getColumnCount(); //Возвращает количества столбцов
            int rowCount = 0;

            while (resultSet.next()) { //Пока, при перемещении курсора в начало есть строка, записывается количество строк
                rowCount++;
            }

            String[][] table = new String[rowCount][columnCount]; //Создаем таблицу данных
            resultSet.beforeFirst(); // Возврат курсора в начало

            int rowIndex = 0;
            while (resultSet.next()) {
                for (int i = 0; i < columnCount; i++) {
                    table[rowIndex][i] = resultSet.getString(i + 1);
                }
                rowIndex++;
            }
            return table;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            return new String[0][0];
        }
    }

    public void addInfo(String doctor, String specialization, String roomNumber, String workingHours, String patient, String infoDisease) {
        String insertDoctorQuery = "INSERT INTO Doctors (Name_doctor, Speciality, Office_number, Work_schedule) VALUES (?, ?, ?, ?)";
        String insertPatientQuery = "INSERT INTO Patients (Doctor, Name_patient, Disease) VALUES (?, ?, ?)";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement doctorStatement = connection.prepareStatement(insertDoctorQuery);
             PreparedStatement patientStatement = connection.prepareStatement(insertPatientQuery)) {

            // Добавление врача
            doctorStatement.setString(1, doctor);
            doctorStatement.setString(2, specialization);
            doctorStatement.setString(3, roomNumber);
            doctorStatement.setString(4, workingHours);
            doctorStatement.executeUpdate();

            // Добавление пациента
            patientStatement.setString(1, doctor);
            patientStatement.setString(2, patient);
            patientStatement.setString(3, infoDisease);
            patientStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    public void deleteRowFromDatabase(DefaultTableModel model, int rowIndex) {
        String doctor = (String) model.getValueAt(rowIndex, 0); //получаем из таблицы значение ячейки по номеру строки и столбца
        String query = "DELETE FROM Patients WHERE Doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, doctor); //устанавливает значение вместо знака вопроса в запросе SQl (позиция, что ставить)
            preparedStatement.executeUpdate(); //приводить в действие SQL-запрос

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    public void changeRowFromDatabase(String doctor, String specialization, String roomNumber, String workingHours, String patient, String infoDisease) {
        String insertDoctorQuery = "UPDATE Doctors SET Speciality  = ?, Office_number  = ?, Work_schedule = ? WHERE Name_doctor = ?";
        String insertPatientQuery = "UPDATE Patients SET Name_patient  = ?, Disease  = ? WHERE Doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement doctorStatement = connection.prepareStatement(insertDoctorQuery);
             PreparedStatement patientStatement = connection.prepareStatement(insertPatientQuery)) {

            // Добавление врача
            doctorStatement.setString(1, specialization);
            doctorStatement.setString(2, roomNumber);
            doctorStatement.setString(3, workingHours);
            doctorStatement.setString(4, doctor);
            doctorStatement.executeUpdate();

            // Добавление пациента
            patientStatement.setString(1, patient);
            patientStatement.setString(2, infoDisease);
            patientStatement.setString(3, doctor);
            patientStatement.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    public List<String> searchDoctorFromDatabase() {
        List<String> doctors = new ArrayList<>();

        String query = "SELECT Name_doctor FROM Doctors";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String doctorName = resultSet.getString("Name_doctor");
                doctors.add(doctorName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
        return doctors;
    }

    public List<String> searchSpecialityFromDatabase() {
        List<String> specialties = new ArrayList<>();

        String query = "SELECT Speciality FROM Doctors";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String doctorName = resultSet.getString("Speciality");
                specialties.add(doctorName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
        return specialties;
    }

    public List<String> searchRowFromDatabase(String doctor, String speciality, String disease) {
        List<String> results = new ArrayList<>();

        String query = "SELECT DISTINCT Doctors.Name_doctor, Doctors.Speciality, Doctors.Office_number, Doctors.Work_schedule, Patients.Name_patient, Patients.Disease " +
                "FROM Doctors " +
                "JOIN Patients ON Doctors.Name_doctor = Patients.Doctor " +
                "WHERE (Doctors.Name_doctor = ? OR Doctors.Speciality = ? OR Patients.Disease = ?)";

        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, doctor);
            statement.setString(2, speciality);
            statement.setString(3, disease);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String doctorName = resultSet.getString("Name_doctor");
                String specialityName = resultSet.getString("Speciality");
                String officeNumber = resultSet.getString("Office_number");
                String workSchedule = resultSet.getString("Work_schedule");
                String patientName = resultSet.getString("Name_patient");
                String patientDisease = resultSet.getString("Disease");
                String resultRow = "Врач: " + doctorName + ", Специальность: " + specialityName + ", Офис: " + officeNumber + ", График работы: " + workSchedule + ", Пациент: " + patientName + ", Заболевание: " + patientDisease;
                results.add(resultRow);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
        return results;
    }
}
