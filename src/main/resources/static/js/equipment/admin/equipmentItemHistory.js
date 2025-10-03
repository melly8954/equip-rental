let equipmentId = null;
let equipmentItemId = null;

const statusLabels = {
    AVAILABLE: "사용 가능",
    RENTED: "대여 중",
    REPAIRING: "수리 중",
    OUT_OF_STOCK: "재고 없음",
    LOST: "분실"
};

const statusClassMap = {
    AVAILABLE: "text-success",
    RENTED: "text-warning",
    REPAIRING: "text-info",
    OUT_OF_STOCK: "text-secondary",
    LOST: "text-danger"
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
    listDiv.addClass('p-3');

    if (!content || content.length === 0) {
        listDiv.append('<p class="text-muted">해당 장비 아이템의 히스토리가 존재하지 않습니다.</p>');
        return;
    }

    content.forEach((item, index) => {
        const sequence = totalElements - ((page - 1) * size + index);
        const formattedDate = new Date(item.createdAt).toLocaleString();

        let extraInfo = "";
        if (item.newStatus === "RENTED") {
            extraInfo = `
                <div class="row mb-1">
                    <div class="col-6"><strong>대여자:</strong> ${item.rentedUserName} (${item.rentedUserDept})</div>
                </div>
            `;
        } else if (item.oldStatus === "RENTED" && item.newStatus === "AVAILABLE") {
            extraInfo = `
                <div class="row mb-1">
                    <div class="col-6"><strong>대여자:</strong> ${item.rentedUserName} (${item.rentedUserDept})</div>
                    <div class="col-6"><strong>대여 시작일:</strong> ${item.rentalStartDate || "-"}</div>
                </div>
                <div class="row mb-1">
                    <div class="col-6"><strong>실 반납일:</strong> ${item.actualReturnDate || "-"}</div>
                </div>
            `;
        }

        const card = `
            <div class="card mb-3 w-75 mx-auto ${index % 2 === 0 ? '' : 'bg-light'}">  
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>순서: ${sequence}</span>
                    <small class="text-muted">${formattedDate}</small>
                </div>
                <div class="card-body">
                    <div class="row mb-1">
                        <div class="col-6"><strong>이전 상태:</strong> <span class="${statusClassMap[item.oldStatus]}">${statusLabels[item.oldStatus]}</span></div>
                        <div class="col-6"><strong>변경 상태:</strong> <span class="${statusClassMap[item.newStatus]}">${statusLabels[item.newStatus]}</span></div>
                    </div>
                    <div class="row mb-1">
                        <div class="col-6"><strong>변경자:</strong> ${item.changedBy}</div>
                        ${extraInfo ? '' : ''}
                    </div>
                    ${extraInfo}
                </div>
            </div>
        `;

        listDiv.append(card);
    });
}