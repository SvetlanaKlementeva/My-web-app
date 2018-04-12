package servlets;


import client.Client;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class CheckUser extends Dispatcher {
    public String getServletInfo() {
        return "Registration servlet";
    }

    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext ctx = getServletContext();
        if (request.getParameter("log_in")!=null) {
            try {
                Client client = new Client(request.getParameter("login"), request.getParameter("password"), false);
                ctx.setAttribute("user", client);
                this.forward("/success.jsp", request, response);
            } catch (Exception e) {
                ctx.setAttribute("error", e.getMessage());
                this.forward("/err.jsp", request, response);
            }
        }
        else if (request.getParameter("register")!=null){
            try {
                Client client = new Client(request.getParameter("login"), request.getParameter("password"), true);
                ctx.setAttribute("user", client);
                this.forward("/success.jsp", request, response);
            } catch (Exception e) {
                ctx.setAttribute("error", e.getMessage());
                this.forward("/err.jsp", request, response);
            }
        }

    }
}