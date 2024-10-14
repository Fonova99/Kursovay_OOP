import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class DatabaseManagerTest {

    private DatabaseManager databaseManager;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private DefaultTableModel mockTableModel;

    //Метод инициализации. Будет выполняться перед каждым последующим методом
    @BeforeMethod
    public void setUp() throws SQLException {
        databaseManager = Mockito.spy(new DatabaseManager());  //Создание объекта-шпиона класса DatabaseManager для тестирования
        //Создаем виртуальные объекты
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockTableModel = mock(DefaultTableModel.class);

        //Мокируем создание подключения
        doReturn(mockConnection).when(databaseManager).getConnection(); //вернуть объект mockConnection, когда вызывается метод databaseManager.getConnection()
        doReturn(mockPreparedStatement).when(mockConnection).prepareStatement(anyString()); //вернуть объект mockPreparedStatement, когда вызывается метод mockConnection.prepareStatement(anyString()
    }

    @Test
    public void testAddDoctorInfo() throws SQLException {
        String doctor = "Dr. Smith";
        String specialization = "Cardiologist";
        String roomNumber = "101";
        String workingHours = "9:00-17:00";

        //Вызываем метод
        databaseManager.addDoctorInfo(doctor, specialization, roomNumber, workingHours);

        //Проверяем, что был вызван соответствующий SQL-запрос
        verify(mockConnection).prepareStatement("INSERT INTO Doctors (Name_doctor, Speciality, Office_number, Work_schedule) VALUES (?, ?, ?, ?)");
        verify(mockPreparedStatement).setString(1, doctor);
        verify(mockPreparedStatement).setString(2, specialization);
        verify(mockPreparedStatement).setString(3, roomNumber);
        verify(mockPreparedStatement).setString(4, workingHours);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    public void testAddPatientInfo() throws SQLException {
        String doctor = "Dr. Smith";
        String patient = "John Doe";
        String disease = "Flu";

        //Вызываем метод
        databaseManager.addPatientInfo(doctor, patient, disease);

        //Проверяем, что был вызван соответствующий SQL-запрос
        verify(mockConnection).prepareStatement("INSERT INTO Patients (Doctor, Name_patient, Disease) VALUES (?, ?, ?)");
        verify(mockPreparedStatement).setString(1, doctor);
        verify(mockPreparedStatement).setString(2, patient);
        verify(mockPreparedStatement).setString(3, disease);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    public void testDeleteRowFromDoctors() throws SQLException {
        int rowIndex = 0;
        String doctorName = "Dr. Smith";

        //Мокируем поведение модели таблицы
        doReturn(doctorName).when(mockTableModel).getValueAt(rowIndex, 0);

        //Вызываем метод
        databaseManager.deleteRowFromDoctors(mockTableModel, rowIndex);

        // Проверяем, что был вызван соответствующий SQL-запрос
        verify(mockConnection).prepareStatement("DELETE FROM Doctors WHERE Name_doctor = ?");
        verify(mockPreparedStatement).setString(1, doctorName);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    public void testDeleteRowFromPatients() throws SQLException {
        int rowIndex = 0;
        String doctorName = "Dr. Smith";

        //Мокируем поведение модели таблицы
        doReturn(doctorName).when(mockTableModel).getValueAt(rowIndex, 0);

        //Вызываем метод
        databaseManager.deleteRowFromPatients(mockTableModel, rowIndex);

        //Проверяем, что был вызван соответствующий SQL-запрос
        verify(mockConnection).prepareStatement("DELETE FROM Patients WHERE Doctor = ?");
        verify(mockPreparedStatement).setString(1, doctorName);
        verify(mockPreparedStatement).executeUpdate();
    }
}
