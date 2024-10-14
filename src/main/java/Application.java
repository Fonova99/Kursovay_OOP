import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
    private JButton searchButtonFromPatients;

    private JButton searchButtonFromDoctors;
    private JButton saveButton;
    private JButton openButton;
    private JButton saveXmlButton;
    private JButton openXmlButton;
    private JToolBar toolBar;
    private JTextField disease;
    private JComboBox doctor, patient;
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
        placeComponents();
        pcAdmin.setVisible(true);
    }

    //Метод по созданию окна
    private void createWindow() {
        pcAdmin = new JFrame("Администратор поликлиники");  //название окна
        pcAdmin.setSize(1200, 500); //размеры окна
        pcAdmin.setLocation(100, 100);  //расположение окна
        pcAdmin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //отображение кнопки "крестик"
    }

    private void createTables() {
        //Создаем две таблицы
        String[] columns1 = {"ФИО врача", "Специализация", "Номер кабинета", "График работы"};  //заголовки колонок
        modelDoctors = new DefaultTableModel(new String[0][0], columns1);   //создание модели таблицы "Врачи"
        tableDoctors = new JTable(modelDoctors);    //создание таблицы
        scrollDoctors = new JScrollPane(tableDoctors);  //добавление прокрутки

        String[] columns2 = {"ФИО врача", "ФИО пациента", "Диагноз"};
        modelPatients = new DefaultTableModel(new String[0][0], columns2);
        tablePatients = new JTable(modelPatients);
        scrollPatients = new JScrollPane(tablePatients);

        //добавляем таблиц на JTabbedPane
        tabbedPane.addTab("Врачи", scrollDoctors);
        tabbedPane.addTab("Пациенты", scrollPatients);

        //добавляем JTabbedPane на окно
        pcAdmin.add(tabbedPane, BorderLayout.CENTER);

        //загрузка данных для таблицы Doctors
        String[][] doctorsData = db.createTableFromDoctors();
        modelDoctors.setDataVector(doctorsData, new String[]{"ФИО врача", "Специализация", "Номер кабинета", "График работы"});

        //загрузка данных для таблицы Patients
        String[][] patientsData = db.createTableFromPatients();
        modelPatients.setDataVector(patientsData, new String[]{"ФИО врача", "ФИО пациента", "Диагноз"});
    }

    private void createButtons() {
        addButton = new JButton("Добавить");
        changeButton = new JButton("Изменить");
        removeButton = new JButton("Удалить");
        searchButtonFromDoctors = new JButton("Поиск");
        searchButtonFromPatients = new JButton("Поиск");
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
                    // Проверка наличия доктора в таблице пациентов
                    boolean doctorExistsInPatients = checkDoctorExistsInPatients(selectedRow);
                    if (doctorExistsInPatients) {
                        JOptionPane.showMessageDialog(null, "Нельзя удалить доктора, так как к нему записаны пациенты.");
                    } else {
                        db.deleteRowFromDoctors(modelDoctors, selectedRow);
                        modelDoctors.removeRow(selectedRow);
                        selectedRow = -1;
                        loadPatientsDataFromDatabase();
                        loadDoctorsDataFromDatabase();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Выберите строку для удаления");
                }
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

        searchButtonFromDoctors.addActionListener(e -> searchButtonFromDoctors());
        searchButtonFromPatients.addActionListener(e -> searchButtonFromPatients());

        saveButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                docFile.saveDoctorsToFile(pcAdmin, modelDoctors);
            } else if (selectedIndex == 1) {
                docFile.savePatientsToFile(pcAdmin, modelPatients, modelDoctors);
            }
        });

        openButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                docFile.loadDoctorsFromFile(pcAdmin, modelDoctors);
                db.updateDoctors(modelDoctors);
            } else if (selectedIndex == 1) {
                docFile.loadPatientsFromFile(pcAdmin, modelPatients);
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
                    xml.recordXmlFilePatients(modelPatients, modelDoctors);
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
                    xml.readXmlFilePatients(modelPatients);
                }
            }
        }).start());

        addButton.setToolTipText("Добавить информацию");
        changeButton.setToolTipText("Изменить информацию");
        removeButton.setToolTipText("Удалить информацию");
        searchButtonFromDoctors.setToolTipText("Поиск информации");
        searchButtonFromPatients.setToolTipText("Поиск информации");
        saveButton.setToolTipText("Сохранить данные");
        openButton.setToolTipText("Загрузить данные");
        saveXmlButton.setToolTipText("Сохранить Xml-файл");
        openXmlButton.setToolTipText("Загрузить Xml-файл");
    }

    //Метод для проверки наличия доктора в таблице пациентов
    private boolean checkDoctorExistsInPatients(int doctorRow) {
        // Получаем имя доктора, который нужно проверить
        String doctorName = modelDoctors.getValueAt(doctorRow, 0).toString(); //Предполагаем, что имя доктора находится в первом столбце
        //Проходим по всем строкам в modelPatients
        for (int i = 0; i < modelPatients.getRowCount(); i++) {
            String patientDoctorName = modelPatients.getValueAt(i, 0).toString(); //Предполагаем, что имя доктора у пациента находится в первом столбце
            if (patientDoctorName.equals(doctorName)) {
                return true; //Доктор найден в таблице пациентов
            }
        }
        return false; //Доктор не найден в таблице пациентов
    }

    //Метод по добавлению кнопок на поле окна
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

    //Метод по созданию выпадающего меню "Врачи"
    private void doctorBox() {
        doctor = new JComboBox();
        List<String> doctors = db.searchDoctorFromDatabase();
        //Добавляем пункт "Врач" в начало списка
        doctor.insertItemAt("Врач", 0);
        doctor.setSelectedIndex(0);
        //Добавляем остальные пункты
        for (String name : doctors) {
            doctor.addItem(name);
        }
    }

    //Метод по созданию выпадающего меню "Пациенты"
    private void patientBox() {
        patient = new JComboBox();
        List<String> patients = db.searchPatientsFromDatabase();
        //Добавляем пункт "Специальность" в начало списка
        patient.insertItemAt("Пациенты", 0);
        patient.setSelectedIndex(0);
        //Добавляем остальные пункты
        for (String name : patients) {
            patient.addItem(name);
        }
    }

    //Метод по отображению полей и кнопок поиска на каждой вкладке
    private void placeComponents() {
        pcAdmin.add(toolBar, BorderLayout.NORTH); //Размещаем кнопки наверху

        //Создаем панель для фильтров (врачи)
        doctorBox();
        JPanel doctorFilterPanel = new JPanel();
        doctorFilterPanel.add(doctor);
        doctorFilterPanel.add(searchButtonFromDoctors);

        //Создаем панель для фильтров (пациенты)
        patientBox();
        //Создаем поле ввода заболеваний с полупрозрачным placeholder
        disease = new JTextField("Введите название заболевания", 20);
        // Добавляем компоненты на панель
        JPanel patientFilterPanel = new JPanel();
        patientFilterPanel.add(patient);
        patientFilterPanel.add(disease);
        patientFilterPanel.add(searchButtonFromPatients);

        //Изначально добавляем панель фильтра для "Врачи"
        filterPanel = doctorFilterPanel;
        pcAdmin.add(filterPanel, BorderLayout.SOUTH);

        //Добавляем слушатель для изменения вкладок
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            pcAdmin.remove(filterPanel); //Удаляем старую панель фильтров

            if (selectedIndex == 0) {  //Вкладка "Врачи"
                filterPanel = doctorFilterPanel;
            } else if (selectedIndex == 1) {  //Вкладка "Пациенты"
                filterPanel = patientFilterPanel;
            }

            pcAdmin.add(filterPanel, BorderLayout.SOUTH);  //Добавляем новую панель
            //Перерисовываем панель
            pcAdmin.revalidate();
            pcAdmin.repaint();
        });
    }

    //Метод, который считывает в таблице "Врачи" какая строка была выбрана
    public void clickMouseForDoctors() {
        //Обработка выбора строки в таблице
        tableDoctors.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { //е - хранит информацию о событии мыши: клик, кнопка мыши и тд
                if (e.getClickCount() == 1) { //Проверяем, что это одиночный клик
                    selectedRow = tableDoctors.rowAtPoint(e.getPoint()); //индекс строки, на которую был произведен клик(координаты точки, где произошел клик мыши)
                }
            }
        });
    }

    //Метод, который считывает в таблице "Пациенты" какая строка была выбрана
    public void clickMouseForPatients() {
        //Обработка выбора строки в таблице
        tablePatients.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { //е - хранит информацию о событии мыши: клик, кнопка мыши и тд
                if (e.getClickCount() == 1) { //Проверяем, что это одиночный клик
                    selectedRow = tablePatients.rowAtPoint(e.getPoint()); //индекс строки, на которую был произведен клик(координаты точки, где произошел клик мыши)
                }
            }
        });
    }

    //Метод обновления таблицы "Врачи" с базы данных
    private void loadDoctorsDataFromDatabase() {
        String[][] doctorsData = db.createTableFromDoctors(); //Создание массива из данных таблицы Doctors
        modelDoctors.setRowCount(0); //Очистка текущих данных
        for (String[] row : doctorsData) {
            modelDoctors.addRow(row); //Добавление построчно
        }
    }

    //Метод обновления таблицы "Пациенты" с базы данных
    private void loadPatientsDataFromDatabase() {
        String[][] patientsData = db.createTableFromPatients(); //Создание массива из данных таблицы Patients
        modelPatients.setRowCount(0); //Очистка текущих данных
        for (String[] row : patientsData) {
            modelPatients.addRow(row); //Добавление построчно
        }
    }

    //Метод по отображению добавления в таблицу "Врачи"
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
            try {
                //Получаем введенную информацию с каждой строки
                doctor = doctorField.getText();
                specialization = specialityField.getText();
                roomNumber = roomNumberField.getText();
                workingHours = workingHoursField.getText();
            } catch (NullPointerException e) { //Обработка исключения на пустую строчку
                JOptionPane.showMessageDialog(null, "Заполните все поля");
                showAddDoctorInfoFromDatabase();  //После выброса исключения, опять показать окно с заполнением полей
            }
            db.addDoctorInfo(doctor, specialization, roomNumber, workingHours); //Добавить строчку в базу данных
            loadDoctorsDataFromDatabase();
        }
    }

    //Метод по отображению добавления в таблицу "Пациенты"
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
            String doctor;
            String patient;
            String infoDisease;
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

    //Метод по отображению изменений в таблице "Врачи"
    public void changeInfoFromDoctors(int selectedRow) {
        //Создание текстовых полей
        JTextField doctorField = new JTextField(modelDoctors.getValueAt(selectedRow, 0).toString(), 30);
        JTextField specialityField = new JTextField(modelDoctors.getValueAt(selectedRow, 1).toString(), 30);
        JTextField roomNumberField = new JTextField(modelDoctors.getValueAt(selectedRow, 2).toString(), 30);
        JTextField workingHoursField = new JTextField(modelDoctors.getValueAt(selectedRow, 3).toString(), 30);

        //Добавление этих полей на панель
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
        if (result == JOptionPane.OK_OPTION) {  //Если была нажата кнопка "ОК"
            String doctor;
            String specialization;
            String roomNumber;
            String workingHours;
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
                changeInfoFromDoctors(selectedRow);  //После выброса исключения, опять показать окно с заполнением полей
                return;
            }
            db.changeRowFromDoctors(doctor, specialization, roomNumber, workingHours); //Изменить строчку в базе данных
            loadDoctorsDataFromDatabase();  //Обновление данных в таблице
        }
    }

    //Метод по отображению изменений в таблице "Пациенты"
    public void changeInfoFromPatients(int selectedRow) {
        //Создание текстовых полей
        JTextField doctorField = new JTextField(modelPatients.getValueAt(selectedRow, 0).toString(), 30);
        JTextField patientField = new JTextField(modelPatients.getValueAt(selectedRow, 1).toString(), 30);
        JTextField diseaseField = new JTextField(modelPatients.getValueAt(selectedRow, 2).toString(), 30);

        //Добавление этих полей на панель
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); //Расположение по вертикали
        panel.add(new JLabel("ФИО врач:"));
        panel.add(doctorField);
        panel.add(new JLabel("ФИО пациента:"));
        panel.add(patientField);
        panel.add(new JLabel("Заболевание:"));
        panel.add(diseaseField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Изменить строку", JOptionPane.OK_CANCEL_OPTION); // Диалоговое окно
        if (result == JOptionPane.OK_OPTION) {  //Если была нажата кнопка "ОК"
            String doctor;
            String patient;
            String infoDisease;
            try {
                doctor = doctorField.getText();
                patient = patientField.getText();
                infoDisease = diseaseField.getText();

                if (doctor.isEmpty() || patient.isEmpty() || infoDisease.isEmpty()) {
                    throw new IllegalArgumentException("Заполните все поля");
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Неправильно набраны данные");
                changeInfoFromPatients(selectedRow);  //После выброса исключения, опять показать окно с заполнением полей
                return;
            }
            db.changeRowFromPatients(doctor, patient, infoDisease); //Изменить строчку в базе данных
            loadPatientsDataFromDatabase();  //Обновление данных в таблице
        }
    }

    //Метод по работе с поисков во вкладке "Врачи"
    public void searchButtonFromDoctors() {
        db.updateDoctors(modelDoctors);
        String selectedDoctor = (String) doctor.getSelectedItem(); //Получаем значение из выпадающего меню
        List<String> searchResults = db.searchRowFromDoctors(selectedDoctor); //Выполняем поиск в базе данных
        //Записываем информацию в удобном формате
        StringBuilder results = new StringBuilder("Результаты поиска:\n");
        for (String doctorName : searchResults) {
            results.append(doctorName).append("\n");
        }
        //Отображаем результаты в диалоговом окне
        JOptionPane.showMessageDialog(pcAdmin, results.toString(), "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
    }

    //Метод по работе с поисков во вкладке "Пациенты"
    public void searchButtonFromPatients() {

        try {
            exception.checkException(disease);
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(pcAdmin, "Поле ввода пустое. Повторите попытку");
            noException = false;
        } catch (TextException myEx) {
            JOptionPane.showMessageDialog(pcAdmin, myEx.getMessage());
            noException = false;
        }

        String selectedPatient = (String) patient.getSelectedItem(); //Получаем значение из выпадающего меню
        String enteredDisease = disease.getText(); //Получаем значение из текстового меню
        List<String> searchResults = db.searchRowFromPatients(selectedPatient, enteredDisease); //Выполняем поиск в базе данных
        //Записываем информацию в удобном формате
        StringBuilder results = new StringBuilder("Результаты поиска:\n");
        for (String infoPatient : searchResults) {
            results.append(infoPatient).append("\n");
        }
        //Отображаем результаты в диалоговом окне
        JOptionPane.showMessageDialog(pcAdmin, results.toString(), "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
    }
}



