import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mariadb://localhost:3306/MyDatabase";
    private static final String USER = "root";
    private static final String PASSWORD = "Fononita#40";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public String[][] createTableFromDoctors() {
        String query = "SELECT * FROM Doctors";
        return createDatabase(query);
    }

    public String[][] createTableFromPatients() {
        String query = "SELECT * FROM Patients";
        return createDatabase(query);
    }
    private String[][] createDatabase(String query) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            int rowCount = 0;

            while (resultSet.next()) {
                rowCount++;
            }

            String[][] table = new String[rowCount][columnCount];
            resultSet.beforeFirst();

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

    public void updateDoctors(DefaultTableModel model) {
        try (Connection connection = getConnection()) {
            // Очистка таблиц в базе данных
            String clearDoctorsQuery = "DELETE FROM Doctors";
            try (PreparedStatement clearDoctorsStatement = connection.prepareStatement(clearDoctorsQuery)) {
                clearDoctorsStatement.executeUpdate();
            }
            // Вставка новых данных в таблицу Doctors
            String insertDoctorsQuery = "INSERT INTO Doctors (Name_doctor, Speciality, Office_number, Work_schedule) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertDoctorsStatement = connection.prepareStatement(insertDoctorsQuery)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    insertDoctorsStatement.setString(1, (String) model.getValueAt(i, 0));
                    insertDoctorsStatement.setString(2, (String) model.getValueAt(i, 1));
                    insertDoctorsStatement.setString(3, (String) model.getValueAt(i, 2));
                    insertDoctorsStatement.setString(4, (String) model.getValueAt(i, 3));
                    insertDoctorsStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePatients(DefaultTableModel model) {
        try (Connection connection = getConnection()) {
            // Очистка таблиц в базе данных
            String clearPatientsQuery = "DELETE FROM Patients";
            try (PreparedStatement clearPatientsStatement = connection.prepareStatement(clearPatientsQuery)) {
                clearPatientsStatement.executeUpdate();
            }
            // Вставка новых данных в таблицу Patients
            String insertPatientsQuery = "INSERT INTO Patients (Doctor, Name_patient, Disease) VALUES (?, ?, ?)";
            try (PreparedStatement insertPatientsStatement = connection.prepareStatement(insertPatientsQuery)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    insertPatientsStatement.setString(1, (String) model.getValueAt(i, 0));
                    insertPatientsStatement.setString(2, (String) model.getValueAt(i, 1));
                    insertPatientsStatement.setString(3, (String) model.getValueAt(i, 2));
                    insertPatientsStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDoctorInfo(String doctor, String specialization, String roomNumber, String workingHours) {
        String insertDoctorQuery = "INSERT INTO Doctors (Name_doctor, Speciality, Office_number, Work_schedule) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement doctorStatement = connection.prepareStatement(insertDoctorQuery)) {

            // Добавление врача
            doctorStatement.setString(1, doctor);
            doctorStatement.setString(2, specialization);
            doctorStatement.setString(3, roomNumber);
            doctorStatement.setString(4, workingHours);
            doctorStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    public void addPatientInfo(String doctor, String patient, String infoDisease) {
        String insertPatientQuery = "INSERT INTO Patients (Doctor, Name_patient, Disease) VALUES (?, ?, ?)";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement patientStatement = connection.prepareStatement(insertPatientQuery)) {

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

    public boolean isDoctorExists(String doctorName) {
        String query = "SELECT COUNT(*) FROM Doctors WHERE Name_doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement patientStatement = connection.prepareStatement(query)) {
            patientStatement.setString(1, doctorName);
            ResultSet rs = patientStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteRowFromDoctors(DefaultTableModel model, int rowIndex) {
        String doctor = (String) model.getValueAt(rowIndex, 0); //получаем из таблицы значение ячейки по номеру строки и столбца
        String query = "DELETE FROM Doctors WHERE Name_doctor = ?";
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

    public void deleteRowFromPatients(DefaultTableModel model, int rowIndex) {
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

    public void changeRowFromDoctors(String doctor, String specialization, String roomNumber, String workingHours) {
        String insertDoctorQuery = "UPDATE Doctors SET Speciality  = ?, Office_number  = ?, Work_schedule = ? WHERE Name_doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement doctorStatement = connection.prepareStatement(insertDoctorQuery)) {

            // Добавление врача
            doctorStatement.setString(1, specialization);
            doctorStatement.setString(2, roomNumber);
            doctorStatement.setString(3, workingHours);
            doctorStatement.setString(4, doctor);
            doctorStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }
    public void changeRowFromPatients(String doctor, String patient, String infoDisease) {
        String insertPatientQuery = "UPDATE Patients SET Name_patient  = ?, Disease  = ? WHERE Doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             // Выполнения SQL-запроса с параметрами
             PreparedStatement patientStatement = connection.prepareStatement(insertPatientQuery)) {

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
//
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

            ResultSet resultSet = statement.executeQuery();  //записывает результат запроса

            while (resultSet.next()) {
                String doctorName = resultSet.getString("Name_doctor");
                String specialityName = resultSet.getString("Speciality");
                String officeNumber = resultSet.getString("Office_number");
                String workSchedule = resultSet.getString("Work_schedule");
                String patientName = resultSet.getString("Name_patient");
                String patientDisease = resultSet.getString("Disease");
                String resultRow = "Врач: " + doctorName + ", Специальность: " + specialityName + ", Кабинет: " + officeNumber + ", График работы: " + workSchedule + ", Пациент: " + patientName + ", Заболевание: " + patientDisease;
                results.add(resultRow);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
        return results;
    }
}
