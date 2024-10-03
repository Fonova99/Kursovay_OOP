import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class DocFile {

    DatabaseManager db = new DatabaseManager();

    public void saveDocFile (JFrame pcAdmin, DefaultTableModel model){
        // Собираем все данные из таблицы в одну строку
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                sb.append(model.getValueAt(i, j)).append("//");
            }
            sb.append("\n");
        }

        // Отображаем текстовый редактор с данными из таблицы
        String editedText = showEditor(sb.toString());

        // Если пользователь сохранил изменения, продолжаем сохранение в файл
        if (!editedText.equals(sb.toString())) {
            FileDialog save = new FileDialog(pcAdmin, "Сохранение данных", FileDialog.SAVE);
            save.setFile("*.txt");
            save.setVisible(true); // Отобразить запрос пользователю
            String fileName = save.getDirectory() + save.getFile(); // Определить имя выбранного каталога и файла

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                writer.write(editedText); // Записываем отредактированный текст в файл
                writer.close();
            } catch (IOException ex) { // Ошибка записи в файл
                ex.printStackTrace();
            }
        }
    }

    private String showEditor(String text) {
        JTextArea textArea = new JTextArea(text);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Редактирование текста", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return textArea.getText();
        } else {
            return text; // Если пользователь отменил редактирование, возвращаем исходный текст
        }
    }

    public void openDocFile(JFrame pcAdmin, DefaultTableModel model) {
        FileDialog open = new FileDialog(pcAdmin, "Загрузка данных", FileDialog.LOAD);
        open.setFile("*.txt");
        open.setVisible(true); // Отобразить запрос пользователю
        String fileName = open.getFile();
        if (fileName == null) {
            return;
        }
        fileName = open.getDirectory() + open.getFile(); // Определить имя выбранного каталога и файла

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            int rows = model.getRowCount();
            for (int i = 0; i < rows; i++) {
                model.removeRow(0); // Очистка таблицы
            }
            String visit;
            while ((visit = reader.readLine()) != null) {
                String[] parts = visit.split("//");
                model.addRow(parts);
            }
            reader.close();
        } catch (FileNotFoundException ex) { // файл не найден
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        db.updateDatabase(model);
    }
}


