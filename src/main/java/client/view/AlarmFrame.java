package client.view;

import client.Client;
import client.controller.AlarmThread;
import common.controller.TaskList;
import common.model.Constants;
import common.model.Task;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;

public class AlarmFrame extends JFrame {
    private Task task;
    private TaskList taskList;
    private AlarmThread parent;
    private MainFrame mainFrame;

    private JPanel panelMain;
    private JButton btCompleteTask;
    private JButton btPostponeTask;

    private JRadioButton rb5Minute;
    private JRadioButton rb10Minute;
    private JRadioButton rb30Minute;
    private JRadioButton rbHour;
    private JRadioButton rbDay;
    private JRadioButton rbManually;
    private ButtonGroup radioButtonGroup;

    private JLabel lTaskInfo;
    private JFormattedTextField formattedTextField;

    public AlarmFrame(Task task, TaskList taskList, AlarmThread parent, MainFrame mainFrame, Client client) {
        this.task = task;
        this.taskList = taskList;
        this.parent = parent;
        this.mainFrame = mainFrame;
        groupRadioButton();
        showTask();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parent.update(AlarmFrame.this);
                dispose();
            }
        });

        //Добавляем слушателей на RadioButton
        Enumeration<AbstractButton> buttons = radioButtonGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton b = buttons.nextElement();
            if (b != rbManually)
                b.addActionListener(event -> formattedTextField.setEnabled(false));
            else
                b.addActionListener(event -> formattedTextField.setEnabled(true));
        }

        //Устанавливаем MaskFormatter на FormattedTextField
        MaskFormatter mf = null;
        try {
            mf = new MaskFormatter(Constants.DATE_TIME_MASK);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mf.setPlaceholderCharacter('_');
        DefaultFormatterFactory dff = new DefaultFormatterFactory(mf);
        formattedTextField.setFormatterFactory(dff);

        //Завершаем Task
        btCompleteTask.addActionListener(event -> {
            try {
                taskList.complete(task);
                client.completeTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }

            parent.update(AlarmFrame.this);
            mainFrame.update();
            dispose();
            //parent.update(this);
        });

        //Откладываем Task
        btPostponeTask.addActionListener(event -> {
            boolean isCorrect = true;
            Calendar newDate = Calendar.getInstance();
            int minutes = 0;
            if (rb5Minute.isSelected())
                minutes = Constants.FIVE_MINUTES;
            else if (rb10Minute.isSelected())
                minutes = Constants.TEN_MINUTES;
            else if (rb30Minute.isSelected())
                minutes = Constants.HALF_AN_HOUR;
            else if (rbHour.isSelected())
                minutes = Constants.HOUR;
            else if (rbDay.isSelected())
                minutes = Constants.DAY;
            else if (rbManually.isSelected()) {
                try {
                    formattedTextField.commitEdit();
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_PATTERN);
                    sdf.setLenient(false);
                    Date date = sdf.parse((String) formattedTextField.getValue());

                    //Сравниваем введённое время с текущим
                    if (new Date().after(date)) {
                        JOptionPane.showMessageDialog(this,
                                "Невозможно отложить задачу в прошлое!");
                        isCorrect = false;
                    }

                    newDate.setTime(date);
                } catch (ParseException | DateTimeException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(),
                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                    isCorrect = false;
                }
            }
            if (isCorrect) {
                newDate.add(Calendar.MINUTE, minutes);
                newDate.set(Calendar.SECOND, 0);
                try {
                    client.postponeTask(task, newDate);
                    taskList.postpone(task, newDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                parent.update(AlarmFrame.this);
                mainFrame.update();
                dispose();
                //parent.update(this);
            }
        });

        setContentPane(panelMain);
        setTitle("AlarmFrame");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int sizeWidth = Constants.ALARM_FRAME_WIDTH;
        int sizeHeight = Constants.ALARM_FRAME_HEIGHT;
        int locationX = (screenSize.width - sizeWidth) / 2;
        int locationY = (screenSize.height - sizeHeight) / 2;
        setBounds(locationX, locationY, sizeWidth, sizeHeight);

        setVisible(true);
    }


    private void groupRadioButton() {
        radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add(rb5Minute);
        radioButtonGroup.add(rb10Minute);
        radioButtonGroup.add(rb30Minute);
        radioButtonGroup.add(rbHour);
        radioButtonGroup.add(rbDay);
        radioButtonGroup.add(rbManually);
    }

    private void showTask() {
        Calendar dateTime = task.getDateTime();
        Formatter dataTimeString = new Formatter();
        dataTimeString.format(Constants.DATE_TIME_FORMAT, dateTime, dateTime, dateTime, dateTime, dateTime);

        String text = "<html>"
                + task.getName() + "<br>"
                + task.getInfo() + "<br>"
                + dataTimeString.toString() + "<br><br>"
                + "Отложить на:"
                + "</html>";
        lTaskInfo.setText(text);
    }
}