import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class DocFile {

    DatabaseManager db = new DatabaseManager();

    // Метод для выгрузки данных врачей в файл
    public void saveDoctorsToFile(JFrame pcAdmin, DefaultTableModel modelDoctors) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modelDoctors.getRowCount(); i++) {
            for (int j = 0; j < modelDoctors.getColumnCount(); j++) {
                sb.append(modelDoctors.getValueAt(i, j)).append("//");
            }
            sb.append("\n");
        }
        String editedText = showEditor(sb.toString());
        if (!editedText.equals(sb.toString())) {
            FileDialog save = new FileDialog(pcAdmin, "Сохранение врачей", FileDialog.SAVE);
            save.setFile("*.txt");
            save.setVisible(true);
            String fileName = save.getDirectory() + save.getFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(editedText);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            db.updateDoctors(modelDoctors);
        }
    }

    // Метод для загрузки данных врачей из файла
    public void loadDoctorsFromFile(JFrame pcAdmin, DefaultTableModel modelDoctors) {
        FileDialog open = new FileDialog(pcAdmin, "Загрузка врачей", FileDialog.LOAD);
        open.setFile("*.txt");
        open.setVisible(true);
        String fileName = open.getFile();
        if (fileName == null) {
            return;
        }
        fileName = open.getDirectory() + open.getFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            clearTableModel(modelDoctors);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("//");
                modelDoctors.addRow(parts);
            }
            db.updateDoctors(modelDoctors);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для выгрузки данных пациентов в файл
    public void savePatientsToFile(JFrame pcAdmin, DefaultTableModel modelPatients, DefaultTableModel modelDoctors) {
        if (modelPatients == null || modelDoctors == null) {
            System.err.println("Model is not initialized.");
            return;
        }

        FileDialog save = new FileDialog(pcAdmin, "Сохранение пациентов", FileDialog.SAVE);
        save.setFile("*.txt");
        save.setVisible(true);
        String fileName = save.getDirectory() + save.getFile();
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modelPatients.getRowCount(); i++) {
            String doctorName = (String) modelPatients.getValueAt(i, 0);
            String patient = (String) modelPatients.getValueAt(i, 1);
            String disease = (String) modelPatients.getValueAt(i, 2);

            sb.append(doctorName).append("//")
                    .append(patient).append("//")
                    .append(disease).append("\n");
        }

        // Показываем редактор для редактирования данных
        String editedText = showEditor(sb.toString());

        // Проверка, если текст был изменен пользователем
        if (!editedText.equals(sb.toString())) {
            // Проверка всех врачей после редактирования
            String[] lines = editedText.split("\n");
            for (String line : lines) {
                String[] parts = line.split("//");
                String doctorName = parts[0]; // Предполагаем, что имя врача в первой колонке

                if (!checkDoctorExists(doctorName, modelDoctors)) {
                    // Окно с ошибкой и возможностью нажать "ОК"
                    int response = JOptionPane.showConfirmDialog(null, "Врач " + doctorName + " не найден в списке врачей. Хотите добавить нового врача?", "Ошибка", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);

                    if (response == JOptionPane.OK_OPTION) {
                        // Если пользователь нажал "ОК", показываем текстовые поля для ввода нового врача
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
                        if (result == JOptionPane.OK_OPTION) {
                            String newDoctorName = doctorField.getText();
                            String specialty = specialtyField.getText();
                            String office = officeField.getText();
                            String schedule = scheduleField.getText();

                            if (newDoctorName.isEmpty() || specialty.isEmpty() || office.isEmpty() || schedule.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Все поля должны быть заполнены. Сохранение отменено.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                return; // Прерываем запись, если данные не введены
                            }

                            // Добавление нового врача в модель врачей
                            modelDoctors.addRow(new Object[]{newDoctorName, specialty, office, schedule});
                            db.updateDoctors(modelDoctors); // Обновляем базу данных врачей
                        } else {
                            return; // Прерываем запись, если пользователь отменил ввод
                        }
                    } else {
                        return; // Прерываем запись, если пользователь отказался от добавления нового врача
                    }
                }
            }

            // Если все проверки пройдены, сохраняем данные в файл
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(editedText);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Сохранение пациентов в базу данных после изменения
            db.updatePatients(modelPatients); // Вызов метода для сохранения пациентов в базу данных
        }
    }


    // Метод для загрузки данных пациентов из файла
    public void loadPatientsFromFile(JFrame pcAdmin, DefaultTableModel modelPatients, DefaultTableModel modelDoctors) {
        FileDialog open = new FileDialog(pcAdmin, "Загрузка пациентов", FileDialog.LOAD);
        open.setFile("*.txt");
        open.setVisible(true);
        String fileName = open.getFile();
        if (fileName == null) {
            return;
        }
        fileName = open.getDirectory() + open.getFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            clearTableModel(modelPatients);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("//");
                    modelPatients.addRow(parts);
            }
            db.updatePatients(modelPatients);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    String showEditor(String text) {
        JTextArea textArea = new JTextArea(text);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Редактирование текста", JOptionPane.OK_CANCEL_OPTION);
        return (result == JOptionPane.OK_OPTION) ? textArea.getText() : text;
    }

    void clearTableModel(DefaultTableModel model) {
        int rows = model.getRowCount();
        for (int i = rows - 1; i >= 0; i--) {
            model.removeRow(i);
        }
    }

    boolean checkDoctorExists(String doctorName, DefaultTableModel modelDoctors) {
        for (int i = 0; i < modelDoctors.getRowCount(); i++) {
            String existingDoctor = (String) modelDoctors.getValueAt(i, 0); // Предполагаем, что ФИО врача в первой колонке
            if (existingDoctor.equals(doctorName)) {
                return true;
            }
        }
        return false;
    }
}