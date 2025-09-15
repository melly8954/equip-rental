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
    renderFilter("filter-container", filterConfig, onFilterChange);

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
            category: getFilterValues(filterConfig).category,
            subCategory: values.subCategory
        };
        fetchRentalList(combinedFilters);
    });
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

    if (!data.length) {
        container.append(`<div class="text-center py-3">아이템이 없습니다.</div>`);
        return;
    }

    data.forEach(r => {
        const card = $(`
            <div class="card mb-3 shadow-sm">
                <div class="row g-0 align-items-center">
                    ${r.thumbnailUrl ? `
                    <div class="col-auto">
                        <img src="${r.thumbnailUrl}" class="img-fluid rounded-start" alt="${r.equipmentName}" style="width:120px; height: 120px; object-fit:cover;">
                    </div>` : ""}
                    <div class="col">
                        <div class="card-body py-2">
                            <h5 class="card-title mb-1">${r.model}</h5>
                            <p class="card-text mb-0">
                                신청 ID: ${r.rentalId} <br>
                                카테고리: ${r.category} / ${r.subCategory} <br>
                                수량: ${r.quantity} <br>
                                대여 기간: ${r.requestStartDate || ""} ~ ${r.requestEndDate || ""} <br>
                                상태: ${r.status} <br>
                                ${r.status === "REJECTED" ? `거절 사유: ${r.rejectReason}` : ""}
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        `);
        container.append(card);
    });
}

