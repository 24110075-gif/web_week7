<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Murach's Java Servlets & JSP - SQL Gateway</title>
    <link rel="stylesheet" href="styles/main.css" type="text/css" />
</head>

<body>

    <div class="container" style="max-width: 920px; margin: 2rem auto;">

        <header class="app-header">
            <div class="app-title">
                <h1>SQL Gateway</h1>
                <p>Jakarta Persistence (JPA) &amp; Jakarta Mail</p>
            </div>
            <div class="db-badge">
                <span class="db-dot"></span>
                <span>PostgreSQL (JPA)</span>
            </div>
        </header>

        <div class="app-body">

            <section class="email-section" style="padding: 1.5rem; background: #ffffff; border-radius: 8px; border: 1px solid #cbd5e1; margin-bottom: 1.75rem;">
                <c:choose>
                    <c:when test="${sessionScope.emailVerified == true}">
                        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                            <div>
                                <h3 style="margin: 0 0 4px 0; font-size: 1.1rem; color: #166534;">Đã xác thực Email thành công</h3>
                                <p style="margin: 0; font-size: 0.9rem; color: #334155;">
                                    Tài khoản: <strong>${sessionScope.user.firstName} ${sessionScope.user.lastName}</strong> (${sessionScope.user.email})
                                </p>
                            </div>
                            <form action="emailList" method="post" style="margin: 0;">
                                <input type="hidden" name="action" value="logout" />
                                <button type="submit" class="btn-secondary" style="font-size: 0.85rem; padding: 6px 14px; cursor: pointer;">
                                    Đăng xuất / Đổi email
                                </button>
                            </form>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <h2 style="font-size: 1.2rem; font-weight: 600; color: #1e293b; margin-top: 0; margin-bottom: 0.35rem;">
                            Bước 1: Kiểm tra / Đăng ký Email
                        </h2>
                        <p style="color: #64748b; font-size: 0.88rem; margin-bottom: 1rem;">
                            Vui lòng kiểm tra email của bạn trước. Nếu email đã có trong database thì sẽ được phép truy vấn SQL, nếu chưa có sẽ tự động đăng ký mới.
                        </p>

                        <form action="emailList" method="post" class="email-form">
                            <input type="hidden" name="action" value="add" />

                            <div style="display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 0.75rem; align-items: end;">
                                <div>
                                    <label for="email" class="form-label" style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.25rem;">Email:</label>
                                    <input type="email" id="email" name="email" value="${user.email}" required style="width: 100%; padding: 0.55rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem;" placeholder="nhapemail@example.com" />
                                </div>
                                <div>
                                    <label for="firstName" class="form-label" style="display: block; font-size: 0.85rem; font-weight: 500; margin-bottom: 0.25rem;">Tên (Nêu đ/ký mới):</label>
                                    <input type="text" id="firstName" name="firstName" value="${user.firstName}" style="width: 100%; padding: 0.55rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem;" placeholder="John" />
                                </div>
                                <div>
                                    <label for="lastName" class="form-label" style="display: block; font-size: 0.85rem; font-weight: 500; margin-bottom: 0.25rem;">Họ (Nếu đ/ký mới):</label>
                                    <input type="text" id="lastName" name="lastName" value="${user.lastName}" style="width: 100%; padding: 0.55rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem;" placeholder="Smith" />
                                </div>
                            </div>

                            <div style="margin-top: 0.85rem;">
                                <button type="submit" class="btn-primary" style="width: 100%; height: 38px; font-size: 0.95rem; font-weight: 600; cursor: pointer;">
                                    Kiểm tra &amp; Mở khóa SQL Gateway
                                </button>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>

                <c:if test="${not empty message}">
                    <div class="result-box result-info" style="margin-top: 0.85rem; margin-bottom: 0; padding: 0.75rem 1rem; border-radius: 6px; background-color: #e0f2fe; color: #0369a1; font-weight: 500;">
                        ${message}
                    </div>
                </c:if>
                <c:if test="${not empty emailStatus}">
                    <div class="result-box result-success" style="margin-top: 0.85rem; margin-bottom: 0; padding: 0.75rem 1rem; border-radius: 6px; background-color: #dcfce7; color: #15803d; font-weight: 500;">
                        ${emailStatus}
                    </div>
                </c:if>
                <c:if test="${not empty emailError}">
                    <div class="result-box result-error" style="margin-top: 0.85rem; margin-bottom: 0; padding: 0.75rem 1rem; border-radius: 6px; background-color: #fee2e2; color: #b91c1c; font-weight: 500;">
                        ${emailError}
                    </div>
                </c:if>
            </section>

            <section class="sql-section" style="padding: 1.5rem; background: #ffffff; border-radius: 8px; border: 1px solid #cbd5e1;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                    <h2 style="font-size: 1.25rem; font-weight: 600; color: #0f172a; margin: 0;">
                        Bước 2: Thực thi câu lệnh SQL
                    </h2>
                    <c:choose>
                        <c:when test="${sessionScope.emailVerified == true}">
                            <span style="font-size: 0.82rem; padding: 3px 10px; border-radius: 12px; background: #dcfce7; color: #166534; font-weight: 600;">🟢 Đã mở khóa</span>
                        </c:when>
                        <c:otherwise>
                            <span style="font-size: 0.82rem; padding: 3px 10px; border-radius: 12px; background: #fee2e2; color: #991b1b; font-weight: 600;">🔴 Đang khóa (Cần check email ở Bước 1)</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <form action="sqlGateway" method="post">
                    <div class="form-group">
                        <label for="sqlStatement" class="form-label">Nhập câu lệnh SQL (SELECT / INSERT / UPDATE / DELETE):</label>
                        <c:choose>
                            <c:when test="${sessionScope.emailVerified == true}">
                                <textarea id="sqlStatement" name="sqlStatement" class="sql-textarea" rows="5" placeholder="VD: SELECT * FROM users">${sqlStatement}</textarea>
                            </c:when>
                            <c:otherwise>
                                <textarea id="sqlStatement" name="sqlStatement" class="sql-textarea" rows="5" disabled style="background-color: #f1f5f9; cursor: not-allowed;" placeholder="Hãy nhập email và kiểm tra ở Bước 1 trước để mở khóa khung nhập SQL này...">${sqlStatement}</textarea>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="form-actions">
                        <c:choose>
                            <c:when test="${sessionScope.emailVerified == true}">
                                <button type="submit" class="btn-primary">Thực thi SQL</button>
                                <button type="button" class="btn-secondary" onclick="document.getElementById('sqlStatement').value=''">Xóa</button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn-primary" disabled style="opacity: 0.6; cursor: not-allowed;">
                                    Thực thi SQL (Yêu cầu xác thực Email)
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </form>

                <c:if test="${not empty sqlResult}">
                    <div class="result-section">
                        <h3 class="result-heading">Kết quả SQL:</h3>
                        ${sqlResult}
                    </div>
                </c:if>
            </section>

        </div>

        <footer class="app-footer" style="margin-top: 1.5rem;">
            <span>Murach's Java Servlets and JSP &bull; SQL Gateway</span>
            <span>Jakarta Persistence (JPA) &bull; Jakarta Mail</span>
        </footer>
    </div>
</body>

</html>