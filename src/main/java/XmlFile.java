import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;

public class XmlFile {
    Document doc;
    Object monitor;

    public void recordXmlFile(DefaultTableModel model) {
        synchronized (monitor) {
            if (model == null) {
                System.err.println("Model is not initialized.");
                return;
            }
            FileDialog read = new FileDialog((Frame) null, "Запись данных", FileDialog.SAVE);
            read.setFile("*.xml");
            read.setVisible(true); // Отобразить запрос пользователю
            String fileName = read.getFile();
            if (fileName == null) {
                return;
            }
            fileName = read.getDirectory() + read.getFile(); // Определить имя выбранного каталога и файла
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); // Создание парсера документа
                Document doc = builder.newDocument(); // Создание пустого документа

                Node administration = doc.createElement("Administration"); // Создание корневого элемента
                doc.appendChild(administration); // Добавление его в документ

                // Создание дочерних элементов и присвоение значений атрибутам
                for (int i = 0; i < model.getRowCount(); i++) {
                    Element visit = doc.createElement("visit");
                    administration.appendChild(visit);
                    visit.setAttribute("doctor", (String) model.getValueAt(i, 0));
                    visit.setAttribute("speciality", (String) model.getValueAt(i, 1));
                    visit.setAttribute("cabinet", (String) model.getValueAt(i, 2));
                    visit.setAttribute("workSchedule", (String) model.getValueAt(i, 3));
                    visit.setAttribute("patient", (String) model.getValueAt(i, 4));
                    visit.setAttribute("disease", (String) model.getValueAt(i, 5));
                }

                // Преобразуем XML-документ в строку для редактирования
                Transformer transformer = TransformerFactory.newInstance().newTransformer(); // Создание преобразователя документа
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                String xmlString = writer.toString();

                // Показываем окно редактирования
                String editedXml = showEditor(xmlString);

                // Преобразуем отредактированную строку обратно в XML
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder newBuilder = factory.newDocumentBuilder();
                Document editedDoc = newBuilder.parse(new ByteArrayInputStream(editedXml.getBytes()));

                // Сохраняем отредактированный XML в файл
                Transformer trans = TransformerFactory.newInstance().newTransformer();
                FileWriter fw = new FileWriter(fileName);
                trans.transform(new DOMSource(editedDoc), new StreamResult(fw));

            } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
                e.printStackTrace();
            }
        }

    }

    private String showEditor(String xmlString) {
        JTextArea textArea = new JTextArea(xmlString);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Редактирование XML", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return textArea.getText();
        } else {
            return xmlString; // Если пользователь отменил редактирование, возвращаем исходный XML
        }
    }

    public void readXmlFile(DefaultTableModel model) {
        synchronized (monitor) {
            if (model == null) {
                System.err.println("Model is not initialized.");
                return;
            }
            FileDialog read = new FileDialog((Frame) null, "Чтение данных", FileDialog.LOAD);
            read.setFile("*.xml");
            read.setVisible(true); // Отобразить запрос пользователю
            String fileName = read.getDirectory() + read.getFile(); // Определить имя выбранного каталога и файла

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); //Создание парсера документа
                doc = builder.parse(new File(fileName)); //Чтение из файла
                doc.getDocumentElement().normalize();
            } catch (ParserConfigurationException e) { // Обработка ошибки парсера при чтении данных из XML-файла
                e.printStackTrace();
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
            NodeList visits = doc.getElementsByTagName("visit"); //Получение списка элементов с именем visit
            model.setRowCount(0); //Очистка таблицы
            for (int i = 0; i < visits.getLength(); i++) { //Цикл просмотра списка элементов и запись данных в таблицу
                Node elem = visits.item(i); //Выбор очередного элемента списка
                NamedNodeMap attrs = elem.getAttributes(); //Получение списка атрибутов элемента
                //Чтение атрибутов элемента
                String doctor = attrs.getNamedItem("doctor").getNodeValue();
                String speciality = attrs.getNamedItem("speciality").getNodeValue();
                String cabinet = attrs.getNamedItem("cabinet").getNodeValue();
                String workSchedule = attrs.getNamedItem("workSchedule").getNodeValue();
                String patient = attrs.getNamedItem("patient").getNodeValue();
                String disease = attrs.getNamedItem("disease").getNodeValue();
                model.addRow(new String[]{doctor, speciality, cabinet, workSchedule, patient, disease}); //Запись данных в таблицу
            }
        }
    }
}
