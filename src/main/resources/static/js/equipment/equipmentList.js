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

$(document).ready(function() {
    // 검색 이벤트
    $("#equipment-search").on("input", function() {
        fetchEquipment();
    });
});

// 필터 변경 시 동작
function onFilterChange(values) {
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
        container.append(`<div class="text-center py-3">장비가 없습니다.</div>`);
        return;
    }

    list.forEach(equip => {
        const card = $(`
            <div class="card mb-3">
                <div class="row g-0 h-100">
                    <!-- 이미지 영역 고정 -->
                    <div class="col-md-2 d-flex align-items-center justify-content-center bg-light" style="height: 168px;">
                        <img src="${equip.imageUrl}"  
                             style="max-width: 100%; max-height: 100%; object-fit: contain;" 
                             alt="대표 이미지">
                    </div>
            
                    <!-- 텍스트 영역 -->
                    <div class="col-md-10 d-flex align-items-center">
                        <div class="card-body">
                            <h5 class="card-title">${equip.model}</h5>
                            <p class="card-text">카테고리: ${categoryLabelMap[equip.category]}</p>
                            <p class="card-text">서브카테고리: ${equip.subCategory || '-'}</p>
                            <p class="card-text">재고: ${equip.availableStock}</p>
                        </div>
                        <div class="d-flex align-items-stretch" style="height: 100%;">
                            <button class="btn btn-outline-primary btn-sm rental-btn w-100 h-100"
                                    data-id="${equip.equipmentId}">
                                <i class="bi bi-box-seam"></i> 대여 신청
                            </button>
                        </div>
                    </div>
                    
                </div>
            </div>
        `);
        container.append(card);
    });
}

$(document).on("click", ".rental-btn", function() {
    const equipmentId = $(this).data("id");
    $("#modalEquipmentId").val(equipmentId);

    // 오늘 날짜를 기본값으로 설정
    const today = new Date().toISOString().split("T")[0];
    $("#rentalStartDate").val(today);
    $("#rentalEndDate").val(today);

    // 모달 띄우기
    const rentalModal = new bootstrap.Modal(document.getElementById('rentalModal'));
    rentalModal.show();
});

$("#submitRental").on("click", function() {
    const equipmentId = $("#modalEquipmentId").val();
    const quantity = parseInt($("#rentalQuantity").val(), 10);
    const startDate = $("#rentalStartDate").val();
    const endDate = $("#rentalEndDate").val();
    const rentalReason = $("#rentalReason").val();

    if (!startDate || !endDate || !rentalReason || quantity <= 0) {
        alert("모든 항목을 올바르게 입력해주세요.");
        return;
    }

    const payload = {
        equipmentId: parseInt(equipmentId, 10),
        quantity,
        startDate,
        endDate,
        rentalReason
    };

    $.ajax({
        url: "/api/v1/rentals",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload)
    }).done(function(response) {
        alert(response.message);
        $("#rentalModal").modal("hide"); // jQuery 방식

        // 현재 필터 상태를 그대로 사용해서 리스트만 갱신
        const currentFilters = getFilterValues(filterConfig);
        fetchEquipment(currentFilters);
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
});

$("#rentalModal").on("hidden.bs.modal", function () {
    $("#rentalForm")[0].reset();      // form 요소 초기화
    $("#modalEquipmentId").val("");    // hidden input 초기화
});