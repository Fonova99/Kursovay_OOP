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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для выгрузки данных пациентов в файл
    public void savePatientsToFile(JFrame pcAdmin, DefaultTableModel modelPatients) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modelPatients.getRowCount(); i++) {
            for (int j = 0; j < modelPatients.getColumnCount(); j++) {
                sb.append(modelPatients.getValueAt(i, j)).append("//");
            }
            sb.append("\n");
        }
        String editedText = showEditor(sb.toString());
        if (!editedText.equals(sb.toString())) {
            FileDialog save = new FileDialog(pcAdmin, "Сохранение пациентов", FileDialog.SAVE);
            save.setFile("*.txt");
            save.setVisible(true);
            String fileName = save.getDirectory() + save.getFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(editedText);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
                String doctorName = parts[0]; // Предполагаем, что имя врача в первой колонке
                if (checkDoctorExists(doctorName, modelDoctors)) {
                    modelPatients.addRow(parts);
                } else {
                    JOptionPane.showMessageDialog(pcAdmin, "Врач " + doctorName + " не найден в списке врачей.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String showEditor(String text) {
        JTextArea textArea = new JTextArea(text);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Редактирование текста", JOptionPane.OK_CANCEL_OPTION);
        return (result == JOptionPane.OK_OPTION) ? textArea.getText() : text;
    }

    private void clearTableModel(DefaultTableModel model) {
        int rows = model.getRowCount();
        for (int i = rows - 1; i >= 0; i--) {
            model.removeRow(i);
        }
    }

    private boolean checkDoctorExists(String doctorName, DefaultTableModel modelDoctors) {
        for (int i = 0; i < modelDoctors.getRowCount(); i++) {
            String existingDoctor = (String) modelDoctors.getValueAt(i, 0); // Предполагаем, что ФИО врача в первой колонке
            if (existingDoctor.equals(doctorName)) {
                return true;
            }
        }
        return false;
    }
}