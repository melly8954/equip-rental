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