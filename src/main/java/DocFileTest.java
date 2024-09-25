import org.junit.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class DocFileTest {

    @Test
    public void testSaveDocFile() throws IOException {
        // Создаем фейковый JFrame и модель таблицы
        JFrame pcAdmin = new JFrame();
        DefaultTableModel model = new DefaultTableModel(new Object[][]{
                {"1", "2"},
                {"3", "4"}
        }, new String[]{"Column1", "Column2"});

        // Создаем объект DocFile
        DocFile docFile = new DocFile();

        // Вызываем метод saveDocFile
        docFile.saveDocFile(pcAdmin, model);

        // Проверяем, что файл был сохранен правильно
        String file = "test.txt";
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> lines = reader.lines().collect(Collectors.toList());

        assertEquals(2, lines.size());
        assertEquals("1//2//", lines.get(0));
        assertEquals("3//4//", lines.get(1));
    }


    @Test
    public void testOpenDocFile() throws IOException {
        // Создаем фейковый JFrame и модель таблицы
        JFrame pcAdmin = new JFrame();
        DefaultTableModel model = new DefaultTableModel(new String[]{"Column1", "Column2"}, 0);

        // Создаем объект DocFile
        DocFile docFile = new DocFile();

        // Создаем тестовый файл
        String file = "test.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("1//2//\n");
        writer.write("3//4//\n");
        writer.close();

        // Вызываем метод openDocFile
        docFile.openDocFile(pcAdmin, model);

        // Проверяем, что данные были загружены правильно
        assertEquals(2, model.getRowCount());
        assertEquals("1", model.getValueAt(0, 0));
        assertEquals("2", model.getValueAt(0, 1));
        assertEquals("3", model.getValueAt(1, 0));
        assertEquals("4", model.getValueAt(1, 1));
    }

}