const loadMoreBtn = $("#load-more-btn");
const listContainer = $("#notification-list");

let currentPage = 1;
const pageSize = 10;
let lastPage = false;
let notificationStatus = ""; // 기본값: 전체

const notificationTypeLabels = {
    EQUIPMENT_OUT_OF_STOCK: "재고 부족",
    RENTAL_REQUEST: "대여 신청",
    RENTAL_APPROVED: "대여 승인",
    RENTAL_REJECTED: "대여 거절",
    RENTAL_DUE_TOMORROW: "반납 예정일 경고",
    RENTAL_OVERDUE: "대여 연체",
    RENTAL_RETURNED: "반납 완료",
    SYSTEM_ANNOUNCEMENT: "공지사항",
    SUGGESTION_CREATED: "FAQ 등록",
    SUGGESTION_ANSWERED: "문의 답변"
};

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
        status: notificationStatus
    });

    $.ajax({
        url: `/api/v1/notifications?${params.toString()}`,
        method: "GET",
    }).done(function (response) {
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
            : `<span class="badge bg-success">읽음</span>`;

        const typeLabel = notificationTypeLabels[n.type] || n.type;

        // UNREAD 상태일 때만 버튼 생성
        let readBtn = n.status === "UNREAD"
            ? `<button class="btn btn-sm btn-outline-primary mark-read-btn" data-id="${n.notificationId}">읽음 처리</button>`
            : ""; // READ이면 버튼 없음

        const row = $(`
            <div class="d-flex text-center border-bottom py-2">
                <div class="col-2 n-status">${statusBadge}</div>
                <div class="col-2">${typeLabel}</div>
                <div class="col-4">${n.message}</div>
                <div class="col-2">${readBtn}</div>
                <div class="col-2">${formatDateTime(n.createdAt)}</div>
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
        $(this).closest("div.d-flex").find(".n-status").html(`<span class="badge bg-success">읽음</span>`);
        $(this).remove(); // 버튼 제거

        // 사이드바 미열람 알림 배지 감소
        const badge = $('#unread-count-badge');
        let currentCount = parseInt(badge.text()) || 0;
        if(currentCount > 0){
            currentCount -= 1;
            badge.text(currentCount > 99 ? '99+' : currentCount);

            if(currentCount === 0){
                badge.hide(); // 0이면 배지 숨김
            } else {
                badge.show();
            }
        }
    }).fail(jqXHR => {
        handleServerError(jqXHR);
    });
});