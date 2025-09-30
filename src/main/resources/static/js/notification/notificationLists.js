const loadMoreBtn = $("#load-more-btn");
const listContainer = $("#notification-list");

let currentPage = 1;
const pageSize = 10;
let lastPage = false;
let notificationStatus = ""; // 기본값: 전체

$(document).ready(function () {
    // 초기 로드
    fetchNotifications(currentPage, pageSize, notificationStatus);

    // Load More 버튼
    loadMoreBtn.on("click", function () {
        if (!lastPage) {
            fetchNotifications(currentPage + 1, pageSize, notificationStatus);
        }
    });

    // 필터 라디오 변경 이벤트
    $("input[name='filter']").on("change", function () {
        notificationStatus = $(this).val(); // 선택된 value 가져오기
        currentPage = 1;
        lastPage = false;
        listContainer.empty(); // 기존 목록 초기화
        fetchNotifications(currentPage, pageSize, notificationStatus);
    });
});

function fetchNotifications(page, pageSize, notificationStatus) {
    const params = new URLSearchParams({
        page: page,
        size: pageSize,
        notificationStatus: notificationStatus
    });

    $.ajax({
        url: `/api/v1/notifications?${params.toString()}`,
        method: "GET",
    }).done(function (response) {
        console.log(response);
        renderNotifications(response.data);
        currentPage = response.data.page;
        lastPage = response.data.last;

        if (lastPage) {
            loadMoreBtn.hide();
        } else {
            loadMoreBtn.show();
        }
    }).fail(function (jqXHR) {
        handleServerError(jqXHR);
    });
}

function renderNotifications(pageData) {
    const notifications = pageData.content;
    const listContainer = $("#notification-list");

    if (notifications.length === 0 && currentPage === 1) {
        listContainer.html(`
            <div class="text-center text-muted py-4">
                알림이 없습니다.
            </div>
        `);
        $("#load-more-btn").hide();
        return;
    }

    notifications.forEach(n => {
        let statusBadge = n.status === "UNREAD"
            ? `<span class="badge bg-danger">안읽음</span>`
            : `<span class="badge bg-secondary">읽음</span>`;

        // UNREAD 상태일 때만 버튼 생성
        let readBtn = n.status === "UNREAD"
            ? `<button class="btn btn-sm btn-outline-primary mark-read-btn" data-id="${n.notificationId}">읽음 처리</button>`
            : ""; // READ이면 버튼 없음

        const row = $(`
            <div class="d-flex text-center border-bottom py-2">
                <div class="col-3 n-status">${statusBadge}</div>
                <div class="col-4">${n.message}</div>
                <div class="col-2">${readBtn}</div>
                <div class="col-3">${formatDateTime(n.createdAt)}</div>
            </div>
        `);

        listContainer.append(row);
    });
}

$(document).on("click", ".mark-read-btn", function() {
    const notificationId = $(this).data("id");

    $.ajax({
        url: `/api/v1/notifications/${notificationId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({
            notificationStatus: "READ"
        })
    }).done(() => {
        // 알림 상태 배지만 변경
        $(this).closest("div.d-flex").find(".n-status").html(`<span class="badge bg-secondary">읽음</span>`);
        $(this).remove(); // 버튼 제거
    }).fail(jqXHR => {
        handleServerError(jqXHR);
    });
});