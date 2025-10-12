function showSnackbar(message) {
    const snackbar = $("#snackbar");
    snackbar.text(message).addClass("show");
    setTimeout(() => snackbar.removeClass("show"), 3000);
}

function handleServerError(jqXHR) {
    console.log(jqXHR);
    // 서버에서 내려준 메시지 활용 (JSON 응답인 경우)
    if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
        showSnackbar(jqXHR.responseJSON.message);
    }
}

// 페이징 렌더링
function renderPagination(containerId, pageInfo, onPageChange) {
    const container = $("#" + containerId);
    container.empty();

    // if (pageInfo.totalPages <= 1) return; // 한 페이지면 페이징 필요 없음

    const pagination = $('<ul class="pagination justify-content-center"></ul>');

    // 이전 페이지
    const prevLi = $('<li class="page-item"></li>');
    const prevLink = $('<a class="page-link" href="#">이전</a>');
    if (pageInfo.first) prevLi.addClass("disabled");
    prevLink.on("click", (e) => {
        e.preventDefault();
        if (!pageInfo.first) onPageChange(pageInfo.page - 1);
    });
    prevLi.append(prevLink);
    pagination.append(prevLi);

    // 페이지 번호
    for (let i = 1; i <= pageInfo.totalPages; i++) {
        const li = $('<li class="page-item"></li>');
        if (i === pageInfo.page) li.addClass("active");
        const link = $(`<a class="page-link" href="#">${i}</a>`);
        link.on("click", (e) => {
            e.preventDefault();
            if (i !== pageInfo.page) onPageChange(i);
        });
        li.append(link);
        pagination.append(li);
    }

    // 다음 페이지
    const nextLi = $('<li class="page-item"></li>');
    const nextLink = $('<a class="page-link" href="#">다음</a>');
    if (pageInfo.last) nextLi.addClass("disabled");
    nextLink.on("click", (e) => {
        e.preventDefault();
        if (!pageInfo.last) onPageChange(pageInfo.page + 1);
    });
    nextLi.append(nextLink);
    pagination.append(nextLi);

    container.append(pagination);
}

function getCurrentPage(containerId) {
    const activeLink = $(`#${containerId} .page-item.active a`);
    if (activeLink.length) {
        return parseInt(activeLink.text(), 10);
    }
    return 1; // 기본 1페이지
}

// 알림창 렌더링
function renderNotificationList(notifications) {
    const container = $('#notification-list');
    container.empty();

    if (!notifications || notifications.length === 0) {
        container.append('<li class="text-center text-muted">새로운 알림이 없습니다.</li>');
        return;
    }

    notifications.forEach(n => {
        const itemHtml = `
            <li>
                <span class="notification-message">${n.message}</span>
                <span class="notification-time">${formatDate(n.createdAt)}</span>
            </li>
        `;
        container.append(itemHtml);
    });
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', { hour12: false });
}

function formatDateTime(isoString) {
    const date = new Date(isoString);
    return date.toLocaleString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
}

$(".bell-icon").css("cursor", "pointer").on("click", function() {
    window.location.href = "/notification";
});