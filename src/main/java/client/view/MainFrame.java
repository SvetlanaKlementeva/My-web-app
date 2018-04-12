package client.view;

import client.Client;
import client.controller.AlarmThread;
import common.controller.TaskList;
import common.model.Constants;
import common.model.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Timer;

public class MainFrame extends JFrame {
    private JPanel panelMain;
    private JLabel lbl;
    private JList<Task> list1;
    private JButton btShowAll;
    private JButton btAdd;
    private JButton btDelete;
    private JButton btEdit;
    private JButton btShowActive;
    private JButton btShowNotActive;
    private JScrollPane scrollPane;

    private TaskList taskList;

    private enum CurrentOutput {ACTIVE, NOT_ACTIVE, ALL}

    private CurrentOutput currentOutput;
    private Client client;

    public MainFrame(Client client) {
        currentOutput = CurrentOutput.ALL;
        this.client = client;
        try {
            this.taskList = new TaskList(client.getAllTasks());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        startAlarm();

        //Действия по закрытию приложения
        //Сохранение
        this.addWindowListener(new WindowListener() {
            public void windowClosing(WindowEvent event) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }

            public void windowActivated(WindowEvent event) {
            }

            public void windowClosed(WindowEvent event) {
            }

            public void windowDeactivated(WindowEvent event) {
            }

            public void windowDeiconified(WindowEvent event) {
            }

            public void windowIconified(WindowEvent event) {
            }

            public void windowOpened(WindowEvent event) {
            }
        });

        showTaskList();

        //Чтобы фон активных задач отличался от не активных
        list1.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list,
                                                          Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);

                Task task = (Task) value;
                if (!task.isActive()) {
                    setBackground(Color.GRAY);
                }
                return this;
            }
        });

        // Задаем активность кнопок
        btDelete.setEnabled(false);
        btEdit.setEnabled(false);
        list1.addListSelectionListener(e -> {
            btDelete.setEnabled(true);
            btEdit.setEnabled(true);
        });

        btShowActive.addActionListener(event -> {
            showList(true);
            currentOutput = CurrentOutput.ACTIVE;
        });
        btShowNotActive.addActionListener(event -> {
            showList(false);
            currentOutput = CurrentOutput.NOT_ACTIVE;
        });
        btShowAll.addActionListener(event -> showList());

        //Удаление Task
        btDelete.addActionListener(event -> {
            Object[] options = {"Да", "Нет!"};
            int n = JOptionPane
                    .showOptionDialog(this, "Вы хотите удалить выбранную задачу?",
                            "Удаление", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);
            if (n == 0) {
                Task selectedTask = list1.getSelectedValue();
                try {
                    client.deleteTask(selectedTask);
                    taskList.deleteTask(selectedTask);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }

                update();
            }
        });
        btAdd.addActionListener(event -> new AddTaskFrame(taskList, this));
        btEdit.addActionListener(event -> new AddTaskFrame(taskList, list1.getSelectedValue(), this));

        setContentPane(panelMain);
        setTitle("MainFrame");

        //Устанавливаем формочку по центру и задаём её начальный размер
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int sizeWidth = Constants.MAIN_FRAME_WIDTH;
        int sizeHeight = Constants.MAIN_FRAME_HEIGHT;
        int locationX = (screenSize.width - sizeWidth) / 2;
        int locationY = (screenSize.height - sizeHeight) / 2;
        setBounds(locationX, locationY, sizeWidth, sizeHeight);

        setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public Client getClient() {
        return client;
    }

    private void startAlarm() {
        new Timer().schedule(new AlarmThread(taskList, this), 1000, 2000);
    }

    private void showTaskList() {
        String text = "Ваш список задач:";
        lbl.setText(text);
        showList();

    }

    private void showList() {
        // Добавляем все задач из taskList'a в список
        DefaultListModel<Task> dfm = new DefaultListModel<>();
        for (int i = 0; i < taskList.getTaskList().size(); i++) {
            dfm.addElement(taskList.getTaskList().get(i));
        }
        list1.setModel(dfm);

        btDelete.setEnabled(false);
        btEdit.setEnabled(false);
    }

    private void showList(boolean active) {
        DefaultListModel<Task> dfm = new DefaultListModel<>();
        for (Task currentTask : taskList.getTaskList(active)) {
            dfm.addElement(currentTask);
        }
        list1.setModel(dfm);

        btDelete.setEnabled(false);
        btEdit.setEnabled(false);
    }

    public void update() {
        if (currentOutput == CurrentOutput.ALL)
            showList();
        else if (currentOutput == CurrentOutput.ACTIVE)
            showList(true);
        else if (currentOutput == CurrentOutput.NOT_ACTIVE)
            showList(false);
    }
}
