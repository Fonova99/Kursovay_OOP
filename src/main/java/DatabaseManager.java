import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mariadb://localhost:3306/MyDatabase";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    Connection getConnection() throws SQLException {
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

    //Метод по заполнению таблиц из базы данных
    private String[][] createDatabase(String query) {
        try (Connection connection = getConnection(); //подключение к базе данных
             Statement statement = connection.createStatement(); //создание объекта Statement, который будет использоваться для выполнения SQL-запросов
             ResultSet resultSet = statement.executeQuery(query)) { //выполнения SQL-запроса и запись результата

            ResultSetMetaData metaData = resultSet.getMetaData(); //получение мета данных о результате SQL-запроса
            int columnCount = metaData.getColumnCount();  //подсчет количества столбцов
            int rowCount = 0; //счётчик строк

            //подсчет количества строк
            while (resultSet.next()) {
                rowCount++;
            }

            String[][] table = new String[rowCount][columnCount]; //создание двумерного массива
            resultSet.beforeFirst();  //перемещаем курсор в начало мета-данных

            //пока данные не закончатся, записываем все значения построчно
            int rowIndex = 0; //счётчик строк
            while (resultSet.next()) {
                for (int i = 0; i < columnCount; i++) {
                    table[rowIndex][i] = resultSet.getString(i + 1); //заполнение массива данными из ResultSet. Индексация столбцов в ResultSet начинается с 1, поэтому используется i + 1
                }
                rowIndex++;
            }
            return table; //возвращаем заполненный двумерный массив

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            return new String[0][0];
        }
    }

    //Метод для обновления базы данных таблицы Doctors
    public void updateDoctors(DefaultTableModel model) {
        try (Connection connection = getConnection()) { //подключение к базе данных
            String clearDoctorsQuery = "DELETE FROM Doctors"; //очистка таблиц в базе данных
            try (PreparedStatement clearDoctorsStatement = connection.prepareStatement(clearDoctorsQuery)) {
                clearDoctorsStatement.executeUpdate(); //выполняет запрос
            }
            //вставка новых данных в таблицу Doctors
            String insertDoctorsQuery = "INSERT INTO Doctors (Name_doctor, Speciality, Office_number, Work_schedule) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertDoctorsStatement = connection.prepareStatement(insertDoctorsQuery)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    insertDoctorsStatement.setString(1, (String) model.getValueAt(i, 0));
                    insertDoctorsStatement.setString(2, (String) model.getValueAt(i, 1));
                    insertDoctorsStatement.setString(3, (String) model.getValueAt(i, 2));
                    insertDoctorsStatement.setString(4, (String) model.getValueAt(i, 3));
                    insertDoctorsStatement.executeUpdate(); //выполняет запрос
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Метод для обновления базы данных таблицы Patients
    public void updatePatients(DefaultTableModel model) {
        try (Connection connection = getConnection()) {
            String clearPatientsQuery = "DELETE FROM Patients"; //очистка таблиц в базе данных
            try (PreparedStatement clearPatientsStatement = connection.prepareStatement(clearPatientsQuery)) {
                clearPatientsStatement.executeUpdate(); //выполняет запрос
            }
            //Вставка новых данных в таблицу Patients
            String insertPatientsQuery = "INSERT INTO Patients (Doctor, Name_patient, Disease) VALUES (?, ?, ?)";
            try (PreparedStatement insertPatientsStatement = connection.prepareStatement(insertPatientsQuery)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    insertPatientsStatement.setString(1, (String) model.getValueAt(i, 0));
                    insertPatientsStatement.setString(2, (String) model.getValueAt(i, 1));
                    insertPatientsStatement.setString(3, (String) model.getValueAt(i, 2));
                    insertPatientsStatement.executeUpdate(); //выполняет запрос
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Добавление новой строки в таблицу Doctors
    public void addDoctorInfo(String doctor, String specialization, String roomNumber, String workingHours) {
        String insertDoctorQuery = "INSERT INTO Doctors (Name_doctor, Speciality, Office_number, Work_schedule) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement doctorStatement = connection.prepareStatement(insertDoctorQuery)) {  //Выполнения SQL-запроса с параметрами

            // Добавление врача
            doctorStatement.setString(1, doctor);
            doctorStatement.setString(2, specialization);
            doctorStatement.setString(3, roomNumber);
            doctorStatement.setString(4, workingHours);
            doctorStatement.executeUpdate(); //выполняет запрос

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    //Добавление новой строки в таблицу Patients
    public void addPatientInfo(String doctor, String patient, String infoDisease) {
        String insertPatientQuery = "INSERT INTO Patients (Doctor, Name_patient, Disease) VALUES (?, ?, ?)";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement patientStatement = connection.prepareStatement(insertPatientQuery)) { //Выполнения SQL-запроса с параметрами

            //Добавление пациента
            patientStatement.setString(1, doctor);
            patientStatement.setString(2, patient);
            patientStatement.setString(3, infoDisease);
            patientStatement.executeUpdate(); //выполняет запрос

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    public boolean isDoctorExists(String doctorName) {
        String query = "SELECT COUNT(*) FROM Doctors WHERE Name_doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement patientStatement = connection.prepareStatement(query)) { //Выполнения SQL-запроса с параметрами

            patientStatement.setString(1, doctorName);
            ResultSet resultSet = patientStatement.executeQuery(); //выполняет запрос

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Удаление строчки из таблицы Doctors
    public void deleteRowFromDoctors(DefaultTableModel model, int rowIndex) {
        String doctor = (String) model.getValueAt(rowIndex, 0); //получаем из таблицы значение ячейки по номеру строки и столбца
        String query = "DELETE FROM Doctors WHERE Name_doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement preparedStatement = connection.prepareStatement(query)) { //Выполнения SQL-запроса с параметрами

            preparedStatement.setString(1, doctor); //устанавливает значение вместо знака вопроса в запросе SQl (позиция, что ставить)
            preparedStatement.executeUpdate(); //выполняет запрос

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    //Удаление строчки из таблицы Patients
    public void deleteRowFromPatients(DefaultTableModel model, int rowIndex) {
        String patient = (String) model.getValueAt(rowIndex, 0); //получаем из таблицы значение ячейки по номеру строки и столбца
        String query = "DELETE FROM Patients WHERE Doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement preparedStatement = connection.prepareStatement(query)) { //Выполнения SQL-запроса с параметрами

            preparedStatement.setString(1, patient); //устанавливает значение вместо знака вопроса в запросе SQl (позиция, что ставить)
            preparedStatement.executeUpdate(); //выполняет запрос

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    //Метод по замене значений в строке таблицы Doctors
    public void changeRowFromDoctors(String doctor, String specialization, String roomNumber, String workingHours) {
        String insertDoctorQuery = "UPDATE Doctors SET Speciality  = ?, Office_number  = ?, Work_schedule = ? WHERE Name_doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement doctorStatement = connection.prepareStatement(insertDoctorQuery)) { //Выполнения SQL-запроса с параметрами

            // Добавление врача
            doctorStatement.setString(1, specialization);
            doctorStatement.setString(2, roomNumber);
            doctorStatement.setString(3, workingHours);
            doctorStatement.setString(4, doctor);
            doctorStatement.executeUpdate(); //выполняет запрос

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    //Метод по замене значений в строке таблицы Patients
    public void changeRowFromPatients(String doctor, String patient, String infoDisease) {
        String insertPatientQuery = "UPDATE Patients SET Name_patient  = ?, Disease  = ? WHERE Doctor = ?";
        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement patientStatement = connection.prepareStatement(insertPatientQuery)) { //Выполнения SQL-запроса с параметрами

            // Добавление пациента
            patientStatement.setString(1, patient);
            patientStatement.setString(2, infoDisease);
            patientStatement.setString(3, doctor);
            patientStatement.executeUpdate(); //выполняет запрос

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    //Метод по поиску всех врачей в таблице Doctors
    public List<String> searchDoctorFromDatabase() {
        List<String> doctors = new ArrayList<>(); //массив для записи результата
        String query = "SELECT Name_doctor FROM Doctors";

        try (Connection connection = getConnection(); //Создает подключение к базе данных
             Statement statement = connection.createStatement(); //создание объекта Statement, который будет использоваться для выполнения SQL-запросов
             ResultSet resultSet = statement.executeQuery(query)) { //записывает результат запроса

            //пока есть еще информация, заносим ФИО врачей в массив
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

    //Метод по поиску всех пациентов в таблице Patients
    public List<String> searchPatientsFromDatabase() {
        List<String> patients = new ArrayList<>();
        String query = "SELECT Name_patient FROM Patients";

        try (Connection connection = getConnection(); //Создает подключение к базе данных
             Statement statement = connection.createStatement(); //создание объекта Statement, который будет использоваться для выполнения SQL-запросов
             ResultSet resultSet = statement.executeQuery(query)) { //записывает результат запроса

            //пока есть еще информация, заносим ФИО пациента в массив
            while (resultSet.next()) {
                String patientName = resultSet.getString("Name_patient");
                patients.add(patientName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
        return patients;
    }

    //Метод по поиску строки в таблице Doctors
    public List<String> searchRowFromDoctors(String doctor) {
        List<String> results = new ArrayList<>();
        String query = "SELECT DISTINCT Doctors.Name_doctor, Doctors.Speciality, Doctors.Office_number, Doctors.Work_schedule " +
                "FROM Doctors " +
                "WHERE (Doctors.Name_doctor = ?)";

        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement statement = connection.prepareStatement(query)) { //Выполнения SQL-запроса с параметрами

            statement.setString(1, doctor);
            ResultSet resultSet = statement.executeQuery();  //записывает результат запроса

            while (resultSet.next()) {
                String doctorName = resultSet.getString("Name_doctor");
                String specialityName = resultSet.getString("Speciality");
                String officeNumber = resultSet.getString("Office_number");
                String workSchedule = resultSet.getString("Work_schedule");
                String resultRow = "Врач: " + doctorName + ", Специальность: " + specialityName + ", Кабинет: " + officeNumber + ", График работы: " + workSchedule;
                results.add(resultRow);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
        return results;
    }

    //Метод по поиску строки в таблице Patients
    public List<String> searchRowFromPatients(String patients, String disease) {
        List<String> results = new ArrayList<>();
        String query = "SELECT DISTINCT Patients.Doctor, Patients.Name_patient, Patients.Disease " +
                "FROM Patients " +
                "WHERE (Patients.Name_patient = ? OR Patients.Disease = ?)";

        try (Connection connection = getConnection(); //Создает подключение к базе данных
             PreparedStatement statement = connection.prepareStatement(query)) { //Выполнения SQL-запроса с параметрами

            statement.setString(1, patients);
            statement.setString(2, disease);
            ResultSet resultSet = statement.executeQuery();  //записывает результат запроса

            while (resultSet.next()) {
                String doctorName = resultSet.getString("Doctor");
                String patientName = resultSet.getString("Name_patient");
                String patientDisease = resultSet.getString("Disease");
                String resultRow = "Врач: " + doctorName + ", Пациент: " + patientName + ", Заболевание: " + patientDisease;
                results.add(resultRow);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
        return results;
    }
}
