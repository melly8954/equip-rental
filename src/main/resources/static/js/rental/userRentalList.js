const filterConfig = {
    category: {
        label: "카테고리",
        type: "radio",
        options: [
            "전체",
            "OFFICE_SUPPLIES",
            "ELECTRONICS",
            "FURNITURE",
            "TOOLS",
            "SAFETY_EQUIPMENT"
        ]
    },
    subCategory: {
        label: "서브카테고리",
        type: "radio",
        options: ["전체"] // 초기값은 전체, 카테고리 선택 시 동적으로 바뀜
    },
    status: {
        label: "신청 상태",
        type: "radio",
        options: ["전체", "PENDING", "APPROVED", "REJECTED"]
    }
};

const categoryLabelMap = {
    "전체": "전체",
    "OFFICE_SUPPLIES": "사무용품",
    "ELECTRONICS": "전자기기",
    "FURNITURE": "가구",
    "TOOLS": "공구",
    "SAFETY_EQUIPMENT": "안전장비"
};

const subCategoryMap = {
    OFFICE_SUPPLIES: [
        "문서 파쇄기",
        "라벨프린터",
        "프로젝트 보드"
    ],
    ELECTRONICS: [
        "노트북",
        "태블릿",
        "프로젝터",
        "모니터",
        "프린터",
        "카메라/캠코더",
        "오디오장비(스피커/마이크)",
        "외장저장장치(SSD/HDD)"
    ],
    FURNITURE: [
        "사무용 의자",
        "책상/테이블",
        "서랍장/캐비닛",
        "이동식 파티션",
        "화이트보드"
    ],
    TOOLS: [
        "전동공구(드릴, 그라인더)",
        "수공구(망치, 드라이버)",
        "측정도구(레이저측정기, 콤파스)",
        "납땜장비"
    ],
    SAFETY_EQUIPMENT: [
        "안전모",
        "안전화",
        "보호안경/귀마개",
        "방진마스크",
        "소화기/응급키트"
    ]
};

const statusLabelMap = {
    "전체": "전체",
    "PENDING": "대기 중",
    "APPROVED": "대여 승인",
    "REJECTED": "대여 거절"
};

// 페이지 조회
let currentPage = 1;

// 페이지 로드 또는 뒤로/앞으로가기 시
window.addEventListener("pageshow", function(event) {
    $("input[name='category']").prop("checked", false);
    $("input[name='category'][value='전체']").prop("checked", true);

    $("input[name='subCategory']").prop("checked", false);
    $("input[name='subCategory'][value='전체']").prop("checked", true);

    $("input[name='status']").prop("checked", false);
    $("input[name='status'][value='전체']").prop("checked", true);

    // 필터 렌더링
    renderFilter("category-filters", { category: filterConfig.category }, onFilterChange);
    renderFilter("sub-category-filters", { subCategory: filterConfig.subCategory }, onFilterChange);
    renderFilter("status-filters", { status: filterConfig.status }, onFilterChange);

    // 초기 데이터 로딩
    fetchRentalList();
});

// 이름 검색(input)
$("#member-search").on("input", function() {
    currentPage = 1;
    fetchRentalList();
});

// 필터 변경 시 동작
function onFilterChange(values) {
    // 현재 선택된 카테고리 값
    const category = getFilterValues(filterConfig).category;

    // 서브카테고리 갱신
    updateSubCategoryOptions(category);

    // 최신 filterConfig 값으로 fetch
    const filters = getFilterValues(filterConfig);
    fetchRentalList(filters);
}


// 서브카테고리 업데이트
function updateSubCategoryOptions(parentCategory) {
    const currentValues = getFilterValues(filterConfig); // 현재 체크된 값 저장
    const options = ["전체"].concat(subCategoryMap[parentCategory] || []);

    filterConfig.subCategory.options = options;

    const container = $("#sub-category-filters");
    container.empty();
    renderFilter("sub-category-filters", {
        subCategory: {
            type: "radio",
            options: options
        }
    }, (values) => {
        // category 필터도 함께 포함
        const combinedFilters = {
            category: currentValues.category,
            subCategory: values.subCategory,
            status: currentValues.status
        };
        fetchRentalList(combinedFilters);
    });

    // 렌더링 후 기존 선택값 복원
    const prevSub = currentValues.subCategory || "";
    $(`#sub-category-filters input[value="${prevSub}"]`).prop("checked", true);
}


// 대여 신청내역 조회 AJAX
function fetchRentalList(filters={}) {
    const filterValues = filters || getFilterValues(filterConfig);
    const params = {
        category: filters.category,
        subCategory: filters.subCategory,
        rentalStatus: filters.status,
    };

    // page가 있으면 추가
    if (filterValues.page) {
        params.page = filterValues.page;
    }

    $.ajax({
        url: "/api/v1/rentals/me",
        method: "GET",
        data: params,
    }).done(function (response){
        renderRentalList(response.data.content);
        renderPagination("pagination-container", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchRentalList({...filterValues, page: newPage});
        });
    }).fail(function (xhr) {
        handleServerError(xhr)
    })
}

// div 리스트 렌더링
function renderRentalList(data) {
    const container = $("#rental-list");
    container.empty();

    if (!data || data.length === 0) {
        container.append(`<div class="text-center py-3">아이템이 없습니다.</div>`);
        return;
    }

    let row = $('<div class="row"></div>');

    data.forEach((r, index) => {
        const thumbnail = r.thumbnailUrl
            ? `<img src="${r.thumbnailUrl}" class="img-fluid rounded-start" alt="${r.equipmentName}" 
                     style="width:120px; height:120px; object-fit:cover;">`
            : `<div class="placeholder-thumbnail d-flex align-items-center justify-content-center bg-light rounded-start" 
                   style="width:120px; height:120px;">No Image</div>`;

        const card = $(`
            <div class="col-md-6 mb-3">
                <div class="card shadow-sm h-100">
                    <div class="row g-0 align-items-center">
                        <div class="col-auto">
                            ${thumbnail}
                        </div>
                        <div class="col">
                            <div class="card-body p-2">
                                <h6 class="card-title mb-1">
                                    <p class="mb-0 fw-bold">${r.model}</p>
                                    <p class="mb-0 text-muted">
                                        ${categoryLabelMap[r.category]} / ${r.subCategory || '-'}
                                    </p>
                                </h6>
                                <p class="card-text mb-1">
                                    신청 ID: ${r.rentalId} <br>
                                    수량: ${r.quantity} <br>
                                    대여 기간: ${r.requestStartDate || ""} ~ ${r.requestEndDate || ""} <br>
                                    상태: ${r.status} <br>
                                    ${r.status === "REJECTED" ? `거절 사유: ${r.rejectReason}` : ""}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `);

        row.append(card);

        // 2개마다 row append 후 새 row 시작
        if ((index + 1) % 2 === 0 || index === data.length - 1) {
            container.append(row);
            row = $('<div class="row"></div>');
        }
    });

    // 반납 처리 버튼 이벤트 바인딩
    $(".return-btn").off("click").on("click", function() {
        const rentalItemId = $(this).data("id");
        handleReturn(rentalItemId, $(this));
    });
}

