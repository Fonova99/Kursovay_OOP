import javax.swing.*;

public class TextException extends  Exception{
    public TextException() {
        super("Вы не ввели название заболевания");
    }
    public void checkException(JTextField disease) throws TextException, NullPointerException{
        String str = disease.getText();
        if (str.contains("Введите название заболевания")) throw new TextException();
        if (str.length() == 0) throw new NullPointerException();

    }
}

