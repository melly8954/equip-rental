$(document).ready(function() {
    const pathParts = window.location.pathname.split("/");
    const rentalId = pathParts[2];

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
        // r.endDate → Date 객체
        const [y, m, d] = r.endDate.split("-").map(Number);
        const endDate = new Date(y, m - 1, d);
        const now = new Date(); now.setHours(0,0,0,0);

        // 각 배지 초기화
        let badgesHtml = '';
        let actionHtml = '';
        
        // 배지 추가
        if (r.status === "RETURNED") {
            // 반납 완료
            badgesHtml = `<span class="badge bg-success mb-1 d-block">반납 완료</span>`;
            if (r.actualReturnDate) {
                badgesHtml += `<small>(반납: ${r.actualReturnDate})</small>`;
            }
        } else {
            // 반납이 아닐 때만 연체/연장 판단
            if (r.status === "OVERDUE") {
                badgesHtml += `<span class="badge bg-danger mb-1 d-block">연체</span>`;
            }
            if (r.extended) {
                badgesHtml += `<span class="badge bg-warning text-dark mb-1 d-block">연장됨</span>`;
            }

            // 버튼은 대여중일 때만
            if (!r.extended && endDate >= now) {
                actionHtml = `<button class="btn btn-sm btn-outline-primary extend-btn"
                                            data-id="${r.rentalItemId}" data-rental-id="${r.rentalId}">
                                            대여 연장 </button>`;
            }
        }

        // 오른쪽 컬럼에 모든 배지/버튼 표시
        const rightColumnHtml = badgesHtml + actionHtml;

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
                                <p class="d-flex align-items-center mb-1 fw-bold">대여 ID ${r.rentalId}</p>
                                <p class="d-flex align-items-center mb-1 fw-bold">
                                  <span>${r.model}</span>
                                  <span class="text-muted ms-2">[${r.category} - ${r.subCategory}]</span>
                                </p>
                                <i class="mb-0 text-muted">${r.serialName}</i>
                            </h6>
                            <p class="card-text mb-0 text-muted">
                                대여기간: ${r.startDate} ~ ${r.endDate} 
                            </p>
                        </div>
                    </div>
                    <div class="col-auto ps-2 pe-2 text-center">
                        ${rightColumnHtml}
                    </div>
                </div>
            </div>
        </div>
        `);

        row.append(card);

        if ((index + 1) % 2 === 0 || index === data.length - 1) {
            container.append(row);
            row = $('<div class="row"></div>');
        }
    });
}

$(document).on("click", ".extend-btn", function() {
    const rentalItemId = $(this).data("id");
    const rentalId = $(this).data("rental-id");

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
        showSnackbar(response.message);
        fetchRentalItemList(rentalId)
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
});