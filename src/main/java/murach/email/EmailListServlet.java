package murach.email;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import jakarta.mail.MessagingException;
import murach.business.User;

@WebServlet("/emailList")
public class EmailListServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String url = "/index.jsp";

        String action = request.getParameter("action");
        if (action == null) {
            action = "join";
        }

        if (action.equals("logout")) {
            session.removeAttribute("user");
            session.removeAttribute("emailSent");
            session.invalidate();
            url = "/index.jsp";
        } else if (action.equals("add") || action.equals("join") || action.equals("login")) {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");

            String message;

            if (email == null || email.trim().isEmpty()) {
                message = "Vui lòng nhập địa chỉ email.";
            } else {
                email = email.trim();
                if (firstName == null || firstName.trim().isEmpty()) {
                    firstName = "Thành viên";
                }
                if (lastName == null || lastName.trim().isEmpty()) {
                    lastName = "Mới";
                }

                User user = new User(firstName.trim(), lastName.trim(), email);

                String to = email;
                String from = "cutcho385@gmail.com";
                String subject = "Xác nhận đăng ký - Murach Email System";
                String body = "Xin chào " + user.getFirstName() + " " + user.getLastName() + ",\n\n" +
                        "Cảm ơn bạn đã tham gia hệ thống của chúng tôi.\n" +
                        "Email của bạn (" + email + ") đã được xác nhận thành công!\n\n" +
                        "Trân trọng,\n" +
                        "Murach's Java Servlets and JSP";

                try {
                    MailUtil.sendMail(to, from, subject, body, false);
                    request.setAttribute("emailStatus", "Email thông báo đã gửi thành công tới " + to);
                } catch (MessagingException e) {
                    String emailError = "Lỗi gửi email: " + e.getMessage();
                    request.setAttribute("emailError", emailError);
                    System.err.println(emailError);
                }

                message = "Thông tin đã được tiếp nhận thành công!";
                session.setAttribute("user", user);
                session.setAttribute("emailSent", true);
            }
            request.setAttribute("message", message);
        }

        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
