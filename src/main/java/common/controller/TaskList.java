package common.controller;

import common.model.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskList implements Serializable {
    private List<Task> taskList;

    public TaskList() {
        this.taskList = new ArrayList<>();
    }

    public TaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public List<Task> getTaskList(boolean active) {
        List<Task> newTaskList = new ArrayList<>();
        for (Task task : taskList) {
            if (task.isActive() == active) {
                newTaskList.add(task);
            }
        }
        return newTaskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void addTask(Task task) {
        taskList.add(task);
    }

    public void deleteTask(Task task) {
        taskList.remove(task);
    }

    //Откладывание задачи
    public void postpone(Task task, Calendar dateTime) {
        editTask(task, dateTime, true);
    }

    //Ставит задачу в неактивное состояние
    public void complete(Task task) {
        editTask(task, task.getDateTime(), false);
    }

    private void editTask(Task task, Calendar dateTime, boolean active) {
        taskList.remove(task);
        task.setDateTime(dateTime);
        task.setActive(active);
        taskList.add(task);
    }

    public void updateTask(Task task){
        for (Task currTask: taskList){
            if (currTask.getId() == task.getId()){
               currTask.setName(task.getName());
               currTask.setActive(task.isActive());
               currTask.setDateTime(task.getDateTime());
               currTask.setContacts(task.getContacts());
               currTask.setInfo(task.getInfo());
               return;
            }
        }
        addTask(task);
    }

    public boolean isExist(Task task) {
        return taskList.contains(task);
    }
}
