<%@ page contentType="text/html;charset=utf-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Ошибка</title>
</head>
<body>
Вход не выполнен!!!!
<jsp:useBean id="error" type="String" scope="application"/>
<br>Что не так: <%= error%><br>
</body>
</html>