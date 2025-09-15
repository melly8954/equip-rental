const filterConfig = {
    department: { label: "부서", type: "radio", options: ["전체", "QA팀", "UI/UX팀", "개발팀","인사팀","인프라팀"] },
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

// 페이지 조회
let currentPage = 1;
const pageSize = 6;

// 페이지 로드 또는 뒤로/앞으로가기 시
window.addEventListener("pageshow", function(event) {
    // 필터 초기화
    $("input[name='department']").prop("checked", false);
    $("input[name='department'][value='전체']").prop("checked", true);

    $("input[name='category']").prop("checked", false);
    $("input[name='category'][value='전체']").prop("checked", true);

    $("input[name='subCategory']").prop("checked", false);
    $("input[name='subCategory'][value='전체']").prop("checked", true);

    // 이름 검색 초기화
    $("#member-search").val("");

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
            department: currentValues.department,
            category: currentValues.category,
            subCategory: values.subCategory
        };
        fetchRentalList(combinedFilters);
    });
    // 기존 선택값 유지
    if (currentValues.subCategory && options.includes(currentValues.subCategory)) {
        $(`#sub-category-filters input[value="${currentValues.subCategory}"]`).prop("checked", true);
    } else {
        $(`#sub-category-filters input[value="전체"]`).prop("checked", true);
    }
}


// 대여 신청내역 조회 AJAX
function fetchRentalList(filters={}) {
    const filterValues = filters || getFilterValues(filterConfig);
    const memberSearch = $("#member-search").val();
    const params = {
        department: filters.department,
        category: filters.category,
        subCategory: filters.subCategory,
        memberName: memberSearch,
        size: pageSize
    };

    // page가 있으면 추가
    if (filterValues.page) {
        params.page = filterValues.page;
    }

    $.ajax({
        url: "/api/v1/rentals",
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

    data.forEach(r => {
        const card = $(`
            <div class="col-md-4">
                <div class="card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">신청 ID: ${r.rentalId}</h5>
                        <p class="card-text">
                            장비 ID: ${r.equipmentId} <br>
                            카테고리: ${r.category} / ${r.subCategory} <br>
                            수량: ${r.quantity} <br>
                            신청 기간: ${r.requestStartDate || ""} ~ ${r.requestEndDate || ""} <br>
                            신청자: ${r.name} (${r.department}) <br>
                            신청일: ${r.createdAt}
                        </p>
                        <div class="d-flex gap-2 mt-3">
                            <button class="btn btn-success btn-approve" data-id="${r.rentalId}">승인</button>
                            <button class="btn btn-danger btn-reject" data-id="${r.rentalId}">거절</button>
                        </div>
                    </div>
                </div>
            </div>
        `);
        container.append(card);
    });
}

