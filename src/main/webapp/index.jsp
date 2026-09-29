<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Murach's Java Servlets & JSP - Email System</title>
    <link rel="stylesheet" href="styles/main.css" type="text/css" />
</head>

<body>

    <div class="container" style="max-width: 650px; margin: 3rem auto;">

        <header class="app-header">
            <div class="app-title">
                <h1>Hệ Thống Gửi Email</h1>
                <p>Jakarta Mail &amp; Resend HTTP API (Chapter 14)</p>
            </div>
            <div class="db-badge">
                <span class="db-dot"></span>
                <span>Active Mail Service</span>
            </div>
        </header>

        <div class="app-body">

            <section class="email-section" style="padding: 1.75rem; background: #ffffff; border-radius: 8px; border: 1px solid #cbd5e1;">
                
                <h2 style="font-size: 1.25rem; font-weight: 600; color: #1e293b; margin-top: 0; margin-bottom: 0.35rem;">
                    Đăng Ký &amp; Gửi Email
                </h2>
                <p style="color: #64748b; font-size: 0.9rem; margin-bottom: 1.25rem;">
                    Nhập thông tin bên dưới để gửi email xác nhận tự động.
                </p>

                <c:if test="${not empty message}">
                    <div class="result-box result-info" style="margin-bottom: 1rem; padding: 0.75rem 1rem; border-radius: 6px; background-color: #e0f2fe; color: #0369a1; font-weight: 500;">
                        ${message}
                    </div>
                </c:if>
                <c:if test="${not empty emailStatus}">
                    <div class="result-box result-success" style="margin-bottom: 1rem; padding: 0.75rem 1rem; border-radius: 6px; background-color: #dcfce7; color: #15803d; font-weight: 500;">
                        ${emailStatus}
                    </div>
                </c:if>
                <c:if test="${not empty emailError}">
                    <div class="result-box result-error" style="margin-bottom: 1rem; padding: 0.75rem 1rem; border-radius: 6px; background-color: #fee2e2; color: #b91c1c; font-weight: 500;">
                        ${emailError}
                    </div>
                </c:if>

                <form action="emailList" method="post" class="email-form">
                    <input type="hidden" name="action" value="add" />

                    <div style="margin-bottom: 1rem;">
                        <label for="email" class="form-label" style="display: block; font-size: 0.9rem; font-weight: 600; margin-bottom: 0.35rem;">Địa chỉ Email:</label>
                        <input type="email" id="email" name="email" value="${user.email}" required style="width: 100%; padding: 0.65rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem;" placeholder="nhapemail@example.com" />
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.25rem;">
                        <div>
                            <label for="firstName" class="form-label" style="display: block; font-size: 0.9rem; font-weight: 500; margin-bottom: 0.35rem;">Tên (First Name):</label>
                            <input type="text" id="firstName" name="firstName" value="${user.firstName}" style="width: 100%; padding: 0.65rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem;" placeholder="John" />
                        </div>
                        <div>
                            <label for="lastName" class="form-label" style="display: block; font-size: 0.9rem; font-weight: 500; margin-bottom: 0.35rem;">Họ (Last Name):</label>
                            <input type="text" id="lastName" name="lastName" value="${user.lastName}" style="width: 100%; padding: 0.65rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem;" placeholder="Smith" />
                        </div>
                    </div>

                    <div>
                        <button type="submit" class="btn-primary" style="width: 100%; height: 42px; font-size: 1rem; font-weight: 600; cursor: pointer;">
                            Gửi Email Xác Nhận
                        </button>
                    </div>
                </form>

            </section>

            <div style="display: none;">
                <section class="sql-section">
                    <form action="sqlGateway" method="post">
                        <textarea id="sqlStatement" name="sqlStatement">${sqlStatement}</textarea>
                        <button type="submit">Thực thi SQL</button>
                    </form>
                    <c:if test="${not empty sqlResult}">
                        <div>${sqlResult}</div>
                    </c:if>
                </section>
            </div>

        </div>

        <footer class="app-footer" style="margin-top: 2rem;">
            <span>Murach's Java Servlets and JSP &bull; Email System</span>
            <span>Jakarta Mail &bull; Resend HTTP API</span>
        </footer>
    </div>
</body>

</html>