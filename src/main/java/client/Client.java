package client;

import common.model.Constants;
import common.model.Task;
import common.model.packet.*;
import common.model.packet.Action;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Client implements Closeable {

    private Socket socket;
    private ObjectOutputStream serverWriter;
    private ObjectInputStream serverReader;
    private String login;

    public Client(String login, String password, boolean newUser) throws Exception {
        socket = new Socket("localhost", Constants.SERVER_PORT);
        serverWriter = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()));
        serverReader = new ObjectInputStream(new DataInputStream(socket.getInputStream()));
        //Ждём ответа от сервера 5 секунд
        socket.setSoTimeout(5000);
        this.login = login;
        if (newUser) {
            register(login, password);
        } else {
            login(login, password);
        }
    }

    public String getLogin() {
        return login;
    }

    //Отпрравляет action(действие) серверу, которое он хочет выполнить и необходимые данные, если нужно
    //Возвращает результат(state) работы сервера
    private State sendRequest(Action action, Object... objects) throws IOException, ClassNotFoundException {
        serverWriter.writeObject(action);
        serverWriter.flush();

        for (Object obj : objects) {
            serverWriter.writeObject(obj);
            serverWriter.flush();
        }

        try {
            return (State) serverReader.readObject();
        }
        catch (SocketTimeoutException ex){
            close();
            throw new SocketTimeoutException("Не получили ответ от сервера!");
        }
    }

    public void login(String login, String password) throws Exception {

        State answerFromServer = sendRequest(Action.LOGIN, login, password);
        switch (answerFromServer) {
            case LOGIN_ERROR:
                throw new Exception("Неверный логин!");
            case PASSWORD_ERROR:
                throw new Exception("Неверный пароль!");
            case LOGIN_USED:
                throw new Exception("Уже кто-то зашёл в систему под данным логином."
                        + "\nПовторите попытку позже.");
            case OK:
                System.out.println("Client: Успешно вошли, сервер ответил OK");
                break;
        }
    }

    public void register(String login, String password) throws Exception {
        State answerFromServer = sendRequest(Action.REGISTRATION, login, password);
        if (answerFromServer == State.LOGIN_ERROR) {
            throw new Exception("Логин уже занят. Придумайте новый.");
        }
        System.out.println("Client: Пользователь зарегистрирован, сервер ответил OK");
    }

    public void addTask(Task task) throws Exception {
        System.out.println("Client: Посылаем запрос на добавление");
        State answerFromServer = sendRequest(Action.ADD_TASK, task);
        if (answerFromServer != State.OK) {
            close();
            throw new Exception("Client: Таск не добавился!!!");
        }
        System.out.println("Client: Таск добавился");
    }

    public void completeTask(Task task) throws Exception {
        System.out.println("Client: Посылаем запрос на завершение");

        State answerFromServer = sendRequest(Action.COMPLETE_TASK, task);
        if (answerFromServer != State.OK) {
            close();
            throw new Exception("Client: Не удалось завершить задачу!!!");
        }
        System.out.println("Client: Таск был отмечен как завершённый");
    }

    public void updateTask(Task newTask) throws Exception {
        System.out.println("Client: Посылаем запрос на обновление таска");
        State answerFromServer = sendRequest(Action.UPDATE_TASK, newTask);
        if (answerFromServer != State.OK) {
            close();
            throw new Exception("Client: Не удалось обновить таск!!!");
        }
        System.out.println("Client: Заменили таск на " + newTask);
    }

    public void postponeTask(Task task, Calendar newDateTime) throws Exception {
        System.out.println("Client: Посылаем запрос на откладывание");
        State answerFromServer = sendRequest(Action.POSTPONE_TASK, task, newDateTime);
        if (answerFromServer != State.OK) {
            close();
            throw new Exception("Client: Таск не отложился!!!");
        }
        System.out.println("Client: Таск отложился");
    }

    public void deleteTask(Task task) throws Exception {
        System.out.println("Client: Посылаем запрос на удаление");
        State answerFromServer = sendRequest(Action.DELETE_TASK, task);
        if (answerFromServer != State.OK) {
            close();
            throw new Exception("Client: Таск не удалился!!!");
        }
        System.out.println("Client: Удалили таск " + task.toString());
    }

    public List<Task> getAllTasks() throws Exception {
        List<Task> tasks = null;

        System.out.println("Client: Посылаем запрос на все таски");
        State answerFromServer = sendRequest(Action.GET_ALL_TASKS);
        if (answerFromServer == State.OK) {
            tasks = (ArrayList<Task>) serverReader.readObject();
            System.out.println("Client: Таски приняты");
        } else if(answerFromServer == State.NO_TASKS){
            tasks = new ArrayList<>();
        }
        else {
            close();
            throw new Exception("Client: Ошибка на сервере!");
        }

        return tasks;
    }

    @Override
    public void close() throws IOException {
        System.out.println("Client: Сматываем удочки");
        serverWriter.writeObject(Action.EXIT);
        serverWriter.flush();

        serverReader.close();
        serverWriter.close();
        socket.close();
    }
}
