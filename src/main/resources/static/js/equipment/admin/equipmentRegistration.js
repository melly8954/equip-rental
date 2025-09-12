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

$(document).ready(function () {
    // 카테고리 변경 시 서브카테고리 옵션 갱신
    $('#equipmentCategory').on('change', function() {
        const category = $(this).val();
        const $subCategory = $('#equipmentSubCategory');
        $subCategory.empty().append('<option value="">선택</option>');

        if (category && subCategoryMap[category]) {
            subCategoryMap[category].forEach(sub => {
                $subCategory.append(`<option value="${sub}">${sub}</option>`);
            });
        }
    });

    // 파일 선택 시 미리보기 리스트 렌더링
    $('#files').on('change', function() {
        const files = Array.from(this.files);
        const $previewArea = $('#filePreviewArea');
        const $previewList = $('#filePreviewList');

        $previewList.empty(); // 기존 리스트 초기화

        if (files.length === 0) {
            $previewArea.hide();
            return;
        }

        files.forEach(file => {
            const listItem = $('<li>')
                .addClass('list-group-item d-flex justify-content-between align-items-center')
                .text(file.name);

            // 삭제 버튼
            const removeBtn = $('<button>')
                .addClass('btn btn-sm btn-danger')
                .text('삭제')
                .on('click', function() {
                    // 선택된 파일 제거
                    const dt = new DataTransfer();
                    Array.from($('#files')[0].files)
                        .filter(f => f.name !== file.name)
                        .forEach(f => dt.items.add(f));
                    $('#files')[0].files = dt.files;

                    listItem.remove();
                    if ($('#files')[0].files.length === 0) {
                        $previewArea.hide();
                    }
                });

            listItem.append(removeBtn);
            $previewList.append(listItem);
        });

        $previewArea.show();
    });
});

function registerEquipment() {
    const $category = $('#equipmentCategory');
    const $subCategory = $('#equipmentSubCategory');
    const $model = $('#model');
    const $stock = $('#stock');
    const $files = $('#files');

    if (!$category.val() || !$subCategory.val() || !$model.val() || !$stock.val()) {
        alert('모든 필드를 입력해주세요.');
        return;
    }

    const formData = new FormData();

    // JSON 데이터
    const data = {
        category: $category.val(),
        subCategory: $subCategory.val(),
        model: $model.val(),
        stock: $stock.val()
    };

    // JSON을 Blob으로 만들어서 FormData에 추가
    const jsonBlob = new Blob([JSON.stringify(data)], { type: "application/json" });
    formData.append("data", jsonBlob);

    // 파일 추가
    const files = $files[0].files;
    for (let i = 0; i < files.length; i++) {
        formData.append("files", files[i]);
    }

    // AJAX 요청
    $.ajax({
        url: '/api/v1/equipments',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false
    }).done(function(response) {
        alert(response.message);
        window.location.href = '/admin/equipment/list';
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    });
}