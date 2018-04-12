<%@ page contentType="text/html;charset=utf-8" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Успех</title>
</head>
<body>
Вход выполнен
<jsp:useBean id="user" type="client.Client" scope="application"/>
<br>Пользователь: <%= user.getLogin()%><br>

</body>
</html>