let currentEquipmentId = null;

// 필터 설정 예시
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

// pageshow 이벤트 활용: 뒤로가기/앞으로가기 시에도 실행
window.addEventListener("pageshow", function(event) {
    // 필터 강제 초기화
    $("input[name='category']").prop("checked", false);
    $("input[name='category'][value='전체']").prop("checked", true);

    $("input[name='subCategory']").prop("checked", false);
    $("input[name='subCategory'][value='전체']").prop("checked", true);

    // 검색어 초기화
    $("#equipment-search").val("");

    // 필터 렌더링
    renderFilter("equipment-filters", filterConfig, onFilterChange);

    // 초기 장비 리스트 조회
    fetchEquipment();
});

// 검색 이벤트 등록
$(document).ready(function() {
    $("#equipment-search").on("input", function() {
        fetchEquipment();
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
    fetchEquipment(filters);
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
        fetchEquipment(combinedFilters);
    });
}

// 장비 리스트 조회 함수
function fetchEquipment(filters={}) {
    // 필터 통합
    const filterValues = filters || getFilterValues(filterConfig);
    const modelSearch = $("#equipment-search").val();
    const params = {
        category: filterValues.category || null,
        subCategory: filterValues.subCategory || null,
        model: modelSearch || null
    };

    // page가 있으면 추가
    if (filterValues.page) {
        params.page = filterValues.page;
    }

    $.ajax({
        url: "/api/v1/equipments",
        method: "GET",
        data: params
    }).done(function(response) {
        renderEquipmentList(response.data.content);
        renderPagination("pagination", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchEquipment({...filterValues, page: newPage});
        });
    }).fail(handleServerError);
}

// 장비 리스트 렌더링
function renderEquipmentList(list) {
    const container = $("#equipment-list");
    container.empty();

    if (!list || list.length === 0) {
        container.append(`<div class="text-center py-3">등록된 장비가 존재하지 않습니다.</div>`);
        return;
    }

    // row를 만들어 col을 담는 구조
    let row = $('<div class="row"></div>');

    list.forEach((equip, index) => {
        const card = $(`
            <div class="col-md-6 mb-3">
                <div class="card shadow-sm h-100">
                    <div class="row g-0 align-items-center">
                        <!-- 이미지 영역 -->
                        <div class="col-auto">
                            <img src="${equip.imageUrl}" 
                                 alt="대표 이미지"
                                 style="width:120px; height:120px; object-fit:contain;"
                                 class="rounded-start bg-light p-1">
                        </div>

                        <!-- 텍스트 영역 -->
                        <div class="col">
                            <div class="card-body p-2">
                                <h6 class="card-title mb-1">
                                    <p class="mb-0 fw-bold">${equip.model}</p>
                                    <p class="mb-0 text-muted">
                                        ${categoryLabelMap[equip.category]} / ${equip.subCategory}
                                    </p>
                                </h6>
                                <p class="card-text mb-1">사용 가능 재고: ${equip.availableStock}</p>
                                <p class="card-text mb-0">
                                    총 재고: ${equip.totalStock}
                                    <span class="text-success stock-increase-btn ms-2" 
                                          style="cursor:pointer;" 
                                          data-id="${equip.equipmentId}">
                                        [➕ 재고 추가]
                                    </span>
                                </p>
                            </div>
                        </div>

                        <!-- 버튼 영역 -->
                        <div class="col-auto pe-2">
                            <button class="btn btn-outline-primary btn-sm item-list-btn" 
                                    data-id="${equip.equipmentId}">
                                <i class="bi bi-box-seam"></i> Item List
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `);

        row.append(card);

        // 2개마다 container에 row 추가하고 새로운 row 시작
        if ((index + 1) % 2 === 0 || index === list.length - 1) {
            container.append(row);
            row = $('<div class="row"></div>');
        }
    });
}

// 부모 컨테이너에 이벤트 위임
$(document).on("click", ".item-list-btn", function () {
    const equipmentId = $(this).data("id");

    $.ajax({
        url: `/api/v1/manager-scopes/${equipmentId}`,
        method: "GET"
    }).done(function(response) {
        if (response.data) {
            window.location.href = `/admin/equipment/${equipmentId}/item`;
        } else {
            alert("접근 권한이 없습니다.");
        }
    }).fail(function(xhr) {
        if (xhr.status === 403) {
            alert("접근 권한이 없습니다.");
        } else {
            alert("서버 오류가 발생했습니다.");
        }
    });
});

// 버튼 클릭 시 모달 열기
$(document).on("click", ".stock-increase-btn", function() {
    currentEquipmentId = $(this).data("id");
    // 권한 체크
    $.ajax({
        url: `/api/v1/manager-scopes/${currentEquipmentId}`,
        method: "GET"
    }).done(function(response) {
        if (response.data) {
            $("#stockAmount").val(1);
            $("#stockIncreaseModal").modal("show");
        } else {
            alert("접근 권한이 없습니다.");
        }
    }).fail(function(xhr) {
        if (xhr.status === 403) {
            alert("접근 권한이 없습니다.");
        } else {
            alert("서버 오류가 발생했습니다.");
        }
    });
});

// 모달 확인 시 API 호출
$("#confirmStockIncrease").on("click", function() {
    const amount = parseInt($("#stockAmount").val(), 10);

    $.ajax({
        url: `/api/v1/equipments/${currentEquipmentId}/stock`,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({ amount })
    }).done(function(response) {
        alert(response.message)
        $("#stockIncreaseModal").modal("hide");

        // 현재 필터와 검색어 상태 가져오기
        const currentFilters = getFilterValues(filterConfig);
        fetchEquipment(currentFilters);
    }).fail(function(xhr) {
        handleServerError(xhr);
    })
});