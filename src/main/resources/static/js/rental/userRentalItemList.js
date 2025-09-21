$(document).ready(function() {
    const pathParts = window.location.pathname.split("/"); // ["", "rental", "60", "item"]
    const rentalId = pathParts[2]; // "60"

    // 초기 데이터 로딩
    fetchRentalItemList(rentalId);
});

// 대여 현황 조회 AJAX
function fetchRentalItemList(rentalId) {

    const params = {};

    $.ajax({
        url: `/api/v1/rentals/${rentalId}/items`,
        method: "GET",
        data: params,
    }).done(function (response) {
        renderRentalItemList(response.data.content);
        renderPagination("pagination", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchRentalItemList({page: newPage});
        });
    }).fail(function (xhr) {
        handleServerError(xhr)
    })
}

// div 리스트 렌더링
function renderRentalItemList(data) {
    const container = $("#rental-item-list");
    container.empty();

    if (!data || data.length === 0) {
        container.append(`<div class="text-center py-3">사용자들의 장비 대여 물품내역이 없습니다.</div>`);
        return;
    }

    let row = $('<div class="row"></div>');

    data.forEach((r, index) => {
        const overdueBadge = r.stats === "OVERDUE"
            ? `<span class="badge bg-danger ms-2">연체</span>`
            : '';

        // 연장 여부 배지
        const extendedBadge = r.extended
            ? `<span class="badge bg-warning text-dark ms-2">연장됨</span>`
            : '';

        // r.endDate 문자열 → Date 객체
        const [y, m, d] = r.endDate.split("-").map(Number);
        const endDate = new Date(y, m - 1, d); // 월은 0부터 시작

        // 오늘 날짜
        const now = new Date();
        now.setHours(0,0,0,0); // 시간 제거

        // 연장 버튼 생성 여부
        const showExtendBtn = !r.extended && endDate >= now;

        // 카드 내부에서 버튼 조건부 생성
        const extendBtnHtml = showExtendBtn
            ? `<button class="btn btn-sm btn-outline-primary extend-btn" data-id="${r.rentalItemId}">
                    대여 연장
                </button>`
            : ''; // 조건에 맞지 않으면 빈 문자열 → 버튼 없음

        const card = $(`
            <div class="col-md-6 mb-3">
                <div class="card shadow-sm h-100">
                    <div class="row g-0 align-items-center">
                        <div class="col-auto">
                            <img src="${r.thumbnailUrl}" class="img-fluid rounded" alt="대표 이미지" style="width:100px; height:100px; object-fit:cover;">
                        </div>
                        <div class="col">
                            <div class="card-body p-2">
                                <h6 class="card-title mb-1">
                                    <p class="d-flex align-items-center mb-1 fw-bold">Rental ${r.rentalId}</p>
                                    <p class="d-flex align-items-center mb-1 fw-bold">
                                      <span>${r.model}</span>
                                      <span class="text-muted ms-2">[${r.category} - ${r.subCategory}]</span>
                                    </p> 
                                    <i class="mb-0 text-muted">${r.serialName}</i>
                                </h6>
                                <p class="card-text mb-0 text-muted">
                                    대여기간: ${r.startDate} ~ ${r.endDate} 
                                    ${r.actualReturnDate ? `(반납: ${r.actualReturnDate})` : ''}
                                    ${overdueBadge}
                                    ${extendedBadge}
                                </p>
                            </div>
                        </div>
                        <div class="col-auto pe-2">
                            ${extendBtnHtml}           
                        </div>
                    </div>
                </div>
            </div>           
        `);

        row.append(card);

        // 2개마다 container에 추가 후 새로운 row 시작
        if ((index + 1) % 2 === 0 || index === data.length - 1) {
            container.append(row);
            row = $('<div class="row"></div>');
        }
    });
}

$(document).on("click", ".extend-btn", function() {
    const rentalItemId = $(this).data("id");

    // confirm 창 띄우기
    const ok = confirm("정말 대여를 연장하시겠습니까?");
    if (!ok) return; // 취소 누르면 종료

    $.ajax({
        url: `/api/v1/rental-items/${rentalItemId}`,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            days: 7
        })
    }).done(function(response) {
        alert(response.message);
        const currentValues = getFilterValues(filterConfig);
        fetchRentalItemList(currentValues);
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
});