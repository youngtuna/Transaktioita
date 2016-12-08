<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Lainausten lis�ys</title>
</head>
<body>
<h1>LIS�� LAINAUS</h1>
<p>
</p>
<form action="NiteenLainausOhjelma" method="get">
Valitse Lainaaja: <select name="asiakas">

<c:forEach items="${asiakkaat}" var= "asiakas">
<option value="${asiakas.numero}">
<c:out value ="${asiakas.numero}" />&nbsp;
<c:out value ="${asiakas.etunimi}" />&nbsp;<c:out value ="${asiakas.sukunimi}" />
</option>
</c:forEach>
</select>
<div>
<c:forEach items="${niteet}" var="nide">
<input type="checkbox" name="nide" value="${nide.kirja.isbn} ${nide.nidenro}">
<c:out value="${nide.kirja.isbn}" /> &nbsp;
<c:out value="${nide.nidenro}" /> &nbsp;
<c:out value="${nide.kirja.nimi}" /> &nbsp;
<c:out value="${nide.kirja.kirjoittaja}" /> &nbsp;
<br />
</input>
</c:forEach>
</div>
<p>
<input type="submit" name="action" value="vahvista lainaus" />&nbsp;&nbsp;
<input type="submit" name="action" value="peruuta lainaus" />
</p>
</form>
</body>
</html>