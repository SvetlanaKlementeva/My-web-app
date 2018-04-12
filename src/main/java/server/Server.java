package server;

import common.controller.TaskList;
import common.model.Task;
import common.model.packet.*;
import server.controller.IOHelper;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

//Сначала серверу необходимо передать Action в виде строки(json).
//
//После, если необходимо, передать в виде строки необходимую информацию
//
//Ответ сервера:
//Сервер всегда возвращает State, говоря о том, как завершилась его работа
//
//Если клиент запросил GET_ALL_TASK и есть хотя бы 1 Task, то после State, сервер вышлет списков Task'ов
//Если же ни одного Task'а ещё нет, то State будет = NO_TASKS и после него больше ничего не последует
public class Server implements Runnable {

    private Socket socket;
    private TaskList taskList;
    private String fileName;
    private String login;
    private ActiveUsers activeUsers;

    public Server(Socket socket) {
        this.socket = socket;
        this.activeUsers = ActiveUsers.getInstance();
        taskList = new TaskList();
    }

    private void sendAnswer(State answer, ObjectOutputStream writer) throws IOException {
        writer.writeObject(answer);
        writer.flush();
    }

    //Шифрует пароль
    private String encrypt(String password, String login) throws NoSuchAlgorithmException {
        // Используем шифрование
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        // Добавляем к паролю соль (пусть будет логин) и зашифровываем
        byte[] encryptPass = sha1.digest((login + password).getBytes());
        // Переводим в 16-ричный вид
        StringBuilder sb = new StringBuilder();
        for (byte b : encryptPass) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    @Override
    public void run() {

        try (ObjectInputStream reader = new ObjectInputStream(new DataInputStream(socket.getInputStream()));
             ObjectOutputStream writer = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()))) {
            while (!socket.isClosed()) {
                Action neededAction = (Action) reader.readObject();
                System.out.println("Server: Получили Action " + neededAction.toString());

                switch (neededAction) {
                    case LOGIN: {
                        //Читаем Login
                        login = (String) reader.readObject();
                        System.out.println("Server: Получили login.");
                        //Читаем пароль
                        String password = (String) reader.readObject();
                        System.out.println("Server: Получили пароль.");

                        //Если в списке юзеров нет полученного логина или пароль не совпадает возвращается ERROR
                        HashMap users = IOHelper.readUsers();
                        if (users.containsKey(login)) {
                            //Проверка на использование данного логина в данный момент
                            if (activeUsers.contains(login)) {
                                sendAnswer(State.LOGIN_USED, writer);
                                System.out.println("Server: Логин(" + login + ") уже используется кем-то.");
                                socket.close();
                                break;
                            }

                            String encryptPassStr = encrypt(password, login);

                            if (users.get(login).equals(encryptPassStr)) {
                                fileName = login + ".json";
                                this.login = login;
                                taskList = IOHelper.readTaskList(fileName);
                                sendAnswer(State.OK, writer);

                                activeUsers.add(login);

                                System.out.println("Server: Пользователь " + login + " вошел");
                            } else {
                                sendAnswer(State.PASSWORD_ERROR, writer);
                                System.out.println("Server: Получили неверный пароль.");
                                socket.close();
                            }
                            break;
                        } else {
                            sendAnswer(State.LOGIN_ERROR, writer);
                            System.out.println("Server: Получили неверный логин.");
                            socket.close();
                            break;
                        }
                    }
                    case REGISTRATION: {
                        //Читаем Login
                        login = (String) reader.readObject();
                        System.out.println("Server: Получили login.");
                        //Читаем пароль
                        String password = (String) reader.readObject();
                        System.out.println("Server: Получили пароль.");
                        HashMap users = IOHelper.readUsers();

                        if (!users.containsKey(login)) {
                            String encryptPassStr = encrypt(password, login);

                            //Заносим в базу логин и пароль
                            users.put(login, encryptPassStr);
                            IOHelper.writeUsers(users);
                            fileName = login + ".json";
                            this.login = login;
                            taskList = new TaskList();
                            IOHelper.writeTaskList(taskList, fileName);
                            sendAnswer(State.OK, writer);

                            activeUsers.add(login);

                            System.out.println("Server: Зарегестрировали нового пользователя " + login);
                        } else {
                            sendAnswer(State.LOGIN_ERROR, writer);
                            System.out.println("Server: Не удалось зарестрировать " + login
                                    + ", т.к. он уже есть в системе");
                            socket.close();
                        }
                        break;
                    }

                    case ADD_TASK: {
                        Task task = (Task) reader.readObject();
                        System.out.println("Server: Получили Task");

                        taskList.addTask(task);
                        System.out.println("Server: Добавил новый таск");

                        sendAnswer(State.OK, writer);
                        break;
                    }

                    case UPDATE_TASK: {
                        Task task = (Task) reader.readObject();
                        System.out.println("Server: Получили новый Task.");
                        taskList.updateTask(task);
                        System.out.println("Server: заменили старый Task на новый.");

                        sendAnswer(State.OK, writer);
                        break;
                    }
                    case DELETE_TASK: {
                        Task task = (Task) reader.readObject();
                        System.out.println("Server: Получили Task.");
                        taskList.deleteTask(task);
                        System.out.println("Server: Удалил Task.");

                        sendAnswer(State.OK, writer);
                        break;
                    }
                    case GET_ALL_TASKS: {
                        List<Task> tasks = taskList.getTaskList();

                        State answer = State.NO_TASKS;
                        if (!tasks.isEmpty()) {
                            answer = State.OK;
                        } else {
                            System.out.println("Server: Тасков нет, ничего не отправил");
                        }

                        sendAnswer(answer, writer);

                        if (answer == State.OK) {
                            writer.writeObject(tasks);
                            writer.flush();
                            System.out.println("Server: Таски отправились");
                        }
                        break;
                    }

                    case COMPLETE_TASK: {
                        Task task = (Task) reader.readObject();
                        System.out.println("Server: Получили Task.");
                        taskList.complete(task);
                        System.out.println("Server: Завершил Task.");

                        sendAnswer(State.OK, writer);
                        break;
                    }

                    case POSTPONE_TASK: {
                        Task task = (Task) reader.readObject();
                        System.out.println("Server: Получили Task.");

                        Calendar newDateTime = (Calendar) reader.readObject();
                        System.out.println("Server: Получили Новое время для таска.");

                        taskList.postpone(task, newDateTime);
                        System.out.println("Server: Отложили таск");

                        sendAnswer(State.OK, writer);
                        break;
                    }

                    case EXIT: {
                        IOHelper.writeTaskList(taskList, fileName);
                        activeUsers.remove(login);
                        System.out.println("Server: Таски записаны в файл");
                        socket.close();
                        break;
                    }
                }

            }
            System.out.println("Server: Работа с клиентом " + login + " завершена");
        } catch (Exception e) {
            System.out.println("Server: Возникла ошибка: " + e.getMessage());
            activeUsers.remove(login);
            try(ObjectOutputStream writer = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()))){
                sendAnswer(State.ERROR, writer);
            }
            catch (IOException ex){
                System.out.println("Server: Не удалось отправить сообщение об ошибке на клиент");
            }
        }
    }

}
