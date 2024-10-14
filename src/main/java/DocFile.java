import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class DocFile {

    DatabaseManager db = new DatabaseManager();

    //Метод для выгрузки данных врачей в файл
    public void saveDoctorsToFile(JFrame pcAdmin, DefaultTableModel modelDoctors) {
        FileDialog save = new FileDialog(pcAdmin, "Сохранение врачей", FileDialog.SAVE); //диалоговое окно по сохранению файлов
        save.setFile("*.txt"); //тип файла
        save.setVisible(true);
        String fileName = save.getDirectory() + save.getFile(); //сохранение пути к файлу
        if (fileName.isEmpty()) {
            return;
        }
        //построчная запись данных
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modelDoctors.getRowCount(); i++) {
            for (int j = 0; j < modelDoctors.getColumnCount(); j++) {
                sb.append(modelDoctors.getValueAt(i, j)).append("//");
            }
            sb.append("\n");
        }
        String editedText = showEditor(sb.toString()); //отображение окна радактирования с данными
        if (!editedText.equals(sb.toString())) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) { //создание файла для записи
                writer.write(editedText); //запись данных в файл
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            db.updateDoctors(modelDoctors); //обновление базы данных
        }
    }

    //Метод для загрузки данных врачей из файла
    public void loadDoctorsFromFile(JFrame pcAdmin, DefaultTableModel modelDoctors) {
        FileDialog open = new FileDialog(pcAdmin, "Загрузка врачей", FileDialog.LOAD); //диалоговое окно по загрузке файлов
        open.setFile("*.txt"); //тип файла
        open.setVisible(true);
        String fileName = open.getFile(); //название файла
        if (fileName.isEmpty()) {
            return;
        }
        fileName = open.getDirectory() + open.getFile(); //путь к файлу
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) { //открытие файла для чтения
            modelDoctors.setRowCount(0); // Очистка таблицы
            String line;
            //пока в файле есть информация, добавляем в таблицу "Врачи"
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("//");
                modelDoctors.addRow(parts);
            }
            db.updateDoctors(modelDoctors);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Метод для выгрузки данных пациентов в файл
    public void savePatientsToFile(JFrame pcAdmin, DefaultTableModel modelPatients, DefaultTableModel modelDoctors) {
        FileDialog save = new FileDialog(pcAdmin, "Сохранение пациентов", FileDialog.SAVE);  //диалоговое окно по сохранению файлов
        save.setFile("*.txt"); //тип файла
        save.setVisible(true);
        String fileName = save.getDirectory() + save.getFile(); //путь к файлу
        if (fileName.isEmpty()) {
            return;
        }

        //построчная запись данных из таблицы
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modelPatients.getRowCount(); i++) {
            for (int j = 0; j < modelPatients.getColumnCount(); j++) {
                sb.append(modelPatients.getValueAt(i, j)).append("//");
            }
            sb.append("\n");
        }

        String editedText = showEditor(sb.toString());  //отображение окна радактирования с данными
        if (!editedText.equals(sb.toString())) { //Проверка, если текст был изменен пользователем
            String[] lines = editedText.split("\n"); //делим текст на строки по переносу
            for (String line : lines) {
                String[] parts = line.split("//"); //строки делим на значения по знаку
                String doctorName = parts[0]; // Предполагаем, что имя врача в первой колонке

                if (!checkDoctorExists(doctorName, modelDoctors)) { //если ФИО врача, есть в таблице Doctors, выпадает окно с ошибкой
                    int response = JOptionPane.showConfirmDialog(null, "Врач " + doctorName + " не найден в списке врачей. Хотите добавить нового врача?", "Ошибка", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                    // Если пользователь нажал "ОК", показываем текстовые поля для ввода нового врача
                    if (response == JOptionPane.OK_OPTION) {
                        JTextField doctorField = new JTextField(doctorName);
                        JTextField specialtyField = new JTextField();
                        JTextField officeField = new JTextField();
                        JTextField scheduleField = new JTextField();

                        JPanel panel = new JPanel(new GridLayout(0, 1));
                        panel.add(new JLabel("Имя врача:"));
                        panel.add(doctorField);
                        panel.add(new JLabel("Специальность:"));
                        panel.add(specialtyField);
                        panel.add(new JLabel("Кабинет:"));
                        panel.add(officeField);
                        panel.add(new JLabel("Режим работы:"));
                        panel.add(scheduleField);

                        int result = JOptionPane.showConfirmDialog(null, panel, "Добавление нового врача", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if (result == JOptionPane.OK_OPTION) { //если в окне ввода была нажата кнопка "Ок", то считываем текст с каждого поля
                            String newDoctorName = doctorField.getText();
                            String specialty = specialtyField.getText();
                            String office = officeField.getText();
                            String schedule = scheduleField.getText();

                            //Если поля или поле были введены пустыми, то выводим сообщение и прерываем запись
                            if (newDoctorName.isEmpty() || specialty.isEmpty() || office.isEmpty() || schedule.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Все поля должны быть заполнены. Сохранение отменено.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // Добавление нового врача в модель врачей
                            modelDoctors.addRow(new Object[]{newDoctorName, specialty, office, schedule}); //Добавление нового врача в модель врачей
                            db.updateDoctors(modelDoctors); //Обновляем базу данных врачей
                        } else {
                            return; //Прерываем запись, если пользователь отменил ввод
                        }
                    } else {
                        return; //Прерываем запись, если пользователь отказался от добавления нового врача
                    }
                }
            }

            // Если все проверки пройдены, сохраняем данные в файл
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(editedText);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            db.updatePatients(modelPatients); //обновление базы данных
        }
    }

    //Метод для загрузки данных пациентов из файла
    public void loadPatientsFromFile(JFrame pcAdmin, DefaultTableModel modelPatients) {
        FileDialog open = new FileDialog(pcAdmin, "Загрузка пациентов", FileDialog.LOAD); //диалоговое окно по загрузке файлов
        open.setFile("*.txt"); //тип файла
        open.setVisible(true);
        String fileName = open.getFile(); //название файла
        if (fileName.isEmpty()) {
            return;
        }
        fileName = open.getDirectory() + open.getFile(); //путь к файлу
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            modelPatients.setRowCount(0); // Очистка таблицы
            String line;
            //пока есть информация, записываем в таблицу данные
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("//");  //разделяет строку на массив строк по определенному символу
                modelPatients.addRow(parts);
            }
            db.updatePatients(modelPatients);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Метод для пока окна редактирования
    String showEditor(String text) {
        JTextArea textArea = new JTextArea(text); //текстовое поле для редактирования
        JScrollPane scrollPane = new JScrollPane(textArea); //прокрутка
        scrollPane.setPreferredSize(new Dimension(800, 400)); //размер скроллируемой панели
        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Редактирование текста", JOptionPane.OK_CANCEL_OPTION);
        return (result == JOptionPane.OK_OPTION) ? textArea.getText() : text; //Если нажали "ОК", то возвращает отредактированный текст,если "Отмена" - исходный текст
    }

    //Проверка наличия врача в модели врачей
    private boolean checkDoctorExists(String doctorName, DefaultTableModel modelDoctors) {
        for (int i = 0; i < modelDoctors.getRowCount(); i++) { //проходимся по всем строкам таблицы "Врачи"
            String existingDoctor = (String) modelDoctors.getValueAt(i, 0); // Предполагаем, что ФИО врача в первой колонке
            if (existingDoctor.equals(doctorName)) { //если переданное имя и имена из таблицы равны, то возвращаем правду
                return true;
            }
        }
        return false;
    }
}