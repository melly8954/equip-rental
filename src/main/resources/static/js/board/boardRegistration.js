$(document).ready(function () {
    // 파일 선택 시 미리보기
    $('#files').on('change', function() {
        const files = Array.from(this.files);
        const $previewList = $('#filePreviewList');
        $previewList.empty();

        files.forEach(file => {
            const listItem = $('<li>')
                .addClass('list-group-item d-flex justify-content-between align-items-center')
                .text(file.name);

            const removeBtn = $('<button>')
                .addClass('btn btn-sm btn-danger')
                .text('삭제')
                .on('click', function() {
                    const dt = new DataTransfer();
                    Array.from($('#files')[0].files)
                        .filter(f => f.name !== file.name)
                        .forEach(f => dt.items.add(f));
                    $('#files')[0].files = dt.files;
                    listItem.remove();
                });

            listItem.append(removeBtn);
            $previewList.append(listItem);
        });
    });

    // 등록 버튼 클릭 (AJAX 등에서 사용)
    $('#submitBtn').on('click', function() {
        const boardType = $('#boardType').val();
        const title = $('#title').val();
        const content = $('#content').val();
        const files = $('#files')[0].files;

        // FormData 객체 생성
        const formData = new FormData();
        const data = JSON.stringify({ boardType, title, content });

        // JSON 형태의 data를 Blob으로 만들어서 'data'로 추가
        formData.append('data', new Blob([data], { type: "application/json" }));

        // 파일 추가
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }

        $.ajax({
            url: '/api/v1/boards',
            method: 'POST',
            data: formData,
            processData: false,  // jQuery가 데이터를 문자열로 변환하지 않도록
            contentType: false,  // multipart/form-data로 전송
        }).done(function(response) {
            showSnackbar(response.message);
            window.location.href = '/board';
        }).fail(function(jqXHR) {
            handleServerError(jqXHR);
        })
    });
});