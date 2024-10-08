import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class Application {
    private JFrame pcAdmin;
    private JTable tableDoctors;
    private JTable tablePatients;
    private DefaultTableModel modelDoctors;
    private DefaultTableModel modelPatients;
    private JScrollPane scrollDoctors;
    private JScrollPane scrollPatients;
    private JButton addButton;
    private JButton changeButton;
    private JButton removeButton;
    private JButton searchButton;
    private JButton saveButton;
    private JButton openButton;
    private JButton saveXmlButton;
    private JButton openXmlButton;
    private JToolBar toolBar;
    private JTextField disease;
    private JComboBox doctor, speciality;
    private JPanel filterPanel;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private final TextException exception = new TextException();
    private boolean noException = true;
    private final XmlFile xml = new XmlFile();
    private final DocFile docFile = new DocFile();
    private final DatabaseManager db = new DatabaseManager();
    private int selectedRow = -1;

    public void show() {
        createWindow();
        createTables();
        createButtons();
        createToolbar();
        createSearchComponents();
        placeComponents();
        pcAdmin.setVisible(true);
    }

    private void createWindow() {
        pcAdmin = new JFrame("Администратор поликлиники");
        pcAdmin.setSize(1200, 500);
        pcAdmin.setLocation(100, 100);
        pcAdmin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createTables() {
        // Создаем две таблицы
        String[] columns1 = {"ФИО врача", "Специализация", "Номер кабинета", "График работы"};
        modelDoctors = new DefaultTableModel(new String[0][0], columns1);
        tableDoctors = new JTable(modelDoctors);
        scrollDoctors = new JScrollPane(tableDoctors);

        String[] columns2 = {"ФИО врача", "ФИО пациента", "Диагноз"};
        modelPatients = new DefaultTableModel(new String[0][0], columns2);
        tablePatients = new JTable(modelPatients);
        scrollPatients = new JScrollPane(tablePatients);

        // Добавляем таблицы на JTabbedPane
        tabbedPane.addTab("Врачи", scrollDoctors);
        tabbedPane.addTab("Пациенты", scrollPatients);

        // Добавляем JTabbedPane на форму
        pcAdmin.add(tabbedPane, BorderLayout.CENTER);

        // Загрузка данных для таблицы Doctors
        String[][] doctorsData = db.createTableFromDoctors();
        modelDoctors.setDataVector(doctorsData, new String[]{"ФИО врача", "Специализация", "Номер кабинета", "График работы"});

        // Загрузка данных для таблицы Patients
        String[][] patientsData = db.createTableFromPatients();
        modelPatients.setDataVector(patientsData, new String[]{"ФИО врача", "ФИО пациента", "Диагноз"});
    }

    private void createButtons() {
        addButton = new JButton("Добавить");
        changeButton = new JButton("Изменить");
        removeButton = new JButton("Удалить");
        searchButton = new JButton("Поиск");
        saveButton = new JButton("Сохранить");
        openButton = new JButton("Загрузить");
        saveXmlButton = new JButton("Сохранить Xml-файл");
        openXmlButton = new JButton("Загрузить Xml-файл");

        addButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                showAddDoctorInfoFromDatabase();
            } else if (selectedIndex == 1) {
                showAddPatientInfoFromDatabase();
            }
        });

        changeButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                clickMouseForDoctors();
                if (selectedRow != -1) {
                    changeInfoFromDoctors(selectedRow);
                    selectedRow = -1;
                } else {
                    JOptionPane.showMessageDialog(null, "Выберите строку для изменения");
                }
            } else if (selectedIndex == 1) {
                clickMouseForPatients();
                if (selectedRow != -1) {
                    changeInfoFromPatients(selectedRow);
                    selectedRow = -1;
                } else {
                    JOptionPane.showMessageDialog(null, "Выберите строку для изменения");
                }
            }
        });

        removeButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                clickMouseForDoctors();
                if (selectedRow != -1) {
                    db.deleteRowFromDoctors(modelDoctors, selectedRow);
                    modelDoctors.removeRow(selectedRow);
                    selectedRow = -1;
                    loadPatientsDataFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(null, "Выберите строку для удаления");
                }
                loadDoctorsDataFromDatabase();
            } else if (selectedIndex == 1) {
                clickMouseForPatients();
                if (selectedRow != -1) {
                    db.deleteRowFromPatients(modelPatients, selectedRow);
                    modelPatients.removeRow(selectedRow);
                    selectedRow = -1;
                } else {
                    JOptionPane.showMessageDialog(null, "Выберите строку для удаления");
                }
                loadPatientsDataFromDatabase();
            }
        });

        searchButton.addActionListener(e -> searchButtonFromDatabase());

        saveButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                docFile.saveDoctorsToFile(pcAdmin, modelDoctors);
            } else if (selectedIndex == 1) {
                docFile.savePatientsToFile(pcAdmin, modelPatients);
            }
        });

        openButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                docFile.loadDoctorsFromFile(pcAdmin, modelDoctors);
                db.updateDoctors(modelDoctors);
            } else if (selectedIndex == 1) {
                docFile.loadPatientsFromFile(pcAdmin, modelPatients, modelDoctors); // Pass doctorModel if needed
                db.updatePatients(modelPatients);
            }
        });

        saveXmlButton.addActionListener(e -> new Thread(new Runnable() {
            @Override
            public void run() {
                int selectedIndex = tabbedPane.getSelectedIndex();
                if (selectedIndex == 0) {
                    xml.recordXmlFileDoctors(modelDoctors);
                } else if (selectedIndex == 1) {
                    xml.recordXmlFilePatients(modelPatients);
                }
            }
        }).start());

        openXmlButton.addActionListener(e -> new Thread(new Runnable() {
            @Override
            public void run() {
                int selectedIndex = tabbedPane.getSelectedIndex();
                if (selectedIndex == 0) {
                    xml.readXmlFileDoctors(modelDoctors);
                } else if (selectedIndex == 1) {
                    xml.readXmlFilePatients(modelPatients, modelDoctors);
                }
            }
        }).start());

        addButton.setToolTipText("Добавить информацию");
        changeButton.setToolTipText("Изменить информацию");
        removeButton.setToolTipText("Удалить информацию");
        searchButton.setToolTipText("Поиск информации");
        saveButton.setToolTipText("Сохранить данные");
        openButton.setToolTipText("Загрузить данные");
        saveXmlButton.setToolTipText("Сохранить Xml-файл");
        openXmlButton.setToolTipText("Загрузить Xml-файл");
    }

    private void createToolbar() {
        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(addButton);
        toolBar.add(changeButton);
        toolBar.add(removeButton);
        toolBar.add(saveButton);
        toolBar.add(openButton);
        toolBar.add(saveXmlButton);
        toolBar.add(openXmlButton);
    }

    private void doctorBox() {
        doctor = new JComboBox();
        List<String> doctors = db.searchDoctorFromDatabase();
        // Добавляем пункт "Врач" в начало списка
        doctor.insertItemAt("Врач", 0);
        doctor.setSelectedIndex(0);
        // Добавляем остальные пункты
        for (String name : doctors) {
            doctor.addItem(name);
        }
    }

    private void specialityBox() {
        speciality = new JComboBox();
        List<String> specialties = db.searchSpecialityFromDatabase();
        // Добавляем пункт "Специальность" в начало списка
        speciality.insertItemAt("Специальность", 0);
        speciality.setSelectedIndex(0);
        // Добавляем остальные пункты
        for (String name : specialties) {
            speciality.addItem(name);
        }
    }

    private void createSearchComponents() {
        doctorBox();
        specialityBox();
        disease = new JTextField("Введите название заболевания");
        filterPanel = new JPanel();
        filterPanel.add(doctor);
        filterPanel.add(speciality);
        filterPanel.add(disease);
        filterPanel.add(searchButton);
    }

    private void placeComponents() {
        pcAdmin.add(toolBar, BorderLayout.NORTH);
        pcAdmin.add(filterPanel, BorderLayout.SOUTH);
    }

    public void clickMouseForDoctors() {
        // Обработка выбора строки в таблице
        tableDoctors.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { //е - хранит информацию о событии мыши: клик, кнопка мыши и тд
                if (e.getClickCount() == 1) { // Проверяем, что это одиночный клик
                    selectedRow = tableDoctors.rowAtPoint(e.getPoint()); //индекс строки, на которую был произведен клик(координаты точки, где произошел клик мыши)
                }
            }
        });
    }

    public void clickMouseForPatients() {
        // Обработка выбора строки в таблице
        tablePatients.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { //е - хранит информацию о событии мыши: клик, кнопка мыши и тд
                if (e.getClickCount() == 1) { // Проверяем, что это одиночный клик
                    selectedRow = tablePatients.rowAtPoint(e.getPoint()); //индекс строки, на которую был произведен клик(координаты точки, где произошел клик мыши)
                }
            }
        });
    }

    private void loadDoctorsDataFromDatabase() {
        String[][] doctorsData = db.createTableFromDoctors(); // Создание массива из данных таблицы Doctors
        modelDoctors.setRowCount(0); // Очистка текущих данных
        for (String[] row : doctorsData) {
            modelDoctors.addRow(row); // Добавление построчно
        }
    }

    private void loadPatientsDataFromDatabase() {
        String[][] patientsData = db.createTableFromPatients(); // Создание массива из данных таблицы Patients
        modelPatients.setRowCount(0); // Очистка текущих данных
        for (String[] row : patientsData) {
            modelPatients.addRow(row); // Добавление построчно
        }
    }

    public void showAddDoctorInfoFromDatabase() {
        //Создание текстовых полей
        JTextField doctorField = new JTextField(30);
        JTextField specialityField = new JTextField(10);
        JTextField roomNumberField = new JTextField(10);
        JTextField workingHoursField = new JTextField(10);

        //Добавление этих полей на панель
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); //Расположение по вертикали
        panel.add(new JLabel("ФИО врач:"));
        panel.add(doctorField);
        panel.add(new JLabel("Специализация:"));
        panel.add(specialityField);
        panel.add(new JLabel("Номер кабинета:"));
        panel.add(roomNumberField);
        panel.add(new JLabel("График работы:"));
        panel.add(workingHoursField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Добавить нового врача", JOptionPane.OK_CANCEL_OPTION); //Диалоговое окно
        if (result == JOptionPane.OK_OPTION) {  //Если была нажата кнопка "ОК"
            String doctor = null;
            String specialization = null;
            String roomNumber = null;
            String workingHours = null;
            //Обработка исключения на пустую строчку
            try {
                doctor = doctorField.getText();
                specialization = specialityField.getText();
                roomNumber = roomNumberField.getText();
                workingHours = workingHoursField.getText();
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(null, "Заполните все поля");
                showAddDoctorInfoFromDatabase();  //После выброса исключения, опять показать окно с заполнением полей
            }
            db.addDoctorInfo(doctor, specialization, roomNumber, workingHours); //Добавить строчку в базу данных
            loadDoctorsDataFromDatabase();
        }
    }

    public void showAddPatientInfoFromDatabase() {
        //Создание текстовых полей
        JTextField doctorField = new JTextField(30);
        JTextField patientField = new JTextField(30);
        JTextField diseaseField = new JTextField(10);

        //Добавление этих полей на панель
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); //Расположение по вертикали
        panel.add(new JLabel("ФИО врач:"));
        panel.add(doctorField);
        panel.add(new JLabel("ФИО пациента:"));
        panel.add(patientField);
        panel.add(new JLabel("Заболевание:"));
        panel.add(diseaseField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Добавить нового пациента", JOptionPane.OK_CANCEL_OPTION); //Диалоговое окно
        if (result == JOptionPane.OK_OPTION) {  //Если была нажата кнопка "ОК"
            String doctor = null;
            String patient = null;
            String infoDisease = null;
            //Обработка исключения на пустую строчку
            try {
                doctor = doctorField.getText();
                patient = patientField.getText();
                infoDisease = diseaseField.getText();

                // Проверка наличия врача в базе данных
                if (!db.isDoctorExists(doctor)) {
                    throw new SQLException("Такого врача не существует");
                }

                db.addPatientInfo(doctor, patient, infoDisease); //Добавить строчку в базу данных
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                showAddPatientInfoFromDatabase();  //После выброса исключения, опять показать окно с заполнением полей
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(null, "Заполните все поля");
                showAddPatientInfoFromDatabase();  //После выброса исключения, опять показать окно с заполнением полей
            }
            loadPatientsDataFromDatabase();
        }
    }

    public void changeInfoFromDoctors(int selectedRow) {
        // Создание текстовых полей
        JTextField doctorField = new JTextField(modelDoctors.getValueAt(selectedRow, 0).toString(), 30);
        JTextField specialityField = new JTextField(modelDoctors.getValueAt(selectedRow, 1).toString(), 30);
        JTextField roomNumberField = new JTextField(modelDoctors.getValueAt(selectedRow, 2).toString(), 30);
        JTextField workingHoursField = new JTextField(modelDoctors.getValueAt(selectedRow, 3).toString(), 30);

        // Добавление этих полей на панель
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Расположение по вертикали
        panel.add(new JLabel("ФИО врач:"));
        panel.add(doctorField);
        panel.add(new JLabel("Специализация:"));
        panel.add(specialityField);
        panel.add(new JLabel("Номер кабинета:"));
        panel.add(roomNumberField);
        panel.add(new JLabel("График работы:"));
        panel.add(workingHoursField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Изменить строку", JOptionPane.OK_CANCEL_OPTION); // Диалоговое окно
        if (result == JOptionPane.OK_OPTION) {  // Если была нажата кнопка "ОК"
            String doctor;
            String specialization;
            String roomNumber;
            String workingHours;
            // Обработка исключения на пустую строчку
            try {
                doctor = doctorField.getText();
                specialization = specialityField.getText();
                roomNumber = roomNumberField.getText();
                workingHours = workingHoursField.getText();

                if (doctor.isEmpty() || specialization.isEmpty() || workingHours.isEmpty()) {
                    throw new IllegalArgumentException("Заполните все поля");
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Неправильно набраны данные");
                changeInfoFromDoctors(selectedRow);  // После выброса исключения, опять показать окно с заполнением полей
                return;
            }
            db.changeRowFromDoctors(doctor, specialization, roomNumber, workingHours); // Изменить строчку в базе данных
            loadDoctorsDataFromDatabase();  // Обновление данных в таблице
        }
    }

    public void changeInfoFromPatients(int selectedRow) {
        // Создание текстовых полей
        JTextField doctorField = new JTextField(modelPatients.getValueAt(selectedRow, 0).toString(), 30);
        JTextField patientField = new JTextField(modelPatients.getValueAt(selectedRow, 1).toString(), 30);
        JTextField diseaseField = new JTextField(modelPatients.getValueAt(selectedRow, 2).toString(), 30);

        // Добавление этих полей на панель
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Расположение по вертикали
        panel.add(new JLabel("ФИО врач:"));
        panel.add(doctorField);
        panel.add(new JLabel("ФИО пациента:"));
        panel.add(patientField);
        panel.add(new JLabel("Заболевание:"));
        panel.add(diseaseField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Изменить строку", JOptionPane.OK_CANCEL_OPTION); // Диалоговое окно
        if (result == JOptionPane.OK_OPTION) {  // Если была нажата кнопка "ОК"
            String doctor;
            String patient;
            String infoDisease;
            // Обработка исключения на пустую строчку
            try {
                doctor = doctorField.getText();
                patient = patientField.getText();
                infoDisease = diseaseField.getText();

                if (doctor.isEmpty() || patient.isEmpty() || infoDisease.isEmpty()) {
                    throw new IllegalArgumentException("Заполните все поля");
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Неправильно набраны данные");
                changeInfoFromPatients(selectedRow);  // После выброса исключения, опять показать окно с заполнением полей
                return;
            }
            db.changeRowFromPatients(doctor, patient, infoDisease); // Изменить строчку в базе данных
            loadPatientsDataFromDatabase();  // Обновление данных в таблице
        }
    }

    public void searchButtonFromDatabase() {
        try {
            exception.checkException(disease);
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(pcAdmin, "Поле ввода пустое. Повторите попытку");
            noException = false;
        } catch (TextException myEx) {
            JOptionPane.showMessageDialog(pcAdmin, myEx.getMessage());
            noException = false;
        }

        String selectedDoctor = (String) doctor.getSelectedItem();
        String selectedSpeciality = (String) speciality.getSelectedItem();
        String enteredDisease = disease.getText();

        // Выполняем поиск в базе данных
        List<String> searchResults = db.searchRowFromDatabase(selectedDoctor, selectedSpeciality, enteredDisease);

        StringBuilder results = new StringBuilder("Результаты поиска:\n");
        for (String doctorName : searchResults) {
            results.append(doctorName).append("\n");
        }
        // Отображаем результаты в диалоговом окне
        JOptionPane.showMessageDialog(pcAdmin, results.toString(), "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
    }
}



