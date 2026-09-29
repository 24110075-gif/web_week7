package murach.email;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import jakarta.mail.MessagingException;
import murach.business.User;
import murach.data.UserDB;

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
            session.removeAttribute("emailVerified");
            session.invalidate();
            url = "/index.jsp";
        } else if (action.equals("add") || action.equals("login")) {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");

            String message;

            if (email == null || email.trim().isEmpty()) {
                message = "Vui lòng nhập địa chỉ email để xác thực.";
            } else {
                email = email.trim();
                
                try {
                    User existingUser = UserDB.selectUser(email);
                    
                    if (existingUser != null) {
                        message = "Xác thực thành công! Chào mừng trở lại " 
                                + existingUser.getFirstName() + " " + existingUser.getLastName() 
                                + ". Quyền truy cập SQL Gateway đã được mở khóa.";
                        
                        session.setAttribute("user", existingUser);
                        session.setAttribute("emailVerified", true);
                    } else {
                        if (firstName == null || firstName.trim().isEmpty()) {
                            firstName = "Thành viên";
                        }
                        if (lastName == null || lastName.trim().isEmpty()) {
                            lastName = "Mới";
                        }

                        User newUser = new User(firstName.trim(), lastName.trim(), email);
                        UserDB.insert(newUser);
                        
                        message = "Tài khoản chưa có trong hệ thống. Đã tự động đăng ký mới và mở khóa SQL Gateway!";

                        String to = email;
                        String from = "cutcho385@gmail.com";
                        String subject = "Xác thực tài khoản thành công - Murach SQL Gateway";
                        String body = "Xin chào " + newUser.getFirstName() + " " + newUser.getLastName() + ",\n\n" +
                                "Cảm ơn bạn đã đăng ký tham gia hệ thống của chúng tôi.\n" +
                                "Tài khoản của bạn đã được kích hoạt và quyền sử dụng SQL Gateway đã sẵn sàng.\n\n" +
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

                        session.setAttribute("user", newUser);
                        session.setAttribute("emailVerified", true);
                    }
                } catch (Exception e) {
                    message = "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage();
                    System.err.println("Database error in EmailListServlet: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            request.setAttribute("message", message);
        }

        try {
            List<User> users = UserDB.selectUsers();
            request.setAttribute("users", users);
        } catch (Exception ignored) {}

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
