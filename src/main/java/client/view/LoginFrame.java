package client.view;

import client.Client;
import common.model.Constants;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.net.ConnectException;

public class LoginFrame extends JFrame {

    private JPanel panelMain;

    private JLabel lableLogin;
    private JLabel lbPassword;
    private JTextField tfLogin;
    private JButton btEnter;
    private JPasswordField pfPassword;
    private JButton btRegistration;

    public LoginFrame() {
        btEnter.addActionListener(event -> login(false));
        btRegistration.addActionListener(event -> login(true));

        //Ставим фильтор, чтобы можно было вводить только буквенные или цифровое символ или знак подчёркивания
        PlainDocument doc = (PlainDocument) tfLogin.getDocument();
        doc.setDocumentFilter(new TextFieldFilter());

        //Устанавливаем формочку по центру и задаём её начальный размер
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int sizeWidth = Constants.LOGIN_FRAME_WIDTH;
        int sizeHeight = Constants.LOGIN_FRAME_HEIGHT;
        int locationX = (screenSize.width - sizeWidth) / 2;
        int locationY = (screenSize.height - sizeHeight) / 2;
        setBounds(locationX, locationY, sizeWidth, sizeHeight);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panelMain);
        setTitle("LoginFrame");
        setVisible(true);
    }

    private void login(boolean newUser) {
        String login = tfLogin.getText();
        String password = new String(pfPassword.getPassword());
        if (login.isEmpty())
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите сначала логин",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
        else if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите сначала пароль",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
        } else {
            try {
                new MainFrame(new Client(login, password, newUser));
                this.dispose();
            } catch (ConnectException e) {
                JOptionPane.showMessageDialog(this, "Сервер не был найден... " +
                        "Повторите попытку позже.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class TextFieldFilter extends DocumentFilter {
        // \\w - буквенный или цифровой символ или знак подчёркивания
        private static final String SYMBOLS = "\\w";

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {

            if (string.matches(SYMBOLS)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            if (string.matches(SYMBOLS)) {
                super.replace(fb, offset, length, string, attrs);
            }
        }
    }
}
