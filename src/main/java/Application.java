import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class Application {
    private JFrame pcAdmin;
    private JButton addButton;
    private JButton changeButton;
    private JButton removeButton;
    private JButton searchButton;
    private JButton saveButton;
    private JButton openButton;
    private JButton saveXmlButton;
    private JButton openXmlButton;
    private JToolBar toolBar;
    private DefaultTableModel model;
    private JTable registry;
    private JScrollPane scroll;
    private JTextField disease;
    private JComboBox doctor, speciality;
    private JPanel filterPanel;
    private final TextException exception = new TextException();
    private boolean noException = true;
    private final XmlFile xml = new XmlFile();
    private final DocFile docFile = new DocFile();
    private final DatabaseManager db = new DatabaseManager();
    private int selectedRow = -1;

    public void show() {
        createWindow();
        createButtons();
        createToolbar();
        createTable();
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

    private void createButtons() {
        addButton = new JButton("Добавить");
        changeButton = new JButton("Изменить");
        removeButton = new JButton("Удалить");
        searchButton = new JButton("Поиск");
        saveButton = new JButton("Сохранить");
        openButton = new JButton("Загрузить");
        saveXmlButton = new JButton("Сохранить Xml-файл");
        openXmlButton = new JButton("Загрузить Xml-файл");

        addButton.addActionListener(e -> showAddInfoFromDatabase());

        changeButton.addActionListener(e -> {
            clickMouse();
            if (selectedRow != -1) {
                changeInfoFromDatabase(selectedRow);
                selectedRow = -1;
            } else {
                JOptionPane.showMessageDialog(null, "Выберите строку для удаления");
            }
        });

        removeButton.addActionListener(e -> {
            clickMouse();
            if (selectedRow != -1) {
                db.deleteRowFromDatabase(model, selectedRow);
                model.removeRow(selectedRow);
                selectedRow = -1;
            } else {
                JOptionPane.showMessageDialog(null, "Выберите строку для удаления");
            }
            loadDataFromDatabase();
        });

        searchButton.addActionListener(e -> searchButtonFromDatabase());

        saveButton.addActionListener(e -> docFile.saveDocFile(pcAdmin, model));

        openButton.addActionListener(e -> docFile.openDocFile(pcAdmin, model));

        saveXmlButton.addActionListener(e -> new Thread(new Runnable() {
            @Override
            public void run() {
                xml.recordXmlFile(model);
            }
        }).start());

        openXmlButton.addActionListener(e -> new Thread(new Runnable() {
            @Override
            public void run() {
                xml.readXmlFile(model);
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

    private void createTable() {
        String[] columns = {"ФИО врача", "Специализация", "Номер кабинета", "График работы", "ФИО пациента", "Заболевания"};
        String[][] data = db.createTableFromDatabase();
        model = new DefaultTableModel(data, columns);
        registry = new JTable(model);
        setColumnWidths(registry, columns);
        scroll = new JScrollPane(registry);
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
        pcAdmin.setLayout(new BorderLayout());
        pcAdmin.add(toolBar, BorderLayout.NORTH);
        pcAdmin.add(scroll, BorderLayout.CENTER);
        pcAdmin.add(filterPanel, BorderLayout.SOUTH);
    }

    private void setColumnWidths(JTable table, String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(200);
        }
    }

    public void clickMouse() {
        // Обработка выбора строки в таблице
        registry.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { //е - хранит информацию о событии мыши: клик, кнопка мыши и тд
                if (e.getClickCount() == 1) { // Проверяем, что это одиночный клик
                    selectedRow = registry.rowAtPoint(e.getPoint()); //индекс строки, на которую был произведен клик(координаты точки, где произошел клик мыши)
                }
            }
        });
    }
    private void loadDataFromDatabase() {
        String[][] data = db.createTableFromDatabase(); //Создание массива из данных таблицы
        model.setRowCount(0); // Очистка текущих данных
        for (String[] row : data) {
            model.addRow(row); //Добавление построчно
        }
    }
    public void showAddInfoFromDatabase() {
        //Создание текстовых полей
        JTextField doctorField = new JTextField(30);
        JTextField specialityField = new JTextField(10);
        JTextField roomNumberField = new JTextField(10);
        JTextField workingHoursField = new JTextField(10);
        JTextField patientField = new JTextField(10);
        JTextField diseaseField = new JTextField(10);

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
        panel.add(new JLabel("ФИО пациента:"));
        panel.add(patientField);
        panel.add(new JLabel("Заболевание:"));
        panel.add(diseaseField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Добавить новую строку", JOptionPane.OK_CANCEL_OPTION); //Диалоговое окно
        if (result == JOptionPane.OK_OPTION) {  //Если была нажата кнопка "ОК"
            String doctor = null;
            String specialization = null;
            String roomNumber = null;
            String workingHours = null;
            String patient = null;
            String infoDisease = null;
            //Обработка исключения на пустую строчку
            try {
                doctor = doctorField.getText();
                specialization = specialityField.getText();
                roomNumber = roomNumberField.getText();
                workingHours = workingHoursField.getText();
                patient = patientField.getText();
                infoDisease = diseaseField.getText();
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(null, "Заполните все поля");
                showAddInfoFromDatabase();  //После выброса исключения, опять показать окно с заполнением полей
            }
            db.addInfo(doctor, specialization, roomNumber, workingHours, patient, infoDisease); //Добавить строчку в базу данных
            loadDataFromDatabase();  //Отобразить окно с таблицей заново
        }
    }

    public void changeInfoFromDatabase(int selectedRow) {
        //Создание текстовых полей
        JTextField doctorField = new JTextField(model.getValueAt(selectedRow, 0).toString(), 30);
        JTextField specialityField = new JTextField(model.getValueAt(selectedRow, 1).toString(), 30);
        JTextField roomNumberField = new JTextField(model.getValueAt(selectedRow, 2).toString(), 30);
        JTextField workingHoursField = new JTextField(model.getValueAt(selectedRow, 3).toString(), 30);
        JTextField patientField = new JTextField(model.getValueAt(selectedRow, 4).toString(), 30);
        JTextField diseaseField = new JTextField(model.getValueAt(selectedRow, 5).toString(), 30);

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
        panel.add(new JLabel("ФИО пациента:"));
        panel.add(patientField);
        panel.add(new JLabel("Заболевание:"));
        panel.add(diseaseField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Изменить строку", JOptionPane.OK_CANCEL_OPTION); //Диалоговое окно
        if (result == JOptionPane.OK_OPTION) {  //Если была нажата кнопка "ОК"
            String doctor;
            String specialization;
            String roomNumber;
            String workingHours;
            String patient;
            String infoDisease;
            // Обработка исключения на пустую строчку
            try {
                doctor = doctorField.getText();
                specialization = specialityField.getText();
                roomNumber = roomNumberField.getText();
                workingHours = workingHoursField.getText();
                patient = patientField.getText();
                infoDisease = diseaseField.getText();

                if (doctor.isEmpty() || specialization.isEmpty() || workingHours.isEmpty() || patient.isEmpty() || infoDisease.isEmpty()) {
                    throw new IllegalArgumentException("Заполните все поля");
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Неправильно набраны данные");
                changeInfoFromDatabase(selectedRow);  // После выброса исключения, опять показать окно с заполнением полей
                return;
            }
            db.changeRowFromDatabase(doctor, specialization, roomNumber, workingHours, patient, infoDisease); //Добавить строчку в базу данных
            loadDataFromDatabase();  //Отобразить окно с таблицей заново
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



