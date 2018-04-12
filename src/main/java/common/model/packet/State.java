package common.model.packet;

public enum State {
    OK,
    ERROR, LOGIN_ERROR, PASSWORD_ERROR,
    //Возвращает сервер, если такой логин уже используется
    LOGIN_USED,
    //Сервер возвращает, если у клиента нет ещё задач
    NO_TASKS
}
