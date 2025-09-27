$(document).ready(function() {
    const pathParts = window.location.pathname.split("/");
    const rentalId = pathParts[2];

    fetchReturnedRentalItems(rentalId);
});

// 반납 완료 아이템 조회
function fetchReturnedRentalItems(rentalId) {
    const params = {};

    $.ajax({
        url: `/api/v1/rentals/${rentalId}/items/returned`,
        method: "GET",
        data: params,
    }).done(function(response) {
        renderReturnedRentalList(response.data.content);
        renderPagination("pagination", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchReturnedRentalItems({page: newPage});
        });
    }).fail(function(xhr) {
        showSnackbar("반납 내역 조회에 실패했습니다.");
    });
}

function renderReturnedRentalList(items) {
    const container = $("#return-item-list");
    container.empty();

    if (!items || items.length === 0) {
        container.append(`<div class="text-center py-3">반납된 장비 내역이 없습니다.</div>`);
        return;
    }

    let row = $('<div class="row g-3"></div>');

    items.forEach((item, index) => {
        const overdueBadge = item.overdueDays ? `<span class="badge bg-danger ms-2">연체 ${item.overdueDays}일</span>` : "";
        const extendBadge = item.extended ? `<span class="badge bg-warning ms-2">연장</span>` : "";

        const card = $(`
            <div class="col-md-6">
                <div class="card shadow-sm h-100">
                    <div class="row g-0 align-items-center">
                        <div class="col-auto">
                            <img src="${item.thumbnailUrl}"
                                 class="img-fluid rounded-start" 
                                 alt="대표 이미지" 
                                 style="width:100px; height:100px; object-fit:cover;">
                        </div>
                        <div class="col">
                            <div class="card-body">
                                <h6 class="card-title mb-1 fw-bold">
                                    ${item.model} (${item.serialName || "-"}) ${overdueBadge} ${extendBadge}
                                </h6>
                                <p class="card-text mb-1 text-muted">${item.category} / ${item.subCategory}</p>
                                <p class="card-text mb-0">
                                    대여 기간: ${item.startDate} ~ ${item.endDate} <br>
                                    실제 반납일: ${item.actualReturnDate} <br>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `);

        row.append(card);

        // 2개마다 row 나누기
        if ((index + 1) % 2 === 0 || index === items.length - 1) {
            container.append(row);
            row = $('<div class="row g-3"></div>');
        }
    });
}