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

    //Метод для записи данных врачей в XML-файл
    public void recordXmlFileDoctors(DefaultTableModel modelDoctors) {
        synchronized (monitor) {
            FileDialog save = new FileDialog((Frame) null, "Запись данных", FileDialog.SAVE); //диалоговое окно по сохранению файлов
            save.setFile("*.xml"); //тип файла
            save.setVisible(true);
            String fileName = save.getDirectory() + save.getFile(); //сохранение пути к файлу
            if (fileName.isEmpty()) {
                return;
            }

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); //Создается объект DocumentBuilder, который используется для создания и разбора XML-документов
                Document doc = builder.newDocument(); //Создается новый пустой XML-документ
                Node administration = doc.createElement("Administration"); //Создается элемент <Administration> в XML-документе
                doc.appendChild(administration); //Добавляет созданный элемент <Administration> в корневой элемент документа

                for (int i = 0; i < modelDoctors.getRowCount(); i++) { //Проходим по всем строкам таблицы modelDoctors
                    Element visit = doc.createElement("visit"); //Для каждой строки создается элемент <visit>
                    administration.appendChild(visit); //Добавляет созданный элемент <visit> в элемент <Administration>
                    //Устанавливаются атрибуты для элемента <visit>. Каждый атрибут соответствует значению из соответствующей ячейки таблицы modelDoctors
                    visit.setAttribute("doctor", (String) modelDoctors.getValueAt(i, 0));
                    visit.setAttribute("speciality", (String) modelDoctors.getValueAt(i, 1));
                    visit.setAttribute("cabinet", (String) modelDoctors.getValueAt(i, 2));
                    visit.setAttribute("workSchedule", (String) modelDoctors.getValueAt(i, 3));
                }

                Transformer transformer = TransformerFactory.newInstance().newTransformer(); //Создается объект Transformer, который используется для преобразования XML-документа в строку или файл
                //Устанавливаются свойства вывода для Transformer, чтобы XML-документ был отформатирован с отступами (4 пробела на каждый уровень вложенности)
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                StringWriter writer = new StringWriter(); //Создается объект StringWriter, который будет использоваться для записи XML-документа в строку
                transformer.transform(new DOMSource(doc), new StreamResult(writer)); //Преобразует XML-документ в строку и записывает его в StringWriter
                String xmlString = writer.toString(); // Получает строковое представление XML-документа

                String editedXml = showEditor(xmlString); //отображение окна редактирования
                //Создается новый XML-документ из отредактированной строки XML
                Document editedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(editedXml.getBytes()));

                Transformer trans = TransformerFactory.newInstance().newTransformer(); //Создается новый объект Transformer для записи XML-документа в файл
                FileWriter fw = new FileWriter(fileName); //Создается объект FileWriter, который будет использоваться для записи XML-документа в файл
                trans.transform(new DOMSource(editedDoc), new StreamResult(fw)); //Преобразует XML-документ в файл, используя FileWriter
            } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
                e.printStackTrace();
            }
            db.updateDoctors(modelDoctors); //обновление базы данных
        }
    }

    //Метод для чтения данных врачей из XML-файла
    public void readXmlFileDoctors(DefaultTableModel modelDoctors) {
        synchronized (monitor) {
            FileDialog read = new FileDialog((Frame) null, "Чтение данных", FileDialog.LOAD); //диалоговое окно по загрузке файлов
            read.setFile("*.xml"); //тип файла
            read.setVisible(true);
            String fileName = read.getDirectory() + read.getFile(); //название файла
            if (fileName.isEmpty()) {
                return;
            }

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); //Создается объект DocumentBuilder, который используется для создания и разбора XML-документов
                doc = builder.parse(new File(fileName)); //Разбирает XML-файл, расположенный по пути fileName, и создает объект Document, представляющий XML-документ
                doc.getDocumentElement().normalize(); //Нормализует XML-документ, чтобы убедиться, что все текстовые узлы находятся в правильном состоянии

                NodeList visits = doc.getElementsByTagName("visit"); //Создается объект NodeList, который содержит все узлы с тегом <visit> в XML-документе
                modelDoctors.setRowCount(0); // Очистка таблицы

                for (int i = 0; i < visits.getLength(); i++) { //Проходимся по всем узлам в NodeList
                    Node elem = visits.item(i); //Получает текущий узел
                    NamedNodeMap attrs = elem.getAttributes(); //Получает атрибуты текущего узла <visit>
                    //Извлекает значения атрибутов из коллекции атрибутов и сохраняет их в соответствующих переменных
                    String doctor = attrs.getNamedItem("doctor").getNodeValue();
                    String speciality = attrs.getNamedItem("speciality").getNodeValue();
                    String cabinet = attrs.getNamedItem("cabinet").getNodeValue();
                    String workSchedule = attrs.getNamedItem("workSchedule").getNodeValue();
                    modelDoctors.addRow(new String[]{doctor, speciality, cabinet, workSchedule});
                }
                db.updateDoctors(modelDoctors); //обновление базы данных
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Метод для записи данных пациентов в XML-файл
    public void recordXmlFilePatients(DefaultTableModel modelPatients, DefaultTableModel modelDoctors) {
        synchronized (monitor) {
            FileDialog save = new FileDialog((Frame) null, "Запись данных", FileDialog.SAVE); //диалоговое окно по сохранению файлов
            save.setFile("*.xml"); //тип файла
            save.setVisible(true);
            String fileName = save.getDirectory() + save.getFile(); //название файла
            if (fileName.isEmpty()) {
                return;
            }

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); //Создается объект DocumentBuilder, который используется для создания и разбора XML-документов
                Document doc = builder.newDocument(); //Создается новый пустой XML-документ
                Node administration = doc.createElement("Administration"); //Создается элемент <Administration> в XML-документе
                doc.appendChild(administration); //Добавляет созданный элемент <Administration> в корневой элемент документа

                for (int i = 0; i < modelPatients.getRowCount(); i++) { //Проходим по всем строкам таблицы modelPatients
                    Element visit = doc.createElement("visit"); //Для каждой строки создается элемент <visit>
                    administration.appendChild(visit); //Добавляет созданный элемент <visit> в элемент <Administration>
                    //Устанавливаются атрибуты для элемента <visit>. Каждый атрибут соответствует значению из соответствующей ячейки таблицы modelPatients
                    visit.setAttribute("doctor", (String) modelPatients.getValueAt(i, 0));
                    visit.setAttribute("patient", (String) modelPatients.getValueAt(i, 1));
                    visit.setAttribute("disease", (String) modelPatients.getValueAt(i, 2));
                }

                Transformer transformer = TransformerFactory.newInstance().newTransformer(); //Создается объект Transformer, который используется для преобразования XML-документа в строку или файл
                //Устанавливаются свойства вывода для Transformer, чтобы XML-документ был отформатирован с отступами (4 пробела на каждый уровень вложенности)
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                StringWriter writer = new StringWriter(); //Создается объект StringWriter, который будет использоваться для записи XML-документа в строку
                transformer.transform(new DOMSource(doc), new StreamResult(writer)); //Преобразует XML-документ в строку и записывает его в StringWriter
                String xmlString = writer.toString(); // Получает строковое представление XML-документа

                String editedXml = showEditor(xmlString); //отображение окна редактирования
                //Создается новый XML-документ из отредактированной строки XML
                Document editedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(editedXml.getBytes()));

                NodeList visits = editedDoc.getElementsByTagName("visit"); //Создается объект NodeList, который содержит все узлы с тегом <visit> в XML-документе editedDoc
                for (int i = 0; i < visits.getLength(); i++) { //Проходимся по всем узлам в NodeList
                    Node visit = visits.item(i); //Получает текущий узел <visit> из списка по индексу i
                    NamedNodeMap attrs = visit.getAttributes(); //Получает атрибуты текущего узла <visit>. NamedNodeMap — это коллекция атрибутов узла
                    String editedDoctor = attrs.getNamedItem("doctor").getNodeValue(); //Извлекает значение атрибута doctor из коллекции атрибутов и сохраняет

                    if (!checkDoctorExists(editedDoctor, modelDoctors)) { //Окно с ошибкой и возможностью нажать "ОК"
                        int response = JOptionPane.showConfirmDialog(null, "Врач " + editedDoctor + " не найден в списке врачей. Хотите добавить нового врача?", "Ошибка", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);

                        if (response == JOptionPane.OK_OPTION) { //Если пользователь нажал "ОК", показываем текстовые поля для ввода нового врача
                            JTextField doctorField = new JTextField(editedDoctor);
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

                                //если поля не были заполнены, то появляется окно с ошибкой
                                if (newDoctorName.isEmpty() || specialty.isEmpty() || office.isEmpty() || schedule.isEmpty()) {
                                    JOptionPane.showMessageDialog(null, "Все поля должны быть заполнены. Запись отменена.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                modelDoctors.addRow(new Object[]{newDoctorName, specialty, office, schedule}); //Добавление нового врача в модель врачей
                            } else {
                                return; //Прерываем запись, если пользователь отменил ввод
                            }
                        } else {
                            return; //Прерываем запись, если пользователь отказался от добавления нового врача
                        }
                    }
                }

                Transformer trans = TransformerFactory.newInstance().newTransformer(); //Создается новый объект Transformer для записи XML-документа в файл
                FileWriter fw = new FileWriter(fileName); //Создается объект FileWriter, который будет использоваться для записи XML-документа в файл
                trans.transform(new DOMSource(editedDoc), new StreamResult(fw)); //Преобразует XML-документ в файл, используя FileWriter

            } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
                e.printStackTrace();
            }
            db.updatePatients(modelPatients); //обновление базы данных
        }
    }

    //Метод для чтения данных пациентов из XML-файла
    public void readXmlFilePatients(DefaultTableModel modelPatients) {
        synchronized (monitor) {
            FileDialog read = new FileDialog((Frame) null, "Чтение данных", FileDialog.LOAD); //диалоговое окно по загрузке файлов
            read.setFile("*.xml"); //тип файла
            read.setVisible(true);
            String fileName = read.getDirectory() + read.getFile(); //название файла
            if (fileName.isEmpty()) {
                return;
            }

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); //Создается объект DocumentBuilder, который используется для создания и разбора XML-документов
                doc = builder.parse(new File(fileName)); //Разбирает XML-файл, расположенный по пути fileName, и создает объект Document, представляющий XML-документ
                doc.getDocumentElement().normalize(); //Нормализует XML-документ, чтобы убедиться, что все текстовые узлы находятся в правильном состоянии

                NodeList visits = doc.getElementsByTagName("visit"); //Создается объект NodeList, который содержит все узлы с тегом <visit> в XML-документе
                modelPatients.setRowCount(0); // Очистка таблицы перед добавлением данных

                for (int i = 0; i < visits.getLength(); i++) { //Проходимся по всем узлам в NodeList
                    Node elem = visits.item(i); //Получает текущий узел
                    NamedNodeMap attrs = elem.getAttributes(); //Получает атрибуты текущего узла <visit>
                    //Извлекает значения атрибутов из коллекции атрибутов и сохраняет их в соответствующих переменных
                    String doctor = attrs.getNamedItem("doctor").getNodeValue();
                    String patient = attrs.getNamedItem("patient").getNodeValue();
                    String disease = attrs.getNamedItem("disease").getNodeValue();
                    modelPatients.addRow(new String[]{doctor, patient, disease});
                }
                db.updatePatients(modelPatients); //Обновляем базу данных пациентов
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Метод для пока окна редактирования
    String showEditor(String text) {
        JTextArea textArea = new JTextArea(text); //текстовое поле для редактирования
        JScrollPane scrollPane = new JScrollPane(textArea); //прокрутка
        scrollPane.setPreferredSize(new Dimension(800, 400)); //размер скроллируемой панели
        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Редактирование XML", JOptionPane.OK_CANCEL_OPTION);
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