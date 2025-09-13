let equipmentId = null;
let equipmentItemId = null;

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
        url: `/api/v1/equipments/${equipmentId}/items/${equipmentItemId}/history?page=${page}&size=${size}`,
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
        listDiv.append('<p class="text-muted">히스토리가 없습니다.</p>');
        return;
    }

    content.forEach((item, index) => {
        // 역순 시퀀스 계산
        const sequence = totalElements - ((page - 1) * size + index);

        const card = `
            <div class="card mb-3">
                <div class="card-body">
                    <p><strong>순서:</strong> ${sequence}</p>
                    <p><strong>이전 상태:</strong> ${item.oldStatus}</p>
                    <p><strong>변경 상태:</strong> ${item.newStatus}</p>
                    <p><strong>변경자:</strong> ${item.changedBy}</p>
                    <p><strong>변경 시간:</strong> ${item.createdAt}</p>
                </div>
            </div>
        `;
        listDiv.append(card);
    });
}