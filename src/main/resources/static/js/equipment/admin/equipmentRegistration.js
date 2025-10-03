$(document).ready(function () {
    const category = $('#equipmentCategory');
    const subCategory = $('#equipmentSubCategory');

    // 카테고리 전체 조회
    $.getJSON('/api/v1/categories', function(categories) {
        categories.data.forEach(cat => {
            category.append(`<option value="${cat.categoryId}">${cat.label}</option>`);
        });
    });

    // 카테고리 변경 시 서브카테고리 조회
    category.on('change', function() {
        const categoryId = $(this).val();
        subCategory.empty().append('<option value="">선택</option>');

        if (!categoryId) return;

        $.getJSON(`/api/v1/categories/${categoryId}/sub-categories`, function(subCategories) {
            subCategories.data.forEach(sub => {
                subCategory.append(`<option value="${sub.subCategoryId}">${sub.label}</option>`);
            });
        });
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
    const subCategoryId = $('#equipmentSubCategory').val();
    const model = $('#model').val();
    const stock = $('#stock').val();
    const $files = $('#files');

    if (!subCategoryId || !model || !stock) {
        showSnackbar('모든 필드를 입력해주세요.');
        return;
    }

    const formData = new FormData();

    // JSON 데이터
    const data = {
        subCategoryId: subCategoryId,
        model: model,
        stock: stock
    };
    console.log(data);
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
        showSnackbar(response.message);
        window.location.href = '/admin/equipment/list';
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    });
}

// 장비 등록 폼 초기화
function registerReset() {
    // select 초기화
    $('#equipmentCategory').val('');
    $('#equipmentSubCategory').empty().append('<option value="">선택</option>');

    // input 초기화
    $('#model').val('');
    $('#stock').val(1);

    // 파일 초기화
    const $files = $('#files');
    $files.val('');
    $('#filePreviewList').empty();
    $('#filePreviewArea').hide();
}