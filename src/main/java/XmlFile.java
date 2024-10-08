import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import org.xml.sax.SAXException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlFile {

    private final Object monitor = new Object();
    private Document doc;
    DatabaseManager db = new DatabaseManager();

    // Метод для чтения данных пациентов из XML-файла
    public void readXmlFilePatients(DefaultTableModel modelPatients, DefaultTableModel modelDoctors) {
        synchronized (monitor) {
            if (modelPatients == null) {
                System.err.println("Model is not initialized.");
                return;
            }
            FileDialog read = new FileDialog((Frame) null, "Чтение данных", FileDialog.LOAD);
            read.setFile("*.xml");
            read.setVisible(true);
            String fileName = read.getDirectory() + read.getFile();
            if (fileName == null || fileName.isEmpty()) {
                System.err.println("File name is not specified.");
                return;
            }

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                doc = builder.parse(new File(fileName));
                doc.getDocumentElement().normalize();
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
                return;
            }

            NodeList visits = doc.getElementsByTagName("visit");
            modelPatients.setRowCount(0); // Очистка таблицы

            for (int i = 0; i < visits.getLength(); i++) {
                Node elem = visits.item(i);
                NamedNodeMap attrs = elem.getAttributes();
                String doctor = attrs.getNamedItem("doctor").getNodeValue();
                String patient = attrs.getNamedItem("patient").getNodeValue();
                String disease = attrs.getNamedItem("disease").getNodeValue();

                if (checkDoctorExists(doctor, modelDoctors)) {
                    modelPatients.addRow(new String[]{doctor, patient, disease});
                } else {
                    JOptionPane.showMessageDialog(null, "Врач " + doctor + " не найден в списке врачей.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }

                db.updatePatients(modelPatients);
            }
        }
    }

    // Метод для записи данных пациентов в XML-файл
    public void recordXmlFilePatients(DefaultTableModel modelPatients) {
        synchronized (monitor) {
            if (modelPatients == null) {
                System.err.println("Model is not initialized.");
                return;
            }
            FileDialog save = new FileDialog((Frame) null, "Запись данных", FileDialog.SAVE);
            save.setFile("*.xml");
            save.setVisible(true);
            String fileName = save.getDirectory() + save.getFile();
            if (fileName == null || fileName.isEmpty()) {
                return;
            }

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.newDocument();
                Node administration = doc.createElement("Administration");
                doc.appendChild(administration);

                for (int i = 0; i < modelPatients.getRowCount(); i++) {
                    Element visit = doc.createElement("visit");
                    administration.appendChild(visit);
                    visit.setAttribute("doctor", (String) modelPatients.getValueAt(i, 0));
                    visit.setAttribute("patient", (String) modelPatients.getValueAt(i, 1));
                    visit.setAttribute("disease", (String) modelPatients.getValueAt(i, 2));
                }

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                String xmlString = writer.toString();

                String editedXml = showEditor(xmlString);
                Document editedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(editedXml.getBytes()));

                Transformer trans = TransformerFactory.newInstance().newTransformer();
                FileWriter fw = new FileWriter(fileName);
                trans.transform(new DOMSource(editedDoc), new StreamResult(fw));
            } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод для чтения данных врачей из XML-файла
    public void readXmlFileDoctors(DefaultTableModel modelDoctors) {
        synchronized (monitor) {
            if (modelDoctors == null) {
                System.err.println("Model is not initialized.");
                return;
            }
            FileDialog read = new FileDialog((Frame) null, "Чтение данных", FileDialog.LOAD);
            read.setFile("*.xml");
            read.setVisible(true);
            String fileName = read.getDirectory() + read.getFile();
            if (fileName == null || fileName.isEmpty()) {
                System.err.println("File name is not specified.");
                return;
            }

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                doc = builder.parse(new File(fileName));
                doc.getDocumentElement().normalize();
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
                return;
            }

            NodeList visits = doc.getElementsByTagName("visit");
            modelDoctors.setRowCount(0); // Очистка таблицы

            for (int i = 0; i < visits.getLength(); i++) {
                Node elem = visits.item(i);
                NamedNodeMap attrs = elem.getAttributes();
                String doctor = attrs.getNamedItem("doctor").getNodeValue();
                String speciality = attrs.getNamedItem("speciality").getNodeValue();
                String cabinet = attrs.getNamedItem("cabinet").getNodeValue();
                String workSchedule = attrs.getNamedItem("workSchedule").getNodeValue();
                modelDoctors.addRow(new String[]{doctor, speciality, cabinet, workSchedule});
                db.updateDoctors(modelDoctors);
            }
        }
    }

    // Метод для записи данных врачей в XML-файл
    public void recordXmlFileDoctors(DefaultTableModel modelDoctors) {
        synchronized (monitor) {
            if (modelDoctors == null) {
                System.err.println("Model is not initialized.");
                return;
            }
            FileDialog save = new FileDialog((Frame) null, "Запись данных", FileDialog.SAVE);
            save.setFile("*.xml");
            save.setVisible(true);
            String fileName = save.getDirectory() + save.getFile();
            if (fileName == null || fileName.isEmpty()) {
                return;
            }

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.newDocument();
                Node administration = doc.createElement("Administration");
                doc.appendChild(administration);

                for (int i = 0; i < modelDoctors.getRowCount(); i++) {
                    Element visit = doc.createElement("visit");
                    administration.appendChild(visit);
                    visit.setAttribute("doctor", (String) modelDoctors.getValueAt(i, 0));
                    visit.setAttribute("speciality", (String) modelDoctors.getValueAt(i, 1));
                    visit.setAttribute("cabinet", (String) modelDoctors.getValueAt(i, 2));
                    visit.setAttribute("workSchedule", (String) modelDoctors.getValueAt(i, 3));
                }

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                String xmlString = writer.toString();

                String editedXml = showEditor(xmlString);
                Document editedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(editedXml.getBytes()));

                Transformer trans = TransformerFactory.newInstance().newTransformer();
                FileWriter fw = new FileWriter(fileName);
                trans.transform(new DOMSource(editedDoc), new StreamResult(fw));
            } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
                e.printStackTrace();
            }
        }
    }

    // Проверка наличия врача в модели врачей
    private boolean checkDoctorExists(String doctorName, DefaultTableModel modelDoctors) {
        for (int i = 0; i < modelDoctors.getRowCount(); i++) {
            String existingDoctor = (String) modelDoctors.getValueAt(i, 0);
            if (existingDoctor.equals(doctorName)) {
                return true;
            }
        }
        return false;
    }

    // Очистка таблиц пациентов и врачей
    public void clearTableModel(DefaultTableModel model) {
        synchronized (monitor) {
            if (model != null) {
                model.setRowCount(0);  // Удалить все строки из таблицы
            }
        }
    }

    // Метод для редактирования XML
    private String showEditor(String text) {
        JTextArea textArea = new JTextArea(text);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Редактирование XML", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return textArea.getText(); // Возвращаем отредактированный текст
        } else {
            return text; // Если пользователь нажал "Отмена", возвращаем исходный текст
        }
    }


}