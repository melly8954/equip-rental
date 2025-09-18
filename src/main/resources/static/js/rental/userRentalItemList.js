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

// 페이지 로드 또는 뒤로/앞으로가기 시
window.addEventListener("pageshow", function (event) {
    $("input[name='category']").prop("checked", false);
    $("input[name='category'][value='전체']").prop("checked", true);

    $("input[name='subCategory']").prop("checked", false);
    $("input[name='subCategory'][value='전체']").prop("checked", true);

    // 이름 검색 초기화
    $("#model-search").val("");

    // 필터 렌더링
    renderFilter("filter-container", filterConfig, onFilterChange);

    // 초기 데이터 로딩
    fetchRentalItemList();
});

// 이름 검색(input)
$(document).ready(function() {
    // 검색 이벤트
    $("#model-search").on("input", function() {
        fetchRentalItemList();
    });
});

// 필터 변경 시 동작
function onFilterChange() {
    // 현재 선택된 카테고리 값
    const category = getFilterValues(filterConfig).category;

    // 서브카테고리 갱신
    updateSubCategoryOptions(category);

    // 최신 filterConfig 값으로 fetch
    const filters = getFilterValues(filterConfig);
    fetchRentalItemList(filters);
}


// 서브카테고리 업데이트
function updateSubCategoryOptions(parentCategory) {
    const currentValues = getFilterValues(filterConfig);
    const options = subCategoryMap[parentCategory] || [];

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
            subCategory: values.subCategory
        };
        fetchRentalItemList(combinedFilters);
    });
    // 기존 선택값 유지
    if (currentValues.subCategory && options.includes(currentValues.subCategory)) {
        $(`#sub-category-filters input[value="${currentValues.subCategory}"]`).prop("checked", true);
    } else {
        $(`#sub-category-filters input[value="전체"]`).prop("checked", true);
    }
}


// 대여 신청내역 조회 AJAX
function fetchRentalItemList(filters = {}) {
    const filterValues = filters || getFilterValues(filterConfig);
    const modelSearch = $("#model-search").val();
    const params = {
        category: filters.category,
        subCategory: filters.subCategory,
        model: modelSearch || filters.model || "",
    };

    // page가 있으면 추가
    if (filterValues.page) {
        params.page = filterValues.page;
    }

    $.ajax({
        url: "/api/v1/rental-items/me",
        method: "GET",
        data: params,
    }).done(function (response) {
        renderRentalItemList(response.data.content);
        renderPagination("pagination-container", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchRentalItemList({...filterValues, page: newPage});
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
        const overdueBadge = r.overdue
            ? `<span class="badge bg-danger ms-2">연체</span>`
            : '';

        const thumbnail = r.thumbnailUrl
            ? `<img src="${r.thumbnailUrl}" class="img-fluid rounded-start" alt="${r.model}" style="width:100px; height:100px; object-fit:cover;">`
            : `<div class="placeholder-thumbnail d-flex align-items-center justify-content-center bg-light rounded-start" 
                   style="width:100px; height:100px;">No Image</div>`;

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
                            ${thumbnail}
                        </div>
                        <div class="col">
                            <div class="card-body p-2">
                                <h6 class="card-title mb-1">
                                    <p class="d-flex align-items-center mb-1 fw-bold">
                                      <span>${r.model}</span>
                                      <span class="text-muted ms-2">[${categoryLabelMap[r.category]} - ${r.subCategory}]</span>
                                    </p> 
                                    <i class="mb-0 text-muted">${r.serialName}</i>
                                </h6>
                                <p class="card-text mb-0 text-muted">
                                    대여기간: ${r.startDate} ~ ${r.endDate} 
                                    ${r.actualReturnDate ? `(반납: ${r.actualReturnDate})` : ''}
                                    ${overdueBadge}
                                </p>
                            </div>
                        </div>
                        <div class="col-auto pe-2">
                            ${extendBtnHtml}           
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