package common.model;

import common.controller.IdManager;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Formatter;

public class Task implements Serializable, Comparable<Task> {
    private int id;
    private String name;
    private String info;
    private Calendar dateTime;
    private String contacts;
    private boolean active;

    public Task() {
        id = 0;
        name = "";
        info = "";
        dateTime = Calendar.getInstance();
        contacts = "";
        active = false;
    }

    public Task(String name, String info, Calendar dateTime, String contacts, Boolean active) {
        this.id = IdManager.getId();
        this.name = name;
        this.info = info;
        this.dateTime = dateTime;
        this.contacts = contacts;
        this.active = active;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public void setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        String str = null;

        if (!active) str = "✓";
        else str = "   ";

        Formatter dateTimeString = new Formatter();
        dateTimeString.format(Constants.DATE_TIME_FORMAT, dateTime, dateTime, dateTime, dateTime, dateTime);

        return str
                + "  "
                + name
                + " ["
                + info
                + " (c) "
                + contacts
                + "]             "
                + dateTimeString.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;

        Task task = (Task) o;

        if (name != null ? !name.equals(task.name) : task.name != null) return false;
        if (info != null ? !info.equals(task.info) : task.info != null) return false;
        return (dateTime != null ? dateTime.equals(task.dateTime) : task.dateTime == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (info != null ? info.hashCode() : 0);
        result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
        result = 31 * result + (contacts != null ? contacts.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Task o) {
        if (this.getDateTime().before(o.getDateTime())) return -1;
        if (this.getDateTime().after(o.getDateTime())) return 1;
        return 0;
    }

}
