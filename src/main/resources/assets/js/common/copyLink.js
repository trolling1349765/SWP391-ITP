function copyShareLink(el) {
    const link = el.getAttribute("data-link");
    if (!link) {
        console.error("Không tìm thấy link chia sẻ!");
        return;
    }

    navigator.clipboard.writeText(link)
        .then(() => {
            showToast(" Đã sao chép liên kết!", "success");
        })
        .catch(err => {
            showToast(" Sao chép thất bại!", "error");
            console.error("Không thể sao chép link:", err);
        });
}
/**
 * Hiển thị thông báo kiểu toast ở góc phải, mượt, không đè header
 */
function showToast(message, type = "info") {
    let container = document.getElementById("toast-container");
    if (!container) {
        container = document.createElement("div");
        container.id = "toast-container";
        Object.assign(container.style, {
            position: "fixed",
            top: "80px",            //  đẩy xuống dưới header
            right: "16px",
            zIndex: "9999",
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-end",
            gap: "12px",
            pointerEvents: "none"
        });
        document.body.appendChild(container);
    }

    // ✅ Giới hạn chỉ 1 toast tại 1 thời điểm
    while (container.children.length > 0) {
        container.removeChild(container.firstChild);
    }

    const toast = document.createElement("div");
    toast.innerHTML = message; // giữ được emoji hoặc icon

    const bgColor = {
        success: "linear-gradient(135deg, #198754, #2ecc71)",
        error: "linear-gradient(135deg, #dc3545, #ff6b6b)",
        info: "linear-gradient(135deg, #0d6efd, #4dabf7)"
    }[type] || "#0d6efd";

    Object.assign(toast.style, {
        background: bgColor,
        color: "#fff",
        padding: "16px 28px",
        borderRadius: "10px",
        boxShadow: "0 6px 20px rgba(0,0,0,0.25)",
        fontSize: "17px",
        fontWeight: "600",
        fontFamily: "'Inter', sans-serif",
        letterSpacing: "0.3px",
        minWidth: "280px",
        textAlign: "center",
        transform: "translateX(150%)",
        opacity: "0",
        transition: "transform 0.6s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.4s ease"
    });

    container.appendChild(toast);

    //  Hiệu ứng trượt vào
    requestAnimationFrame(() => {
        toast.style.transform = "translateX(0)";
        toast.style.opacity = "1";
    });

    //  Tự ẩn mượt mà
    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(150%)";
        toast.style.transition = "transform 0.8s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.6s ease";
        setTimeout(() => toast.remove(), 800);
    }, 2500);
}
