let equipmentId = null;
let equipmentItemId = null;

const statusLabels = {
    AVAILABLE: "사용 가능",
    RENTED: "대여 중",
    REPAIRING: "수리 중",
    OUT_OF_STOCK: "재고 없음",
    LOST: "분실"
};

$(document).ready(function () {
    // URL에서 equipmentId 추출
    const pathParts = window.location.pathname.split("/");
    equipmentId = pathParts[3];
    equipmentItemId  = pathParts[5];

    // 최초 조회
    fetchHistory();
});

function fetchHistory(page = 1, size = 10) {
    $.ajax({
        url: `/api/v1/equipment-items/${equipmentItemId}/history?page=${page}&size=${size}`,
        method: 'GET',
    }).done(function (response) {
        renderHistoryList(response.data.content, response.data.page, response.data.size, response.data.totalElements);
        renderPagination("history-pagination",{
            page: response.data.page,       // 1-based
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, fetchHistory);
    }).fail(function(xhr) {
        handleServerError(xhr)
    })
}

function renderHistoryList(content, page, size, totalElements) {
    const listDiv = $('#history-list');
    listDiv.empty();

    if (!content || content.length === 0) {
        listDiv.append('<p class="text-muted">해당 장비 아이템의 히스토리가 존재하지 않습니다.</p>');
        return;
    }

    content.forEach((item, index) => {
        // 역순 시퀀스 계산
        const sequence = totalElements - ((page - 1) * size + index);

        let extraInfo = "";

        if (item.newStatus === "RENTED") {
            extraInfo = `
                <p><strong>대여자:</strong> ${item.rentedUserName} (${item.rentedUserDept})</p>
            `;
        } else if (item.oldStatus === "RENTED" && item.newStatus === "AVAILABLE") {
            extraInfo = `
                <p><strong>대여자:</strong> ${item.rentedUserName} (${item.rentedUserDept})</p>
                <p><strong>대여 시작일:</strong> ${item.rentalStartDate || "-"}</p>
                <p><strong>실 반납일:</strong> ${item.actualReturnDate || "-"}</p>
            `;
        }

        const card = `
            <div class="card mb-3">
                <div class="card-body">
                    <p><strong>순서:</strong> ${sequence}</p>
                    <p><strong>이전 상태:</strong> ${statusLabels[item.oldStatus]}</p>
                    <p><strong>변경 상태:</strong> ${statusLabels[item.newStatus]}</p>
                    <p><strong>변경자:</strong> ${item.changedBy}</p>
                    ${extraInfo}
                    <p><strong>변경 시간:</strong> ${item.createdAt}</p>
                </div>
            </div>
        `;
        listDiv.append(card);
    });
}